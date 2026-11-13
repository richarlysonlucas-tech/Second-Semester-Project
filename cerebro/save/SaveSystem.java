package save;

import estado.DadosDoJogo;

/**
 * Contrato do sistema de save. O resto do jogo só conhece esta interface,
 * então o formato (JSON, binário, nuvem) pode mudar sem afetar ninguém.
 */
public interface SaveSystem {
    /** Grava o retrato do jogo no slot. Os dados são validados antes de gravar. */
    void salvar(int slot, DadosDoJogo dados) throws SaveException;

    /** Lê e valida o slot. Nunca devolve null: ou devolve dados válidos ou lança SaveException. */
    DadosDoJogo carregar(int slot) throws SaveException;

    boolean existe(int slot);

    void apagar(int slot) throws SaveException;
}
