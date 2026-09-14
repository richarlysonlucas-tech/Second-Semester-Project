import java.util.Scanner;

public class SistemaBatalha {

    public static void main(String[] args) {

        // =================================
        // PERSONAGEM DE TESTE
        // =================================
        try (Scanner scanner = new Scanner(System.in)) {
            // =================================
            // PERSONAGEM DE TESTE
            // =================================
            
            Personagem jogador = new Personagem();
            
            jogador.nome = "Vitor";
            jogador.vida = 120;
            jogador.forca = 20;
            jogador.velocidade = 10;
            jogador.precisao = 10;
            jogador.magia = 5;
            
            // =================================
            // INICIAR BATALHA
            // =================================
            
            Batalha batalha = new Batalha();
            
            boolean batalhaAtiva = true;
            
            while (batalhaAtiva) {
                
                System.out.println("\n");
                System.out.println("          BATALHA");
                System.out.println("");
                
                System.out.println("\n" + jogador.nome +
                        " - Vida: " + jogador.vida);
                
                System.out.println(batalha.nomeInimigo +
                        " - Vida: " + batalha.vidaInimigo);
                
                System.out.println("\n1 - Atacar");
                System.out.println("2 - Abrir Mochila");
                System.out.println("3 - Fugir");
                
                System.out.print("\nEscolha: ");
                
                int opcao = scanner.nextInt();
                
                switch (opcao) {
                    
                    // =================================
                    // ATACAR
                    // =================================
                    
                    case 1:
                        
                        System.out.println("\n");
                        System.out.println("           ATAQUES");
                        System.out.println("");
                        
                        System.out.println("1 - Ataque físico");
                        System.out.println("2 - Ataque mágico");
                        System.out.println("3 - Buffar");
                        
                        System.out.print("\nEscolha: ");
                        
                        int ataque = scanner.nextInt();
                        
                        switch (ataque) {
                            
                            case 1:
                                
                                int danoFisico = jogador.forca;
                                
                                batalha.vidaInimigo -= danoFisico;
                                
                                System.out.println("\nVocê atacou o "
                                        + batalha.nomeInimigo + "!");
                                
                                System.out.println("Dano causado: "
                                        + danoFisico);
                                
                                break;
                                
                            case 2:
                                
                                int danoMagico = jogador.magia;
                                
                                batalha.vidaInimigo -= danoMagico;
                                
                                System.out.println("\nVocê lançou uma magia!");
                                
                                System.out.println("Dano mágico causado: "
                                        + danoMagico);
                                
                                break;
                                
                            case 3:
                                
                                jogador.forca += 5;
                                
                                System.out.println("\nVocê aumentou sua força!");
                                
                                System.out.println("Força atual: "
                                        + jogador.forca);
                                
                                break;
                                
                            default:
                                
                                System.out.println("\nAtaque inválido!");
                                
                                break;
                        }
                        
                        // =================================
                        // VERIFICAR INIMIGO
                        // =================================
                        
                        if (batalha.vidaInimigo <= 0) {
                            
                            batalha.vidaInimigo = 0;
                            
                            System.out.println("\nVocê derrotou o "
                                    + batalha.nomeInimigo + "!");
                            
                            batalhaAtiva = false;
                            
                        } else {
                            
                            // Inimigo contra-ataca
                            batalha.ataqueInimigo(jogador);
                            
                            // =================================
                            // VERIFICAR JOGADOR
                            // =================================
                            
                            if (jogador.vida <= 0) {
                                
                                jogador.vida = 0;
                                
                                System.out.println("\nVocê foi derrotado!");
                                
                                batalhaAtiva = false;
                            }
                        }
                        
                        break;
                        
                        // =================================
                        // MOCHILA
                        // =================================
                        
                    case 2:
                        
                        System.out.println("\n");
                        System.out.println("          MOCHILA");
                        System.out.println("");
                        
                        System.out.println("1 - Poção de vida");
                        System.out.println("2 - Poção de força");
                        System.out.println("3 - Poção de velocidade");
                        System.out.println("4 - Poção de precisão");
                        System.out.println("5 - Poção de magia");
                        System.out.println("6 - Voltar");
                        
                        System.out.print("\nEscolha: ");
                        
                        int item = scanner.nextInt();
                        
                        switch (item) {
                            
                            case 1:
                                
                                jogador.vida += 30;
                                
                                System.out.println("\nVocê usou uma poção de vida!");
                                
                                System.out.println("Vida atual: "
                                        + jogador.vida);
                                
                                break;
                                
                            case 2:
                                
                                jogador.forca += 5;
                                
                                System.out.println("\nSua força aumentou!");
                                
                                System.out.println("Força atual: "
                                        + jogador.forca);
                                
                                break;
                                
                            case 3:
                                
                                jogador.velocidade += 5;
                                
                                System.out.println("\nSua velocidade aumentou!");
                                
                                System.out.println("Velocidade atual: "
                                        + jogador.velocidade);
                                
                                break;
                                
                            case 4:
                                
                                jogador.precisao += 5;
                                
                                System.out.println("\nSua precisão aumentou!");
                                
                                System.out.println("Precisão atual: "
                                        + jogador.precisao);
                                
                                break;
                                
                            case 5:
                                
                                jogador.magia += 5;
                                
                                System.out.println("\nSua magia aumentou!");
                                
                                System.out.println("Magia atual: "
                                        + jogador.magia);
                                
                                break;
                                
                            case 6:
                                
                                System.out.println("\nVoltando para a batalha...");
                                
                                break;
                                
                            default:
                                
                                System.out.println("\nItem inválido!");
                                
                                break;
                        }
                        
                        break;
                        
                        // =================================
                        // FUGIR
                        // =================================
                        
                    case 3:
                        
                        System.out.println("\nVocê fugiu da batalha!");
                        
                        batalhaAtiva = false;
                        
                        break;
                        
                    default:
                        
                        System.out.println("\nOpção inválida!");
                        
                        break;
                }
            }
        }
    }
}