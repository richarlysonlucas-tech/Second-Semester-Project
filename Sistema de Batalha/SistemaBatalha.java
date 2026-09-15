import java.util.Scanner;

public class SistemaBatalha {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Batalha batalha = new Batalha();

        batalha.iniciar();

        while (batalha.batalhaAtiva()) {

            if (batalha.ehTurnoJogador()) {
                System.out.println("\n>>> TURNO DO JOGADOR <<<");
            } else {
                System.out.println("\n>>> TURNO DO INIMIGO <<<");
            }

            System.out.println("\n");
            System.out.println(" BATALHA ");
            System.out.println("");
            System.out.println("1 - Atacar");
            System.out.println("2 - Abrir Mochila");
            System.out.println("3 - Fugir");

            System.out.print("\nEscolha: ");

            int opcao = scanner.nextInt();

            switch (opcao) {

                case 1:
                    batalha.atacar();
                    batalha.passarTurno();
                    break;

                case 2:
                    batalha.abrirMochila();
                    batalha.passarTurno();
                    break;

                case 3:
                    batalha.fugir();
                    break;

                default:
                    System.out.println("Opção inválida!");
                    break;
            }

            if (!batalha.ehTurnoJogador()) {
                System.out.println("\nInimigo realizou sua ação!");
                batalha.passarTurno();
            }
        }

        scanner.close();
    }
}