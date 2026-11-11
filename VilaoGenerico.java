/** Vilão Genérico: inimigo fraco e comum. */
public class VilaoGenerico extends Inimigo {
    public VilaoGenerico(String nome) {
        super(nome, 30);
        adicionarAtaque(new Ataque("Ataque simples", 5, 100));
        adicionarAtaque(new Ataque("Ataque especial", 12, 75));
    }
}
