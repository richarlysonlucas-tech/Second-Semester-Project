package batalha.contratos;

/** Contrato da inteligência artificial dos inimigos. O motor apenas pergunta qual ação executar. */
public interface IAInimigo {

    /**
     * @return a ação escolhida. NÃO devolva {@code null} nem id vazio: o motor trata isso como erro de
     *         integração (evento ERRO_INTEGRACAO) e o inimigo perde o turno. O id precisa existir entre as
     *         opções que {@code SistemaAtaques.listarAtaques(inimigo, tipo)} devolve para o inimigo.
     */
    AcaoInimiga escolherAcao(Combatente inimigo, Combatente jogador);
}
