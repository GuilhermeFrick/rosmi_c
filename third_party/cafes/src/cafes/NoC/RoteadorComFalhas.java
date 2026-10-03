package cafes.NoC;

public class RoteadorComFalhas
{
	private int linha, coluna,altura;
	private boolean norte, sul, leste, oeste, local, roteador, tile, espera, superior, inferior;
	private RoteadorComFalhas prox;

	public RoteadorComFalhas(int linha, int coluna, int altura)
	{
		this.linha = linha;
		this.coluna = coluna;
		this.altura = altura;
	}	
	public int getLinha() { return linha; }
	public int getColuna() { return coluna; }
	public int getAltura() { return altura; }
	public void setFalhaNorte() { norte = true; }
	public void setFalhaSuperior() { superior = true; }
	public void setFalhaInferior() { inferior = true; }
	public void setFalhaSul() { sul = true; }
	public void setFalhaLeste() { leste = true; }
	public void setFalhaOeste() { oeste = true; }
	public void setFalhaLocal() { local = true; }
	public void setFalhaRoteador() { roteador = true; }
	public void setFalhaTile() { tile = true; }
	public void setTileDeEspera() { espera = true; }
	public boolean linkNorteComFalha() { return norte; }
	public boolean linkSuperiorComFalha() { return superior; }
	public boolean linkInferiorComFalha() { return inferior; }
	public boolean linkSulComFalha() { return sul; }
	public boolean linkLesteComFalha() { return leste; }
	public boolean linkOesteComFalha() { return oeste; }
	public boolean linkLocalComFalha() { return local; }
	public boolean roteadorComFalha() { return roteador; }
	public boolean tileComFalha() { return tile; }
	public boolean tileDeEspera() { return espera; }
	public void setProx(RoteadorComFalhas prox)	{ this.prox = prox; }
	public RoteadorComFalhas getProx() { return prox; }
}

