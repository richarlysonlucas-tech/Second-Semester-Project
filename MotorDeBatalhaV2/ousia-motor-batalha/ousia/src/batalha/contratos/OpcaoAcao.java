package batalha.contratos;

import java.util.Objects;

/**
 * Uma opção selecionável (um ataque ou um item). O motor só repassa o {@code id};
 * o significado fica com o módulo dono.
 */
public final class OpcaoAcao {

    private final String id;
    private final String nome;

    public OpcaoAcao(String id, String nome) {
        this.id = Objects.requireNonNull(id, "id");
        this.nome = Objects.requireNonNull(nome, "nome");
    }

    public String getId() { return id; }

    public String getNome() { return nome; }
}
