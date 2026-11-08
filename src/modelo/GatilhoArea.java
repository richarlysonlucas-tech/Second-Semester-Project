package modelo;

import java.util.List;

/**
 * Gatilho que é ativado quando o jogador entra em uma área retangular.
 */
public class GatilhoArea implements Gatilho {
    private String id;
    private int prioridade;
    private double xInicial;
    private double yInicial;
    private double largura;
    private double altura;
    private Inimigo inimigo;

    public GatilhoArea(String id, int prioridade, double xInicial, double yInicial,
            double largura, double altura, Inimigo inimigo) {
        this.id = id;
        this.prioridade = prioridade;
        this.xInicial = xInicial;
        this.yInicial = yInicial;
        this.largura = largura;
        this.altura = altura;
        this.inimigo = inimigo;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getPrioridade() {
        return prioridade;
    }

    @Override
    public boolean foiAtivado(Jogador jogador, List<String> acoesRealizadas) {
        return jogador.getPosicaoX() >= xInicial
            && jogador.getPosicaoX() <= xInicial + largura
            && jogador.getPosicaoY() >= yInicial
            && jogador.getPosicaoY() <= yInicial + altura;
    }

    @Override
    public Inimigo getInimigo() {
        return inimigo;
    }
}
