package portas;

import java.util.List;

import estado.ResultadoBatalha;

/**
 * Porta do módulo de batalha. O cérebro só conhece esta interface; o adaptador
 * (por exemplo, o que embrulha o motor por turnos) fica na pasta integracao/.
 *
 * Uma instância representa UMA batalha. Quando terminou() vira true, resultado() diz como acabou
 * e os demais métodos de balanço (vida, fragmentos, flag) informam o que aplicar ao progresso.
 */
public interface BatalhaEmCurso {
    /** Inicia a batalha. Se o motor recusar, foiIniciada() continua false. */
    List<EventoDoJogo> iniciar();

    boolean foiIniciada();

    boolean terminou();

    boolean ehTurnoInimigo();

    /** Menu em que o jogador está (ex.: "PRINCIPAL", "ATAQUES", "MOCHILA"). */
    String menuAtual();

    /** Executa um comando do jogador (ou o turno do inimigo). Nunca deve lançar exceção por uso inválido. */
    List<EventoDoJogo> executar(ComandoBatalha comando);

    List<OpcaoDoJogo> listarAtaques(String categoria);

    List<OpcaoDoJogo> listarItens();

    /** Como a batalha acabou. Só vale depois de terminou() == true. */
    ResultadoBatalha resultado();

    /** Vida do herói ao fim da batalha (o cérebro corrige valores fora do limite). */
    int vidaRestanteDoHeroi();

    /** Fragmentos de recompensa. Só são aplicados em caso de vitória. */
    int fragmentosGanhos();

    /** Flag permanente a registrar em caso de vitória (ex.: "derrotado:REI_SOMBRAS"), ou null. */
    String flagDerrotado();
}
