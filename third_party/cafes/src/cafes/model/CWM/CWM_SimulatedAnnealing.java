package cafes.model.CWM;

import cafes.common.*;

class CWM_SimulatedAnnealing
{
	private int temperature, interaction;
	private CWM_Grafo g;
	private CWM_NoC noc;
	private CWM_VerticeNoC globalMinimumMapping[][][];
	private CWM_VerticeNoC minimumMapping[][][];
	private CWM_VerticeNoC lastAcceptedMapping[][][];
	private boolean comEstimativaDeTempo, comSemente;
	private CWM_UGraph ugraph;

	public CWM_SimulatedAnnealing(CWM_NoC _noc, CWM_Grafo _g, int _temperatura, int _iteracoes, boolean _comEstimativaDeTempo, boolean _comSemente)
	{
		int iteracoes = (_iteracoes<0)?(int)Math.pow(_noc.getNumeroLinhas() + _noc.getNumeroColunas(), 2.0):_iteracoes;

		if(!_comSemente)
			iteracoes = (iteracoes>100)?100:iteracoes;

		double log = Math.log(_noc.getNumeroLinhas() * _noc.getNumeroColunas());
		int temperatura = (_temperatura<0)?(int)Math.pow(10, log):_temperatura;
		if(!_comSemente)
			temperatura = (temperatura>100000)?100000:temperatura;
		inicio(_noc, _g, temperatura, iteracoes, _comEstimativaDeTempo, _comSemente);
	}
	public CWM_SimulatedAnnealing(CWM_NoC _noc, CWM_Grafo _g, boolean _comEstimativaDeTempo, boolean _comSemente)
	{
		int iteracoes = (int)Math.pow(_noc.getNumeroLinhas() + _noc.getNumeroColunas(), 2.0);
		iteracoes = (iteracoes>100)?100:iteracoes;

		double log = Math.log(_noc.getNumeroLinhas() * _noc.getNumeroColunas());
		int temperatura = (int)Math.pow(10, log);
		temperatura = (temperatura>100000) ? 100000 : temperatura;
		inicio(_noc, _g, temperatura, iteracoes, _comEstimativaDeTempo, _comSemente);
	}
	
