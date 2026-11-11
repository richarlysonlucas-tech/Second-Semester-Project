package batalha.teste;

import batalha.Batalha;
import batalha.EstadoBatalha;
import batalha.MenuBatalha;
import batalha.Turno;
import batalha.contratos.AcaoInimiga;
import batalha.contratos.Combatente;
import batalha.contratos.EventoBatalha;
import batalha.contratos.IAInimigo;
import batalha.contratos.OpcaoAcao;
import batalha.contratos.ResultadoAcao;
import batalha.contratos.SistemaAtaques;
import batalha.contratos.SistemaFuga;
import batalha.contratos.SistemaItens;
import batalha.contratos.ResultadoFuga;
import batalha.contratos.TipoAtaque;
import batalha.contratos.TipoEvento;
import batalha.simulacao.AtaquesSimulados;
import batalha.simulacao.CombatenteSimulado;
import batalha.simulacao.FugaSimulada;
import batalha.simulacao.IASimulada;
import batalha.simulacao.ItensSimulados;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Testes do motor usando as simulações. Sem JUnit (nenhuma dependência):
 * rode com {@code java -cp out batalha.teste.TestesBatalha}. Sai com código 1 se algo falhar.
 */
public class TestesBatalha {

    private static int ok = 0;
    private static int falhas = 0;

    /** Monta um cenário com as simulações. */
    private static class Cenario {
        final CombatenteSimulado heroi;
        final CombatenteSimulado goblin;
        final AtaquesSimulados ataques;
        final ItensSimulados itens = new ItensSimulados();
        final FugaSimulada fuga;
        final IASimulada ia = new IASimulada();
        final Batalha batalha;

        Cenario(int pvHeroi, int pvGoblin, int dano, ResultadoFuga fugaResultado) {
            heroi = new CombatenteSimulado("Herói", pvHeroi);
            goblin = new CombatenteSimulado("Goblin", pvGoblin);
            ataques = new AtaquesSimulados(dano);
            fuga = new FugaSimulada(fugaResultado);
            batalha = new Batalha(heroi, goblin, ataques, itens, fuga, ia);
        }

        static Cenario padrao() { return new Cenario(100, 100, 10, ResultadoFuga.SUCESSO); }
    }

    public static void main(String[] args) {
        inicioDeBatalha();
        acoesAntesDeIniciarBloqueadas();
        alternanciaDeTurnos();
        selecaoDeAtaques();
        utilizacaoDeItens();
        itemInvalidoNaoConsomeTurno();
        retornoEntreMenusNaoConsomeTurno();
        acaoNoMenuErradoBloqueada();
        fugaComSucesso();
        fugaFalhaConsomeTurno();
        fugaNaoPermitidaNaoConsomeTurno();
        vitoria();
        derrota();
        bloqueioAposEncerramento();
        acaoForaDoTurnoBloqueada();
        multiplasAcoesNoMesmoTurnoBloqueadas();
        integracaoComSistemasExternos();
        validacaoDoInicioDaBatalha();
        resultadosNulosDosModulos();
        acoesInvalidasDaIA();
        listasDeAtaquesEItens();
        terminalSelecaoDeAtaquesEItens();

        System.out.println("\n" + ok + " verificações OK, " + falhas + " falhas.");
        if (falhas > 0) { System.exit(1); }
    }

    // ---------------------------------------------------------------- testes

    static void inicioDeBatalha() {
        Cenario c = Cenario.padrao();
        List<EventoBatalha> ev = c.batalha.iniciar();
        check("início: estado EM_ANDAMENTO", c.batalha.getEstado() == EstadoBatalha.EM_ANDAMENTO);
        check("início: turno do jogador", c.batalha.getTurno() == Turno.JOGADOR);
        check("início: menu principal", c.batalha.getMenuAtual() == MenuBatalha.PRINCIPAL);
        check("início: evento BATALHA_INICIADA", tem(ev, TipoEvento.BATALHA_INICIADA));
        check("início: segunda chamada recusada", tem(c.batalha.iniciar(), TipoEvento.ACAO_INVALIDA));
    }

    static void acoesAntesDeIniciarBloqueadas() {
        Cenario c = Cenario.padrao();
        check("antes de iniciar: abrir ataques bloqueado", tem(c.batalha.abrirMenuAtaques(), TipoEvento.ACAO_INVALIDA));
        check("antes de iniciar: fugir bloqueado", tem(c.batalha.tentarFugir(), TipoEvento.ACAO_INVALIDA));
        check("antes de iniciar: turno inimigo bloqueado", tem(c.batalha.executarTurnoInimigo(), TipoEvento.ACAO_INVALIDA));
        check("antes de iniciar: batalha não está ativa", !c.batalha.batalhaAtiva());
    }

    static void alternanciaDeTurnos() {
        Cenario c = Cenario.padrao();
        c.batalha.iniciar();
        c.batalha.abrirMenuAtaques();
        c.batalha.escolherAtaque(TipoAtaque.FISICO, "golpe");
        check("alternância: após ataque é turno do inimigo", c.batalha.ehTurnoInimigo());
        List<EventoBatalha> ev = c.batalha.executarTurnoInimigo();
        check("alternância: após inimigo volta ao jogador", c.batalha.ehTurnoJogador());
        check("alternância: evento TURNO_JOGADOR emitido", tem(ev, TipoEvento.TURNO_JOGADOR));
        check("alternância: menu volta ao principal", c.batalha.getMenuAtual() == MenuBatalha.PRINCIPAL);
        check("alternância: IA consultada 1 vez", c.ia.getChamadas() == 1);
    }

