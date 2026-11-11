package batalha.teste;

import batalha.Batalha;
import batalha.contratos.EventoBatalha;
import batalha.contratos.OpcaoAcao;
import batalha.contratos.ResultadoFuga;
import batalha.contratos.TipoAtaque;
import batalha.simulacao.AtaquesSimulados;
import batalha.simulacao.CombatenteSimulado;
import batalha.simulacao.FugaSimulada;
import batalha.simulacao.IASimulada;
import batalha.simulacao.ItensSimulados;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Programa de teste pelo terminal (temporário).
 *
 * Aqui ficam SOMENTE: leitura do teclado, impressão e a montagem das simulações.
 * Nenhuma regra de turno, menu ou fim de batalha é decidida nesta classe:
 * ela pergunta ao motor ({@link Batalha}) o que fazer e imprime o que o motor devolve.
 * A interface gráfica 2D fará o mesmo, trocando Scanner/println por entrada e desenho.
 *
 * Para poder ser testado automaticamente, o laço principal está em {@link #executar}, que recebe a
 * batalha, o Scanner e o PrintStream como parâmetros. O {@code main} só liga tudo a System.in/System.out.
 */
public class SistemaBatalha {

    private static final TipoAtaque[] CATEGORIAS = {TipoAtaque.FISICO, TipoAtaque.MAGICO, TipoAtaque.BUFF};

    public static void main(String[] args) {
        executar(criarBatalhaDeTeste(), new Scanner(System.in), System.out);
    }

    /** Monta uma batalha com as simulações (trocar pelos módulos reais quando existirem). */
    public static Batalha criarBatalhaDeTeste() {
        return new Batalha(
                new CombatenteSimulado("Herói", 100),
                new CombatenteSimulado("Goblin", 40),
                new AtaquesSimulados(10),
                new ItensSimulados(),
                new FugaSimulada(ResultadoFuga.SUCESSO),
                new IASimulada());
    }

    /** Laço do jogo de teste. Termina quando a batalha acaba ou quando a entrada acaba. */
    public static void executar(Batalha batalha, Scanner scanner, PrintStream saida) {
        imprimir(saida, batalha.iniciar());
        if (!batalha.foiIniciada()) {
            saida.println("\nA batalha não pôde ser iniciada.");
            return;
        }

        while (batalha.batalhaAtiva()) {
            if (batalha.ehTurnoInimigo()) {
                imprimir(saida, batalha.executarTurnoInimigo());
                continue;
            }

            boolean entradaContinua;
            switch (batalha.getMenuAtual()) {
                case PRINCIPAL:
                    entradaContinua = menuPrincipal(batalha, scanner, saida);
                    break;
                case ATAQUES:
                    entradaContinua = menuAtaques(batalha, scanner, saida);
                    break;
                default:
                    entradaContinua = menuMochila(batalha, scanner, saida);
                    break;
            }
            if (!entradaContinua) {
                saida.println("\nEntrada encerrada.");
                return;
            }
        }
        saida.println("\nResultado final: " + batalha.getEstado());
    }

    // ---------------------------------------------------------------- menus (cada um devolve false se a entrada acabou)

    private static boolean menuPrincipal(Batalha batalha, Scanner scanner, PrintStream saida) {
        saida.println("\n=== BATALHA ===\n1 - Atacar\n2 - Mochila\n3 - Fugir");
        Integer opcao = ler(scanner, saida);
        if (opcao == null) {
            return false;
        }
        switch (opcao) {
            case 1: imprimir(saida, batalha.abrirMenuAtaques()); break;
            case 2: imprimir(saida, batalha.abrirMochila()); break;
            case 3: imprimir(saida, batalha.tentarFugir()); break;
            default: saida.println("Opção inválida!"); break;
        }
        return true;
    }

    private static boolean menuAtaques(Batalha batalha, Scanner scanner, PrintStream saida) {
        saida.println("\n=== ATAQUES ===\n1 - Físico\n2 - Mágico\n3 - Buff\n4 - Voltar");
        Integer opcao = ler(scanner, saida);
        if (opcao == null) {
            return false;
        }
        if (opcao >= 1 && opcao <= CATEGORIAS.length) {
            return escolherAtaqueDaLista(batalha, CATEGORIAS[opcao - 1], scanner, saida);
        }
        if (opcao == 4) {
            imprimir(saida, batalha.voltarMenuPrincipal());
        } else {
            saida.println("Opção inválida!");
        }
        return true;
    }

    /**
     * Lista TODOS os ataques da categoria, numerados, e deixa o jogador escolher.
     * "0 - Voltar" só retorna ao menu de categorias: o motor nem é chamado, então nenhum turno é consumido.
     */
    private static boolean escolherAtaqueDaLista(Batalha batalha, TipoAtaque tipo, Scanner scanner, PrintStream saida) {
        List<OpcaoAcao> opcoes = batalha.listarAtaques(tipo);
        if (opcoes.isEmpty()) {
            saida.println("Nenhum ataque disponível nesta categoria.");
            return true;
        }
        while (true) {
            saida.println("\n--- Ataques (" + tipo + ") ---");
            for (int i = 0; i < opcoes.size(); i++) {
                saida.println((i + 1) + " - " + opcoes.get(i).getNome());
            }
            saida.println("0 - Voltar");
            Integer opcao = ler(scanner, saida);
            if (opcao == null) {
                return false;
            }
            if (opcao == 0) {
                return true;
            }
            if (opcao >= 1 && opcao <= opcoes.size()) {
                imprimir(saida, batalha.escolherAtaque(tipo, opcoes.get(opcao - 1).getId()));
                return true;
            }
            saida.println("Opção inválida!");
        }
    }

    private static boolean menuMochila(Batalha batalha, Scanner scanner, PrintStream saida) {
        List<OpcaoAcao> itens = batalha.listarItens();
        saida.println("\n=== MOCHILA ===");
        if (itens.isEmpty()) {
            saida.println("Mochila vazia.");
        }
        for (int i = 0; i < itens.size(); i++) {
            saida.println((i + 1) + " - " + itens.get(i).getNome());
        }
        saida.println("0 - Voltar");
        Integer opcao = ler(scanner, saida);
        if (opcao == null) {
            return false;
        }
        if (opcao == 0) {
            imprimir(saida, batalha.voltarMenuPrincipal());
        } else if (opcao >= 1 && opcao <= itens.size()) {
            imprimir(saida, batalha.usarItem(itens.get(opcao - 1).getId()));
        } else {
            saida.println("Opção inválida!");
        }
        return true;
    }

    // ---------------------------------------------------------------- entrada e saída

    /** @return o número digitado, -1 se não for número, ou {@code null} se a entrada acabou. */
    private static Integer ler(Scanner scanner, PrintStream saida) {
        saida.print("Escolha: ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        try {
            return Integer.valueOf(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return Integer.valueOf(-1);
        }
    }

    private static void imprimir(PrintStream saida, List<EventoBatalha> eventos) {
        for (EventoBatalha e : eventos) {
            saida.println(e.getTexto());
        }
    }
}
