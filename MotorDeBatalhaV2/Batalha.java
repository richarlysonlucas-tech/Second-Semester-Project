package batalha;

import batalha.contratos.AcaoInimiga;
import batalha.contratos.Combatente;
import batalha.contratos.EventoBatalha;
import batalha.contratos.IAInimigo;
import batalha.contratos.OpcaoAcao;
import batalha.contratos.ResultadoAcao;
import batalha.contratos.ResultadoFuga;
import batalha.contratos.SistemaAtaques;
import batalha.contratos.SistemaFuga;
import batalha.contratos.SistemaItens;
import batalha.contratos.TipoAtaque;
import batalha.contratos.TipoEvento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Motor de batalha por turnos do Ousia.
 *
 * <p><b>Responsabilidade:</b> coordenar o fluxo do combate (turnos, menus, estado final),
 * encaminhar as ações aos módulos externos e interpretar os resultados.
 * Não calcula dano, não cria personagens, não controla inventário e não implementa IA.</p>
 *
 * <p><b>Independência de interface:</b> esta classe não usa Scanner, System.in nem System.out.
 * Todo método de ação devolve uma lista de {@link EventoBatalha} com o que aconteceu naquela chamada.</p>
 *
 * <p><b>Fluxo:</b> {@link #iniciar()} → jogador age (menus + ação) → se a ação consumiu o turno,
 * o turno passa ao inimigo → a interface chama {@link #executarTurnoInimigo()} → turno volta ao jogador.
 * O turno do inimigo é um passo separado para que a interface 2D possa animar a ação do jogador
 * antes de mostrar a resposta do inimigo.</p>
 *
 * <p>Ações inválidas (fora do turno, menu errado, batalha encerrada...) nunca lançam exceção:
 * devolvem um evento {@link TipoEvento#ACAO_INVALIDA} e não alteram o estado.</p>
 */
public class Batalha {

    private final Combatente jogador;
    private final Combatente inimigo;
    private final SistemaAtaques sistemaAtaques;
    private final SistemaItens sistemaItens;
    private final SistemaFuga sistemaFuga;
    private final IAInimigo iaInimigo;

    private boolean iniciada = false;
    private EstadoBatalha estado = EstadoBatalha.EM_ANDAMENTO;
    private Turno turno = Turno.JOGADOR;
    private MenuBatalha menu = MenuBatalha.PRINCIPAL;

    public Batalha(Combatente jogador, Combatente inimigo,
                   SistemaAtaques sistemaAtaques, SistemaItens sistemaItens,
                   SistemaFuga sistemaFuga, IAInimigo iaInimigo) {
        this.jogador = Objects.requireNonNull(jogador, "jogador");
        this.inimigo = Objects.requireNonNull(inimigo, "inimigo");
        this.sistemaAtaques = Objects.requireNonNull(sistemaAtaques, "sistemaAtaques");
        this.sistemaItens = Objects.requireNonNull(sistemaItens, "sistemaItens");
        this.sistemaFuga = Objects.requireNonNull(sistemaFuga, "sistemaFuga");
        this.iaInimigo = Objects.requireNonNull(iaInimigo, "iaInimigo");
        if (jogador == inimigo) {
            throw new IllegalArgumentException("jogador e inimigo devem ser combatentes diferentes.");
        }
    }

    // ------------------------------------------------------------------ início

    /**
     * Inicia a batalha: estado EM_ANDAMENTO, turno do jogador, menu principal. Só funciona uma vez.
     *
     * <p>A batalha NÃO começa se o jogador, o inimigo ou ambos já estiverem derrotados
     * ({@link Combatente#estaVivo()} falso). Nesse caso devolve um evento
     * {@link TipoEvento#ACAO_INVALIDA} explicando o motivo, não altera nenhum estado e não lança exceção.
     * Depois de uma recusa é possível chamar {@code iniciar()} de novo (por exemplo, se o personagem
     * for revivido). Use {@link #foiIniciada()} para saber se a batalha realmente começou.</p>
     */
    public List<EventoBatalha> iniciar() {
        List<EventoBatalha> ev = new ArrayList<EventoBatalha>();
        if (iniciada) {
            ev.add(invalido("A batalha já foi iniciada."));
            return ev;
        }
        String recusa = motivoDeRecusaDoInicio();
        if (recusa != null) {
            ev.add(invalido(recusa));
            return ev;
        }
        iniciada = true;
        estado = EstadoBatalha.EM_ANDAMENTO;
        turno = Turno.JOGADOR;
        menu = MenuBatalha.PRINCIPAL;
        ev.add(new EventoBatalha(TipoEvento.BATALHA_INICIADA,
                "Batalha iniciada: " + jogador.getNome() + " vs " + inimigo.getNome() + "!"));
        ev.add(new EventoBatalha(TipoEvento.TURNO_JOGADOR, "Turno do jogador."));
        return ev;
    }

    /** @return o motivo da recusa, ou {@code null} se os dois combatentes estão vivos. */
    private String motivoDeRecusaDoInicio() {
        boolean jogadorVivo = jogador.estaVivo();
        boolean inimigoVivo = inimigo.estaVivo();
        if (jogadorVivo && inimigoVivo) {
            return null;
        }
        if (!jogadorVivo && !inimigoVivo) {
            return "Não é possível iniciar a batalha: jogador e inimigo já estão derrotados.";
        }
        if (!jogadorVivo) {
            return "Não é possível iniciar a batalha: o jogador já está derrotado.";
        }
        return "Não é possível iniciar a batalha: o inimigo já está derrotado.";
    }

    // ------------------------------------------------------------ navegação de menus (não consome turno)

    /** Menu principal → menu de ataques. */
    public List<EventoBatalha> abrirMenuAtaques() {
        return abrirMenu(MenuBatalha.ATAQUES);
    }

    /** Menu principal → mochila. */
    public List<EventoBatalha> abrirMochila() {
        return abrirMenu(MenuBatalha.MOCHILA);
    }

    /** Submenu (ataques ou mochila) → menu principal, sem consumir o turno. */
    public List<EventoBatalha> voltarMenuPrincipal() {
        List<EventoBatalha> ev = new ArrayList<EventoBatalha>();
        if (!jogadorPodeAgir(ev)) {
            return ev;
        }
        if (menu == MenuBatalha.PRINCIPAL) {
            ev.add(invalido("Já está no menu principal."));
            return ev;
        }
        menu = MenuBatalha.PRINCIPAL;
        ev.add(new EventoBatalha(TipoEvento.MENU_ALTERADO, "Menu principal."));
        return ev;
    }

    private List<EventoBatalha> abrirMenu(MenuBatalha destino) {
        List<EventoBatalha> ev = new ArrayList<EventoBatalha>();
        if (!jogadorPodeAgir(ev) || !menuEh(MenuBatalha.PRINCIPAL, ev)) {
            return ev;
        }
        menu = destino;
        ev.add(new EventoBatalha(TipoEvento.MENU_ALTERADO, "Menu: " + destino + "."));
        return ev;
    }

    // ------------------------------------------------------------------ ações do jogador

    /** No menu de ataques: executa o ataque/habilidade escolhido pelo módulo de ataques. */
    public List<EventoBatalha> escolherAtaque(TipoAtaque tipo, String idAtaque) {
        List<EventoBatalha> ev = new ArrayList<EventoBatalha>();
        if (!jogadorPodeAgir(ev) || !menuEh(MenuBatalha.ATAQUES, ev)) {
            return ev;
        }
        if (tipo == null || idVazio(idAtaque)) {
            ev.add(invalido("Ataque não informado."));
            return ev;
        }
        ResultadoAcao r = sistemaAtaques.executar(jogador, inimigo, tipo, idAtaque);
        return processarAcaoJogador(ev, r, "ataques");
    }

    /** Na mochila: usa um item através do módulo de itens. */
    public List<EventoBatalha> usarItem(String idItem) {
        List<EventoBatalha> ev = new ArrayList<EventoBatalha>();
        if (!jogadorPodeAgir(ev) || !menuEh(MenuBatalha.MOCHILA, ev)) {
            return ev;
        }
        if (idVazio(idItem)) {
            ev.add(invalido("Item não informado."));
            return ev;
        }
        ResultadoAcao r = sistemaItens.usar(jogador, inimigo, idItem);
        return processarAcaoJogador(ev, r, "itens");
    }

    /** No menu principal: pede ao módulo de regras a tentativa de fuga. */
    public List<EventoBatalha> tentarFugir() {
        List<EventoBatalha> ev = new ArrayList<EventoBatalha>();
        if (!jogadorPodeAgir(ev) || !menuEh(MenuBatalha.PRINCIPAL, ev)) {
            return ev;
        }
        ResultadoFuga r = sistemaFuga.tentarFuga(jogador, inimigo);
        if (r == null) {
            // Não é o mesmo que NAO_PERMITIDA: é um erro do módulo de fuga e não pode ficar escondido.
            ev.add(erroIntegracao("O módulo de fuga devolveu resultado nulo; a tentativa foi ignorada."));
            return ev;
        }
        if (r == ResultadoFuga.SUCESSO) {
            estado = EstadoBatalha.FUGA;
            ev.add(new EventoBatalha(TipoEvento.FUGA, jogador.getNome() + " fugiu da batalha!"));
            ev.add(new EventoBatalha(TipoEvento.BATALHA_ENCERRADA, "Batalha encerrada: " + estado + "."));
        } else if (r == ResultadoFuga.FALHOU) {
            ev.add(new EventoBatalha(TipoEvento.FUGA, "A fuga falhou!"));
            concluirAcaoDoJogador(ev);
        } else {
            ev.add(invalido("Não é possível fugir desta batalha."));
        }
        return ev;
    }

    /**
     * Interpreta o resultado de uma ação do jogador. Resultado nulo NUNCA é sucesso:
     * gera ERRO_INTEGRACAO, não consome turno e não altera o estado.
     */
    private List<EventoBatalha> processarAcaoJogador(List<EventoBatalha> ev, ResultadoAcao r, String modulo) {
        if (r == null) {
            ev.add(erroIntegracao("O módulo de " + modulo + " devolveu resultado nulo; a ação foi ignorada."));
            return ev;
        }
        adicionarEventos(ev, r, modulo);
        if (r.isValida() && r.consomeTurno()) {
            concluirAcaoDoJogador(ev);
        }
        // Ação inválida ou que não consome turno: continua no mesmo menu, mesmo turno.
        return ev;
    }

    /** Copia os eventos do resultado para a lista de saída, descartando (e avisando sobre) eventos nulos. */
    private void adicionarEventos(List<EventoBatalha> ev, ResultadoAcao r, String modulo) {
        boolean havianulo = false;
        for (EventoBatalha e : r.getEventos()) {
            if (e == null) {
                havianulo = true;
            } else {
                ev.add(e);
            }
        }
        if (havianulo) {
            ev.add(erroIntegracao("O módulo de " + modulo + " devolveu um evento nulo; ele foi descartado."));
        }
    }

    private void concluirAcaoDoJogador(List<EventoBatalha> ev) {
        if (verificarFim(ev)) {
            return;
        }
        turno = Turno.INIMIGO;
        menu = MenuBatalha.PRINCIPAL;
        ev.add(new EventoBatalha(TipoEvento.TURNO_INIMIGO, "Turno do inimigo."));
    }

    // ------------------------------------------------------------------ turno do inimigo

    /**
     * Executa o turno do inimigo: consulta a IA, encaminha a ação ao módulo de ataques,
     * verifica o fim da batalha e devolve o turno ao jogador. Só vale durante o turno do inimigo.
     *
     * <p><b>Regra de segurança:</b> se a IA devolver {@code null}, uma ação sem identificador ou um ataque
     * que não está entre as opções do inimigo, ou se o módulo de ataques devolver resultado nulo ou
     * recusar a ação, o motor emite {@link TipoEvento#ERRO_INTEGRACAO} e o inimigo <b>perde o turno</b>:
     * a vez volta ao jogador. Assim o turno do inimigo nunca fica travado, e o erro de integração
     * continua visível nos eventos. O estado da batalha (vitória/derrota) segue sendo verificado.</p>
     */
    public List<EventoBatalha> executarTurnoInimigo() {
        List<EventoBatalha> ev = new ArrayList<EventoBatalha>();
        if (!iniciada) {
            ev.add(invalido("A batalha ainda não foi iniciada."));
            return ev;
        }
        if (estado != EstadoBatalha.EM_ANDAMENTO) {
            ev.add(invalido("A batalha já terminou."));
            return ev;
        }
        if (turno != Turno.INIMIGO) {
            ev.add(invalido("Não é o turno do inimigo."));
            return ev;
        }

        executarAcaoDoInimigo(ev);

        if (verificarFim(ev)) {
            return ev;
        }
        turno = Turno.JOGADOR;
        menu = MenuBatalha.PRINCIPAL;
        ev.add(new EventoBatalha(TipoEvento.TURNO_JOGADOR, "Turno do jogador."));
        return ev;
    }

    /**
     * Pede a ação à IA, valida e encaminha ao módulo de ataques. Qualquer falha de integração
     * faz o inimigo perder o turno (ver {@link #executarTurnoInimigo()}); o chamador sempre devolve
     * a vez ao jogador depois deste método.
     */
    private void executarAcaoDoInimigo(List<EventoBatalha> ev) {
        AcaoInimiga acao = iaInimigo.escolherAcao(inimigo, jogador);
        String problema = problemaNaAcaoDaIA(acao);
        if (problema != null) {
            ev.add(erroIntegracao("IA: " + problema + " O inimigo perde o turno."));
            return;
        }
        ResultadoAcao r = sistemaAtaques.executar(inimigo, jogador, acao.getTipo(), acao.getIdAtaque());
        if (r == null) {
            ev.add(erroIntegracao("O módulo de ataques devolveu resultado nulo para a ação do inimigo. O inimigo perde o turno."));
            return;
        }
        adicionarEventos(ev, r, "ataques");
        if (!r.isValida()) {
            ev.add(erroIntegracao("O módulo de ataques recusou a ação escolhida pela IA. O inimigo perde o turno."));
        }
    }

    /** @return descrição do problema encontrado na ação da IA, ou {@code null} se a ação é aceitável. */
    private String problemaNaAcaoDaIA(AcaoInimiga acao) {
        if (acao == null) {
            return "nenhuma ação foi devolvida.";
        }
        if (idVazio(acao.getIdAtaque())) {
            return "a ação veio sem identificador.";
        }
        if (!ataqueDisponivelParaInimigo(acao.getTipo(), acao.getIdAtaque())) {
            return "o ataque '" + acao.getIdAtaque() + "' não está entre as opções " + acao.getTipo() + " do inimigo.";
        }
        return null;
    }

    /**
     * Confere o id escolhido pela IA contra {@code SistemaAtaques.listarAtaques(inimigo, tipo)}.
     * Isso exige que o módulo de ataques saiba listar as opções também do INIMIGO.
     * (Para desligar essa conferência basta fazer este método devolver sempre true: o módulo de ataques
     * continua recusando ids desconhecidos pelo resultado inválido.)
     */
    private boolean ataqueDisponivelParaInimigo(TipoAtaque tipo, String id) {
        for (OpcaoAcao opcao : copiarOpcoes(sistemaAtaques.listarAtaques(inimigo, tipo))) {
            if (opcao.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cópia segura de uma lista de opções vinda de outro módulo: lista nula vira lista vazia,
     * elementos nulos são descartados e o resultado é uma lista nova (não é "janela" para a original).
     */
    private static List<OpcaoAcao> copiarOpcoes(List<OpcaoAcao> origem) {
        List<OpcaoAcao> copia = new ArrayList<OpcaoAcao>();
        if (origem != null) {
            for (OpcaoAcao opcao : origem) {
                if (opcao != null) {
                    copia.add(opcao);
                }
            }
        }
        return Collections.unmodifiableList(copia);
    }

    // ------------------------------------------------------------------ fim de batalha

    /** Se alguém morreu, fixa o estado final. Se ambos morrerem, prevalece DERROTA. */
    private boolean verificarFim(List<EventoBatalha> ev) {
        if (!jogador.estaVivo()) {
            estado = EstadoBatalha.DERROTA;
        } else if (!inimigo.estaVivo()) {
            estado = EstadoBatalha.VITORIA;
        } else {
            return false;
        }
        ev.add(new EventoBatalha(TipoEvento.BATALHA_ENCERRADA, "Batalha encerrada: " + estado + "."));
        return true;
    }

    // ------------------------------------------------------------------ consultas para a interface

    /** @return true se {@link #iniciar()} foi aceito. Antes disso (ou se foi recusado) é false. */
    public boolean foiIniciada() { return iniciada; }

    /** Atenção: antes de iniciar, o estado é EM_ANDAMENTO. Use {@link #foiIniciada()} para distinguir. */
    public EstadoBatalha getEstado() { return estado; }

    public Turno getTurno() { return turno; }

    public MenuBatalha getMenuAtual() { return menu; }

    public boolean ehTurnoJogador() { return iniciada && estado == EstadoBatalha.EM_ANDAMENTO && turno == Turno.JOGADOR; }

    public boolean ehTurnoInimigo() { return iniciada && estado == EstadoBatalha.EM_ANDAMENTO && turno == Turno.INIMIGO; }

    public boolean batalhaAtiva() { return iniciada && estado == EstadoBatalha.EM_ANDAMENTO; }

    public boolean terminou() { return iniciada && estado != EstadoBatalha.EM_ANDAMENTO; }

    public Combatente getJogador() { return jogador; }

    public Combatente getInimigo() { return inimigo; }

    /**
     * Opções para preencher o submenu de ataques (dados vindos do módulo de ataques).
     * Devolve sempre uma CÓPIA somente-leitura: mudanças futuras na lista do módulo não aparecem nela,
     * e quem a recebe não consegue alterar a lista do módulo. Lista nula, tipo nulo ou módulo sem
     * opções resultam em lista vazia; elementos nulos são descartados.
     */
    public List<OpcaoAcao> listarAtaques(TipoAtaque tipo) {
        if (tipo == null) {
            return copiarOpcoes(null);
        }
        return copiarOpcoes(sistemaAtaques.listarAtaques(jogador, tipo));
    }

    /** Opções para preencher a mochila. Mesmas garantias de {@link #listarAtaques(TipoAtaque)}. */
    public List<OpcaoAcao> listarItens() {
        return copiarOpcoes(sistemaItens.listarItens(jogador));
    }

    // ------------------------------------------------------------------ validações internas

    private boolean jogadorPodeAgir(List<EventoBatalha> ev) {
        if (!iniciada) {
            ev.add(invalido("A batalha ainda não foi iniciada."));
            return false;
        }
        if (estado != EstadoBatalha.EM_ANDAMENTO) {
            ev.add(invalido("A batalha já terminou."));
            return false;
        }
        if (turno != Turno.JOGADOR) {
            ev.add(invalido("Não é o turno do jogador."));
            return false;
        }
        return true;
    }

    private boolean menuEh(MenuBatalha esperado, List<EventoBatalha> ev) {
        if (menu != esperado) {
            ev.add(invalido("Ação indisponível no menu atual (" + menu + ")."));
            return false;
        }
        return true;
    }

    private static EventoBatalha invalido(String motivo) {
        return new EventoBatalha(TipoEvento.ACAO_INVALIDA, motivo);
    }

    private static EventoBatalha erroIntegracao(String motivo) {
        return new EventoBatalha(TipoEvento.ERRO_INTEGRACAO, motivo);
    }

    /** Identificador ausente ou só com espaços. */
    private static boolean idVazio(String id) {
        return id == null || id.trim().isEmpty();
    }
}
