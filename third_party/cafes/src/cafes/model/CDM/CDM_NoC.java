package cafes.model.CDM;

import cafes.ui.WindowPrincipal;

import java.io.*;
import java.util.Date;
import java.awt.*;

import cafes.common.*;
import cafes.NoC.*;
import cafes.model.*;

class CDM_NoC implements Serializable {
	private static final long serialVersionUID = 5533916862473954873L;
	public static final int ROUTER_SIZE_X = 75;
	public static final int ROUTER_SIZE_Y = 75;
	public static final int CHANNEL_SIZE_X = 75;
	public static final int CHANNEL_SIZE_Y = 75;
	public static final int TILE_SIZE_X = ROUTER_SIZE_X + CHANNEL_SIZE_X;
	public static final int TILE_SIZE_Y = ROUTER_SIZE_Y + CHANNEL_SIZE_Y;
	public static final int ARROW_WIDTH = 7;
	public static final int ARROW_FINAL = 14;
	public static final int LOZANGE_WIDTH = 25;
	public static final int LOZANGE_EIGHT = 25;

	private int plAnt;
	private int contador = 0;
	private double PsNoC;
	private LinhaColunaAltura vetLinhaColuna[];
	private String vetCore[];
	private boolean primeiraVez;
	private long numeroCombinacoes, numeroTotalCombinacoes;
	private int numeroLinhas, numeroColunas, numeroAltura;
	private double energiaConsumidaMapeamento;
	private transient WindowPrincipal WP;
	private transient CDM_WindowNoC WN;
	private long numeroCiclosTodosMapeamentos;
	private long menorNumeroCiclosDeTodosMapeamentos;
	private long maiorNumeroCiclosDeTodosMapeamentos;
	private long ciclo; // N�mero de ciclos da aplica��o
	private long ciclosLink;
	private long ciclosRoteador;
	private boolean comContencao; // Informa se o c�lculo do tempo deve ou n�o
									// considerar o efeito das conten��es
	private double periodoDoRelogio;
	private boolean ehTopologiaMesh;
	private double energiaBuffer;
	private double bufferSize;
	private double energiaControle;
	private double energiaLinkVertical;
	private double energiaLinkHorizontal, energiaLinkLongitudinal;
	private double energiaLocalLink;

	protected CDM_VerticeNoC matriz[][][];
	protected CDM_VerticeNoC matSalva[][][];
	private int viewX=0,viewY=0,viewZ=0;
	public void setView(int x, int y , int z) {
		viewX = x;
		viewY = y;
		viewZ = z;
		
	}
	
	public CDM_NoC(WindowPrincipal WP, CDM_WindowNoC WN, int numeroLinhas, int numeroColunas, int numeroAltura) {
		this.numeroLinhas = numeroLinhas;
		this.numeroColunas = numeroColunas;
		this.numeroAltura = numeroAltura;
		viewZ = numeroAltura-1;
		this.WP = WP;
		this.WN = WN;
		matriz = new CDM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
		matSalva = new CDM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
		PsNoC = WP.getPotRoteador() * numeroLinhas * numeroColunas * numeroAltura;
		ciclo = 0;

		energiaControle = WP.getEnergiaControlePhit();
		energiaBuffer = WP.getEnergiaBufferPhit();
		bufferSize = WP.getBufferSize();
		energiaLinkVertical = WP.getEnergiaLinkPhit() * WP.getTileHeigth();
		energiaLinkHorizontal = WP.getEnergiaLinkPhit() * WP.getTileWidth();
		energiaLinkLongitudinal = WP.getEnergiaLinkLongitudinalPhit() * WP.getTileWidth();
		energiaLocalLink = WP.getEnergiaLocalLinkPhit();
		ehTopologiaMesh = WP.ehTopologiaMesh();

		periodoDoRelogio = 1.0 / WP.getClockCycle();
		ciclosLink = WP.getLinkingCycles();
		ciclosRoteador = WP.getRoutingCycles();
	}

	public double getEnergiaBuffer() {
		return energiaBuffer;
	}

	public double getBufferSize() {
		return bufferSize;
	}

