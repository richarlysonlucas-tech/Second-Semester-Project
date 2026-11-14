package portas;

/** Uma opção de menu de batalha (um ataque ou um item), com o id que o front devolve ao escolher. */
public final class OpcaoDoJogo {
    private final String id;
    private final String nome;

    public OpcaoDoJogo(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
