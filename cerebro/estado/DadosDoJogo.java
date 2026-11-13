package estado;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Retrato (snapshot) do progresso do jogador. É exatamente isto que vai para o save.
 * Guarda só dados simples, sem referência a objetos vivos (batalha, motor, etc.).
 */
public class DadosDoJogo {
    private String nomeHeroi;
    private String classeHeroi;
    private int vidaAtual;
    private int vidaMaxima;
    private int nivel;

    private double posicaoX;
    private double posicaoY;
    private String areaId;

    /** Onde o jogador estava ao salvar: "EXPLORACAO" ou "HUB". */
    private String estadoSalvo;

    private int fragmentosColetados;
    private int fragmentosPurificados;
    private int marcaDoEquilibrio;

    private final Map<String, Integer> inventario = new TreeMap<String, Integer>();

    /** Marcas permanentes do mundo: "bau:BAU_01", "derrotado:REI_SOMBRAS", "bioma:FLORESTA"... */
    private final Set<String> flagsMundo = new TreeSet<String>();

    public DadosDoJogo() {
        this.estadoSalvo = EstadoJogo.EXPLORACAO.name();
        this.nivel = 1;
        this.areaId = "INICIO";
    }

    public static DadosDoJogo novo(String nomeHeroi, String classeHeroi, int vidaMaxima) {
        DadosDoJogo d = new DadosDoJogo();
        d.nomeHeroi = nomeHeroi;
        d.classeHeroi = classeHeroi;
        d.vidaMaxima = vidaMaxima;
        d.vidaAtual = vidaMaxima;
        return d;
    }

    /** Cópia profunda: quem recebe a cópia não consegue alterar o estado original. */
    public DadosDoJogo copiar() {
        DadosDoJogo c = new DadosDoJogo();
        c.nomeHeroi = nomeHeroi;
        c.classeHeroi = classeHeroi;
        c.vidaAtual = vidaAtual;
        c.vidaMaxima = vidaMaxima;
        c.nivel = nivel;
        c.posicaoX = posicaoX;
        c.posicaoY = posicaoY;
        c.areaId = areaId;
        c.estadoSalvo = estadoSalvo;
        c.fragmentosColetados = fragmentosColetados;
        c.fragmentosPurificados = fragmentosPurificados;
        c.marcaDoEquilibrio = marcaDoEquilibrio;
        c.inventario.putAll(inventario);
        c.flagsMundo.addAll(flagsMundo);
        return c;
    }

    /**
     * Confere se os dados fazem sentido. Usado antes de salvar e depois de carregar,
     * para nunca gravar nem aceitar um estado impossível.
     *
     * @throws IllegalArgumentException com o motivo
     */
    public void validar() {
        exigir(nomeHeroi != null && !nomeHeroi.trim().isEmpty(), "nomeHeroi é obrigatório.");
        exigir(classeHeroi != null && !classeHeroi.trim().isEmpty(), "classeHeroi é obrigatória.");
        exigir(vidaMaxima > 0, "vidaMaxima deve ser maior que zero.");
        exigir(vidaAtual >= 0 && vidaAtual <= vidaMaxima, "vidaAtual deve estar entre 0 e vidaMaxima.");
        exigir(nivel >= 1, "nivel deve ser pelo menos 1.");
        exigir(!Double.isNaN(posicaoX) && !Double.isInfinite(posicaoX)
            && !Double.isNaN(posicaoY) && !Double.isInfinite(posicaoY), "posição inválida.");
        exigir(areaId != null && !areaId.trim().isEmpty(), "areaId é obrigatório.");
        exigir(EstadoJogo.EXPLORACAO.name().equals(estadoSalvo) || EstadoJogo.HUB.name().equals(estadoSalvo),
            "estadoSalvo deve ser EXPLORACAO ou HUB.");
        exigir(fragmentosColetados >= 0 && fragmentosPurificados >= 0, "fragmentos não podem ser negativos.");
        exigir(marcaDoEquilibrio >= 0, "marcaDoEquilibrio não pode ser negativa.");
        for (Map.Entry<String, Integer> item : inventario.entrySet()) {
            exigir(item.getKey() != null && !item.getKey().trim().isEmpty(), "item sem id no inventário.");
            exigir(item.getValue() != null && item.getValue() > 0, "quantidade inválida para o item " + item.getKey() + ".");
        }
        for (String flag : flagsMundo) {
            exigir(flag != null && !flag.trim().isEmpty(), "flag de mundo vazia.");
        }
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public String getNomeHeroi() { return nomeHeroi; }
    public void setNomeHeroi(String nomeHeroi) { this.nomeHeroi = nomeHeroi; }

    public String getClasseHeroi() { return classeHeroi; }
    public void setClasseHeroi(String classeHeroi) { this.classeHeroi = classeHeroi; }

    public int getVidaAtual() { return vidaAtual; }
    public void setVidaAtual(int vidaAtual) { this.vidaAtual = vidaAtual; }

    public int getVidaMaxima() { return vidaMaxima; }
    public void setVidaMaxima(int vidaMaxima) { this.vidaMaxima = vidaMaxima; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public double getPosicaoX() { return posicaoX; }
    public double getPosicaoY() { return posicaoY; }
    public String getAreaId() { return areaId; }

    public void setPosicao(double posicaoX, double posicaoY, String areaId) {
        this.posicaoX = posicaoX;
        this.posicaoY = posicaoY;
        this.areaId = areaId;
    }

    public String getEstadoSalvo() { return estadoSalvo; }
    public void setEstadoSalvo(String estadoSalvo) { this.estadoSalvo = estadoSalvo; }

    public int getFragmentosColetados() { return fragmentosColetados; }
    public void setFragmentosColetados(int valor) { this.fragmentosColetados = valor; }

    public int getFragmentosPurificados() { return fragmentosPurificados; }
    public void setFragmentosPurificados(int valor) { this.fragmentosPurificados = valor; }

    public int getMarcaDoEquilibrio() { return marcaDoEquilibrio; }
    public void setMarcaDoEquilibrio(int valor) { this.marcaDoEquilibrio = valor; }

    /** Acesso direto ao mapa (usado pelo controlador e pelo save). */
    public Map<String, Integer> getInventario() { return inventario; }

    /** Acesso direto ao conjunto (usado pelo controlador e pelo save). */
    public Set<String> getFlagsMundo() { return flagsMundo; }
}
