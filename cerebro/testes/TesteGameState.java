package testes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import estado.DadosDoJogo;
import estado.EstadoJogo;
import estado.GameStateController;
import estado.Resultado;
import estado.ResultadoBatalha;
import save.SaveEmJson;
import save.SaveException;

/**
 * Testes simples, sem framework, para executar com Java padrão.
 *
 * Compilar: javac -encoding UTF-8 -d out $(find src -name '*.java')
 * Rodar:    java -cp out testes.TesteGameState
 */
public class TesteGameState {
    private static int total = 0;

    public static void main(String[] args) throws Exception {
        testarNovoJogoVaiParaExploracao();
        testarNovoJogoRecusadoForaDoMenu();
        testarTransicaoInvalidaERecusada();
        testarNaoSalvaDuranteBatalha();
        testarIdaEVoltaDoSave();
        testarCarregarRestauraHub();
        testarTextosEspeciaisNoSave();
        testarSlotInvalido();
        testarSlotVazio();
        testarSaveRecusaDadosInvalidos();
        testarArquivoCorrompidoUsaBackup();
        testarArquivoCorrompidoSemBackup();
        testarChecksumAlteradoERejeitado();
        testarVersaoFuturaERejeitada();
        testarCargaComFalhaNaoAlteraOJogoAtual();
        testarFiltroDeEncontroPorPasso();
        testarVitoriaSomaFragmentosEMarcaFlag();
        testarFugaVoltaParaExploracao();
        testarDerrotaVaiParaGameOver();
        testarCarregarAPartirDoGameOver();
        testarPurificarSoNoHub();
        testarDadosExpostosSaoCopias();
        testarSalvarDuasVezesSeguidas();
        testarApagarSlot();
        System.out.println("Todos os " + total + " testes passaram.");
    }

    // ---------------------------------------------------------------- estados

    private static void testarNovoJogoVaiParaExploracao() throws Exception {
        GameStateController c = novoController();
        verificar(c.getEstado() == EstadoJogo.MENU, "Deveria começar no MENU.");
        verificar(c.getDados() == null, "Sem jogo, não deveria haver dados.");
        Resultado r = c.novoJogo("Arthur", "Guerreiro", 60);
        verificar(r.isOk(), "Novo jogo deveria funcionar: " + r);
        verificar(c.getEstado() == EstadoJogo.EXPLORACAO, "Deveria ir para EXPLORACAO.");
        verificar(c.getDados().getVidaAtual() == 60, "A vida deveria começar cheia.");
        fim("novo jogo vai para exploração");
    }

    private static void testarNovoJogoRecusadoForaDoMenu() throws Exception {
        GameStateController c = controllerEmExploracao();
        verificar(!c.novoJogo("Outro", "Mago", 40).isOk(), "Novo jogo não pode começar fora do menu.");
        verificar(c.getDados().getNomeHeroi().equals("Arthur"), "O herói atual não pode ser trocado.");
        fim("novo jogo recusado fora do menu");
    }

    private static void testarTransicaoInvalidaERecusada() throws Exception {
        GameStateController c = novoController();
        verificar(!c.iniciarBatalha("SLIME", "TESTE").isOk(), "MENU -> BATALHA deveria ser recusado.");
        verificar(!c.entrarNoHub().isOk(), "MENU -> HUB deveria ser recusado.");
        verificar(c.getEstado() == EstadoJogo.MENU, "O estado não pode mudar após recusa.");

        c = controllerEmExploracao();
        c.iniciarBatalha("SLIME", "ALEATORIO");
        verificar(!c.entrarNoHub().isOk(), "BATALHA -> HUB deveria ser recusado.");
        verificar(!c.voltarAoMenu().isOk(), "BATALHA -> MENU deveria ser recusado.");
        verificar(c.getEstado() == EstadoJogo.BATALHA, "Deveria continuar em BATALHA.");
        fim("transições inválidas são recusadas");
    }

    private static void testarNaoSalvaDuranteBatalha() throws Exception {
        GameStateController c = controllerEmExploracao();
        c.iniciarBatalha("SLIME", "ALEATORIO");
        verificar(!c.salvar(1).isOk(), "Não deveria salvar durante a batalha.");
        verificar(!c.carregar(1).isOk(), "Não deveria carregar durante a batalha.");
        fim("não salva nem carrega durante batalha");
    }

    // ---------------------------------------------------------------- save

