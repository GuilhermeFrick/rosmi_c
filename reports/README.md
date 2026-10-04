# ROSMI — Registro do desenvolvimento

Registro organizado de tudo o que foi feito no trabalho, na ordem das
atividades do enunciado, com os comandos, as saídas e os prints de cada etapa.
Serve de base para escrever o relatório final. Os tópicos ainda sem conteúdo
são atividades que ainda não foram feitas.

Todos os comandos rodam a partir da **raiz do repositório** (`rosmi_c/`).

---

## 1. Introdução

Este trabalho faz parte da disciplina de Modelagem de Sistemas Embarcados
(*Sistemas Multiprocessados em Chip — Comunicação e Computação*). A disciplina
trata de **modelos de computação** (MoCs): abstrações que capturam as
informações essenciais de um sistema para que se possa raciocinar sobre ele
antes de implementá-lo. O foco é a comunicação e a computação de aplicações
paralelas executando em sistemas multiprocessados em chip, interligados por uma
rede intrachip (NoC).

O objetivo é modelar, explorar e caracterizar uma aplicação paralela, o ROSMI,
e usar o framework **CAFES** para mapear os seus núcleos nos tiles de uma NoC,
estimando tempo de execução e consumo de energia.

O trabalho segue a metodologia apresentada na disciplina no estudo de caso do
Mandelbrot:

```
Aplicação + Arquitetura alvo  →  Descrição (versão paralela)
Descrição  →  Simulação / Extração  →  Dados  →  modelos CWM, ACPM e CDCM  →  CAFES
```

1. entender a aplicação;
2. implementar uma versão sequencial;
3. paralelizar a aplicação, com uma visão parcial da arquitetura alvo;
4. obter os dados da versão paralela e construir os modelos da aplicação;
5. usar os modelos no CAFES para avaliar mapeamentos, tempo e energia.

---

## 2. Problema proposto

> Este trabalho consiste da modelagem, exploração e caracterização de uma
> aplicação paralela, cujo objetivo é reconhecer objetos por segmentação de
> imagens dispostas em uma matriz de N × M segmentos, sendo cada segmento
> composto por K × L pixels. Denominaremos esta aplicação de ROSMI
> (Reconhecedor de Objetos por Segmentação em Múltiplas Imagens).
>
> "Dada uma matriz de N × M de segmentos de imagens binárias, com pixels de um
> valor que representam o fundo (e.g. valor 0) e com pixels de outro valor que
> representam os objetos (e.g. valor 1), onde um objeto é um conjunto de pixels
> contíguos, o algoritmo de reconhecimento de imagem deve contabilizar todos os
> objetos identificados uma única vez". Deseja-se que a solução proposta
> permita resolver o problema de forma sequencial e paralela.
>
> — enunciado ([`docs/ROSMI_V11.pdf`](../docs/ROSMI_V11.pdf))

A figura do enunciado mostra uma imagem de **N = 2 × M = 3 segmentos**, cada um
com **K = 768 × L = 1024 pixels**, com **13 objetos**. Um deles, o hexágono, é
distribuído em quatro segmentos e só pode ser contado uma vez.

![Figura do enunciado](../docs/imagem.png)

O grupo deve usar o CAFES para determinar o mapeamento dos núcleos nos tiles de
uma NoC 2D ou 3D. Para o consumo de energia e o tempo de computação, podem ser
usados valores sintéticos, desde que a caracterização seja verossímil.

**Atividades pedidas:**

1. Entender a aplicação proposta.
2. Fazer uma versão sequencial da aplicação. Apresentar o algoritmo e a técnica
   de reconhecimento de imagem usada. Executar e verificar a solução no exemplo
   fornecido.
3. Fazer uma versão paralela da aplicação em C ou Java.
4. Usar o CAFES para explorar a aplicação com os modelos **CWM** e **ACPM**
   (e, eventualmente, **CDCM**), explorando os tempos de computação estimados,
   o mapeamento e o consumo de energia de cada modelo.
5. Explorar soluções com diferentes números de processadores, comparando
   desempenho e consumo de energia, em **3 segmentações** de imagem (a do
   exemplo e mais duas, e.g. 8 × 6 e 7 × 7), com arquiteturas alvo de número de
   tiles próximo, mas igual ou superior, ao de núcleos.

**Entrega:** um relatório, de formato livre, com a descrição da aplicação e as
explorações acima.

