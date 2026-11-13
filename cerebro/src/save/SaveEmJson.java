package save;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import estado.DadosDoJogo;

/**
 * Save em arquivos JSON, um por slot: slot1.json, slot2.json, ...
 *
 * Proteções:
 * - gravação atômica (escreve em .tmp e renomeia), para não deixar arquivo pela metade;
 * - backup automático do save anterior (.bak), usado se o principal estiver corrompido;
 * - versão do formato, para rejeitar saves de uma versão mais nova;
 * - checksum, para detectar arquivo corrompido ou alterado;
 * - validação dos dados antes de gravar e depois de carregar.
 *
 * Formato do arquivo:
 * { "versao": 1, "checksum": "abcd1234", "dados": { ... } }
 */
public class SaveEmJson implements SaveSystem {
    public static final int VERSAO_ATUAL = 1;
    public static final int MAX_SLOTS = 3;

    private final Path pasta;
    private boolean ultimaCargaUsouBackup;

    public SaveEmJson(Path pasta) {
        this.pasta = pasta;
    }

    @Override
    public void salvar(int slot, DadosDoJogo dados) throws SaveException {
        validarSlot(slot);
        if (dados == null) {
            throw new SaveException("Não há dados para salvar.");
        }
        try {
            dados.validar();
        } catch (IllegalArgumentException e) {
            throw new SaveException("Dados inválidos, save cancelado: " + e.getMessage(), e);
        }

        Map<String, Object> mapaDados = paraMapa(dados);
        Map<String, Object> envelope = new LinkedHashMap<String, Object>();
        envelope.put("versao", VERSAO_ATUAL);
        envelope.put("checksum", checksum(JsonSimples.escrever(mapaDados)));
        envelope.put("dados", mapaDados);
        byte[] bytes = JsonSimples.escrever(envelope).getBytes(StandardCharsets.UTF_8);

        Path principal = arquivo(slot);
        Path temporario = pasta.resolve("slot" + slot + ".json.tmp");
        try {
            Files.createDirectories(pasta);
            Files.write(temporario, bytes);
            if (Files.exists(principal)) {
                Files.copy(principal, backup(slot), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temporario, principal,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, principal, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new SaveException("Não foi possível gravar o slot " + slot + ": " + e.getMessage(), e);
        }
    }

    @Override
    public DadosDoJogo carregar(int slot) throws SaveException {
        validarSlot(slot);
        ultimaCargaUsouBackup = false;
        Path principal = arquivo(slot);
        Path backup = backup(slot);

        if (!Files.exists(principal) && !Files.exists(backup)) {
            throw new SaveException("O slot " + slot + " está vazio.");
        }

        SaveException falhaPrincipal;
        if (Files.exists(principal)) {
            try {
                return lerArquivo(principal);
            } catch (SaveException e) {
                falhaPrincipal = e;
            }
        } else {
            falhaPrincipal = new SaveException("Arquivo principal do slot " + slot + " não existe.");
        }

        if (Files.exists(backup)) {
            try {
                DadosDoJogo dados = lerArquivo(backup);
                ultimaCargaUsouBackup = true;
                return dados;
            } catch (SaveException e) {
                throw new SaveException("Save e backup do slot " + slot + " estão inutilizáveis. "
                    + "Principal: " + falhaPrincipal.getMessage() + " Backup: " + e.getMessage(), e);
            }
        }
        throw falhaPrincipal;
    }

    @Override
    public boolean existe(int slot) {
        if (slot < 1 || slot > MAX_SLOTS) {
            return false;
        }
        return Files.exists(arquivo(slot)) || Files.exists(backup(slot));
    }

    @Override
    public void apagar(int slot) throws SaveException {
        validarSlot(slot);
        try {
            Files.deleteIfExists(arquivo(slot));
            Files.deleteIfExists(backup(slot));
            Files.deleteIfExists(pasta.resolve("slot" + slot + ".json.tmp"));
        } catch (IOException e) {
            throw new SaveException("Não foi possível apagar o slot " + slot + ": " + e.getMessage(), e);
        }
    }

    /** true se a última chamada de carregar() precisou recorrer ao backup. */
    public boolean ultimaCargaUsouBackup() {
        return ultimaCargaUsouBackup;
    }

    // ------------------------------------------------------------ internos

    private DadosDoJogo lerArquivo(Path caminho) throws SaveException {
        String texto;
        try {
            texto = new String(Files.readAllBytes(caminho), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SaveException("Não foi possível ler " + caminho.getFileName() + ": " + e.getMessage(), e);
        }

        Object raiz;
        try {
            raiz = JsonSimples.ler(texto);
        } catch (IllegalArgumentException e) {
            throw new SaveException("Arquivo corrompido (" + caminho.getFileName() + "): " + e.getMessage(), e);
        }
        if (!(raiz instanceof Map)) {
            throw new SaveException("Arquivo corrompido: formato inesperado.");
        }
        Map<?, ?> envelope = (Map<?, ?>) raiz;

        long versao = numero(envelope.get("versao"), "versao");
        if (versao > VERSAO_ATUAL) {
            throw new SaveException("Este save é da versão " + versao
                + ", mais nova que a suportada (" + VERSAO_ATUAL + "). Atualize o jogo.");
        }
        if (versao < 1) {
            throw new SaveException("Versão de save inválida: " + versao + ".");
        }
        // Quando existir a versão 2, as migrações entram aqui (versao 1 -> 2 -> ...).

        Object bruto = envelope.get("dados");
        if (!(bruto instanceof Map)) {
            throw new SaveException("Arquivo corrompido: campo 'dados' ausente.");
        }
        DadosDoJogo dados = deMapa((Map<?, ?>) bruto);

        try {
            dados.validar();
        } catch (IllegalArgumentException e) {
            throw new SaveException("Save com dados inválidos: " + e.getMessage(), e);
        }

        Object esperado = envelope.get("checksum");
        String calculado = checksum(JsonSimples.escrever(paraMapa(dados)));
        if (!calculado.equals(esperado)) {
            throw new SaveException("Arquivo corrompido ou alterado (checksum não confere).");
        }
        return dados;
    }

    private static Map<String, Object> paraMapa(DadosDoJogo d) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("nomeHeroi", d.getNomeHeroi());
        m.put("classeHeroi", d.getClasseHeroi());
        m.put("vidaAtual", d.getVidaAtual());
        m.put("vidaMaxima", d.getVidaMaxima());
        m.put("nivel", d.getNivel());
        m.put("posicaoX", d.getPosicaoX());
        m.put("posicaoY", d.getPosicaoY());
        m.put("areaId", d.getAreaId());
        m.put("estadoSalvo", d.getEstadoSalvo());
        m.put("fragmentosColetados", d.getFragmentosColetados());
        m.put("fragmentosPurificados", d.getFragmentosPurificados());
        m.put("marcaDoEquilibrio", d.getMarcaDoEquilibrio());
        m.put("inventario", new LinkedHashMap<String, Integer>(d.getInventario()));
        m.put("flagsMundo", new ArrayList<String>(d.getFlagsMundo()));
        return m;
    }

    private static DadosDoJogo deMapa(Map<?, ?> m) throws SaveException {
        DadosDoJogo d = new DadosDoJogo();
        d.setNomeHeroi(texto(m.get("nomeHeroi"), "nomeHeroi"));
        d.setClasseHeroi(texto(m.get("classeHeroi"), "classeHeroi"));
        d.setVidaAtual(inteiro(m.get("vidaAtual"), "vidaAtual"));
        d.setVidaMaxima(inteiro(m.get("vidaMaxima"), "vidaMaxima"));
        d.setNivel(inteiro(m.get("nivel"), "nivel"));
        d.setPosicao(decimal(m.get("posicaoX"), "posicaoX"),
            decimal(m.get("posicaoY"), "posicaoY"),
            texto(m.get("areaId"), "areaId"));
        d.setEstadoSalvo(texto(m.get("estadoSalvo"), "estadoSalvo"));
        d.setFragmentosColetados(inteiro(m.get("fragmentosColetados"), "fragmentosColetados"));
        d.setFragmentosPurificados(inteiro(m.get("fragmentosPurificados"), "fragmentosPurificados"));
        d.setMarcaDoEquilibrio(inteiro(m.get("marcaDoEquilibrio"), "marcaDoEquilibrio"));

        Object inv = m.get("inventario");
        if (!(inv instanceof Map)) {
            throw new SaveException("Arquivo corrompido: 'inventario' ausente ou inválido.");
        }
        for (Map.Entry<?, ?> e : ((Map<?, ?>) inv).entrySet()) {
            d.getInventario().put(String.valueOf(e.getKey()), inteiro(e.getValue(), "inventario." + e.getKey()));
        }

        Object flags = m.get("flagsMundo");
        if (!(flags instanceof List)) {
            throw new SaveException("Arquivo corrompido: 'flagsMundo' ausente ou inválido.");
        }
        for (Object flag : (List<?>) flags) {
            d.getFlagsMundo().add(texto(flag, "flagsMundo"));
        }
        return d;
    }

    private static String texto(Object v, String campo) throws SaveException {
        if (!(v instanceof String)) {
            throw new SaveException("Arquivo corrompido: campo '" + campo + "' ausente ou inválido.");
        }
        return (String) v;
    }

    private static long numero(Object v, String campo) throws SaveException {
        if (!(v instanceof Long)) {
            throw new SaveException("Arquivo corrompido: campo '" + campo + "' ausente ou inválido.");
        }
        return ((Long) v).longValue();
    }

    private static int inteiro(Object v, String campo) throws SaveException {
        long n = numero(v, campo);
        if (n < Integer.MIN_VALUE || n > Integer.MAX_VALUE) {
            throw new SaveException("Arquivo corrompido: campo '" + campo + "' fora do limite.");
        }
        return (int) n;
    }

    private static double decimal(Object v, String campo) throws SaveException {
        if (v instanceof Double || v instanceof Long) {
            return ((Number) v).doubleValue();
        }
        throw new SaveException("Arquivo corrompido: campo '" + campo + "' ausente ou inválido.");
    }

    private static String checksum(String texto) {
        CRC32 crc = new CRC32();
        crc.update(texto.getBytes(StandardCharsets.UTF_8));
        return Long.toHexString(crc.getValue());
    }

    private void validarSlot(int slot) throws SaveException {
        if (slot < 1 || slot > MAX_SLOTS) {
            throw new SaveException("Slot inválido: " + slot + ". Use de 1 a " + MAX_SLOTS + ".");
        }
    }

    private Path arquivo(int slot) {
        return pasta.resolve("slot" + slot + ".json");
    }

    private Path backup(int slot) {
        return pasta.resolve("slot" + slot + ".json.bak");
    }
}