    private static void testarIdaEVoltaDoSave() throws Exception {
        Path pasta = pastaTemporaria();
        GameStateController c = new GameStateController(new SaveEmJson(pasta));
        c.novoJogo("Arthur", "Guerreiro", 60);
        c.registrarMovimento(120.5, 80.25, "FLORESTA_01");
        c.adicionarItem("POCAO", 3);
        c.adicionarItem("POCAO", 2);
        c.adicionarItem("CHAVE", 1);
        c.marcarFlag("bau:BAU_01");
        c.marcarFlag("derrotado:REI_SOMBRAS");
        c.iniciarBatalha("SLIME_01", "ALEATORIO");
        c.encerrarBatalha(ResultadoBatalha.VITORIA, 4, null, 45);
        DadosDoJogo antes = c.getDados();
        verificar(c.salvar(1).isOk(), "Salvar deveria funcionar.");

        GameStateController outro = new GameStateController(new SaveEmJson(pasta));
        Resultado r = outro.carregar(1);
        verificar(r.isOk(), "Carregar deveria funcionar: " + r);
        DadosDoJogo depois = outro.getDados();
        verificar(outro.getEstado() == EstadoJogo.EXPLORACAO, "Deveria voltar para EXPLORACAO.");
        verificar(depois.getNomeHeroi().equals("Arthur"), "Nome diferente.");
        verificar(depois.getClasseHeroi().equals("Guerreiro"), "Classe diferente.");
        verificar(depois.getVidaAtual() == 45 && depois.getVidaMaxima() == 60, "Vida diferente.");
        verificar(depois.getPosicaoX() == 120.5 && depois.getPosicaoY() == 80.25, "Posição diferente.");
        verificar(depois.getAreaId().equals("FLORESTA_01"), "Área diferente.");
        verificar(depois.getFragmentosColetados() == 4, "Fragmentos diferentes.");
        verificar(depois.getInventario().get("POCAO") == 5, "Poções diferentes.");
        verificar(depois.getInventario().get("CHAVE") == 1, "Chave diferente.");
        verificar(depois.getFlagsMundo().equals(antes.getFlagsMundo()), "Flags diferentes.");
        verificar(outro.jaResolvido("bau:BAU_01"), "O baú deveria constar como resolvido.");
        fim("ida e volta do save preserva tudo");
    }

    private static void testarCarregarRestauraHub() throws Exception {
        Path pasta = pastaTemporaria();
        GameStateController c = new GameStateController(new SaveEmJson(pasta));
        c.novoJogo("Arthur", "Guerreiro", 60);
        c.entrarNoHub();
        verificar(c.salvar(2).isOk(), "Salvar no hub deveria funcionar.");

        GameStateController outro = new GameStateController(new SaveEmJson(pasta));
        outro.carregar(2);
        verificar(outro.getEstado() == EstadoJogo.HUB, "Deveria voltar para o HUB.");
        fim("carregar restaura o hub");
    }

    private static void testarTextosEspeciaisNoSave() throws Exception {
        Path pasta = pastaTemporaria();
        GameStateController c = new GameStateController(new SaveEmJson(pasta));
        String nome = "Ação \"Herói\"\\ Ú\tnico\n日本";
        c.novoJogo(nome, "Mago", 40);
        c.marcarFlag("flag com \"aspas\" e \\barra");
        c.salvar(1);
        GameStateController outro = new GameStateController(new SaveEmJson(pasta));
        verificar(outro.carregar(1).isOk(), "Carregar texto especial deveria funcionar.");
        verificar(outro.getDados().getNomeHeroi().equals(nome), "Nome com caracteres especiais mudou.");
        verificar(outro.jaResolvido("flag com \"aspas\" e \\barra"), "Flag com caracteres especiais mudou.");
        fim("aspas, barras, acentos e unicode sobrevivem ao save");
    }

    private static void testarSlotInvalido() throws Exception {
        GameStateController c = controllerEmExploracao();
        verificar(!c.salvar(0).isOk(), "Slot 0 deveria ser recusado.");
        verificar(!c.salvar(SaveEmJson.MAX_SLOTS + 1).isOk(), "Slot acima do máximo deveria ser recusado.");
        verificar(!c.carregar(-1).isOk(), "Slot negativo deveria ser recusado.");
        fim("slots inválidos são recusados");
    }

    private static void testarSlotVazio() throws Exception {
        GameStateController c = controllerEmExploracao();
        Resultado r = c.carregar(3);
        verificar(!r.isOk(), "Slot vazio deveria falhar.");
        verificar(r.getMensagem().contains("vazio"), "A mensagem deveria dizer que o slot está vazio.");
        fim("slot vazio gera erro claro");
    }

