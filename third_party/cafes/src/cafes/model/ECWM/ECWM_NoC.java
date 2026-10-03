package cafes.model.ECWM;

import cafes.ui.WindowPrincipal;

import java.io.*;
import java.awt.*;
import java.util.*;

import cafes.common.*;
import cafes.NoC.*;
import cafes.model.*;

class ECWM_NoC implements Serializable {
	private static final long serialVersionUID = 8279502460022109285L;
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
	private double energiaConsumidaTodosMapeamentos;
	private double maiorEnergiaConsumidaDeTodosMapeamentos;

	private transient WindowPrincipal WP;
	private transient ECWM_WindowNoC WN;
	private int ciclosLink;
	private int ciclosRoteador;
	private double periodoDoRelogio;
	private boolean ehTopologiaMesh;

	private double energiaControle;
	private double energiaControleComChaveamento;
	private double energiaBuffer;
	private double energiaBufferComChaveamento;
	private double energiaLinkVertical;
	private double energiaLinkHorizontal;
	private double energiaLinkLongitudinal;
	private double energiaLinkComChaveamentoVertical;
	private double energiaLinkComChaveamentoHorizontal;
	private double energiaLinkComChaveamentoLongitudinal;
	private double energiaLocalLink;
	private double energiaLocalLinkComChaveamento;

	private long ciclosOperacao;
	private long ciclosOperacaoTemporario;

	protected ECWM_VerticeNoC matriz[][][];
	protected ECWM_VerticeNoC matSalva[][][];

	private int viewX=0,viewY=0,viewZ=0;
	public void setView(int x, int y , int z) {
		viewX = x;
		viewY = y;
		viewZ = z;
		
	}
	public ECWM_NoC(WindowPrincipal WP, ECWM_WindowNoC WN, int numeroLinhas, int numeroColunas, int numeroAltura) {
		this.numeroLinhas = numeroLinhas;
		this.numeroColunas = numeroColunas;
		this.numeroAltura = numeroAltura;
		this.WP = WP;
		this.WN = WN;
		viewZ = numeroAltura-1;
		energiaControle = WP.getEnergiaControlePhit();
		energiaControleComChaveamento = WP.getEnergiaControleComChaveamentoPhit();
		energiaBuffer = WP.getEnergiaBufferPhit() * WP.getBufferSize();
		energiaBufferComChaveamento = WP.getEnergiaBufferComChaveamentoPhit();
		energiaLinkVertical = WP.getEnergiaLinkPhit() * WP.getTileHeigth();
		energiaLinkHorizontal = WP.getEnergiaLinkPhit() * WP.getTileWidth();
		energiaLinkLongitudinal = WP.getEnergiaLinkLongitudinalPhit() * WP.getTileWidth();
		energiaLinkComChaveamentoVertical = WP.getEnergiaLinkComChaveamentoPhit();
		energiaLinkComChaveamentoHorizontal = WP.getEnergiaLinkComChaveamentoPhit();
		energiaLinkComChaveamentoLongitudinal = WP.getEnergiaLinkComChaveamentoPhit();
		energiaLocalLink = WP.getEnergiaLocalLinkPhit();
		energiaLocalLinkComChaveamento = WP.getEnergiaLocalLinkComChaveamentoPhit();
		ehTopologiaMesh = WP.ehTopologiaMesh();

		periodoDoRelogio = 1.0 / WP.getClockCycle();
		ciclosLink = WP.getLinkingCycles();
		ciclosRoteador = WP.getRoutingCycles();
		PsNoC = WP.getPotRoteador() * numeroLinhas * numeroColunas;
		matriz = new ECWM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
		matSalva = new ECWM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
	}

	public double getEnergiaControle() {
		return energiaControle;
	}

	public double getEnergiaControleComChaveamento() {
		return energiaControleComChaveamento;
	}

	public double getEnergiaBuffer() {
		return energiaBuffer;
	}

	public double getEnergiaBufferComChaveamento() {
		return energiaBufferComChaveamento;
	}

	public double getEnergiaLinkLongitudinal() {
		return energiaLinkLongitudinal;
	}

	public double getEnergiaLinkVertical() {
		return energiaLinkVertical;
	}

