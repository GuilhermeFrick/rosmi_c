# ROSMI — Reconhecedor de Objetos por Segmentação em Múltiplas Imagens

Conta os objetos de uma imagem binária dividida em **N × M segmentos**,
contando **uma única vez** os objetos que atravessam a fronteira entre
segmentos. Tem uma versão sequencial e uma paralela, em C.

Trabalho da disciplina de Modelagem de Sistemas Embarcados.

## Como funciona

1. **Cada segmento conta os seus objetos**, em paralelo (rotulação de
   componentes conexos).
2. **Segmentos vizinhos comparam as bordas** e unem os pedaços do mesmo objeto
   (union-find).
3. **Conta os grupos que sobraram:** esse é o total de objetos.

Sem o passo 2, um objeto que atravessa 4 segmentos seria contado 4 vezes.

## Instalar

```bash
sudo apt install build-essential libpng-dev default-jre
```

Ou, sem instalar nada: no GitHub, **Code → Codespaces → Create codespace**. O
ambiente já vem pronto, e a área de trabalho para o CAFES abre na porta 6080
(aba **Ports**, senha `vscode`).

## Compilar e testar

```bash
make          # gera bin/rosmi_seq e bin/rosmi_par
make test     # roda os testes unitários
```

## Rodar

Com a imagem do enunciado (2 × 3 segmentos de 768 × 1024 pixels):

```bash
bin/rosmi_seq docs/imagem_trabalho.png --N 2 --M 3 --conn 8
bin/rosmi_par docs/imagem_trabalho.png --N 2 --M 3 --conn 8 --threads 4
```

Saída do `rosmi_par` (resumida):

```
  OBJETOS           : 13
  soma local        : 18 (5 a mais: objetos distribuidos)
```

`OBJETOS` é a resposta. A `soma local` é o que daria somar cada segmento
sozinho, sem a união das bordas.

| Opção | Significado |
|---|---|
| `--N`, `--M` | número de segmentos na vertical e na horizontal |
| `--conn` | vizinhança dos pixels: 4 (padrão) ou 8 |
| `--threads` | número de threads (padrão: todos os processadores) |
| `--reps` | repete a medição e mostra a melhor |
| `--csv <arquivo>` | acrescenta uma linha de resultados ao arquivo |
| `--trace <prefixo>` | grava o tempo de cada segmento, usado pelo CAFES |

A entrada pode ser `.png` ou `.pbm`. A imagem do enunciado precisa de
`--conn 8`: os contornos são finos e, nas diagonais, os pixels só se tocam pela
quina.

## Explorar no CAFES

O CAFES mapeia a aplicação numa NoC e estima tempo e energia. Ele já vem neste
repositório, e os modelos do exemplo do enunciado estão em `reports/cafes/`:

```bash
tools/cafes.sh
```

O passo a passo completo, com prints, está em
[`reports/README.md`](reports/README.md).

## Estrutura

```
src/            código C (um módulo por arquivo)
test/           testes unitários
data/           imagens de teste com resposta conhecida
docs/           enunciado, imagem oficial, diagramas e fundamentos
reports/        exemplo do enunciado passo a passo e modelos do CAFES
tools/          scripts auxiliares (geração de imagens, modelos, cafes.sh)
third_party/    o CAFES
```

## Mais detalhes

- [`docs/background.md`](docs/background.md): o problema, o algoritmo, a
  arquitetura do código, a validação e o desempenho.
- [`reports/README.md`](reports/README.md): o exemplo do enunciado, da linha de
  comando ao resultado no CAFES.
- [`third_party/cafes/README.md`](third_party/cafes/README.md): o CAFES e os
  seus problemas conhecidos.