    static void selecaoDeAtaques() {
        Cenario c = Cenario.padrao();
        c.batalha.iniciar();
        TipoAtaque[] tipos = TipoAtaque.values();
        for (TipoAtaque t : tipos) {
            c.batalha.abrirMenuAtaques();
            c.batalha.escolherAtaque(t, "id_" + t);
            check("ataque " + t + ": encaminhado ao módulo de ataques", c.ataques.getUltimoTipo() == t);
            c.batalha.executarTurnoInimigo();
        }
        check("ataques: 3 do jogador + 3 do inimigo", c.ataques.getChamadas() == 6);
    }

    static void utilizacaoDeItens() {
        Cenario c = new Cenario(50, 100, 10, ResultadoFuga.SUCESSO);
        c.batalha.iniciar();
        c.batalha.abrirMochila();
        check("item: mochila aberta", c.batalha.getMenuAtual() == MenuBatalha.MOCHILA);
        c.batalha.usarItem("pocao_vida");
        check("item: módulo de itens foi chamado", c.itens.getUsos() == 1);
        check("item: poção curou (efeito é do módulo)", c.heroi.getPv() == 70);
        check("item: consumiu o turno", c.batalha.ehTurnoInimigo());
    }

    static void itemInvalidoNaoConsomeTurno() {
        Cenario c = Cenario.padrao();
        c.batalha.iniciar();
        c.batalha.abrirMochila();
        List<EventoBatalha> ev = c.batalha.usarItem("inexistente");
        check("item inválido: evento ACAO_INVALIDA", tem(ev, TipoEvento.ACAO_INVALIDA));
        check("item inválido: turno continua do jogador", c.batalha.ehTurnoJogador());
        check("item inválido: continua na mochila", c.batalha.getMenuAtual() == MenuBatalha.MOCHILA);
    }

    static void retornoEntreMenusNaoConsomeTurno() {
        Cenario c = Cenario.padrao();
        c.batalha.iniciar();
        c.batalha.abrirMochila();
        c.batalha.voltarMenuPrincipal();
        check("voltar da mochila: menu principal", c.batalha.getMenuAtual() == MenuBatalha.PRINCIPAL);
        check("voltar da mochila: turno preservado", c.batalha.ehTurnoJogador());
        c.batalha.abrirMenuAtaques();
        c.batalha.voltarMenuPrincipal();
        check("voltar dos ataques: menu principal", c.batalha.getMenuAtual() == MenuBatalha.PRINCIPAL);
        check("voltar dos ataques: turno preservado", c.batalha.ehTurnoJogador());
        check("voltar: nenhum sistema externo acionado", c.ataques.getChamadas() == 0 && c.itens.getUsos() == 0);
        check("voltar no menu principal é recusado", tem(c.batalha.voltarMenuPrincipal(), TipoEvento.ACAO_INVALIDA));
    }

    static void acaoNoMenuErradoBloqueada() {
        Cenario c = Cenario.padrao();
        c.batalha.iniciar();
        check("menu errado: atacar direto do principal", tem(c.batalha.escolherAtaque(TipoAtaque.FISICO, "golpe"), TipoEvento.ACAO_INVALIDA));
        check("menu errado: usar item direto do principal", tem(c.batalha.usarItem("pocao"), TipoEvento.ACAO_INVALIDA));
        c.batalha.abrirMenuAtaques();
        check("menu errado: fugir dentro de ataques", tem(c.batalha.tentarFugir(), TipoEvento.ACAO_INVALIDA));
        check("menu errado: nada foi executado", c.ataques.getChamadas() == 0 && c.fuga.getChamadas() == 0);
    }

    static void fugaComSucesso() {
        Cenario c = Cenario.padrao();
        c.batalha.iniciar();
        List<EventoBatalha> ev = c.batalha.tentarFugir();
        check("fuga: estado FUGA", c.batalha.getEstado() == EstadoBatalha.FUGA);
        check("fuga: batalha encerrada", c.batalha.terminou() && !c.batalha.batalhaAtiva());
        check("fuga: evento BATALHA_ENCERRADA", tem(ev, TipoEvento.BATALHA_ENCERRADA));
    }

    static void fugaFalhaConsomeTurno() {
        Cenario c = new Cenario(100, 100, 10, ResultadoFuga.FALHOU);
        c.batalha.iniciar();
        c.batalha.tentarFugir();
        check("fuga falhou: batalha continua", c.batalha.getEstado() == EstadoBatalha.EM_ANDAMENTO);
        check("fuga falhou: turno do inimigo", c.batalha.ehTurnoInimigo());
    }

    static void fugaNaoPermitidaNaoConsomeTurno() {
        Cenario c = new Cenario(100, 100, 10, ResultadoFuga.NAO_PERMITIDA);
        c.batalha.iniciar();
        List<EventoBatalha> ev = c.batalha.tentarFugir();
        check("fuga não permitida: evento ACAO_INVALIDA", tem(ev, TipoEvento.ACAO_INVALIDA));
        check("fuga não permitida: turno do jogador mantido", c.batalha.ehTurnoJogador());
        check("fuga não permitida: batalha continua", c.batalha.batalhaAtiva());
    }

    static void vitoria() {
        Cenario c = new Cenario(100, 10, 10, ResultadoFuga.SUCESSO);
        c.batalha.iniciar();
        c.batalha.abrirMenuAtaques();
        List<EventoBatalha> ev = c.batalha.escolherAtaque(TipoAtaque.FISICO, "golpe");
        check("vitória: estado VITORIA", c.batalha.getEstado() == EstadoBatalha.VITORIA);
        check("vitória: evento BATALHA_ENCERRADA", tem(ev, TipoEvento.BATALHA_ENCERRADA));
        check("vitória: inimigo não age", c.ia.getChamadas() == 0);
    }