---

## 3. Desenvolvimento

### 3.1 Ambiente e ferramentas

Tudo o que é necessário está no repositório: o ROSMI (em C), o gerador dos
modelos (`tools/gen_cafes.py`) e o CAFES (`third_party/cafes/`).

```bash
sudo apt install build-essential libpng-dev default-jre
```

- `build-essential`: `gcc` e `make`, para compilar o ROSMI;
- `libpng-dev`: para o ROSMI ler a imagem PNG;
- `default-jre`: Java, para o CAFES;
- Python 3, que já vem no Ubuntu, para gerar os modelos do CAFES.

Compilação:

```bash
make
```

![make](prints/terminal_01_make.png)

São gerados `bin/rosmi_seq` (versão sequencial) e `bin/rosmi_par` (versão
paralela). A compilação não emite nenhum aviso.

---

### 3.2 Atividade 1 — Entendimento da aplicação

#### 3.2.1 A imagem de entrada

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

O enunciado cita só o hexágono, mas a imagem tem **3 objetos distribuídos**.
Somar o que cada segmento vê sozinho dá **18**, e não 13: o hexágono é contado
4 vezes, e o retângulo e a seta 2 vezes cada. A dificuldade do problema está
nessa reconciliação entre segmentos.

#### 3.2.2 A imagem exige conectividade 8

Os contornos da imagem têm a espessura de um traço fino, e nas diagonais os
pixels **só se tocam pela quina**. Ampliando um trecho de diagonal (cada cor é
um objeto diferente para a conectividade 4):

![zoom de uma diagonal](prints/zoom_diagonal_conn4.png)

- Com **conectividade 4**, que só liga vizinhos de lado, cada degrau vira um
  objeto separado: **473 objetos**.
- Com **conectividade 8**, que também liga vizinhos de quina, cada contorno é
  um objeto só: **13 objetos**, a resposta do enunciado.

Por isso todas as execuções a seguir usam `--conn 8`.

---

### 3.3 Atividade 2 — Versão sequencial

#### 3.3.1 Algoritmo e técnica de reconhecimento

A técnica é a **rotulação de componentes conexos** (*connected component
labeling*). A imagem é percorrida linha a linha; cada pixel de objeto recebe um
rótulo, herdado dos vizinhos já visitados. Quando dois vizinhos têm rótulos
diferentes, os dois rótulos são do mesmo objeto, e essa equivalência é anotada
numa estrutura **union-find**. No fim, o número de objetos é o número de
grupos distintos no union-find.

A rotulação guarda só a linha anterior e a linha corrente, não a imagem de
rótulos inteira. A explicação completa, com exemplos, está em
[`docs/background.md`](../docs/background.md), seções 2.1 e 2.2.

#### 3.3.2 Execução no exemplo fornecido

```bash
bin/rosmi_seq reports/imagem_trabalho.png --N 2 --M 3 --conn 8
```

![rosmi_seq](prints/terminal_02_rosmi_seq.png)

- **OBJETOS: 13**, a resposta do enunciado.
- **Soma ingênua: 18**, o que daria somar cada segmento sozinho.

---

### 3.4 Atividade 3 — Versão paralela

#### 3.4.1 Algoritmo paralelo

A versão paralela usa o mesmo núcleo de rotulação, chamado uma vez por
segmento, em três fases:

1. **Local:** cada segmento conta os seus objetos e guarda os rótulos das suas
   quatro bordas. Os segmentos são independentes e rodam em paralelo.
2. **Bordas:** segmentos vizinhos comparam as bordas que se encostam e unem, no
   union-find, os pedaços do mesmo objeto. Só os vetores de borda trafegam,
   nunca os pixels.
3. **Redução:** conta os grupos que sobraram.

Detalhes em [`docs/background.md`](../docs/background.md), seções 2.3 e 2.4.

#### 3.4.2 Execução no exemplo fornecido

```bash
bin/rosmi_par reports/imagem_trabalho.png --N 2 --M 3 --conn 8 --threads 4
```

![rosmi_par](prints/terminal_03_rosmi_par.png)

- **Fase 1:** soma local = 18.
- **Fase 2:** 7 fronteiras, 4 entre vizinhos da mesma linha e 3 entre vizinhos
  da mesma coluna. Com conectividade 8, os cantos onde 4 segmentos se encontram
  também são comparados; por isso trafegam 16 bytes a mais do que com
  conectividade 4.
