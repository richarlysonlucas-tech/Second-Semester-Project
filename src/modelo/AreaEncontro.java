package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Zona da fase onde encontros aleatórios podem acontecer.
 */
public class AreaEncontro {
    private String id;
    private double xInicial;
    private double yInicial;
    private double largura;
    private double altura;
    private int chanceEncontroPercentual;
    private List<Inimigo> inimigosPossiveis;

    public AreaEncontro(String id, double xInicial, double yInicial, double largura, double altura,
            int chanceEncontroPercentual, List<Inimigo> inimigosPossiveis) {
        this.id = id;
        this.xInicial = xInicial;
        this.yInicial = yInicial;
        this.largura = largura;
        this.altura = altura;
        this.chanceEncontroPercentual = chanceEncontroPercentual;

        // Uma cópia evita que a área seja alterada sem querer por outra classe.
        this.inimigosPossiveis = new ArrayList<Inimigo>(inimigosPossiveis);
    }

    public String getId() {
        return id;
    }

    public double getXInicial() {
        return xInicial;
    }

    public double getYInicial() {
        return yInicial;
    }

    public double getLargura() {
        return largura;
    }

    public double getAltura() {
        return altura;
    }

    public int getChanceEncontroPercentual() {
        return chanceEncontroPercentual;
    }

    public List<Inimigo> getInimigosPossiveis() {
        return new ArrayList<Inimigo>(inimigosPossiveis);
    }
}
