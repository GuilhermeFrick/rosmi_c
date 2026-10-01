#!/usr/bin/env python3
"""
Modelo de referencia do algoritmo paralelo do ROSMI, em Python.

Este arquivo NAO faz parte da aplicacao -- ele existe para validar o DESENHO do
algoritmo de forma independente do codigo C. A rotulacao local e delegada ao
scipy (ja confiavel), de modo que o que esta sendo testado aqui e exatamente a
parte escrita a mao: a uniao dos rotulos atraves das fronteiras entre
segmentos, que e onde mora o requisito de contar um objeto distribuido uma
unica vez.

O modo --stress compara este modelo com scipy.ndimage.label sobre a imagem
inteira, usando ruido aleatorio. Ruido e um teste severo de proposito: produz
centenas de componentes tocando as fronteiras e, em particular, os casos
diagonais nos cantos onde quatro segmentos se encontram, que sao o ponto cego
natural de uma implementacao de merge.
"""

import argparse
import sys

import numpy as np
from scipy import ndimage

BG = -1
C4 = np.array([[0, 1, 0], [1, 1, 1], [0, 1, 0]], dtype=bool)
C8 = np.ones((3, 3), dtype=bool)


class UF:
    """Union-find com uniao por tamanho e compressao de caminho."""

    def __init__(self, n):
        self.p = list(range(n))
        self.sz = [1] * n

    def find(self, x):
        while self.p[x] != x:
            self.p[x] = self.p[self.p[x]]
            x = self.p[x]
        return x

    def union(self, a, b):
        a, b = self.find(a), self.find(b)
        if a == b:
            return
        if self.sz[a] < self.sz[b]:
            a, b = b, a
        self.p[b] = a
        self.sz[a] += self.sz[b]


def local_ccl(sub, conn):
    """Rotula um segmento isoladamente.

    Devolve (n, top, bot, left, right) com rotulos compactos em [0, n) e BG nas
    posicoes de fundo -- a mesma interface de ccl_segment() no lado C.
    """
    lab, n = ndimage.label(sub, structure=(C4 if conn == 4 else C8))
    lab = lab.astype(np.int64) - 1          # 1..n vira 0..n-1; fundo vira -1
    return n, lab[0, :].copy(), lab[-1, :].copy(), lab[:, 0].copy(), lab[:, -1].copy()


def count_objects(arr, N, M, conn):
    """Aplica as tres fases do algoritmo paralelo e devolve a contagem global."""
    H, W = arr.shape
    K, L = H // N, W // M

    # ---- Fase 1: rotulacao local, independente por segmento ---------------
    nloc, top, bot, left, right = {}, {}, {}, {}, {}
    for r in range(N):
        for c in range(M):
            sub = arr[r * K:(r + 1) * K, c * L:(c + 1) * L]
            s = r * M + c
            nloc[s], top[s], bot[s], left[s], right[s] = local_ccl(sub, conn)

    # ---- Fase 2: prefix sum para o espaco global de rotulos ---------------
    offset = [0] * (N * M + 1)
    for s in range(N * M):
        offset[s + 1] = offset[s] + nloc[s]
    uf = UF(offset[-1])

    def uni(sa, la, sb, lb):
        if la != BG and lb != BG:
            uf.union(offset[sa] + int(la), offset[sb] + int(lb))

    # vizinhos horizontais: borda direita de (r,c) com borda esquerda de (r,c+1)
    for r in range(N):
        for c in range(M - 1):
            a, b = r * M + c, r * M + c + 1
            ra, lb = right[a], left[b]
            for y in range(K):
                uni(a, ra[y], b, lb[y])
                if conn == 8:
                    if y > 0:
                        uni(a, ra[y], b, lb[y - 1])
                    if y + 1 < K:
                        uni(a, ra[y], b, lb[y + 1])

    # vizinhos verticais: borda inferior de (r,c) com borda superior de (r+1,c)
    for r in range(N - 1):
        for c in range(M):
            a, b = r * M + c, (r + 1) * M + c
            ba, tb = bot[a], top[b]
            for x in range(L):
                uni(a, ba[x], b, tb[x])
                if conn == 8:
                    if x > 0:
                        uni(a, ba[x], b, tb[x - 1])
                    if x + 1 < L:
                        uni(a, ba[x], b, tb[x + 1])

    # conectividade 8: pares diagonais nos cantos onde 4 segmentos se encontram,
    # que atravessam duas fronteiras de uma vez e escapam das varreduras acima
    if conn == 8:
        for r in range(N - 1):
            for c in range(M - 1):
                a, b = r * M + c, r * M + c + 1
                d, e = (r + 1) * M + c, (r + 1) * M + c + 1
                uni(a, bot[a][L - 1], e, top[e][0])
                uni(b, bot[b][0], d, top[d][L - 1])

    # ---- Fase 3: numero de raizes distintas -------------------------------
    return sum(1 for i in range(offset[-1]) if uf.find(i) == i)


def stress(trials, seed):
    rng = np.random.default_rng(seed)
    bad = 0
    for t in range(trials):
        N = int(rng.integers(1, 5))
        M = int(rng.integers(1, 5))
        K = int(rng.integers(1, 12))
        L = int(rng.integers(1, 12))
        dens = float(rng.uniform(0.1, 0.6))
        arr = rng.random((N * K, M * L)) < dens
        for conn in (4, 8):
            ref = int(ndimage.label(arr, structure=(C4 if conn == 4 else C8))[1])
            got = count_objects(arr, N, M, conn)
            if got != ref:
                bad += 1
                print(f"  DIVERGENCIA teste {t}: N={N} M={M} K={K} L={L} "
                      f"dens={dens:.2f} conn={conn} -> modelo={got} scipy={ref}")
                if bad == 1:
                    print(arr.astype(int))
    print(f"{trials} imagens aleatorias x 2 conectividades: "
          f"{'OK, nenhuma divergencia' if bad == 0 else f'{bad} DIVERGENCIAS'}")
    return 0 if bad == 0 else 1


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--stress", type=int, default=0,
                    help="numero de imagens aleatorias a testar")
    ap.add_argument("--seed", type=int, default=0)
    ap.add_argument("--case", default=None,
                    help="caminho base de um caso gerado, para conferir")
    args = ap.parse_args()

    rc = 0
    if args.case:
        import json
        sys.path.insert(0, __file__.rsplit("/", 1)[0] if "/" in __file__ else ".")
        from pbm import read_pbm
        meta = json.load(open(args.case + ".json"))
        arr = read_pbm(args.case + ".pbm")
        for conn in (4, 8):
            got = count_objects(arr, meta["N"], meta["M"], conn)
            ok = got == meta["ground_truth"]
            print(f"  {args.case} conn={conn}: modelo={got} "
                  f"ground_truth={meta['ground_truth']} {'ok' if ok else 'FALHA'}")
            if not ok:
                rc = 1
    if args.stress:
        rc |= stress(args.stress, args.seed)
    return rc


if __name__ == "__main__":
    sys.exit(main())
