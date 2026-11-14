package integracao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

import batalha.Batalha;
import batalha.contratos.ResultadoFuga;
import batalha.simulacao.AtaquesSimulados;
import batalha.simulacao.CombatenteSimulado;
import batalha.simulacao.FugaSimulada;
import batalha.simulacao.IASimulada;
import batalha.simulacao.ItensSimulados;
import controle.ControladorEncontro;
import estado.EstadoJogo;
import estado.GameStateController;
import jogo.OrquestradorDoJogo;
import jogo.RespostaDoJogo;
import modelo.AreaEncontro;
import modelo.EntidadeMundo;
import modelo.GatilhoAcao;
import modelo.GatilhoArea;
import modelo.Inimigo;
import portas.ComandoBatalha;
import portas.FabricaDeBatalha;
import save.SaveEmJson;

/**
 * Teste da integração REAL: módulo de encontros do Richarlyson + motor de batalha do Vitor
 * (com as simulações de ataques/itens/fuga/IA dele) + cérebro. Só compila junto com o código deles.
 *
 * Rodar com integracao/compilar.sh
 */
public class TesteIntegracao {
    private static int total = 0;

    /** Sorteador que sempre cai no encontro e escolhe o primeiro inimigo. */
    private static class SempreEncontra extends Random {
        private static final long serialVersionUID = 1L;

        @Override
        public int nextInt(int limite) {
            return 0;
        }
    }

    /** Sorteador que nunca encontra nada (valor sorteado alto). */
    private static class NuncaEncontra extends Random {
        private static final long serialVersionUID = 1L;

        @Override
        public int nextInt(int limite) {
            return limite - 1;
        }
    }

    public static void main(String[] args) throws Exception {
        testarCaminhoCompletoComEncontroAleatorio();
        testarSemEncontroNaoIniciaBatalha();
        testarGatilhoDeAcaoComOrigemUnica();
        testarProximidade();
        testarDerrotaPeloMotorReal();
        testarFugaPeloMotorReal();
        testarSaveDepoisDaBatalha();
        System.out.println("Todos os " + total + " testes de integração passaram.");
    }

    // ---------------------------------------------------------------- montagem

    private static ControladorEncontro encontros(Random sorteador, boolean comArea) {
        ControladorEncontro c = EncontrosDoRicharlyson.criarControlador(sorteador);
        if (comArea) {
            Inimigo slime = new Inimigo("SLIME_01", "Slime Verde", 1);
            Inimigo morcego = new Inimigo("MORCEGO_01", "Morcego Sombrio", 2);
            c.cadastrarArea(new AreaEncontro("FLORESTA_01", 0, 0, 1000, 1000, 50, Arrays.asList(slime, morcego)));
        }
        return c;
    }

    /** Fábrica com o motor REAL do Vitor e as simulações dele para ataques, itens, fuga e IA. */
    private static FabricaDeBatalha fabrica(final int danoDoHeroi, final int pvInimigoPorNivel,
            final ResultadoFuga fuga) {
        return (heroi, encontro) -> {
            final CombatenteSimulado h = new CombatenteSimulado(heroi.getNomeHeroi(), heroi.getVidaAtual());
            CombatenteSimulado inimigo = new CombatenteSimulado(encontro.getInimigoNome(),
                pvInimigoPorNivel * encontro.getInimigoNivel());
            Batalha b = new Batalha(h, inimigo, new AtaquesSimulados(danoDoHeroi), new ItensSimulados(),
                new FugaSimulada(fuga), new IASimulada());
            return new BatalhaViaMotorV2(b, h::getPv, 3, null);
        };
    }

    private static OrquestradorDoJogo jogo(ControladorEncontro enc, FabricaDeBatalha fab, Path pasta) {
        OrquestradorDoJogo o = new OrquestradorDoJogo(new GameStateController(new SaveEmJson(pasta)),
            EncontrosDoRicharlyson.de(enc), fab, "jogador-1");
        o.novoJogo("Arthur", "Guerreiro", 100);
        return o;
    }

    private static Path pasta() throws Exception {
        Path p = Files.createTempDirectory("ousia-integracao-");
        p.toFile().deleteOnExit();
        return p;
    }

    /** Anda até o primeiro passo completo e devolve a resposta da checagem. */
    private static RespostaDoJogo andar(OrquestradorDoJogo o, double x0) {
        o.mover(x0, 50, "FLORESTA_01");
        return o.mover(x0 + 16, 50, "FLORESTA_01");
    }

    // ---------------------------------------------------------------- testes

