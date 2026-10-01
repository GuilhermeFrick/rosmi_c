#!/usr/bin/env python3
"""
Gerador de imagens binarias sinteticas para o ROSMI.

Produz uma imagem de (N*K) x (M*L) pixels contendo objetos (poligonos cheios)
cujo numero e conhecido por construcao. Parte dos objetos e "local" (cabe dentro
de um unico segmento) e parte e "distribuida" (cruza a fronteira entre segmentos
vizinhos), que e exatamente o caso que o algoritmo precisa contar uma unica vez.

Garantia de ground truth: nenhum objeto encosta em outro. Isso e assegurado por
rejeicao de candidatos cuja bounding box, dilatada por MARGIN pixels, colida com
a de algum objeto ja colocado. Logo, numero de componentes conexos == numero de
objetos desenhados, tanto em conectividade-4 quanto em conectividade-8.

Saida:
  <out>.pbm   imagem binaria P4 (1 = objeto, 0 = fundo)
  <out>.json  metadados + ground truth

Convencao de coordenadas: a imagem e indexada por (linha, coluna) com a origem
no canto superior esquerdo. O segmento (r, c) ocupa as linhas [r*K, (r+1)*K) e
as colunas [c*L, (c+1)*L). A notacao (m, n) do enunciado -- com origem no canto
inferior esquerdo -- corresponde a c = m e r = N-1-n.
"""

import argparse
import json
import math
import os
import random

import numpy as np
from PIL import Image, ImageDraw
from scipy import ndimage

from pbm import write_pbm

# Estrutura de conectividade-4 (cruz). Usada tanto para validar cada objeto
# quanto para "engrossar" pontas finas que ficariam ligadas so na diagonal.
_CROSS = np.array([[0, 1, 0], [1, 1, 1], [0, 1, 0]], dtype=bool)
_BOX = np.ones((3, 3), dtype=bool)

# Folga minima, em pixels, entre dois objetos quaisquer. Precisa ser >= 2 para
# que nem conectividade-4 nem conectividade-8 jamais una dois objetos distintos.
MARGIN = 6


# ---------------------------------------------------------------------------
# Formas: cada funcao devolve uma lista de vertices (x, y) em torno de (cx, cy)
# ---------------------------------------------------------------------------

def _poly(cx, cy, radii, n_pts, phase=0.0):
    """Poligono com raio variando por vertice (permite estrelas)."""
    pts = []
    for i in range(n_pts):
        ang = phase + 2.0 * math.pi * i / n_pts
        r = radii[i % len(radii)]
        pts.append((cx + r * math.cos(ang), cy + r * math.sin(ang)))
    return pts


def star4(cx, cy, r, phase):
    return _poly(cx, cy, [r, r * 0.32], 8, phase)


def star5(cx, cy, r, phase):
    return _poly(cx, cy, [r, r * 0.45], 10, phase)


def star6(cx, cy, r, phase):
    return _poly(cx, cy, [r, r * 0.52], 12, phase)


def triangle(cx, cy, r, phase):
    return _poly(cx, cy, [r], 3, phase)


def hexagon(cx, cy, r, phase):
    return _poly(cx, cy, [r], 6, phase)


def rectangle(cx, cy, r, phase):
    w, h = r * 1.5, r * 0.6
    return [(cx - w, cy - h), (cx + w, cy - h), (cx + w, cy + h), (cx - w, cy + h)]


def square(cx, cy, r, phase):
    return [(cx - r, cy - r), (cx + r, cy - r), (cx + r, cy + r), (cx - r, cy + r)]


def arrow(cx, cy, r, phase):
    w = r * 0.35
    return [
        (cx - w, cy - r), (cx + w, cy - r), (cx + w, cy + r * 0.1),
        (cx + r * 0.7, cy + r * 0.1), (cx, cy + r), (cx - r * 0.7, cy + r * 0.1),
        (cx - w, cy + r * 0.1),
    ]


SHAPES = [star4, star5, star6, triangle, hexagon, rectangle, square, arrow]


# ---------------------------------------------------------------------------
# Colocacao com rejeicao por bounding box
# ---------------------------------------------------------------------------

