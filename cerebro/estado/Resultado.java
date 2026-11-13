package estado;

/**
 * Resposta de uma operação do controlador: deu certo ou não, e por quê.
 * O controlador nunca lança exceção por uso inválido; ele recusa e explica.
 */
public class Resultado {
    private final boolean ok;
    private final String mensagem;

    private Resultado(boolean ok, String mensagem) {
        this.ok = ok;
        this.mensagem = mensagem;
    }

    public static Resultado ok(String mensagem) {
        return new Resultado(true, mensagem);
    }

    public static Resultado erro(String mensagem) {
        return new Resultado(false, mensagem);
    }

    public boolean isOk() {
        return ok;
    }

    public String getMensagem() {
        return mensagem;
    }

    @Override
    public String toString() {
        return (ok ? "OK: " : "ERRO: ") + mensagem;
    }
}
