package batalha.simulacao;

import batalha.contratos.Combatente;
import batalha.contratos.EventoBatalha;
import batalha.contratos.OpcaoAcao;
import batalha.contratos.ResultadoAcao;
import batalha.contratos.SistemaItens;
import batalha.contratos.TipoEvento;

import java.util.ArrayList;
import java.util.List;

/**
 * STUB do módulo de itens com as 6 poções da mochila. Só a poção de vida tem efeito (cura 20);
 * as demais apenas geram mensagem, pois os efeitos reais pertencem ao módulo de itens.
 */
public class ItensSimulados implements SistemaItens {

    private static final String[][] CATALOGO = {
        {"pocao_vida", "Poção de Vida"},
        {"pocao_mana", "Poção de Mana"},
        {"pocao_forca", "Poção de Força"},
        {"pocao_velocidade", "Poção de Velocidade"},
        {"pocao_precisao", "Poção de Precisão"},
        {"pocao_magia", "Poção de Magia"}
    };

    private int usos = 0;
    private String ultimoId;

    @Override
    public List<OpcaoAcao> listarItens(Combatente usuario) {
        List<OpcaoAcao> itens = new ArrayList<OpcaoAcao>();
        for (String[] item : CATALOGO) {
            itens.add(new OpcaoAcao(item[0], item[1]));
        }
        return itens;
    }

    @Override
    public ResultadoAcao usar(Combatente usuario, Combatente oponente, String idItem) {
        String nome = null;
        for (String[] item : CATALOGO) {
            if (item[0].equals(idItem)) {
                nome = item[1];
            }
        }
        if (nome == null) {
            return ResultadoAcao.invalida("Item inexistente: " + idItem);
        }
        usos++;
        ultimoId = idItem;
        if ("pocao_vida".equals(idItem)) {
            if (usuario instanceof CombatenteSimulado) {
                ((CombatenteSimulado) usuario).curar(20);
            }
            return ResultadoAcao.executada(new EventoBatalha(TipoEvento.MENSAGEM,
                    usuario.getNome() + " usou " + nome + " e recuperou 20 PV."));
        }
        return ResultadoAcao.executada(new EventoBatalha(TipoEvento.MENSAGEM,
                usuario.getNome() + " usou " + nome + "."));
    }

    public int getUsos() { return usos; }

    public String getUltimoId() { return ultimoId; }
}
