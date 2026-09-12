import java.util.Scanner;

public class SistemaBatalha {
// Classe principal do sistema de batalha
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Personagem jogador = new Personagem();
            Batalha resposta = new Batalha();
// Solicita o nome do personagem e armazena na variável nome
            System.out.println("Digite o nome do seu personagem: ");
            jogador.nome = scanner.nextLine();



            System.out.println("Seja Bem-vindo, " + jogador.nome + "!");
            System.out.println("       ESCOLHA SUA CLASSE");
            System.out.println("\n                         ");

            System.out.println("1 - Guerreiro");
            System.out.println("2 - Mago");
            System.out.println("3 - Arqueiro");

            System.out.print("\nEscolha sua classe: ");
            int escolha = scanner.nextInt();
// Escolha da classe do personagem e atribuição de atributos

            switch (escolha) {
                case 1 -> {
                    jogador.vida = 120;
                    jogador.forca = 20;
                    jogador.velocidade = 7;
                    jogador.precisao = 7;
                    jogador.magia = 2;
                }
                case 2 -> {
                    jogador.vida = 80;
                    jogador.forca = 5;
                    jogador.velocidade = 8;
                    jogador.precisao = 8;
                    jogador.magia = 20;
                }
                case 3 -> {
                    jogador.vida = 90;
                    jogador.forca = 6;
                    jogador.velocidade = 12;
                    jogador.precisao = 20;
                    jogador.magia = 5;
                }
                default -> {
                    System.out.println("Opção inválida!");
                    return;
                }
            }
// Exibe os atributos do personagem 
            System.out.println("\n                      ");
            System.out.println("          SEU PERSONAGEM");
            System.out.println("\n                        ");

            System.out.println("Nome: " + jogador.nome);
            System.out.println("Vida: " + jogador.vida);
            System.out.println("Força: " + jogador.forca);
            System.out.println("Velocidade: " + jogador.velocidade);
            System.out.println("Precisão: " + jogador.precisao);
            System.out.println("Magia: " + jogador.magia);
// Pergunta ao jogador se deseja iniciar a batalha
            System.out.println("\n                      ");
            System.out.println("Deseja iniciar uma batalha? (S/N)");
            resposta.opcao = scanner.next();
// Avalia a resposta do jogador e executa a ação 
            switch (resposta.opcao.toUpperCase()) {
                case "S" -> System.out.println("Iniciando a batalha...");
                case "N" -> {
                    System.out.println("Saindo do jogo...");
                    
                }
                default -> {
                    System.out.println("Opção inválida!");
                
                }
            }

        
            

            

        }
    }
}
