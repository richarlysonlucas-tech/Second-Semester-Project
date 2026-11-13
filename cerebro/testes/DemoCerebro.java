package testes;

import java.nio.file.Path;
import java.nio.file.Paths;

import estado.GameStateController;
import estado.ResultadoBatalha;
import save.SaveEmJson;

/**
 * Demonstração no console: uma sessão curta de jogo, com save e load.
 *
 * Rodar: java -cp out testes.DemoCerebro
 * Os saves ficam na pasta "saves/" (ignorada pelo git).
 */
public class DemoCerebro {
    public static void main(String[] args) {
        Path pasta = Paths.get("saves");
        GameStateController jogo = new GameStateController(new SaveEmJson(pasta));

        passo("Começa no menu", jogo);
        System.out.println(jogo.novoJogo("Arthur", "Guerreiro", 60));
        passo("Novo jogo", jogo);

        System.out.println("\nAndando pelo mapa (o front chamaria isto a cada frame):");
        for (int x = 0; x <= 40; x += 4) {
            boolean verificar = jogo.registrarMovimento(x, 50, "FLORESTA_01");
            if (verificar) {
                System.out.println("  x=" + x + " -> hora de perguntar ao ServicoEncontro se há encontro");
            }
        }

        System.out.println("\n" + jogo.iniciarBatalha("SLIME_01", "ALEATORIO"));
        System.out.println("Tentando salvar no meio da batalha: " + jogo.salvar(1));
        System.out.println(jogo.encerrarBatalha(ResultadoBatalha.VITORIA, 3, null, 48));
        jogo.marcarFlag("bau:BAU_01");
        jogo.adicionarItem("POCAO", 2);

        System.out.println("\n" + jogo.entrarNoHub());
        System.out.println(jogo.purificarFragmentos());
        System.out.println(jogo.salvar(1));

        System.out.println("\nSimulando fechar e abrir o jogo:");
        GameStateController novaSessao = new GameStateController(new SaveEmJson(pasta));
        System.out.println(novaSessao.carregar(1));
        passo("Depois de carregar", novaSessao);
        System.out.println("Baú já aberto? " + novaSessao.jaResolvido("bau:BAU_01"));
    }

    private static void passo(String titulo, GameStateController jogo) {
        System.out.println("[" + titulo + "] estado=" + jogo.getEstado()
            + (jogo.getDados() == null ? "" : " vida=" + jogo.getDados().getVidaAtual()
            + "/" + jogo.getDados().getVidaMaxima()
            + " purificados=" + jogo.getDados().getFragmentosPurificados()
            + " inventario=" + jogo.getDados().getInventario()));
    }
}
