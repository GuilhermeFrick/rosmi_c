# Exemplo do enunciado: da imagem oficial ao CAFES

Passo a passo para reproduzir o exemplo do enunciado (2 × 3 segmentos de
768 × 1024 pixels, 13 objetos) a partir da imagem fornecida pelo professor.
Tudo o que é necessário está neste repositório: o ROSMI conta os objetos, o
`gen_cafes.py` gera os modelos, e o CAFES (em `third_party/cafes/`) calcula o
mapeamento na NoC e a energia.

Todos os comandos rodam a partir da **raiz do repositório** (`rosmi_c/`).

---

## 0. Pré-requisitos

```bash
sudo apt install build-essential libpng-dev default-jre
```

- `build-essential`: `gcc` e `make`, para compilar o ROSMI;
- `libpng-dev`: para o ROSMI ler a imagem PNG;
- `default-jre`: Java, para o CAFES;
- Python 3, que já vem no Ubuntu, para gerar os modelos do CAFES.

---

## 1. Compilar o ROSMI

```bash
make
```

![make](prints/terminal_01_make.png)

São gerados `bin/rosmi_seq` (versão sequencial) e `bin/rosmi_par` (versão
paralela). A compilação não emite nenhum aviso.

---

## 2. A imagem de entrada

A entrada é a imagem fornecida pelo professor,
[`imagem_trabalho.png`](imagem_trabalho.png), com 3072 × 1536 pixels. O ROSMI lê
o PNG diretamente: um pixel escuro (cinza abaixo de 128) é objeto, e um pixel
claro é fundo.

Com a divisão em 2 × 3 segmentos, a imagem fica assim. Em laranja, os 3 objetos
que atravessam a fronteira entre segmentos:

![imagem segmentada](prints/imagem_segmentada.png)

| Objeto distribuído | Segmentos que ele toca |
|---|---|
| hexágono | (0,0), (0,1), (1,0), (1,1) |
| retângulo pequeno do alto | (0,1), (0,2) |
| seta | (0,2), (1,2) |

> Os segmentos aqui são numerados como **(linha, coluna)**, a partir do canto
> superior esquerdo. A figura do enunciado usa outra convenção (coluna, linha),
> contando de baixo para cima.

### A imagem exige conectividade 8

Os contornos da imagem têm a espessura de um traço fino, e nas diagonais os
pixels **só se tocam pela quina**. Ampliando um trecho de diagonal (cada cor é
um objeto diferente para a conectividade 4):

![zoom de uma diagonal](prints/zoom_diagonal_conn4.png)

- Com **conectividade 4**, que só liga vizinhos de lado, cada degrau vira um
  objeto separado: **473 objetos**.
- Com **conectividade 8**, que também liga vizinhos de quina, cada contorno é
  um objeto só: **13 objetos**, a resposta do enunciado.

Por isso todos os passos a seguir usam `--conn 8`.

---

## 3. Versão sequencial

```bash
bin/rosmi_seq reports/imagem_trabalho.png --N 2 --M 3 --conn 8
```

![rosmi_seq](prints/terminal_02_rosmi_seq.png)

- **OBJETOS: 13**, a resposta do enunciado.
- **Soma ingênua: 18**: é o que daria somar o que cada segmento vê sozinho. O
  hexágono é contado 4 vezes, e o retângulo e a seta 2 vezes cada
  (13 + 3 + 1 + 1 = 18).

---

## 4. Versão paralela

```bash
bin/rosmi_par reports/imagem_trabalho.png --N 2 --M 3 --conn 8 --threads 4
```

![rosmi_par](prints/terminal_03_rosmi_par.png)

As três fases do algoritmo paralelo:

1. **Local:** cada segmento conta os seus objetos (soma local = 18).
2. **Bordas:** segmentos vizinhos comparam as bordas e unem os pedaços do mesmo
   objeto. São 7 fronteiras: 4 entre vizinhos da mesma linha e 3 entre
   vizinhos da mesma coluna. Com conectividade 8, os cantos onde 4 segmentos se
   encontram também são comparados; por isso trafegam 16 bytes a mais do que
   com conectividade 4.
3. **Redução:** conta os grupos que sobraram, chegando a **13**.

Para comparar, a mesma imagem com conectividade 4:

```bash
bin/rosmi_par reports/imagem_trabalho.png --N 2 --M 3 --conn 4 --threads 4
```

![rosmi_par conn 4](prints/terminal_04_rosmi_par_conn4.png)

---

## 5. A contagem não depende do número de threads

```bash
for p in 1 2 4 8; do
  bin/rosmi_par reports/imagem_trabalho.png --N 2 --M 3 --conn 8 --threads $p | grep OBJETOS
done
```

![invariância](prints/terminal_05_invariancia.png)

---

## 6. Medir o tempo de cada segmento (traço)

O CAFES precisa do tempo de computação de cada tile. Ele é medido com
**1 thread**, para que um tile não dispute processador com outro, e com a
melhor de 8 repetições:

