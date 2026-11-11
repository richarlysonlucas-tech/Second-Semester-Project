/** Vilão Específico: inimigo mais forte, com golpes mais pesados. */
public class VilaoEspecifico extends Inimigo {
    public VilaoEspecifico(String nome) {
        super(nome, 45);
        adicionarAtaque(new Ataque("Ataque pesado", 9, 100));
        adicionarAtaque(new Ataque("Ataque especial", 17, 70));
    }
}
