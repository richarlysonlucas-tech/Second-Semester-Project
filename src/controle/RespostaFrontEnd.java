package controle;

/**
 * Estrutura enviada ao front-end. O controle não expõe o modelo interno inteiro.
 */
public class RespostaFrontEnd {
    private String acao;
    private String jogadorId;
    private String areaId;
    private String inimigoId;
    private String inimigoNome;
    private int inimigoNivel;
    private String mensagem;
    private String origem;

    public RespostaFrontEnd(String acao, String jogadorId, String areaId, String mensagem) {
        this.acao = acao;
        this.jogadorId = jogadorId;
        this.areaId = areaId;
        this.mensagem = mensagem;
    }

    public void definirInimigo(String inimigoId, String inimigoNome, int inimigoNivel) {
        this.inimigoId = inimigoId;
        this.inimigoNome = inimigoNome;
        this.inimigoNivel = inimigoNivel;
    }

    public void definirOrigem(String origem) {
        this.origem = origem;
    }

    public String paraJson() {
        StringBuilder json = new StringBuilder();
        json.append("{")
            .append("\"acao\":\"").append(acao).append("\"")
            .append(",\"jogadorId\":\"").append(escape(jogadorId)).append("\"")
            .append(",\"areaId\":\"").append(escape(areaId)).append("\"")
            .append(",\"mensagem\":\"").append(escape(mensagem)).append("\"");
        if (origem != null) {
            json.append(",\"origem\":\"").append(escape(origem)).append("\"");
        }
        if (inimigoId != null) {
            json.append(",\"inimigo\":{\"id\":\"").append(escape(inimigoId)).append("\"")
                .append(",\"nome\":\"").append(escape(inimigoNome)).append("\"")
                .append(",\"nivel\":").append(inimigoNivel).append("}");
        }
        json.append("}");
        return json.toString();
    }

    private String escape(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
