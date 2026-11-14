package portas;

/** Um encontro que o módulo de encontros mandou iniciar. Imutável. */
public final class EncontroDetectado {
    private final String inimigoId;
    private final String inimigoNome;
    private final int inimigoNivel;
    private final String origem;

    public EncontroDetectado(String inimigoId, String inimigoNome, int inimigoNivel, String origem) {
        this.inimigoId = inimigoId;
        this.inimigoNome = inimigoNome;
        this.inimigoNivel = inimigoNivel;
        this.origem = origem;
    }

    public String getInimigoId() {
        return inimigoId;
    }

    public String getInimigoNome() {
        return inimigoNome;
    }

    public int getInimigoNivel() {
        return inimigoNivel;
    }

    /** "ALEATORIO", "GATILHO_<id>" ou "PROXIMIDADE_<id>", como informado pelo módulo de encontros. */
    public String getOrigem() {
        return origem;
    }
}