    static void derrota() {
        Cenario c = new Cenario(10, 100, 10, ResultadoFuga.SUCESSO);
        c.batalha.iniciar();
        c.batalha.abrirMenuAtaques();
        c.batalha.escolherAtaque(TipoAtaque.FISICO, "golpe");
        check("derrota: ainda em andamento antes do inimigo", c.batalha.getEstado() == EstadoBatalha.EM_ANDAMENTO);
        c.batalha.executarTurnoInimigo();
        check("derrota: estado DERROTA", c.batalha.getEstado() == EstadoBatalha.DERROTA);
    }

    static void bloqueioAposEncerramento() {
        Cenario c = new Cenario(100, 10, 10, ResultadoFuga.SUCESSO);
        c.batalha.iniciar();
        c.batalha.abrirMenuAtaques();
        c.batalha.escolherAtaque(TipoAtaque.FISICO, "golpe");
        int chamadas = c.ataques.getChamadas();
        check("encerrada: abrir ataques bloqueado", tem(c.batalha.abrirMenuAtaques(), TipoEvento.ACAO_INVALIDA));
        check("encerrada: abrir mochila bloqueado", tem(c.batalha.abrirMochila(), TipoEvento.ACAO_INVALIDA));
        check("encerrada: fugir bloqueado", tem(c.batalha.tentarFugir(), TipoEvento.ACAO_INVALIDA));
        check("encerrada: turno inimigo bloqueado", tem(c.batalha.executarTurnoInimigo(), TipoEvento.ACAO_INVALIDA));
        check("encerrada: estado final preservado", c.batalha.getEstado() == EstadoBatalha.VITORIA);
        check("encerrada: nenhum módulo acionado de novo", c.ataques.getChamadas() == chamadas);
    }

    static void acaoForaDoTurnoBloqueada() {
        Cenario c = Cenario.padrao();
        c.batalha.iniciar();
        check("fora do turno: turno do inimigo recusado durante turno do jogador",
                tem(c.batalha.executarTurnoInimigo(), TipoEvento.ACAO_INVALIDA));
        c.batalha.abrirMenuAtaques();
        c.batalha.escolherAtaque(TipoAtaque.FISICO, "golpe");
        check("fora do turno: abrir ataques durante turno do inimigo", tem(c.batalha.abrirMenuAtaques(), TipoEvento.ACAO_INVALIDA));
        check("fora do turno: mochila durante turno do inimigo", tem(c.batalha.abrirMochila(), TipoEvento.ACAO_INVALIDA));
        check("fora do turno: fugir durante turno do inimigo", tem(c.batalha.tentarFugir(), TipoEvento.ACAO_INVALIDA));
    }

    static void multiplasAcoesNoMesmoTurnoBloqueadas() {
        Cenario c = Cenario.padrao();
        c.batalha.iniciar();
        c.batalha.abrirMenuAtaques();
        c.batalha.escolherAtaque(TipoAtaque.FISICO, "golpe");
        List<EventoBatalha> ev = c.batalha.escolherAtaque(TipoAtaque.MAGICO, "bola_de_fogo");
        check("duas ações: segunda recusada", tem(ev, TipoEvento.ACAO_INVALIDA));
        check("duas ações: módulo de ataques chamado só 1 vez", c.ataques.getChamadas() == 1);
    }

    static void integracaoComSistemasExternos() {
        Cenario c = Cenario.padrao();
        c.batalha.iniciar();
        c.batalha.abrirMenuAtaques();
        c.batalha.escolherAtaque(TipoAtaque.MAGICO, "bola_de_fogo");
        check("integração: jogador enviado como usuário", c.ataques.getUltimoUsuario() == c.heroi);
        check("integração: inimigo enviado como alvo", c.ataques.getUltimoAlvo() == c.goblin);
        check("integração: id repassado sem alteração", "bola_de_fogo".equals(c.ataques.getUltimoId()));
        c.batalha.executarTurnoInimigo();
        check("integração: inimigo usa o mesmo módulo, papéis invertidos",
                c.ataques.getUltimoUsuario() == c.goblin && c.ataques.getUltimoAlvo() == c.heroi);
        check("integração: opções do submenu vêm do módulo", !c.batalha.listarAtaques(TipoAtaque.FISICO).isEmpty());
        check("integração: itens da mochila vêm do módulo (6 poções)", c.batalha.listarItens().size() == 6);
    }

