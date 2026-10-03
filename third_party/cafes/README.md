# CAFES

*Communication Analysis For Embedded Systems*, versão 4.0.1, do grupo GAPH da
PUCRS. Usado nos itens 4 e 5 do trabalho para mapear a aplicação numa NoC e
estimar tempo e energia.

Para abrir, a partir da raiz do repositório:

```bash
tools/cafes.sh
```

## Conteúdo

| Arquivo | O que é |
|---|---|
| `cafes.jar` | as classes compiladas, empacotadas num jar executável |
| `src/` | o código-fonte Java correspondente |
| `LICENSE` | licença do CAFES (GNU LGPL 2.1) |
| `README.original` | o README que acompanha a distribuição |

O `cafes.jar` foi montado a partir da pasta `class/` da distribuição recebida,
sem nenhuma alteração no código. É a mesma versão usada para gerar os
resultados em [`reports/`](../../reports/).

## Problemas conhecidos

- **O `bin/cafes.sh` original não abre o programa:** ele procura as classes
  em `bin/`, mas elas ficam em `class/`. Use o `tools/cafes.sh`.
- **"Compute Mapping" trava no modelo CWM numa NoC 2D.** O leitor do `.CWG`
  grava o mapeamento do arquivo na camada errada
  (`src/cafes/model/CWM/CWM_GrafoFormatoTextual.java`, linha 273, usa o número
  de camadas da NoC como índice da camada). Os algoritmos de busca (Exhaustive,
  Simulated Annealing, Taboo) não são afetados, nem os modelos ACPM e CDCM.
