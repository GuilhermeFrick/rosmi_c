#!/usr/bin/env python3
"""
Verificador dos arquivos de entrada do CAFES.

O parser do CAFES e acoplado a interface grafica, entao nao da para carregar um
arquivo sem abrir a ferramenta. Este script aplica, fora dela, as mesmas regras
que o parser dele impoe, para que um arquivo malformado apareca aqui e nao no
meio da apresentacao.

O que e conferido, nos tres formatos:

  - as secoes obrigatorias existem e estao escritas exatamente como o parser
    espera (ele compara a linha inteira com a palavra-chave);
  - a malha declarada tem tiles suficientes para os nucleos;
  - toda aresta liga nucleos que foram declarados na lista de vertices;
  - a matriz de mapeamento, quando existe, tem exatamente as dimensoes da
    malha, sem nucleo repetido nem tile vazio -- o formato nao tem simbolo para
    tile vazio, entao qualquer palavra na matriz vira um nucleo.

Uso:
  python3 tools/check_cafes.py cafes/grande_7x7
"""

import sys


def read_sections(path, prefix):
    """Divide o arquivo em secoes, indexadas pela palavra-chave que as abre."""
    sections = {}
    current = None
    with open(path, encoding="utf-8", errors="replace") as handle:
        for raw in handle:
            # O cabecalho de secao pode vir indentado: o parser do CAFES compara
            # tokens separados por espaco, nao linhas, entao recuo e irrelevante
            # para ele. Os exemplos que acompanham a ferramenta usam os dois
            # estilos no mesmo arquivo.
            line = raw.strip()
            if line.startswith("#_"):
                current = line.split(" ")[0]
                sections[current] = []
            elif current is not None and line:
                sections[current].append(line)
    return sections


def fail(problems, message):
    problems.append(message)


def check_noc(sections, key, problems):
    """Le a malha e devolve (linhas, colunas)."""
    if key not in sections:
        fail(problems, f"falta a secao {key}")
        return (0, 0)
    try:
        rows, cols = (int(v) for v in sections[key][0].split())
        return (rows, cols)
    except (IndexError, ValueError):
        fail(problems, f"{key}: nao consegui ler 'linhas colunas'")
        return (0, 0)


def check_mapping(sections, key, rows, cols, cores, problems):
    """Confere a matriz de mapeamento, se o arquivo trouxer uma."""
    if key not in sections:
        return
    grid = [line.split() for line in sections[key]]
    if len(grid) != rows:
        fail(problems, f"{key}: {len(grid)} linhas, malha tem {rows}")
    for i, row in enumerate(grid):
        if len(row) != cols:
            fail(problems, f"{key}: linha {i} tem {len(row)} tiles, malha tem {cols}")
    placed = [name for row in grid for name in row]
    if len(placed) != len(set(placed)):
        fail(problems, f"{key}: ha nucleo repetido na matriz")
    unknown = sorted(set(placed) - cores)
    if unknown:
        fail(problems, f"{key}: nomes que nao sao nucleos declarados: {unknown}")
    missing = sorted(cores - set(placed))
    if missing:
        fail(problems, f"{key}: nucleos declarados e nao mapeados: {missing}")


def check_cwg(path, problems):
    sections = read_sections(path, "CWG")
    rows, cols = check_noc(sections, "#_NoC_Size", problems)

    if "#_CWG_Vertices" not in sections:
        fail(problems, "falta a secao #_CWG_Vertices")
        return 0
    cores = {line.split()[0] for line in sections["#_CWG_Vertices"]}

    if (rows * cols) < len(cores):
        fail(problems, f"malha {rows}x{cols} = {rows*cols} tiles, "
                       f"insuficiente para {len(cores)} nucleos")

    edges = 0
    for line in sections.get("#_CWG_Edges", []):
        parts = line.split()
        if len(parts) < 4 or parts[1] != "-":
            fail(problems, f"#_CWG_Edges: linha mal formada: {line!r}")
            continue
        for endpoint in (parts[0], parts[2]):
            if endpoint not in cores:
                fail(problems, f"#_CWG_Edges: '{endpoint}' nao esta em #_CWG_Vertices")
        if not parts[3].isdigit():
            fail(problems, f"#_CWG_Edges: phits nao numerico em {line!r}")
        edges += 1

    for line in sections.get("#_CWG_Graphic", []):
        name = line.split()[0]
        if name not in cores:
            fail(problems, f"#_CWG_Graphic: '{name}' nao esta em #_CWG_Vertices")

    check_mapping(sections, "#_CWG2NoC_Mapping", rows, cols, cores, problems)
    return edges


