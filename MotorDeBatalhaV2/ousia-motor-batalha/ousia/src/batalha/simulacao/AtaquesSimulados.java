package batalha.simulacao;

import batalha.contratos.Combatente;
import batalha.contratos.EventoBatalha;
import batalha.contratos.OpcaoAcao;
import batalha.contratos.ResultadoAcao;
import batalha.contratos.SistemaAtaques;
import batalha.contratos.TipoAtaque;
import batalha.contratos.TipoEvento;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * STUB do módulo de ataques: dano fixo e sem cálculo real. Registra as chamadas
 * recebidas para que os testes verifiquem a integração.
 */
public class AtaquesSimulados implements SistemaAtaques {

    private final int dano;
    private int chamadas = 0;
    private Combatente ultimoUsuario;
    private Combatente ultimoAlvo;
    private TipoAtaque ultimoTipo;
    private String ultimoId;
    private final List<String> historicoIds = new ArrayList<String>();

    public AtaquesSimulados(int dano) { this.dano = dano; }

    @Override
    public List<OpcaoAcao> listarAtaques(Combatente usuario, TipoAtaque tipo) {
        // Dois ataques por categoria, para testar a escolha entre várias opções.
        switch (tipo) {
            case FISICO:
                return Arrays.asList(new OpcaoAcao("golpe", "Golpe"), new OpcaoAcao("pancada", "Pancada"));
            case MAGICO:
                return Arrays.asList(new OpcaoAcao("bola_de_fogo", "Bola de Fogo"), new OpcaoAcao("raio", "Raio"));
            default:
                return Arrays.asList(new OpcaoAcao("foco", "Foco"), new OpcaoAcao("escudo", "Escudo"));
        }
    }

    @Override
    public ResultadoAcao executar(Combatente usuario, Combatente alvo, TipoAtaque tipo, String idAtaque) {
        chamadas++;
        ultimoUsuario = usuario;
        ultimoAlvo = alvo;
        ultimoTipo = tipo;
        ultimoId = idAtaque;
        historicoIds.add(idAtaque);

        List<EventoBatalha> ev = new ArrayList<EventoBatalha>();
        if (tipo == TipoAtaque.BUFF) {
            ev.add(new EventoBatalha(TipoEvento.MENSAGEM, usuario.getNome() + " usou " + idAtaque + " (buff)."));
        } else {
            if (alvo instanceof CombatenteSimulado) {
                ((CombatenteSimulado) alvo).receberDano(dano);
            }
            ev.add(new EventoBatalha(TipoEvento.MENSAGEM,
                    usuario.getNome() + " usou " + idAtaque + " e causou " + dano + " de dano em " + alvo.getNome() + "."));
        }
        return ResultadoAcao.executada(ev);
    }

    public int getChamadas() { return chamadas; }
    public Combatente getUltimoUsuario() { return ultimoUsuario; }
    public Combatente getUltimoAlvo() { return ultimoAlvo; }
    public TipoAtaque getUltimoTipo() { return ultimoTipo; }
    public String getUltimoId() { return ultimoId; }
    /** Ids recebidos, na ordem das chamadas (jogador e inimigo misturados). */
    public List<String> getHistoricoIds() { return new ArrayList<String>(historicoIds); }
}
