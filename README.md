# ROSMI — Reconhecedor de Objetos por Segmentação em Múltiplas Imagens

Conta quantos objetos existem numa imagem binária que foi dividida em uma
matriz de **N × M segmentos** de **K × L pixels**, garantindo que um objeto
atravessando vários segmentos seja contado **uma única vez**.

Implementação em C, com versão sequencial e versão paralela parametrizável.

---

## 1. O problema

Uma imagem binária tem apenas dois valores: **0 é fundo** e **1 é objeto**. Um
objeto é um conjunto de pixels contíguos — pixels acesos que se encostam formam
um único objeto.

A imagem é grande e foi recortada em um tabuleiro de N × M segmentos, cada um
com K × L pixels. No exemplo do enunciado são 2 × 3 segmentos de 768 × 1024
pixels cada.

Contar objetos dentro de um segmento é um problema clássico e resolvido. A
dificuldade está em outro lugar: **um objeto pode atravessar a fronteira entre
segmentos**, e nesse caso cada lado o enxerga como se fosse um objeto próprio.

### Por que não basta dividir e somar

Vamos a uma imagem de 8 × 4 pixels, dividida em 2 × 2 segmentos de 2 linhas por
4 colunas. É pequena o bastante para conferir no papel e já contém toda a
dificuldade do caso real.

```
        c0 c1 c2 c3 | c4 c5 c6 c7
  r0:    #  #  .  . |  .  .  .  .
  r1:    .  .  .  # |  #  .  .  .
        ------------+------------
  r2:    .  .  .  . |  .  .  #  .
  r3:    .  #  .  . |  .  .  #  .
```

Olhando a imagem inteira, de uma vez, há **4 objetos**:

| Objeto | Pixels | Onde está |
|---|---|---|
| **A** | (r0,c0), (r0,c1) | inteiro no segmento (0,0) |
| **B** | (r1,c3), (r1,c4) | **atravessa** a fronteira entre (0,0) e (0,1) |
| **C** | (r3,c1) | inteiro no segmento (1,0) |
| **D** | (r2,c6), (r3,c6) | inteiro no segmento (1,1) |

Agora a abordagem ingênua: cada segmento conta o que vê, e somamos no final.

| Segmento | O que enxerga sozinho | Conta |
|---|---|---|
| (0,0) | A inteiro, e a metade esquerda de B | 2 |
| (0,1) | só a metade direita de B | 1 |
| (1,0) | C | 1 |
| (1,1) | D | 1 |
| | **soma** | **5** |

Deu **5**, mas a resposta é **4**. O objeto B foi contado duas vezes, porque
nenhum dos dois lados tinha como saber que a outra metade existia.

Não é um detalhe de borda. Nos casos grandes deste projeto a soma ingênua erra
em **38%** — 380 contra 275, e 389 contra 281. Qualquer solução que particione a
imagem e some os resultados está errada por construção.

> **A reconciliação entre segmentos é o núcleo do problema, não um acessório.**

---

## 2. A estratégia

### 2.1 Reconhecer objetos: componentes conexos

A técnica usada é **rotulação de componentes conexos** (*Connected Component
Labeling*). A imagem é percorrida linha por linha, da esquerda para a direita,
como quem lê um texto. Cada pixel de objeto recebe um **rótulo**, de forma que
pixels do mesmo objeto terminem com o mesmo rótulo.

Para cada pixel de objeto olhamos apenas os vizinhos **já visitados** — os que
ficam para trás na leitura:

```
   conectividade 4            conectividade 8
      . N .                      NW N NE
      W X .                      W  X  .
      . . .                      .  .  .
```

- **Nenhum vizinho rotulado?** Começa um objeto novo, cria um rótulo novo.
- **Um vizinho rotulado?** Herda o rótulo dele.
- **Dois vizinhos com rótulos diferentes?** Descobrimos que aqueles dois rótulos
  são, na verdade, o mesmo objeto — e anotamos essa equivalência.

