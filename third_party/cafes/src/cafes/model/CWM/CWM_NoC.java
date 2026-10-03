package cafes.model.CWM;

/*
 * Autor: 
 * 		C�sar Marcon
 * Revisor:
 * 		Edson Moreno
 * Objetivo:
 */
import cafes.ui.WindowPrincipal;

import java.io.*;
import java.awt.*;
import java.util.*;

import cafes.common.*;
import cafes.NoC.*;
import cafes.model.*;

public class CWM_NoC implements Serializable {
	private static final long serialVersionUID = -8104568477645963594L;
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
	private transient CWM_WindowNoC WN;
	private transient WindowPrincipal WP;
	private int ciclosLink;
	private int ciclosRoteador;
	private double periodoDoRelogio;
	private boolean ehTopologiaMesh;
	private double energiaBuffer;
	private double energiaControle;
	private double energiaLinkVertical;
	private double energiaLinkHorizontal, energiaLinkLongitudinal;
	private double energiaLocalLink;
	private long ciclosOperacao;
	private long ciclosOperacaoTemporario;
	private long computationTimems;
	public int iteracoes, temperatura;

	protected CWM_VerticeNoC matriz[][][];
	protected CWM_VerticeNoC matSalva[][][];

	private boolean showWindow;
	
	private int viewX=0,viewY=0,viewZ=0;
	public void setView(int x, int y , int z) {
		viewX = x;
		viewY = y;
		viewZ = z;
		
	}

	/*
	 * Objetivo: Construtor da classe. Parametros: WP-> Classe windowPrincipal a
	 * ser utilizada para carga de valores definidos na primeira janela do
	 * programa (e.g. tamanho da noc, consumo de energia para execucao do
	 * controle da noc, da transacao de uma chave para outra) WN-> Classe NoC
	 * que define a liga��o das chaves... numeroLinhas-> n�mero de linhas na
	 * noc. numeroColunas-> n�mero de colunas na noc.
	 */
	public CWM_NoC(WindowPrincipal WP, CWM_WindowNoC WN, int numeroLinhas, int numeroColunas, int numeroAltura, boolean _showWindow) {
		this.numeroLinhas = numeroLinhas;
		this.numeroColunas = numeroColunas;
		this.numeroAltura = numeroAltura;
		this.WP = WP;
		this.WN = WN;
		viewZ = numeroAltura-1;
		iteracoes = -1;
		temperatura = -1;
		showWindow = _showWindow;

		energiaBuffer = WP.getEnergiaBufferPhit() * WP.getBufferSize();
		energiaControle = WP.getEnergiaControlePhit();
		energiaLinkVertical = WP.getEnergiaLinkPhit() * WP.getTileHeigth();
		energiaLinkHorizontal = WP.getEnergiaLinkPhit() * WP.getTileWidth();
		energiaLinkLongitudinal = WP.getEnergiaLinkLongitudinalPhit() * WP.getTileWidth();
		energiaLocalLink = WP.getEnergiaLocalLinkPhit();

		ehTopologiaMesh = WP.ehTopologiaMesh();
		periodoDoRelogio = 1.0 / WP.getClockCycle();
		ciclosLink = WP.getLinkingCycles();
		ciclosRoteador = WP.getRoutingCycles();
		PsNoC = WP.getPotRoteador() * numeroLinhas * numeroColunas * numeroAltura;
		matriz = new CWM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
		matSalva = new CWM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
	}

	public LinhaColunaAltura[] getVetLinhaColuna() {
		return vetLinhaColuna;
	}

	public void setiteracoes(int _nriteracoes) {
		iteracoes = _nriteracoes;
	}

	public void setTemperatura(int _Temperatura) {
		temperatura = _Temperatura;
	}

	/*
	 * Objetivos: retornar o valor de consumo de energia de armazenamento nos
	 * buffers da NoC Parametros: Nao ha...
	 */
	public double getEnergiaBuffer() {
		return energiaBuffer;
	}

	/*
	 * Objetivos: retornar o valor de consumo de energia de execucao do controle
	 * ao acesso ao mecanismo de roteamento e a execucao do roteamento na noc
	 * Parametros: Nao ha...
	 */
	public double getEnergiaControle() {
		return energiaControle;
	}

	/*
	 * Objetivos: retorna o valor de energia consumida para a transmissao de
	 * phits em links verticais. Parametros: Nao ha...
	 */
	public double getEnergiaLinkVertical() {
		return energiaLinkVertical;
	}
	public double getEnergiaLinkLongitudinal(){
		return energiaLinkLongitudinal;
	}
	/*
	 * Objetivos: retorna o valor de energia consumida para a transmissao de
	 * phits em links horizontais. Parametros: Nao ha...
	 */
	public double getEnergiaLinkHorizontal() {
		return energiaLinkHorizontal;
	}

	/*
	 * Objetivos: retorna o valor de energia consumida para a transmissao de
	 * phits em links locais. Parametros: Nao ha...
	 */
	public double getEnergiaLocalLink() {
		return energiaLocalLink;
	}

	/*
	 * Objetivos: retorna o numero de ciclos para a transmissao de um phit por
	 * um link, seja entre roteadores ou de um roteador para um IP/Vertice.
	 * Parametros: Nao ha...
	 */
	public int getLinkingCycles() {
		return ciclosLink;
	}

