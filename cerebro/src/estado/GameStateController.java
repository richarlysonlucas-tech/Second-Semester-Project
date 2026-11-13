package estado;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import save.SaveException;
import save.SaveSystem;

/**
 * O "cérebro" do jogo: guarda o estado atual, o progresso do jogador e decide
 * quais mudanças são permitidas. Não conhece front-end, batalha nem formato de save;
 * só conversa com contratos (SaveSystem) e com tipos próprios (EstadoJogo, DadosDoJogo).
 *
 * Operações inválidas nunca lançam exceção: voltam como Resultado.erro(...) e nada muda.
 */
public class GameStateController {
    /**
     * Distância mínima (em unidades do mapa) que o jogador precisa andar entre duas
     * verificações de encontro. Evita sortear encontro a cada frame.
     */
    public static final double PASSO_MINIMO_ENCONTRO = 16.0;

    private final SaveSystem saveSystem;
    private final Map<EstadoJogo, Set<EstadoJogo>> transicoes;

    private EstadoJogo estado = EstadoJogo.MENU;
    private DadosDoJogo dados;

    private boolean temReferenciaDeMovimento;
    private double referenciaX;
    private double referenciaY;

    private String inimigoDaBatalha;
    private String origemDaBatalha;

    public GameStateController(SaveSystem saveSystem) {
        this.saveSystem = saveSystem;
        this.transicoes = new EnumMap<EstadoJogo, Set<EstadoJogo>>(EstadoJogo.class);
        permitir(EstadoJogo.MENU, EstadoJogo.EXPLORACAO);
        permitir(EstadoJogo.EXPLORACAO, EstadoJogo.BATALHA, EstadoJogo.HUB, EstadoJogo.MENU);
        permitir(EstadoJogo.BATALHA, EstadoJogo.EXPLORACAO, EstadoJogo.GAME_OVER);
        permitir(EstadoJogo.HUB, EstadoJogo.EXPLORACAO, EstadoJogo.MENU);
        permitir(EstadoJogo.GAME_OVER, EstadoJogo.MENU);
    }

    private void permitir(EstadoJogo origem, EstadoJogo... destinos) {
        transicoes.put(origem, new HashSet<EstadoJogo>(Arrays.asList(destinos)));
    }

    // ------------------------------------------------------------ consultas

    public EstadoJogo getEstado() {
        return estado;
    }

    /** Cópia dos dados atuais, ou null se ainda não há jogo em andamento. */
    public DadosDoJogo getDados() {
        return dados == null ? null : dados.copiar();
    }

    public String getInimigoDaBatalha() {
        return inimigoDaBatalha;
    }

    public String getOrigemDaBatalha() {
        return origemDaBatalha;
    }

    public boolean temFlag(String flag) {
        return dados != null && dados.getFlagsMundo().contains(flag);
    }

    // ------------------------------------------------------------ início e fim de sessão

    public Resultado novoJogo(String nomeHeroi, String classeHeroi, int vidaMaxima) {
        if (estado != EstadoJogo.MENU) {
            return Resultado.erro("Novo jogo só pode começar a partir do menu (estado atual: " + estado + ").");
        }
        DadosDoJogo novo = DadosDoJogo.novo(nomeHeroi, classeHeroi, vidaMaxima);
        try {
            novo.validar();
        } catch (IllegalArgumentException e) {
            return Resultado.erro("Dados do herói inválidos: " + e.getMessage());
        }
        dados = novo;
        entrarEm(EstadoJogo.EXPLORACAO);
        // A posição inicial ainda é provisória: a primeira posição real do front vira a referência.
        temReferenciaDeMovimento = false;
        return Resultado.ok("Novo jogo iniciado com " + nomeHeroi + ".");
    }

    public Resultado voltarAoMenu() {
        Resultado r = transicionar(EstadoJogo.MENU);
        if (r.isOk()) {
            limparBatalha();
        }
        return r;
    }

    // ------------------------------------------------------------ exploração

