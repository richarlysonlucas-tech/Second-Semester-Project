package jogo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import estado.DadosDoJogo;
import estado.EstadoJogo;
import estado.GameStateController;
import estado.Resultado;
import estado.ResultadoBatalha;
import portas.BatalhaEmCurso;
import portas.ComandoBatalha;
import portas.EncontroDetectado;
import portas.EncontroViaJson;
import portas.EventoDoJogo;
import portas.FabricaDeBatalha;
import portas.FonteDeEncontros;
import portas.OpcaoDoJogo;

/**
 * Liga o cérebro (GameStateController) aos módulos de encontro e de batalha.
 *
 * É a única classe que o servidor/front-end precisa chamar: andar, agir, comandos de batalha,
 * hub, salvar e carregar. Cada chamada devolve uma {@link RespostaDoJogo}; erros de uso ou falhas
 * dos módulos viram resposta com ok=false, sem derrubar o jogo e sem deixá-lo num estado quebrado.
 */
public class OrquestradorDoJogo {
    /** Prefixo da flag que marca um gatilho de uso único como já resolvido. */
    public static final String FLAG_RESOLVIDO = "resolvido:";

    private final GameStateController controller;
    private final FonteDeEncontros fonteDeEncontros;
    private final FabricaDeBatalha fabricaDeBatalha;
    private final String jogadorId;
    private final Set<String> origensUnicas = new HashSet<String>();

    private boolean turnoInimigoAutomatico;
    private BatalhaEmCurso batalha;
    private EncontroDetectado encontroAtual;

    public OrquestradorDoJogo(GameStateController controller, FonteDeEncontros fonteDeEncontros,
            FabricaDeBatalha fabricaDeBatalha, String jogadorId) {
        if (controller == null || fonteDeEncontros == null || fabricaDeBatalha == null) {
            throw new IllegalArgumentException("controller, fonteDeEncontros e fabricaDeBatalha são obrigatórios.");
        }
        if (jogadorId == null || jogadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("jogadorId é obrigatório.");
        }
        this.controller = controller;
        this.fonteDeEncontros = fonteDeEncontros;
        this.fabricaDeBatalha = fabricaDeBatalha;
        this.jogadorId = jogadorId;
    }

    // ------------------------------------------------------------ configuração

    /**
     * Declara que encontros com esta origem (ex.: "GATILHO_BAU", "PROXIMIDADE_LOBISOMEM") só valem
     * uma vez: depois de vencidos, deixam de disparar. Fica registrado no save como "resolvido:ORIGEM".
     */
    public void definirOrigemUnica(String origem) {
        if (origem != null && !origem.trim().isEmpty()) {
            origensUnicas.add(origem);
        }
    }

    /**
     * Se ligado, o turno do inimigo roda sozinho logo depois da ação do jogador.
     * Desligado (padrão), o front envia ComandoBatalha.turnoInimigo() quando quiser,
     * por exemplo depois de animar o golpe do jogador.
     */
    public void setTurnoInimigoAutomatico(boolean automatico) {
        this.turnoInimigoAutomatico = automatico;
    }

    // ------------------------------------------------------------ consultas

    public EstadoJogo getEstado() {
        return controller.getEstado();
    }

    public DadosDoJogo getDados() {
        return controller.getDados();
    }

    public boolean haBatalhaEmCurso() {
        return batalha != null;
    }

    public List<OpcaoDoJogo> opcoesDeAtaque(String categoria) {
        if (batalha == null) {
            return Collections.<OpcaoDoJogo>emptyList();
        }
        try {
            List<OpcaoDoJogo> lista = batalha.listarAtaques(categoria);
            return lista == null ? Collections.<OpcaoDoJogo>emptyList() : lista;
        } catch (RuntimeException e) {
            return Collections.<OpcaoDoJogo>emptyList();
        }
    }

    public List<OpcaoDoJogo> opcoesDeItens() {
        if (batalha == null) {
            return Collections.<OpcaoDoJogo>emptyList();
        }
        try {
            List<OpcaoDoJogo> lista = batalha.listarItens();
            return lista == null ? Collections.<OpcaoDoJogo>emptyList() : lista;
        } catch (RuntimeException e) {
            return Collections.<OpcaoDoJogo>emptyList();
        }
    }

    // ------------------------------------------------------------ sessão

    public RespostaDoJogo novoJogo(String nomeHeroi, String classeHeroi, int vidaMaxima) {
        return converter(controller.novoJogo(nomeHeroi, classeHeroi, vidaMaxima));
    }

    public RespostaDoJogo salvar(int slot) {
        return converter(controller.salvar(slot));
    }

    public RespostaDoJogo carregar(int slot) {
        Resultado r = controller.carregar(slot);
        if (r.isOk()) {
            batalha = null;
            encontroAtual = null;
        }
        return converter(r);
    }