def _bbox(pts):
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    return (min(xs), min(ys), max(xs), max(ys))


def _collides(box, placed):
    x0, y0, x1, y1 = box
    x0 -= MARGIN; y0 -= MARGIN; x1 += MARGIN; y1 += MARGIN
    for (px0, py0, px1, py1) in placed:
        if x0 <= px1 and px0 <= x1 and y0 <= py1 and py0 <= y1:
            return True
    return False


def _inside_image(box, W, H):
    x0, y0, x1, y1 = box
    return x0 >= MARGIN and y0 >= MARGIN and x1 < W - MARGIN and y1 < H - MARGIN


def _tight_box(patch, px, py):
    """Bounding box do conteudo do patch, ja em coordenadas da imagem."""
    ys, xs = np.nonzero(patch)
    return (px + int(xs.min()), py + int(ys.min()),
            px + int(xs.max()), py + int(ys.max()))


def _stamp(canvas, patch, px, py):
    h, w = patch.shape
    canvas[py:py + h, px:px + w] |= patch


def _render_shape(pts):
    """Rasteriza um poligono num patch proprio e garante que ele seja UM unico
    componente conexo tanto em conectividade-4 quanto em conectividade-8.

    Poligonos com pontas finas (estrelas) saem da rasterizacao com trechos
    ligados apenas na diagonal: sao um componente em c8, mas varios em c4. Nesse
    caso o patch e dilatado com a estrutura em cruz, o que engrossa a ponta o
    suficiente para uni-la em c4 sem mudar a forma de maneira perceptivel.

    Devolve (patch booleano, x0, y0) onde (x0, y0) e o canto do patch na imagem,
    ou None se a forma nao pode ser tornada conexa.
    """
    x0, y0, x1, y1 = _bbox(pts)
    pad = 4
    ix0, iy0 = int(math.floor(x0)) - pad, int(math.floor(y0)) - pad
    w = int(math.ceil(x1)) - ix0 + pad + 1
    h = int(math.ceil(y1)) - iy0 + pad + 1

    im = Image.new("1", (w, h), 0)
    ImageDraw.Draw(im).polygon([(px - ix0, py - iy0) for (px, py) in pts], fill=1)
    patch = np.array(im, dtype=bool)

    for _ in range(4):
        if patch.sum() == 0:
            return None
        if ndimage.label(patch, structure=_CROSS)[1] == 1:
            # conexo em c4 implica conexo em c8
            return patch, ix0, iy0
        patch = ndimage.binary_dilation(patch, structure=_CROSS)
    return None


