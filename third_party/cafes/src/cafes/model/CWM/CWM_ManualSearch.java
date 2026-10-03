package cafes.model.CWM;

/*
 * Autor: 
 * 		Edson Moreno
 * 		C�sar Marcon
 * Objetivo: 
 * 		Criar um algoritmo heur�stico que possa ser utilizado para definir
 * 			o mapeamento de PE sobre a NoC com menor numero de iteracoes e
 * 			da forma mais proxima da otima possivel. 
 */

import javax.swing.JOptionPane;

import cafes.common.*;

class CWM_ManualSearch {
	private CWM_Grafo g;
	private CWM_NoC noc;
	private CWM_VerticeNoC minimumMapping[][][];
	private boolean comEstimativaDeTempo;

	/*
	 * Objetivos: Construtor da classe CWM_HeuristicSearch Parametros: _noc ->
	 * Inst�ncia CWM_NoC para o mapeamento dos vertices _g -> Grafo dos vertices
	 * e arcos do modelo carregado _comEstimativaDeTempo -> Se deve realizar
	 * estimativa de tempo (true) ou nao (false)
	 */
	public CWM_ManualSearch(CWM_NoC _noc, CWM_Grafo _g, boolean _comEstimativaDeTempo) {
		this.noc = _noc;
		this.g = _g;
		this.comEstimativaDeTempo = _comEstimativaDeTempo;
	}

	public void algoritmo(LinhaColunaAltura[] vetLinhaColuna, String[] vetCore) {
		int x, y, z;
		CWM_Vertice localVertice = this.g.getInicio();

		for (int linha = 0; linha < this.noc.getNumeroLinhas(); linha++)
			for (int coluna = 0; coluna < this.noc.getNumeroColunas(); coluna++)
				for (int altura = 0; altura < this.noc.getNumeroAltura(); altura++)
					this.noc.conectaLinhaColunaComCore(new LinhaColunaAltura(linha, coluna, altura), "");

		while (localVertice != null) {
			x = Integer.parseInt(JOptionPane.showInputDialog("Linha de associacao do core:" + localVertice.getInf()));
			y = Integer.parseInt(JOptionPane.showInputDialog("Coluna de associacao do core:" + localVertice.getInf()));
			z = Integer.parseInt(JOptionPane.showInputDialog("Altura de associacao do core:" + localVertice.getInf()));
			this.noc.conectaLinhaColunaComCore(new LinhaColunaAltura(x, y, z), localVertice.getInf());
			localVertice = localVertice.getProx();
		}

		this.noc.salvaPosicionamento();
		double energia = this.noc.computa(this.g, this.comEstimativaDeTempo);
		this.noc.setEnergiaConsumidaMapeamento(energia);

		// copiaMapeamentos(globalMinimumMapping, this.noc.matriz);
		// noc.setEnergiaConsumidaMapeamento(globalMinimumMappingCost);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DEPURA��O
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe() {
		System.out.println("\nNoC[vMS] (" + noc.getNumeroLinhas() + "x" + noc.getNumeroColunas() + "x"
				+ noc.getNumeroAltura() + ")");
		for (int linha = 0; linha < minimumMapping.length; linha++) {
			for (int coluna = 0; coluna < minimumMapping[linha].length; coluna++)
				for (int altura = 0; altura < minimumMapping[linha][coluna].length; altura++) {
					if (minimumMapping[linha][coluna][altura] == null)
						return;
					System.out.println("\tRoteador(" + linha + ", " + coluna + ", " + altura + ")");
					minimumMapping[linha][coluna][altura].exibe();
				}
		}
	}
}
