package cafes.common;

/*
 * Autor:
 * 		C�sar Marcon
 * Revisor:
 * 		Edson Moreno
 * Objetivo:
 * 		Classe utilizada para relacionar o enderecamento da NoC. Linha
 * 			equivale a Y e coluna equivale a X.
 */

public class LinhaColunaAltura implements java.io.Serializable
{
	private static final long serialVersionUID = 5651148427675510490L;
	private int linha, coluna,altura;

	/*
	 * Objetivo:
	 * 		Construtor da classe LinhaColuna, atribuindo uma linha e uma
	 * 			coluna j� na construcao da classe.
	 * Parametros:
	 * 		linha-> linha na noc
	 * 		Coluna-> coluna na noc
	 */
	public LinhaColunaAltura(int linha, int coluna, int altura)
	{
		this.linha = linha;
		this.coluna = coluna;
		this.altura = altura;
	}
	/*
	 * Objetivo:
	 * 		Construtor da classe. Copia uma classe j� existente.
	 * Parametros
	 * 		lc-> classe LinhaColuna a ser copiada.
	 */
	public LinhaColunaAltura(LinhaColunaAltura lc)
	{
		this.linha = lc.linha;
		this.coluna = lc.coluna;
		this.altura = lc.altura;
	}

	/*
	 * Objetivo:
	 * 		Retornar a linha a qual a classe est� associada.
	 * Parametros:
	 * 		Nao ha...
	 */
	public int getLinha()
	{
		return linha;
	}

	/*
	 * Objetivo:
	 * 		Retorna a coluna a qual a classe esta associada.
	 * Parametros:
	 * 		Nao ha...
	 */
	public int getColuna()
	{
		return coluna;
	}
	public int getAltura()
	{
		return altura;
	}
	/*
	 * Objetivos:
	 * 		Definir/associar uma linha a classe.
	 * Parametros:
	 * 		linha-> linha a qual a classe deve ser associada.
	 */
	public void setLinha(int linha)
	{
		this.linha = linha;
	}
	public void setAltura(int altura)
	{
		this.altura = altura;
	}

	/*
	 * Objetivos:
	 * 		Definir/associar uma coluna a classe.
	 * Parametros:
	 * 		coluna-> coluna a qual a classe deve ser associada.
	 */
	public void setColuna(int coluna)
	{
		this.coluna = coluna;
	}

	public boolean equals(int linha, int coluna)
	{
		if(linha==this.linha && coluna==this.coluna)
			return true;
		return false;
	}

	public boolean equals(LinhaColunaAltura _lc)
	{
		if(_lc.getLinha()==this.linha && _lc.getColuna()==this.coluna&& _lc.getAltura()==this.altura)
			return true;
		return false;
	}

	// Fun��o utilizada para criterio de comparacao da funcao contains da classe Vector
	public boolean equals(Object o)
	{
		if(o instanceof LinhaColunaAltura)
			return ((((LinhaColunaAltura) o).getLinha()==linha) && (((LinhaColunaAltura) o).getColuna()==coluna)&& (((LinhaColunaAltura) o).getAltura()==altura));
		return false;
	}
}