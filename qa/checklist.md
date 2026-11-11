
# QA - Mark of Balance



## 2. Critical Test
- [x] Nenhum áudio apresenta erro 404
- [x] Os sons não travam o navegador
- [x] Os controles de música funcionam
- [ ] Arquivos inexistentes geram avisos
- [x] O navegador não apresenta erros críticos

### Testes adicionais
- [x] Dez cliques rápidos no botão Espada
- [x] Música com Zombie, Golem e Magia
- [x] Vários cliques no botão Música
- [x] Parar música durante a reprodução
- [x] Efeitos diferentes em sequência
- [x] Console sem novos erros críticos

Resultado: 6/6 testes críticos adicionais aprovados.

## 3. Full Test
- [ ] Som de Zombie
- [ ] Som de Golem
- [ ] Som de espada
- [ ] Som de passos
- [ ] Som de magia
- [ ] Som de coleta
- [ ] Som da interface
- [ ] Som de Game Over
- [ ] Som de regeneração
- [ ] Som de eliminar inimigo
- [ ] Música de fundo
- [ ] Reprodução de sons simultâneos

## 4. Testes de Desempenho
- [ ] Os áudios iniciam sem atrasos perceptíveis
- [ ] A reprodução não causa travamentos
- [ ] Vários efeitos podem tocar simultaneamente
- [ ] A música continua durante os efeitos
- [ ] O navegador mantém bom desempenho

## 5. Bugs Encontrados

Nenhum bug registrado até o momento.

## 6. Status do QA

Status: Aguardando execução dos testes.

## 7. Responsável

Projeto: Mark of Balance
Área: Quality Assurance (QA)

## 8. QA Avançado

Data: 11/11/2026

- [x] Áudio desconhecido tratado sem exceção
- [x] Volume máximo respeitado
- [x] Volume mínimo respeitado
- [x] Arquivo inexistente detectado
- [x] Chamadas consecutivas sem exceção imediata
- [x] Chamada de recuperação sem exceção imediata

Resultado: 6/6 verificações iniciais aprovadas.

Observação:
Ainda precisamos validar a reprodução real,
a sobreposição de sons e o consumo de memória.

## 9. Testes de Otimização

Data: 11/11/2026

- [x] Limite de 8 efeitos configurado
- [x] Conjunto de sons ativos criado
- [x] Bloqueio do nono efeito validado
- [x] Remoção simulada de efeito validada

Resultado: 4/4 verificações aprovadas.

Pendente:
- Testar remoção automática após o áudio terminar
- Verificar desempenho com reprodução prolongada
- Confirmar ausência de vazamentos de recursos

## 10. Teste de Cleanup

Data: 11/11/2026

- [x] Efeito registrado no conjunto de sons ativos
- [x] Efeito removido automaticamente
- [x] Liberação realizada dentro de 15 segundos

Resultado: APROVADO

Observação:
A remoção foi confirmada.
A causa da remoção ainda precisa ser validada:
término normal ou falha de reprodução.

## 11. Correção de Caminhos e Regressão

Data: 11/11/2026

- [x] Corrigidos os caminhos dos arquivos MP3
- [x] Reprodução da espada confirmada
- [x] Cleanup funcionando
- [x] Testes automatizados executados novamente
- [x] 24/24 testes aprovados

Resultado: APROVADO

Próxima etapa:
Teste de desempenho prolongado.
