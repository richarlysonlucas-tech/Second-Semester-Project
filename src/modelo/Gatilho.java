package modelo;

import java.util.List;

/**
 * Contrato comum para gatilhos. Diferentes tipos podem ter condiçoes diferentes.
 * Isso permite usar a mesma lista sem saber qual classe concreta foi criada.
 */
public interface Gatilho {
    String getId();
    int getPrioridade();
    boolean foiAtivado(Jogador jogador, List<String> acoesRealizadas);
    Inimigo getInimigo();
}