	/*
	 * Objetivos: retorna o numero de ciclos para executar um roteamento.
	 * Parametros: Nao ha...
	 */
	public int getRoutingCycles() {
		return ciclosRoteador;
	}

	/*
	 * Objetivos: retorna a janela principal na qual esta sendo impresso o
	 * modelo. Parametros: Nao ha...
	 */
	public WindowPrincipal getMainWindow() {
		return WP;
	}

	/*
	 * Objetivos: retorna o numero de linhas da NoC. Parametros: Nao ha...
	 */
	public int getNumeroLinhas() {
		return numeroLinhas;
	}

	/*
	 * Objetivos: retorna o numero de colunas da NoC. Parametros: Nao ha...
	 */
	public int getNumeroColunas() {
		return numeroColunas;
	}
	public int getNumeroAltura() {
		return numeroAltura;
	}

	/*
	 * Objetivos: retorna o valor de energia consumida para a transmissao de
	 * phits em links locais. Parametros: Nao ha...
	 */
	public double getEnergiaIdle() {
		return getEnergiaIdle(ciclosOperacao); // energia em nJ --> 10e-9
	}

	/*
	 * Objetivos: retorna o valor de energia consumida durante a operacao basica
	 * dos roteadores, sem chaveamento. Parametros: ciclos -> numero de ciclos
	 * em que nao houve chaveamento
	 */
	public double getEnergiaIdle(long ciclos) {
		return PsNoC * ciclos * periodoDoRelogio; // energia em nJ --> 10e-9
	}

	/*
	 * Objetivos: Adiciona a um determinado roteador a quantidade de phits que
	 * passam por ali. Parametros: linha -> linha do roteador alvo a ser
	 * incrementado com o nro de phits coluna -> coluna do roteador alvo a ser
	 * incrementado com o nro de phits phits -> nro de phits q passar�o pelo
	 * roteador
	 */
	public void computaEnergiaRoteador(int linha, int coluna, int altura, long phits) {
		matriz[linha][coluna][altura].computaEnergiaRoteador(phits);
	}

	/*
	 * Objetivos: Direciona a quantidade de phits a serem transmitidos para um
	 * determinado link de um determinado roteador da noc. Parametros: linha ->
	 * linha do roteador alvo a ser incrementado com o nro de phits coluna ->
	 * coluna do roteador alvo a ser incrementado com o nro de phits posicao ->
	 * porta por onde serao transmitidos os phits (norte/sul/leste/oeste/local)
	 * phits -> a quantidade de phits a ser transmitida
	 */
	public void computaEnergiaLink(int linha, int coluna, int altura, int posicao, long phits) {
		matriz[linha][coluna][altura].computaEnergiaLink(posicao, phits);
	}

	/*
	 * Objetivos: retorna a energia consumida em algum mapeamento. Ao final a
	 * melhor resposta eh a q tiver o menor valor. Parametros: Nao ha...
	 */
	public double getEnergiaConsumidaMapeamento() {
		return energiaConsumidaMapeamento;
	}

	/*
	 * Objetivos: Define a energia consumida em algum mapeamento. Parametros:
	 * energiaConsumidaMapeamento-> valor da energia consumida.
	 */
	public void setEnergiaConsumidaMapeamento(double energiaConsumidaMapeamento) {
		this.energiaConsumidaMapeamento = energiaConsumidaMapeamento;
	}

	/*
	 * Objetivos: retorna a linha onde um determinado core se encontra
	 * Parametros: core -> nome do core que se estah procurando
	 */
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

	/*
	 * Objetivos: retorna a coluna onde um determinado core se encontra
	 * Parametros: core -> nome do core que se estah procurando
	 */
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