	public double getEnergiaControle() {
		return energiaControle;
	}

	public double getEnergiaLinkVertical() {
		return energiaLinkVertical;
	}

	public double getEnergiaLinkHorizontal() {
		return energiaLinkHorizontal;
	}

	public double getEnergiaLinkLongitudinal(){
		return energiaLinkLongitudinal;
		
	}
	public double getEnergiaLocalLink() {
		return energiaLocalLink;
	}

	public WindowPrincipal getMainWindow() {
		return WP;
	}

	public int getNumeroLinhas() {
		return numeroLinhas;
	}

	public int getNumeroColunas() {
		return numeroColunas;
	}

	public int getNumeroAltura() {
		return numeroAltura;
	}

	public void setCiclo(long ciclo) {
		this.ciclo = ciclo;
	}

	public double getEnergiaIdle() {
		return PsNoC * ciclo * periodoDoRelogio; // energia em nJ --> 10e-9
	}

	public double getEnergiaConsumidaMapeamento() {
		return energiaConsumidaMapeamento;
	}

	public void setEnergiaConsumidaMapeamento(double energiaConsumidaMapeamento) {
		this.energiaConsumidaMapeamento = energiaConsumidaMapeamento;
	}

	public void setComSemContencao(boolean comContencao) {
		this.comContencao = comContencao;
	}

	public long computaRoteador(CDM_Vertice p, int linha, int coluna, int altura, int posicao, long phits) {
		long cicloInicial = ciclo;
		long cicloFinal = cicloInicial + (int) (ciclosRoteador * (phits - 1));

		matriz[linha][coluna][altura].computaRoteador(p, cicloInicial, cicloFinal, posicao, phits);
		ciclo = ciclo + ciclosRoteador;

		return cicloFinal;
	}

	public long computaLink(CDM_Vertice p, int linha, int coluna, int altura, int posicao, long phits) {
		long cicloInicial = ciclo;
		long cicloFinal;
		long cicloFinalSemContencao = ciclo + (int) (ciclosLink * (phits - 1));

		cicloFinal = matriz[linha][coluna][altura]
				.computaLink(WN.getSequencia(), p, cicloInicial, ciclosLink, posicao, phits, comContencao);
		ciclo = ciclo + ciclosLink + cicloFinal - cicloFinalSemContencao;
		return cicloFinal;
	}

	public long topologia(CDM_Vertice p) {
		if (ehTopologiaMesh)
			return topologiaMesh(p);
		return topologiaTorus(p);
	}

