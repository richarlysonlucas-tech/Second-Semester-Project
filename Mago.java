/** Mago: herói de cajado. */
public class Mago extends Heroi {
    public Mago(String nome) {
        super(nome, 45); // menos vida, mais dano especial
        adicionarAtaque(new Ataque("Ataque simples", 5, 100));
        adicionarAtaque(new Ataque("Ataque pesado", 15, 85));
        adicionarAtaque(new Ataque("Ataque especial", 23, 65));
    }
}
