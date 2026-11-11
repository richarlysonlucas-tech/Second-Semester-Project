package batalha.contratos;

import java.util.List;

/**
 * Contrato do módulo de ataques e habilidades (também usado pelos cálculos).
 * Implementado pelo integrante responsável. O motor só pede e recebe o resultado.
 */
public interface SistemaAtaques {

    /**
     * Lista as opções disponíveis ao combatente naquela categoria (para o submenu).
     * O motor chama este método para o JOGADOR (menu) e também para o INIMIGO (para conferir a ação
     * escolhida pela IA), portanto ele deve funcionar para qualquer {@link Combatente}.
     */
    List<OpcaoAcao> listarAtaques(Combatente usuario, TipoAtaque tipo);

    /**
     * Executa o ataque/habilidade, aplicando os efeitos e cálculos.
     *
     * @param usuario  quem executa
     * @param alvo     oponente (para BUFF o módulo decide em quem aplicar)
     * @param tipo     categoria escolhida
     * @param idAtaque identificador da opção escolhida
     * @return resultado com validade, consumo de turno e mensagens
     */
    ResultadoAcao executar(Combatente usuario, Combatente alvo, TipoAtaque tipo, String idAtaque);
}
