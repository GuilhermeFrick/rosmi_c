package cafes.model.CDCM;

import cafes.ui.WindowPrincipal;

import java.io.*;
import java.util.Date;
import java.awt.*;
import java.awt.geom.Rectangle2D;

import cafes.common.*;
import cafes.NoC.*;
import cafes.model.*;

class CDCM_NoC implements Serializable {
	private static final long			serialVersionUID	= 760712986697083479L;
	public static final int				ROUTER_SIZE_X		= 75;
	public static final int				ROUTER_SIZE_Y		= 75;
	public static final int				ROUTER_SIZE_Z		= 75;
	public static final int				CHANNEL_SIZE_X		= 75;
	public static final int				CHANNEL_SIZE_Y		= 75;
	public static final int				CHANNEL_SIZE_Z		= 75;
	public static final int				TILE_SIZE_X			= ROUTER_SIZE_X + CHANNEL_SIZE_X;
	public static final int				TILE_SIZE_Y			= ROUTER_SIZE_Y + CHANNEL_SIZE_Y;
	public static final int				TILE_SIZE_Z			= ROUTER_SIZE_Z + CHANNEL_SIZE_Z;
	public static final int				ARROW_WIDTH			= 7;
	public static final int				ARROW_FINAL			= 14;
	public static final int				LOZANGE_WIDTH		= 25;
	public static final int				LOZANGE_EIGHT		= 25;
	public static String				precisionShowed		= "%.3f";

	private int							plAnt;
	private int							contador			= 0;
	private double						PsNoC;
	private LinhaColunaAltura			vetLinhaColuna[];
	private LinhaColunaAltura			vetLinColEsperas[];
	private LinhaColunaAltura			vetLinColFalhas[];
	private String						vetCore[];
	private boolean						primeiraVez;
	private long						falhasDeRoteamento;
	private long						numeroCombinacoes, numeroTotalCombinacoes;
	private int							numeroLinhas, numeroColunas, numeroAltura;
	private double						energiaConsumidaMapeamento;
	private transient WindowPrincipal	WP;
	private transient CDCM_WindowNoC	WN;
	private long						numeroCiclosTodosMapeamentos;
	private long						optimumMappingCicle;
	private long						menorNumeroCiclosDeTodosMapeamentos;
	private long						maiorNumeroCiclosDeTodosMapeamentos;
	private long						ciclo;													// N�mero
																								// de
																								// ciclos
																								// da
																								// aplica��o
	private long						ciclosLink;
	private long						ciclosRoteador;
	private double						periodoDoRelogio;
	private boolean						ehTopologiaMesh;
	private double						energiaBuffer;
	private double						bufferSize;
	private double						energiaControle;
	private double						energiaLinkVertical;
	private double						energiaLinkHorizontal;
	private double 						energiaLinkLongitudinal;
	private double						energiaLocalLink;

	protected CDCM_VerticeNoC			matriz[][][];
	protected CDCM_VerticeNoC			matrizCustoMinimo[][][];
	protected CDCM_VerticeNoC			matrizMinimoTempoExecucao[][][];
	protected CDCM_VerticeNoC			matrizMaximoTempoExecucao[][][];
	private int viewX=0,viewY=0,viewZ=0;
	public void setView(int x, int y , int z) {
		viewX = x;
		viewY = y;
		viewZ = z;
		
	}
	public LinhaColunaAltura[] getVetLinhaColuna() {
		return vetLinhaColuna;
	}

	public CDCM_NoC(WindowPrincipal WP, CDCM_WindowNoC WN, int numeroLinhas, int numeroColunas, int numeroAltura) {
		this.numeroLinhas = numeroLinhas;
		this.numeroColunas = numeroColunas;
		this.numeroAltura = numeroAltura;
		viewZ = numeroAltura-1;
		this.WP = WP;
		this.WN = WN;
		matriz = new CDCM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
		matrizCustoMinimo = new CDCM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
		matrizMinimoTempoExecucao = new CDCM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
		matrizMaximoTempoExecucao = new CDCM_VerticeNoC[numeroLinhas][numeroColunas][numeroAltura];
		PsNoC = WP.getPotRoteador() * numeroLinhas * numeroColunas * numeroAltura;
		ciclo = 0;
		energiaControle = WP.getEnergiaControlePhit();
		energiaBuffer = WP.getEnergiaBufferPhit();
		bufferSize = WP.getBufferSize();
		energiaLinkVertical = WP.getEnergiaLinkPhit() * WP.getTileHeigth();
		energiaLinkHorizontal = WP.getEnergiaLinkPhit() * WP.getTileWidth();
		energiaLinkLongitudinal = WP.getEnergiaLinkLongitudinalPhit() * WP.getTileLongitudinal();
		energiaLocalLink = WP.getEnergiaLocalLinkPhit();
		ehTopologiaMesh = WP.ehTopologiaMesh();
		periodoDoRelogio = 1.0 / WP.getClockCycle();
		ciclosLink = WP.getLinkingCycles();
		ciclosRoteador = WP.getRoutingCycles();
	}

