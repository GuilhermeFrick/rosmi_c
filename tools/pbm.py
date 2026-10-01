#!/usr/bin/env python3
"""
Leitura e escrita de PBM binario (P4) com uma convencao explicita.

CONVENCAO DO PROJETO: o bit 1 representa um pixel de OBJETO e o bit 0 um pixel
de FUNDO, exatamente como o enunciado define ("pixels de um valor que
representam o fundo (e.g. valor 0) e com pixels de outro valor que representam
os objetos (e.g. valor 1)"). Isso tambem coincide com a semantica padrao do P4,
onde o bit 1 e preto -- ou seja, num visualizador os objetos aparecem pretos
sobre fundo branco.

Nao usamos PIL para gravar porque, no modo '1' do PIL, o valor 255 (branco)
vira bit 0 no P4; um array booleano com True=objeto sairia gravado invertido.
Aqui o empacotamento e feito explicitamente com np.packbits.

Layout do P4: cada linha ocupa ceil(W/8) bytes, bits MSB-first, e a linha e
preenchida com zeros ate fechar o byte (zeros = fundo, o que e inofensivo).
"""

import numpy as np


def write_pbm(path, arr):
    """Grava um array booleano (H, W) como P4, com bit 1 = objeto."""
    arr = np.asarray(arr, dtype=bool)
    H, W = arr.shape
    packed = np.packbits(arr, axis=1)  # MSB-first, ultimo byte zero-padded
    with open(path, "wb") as f:
        f.write(b"P4\n%d %d\n" % (W, H))
        f.write(packed.tobytes())


def _read_token(f):
    """Le um token do header, pulando espacos e comentarios (# ate fim da linha)."""
    tok = b""
    while True:
        ch = f.read(1)
        if ch == b"":
            raise ValueError("PBM truncado no header")
        if ch == b"#":
            while ch not in (b"\n", b""):
                ch = f.read(1)
            continue
        if ch.isspace():
            if tok:
                return tok
            continue
        tok += ch


def read_pbm(path):
    """Le um P4 e devolve um array booleano (H, W) com True = objeto."""
    with open(path, "rb") as f:
        magic = _read_token(f)
        if magic != b"P4":
            raise ValueError(f"esperado P4, encontrado {magic!r}")
        W = int(_read_token(f))
        H = int(_read_token(f))
        stride = (W + 7) // 8
        data = f.read(stride * H)
        if len(data) != stride * H:
            raise ValueError(f"PBM truncado: {len(data)} de {stride*H} bytes")
    buf = np.frombuffer(data, dtype=np.uint8).reshape(H, stride)
    return np.unpackbits(buf, axis=1)[:, :W].astype(bool)
