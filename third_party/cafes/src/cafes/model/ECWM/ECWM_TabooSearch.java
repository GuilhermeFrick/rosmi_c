package cafes.model.ECWM;

import java.util.*;

import cafes.common.*;

class ECWM_TabooSearch
{
	private Vector<LinhaColunaAltura> tabooList = new Vector<LinhaColunaAltura>(100,5);
	private int temperature, interaction;
	private ECWM_Grafo g;
	private ECWM_NoC noc;
	private ECWM_VerticeNoC globalMinimumMapping[][][];
	private ECWM_VerticeNoC minimumMapping[][][];
	private boolean comEstimativaDeTempo;

	public void insertTabooList(LinhaColunaAltura lc1, LinhaColunaAltura lc2)
	{
		tabooList.add(new LinhaColunaAltura(lc1));
		tabooList.add(new LinhaColunaAltura(lc2));
	}
	public boolean isInTabooList(LinhaColunaAltura lc1, LinhaColunaAltura lc2)
	{
		for(int i=0; i<tabooList.size(); i+=2)
		{
			LinhaColunaAltura lAux1 = tabooList.elementAt(i);
			LinhaColunaAltura lAux2 = tabooList.elementAt(i+1);
	
			if((lAux1.equals(lc1) && lAux2.equals(lc2)) || (lAux1.equals(lc2) && lAux2.equals(lc1)))
				return true;
		}
		return false;
	}
	public ECWM_TabooSearch(ECWM_NoC noc, ECWM_Grafo g, int temperatura, int iteracoes, boolean comEstimativaDeTempo)
	{
		inicio(noc, g, iteracoes, temperatura, comEstimativaDeTempo);
	}
	public ECWM_TabooSearch(ECWM_NoC noc, ECWM_Grafo g, boolean comEstimativaDeTempo)
	{
		int iteracoes = (int)Math.pow(noc.getNumeroLinhas() + noc.getNumeroColunas(), 2.0);
		if(iteracoes>100)	// Limitador
			iteracoes = 100;
		double log = Math.log(noc.getNumeroLinhas() * noc.getNumeroColunas());
		int temperatura = (int)Math.pow(10, log);
		if(temperatura>1000)	// Limitador
			temperatura = 1000;
		inicio(noc, g, iteracoes, temperatura, comEstimativaDeTempo);
	}
	private void inicio(ECWM_NoC n, ECWM_Grafo gr, int temperatura, int iteracoes, boolean comEstTempo)
	{
		this.noc = n;
		this.g = gr;
		this.interaction = iteracoes;
		this.temperature = temperatura;
		this.comEstimativaDeTempo = comEstTempo;
		globalMinimumMapping = new ECWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
		minimumMapping = new ECWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
	}
	public int numeroTotalCombinacoes()
	{
		return interaction * temperature;
	}
	public void algoritmo(LinhaColunaAltura[] vetLinhaColuna, String[] vetCore)
	{
		int interact = interaction;
		double globalMinimumMappingCost = Double.MAX_VALUE;
		long globalMinimumCicle=Long.MAX_VALUE, minimumCicle=Long.MAX_VALUE;
		LinhaColunaAltura p1 = new LinhaColunaAltura(0, 0,0);
		LinhaColunaAltura p2 = new LinhaColunaAltura(0, 0,0);
		LinhaColunaAltura p1Salvo = new LinhaColunaAltura(0, 0,0);
		LinhaColunaAltura p2Salvo = new LinhaColunaAltura(0, 0,0);
		
		Randomico rand = new Randomico();
		randomMappingBigMove(rand, vetLinhaColuna, vetCore);
		
		while(interact>0)
		{
			ECWM_VerticeNoC vetorMapa[][][] = new ECWM_VerticeNoC[noc.getNumeroLinhas()][noc.getNumeroColunas()][noc.getNumeroAltura()];
			int vizinhanca = temperature;
			double minimumMappingCost = Double.MAX_VALUE;
			copiaMapeamentos(vetorMapa, noc.matriz);

			while(vizinhanca>0)
			{
				copiaMapeamentos(noc.matriz, vetorMapa);
				int avoid=0;
				do
				{
					randomMappingSmallMove(rand, p1, p2);
				} while(isInTabooList(p1,p2) && ++avoid < temperature);
				
				swapCores(p1,p2);
				double actualMappingCost = noc.computa(g, comEstimativaDeTempo);				
				
				if(minimumMappingCost > actualMappingCost)
				{
					p1Salvo = p1;
					p2Salvo = p2;
					minimumMappingCost = actualMappingCost;
					copiaMapeamentoAtual(minimumMapping);
					if(comEstimativaDeTempo)
						minimumCicle = noc.getCiclosOperacaoTemporario();
				}
				vizinhanca--;
			}
			insertTabooList(p1Salvo,p2Salvo);
			copiaMapeamentos(noc.matriz, minimumMapping);
			
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
	private void randomMappingSmallMove(Randomico rand, LinhaColunaAltura p1, LinhaColunaAltura p2)	
	{
		int l1, c1, l2, c2,a1,a2;
		
		do{
			l1 = rand.randomNumber(noc.matriz.length);
			c1 = rand.randomNumber(noc.matriz[0].length);
			a1 = rand.randomNumber(noc.matriz[0][0].length);
			l2 = rand.randomNumber(noc.matriz.length);
			c2 = rand.randomNumber(noc.matriz[0].length);
			a2 = rand.randomNumber(noc.matriz[0][0].length);
		}
		while(l1==l2 && c1==c2 && a1==a2);

		p1.setLinha(l1);
		p1.setColuna(c1);
		p1.setAltura(a1);
		p2.setLinha(l2);
		p2.setColuna(c2);
		p2.setAltura(a2);
	}
	
	private void swapCores(LinhaColunaAltura p1, LinhaColunaAltura p2)
	{
		ECWM_VerticeNoC nodo = new ECWM_VerticeNoC(noc.matriz[p1.getLinha()][p1.getColuna()][p1.getAltura()]);
		noc.matriz[p1.getLinha()][p1.getColuna()][p1.getAltura()] = new ECWM_VerticeNoC(noc.matriz[p2.getLinha()][p2.getColuna()][p2.getAltura()]);
		noc.matriz[p2.getLinha()][p2.getColuna()][p2.getAltura()] = new ECWM_VerticeNoC(nodo);		
	}
	private void copiaMapeamentos(ECWM_VerticeNoC destino[][][], ECWM_VerticeNoC origem[][][])
	{
		for(int linha=0; linha<destino.length; linha++)
		{
			for(int coluna=0; coluna<destino[linha].length; coluna++)
				for(int altura=0; altura<destino[linha][coluna].length; altura++)
				destino[linha][coluna][altura] =  new ECWM_VerticeNoC(origem[linha][coluna][altura]);
		}
	}
	private void copiaMapeamentoAtual(ECWM_VerticeNoC destino[][][])
	{
		for(int linha=0; linha<noc.matriz.length; linha++)
		{
			for(int coluna=0; coluna<noc.matriz[linha].length; coluna++)
				for(int altura=0; altura<noc.matriz[linha][coluna].length; altura++)
				destino[linha][coluna][altura] = new ECWM_VerticeNoC(noc.matriz[linha][coluna][altura]);
		}
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURA��O
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		System.out.println("\nNoC[vMS] (" + noc.getNumeroLinhas() + "x" + noc.getNumeroColunas()+ "x" + noc.getNumeroAltura() + ")");
		for(int linha=0; linha<minimumMapping.length; linha++)
		{
			for(int coluna=0; coluna<minimumMapping[linha].length; coluna++)
				for(int altura=0; altura<minimumMapping[linha][coluna].length; altura++)
			{
				if(minimumMapping[linha][coluna][altura]==null)
					return;
				System.out.println("\tRoteador(" + linha + ", " + coluna + ", " + altura + ")");
				minimumMapping[linha][coluna][altura].exibe();
			}
		}
	}
}