	public void incRoutingFail() {
		falhasDeRoteamento++;
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

	public long getCiclo() {
		return this.ciclo;
	}

	public double getTempoExecucao() {
		return ciclo * periodoDoRelogio; // tempo em micro segundos
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

	// A id�ia � verifcar se a mensagem que vai ocupar o recurso ir� usar o
	// recurso ao mesmo tempo que uma outra
	// mensagem. Caso for, a id�ia � tomar as seguintes decis�es:
	// 1) Se mensagem atual iniciar ao mesmo tempo ou depois que a j� gravada,
	// apenas posterga a mensagem atual.
	// Ou seja, a mensagem ficaria contida no recurso pelo per�odo que a outra
	// mensagem est� ocupando o mesmo.
	// 2) Se mensagem atual come�ar antes de outra mensagem, ent�o divide a
	// mensagem atual. Ou seja, parte da
	// mensagem ocupar� o recurso antes da outra mensagem e o restante ocupar�
	// ap�s a outra mensagem. Este n�o
	// � o caso pr�tico, mas gera uma aproxima��o, pois o canal ficar� ocupado
	// exatamente o tempo que seria se
	// as mensagens estivessem cont�guas e fica muito mais f�cil de tratar.
	// Assim, � necess�rio armazenar todas as mensagens em todos os recursos de
	// comunica��o para ter o tempo que estas
	// ocuparam os recursos
	public long computaLocalLink(CDCM_Vertice p, long cicloInicial, int linha, int coluna, int altura, int posicao, long phits) {
		if (matriz[linha][coluna][altura] == null)
			throw new RuntimeException("Posi��o [" + linha + "][" + coluna  + "][" + altura+ "] n�o alocada!");
		// Retorna o ciclo final considerando todos os phits do pacote
		cicloInicial = matriz[linha][coluna][altura].computaLink(p, cicloInicial, ciclosLink, posicao, phits);
		p.setCicloFinalLocal(cicloInicial + ciclosLink * phits - 1);
		return cicloInicial;
	}

	public long computaLink(CDCM_Vertice p, long cicloInicial, int linha, int coluna, int altura, int posicao, long phits) {
		if (matriz[linha][coluna][altura] == null)
			throw new RuntimeException("Posi��o [" + linha + "][" + coluna + "][" + altura + "] n�o alocada!");
		// Retorna o ciclo final considerando todos os phits do pacote
		return matriz[linha][coluna][altura].computaLink(p, cicloInicial, ciclosLink, posicao, phits);
	}

	public long computaRoteador(CDCM_Vertice p, long cicloInicial, int linha, int coluna, int altura, int posicao, long phits) {
		if (matriz[linha][coluna][altura] == null)
			throw new RuntimeException("Posi��o [" + linha + "][" + coluna + "][" + altura + "] n�o alocada!");
		return matriz[linha][coluna][altura].computaRoteador(p, cicloInicial, ciclosRoteador, posicao, phits);
	}

	public long topologia(CDCM_Vertice p, String coreOrigem, String coreDestino, int linhaOrigem, int colunaOrigem, int alturaOrigem, int linhaDestino, int colunaDestino, int alturaDestino) {
		if (ehTopologiaMesh)
			return topologiaMesh(p, coreOrigem, coreDestino, linhaOrigem, colunaOrigem, alturaOrigem, linhaDestino, colunaDestino, alturaDestino);
		return topologiaTorus(p, coreOrigem, coreDestino, linhaOrigem, colunaOrigem, alturaOrigem, linhaDestino, colunaDestino, alturaDestino);
	}

	// Realiza o caminhamento XY, contabilizando os phits para cada aresta
	public long topologiaTorus(CDCM_Vertice p, String coreOrigem, String coreDestino, int linhaOrigem, int colunaOrigem, int alturaOrigem, int linhaDestino, int colunaDestino, int alturaDestino) {
		long phits = p.getPhits();
		long cicloInicial = computaLocalLink(p, p.getCicloInicial(), linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);

		cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);
		if (Torus.deveAvancarPonteiro(colunaOrigem, colunaDestino, numeroColunas)) {
			while (colunaOrigem != colunaDestino) {
				colunaOrigem = Torus.incrementaPonteiro(colunaOrigem, numeroColunas);
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
			}
		} else {
			while (colunaOrigem != colunaDestino) // Execu��o pelo link externo
			{
				colunaOrigem = Torus.decrementaPonteiro(colunaOrigem, numeroColunas);
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
			}
		}
		if (Torus.deveAvancarPonteiro(linhaOrigem, linhaDestino, numeroLinhas)) {
			while (linhaOrigem != linhaDestino) {
				linhaOrigem = Torus.incrementaPonteiro(linhaOrigem, numeroLinhas);
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
			}
		} else {
			while (linhaOrigem != linhaDestino) {
				linhaOrigem = Torus.decrementaPonteiro(linhaOrigem, numeroLinhas);
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
			}
		}
		if (Torus.deveAvancarPonteiro(alturaOrigem, alturaDestino, numeroAltura)) {
			while (alturaOrigem != alturaDestino) {
				alturaOrigem = Torus.incrementaPonteiro(alturaOrigem, numeroAltura);
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
			}
		} else {
			while (alturaOrigem != alturaDestino) {
				alturaOrigem = Torus.decrementaPonteiro(alturaOrigem, numeroAltura);
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
			}
		}
		p.setCicloFinal(cicloInicial);
		cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_OUT, phits);
		return cicloInicial + (ciclosLink * phits) - (2 * ciclosLink);
	}

	// Realiza o caminhamento XY, contabilizando os phits para cada aresta
	public long topologiaMesh(CDCM_Vertice p, String coreOrigem, String coreDestino, int linhaOrigem, int colunaOrigem, int alturaOrigem, int linhaDestino, int colunaDestino, int alturaDestino) {
		long phits = p.getPhits();
		long cicloInicial = computaLocalLink(p, p.getCicloInicial(), linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);

		if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
			throw new NoRoutingException();
		cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);