    private static void testarCaminhoCompletoComEncontroAleatorio() throws Exception {
        OrquestradorDoJogo o = jogo(encontros(new SempreEncontra(), true), fabrica(50, 10, ResultadoFuga.SUCESSO), pasta());
        RespostaDoJogo r = andar(o, 10);
        verificar(r.isOk() && r.getEstado() == EstadoJogo.BATALHA, "Deveria entrar em batalha: " + r);
        verificar(r.getEventos().size() >= 1 && "BATALHA_INICIADA".equals(r.getEventos().get(0).getTipo()),
            "Deveria trazer os eventos reais do motor.");
        verificar("SLIME_01".equals(((java.util.Map<?, ?>) r.getExtra("inimigo")).get("id")), "Inimigo do módulo de encontros.");
        verificar("ALEATORIO".equals(r.getExtra("origem")), "Origem aleatória.");

        verificar(!o.opcoesDeAtaque("FISICO").isEmpty(), "Os ataques reais do motor deveriam aparecer.");
        verificar(!o.opcoesDeItens().isEmpty(), "Os itens reais do motor deveriam aparecer.");

        RespostaDoJogo menu = o.comandoDeBatalha(ComandoBatalha.abrirAtaques());
        verificar("ATAQUES".equals(menu.getExtra("menu")), "O menu deveria ser ATAQUES.");
        String idAtaque = o.opcoesDeAtaque("FISICO").get(0).getId();
        RespostaDoJogo fim = o.comandoDeBatalha(ComandoBatalha.atacar("FISICO", idAtaque));
        verificar(fim.isOk() && fim.getEstado() == EstadoJogo.EXPLORACAO, "Inimigo fraco: vitória no primeiro golpe. " + fim);
        verificar("VITORIA".equals(fim.getExtra("resultadoBatalha")), "Resultado do motor real: vitória.");
        verificar(o.getDados().getFragmentosColetados() == 3, "Fragmentos de recompensa.");
        fim("caminho completo: andar, encontro real, batalha com motor real, vitória");
    }

    private static void testarSemEncontroNaoIniciaBatalha() throws Exception {
        OrquestradorDoJogo o = jogo(encontros(new NuncaEncontra(), true), fabrica(10, 10, ResultadoFuga.SUCESSO), pasta());
        RespostaDoJogo r = andar(o, 10);
        verificar(r.isOk() && r.getEstado() == EstadoJogo.EXPLORACAO, "Sem encontro, continua explorando.");
        verificar("Nenhum encontro.".equals(r.getMensagem()), "Mensagem de sem encontro.");
        fim("módulo real sem encontro não inicia batalha");
    }

    private static void testarGatilhoDeAcaoComOrigemUnica() throws Exception {
        ControladorEncontro enc = encontros(new NuncaEncontra(), false);
        Inimigo guardiao = new Inimigo("GUARDIAO_BAU", "Guardião do Baú", 1);
        enc.cadastrarGatilho(new GatilhoAcao("BAU", "ABRIR_BAU", 20, guardiao));
        OrquestradorDoJogo o = jogo(enc, fabrica(50, 10, ResultadoFuga.SUCESSO), pasta());
        o.definirOrigemUnica("GATILHO_BAU");

        RespostaDoJogo r = o.agir("ABRIR_BAU");
        verificar(r.getEstado() == EstadoJogo.BATALHA, "Abrir o baú deveria iniciar a batalha: " + r);
        verificar("GATILHO_BAU".equals(r.getExtra("origem")), "Origem do gatilho.");
        o.comandoDeBatalha(ComandoBatalha.abrirAtaques());
        o.comandoDeBatalha(ComandoBatalha.atacar("FISICO", o.opcoesDeAtaque("FISICO").get(0).getId()));
        verificar(o.getEstado() == EstadoJogo.EXPLORACAO, "Venceu o guardião.");
        verificar(o.getDados().getFlagsMundo().contains("resolvido:GATILHO_BAU"), "Baú marcado como resolvido.");

        RespostaDoJogo de_novo = o.agir("ABRIR_BAU");
        verificar(de_novo.isOk() && de_novo.getEstado() == EstadoJogo.EXPLORACAO, "O baú não dispara de novo.");
        verificar(de_novo.getMensagem().contains("já resolvido"), "Deveria explicar o motivo.");
        fim("gatilho real de ação: dispara uma vez e não repete");
    }

    private static void testarProximidade() throws Exception {
        ControladorEncontro enc = encontros(new NuncaEncontra(), false);
        enc.cadastrarEntidade(new EntidadeMundo("LOBISOMEM", 55, 55, 60,
            new Inimigo("LOBISOMEM_01", "Lobisomem", 2)));
        OrquestradorDoJogo o = jogo(enc, fabrica(50, 10, ResultadoFuga.SUCESSO), pasta());
        o.mover(0, 0, "FLORESTA_01");
        RespostaDoJogo r = o.mover(16, 16, "FLORESTA_01");
        verificar(r.getEstado() == EstadoJogo.BATALHA, "Entrar no raio da entidade inicia a batalha: " + r);
        verificar("PROXIMIDADE_LOBISOMEM".equals(r.getExtra("origem")), "Origem por proximidade.");
        fim("entidade real por proximidade inicia a batalha");
    }

