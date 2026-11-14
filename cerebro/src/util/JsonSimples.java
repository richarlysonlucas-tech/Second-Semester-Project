package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Leitor e escritor de JSON mínimo, sem dependências externas.
 * Suporta objetos, listas, textos, números, booleanos e null.
 * Números inteiros viram Long; números com ponto ou expoente viram Double.
 */
public final class JsonSimples {
    private static final int PROFUNDIDADE_MAXIMA = 32;

    private final String texto;
    private int posicao;

    private JsonSimples(String texto) {
        this.texto = texto;
    }

    // ---------------------------------------------------------------- escrita

    public static String escrever(Object valor) {
        StringBuilder sb = new StringBuilder();
        escrever(valor, sb);
        return sb.toString();
    }

    private static void escrever(Object valor, StringBuilder sb) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof String) {
            escreverTexto((String) valor, sb);
        } else if (valor instanceof Boolean) {
            sb.append(valor.toString());
        } else if (valor instanceof Double || valor instanceof Float) {
            double d = ((Number) valor).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new IllegalArgumentException("Número não finito não pode virar JSON.");
            }
            sb.append(Double.toString(d));
        } else if (valor instanceof Number) {
            sb.append(((Number) valor).longValue());
        } else if (valor instanceof Map) {
            sb.append('{');
            boolean primeiro = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) valor).entrySet()) {
                if (!primeiro) {
                    sb.append(',');
                }
                primeiro = false;
                escreverTexto(String.valueOf(e.getKey()), sb);
                sb.append(':');
                escrever(e.getValue(), sb);
            }
            sb.append('}');
        } else if (valor instanceof Collection) {
            sb.append('[');
            boolean primeiro = true;
            for (Object item : (Collection<?>) valor) {
                if (!primeiro) {
                    sb.append(',');
                }
                primeiro = false;
                escrever(item, sb);
            }
            sb.append(']');
        } else {
            throw new IllegalArgumentException("Tipo não suportado: " + valor.getClass().getName());
        }
    }

    private static void escreverTexto(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    // ---------------------------------------------------------------- leitura

    /** @throws IllegalArgumentException se o texto não for um JSON válido */
    public static Object ler(String texto) {
        JsonSimples leitor = new JsonSimples(texto);
        Object valor = leitor.lerValor(0);
        leitor.pularEspacos();
        if (leitor.posicao != texto.length()) {
            throw leitor.erro("conteúdo extra após o fim do JSON");
        }
        return valor;
    }

    private Object lerValor(int profundidade) {
        if (profundidade > PROFUNDIDADE_MAXIMA) {
            throw erro("JSON aninhado demais");
        }
        pularEspacos();
        if (posicao >= texto.length()) {
            throw erro("fim inesperado");
        }
        char c = texto.charAt(posicao);
        if (c == '{') {
            return lerObjeto(profundidade);
        } else if (c == '[') {
            return lerLista(profundidade);
        } else if (c == '"') {
            return lerTexto();
        } else if (c == 't' || c == 'f' || c == 'n') {
            return lerLiteral();
        }
        return lerNumero();
    }

    private Map<String, Object> lerObjeto(int profundidade) {
        Map<String, Object> mapa = new LinkedHashMap<String, Object>();
        posicao++; // {
        pularEspacos();
        if (olhar() == '}') {
            posicao++;
            return mapa;
        }
        while (true) {
            pularEspacos();
            if (olhar() != '"') {
                throw erro("esperava o nome de um campo");
            }
            String chave = lerTexto();
            pularEspacos();
            esperar(':');
            mapa.put(chave, lerValor(profundidade + 1));
            pularEspacos();
            char c = olhar();
            posicao++;
            if (c == '}') {
                return mapa;
            }
            if (c != ',') {
                throw erro("esperava ',' ou '}'");
            }
        }
    }

    private List<Object> lerLista(int profundidade) {
        List<Object> lista = new ArrayList<Object>();
        posicao++; // [
        pularEspacos();
        if (olhar() == ']') {
            posicao++;
            return lista;
        }
        while (true) {
            lista.add(lerValor(profundidade + 1));
            pularEspacos();
            char c = olhar();
            posicao++;
            if (c == ']') {
                return lista;
            }
            if (c != ',') {
                throw erro("esperava ',' ou ']'");
            }
        }
    }

    private String lerTexto() {
        StringBuilder sb = new StringBuilder();
        posicao++; // aspas de abertura
        while (true) {
            if (posicao >= texto.length()) {
                throw erro("texto não terminado");
            }
            char c = texto.charAt(posicao++);
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (posicao >= texto.length()) {
                throw erro("escape incompleto");
            }
            char e = texto.charAt(posicao++);
            switch (e) {
                case '"':  sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/':  sb.append('/'); break;
                case 'n':  sb.append('\n'); break;
                case 'r':  sb.append('\r'); break;
                case 't':  sb.append('\t'); break;
                case 'b':  sb.append('\b'); break;
                case 'f':  sb.append('\f'); break;
                case 'u':
                    if (posicao + 4 > texto.length()) {
                        throw erro("escape \\u incompleto");
                    }
                    try {
                        sb.append((char) Integer.parseInt(texto.substring(posicao, posicao + 4), 16));
                    } catch (NumberFormatException ex) {
                        throw erro("escape \\u inválido");
                    }
                    posicao += 4;
                    break;
                default:
                    throw erro("escape desconhecido");
            }
        }
    }

    private Object lerLiteral() {
        if (texto.startsWith("true", posicao)) {
            posicao += 4;
            return Boolean.TRUE;
        }
        if (texto.startsWith("false", posicao)) {
            posicao += 5;
            return Boolean.FALSE;
        }
        if (texto.startsWith("null", posicao)) {
            posicao += 4;
            return null;
        }
        throw erro("valor desconhecido");
    }

    private Object lerNumero() {
        int inicio = posicao;
        boolean decimal = false;
        while (posicao < texto.length()) {
            char c = texto.charAt(posicao);
            if (c == '.' || c == 'e' || c == 'E') {
                decimal = true;
            } else if (!(c == '-' || c == '+' || (c >= '0' && c <= '9'))) {
                break;
            }
            posicao++;
        }
        String numero = texto.substring(inicio, posicao);
        if (numero.isEmpty()) {
            throw erro("valor inesperado");
        }
        try {
            return decimal ? (Object) Double.valueOf(numero) : (Object) Long.valueOf(numero);
        } catch (NumberFormatException ex) {
            throw erro("número inválido");
        }
    }

    private void pularEspacos() {
        while (posicao < texto.length() && Character.isWhitespace(texto.charAt(posicao))) {
            posicao++;
        }
    }

    private char olhar() {
        if (posicao >= texto.length()) {
            throw erro("fim inesperado");
        }
        return texto.charAt(posicao);
    }

    private void esperar(char esperado) {
        if (olhar() != esperado) {
            throw erro("esperava '" + esperado + "'");
        }
        posicao++;
    }

    private IllegalArgumentException erro(String motivo) {
        return new IllegalArgumentException("JSON inválido na posição " + posicao + ": " + motivo + ".");
    }
}
