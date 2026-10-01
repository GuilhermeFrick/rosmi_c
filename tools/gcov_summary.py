#!/usr/bin/env python3
"""
Resume a saida do gcov numa tabela por modulo.

O gcov imprime um bloco por arquivo, incluindo os cabecalhos e os fontes de
teste. Aqui ficam apenas os modulos de src/, que sao os que o criterio de
cobertura do projeto cobra, com linhas e ramos lado a lado.

Uso:
  python3 tools/gcov_summary.py <arquivo-de-log-do-gcov>
"""

import re
import sys

FILE_RE = re.compile(r"^File '(?:.*/)?([^/']+)'")
LINES_RE = re.compile(r"^Lines executed:([\d.]+)% of (\d+)")
BRANCH_RE = re.compile(r"^Taken at least once:([\d.]+)% of (\d+)")


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 2

    modules = []
    current = None
    with open(sys.argv[1], errors="replace") as f:
        for line in f:
            m = FILE_RE.match(line)
            if m:
                current = {"name": m.group(1), "lines": None, "nlines": 0,
                           "branches": None, "nbranches": 0}
                continue
            if current is None:
                continue
            m = LINES_RE.match(line)
            if m:
                current["lines"] = float(m.group(1))
                current["nlines"] = int(m.group(2))
                continue
            m = BRANCH_RE.match(line)
            if m:
                current["branches"] = float(m.group(1))
                current["nbranches"] = int(m.group(2))
                modules.append(current)
                current = None
            elif line.startswith("No branches"):
                modules.append(current)
                current = None

    # so os modulos da aplicacao; cabecalhos e arquivos de teste ficam de fora
    modules = [m for m in modules
               if m["name"].endswith(".c") and m["name"].startswith("Rosmi")]
    if not modules:
        print("  (nenhum modulo encontrado no log do gcov)")
        return 1

    print(f"  {'modulo':<22} {'linhas':>16} {'ramos':>16}")
    tot_l = tot_ln = tot_b = tot_bn = 0.0
    for m in sorted(modules, key=lambda x: x["name"]):
        lines = f"{m['lines']:5.1f}% de {m['nlines']:4d}"
        if m["branches"] is None:
            branches = "         --"
        else:
            branches = f"{m['branches']:5.1f}% de {m['nbranches']:4d}"
            tot_b += m["branches"] * m["nbranches"] / 100.0
            tot_bn += m["nbranches"]
        tot_l += m["lines"] * m["nlines"] / 100.0
        tot_ln += m["nlines"]
        print(f"  {m['name']:<22} {lines:>16} {branches:>16}")

    if tot_ln > 0:
        agg_l = f"{100.0 * tot_l / tot_ln:5.1f}% de {int(tot_ln):4d}"
        agg_b = f"{100.0 * tot_b / tot_bn:5.1f}% de {int(tot_bn):4d}" if tot_bn else "--"
        print(f"  {'TOTAL':<22} {agg_l:>16} {agg_b:>16}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