    public RespostaDoJogo voltarAoMenu() {
        Resultado r = controller.voltarAoMenu();
        if (r.isOk()) {
            batalha = null;
            encontroAtual = null;
        }
        return converter(r);
    }

    // ------------------------------------------------------------ exploração

    /**
     * O front chama a cada movimento (pode ser a cada frame). O encontro só é consultado
     * quando o jogador andou um passo completo.
     */
    public RespostaDoJogo mover(double x, double y, String areaId) {
        if (controller.getEstado() != EstadoJogo.EXPLORACAO) {
            return erro("Só é possível andar durante a exploração (estado atual: " + controller.getEstado() + ").");
        }
        if (Double.isNaN(x) || Double.isInfinite(x) || Double.isNaN(y) || Double.isInfinite(y)
                || areaId == null || areaId.trim().isEmpty()) {
            return erro("Posição inválida.");
        }
        if (!controller.registrarMovimento(x, y, areaId)) {
            return RespostaDoJogo.ok(controller.getEstado(), "Movimento registrado.");
        }
        return verificarEncontro(x, y, areaId, Collections.<String>emptyList());
    }

    /**
     * Ação explícita do jogador na posição atual (abrir baú, interagir...). Não passa pelo filtro de passo,
     * porque foi o jogador quem pediu.
     */
    public RespostaDoJogo agir(String nomeDaAcao) {
        if (controller.getEstado() != EstadoJogo.EXPLORACAO) {
            return erro("Só é possível agir durante a exploração (estado atual: " + controller.getEstado() + ").");
        }
        if (nomeDaAcao == null || nomeDaAcao.trim().isEmpty()) {
            return erro("Nome da ação é obrigatório.");
        }
        DadosDoJogo d = controller.getDados();
        return verificarEncontro(d.getPosicaoX(), d.getPosicaoY(), d.getAreaId(),
            Collections.singletonList(nomeDaAcao));
    }

    private RespostaDoJogo verificarEncontro(double x, double y, String areaId, List<String> acoes) {
        String json;
        try {
            json = fonteDeEncontros.verificarPresenca(jogadorId, x, y, areaId, acoes);
        } catch (RuntimeException e) {
            return erro("Falha no módulo de encontros: " + e.getMessage());
        }

        EncontroDetectado encontro;
        try {
            encontro = EncontroViaJson.ler(json);
        } catch (IllegalArgumentException e) {
            return erro("Resposta inválida do módulo de encontros: " + e.getMessage());
        }
        if (encontro == null) {
            return RespostaDoJogo.ok(controller.getEstado(), "Nenhum encontro.");
        }
        if (origensUnicas.contains(encontro.getOrigem())
                && controller.jaResolvido(FLAG_RESOLVIDO + encontro.getOrigem())) {
            return RespostaDoJogo.ok(controller.getEstado(),
                "Encontro já resolvido anteriormente (" + encontro.getOrigem() + ").");
        }
        return iniciarBatalha(encontro);
    }

    // ------------------------------------------------------------ batalha

    private RespostaDoJogo iniciarBatalha(EncontroDetectado encontro) {
        Resultado entrada = controller.iniciarBatalha(encontro.getInimigoId(), encontro.getOrigem());
        if (!entrada.isOk()) {
            return erro(entrada.getMensagem());
        }

        BatalhaEmCurso nova;
        List<EventoDoJogo> eventos;
        try {
            nova = fabricaDeBatalha.criar(controller.getDados(), encontro);
            if (nova == null) {
                abortarBatalha();
                return erro("Não foi possível montar a batalha contra " + encontro.getInimigoNome() + ".");
            }
            eventos = nova.iniciar();
        } catch (RuntimeException e) {
            abortarBatalha();
            return erro("Falha ao montar a batalha: " + e.getMessage());
        }
        if (!nova.foiIniciada()) {
            abortarBatalha();
            return erro("A batalha não pôde começar.", eventos);
        }

        batalha = nova;
        encontroAtual = encontro;

        Map<String, Object> inimigo = new LinkedHashMap<String, Object>();
        inimigo.put("id", encontro.getInimigoId());
        inimigo.put("nome", encontro.getInimigoNome());
        inimigo.put("nivel", Integer.valueOf(encontro.getInimigoNivel()));

        RespostaDoJogo r = RespostaDoJogo.ok(controller.getEstado(),
            "Batalha iniciada contra " + encontro.getInimigoNome() + ".", eventos);
        r.extra("inimigo", inimigo);
        r.extra("origem", encontro.getOrigem());
        informarSituacaoDaBatalha(r);
        return r;
    }

