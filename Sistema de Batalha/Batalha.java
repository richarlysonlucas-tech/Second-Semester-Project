public class Batalha {

    private boolean batalhaAtiva;
    private boolean turnoJogador;

    public void iniciar() {
        batalhaAtiva = true;
        turnoJogador = true;

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

    public boolean ehTurnoJogador() {
        return turnoJogador;
    }

    public void passarTurno() {
        turnoJogador = !turnoJogador;
    }
}