- **Fase 3:** **13 objetos**.

Para comparar, a mesma imagem com conectividade 4:

```bash
bin/rosmi_par reports/imagem_trabalho.png --N 2 --M 3 --conn 4 --threads 4
```

![rosmi_par conn 4](prints/terminal_04_rosmi_par_conn4.png)

#### 3.4.3 A contagem não depende do número de threads

```bash
for p in 1 2 4 8; do
  bin/rosmi_par reports/imagem_trabalho.png --N 2 --M 3 --conn 8 --threads $p | grep OBJETOS
done
```

![invariância](prints/terminal_05_invariancia.png)

---

### 3.5 Atividade 4 — Exploração no CAFES

#### 3.5.1 Arquitetura alvo e modelo da aplicação

A aplicação é modelada com três tipos de núcleo, um por tile de uma **NoC malha
2D** com roteamento XY:

- `ME`: a memória que guarda a imagem e envia cada segmento ao seu PA;
- `PA0` a `PA5`: um processador por segmento, que faz a rotulação local e troca
  bordas com os vizinhos;
- `PC`: o coletor, que faz a união final e produz a contagem.

No exemplo 2 × 3 são **8 núcleos** numa NoC de **2 × 4 tiles**. A comunicação
acontece em fases:

| Fase | Comunicação | Volume |
|---|---|---|
| 0 | `ME → PAs` | carga do segmento: K·L bits |
| 1 | `PA → PA` (mesma linha) | borda vertical: K rótulos de 32 bits |
| 2 | `PA → PA` (mesma coluna) | borda horizontal: L rótulos de 32 bits |
| 3 | `PAs → PC` | contagem local e pares de equivalência |
| 4 | `PC → PAs` | resultado global |

#### 3.5.2 Obtenção dos dados para modelagem

**Tempo de computação de cada segmento**, medido com 1 thread, para que um
tile não dispute processador com outro, e com a melhor de 8 repetições:

```bash
bin/rosmi_par reports/imagem_trabalho.png --N 2 --M 3 --conn 8 \
    --threads 1 --reps 8 --trace reports/saidas/trabalho_p1
```

![traço](prints/terminal_06_rosmi_par_trace.png)

São gravados `saidas/trabalho_p1_tiles.csv` (tempo e componentes por segmento)
e `saidas/trabalho_p1_edges.csv` (uniões por fronteira).

**Geração dos modelos.** O `gen_cafes.py` calcula o volume de cada mensagem a
partir da segmentação (lida de [`imagem_trabalho.json`](imagem_trabalho.json)),
usa os tempos medidos como computação do CDCM e gera os três arquivos de
entrada do CAFES:

```bash
python3 tools/gen_cafes.py --case reports/imagem_trabalho --out reports/cafes \
    --trace reports/saidas/trabalho_p1_tiles.csv
python3 tools/check_cafes.py reports/cafes/imagem_trabalho
```

![gen_cafes](prints/terminal_07_gen_cafes.png)

![check_cafes](prints/terminal_08_check_cafes.png)

| Arquivo | Modelo |
|---|---|
| `cafes/imagem_trabalho.CWG` | CWM |
| `cafes/imagem_trabalho.ACPG` | ACPM |
| `cafes/imagem_trabalho.cdcg` | CDCM |

Nesta geração, o volume é convertido de bits para phits de 16 bits, e o ACPM
agrupa as mensagens nas 5 fases da tabela da seção 3.5.1.

#### 3.5.3 Decisões de modelagem (unidade do phit, ordem das mensagens no ACPM, extração por instrumentação)

#### 3.5.4 Parâmetros da NoC usados no CAFES

#### 3.5.5 Modelo CWM

##### 3.5.5.1 Busca exaustiva

**Abrir o CAFES:**

```bash
tools/cafes.sh
```

**Abrir o modelo CWM:** clique em **Communication Weight Model (CWM)**.

![janela principal](prints/cafes_01_janela_principal.png)

**Carregar o grafo:** na janela **CWM Mapping**, clique em **File → Load
graph**.

![menu File](prints/cafes_02_menu_file.png)

Na janela de escolha de arquivo, aperte **Ctrl+L**, digite o caminho do `.CWG`
e clique em **Open**:

