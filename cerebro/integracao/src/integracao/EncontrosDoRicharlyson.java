package integracao;

import java.util.Random;

import controle.ControladorEncontro;
import controle.IniciadorDeBatalha;
import modelo.Encontro;
import portas.FonteDeEncontros;
import servico.ServicoEncontro;

/**
 * Liga o módulo de encontros (ControladorEncontro, do Richarlyson) à porta {@link FonteDeEncontros}.
 *
 * O ServicoEncontro chama IniciadorDeBatalha.iniciar(...) sozinho a cada encontro. Quem decide
 * quando a batalha começa é o cérebro, então aqui usamos um iniciador que não faz nada: a informação
 * do encontro chega ao cérebro pelo JSON que o ControladorEncontro devolve.
 */
public final class EncontrosDoRicharlyson {
    private EncontrosDoRicharlyson() {
    }

    /** Iniciador que não faz nada (o cérebro inicia a batalha a partir do JSON). */
    public static IniciadorDeBatalha iniciadorNulo() {
        return new IniciadorDeBatalha() {
            @Override
            public void iniciar(Encontro encontro) {
                // intencionalmente vazio
            }
        };
    }

    /** Cria o controlador de encontros pronto para ser cadastrado com áreas, gatilhos e entidades. */
    public static ControladorEncontro criarControlador(Random sorteador) {
        return new ControladorEncontro(new ServicoEncontro(sorteador, iniciadorNulo()));
    }

    /** Adapta um controlador existente à porta do cérebro. */
    public static FonteDeEncontros de(final ControladorEncontro controlador) {
        return (jogadorId, x, y, areaId, acoes) -> controlador.verificarPresenca(jogadorId, x, y, areaId, acoes);
    }
}