    private static void testarSaveRecusaDadosInvalidos() throws Exception {
        Path pasta = pastaTemporaria();
        SaveEmJson save = new SaveEmJson(pasta);
        DadosDoJogo ruim = DadosDoJogo.novo("Arthur", "Guerreiro", 60);
        ruim.setVidaAtual(999);
        try {
            save.salvar(1, ruim);
            verificar(false, "Vida acima do máximo deveria ser recusada.");
        } catch (SaveException esperado) {
            // ok
        }
        verificar(!save.existe(1), "Nenhum arquivo deveria ter sido criado.");
        fim("save recusa dados impossíveis e não cria arquivo");
    }

    private static void testarArquivoCorrompidoUsaBackup() throws Exception {
        Path pasta = pastaTemporaria();
        SaveEmJson save = new SaveEmJson(pasta);
        GameStateController c = new GameStateController(save);
        c.novoJogo("Arthur", "Guerreiro", 60);
        c.salvar(1);                       // versão A
        c.adicionarItem("POCAO", 1);
        c.salvar(1);                       // versão B (A vira backup)

        Files.write(pasta.resolve("slot1.json"), "{ isto não é json".getBytes(StandardCharsets.UTF_8));

        GameStateController outro = new GameStateController(save);
        Resultado r = outro.carregar(1);
        verificar(r.isOk(), "Deveria recuperar pelo backup: " + r);
        verificar(save.ultimaCargaUsouBackup(), "Deveria avisar que usou o backup.");
        verificar(outro.getDados().getInventario().isEmpty(), "O backup é a versão anterior (sem poção).");
        fim("arquivo corrompido recupera pelo backup");
    }

    private static void testarArquivoCorrompidoSemBackup() throws Exception {
        Path pasta = pastaTemporaria();
        SaveEmJson save = new SaveEmJson(pasta);
        GameStateController c = new GameStateController(save);
        c.novoJogo("Arthur", "Guerreiro", 60);
        c.salvar(1);
        Files.write(pasta.resolve("slot1.json"), new byte[] {'{', '"', 'v'});

        GameStateController outro = new GameStateController(save);
        verificar(!outro.carregar(1).isOk(), "Sem backup, arquivo corrompido deveria falhar.");
        fim("arquivo corrompido sem backup falha sem quebrar");
    }

    private static void testarChecksumAlteradoERejeitado() throws Exception {
        Path pasta = pastaTemporaria();
        SaveEmJson save = new SaveEmJson(pasta);
        GameStateController c = new GameStateController(save);
        c.novoJogo("Arthur", "Guerreiro", 60);
        c.salvar(1);
        Files.deleteIfExists(pasta.resolve("slot1.json.bak")); // sem backup para o teste ser direto

        Path arquivo = pasta.resolve("slot1.json");
        String texto = new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8);
        Files.write(arquivo, texto.replace("\"nivel\":1", "\"nivel\":99").getBytes(StandardCharsets.UTF_8));