def generate(N, M, K, L, n_local, n_crossing, seed):
    """Desenha a imagem e devolve (array bool HxW, ground_truth, stats)."""
    rng = random.Random(seed)
    H, W = N * K, M * L

    canvas = np.zeros((H, W), dtype=bool)
    placed = []
    n_drawn_local = 0
    n_drawn_cross = 0

    # -- objetos distribuidos: centrados sobre uma fronteira entre segmentos ---
    # Candidatos de fronteira: cruzamentos internos (canto de 4 segmentos),
    # fronteiras verticais internas e fronteiras horizontais internas.
    corners = [(r * K, c * L) for r in range(1, N) for c in range(1, M)]
    vedges = [(rng.randrange(K // 4, 3 * K // 4) + r * K, c * L)
              for r in range(N) for c in range(1, M)]
    hedges = [(r * K, rng.randrange(L // 4, 3 * L // 4) + c * L)
              for r in range(1, N) for c in range(M)]

    # Prioriza os cruzamentos de 4 segmentos (caso mais dificil do enunciado).
    sites = corners + vedges + hedges
    rng.shuffle(corners)
    rng.shuffle(vedges)
    rng.shuffle(hedges)
    sites = corners + vedges + hedges

    # Raio grande o bastante para de fato atravessar a fronteira com folga.
    cross_rmin = max(40, min(K, L) // 12)
    cross_rmax = max(cross_rmin + 20, min(K, L) // 6)

    si = 0
    attempts = 0
    while n_drawn_cross < n_crossing and attempts < 20000:
        attempts += 1
        if not sites:
            break
        cy, cx = sites[si % len(sites)]
        si += 1
        # jitter pequeno para nao ficar exatamente centrado sempre
        cy += rng.randint(-8, 8)
        cx += rng.randint(-8, 8)
        r = rng.randint(cross_rmin, cross_rmax)
        shape = rng.choice(SHAPES)
        pts = shape(cx, cy, r, rng.uniform(0, math.pi))
        rendered = _render_shape(pts)
        if rendered is None:
            continue
        patch, px, py = rendered
        box = _tight_box(patch, px, py)
        if not _inside_image(box, W, H) or _collides(box, placed):
            continue
        _stamp(canvas, patch, px, py)
        placed.append(box)
        n_drawn_cross += 1

    # -- objetos locais: inteiramente dentro de um segmento -------------------
    loc_rmin = max(12, min(K, L) // 40)
    loc_rmax = max(loc_rmin + 8, min(K, L) // 14)

    attempts = 0
    while n_drawn_local < n_local and attempts < 200000:
        attempts += 1
        r = rng.randint(loc_rmin, loc_rmax)
        pad = r + MARGIN + 2
        if 2 * pad >= K or 2 * pad >= L:
            break
        sr = rng.randrange(N)
        sc = rng.randrange(M)
        cy = sr * K + rng.randrange(pad, K - pad)
        cx = sc * L + rng.randrange(pad, L - pad)
        shape = rng.choice(SHAPES)
        pts = shape(cx, cy, r, rng.uniform(0, math.pi))
        rendered = _render_shape(pts)
        if rendered is None:
            continue
        patch, px, py = rendered
        box = _tight_box(patch, px, py)
        # exige que fique inteiramente dentro do segmento escolhido
        if not (sc * L <= box[0] and box[2] < (sc + 1) * L and
                sr * K <= box[1] and box[3] < (sr + 1) * K):
            continue
        if not _inside_image(box, W, H) or _collides(box, placed):
            continue
        _stamp(canvas, patch, px, py)
        placed.append(box)
        n_drawn_local += 1

    gt = n_drawn_local + n_drawn_cross
    stats = {"local": n_drawn_local, "crossing": n_drawn_cross}
    return canvas, gt, stats


def main():
    ap = argparse.ArgumentParser(description="Gerador de imagens binarias para o ROSMI")
    ap.add_argument("--N", type=int, required=True, help="segmentos na vertical")
    ap.add_argument("--M", type=int, required=True, help="segmentos na horizontal")
    ap.add_argument("--K", type=int, default=768, help="altura do segmento em pixels")
    ap.add_argument("--L", type=int, default=1024, help="largura do segmento em pixels")
    ap.add_argument("--local", type=int, required=True, help="objetos locais")
    ap.add_argument("--crossing", type=int, required=True, help="objetos distribuidos")
    ap.add_argument("--seed", type=int, default=1)
    ap.add_argument("--out", required=True, help="caminho base de saida (sem extensao)")
    args = ap.parse_args()

    canvas, gt, stats = generate(args.N, args.M, args.K, args.L,
                                 args.local, args.crossing, args.seed)

    os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
    pbm = args.out + ".pbm"
    write_pbm(pbm, canvas)

    meta = {
        "N": args.N, "M": args.M, "K": args.K, "L": args.L,
        "width": args.M * args.L, "height": args.N * args.K,
        "segments": args.N * args.M,
        "ground_truth": gt,
        "objects_local": stats["local"],
        "objects_crossing": stats["crossing"],
        "seed": args.seed,
        "margin_px": MARGIN,
        "pbm": os.path.basename(pbm),
    }
    with open(args.out + ".json", "w") as f:
        json.dump(meta, f, indent=2)

    if stats["local"] < args.local or stats["crossing"] < args.crossing:
        print(f"AVISO: pedidos {args.local} locais + {args.crossing} cruzando, "
              f"colocados {stats['local']} + {stats['crossing']}")
    print(f"{pbm}: {args.N}x{args.M} segmentos de {args.K}x{args.L}, "
          f"ground truth = {gt} objetos "
          f"({stats['local']} locais + {stats['crossing']} distribuidos)")


if __name__ == "__main__":
    main()
