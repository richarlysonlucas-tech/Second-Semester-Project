# Cérebro do jogo (Game State Controller + Save System + orquestração)

Tudo desta pasta é do Atila e não altera nenhum arquivo de outros integrantes.
O núcleo (`src/`) compila e testa sozinho; a ligação com o código dos parceiros fica separada em `integracao/`.

## Como as peças se encaixam

```
Front-end (Phaser)
      │  mover(x,y,área) · agir("ABRIR_BAU") · comandoDeBatalha(...) · hub · salvar · carregar
      ▼
OrquestradorDoJogo ── devolve RespostaDoJogo (JSON: ok, estado, mensagem, eventos, extras)
      │
      ├── GameStateController  (estado, progresso, regras de transição)   ──► SaveSystem ──► JSON em disco
      ├── FonteDeEncontros     (porta)  ◄── ControladorEncontro (Richarlyson), via JSON
      └── FabricaDeBatalha     (porta)  ──► BatalhaEmCurso ◄── motor Batalha (Vitor)
```

| Pasta | Conteúdo |
|---|---|
| `src/estado` | `GameStateController`, `DadosDoJogo` (o que vai para o save), `EstadoJogo`, `ResultadoBatalha`, `Resultado` |
| `src/save` | `SaveSystem` (interface), `SaveEmJson`, `SaveException` |
| `src/util` | `JsonSimples` (JSON sem dependências) |
| `src/portas` | Interfaces do cérebro: `FonteDeEncontros`, `BatalhaEmCurso`, `FabricaDeBatalha`, `ComandoBatalha`, `EncontroDetectado`, `EncontroViaJson`... |
| `src/jogo` | `OrquestradorDoJogo`, `RespostaDoJogo` |
| `src/testes` | `TesteGameState` (24), `TesteOrquestrador` (22), `DemoCerebro` |
| `integracao/` | Adaptadores reais (`BatalhaViaMotorV2`, `EncontrosDoRicharlyson`), `TesteIntegracao` (7), `DemoIntegrada`, `compilar.sh` |

## Compilar e testar (só o núcleo)

```
cd cerebro
javac -encoding UTF-8 -d out $(find src -name '*.java')
java -cp out testes.TesteGameState
java -cp out testes.TesteOrquestrador
java -cp out testes.DemoCerebro
```

## Compilar e testar com os módulos reais

Precisa do código do Richarlyson (`src/`) e do Vitor (`MotorDeBatalhaV2/...`) na mesma árvore, por exemplo depois dos merges na `main`. A partir da raiz do projeto:

```
sh cerebro/integracao/compilar.sh
```

O script aceita as pastas dos módulos como argumentos, se estiverem em outro lugar.

## Regras importantes

**Estados:** `MENU → EXPLORACAO → BATALHA → EXPLORACAO | GAME_OVER`, `EXPLORACAO ⇄ HUB`. Ação fora das regras volta como erro (`ok:false`) e não muda nada.

**Encontros:** o front pode chamar `mover(...)` a cada frame; o módulo de encontros só é consultado a cada passo completo (16 unidades) e nunca durante a batalha. `agir(...)` consulta na hora, porque foi o jogador que pediu. Encontros de uso único (`definirOrigemUnica("GATILHO_BAU")`) deixam de disparar depois de vencidos e isso fica salvo (`resolvido:GATILHO_BAU`).

**Batalha:** a vida final é limitada entre 0 e o máximo; derrota zera a vida; fuga não dá recompensa. Se a batalha não puder ser montada ou iniciada, a entrada é desfeita e o jogo continua em exploração. Se o motor falhar no meio, a resposta é erro e a batalha continua.

**Save:** só em exploração ou hub, nunca em batalha. Slots 1 a 3, gravação atômica, backup `.bak` automático, versão do formato e checksum. Carregar nunca deixa batalha pendurada.

## O que cada integrante precisa entregar para a integração real

- **Encontros (Richarlyson):** nada. O cérebro lê o JSON que o `ControladorEncontro` já devolve.
- **Batalha (Vitor):** nada no motor. O adaptador `BatalhaViaMotorV2` o embrulha.
- **Personagens/ataques/itens (Mateus e outros):** quem montar o `Batalha` precisa fornecer à `FabricaDeBatalha` a vida do herói (um `IntSupplier`), a recompensa em fragmentos e, se quiser, a flag de inimigo derrotado. Observação: as classes do Mateus estão no pacote padrão do Java, e o Java não permite importá-las de classes que estão em pacotes. Para o jogo real usá-las, elas precisam ficar num pacote (ex.: `personagens`).
