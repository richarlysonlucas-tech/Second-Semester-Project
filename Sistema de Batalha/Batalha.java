public class Batalha {

    String nomeInimigo = "Goblin";

    int vidaInimigo = 100;
    int forcaInimigo = 15;

    public void ataqueInimigo(Personagem jogador) {

        int dano = forcaInimigo;

        jogador.vida -= dano;

        System.out.println("\nO " + nomeInimigo + " atacou!");
        System.out.println("Dano recebido: " + dano);
        System.out.println("Sua vida: " + jogador.vida);
    }
}