	/*
	 * Objetivos: retorna a energia total consumida na NoC atraves da soma das
	 * energias consumidas em cada roteador. Parametros: Nao ha...
	 */
	public double energiaNoC() {
		double energia = 0;

		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					energia = energia + matriz[linha][coluna][altura].energiaTotalRoteador();
		}
		return energia;
	}

	/*
	 * Objetivos: retorna a energia total consumida na NoC atraves da soma das
	 * energias consumidas em cada roteador. Parametros: Nao ha...
	 */
	public double energiaNoC(CWM_VerticeNoC[][][] _vn) {
		double energia = 0;

		for (int linha = 0; linha < _vn.length; linha++) {
			for (int coluna = 0; coluna < _vn[linha].length; coluna++)
				for (int altura = 0; altura < _vn[linha][coluna].length; altura++)
					energia = energia + _vn[linha][coluna][altura].energiaTotalRoteador();
		}
		return energia;
	}

	/*
	 * Objetivos: Reseta o valor da energia de chaveamento na NoC. Mais
	 * precisamente, reseta o valor de consumo de energia de chaveamento em cada
	 * roteador. Parametros: Nao ha...
	 */
	public void limpaEnergiaDinamica() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matriz[linha][coluna][altura].limpaEnergiaDinamica();
		}
	}

	/*
	 * Objetivos: Guarda uma determinada distribuicao dos IPs/vertices pela NoC
	 * Parametros: Nao ha...
	 */
	public void salvaPosicionamento() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matSalva[linha][coluna][altura] = new CWM_VerticeNoC(matriz[linha][coluna][altura]);
		}
	}

	/*
	 * Objetivos: Cria um grafo que contem os vertices e seus adjacentes baseado
	 * em um grafo inicial, distribuindo-o pela noc. Parametros: g-> grafo dos
	 * vertices e seus adjacentes...
	 */
	public void criaVetorDeCores(CWM_Grafo g) {
		CWM_Vertice l = g.getInicio();
		int posicao = 0;

		vetCore = new String[matSalva.length * matSalva[0].length* matSalva[0][0].length];
		while (l != null) {
			vetCore[posicao] = new String(l.getInf());
			l = l.getProx();
			posicao++;
		}
		while (posicao < vetCore.length)
			vetCore[posicao++] = new String("-");
	}

	/*
	 * Objetivos: Sobreescreve a matriz original com a matriz salva durante um
	 * mapeamento. Parametros: Nao ha...
	 */
	public void copiaMatComMatSalva() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				matriz[linha][coluna] = matSalva[linha][coluna];
		}
	}

	/*
	 * Objetivos: Sobreescreve e matriz salva com a matriz original. Parametros:
	 * Nao ha...
	 */
	public void copiaMatSalvaComMat() {
		for (int linha = 0; linha < matSalva.length; linha++) {
			for (int coluna = 0; coluna < matSalva[linha].length; coluna++)
				matSalva[linha][coluna] = matriz[linha][coluna];
		}
	}

	/*
	 * Objetivos: Associa um par linha coluna com um core/IP/Vertice Parametros:
	 * lc-> linha e coluna da noc a ser associado o core coreElemento-> nome do
	 * core a ser associado
	 */
	public void conectaLinhaColunaComCore(LinhaColunaAltura lc, String coreElemento) {
		matriz[lc.getLinha()][lc.getColuna()][lc.getAltura()] = new CWM_VerticeNoC(coreElemento, lc, this);
	}

	/*
	 * Objetivos: Cria/Inicializa o vetor vetLinhaColuna com o numero de
	 * posicoes da noc. Parametros: Nao ha...
	 */
	public void criaVetorDeLinhaColuna() {
		int k = 0;
		LinhaColunaAltura vetLinColAux[] = new LinhaColunaAltura[matriz.length * matriz[0].length * matriz[0][0].length];

		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (!NocComFalhas.temFalha("T", linha, coluna, altura) && !NocComFalhas.temFalha("L", linha, coluna, altura))
						vetLinColAux[k++] = new LinhaColunaAltura(linha, coluna, altura);
				}
		}
		vetLinhaColuna = new LinhaColunaAltura[k];
		// Reduz o vetor vetLinhaColuna a apenas os tiles que n�o t�m falhas
		System.arraycopy(vetLinColAux, 0, vetLinhaColuna, 0, k);
	}

	/*
	 * Objetivos: Armazena o numero de ciclos para a execucao de uma determinada
	 * operacao em uma variavel temporaria. Parametros: ciclos-> numero de
	 * ciclos da execucao da operacao.
	 */
	public void armazenaCiclosOperacao(long ciclos) {
		ciclosOperacaoTemporario = ciclos;
	}

	/*
	 * Objetivos: Retorna o numero de ciclos da execucao de uma operacao
	 * armazenado na variavel temporaria. Parametros: Nao ha...
	 */
	public long getCiclosOperacaoTemporario() {
		return ciclosOperacaoTemporario;
	}

	/*
	 * Objetivos: Salvar o numero de ciclos armazenados armazenados na variavel
	 * temporaria na variavel final. Parametros: Nao ha...
	 */
	public void salvaCiclosOperacao() {
		ciclosOperacao = ciclosOperacaoTemporario;
	}

	/*
	 * Objetivos: Salva o numero de ciclos de uma determinada operacao na
	 * variavel final. Parametros: ciclos-> nro de ciclos para a execucao da
	 * operacao
	 */
	public void salvaCiclosOperacao(long ciclos) {
		ciclosOperacao = ciclos;
	}

	/*
	 * Objetivos: Executa o calculo de roteamento para a transferencia de phits
	 * da origem CWM_Vertice ao destino CWM_VerticeAdjacente. Jah define sob
	 * qual topologia serah executado o calculo, de acordo com a informacao
	 * definida na primeira tela do programa. Parametros: p-> Origem do phit v->
	 * Destino do phit
	 */
	public void topologia(CWM_Vertice p, CWM_VerticeAdjacente v) {
		if (ehTopologiaMesh) {
			topologiaMesh(p, v);
			return;
		}
		topologiaTorus(p, v);
	}

	/*
	 * Objetivos: Algoritmo de caminhamento dos phits para a topologia torus
	 * Parametros: p-> origem dos phits v-> destino dos phits
	 */
	public void topologiaTorus(CWM_Vertice p, CWM_VerticeAdjacente v) {
		int linhaOrigem, colunaOrigem, linhaDestino, colunaDestino, alturaOrigem, alturaDestino;
		String origem = p.getInf();
		String destino = v.getInf();

		linhaOrigem = DescobreLinha(origem);
		colunaOrigem = DescobreColuna(origem);
		alturaOrigem = DescobreAltura(origem);
		linhaDestino = DescobreLinha(destino);
		colunaDestino = DescobreColuna(destino);
		alturaDestino = DescobreAltura(destino);

		computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, v.getPhits());
		computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
		if (Torus.deveAvancarPonteiro(alturaOrigem, alturaDestino, numeroAltura)) {
			while (alturaOrigem != alturaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, v.getPhits());
				alturaOrigem = Torus.incrementaPonteiro(alturaOrigem, numeroAltura);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
			}
		} else {
			while (alturaOrigem != alturaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, v.getPhits());
				alturaOrigem = Torus.decrementaPonteiro(alturaOrigem, numeroColunas);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
			}
		}

		if (Torus.deveAvancarPonteiro(colunaOrigem, colunaDestino, numeroColunas)) {
			while (colunaOrigem != colunaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, v.getPhits());
				colunaOrigem = Torus.incrementaPonteiro(colunaOrigem, numeroColunas);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
			}
		} else {
			while (colunaOrigem != colunaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, v.getPhits());
				colunaOrigem = Torus.decrementaPonteiro(colunaOrigem, numeroColunas);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
			}
		}
		if (Torus.deveAvancarPonteiro(linhaOrigem, linhaDestino, numeroLinhas)) {
			while (linhaOrigem != linhaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, v.getPhits());
				linhaOrigem = Torus.incrementaPonteiro(linhaOrigem, numeroLinhas);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
			}
		} else {
			while (linhaOrigem != linhaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, v.getPhits());
				linhaOrigem = Torus.decrementaPonteiro(linhaOrigem, numeroLinhas);
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
			}
		}
		computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_OUT, v.getPhits());
	}

	/*
	 * Objetivos: Algoritmo de caminhamento dos phits para a topologia mesh
	 * Parametros: p-> origem dos phits v-> destino dos phits
	 */
	public void topologiaMesh(CWM_Vertice p, CWM_VerticeAdjacente v) {
		int linhaOrigem, colunaOrigem, linhaDestino, colunaDestino, alturaDestino, alturaOrigem;
		String origem = p.getInf();
		String destino = v.getInf();

		linhaOrigem = DescobreLinha(origem);
		colunaOrigem = DescobreColuna(origem);
		alturaOrigem = DescobreAltura(origem);
		linhaDestino = DescobreLinha(destino);
		colunaDestino = DescobreColuna(destino);
		alturaDestino = DescobreAltura(destino);

		double llvalue = 0, vlvalue = 0, hlvalue = 0, rvalue = 0, longlvalue = 0;
		System.out.println();
		System.out.print(origem + "(" + linhaOrigem + "," + colunaOrigem + "," + alturaOrigem + ") -> " + destino + "(" + linhaDestino
				+ "," + colunaDestino + "," + alturaDestino + ") - phits(" + v.getPhits() + "): ");

		computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, v.getPhits());
		computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
		llvalue += (v.getPhits() * energiaLocalLink);
		rvalue += (v.getPhits() * (energiaBuffer + energiaControle));

		while (alturaOrigem != alturaDestino) {
			if (alturaOrigem > alturaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, v.getPhits());
				alturaOrigem--;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
				longlvalue += (v.getPhits() * energiaLinkLongitudinal);
				rvalue += (v.getPhits() * (energiaBuffer + energiaControle));
			} else {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, v.getPhits());
				alturaOrigem++;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
				longlvalue += (v.getPhits() * energiaLinkLongitudinal);
				rvalue += (v.getPhits() * (energiaBuffer + energiaControle));
			}
		}

		while (colunaOrigem != colunaDestino) {
			if (colunaOrigem > colunaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, v.getPhits());
				colunaOrigem--;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
				hlvalue += (v.getPhits() * energiaLinkHorizontal);
				rvalue += (v.getPhits() * (energiaBuffer + energiaControle));
			} else {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, v.getPhits());
				colunaOrigem++;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
				hlvalue += (v.getPhits() * energiaLinkHorizontal);
				rvalue += (v.getPhits() * (energiaBuffer + energiaControle));
			}
		}
		while (linhaOrigem != linhaDestino) // Algoritmo XY
		{
			if (linhaOrigem > linhaDestino) {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, v.getPhits());
				linhaOrigem--;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
				vlvalue += (v.getPhits() * energiaLinkVertical);
				rvalue += (v.getPhits() * (energiaBuffer + energiaControle));
			} else {
				computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, v.getPhits());
				linhaOrigem++;
				computaEnergiaRoteador(linhaOrigem, colunaOrigem, alturaOrigem, v.getPhits());
				vlvalue += (v.getPhits() * energiaLinkVertical);
				rvalue += (v.getPhits() * (energiaBuffer + energiaControle));
			}
		}
		computaEnergiaLink(linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_OUT, v.getPhits());
		llvalue += (v.getPhits() * energiaLocalLink);
		System.out.println(llvalue + vlvalue + hlvalue + rvalue + longlvalue);
		System.out.println("  llink:" + llvalue + ", hlink:" + hlvalue + ", vlink:" + vlvalue + ", routing:" + rvalue + ", longLink:"
				+ longlvalue);
		System.out.println("  ell:" + energiaLocalLink + ", evl:" + energiaLinkVertical + ", ehl:" + energiaLinkHorizontal + ", ecr:"
				+ energiaControle + ", ebr:" + energiaBuffer);
	}

	/*
	 * Objetivos: Executa o calculo CWM para todo o grafo definido de vertices e
	 * seus adjacentes. Retorna o total de energia consumida. Parametros: g->
	 * grafo de vertices e seus adjacentes comEstimativaDeTempo-> solicita ou
	 * n�o a execucao de estimativa de tempo.
	 */
	public double computa(CWM_Grafo g, boolean comEstimativaDeTempo) {
		double energiaConsumida;

		limpaEnergiaDinamica();
		if (comEstimativaDeTempo) {
			g.executaCWM(this, ehTopologiaMesh);
			energiaConsumida = energiaNoC() + getEnergiaIdle(ciclosOperacaoTemporario);
		} else {
			g.executaCWM(this);
			energiaConsumida = energiaNoC();
		}
		exibeAndamento();
		energiaConsumidaTodosMapeamentos = energiaConsumidaTodosMapeamentos + energiaConsumida;
		if (maiorEnergiaConsumidaDeTodosMapeamentos < energiaConsumida)
			maiorEnergiaConsumidaDeTodosMapeamentos = energiaConsumida;
		return energiaConsumida;
	}

	/*
	 * Objetivos: Executa o calculo CWM para todo o grafo definido de vertices e
	 * seus adjacentes. Retorna o total de energia consumida. Parametros: g->
	 * grafo de vertices e seus adjacentes comEstimativaDeTempo-> solicita ou
	 * n�o a execucao de estimativa de tempo.
	 */
	public double computa(CWM_VerticeNoC[][][] _vn) {
		double energiaConsumida;

		for (int linha = 0; linha < _vn.length; linha++) {
			for (int coluna = 0; coluna < _vn[linha].length; coluna++)
				for (int altura = 0; coluna < _vn[linha][coluna].length; altura++)
				_vn[linha][coluna][altura].limpaEnergiaDinamica();
		}
		energiaConsumida = energiaNoC();

		return energiaConsumida;
	}

	/*
	 * Objetivos: Destina o grafo para os diferentes algoritmos de
	 * posicionamento (simulated annealing/exaustivo/taboo). Adicionalmente
	 * solicita a estimativa de tempo ou n�o. Parametros: g-> grafo de vertices
	 * e seus adjacentes algo-> algoritmo de posicionamento a ser executado
	 * comEstimativaDeTempo-> solicitacao ou nao da realizacao do calculo de
	 * estimativa de tempo.
	 */
	public void algoritmoPosicionamento(CWM_Grafo g, int algo, boolean comEstimativaDeTempo) {
		computationTimems = 0;
		primeiraVez = true;
		numeroCombinacoes = 0;
		energiaConsumidaTodosMapeamentos = 0;
		maiorEnergiaConsumidaDeTodosMapeamentos = 0;
		energiaConsumidaMapeamento = Double.MAX_VALUE;
		criaVetorDeCores(g);
		criaVetorDeLinhaColuna();
		if (showWindow) {
			exibeVetLinhaColuna(vetLinhaColuna);
			exibeVetString(vetCore);
		}
		long tInicial = 0, tFinal = 0; // Para avaliar o tempo de CPU para
										// executar o algoritmo
		switch (algo) {
		case Algoritmo.ExhaustiveSearch:
			CWM_Exaustivo e = new CWM_Exaustivo(this, g, comEstimativaDeTempo);
			numeroTotalCombinacoes = Matematico.fatorial(numeroLinhas * numeroColunas);
			tInicial = new Date().getTime();
			e.algoritmo(vetLinhaColuna, vetCore, vetLinhaColuna.length);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.SimulatedAnnealing:
			CWM_SimulatedAnnealing sa = new CWM_SimulatedAnnealing(this, g, temperatura, iteracoes, comEstimativaDeTempo, false);
			numeroTotalCombinacoes = sa.numeroTotalCombinacoes();
			tInicial = new Date().getTime();
			sa.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.TabooSearch:
			CWM_TabooSearch ts = new CWM_TabooSearch(this, g, temperatura, iteracoes, comEstimativaDeTempo, false);
			numeroTotalCombinacoes = ts.numeroTotalCombinacoes();
			tInicial = new Date().getTime();
			ts.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.HeuristicSearch:
			CWM_HeuristicSearch hs = new CWM_HeuristicSearch(this, g, comEstimativaDeTempo);
			tInicial = new Date().getTime();
			hs.algoritmo();
			tFinal = new Date().getTime();
			break;

		case Algoritmo.HeuristicSearch_SA:
			CWM_HeuristicSearch hs2 = new CWM_HeuristicSearch(this, g, comEstimativaDeTempo);
			CWM_SimulatedAnnealing sa2 = new CWM_SimulatedAnnealing(this, g, 1000, 1, comEstimativaDeTempo, true);
			tInicial = new Date().getTime();
			hs2.algoritmo();
			numeroTotalCombinacoes = hs2.numeroTotalCombinacoes() + sa2.numeroTotalCombinacoes();
			sa2.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.HeuristicSearch_Taboo:
			CWM_HeuristicSearch hs3 = new CWM_HeuristicSearch(this, g, comEstimativaDeTempo);
			CWM_TabooSearch ts3 = new CWM_TabooSearch(this, g, temperatura, iteracoes, comEstimativaDeTempo, true);
			tInicial = new Date().getTime();
			numeroTotalCombinacoes = ts3.numeroTotalCombinacoes() + hs3.numeroTotalCombinacoes();
			hs3.algoritmo();
			ts3.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.BadMappingSearch:
			CWM_BadMappingSearch bm = new CWM_BadMappingSearch(this, g, temperatura, iteracoes, comEstimativaDeTempo, false);
			numeroTotalCombinacoes = bm.numeroTotalCombinacoes();
			tInicial = new Date().getTime();
			bm.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.ManualSearch:
			CWM_HeuristicSearch hms2 = new CWM_HeuristicSearch(this, g, comEstimativaDeTempo);
			CWM_HeuristicTwoSearch h2ms2 = new CWM_HeuristicTwoSearch(this, g, comEstimativaDeTempo, true);
			CWM_SimulatedAnnealing sam2 = new CWM_SimulatedAnnealing(this, g, 1000, 1, comEstimativaDeTempo, true);
			tInicial = new Date().getTime();
			numeroTotalCombinacoes = hms2.numeroTotalCombinacoes() + h2ms2.numeroTotalCombinacoes() + sam2.numeroTotalCombinacoes();
			hms2.algoritmo();
			h2ms2.algoritmo(vetLinhaColuna, vetCore);
			sam2.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();

			/*
			 * CWM_ManualSearch ms = new CWM_ManualSearch(this, g,
			 * comEstimativaDeTempo); numeroTotalCombinacoes = 1; tInicial = new
			 * Date().getTime(); ms.algoritmo(); tFinal = new Date().getTime();
			 */
			break;

		case Algoritmo.HeuristicTwoSearch:
			CWM_HeuristicSearch h1ms1 = new CWM_HeuristicSearch(this, g, comEstimativaDeTempo);
			CWM_HeuristicTwoSearch h2s = new CWM_HeuristicTwoSearch(this, g, comEstimativaDeTempo, true);
			numeroTotalCombinacoes = h2s.numeroTotalCombinacoes() + h1ms1.numeroTotalCombinacoes();
			tInicial = new Date().getTime();
			h1ms1.algoritmo();
			h2s.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();
			break;

		case Algoritmo.HeuristicThreeSearch:
			CWM_HeuristicSearch h1ms2 = new CWM_HeuristicSearch(this, g, comEstimativaDeTempo);
			CWM_HeuristicThreeSearch h3ms = new CWM_HeuristicThreeSearch(this, g, comEstimativaDeTempo, true);
			tInicial = new Date().getTime();
			numeroTotalCombinacoes = h3ms.numeroTotalCombinacoes() + h1ms2.numeroTotalCombinacoes();
			h1ms2.algoritmo();
			h3ms.algoritmo(vetLinhaColuna, vetCore);
			tFinal = new Date().getTime();
			break;

		}
		computationTimems = (tFinal - tInicial);
		if (showWindow) {
			exibeNoCSalva();
			System.out.println("Execution Time of CWA (CPU time): " + (tFinal - tInicial) + "ms");
			System.out.println("Maximum Energy Consumption: " + maiorEnergiaConsumidaDeTodosMapeamentos / 1000 + "uJ");
			System.out.println("Energy Consumption Average (all mappings): " + energiaConsumidaTodosMapeamentos / numeroTotalCombinacoes
					/ 1000 + "uJ");
			System.out.println("Minimum Energy Consumption: " + (getEnergiaConsumidaMapeamento() - getEnergiaIdle()) / 1000 + "uJ");
			System.out.println("Idle Energy Consumption: " + getEnergiaIdle() / 1000 + "uJ");
			System.out.println("Cycles: " + ciclosOperacao);
		}
	}

	/*
	 * Objetivos: Representar a evolucao do calculo de posicionamento dos IPs.
	 * Parametros: Nao ha...
	 */
	public long getComputationTime() {
		return computationTimems;
	}

	/*
	 * Objetivos: Representar a evolucao do calculo de posicionamento dos IPs.
	 * Parametros: Nao ha...
	 */
	public void exibeAndamento() {
		if (!showWindow)
			return;
		if (primeiraVez) {
			plAnt = 0;
			primeiraVez = false;
			System.out.println("    1    2    3    4    5    6    7    8    9   10");
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

	/*
	 * Objetivos: Imprime textualmente as linhas e colunas associadas a cada
	 * posicao de um determinado vetor de linhas e colunas. Parametros: vet[]->
	 * vetor de linhas e colunas
	 */
	public void exibeVetLinhaColuna(LinhaColunaAltura vet[]) {
		System.out.println("Vetor LinhaColunaAltura:");
		System.out.print("\t");
		for (int i = 0; i < vet.length; i++)
			System.out.print("[" + vet[i].getLinha() + ", " + vet[i].getColuna() + ", " + vet[i].getAltura() + "] ");
		System.out.println("\n---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}

	/*
	 * Objetivos: Imprime textualmente os cores/vertices/IPs associadas a cada
	 * posicao de um determinado vetor de nome de cores/vertices/IPs.
	 * Parametros: vet[]-> vetor de cores
	 */
	public void exibeVetString(String vet[]) {
		System.out.println("Vetor de cores:");
		System.out.print("\t");
		for (int i = 0; i < vet.length; i++)
			System.out.print(vet[i] + " ");
		System.out.println("\n---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}

	/*
	 * Objetivos: Dada a matriz de distribuicao dos IPs/Vertices/cores pela noc,
	 * a matriz, ou seja, os cores associados a cada posicao, eh impresso na
	 * tela. Parametros: Nao ha...
	 */
	public void exibeNoCResumido() {
		System.out.println("-------------");
		System.out.println(contador++);
		for (int linha = 0; linha < matriz.length; linha++) {
			System.out.print("\t");
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					System.out.print("[" + matriz[linha][coluna][altura].getCoreName() + "] ");
					System.out.println();
				}

		}
		if (contador == 1560)
			System.out.println();
	}

	/*
	 * Objetivos: Apresenta a linha e coluna de cada roteador e no final a
	 * energia consumida na NoC composta por estes roteadores. Parametros: Nao
	 * ha...
	 */
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

	/*
	 * Objetivos: Imprime o nome de cada core/vertice/IP distribuido na matriz
	 * salva. Parametros: Nao ha...
	 */
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

	/*
	 * Objetivos: Imprimir o nome/posicao de cada um dos roteadores que
	 * executaram roteamento em algum momento. Parametros: Nao ha...
	 */
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

	/*
	 * Objetivos: Desenha a NoC na tela. Diferenciando para os casos onde a noc
	 * eh mesh ou torus. Parametros: g-> grafico onde serao realizados os
	 * desenhos.
	 */
	public void desenhaNoC(Graphics g) {
		int l = ehTopologiaMesh ? 0 : CHANNEL_SIZE_Y;

		for (int linha = 0; linha < numeroLinhas; linha++) {
			int c = ehTopologiaMesh ? 0 : CHANNEL_SIZE_X;

			for (int coluna = 0; coluna < numeroColunas; coluna++) {
				for (int altura = viewZ; altura < viewZ+1; altura++) {
					CWM_VerticeNoC verticeNoC = matriz[linha][coluna][altura];

					if (!verticeNoC.getCoreName().equals("-")) {
						desenhaCanalLocalIn(g, c, l, linha, coluna, altura, verticeNoC.linkEntrada[Router.LOCAL_IN].energiaLink());
						desenhaCanalLocalOut(g, c, l, linha, coluna, altura, verticeNoC.linkEntrada[Router.LOCAL_OUT].energiaLink());
					}
					if (!ehTopologiaMesh || coluna != numeroColunas - 1)
						desenhaCanalLeste(g, c, l, linha, coluna, altura, verticeNoC.linkEntrada[Router.LESTE].energiaLink());
					if (!ehTopologiaMesh || coluna != 0)
						desenhaCanalOeste(g, c, l, linha, coluna, altura, verticeNoC.linkEntrada[Router.OESTE].energiaLink());
					if (!ehTopologiaMesh || linha != 0)
						desenhaCanalNorte(g, c, l, linha, coluna, altura, verticeNoC.linkEntrada[Router.NORTE].energiaLink());
					if (!ehTopologiaMesh || linha != numeroLinhas - 1)
						desenhaCanalSul(g, c, l, linha, coluna, altura, verticeNoC.linkEntrada[Router.SUL].energiaLink());
					
					if (!ehTopologiaMesh || altura != 0)
						desenhaCanalSuperior(g, c, l, linha, coluna, altura, verticeNoC.linkEntrada[Router.SUPERIOR].energiaLink());
					if (!ehTopologiaMesh || altura != numeroAltura- 1)
						desenhaCanalInferior(g, c, l, linha, coluna, altura, verticeNoC.linkEntrada[Router.INFERIOR].energiaLink());
					
					desenhaRoteador(g, c, l, linha, coluna, altura);
					if (!verticeNoC.getCoreName().equals("-"))
						desenhaCore(g, c, l, verticeNoC.getCoreName());
				}
				c = c + TILE_SIZE_X;
			}
			l = l + TILE_SIZE_Y;
		}
	}

	/*
	 * Objetivos: Desenhar o roteador e seus canais de comunicacao, com excecao
	 * do canal local. Parametros: g-> grafico onde sera realizado o desenho c->
	 * coluna no grafico onde serah desenhado o roteador l-> linha no grafico
	 * onde serah desenhado o roteador linha-> linha da matriz correspondente a
	 * posicao da noc coluna-> coluna da matriz correspondente a posicao da noc
	 */
	public void desenhaRoteador(Graphics g, int c, int l, int linha, int coluna,int altura) {
		String coreName = matriz[linha][coluna][altura].getCoreName();

		// Retangulo
		Color qualCor = coreName.equals("-") ? Color.blue : Color.black;
		if (NocComFalhas.temFalha("T", linha, coluna,altura))
			g.setColor(Color.red);
		else
			g.setColor(qualCor);
		g.fillRect(c, l, ROUTER_SIZE_X, ROUTER_SIZE_Y);
		// T�tulo
		g.setFont(new Font("Arial", Font.BOLD, 12));
		g.setColor(Color.white);
		String id = new String("R[" + linha + ", " + coluna+", " + altura + "]");
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

	/*
	 * Objetivos: Desenhar o core/vertice/IP na posicao da NoC. Parametros: g->
	 * grafico onde serah realizado o desenho linha-> linha correspondete a
	 * posicao da noc no grafico coluna-> coluna correspondente a posicao da noc
	 * no grafico label-> label a ser impresso no vertice/core/IP
	 */
	public void desenhaCore(Graphics g, int linha, int coluna, String label) {
		int width = 50;
		int eight = 50;

		Draw.lozengeAndLabel(g, Color.gray, Color.white, linha + TILE_SIZE_X - width / 2, coluna + TILE_SIZE_Y - eight / 2, width, eight,
				label);
	}

	/*
	 * Objetivos: Desenha o canal de conexao local de entrada no roteador entre
	 * o roteador e o IP/core/vertice. Parametros: g-> grafico a ser desenhado
	 * c-> coluna do grafico a ser desenhado o canal l-> linha do grafico a ser
	 * desenhado o canal value-> valor a ser impresso no canal
	 */
	public void desenhaCanalLocalIn(Graphics g, int c, int l, int linha, int coluna,int altura, double value) {
		int x = c + ROUTER_SIZE_X - ARROW_WIDTH * 2 - LOZANGE_WIDTH;
		int y = l + ROUTER_SIZE_Y - LOZANGE_EIGHT;

		Color qualCor = NocComFalhas.temFalha("L", linha, coluna,altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x + CHANNEL_SIZE_X - 6, y + CHANNEL_SIZE_Y - 6, x + LOZANGE_WIDTH, y
				+ LOZANGE_EIGHT);
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

	/*
	 * Objetivos: Desenha o canal de conexao local de sa�da do roteador entre o
	 * roteador e o IP/core/vertice. Parametros: g-> grafico a ser desenhado c->
	 * coluna do grafico a ser desenhado o canal l-> linha do grafico a ser
	 * desenhado o canal value-> valor a ser impresso no canal
	 */
	public void desenhaCanalLocalOut(Graphics g, int c, int l, int linha, int coluna,int altura, double value) {
		int x = c + ROUTER_SIZE_X - 6 - LOZANGE_WIDTH;
		int y = l + ROUTER_SIZE_Y - ARROW_WIDTH * 2 - 6 - LOZANGE_EIGHT;

		Color qualCor = NocComFalhas.temFalha("L", linha, coluna,altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x + CHANNEL_SIZE_X, y + CHANNEL_SIZE_Y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // leste
		{
			String bytes = String.format("%.2fF", new Double(value / 1000));
			int offsetX = c + ROUTER_SIZE_X  +ARROW_FINAL;
			int offsetY = l + ROUTER_SIZE_Y - 8+ARROW_FINAL;;
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

	/*
	 * Objetivos: Desenha o canal de conexao Norte de sa�da do roteador.
	 * Parametros: g-> grafico a ser desenhado c-> coluna do grafico a ser
	 * desenhado o canal l-> linha do grafico a ser desenhado o canal value->
	 * valor a ser impresso no canal
	 */
	public void desenhaCanalNorte(Graphics g, int c, int l, int linha, int coluna, int altura,double value) {
		int x = c + (2 * (ROUTER_SIZE_X / 3));
		int y = l;

		Color qualCor = NocComFalhas.temFalha("N", linha, coluna,altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x, y - CHANNEL_SIZE_Y);
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

	/*
	 * Objetivos: Desenha o canal de conexao Sul de sa�da do roteador.
	 * Parametros: g-> grafico a ser desenhado c-> coluna do grafico a ser
	 * desenhado o canal l-> linha do grafico a ser desenhado o canal value->
	 * valor a ser impresso no canal
	 */
	public void desenhaCanalSul(Graphics g, int c, int l, int linha, int coluna, int altura,double value) {
		int x = c + (ROUTER_SIZE_X / 3);
		int y = l + ROUTER_SIZE_Y;

		Color qualCor = NocComFalhas.temFalha("S", linha, coluna,altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x, y + CHANNEL_SIZE_Y);
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
	
	public void desenhaCanalSuperior(Graphics g, int c, int l, int linha, int coluna, int altura, double value) {
		int width = ROUTER_SIZE_X/2;
		int heigth = ARROW_FINAL;
		int x = c + ROUTER_SIZE_X - width/2 +10;
		int y = l + ROUTER_SIZE_Y - CHANNEL_SIZE_Y ;
		
		//int x = c;
		//int y = l + ROUTER_SIZE_Y / 3;

		Color qualCor = NocComFalhas.temFalha("U", linha, coluna, altura) ? Color.red : WP.getHighLinkColor();
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

	public void desenhaCanalInferior(Graphics g, int c, int l,  int linha, int coluna, int altura, double value) {
		int width = ROUTER_SIZE_X/2;
		int heigth = ARROW_FINAL;
		int x = c + ROUTER_SIZE_X - width/2 +10;
		int y = l + ROUTER_SIZE_Y - heigth-1;

		Color qualCor = NocComFalhas.temFalha("I", linha, coluna, altura) ? Color.red :  WP.getHighLinkColor();
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

	/*
	 * Objetivos: Desenha o canal de conexao Leste de sa�da do roteador.
	 * Parametros: g-> grafico a ser desenhado c-> coluna do grafico a ser
	 * desenhado o canal l-> linha do grafico a ser desenhado o canal value->
	 * valor a ser impresso no canal
	 */
	public void desenhaCanalLeste(Graphics g, int c, int l, int linha, int coluna,int altura,  double value) {
		int x = c + ROUTER_SIZE_X;
		int y = l + (2 * (ROUTER_SIZE_Y / 3));

		Color qualCor = NocComFalhas.temFalha("E", linha, coluna,altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x + CHANNEL_SIZE_X, y);
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

	/*
	 * Objetivos: Desenha o canal de conexao oeste de sa�da do roteador.
	 * Parametros: g-> grafico a ser desenhado c-> coluna do grafico a ser
	 * desenhado o canal l-> linha do grafico a ser desenhado o canal value->
	 * valor a ser impresso no canal
	 */
	public void desenhaCanalOeste(Graphics g, int c, int l, int linha, int coluna,int altura, double value) {
		int x = c;
		int y = l + (ROUTER_SIZE_Y / 3);

		Color qualCor = NocComFalhas.temFalha("W", linha, coluna,altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x - CHANNEL_SIZE_X, y);
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
