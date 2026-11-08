package modelo;

/**
 * Representa um inimigo que pode participar de um encontro.
 */
public class Inimigo {
    private String id;
    private String nome;
    private int nivel;

    public Inimigo(String id, String nome, int nivel) {
        this.id = id;
        this.nome = nome;
        this.nivel = nivel;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getNivel() {
        return nivel;
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Inimigo
            && id.equals(((Inimigo) outro).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
