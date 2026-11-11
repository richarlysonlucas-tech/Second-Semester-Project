# Ousia — Motor de Batalha

Responsável: motor de batalha (pacote `batalha`). Este guia diz o que cada integrante deve implementar
e como o motor reage quando algo dá errado.

## Estrutura

| Pacote | Conteúdo | Quem mexe |
|---|---|---|
| `batalha` | `Batalha` (motor), `EstadoBatalha`, `Turno`, `MenuBatalha` | Dono do motor |
| `batalha.contratos` | Interfaces e tipos trocados entre módulos | Combinado em equipe (mudar = avisar todos) |
| `batalha.simulacao` | Stubs para teste | Dono do motor; descartáveis |
| `batalha.teste` | `SistemaBatalha` (terminal) e `TestesBatalha` | Dono do motor |

## O que cada módulo implementa

| Módulo | Interface | O motor chama |
|---|---|---|
| Personagens | `Combatente` | `getNome()`, `estaVivo()` |
| Ataques/habilidades/cálculos | `SistemaAtaques` | `listarAtaques(usuario, tipo)` (**para o jogador E para o inimigo**), `executar(usuario, alvo, tipo, idAtaque)` |
| Itens/inventário | `SistemaItens` | `listarItens(usuario)`, `usar(usuario, oponente, idItem)` |
| Regras (fuga) | `SistemaFuga` | `tentarFuga(jogador, inimigo)` → `NAO_PERMITIDA` / `FALHOU` / `SUCESSO` |
| IA | `IAInimigo` | `escolherAcao(inimigo, jogador)` → `AcaoInimiga` |

### Regras para quem devolve `ResultadoAcao`
- `executada(...)`: ação válida que **consome** o turno (padrão para ataques e itens usados).
- `semConsumirTurno(...)`: ação válida que não gasta o turno.
- `invalida(motivo)`: ação recusada; o turno **não** passa e o jogador continua no mesmo menu.
- **Nunca devolva `null`** (nem `ResultadoFuga` nulo, nem eventos nulos dentro do resultado): o motor trata como erro de integração.
- Os efeitos (dano, cura, buff) são aplicados dentro do seu módulo. O motor só lê `estaVivo()`.

### Regras para a IA (`IAInimigo`)
- Devolva sempre uma `AcaoInimiga` com tipo e **id preenchido**.
- O id precisa estar entre as opções de `SistemaAtaques.listarAtaques(inimigo, tipo)`.
- Se a IA devolver `null`, id vazio ou um ataque fora da lista, o motor emite `ERRO_INTEGRACAO` e o inimigo **perde o turno** (a vez volta ao jogador). O mesmo vale se o módulo de ataques devolver `null` ou recusar a ação do inimigo.

## Comportamentos do motor que a interface deve conhecer

| Situação | O que o motor faz |
|---|---|
| `iniciar()` com jogador e/ou inimigo derrotado | Recusa: evento `ACAO_INVALIDA`, batalha **não** começa. Use `foiIniciada()` para saber. Pode tentar de novo depois. |
| Ação fora do turno, no menu errado ou depois do fim | Evento `ACAO_INVALIDA`; nada muda. Nunca lança exceção. |
| Módulo devolve `null` em ação do jogador ou na fuga | Evento `ERRO_INTEGRACAO`; **não** consome turno; continua no mesmo menu. |
| Módulo devolve evento `null` dentro do resultado | O evento é descartado e sai um `ERRO_INTEGRACAO`; o resto do resultado vale. |
| `listarAtaques` / `listarItens` do motor | Devolvem **cópia somente-leitura**; lista nula vira vazia; elementos nulos são descartados. |
| Fim da batalha | Estado `VITORIA`, `DERROTA` ou `FUGA` fica fixo; toda ação nova é recusada. Se os dois morrerem juntos, vale `DERROTA`. |

`getEstado()` devolve `EM_ANDAMENTO` também **antes** de iniciar; para distinguir use `foiIniciada()`.

## Como a interface gráfica usa o motor

```java
Batalha b = new Batalha(jogador, inimigo, ataques, itens, fuga, ia);   // jogador e inimigo devem ser objetos diferentes
List<EventoBatalha> ev = b.iniciar();      // toda ação devolve eventos para exibir
if (!b.foiIniciada()) { /* mostrar o motivo (ev) e não abrir a tela de batalha */ }

// Consultas: b.getEstado(), b.getTurno(), b.getMenuAtual(), b.batalhaAtiva(), b.terminou()
// Menus:     b.abrirMenuAtaques(), b.abrirMochila(), b.voltarMenuPrincipal()
// Ações:     b.escolherAtaque(tipo, id), b.usarItem(id), b.tentarFugir()
// Inimigo:   if (b.ehTurnoInimigo()) b.executarTurnoInimigo();
// Listas:    b.listarAtaques(tipo), b.listarItens()
```

- O motor **não imprime nada**. Cada `EventoBatalha` tem `getTipo()` (para animar) e `getTexto()`.
- Tipos de evento: `BATALHA_INICIADA`, `TURNO_JOGADOR`, `TURNO_INIMIGO`, `MENU_ALTERADO`, `ACAO_INVALIDA`, `FUGA`,
  `BATALHA_ENCERRADA`, `MENSAGEM` e **`ERRO_INTEGRACAO`** (novo: indica falha entre módulos; se a interface usa
  `switch` sobre `TipoEvento`, trate-o).
- O turno do inimigo é um passo separado, para a interface mostrar a ação do jogador antes da resposta.

## Compilar e testar

```
javac -encoding UTF-8 -d out $(find src -name '*.java')
java -cp out batalha.teste.TestesBatalha     # testes automáticos (sem dependências)
java -cp out batalha.teste.SistemaBatalha    # jogo de teste no terminal
```

## Trocar as simulações pelos módulos reais

Em `SistemaBatalha` (e na interface gráfica), troque os construtores de `batalha.simulacao.*` pelas
implementações reais dos contratos. `Batalha.java` não muda.