    /**
     * Registra o movimento do jogador (o front pode chamar a cada frame).
     *
     * @return true somente quando vale a pena verificar encontro agora:
     *         está explorando e andou pelo menos PASSO_MINIMO_ENCONTRO desde a última verificação.
     */
    public boolean registrarMovimento(double x, double y, String areaId) {
        if (estado != EstadoJogo.EXPLORACAO || dados == null
                || Double.isNaN(x) || Double.isInfinite(x)
                || Double.isNaN(y) || Double.isInfinite(y)
                || areaId == null || areaId.trim().isEmpty()) {
            return false;
        }
        dados.setPosicao(x, y, areaId);

        if (!temReferenciaDeMovimento) {
            definirReferencia(x, y);
            return false;
        }
        double dx = x - referenciaX;
        double dy = y - referenciaY;
        if (Math.sqrt(dx * dx + dy * dy) >= PASSO_MINIMO_ENCONTRO) {
            definirReferencia(x, y);
            return true;
        }
        return false;
    }

    /**
     * Diz se um gatilho/entidade já foi resolvido (ex.: baú aberto, chefe derrotado).
     * Use antes de chamar o serviço de encontros para não repetir o que já aconteceu.
     */
    public boolean jaResolvido(String flag) {
        return temFlag(flag);
    }

    public Resultado marcarFlag(String flag) {
        if (dados == null) {
            return Resultado.erro("Não há jogo em andamento.");
        }
        if (flag == null || flag.trim().isEmpty()) {
            return Resultado.erro("Flag vazia.");
        }
        dados.getFlagsMundo().add(flag);
        return Resultado.ok("Flag registrada: " + flag);
    }

    public Resultado adicionarItem(String idItem, int quantidade) {
        if (dados == null) {
            return Resultado.erro("Não há jogo em andamento.");
        }
        if (idItem == null || idItem.trim().isEmpty() || quantidade <= 0) {
            return Resultado.erro("Item ou quantidade inválidos.");
        }
        Integer atual = dados.getInventario().get(idItem);
        dados.getInventario().put(idItem, (atual == null ? 0 : atual) + quantidade);
        return Resultado.ok("Item adicionado: " + idItem + " x" + quantidade);
    }

    // ------------------------------------------------------------ batalha

    public Resultado iniciarBatalha(String inimigoId, String origem) {
        if (inimigoId == null || inimigoId.trim().isEmpty()) {
            return Resultado.erro("inimigoId é obrigatório.");
        }
        Resultado r = transicionar(EstadoJogo.BATALHA);
        if (r.isOk()) {
            inimigoDaBatalha = inimigoId;
            origemDaBatalha = origem;
        }
        return r;
    }

    /**
     * Fecha a batalha atual.
     *
     * @param fragmentos       fragmentos ganhos (só contam na VITORIA)
     * @param flagDerrotado    flag a registrar na VITORIA (ex.: "derrotado:REI_SOMBRAS"); pode ser null
     * @param vidaRestante     vida do herói ao fim da batalha
     */
    public Resultado encerrarBatalha(ResultadoBatalha resultado, int fragmentos,
            String flagDerrotado, int vidaRestante) {
        if (estado != EstadoJogo.BATALHA) {
            return Resultado.erro("Não há batalha em andamento (estado atual: " + estado + ").");
        }
        if (resultado == null || fragmentos < 0 || vidaRestante < 0 || vidaRestante > dados.getVidaMaxima()) {
            return Resultado.erro("Dados de encerramento de batalha inválidos.");
        }

        dados.setVidaAtual(vidaRestante);
        EstadoJogo destino;
        String mensagem;
        switch (resultado) {
            case VITORIA:
                dados.setFragmentosColetados(dados.getFragmentosColetados() + fragmentos);
                if (flagDerrotado != null && !flagDerrotado.trim().isEmpty()) {
                    dados.getFlagsMundo().add(flagDerrotado);
                }
                destino = EstadoJogo.EXPLORACAO;
                mensagem = "Vitória! Fragmentos ganhos: " + fragmentos + ".";
                break;
            case FUGA:
                destino = EstadoJogo.EXPLORACAO;
                mensagem = "Você fugiu da batalha.";
                break;
            default:
                destino = EstadoJogo.GAME_OVER;
                mensagem = "Você foi derrotado.";
        }
        entrarEm(destino);
        limparBatalha();
        // Depois de uma batalha, é preciso andar de novo antes de outro sorteio de encontro.
        temReferenciaDeMovimento = false;
        return Resultado.ok(mensagem);
    }

