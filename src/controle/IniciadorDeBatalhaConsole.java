package controle;

import modelo.Encontro;

/**
 * Implementação de demonstração. A equipe da batalha pode trocar esta classe
 * por outra implementação sem mudar a regra de encontros.
 */
public class IniciadorDeBatalhaConsole implements IniciadorDeBatalha {
    @Override
    public void iniciar(Encontro encontro) {
        System.out.println("BATALHA: jogador " + encontro.getJogador().getId()
            + " encontrou " + encontro.getInimigo().getNome() + ".");
    }
}
