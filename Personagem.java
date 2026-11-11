import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe BASE (abstrata) de todo mundo que luta: heróis e inimigos.
 *
 * "abstract" significa que você não pode fazer "new Personagem()".
 * Ela existe apenas para ser herdada, guardando o que é COMUM a todos:
 * nome, vida e lista de ataques.
 */
public abstract class Personagem {

    private final String nome;
    private final int vidaMaxima;
    private int vidaAtual;
    private final List<Ataque> ataques = new ArrayList<>();

    public Personagem(String nome, int vidaMaxima) {
        this.nome = nome;
        this.vidaMaxima = vidaMaxima;
        this.vidaAtual = vidaMaxima; // começa com a vida cheia
    }

    // "protected": só as classes filhas (Guerreiro, Boss...) podem chamar.
    protected void adicionarAtaque(Ataque ataque) {
        ataques.add(ataque);
    }

    /**
     * POLIMORFISMO: cada tipo de personagem decide do seu jeito
     * qual ataque usar. Aqui só declaramos que o método EXISTE;
     * as classes filhas são obrigadas a implementá-lo.
     */
    public abstract Ataque escolherAtaque(Random random);

    /** Reduz a vida. Math.max garante que a vida nunca fique negativa. */
    public void receberDano(int dano) {
        vidaAtual = Math.max(0, vidaAtual - dano);
    }

    public boolean estaVivo() {
        return vidaAtual > 0;
    }

    public String getNome()          { return nome; }
    public int getVidaAtual()        { return vidaAtual; }
    public int getVidaMaxima()       { return vidaMaxima; }
    public List<Ataque> getAtaques() { return ataques; }
}