```bash
bin/rosmi_par reports/imagem_trabalho.png --N 2 --M 3 --conn 8 \
    --threads 1 --reps 8 --trace reports/saidas/trabalho_p1
```

![traço](prints/terminal_06_rosmi_par_trace.png)

São gravados `saidas/trabalho_p1_tiles.csv` (tempo e componentes por segmento)
e `saidas/trabalho_p1_edges.csv` (uniões por fronteira).

---

## 7. Gerar os modelos do CAFES

```bash
python3 tools/gen_cafes.py --case reports/imagem_trabalho --out reports/cafes \
    --trace reports/saidas/trabalho_p1_tiles.csv
python3 tools/check_cafes.py reports/cafes/imagem_trabalho
```

![gen_cafes](prints/terminal_07_gen_cafes.png)

![check_cafes](prints/terminal_08_check_cafes.png)

O `gen_cafes.py` lê a segmentação (N, M, K, L) de
[`imagem_trabalho.json`](imagem_trabalho.json). A aplicação vira **8 núcleos**
numa NoC de **2 × 4 tiles**:

- `ME`: a memória que guarda a imagem;
- `PA0` a `PA5`: um processador por segmento;
- `PC`: o coletor, que faz a união final e dá o total.

São gerados três arquivos em `cafes/`, um por modelo: `.CWG` (CWM), `.ACPG`
(ACPM) e `.cdcg` (CDCM).

---

## 8. Rodar no CAFES (modelo CWM)

### 8.1 Abrir o CAFES

```bash
tools/cafes.sh
```

### 8.2 Abrir o modelo CWM

Clique em **Communication Weight Model (CWM)**.

![janela principal](prints/cafes_01_janela_principal.png)

### 8.3 Carregar o grafo

Na janela **CWM Mapping**, clique em **File → Load graph**.

![menu File](prints/cafes_02_menu_file.png)

Na janela de escolha de arquivo, aperte **Ctrl+L**, digite o caminho do `.CWG`
e clique em **Open**:

```
<caminho do repositório>/reports/cafes/imagem_trabalho.CWG
```

![escolher arquivo](prints/cafes_03_escolher_arquivo.png)

### 8.4 Conferir o grafo

![grafo carregado](prints/cafes_04_grafo_carregado.png)

Os números nas setas são os phits trocados entre os núcleos: 49 152 na carga de
cada segmento (`ME → PA`), 1 536 e 2 048 nas bordas (`PA → PA`), 114 no envio
ao coletor (`PA → PC`) e 2 no resultado (`PC → PA`).

Na janela principal, o **NoC size** muda sozinho para **2 · 4 · 1**, lido do
arquivo:

![NoC size](prints/cafes_05_noc_size.png)

### 8.5 Rodar o mapeamento

Na janela **CWM Mapping**, clique em **Tools → Exhaustive Search Mapping
Algorithm**:

![menu Tools](prints/cafes_06_menu_tools.png)

> **Não use "Compute Mapping" (em laranja) no CWM.** Um bug do CAFES na leitura
> do `.CWG` faz essa opção travar em "Computing..." numa NoC 2D (detalhes em
> [`third_party/cafes/README.md`](../third_party/cafes/README.md)). Os
> algoritmos de busca não são afetados.

Com 8 núcleos, a busca exaustiva termina em poucos segundos.

### 8.6 Resultado

![resultado](prints/cafes_07_resultado.png)

- **Energy = 5079,97 µJ**: a energia do melhor mapeamento possível, porque a
  busca exaustiva testa todas as combinações.
- Cada quadrado preto é um roteador `R[linha, coluna, camada]`, e o losango
  cinza ao lado é o núcleo mapeado nele.
- O **ME** ficou no centro da malha, em `R[0,1]`. A carga `ME → PAs` é quase
  96% do tráfego, então o melhor lugar para a memória é onde ela fica mais
  perto de todos os PAs.

O CWM só enxerga o grafo de comunicação, que depende da segmentação
(2 × 3 de 768 × 1024) e não do desenho da imagem. O conteúdo da imagem só
entra no modelo CDCM, pelos tempos medidos no passo 6.

---

## Resumo

| Etapa | Resultado |
|---|---|
| `rosmi_seq --conn 8` | **13** (soma ingênua 18) |
| `rosmi_par --conn 8`, 1/2/4/8 threads | **13** em todas |
| `rosmi_par --conn 4` | 473 |
| CAFES, CWM, busca exaustiva, NoC 2 × 4 | **5079,97 µJ** |

## Conteúdo desta pasta

```
reports/
  README.md                este passo a passo
  imagem_trabalho.png      imagem oficial do trabalho (entrada do ROSMI)
  imagem_trabalho.json     segmentação (N, M, K, L), lida pelo gen_cafes.py
  saidas/                  saída de cada comando (.txt) e traços (.csv)
  cafes/                   modelos .CWG, .ACPG e .cdcg para o CAFES
  prints/                  imagens deste tutorial
```
