package integracao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

import batalha.Batalha;
import batalha.contratos.EventoBatalha;
import batalha.contratos.OpcaoAcao;
import batalha.contratos.TipoAtaque;
import estado.ResultadoBatalha;
import portas.BatalhaEmCurso;
import portas.ComandoBatalha;
import portas.EventoDoJogo;
import portas.OpcaoDoJogo;

/**
 * Adaptador: faz o motor de batalha por turnos (batalha.Batalha, do Vitor) cumprir a porta
 * {@link BatalhaEmCurso} do cérebro. Não altera nada do motor: só o chama e traduz os tipos.
 *
 * O motor só informa vitória/derrota/fuga; o resto do balanço vem de quem montou a batalha:
 * a vida do herói (um IntSupplier que lê o personagem real), os fragmentos de recompensa
 * e, opcionalmente, a flag a registrar quando o inimigo for derrotado.
 */
public class BatalhaViaMotorV2 implements BatalhaEmCurso {
    private final Batalha batalha;
    private final IntSupplier vidaDoHeroi;
    private final int fragmentosPorVitoria;
    private final String flagDerrotado;

    public BatalhaViaMotorV2(Batalha batalha, IntSupplier vidaDoHeroi,
            int fragmentosPorVitoria, String flagDerrotado) {
        if (batalha == null || vidaDoHeroi == null) {
            throw new IllegalArgumentException("batalha e vidaDoHeroi são obrigatórios.");
        }
        this.batalha = batalha;
        this.vidaDoHeroi = vidaDoHeroi;
        this.fragmentosPorVitoria = fragmentosPorVitoria;
        this.flagDerrotado = flagDerrotado;
    }

    @Override
    public List<EventoDoJogo> iniciar() {
        return converter(batalha.iniciar());
    }

    @Override
    public boolean foiIniciada() {
        return batalha.foiIniciada();
    }

    @Override
    public boolean terminou() {
        return batalha.terminou();
    }

    @Override
    public boolean ehTurnoInimigo() {
        return batalha.ehTurnoInimigo();
    }

    @Override
    public String menuAtual() {
        return batalha.getMenuAtual().name();
    }

    @Override
    public List<EventoDoJogo> executar(ComandoBatalha comando) {
        switch (comando.getTipo()) {
            case ABRIR_ATAQUES:
                return converter(batalha.abrirMenuAtaques());
            case ABRIR_MOCHILA:
                return converter(batalha.abrirMochila());
            case VOLTAR:
                return converter(batalha.voltarMenuPrincipal());
            case FUGIR:
                return converter(batalha.tentarFugir());
            case TURNO_INIMIGO:
                return converter(batalha.executarTurnoInimigo());
            case USAR_ITEM:
                return converter(batalha.usarItem(comando.getId()));
            case ATACAR:
                TipoAtaque tipo = tipoDe(comando.getCategoria());
                if (tipo == null) {
                    return Collections.singletonList(new EventoDoJogo("ACAO_INVALIDA",
                        "Categoria de ataque desconhecida: " + comando.getCategoria() + "."));
                }
                return converter(batalha.escolherAtaque(tipo, comando.getId()));
            default:
                return Collections.singletonList(new EventoDoJogo("ACAO_INVALIDA", "Comando desconhecido."));
        }
    }

    @Override
    public List<OpcaoDoJogo> listarAtaques(String categoria) {
        TipoAtaque tipo = tipoDe(categoria);
        return tipo == null ? Collections.<OpcaoDoJogo>emptyList() : opcoes(batalha.listarAtaques(tipo));
    }

    @Override
    public List<OpcaoDoJogo> listarItens() {
        return opcoes(batalha.listarItens());
    }

    @Override
    public ResultadoBatalha resultado() {
        switch (batalha.getEstado()) {
            case VITORIA:
                return ResultadoBatalha.VITORIA;
            case DERROTA:
                return ResultadoBatalha.DERROTA;
            case FUGA:
                return ResultadoBatalha.FUGA;
            default:
                return null;
        }
    }

    @Override
    public int vidaRestanteDoHeroi() {
        return vidaDoHeroi.getAsInt();
    }

    @Override
    public int fragmentosGanhos() {
        return fragmentosPorVitoria;
    }

    @Override
    public String flagDerrotado() {
        return flagDerrotado;
    }

    // ---------------------------------------------------------------- tradução

    private static TipoAtaque tipoDe(String categoria) {
        if (categoria == null) {
            return null;
        }
        try {
            return TipoAtaque.valueOf(categoria.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<EventoDoJogo> converter(List<EventoBatalha> eventos) {
        List<EventoDoJogo> saida = new ArrayList<EventoDoJogo>();
        for (EventoBatalha e : eventos) {
            if (e != null) {
                saida.add(new EventoDoJogo(e.getTipo().name(), e.getTexto()));
            }
        }
        return saida;
    }

    private static List<OpcaoDoJogo> opcoes(List<OpcaoAcao> origem) {
        List<OpcaoDoJogo> saida = new ArrayList<OpcaoDoJogo>();
        for (OpcaoAcao o : origem) {
            saida.add(new OpcaoDoJogo(o.getId(), o.getNome()));
        }
        return saida;
    }
}
