package integracao;

import java.nio.file.Path;
import java.nio.file.Paths;
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
import estado.GameStateController;
import jogo.OrquestradorDoJogo;
import jogo.RespostaDoJogo;
import modelo.AreaEncontro;
import modelo.Inimigo;
import portas.ComandoBatalha;
import save.SaveEmJson;

/**
 * Demonstração da integração real: encontros do Richarlyson + motor do Vitor + cérebro.
 * Mostra o JSON que o front-end receberia em cada passo.
 */
public class DemoIntegrada {
    public static void main(String[] args) {
        Random sempreEncontra = new Random() {
            private static final long serialVersionUID = 1L;

            @Override
            public int nextInt(int limite) {
                return 0;
            }
        };
        ControladorEncontro encontros = EncontrosDoRicharlyson.criarControlador(sempreEncontra);
        encontros.cadastrarArea(new AreaEncontro("FLORESTA_01", 0, 0, 1000, 1000, 50,
            Arrays.asList(new Inimigo("SLIME_01", "Slime Verde", 1))));

        Path pasta = Paths.get("saves");
        OrquestradorDoJogo jogo = new OrquestradorDoJogo(
            new GameStateController(new SaveEmJson(pasta)),
            EncontrosDoRicharlyson.de(encontros),
            (heroi, encontro) -> {
                final CombatenteSimulado h = new CombatenteSimulado(heroi.getNomeHeroi(), heroi.getVidaAtual());
                CombatenteSimulado inimigo = new CombatenteSimulado(encontro.getInimigoNome(),
                    30 * encontro.getInimigoNivel());
                Batalha b = new Batalha(h, inimigo, new AtaquesSimulados(15), new ItensSimulados(),
                    new FugaSimulada(ResultadoFuga.SUCESSO), new IASimulada());
                return new BatalhaViaMotorV2(b, h::getPv, 3, null);
            },
            "jogador-1");

        mostrar("novoJogo", jogo.novoJogo("Arthur", "Guerreiro", 100));
        mostrar("mover(10,50)", jogo.mover(10, 50, "FLORESTA_01"));
        mostrar("mover(26,50)  <- passo completo, o sorteio acontece", jogo.mover(26, 50, "FLORESTA_01"));
        mostrar("abrirAtaques", jogo.comandoDeBatalha(ComandoBatalha.abrirAtaques()));
        String ataque = jogo.opcoesDeAtaque("FISICO").get(0).getId();
        mostrar("atacar(FISICO, " + ataque + ")", jogo.comandoDeBatalha(ComandoBatalha.atacar("FISICO", ataque)));
        RespostaDoJogo turno = jogo.comandoDeBatalha(ComandoBatalha.turnoInimigo());
        mostrar("turnoInimigo", turno);
        if (jogo.haBatalhaEmCurso()) {
            jogo.comandoDeBatalha(ComandoBatalha.abrirAtaques());
            mostrar("atacar de novo", jogo.comandoDeBatalha(ComandoBatalha.atacar("FISICO", ataque)));
        }
        mostrar("entrarNoHub", jogo.entrarNoHub());
        mostrar("purificarFragmentos", jogo.purificarFragmentos());
        mostrar("salvar(1)", jogo.salvar(1));
    }

    private static void mostrar(String passo, RespostaDoJogo r) {
        System.out.println("> " + passo);
        System.out.println("  " + r.paraJson());
    }
}