    static void validacaoDoInicioDaBatalha() {
        // jogador vivo e inimigo vivo
        Cenario vivos = Cenario.padrao();
        vivos.batalha.iniciar();
        check("validação início: ambos vivos iniciam", vivos.batalha.foiIniciada() && vivos.batalha.batalhaAtiva());

        // jogador derrotado e inimigo vivo
        Cenario jogadorMorto = new Cenario(0, 100, 10, ResultadoFuga.SUCESSO);
        List<EventoBatalha> ev = jogadorMorto.batalha.iniciar();
        check("validação início: jogador derrotado é recusado", tem(ev, TipoEvento.ACAO_INVALIDA));
        check("validação início: jogador derrotado não inicia", !jogadorMorto.batalha.foiIniciada() && !jogadorMorto.batalha.batalhaAtiva());
        check("validação início: mensagem cita o jogador", textoContem(ev, "jogador já está derrotado"));
        check("validação início: estado não muda", jogadorMorto.batalha.getEstado() == EstadoBatalha.EM_ANDAMENTO);

        // jogador vivo e inimigo derrotado
        Cenario inimigoMorto = new Cenario(100, 0, 10, ResultadoFuga.SUCESSO);
        ev = inimigoMorto.batalha.iniciar();
        check("validação início: inimigo derrotado é recusado", tem(ev, TipoEvento.ACAO_INVALIDA));
        check("validação início: inimigo derrotado não inicia", !inimigoMorto.batalha.foiIniciada());
        check("validação início: mensagem cita o inimigo", textoContem(ev, "inimigo já está derrotado"));

        // ambos derrotados
        Cenario ambosMortos = new Cenario(0, 0, 10, ResultadoFuga.SUCESSO);
        ev = ambosMortos.batalha.iniciar();
        check("validação início: ambos derrotados são recusados", tem(ev, TipoEvento.ACAO_INVALIDA));
        check("validação início: ambos derrotados não iniciam", !ambosMortos.batalha.foiIniciada());
        check("validação início: mensagem cita os dois", textoContem(ev, "jogador e inimigo já estão derrotados"));

        // batalha recusada não aceita nenhuma ação
        check("início recusado: abrir ataques bloqueado", tem(jogadorMorto.batalha.abrirMenuAtaques(), TipoEvento.ACAO_INVALIDA));
        check("início recusado: fugir bloqueado", tem(jogadorMorto.batalha.tentarFugir(), TipoEvento.ACAO_INVALIDA));
        check("início recusado: nenhum módulo foi acionado",
                jogadorMorto.ataques.getChamadas() == 0 && jogadorMorto.fuga.getChamadas() == 0 && jogadorMorto.ia.getChamadas() == 0);

        // depois de uma recusa é possível tentar de novo (personagem revivido)
        jogadorMorto.heroi.curar(50);
        jogadorMorto.batalha.iniciar();
        check("início recusado: pode iniciar depois que o jogador for revivido", jogadorMorto.batalha.batalhaAtiva());

        // o motor exige dois combatentes diferentes
        CombatenteSimulado mesmo = new CombatenteSimulado("Mesmo", 10);
        boolean recusou = false;
        try {
            new Batalha(mesmo, mesmo, new AtaquesSimulados(1), new ItensSimulados(),
                    new FugaSimulada(ResultadoFuga.SUCESSO), new IASimulada());
        } catch (IllegalArgumentException e) {
            recusou = true;
        }
        check("construtor: jogador e inimigo iguais são recusados", recusou);
    }

    static void resultadosNulosDosModulos() {
        // --- ataque do jogador devolve null
        final Batalha b1 = novaBatalha(new AtaquesFalsos(), new ItensSimulados(), new FugaSimulada(ResultadoFuga.SUCESSO), new IASimulada());
        b1.iniciar();
        b1.abrirMenuAtaques();
        List<EventoBatalha> ev = semExcecao("ataque nulo", () -> b1.escolherAtaque(TipoAtaque.FISICO, "golpe"));
        check("nulo (ataques): evento ERRO_INTEGRACAO", tem(ev, TipoEvento.ERRO_INTEGRACAO));
        check("nulo (ataques): não consome turno", b1.ehTurnoJogador());
        check("nulo (ataques): continua no menu de ataques", b1.getMenuAtual() == MenuBatalha.ATAQUES);
        check("nulo (ataques): estado preservado", b1.getEstado() == EstadoBatalha.EM_ANDAMENTO);

        // --- item devolve null
        final Batalha b2 = novaBatalha(new AtaquesSimulados(10), new ItensFalsos(), new FugaSimulada(ResultadoFuga.SUCESSO), new IASimulada());
        b2.iniciar();
        b2.abrirMochila();
        ev = semExcecao("item nulo", () -> b2.usarItem("pocao_vida"));
        check("nulo (itens): evento ERRO_INTEGRACAO", tem(ev, TipoEvento.ERRO_INTEGRACAO));
        check("nulo (itens): não consome turno", b2.ehTurnoJogador());
        check("nulo (itens): continua na mochila", b2.getMenuAtual() == MenuBatalha.MOCHILA);

        // --- fuga devolve null (não pode virar "fuga não permitida")
        final Batalha b3 = novaBatalha(new AtaquesSimulados(10), new ItensSimulados(), new FugaFalsa(null), new IASimulada());
        b3.iniciar();
        ev = semExcecao("fuga nula", () -> b3.tentarFugir());
        check("nulo (fuga): evento ERRO_INTEGRACAO", tem(ev, TipoEvento.ERRO_INTEGRACAO));
        check("nulo (fuga): não é confundido com ACAO_INVALIDA", !tem(ev, TipoEvento.ACAO_INVALIDA));
        check("nulo (fuga): não consome turno", b3.ehTurnoJogador());
        check("nulo (fuga): batalha continua", b3.getEstado() == EstadoBatalha.EM_ANDAMENTO);

        // --- evento nulo dentro de um resultado válido
        AtaquesFalsos comEventoNulo = new AtaquesFalsos();
        comEventoNulo.respostas.add(ResultadoAcao.executada(Arrays.asList(
                new EventoBatalha(TipoEvento.MENSAGEM, "antes"), null, new EventoBatalha(TipoEvento.MENSAGEM, "depois"))));
        final Batalha b4 = novaBatalha(comEventoNulo, new ItensSimulados(), new FugaSimulada(ResultadoFuga.SUCESSO), new IASimulada());
        b4.iniciar();
        b4.abrirMenuAtaques();
        ev = semExcecao("evento nulo", () -> b4.escolherAtaque(TipoAtaque.FISICO, "golpe"));
        check("evento nulo: eventos válidos preservados", textoContem(ev, "antes") && textoContem(ev, "depois"));
        check("evento nulo: avisa com ERRO_INTEGRACAO", tem(ev, TipoEvento.ERRO_INTEGRACAO));
        check("evento nulo: nenhum null chega à interface", !contemNulo(ev));
        check("evento nulo: ação válida segue consumindo turno", b4.ehTurnoInimigo());

        // --- identificadores vazios do jogador
        AtaquesFalsos contador = new AtaquesFalsos();
        final Batalha b5 = novaBatalha(contador, new ItensSimulados(), new FugaSimulada(ResultadoFuga.SUCESSO), new IASimulada());
        b5.iniciar();
        b5.abrirMenuAtaques();
        check("id vazio: ataque com espaços é recusado", tem(b5.escolherAtaque(TipoAtaque.FISICO, "   "), TipoEvento.ACAO_INVALIDA));
        check("id vazio: ataque com null é recusado", tem(b5.escolherAtaque(TipoAtaque.FISICO, null), TipoEvento.ACAO_INVALIDA));
        check("id vazio: módulo de ataques não foi chamado", contador.chamadas == 0);
        b5.voltarMenuPrincipal();
        b5.abrirMochila();
        check("id vazio: item com espaços é recusado", tem(b5.usarItem(" "), TipoEvento.ACAO_INVALIDA));
        check("id vazio: turno do jogador mantido", b5.ehTurnoJogador());
    }

