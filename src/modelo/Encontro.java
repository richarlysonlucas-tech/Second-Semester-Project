package modelo;

/**
 * Guarda o resultado de uma tentativa de encontro.
 */
public class Encontro {
    private boolean ocorreu;
    private Jogador jogador;
    private Inimigo inimigo;
    private String origem;

    private Encontro(boolean ocorreu, Jogador jogador, Inimigo inimigo, String origem) {
        this.ocorreu = ocorreu;
        this.jogador = jogador;
        this.inimigo = inimigo;
        this.origem = origem;
    }

    public static Encontro semEncontro(Jogador jogador) {
        return new Encontro(false, jogador, null, "NENHUM");
    }

    public static Encontro comEncontro(Jogador jogador, Inimigo inimigo) {
        return new Encontro(true, jogador, inimigo, "ALEATORIO");
    }

    public static Encontro comEncontro(Jogador jogador, Inimigo inimigo, String origem) {
        return new Encontro(true, jogador, inimigo, origem);
    }

    public boolean ocorreu() {
        return ocorreu;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public Inimigo getInimigo() {
        return inimigo;
    }

    public String getOrigem() {
        return origem;
    }
}
