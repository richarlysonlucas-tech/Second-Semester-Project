import java.util.Random;

/**
 * Boss: o chefão.
 * Aqui o POLIMORFISMO aparece de verdade: o Boss tem uma lógica própria
 * de escolha de ataque, diferente do sorteio padrão de Inimigo.
 */
public class Boss extends Inimigo {

    public Boss(String nome) {
        super(nome, 120);
        adicionarAtaque(new Ataque("Ataque especial", 15, 100));
        adicionarAtaque(new Ataque("Ataque supremo", 25, 85));
    }

    /** O Boss usa o ataque supremo em 50% dos turnos; no resto, o especial. */
    @Override
    public Ataque escolherAtaque(Random random) {
        boolean usarSupremo = random.nextInt(100) < 50;
        return usarSupremo ? getAtaques().get(1) : getAtaques().get(0);
    }
}