    /** Cria uma batalha e faz o jogador atacar com sucesso; devolve a batalha já no turno do inimigo. */
    static Batalha batalhaNoTurnoDoInimigo(AtaquesFalsos ataques, IAInimigo ia) {
        ataques.respostas.addFirst(ok()); // resposta da ação do jogador (vem antes da do inimigo)
        Batalha b = novaBatalha(ataques, new ItensSimulados(), new FugaSimulada(ResultadoFuga.SUCESSO), ia);
        b.iniciar();
        b.abrirMenuAtaques();
        b.escolherAtaque(TipoAtaque.FISICO, "golpe");
        return b;
    }

    /** Verifica que o turno do inimigo falhou "com segurança": erro visível, vez volta ao jogador. */
    static void confereTurnoInimigoSeguro(String nome, final Batalha b, AtaquesFalsos ataques, int chamadasEsperadas) {
        List<EventoBatalha> ev = semExcecao(nome, () -> b.executarTurnoInimigo());
        check(nome + ": evento ERRO_INTEGRACAO", tem(ev, TipoEvento.ERRO_INTEGRACAO));
        check(nome + ": turno volta ao jogador (não trava)", b.ehTurnoJogador());
        check(nome + ": evento TURNO_JOGADOR emitido", tem(ev, TipoEvento.TURNO_JOGADOR));
        check(nome + ": batalha continua em andamento", b.getEstado() == EstadoBatalha.EM_ANDAMENTO);
        check(nome + ": módulo de ataques chamado " + chamadasEsperadas + "x", ataques.chamadas == chamadasEsperadas);
    }

    static void acoesInvalidasDaIA() {
        // IA devolve null (só o ataque do jogador chegou ao módulo)
        AtaquesFalsos a1 = new AtaquesFalsos();
        IAFalsa ia1 = new IAFalsa(null);
        confereTurnoInimigoSeguro("IA nula", batalhaNoTurnoDoInimigo(a1, ia1), a1, 1);
        check("IA nula: IA foi consultada", ia1.chamadas == 1);

        // ação sem identificador (só espaços)
        AtaquesFalsos a2 = new AtaquesFalsos();
        confereTurnoInimigoSeguro("IA sem id", batalhaNoTurnoDoInimigo(a2, new IAFalsa(new AcaoInimiga(TipoAtaque.FISICO, "   "))), a2, 1);

        // identificador inexistente
        AtaquesFalsos a3 = new AtaquesFalsos();
        confereTurnoInimigoSeguro("IA id inexistente", batalhaNoTurnoDoInimigo(a3, new IAFalsa(new AcaoInimiga(TipoAtaque.FISICO, "naoexiste"))), a3, 1);

        // id existe, mas em outra categoria (golpe é FISICO; a IA pediu MAGICO)
        AtaquesFalsos a4 = new AtaquesFalsos();
        confereTurnoInimigoSeguro("IA id incompatível com o tipo", batalhaNoTurnoDoInimigo(a4, new IAFalsa(new AcaoInimiga(TipoAtaque.MAGICO, "golpe"))), a4, 1);

        // módulo de ataques não consegue listar as opções do inimigo (lista nula)
        AtaquesFalsos a5 = new AtaquesFalsos();
        a5.listaFisico = null;
        confereTurnoInimigoSeguro("lista de opções do inimigo nula", batalhaNoTurnoDoInimigo(a5, new IAFalsa(new AcaoInimiga(TipoAtaque.FISICO, "golpe"))), a5, 1);

        // ação da IA é válida, mas o módulo de ataques devolve null
        AtaquesFalsos a6 = new AtaquesFalsos();
        confereTurnoInimigoSeguro("ataque do inimigo devolve nulo", batalhaNoTurnoDoInimigo(a6, new IAFalsa(new AcaoInimiga(TipoAtaque.FISICO, "golpe"))), a6, 2);

        // ação da IA é válida, mas o módulo de ataques recusa (resultado inválido)
        AtaquesFalsos a7 = new AtaquesFalsos();
        a7.respostas.add(ResultadoAcao.invalida("alvo inválido"));
        final Batalha b7 = batalhaNoTurnoDoInimigo(a7, new IAFalsa(new AcaoInimiga(TipoAtaque.FISICO, "golpe")));
        List<EventoBatalha> ev7 = semExcecao("ataque do inimigo inválido", () -> b7.executarTurnoInimigo());
        check("ataque do inimigo inválido: mensagem do módulo é preservada", tem(ev7, TipoEvento.ACAO_INVALIDA));
        check("ataque do inimigo inválido: ERRO_INTEGRACAO emitido", tem(ev7, TipoEvento.ERRO_INTEGRACAO));
        check("ataque do inimigo inválido: vez volta ao jogador", b7.ehTurnoJogador());

        // caso normal continua funcionando e sem erro
        AtaquesFalsos a8 = new AtaquesFalsos();
        a8.respostas.add(ok());
        final Batalha b8 = batalhaNoTurnoDoInimigo(a8, new IAFalsa(new AcaoInimiga(TipoAtaque.FISICO, "golpe")));
        List<EventoBatalha> ev8 = semExcecao("ação válida da IA", () -> b8.executarTurnoInimigo());
        check("ação válida da IA: sem ERRO_INTEGRACAO", !tem(ev8, TipoEvento.ERRO_INTEGRACAO));
        check("ação válida da IA: ataque do inimigo executado", a8.chamadas == 2);
        check("ação válida da IA: turno do jogador", b8.ehTurnoJogador());

        // depois de uma falha da IA o jogador continua podendo agir normalmente
        AtaquesFalsos a9 = new AtaquesFalsos();
        final Batalha b9 = batalhaNoTurnoDoInimigo(a9, new IAFalsa(null));
        b9.executarTurnoInimigo();
        check("depois da falha da IA: jogador consegue abrir o menu", !tem(b9.abrirMenuAtaques(), TipoEvento.ACAO_INVALIDA));
    }

