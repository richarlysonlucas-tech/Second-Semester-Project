import java.util.Scanner;

public class SistemaBatalha {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Batalha batalha = new Batalha();

        batalha.iniciar();

        while (batalha.batalhaAtiva()) {

            System.out.println("\n========================");
            System.out.println("       BATALHA");
            System.out.println("========================");

            System.out.println("1 - Atacar");
            System.out.println("2 - Abrir Mochila");
            System.out.println("3 - Fugir");

            System.out.print("\nEscolha: ");

            int opcao = scanner.nextInt();

            switch (opcao) {

                case 1:
                    batalha.atacar();
                    break;

                case 2:
                    batalha.abrirMochila();
                    break;

                case 3:
                    batalha.fugir();
                    break;

                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        }

        scanner.close();
    }
}