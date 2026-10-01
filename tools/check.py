#!/usr/bin/env python3
"""
Arnes de verificacao de ponta a ponta do ROSMI (versao C).

Roda os dois binarios sobre um caso gerado e confere a contagem contra o
ground truth. O ponto central e a INVARIANCIA: o numero de objetos nao pode
depender de quantas threads foram usadas nem de como a imagem foi segmentada.
Se depender, o merge de bordas esta errado.

Isto complementa os testes unitarios: eles exercitam cada modulo em isolamento,
este roda a aplicacao inteira sobre as imagens reais.

Uso:
  python3 tools/check.py <base-do-caso> [dir-dos-binarios]
"""

import json
import os
import re
import subprocess
import sys

OBJ_RE = re.compile(r"OBJETOS\s*:\s*(\d+)")


def run(binary, args):
    proc = subprocess.run([binary] + args, capture_output=True, text=True)
    if proc.returncode != 0:
        raise RuntimeError(f"{os.path.basename(binary)} falhou "
                           f"(rc={proc.returncode}):\n{proc.stderr.strip()}")
    found = OBJ_RE.search(proc.stdout)
    if not found:
        raise RuntimeError(f"nao achei a contagem na saida de "
                           f"{os.path.basename(binary)}:\n{proc.stdout}")
    return int(found.group(1))


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 2
    base = sys.argv[1]
    bindir = sys.argv[2] if len(sys.argv) > 2 else "bin"

    with open(base + ".json") as handle:
        meta = json.load(handle)
    pbm = base + ".pbm"
    seg_rows, seg_cols = meta["N"], meta["M"]
    truth = meta["ground_truth"]

    suffix = ".exe" if os.name == "nt" else ""
    seq = os.path.join(bindir, "rosmi_seq" + suffix)
    par = os.path.join(bindir, "rosmi_par" + suffix)

    common = [pbm, "--N", str(seg_rows), "--M", str(seg_cols)]
    failures = []
    print(f"caso {base}: {seg_rows}x{seg_cols} segmentos, ground truth = {truth}")

    for conn in (4, 8):
        got = run(seq, common + ["--conn", str(conn)])
        tag = f"seq  conn={conn}"
        print(f"  {tag:22s} -> {got:5d}  {'ok' if got == truth else 'FALHA'}")
        if got != truth:
            failures.append(f"{tag}: {got} != {truth}")

        for threads in (1, 2, 4, 8):
            got = run(par, common + ["--conn", str(conn), "--threads", str(threads)])
            tag = f"par  conn={conn} t={threads}"
            print(f"  {tag:22s} -> {got:5d}  {'ok' if got == truth else 'FALHA'}")
            if got != truth:
                failures.append(f"{tag}: {got} != {truth}")

    if failures:
        print("\nFALHOU:")
        for item in failures:
            print("  -", item)
        return 1
    print("  todas as combinacoes concordam com o ground truth")
    return 0


if __name__ == "__main__":
    sys.exit(main())
