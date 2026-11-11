import java.util.Random;

/**
 * Herói é um tipo de Personagem (herança: "extends").
 * Acrescenta o que só heróis têm: a raça.
 */
public abstract class Heroi extends Personagem {

    private final String raca;

    public Heroi(String nome, int vidaMaxima) {
        super(nome, vidaMaxima); // chama o construtor da classe mãe
        this.raca = "Humano";    // todos os heróis do jogo são humanos
    }

    public String getRaca() { return raca; }

    /**
     * Implementação do método abstrato da classe Personagem.
     * No jogo real, quem escolhe o golpe é o JOGADOR (clicando num botão).
     * Aqui, para a simulação, sorteamos um dos 3 ataques.
     */
    @Override
    public Ataque escolherAtaque(Random random) {
        int indice = random.nextInt(getAtaques().size());
        return getAtaques().get(indice);
    }
}
