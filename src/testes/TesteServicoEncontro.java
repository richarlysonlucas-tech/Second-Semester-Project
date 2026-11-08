package testes;

import java.util.Arrays;
import java.util.Random;

import controle.IniciadorDeBatalha;
import modelo.AreaEncontro;
import modelo.Encontro;
import modelo.Inimigo;
import modelo.Jogador;
import modelo.GatilhoArea;
import modelo.GatilhoAcao;
import modelo.EntidadeMundo;
import servico.ServicoEncontro;
import servico.DetectorPresenca;

/**
 * Testes simples, sem framework, para executar com Java padrão.
 */
public class TesteServicoEncontro {
    public static void main(String[] args) {
        testarForaDaArea();
        testarDentroDaAreaComEncontro();
        testarGatilhoDeArea();
        testarGatilhoDeAcao();
        testarProximidade();
        testarPrioridadeDosGatilhos();
        testarEntidadeMaisProxima();
        testarFormulaDaDistancia();
        System.out.println("Todos os testes passaram.");
    }

    private static void testarGatilhoDeArea() {
        Inimigo inimigo = new Inimigo("GUARDIAO", "Guardiao", 3);
        GatilhoArea gatilho = new GatilhoArea("PORTAL", 10, 10, 10, 4, 4, inimigo);
        ContadorDeBatalha contador = new ContadorDeBatalha();
        ServicoEncontro servico = new ServicoEncontro(new RandomZero(), contador);
        servico.cadastrarGatilho(gatilho);

        Encontro resultado = servico.verificar(new Jogador("J1", 12, 12, "AREA_1"));
        verificar(resultado.ocorreu(), "O gatilho de area deveria ser ativado.");
        verificar(resultado.getOrigem().contains("PORTAL"), "A origem deveria identificar o gatilho.");
    }

    private static void testarGatilhoDeAcao() {
        Inimigo inimigo = new Inimigo("CHEFE", "Guardiao do Bau", 4);
        GatilhoAcao gatilho = new GatilhoAcao("BAU", "ABRIR_BAU", 10, inimigo);
        ContadorDeBatalha contador = new ContadorDeBatalha();
        ServicoEncontro servico = new ServicoEncontro(new RandomZero(), contador);
        servico.cadastrarGatilho(gatilho);

        Encontro resultado = servico.verificar(
            new Jogador("J1", 0, 0, "AREA_1"),
            Arrays.asList("ABRIR_BAU")
        );
        verificar(resultado.ocorreu(), "O gatilho de acao deveria ser ativado.");
        verificar(resultado.getOrigem().contains("BAU"), "A origem deveria identificar o gatilho.");
    }

    private static void testarProximidade() {
        Inimigo inimigo = new Inimigo("LOBISOMEM", "Lobisomem", 5);
        EntidadeMundo entidade = new EntidadeMundo("MONSTRO_1", 10, 10, 5, inimigo);
        ContadorDeBatalha contador = new ContadorDeBatalha();
        ServicoEncontro servico = new ServicoEncontro(new RandomZero(), contador);
        servico.cadastrarEntidade(entidade);

        Encontro resultado = servico.verificar(
            new Jogador("J1", 12, 13, "AREA_1")
        );
        verificar(resultado.ocorreu(), "A entidade deveria estar dentro do raio.");
        verificar(resultado.getOrigem().contains("MONSTRO_1"), "A origem deveria identificar a entidade.");
    }

    private static void testarPrioridadeDosGatilhos() {
        Inimigo inimigoComum = new Inimigo("COMUM", "Inimigo comum", 1);
        Inimigo chefe = new Inimigo("CHEFE", "Chefe", 10);
        GatilhoArea gatilhoComum = new GatilhoArea("COMUM", 10, 0, 0, 10, 10, inimigoComum);
        GatilhoAcao gatilhoChefe = new GatilhoAcao("CHEFE", "ABRIR_BAU", 20, chefe);
        ContadorDeBatalha contador = new ContadorDeBatalha();
        ServicoEncontro servico = new ServicoEncontro(new RandomZero(), contador);
        servico.cadastrarGatilho(gatilhoComum);
        servico.cadastrarGatilho(gatilhoChefe);

        Encontro resultado = servico.verificar(
            new Jogador("J1", 5, 5, "AREA_1"),
            Arrays.asList("ABRIR_BAU")
        );

        verificar(resultado.getInimigo().equals(chefe), "O gatilho de maior prioridade deveria vencer.");
        verificar(contador.quantidade == 1, "O início da batalha deveria ser chamado uma única vez.");
    }