	// Realiza o caminhamento XY, contabilizando os phits para cada aresta
	public long topologiaTorus(CDM_Vertice p) {
		int linhaOrigem, colunaOrigem, linhaDestino, colunaDestino, alturaOrigem, alturaDestino;
		String coreOrigem = p.getCoreOrigem();
		String coreDestino = p.getCoreDestino();
		long phits = p.getPhits();

		linhaOrigem = DescobreLinha(coreOrigem);
		colunaOrigem = DescobreColuna(coreOrigem);
		alturaOrigem = DescobreAltura(coreOrigem);
		linhaDestino = DescobreLinha(coreDestino);
		colunaDestino = DescobreColuna(coreDestino);
		alturaDestino = DescobreAltura(coreDestino);

		ciclo = p.getCicloInicial();
		long cicloFinalLocal = computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);
		p.setCicloFinalLocal(cicloFinalLocal);
		computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);

		if (Torus.deveAvancarPonteiro(alturaOrigem, alturaDestino, numeroAltura)) {
			while (alturaOrigem != alturaDestino) {
				alturaOrigem = Torus.incrementaPonteiro(alturaOrigem, numeroAltura);
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
			}
		} else {
			while (alturaOrigem != alturaDestino) {
				alturaOrigem = Torus.decrementaPonteiro(alturaOrigem, numeroAltura);
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
			}
		}

		if (Torus.deveAvancarPonteiro(colunaOrigem, colunaDestino, numeroColunas)) {
			while (colunaOrigem != colunaDestino) {
				colunaOrigem = Torus.incrementaPonteiro(colunaOrigem, numeroColunas);
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
			}
		} else {
			while (colunaOrigem != colunaDestino) {
				colunaOrigem = Torus.decrementaPonteiro(colunaOrigem, numeroColunas);
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
			}
		}
		if (Torus.deveAvancarPonteiro(linhaOrigem, linhaDestino, numeroLinhas)) {
			while (linhaOrigem != linhaDestino) {
				linhaOrigem = Torus.incrementaPonteiro(linhaOrigem, numeroLinhas);
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
			}
		} else {
			while (linhaOrigem != linhaDestino) {
				linhaOrigem = Torus.decrementaPonteiro(linhaOrigem, numeroLinhas);
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
			}
		}
		return computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_OUT, phits);
	}

	// Realiza o caminhamento XY, contabilizando os phits para cada aresta
	public long topologiaMesh(CDM_Vertice p) {
		int linhaOrigem, colunaOrigem, linhaDestino, colunaDestino, alturaOrigem, alturaDestino;
		String coreOrigem = p.getCoreOrigem();
		String coreDestino = p.getCoreDestino();
		long phits = p.getPhits();

		linhaOrigem = DescobreLinha(coreOrigem);
		colunaOrigem = DescobreColuna(coreOrigem);
		alturaOrigem = DescobreAltura(coreOrigem);
		linhaDestino = DescobreLinha(coreDestino);
		colunaDestino = DescobreColuna(coreDestino);
		alturaDestino = DescobreAltura(coreDestino);

		ciclo = p.getCicloInicial();
		long cicloFinalLocal = computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);
		p.setCicloFinalLocal(cicloFinalLocal);
		computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);
		while (alturaOrigem != alturaDestino) // Algoritmo XY
		{
			if (alturaOrigem > alturaDestino) {
				alturaOrigem--;
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
			} else {
				alturaOrigem++;
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
			}
		}

		while (colunaOrigem != colunaDestino) {
			if (colunaOrigem > colunaDestino) {
				colunaOrigem--;
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
			} else {
				colunaOrigem++;
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
			}
		}
		while (linhaOrigem != linhaDestino) // Algoritmo XY
		{
			if (linhaOrigem > linhaDestino) {
				linhaOrigem--;
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
			} else {
				linhaOrigem++;
				computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
				computaRoteador(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
			}
		}
		return computaLink(p, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_OUT, phits);
	}

	// Descobre a posi��o X de um m�dulo em uma NoC
	public int DescobreLinha(String core) {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (matriz[linha][coluna][altura].getCoreName().equals(core))
						return linha;
				}
		}
		return -1;
	}

	// Descobre a posi��o Y de um m�dulo em uma NoC
	public int DescobreColuna(String core) {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (matriz[linha][coluna][altura].getCoreName().equals(core))
						return coluna;
				}
		}
		return -1;
	}

	public int DescobreAltura(String core) {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (matriz[linha][coluna][altura].getCoreName().equals(core))
						return altura;
				}
		}
		return -1;
	}

	public double energiaTotalNoC() {
		return energiaNoC() + getEnergiaIdle();
	}

	public double energiaNoC() {
		double energia = 0;

		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					energia = energia + matriz[linha][coluna][altura].energiaTotalRoteador();
		}
		return energia;
	}

	public void limpaEnergiaDinamica() {
		ciclo = 0;
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matriz[linha][coluna][altura].limpaEnergiaDinamica();
		}
	}

	public void salvaPosicionamento() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matSalva[linha][coluna][altura] = new CDM_VerticeNoC(matriz[linha][coluna][altura]);
		}
	}

	public void criaVetorDeCores(CDM_Grafo g) {
		CDM_Vertice l = g.getInicio();
		String coreOrigem = new String("");
		String coreDestino = new String("");
		int posicao = 0;

		vetCore = new String[matSalva.length * matSalva[0].length* matSalva[0][0].length];
		while (l != null) {
			if (l.getID() == CDM_Grafo.END || l.getID() == CDM_Grafo.START) {
				l = l.getProx();
				continue;
			}
			boolean achouCoreOrigem = false, achouCoreDestino = false;
			if (posicao == 0) {
				coreOrigem = l.getCoreOrigem();
				coreDestino = l.getCoreDestino();
			}
			for (int i = 0; i < posicao; i++) // Pesquisa para ver se est� no
												// vetor
			{
				coreOrigem = l.getCoreOrigem();
				if (vetCore[i] != null && vetCore[i].equals(coreOrigem))
					achouCoreOrigem = true;
				coreDestino = l.getCoreDestino();
				if (vetCore[i] != null && vetCore[i].equals(coreDestino))
					achouCoreDestino = true;
			}
			if (achouCoreOrigem == false) {
				vetCore[posicao] = coreOrigem;
				posicao++;
			}
			if (achouCoreDestino == false) {
				vetCore[posicao] = coreDestino;
				posicao++;
			}
			l = l.getProx();
		}
		while (posicao < vetCore.length)
			vetCore[posicao++] = new String("-");
	}

	public void copiaMatComMatSalva() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				matriz[linha][coluna] = matSalva[linha][coluna];
		}
	}

	public void copiaMatSalvaComMat() {
		for (int linha = 0; linha < matSalva.length; linha++) {
			for (int coluna = 0; coluna < matSalva[linha].length; coluna++)
				for (int altura = 0; altura < matSalva[linha][coluna].length; altura++)
					matSalva[linha][coluna][altura] = matriz[linha][coluna][altura];
		}
	}

	public void conectaLinhaColunaComCore(LinhaColunaAltura lc, String coreElemento) {
		matriz[lc.getLinha()][lc.getColuna()][lc.getAltura()] = new CDM_VerticeNoC(coreElemento, lc, this);
	}

	public void criaVetorDeLinhaColuna() {
		vetLinhaColuna = new LinhaColunaAltura[matriz.length * matriz[0].length * matriz[0][0].length];

		for (int linha = 0, k = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)

					vetLinhaColuna[k++] = new LinhaColunaAltura(linha, coluna, altura);
		}
	}

	public double computa(CDM_Grafo g) {
		ciclo = g.executaCDM(this, true); // ciclo passa a ser atualizado com o
											// ciclo da chegada do �ltimo pacote
		exibeAndamento();

		numeroCiclosTodosMapeamentos = numeroCiclosTodosMapeamentos + ciclo;
		if (maiorNumeroCiclosDeTodosMapeamentos < ciclo)
			maiorNumeroCiclosDeTodosMapeamentos = ciclo;
		if (menorNumeroCiclosDeTodosMapeamentos > ciclo)
			menorNumeroCiclosDeTodosMapeamentos = ciclo;

		return energiaTotalNoC();
	}

	public void algoritmoPosicionamento(CDM_Grafo g, int algo) {
		primeiraVez = true;
		numeroCombinacoes = 0;
		numeroCiclosTodosMapeamentos = 0;
		maiorNumeroCiclosDeTodosMapeamentos = 0;
		menorNumeroCiclosDeTodosMapeamentos = Integer.MAX_VALUE;
		energiaConsumidaMapeamento = Double.MAX_VALUE;
		criaVetorDeCores(g);
		criaVetorDeLinhaColuna();
		exibeVetLinhaColuna(vetLinhaColuna);
		exibeVetString(vetCore);
		g.executaASAPeCriaListaDeNiveis();
		long tInicial = 0, tFinal = 0;
		switch (algo) {
		case Algoritmo.ExhaustiveSearch:
			CDM_Exaustivo e = new CDM_Exaustivo(this, g);
			numeroTotalCombinacoes = Matematico.fatorial(numeroLinhas * numeroColunas);
			tInicial = new Date().getTime();
			e.algoritmo(vetLinhaColuna, vetCore, vetLinhaColuna.length);
			setCiclo(menorNumeroCiclosDeTodosMapeamentos);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.SimulatedAnnealing:
			CDM_SimulatedAnnealing sa = new CDM_SimulatedAnnealing(this, g);
			numeroTotalCombinacoes = sa.numeroTotalCombinacoes();
			tInicial = new Date().getTime();
			sa.algoritmo(vetLinhaColuna, vetCore);
			setCiclo(menorNumeroCiclosDeTodosMapeamentos);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.TabooSearch:
			CDM_TabooSearch ts = new CDM_TabooSearch(this, g);
			numeroTotalCombinacoes = ts.numeroTotalCombinacoes();
			tInicial = new Date().getTime();
			ts.algoritmo(vetLinhaColuna, vetCore);
			setCiclo(menorNumeroCiclosDeTodosMapeamentos);
			tFinal = new Date().getTime();
			break;
		}
		exibeNoCSalva();
		copiaMatComMatSalva();
		System.out.println("Execution Time of CDA (CPU time): " + (tFinal - tInicial) + "ms");
		System.out.println("Energy Consumption: " + (energiaConsumidaMapeamento - getEnergiaIdle()) / 1000 + "uJ");
		System.out.println("Idle Energy Consumption: " + getEnergiaIdle() / 1000 + "uJ");
		System.out.println("Minimum Number of Cycles: " + menorNumeroCiclosDeTodosMapeamentos);
		System.out.println("Maximum Number of Cycles: " + maiorNumeroCiclosDeTodosMapeamentos);
		System.out.println("Number of Cycles: Average (all mappings): " + numeroCiclosTodosMapeamentos / numeroTotalCombinacoes);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DEPURA��O
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibeAndamento() {
		if (primeiraVez) {
			plAnt = 0;
			primeiraVez = false;
			System.out.println("	1	2	3	4	5	6	7	8	9   10");
			System.out.println("24680246802468024680246802468024680246802468024680");
		} else {
			double pd = (double) numeroCombinacoes / (double) numeroTotalCombinacoes;
			int pl = (int) (pd * 100.0);

			if (pl > plAnt) {
				int localCount = plAnt;

				for (int i = 0; i < (pl - plAnt); i++) {
					localCount++;
					if ((localCount % 2) == 0) {
						EvolucaoAlgoritmoMapeamento eam = WN.getEvolucaoMapeamento();

						eam.incrementaEvolucaoAlgoritmoMapeamento();
						eam.paint(eam.getGraphics());
						System.out.print(".");
					}
				}
				plAnt = pl;
			}
		}
		numeroCombinacoes++;
	}

	public void exibeVetLinhaColuna(LinhaColunaAltura vet[]) {
		System.out.println("Vetor LinhaColunaAltura:");
		System.out.print("\t");
		for (int i = 0; i < vet.length; i++)
			System.out.print("[" + vet[i].getLinha() + ", " + vet[i].getColuna()  + ", " + vet[i].getAltura() + "] ");
		System.out.println("\n---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}

	public void exibeVetString(String vet[]) {
		System.out.println("Vetor de cores:");
		System.out.print("\t");
		for (int i = 0; i < vet.length; i++)
			System.out.print(vet[i] + " ");
		System.out.println("\n---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}

	public void exibeNoCResumido() {
		System.out.println("-------------");
		System.out.println(contador++);
		for (int linha = 0; linha < matriz.length; linha++) {
			System.out.print("\t");
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					System.out.print("[" + matriz[linha][coluna][altura].getCoreName() + "] ");
			System.out.println();
		}
		if (contador == 1560)
			System.out.println();
	}

	public void exibe() {
		System.out.println("\nNoC (" + numeroLinhas + "x" + numeroColunas + "x" + numeroAltura + ")");
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (matriz[linha][coluna][altura].getEnergiaControleRoteador() <= 0)
						continue;
					System.out.print("\n\tRoteador(" + linha + ", " + coluna + "," + altura + ")");
					matriz[linha][coluna][altura].exibe();
				}
			}
		}
		System.out.println("\n");
		System.out.println("Energy Consumption: " + energiaNoC() / 1000 + "uJ");
		System.out.println("Idle Energy Consumption: " + getEnergiaIdle() / 1000 + "uJ");
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}

	public void exibeNoCSalva() {
		System.out.print("\nNoC FINAL(" + numeroLinhas + "x" + numeroColunas + "x" + numeroAltura+ ")");
		for (int linha = 0; linha < matSalva.length; linha++) {
			for (int coluna = 0; coluna < matSalva[linha].length; coluna++) {
				for (int altura = 0; altura < matSalva[linha][coluna].length; altura++) {
					if (matSalva[linha][coluna][altura].getEnergiaControleRoteador() <= 0)
						continue;
					System.out.print("\n\tRoteador(" + linha + ", " + coluna + "," + altura + ")");
					matSalva[linha][coluna][altura].exibe();
				}
			}
		}
		System.out.println();
		System.out.println("Energy Consumption: " + energiaNoC() / 1000 + "uJ");
		System.out.println("Idle Energy Consumption: " + getEnergiaIdle() / 1000 + "uJ");
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}

	public String imprimeNoC() {
		String s = new String("");

		for (int linha = 0; linha < matSalva.length; linha++) {
			for (int coluna = 0; coluna < matSalva[linha].length; coluna++)
				for (int altura = 0; altura < matSalva[linha][coluna].length; altura++)
					s = s.concat(" " + matSalva[linha][coluna][altura].getCoreName());
			s = s.concat("\n");
		}
		return s;
	}

	public void desenhaNoC(Graphics g) {
		int l = ehTopologiaMesh ? 0 : CHANNEL_SIZE_Y;

		for (int linha = 0; linha < numeroLinhas; linha++) {
			int c = ehTopologiaMesh ? 0 : CHANNEL_SIZE_X;

			for (int coluna = 0; coluna < numeroColunas; coluna++) {
				for (int altura = viewZ; altura < viewZ+1; altura++) {
					desenhaCanalLocalIn(g, c, l, matriz[linha][coluna][altura].linkEntrada[Router.LOCAL_IN].energiaLink());
					desenhaCanalLocalOut(g, c, l, matriz[linha][coluna][altura].linkEntrada[Router.LOCAL_OUT].energiaLink());
					if (!ehTopologiaMesh || coluna != numeroColunas - 1)
						desenhaCanalLeste(g, c, l, matriz[linha][coluna][altura].linkEntrada[Router.LESTE].energiaLink());
					if (!ehTopologiaMesh || coluna != 0)
						desenhaCanalOeste(g, c, l, matriz[linha][coluna][altura].linkEntrada[Router.OESTE].energiaLink());
					if (!ehTopologiaMesh || linha != 0)
						desenhaCanalNorte(g, c, l, matriz[linha][coluna][altura].linkEntrada[Router.NORTE].energiaLink());
					if (!ehTopologiaMesh || linha != numeroLinhas - 1)
						desenhaCanalSul(g, c, l, matriz[linha][coluna][altura].linkEntrada[Router.SUL].energiaLink());
					if (!ehTopologiaMesh || altura != 0)
						desenhaCanalSuperior(g, c, l, matriz[linha][coluna][altura].linkEntrada[Router.SUPERIOR].energiaLink());
					if (!ehTopologiaMesh || altura != numeroAltura - 1)
						desenhaCanalInferior(g, c, l, matriz[linha][coluna][altura].linkEntrada[Router.INFERIOR].energiaLink());
					desenhaRoteador(g, c, l, linha, coluna, altura);
					desenhaCore(g, c, l, matriz[linha][coluna][altura].getCoreName());
				}
				c = c + TILE_SIZE_X;
			}
			l = l + TILE_SIZE_Y;
		}
	}

	public void desenhaRoteador(Graphics g, int c, int l, int linha, int coluna,int altura) {
		String coreName = matriz[linha][coluna][altura].getCoreName();

		// Retangulo
		if (coreName.equals("-"))
			g.setColor(new Color(0, 0, 100));
		else
			g.setColor(new Color(0, 0, 0));
		g.fillRect(c, l, ROUTER_SIZE_X, ROUTER_SIZE_Y);
		// T�tulo
		g.setFont(new Font("Arial", Font.BOLD, 12));
		g.setColor(Color.white);
		String id = new String("R[" + linha + ", " + coluna  + ", " + altura + "]");
		g.drawString(id, Math.round(c + (ROUTER_SIZE_X - id.length() * 6) / 2), l + (ROUTER_SIZE_Y / 3) - 8);
		// Energias
		g.setFont(new Font("Arial", Font.BOLD, 11));

		String energiaBufferRoteador = String.format("Eb %.2f", new Double(matriz[linha][coluna][altura].getEnergiaBufferRoteador() / 1000));
		g.drawString(energiaBufferRoteador, (int) Math.round(c + (ROUTER_SIZE_X - energiaBufferRoteador.length() * 5.5) / 2), l
				+ (2 * (ROUTER_SIZE_Y / 3)) - 8);

		String energiaControleRoteador = String.format("Es %.2f", new Double(matriz[linha][coluna][altura].getEnergiaControleRoteador() / 1000));
		g.drawString(energiaControleRoteador, (int) Math.round(c + (ROUTER_SIZE_X - energiaControleRoteador.length() * 5.5) / 2), l
				+ ROUTER_SIZE_Y - 8);
	}

	public void desenhaCore(Graphics g, int linha, int coluna, String label) {
		int width = 50;
		int eight = 50;

		Draw.lozengeAndLabel(g, Color.gray, Color.white, linha + TILE_SIZE_X - width / 2, coluna + TILE_SIZE_Y - eight / 2, width, eight,
				label);
	}

	public void desenhaCanalLocalIn(Graphics g, int c, int l, double value) {
		int x = c + ROUTER_SIZE_X - ARROW_WIDTH * 2 - LOZANGE_WIDTH;
		int y = l + ROUTER_SIZE_Y - LOZANGE_EIGHT;

		Draw.arrow(g, Color.blue, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x + CHANNEL_SIZE_X - 6, y + CHANNEL_SIZE_Y - 6, x + LOZANGE_WIDTH,
				y + LOZANGE_EIGHT);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // leste
		{
			String bytes = String.format("F%.2f", new Double(value / 1000));
			int offsetX = c + 2 * ROUTER_SIZE_X - ARROW_WIDTH * 2 - 12 - LOZANGE_WIDTH;
			int offsetY = l + 2 * ROUTER_SIZE_Y - 6 - LOZANGE_EIGHT;
			int variacao = 0;

			StringBuffer SB = new StringBuffer(bytes);
			SB = SB.reverse();
			bytes = SB.toString();
			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				g.drawString("" + bytes.charAt(k), offsetX + variacao, offsetY + variacao);
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					variacao = variacao - 4;
				else
					variacao = variacao - 5;
			}
		}
	}

	public void desenhaCanalLocalOut(Graphics g, int c, int l, double value) {
		int x = c + ROUTER_SIZE_X - 6 - LOZANGE_WIDTH;
		int y = l + ROUTER_SIZE_Y - ARROW_WIDTH * 2 - 6 - LOZANGE_EIGHT;

		Draw.arrow(g, Color.red, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x + CHANNEL_SIZE_X, y + CHANNEL_SIZE_Y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // leste
		{
			String bytes = String.format("%.2fF", new Double(value / 1000));
			
			int offsetX = c + ROUTER_SIZE_X  +ARROW_FINAL;
			int offsetY = l + ROUTER_SIZE_Y - 8+ARROW_FINAL;
			int variacao = 0;

			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					variacao = variacao + 4;
				else
					variacao = variacao + 5;
				g.drawString("" + bytes.charAt(k), offsetX + variacao, offsetY + variacao);
			}
		}
	}

	public void desenhaCanalNorte(Graphics g, int c, int l, double value) {
		int x = c + (ROUTER_SIZE_X / 3);
		int y = l;

		Draw.arrow(g, Color.blue, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y - CHANNEL_SIZE_Y, x, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // norte
		{
			String bytes = String.format("%.2fF", new Double(value / 1000));
			int offset = -CHANNEL_SIZE_Y;

			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 3;
				else
					offset = offset + 9;
				g.drawString("" + bytes.charAt(k), c + ROUTER_SIZE_X / 3 - ARROW_WIDTH / 2, l + offset);
			}
		}
	}

	public void desenhaCanalSul(Graphics g, int c, int l, double value) {
		int x = c + (2 * (ROUTER_SIZE_X / 3));
		int y = l + ROUTER_SIZE_Y;

		Draw.arrow(g, Color.red, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y + CHANNEL_SIZE_Y, x, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // sul
		{
			String bytes = String.format("F%.2f", new Double(value / 1000));
			int offset = 2 * ROUTER_SIZE_X;

			StringBuffer SB = new StringBuffer(bytes);
			SB = SB.reverse();
			bytes = SB.toString();
			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				g.drawString("" + bytes.charAt(k), c + 2 * (ROUTER_SIZE_X / 3) - ARROW_WIDTH / 2, l + offset);
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset - 3;
				else
					offset = offset - 9;
			}
		}
	}
	
	public void desenhaCanalSuperior(Graphics g, int c, int l, double value) {
		int width = ROUTER_SIZE_X/2;
		int heigth = ARROW_FINAL;
		int x = c + ROUTER_SIZE_X - width/2 +10;
		int y = l + ROUTER_SIZE_Y - CHANNEL_SIZE_Y ;
		
		//int x = c;
		//int y = l + ROUTER_SIZE_Y / 3;

		Color qualCor =  WP.getHighLinkColor();
		g.setColor(qualCor);
		g.fillRect(x,  y , width+heigth/2, heigth);
		g.setColor(Color.BLACK);
		g.drawRect(x,  y , width+heigth/2, heigth);
		//Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x - CHANNEL_SIZE_X, y, x, y);
		
		Draw.upperLink(g, qualCor, x+width, y, heigth);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // oeste
		{
			String bytes = String.format("%.2fF", new Double(value / 1000));
			int offset = 8;

			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				g.drawString("" + bytes.charAt(k),  x + offset, y+heigth/2+3);
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 2;
				else
					offset = offset + 6;
			}
			
		}
		
	}

	public void desenhaCanalInferior(Graphics g, int c, int l, double value) {
		int width = ROUTER_SIZE_X/2;
		int heigth = ARROW_FINAL;
		int x = c + ROUTER_SIZE_X - width/2 +10;
		int y = l + ROUTER_SIZE_Y - heigth-1;

		Color qualCor =   WP.getHighLinkColor();
		g.setColor(qualCor);
		g.fillRect(x,  y , width+heigth/2, heigth);
		g.setColor(Color.BLACK);
		g.drawRect(x,  y , width+heigth/2, heigth);
		//Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x - CHANNEL_SIZE_X, y, x, y);
		
				
		
		Draw.downLink(g, qualCor, x+width, y, heigth);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // oeste
		{
			String bytes = String.format("%.2fF", new Double(value / 1000));
			int offset = 8;
			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				g.drawString("" + bytes.charAt(k), x + offset, y+heigth/2+3);
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 2;
				else
					offset = offset + 6;
			}
			
			
		}
	}

	public void desenhaCanalLeste(Graphics g, int c, int l, double value) {
		int x = c + ROUTER_SIZE_X;
		int y = l + (ROUTER_SIZE_Y / 3);

		Draw.arrow(g, Color.red, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x + CHANNEL_SIZE_X, y, x, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // leste
		{
			String bytes = String.format("%.2fF", new Double(value / 1000));
			int offset = 2 * ROUTER_SIZE_X;

			StringBuffer SB = new StringBuffer(bytes);
			SB = SB.reverse();
			bytes = SB.toString();
			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset - 2;
				else
					offset = offset - 6;
				g.drawString("" + bytes.charAt(k), c + offset, l + ROUTER_SIZE_Y / 3 + ARROW_WIDTH / 2 + 1);
			}
		}
	}

	public void desenhaCanalOeste(Graphics g, int c, int l, double value) {
		int x = c;
		int y = l + (2 * (ROUTER_SIZE_Y / 3));

		Draw.arrow(g, Color.blue, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x - CHANNEL_SIZE_X, y, x, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // oeste
		{
			String bytes = String.format("%.2fF", new Double(value / 1000));
			int offset = -CHANNEL_SIZE_Y;

			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				g.drawString("" + bytes.charAt(k), c + offset, l + 2 * (ROUTER_SIZE_Y / 3) + ARROW_WIDTH / 2 + 1);
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 2;
				else
					offset = offset + 6;
			}
		}
	}
}