def check_acpg(path, problems):
    sections = read_sections(path, "ACPG")
    rows, cols = check_noc(sections, "#_NoC_Size", problems)

    tags = set()
    for line in sections.get("#_ACPG_TagsGraphic", []):
        tags.add(line.split()[0])
    if "START" not in tags or "END" not in tags:
        fail(problems, "#_ACPG_TagsGraphic: faltam as marcas START e END")

    cores = set()
    edges = 0
    for line in sections.get("#_ACPG_VerticesGraphic", []):
        parts = line.replace("\t", " ").split()
        # formato: <tag> <origem> - <destino> <phits> : <x> <y>
        if len(parts) < 5 or parts[2] != "-":
            fail(problems, f"#_ACPG_VerticesGraphic: linha mal formada: {line!r}")
            continue
        if parts[0] not in tags:
            fail(problems, f"#_ACPG_VerticesGraphic: tag '{parts[0]}' nao declarada")
        cores.update((parts[1], parts[3]))
        edges += 1

    if (rows * cols) < len(cores):
        fail(problems, f"malha {rows}x{cols} = {rows*cols} tiles, "
                       f"insuficiente para {len(cores)} nucleos")
    check_mapping(sections, "#_ACPG2NoC_Mapping", rows, cols, cores, problems)
    return edges


def check_cdcg(path, problems):
    sections = read_sections(path, "CDCG")
    rows, cols = check_noc(sections, "#_NoC_Size", problems)

    cores = set()
    ids = set()
    for line in sections.get("#_CDCG_Vertices", []):
        parts = line.replace(":", " : ").split()
        # formato: <id> <origem> - <destino> <phits> : <computacao>
        if len(parts) < 7 or parts[2] != "-":
            fail(problems, f"#_CDCG_Vertices: linha mal formada: {line!r}")
            continue
        if parts[0] in ids:
            fail(problems, f"#_CDCG_Vertices: id repetido: {parts[0]}")
        ids.add(parts[0])
        cores.update((parts[1], parts[3]))

    seen_start = False
    seen_end = False
    for line in sections.get("#_CDCG_Edges", []):
        parts = line.replace("#_CDCG2NoC_Mapping", "").split()
        if not parts:
            continue
        head = parts[0]
        if head == "START":
            seen_start = True
        elif head == "END":
            seen_end = True
        elif head not in ids:
            fail(problems, f"#_CDCG_Edges: origem '{head}' nao e um vertice declarado")
        for target in parts[1:]:
            if target not in ids and target != "END":
                fail(problems, f"#_CDCG_Edges: destino '{target}' nao e um vertice")

    if not seen_start or not seen_end:
        fail(problems, "#_CDCG_Edges: faltam as linhas de START e/ou END")
    if (rows * cols) < len(cores):
        fail(problems, f"malha {rows}x{cols} = {rows*cols} tiles, "
                       f"insuficiente para {len(cores)} nucleos")
    return len(ids)


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 2

    status = 0
    for base in sys.argv[1:]:
        print(f"{base}")
        for suffix, checker, label in ((".CWG", check_cwg, "CWM"),
                                       (".ACPG", check_acpg, "ACPM"),
                                       (".cdcg", check_cdcg, "CDCM")):
            path = base + suffix
            problems = []
            try:
                count = checker(path, problems)
            except FileNotFoundError:
                print(f"  {label:5s} {suffix:6s} ausente")
                status = 1
                continue
            if problems:
                print(f"  {label:5s} {suffix:6s} {len(problems)} PROBLEMA(S)")
                for item in problems:
                    print(f"            - {item}")
                status = 1
            else:
                print(f"  {label:5s} {suffix:6s} ok ({count} registros)")
    return status


if __name__ == "__main__":
    sys.exit(main())
