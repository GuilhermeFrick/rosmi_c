#!/bin/bash
# Abre o CAFES que acompanha este repositorio (third_party/cafes/cafes.jar).
#
#   tools/cafes.sh
#
# Substitui o bin/cafes.sh original do CAFES, que procura as classes em bin/
# em vez de class/ e por isso nao abre. Pode ser chamado de qualquer pasta.

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export CAFES_HOME="$ROOT/third_party/cafes"

if ! command -v java >/dev/null 2>&1; then
    echo "erro: java nao encontrado; instale com: sudo apt install default-jre" >&2
    exit 1
fi

exec java -jar "$CAFES_HOME/cafes.jar" "$@"
