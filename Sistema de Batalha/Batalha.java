public class Batalha {

    private boolean batalhaAtiva;

    public void iniciar() {
        batalhaAtiva = true;

        System.out.println("Batalha iniciada!");
    }

    public void atacar() {
        System.out.println("Jogador realizou um ataque!");
    }

    public void abrirMochila() {
        System.out.println("Jogador abriu a mochila!");
    }

    public void fugir() {
        batalhaAtiva = false;

        System.out.println("Jogador fugiu da batalha!");
    }

    public boolean batalhaAtiva() {
        return batalhaAtiva;
    }
}