A conectividade é parametrizável (`--conn 4`, padrão, ou `--conn 8`): dois
pixels ligados só pela diagonal formam um objeto em conectividade 8 e dois
objetos em conectividade 4.

### 2.2 Anotar equivalências: union-find

Considere esta situação no meio da varredura:

```
   .  #  .  #  .      os rótulos 1 e 2 parecem objetos distintos...
   .  #  #  #  .      ...mas esta linha mostra que são o mesmo
```

Reescrever todos os pixels do rótulo 2 para 1 seria lentíssimo num objeto
grande. Em vez disso, não reescrevemos nada: apenas anotamos num caderno que
"1 e 2 são o mesmo grupo". Esse caderno é a estrutura de **union-find**.

Pensando por analogia: cada rótulo é uma pessoa e cada objeto é uma família.

- `find(x)` — *quem é o chefe da família de x?* Pergunta-se a x, depois ao chefe
  dele, até chegar em alguém que aponta para si mesmo.
- `union(a, b)` — *estas duas famílias são uma só.* Acha-se o chefe de cada uma
  e um passa a apontar para o outro. Uma única alteração, não importa o tamanho.

No final, **o número de objetos é o número de chefes que sobraram**.

### 2.3 Paralelizar: rotular localmente, unir nas bordas

> O diagrama [`docs/fluxo-processamento.excalidraw`](docs/fluxo-processamento.excalidraw)
> percorre as três fases sobre esta mesma imagem de 8 × 4 pixels.

A parte fácil é que cada segmento pode ser rotulado sozinho, sem falar com
ninguém. A parte difícil é a reconciliação. E aqui está a ideia central do
projeto:

> **A ferramenta certa já está na mão.** O union-find, usado *dentro* de um
> segmento para unir rótulos equivalentes, serve igualmente *entre* segmentos
> para unir componentes que se tocam na fronteira. É a mesma estrutura, aplicada
> num segundo nível. A versão paralela não é um algoritmo novo.

O algoritmo tem três fases.

**Fase 1 — rotulação local (paralela, sem comunicação).**
Cada um dos N·M segmentos roda exatamente o mesmo núcleo de rotulação, na sua
própria fatia. Produz dois resultados: quantos componentes locais encontrou, e
os rótulos das suas **quatro bordas** — primeira linha, última linha, primeira
coluna, última coluna. Isso é tudo que os vizinhos precisam saber.

Na imagem miniatura:

| Segmento | O que vê | Componentes locais |
|---|---|---|
| (0,0) | `# # . .` / `. . . #` | 2 — o objeto A e a ponta esquerda de B |
| (0,1) | `. . . .` / `# . . .` | 1 — a ponta direita de B |
| (1,0) | `. . . .` / `. # . .` | 1 — o objeto C |
| (1,1) | `. . # .` / `. . # .` | 1 — o objeto D |
| | **soma** | **5** |

Cinco é a soma ingênua. Ainda está errado — a próxima fase conserta.

**Fase 2 — troca de bordas (comunicação entre vizinhos).**
Cada segmento numerou seus componentes a partir do zero, então o "componente 0"
de um não é o "componente 0" de outro. Uma soma de prefixos dá a cada segmento
uma faixa própria no espaço global:

| Segmento | Locais | Faixa global |
|---|---|---|
| (0,0) | 2 | **0, 1** |
| (0,1) | 1 | **2** |
| (1,0) | 1 | **3** |
| (1,1) | 1 | **4** |

Agora cada par de segmentos vizinhos compara as bordas encostadas. Onde houver
pixel de objeto **dos dois lados, na mesma altura**, os componentes são o mesmo.

```
   segmento (0,0)        segmento (0,1)
   borda direita (c3)    borda esquerda (c4)
       r0:  .        |       .          → ambos fundo, nada a fazer
       r1:  #        |       #          → objeto dos DOIS lados → union
```

O componente local 1 de (0,0) é o global **1**; o componente local 0 de (0,1) é
o global **2**. Logo: `union(1, 2)`. As outras três fronteiras não produzem
nenhuma união.

