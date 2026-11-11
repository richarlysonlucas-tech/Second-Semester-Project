/** Arqueiro: herói de arco. */
public class Arqueiro extends Heroi {
    public Arqueiro(String nome) {
        super(nome, 50);
        adicionarAtaque(new Ataque("Flechada simples", 6, 100));
        adicionarAtaque(new Ataque("Flechada múltipla", 12, 90));
        adicionarAtaque(new Ataque("Flechada especial", 19, 70));
    }
}