    static void listasDeAtaquesEItens() {
        // listas vazias
        AtaquesFalsos ataques = new AtaquesFalsos();
        ItensFalsos itens = new ItensFalsos();
        ataques.listaFisico = new ArrayList<OpcaoAcao>();
        Batalha b = novaBatalha(ataques, itens, new FugaSimulada(ResultadoFuga.SUCESSO), new IASimulada());
        check("lista vazia de ataques: devolve lista vazia", b.listarAtaques(TipoAtaque.FISICO).isEmpty());
        check("lista vazia de itens: devolve lista vazia", b.listarItens().isEmpty());

        // listas nulas viram listas vazias (sem NullPointerException)
        ataques.listaFisico = null;
        itens.lista = null;
        final Batalha bNula = b;
        check("lista nula de ataques: vira lista vazia", semExcecaoLista("ataques nulos", () -> bNula.listarAtaques(TipoAtaque.FISICO)).isEmpty());
        check("lista nula de itens: vira lista vazia", semExcecaoLista("itens nulos", () -> bNula.listarItens()).isEmpty());
        check("tipo de ataque nulo: vira lista vazia", b.listarAtaques(null).isEmpty());

        // elementos nulos são descartados
        ataques.listaFisico = new ArrayList<OpcaoAcao>(Arrays.asList(new OpcaoAcao("golpe", "Golpe"), null));
        check("elemento nulo na lista: é descartado", b.listarAtaques(TipoAtaque.FISICO).size() == 1);

        // quem recebe a lista não consegue alterá-la
        ataques.listaFisico = new ArrayList<OpcaoAcao>(Arrays.asList(new OpcaoAcao("golpe", "Golpe")));
        List<OpcaoAcao> recebida = b.listarAtaques(TipoAtaque.FISICO);
        boolean bloqueouAdd = false;
        try { recebida.add(new OpcaoAcao("hack", "Hack")); } catch (UnsupportedOperationException e) { bloqueouAdd = true; }
        boolean bloqueouRemove = false;
        try { recebida.clear(); } catch (UnsupportedOperationException e) { bloqueouRemove = true; }
        check("proteção: interface não consegue adicionar à lista de ataques", bloqueouAdd);
        check("proteção: interface não consegue limpar a lista de ataques", bloqueouRemove);
        check("proteção: lista original do módulo intacta", ataques.listaFisico.size() == 1);

        // alterações posteriores na lista original do módulo não vazam para a lista já entregue (cópia defensiva)
        List<OpcaoAcao> antes = b.listarAtaques(TipoAtaque.FISICO);
        ataques.listaFisico.add(new OpcaoAcao("novo", "Novo"));
        check("cópia defensiva: lista já entregue não muda", antes.size() == 1);
        check("cópia defensiva: nova consulta enxerga o dado atual", b.listarAtaques(TipoAtaque.FISICO).size() == 2);
        ataques.listaFisico.clear();
        check("cópia defensiva: limpar a original não esvazia a entregue", antes.size() == 1);

        // o mesmo para itens
        itens.lista = new ArrayList<OpcaoAcao>(Arrays.asList(new OpcaoAcao("pocao_vida", "Poção de Vida")));
        List<OpcaoAcao> itensRecebidos = b.listarItens();
        boolean itensBloqueados = false;
        try { itensRecebidos.add(new OpcaoAcao("hack", "Hack")); } catch (UnsupportedOperationException e) { itensBloqueados = true; }
        check("proteção: interface não consegue alterar a lista de itens", itensBloqueados);
        itens.lista.clear();
        check("cópia defensiva: itens já entregues não mudam", itensRecebidos.size() == 1);
        check("cópia defensiva: nova consulta de itens vê a lista vazia", b.listarItens().isEmpty());
    }