**Fase 3 — contagem.**
Resta perguntar ao union-find quantos chefes sobraram entre 0, 1, 2, 3, 4:

Grupos distintos: **{0}, {1,2}, {3}, {4}** → **4 objetos.** ✅

O que o programa responde na mesma imagem:

```
$ bin/rosmi_par data/mini.pbm --N 2 --M 2 --threads 4

  OBJETOS           : 4
  soma local        : 5 (1 a mais: objetos distribuidos)
  fase 2 (bordas)   : [4 arestas, 48 bytes, 1 unioes]
```

Os números batem com o traçado a mão: 5 componentes locais, 1 união, 4 objetos.
E apenas **48 bytes** atravessaram as fronteiras — o objeto B poderia ter mil
pixels, e ainda assim o que trafega é só uma linha de rótulos.

### 2.4 O caso que quase escapa

Em conectividade 8 existe uma situação que as duas varreduras de fronteira não
alcançam. No ponto onde **quatro segmentos se encontram**:

```
        segmento A     |  segmento B
                   . # | # .
                   . . | . .
        ---------------+---------------
                   . . | . .
                   . X | Y .
        segmento C     |  segmento D
```

O pixel **X** está no canto inferior direito de A e o pixel **Y** no canto
superior esquerdo de D. Eles são vizinhos na diagonal, portanto o mesmo objeto.
Mas esse par **atravessa duas fronteiras ao mesmo tempo**: a comparação "A com
B" não o vê, porque D não participa dela; a comparação "A com C" também não.

O algoritmo trata esses pares de canto explicitamente. Que isso importa não é
suposição: removendo o tratamento, a suíte de testes acusa a diferença
imediatamente.

---

## 3. Arquitetura do código

> O diagrama [`docs/arquitetura-modulos.excalidraw`](docs/arquitetura-modulos.excalidraw)
> mostra este fluxo bloco a bloco, indicando o arquivo `.c` que implementa cada
> etapa e o dado que atravessa cada seta.

O código foi organizado em **três camadas**, com uma regra: cada camada só pode
depender das que estão abaixo dela.

```
   ┌──────────────────────────────────────────────────┐
   │  Programas                                       │
   │  RosmiSeqMain.c          RosmiParMain.c          │
   ├──────────────────────────────────────────────────┤
   │  Apoio                                           │
   │  RosmiCli.c              RosmiReport.c           │
   ├──────────────────────────────────────────────────┤
   │  Camada de tarefas  (só ela conhece threads)     │
   │  RosmiTask.c  ──────────  RosmiPosixTask.c       │
   ├──────────────────────────────────────────────────┤
   │  Núcleo funcional  (sequencial puro)             │
   │  RosmiLabel.c    RosmiMerge.c   RosmiUnionFind.c │
   │  RosmiImage.c    Rosmi.c                         │
   └──────────────────────────────────────────────────┘
```

A decisão que guia todo o resto: **o núcleo funcional não sabe que threads
existem**. Ele é sequencial puro. Quem distribui trabalho é uma camada acima, e
quem fala com o sistema operacional é um único arquivo acima dessa.

Isso tem três consequências práticas:

- o núcleo pode ser testado em isolamento, sem concorrência no meio;
- a versão sequencial reaproveita o núcleo **sem alteração nenhuma**;
- trocar de plataforma é trocar um arquivo.

### 3.1 O núcleo funcional

