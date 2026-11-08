package modelo;

/**
 * Representa apenas os dados de posição usados pelo sistema de encontros.
 */
public class Jogador {
    private String id;
    private double posicaoX;
    private double posicaoY;
    private String areaAtualId;

    public Jogador(String id, double posicaoX, double posicaoY, String areaAtualId) {
        this.id = id;
        this.posicaoX = posicaoX;
        this.posicaoY = posicaoY;
        this.areaAtualId = areaAtualId;
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

    public String getAreaAtualId() {
        return areaAtualId;
    }

    public void atualizarPosicao(double posicaoX, double posicaoY, String areaAtualId) {
        this.posicaoX = posicaoX;
        this.posicaoY = posicaoY;
        this.areaAtualId = areaAtualId;
    }
}
