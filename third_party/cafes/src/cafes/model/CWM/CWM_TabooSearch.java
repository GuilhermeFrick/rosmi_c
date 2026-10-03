package cafes.model.CWM;

import java.util.*;

import cafes.common.*;

class CWM_TabooSearch {
	private Vector<pairLC> tabooList = new Vector<pairLC>(100, 5);
	private Vector<pairLC> possibleMoves;
	private int temperature, interaction;
	private CWM_Grafo g;
	private CWM_NoC noc;
	private CWM_VerticeNoC globalMinimumMapping[][][];
	private CWM_VerticeNoC minimumMapping[][][];
	private boolean comEstimativaDeTempo;
	private boolean comSemente;
	private CWM_UGraph ugraph;

	public class pairLC {
		public LinhaColunaAltura lc1;
		public LinhaColunaAltura lc2;

		public pairLC(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2) {
			lc1 = _lc1;
			lc2 = _lc2;
		}

		public boolean equals(pairLC _plc) {
			if ((_plc.lc1.equals(lc1) && _plc.lc2.equals(lc2)) || (_plc.lc1.equals(lc2) && _plc.lc2.equals(lc1)))
				return true;
			return false;
		}

		public boolean equals(Object _o) {
			if (_o instanceof pairLC) {
				pairLC _plc = (pairLC) _o;
				if ((_plc.lc1.equals(lc1) && _plc.lc2.equals(lc2)) || (_plc.lc1.equals(lc2) && _plc.lc2.equals(lc1)))
					return true;
			}
			return false;
		}
	}

	public void insertTabooList(pairLC _plc) {
		tabooList.add(_plc);
	}

	public CWM_TabooSearch(CWM_NoC _noc, CWM_Grafo _g, int _temperatura, int _iteracoes, boolean _comEstimativaDeTempo, boolean _comSemente) {
		int iteracoes;

		iteracoes = (_iteracoes < 0) ? (int) Math.pow(_noc.getNumeroLinhas() + _noc.getNumeroColunas(), 2.0) : _iteracoes;
		iteracoes = (iteracoes > 100) ? 100 : iteracoes;

		double log = Math.log(_noc.getNumeroLinhas() * _noc.getNumeroColunas());
		int temperatura = (_temperatura < 0) ? (int) Math.pow(10, log) : _temperatura;
		temperatura = (temperatura > 1000) ? 1000 : temperatura;

		inicio(_noc, _g, iteracoes, temperatura, _comEstimativaDeTempo, _comSemente);
	}

	public CWM_TabooSearch(CWM_NoC _noc, boolean _comSemente) {
		int iteracoes = (int) Math.pow(_noc.getNumeroLinhas() + _noc.getNumeroColunas(), 2.0);
		iteracoes = (iteracoes > 100) ? 100 : iteracoes;

		double log = Math.log(_noc.getNumeroLinhas() * _noc.getNumeroColunas());
		int temperatura = (int) Math.pow(10, log);
		temperatura = (temperatura > 1000) ? 1000 : temperatura;

		inicio(noc, g, iteracoes, temperatura, comEstimativaDeTempo, _comSemente);
	}

	private void fillPossibleMoves() {
		int tam = (noc.getNumeroColunas() * noc.getNumeroLinhas());
		possibleMoves = new Vector<pairLC>((tam * (tam + 1)) / 2, 5);
		Vector<LinhaColunaAltura> positions = new Vector<LinhaColunaAltura>(tam, 5);

		for (int linha = 0; linha < noc.getNumeroLinhas(); linha++) {
			for (int coluna = 0; coluna < noc.getNumeroColunas(); coluna++)
				for (int altura = 0; altura < noc.getNumeroAltura(); altura++)
					positions.add(new LinhaColunaAltura(linha, coluna, altura));
		}
		for (int index = 0; index < (positions.size() - 1); index++) {
			for (int pair = index + 1; pair < (positions.size() - 1); pair++)
				possibleMoves.add(new pairLC(positions.get(index), positions.get(pair)));
		}
	}