| Arquivo | O que faz |
|---|---|
| `Rosmi.h` / `Rosmi.c` | Tipos comuns do componente: o rótulo, o índice de segmento, a conectividade e os códigos de retorno. Também concentra o acesso a memória, tempo e contagem de processadores, de modo que nenhum outro módulo chame `malloc` ou o relógio diretamente. |
| `RosmiImage.h` / `.c` | A imagem binária e a leitura do arquivo PBM. Os pixels ficam **empacotados a 8 por byte** — para a segmentação de 7 × 7 isso reduz a imagem de 38 MB para 4,8 MB. O acesso a um pixel é uma função `inline`, porque é chamada uma vez por pixel no laço mais interno. |
| `RosmiUnionFind.h` / `.c` | A estrutura da seção 2.2, com união por tamanho e compressão de caminho. É usada nos **dois níveis** do algoritmo, e é essa reutilização que dispensa um algoritmo novo na versão paralela. |
| `RosmiLabel.h` / `.c` | A rotulação de um retângulo da imagem. É **o mesmo código nas duas versões**: a sequencial chama uma vez com o retângulo igual à imagem inteira, a paralela chama uma vez por segmento. Não existem dois algoritmos, existe um chamado de dois jeitos. |
| `RosmiMerge.h` / `.c` | A reconciliação entre segmentos: as fases 2 e 3. Consome apenas os vetores de borda e **nunca toca em pixels** — é por isso que a comunicação entre segmentos é barata. |

**Uma decisão de memória que define o projeto.** A rotulação clássica guarda a
imagem de rótulos inteira. Esta não guarda: como só precisamos da *contagem*,
basta manter **a linha anterior e a linha corrente**. Para um segmento de
768 × 1024, é a diferença entre 1024 e 786 432 rótulos em memória.

Não é economia cosmética. É o que torna o algoritmo implementável num
processador pequeno com pouca memória local, que é exatamente o alvo deste
trabalho: um tile precisa guardar o seu segmento e um punhado de vetores de
borda, não a imagem.

### 3.2 A camada de tarefas

| Arquivo | O que faz |
|---|---|
| `RosmiTask.h` / `.c` | Distribui a rotulação dos N·M segmentos entre as threads e espera todas terminarem. É o único módulo que sabe que concorrência existe. |
| `RosmiPosixTask.c` | Implementa a criação e a junção de thread usando POSIX threads. |

**Repartição estática, sem trava.** As tarefas não passam por uma fila protegida
por mutex. Cada thread `t`, de um total `P`, processa os segmentos cujo índice
satisfaz `(s % P) == t`, decidido antes de qualquer thread começar.

A escolha se apoia numa propriedade medida: o custo de um tile é dominado pela
varredura de K·L pixels, que é **idêntica para todo segmento, independentemente
do que ele contém**. A correlação medida entre o tempo de um tile e a densidade
de objetos do seu segmento é **+0,08** — ou seja, nenhuma. Com a carga já
equilibrada por construção, uma fila dinâmica só acrescentaria contenção sem
melhorar o equilíbrio. A repartição é intercalada, e não em blocos contíguos,
para diluir qualquer gradiente espacial da imagem.

Como cada thread escreve apenas em posições próprias dos vetores de saída, não
há disputa e nenhuma seção crítica é necessária.

**Por que a plataforma fica num arquivo separado.** `RosmiTask.c` contém a
lógica portável — validar, alocar, repartir, coletar. O que varia entre
plataformas são só duas operações: criar uma thread e esperá-la terminar, e
essas ficam isoladas em `RosmiPosixTask.c`. Trocar de ambiente é trocar esse
arquivo; nem o algoritmo nem a distribuição de trabalho mudam.

### 3.3 Apoio e programas

| Arquivo | O que faz |
|---|---|
| `RosmiCli.h` / `.c` | Leitura da linha de comando, compartilhada pelos dois programas. As opções são descritas por tabelas, e não por uma cadeia de comparações. |
| `RosmiReport.h` / `.c` | Grava o CSV de resultados e os traços por tile e por aresta, que alimentam a caracterização da arquitetura. |
| `RosmiSeqMain.c` | Programa sequencial: trata a imagem inteira como um bloco único. Reporta também a soma ingênua, para tornar visível o tamanho do erro que ela cometeria. |
| `RosmiParMain.c` | Programa paralelo: executa as três fases e reporta o tempo de cada uma separadamente. |

