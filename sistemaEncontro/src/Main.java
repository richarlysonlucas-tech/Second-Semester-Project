public class Main {
    
    public static void main(String[] args) {
       
        int[][] mapa = new int[][]; 


        int heroiX = 0;
        int heroiY = 0;

       
        int inimigoX = 0;
        int inimigoY = 0;

        System.out.println("O herói deu um passo para a posição [" + heroiX + "][" + heroiY + "]");

       
        if (heroiX == inimigoX && heroiY == inimigoY) {
            System.out.println("Gatilho ativado: Inimigo detectado! Iniciando batalha...");
        } else {
            System.out.println("Caminho livre. Continue explorando os arredores.");
        }

    }

}
