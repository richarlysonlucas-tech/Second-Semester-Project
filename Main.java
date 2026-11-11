import java.util.Random;

/** Simulação de uma batalha: Guerreiro vs Boss. */
public class Main {

    public static void main(String[] args) {
        CalculadoraDeBatalha calculadora = new CalculadoraDeBatalha(new Random());
        Random sorteio = new Random();

        // Note: a variável é do tipo Personagem, mas guarda um Guerreiro e um Boss.
        // Isso só é possível por causa da herança.
        Personagem heroi = new Guerreiro("Arthur");
        Personagem inimigo = new Boss("Rei das Sombras");

        System.out.println("=== BATALHA: " + heroi.getNome() + " vs " + inimigo.getNome() + " ===");

        int rodadasSimuladas = 3;
        for (int rodada = 1; rodada <= rodadasSimuladas; rodada++) {
            if (!heroi.estaVivo() || !inimigo.estaVivo()) break;

            System.out.println("\n--- Rodada " + rodada + " ---");

            // Turno do herói. escolherAtaque() é POLIMÓRFICO: o Java chama
            // a versão certa (de Heroi ou de Boss) sem a gente precisar de "if".
            Ataque golpeHeroi = heroi.escolherAtaque(sorteio);
            calculadora.executarAtaque(heroi, inimigo, golpeHeroi);

            if (!inimigo.estaVivo()) break;

            // Turno do inimigo
            Ataque golpeInimigo = inimigo.escolherAtaque(sorteio);
            calculadora.executarAtaque(inimigo, heroi, golpeInimigo);
        }

        System.out.println("\n=== FIM ===");
        System.out.printf("%s: %d/%d | %s: %d/%d%n",
                heroi.getNome(), heroi.getVidaAtual(), heroi.getVidaMaxima(),
                inimigo.getNome(), inimigo.getVidaAtual(), inimigo.getVidaMaxima());
    }
}