**O binário sequencial não liga a camada de tarefas.** `RosmiSeqMain` é
construído apenas com o núcleo funcional. Isso *garante*, e não apenas promete,
que não há concorrência nele — serve de linha de base honesta para medir o
ganho do paralelismo.

### 3.4 Fluxo de uma execução paralela

```
  RosmiParMain
      │
      ├─ RosmiCli ............... lê a linha de comando
      ├─ RosmiImage ............. carrega o .pbm
      │
      ├─ RosmiTask .............. FASE 1
      │     └─ RosmiLabel ....... um segmento por vez, em paralelo
      │           └─ RosmiUnionFind
      │        saída: componentes locais + 4 bordas por segmento
      │
      ├─ RosmiMerge ............. FASE 2
      │     ├─ soma de prefixos → espaço global de rótulos
      │     └─ compara bordas vizinhas → uniões
      │
      ├─ RosmiUnionFind ......... FASE 3: conta as raízes distintas
      │
      └─ RosmiReport ............ grava CSV e traços
```

---

## 4. Como compilar e rodar

Requer um compilador C11 com POSIX threads, `make` e, para gerar imagens de
teste, Python com `numpy`, `scipy` e `pillow`.

```bash
make              # gera bin/rosmi_seq e bin/rosmi_par
make test         # compila e roda a suíte de testes unitários
make coverage     # roda a suíte instrumentada e emite o relatório de cobertura
make clean
```

```bash
bin/rosmi_seq <imagem.pbm> --N <n> --M <m> [--conn 4|8] [--reps <r>] [--csv <f>]

bin/rosmi_par <imagem.pbm> --N <n> --M <m> [--conn 4|8] [--threads <p>]
              [--reps <r>] [--csv <f>] [--trace <prefixo>]
```

| Opção | Significado |
|---|---|
| `--N`, `--M` | segmentos na vertical e na horizontal |
| `--conn` | critério de vizinhança, 4 (padrão) ou 8 |
| `--threads` | número de threads; por omissão, todos os processadores |
| `--reps` | repete a medição e reporta a melhor |
| `--csv` | acrescenta uma linha de resultados ao arquivo |
| `--trace` | grava `<prefixo>_tiles.csv` e `<prefixo>_edges.csv` |

### Entendendo a saída

```
ROSMI paralelo (C)
  imagem            : data/grande_7x7.pbm (5376 x 7168 px)
  segmentacao       : N=7 x M=7 = 49 segmentos de K=768 x L=1024
  conectividade     : 4
  threads           : 8
  OBJETOS           : 281          ← a resposta
  soma local        : 389 (108 a mais: objetos distribuidos)
  fase 1 (local)    : 0.012753 s   [soma dos tiles 0.096595 s, tile mais lento 0.006713 s]
  fase 2 (bordas)   : 0.000086 s   [84 arestas, 301056 bytes, 10919 unioes]
  fase 3 (reducao)  : 0.000001 s
  TEMPO TOTAL       : 0.012840 s
```

| Linha | O que significa |
|---|---|
| `OBJETOS` | a resposta do problema |
| `soma local` | o que a abordagem ingênua teria respondido |
| `soma dos tiles` | tempo de CPU somado de todos os segmentos; **maior** que a fase 1 justamente porque rodaram em paralelo |
| `tile mais lento` | quem determina o fim da fase 1 — todos esperam o último |

---

## 5. Casos de teste

| Caso | N × M | Segmentos | Imagem | Objetos | Distribuídos | Soma ingênua |
|---|---|---|---|---|---|---|
| `mini` | 2 × 2 | 4 | 8 × 4 | **4** | 1 | 5 |
| `exemplo_2x3` | 2 × 3 | 6 | 1536 × 3072 | **13** | 1 | 16 |
| `grande_8x6` | 8 × 6 | 48 | 6144 × 6144 | **275** | 35 | 380 |
| `grande_7x7` | 7 × 7 | 49 | 5376 × 7168 | **281** | 36 | 389 |