	private void inicio(CWM_NoC n, CWM_Grafo gr, int iteracoes, int temperatura, boolean comEstTempo, boolean _comSemente) {
		this.noc = n;
		this.g = gr;
		this.interaction = iteracoes;
		this.temperature = temperatura;
		this.comEstimativaDeTempo = comEstTempo;
		this.comSemente = _comSemente;
		globalMinimumMapping = new CWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		minimumMapping = new CWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		ugraph = new CWM_UGraph(gr, n);
		noc.setiteracoes(iteracoes);
		noc.setTemperatura(temperatura);
		fillPossibleMoves();
	}

	public int numeroTotalCombinacoes() {
		return interaction * temperature;
	}

	// return n!
	// precondition: n >= 0 and n <= 20
	public static long factorial(long n) {
		if (n < 0)
			throw new RuntimeException("Underflow error in factorial");
		else if (n > 20)
			throw new RuntimeException("Overflow error in factorial");
		else if (n == 0)
			return 1;
		else
			return n * factorial(n - 1);
	}

	public void algoritmo(LinhaColunaAltura[] vetLinhaColuna, String[] vetCore) {
		int interact = interaction;
		double globalMinimumMappingCost = Double.MAX_VALUE;
		long globalMinimumCicle = Long.MAX_VALUE, minimumCicle = Long.MAX_VALUE;
		long maxTabooListSize = ((noc.getNumeroColunas() * noc.getNumeroLinhas()) <= 6) ? factorial((long) noc.getNumeroColunas()
				* noc.getNumeroLinhas()) : factorial(6);
		Randomico rand = new Randomico();

		if (!comSemente)
			randomMappingBigMove(rand, vetLinhaColuna, vetCore);
		ugraph.linkVertex(noc.matriz);
		ugraph.saveMapping(globalMinimumMapping);
		int cantFindBetter = 0; // Contador q diz taz vezes jah houve iteracoes
								// sem melhor alguma
		int saveIndex, index;
		double actualMappingCost;

		while ((interact > 0) && (tabooList.size() < maxTabooListSize) && (possibleMoves.size() > 0)) {
			int vizinhanca = temperature;
			double minimumMappingCost = 0;
			pairLC plc;

			saveIndex = -1;

			while (vizinhanca > 0) {
				index = randomMappingSmallMove(rand);
				plc = possibleMoves.get(index);
				actualMappingCost = ugraph.saveEnergyIfSwap(plc.lc1, plc.lc2);
				if (minimumMappingCost > actualMappingCost) {
					saveIndex = index;
					minimumMappingCost = actualMappingCost;
				}
				vizinhanca--;
				noc.exibeAndamento();
			}
			if (saveIndex != -1) {
				actualMappingCost = ugraph.exibeCustos();
				if (globalMinimumMappingCost > actualMappingCost) {
					globalMinimumMappingCost = actualMappingCost;
					plc = possibleMoves.get(saveIndex);
					ugraph.swap(plc.lc1, plc.lc2);
					ugraph.saveMapping(globalMinimumMapping);
					if (comEstimativaDeTempo)
						globalMinimumCicle = minimumCicle;
				}
				cantFindBetter = 0;
				tabooList.add(possibleMoves.get(saveIndex));
				possibleMoves.removeElementAt(saveIndex);
			} else
				cantFindBetter++;
			interact--;
		}
		if (comEstimativaDeTempo)
			noc.salvaCiclosOperacao(globalMinimumCicle);
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

	private int randomMappingSmallMove(Randomico rand) {
		int index;

		if (possibleMoves.size() == 1)
			index = 0;
		else
			index = rand.randomNumber(possibleMoves.size() - 1);
		return index;
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
					System.out.println("\tRoteador(" + linha + ", " + coluna + ", " + altura + ")");
					minimumMapping[linha][coluna][altura].exibe();
				}
		}
	}
}
