package modelo;

import java.util.List;

/**
 * Gatilho ativado quando o front-end informa que uma ação foi realizada.
 */
public class GatilhoAcao implements Gatilho {
    private String id;
    private String acaoNecessaria;
    private int prioridade;
    private Inimigo inimigo;

    public GatilhoAcao(String id, String acaoNecessaria, int prioridade, Inimigo inimigo) {
        this.id = id;
        this.acaoNecessaria = acaoNecessaria;
        this.prioridade = prioridade;
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
        return acoesRealizadas.contains(acaoNecessaria);
    }

    @Override
    public Inimigo getInimigo() {
        return inimigo;
    }
}
