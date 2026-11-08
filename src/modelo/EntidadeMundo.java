package modelo;

/**
 * Entidade visível no mundo, usada para verificar proximidade.
 */
public class EntidadeMundo {
    private String id;
    private double posicaoX;
    private double posicaoY;
    private double raioDeteccao;
    private Inimigo inimigo;

    public EntidadeMundo(String id, double posicaoX, double posicaoY,
            double raioDeteccao, Inimigo inimigo) {
        this.id = id;
        this.posicaoX = posicaoX;
        this.posicaoY = posicaoY;
        this.raioDeteccao = raioDeteccao;
        this.inimigo = inimigo;
    }

    public String getId() {
        return id;
    }

    public double getPosicaoX() {
        return posicaoX;
    }

    public double getPosicaoY() {
        return posicaoY;
    }

    public double getRaioDeteccao() {
        return raioDeteccao;
    }

    public Inimigo getInimigo() {
        return inimigo;
    }
}
