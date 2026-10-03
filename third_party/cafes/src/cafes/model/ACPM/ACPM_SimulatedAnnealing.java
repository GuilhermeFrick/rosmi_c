package cafes.model.ACPM;

import cafes.common.*;

class ACPM_SimulatedAnnealing
{
	private int temperature, interaction;
	private ACPM_Grafo g;
	private ACPM_NoC noc;
	private ACPM_VerticeNoC globalMinimumMapping[][][];
	private ACPM_VerticeNoC minimumMapping[][][];
	private ACPM_VerticeNoC lastAcceptedMapping[][][];

	public ACPM_SimulatedAnnealing(ACPM_NoC noc, ACPM_Grafo g, int temperatura, int iteracoes)
	{
		inicio(noc, g, iteracoes, temperatura);
	}
	public ACPM_SimulatedAnnealing(ACPM_NoC noc, ACPM_Grafo g)
	{
		int iteracoes = (int)Math.pow(noc.getNumeroLinhas() + noc.getNumeroColunas(), 2.0);
		if(iteracoes>500)	// Limitador
			iteracoes = 500;
		double log = Math.log(noc.getNumeroLinhas() * noc.getNumeroColunas());
		int temperatura = (int)Math.pow(10, log);
		if(temperatura>1000)	// Limitador
			temperatura = 1000;
		inicio(noc, g, iteracoes, temperatura);
	}
	private void inicio(ACPM_NoC n, ACPM_Grafo gr, int temperatura, int iteracoes)
	{
		this.noc = n;
		this.g = gr;
		this.interaction = iteracoes;
		this.temperature = temperatura;
		globalMinimumMapping = new ACPM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		minimumMapping = new ACPM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		lastAcceptedMapping = new ACPM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
	}
	public int numeroTotalCombinacoes()
	{
		return interaction * temperature;
	}
	public void algoritmo(LinhaColunaAltura[] vetLinhaColuna, String[] vetCore)
	{
		int interact = interaction;
		double globalMinimumMappingCost = Double.MAX_VALUE;
		while(interact>0)
		{
			int temp = temperature;
			double minimumMappingCost = Double.MAX_VALUE;
			Randomico rand = new Randomico();
			randomMappingBigMove(rand, vetLinhaColuna, vetCore);
			while(temp>0)
			{
				double actualMappingCost = noc.computa(g);

				if(minimumMappingCost > actualMappingCost)
				{
					minimumMappingCost = actualMappingCost;
					copiaMapeamentoAtual(minimumMapping);
					copiaMapeamentoAtual(lastAcceptedMapping);
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
			}
			interact--;
		}
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

		ACPM_VerticeNoC nodo = new ACPM_VerticeNoC(noc.matriz[l1][c1][a1]);
		noc.matriz[l1][c1][a1] = new ACPM_VerticeNoC(noc.matriz[l2][c2][a2]);
		noc.matriz[l2][c2][a2] = new ACPM_VerticeNoC(nodo);
	}
	private void copiaMapeamentos(ACPM_VerticeNoC destino[][][], ACPM_VerticeNoC origem[][][])
	{
		for(int linha=0; linha<destino.length; linha++)
		{
			for(int coluna=0; coluna<destino[linha].length; coluna++)
				for(int altura=0; altura<destino[linha][coluna].length; altura++)
				destino[linha][coluna][altura] =  new ACPM_VerticeNoC(origem[linha][coluna][altura]);
		}
	}
	private void copiaMapeamentoAtual(ACPM_VerticeNoC destino[][][])
	{
		for(int linha=0; linha<noc.matriz.length; linha++)
		{
			for(int coluna=0; coluna<noc.matriz[linha].length; coluna++)
				for(int altura=0; altura<noc.matriz[linha][coluna].length; altura++)
				destino[linha][coluna][altura] = new ACPM_VerticeNoC(noc.matriz[linha][coluna][altura]);
		}
	}
	private boolean tresholdAceitacao(int temp, double actualMappingCost, double acceptedMappingCost)
	{
		if(actualMappingCost < (acceptedMappingCost * (1.0 + (double)temp/(double)temperature) * 0.3))
			return true;
		return false;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		System.out.println("\nNoC[vMS] (" + noc.getNumeroLinhas() + "x" + noc.getNumeroColunas() + "x" + noc.getNumeroAltura() + ")");
		for(int linha=0; linha<minimumMapping.length; linha++)
		{
			for(int coluna=0; coluna<minimumMapping[linha].length; coluna++)
				for(int altura=0; altura<minimumMapping[linha][coluna].length; altura++)
			{
				if(minimumMapping[linha][coluna][altura]==null)
					continue;
				if(minimumMapping[linha][coluna][altura].getEnergiaControleRoteador()<=0)
					continue;
				System.out.print("\n\tRoteador(" + linha + ", " + coluna  + ", " + altura + ")");
				minimumMapping[linha][coluna][altura].exibe();
			}
		}
	}
}