	private void inicio(CWM_NoC n, CWM_Grafo gr, int temperatura, int iteracoes, boolean comEstTempo, boolean _comSemente)
	{
		this.noc = n;
		this.g = gr;
		this.interaction = iteracoes;
		this.temperature = temperatura;
		this.comEstimativaDeTempo = comEstTempo;
		this.comSemente = _comSemente;
		globalMinimumMapping = new CWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		minimumMapping = new CWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		lastAcceptedMapping = new CWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		noc.setiteracoes(iteracoes);
		noc.setTemperatura(temperatura);
		ugraph = new CWM_UGraph(gr, n);
	}
	public int numeroTotalCombinacoes()
	{
		return interaction * temperature;
	}
	public void algoritmo(LinhaColunaAltura[] vetLinhaColuna, String[] vetCore)
	{
		int interact = interaction;
//		boolean melhorou;
		double globalMinimumMappingCost = Double.MAX_VALUE, minimumMappingCost=Double.MAX_VALUE, calc;
		LinhaColunaAltura lc1 = new LinhaColunaAltura(-1,-1,-1);
		LinhaColunaAltura lc2 = new LinhaColunaAltura(-1,-1,-1);
		
		while(interact>0)
		{
			int temp = temperature;
			Randomico rand = new Randomico();
	
			if(!comSemente)
				randomMappingBigMove(rand, vetLinhaColuna, vetCore);
			ugraph.linkVertex(noc.matriz);	
			
			double actualMappingCost = ugraph.exibeCustos();
			
			if(comSemente)
			{
				minimumMappingCost=actualMappingCost;
				ugraph.saveMapping(minimumMapping);
			}				
			double value = 0;
//			melhorou = false;
			while(temp>0)
			{
				randomMappingSmallMove(rand, lc1, lc2);
				value = ugraph.saveEnergyIfSwap(lc1, lc2);
				if(value<0)
				{
					ugraph.swap(lc1, lc2);
					actualMappingCost += value;
					if(minimumMappingCost>actualMappingCost)
					{
//						melhorou = true;
						minimumMappingCost = actualMappingCost;
						ugraph.saveMapping(minimumMapping);
					}
				}
				else
				{
					calc = (actualMappingCost+value)-minimumMappingCost;
					//if(tresholdAceitacao(temp,actualMappingCost+value,minimumMappingCost))
					//if(actualMappingCost < (minimumMappingCost * (1.0 + (((double)temp/(double)temperature)*0.3)) ))
					if((actualMappingCost+value) < (minimumMappingCost * (1.0 + Math.exp(-calc/temperature)*0.3 )))
					{
						ugraph.swap(lc1, lc2);
						actualMappingCost += value;
					}
				}
				temp--;
				noc.exibeAndamento();
			}
			if(globalMinimumMappingCost > minimumMappingCost)
			{
				globalMinimumMappingCost = minimumMappingCost;
				copiaMapeamentos(globalMinimumMapping, minimumMapping);
			}
			interact--;
		}
		
		copiaMapeamentos(noc.matriz, globalMinimumMapping);
		double energia = noc.computa(g, comEstimativaDeTempo);
		noc.setEnergiaConsumidaMapeamento(energia);
		noc.salvaPosicionamento();
	}
	public void algoritmoWithSeed()
	{
		int interact = interaction;
		double globalMinimumMappingCost = Double.MAX_VALUE;
		long globalMinimumCicle=Long.MAX_VALUE, minimumCicle=Long.MAX_VALUE;
		
		while(interact>0)
		{
			int temp = temperature;
			double minimumMappingCost = Double.MAX_VALUE;
			Randomico rand = new Randomico();
			//randomMappingBigMove(rand, vetLinhaColuna, vetCore);
			while(temp>0)
			{
				double actualMappingCost = noc.computa(g, comEstimativaDeTempo);
				
				if(minimumMappingCost > actualMappingCost)
				{
					minimumMappingCost = actualMappingCost;
					copiaMapeamentoAtual(minimumMapping);
					copiaMapeamentoAtual(lastAcceptedMapping);
					if(comEstimativaDeTempo)
						minimumCicle = noc.getCiclosOperacaoTemporario();
				}
				else
				{
					if(tresholdAceitacao(temp, actualMappingCost, minimumMappingCost))
						copiaMapeamentoAtual(lastAcceptedMapping);
					else
						copiaMapeamentos(noc.matriz, lastAcceptedMapping);
				}
				randomMappingSmallMove(rand);
				temp--;
			}
			if(globalMinimumMappingCost > minimumMappingCost)
			{
				globalMinimumMappingCost = minimumMappingCost;
				copiaMapeamentos(globalMinimumMapping, minimumMapping);
				if(comEstimativaDeTempo)
					globalMinimumCicle = minimumCicle;
			}
			interact--;
		}
		if(comEstimativaDeTempo)
			noc.salvaCiclosOperacao(globalMinimumCicle);
		copiaMapeamentos(noc.matSalva, globalMinimumMapping);
		noc.setEnergiaConsumidaMapeamento(globalMinimumMappingCost);
	}
	private void randomMappingBigMove(Randomico rand, LinhaColunaAltura[] vetLinhaColuna, String[] vetCore)
	{
		int vetorInts[] = new int[vetCore.length];
		rand.fillIntVectorRandomly(vetorInts, vetorInts.length);
		for(int i=0; i<vetLinhaColuna.length; i++)
			noc.conectaLinhaColunaComCore(vetLinhaColuna[i], vetCore[vetorInts[i]]);
	}
	private void randomMappingSmallMove(Randomico rand)
	{
		int l1 = rand.randomNumber(noc.matriz.length);
		int c1 = rand.randomNumber(noc.matriz[0].length);
		int a1 = rand.randomNumber(noc.matriz[0][0].length);
		int l2 = rand.randomNumber(noc.matriz.length);
		int c2 = rand.randomNumber(noc.matriz[0].length);
		int a2 = rand.randomNumber(noc.matriz[0][0].length);

		CWM_VerticeNoC nodo = new CWM_VerticeNoC(noc.matriz[l1][c1][a1]);
		noc.matriz[l1][c1][a1] = new CWM_VerticeNoC(noc.matriz[l2][c2][a2]);
		noc.matriz[l2][c2][a2] = new CWM_VerticeNoC(nodo);
	}
	private void randomMappingSmallMove(Randomico rand, LinhaColunaAltura lc1, LinhaColunaAltura lc2)
	{
		LinhaColunaAltura[] vetLinCol = noc.getVetLinhaColuna();
		int randPos = rand.randomNumber(vetLinCol.length);
		LinhaColunaAltura linCol;
		
		linCol = vetLinCol[randPos];
		lc1.setLinha(linCol.getLinha());
		lc1.setColuna(linCol.getColuna());
		lc1.setAltura(linCol.getAltura());

		LinhaColunaAltura[] vetLC = new LinhaColunaAltura[vetLinCol.length - 1];
		System.arraycopy(vetLinCol, 0, vetLC, 0, vetLC.length);
		
		if(randPos < vetLinCol.length - 1)
			vetLC[randPos] = vetLinCol[vetLinCol.length - 1];
		randPos = rand.randomNumber(vetLC.length);

		linCol = vetLC[randPos];
		lc2.setLinha(linCol.getLinha());
		lc2.setColuna(linCol.getColuna());
		lc2.setAltura(linCol.getAltura());
	}
	private void copiaMapeamentos(CWM_VerticeNoC destino[][][], CWM_VerticeNoC origem[][][])
	{
		for(int linha=0; linha<destino.length; linha++)
		{
			for(int coluna=0; coluna<destino[linha].length; coluna++)
				for(int altura=0; altura<destino[linha][coluna].length; altura++)
				destino[linha][coluna][altura] =  new CWM_VerticeNoC(origem[linha][coluna][altura]);
		}
	}
	private void copiaMapeamentoAtual(CWM_VerticeNoC destino[][][])
	{
		for(int linha=0; linha<noc.matriz.length; linha++)
		{
			for(int coluna=0; coluna<noc.matriz[linha].length; coluna++)
				for(int altura=0; altura<noc.matriz[linha][coluna].length; altura++)
				destino[linha][coluna][altura] = new CWM_VerticeNoC(noc.matriz[linha][coluna][altura]);
		}
	}
	private boolean tresholdAceitacao(int temp, double actualMappingCost, double acceptedMappingCost)
	{
		if(actualMappingCost < (acceptedMappingCost * (1.0 + ( (double)temp/(double)temperature) * 0.3)))
			return true;
		return false;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURA��O
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		System.out.println("\nNoC[vMS] (" + noc.getNumeroLinhas() + "x" + noc.getNumeroColunas()  + "x" + noc.getNumeroAltura()+ ")");
		for(int linha=0; linha<minimumMapping.length; linha++)
		{
			for(int coluna=0; coluna<minimumMapping[linha].length; coluna++)
				for(int altura=0; altura<minimumMapping[linha][coluna].length; altura++)
			{
				if(minimumMapping[linha][coluna]==null)
					return;
				System.out.println("\tRoteador(" + linha + ", " + coluna + ", " + altura+ ")");
				minimumMapping[linha][coluna][altura].exibe();
			}
		}
	}
}
