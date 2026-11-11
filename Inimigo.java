import java.util.Random;

/**
 * Inimigo é outro tipo de Personagem.
 * Sua escolha de ataque padrão é sorteada entre os golpes disponíveis,
 * mas as classes filhas podem SOBRESCREVER (@Override) para ficarem mais espertas.
 */
public abstract class Inimigo extends Personagem {

    public Inimigo(String nome, int vidaMaxima) {
        super(nome, vidaMaxima);
    }

    @Override
    public Ataque escolherAtaque(Random random) {
        int indice = random.nextInt(getAtaques().size());
        return getAtaques().get(indice);
    }
}