    interface FonteDeLista { List<OpcaoAcao> obter(); }

    static List<OpcaoAcao> semExcecaoLista(String nome, FonteDeLista fonte) {
        try {
            return fonte.obter();
        } catch (RuntimeException e) {
            check(nome + ": não deve lançar exceção (" + e + ")", false);
            return new ArrayList<OpcaoAcao>();
        }
    }

    /** Roda o programa de terminal com entrada pronta e devolve tudo o que ele imprimiu. */
    static String rodarTerminal(Batalha batalha, String entrada) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            PrintStream saida = new PrintStream(bytes, true, "UTF-8");
            SistemaBatalha.executar(batalha, new Scanner(entrada), saida);
            saida.flush();
            return bytes.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    static void terminalSelecaoDeAtaquesEItens() {
        // Entradas: 1=Atacar, 2=Mochila, 3=Fugir. Ataques: 1 Físico/2 Mágico/3 Buff/4 Voltar. Listas: 0=Voltar.

        // lista todos os ataques numerados e permite voltar sem consumir turno
        Cenario c1 = Cenario.padrao();
        String s1 = rodarTerminal(c1.batalha, "1\n1\n0\n4\n");
        check("terminal: lista o 1º ataque numerado", s1.contains("1 - Golpe"));
        check("terminal: lista o 2º ataque numerado", s1.contains("2 - Pancada"));
        check("terminal: lista o voltar", s1.contains("0 - Voltar"));
        check("terminal: voltar não consome turno", c1.batalha.ehTurnoJogador());
        check("terminal: voltar não aciona o módulo de ataques", c1.ataques.getChamadas() == 0);
        check("terminal: voltou ao menu principal", c1.batalha.getMenuAtual() == MenuBatalha.PRINCIPAL);

        // escolhe o SEGUNDO ataque físico (o jogador age primeiro, depois a IA usa "golpe")
        Cenario c2 = Cenario.padrao();
        rodarTerminal(c2.batalha, "1\n1\n2\n");
        check("terminal: escolheu o 2º ataque físico (pancada)", "pancada".equals(c2.ataques.getHistoricoIds().get(0)));
        check("terminal: inimigo agiu em seguida", c2.ataques.getHistoricoIds().size() == 2);

        // escolhe o segundo ataque mágico
        Cenario c3 = Cenario.padrao();
        rodarTerminal(c3.batalha, "1\n2\n2\n");
        check("terminal: escolheu o 2º ataque mágico (raio)", "raio".equals(c3.ataques.getHistoricoIds().get(0)));

        // escolhe o primeiro buff
        Cenario c4 = Cenario.padrao();
        rodarTerminal(c4.batalha, "1\n3\n1\n");
        check("terminal: escolheu o 1º buff (foco)", "foco".equals(c4.ataques.getHistoricoIds().get(0)));

        // número fora da lista e texto não numérico
        Cenario c5 = Cenario.padrao();
        String s5 = rodarTerminal(c5.batalha, "1\n1\n9\nabc\n0\n4\n");
        check("terminal: opção inválida é avisada", s5.contains("Opção inválida!"));
        check("terminal: opção inválida não aciona o módulo", c5.ataques.getChamadas() == 0);
        check("terminal: opção inválida não consome turno", c5.batalha.ehTurnoJogador());

        // lista de ataques vazia não causa erro
        AtaquesFalsos vazios = new AtaquesFalsos();
        vazios.listaFisico = new ArrayList<OpcaoAcao>();
        Batalha bVazia = novaBatalha(vazios, new ItensSimulados(), new FugaSimulada(ResultadoFuga.SUCESSO), new IASimulada());
        String s6 = rodarTerminal(bVazia, "1\n1\n");
        check("terminal: lista vazia de ataques é avisada", s6.contains("Nenhum ataque disponível"));
        check("terminal: lista vazia não consome turno", bVazia.ehTurnoJogador() && vazios.chamadas == 0);

        // mochila: lista as 6 poções e usa a escolhida
        Cenario c7 = Cenario.padrao();
        String s7 = rodarTerminal(c7.batalha, "2\n3\n");
        check("terminal: mochila lista as 6 poções", s7.contains("1 - Poção de Vida") && s7.contains("6 - Poção de Magia"));
        check("terminal: usou o 3º item (poção de força)", "pocao_forca".equals(c7.itens.getUltimoId()));

        // mochila: voltar sem consumir turno
        Cenario c8 = Cenario.padrao();
        rodarTerminal(c8.batalha, "2\n0\n");
        check("terminal: voltar da mochila não usa item", c8.itens.getUsos() == 0);
        check("terminal: voltar da mochila não consome turno", c8.batalha.ehTurnoJogador());

        // mochila vazia
        Batalha bMochilaVazia = novaBatalha(new AtaquesSimulados(10), new ItensFalsos(), new FugaSimulada(ResultadoFuga.SUCESSO), new IASimulada());
        String s9 = rodarTerminal(bMochilaVazia, "2\n0\n");
        check("terminal: mochila vazia é avisada", s9.contains("Mochila vazia."));
        check("terminal: mochila vazia deixa voltar", bMochilaVazia.getMenuAtual() == MenuBatalha.PRINCIPAL && bMochilaVazia.ehTurnoJogador());

        // início recusado é informado
        Cenario c10 = new Cenario(0, 100, 10, ResultadoFuga.SUCESSO);
        check("terminal: início recusado é informado", rodarTerminal(c10.batalha, "1\n").contains("não pôde ser iniciada"));

        // finais de batalha pelo terminal
        check("terminal: fuga chega ao resultado FUGA", rodarTerminal(Cenario.padrao().batalha, "3\n").contains("Resultado final: FUGA"));
        Cenario c12 = new Cenario(100, 10, 10, ResultadoFuga.SUCESSO);
        check("terminal: vitória chega ao resultado VITORIA", rodarTerminal(c12.batalha, "1\n1\n1\n").contains("Resultado final: VITORIA"));
        Cenario c13 = new Cenario(10, 100, 10, ResultadoFuga.SUCESSO);
        check("terminal: derrota chega ao resultado DERROTA", rodarTerminal(c13.batalha, "1\n1\n1\n").contains("Resultado final: DERROTA"));

        // entrada que acaba no meio não trava o programa
        check("terminal: entrada vazia encerra sem travar", rodarTerminal(Cenario.padrao().batalha, "").contains("Entrada encerrada."));
    }

