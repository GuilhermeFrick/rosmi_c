package cafes.model.CWM;

/*
 * Evaluate all possible swaps
 */

import cafes.common.*;

class CWM_HeuristicTwoSearch {
	private CWM_Grafo g;
	private CWM_NoC noc;
	private CWM_VerticeNoC globalMinimumMapping[][][];
	private CWM_VerticeNoC minimumMapping[][][];
	private boolean comEstimativaDeTempo, comSemente;
	private CWM_UGraph ugraph;

	public CWM_HeuristicTwoSearch(CWM_NoC _noc, CWM_Grafo _g, boolean _comEstimativaDeTempo, boolean _comSemente) {
		this.noc = _noc;
		this.g = _g;
		this.comEstimativaDeTempo = _comEstimativaDeTempo;
		this.comSemente = _comSemente;
		globalMinimumMapping = new CWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		minimumMapping = new CWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		ugraph = new CWM_UGraph(this.g, this.noc);
	}

	public int numeroTotalCombinacoes() {
		int npositions = noc.getNumeroLinhas() * noc.getNumeroColunas() * noc.getNumeroAltura();
		return npositions * (npositions - 1);
	}

	public void algoritmo(LinhaColunaAltura[] vetLinhaColuna, String[] vetCore) {
		Randomico rand = new Randomico();
		LinhaColunaAltura fixaLC, searchLC, swapLC = null;
		double minimumMappingCost, currentCost;
		boolean found;

		if (!comSemente)
			randomMappingBigMove(rand, vetLinhaColuna, vetCore);

		ugraph.linkVertex(noc.matriz);

		for (int fixaLinha = 0; fixaLinha < noc.getNumeroLinhas(); fixaLinha++)
			for (int fixaColuna = 0; fixaColuna < noc.getNumeroColunas(); fixaColuna++)
				for (int fixaAltura = 0; fixaAltura < noc.getNumeroAltura(); fixaAltura++) {
					fixaLC = new LinhaColunaAltura(fixaLinha, fixaColuna, fixaAltura);
					found = false;
					minimumMappingCost = 0;
					for (int searchLinha = 0; searchLinha < noc.getNumeroLinhas(); searchLinha++)
						for (int searchColuna = 0; searchColuna < noc.getNumeroColunas(); searchColuna++) 
							for (int searchAltura = 0; searchAltura < noc.getNumeroAltura(); searchAltura++) {
							searchLC = new LinhaColunaAltura(searchLinha, searchColuna,searchAltura);
							if ((searchLC != fixaLC) && ((currentCost = ugraph.saveEnergyIfSwap(fixaLC, searchLC)) < 0)) {
								if (currentCost < minimumMappingCost) {
									minimumMappingCost = currentCost;
									found = true;
									swapLC = new LinhaColunaAltura(searchLC);
								}
							}
						}

					if (found) {
						ugraph.swap(swapLC, fixaLC);
					}

					noc.exibeAndamento();
				}

		ugraph.saveMapping(globalMinimumMapping);

		copiaMapeamentos(noc.matriz, globalMinimumMapping);
		double energia = noc.computa(g, comEstimativaDeTempo);
		noc.setEnergiaConsumidaMapeamento(energia);
		noc.salvaPosicionamento();
	}

	private void randomMappingBigMove(Randomico rand, LinhaColunaAltura[] vetLinhaColuna, String[] vetCore) {
		int vetorInts[] = new int[vetCore.length];
		rand.fillIntVectorRandomly(vetorInts, vetorInts.length);
		for (int i = 0; i < vetLinhaColuna.length; i++)
			noc.conectaLinhaColunaComCore(vetLinhaColuna[i], vetCore[vetorInts[i]]);
	}

	private void copiaMapeamentos(CWM_VerticeNoC destino[][][], CWM_VerticeNoC origem[][][]) {
		for (int linha = 0; linha < destino.length; linha++) {
			for (int coluna = 0; coluna < destino[linha].length; coluna++)
				for (int altura = 0; altura < destino[linha][coluna].length; altura++)
				destino[linha][coluna][altura] = new CWM_VerticeNoC(origem[linha][coluna][altura]);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DEPURA��O
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe() {
		System.out.println("\nNoC[vMS] (" + noc.getNumeroLinhas() + "x" + noc.getNumeroColunas() + "x" + noc.getNumeroAltura() + ")");
		for (int linha = 0; linha < minimumMapping.length; linha++) {
			for (int coluna = 0; coluna < minimumMapping[linha].length; coluna++) 
				for (int altura = 0; altura < minimumMapping[linha][coluna].length; altura++) {
				if (minimumMapping[linha][coluna][altura] == null)
					return;
				System.out.println("\tRoteador(" + linha + ", " + coluna+ ", " + altura  + ")");
				minimumMapping[linha][coluna][altura].exibe();
			}
		}
	}
}
