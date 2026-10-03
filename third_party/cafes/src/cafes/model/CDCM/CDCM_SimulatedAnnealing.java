package cafes.model.CDCM;

import cafes.common.*;
import cafes.NoC.*;

class CDCM_SimulatedAnnealing
{
	private int temperature, interaction;
	private CDCM_Grafo g;
	private CDCM_NoC noc;
	private CDCM_VerticeNoC globalMinimumMapping[][][];
	private CDCM_VerticeNoC minimumMapping[][][];
	private CDCM_VerticeNoC lastAcceptedMapping[][][];

	public CDCM_SimulatedAnnealing(CDCM_NoC noc, CDCM_Grafo g, int temperatura, int iteracoes)
	{
		this.noc = noc;
		this.g = g;
		inicio(iteracoes, temperatura);
	}
	public CDCM_SimulatedAnnealing(CDCM_NoC noc, CDCM_Grafo g)
	{
		int iteracoes = (int)Math.pow(noc.getNumeroLinhas() + noc.getNumeroColunas(), 2.0);
		if(iteracoes > 500)	// Limitador
			iteracoes = 500;
		double log = Math.log(noc.getNumeroLinhas() * noc.getNumeroColunas());
		int temperatura = (int)Math.pow(10, log);
		if(temperatura > 1000)	// Limitador
			temperatura = 1000;
		this.noc = noc;
		this.g = g;
		inicio(iteracoes, temperatura);
	}
	private void inicio(int temperatura, int iteracoes)
	{
		this.interaction = iteracoes;
		this.temperature = temperatura;
		globalMinimumMapping = new CDCM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		minimumMapping = new CDCM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		lastAcceptedMapping = new CDCM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
	}
	public int numeroTotalCombinacoes()
	{
		return interaction * temperature;
	}
	public long algoritmo(LinhaColunaAltura[] vetLinhaColuna, String[] vetCore, boolean linhaComando)
	{
		int interact = interaction;
		double globalMinimumMappingCost = Double.MAX_VALUE;
		long minimumLocalCiclo =  Long.MAX_VALUE;
		long minimumGlobalCiclo =  Long.MAX_VALUE;

		while(interact > 0)
		{
			int temp = temperature;
			double minimumMappingCost = Double.MAX_VALUE;

			Randomico rand = new Randomico();
			randomMappingBigMove(rand, vetLinhaColuna, vetCore);

			while(temp > 0)
			{
				try
				{
					double actualMappingCost = noc.computa(g, linhaComando);
					
					if(minimumMappingCost > actualMappingCost)
					{
						minimumMappingCost = actualMappingCost;
						minimumLocalCiclo = noc.getCiclo();
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
				}
				catch(NoRoutingException nre)
				{
					noc.incRoutingFail();
				}
				randomMappingSmallMove(rand);
				temp--;
			}
			if(globalMinimumMappingCost > minimumMappingCost)
			{
				globalMinimumMappingCost = minimumMappingCost;
				minimumGlobalCiclo = minimumLocalCiclo;
				copiaMapeamentos(globalMinimumMapping, minimumMapping);
			}
			interact--;
		}
		copiaMapeamentos(noc.matrizCustoMinimo, globalMinimumMapping);
		noc.setEnergiaConsumidaMapeamento(globalMinimumMappingCost);
		noc.setCiclo(minimumGlobalCiclo);
		return minimumGlobalCiclo;
	}
	private void randomMappingBigMove(Randomico rand, LinhaColunaAltura[] vetLinhaColuna, String[] vetCore)
	{
		int vetorInts[] = new int[vetLinhaColuna.length];
		
		rand.fillIntVectorRandomly(vetorInts, vetLinhaColuna.length);
		for(int i = 0; i < vetLinhaColuna.length; i++)
			noc.conectaLinhaColunaComCore(vetLinhaColuna[i], vetCore[vetorInts[i]]);
	}
	private void randomMappingSmallMove(Randomico rand)
	{
		int l1, c1,a1, l2, c2,a2;

		LinhaColunaAltura[] vetLinCol = noc.getVetLinhaColuna();
		int randPos = rand.randomNumber(vetLinCol.length);
		LinhaColunaAltura linCol;
		
		linCol = vetLinCol[randPos];
		l1 = linCol.getLinha();
		c1 = linCol.getColuna();
		a1 = linCol.getAltura();

		LinhaColunaAltura[] vetLC = new LinhaColunaAltura[vetLinCol.length - 1];
		System.arraycopy(vetLinCol, 0, vetLC, 0, vetLC.length);
		
		if(randPos < vetLinCol.length - 1)
			vetLC[randPos] = vetLinCol[vetLinCol.length - 1];
		randPos = rand.randomNumber(vetLC.length);

		linCol = vetLC[randPos];
		l2 = linCol.getLinha();
		c2 = linCol.getColuna();
		a2 = linCol.getAltura();
		
		CDCM_VerticeNoC nodo = new CDCM_VerticeNoC(noc.matriz[l1][c1][a1]);
		noc.matriz[l1][c1][a1] = new CDCM_VerticeNoC(noc.matriz[l2][c2][a2]);
		noc.matriz[l2][c2][a2] = new CDCM_VerticeNoC(nodo);
	}
	private void copiaMapeamentos(CDCM_VerticeNoC destino[][][], CDCM_VerticeNoC origem[][][])
	{
		for(int linha = 0; linha < destino.length; linha++)
		{
			for(int coluna=0; coluna<destino[linha].length; coluna++)
				for(int altura=0; altura<destino[linha][coluna].length; altura++)
				destino[linha][coluna][altura] =  new CDCM_VerticeNoC(origem[linha][coluna][altura]);
		}
	}
	private void copiaMapeamentoAtual(CDCM_VerticeNoC destino[][][])
	{
		for(int linha = 0; linha < noc.matriz.length; linha++)
		{
			for(int coluna = 0; coluna < noc.matriz[linha].length; coluna++)
				for(int altura=0; altura<destino[linha][coluna].length; altura++)
				destino[linha][coluna][altura] = new CDCM_VerticeNoC(noc.matriz[linha][coluna][altura]);
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
		System.out.println("\nNoC[vMS] (" + noc.getNumeroLinhas() + "x" + noc.getNumeroColunas()+ "x" + noc.getNumeroAltura() + ")");
		for(int linha=0; linha<minimumMapping.length; linha++)
		{
			for(int coluna=0; coluna<minimumMapping[linha].length; coluna++)
			{
				for(int altura=0; altura<minimumMapping[linha][coluna].length; altura++)
				{
				if(minimumMapping[linha][coluna][altura]==null)
					return;
				if(minimumMapping[linha][coluna][altura].getEnergiaControleRoteador()<=0)
					continue;
				System.out.print("\n\tRoteador(" + linha + ", " + coluna + ", " + altura + ")");
				minimumMapping[linha][coluna][altura].exibe();
			}
			}
		}
	}
}
