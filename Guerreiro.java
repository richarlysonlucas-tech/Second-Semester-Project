/** Guerreiro: herói de espada. Só precisa configurar seus dados. */
public class Guerreiro extends Heroi {
    public Guerreiro(String nome) {
        super(nome, 60); // 60 de vida (valor sugerido, ajuste ao seu balanceamento)
        adicionarAtaque(new Ataque("Ataque simples", 7, 100));
        adicionarAtaque(new Ataque("Ataque pesado", 15, 80));
        adicionarAtaque(new Ataque("Ataque especial", 21, 65));
    }
}