    /** Executa um comando do jogador (ou o turno do inimigo) na batalha em andamento. */
    public RespostaDoJogo comandoDeBatalha(ComandoBatalha comando) {
        if (comando == null) {
            return erro("Comando de batalha não informado.");
        }
        if (controller.getEstado() != EstadoJogo.BATALHA || batalha == null) {
            return erro("Não há batalha em andamento (estado atual: " + controller.getEstado() + ").");
        }

        List<EventoDoJogo> eventos = new ArrayList<EventoDoJogo>();
        try {
            eventos.addAll(nuncaNulo(batalha.executar(comando)));
            if (turnoInimigoAutomatico && !batalha.terminou() && batalha.ehTurnoInimigo()) {
                eventos.addAll(nuncaNulo(batalha.executar(ComandoBatalha.turnoInimigo())));
            }
        } catch (RuntimeException e) {
            return erro("Falha no motor de batalha: " + e.getMessage(), eventos);
        }

        if (batalha.terminou()) {
            return finalizarBatalha(eventos);
        }
        RespostaDoJogo r = RespostaDoJogo.ok(controller.getEstado(), "Comando executado.", eventos);
        informarSituacaoDaBatalha(r);
        return r;
    }

    private RespostaDoJogo finalizarBatalha(List<EventoDoJogo> eventos) {
        EncontroDetectado encontro = encontroAtual;
        ResultadoBatalha resultado;
        int vida;
        int fragmentos = 0;
        String flag = null;
        try {
            resultado = batalha.resultado();
            vida = batalha.vidaRestanteDoHeroi();
            if (resultado == ResultadoBatalha.VITORIA) {
                fragmentos = Math.max(0, batalha.fragmentosGanhos());
                flag = batalha.flagDerrotado();
            }
        } catch (RuntimeException e) {
            abortarBatalha();
            return erro("O motor de batalha não informou o resultado: " + e.getMessage(), eventos);
        }
        if (resultado == null) {
            abortarBatalha();
            return erro("O motor de batalha terminou sem informar o resultado.", eventos);
        }

        DadosDoJogo atual = controller.getDados();
        if (resultado == ResultadoBatalha.DERROTA) {
            vida = 0;
        }
        vida = Math.max(0, Math.min(vida, atual.getVidaMaxima()));

        Resultado fim = controller.encerrarBatalha(resultado, fragmentos, flag, vida);
        if (!fim.isOk()) {
            abortarBatalha();
            return erro(fim.getMensagem(), eventos);
        }
        if (resultado == ResultadoBatalha.VITORIA && encontro != null
                && origensUnicas.contains(encontro.getOrigem())) {
            controller.marcarFlag(FLAG_RESOLVIDO + encontro.getOrigem());
        }
        batalha = null;
        encontroAtual = null;

        RespostaDoJogo r = RespostaDoJogo.ok(controller.getEstado(), fim.getMensagem(), eventos);
        r.extra("resultadoBatalha", resultado.name());
        r.extra("fragmentosGanhos", Integer.valueOf(fragmentos));
        r.extra("vidaRestante", Integer.valueOf(vida));
        return r;
    }

    /** Cancela uma batalha que não pôde acontecer, sem recompensa e sem alterar a vida. */
    private void abortarBatalha() {
        DadosDoJogo d = controller.getDados();
        if (controller.getEstado() == EstadoJogo.BATALHA && d != null) {
            controller.encerrarBatalha(ResultadoBatalha.FUGA, 0, null, d.getVidaAtual());
        }
        batalha = null;
        encontroAtual = null;
    }

    private void informarSituacaoDaBatalha(RespostaDoJogo r) {
        try {
            r.extra("menu", batalha.menuAtual());
            r.extra("turno", batalha.ehTurnoInimigo() ? "INIMIGO" : "JOGADOR");
        } catch (RuntimeException e) {
            // A situação é só informativa: se o módulo falhar aqui, a resposta segue sem ela.
        }
    }

    // ------------------------------------------------------------ hub

    public RespostaDoJogo entrarNoHub() {
        return converter(controller.entrarNoHub());
    }

    public RespostaDoJogo sairDoHub() {
        return converter(controller.sairDoHub());
    }

    public RespostaDoJogo purificarFragmentos() {
        return converter(controller.purificarFragmentos());
    }

    // ------------------------------------------------------------ internos

    private RespostaDoJogo converter(Resultado r) {
        return r.isOk()
            ? RespostaDoJogo.ok(controller.getEstado(), r.getMensagem())
            : RespostaDoJogo.erro(controller.getEstado(), r.getMensagem());
    }

    private RespostaDoJogo erro(String mensagem) {
        return RespostaDoJogo.erro(controller.getEstado(), mensagem);
    }

    private RespostaDoJogo erro(String mensagem, List<EventoDoJogo> eventos) {
        return RespostaDoJogo.erro(controller.getEstado(), mensagem, eventos);
    }

    private static List<EventoDoJogo> nuncaNulo(List<EventoDoJogo> lista) {
        return lista == null ? Collections.<EventoDoJogo>emptyList() : lista;
    }
}