	public double getEnergiaLinkHorizontal() {
		return energiaLinkHorizontal;
	}

	public double getEnergiaLinkComChaveamentoVertical() {
		return energiaLinkComChaveamentoVertical;
	}

	public double getEnergiaLinkComChaveamentoHorizontal() {
		return energiaLinkComChaveamentoHorizontal;
	}

	public double getEnergiaLinkComChaveamentoLongitudinal() {
		return energiaLinkComChaveamentoLongitudinal;
	}

	public double getEnergiaLocalLink() {
		return energiaLocalLink;
	}

	public double getEnergiaLocalLinkComChaveamento() {
		return energiaLocalLinkComChaveamento;
	}

	public int getLinkingCycles() {
		return ciclosLink;
	}

	public int getRoutingCycles() {
		return ciclosRoteador;
	}

	public WindowPrincipal getMainWindow() {
		return WP;
	}

	public int getNumeroLinhas() {
		return numeroLinhas;
	}

	public int getNumeroAltura() {
		return numeroAltura;
	}

	public int getNumeroColunas() {
		return numeroColunas;
	}

	public double getEnergiaIdle() {
		return getEnergiaIdle(ciclosOperacao); // energia em nJ --> 10e-9
	}

	public double getEnergiaIdle(long ciclos) {
		return PsNoC * ciclos * periodoDoRelogio; // energia em nJ --> 10e-9
	}

	public void computaEnergiaRoteador(int linha, int coluna, int altura, long phitsSemChaveamento,
			long phitsComChaveamento) {
		matriz[linha][coluna][altura].computaEnergiaRoteador(phitsSemChaveamento, phitsComChaveamento);
	}

	public void computaEnergiaLink(int linha, int coluna, int altura, int posicao, long phitsSemChaveamento,
			long phitsComChaveamento) {
		matriz[linha][coluna][altura].computaEnergiaLink(posicao, phitsSemChaveamento, phitsComChaveamento);
	}

	public double getEnergiaConsumidaMapeamento() {
		return energiaConsumidaMapeamento;
	}