O caso `exemplo_2x3` reproduz a figura do enunciado: 13 objetos, um deles
atravessando os quatro segmentos centrais. Em todos os casos grandes há objetos
tocando 4 segmentos simultaneamente.

As imagens são geradas por `tools/gen_image.py`, que conhece a resposta **por
construção**: nenhum objeto encosta em outro, então o número de componentes
conexos é igual ao número de formas desenhadas. `tools/validate.py` reconfere
com uma biblioteca externa independente.

---

## 6. Validação

O item 2 do enunciado pede executar e verificar a solução no exemplo fornecido.
A verificação é feita em duas frentes.

### A propriedade cobrada

> **O número de objetos de uma imagem não pode depender de quantas threads
> foram usadas para contá-los, nem de como a imagem foi segmentada.**

Se depender, a reconciliação entre segmentos está errada. É essa invariância que
os testes exigem.

### Ponta a ponta

`tools/check.py` roda os dois binários sobre as imagens reais, variando
conectividade (4 e 8) e número de threads (1, 2, 4 e 8):

```
mini        (2x2)  esperado 4     10/10 combinações ok
exemplo_2x3 (2x3)  esperado 13    10/10 combinações ok
grande_8x6  (8x6)  esperado 275   10/10 combinações ok
grande_7x7  (7x7)  esperado 281   10/10 combinações ok
```

O caso `exemplo_2x3` é a figura do enunciado: **13 objetos**, com um deles
atravessando os quatro segmentos centrais.

### Testes unitários

```
make test        # 580 asserções em 7 grupos, uma por módulo
make coverage    # 94,4% de linhas e 86,3% de ramos
```

`TestRosmiMerge` compara o resultado das três fases contra a contagem da imagem
inteira em seis segmentações diferentes, e `TestRosmiTask` repete a rotulação
com 1 a 32 threads exigindo sempre o mesmo número.

A compilação não emite nenhum aviso com `-Wall -Wextra -Wpedantic -Wshadow
-Wconversion -Wsign-conversion -Wcast-qual -Wstrict-prototypes
-Wmissing-prototypes`.

---

## 7. Desempenho

Intel i7-1165G7, **4 núcleos físicos / 8 lógicos**, GCC 16.2 com `-O2`, caso
`grande_7x7` (49 segmentos de 768 × 1024), melhor de 8 repetições:

| Threads | Tempo | Speedup |
|---:|---:|---:|
| sequencial | 0,0395 s | — |
| 1 | 0,0400 s | 1,00× |
| 2 | 0,0212 s | 1,89× |
| 4 | 0,0122 s | 3,28× |
| 8 | 0,0122 s | 3,28× |

A saturação em 4 threads é a esperada: a aplicação lê muitos pixels e faz pouca
conta com cada um, ou seja, é limitada por **banda de memória**. A máquina tem 4
núcleos físicos, e as threads 5 a 8 são *hyperthreads*, que compartilham
hardware e pouco acrescentam.

Vale observar que isso **subestima** a arquitetura alvo. Numa rede em chip cada
tile tem processador **e memória locais**, e essa disputa simplesmente não
existe. A medição aqui é um piso, não um teto.

Dois outros números dão a dimensão do desenho:

- **A fase de bordas é irrelevante no tempo.** Medida em fração de milissegundo
  contra dezenas de milissegundos da fase local: 0,086 ms contra 12,8 ms, ou
  0,7% do tempo total.
- **O teto de paralelismo é o número de segmentos.** Não há mais que N·M
  unidades independentes de trabalho; para escalar mais, segmenta-se mais fino.

---

## 8. Modelos para o CAFES

Os itens 4 e 5 do enunciado pedem a exploração da aplicação no CAFES. A ponte é
`tools/gen_cafes.py`, que traduz a aplicação para os três formatos de entrada da
ferramenta, a partir da mesma descrição, de modo que não possam divergir:

| Modelo | Arquivo | O que descreve |
|---|---|---|
| **CWM** | `.CWG` | quem fala com quem e quantos phits, sem noção de tempo |
| **ACPM** | `.ACPG` | o mesmo, com as mensagens agrupadas em fases |
| **CDCM** | `.cdcg` | fases, dependências e o **tempo de computação** de cada etapa |

A aplicação é modelada como `ME` (a memória que guarda a imagem), um `PA` por
segmento e `PC` (o coletor, que faz a união global e produz a contagem). As
fases são as mesmas da seção 2.3, mais a carga inicial:

| Fase | Comunicação | Volume |
|---|---|---|
| 0 | `ME → PAs` | carga do segmento: K·L bits = 49 152 phits |
| 1 e 2 | `PAs → vizinhos` | bordas: 1 536 e 2 048 phits |
| 3 | `PAs → PC` | pares de equivalência: 114 phits |
| 4 | `PC → PAs` | resultado: 2 phits |

O tempo de computação de cada tile vem do traço medido (`rosmi_par --trace`), e
não de estimativa. Usa-se o traço de **uma thread**: com várias threads
disputando os mesmos núcleos e a mesma memória da máquina de desenvolvimento, o
tempo por tile incha e passa a medir contenção do host, não o trabalho do tile.
Na NoC alvo cada tile tem processador e memória próprios.

```bash
python3 tools/gen_cafes.py --case data/grande_7x7 --out cafes/         --trace results/grande_7x7_p1_tiles.csv
python3 tools/check_cafes.py cafes/grande_7x7
```

`check_cafes.py` confere os arquivos contra a gramática que o CAFES espera —
seções obrigatórias, malha com tiles suficientes, arestas ligando apenas núcleos
declarados e matriz de mapeamento com as dimensões exatas da malha.

### O que o tráfego revela

| Caso | Carga (ME→PA) | Bordas (PA→PA) | Total |
|---|---|---|---|
| `exemplo_2x3` | 294 912 (95,8%) | 12 288 (4,0%) | 307 896 |
| `grande_8x6` | 2 359 296 (93,9%) | 147 456 (5,9%) | 2 512 320 |
| `grande_7x7` | 2 408 448 (93,9%) | 150 528 (5,9%) | 2 564 660 |

**A troca de bordas custa cerca de 6% do tráfego; quase 94% é alimentar os tiles
com os pixels.** A parte conceitualmente difícil do problema é barata em
comunicação — o gargalo da arquitetura é a banda da memória para os tiles, não o
diálogo entre vizinhos.

Isso tem consequência direta para o item 5: segmentar mais fino **não** aumenta o
tráfego de carga, que depende só do tamanho da imagem. Aumenta apenas a parcela
das bordas, que é a pequena.

### Estado

Os nove arquivos (3 segmentações × 3 modelos) estão gerados e validados, e
carregam na ferramenta. **A avaliação em si — rodar os mapeamentos e colher
tempo e energia — ainda não foi feita**, e exige a interface do CAFES.

---

## 9. Organização dos arquivos

```
src/
  Rosmi.h  .c            tipos, códigos de retorno, memória e tempo
  RosmiImage.h  .c       imagem empacotada em bits e leitor PBM
  RosmiUnionFind.h  .c   conjuntos disjuntos
  RosmiLabel.h  .c       rotulação de componentes conexos
  RosmiMerge.h  .c       reconciliação entre segmentos
  RosmiTask.h  .c        distribuição das tarefas entre threads
  RosmiPosixTask.c       criação e junção de thread em POSIX
  RosmiCli.h  .c         linha de comando
  RosmiReport.h  .c      CSV de resultados e traços
  RosmiSeqMain.c         programa sequencial
  RosmiParMain.c         programa paralelo

cafes/                   modelos CWM, ACPM e CDCM das tres segmentacoes
docs/                    diagramas em .excalidraw: fluxo e arquitetura
test/                    um arquivo de teste por módulo
tools/                   geração de imagens, validação e relatórios
data/                    imagens de teste com resposta conhecida
```