    // ---------------------------------------------------------------- dublês de teste
    // Simulam módulos com comportamento defeituoso ou controlado pelo teste (retornos nulos, listas
    // alteráveis...). Ficam aqui porque são específicos dos testes; as simulações "normais" estão em
    // batalha.simulacao.

    /** Módulo de ataques cujas respostas são uma fila controlada pelo teste. Fila vazia => devolve null. */
    static class AtaquesFalsos implements SistemaAtaques {
        List<OpcaoAcao> listaFisico = new ArrayList<OpcaoAcao>(Arrays.asList(new OpcaoAcao("golpe", "Golpe")));
        final LinkedList<ResultadoAcao> respostas = new LinkedList<ResultadoAcao>();
        int chamadas = 0;

        @Override
        public List<OpcaoAcao> listarAtaques(Combatente usuario, TipoAtaque tipo) {
            return tipo == TipoAtaque.FISICO ? listaFisico : new ArrayList<OpcaoAcao>();
        }

        @Override
        public ResultadoAcao executar(Combatente usuario, Combatente alvo, TipoAtaque tipo, String idAtaque) {
            chamadas++;
            return respostas.isEmpty() ? null : respostas.removeFirst();
        }
    }

    /** Módulo de itens controlado pelo teste. Lista começa vazia; fila vazia => devolve null. */
    static class ItensFalsos implements SistemaItens {
        List<OpcaoAcao> lista = new ArrayList<OpcaoAcao>();
        final LinkedList<ResultadoAcao> respostas = new LinkedList<ResultadoAcao>();
        int chamadas = 0;

        @Override
        public List<OpcaoAcao> listarItens(Combatente usuario) { return lista; }

        @Override
        public ResultadoAcao usar(Combatente usuario, Combatente oponente, String idItem) {
            chamadas++;
            return respostas.isEmpty() ? null : respostas.removeFirst();
        }
    }

    /** Regra de fuga que devolve o que o teste mandar (inclusive null). */
    static class FugaFalsa implements SistemaFuga {
        ResultadoFuga resultado;
        FugaFalsa(ResultadoFuga resultado) { this.resultado = resultado; }

        @Override
        public ResultadoFuga tentarFuga(Combatente jogador, Combatente inimigo) { return resultado; }
    }

    /** IA que devolve a ação que o teste mandar (inclusive null). */
    static class IAFalsa implements IAInimigo {
        AcaoInimiga acao;
        int chamadas = 0;
        IAFalsa(AcaoInimiga acao) { this.acao = acao; }

        @Override
        public AcaoInimiga escolherAcao(Combatente inimigo, Combatente jogador) {
            chamadas++;
            return acao;
        }
    }

    static ResultadoAcao ok() {
        return ResultadoAcao.executada(new EventoBatalha(TipoEvento.MENSAGEM, "ok"));
    }

    static Batalha novaBatalha(SistemaAtaques ataques, SistemaItens itens, SistemaFuga fuga, IAInimigo ia) {
        return new Batalha(new CombatenteSimulado("Herói", 100), new CombatenteSimulado("Goblin", 100),
                ataques, itens, fuga, ia);
    }

    interface Acao { List<EventoBatalha> executar(); }

    /** Executa a ação e, se ela lançar exceção, registra FALHA (em vez de derrubar todos os testes). */
    static List<EventoBatalha> semExcecao(String nome, Acao acao) {
        try {
            return acao.executar();
        } catch (RuntimeException e) {
            check(nome + ": não deve lançar exceção (" + e + ")", false);
            return new ArrayList<EventoBatalha>();
        }
    }

    private static boolean contemNulo(List<EventoBatalha> eventos) {
        for (EventoBatalha e : eventos) {
            if (e == null) { return true; }
        }
        return false;
    }

    // ---------------------------------------------------------------- utilitários

    private static boolean textoContem(List<EventoBatalha> eventos, String trecho) {
        for (EventoBatalha e : eventos) {
            if (e.getTexto().contains(trecho)) { return true; }
        }
        return false;
    }

    private static boolean tem(List<EventoBatalha> eventos, TipoEvento tipo) {
        for (EventoBatalha e : eventos) {
            if (e.getTipo() == tipo) { return true; }
        }
        return false;
    }

    private static void check(String nome, boolean condicao) {
        if (condicao) { ok++; System.out.println("OK    " + nome); }
        else { falhas++; System.out.println("FALHA " + nome); }
    }
}
