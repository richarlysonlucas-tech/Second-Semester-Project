import java.util.Arrays;
import modelo.GatilhoArea;
import modelo.GatilhoAcao;
import modelo.EntidadeMundo;

import controle.ControladorEncontro;
import controle.IniciadorDeBatalhaConsole;
import modelo.AreaEncontro;
import modelo.Inimigo;
import servico.ServicoEncontro;

public class Principal {
    public static void main(String[] args) {
        IniciadorDeBatalhaConsole batalha = new IniciadorDeBatalhaConsole();
        ServicoEncontro servico = new ServicoEncontro(batalha);

        Inimigo slime = new Inimigo("SLIME_01", "Slime Verde", 1);
        Inimigo morcego = new Inimigo("MORCEGO_01", "Morcego Sombrio", 2);
        AreaEncontro floresta = new AreaEncontro(
            "FLORESTA_01", 0, 0, 100, 100, 35, Arrays.asList(slime, morcego)
        );

        ControladorEncontro controlador = new ControladorEncontro(servico);
        controlador.cadastrarArea(floresta);
        controlador.cadastrarGatilho(new GatilhoArea(
            "PORTAL", 10, 80, 80, 5, 5, slime
        ));
        controlador.cadastrarGatilho(new GatilhoAcao(
            "BAU", "ABRIR_BAU", 20, morcego
        ));
        controlador.cadastrarEntidade(new EntidadeMundo(
            "LOBISOMEM", 55, 55, 4, morcego
        ));

        System.out.println(controlador.verificarPresenca("jogador-1", 50, 50, "FLORESTA_01"));
    }
}
