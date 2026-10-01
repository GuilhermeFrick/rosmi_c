#!/usr/bin/env python3
"""
Gerador das entradas do CAFES para o ROSMI.

Traduz a aplicacao paralela (N x M segmentos) nos tres formatos textuais que o
CAFES le, todos derivados da MESMA descricao da aplicacao:

  .CWG   modelo CWM  -- grafo de comunicacao: quem fala com quem e quantos
                        phits trafegam. Nao tem nocao de tempo.
  .ACPG  modelo ACPM -- o mesmo grafo, com as arestas agrupadas em FASES
                        (tags). Captura que a carga dos segmentos, a troca de
                        bordas e a reducao nao acontecem ao mesmo tempo.
  .cdcg  modelo CDCM -- grafo de dependencias entre mensagens, com o tempo de
                        COMPUTACAO associado a cada uma. E o unico dos tres que
                        enxerga o custo de processamento, nao so o de trafego.

Modelo da aplicacao (mesma estrutura do exemplo SegImag que acompanha o CAFES,
generalizada para N x M segmentos):

  ME          memoria que guarda a imagem
  PA0..PAs-1  um nucleo por segmento; PAs fica no tile (r, c) correspondente ao
              segmento (r, c), de modo que vizinhos na imagem sao vizinhos na
              NoC e a troca de bordas vira comunicacao de 1 hop
  PC          nucleo coletor, que aplica o union-find global e conta

Fases:
  0  ME -> PAs            carga do segmento (K*L bits)
  1  PAs -> PA(leste)     troca da borda vertical   (K rotulos de 32 bits)
  2  PAs -> PA(sul)       troca da borda horizontal (L rotulos de 32 bits)
  3  PAs -> PC            contagem local + pares de equivalencia
  4  PC  -> PAs           resultado global

Uso:
  python3 tools/gen_cafes.py --case data/grande_7x7 --out cafes/
  python3 tools/gen_cafes.py --case data/grande_7x7 --out cafes/ \\
          --trace results/grande_7x7_p8_tiles.csv
"""

import argparse
import csv
import json
import math
import os

# Um phit e a unidade de largura do link da NoC. 16 bits e o valor usado nos
# exemplos que acompanham o CAFES.
DEFAULT_PHIT_BITS = 16
# Rotulos sao int32 no codigo C; e esse o tamanho que trafega nas bordas.
LABEL_BITS = 32


def phits(nbits, phit_bits):
    """Quantos phits sao necessarios para transportar nbits bits."""
    return max(1, math.ceil(nbits / phit_bits))


MAX_ASPECT = 2.0  # malhas mais alongadas que isto sao descartadas


def noc_size(n_cores, prefer=None):
    """Malha com tiles suficientes para os nucleos.

    O enunciado pede arquiteturas com numero de tiles "proximo, mas igual ou
    superior" ao necessario. Minimizar so a sobra, porem, leva a malhas
    degeneradas: 51 nucleos caberiam exatamente numa 3x17, que e pessima como
    NoC -- o diametro explode e a vizinhanca da imagem deixa de corresponder a
    vizinhanca da malha, que e a propriedade que faz a troca de bordas custar
    1 hop. Por isso descartamos antes as malhas com razao de aspecto acima de
    MAX_ASPECT e so entao minimizamos a sobra.
    """
    if prefer:
        return prefer
    best = None
    for rows in range(1, n_cores + 1):
        for cols in range(1, n_cores + 1):
            if rows * cols < n_cores:
                continue
            if max(rows, cols) / min(rows, cols) > MAX_ASPECT:
                continue
            waste = rows * cols - n_cores
            key = (waste, abs(rows - cols), rows)
            if best is None or key < best[0]:
                best = (key, (rows, cols))
    if best is None:                       # nenhuma malha razoavel: relaxa
        rows = math.ceil(math.sqrt(n_cores))
        return (rows, math.ceil(n_cores / rows))
    return best[1]


def load_trace(path):
    """Le o CSV por tile produzido por rosmi_par --trace."""
    if not path or not os.path.exists(path):
        return None
    out = {}
    with open(path, newline="") as f:
        for row in csv.DictReader(f):
            out[int(row["segmento"])] = {
                "tempo_s": float(row["tempo_s"]),
                "componentes": int(row["componentes_locais"]),
            }
    return out


