package portas;

import java.util.Map;

import util.JsonSimples;

/**
 * Lê a resposta JSON do módulo de encontros e a transforma em {@link EncontroDetectado}.
 * Depende só do formato do JSON, não das classes do módulo.
 */
public final class EncontroViaJson {
    private EncontroViaJson() {
    }

    /**
     * @return o encontro, ou {@code null} se a resposta for SEM_ENCONTRO
     * @throws IllegalArgumentException se o JSON for inválido, incompleto ou indicar entrada inválida
     */
    public static EncontroDetectado ler(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("resposta vazia.");
        }
        Object raiz = JsonSimples.ler(json);
        if (!(raiz instanceof Map)) {
            throw new IllegalArgumentException("a resposta não é um objeto JSON.");
        }
        Map<?, ?> mapa = (Map<?, ?>) raiz;
        Object acao = mapa.get("acao");
        if (!(acao instanceof String)) {
            throw new IllegalArgumentException("campo 'acao' ausente.");
        }

        if ("SEM_ENCONTRO".equals(acao)) {
            return null;
        }
        if ("ENTRADA_INVALIDA".equals(acao)) {
            throw new IllegalArgumentException("entrada inválida: " + mapa.get("mensagem"));
        }
        if (!"INICIAR_BATALHA".equals(acao)) {
            throw new IllegalArgumentException("acao desconhecida: " + acao);
        }

        Object bruto = mapa.get("inimigo");
        if (!(bruto instanceof Map)) {
            throw new IllegalArgumentException("encontro sem dados do inimigo.");
        }
        Map<?, ?> inimigo = (Map<?, ?>) bruto;
        Object id = inimigo.get("id");
        if (!(id instanceof String) || ((String) id).trim().isEmpty()) {
            throw new IllegalArgumentException("inimigo sem id.");
        }
        Object nome = inimigo.get("nome");
        Object nivel = inimigo.get("nivel");
        Object origem = mapa.get("origem");
        return new EncontroDetectado(
            (String) id,
            nome instanceof String ? (String) nome : (String) id,
            nivel instanceof Long ? (int) Math.max(1L, Math.min(((Long) nivel).longValue(), 9999L)) : 1,
            origem instanceof String ? (String) origem : "ALEATORIO");
    }
}
