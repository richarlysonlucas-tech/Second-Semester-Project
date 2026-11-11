package batalha.contratos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * O que um módulo externo devolve ao motor depois de executar uma ação.
 * <ul>
 *   <li><b>valida</b>: a ação pôde ser executada?</li>
 *   <li><b>consomeTurno</b>: a ação gastou o turno? (só importa quando válida)</li>
 *   <li><b>eventos</b>: mensagens produzidas (dano, cura, buff...).</li>
 * </ul>
 * O motor só passa o turno quando a ação é válida E consome turno.
 */
public final class ResultadoAcao {

    private final boolean valida;
    private final boolean consomeTurno;
    private final List<EventoBatalha> eventos;

    private ResultadoAcao(boolean valida, boolean consomeTurno, List<EventoBatalha> eventos) {
        this.valida = valida;
        this.consomeTurno = consomeTurno;
        this.eventos = Collections.unmodifiableList(new ArrayList<EventoBatalha>(eventos));
    }

    /** Ação válida que consome o turno. */
    public static ResultadoAcao executada(List<EventoBatalha> eventos) {
        return new ResultadoAcao(true, true, eventos);
    }

    public static ResultadoAcao executada(EventoBatalha... eventos) {
        return executada(Arrays.asList(eventos));
    }

    /** Ação válida que NÃO consome o turno. */
    public static ResultadoAcao semConsumirTurno(List<EventoBatalha> eventos) {
        return new ResultadoAcao(true, false, eventos);
    }

    /** Ação recusada (alvo inválido, item inexistente...). Não consome turno. */
    public static ResultadoAcao invalida(String motivo) {
        return new ResultadoAcao(false, false,
                Collections.singletonList(new EventoBatalha(TipoEvento.ACAO_INVALIDA, motivo)));
    }

    public boolean isValida() { return valida; }

    public boolean consomeTurno() { return consomeTurno; }

    public List<EventoBatalha> getEventos() { return eventos; }
}
