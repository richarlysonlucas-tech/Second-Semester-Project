#!/bin/sh
# Compila o cérebro junto com os módulos dos outros integrantes e roda o teste de integração real.
#
# Só funciona quando o código dos outros estiver na mesma árvore (por exemplo, depois dos merges na main).
# Uso, a partir da raiz do projeto:
#   sh cerebro/integracao/compilar.sh [pasta-src-encontros] [pasta-src-batalha]
#
# Padrões:
#   encontros: ./src                                              (Richarlyson)
#   batalha:   ./MotorDeBatalhaV2/ousia-motor-batalha/ousia/src   (Vitor)
set -e
AQUI="$(cd "$(dirname "$0")" && pwd)"
CEREBRO="$(cd "$AQUI/.." && pwd)"
RAIZ="$(cd "$CEREBRO/.." && pwd)"
ENCONTROS="${1:-$RAIZ/src}"
BATALHA="${2:-$RAIZ/MotorDeBatalhaV2/ousia-motor-batalha/ousia/src}"
OUT="${TMPDIR:-/tmp}/ousia-integracao-out"

for pasta in "$ENCONTROS" "$BATALHA"; do
  if [ ! -d "$pasta" ]; then
    echo "Pasta não encontrada: $pasta"
    echo "Os módulos do Richarlyson e do Vitor precisam estar na árvore. Veja o cabeçalho deste script."
    exit 1
  fi
done

rm -rf "$OUT"
mkdir -p "$OUT"
# Principal.java (Richarlyson) está no pacote padrão e não é necessário para a integração.
javac -encoding UTF-8 -d "$OUT" $(find "$CEREBRO/src" "$CEREBRO/integracao/src" "$ENCONTROS" "$BATALHA" -name '*.java' ! -name Principal.java)
java -Dstdout.encoding=UTF-8 -cp "$OUT" integracao.TesteIntegracao
