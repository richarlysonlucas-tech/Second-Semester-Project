package batalha.simulacao;

import batalha.contratos.Combatente;
import batalha.contratos.ResultadoFuga;
import batalha.contratos.SistemaFuga;

/** STUB da regra de fuga: devolve sempre o resultado configurado. */
public class FugaSimulada implements SistemaFuga {

    private ResultadoFuga resultado;
    private int chamadas = 0;

    public FugaSimulada(ResultadoFuga resultado) { this.resultado = resultado; }

    public void setResultado(ResultadoFuga resultado) { this.resultado = resultado; }

    public int getChamadas() { return chamadas; }

    @Override
    public ResultadoFuga tentarFuga(Combatente jogador, Combatente inimigo) {
        chamadas++;
        return resultado;
    }
}
