package servico;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.List;

import controle.IniciadorDeBatalha;
import modelo.AreaEncontro;
import modelo.Encontro;
import modelo.Inimigo;
import modelo.Jogador;
import modelo.Gatilho;
import modelo.EntidadeMundo;

/**
 * Contém a regra de encontros e não conhece detalhes de tela ou JSON.
 */
public class ServicoEncontro {
    private Map<String, AreaEncontro> areas;
    private DetectorPresenca detectorPresenca;
    private Random sorteador;
    private IniciadorDeBatalha iniciadorDeBatalha;
    private List<Gatilho> gatilhos;
    private List<EntidadeMundo> entidades;

    public ServicoEncontro(IniciadorDeBatalha iniciadorDeBatalha) {
        this(new Random(), iniciadorDeBatalha);
    }

    public ServicoEncontro(Random sorteador, IniciadorDeBatalha iniciadorDeBatalha) {
        this.sorteador = sorteador;
        this.iniciadorDeBatalha = iniciadorDeBatalha;
        this.detectorPresenca = new DetectorPresenca();
        this.areas = new HashMap<String, AreaEncontro>();
        this.gatilhos = new ArrayList<Gatilho>();
        this.entidades = new ArrayList<EntidadeMundo>();
    }

    public void cadastrarArea(AreaEncontro area) {
        areas.put(area.getId(), area);
    }

    public void cadastrarGatilho(Gatilho gatilho) {
        gatilhos.add(gatilho);
    }

    public void cadastrarEntidade(EntidadeMundo entidade) {
        entidades.add(entidade);
    }

    /**
     * Recebe a posição informada pelo front-end e decide se ocorre um encontro.
     * A batalha só é acionada quando o encontro de fato é criado.
     */
    public Encontro verificar(Jogador jogador) {
        return verificar(jogador, new ArrayList<String>());
    }

    public Encontro verificar(Jogador jogador, List<String> acoesRealizadas) {
        if (acoesRealizadas == null) {
            acoesRealizadas = new ArrayList<String>();
        }
        Gatilho gatilho = encontrarGatilho(jogador, acoesRealizadas);
        if (gatilho != null) {
            return iniciar(Encontro.comEncontro(jogador, gatilho.getInimigo(), "GATILHO_" + gatilho.getId()));
        }

        EntidadeMundo entidade = encontrarEntidadeMaisProxima(jogador);
        if (entidade != null) {
            return iniciar(Encontro.comEncontro(jogador, entidade.getInimigo(), "PROXIMIDADE_" + entidade.getId()));
        }

        AreaEncontro area = areas.get(jogador.getAreaAtualId());
        if (area == null || !detectorPresenca.estaNaArea(
                jogador.getPosicaoX(), jogador.getPosicaoY(), area)) {
            return Encontro.semEncontro(jogador);
        }

        int valorSorteado = sorteador.nextInt(100);
        if (valorSorteado >= area.getChanceEncontroPercentual()) {
            return Encontro.semEncontro(jogador);
        }

        if (area.getInimigosPossiveis().isEmpty()) {
            return Encontro.semEncontro(jogador);
        }

        int indice = sorteador.nextInt(area.getInimigosPossiveis().size());
        Inimigo inimigo = area.getInimigosPossiveis().get(indice);
        Encontro encontro = Encontro.comEncontro(jogador, inimigo);
        return iniciar(encontro);
    }

    private Gatilho encontrarGatilho(Jogador jogador, List<String> acoesRealizadas) {
        Gatilho escolhido = null;
        for (Gatilho gatilho : gatilhos) {
            if (gatilho.foiAtivado(jogador, acoesRealizadas)
                    && (escolhido == null || gatilho.getPrioridade() > escolhido.getPrioridade())) {
                escolhido = gatilho;
            }
        }
        return escolhido;
    }

    private EntidadeMundo encontrarEntidadeMaisProxima(Jogador jogador) {
        EntidadeMundo maisProxima = null;
        double menorDistancia = Double.MAX_VALUE;
        for (EntidadeMundo entidade : entidades) {
            double distancia = detectorPresenca.calcularDistancia(jogador, entidade);
            if (distancia <= entidade.getRaioDeteccao() && distancia < menorDistancia) {
                maisProxima = entidade;
                menorDistancia = distancia;
            }
        }
        return maisProxima;
    }

    private Encontro iniciar(Encontro encontro) {
        iniciadorDeBatalha.iniciar(encontro);
        return encontro;
    }
}
