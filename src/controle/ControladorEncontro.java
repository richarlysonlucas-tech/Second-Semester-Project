package controle;

import modelo.AreaEncontro;
import modelo.Encontro;
import modelo.Jogador;
import servico.ServicoEncontro;
import modelo.Gatilho;
import modelo.EntidadeMundo;
import java.util.Collections;
import java.util.List;

/**
 * Faz a adaptação entre chamadas do front-end e classes de regra do jogo.
 */
public class ControladorEncontro {
    private ServicoEncontro servicoEncontro;
    private AreaEncontro areaAtual;

    public ControladorEncontro(ServicoEncontro servicoEncontro) {
        this.servicoEncontro = servicoEncontro;
    }

    public void cadastrarArea(AreaEncontro area) {
        areaAtual = area;
        servicoEncontro.cadastrarArea(area);
    }

    public void cadastrarGatilho(Gatilho gatilho) {
        servicoEncontro.cadastrarGatilho(gatilho);
    }

    public void cadastrarEntidade(EntidadeMundo entidade) {
        servicoEncontro.cadastrarEntidade(entidade);
    }

    /**
     * Contrato do front-end:
     * entrada: jogadorId, posicaoX, posicaoY e areaId;
     * saída: JSON com acao "SEM_ENCONTRO" ou "INICIAR_BATALHA".
     */
    public String verificarPresenca(String jogadorId, double posicaoX, double posicaoY, String areaId) {
        return verificarPresenca(jogadorId, posicaoX, posicaoY, areaId, Collections.<String>emptyList());
    }

    public String verificarPresenca(String jogadorId, double posicaoX, double posicaoY,
            String areaId, List<String> acoesRealizadas) {
        if (jogadorId == null || jogadorId.trim().isEmpty() || areaId == null || areaId.trim().isEmpty()) {
            return new RespostaFrontEnd(
                "ENTRADA_INVALIDA", "", "", "jogadorId e areaId são obrigatórios."
            ).paraJson();
        }

        Jogador jogador = new Jogador(jogadorId, posicaoX, posicaoY, areaId);
        Encontro encontro = servicoEncontro.verificar(jogador, acoesRealizadas);
        RespostaFrontEnd resposta;

        if (!encontro.ocorreu()) {
            resposta = new RespostaFrontEnd(
                "SEM_ENCONTRO", jogadorId, areaId, "Nenhum inimigo encontrado nesta posição."
            );
        } else {
            resposta = new RespostaFrontEnd(
                "INICIAR_BATALHA", jogadorId, areaId, "Encontro iniciado. O sistema de batalha deve assumir."
            );
            resposta.definirInimigo(
                encontro.getInimigo().getId(),
                encontro.getInimigo().getNome(),
                encontro.getInimigo().getNivel()
            );
        }
        resposta.definirOrigem(encontro.getOrigem());
        return resposta.paraJson();
    }

    /**
     * Conveniência para ações realizadas pelo jogador, objeto de interação ou conclusão.
     */
    public String verificarAcao(String jogadorId, double posicaoX, double posicaoY,
            String areaId, String acaoRealizada) {
        return verificarPresenca(jogadorId, posicaoX, posicaoY, areaId,
            java.util.Arrays.asList(acaoRealizada));
    }
}
