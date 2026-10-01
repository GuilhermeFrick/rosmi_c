#!/usr/bin/env python3
"""
Oraculo de referencia para o ROSMI.

Conta os componentes conexos da imagem inteira com scipy.ndimage.label (uma
implementacao independente e amplamente testada) e compara com o ground truth
declarado pelo gerador. Serve para (a) provar que a imagem gerada tem de fato o
numero de objetos que dizemos ter e (b) conferir a saida dos binarios C.

Tambem reporta quantos objetos cruzam fronteiras de segmento e a soma das
contagens puramente locais -- esse segundo numero e o "erro" que um algoritmo
ingenuo (que so somasse os objetos de cada segmento) cometeria.
"""

import argparse
import json
import sys

import numpy as np
from scipy import ndimage

from pbm import read_pbm

C4 = np.array([[0, 1, 0], [1, 1, 1], [0, 1, 0]], dtype=bool)
C8 = np.ones((3, 3), dtype=bool)


def analyse(pbm, N, M, K, L):
    arr = read_pbm(pbm)
    H, W = arr.shape
    assert H == N * K and W == M * L, f"imagem {H}x{W} != {N*K}x{M*L}"

    out = {}
    for name, st in (("c4", C4), ("c8", C8)):
        lab, n = ndimage.label(arr, structure=st)
        out[name] = {"total": int(n)}

        # quantos segmentos cada label toca
        seg_of = np.zeros_like(lab)
        for r in range(N):
            for c in range(M):
                seg_of[r * K:(r + 1) * K, c * L:(c + 1) * L] = r * M + c + 1

        crossing = 0
        local_sum = 0
        for r in range(N):
            for c in range(M):
                sub = lab[r * K:(r + 1) * K, c * L:(c + 1) * L]
                local_sum += len(np.unique(sub[sub > 0]))
        # um label que aparece em s segmentos foi contado s vezes na soma local
        ids, counts = np.unique(lab[lab > 0], return_counts=True)
        touched = {}
        for r in range(N):
            for c in range(M):
                sub = lab[r * K:(r + 1) * K, c * L:(c + 1) * L]
                for i in np.unique(sub[sub > 0]):
                    touched[int(i)] = touched.get(int(i), 0) + 1
        crossing = sum(1 for v in touched.values() if v > 1)

        out[name]["crossing"] = crossing
        out[name]["naive_local_sum"] = local_sum
        out[name]["max_segments_touched"] = max(touched.values()) if touched else 0
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("base", help="caminho base do caso (sem extensao)")
    ap.add_argument("--expect", type=int, default=None,
                    help="contagem produzida pelo programa C, para conferir")
    args = ap.parse_args()

    with open(args.base + ".json") as f:
        meta = json.load(f)

    res = analyse(args.base + ".pbm", meta["N"], meta["M"], meta["K"], meta["L"])
    gt = meta["ground_truth"]

    print(f"caso: {args.base}")
    print(f"  {meta['N']}x{meta['M']} segmentos de {meta['K']}x{meta['L']} "
          f"= {meta['height']}x{meta['width']} px")
    print(f"  ground truth declarado pelo gerador : {gt}")
    for name in ("c4", "c8"):
        r = res[name]
        print(f"  scipy.label ({name}) total={r['total']:5d}  "
              f"distribuidos={r['crossing']:3d}  "
              f"soma ingenua por segmento={r['naive_local_sum']:5d}  "
              f"max segmentos tocados={r['max_segments_touched']}")

    ok = True
    if res["c4"]["total"] != gt or res["c8"]["total"] != gt:
        print("  FALHA: scipy discorda do ground truth do gerador")
        ok = False
    if args.expect is not None:
        if args.expect != gt:
            print(f"  FALHA: programa C respondeu {args.expect}, esperado {gt}")
            ok = False
        else:
            print(f"  OK: programa C respondeu {args.expect}")
    if ok:
        print("  OK")
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