    private static void testarDerrotaPeloMotorReal() throws Exception {
        // Inimigo muito forte e herói fraco: o inimigo derrota o herói no primeiro turno dele.
        FabricaDeBatalha fraca = (heroi, encontro) -> {
            final CombatenteSimulado h = new CombatenteSimulado(heroi.getNomeHeroi(), 5);
            CombatenteSimulado inimigo = new CombatenteSimulado(encontro.getInimigoNome(), 1000);
            Batalha b = new Batalha(h, inimigo, new AtaquesSimulados(10), new ItensSimulados(),
                new FugaSimulada(ResultadoFuga.NAO_PERMITIDA), new IASimulada());
            return new BatalhaViaMotorV2(b, h::getPv, 0, null);
        };
        OrquestradorDoJogo o = jogo(encontros(new SempreEncontra(), true), fraca, pasta());
        andar(o, 10);
        o.comandoDeBatalha(ComandoBatalha.abrirAtaques());
        o.comandoDeBatalha(ComandoBatalha.atacar("FISICO", o.opcoesDeAtaque("FISICO").get(0).getId()));
        verificar(o.getEstado() == EstadoJogo.BATALHA, "O inimigo ainda precisa agir.");
        RespostaDoJogo fim = o.comandoDeBatalha(ComandoBatalha.turnoInimigo());
        verificar(fim.getEstado() == EstadoJogo.GAME_OVER, "Derrota pelo motor real leva ao game over: " + fim);
        verificar("DERROTA".equals(fim.getExtra("resultadoBatalha")), "Resultado: derrota.");
        verificar(o.getDados().getVidaAtual() == 0, "Vida zero.");
        fim("derrota pelo motor real leva ao game over");
    }

    private static void testarFugaPeloMotorReal() throws Exception {
        OrquestradorDoJogo o = jogo(encontros(new SempreEncontra(), true), fabrica(10, 1000, ResultadoFuga.SUCESSO), pasta());
        andar(o, 10);
        RespostaDoJogo fim = o.comandoDeBatalha(ComandoBatalha.fugir());
        verificar(fim.getEstado() == EstadoJogo.EXPLORACAO, "Fuga volta à exploração: " + fim);
        verificar("FUGA".equals(fim.getExtra("resultadoBatalha")), "Resultado: fuga.");
        verificar(o.getDados().getFragmentosColetados() == 0, "Fuga não dá recompensa.");

        // Fuga não permitida (ex.: chefe): o motor recusa, a batalha continua e o motivo chega ao front.
        OrquestradorDoJogo chefe = jogo(encontros(new SempreEncontra(), true),
            fabrica(10, 1000, ResultadoFuga.NAO_PERMITIDA), pasta());
        andar(chefe, 10);
        RespostaDoJogo recusada = chefe.comandoDeBatalha(ComandoBatalha.fugir());
        verificar(recusada.getEstado() == EstadoJogo.BATALHA, "Fuga recusada: a batalha continua.");
        boolean temMotivo = false;
        for (portas.EventoDoJogo e : recusada.getEventos()) {
            temMotivo = temMotivo || "ACAO_INVALIDA".equals(e.getTipo());
        }
        verificar(temMotivo, "O motivo da recusa (ACAO_INVALIDA) deveria chegar ao front.");
        fim("fuga pelo motor real");
    }

    private static void testarSaveDepoisDaBatalha() throws Exception {
        Path pasta = pasta();
        OrquestradorDoJogo o = jogo(encontros(new SempreEncontra(), true), fabrica(50, 10, ResultadoFuga.SUCESSO), pasta);
        andar(o, 10);
        verificar(!o.salvar(1).isOk(), "Não salva durante a batalha.");
        o.comandoDeBatalha(ComandoBatalha.abrirAtaques());
        o.comandoDeBatalha(ComandoBatalha.atacar("FISICO", o.opcoesDeAtaque("FISICO").get(0).getId()));
        verificar(o.salvar(1).isOk(), "Salva depois da batalha.");

        OrquestradorDoJogo outro = new OrquestradorDoJogo(new GameStateController(new SaveEmJson(pasta)),
            EncontrosDoRicharlyson.de(encontros(new NuncaEncontra(), true)), fabrica(10, 10, ResultadoFuga.SUCESSO), "jogador-1");
        verificar(outro.carregar(1).isOk(), "Carrega o save.");
        verificar(outro.getDados().getFragmentosColetados() == 3, "Progresso preservado.");
        fim("save depois de uma batalha real e load em outra sessão");
    }

    private static void fim(String nome) {
        total++;
        System.out.println("  ok  " + nome);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