		//cicloInicial = roteamentoXY(cicloInicial, phits, p, coreOrigem, coreDestino, linhaOrigem, colunaOrigem, alturaOrigem, linhaDestino, colunaDestino, alturaDestino);
		while (alturaOrigem != alturaDestino) {
			if (alturaOrigem > alturaDestino) {
				alturaOrigem--;
				if (NocComFalhas.temFalha("I", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
			} else {
				alturaOrigem++;
				if (NocComFalhas.temFalha("U", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
			}
		}

		while (colunaOrigem != colunaDestino) // Algoritmo XY
		{
			if (colunaOrigem > colunaDestino) {
				colunaOrigem--;
				if (NocComFalhas.temFalha("E", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
			} else {
				colunaOrigem++;
				if (NocComFalhas.temFalha("W", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
			}
		}
		while (linhaOrigem != linhaDestino) {
			if (linhaOrigem > linhaDestino) {
				linhaOrigem--;
				if (NocComFalhas.temFalha("S", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
			} else {
				linhaOrigem++;
				if (NocComFalhas.temFalha("N", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
			}
		}

		//return cicloInicial;
		// cicloInicial = roteamentoNF(cicloInicial, phits, p, coreOrigem ,
		// coreDestino, linhaOrigem, colunaOrigem, linhaDestino, colunaDestino);

		if (NocComFalhas.temFalha("L", linhaOrigem, colunaOrigem, alturaOrigem))
			throw new NoRoutingException();
		p.setCicloFinalLocal(cicloInicial);
		
		cicloInicial = computaLocalLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_OUT, phits);

		return cicloInicial + (ciclosLink * phits) - (2 * ciclosLink);
		
	//	int linhaOrigem, colunaOrigem, linhaDestino, colunaDestino, alturaOrigem, alturaDestino;
	//	String coreOrigem = p.getCoreOrigem();
	//	String coreDestino = p.getCoreDestino();
		/* 
		 //WORKING
		 
		
		long phits = p.getPhits();

		linhaOrigem = DescobreLinha(coreOrigem);
		colunaOrigem = DescobreColuna(coreOrigem);
		alturaOrigem = DescobreAltura(coreOrigem);
		linhaDestino = DescobreLinha(coreDestino);
		colunaDestino = DescobreColuna(coreDestino);
		alturaDestino = DescobreAltura(coreDestino);

		ciclo = p.getCicloInicial();
		//p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits
		long cicloFinalLocal = computaLocalLink(p, ciclo,linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);
		p.setCicloFinalLocal(cicloFinalLocal);
		computaRoteador(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_IN, phits);
		while (alturaOrigem != alturaDestino) // Algoritmo XY
		{
			if (alturaOrigem > alturaDestino) {
				alturaOrigem--;
				computaLink(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
				computaRoteador(p, ciclo,linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
			} else {
				alturaOrigem++;
				computaLink(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
				computaRoteador(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
			}
		}

		while (colunaOrigem != colunaDestino) {
			if (colunaOrigem > colunaDestino) {
				colunaOrigem--;
				computaLink(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
				computaRoteador(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
			} else {
				colunaOrigem++;
				computaLink(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
				computaRoteador(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
			}
		}
		while (linhaOrigem != linhaDestino) // Algoritmo XY
		{
			if (linhaOrigem > linhaDestino) {
				linhaOrigem--;
				computaLink(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
				computaRoteador(p, ciclo,linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
			} else {
				linhaOrigem++;
				computaLink(p, ciclo,linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
				computaRoteador(p, ciclo,linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
			}
		}
		return computaLocalLink(p,ciclo, linhaOrigem, colunaOrigem, alturaOrigem, Router.LOCAL_OUT, phits);
		*/
	}

	/*public long roteamentoNF(long cicloInicial, long phits, CDCM_Vertice p, String coreOrigem, String coreDestino, int linhaOrigem, int alturaOrigem, int colunaOrigem, int linhaDestino,
			int colunaDestino, int alturaDestino) {
		while (colunaOrigem > colunaDestino || linhaOrigem > linhaDestino || alturaOrigem > alturaDestino) // Negative
		// first
		{
			if (alturaOrigem > alturaDestino) {// Deslocamento inicial em X
				alturaOrigem--;
				if (NocComFalhas.temFalha("E", linhaOrigem, colunaOrigem, alturaOrigem) || NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem)) {
				if (colunaOrigem > colunaDestino) // Deslocamento inicial em X
				{
					colunaOrigem--;
					if (NocComFalhas.temFalha("E", linhaOrigem, colunaOrigem, alturaOrigem) || NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem)) {
						// Tem que deslocar para um sentido negativo
						if (linhaOrigem > linhaDestino) {
							colunaOrigem++;
							linhaOrigem--;
							if (NocComFalhas.temFalha("S", linhaOrigem, colunaOrigem, alturaOrigem))
								throw new NoRoutingException();
							cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
							if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
								throw new NoRoutingException();
							cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
						} else
							throw new NoRoutingException();
					} else {
						cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
						cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
					}
				} else // Deslocamento inicial em Y (linhaOrigem > linhaDestino)
				{
					linhaOrigem--;
					if (NocComFalhas.temFalha("S", linhaOrigem, colunaOrigem, alturaOrigem))
						throw new NoRoutingException();
					cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
					if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
						throw new NoRoutingException();
					cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
				}
				}
			}
		}
		while (linhaOrigem != linhaDestino || colunaOrigem != colunaDestino || alturaOrigem != alturaDestino) // Positive
		// last
		{
			if( alturaOrigem < alturaDestino) {
				if (colunaOrigem < colunaDestino) // Deslocamento inicial em X
				{
					colunaOrigem++;
					if (NocComFalhas.temFalha("W", linhaOrigem, colunaOrigem, alturaOrigem) || NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem)) {
						if (linhaOrigem < linhaDestino) {
							colunaOrigem--;
							linhaOrigem++;
							if (NocComFalhas.temFalha("N", linhaOrigem, colunaOrigem, alturaOrigem))
								throw new NoRoutingException();
							cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
							if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
								throw new NoRoutingException();
							cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
						} else
							throw new NoRoutingException();
					} else {
						cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
						cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
					}
				} else {
					linhaOrigem++;
					if (NocComFalhas.temFalha("N", linhaOrigem, colunaOrigem, alturaOrigem))
						throw new NoRoutingException();
					cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
					if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
						throw new NoRoutingException();
					cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
				}
			}
		}
		return cicloInicial;
	}
*/
	/*public long roteamentoXY(long cicloInicial, long phits, CDCM_Vertice p, String coreOrigem, String coreDestino, int linhaOrigem, int colunaOrigem, int alturaOrigem, int linhaDestino,
			int colunaDestino, int alturaDestino) {

		while (alturaOrigem != alturaDestino) {
			if (alturaOrigem > alturaDestino) {
				alturaOrigem--;
				if (NocComFalhas.temFalha("I", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.INFERIOR, phits);
			} else {
				alturaOrigem++;
				if (NocComFalhas.temFalha("U", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUPERIOR, phits);
			}
		}

		while (colunaOrigem != colunaDestino) // Algoritmo XY
		{
			if (colunaOrigem > colunaDestino) {
				colunaOrigem--;
				if (NocComFalhas.temFalha("E", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.LESTE, phits);
			} else {
				colunaOrigem++;
				if (NocComFalhas.temFalha("W", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.OESTE, phits);
			}
		}
		while (linhaOrigem != linhaDestino) {
			if (linhaOrigem > linhaDestino) {
				linhaOrigem--;
				if (NocComFalhas.temFalha("S", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.SUL, phits);
			} else {
				linhaOrigem++;
				if (NocComFalhas.temFalha("N", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaLink(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
				if (NocComFalhas.temFalha("R", linhaOrigem, colunaOrigem, alturaOrigem))
					throw new NoRoutingException();
				cicloInicial = computaRoteador(p, cicloInicial, linhaOrigem, colunaOrigem, alturaOrigem, Router.NORTE, phits);
			}
		}

		return cicloInicial;
	}*/

	// Descobre a posi��o X de um m�dulo em uma NoC
	public int DescobreLinha(String core) {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (matriz[linha][coluna][altura] != null) {
						if (matriz[linha][coluna][altura].getCoreName() != null) {
							if (matriz[linha][coluna][altura].getCoreName().equals(core))
								return linha;
						}
					}
				}
			}
		}
		return -1;
	}

	// Descobre a posi��o Y de um m�dulo em uma NoC
	public int DescobreColuna(String core) {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (matriz[linha][coluna][altura] != null) {
						if (matriz[linha][coluna][altura].getCoreName() != null) {
							if (matriz[linha][coluna][altura].getCoreName().equals(core))
								return coluna;
						}
					}
				}
			}
		}
		return -1;
	}

	// Descobre a posi��o Z de um m�dulo em uma NoC
	public int DescobreAltura(String core) {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (matriz[linha][coluna][altura] != null) {
						if (matriz[linha][coluna][altura].getCoreName() != null) {
							if (matriz[linha][coluna][altura].getCoreName().equals(core))
								return altura;
						}
					}
				}
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
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (matriz[linha][coluna][altura] != null)
						energia = energia + matriz[linha][coluna][altura].energiaTotalRoteador();
				}
			}
		}
		return energia;
	}

