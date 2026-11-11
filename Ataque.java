/**
 * Representa UM golpe que um personagem pode usar.
 * É uma classe simples que só guarda dados: nome, dano e precisão.
 */
public class Ataque {

    private final String nome;
    private final int dano;
    private final int precisao; // de 1 a 100 (é uma porcentagem)

    public Ataque(String nome, int dano, int precisao) {
        this.nome = nome;
        this.dano = dano;
        this.precisao = precisao;
    }

    // Getters: permitem LER os valores, mas ninguém de fora consegue alterá-los
    // (isso se chama encapsulamento).
    public String getNome()   { return nome; }
    public int getDano()      { return dano; }
    public int getPrecisao()  { return precisao; }
}