    private static void testarEntidadeMaisProxima() {
        Inimigo inimigoLonge = new Inimigo("LONGE", "Inimigo longe", 1);
        Inimigo inimigoPerto = new Inimigo("PERTO", "Inimigo perto", 2);
        EntidadeMundo entidadeLonge = new EntidadeMundo("E1", 8, 8, 10, inimigoLonge);
        EntidadeMundo entidadePerto = new EntidadeMundo("E2", 1, 1, 10, inimigoPerto);
        ContadorDeBatalha contador = new ContadorDeBatalha();
        ServicoEncontro servico = new ServicoEncontro(new RandomZero(), contador);
        servico.cadastrarEntidade(entidadeLonge);
        servico.cadastrarEntidade(entidadePerto);

        Encontro resultado = servico.verificar(new Jogador("J1", 0, 0, "AREA_1"));

        verificar(resultado.getInimigo().equals(inimigoPerto),
            "A entidade mais próxima deveria vencer.");
        verificar(resultado.getOrigem().contains("E2"),
            "A origem deveria identificar a entidade mais próxima.");
    }

    private static void testarFormulaDaDistancia() {
        DetectorPresenca detector = new DetectorPresenca();
        Jogador jogador = new Jogador("J1", 0, 0, "AREA_1");
        EntidadeMundo entidade = new EntidadeMundo("E1", 3, 4, 5, new Inimigo("E", "Entidade", 1));
        double distancia = detector.calcularDistancia(jogador, entidade);
        verificar(Math.abs(distancia - 5.0) < 0.0001,
            "A distância deveria ser calculada pela raiz de dx² + dy².");
    }

    private static void testarForaDaArea() {
        Inimigo inimigo = new Inimigo("SLIME", "Slime", 1);
        AreaEncontro area = new AreaEncontro(
            "AREA_1", 0, 0, 10, 10, 100, Arrays.asList(inimigo)
        );
        ContadorDeBatalha contador = new ContadorDeBatalha();
        ServicoEncontro servico = new ServicoEncontro(new RandomZero(), contador);
        servico.cadastrarArea(area);

        Encontro resultado = servico.verificar(new Jogador("J1", 20, 20, "AREA_1"));
        verificar(!resultado.ocorreu(), "Não deveria haver encontro fora da área.");
        verificar(contador.quantidade == 0, "A batalha não deveria iniciar fora da área.");
    }

    private static void testarDentroDaAreaComEncontro() {
        Inimigo inimigo = new Inimigo("MORCEGO", "Morcego", 2);
        AreaEncontro area = new AreaEncontro(
            "AREA_1", 0, 0, 10, 10, 100, Arrays.asList(inimigo)
        );
        ContadorDeBatalha contador = new ContadorDeBatalha();
        ServicoEncontro servico = new ServicoEncontro(new RandomZero(), contador);
        servico.cadastrarArea(area);

        Encontro resultado = servico.verificar(new Jogador("J1", 5, 5, "AREA_1"));
        verificar(resultado.ocorreu(), "Deveria haver encontro dentro da área.");
        verificar(contador.quantidade == 1, "A batalha deveria ser iniciada uma vez.");
        verificar(resultado.getInimigo().equals(inimigo), "O inimigo sorteado está incorreto.");
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static class RandomZero extends Random {
        private static final long serialVersionUID = 1L;

        @Override
        public int nextInt(int limite) {
            return 0;
        }
    }

    private static class ContadorDeBatalha implements IniciadorDeBatalha {
        private int quantidade;

        @Override
        public void iniciar(Encontro encontro) {
            quantidade++;
        }
    }
}
