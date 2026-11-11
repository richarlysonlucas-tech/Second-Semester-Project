import java.util.Random;

/**
 * Cuida das REGRAS da batalha: rolar o dado, ver se acertou e aplicar o dano.
 * Separar isso das classes de personagem deixa o código organizado:
 * personagem guarda dados, calculadora aplica regras.
 */
public class CalculadoraDeBatalha {

    private final Random random;

    // Recebemos o Random de fora. Assim, nos testes, dá para passar
    // new Random(42) e obter sempre os mesmos resultados.
    public CalculadoraDeBatalha(Random random) {
        this.random = random;
    }

    /** Rola um "dado de 100 lados": devolve um número de 1 a 100. */
    public int rolarD100() {
        // nextInt(100) devolve de 0 a 99, então somamos 1.
        return random.nextInt(100) + 1;
    }

    /**
     * Regra de acerto: o golpe acerta se a rolagem for MENOR OU IGUAL à precisão.
     * Precisão 80 -> acerta com rolagens de 1 a 80 (80% de chance).
     * Precisão 100 -> acerta sempre.
     */
    public boolean acertou(Ataque ataque, int rolagem) {
        return rolagem <= ataque.getPrecisao();
    }

    /** Executa um turno de ataque completo e mostra tudo no console. */
    public void executarAtaque(Personagem atacante, Personagem alvo, Ataque ataque) {
        int rolagem = rolarD100();

        System.out.printf("%s usa %s (dano %d | precisão %d%%) - rolagem: %d%n",
                atacante.getNome(), ataque.getNome(),
                ataque.getDano(), ataque.getPrecisao(), rolagem);

        if (acertou(ataque, rolagem)) {
            alvo.receberDano(ataque.getDano());
            System.out.printf("  Acertou! %s perde %d de vida (restam %d/%d)%n",
                    alvo.getNome(), ataque.getDano(),
                    alvo.getVidaAtual(), alvo.getVidaMaxima());
        } else {
            System.out.println("  Errou o ataque");
        }
    }
}