    // ------------------------------------------------------------ hub

    public Resultado entrarNoHub() {
        return transicionar(EstadoJogo.HUB);
    }

    public Resultado sairDoHub() {
        if (estado != EstadoJogo.HUB) {
            return Resultado.erro("Você não está no hub (estado atual: " + estado + ").");
        }
        Resultado r = transicionar(EstadoJogo.EXPLORACAO);
        if (r.isOk()) {
            temReferenciaDeMovimento = false;
        }
        return r;
    }

    /** Move os fragmentos coletados para purificados. Só no hub. */
    public Resultado purificarFragmentos() {
        if (estado != EstadoJogo.HUB) {
            return Resultado.erro("Fragmentos só podem ser purificados no hub (estado atual: " + estado + ").");
        }
        int quantidade = dados.getFragmentosColetados();
        if (quantidade == 0) {
            return Resultado.erro("Não há fragmentos para purificar.");
        }
        dados.setFragmentosPurificados(dados.getFragmentosPurificados() + quantidade);
        dados.setFragmentosColetados(0);
        return Resultado.ok(quantidade + " fragmento(s) purificado(s).");
    }

    // ------------------------------------------------------------ save e load

    /** Só é permitido salvar na exploração ou no hub, nunca no meio de uma batalha. */
    public Resultado salvar(int slot) {
        if (dados == null) {
            return Resultado.erro("Não há jogo em andamento para salvar.");
        }
        if (estado != EstadoJogo.EXPLORACAO && estado != EstadoJogo.HUB) {
            return Resultado.erro("Não é possível salvar agora (estado atual: " + estado + ").");
        }
        DadosDoJogo foto = dados.copiar();
        foto.setEstadoSalvo(estado.name());
        try {
            saveSystem.salvar(slot, foto);
        } catch (SaveException e) {
            return Resultado.erro(e.getMessage());
        }
        dados.setEstadoSalvo(estado.name());
        return Resultado.ok("Jogo salvo no slot " + slot + ".");
    }

    /** Carrega um slot. Não é permitido durante uma batalha. Se falhar, o jogo atual não muda. */
    public Resultado carregar(int slot) {
        if (estado == EstadoJogo.BATALHA) {
            return Resultado.erro("Não é possível carregar durante uma batalha.");
        }
        DadosDoJogo carregados;
        try {
            carregados = saveSystem.carregar(slot);
        } catch (SaveException e) {
            return Resultado.erro(e.getMessage());
        }
        dados = carregados;
        limparBatalha();
        entrarEm(EstadoJogo.HUB.name().equals(carregados.getEstadoSalvo())
            ? EstadoJogo.HUB : EstadoJogo.EXPLORACAO);
        return Resultado.ok("Jogo carregado do slot " + slot + ".");
    }

    // ------------------------------------------------------------ internos

    private Resultado transicionar(EstadoJogo destino) {
        if (!transicoes.get(estado).contains(destino)) {
            return Resultado.erro("Transição não permitida: " + estado + " -> " + destino + ".");
        }
        entrarEm(destino);
        return Resultado.ok("Estado: " + destino + ".");
    }

    private void entrarEm(EstadoJogo novo) {
        estado = novo;
        if (novo == EstadoJogo.EXPLORACAO && dados != null) {
            definirReferencia(dados.getPosicaoX(), dados.getPosicaoY());
        }
    }

    private void definirReferencia(double x, double y) {
        referenciaX = x;
        referenciaY = y;
        temReferenciaDeMovimento = true;
    }

    private void limparBatalha() {
        inimigoDaBatalha = null;
        origemDaBatalha = null;
    }
}