	public void setEnergiaConsumidaMapeamento(double energiaConsumidaMapeamento) {
		this.energiaConsumidaMapeamento = energiaConsumidaMapeamento;
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
					matSalva[linha][coluna][altura] = new ECWM_VerticeNoC(matriz[linha][coluna][altura]);
		}
	}

	public void criaVetorDeCores(ECWM_Grafo g) {
		ECWM_Vertice l = g.getInicio();
		int posicao = 0;

		vetCore = new String[matSalva.length * matSalva[0].length * matSalva[0][0].length];
		while (l != null) {
			vetCore[posicao] = new String(l.getInf());
			l = l.getProx();
			posicao++;
		}
		while (posicao < vetCore.length)
			vetCore[posicao++] = new String("-");
	}

	public void copiaMatComMatSalva() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matriz[linha][coluna][altura] = matSalva[linha][coluna][altura];
		}
	}

	public void copiaMatSalvaComMat() {
		for (int linha = 0; linha < matSalva.length; linha++) {
			for (int coluna = 0; coluna < matSalva[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matSalva[linha][coluna][altura] = matriz[linha][coluna][altura];
		}
	}

	public void conectaLinhaColunaComCore(LinhaColunaAltura lc, String coreElemento) {
		matriz[lc.getLinha()][lc.getColuna()][lc.getAltura()] = new ECWM_VerticeNoC(coreElemento, lc, this);
	}

	public void criaVetorDeLinhaColuna() {
		vetLinhaColuna = new LinhaColunaAltura[matriz.length * matriz[0].length * matriz[0][0].length];

		for (int linha = 0, k = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					vetLinhaColuna[k++] = new LinhaColunaAltura(linha, coluna, altura);
		}
	}

	public void armazenaCiclosOperacao(long ciclos) {
		ciclosOperacaoTemporario = ciclos;
	}

	public long getCiclosOperacaoTemporario() {
		return ciclosOperacaoTemporario;
	}

	public void salvaCiclosOperacao() {
		ciclosOperacao = ciclosOperacaoTemporario;
	}

	public void salvaCiclosOperacao(long ciclos) {
		ciclosOperacao = ciclos;
	}

	public void topologia(ECWM_Vertice p, ECWM_VerticeAdjacente v) {
		if (ehTopologiaMesh) {
			topologiaMesh(p, v);
			return;
		}
		topologiaTorus(p, v);
	}

	// Realiza o caminhamento XY, contabilizando os phits para cada aresta
	public void topologiaTorus(ECWM_Vertice p, ECWM_VerticeAdjacente v) {
		int linhaOrigem, colunaOrigem, linhaDestino, colunaDestino, alturaDestino, alturaOrigem;
		String origem = p.getInf();
		String destino = v.getInf();

		long phitsComChaveamento = (long) (v.getPhits() * (v.getPercentualChaveamentoPhits() / 100.0));
		long phitsSemChaveamento = v.getPhits() - phitsComChaveamento;

		linhaOrigem = DescobreLinha(origem);
		colunaOrigem = DescobreColuna(origem);
		alturaOrigem = DescobreAltura(origem);
		linhaDestino = DescobreLinha(destino);
		colunaDestino = DescobreColuna(destino);
		alturaDestino = DescobreAltura(destino);

		computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phitsComChaveamento,
				phitsSemChaveamento);
		computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento, phitsSemChaveamento);
		if (Torus.deveAvancarPonteiro(colunaOrigem, colunaDestino, numeroColunas)) {
			while (colunaOrigem != colunaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phitsComChaveamento,
						phitsSemChaveamento);
				colunaOrigem = Torus.incrementaPonteiro(colunaOrigem, numeroColunas);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			}
		} else {
			while (colunaOrigem != colunaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phitsComChaveamento,
						phitsSemChaveamento);
				colunaOrigem = Torus.decrementaPonteiro(colunaOrigem, numeroColunas);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			}
		}
		if (Torus.deveAvancarPonteiro(linhaOrigem, linhaDestino, numeroLinhas)) {
			while (linhaOrigem != linhaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phitsComChaveamento,
						phitsSemChaveamento);
				linhaOrigem = Torus.incrementaPonteiro(linhaOrigem, numeroLinhas);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			}
		} else {
			while (linhaOrigem != linhaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phitsComChaveamento,
						phitsSemChaveamento);
				linhaOrigem = Torus.decrementaPonteiro(linhaOrigem, numeroLinhas);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			}
		}

		if (Torus.deveAvancarPonteiro(alturaOrigem, alturaDestino, numeroAltura)) {
			while (alturaOrigem != alturaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phitsComChaveamento,
						phitsSemChaveamento);
				alturaOrigem = Torus.incrementaPonteiro(alturaOrigem, numeroAltura);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			}
		} else {
			while (alturaOrigem != alturaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phitsComChaveamento,
						phitsSemChaveamento);
				alturaOrigem = Torus.decrementaPonteiro(alturaOrigem, numeroAltura);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			}
		}

		computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_OUT, phitsComChaveamento,
				phitsSemChaveamento);
	}

	// Realiza o caminhamento XY, contabilizando os phits para cada aresta
	public void topologiaMesh(ECWM_Vertice p, ECWM_VerticeAdjacente v) {
		int linhaOrigem, colunaOrigem, linhaDestino, colunaDestino, alturaOrigem, alturaDestino;
		String origem = p.getInf();
		String destino = v.getInf();

		long phitsComChaveamento = (long) (v.getPhits() * (v.getPercentualChaveamentoPhits() / 100.0));
		long phitsSemChaveamento = v.getPhits() - phitsComChaveamento;

		linhaOrigem = DescobreLinha(origem);
		colunaOrigem = DescobreColuna(origem);
		alturaOrigem = DescobreAltura(origem);
		linhaDestino = DescobreLinha(destino);
		colunaDestino = DescobreColuna(destino);
		alturaDestino = DescobreAltura(destino);

		computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phitsComChaveamento,
				phitsSemChaveamento);
		computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento, phitsSemChaveamento);
		while (colunaOrigem != colunaDestino) {
			if (colunaOrigem > colunaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phitsComChaveamento,
						phitsSemChaveamento);
				colunaOrigem--;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			} else {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phitsComChaveamento,
						phitsSemChaveamento);
				colunaOrigem++;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			}
		}
		while (linhaOrigem != linhaDestino) // Algoritmo XY
		{
			if (linhaOrigem > linhaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phitsComChaveamento,
						phitsSemChaveamento);
				linhaOrigem--;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			} else {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phitsComChaveamento,
						phitsSemChaveamento);
				linhaOrigem++;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			}
		}

		while (alturaOrigem != alturaDestino) // Algoritmo XY
		{
			if (alturaOrigem > alturaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phitsComChaveamento,
						phitsSemChaveamento);
				alturaOrigem--;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			} else {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phitsComChaveamento,
						phitsSemChaveamento);
				alturaOrigem++;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, phitsComChaveamento,
						phitsSemChaveamento);
			}
		}
		computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_OUT, phitsComChaveamento,
				phitsSemChaveamento);
	}

	public double computa(ECWM_Grafo g, boolean comEstimativaDeTempo) {
		double energiaConsumida;

		limpaEnergiaDinamica();
		if (comEstimativaDeTempo) {
			g.executaECWM(this, ehTopologiaMesh);
			energiaConsumida = energiaNoC() + getEnergiaIdle(ciclosOperacaoTemporario);
		} else {
			g.executaECWM(this);
			energiaConsumida = energiaNoC();
		}
		exibeAndamento();

		energiaConsumidaTodosMapeamentos = energiaConsumidaTodosMapeamentos + energiaConsumida;
		if (maiorEnergiaConsumidaDeTodosMapeamentos < energiaConsumida)
			maiorEnergiaConsumidaDeTodosMapeamentos = energiaConsumida;

		return energiaConsumida;
	}

	public void algoritmoPosicionamento(ECWM_Grafo g, int algo, boolean comEstimativaDeTempo) {
		primeiraVez = true;
		numeroCombinacoes = 0;
		energiaConsumidaTodosMapeamentos = 0;
		maiorEnergiaConsumidaDeTodosMapeamentos = 0;
		energiaConsumidaMapeamento = Double.MAX_VALUE;
		criaVetorDeCores(g);
		criaVetorDeLinhaColuna();
		exibeVetLinhaColuna(vetLinhaColuna);
		exibeVetString(vetCore);
		long tInicial = 0, tFinal = 0;
		switch (algo) {
		case Algoritmo.ExhaustiveSearch:
			ECWM_Exaustivo e = new ECWM_Exaustivo(this, g, comEstimativaDeTempo);
			numeroTotalCombinacoes = Matematico.fatorial(numeroLinhas * numeroColunas * numeroAltura);
			tInicial = new Date().getTime();
			e.algoritmo(vetLinhaColuna, vetCore, vetLinhaColuna.length);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.SimulatedAnnealing:
			ECWM_SimulatedAnnealing sa = new ECWM_SimulatedAnnealing(this, g, comEstimativaDeTempo);
			numeroTotalCombinacoes = sa.numeroTotalCombinacoes();
			tInicial = new Date().getTime();
			sa.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.TabooSearch:
			ECWM_TabooSearch ts = new ECWM_TabooSearch(this, g, comEstimativaDeTempo);
			numeroTotalCombinacoes = ts.numeroTotalCombinacoes();
			tInicial = new Date().getTime();
			ts.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();
			break;
		}
		exibeNoCSalva();
		System.out.println("Execution Time of ECWA (CPU time): " + (tFinal - tInicial) + "ms");
		System.out.println("Maximum Energy Consumption: " + maiorEnergiaConsumidaDeTodosMapeamentos / 1000 + "uJ");
		System.out.println("Energy Consumption Average (all mappings): " + energiaConsumidaTodosMapeamentos
				/ numeroTotalCombinacoes / 1000 + "uJ");
		System.out.println("Minimum Energy Consumption: " + (getEnergiaConsumidaMapeamento() - getEnergiaIdle()) / 1000
				+ "uJ");
		System.out.println("Idle Energy Consumption: " + getEnergiaIdle() / 1000 + "uJ");
		System.out.println("Cycles: " + ciclosOperacao);
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
		System.out.println("Vetor LinhaColuna:");
		System.out.print("\t");
		for (int i = 0; i < vet.length; i++)
			System.out.print("[" + vet[i].getLinha() + ", " + vet[i].getColuna() + "] ");
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
		double energia = 0;

		System.out.println("\nNoC (" + numeroLinhas + "x" + numeroColunas + "x" + numeroAltura + ")");
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					System.out.print("\tRoteador(" + linha + ", " + coluna + ", " + altura + ") ");
					energia = energia + matriz[linha][coluna][altura].exibe();
				}
			}
		}
		System.out.println("\n");
		System.out.println("Energy consumption: " + energia);
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

	public void exibeNoCSalva() {
		System.out.println("\nNoC FINAL(" + numeroLinhas + "x" + numeroColunas + "x" + numeroAltura + ")");
		for (int linha = 0; linha < matSalva.length; linha++) {
			for (int coluna = 0; coluna < matSalva[linha].length; coluna++) {
				for (int altura = 0; altura < matSalva[linha][coluna].length; altura++) {
					if (matSalva[linha][coluna][altura].getEnergiaControleRoteador() <= 0)
						continue;
					System.out.print("\tRoteador(" + linha + ", " + coluna + ", " + altura + ") ");
					matSalva[linha][coluna][altura].exibe();
				}
			}
		}
		System.out.println();
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}

	public void desenhaNoC(Graphics g) {
		int l = ehTopologiaMesh ? 0 : CHANNEL_SIZE_Y;

		for (int linha = 0; linha < numeroLinhas; linha++) {
			int c = ehTopologiaMesh ? 0 : CHANNEL_SIZE_X;

			for (int coluna = 0; coluna < numeroColunas; coluna++) {

				for (int altura = viewZ; altura < viewZ+1; altura++) {
					desenhaCanalLocalIn(g, c, l,
							matriz[linha][coluna][altura].linkEntrada[Router.LOCAL_IN].energiaLink());
					desenhaCanalLocalOut(g, c, l,
							matriz[linha][coluna][altura].linkEntrada[Router.LOCAL_OUT].energiaLink());
					if (!ehTopologiaMesh || coluna != numeroColunas - 1)
						desenhaCanalLeste(g, c, l,
								matriz[linha][coluna][altura].linkEntrada[Router.LESTE].energiaLink());
					if (!ehTopologiaMesh || coluna != 0)
						desenhaCanalOeste(g, c, l,
								matriz[linha][coluna][altura].linkEntrada[Router.OESTE].energiaLink());
					if (!ehTopologiaMesh || linha != 0)
						desenhaCanalNorte(g, c, l,
								matriz[linha][coluna][altura].linkEntrada[Router.NORTE].energiaLink());
					if (!ehTopologiaMesh || linha != numeroLinhas - 1)
						desenhaCanalSul(g, c, l, matriz[linha][coluna][altura].linkEntrada[Router.SUL].energiaLink());
					
					
					if (!ehTopologiaMesh || altura != 0)
						desenhaCanalSuperior(g, c, l,
								matriz[linha][coluna][altura].linkEntrada[Router.SUPERIOR].energiaLink());
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

	public void desenhaRoteador(Graphics g, int c, int l, int linha, int coluna, int altura) {
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
		String id = new String("R[" + linha + ", " + coluna+ ", " + altura  + "]");
		g.drawString(id, Math.round(c + (ROUTER_SIZE_X - id.length() * 6) / 2), l + (ROUTER_SIZE_Y / 3) - 8);
		// Energias
		g.setFont(new Font("Arial", Font.BOLD, 11));

		String energiaBufferRoteador = String.format("Eb %.2f",
				new Double(matriz[linha][coluna][altura].getEnergiaBufferRoteador() / 1000));
		g.drawString(energiaBufferRoteador,
				(int) Math.round(c + (ROUTER_SIZE_X - energiaBufferRoteador.length() * 5.5) / 2), l
						+ (2 * (ROUTER_SIZE_Y / 3)) - 8);

		String energiaControleRoteador = String.format("Es %.2f",
				new Double(matriz[linha][coluna][altura].getEnergiaControleRoteador() / 1000));
		g.drawString(energiaControleRoteador,
				(int) Math.round(c + (ROUTER_SIZE_X - energiaControleRoteador.length() * 5.5) / 2), l + ROUTER_SIZE_Y
						- 8);
	}

	public void desenhaCore(Graphics g, int linha, int coluna, String label) {
		int width = 50;
		int eight = 50;

		Draw.lozengeAndLabel(g, Color.gray, Color.white, linha + TILE_SIZE_X - width / 2, coluna + TILE_SIZE_Y - eight
				/ 2, width, eight, label);
	}

	public void desenhaCanalLocalIn(Graphics g, int c, int l, double value) {
		int x = c + ROUTER_SIZE_X - ARROW_WIDTH * 2 - LOZANGE_WIDTH;
		int y = l + ROUTER_SIZE_Y - LOZANGE_EIGHT;

		Draw.arrow(g, Color.red, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x + CHANNEL_SIZE_X - 6, y + CHANNEL_SIZE_Y - 6,
				x + LOZANGE_WIDTH, y + LOZANGE_EIGHT);
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

		Draw.arrow(g, Color.blue, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x + CHANNEL_SIZE_X, y + CHANNEL_SIZE_Y);
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

	public void desenhaCanalSuperior(Graphics g, int c, int l, double value) {
		int width = ROUTER_SIZE_X/2;
		int heigth = ARROW_FINAL;
		int x = c + ROUTER_SIZE_X - width/2 +10;
		int y = l + ROUTER_SIZE_Y - CHANNEL_SIZE_Y ;
		
		//int x = c;
		//int y = l + ROUTER_SIZE_Y / 3;

		Color qualCor =   WP.getHighLinkColor();
		g.setColor(qualCor);
		g.fillRect(x,  y , width+heigth/2, heigth);
		g.setColor(Color.BLACK);
		g.drawRect(x,  y , width+heigth/2, heigth);
		//Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x - CHANNEL_SIZE_X, y, x, y);
		
		Draw.upperLink(g, Color.blue, x+width, y, heigth);
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

	public void desenhaCanalInferior(Graphics g, int c, int l,  double value) {
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
		
		Draw.downLink(g, Color.blue, x+width, y, heigth);
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
	
	public void desenhaCanalNorte(Graphics g, int c, int l, double value) {
		int x = c + (2 * (ROUTER_SIZE_X / 3));
		int y = l;

		Draw.arrow(g, Color.blue, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x, y - CHANNEL_SIZE_Y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // norte
		{
			String bytes = String.format("F%.2f", new Double(value / 1000));
			int offset = 0;

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

	public void desenhaCanalSul(Graphics g, int c, int l, double value) {
		int x = c + (ROUTER_SIZE_X / 3);
		int y = l + ROUTER_SIZE_Y;

		Draw.arrow(g, Color.red, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x, y + CHANNEL_SIZE_Y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // sul
		{
			String bytes = String.format("%.2fF", new Double(value / 1000));
			int offset = CHANNEL_SIZE_Y;

			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 3;
				else
					offset = offset + 9;
				g.drawString("" + bytes.charAt(k), c + ROUTER_SIZE_X / 3 - ARROW_WIDTH / 2, l + offset);
			}
		}
	}

	public void desenhaCanalLeste(Graphics g, int c, int l, double value) {
		int x = c + ROUTER_SIZE_X;
		int y = l + (2 * (ROUTER_SIZE_Y / 3));

		Draw.arrow(g, Color.red, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x + CHANNEL_SIZE_X, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // leste
		{
			String bytes = String.format("%.2fF", new Double(value / 1000));
			int offset = ROUTER_SIZE_X;

			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				g.drawString("" + bytes.charAt(k), c + offset, l + 2 * (ROUTER_SIZE_Y / 3) + ARROW_WIDTH / 2 + 1);
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 2;
				else
					offset = offset + 6;
			}
		}
	}

	public void desenhaCanalOeste(Graphics g, int c, int l, double value) {
		int x = c;
		int y = l + (ROUTER_SIZE_Y / 3);

		Draw.arrow(g, Color.blue, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x - CHANNEL_SIZE_X, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // oeste
		{
			String bytes = String.format("F%.2f", new Double(value / 1000));
			int offset = 0;

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
}