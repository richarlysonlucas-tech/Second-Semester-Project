package jogo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import estado.EstadoJogo;
import portas.EventoDoJogo;
import util.JsonSimples;

/**
 * O que o cérebro devolve ao front-end depois de cada pedido: deu certo?, em que estado o jogo ficou,
 * uma mensagem, os eventos (batalha) e dados extras (inimigo, resultado...). Vira JSON com paraJson().
 */
public final class RespostaDoJogo {
    private final boolean ok;
    private final EstadoJogo estado;
    private final String mensagem;
    private final List<EventoDoJogo> eventos;
    private final Map<String, Object> extras = new LinkedHashMap<String, Object>();

    private RespostaDoJogo(boolean ok, EstadoJogo estado, String mensagem, List<EventoDoJogo> eventos) {
        this.ok = ok;
        this.estado = estado;
        this.mensagem = mensagem == null ? "" : mensagem;
        this.eventos = eventos == null
            ? new ArrayList<EventoDoJogo>() : new ArrayList<EventoDoJogo>(eventos);
    }

    public static RespostaDoJogo ok(EstadoJogo estado, String mensagem) {
        return new RespostaDoJogo(true, estado, mensagem, null);
    }

    public static RespostaDoJogo ok(EstadoJogo estado, String mensagem, List<EventoDoJogo> eventos) {
        return new RespostaDoJogo(true, estado, mensagem, eventos);
    }

    public static RespostaDoJogo erro(EstadoJogo estado, String mensagem) {
        return new RespostaDoJogo(false, estado, mensagem, null);
    }

    public static RespostaDoJogo erro(EstadoJogo estado, String mensagem, List<EventoDoJogo> eventos) {
        return new RespostaDoJogo(false, estado, mensagem, eventos);
    }

    RespostaDoJogo extra(String chave, Object valor) {
        extras.put(chave, valor);
        return this;
    }

    public boolean isOk() { return ok; }
    public EstadoJogo getEstado() { return estado; }
    public String getMensagem() { return mensagem; }

    public List<EventoDoJogo> getEventos() {
        return Collections.unmodifiableList(eventos);
    }

    public Object getExtra(String chave) {
        return extras.get(chave);
    }

    public String paraJson() {
        Map<String, Object> raiz = new LinkedHashMap<String, Object>();
        raiz.put("ok", Boolean.valueOf(ok));
        raiz.put("estado", estado == null ? null : estado.name());
        raiz.put("mensagem", mensagem);
        List<Object> lista = new ArrayList<Object>();
        for (EventoDoJogo e : eventos) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("tipo", e.getTipo());
            item.put("texto", e.getTexto());
            lista.add(item);
        }
        raiz.put("eventos", lista);
        raiz.putAll(extras);
        return JsonSimples.escrever(raiz);
    }

    @Override
    public String toString() {
        return (ok ? "OK" : "ERRO") + " [" + estado + "] " + mensagem;
    }
}