class App:
    """Descricao da aplicacao, independente do formato de saida."""

    def __init__(self, N, M, K, L, phit_bits, trace, freq_hz, cycles_per_pixel,
                 writeback):
        self.N, self.M, self.K, self.L = N, M, K, L
        self.S = N * M
        self.phit_bits = phit_bits
        self.writeback = writeback

        self.cores = ["ME"] + [f"PA{s}" for s in range(self.S)] + ["PC"]

        # --- custo de computacao por tile, em ciclos -----------------------
        # Com traco: converte o tempo medido para ciclos na frequencia alvo.
        # Sem traco: usa um modelo analitico de ciclos por pixel. Os dois
        # caminhos produzem a mesma ordem de grandeza; o traco so torna o
        # desbalanceamento entre tiles realista.
        self.cycles = {}
        for s in range(self.S):
            if trace and s in trace:
                self.cycles[s] = max(1, round(trace[s]["tempo_s"] * freq_hz))
            else:
                self.cycles[s] = max(1, round(cycles_per_pixel * K * L))

        # Custo do coletor: proporcional ao numero de rotulos locais que ele
        # precisa unir. Sem traco, estima-se pelo perimetro dos segmentos.
        if trace:
            ncomp = sum(trace[s]["componentes"] for s in trace)
        else:
            ncomp = self.S * 8
        self.pc_cycles = max(1, round(0.5 * ncomp))
        self.ncomp_total = ncomp

        # --- volumes de comunicacao, em phits ------------------------------
        self.ph_seg = phits(K * L, phit_bits)                 # ME -> PA
        self.ph_vborder = phits(K * LABEL_BITS, phit_bits)    # borda leste/oeste
        self.ph_hborder = phits(L * LABEL_BITS, phit_bits)    # borda norte/sul
        # PA -> PC: contagem local + pares de equivalencia das bordas que o
        # tile recebeu. Estimado em 2 palavras por par, com o numero de pares
        # limitado pelo perimetro do segmento.
        pairs = max(1, (K + L) // 64)
        self.ph_result = phits((1 + 2 * pairs) * LABEL_BITS, phit_bits)
        self.ph_final = phits(LABEL_BITS, phit_bits)          # PC -> PA

    def seg(self, r, c):
        return r * self.M + c

    def edges(self):
        """Todas as comunicacoes, na ordem das fases.

        Devolve tuplas (fase, origem, destino, phits, computacao_em_ciclos).
        A computacao da rotulacao local e atribuida a PRIMEIRA mensagem que o
        tile envia e zerada nas seguintes, para nao contar o mesmo trabalho
        duas vezes quando um tile fala com dois vizinhos.
        """
        out = []
        N, M = self.N, self.M

        # fase 0: carga dos segmentos
        for s in range(self.S):
            out.append((0, "ME", f"PA{s}", self.ph_seg, 0))

        ja_computou = set()

        def comp(s):
            if s in ja_computou:
                return 0
            ja_computou.add(s)
            return self.cycles[s]

        # fase 1: bordas verticais (vizinho a leste)
        for r in range(N):
            for c in range(M - 1):
                a, b = self.seg(r, c), self.seg(r, c + 1)
                out.append((1, f"PA{a}", f"PA{b}", self.ph_vborder, comp(a)))

        # fase 2: bordas horizontais (vizinho ao sul)
        for r in range(N - 1):
            for c in range(M):
                a, b = self.seg(r, c), self.seg(r + 1, c)
                out.append((2, f"PA{a}", f"PA{b}", self.ph_hborder, comp(a)))

        # fase 3: resultados parciais para o coletor
        for s in range(self.S):
            out.append((3, f"PA{s}", "PC", self.ph_result, comp(s)))

        # fase 4: resultado global de volta
        for s in range(self.S):
            out.append((4, "PC", f"PA{s}", self.ph_final,
                        self.pc_cycles if s == 0 else 0))

        # fase 5 (opcional): devolucao dos segmentos a memoria
        if self.writeback:
            for s in range(self.S):
                out.append((5, f"PA{s}", "ME", self.ph_seg, 0))
        return out

    # ------------------------------------------------------------------ #
    # Posicoes so para o desenho na interface do CAFES                    #
    # ------------------------------------------------------------------ #
    def core_xy(self):
        xy = {"ME": (50, 50)}
        for r in range(self.N):
            for c in range(self.M):
                xy[f"PA{self.seg(r, c)}"] = (150 + c * 90, 130 + r * 70)
        xy["PC"] = (150 + self.M * 90, 50)
        return xy

    def mapping(self, rows, cols):
        """Mapeamento natural: PAs no tile do seu proprio segmento.

        So faz sentido emitir quando nucleos e tiles se equivalem em numero: o
        formato do CAFES nao tem um token para "tile vazio" -- qualquer palavra
        na matriz vira um nucleo. Com sobra de tiles, omitimos a secao e
        deixamos o proprio CAFES fazer o mapeamento, que e justamente uma das
        explorações pedidas.
        """
        # Tres condicoes, todas necessarias: a malha precisa conter o bloco
        # N x M de segmentos sem deformar a vizinhanca, sobrar exatamente os
        # dois tiles de ME e PC, e nao deixar nenhum tile sem nucleo.
        if rows < self.N or cols < self.M:
            return None
        if rows * cols != self.S + 2:
            return None
        grid = [[None] * cols for _ in range(rows)]
        for r in range(self.N):
            for c in range(self.M):
                grid[r][c] = f"PA{self.seg(r, c)}"
        livres = [(r, c) for r in range(rows) for c in range(cols)
                  if grid[r][c] is None]
        if len(livres) != 2:
            return None
        # ME e PC nos tiles livres mais centrais, para encurtar o caminho medio
        cy, cx = (rows - 1) / 2.0, (cols - 1) / 2.0
        livres.sort(key=lambda p: (p[0] - cy) ** 2 + (p[1] - cx) ** 2)
        (r1, c1), (r2, c2) = livres[0], livres[1]
        grid[r1][c1] = "ME"
        grid[r2][c2] = "PC"
        for r in range(rows):
            for c in range(cols):
                if grid[r][c] is None:
                    return None   # sobrou tile: nao da para descrever
        return grid


# ---------------------------------------------------------------------------
# Escritores dos tres formatos
# ---------------------------------------------------------------------------

def write_cwg(app, rows, cols, path):
    """CWM: grafo de comunicacao agregado (sem fases)."""
    agg = {}
    for _, src, dst, ph, _ in app.edges():
        agg[(src, dst)] = agg.get((src, dst), 0) + ph
    xy = app.core_xy()
    with open(path, "w") as f:
        f.write("#_NoC_Size (lines columns)\n %d %d\n\n" % (rows, cols))
        f.write("#_CWG_Graphic (list of: core x y)\n")
        for core in app.cores:
            f.write(" %s %d %d\n" % (core, xy[core][0], xy[core][1]))
        f.write("\n#_CWG_Vertices (list of: vertices)\n")
        for core in app.cores:
            f.write(" %s\n" % core)
        f.write("\n#_CWG_Edges (list of: sourceVertex - targetVertex "
                "numberOfPhitsTransmited)\n")
        for (src, dst), ph in sorted(agg.items()):
            f.write(" %s - %s %d\n" % (src, dst, ph))
        grid = app.mapping(rows, cols)
        if grid:
            f.write("\n#_CWG2NoC_Mapping (matrix of: cores)\n")
            for row in grid:
                f.write(" " + " ".join(row) + "\n")


def write_acpg(app, rows, cols, path):
    """ACPM: o mesmo grafo, com as arestas agrupadas por fase."""
    edges = app.edges()
    tags = sorted({e[0] for e in edges})
    xy_tag_y = {}
    with open(path, "w") as f:
        f.write("#_NoC_Size (lines columns)\n %d %d\n\n" % (rows, cols))
        f.write("#_ACPG_TagsGraphic (list of: Tag x y)\n")
        f.write(" START 50 50\n")
        y = 125
        for t in tags:
            xy_tag_y[t] = y
            f.write(" %d 50 %d\n" % (t, y))
            y += 75
        f.write(" END 50 %d\n" % y)
        f.write("\n#_ACPG_VerticesGraphic (list of: vertices --> Tag "
                "sourceCore - targetCore phits (x y)\n")
        for t in tags:
            yy = xy_tag_y[t]
            for (tag, src, dst, ph, _) in edges:
                if tag != t:
                    continue
                f.write(" %d\t%s - %s\t%d\t: %d\t%d\n" % (t, src, dst, ph, 150, yy))
                yy += 20
        grid = app.mapping(rows, cols)
        if grid:
            f.write("\n#_ACPG2NoC_Mapping (matrix of: cores)\n")
            for row in grid:
                f.write(" " + " ".join(row) + "\n")


def write_cdcg(app, rows, cols, path):
    """CDCM: grafo de dependencias com tempo de computacao por mensagem."""
    edges = app.edges()
    ids = list(range(len(edges)))
    by_phase = {}
    for i, e in enumerate(edges):
        by_phase.setdefault(e[0], []).append(i)
    phases = sorted(by_phase)

    with open(path, "w") as f:
        f.write("#_NoC_Size (lines columns)\n %d %d\n\n" % (rows, cols))
        f.write("#_CDCG_Graphic (list of: IDCore x y)\n")
        f.write(" START 50 50\n END 50 %d\n" % (120 + 30 * len(phases) + 30))
        y = 120
        for p in phases:
            x = 50
            for i in by_phase[p]:
                f.write(" %d %d %d\n" % (i, x, y))
                x += 70
                if x > 700:
                    x = 50
                    y += 25
            y += 30
        f.write("\n#_CDCG_Vertices (list of: vertices --> IDCore sourceCore - "
                "targetCore phits : computation)\n")
        for i, (tag, src, dst, ph, cyc) in enumerate(edges):
            f.write(" %d %s - %s %d : %d\n" % (i, src, dst, ph, cyc))
        f.write("\n#_CDCG_Edges (list of: dependent vertices)\n")
        f.write(" START " + " ".join(str(i) for i in by_phase[phases[0]]) + "\n")
        f.write(" END\n")
        for pi, p in enumerate(phases):
            nxt = by_phase[phases[pi + 1]] if pi + 1 < len(phases) else None
            for i in by_phase[p]:
                if nxt is None:
                    f.write(" %d END\n" % i)
                else:
                    f.write(" %d %s\n" % (i, " ".join(str(j) for j in nxt)))
        grid = app.mapping(rows, cols)
        if grid:
            f.write(" #_CDCG2NoC_Mapping (matrix of: cores)\n")
            for row in grid:
                f.write(" " + " ".join(row) + "\n")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--case", required=True, help="caminho base do caso gerado")
    ap.add_argument("--out", default="cafes", help="diretorio de saida")
    ap.add_argument("--trace", default=None,
                    help="CSV *_tiles.csv de rosmi_par --trace (opcional)")
    ap.add_argument("--phit-bits", type=int, default=DEFAULT_PHIT_BITS)
    ap.add_argument("--freq-mhz", type=float, default=100.0,
                    help="frequencia alvo para converter tempo em ciclos")
    ap.add_argument("--cycles-per-pixel", type=float, default=8.0,
                    help="custo por pixel quando nao ha traco medido")
    ap.add_argument("--noc", default=None, help="forca o tamanho da NoC, ex 8x7")
    ap.add_argument("--writeback", action="store_true",
                    help="inclui a devolucao dos segmentos a memoria")
    args = ap.parse_args()

    meta = json.load(open(args.case + ".json"))
    N, M, K, L = meta["N"], meta["M"], meta["K"], meta["L"]

    trace = load_trace(args.trace)
    app = App(N, M, K, L, args.phit_bits, trace, args.freq_mhz * 1e6,
              args.cycles_per_pixel, args.writeback)

    prefer = None
    if args.noc:
        rr, cc = args.noc.lower().split("x")
        prefer = (int(rr), int(cc))
    rows, cols = noc_size(len(app.cores), prefer)
    if rows * cols < len(app.cores):
        raise SystemExit(f"erro: NoC {rows}x{cols} tem {rows*cols} tiles, "
                         f"insuficiente para {len(app.cores)} nucleos")

    os.makedirs(args.out, exist_ok=True)
    name = os.path.basename(args.case)
    base = os.path.join(args.out, name)
    write_cwg(app, rows, cols, base + ".CWG")
    write_acpg(app, rows, cols, base + ".ACPG")
    write_cdcg(app, rows, cols, base + ".cdcg")

    grid = app.mapping(rows, cols)
    total_ph = sum(e[3] for e in app.edges())
    print(f"{name}: {N}x{M} = {app.S} segmentos de {K}x{L}")
    print(f"  nucleos            : {len(app.cores)} (ME + {app.S} PA + PC)")
    print(f"  NoC                : {rows}x{cols} = {rows*cols} tiles"
          f"  (sobra {rows*cols - len(app.cores)})")
    print(f"  phit               : {args.phit_bits} bits")
    print(f"  ME->PA             : {app.ph_seg} phits (segmento de {K}x{L} bits)")
    print(f"  borda vertical     : {app.ph_vborder} phits ({K} rotulos de 32 bits)")
    print(f"  borda horizontal   : {app.ph_hborder} phits ({L} rotulos de 32 bits)")
    print(f"  mensagens          : {len(app.edges())}")
    print(f"  trafego total      : {total_ph} phits")
    print(f"  computacao por tile: {min(app.cycles.values())}..{max(app.cycles.values())} ciclos"
          f"  ({'medida' if trace else 'estimada'})")
    print(f"  mapeamento natural : {'emitido' if grid else 'omitido (CAFES mapeia)'}")
    print(f"  gerados            : {base}.CWG .ACPG .cdcg")


if __name__ == "__main__":
    main()
