package testes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import estado.DadosDoJogo;
import estado.EstadoJogo;
import estado.GameStateController;
import estado.ResultadoBatalha;
import jogo.OrquestradorDoJogo;
import jogo.RespostaDoJogo;
import portas.BatalhaEmCurso;
import portas.ComandoBatalha;
import portas.EncontroDetectado;
import portas.EncontroViaJson;
import portas.EventoDoJogo;
import portas.FabricaDeBatalha;
import portas.FonteDeEncontros;
import portas.OpcaoDoJogo;
import save.SaveEmJson;
import util.JsonSimples;

/**
 * Testes do orquestrador com módulos falsos (sem depender do código dos outros integrantes).
 *
 * Compilar: javac -encoding UTF-8 -d out $(find src -name '*.java')
 * Rodar:    java -cp out testes.TesteOrquestrador
 */
public class TesteOrquestrador {
    private static int total = 0;

    private static final String SEM_ENCONTRO =
        "{\"acao\":\"SEM_ENCONTRO\",\"jogadorId\":\"J1\",\"areaId\":\"A\",\"mensagem\":\"Nenhum inimigo encontrado nesta posição.\",\"origem\":\"NENHUM\"}";

    public static void main(String[] args) throws Exception {
        testarPassosCurtosNaoConsultamEncontros();
        testarSemEncontroMantemExploracao();
        testarEncontroIniciaBatalha();
        testarComandoForaDaBatalhaERecusado();
        testarVitoriaAplicaRecompensas();
        testarDerrotaLevaAoGameOver();
        testarFugaNaoDaRecompensa();
        testarOrigemUnicaNaoRepete();
        testarOrigemComumRepete();
        testarRespostasInvalidasDeEncontros();
        testarFabricaQueFalhaDesfazAEntrada();
        testarMotorQueRecusaIniciarDesfazAEntrada();
        testarAcaoExplicitaIgnoraOFiltroDePasso();
        testarVidaFinalEhCorrigida();
        testarTurnoInimigoAutomatico();
        testarSaveDuranteBatalhaERecusadoELoadLimpa();
        testarMotorQueLancaExcecaoNaoQuebraOJogo();
        testarMotorSemResultadoNaoTravaOJogo();
        testarEntradasInvalidas();
        testarOpcoesDeMenu();
        testarRespostaEmJson();
        testarLeitorDeEncontroComJsonDoRicharlyson();
        System.out.println("Todos os " + total + " testes passaram.");
    }

    // ---------------------------------------------------------------- batalha e encontros

    private static void testarPassosCurtosNaoConsultamEncontros() throws Exception {
        Cenario c = new Cenario();
        c.orq.mover(0, 0, "A");
        c.orq.mover(5, 0, "A");
        c.orq.mover(10, 0, "A");
        verificar(c.fonte.chamadas == 0, "Passos curtos não deveriam consultar encontros.");
        c.orq.mover(16, 0, "A");
        verificar(c.fonte.chamadas == 1, "Um passo completo deveria consultar encontros uma vez.");
        verificar("J1".equals(c.fonte.ultimoJogadorId), "Deveria enviar o id do jogador.");
        verificar("A".equals(c.fonte.ultimaArea), "Deveria enviar a área.");
        fim("passos curtos não consultam o módulo de encontros");
    }

    private static void testarSemEncontroMantemExploracao() throws Exception {
        Cenario c = new Cenario();
        RespostaDoJogo r = c.andarAteChecar(0);
        verificar(r.isOk(), "Sem encontro deveria ser sucesso: " + r);
        verificar(r.getEstado() == EstadoJogo.EXPLORACAO, "Deveria continuar explorando.");
        verificar(r.getMensagem().equals("Nenhum encontro."), "Mensagem inesperada: " + r.getMensagem());
        verificar(c.fabrica.chamadas == 0, "Nenhuma batalha deveria ser montada.");
        fim("sem encontro continua explorando");
    }

