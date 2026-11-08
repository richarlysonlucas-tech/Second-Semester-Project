# Sistema de Encontros

Projeto Java padrão para detectar a presença do jogador e preparar um encontro aleatório com inimigo.

## Gatilhos e proximidade

O front-end deve enviar a posição atual a cada atualização. O sistema verifica nesta ordem:

1. Gatilhos ativados, do maior para o menor nível de prioridade.
2. Entidade mais próxima dentro do raio de detecção.
3. Encontro aleatório da área atual.

Quando houver vários gatilhos ativos, vence o gatilho com maior prioridade. Quando houver várias entidades próximas, vence a que estiver mais perto do jogador.

### Tipos de gatilho

`GatilhoArea` é ativado ao entrar em um retângulo definido pelas coordenadas, largura e altura.

`GatilhoAcao` é ativado quando o front-end informa uma ação, por exemplo `ABRIR_BAU`.

### Contrato de proximidade

```java
controlador.verificarPresenca(jogadorId, posicaoX, posicaoY, areaId, acoesRealizadas);
```

```java
controlador.verificarAcao(jogadorId, posicaoX, posicaoY, areaId, acaoRealizada);
```

O resultado continua sendo JSON. Para uma entidade próxima, por exemplo:

```json
{"acao":"INICIAR_BATALHA","jogadorId":"jogador-1","areaId":"FLORESTA_01","mensagem":"Encontro iniciado. O sistema de batalha deve assumir.","origem":"PROXIMIDADE_MONSTRO_1","inimigo":{"id":"LOBISOMEM_01","nome":"Lobisomem","nivel":5}}
```