```
<caminho do repositório>/reports/cafes/imagem_trabalho.CWG
```

![escolher arquivo](prints/cafes_03_escolher_arquivo.png)

**Conferir o grafo:**

![grafo carregado](prints/cafes_04_grafo_carregado.png)

Os números nas setas são os phits trocados entre os núcleos: 49 152 na carga de
cada segmento (`ME → PA`), 1 536 e 2 048 nas bordas (`PA → PA`), 114 no envio
ao coletor (`PA → PC`) e 2 no resultado (`PC → PA`).

Na janela principal, o **NoC size** muda sozinho para **2 · 4 · 1**, lido do
arquivo:

![NoC size](prints/cafes_05_noc_size.png)

**Rodar o mapeamento:** na janela **CWM Mapping**, clique em **Tools →
Exhaustive Search Mapping Algorithm**.

![menu Tools](prints/cafes_06_menu_tools.png)

> **Não use "Compute Mapping" (em laranja) no CWM.** Um bug do CAFES na leitura
> do `.CWG` faz essa opção travar em "Computing..." numa NoC 2D (detalhes em
> [`third_party/cafes/README.md`](../third_party/cafes/README.md)). Os
> algoritmos de busca não são afetados.

Com 8 núcleos, a busca exaustiva termina em poucos segundos.

**Resultado:**

![resultado](prints/cafes_07_resultado.png)

- **Energy = 5079,97 µJ**: a energia do melhor mapeamento possível, porque a
  busca exaustiva testa todas as combinações.
- Cada quadrado preto é um roteador `R[linha, coluna, camada]`, e o losango
  cinza ao lado é o núcleo mapeado nele.
- O **ME** ficou no centro da malha, em `R[0,1]`. A carga `ME → PAs` é quase
  96% do tráfego, então o melhor lugar para a memória é onde ela fica mais
  perto de todos os PAs.

O CWM só enxerga a quantidade de comunicação, que depende da segmentação
(2 × 3 de 768 × 1024) e não do desenho da imagem.

##### 3.5.5.2 Simulated Annealing

##### 3.5.5.3 Busca Tabu

##### 3.5.5.4 Estimativa de tempo

#### 3.5.6 Modelo ACPM

##### 3.5.6.1 Mapeamento

##### 3.5.6.2 Tempo de execução

##### 3.5.6.3 Consumo de energia

#### 3.5.7 Modelo CDCM

##### 3.5.7.1 Tempo de execução e caminho crítico

##### 3.5.7.2 Consumo de energia

#### 3.5.8 Comparação entre os modelos

---

### 3.6 Atividade 5 — Número de processadores × desempenho × energia

#### 3.6.1 Segmentações e arquiteturas alvo

As três segmentações mantêm o tamanho do segmento (768 × 1024 pixels) e
aumentam o número de segmentos. Cada uma vira um PA por segmento, mais o ME e o
PC:

| Segmentação | Imagem | Núcleos | NoC (tiles) |
|---|---|---|---|
| 2 × 3 | imagem oficial (`imagem_trabalho.png`) | 8 | 2 × 4 = 8 |
| 8 × 6 | sintética (`data/grande_8x6.pbm`) | 50 | 5 × 10 = 50 |
| 7 × 7 | sintética (`data/grande_7x7.pbm`) | 51 | 6 × 9 = 54 |

Os modelos das segmentações 8 × 6 e 7 × 7 estão em [`cafes/`](../cafes/).

#### 3.6.2 Resultados no CAFES

#### 3.6.3 Análise

---

## 4. Resumo dos resultados

| Etapa | Resultado |
|---|---|
| `rosmi_seq --conn 8` | **13** (soma ingênua 18) |
| `rosmi_par --conn 8`, 1/2/4/8 threads | **13** em todas |
| `rosmi_par --conn 4` | 473 |
| CAFES, CWM, busca exaustiva, NoC 2 × 4 | **5079,97 µJ** |

## 5. Conteúdo desta pasta

```
reports/
  README.md                este registro
  imagem_trabalho.png      imagem oficial do trabalho (entrada do ROSMI)
  imagem_trabalho.json     segmentação (N, M, K, L), lida pelo gen_cafes.py
  saidas/                  saída de cada comando (.txt) e traços (.csv)
  cafes/                   modelos .CWG, .ACPG e .cdcg para o CAFES
  prints/                  imagens deste registro
```