	public void limpaEnergiaDinamica() {
		ciclo = 0;
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (matriz[linha][coluna][altura] != null)
						matriz[linha][coluna][altura].limpaEnergiaDinamica();
				}
			}
		}
	}

	public void salvaPosicionamentoMinimoTempoExecucao() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matrizMinimoTempoExecucao[linha][coluna][altura] = new CDCM_VerticeNoC(matriz[linha][coluna][altura]);
		}
	}

	public void salvaPosicionamentoMaximoTempoExecucao() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matrizMaximoTempoExecucao[linha][coluna][altura] = new CDCM_VerticeNoC(matriz[linha][coluna][altura]);
		}
	}

	public void salvaPosicionamento() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matrizCustoMinimo[linha][coluna][altura] = new CDCM_VerticeNoC(matriz[linha][coluna][altura]);
		}
	}

	public void copiaMatComMatSalva() {
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matriz[linha][coluna][altura] = matrizCustoMinimo[linha][coluna][altura];
		}
	}

	public void copiaMatSalvaComMat() {
		for (int linha = 0; linha < matrizCustoMinimo.length; linha++) {
			for (int coluna = 0; coluna < matrizCustoMinimo[linha].length; coluna++)
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++)
					matrizCustoMinimo[linha][coluna][altura] = matriz[linha][coluna][altura];
		}
	}

	public void conectaLinhaColunaComCore(LinhaColunaAltura lc, String coreElemento) {
		matriz[lc.getLinha()][lc.getColuna()][lc.getAltura()] = new CDCM_VerticeNoC(coreElemento, lc, this);
	}

	// Ao final da execu��o do m�todo criaVetorDeCores, o vetor vetCore,
	// contendo o n�mero de posi��es igual ao
	// n�mero de tiles da NoC, ter� todos os PEs da aplica��o, e as posi��es
	// restantes ser�o preenchidas com "-",
	// ilustrando que o tile associado a este s�mbolo n�o ter� PEs mapeados.
	public void criaVetorDeCores(CDCM_Grafo g) {
		CDCM_Vertice l = g.getInicio();
		String coreOrigem = new String("");
		String coreDestino = new String("");
		int posicao = 0;

		vetCore = new String[matrizCustoMinimo.length * matrizCustoMinimo[0].length* matrizCustoMinimo[0][0].length];
		while (l != null) {
			if (l.getID() == CDCM_Grafo.END || l.getID() == CDCM_Grafo.START) {
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

	public void criaVetorDeLinhaColunaAltura() {
		int contEsperas = 0, contFalhas = 0;
		LinhaColunaAltura vetLinColEsperasAux[] = new LinhaColunaAltura[matriz.length * matriz[0].length * matriz[0][0].length];
		LinhaColunaAltura vetLinColFalhasAux[] = new LinhaColunaAltura[matriz.length * matriz[0].length * matriz[0][0].length];

		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (NocComFalhas.temTileDeEspera(linha, coluna, altura))
						vetLinColEsperasAux[contEsperas++] = new LinhaColunaAltura(linha, coluna, altura);
					else if (NocComFalhas.temFalha("T", linha, coluna, altura) || NocComFalhas.temFalha("L", linha, coluna, altura)) {
						vetLinColFalhasAux[contFalhas++] = new LinhaColunaAltura(linha, coluna, altura);
						matriz[linha][coluna][altura] = new CDCM_VerticeNoC("*", new LinhaColunaAltura(linha, coluna, altura), this);
					}
				}
			}
		}
		// Aloca o vetLinColEsperas com exatamente o n�mero de esperas
		vetLinColEsperas = new LinhaColunaAltura[contEsperas];
		System.arraycopy(vetLinColEsperasAux, 0, vetLinColEsperas, 0, contEsperas);

		// Aloca o vetLinColFalhas com exatamente o n�mero de falhas
		vetLinColFalhas = new LinhaColunaAltura[contFalhas];
		System.arraycopy(vetLinColFalhasAux, 0, vetLinColFalhas, 0, contFalhas);

		int k = 0;
		LinhaColunaAltura vetLinColAux[] = new LinhaColunaAltura[matriz.length * matriz[0].length* matriz[0][0].length];
		for (int linha = 0; linha < matriz.length; linha++) {
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					if (NocComFalhas.temFalha("T", linha, coluna, altura) || NocComFalhas.temFalha("L", linha, coluna, altura))
						continue;
					if (NocComFalhas.temTileDeEspera(linha, coluna, altura) && contFalhas == 0) {
						matriz[linha][coluna][altura] = new CDCM_VerticeNoC("+", new LinhaColunaAltura(linha, coluna, altura), this);
						continue;
					}
					if (NocComFalhas.temTileDeEspera(linha, coluna, altura) && contFalhas > 0)
						contFalhas--;
					vetLinColAux[k++] = new LinhaColunaAltura(linha, coluna, altura);
				}
			}
		}
		// Reduz o vetor vetLinhaColuna a apenas os tiles que n�o t�m falhas
		vetLinhaColuna = new LinhaColunaAltura[k];
		System.arraycopy(vetLinColAux, 0, vetLinhaColuna, 0, k);
	}

	public double computa(CDCM_Grafo g, boolean linhaComando) {
		ciclo = g.executaCDCM(this);
		if (linhaComando == false)
			exibeAndamento();

		numeroCiclosTodosMapeamentos = numeroCiclosTodosMapeamentos + ciclo;
		if (maiorNumeroCiclosDeTodosMapeamentos < ciclo) {
			salvaPosicionamentoMaximoTempoExecucao();
			maiorNumeroCiclosDeTodosMapeamentos = ciclo;
		}
		if (menorNumeroCiclosDeTodosMapeamentos > ciclo) {
			salvaPosicionamentoMinimoTempoExecucao();
			menorNumeroCiclosDeTodosMapeamentos = ciclo;
		}
		return energiaTotalNoC();
	}

	public void algoritmoPosicionamento(CDCM_Grafo g, int algo) {
		algoritmoPosicionamento(g, algo, false);
	}

	public void algoritmoPosicionamento(CDCM_Grafo g, int algo, boolean linhaComando) {
		primeiraVez = true;
		numeroCombinacoes = 0;
		falhasDeRoteamento = 0;
		numeroCiclosTodosMapeamentos = 0;
		maiorNumeroCiclosDeTodosMapeamentos = 0;
		menorNumeroCiclosDeTodosMapeamentos = Integer.MAX_VALUE;
		energiaConsumidaMapeamento = Double.MAX_VALUE;
		criaVetorDeCores(g);
		criaVetorDeLinhaColunaAltura();
		if (!linhaComando)
			exibeVetLinhaColunaAltura("LinhaColunaAltura", vetLinhaColuna);
		if (!linhaComando)
			exibeVetLinhaColunaAltura("LinhaColunaAltura_Esperas", vetLinColEsperas);
		exibeVetLinhaColunaAltura("LinhaColunaAltura_Falhas", vetLinColFalhas);
		if (!linhaComando)
			exibeVetString(vetCore);
		g.executaASAPeCriaListaDeNiveis(!linhaComando); // by amory
		long tInicial = 0, tFinal = 0;
		switch (algo) {
			case Algoritmo.ExhaustiveSearch:
				CDCM_Exaustivo e = new CDCM_Exaustivo(this, g);
				numeroTotalCombinacoes = Matematico.fatorial(numeroLinhas * numeroColunas* numeroAltura);
				tInicial = new Date().getTime();
				optimumMappingCicle = e.algoritmo(vetLinhaColuna, vetCore, vetLinhaColuna.length, linhaComando);
			break;

			case Algoritmo.SimulatedAnnealing:
				CDCM_SimulatedAnnealing sa = new CDCM_SimulatedAnnealing(this, g);
				numeroTotalCombinacoes = sa.numeroTotalCombinacoes();
				tInicial = new Date().getTime();
				optimumMappingCicle = sa.algoritmo(vetLinhaColuna, vetCore, linhaComando);
			break;

			case Algoritmo.TabooSearch:
				CDCM_TabooSearch ts = new CDCM_TabooSearch(this, g);
				numeroTotalCombinacoes = ts.numeroTotalCombinacoes();
				tInicial = new Date().getTime();
				optimumMappingCicle = ts.algoritmo(vetLinhaColuna, vetCore, linhaComando);
			break;
		}
		tFinal = new Date().getTime();
		setCiclo(optimumMappingCicle);
		// by amory. reduce print while running in batch mode
		if (!linhaComando)
			exibeNoCSalva();
		copiaMatComMatSalva();
		System.out.println("Execution Time of CDA (CPU time): " + (tFinal - tInicial) + "ms");
		System.out.println("Energy Consumption: " + energiaNoC() / 1000 + "uJ");
		System.out.println("Idle Energy Consumption: " + getEnergiaIdle() / 1000 + "uJ");
		System.out.println("Total Energy Consumption: " + (energiaNoC() + getEnergiaIdle()) / 1000 + "uJ");
		System.out.println("Number of Cycles of Optimum Mapping: " + optimumMappingCicle);
		System.out.println("Minimum Number of Cycles: " + menorNumeroCiclosDeTodosMapeamentos);
		System.out.println("Maximum Number of Cycles: " + maiorNumeroCiclosDeTodosMapeamentos);
		System.out.println("Number of Cycles: Average (all mappings): " + numeroCiclosTodosMapeamentos / numeroTotalCombinacoes);
		System.out.println("Number of Combinations: " + numeroTotalCombinacoes);
		System.out.println("Number of routing fails: " + falhasDeRoteamento);

		// by amory
		try {
			String path = System.getProperties().getProperty("user.dir");
			// System.out.println("Energy Directory " + path);
			OutputStream fileOut = new FileOutputStream(path + "/Energy.txt");
			DataOutputStream ds = new DataOutputStream(fileOut);
			String string = Double.toString(energiaNoC() / 1000) + "\n";
			ds.write(string.getBytes());
			ds.close();
		} catch (Exception e) {
			System.out.println("Problems while generating CDCM Energy analisys");
			e.printStackTrace();
			return;
		}
		try {
			String path = System.getProperties().getProperty("user.dir");
			// System.out.println("Energy Directory " + path);
			OutputStream fileOut = new FileOutputStream(path + "/Latency.txt");
			DataOutputStream ds = new DataOutputStream(fileOut);
			String string = Long.toString(optimumMappingCicle) + "\n";
			ds.write(string.getBytes());
			ds.close();
		} catch (Exception e) {
			System.out.println("Problems while generating CDCM Latency analisys");
			e.printStackTrace();
			return;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DEPURA��O
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibeAndamento() {
		if (primeiraVez) {
			plAnt = 0;
			primeiraVez = false;
			System.out.println("0    1    2    3    4    5    6    7    8    9");
			System.out.println("024680246802468024680246802468024680246802468024680");
		} else {
			double pd = (double) (numeroCombinacoes + falhasDeRoteamento) / (double) numeroTotalCombinacoes;
			int pl = (int) (pd * 100.0);

			if (pl > plAnt) {
				int localCount = plAnt;

				for (int i = 0; i < pl - plAnt; i++) {
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

	public void exibeVetLinhaColunaAltura(String nome, LinhaColunaAltura vet[]) {
		System.out.println("Vetor " + nome + ":");
		System.out.print("\t");
		for (int i = 0; i < vet.length; i++)
			System.out.print("[" + vet[i].getLinha() + ", " + vet[i].getColuna() + ", " + vet[i].getAltura()+ "] ");
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
			for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
				System.out.print("\t");
				for (int altura = 0; altura < matriz[linha][coluna].length; altura++) {
					System.out.print("[" + matriz[linha][coluna][altura].getCoreName() + "] ");
				}
				System.out.println();
			}
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
					if (matriz[linha][coluna][altura].getEnergiaControleRoteador() <= 0)
						continue;
					System.out.print("\n\tRoteador(" + linha + ", " + coluna + ", " + altura + ")");
					energia = energia + matriz[linha][coluna][altura].exibe();
				}
			}
		}
		System.out.println("\n");
		System.out.println("Energy Consumption: " + energia / 1000 + "uJ");
		System.out.println("Idle Energy Consumption: " + getEnergiaIdle() / 1000 + "uJ");
		System.out.println("Minimum Number of Cicles: " + getCiclo());
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}

	public void exibeNoCSalva() {
		double energia = 0;

		System.out.print("\nNoC FINAL(" + numeroLinhas + "x" + numeroColunas + "x" + numeroAltura + ")");
		for (int linha = 0; linha < matrizCustoMinimo.length; linha++) {
			for (int coluna = 0; coluna < matrizCustoMinimo[linha].length; coluna++) {
				for (int altura = 0; altura < matrizCustoMinimo[linha][coluna].length; altura++) {
					// if(matrizCustoMinimo[linha][coluna].getEnergiaControleRoteador()
					// <= 0)
					// continue;
					System.out.print("\n\tRoteador(" + linha + ", " + coluna + ", " + altura + ")");
					energia = energia + matrizCustoMinimo[linha][coluna][altura].exibe();
				}
			}
		}
		System.out.println("\n");
		System.out.println("Energy Consumption: " + energia / 1000 + "uJ");
		System.out.println("Idle Energy Consumption: " + getEnergiaIdle() / 1000 + "uJ");
		System.out.println("Minimum Number of Cicles: " + getCiclo());
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}

	public String imprimeNoC() {
		String s = new String("");

		for (int linha = 0; linha < matrizCustoMinimo.length; linha++) {
			for (int coluna = 0; coluna < matrizCustoMinimo[linha].length; coluna++)
				for (int altura = 0; altura < matrizCustoMinimo[linha][coluna].length; altura++)
					s = s.concat(" " + matrizCustoMinimo[linha][coluna][altura].getCoreName());
			s = s.concat("\n");
		}
		return s;
	}

	public void desenhaNoC(Graphics g) {
		int l = ehTopologiaMesh ? 0 : CHANNEL_SIZE_Y;
		
		for (int linha = 0; linha < numeroLinhas; linha++) {
			int c = ehTopologiaMesh ? 0 : CHANNEL_SIZE_X;

			for (int coluna = 0; coluna < numeroColunas; coluna++) {
				int a = ehTopologiaMesh ? 0 : CHANNEL_SIZE_Z;
				for (int altura = viewZ; altura < viewZ+1; altura++) {
					CDCM_VerticeNoC verticeNoC = matriz[linha][coluna][altura];

					if (verticeNoC == null)
						continue;
					
					if (verticeNoC.getCoreName() != null && !verticeNoC.getCoreName().equals("-")) {
						desenhaCanalLocalIn(g, c, l, a, linha, coluna, altura, verticeNoC.linkEntrada[Router.LOCAL_IN].energiaLink());
						desenhaCanalLocalOut(g, c, l, a, linha, coluna, altura, verticeNoC.linkEntrada[Router.LOCAL_OUT].energiaLink());
					}
					if (!ehTopologiaMesh || coluna != numeroColunas - 1) {
						CDCM_Link lnk = verticeNoC.linkEntrada[Router.LESTE];
						double energia = lnk != null ? lnk.energiaLink() : 0;

						desenhaCanalLeste(g, c, l, a, linha, coluna, altura, energia);
					}
					if (!ehTopologiaMesh || coluna != 0) {
						CDCM_Link lnk = verticeNoC.linkEntrada[Router.OESTE];
						double energia = lnk != null ? lnk.energiaLink() : 0;

						desenhaCanalOeste(g, c, l, a, linha, coluna, altura, energia);
					}
					if (!ehTopologiaMesh || linha != 0) {
						CDCM_Link lnk = verticeNoC.linkEntrada[Router.NORTE];
						double energia = lnk != null ? lnk.energiaLink() : 0;

						desenhaCanalNorte(g, c, l, a, linha, coluna, altura, energia);
					}
					if (!ehTopologiaMesh || linha != numeroLinhas - 1) {
						CDCM_Link lnk = verticeNoC.linkEntrada[Router.SUL];
						double energia = lnk != null ? lnk.energiaLink() : 0;

						desenhaCanalSul(g, c, l, a, linha, coluna, altura, energia);
					}
					if (!ehTopologiaMesh || altura != 0) {
						CDCM_Link lnk = verticeNoC.linkEntrada[Router.SUPERIOR];
						double energia = lnk != null ? lnk.energiaLink() : 0;

						desenhaCanalSuperior(g, c, l, a, linha, coluna, altura, energia);
					}
					if (!ehTopologiaMesh || altura != numeroAltura - 1) {
						CDCM_Link lnk = verticeNoC.linkEntrada[Router.INFERIOR];
						double energia = lnk != null ? lnk.energiaLink() : 0;
						desenhaCanalInferior(g, c, l, a, linha, coluna, altura, energia);
						
					}
					if (verticeNoC.getCoreName() != null && !verticeNoC.getCoreName().equals("-"))
						desenhaCore(g, c, l, a, verticeNoC.getCoreName());
					desenhaRoteador(g, c, l, a, linha, coluna, altura);
					

					a = a + TILE_SIZE_Z;
				}
				c = c + TILE_SIZE_X;
			}
			l = l + TILE_SIZE_Y;
		}
	}

	public void desenhaRoteador(Graphics g, int c, int l, int a, int linha, int coluna, int altura) {
		if (matriz[linha][coluna][altura] == null)
			return;
		Color qualCor;
		if (NocComFalhas.temFalha("R", linha, coluna, altura)) // Roteador com
																// falha
			qualCor = Color.red;
		else
			qualCor = Color.black;
		g.setColor(qualCor);
		g.fillRect(c, l, ROUTER_SIZE_X, ROUTER_SIZE_Y);
		// T�tulo
		g.setFont(new Font("Arial", Font.BOLD, 12));
		g.setColor(Color.white);
		String id = new String("R[" + linha + ", " + coluna + ", " + altura + "]");
		g.drawString(id, Math.round(c + (ROUTER_SIZE_X - id.length() * 6) / 2), l + ROUTER_SIZE_Y / 3 - 8);
		// Energias
		g.setFont(new Font("Arial", Font.BOLD, 11));

		String energiaBufferRoteador = String.format("Eb " + precisionShowed, new Double(matriz[linha][coluna][altura].getEnergiaBufferRoteador() / 1000));
		g.drawString(energiaBufferRoteador, (int) Math.round(c + (ROUTER_SIZE_X - energiaBufferRoteador.length() * 5.5) / 2), l + (2 * ROUTER_SIZE_Y) / 3 - 8);

		String energiaControleRoteador = String.format("Es " + precisionShowed, new Double(matriz[linha][coluna][altura].getEnergiaControleRoteador() / 1000));
		g.drawString(energiaControleRoteador, (int) Math.round(c + (ROUTER_SIZE_X - energiaControleRoteador.length() * 5.5) / 2), l + ROUTER_SIZE_Y - 8);
	}

	public void desenhaCore(Graphics g, int linha, int coluna, int altura, String coreName) {
		Color qualCor;
		if (coreName == null)
			qualCor = Color.blue;
		else if (coreName.equals("*")) // Tile com falha
			qualCor = Color.red;
		else if (coreName.equals("+")) // � uma espera
			qualCor = Color.yellow;
		else if (coreName.equals("-")) // N�o tem PE mapeado no tile
			qualCor = Color.blue;
		else
			qualCor = Color.gray;

		int width = 50;
		int eight = 50;

		Draw.lozengeAndLabel(g, qualCor, Color.black, linha + TILE_SIZE_X - width / 2, coluna + TILE_SIZE_Y - eight / 2, width, eight, coreName);
	}

	public void desenhaCanalLocalIn(Graphics g, int c, int l, int a, int linha, int coluna, int altura, double value) {
		int x = c + ROUTER_SIZE_X - ARROW_WIDTH * 2 - LOZANGE_WIDTH;
		int y = l + ROUTER_SIZE_Y - LOZANGE_EIGHT;

		Color qualCor = NocComFalhas.temFalha("L", linha, coluna, altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x + CHANNEL_SIZE_X - 6, y + CHANNEL_SIZE_Y - 6, x + LOZANGE_WIDTH, y + LOZANGE_EIGHT);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // leste
		{
			String bytes = String.format("F" + precisionShowed, new Double(value / 1000));
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

	public void desenhaCanalLocalOut(Graphics g, int c, int l, int a, int linha, int coluna, int altura, double value) {
		int x = c + ROUTER_SIZE_X ;
		int y = l + ROUTER_SIZE_Y - ARROW_WIDTH * 2 ;

		Color qualCor = NocComFalhas.temFalha("L", linha, coluna, altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y, x + CHANNEL_SIZE_X/2+ARROW_WIDTH, y + CHANNEL_SIZE_Y/2+ARROW_WIDTH);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // leste
		{
			String bytes = String.format(precisionShowed + "F", new Double(value / 1000));
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

	public void desenhaCanalSuperior(Graphics g, int c, int l, int a, int linha, int coluna, int altura, double value) {
		int width = ROUTER_SIZE_X/2;
		int heigth = ARROW_FINAL;
		int x = c + ROUTER_SIZE_X - width/2 +10;
		int y = l + ROUTER_SIZE_Y - CHANNEL_SIZE_Y ;
		
		//int x = c;
		//int y = l + ROUTER_SIZE_Y / 3;

		Color qualCor = NocComFalhas.temFalha("U", linha, coluna, altura) ? Color.red :  WP.getHighLinkColor();
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
			String bytes = String.format(precisionShowed + "F", new Double(value / 1000));
			int offset = 8;

			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				g.drawString("" + bytes.charAt(k),  x + offset, y+heigth/2+3);
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 2;
				else
					offset = offset + 6;
			}
			
		}
		/*int x = c +  ROUTER_SIZE_X;
		int y = l-ROUTER_SIZE_X;

		Color qualCor = NocComFalhas.temFalha("U", linha, coluna, altura) ? Color.red : Color.blue;
		g.setColor(Color.red);
		g.drawRect(x,  y - CHANNEL_SIZE_Y, ROUTER_SIZE_X/2, ARROW_FINAL);
	//	Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y - CHANNEL_SIZE_Y, x, y);
	//	g.setColor(Color.RED);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // superior
		{
			String bytes = String.format("F" + precisionShowed, new Double(value / 1000));
			int offset = ROUTER_SIZE_X * 2;

			StringBuffer SB = new StringBuffer(bytes);
			SB = SB.reverse();
			bytes = SB.toString();
			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 2;
				else
					offset = offset = 6;
				g.drawString("" + bytes.charAt(k), x + offset, y- CHANNEL_SIZE_Y);
			}
		}*/
	}

	public void desenhaCanalInferior(Graphics g, int c, int l, int a, int linha, int coluna, int altura, double value) {
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
			String bytes = String.format(precisionShowed + "F", new Double(value / 1000));
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

	public void desenhaCanalNorte(Graphics g, int c, int l, int a, int linha, int coluna, int altura, double value) {
		int x = c + (2 * ROUTER_SIZE_X) / 3;
		int y = l;

		Color qualCor = NocComFalhas.temFalha("N", linha, coluna, altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y - CHANNEL_SIZE_Y, x, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // norte
		{
			String bytes = String.format(precisionShowed + "F", new Double(value / 1000));
			int offset = -CHANNEL_SIZE_Y;

			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 3;
				else
					offset = offset + 9;
				g.drawString("" + bytes.charAt(k), c + (2 * ROUTER_SIZE_X) / 3 - ARROW_WIDTH / 2, l + offset);
			}
		}
	}

	public void desenhaCanalSul(Graphics g, int c, int l, int a, int linha, int coluna, int altura, double value) {
		int x = c + ROUTER_SIZE_X / 3;
		int y = l + ROUTER_SIZE_Y;

		Color qualCor = NocComFalhas.temFalha("S", linha, coluna, altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x, y + CHANNEL_SIZE_Y, x, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // sul
		{
			String bytes = String.format("F" + precisionShowed, new Double(value / 1000));

			int offset = CHANNEL_SIZE_Y * 2;

			StringBuffer SB = new StringBuffer(bytes);
			SB = SB.reverse();
			bytes = SB.toString();
			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				g.drawString("" + bytes.charAt(k), c + ROUTER_SIZE_X / 3 - ARROW_WIDTH / 2, l + offset);
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset - 3;
				else
					offset = offset - 9;
			}
		}
	}

	public void desenhaCanalLeste(Graphics g, int c, int l, int a, int linha, int coluna, int altura, double value) {
		int x = c + ROUTER_SIZE_X;
		int y = l + (2 * ROUTER_SIZE_Y) / 3;

		Color qualCor = NocComFalhas.temFalha("E", linha, coluna, altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x + CHANNEL_SIZE_X, y, x, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // leste
		{
			String bytes = String.format("F" + precisionShowed, new Double(value / 1000));
			int offset = ROUTER_SIZE_X * 2;

			StringBuffer SB = new StringBuffer(bytes);
			SB = SB.reverse();
			bytes = SB.toString();
			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset - 2;
				else
					offset = offset - 6;
				g.drawString("" + bytes.charAt(k), c + offset, l + (2 * ROUTER_SIZE_Y) / 3 + ARROW_WIDTH / 2 + 1);
			}
		}
	}

	public void desenhaCanalOeste(Graphics g, int c, int l, int a, int linha, int coluna, int altura, double value) {
		int x = c;
		int y = l + ROUTER_SIZE_Y / 3;

		Color qualCor = NocComFalhas.temFalha("W", linha, coluna, altura) ? Color.red : Color.blue;
		Draw.arrow(g, qualCor, ARROW_FINAL, ARROW_FINAL, ARROW_WIDTH, x - CHANNEL_SIZE_X, y, x, y);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 11));
		if (value != 0.0) // oeste
		{
			String bytes = String.format(precisionShowed + "F", new Double(value / 1000));
			int offset = -ROUTER_SIZE_X;

			for (int k = 0; bytes.charAt(k) != 'F'; k++) {
				g.drawString("" + bytes.charAt(k), c + offset, l + ROUTER_SIZE_Y / 3 + ARROW_WIDTH / 2 + 1);
				if (bytes.charAt(k) == '.' || bytes.charAt(k) == ',')
					offset = offset + 2;
				else
					offset = offset + 6;
			}
		}
	}
}