        GameStateController outro = new GameStateController(save);
        Resultado r = outro.carregar(1);
        verificar(!r.isOk(), "Save alterado na mão deveria ser rejeitado.");
        verificar(r.getMensagem().contains("checksum"), "A mensagem deveria citar o checksum: " + r);
        fim("save alterado na mão é detectado");
    }

    private static void testarVersaoFuturaERejeitada() throws Exception {
        Path pasta = pastaTemporaria();
        SaveEmJson save = new SaveEmJson(pasta);
        GameStateController c = new GameStateController(save);
        c.novoJogo("Arthur", "Guerreiro", 60);
        c.salvar(1);
        Files.deleteIfExists(pasta.resolve("slot1.json.bak"));

        Path arquivo = pasta.resolve("slot1.json");
        String texto = new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8);
        Files.write(arquivo, texto.replace("\"versao\":1", "\"versao\":99").getBytes(StandardCharsets.UTF_8));

        Resultado r = new GameStateController(save).carregar(1);
        verificar(!r.isOk(), "Versão futura deveria ser rejeitada.");
        verificar(r.getMensagem().contains("versão"), "A mensagem deveria citar a versão: " + r);
        fim("save de versão futura é rejeitado");
    }

    private static void testarCargaComFalhaNaoAlteraOJogoAtual() throws Exception {
        GameStateController c = controllerEmExploracao();
        c.marcarFlag("bau:BAU_01");
        verificar(!c.carregar(3).isOk(), "Slot vazio deveria falhar.");
        verificar(c.getEstado() == EstadoJogo.EXPLORACAO, "O estado não pode mudar após carga que falhou.");
        verificar(c.jaResolvido("bau:BAU_01"), "Os dados não podem mudar após carga que falhou.");
        fim("carga que falha não altera o jogo atual");
    }

    private static void testarSalvarDuasVezesSeguidas() throws Exception {
        GameStateController c = controllerEmExploracao();
        verificar(c.salvar(1).isOk(), "Primeiro save deveria funcionar.");
        verificar(c.salvar(1).isOk(), "Segundo save no mesmo slot deveria funcionar.");
        verificar(c.carregar(1).isOk(), "Carregar depois de salvar duas vezes deveria funcionar.");
        fim("salvar duas vezes seguidas funciona");
    }

    private static void testarApagarSlot() throws Exception {
        Path pasta = pastaTemporaria();
        SaveEmJson save = new SaveEmJson(pasta);
        GameStateController c = new GameStateController(save);
        c.novoJogo("Arthur", "Guerreiro", 60);
        c.salvar(1);
        c.salvar(1);
        verificar(save.existe(1), "Slot deveria existir.");
        save.apagar(1);
        verificar(!save.existe(1), "Slot deveria ter sido apagado (inclusive o backup).");
        fim("apagar slot remove arquivo e backup");
    }

    // ---------------------------------------------------------------- exploração e batalha

    private static void testarFiltroDeEncontroPorPasso() throws Exception {
        GameStateController c = controllerEmExploracao();
        verificar(!c.registrarMovimento(0, 0, "A"), "Primeira posição só define a referência.");
        verificar(!c.registrarMovimento(1, 0, "A"), "Passo curto não deveria verificar encontro.");
        verificar(!c.registrarMovimento(10, 0, "A"), "Ainda abaixo do passo mínimo.");
        verificar(c.registrarMovimento(16, 0, "A"), "Passo completo deveria verificar encontro.");
        verificar(!c.registrarMovimento(17, 0, "A"), "Logo depois, não deveria verificar de novo.");
        verificar(c.registrarMovimento(33, 0, "A"), "Outro passo completo deveria verificar.");
        verificar(!c.registrarMovimento(Double.NaN, 0, "A"), "NaN deveria ser ignorado.");
        verificar(!c.registrarMovimento(50, 0, ""), "Área vazia deveria ser ignorada.");

        GameStateController menu = novoController();
        verificar(!menu.registrarMovimento(100, 100, "A"), "Fora da exploração nunca verifica encontro.");

        c.iniciarBatalha("SLIME", "ALEATORIO");
        verificar(!c.registrarMovimento(500, 500, "A"), "Durante a batalha não verifica encontro.");
        c.encerrarBatalha(ResultadoBatalha.FUGA, 0, null, 50);
        verificar(!c.registrarMovimento(500, 500, "A"), "Depois da batalha, a primeira posição só define a referência.");
        verificar(!c.registrarMovimento(505, 500, "A"), "Depois da batalha, é preciso andar de novo.");
        verificar(c.registrarMovimento(516, 500, "A"), "Depois de andar um passo, volta a verificar.");
        fim("encontro só é verificado a cada passo completo");
    }

    private static void testarVitoriaSomaFragmentosEMarcaFlag() throws Exception {
        GameStateController c = controllerEmExploracao();
        c.iniciarBatalha("REI_SOMBRAS", "GATILHO_PORTAL");
        verificar("REI_SOMBRAS".equals(c.getInimigoDaBatalha()), "Deveria guardar o inimigo da batalha.");
        Resultado r = c.encerrarBatalha(ResultadoBatalha.VITORIA, 5, "derrotado:REI_SOMBRAS", 30);
        verificar(r.isOk(), "Encerrar com vitória deveria funcionar: " + r);
        verificar(c.getEstado() == EstadoJogo.EXPLORACAO, "Vitória volta para EXPLORACAO.");
        verificar(c.getDados().getFragmentosColetados() == 5, "Deveria ganhar 5 fragmentos.");
        verificar(c.getDados().getVidaAtual() == 30, "A vida deveria refletir a batalha.");
        verificar(c.jaResolvido("derrotado:REI_SOMBRAS"), "O chefe deveria constar como derrotado.");
        verificar(c.getInimigoDaBatalha() == null, "Os dados da batalha deveriam ser limpos.");
        verificar(!c.encerrarBatalha(ResultadoBatalha.VITORIA, 5, null, 30).isOk(),
            "Não dá para encerrar uma batalha que já acabou.");
        fim("vitória soma fragmentos e marca o chefe");
    }

    private static void testarFugaVoltaParaExploracao() throws Exception {
        GameStateController c = controllerEmExploracao();
        c.iniciarBatalha("SLIME", "ALEATORIO");
        c.encerrarBatalha(ResultadoBatalha.FUGA, 99, "derrotado:SLIME", 50);
        verificar(c.getEstado() == EstadoJogo.EXPLORACAO, "Fuga volta para EXPLORACAO.");
        verificar(c.getDados().getFragmentosColetados() == 0, "Fuga não dá fragmentos.");
        verificar(!c.jaResolvido("derrotado:SLIME"), "Fuga não marca o inimigo como derrotado.");
        fim("fuga não dá recompensa");
    }

    private static void testarDerrotaVaiParaGameOver() throws Exception {
        GameStateController c = controllerEmExploracao();
        c.iniciarBatalha("BOSS", "ALEATORIO");
        c.encerrarBatalha(ResultadoBatalha.DERROTA, 0, null, 0);
        verificar(c.getEstado() == EstadoJogo.GAME_OVER, "Derrota vai para GAME_OVER.");
        verificar(!c.salvar(1).isOk(), "Não se salva em GAME_OVER.");
        verificar(!c.registrarMovimento(10, 10, "A"), "Em GAME_OVER não há movimento.");
        fim("derrota leva ao game over");
    }

    private static void testarCarregarAPartirDoGameOver() throws Exception {
        Path pasta = pastaTemporaria();
        GameStateController c = new GameStateController(new SaveEmJson(pasta));
        c.novoJogo("Arthur", "Guerreiro", 60);
        c.salvar(1);
        c.iniciarBatalha("BOSS", "ALEATORIO");
        c.encerrarBatalha(ResultadoBatalha.DERROTA, 0, null, 0);
        verificar(c.getEstado() == EstadoJogo.GAME_OVER, "Deveria estar em GAME_OVER.");
        verificar(c.carregar(1).isOk(), "Deveria poder carregar o último save.");
        verificar(c.getEstado() == EstadoJogo.EXPLORACAO, "Deveria voltar à EXPLORACAO.");
        verificar(c.getDados().getVidaAtual() == 60, "A vida deveria voltar ao valor salvo.");
        fim("dá para continuar do último save após o game over");
    }

    private static void testarPurificarSoNoHub() throws Exception {
        GameStateController c = controllerEmExploracao();
        c.iniciarBatalha("SLIME", "ALEATORIO");
        c.encerrarBatalha(ResultadoBatalha.VITORIA, 3, null, 50);
        verificar(!c.purificarFragmentos().isOk(), "Fora do hub não se purifica.");
        c.entrarNoHub();
        verificar(c.purificarFragmentos().isOk(), "No hub deveria purificar.");
        verificar(c.getDados().getFragmentosColetados() == 0, "Coletados deveria zerar.");
        verificar(c.getDados().getFragmentosPurificados() == 3, "Purificados deveria ser 3.");
        verificar(!c.purificarFragmentos().isOk(), "Sem fragmentos, não há o que purificar.");
        verificar(c.sairDoHub().isOk(), "Deveria sair do hub.");
        fim("purificação só acontece no hub");
    }

    private static void testarDadosExpostosSaoCopias() throws Exception {
        GameStateController c = controllerEmExploracao();
        DadosDoJogo copia = c.getDados();
        copia.setVidaAtual(1);
        copia.getFlagsMundo().add("trapaca");
        verificar(c.getDados().getVidaAtual() == 50, "Alterar a cópia não pode mudar o jogo.");
        verificar(!c.jaResolvido("trapaca"), "Flag da cópia não pode vazar para o jogo.");
        fim("getDados devolve cópia protegida");
    }

    // ---------------------------------------------------------------- utilitários

    private static GameStateController novoController() throws IOException {
        return new GameStateController(new SaveEmJson(pastaTemporaria()));
    }

    private static GameStateController controllerEmExploracao() throws IOException {
        GameStateController c = novoController();
        c.novoJogo("Arthur", "Guerreiro", 50);
        return c;
    }

    private static Path pastaTemporaria() throws IOException {
        Path pasta = Files.createTempDirectory("ousia-teste-");
        pasta.toFile().deleteOnExit();
        return pasta;
    }

    private static void fim(String nome) {
        total++;
        System.out.println("  ok  " + nome);
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