    private static void testarEncontroIniciaBatalha() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        RespostaDoJogo r = c.andarAteChecar(0);
        verificar(r.isOk(), "Encontro deveria iniciar batalha: " + r);
        verificar(r.getEstado() == EstadoJogo.BATALHA, "Deveria estar em BATALHA.");
        verificar(c.orq.haBatalhaEmCurso(), "Deveria haver batalha em curso.");
        verificar(c.fabrica.chamadas == 1, "A fábrica deveria ser chamada uma vez.");
        verificar("SLIME_01".equals(c.fabrica.ultimoEncontro.getInimigoId()), "Inimigo errado na fábrica.");
        verificar(c.fabrica.ultimoEncontro.getInimigoNivel() == 2, "Nível errado na fábrica.");
        verificar("Arthur".equals(c.fabrica.ultimoHeroi.getNomeHeroi()), "Herói errado na fábrica.");
        verificar(!r.getEventos().isEmpty(), "Deveria devolver os eventos de início.");

        Map<?, ?> json = (Map<?, ?>) JsonSimples.ler(r.paraJson());
        verificar(Boolean.TRUE.equals(json.get("ok")), "JSON: ok.");
        verificar("BATALHA".equals(json.get("estado")), "JSON: estado.");
        verificar("Slime Verde".equals(((Map<?, ?>) json.get("inimigo")).get("nome")), "JSON: inimigo.");
        verificar("PRINCIPAL".equals(json.get("menu")), "JSON: menu.");
        verificar("JOGADOR".equals(json.get("turno")), "JSON: turno.");
        fim("encontro inicia batalha e informa inimigo, menu e turno");
    }

    private static void testarComandoForaDaBatalhaERecusado() throws Exception {
        Cenario c = new Cenario();
        RespostaDoJogo r = c.orq.comandoDeBatalha(ComandoBatalha.abrirAtaques());
        verificar(!r.isOk(), "Comando de batalha em exploração deveria ser recusado.");
        verificar(r.getEstado() == EstadoJogo.EXPLORACAO, "O estado não pode mudar.");
        fim("comando de batalha fora da batalha é recusado");
    }

    private static void testarVitoriaAplicaRecompensas() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.andarAteChecar(0);
        c.ultimaBatalha().desfechoAoAtacar = ResultadoBatalha.VITORIA;
        c.ultimaBatalha().vida = 40;
        c.ultimaBatalha().fragmentos = 3;
        c.ultimaBatalha().flag = "derrotado:SLIME_01";

        RespostaDoJogo r = c.orq.comandoDeBatalha(ComandoBatalha.atacar("FISICO", "golpe"));
        verificar(r.isOk(), "Comando deveria funcionar: " + r);
        verificar(r.getEstado() == EstadoJogo.EXPLORACAO, "Vitória volta para EXPLORACAO.");
        verificar("VITORIA".equals(r.getExtra("resultadoBatalha")), "Deveria informar a vitória.");
        DadosDoJogo d = c.orq.getDados();
        verificar(d.getFragmentosColetados() == 3, "Deveria ganhar 3 fragmentos.");
        verificar(d.getVidaAtual() == 40, "A vida deveria refletir a batalha.");
        verificar(d.getFlagsMundo().contains("derrotado:SLIME_01"), "Deveria marcar o inimigo derrotado.");
        verificar(!c.orq.haBatalhaEmCurso(), "A batalha deveria ter sido encerrada.");
        verificar(c.ultimaBatalha().recebidos.size() == 1
            && c.ultimaBatalha().recebidos.get(0).getTipo() == ComandoBatalha.Tipo.ATACAR
            && "golpe".equals(c.ultimaBatalha().recebidos.get(0).getId()),
            "O comando deveria chegar ao motor sem alteração.");
        fim("vitória aplica fragmentos, vida e flag");
    }

    private static void testarDerrotaLevaAoGameOver() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("BOSS", "Chefe", 9, "GATILHO_PORTAL");
        c.andarAteChecar(0);
        c.ultimaBatalha().desfechoNoTurnoInimigo = ResultadoBatalha.DERROTA;
        c.ultimaBatalha().vida = 17; // o motor não deveria informar isso, mas a derrota sempre zera a vida

        RespostaDoJogo r1 = c.orq.comandoDeBatalha(ComandoBatalha.atacar("FISICO", "golpe"));
        verificar(r1.getEstado() == EstadoJogo.BATALHA && "INIMIGO".equals(r1.getExtra("turno")),
            "Depois do ataque deveria ser o turno do inimigo.");
        RespostaDoJogo r2 = c.orq.comandoDeBatalha(ComandoBatalha.turnoInimigo());
        verificar(r2.getEstado() == EstadoJogo.GAME_OVER, "Derrota leva ao GAME_OVER.");
        verificar("DERROTA".equals(r2.getExtra("resultadoBatalha")), "Deveria informar a derrota.");
        verificar(c.orq.getDados().getVidaAtual() == 0, "Na derrota a vida é zero.");
        verificar(!c.orq.mover(100, 0, "A").isOk(), "Em game over não se anda.");
        fim("derrota leva ao game over com vida zero");
    }

    private static void testarFugaNaoDaRecompensa() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.andarAteChecar(0);
        c.ultimaBatalha().fragmentos = 99;
        c.ultimaBatalha().flag = "derrotado:SLIME_01";
        c.ultimaBatalha().vida = 45;
        RespostaDoJogo r = c.orq.comandoDeBatalha(ComandoBatalha.fugir());
        verificar(r.getEstado() == EstadoJogo.EXPLORACAO, "Fuga volta para EXPLORACAO.");
        verificar(c.orq.getDados().getFragmentosColetados() == 0, "Fuga não dá fragmentos.");
        verificar(!c.orq.getDados().getFlagsMundo().contains("derrotado:SLIME_01"), "Fuga não marca derrotado.");
        verificar(c.orq.getDados().getVidaAtual() == 45, "A vida da fuga deveria ser aplicada.");
        fim("fuga não dá recompensa, mas mantém a vida");
    }

    private static void testarOrigemUnicaNaoRepete() throws Exception {
        Cenario c = new Cenario();
        c.orq.definirOrigemUnica("GATILHO_PORTAL");
        c.fonte.resposta = encontro("GUARDIAO", "Guardião", 3, "GATILHO_PORTAL");
        c.andarAteChecar(0);
        c.ultimaBatalha().desfechoAoAtacar = ResultadoBatalha.VITORIA;
        c.orq.comandoDeBatalha(ComandoBatalha.atacar("FISICO", "golpe"));
        verificar(c.orq.getDados().getFlagsMundo().contains("resolvido:GATILHO_PORTAL"),
            "Deveria marcar o gatilho como resolvido.");

        int antes = c.fabrica.chamadas;
        RespostaDoJogo r = c.andarAteChecar(100);
        verificar(r.isOk() && r.getEstado() == EstadoJogo.EXPLORACAO, "Não deveria entrar em batalha de novo.");
        verificar(r.getMensagem().contains("já resolvido"), "Deveria explicar: " + r.getMensagem());
        verificar(c.fabrica.chamadas == antes, "Nenhuma batalha nova deveria ser montada.");
        fim("gatilho de uso único não dispara de novo depois de vencido");
    }

    private static void testarOrigemComumRepete() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.andarAteChecar(0);
        c.ultimaBatalha().desfechoAoAtacar = ResultadoBatalha.VITORIA;
        c.orq.comandoDeBatalha(ComandoBatalha.atacar("FISICO", "golpe"));
        RespostaDoJogo r = c.andarAteChecar(100);
        verificar(r.getEstado() == EstadoJogo.BATALHA, "Encontro aleatório pode repetir.");
        verificar(c.fabrica.chamadas == 2, "Deveria montar a segunda batalha.");
        fim("encontro aleatório pode se repetir");
    }

    // ---------------------------------------------------------------- falhas dos módulos

    private static void testarRespostasInvalidasDeEncontros() throws Exception {
        String[] ruins = {
            "isto não é json",
            "",
            "[1,2,3]",
            "{\"mensagem\":\"sem acao\"}",
            "{\"acao\":\"VOAR\"}",
            "{\"acao\":\"ENTRADA_INVALIDA\",\"mensagem\":\"jogadorId e areaId são obrigatórios.\"}",
            "{\"acao\":\"INICIAR_BATALHA\",\"origem\":\"ALEATORIO\"}",
            "{\"acao\":\"INICIAR_BATALHA\",\"inimigo\":{\"id\":\"\",\"nome\":\"x\",\"nivel\":1}}"
        };
        for (String ruim : ruins) {
            Cenario c = new Cenario();
            c.fonte.resposta = ruim;
            RespostaDoJogo r = c.andarAteChecar(0);
            verificar(!r.isOk(), "Resposta inválida deveria ser erro: " + ruim);
            verificar(r.getEstado() == EstadoJogo.EXPLORACAO, "O estado não pode mudar com resposta inválida.");
            verificar(c.fabrica.chamadas == 0, "Nenhuma batalha com resposta inválida.");
        }
        Cenario c = new Cenario();
        c.fonte.lancar = true;
        RespostaDoJogo r = c.andarAteChecar(0);
        verificar(!r.isOk() && r.getEstado() == EstadoJogo.EXPLORACAO, "Módulo que lança exceção não derruba o jogo.");
        fim("respostas inválidas ou exceções dos encontros viram erro sem mudar o estado");
    }

    private static void testarFabricaQueFalhaDesfazAEntrada() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.fabrica.devolverNull = true;
        RespostaDoJogo r = c.andarAteChecar(0);
        verificar(!r.isOk(), "Fábrica que devolve null deveria ser erro.");
        verificar(r.getEstado() == EstadoJogo.EXPLORACAO, "Deveria voltar à exploração.");
        verificar(c.orq.getDados().getVidaAtual() == 50, "A vida não pode mudar.");
        verificar(!c.orq.haBatalhaEmCurso(), "Não deveria haver batalha.");

        Cenario c2 = new Cenario();
        c2.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c2.fabrica.lancar = true;
        RespostaDoJogo r2 = c2.andarAteChecar(0);
        verificar(!r2.isOk() && r2.getEstado() == EstadoJogo.EXPLORACAO, "Fábrica que lança exceção desfaz a entrada.");

        // depois da falha, o jogo continua jogável
        c2.fabrica.lancar = false;
        verificar(c2.andarAteChecar(100).getEstado() == EstadoJogo.BATALHA, "Depois da falha, novas batalhas funcionam.");
        fim("falha ao montar a batalha desfaz a entrada e o jogo continua jogável");
    }

    private static void testarMotorQueRecusaIniciarDesfazAEntrada() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.fabrica.aceitaIniciar = false;
        RespostaDoJogo r = c.andarAteChecar(0);
        verificar(!r.isOk(), "Motor que recusa iniciar deveria ser erro.");
        verificar(r.getEstado() == EstadoJogo.EXPLORACAO, "Deveria voltar à exploração.");
        verificar(!r.getEventos().isEmpty(), "Deveria repassar o motivo (eventos) do motor.");
        verificar(!c.orq.haBatalhaEmCurso(), "Não deveria haver batalha.");
        fim("motor que recusa iniciar desfaz a entrada e repassa o motivo");
    }

    private static void testarMotorQueLancaExcecaoNaoQuebraOJogo() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.andarAteChecar(0);
        c.ultimaBatalha().lancar = true;
        RespostaDoJogo r = c.orq.comandoDeBatalha(ComandoBatalha.fugir());
        verificar(!r.isOk(), "Exceção do motor deveria virar erro.");
        verificar(r.getEstado() == EstadoJogo.BATALHA && c.orq.haBatalhaEmCurso(), "A batalha continua em curso.");
        c.ultimaBatalha().lancar = false;
        verificar(c.orq.comandoDeBatalha(ComandoBatalha.fugir()).isOk(), "Depois do erro, o jogador pode tentar de novo.");
        fim("exceção do motor vira erro e a batalha continua");
    }

    private static void testarMotorSemResultadoNaoTravaOJogo() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.andarAteChecar(0);
        c.ultimaBatalha().terminarSemResultado = true;
        RespostaDoJogo r = c.orq.comandoDeBatalha(ComandoBatalha.fugir());
        verificar(!r.isOk(), "Batalha sem resultado deveria ser erro.");
        verificar(r.getEstado() == EstadoJogo.EXPLORACAO, "Mas o jogo não pode travar em BATALHA.");
        verificar(!c.orq.haBatalhaEmCurso(), "A batalha deveria ter sido descartada.");
        fim("batalha que termina sem resultado não trava o jogo");
    }

    // ---------------------------------------------------------------- detalhes

    private static void testarAcaoExplicitaIgnoraOFiltroDePasso() throws Exception {
        Cenario c = new Cenario();
        c.orq.agir("ABRIR_BAU");
        verificar(c.fonte.chamadas == 1, "Ação explícita deveria consultar na hora.");
        verificar(c.fonte.ultimasAcoes.equals(Arrays.asList("ABRIR_BAU")), "Deveria enviar o nome da ação.");
        verificar(!c.orq.agir(" ").isOk(), "Ação sem nome é erro.");
        fim("ação explícita ignora o filtro de passo");
    }

    private static void testarVidaFinalEhCorrigida() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.andarAteChecar(0);
        c.ultimaBatalha().desfechoAoAtacar = ResultadoBatalha.VITORIA;
        c.ultimaBatalha().vida = 999;
        c.orq.comandoDeBatalha(ComandoBatalha.atacar("FISICO", "golpe"));
        verificar(c.orq.getDados().getVidaAtual() == 50, "Vida acima do máximo deveria ser limitada ao máximo.");

        c.andarAteChecar(100);
        c.ultimaBatalha().desfechoAoAtacar = ResultadoBatalha.VITORIA;
        c.ultimaBatalha().vida = -5;
        c.ultimaBatalha().fragmentos = -3;
        c.orq.comandoDeBatalha(ComandoBatalha.atacar("FISICO", "golpe"));
        verificar(c.orq.getDados().getVidaAtual() == 0, "Vida negativa deveria virar zero.");
        verificar(c.orq.getDados().getFragmentosColetados() == 0, "Fragmentos negativos não contam.");
        fim("vida e fragmentos fora do limite são corrigidos");
    }

    private static void testarTurnoInimigoAutomatico() throws Exception {
        Cenario c = new Cenario();
        c.orq.setTurnoInimigoAutomatico(true);
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.andarAteChecar(0);
        RespostaDoJogo r = c.orq.comandoDeBatalha(ComandoBatalha.atacar("FISICO", "golpe"));
        List<ComandoBatalha> recebidos = c.ultimaBatalha().recebidos;
        verificar(recebidos.size() == 2 && recebidos.get(1).getTipo() == ComandoBatalha.Tipo.TURNO_INIMIGO,
            "Deveria rodar o turno do inimigo logo depois.");
        verificar("JOGADOR".equals(r.getExtra("turno")), "A vez deveria voltar ao jogador.");
        fim("turno do inimigo automático");
    }

    private static void testarSaveDuranteBatalhaERecusadoELoadLimpa() throws Exception {
        Path pasta = Files.createTempDirectory("ousia-orq-");
        Cenario c = new Cenario(pasta);
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.andarAteChecar(0);
        verificar(!c.orq.salvar(1).isOk(), "Não se salva durante a batalha.");

        c.ultimaBatalha().desfechoAoAtacar = ResultadoBatalha.VITORIA;
        c.ultimaBatalha().fragmentos = 2;
        c.orq.comandoDeBatalha(ComandoBatalha.atacar("FISICO", "golpe"));
        verificar(c.orq.salvar(1).isOk(), "Depois da batalha, salvar funciona.");

        Cenario outro = new Cenario(pasta, false);
        verificar(outro.orq.carregar(1).isOk(), "Carregar deveria funcionar.");
        verificar(outro.orq.getEstado() == EstadoJogo.EXPLORACAO, "Deveria voltar à exploração.");
        verificar(outro.orq.getDados().getFragmentosColetados() == 2, "Fragmentos deveriam voltar.");
        verificar(!outro.orq.haBatalhaEmCurso(), "Não pode haver batalha após carregar.");
        verificar(!outro.orq.comandoDeBatalha(ComandoBatalha.fugir()).isOk(), "Sem batalha, comando é erro.");
        fim("save é recusado em batalha e load deixa tudo limpo");
    }

    private static void testarEntradasInvalidas() throws Exception {
        GameStateController g = new GameStateController(new SaveEmJson(Files.createTempDirectory("ousia-orq-")));
        OrquestradorDoJogo orq = new OrquestradorDoJogo(g, new FonteFalsa(), new FabricaFalsa(new ArrayList<BatalhaFalsa>()), "J1");
        verificar(!orq.mover(1, 1, "A").isOk(), "No menu não se anda.");
        orq.novoJogo("Arthur", "Guerreiro", 50);
        verificar(!orq.mover(Double.NaN, 0, "A").isOk(), "NaN é posição inválida.");
        verificar(!orq.mover(0, 0, " ").isOk(), "Área vazia é inválida.");
        verificar(!orq.comandoDeBatalha(null).isOk(), "Comando nulo é erro.");
        verificar(!orq.salvar(0).isOk(), "Slot 0 é erro.");
        verificar(!orq.carregar(3).isOk(), "Slot vazio é erro.");

        boolean recusou = false;
        try {
            new OrquestradorDoJogo(null, new FonteFalsa(), new FabricaFalsa(new ArrayList<BatalhaFalsa>()), "J1");
        } catch (IllegalArgumentException e) {
            recusou = true;
        }
        verificar(recusou, "Construtor sem controller deveria ser recusado.");
        fim("entradas inválidas são recusadas");
    }

    private static void testarOpcoesDeMenu() throws Exception {
        Cenario c = new Cenario();
        verificar(c.orq.opcoesDeAtaque("FISICO").isEmpty(), "Sem batalha, sem opções.");
        verificar(c.orq.opcoesDeItens().isEmpty(), "Sem batalha, sem itens.");
        c.fonte.resposta = encontro("SLIME_01", "Slime Verde", 2, "ALEATORIO");
        c.andarAteChecar(0);
        verificar(c.orq.opcoesDeAtaque("FISICO").size() == 1, "Deveria listar o ataque.");
        verificar("golpe".equals(c.orq.opcoesDeAtaque("FISICO").get(0).getId()), "Id do ataque errado.");
        verificar(c.orq.opcoesDeItens().size() == 1, "Deveria listar o item.");
        fim("opções de ataque e de itens vêm da batalha em curso");
    }

    private static void testarRespostaEmJson() throws Exception {
        Cenario c = new Cenario();
        c.fonte.resposta = encontro("SLIME_01", "Slime \"Verde\"\nÚnico", 2, "ALEATORIO");
        RespostaDoJogo r = c.andarAteChecar(0);
        Map<?, ?> json = (Map<?, ?>) JsonSimples.ler(r.paraJson());
        Map<?, ?> inimigo = (Map<?, ?>) json.get("inimigo");
        verificar("Slime \"Verde\"\nÚnico".equals(inimigo.get("nome")), "Aspas, quebra de linha e acento devem sobreviver.");
        List<?> eventos = (List<?>) json.get("eventos");
        verificar(!eventos.isEmpty() && ((Map<?, ?>) eventos.get(0)).get("tipo") != null, "Eventos no JSON.");

        RespostaDoJogo erro = c.orq.salvar(0);
        Map<?, ?> jsonErro = (Map<?, ?>) JsonSimples.ler(erro.paraJson());
        verificar(Boolean.FALSE.equals(jsonErro.get("ok")), "JSON de erro deveria ter ok=false.");
        fim("resposta vira JSON válido, inclusive com caracteres especiais");
    }

    private static void testarLeitorDeEncontroComJsonDoRicharlyson() throws Exception {
        // Formato exato gerado por ControladorEncontro/RespostaFrontEnd.
        String json = "{\"acao\":\"INICIAR_BATALHA\",\"jogadorId\":\"jogador-1\",\"areaId\":\"FLORESTA_01\","
            + "\"mensagem\":\"Encontro iniciado. O sistema de batalha deve assumir.\","
            + "\"origem\":\"GATILHO_PORTAL\",\"inimigo\":{\"id\":\"MORCEGO_01\",\"nome\":\"Morcego Sombrio\",\"nivel\":2}}";
        EncontroDetectado e = EncontroViaJson.ler(json);
        verificar(e != null && "MORCEGO_01".equals(e.getInimigoId()), "Id do inimigo.");
        verificar("Morcego Sombrio".equals(e.getInimigoNome()), "Nome do inimigo.");
        verificar(e.getInimigoNivel() == 2, "Nível do inimigo.");
        verificar("GATILHO_PORTAL".equals(e.getOrigem()), "Origem.");

        verificar(EncontroViaJson.ler(SEM_ENCONTRO) == null, "SEM_ENCONTRO deveria devolver null.");

        EncontroDetectado semOrigem = EncontroViaJson.ler(
            "{\"acao\":\"INICIAR_BATALHA\",\"inimigo\":{\"id\":\"X\",\"nome\":\"Xis\",\"nivel\":3}}");
        verificar("ALEATORIO".equals(semOrigem.getOrigem()), "Sem origem assume ALEATORIO.");
        EncontroDetectado nivelAbsurdo = EncontroViaJson.ler(
            "{\"acao\":\"INICIAR_BATALHA\",\"inimigo\":{\"id\":\"X\",\"nome\":\"Xis\",\"nivel\":99999999999}}");
        verificar(nivelAbsurdo.getInimigoNivel() == 9999, "Nível absurdo é limitado.");
        EncontroDetectado nivelNegativo = EncontroViaJson.ler(
            "{\"acao\":\"INICIAR_BATALHA\",\"inimigo\":{\"id\":\"X\",\"nome\":\"Xis\",\"nivel\":-4}}");
        verificar(nivelNegativo.getInimigoNivel() == 1, "Nível negativo vira 1.");
        fim("lê o JSON exato do módulo de encontros");
    }

    // ---------------------------------------------------------------- apoio

    private static String encontro(String id, String nome, int nivel, String origem) {
        return JsonSimples.escrever(jsonDeEncontro(id, nome, nivel, origem));
    }

    private static Map<String, Object> jsonDeEncontro(String id, String nome, int nivel, String origem) {
        Map<String, Object> inimigo = new java.util.LinkedHashMap<String, Object>();
        inimigo.put("id", id);
        inimigo.put("nome", nome);
        inimigo.put("nivel", Integer.valueOf(nivel));
        Map<String, Object> raiz = new java.util.LinkedHashMap<String, Object>();
        raiz.put("acao", "INICIAR_BATALHA");
        raiz.put("jogadorId", "J1");
        raiz.put("areaId", "A");
        raiz.put("mensagem", "Encontro iniciado.");
        raiz.put("origem", origem);
        raiz.put("inimigo", inimigo);
        return raiz;
    }

    private static class Cenario {
        final FonteFalsa fonte = new FonteFalsa();
        final List<BatalhaFalsa> batalhas = new ArrayList<BatalhaFalsa>();
        final FabricaFalsa fabrica = new FabricaFalsa(batalhas);
        final OrquestradorDoJogo orq;

        Cenario() throws IOException {
            this(Files.createTempDirectory("ousia-orq-"), true);
        }

        Cenario(Path pasta) {
            this(pasta, true);
        }

        Cenario(Path pasta, boolean comNovoJogo) {
            orq = new OrquestradorDoJogo(new GameStateController(new SaveEmJson(pasta)), fonte, fabrica, "J1");
            if (comNovoJogo) {
                orq.novoJogo("Arthur", "Guerreiro", 50);
            }
        }

        /** Anda um passo completo a partir de x0 e devolve a resposta da checagem de encontro. */
        RespostaDoJogo andarAteChecar(double x0) {
            orq.mover(x0, 0, "A");
            return orq.mover(x0 + 16, 0, "A");
        }

        BatalhaFalsa ultimaBatalha() {
            return batalhas.get(batalhas.size() - 1);
        }
    }

    private static class FonteFalsa implements FonteDeEncontros {
        String resposta = SEM_ENCONTRO;
        boolean lancar;
        int chamadas;
        String ultimoJogadorId;
        String ultimaArea;
        List<String> ultimasAcoes = Collections.<String>emptyList();

        @Override
        public String verificarPresenca(String jogadorId, double x, double y, String areaId, List<String> acoes) {
            chamadas++;
            ultimoJogadorId = jogadorId;
            ultimaArea = areaId;
            ultimasAcoes = new ArrayList<String>(acoes);
            if (lancar) {
                throw new IllegalStateException("módulo de encontros quebrou");
            }
            return resposta;
        }
    }

    private static class FabricaFalsa implements FabricaDeBatalha {
        final List<BatalhaFalsa> batalhas;
        int chamadas;
        boolean devolverNull;
        boolean lancar;
        boolean aceitaIniciar = true;
        DadosDoJogo ultimoHeroi;
        EncontroDetectado ultimoEncontro;

        FabricaFalsa(List<BatalhaFalsa> batalhas) {
            this.batalhas = batalhas;
        }

        @Override
        public BatalhaEmCurso criar(DadosDoJogo heroi, EncontroDetectado encontro) {
            chamadas++;
            ultimoHeroi = heroi;
            ultimoEncontro = encontro;
            if (lancar) {
                throw new IllegalStateException("fábrica quebrou");
            }
            if (devolverNull) {
                return null;
            }
            BatalhaFalsa b = new BatalhaFalsa();
            b.aceitaIniciar = aceitaIniciar;
            batalhas.add(b);
            return b;
        }
    }

    private static class BatalhaFalsa implements BatalhaEmCurso {
        boolean aceitaIniciar = true;
        boolean iniciada;
        boolean terminou;
        boolean turnoInimigo;
        boolean lancar;
        boolean terminarSemResultado;
        String menu = "PRINCIPAL";
        ResultadoBatalha desfechoAoAtacar;
        ResultadoBatalha desfechoNoTurnoInimigo;
        ResultadoBatalha resultado;
        int vida = 50;
        int fragmentos;
        String flag;
        final List<ComandoBatalha> recebidos = new ArrayList<ComandoBatalha>();

        @Override
        public List<EventoDoJogo> iniciar() {
            if (aceitaIniciar) {
                iniciada = true;
                return Arrays.asList(new EventoDoJogo("BATALHA_INICIADA", "A batalha começou."));
            }
            return Arrays.asList(new EventoDoJogo("ACAO_INVALIDA", "O herói já está derrotado."));
        }

        @Override
        public boolean foiIniciada() { return iniciada; }

        @Override
        public boolean terminou() { return terminou; }

        @Override
        public boolean ehTurnoInimigo() { return turnoInimigo; }

        @Override
        public String menuAtual() { return menu; }

        @Override
        public List<EventoDoJogo> executar(ComandoBatalha comando) {
            if (lancar) {
                throw new IllegalStateException("motor quebrou");
            }
            recebidos.add(comando);
            switch (comando.getTipo()) {
                case ATACAR:
                    turnoInimigo = true;
                    if (desfechoAoAtacar != null) {
                        terminar(desfechoAoAtacar);
                    }
                    break;
                case FUGIR:
                    terminar(ResultadoBatalha.FUGA);
                    break;
                case TURNO_INIMIGO:
                    turnoInimigo = false;
                    if (desfechoNoTurnoInimigo != null) {
                        terminar(desfechoNoTurnoInimigo);
                    }
                    break;
                default:
                    break;
            }
            return Arrays.asList(new EventoDoJogo("MENSAGEM", "executou " + comando.getTipo()));
        }

        private void terminar(ResultadoBatalha r) {
            terminou = true;
            resultado = terminarSemResultado ? null : r;
        }

        @Override
        public List<OpcaoDoJogo> listarAtaques(String categoria) {
            return Arrays.asList(new OpcaoDoJogo("golpe", "Golpe"));
        }

        @Override
        public List<OpcaoDoJogo> listarItens() {
            return Arrays.asList(new OpcaoDoJogo("pocao_vida", "Poção de Vida"));
        }

        @Override
        public ResultadoBatalha resultado() { return resultado; }

        @Override
        public int vidaRestanteDoHeroi() { return vida; }

        @Override
        public int fragmentosGanhos() { return fragmentos; }

        @Override
        public String flagDerrotado() { return flag; }
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
