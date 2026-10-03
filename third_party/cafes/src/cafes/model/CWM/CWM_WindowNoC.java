package cafes.model.CWM;

/*
 * Autor: 
 * 		C�sar Marcon
 * Revisor:
 * 		Edson Moreno
 * Objetivo:
 * 		Criar e gerenciar as janelas de evolucao do calculo de melhor 
 * 			posicionamento e de resultado do calculo do melhor posicionamento.
 */

import cafes.ui.WindowPrincipal;

import java.awt.event.*;

import javax.swing.*;

import java.awt.*;

import cafes.model.*;

class CWM_WindowNoC extends JFrame
{
	private static final long serialVersionUID = 1583193944466395065L;
	private ScrollPane scroll;
	private boolean needScroll;
	private int numPixelColuna, numPixelCanvasColuna;
	private int numPixelLinha, numPixelCanvasLinha;
	private int roteadoresPorColuna, roteadoresPorAltura, roteadoresPorLinha; 
	private CWM_DesenhoNoC Dnc;
	private CWM_Sequencia seq;
	private CWM_NoC noc;
	private WindowPrincipal WP;
	private EvolucaoAlgoritmoMapeamento EPos;
	private boolean comEstimativaDeTempo;
	private JComboBox tz;
	private int viewZ=1;
	private JLayeredPane pbNetwork = new JLayeredPane();

	/*
	 * Objetivos:
	 * 		Construtor da classe CWM_WindowNoC.
	 * Parametros:
	 * 		_noc->NoC definida pelo usu�rio.
	 * 		rotPorLinhas->N�mero de linha da noc
	 * 		rotPorColuna->N�mero de colunas da noc
	 * 		_seq->Sequencia dos vertices/cores e seus vertices adjacentes
	 * 		_comEstimativaDeTempo->solicita o calculo de estimativa de tempo 
	 */
	public CWM_WindowNoC(CWM_NoC _noc, int rotPorLinhas, int rotPorColuna,int rotPorAltura, CWM_Sequencia _seq, boolean _comEstimativaDeTempo)
	{
		this(_noc, rotPorLinhas, rotPorColuna,rotPorAltura, _seq, 0,  _comEstimativaDeTempo);
	}
	
	/*
	 * Objetivos:
	 * 		Construtor da classe CWM_WindowNoC.
	 * Parametros:
	 * 		_noc->NoC definida pelo usu�rio.
	 * 		rotPorLinhas->N�mero de linha na noc
	 * 		rotPorColuna->N�mero de colunas na noc
	 * 		_seq->Sequencia dos vertices/cores e seus vertices adjacentes
	 * 		_algo->algoritmo a ser utilizado no calculo (e.g. exaustivo/taboo search/simulated annealing)
	 * 		_comEstimativaDeTempo->solicita o calculo de estimativa de tempo 
	 */
	public CWM_WindowNoC(CWM_NoC _noc, int rotPorLinhas, int rotPorColuna,int rotPorAltura, CWM_Sequencia _seq, int _algo, boolean _comEstimativaDeTempo)
	{
		super();
		this.noc = _noc;
		cwmWindowNoC(rotPorLinhas, rotPorColuna, rotPorAltura,_seq, _algo,  _comEstimativaDeTempo);
	}
	
	/*
	 * Objetivos:
	 * 		Construtor da classe CWM_WindowNoC.
	 * Parametros:
	 * 		_WP->janela principal a ser acessada desejada a NoC
	 * 		rotPorLinhas->N�mero de linhas na noc
	 * 		rotPorColuna->N�mero de colunas na noc
	 * 		_seq->Sequencia dos vertices/cores e seus vertices adjacentes
	 * 		_comEstimativaDeTempo->solicita o calculo de estimativa de tempo 
	 */
	public CWM_WindowNoC(WindowPrincipal _WP, int rotPorLinhas, int rotPorColuna,int rAltura, CWM_Sequencia _seq, boolean _comEstimativaDeTempo)
	{
		this(_WP, rotPorLinhas, rotPorColuna,rAltura, _seq, 0,  _comEstimativaDeTempo);
	}

	/*
	 * Objetivos:
	 * 		Construtor da classe CWM_WindowNoC.
	 * Parametros:
	 * 		_WP->janela principal a ser acessada desejada a NoC
	 * 		rotPorLinhas->N�mero de linhas na noc
	 * 		_numColuna->N�mero de colunas na noc
	 * 		_seq->Sequencia dos vertices/cores e seus vertices adjacentes
	 * 		_algo->algoritmo a ser utilizado no calculo (e.g. exaustivo/taboo search/simulated annealing)
	 * 		_comEstimativaDeTempo->solicita o calculo de estimativa de tempo 
	 */
	public CWM_WindowNoC(WindowPrincipal _WP, int rotPorLinhas, int rotPorColuna, int rotporAltura, CWM_Sequencia _seq, int _algo, boolean _comEstimativaDeTempo)
	{
		super();
		this.WP = _WP;
		cwmWindowNoC(rotPorLinhas, rotPorColuna,rotporAltura, _seq, _algo,  _comEstimativaDeTempo);
	}
	
	/*
	 * Objetivos:
	 * 		Criar a janela associando os menus e algoritmos a serem utilizados
	 * Parametros:
	 * 		rotPorLinhas->numero de linhas na noc
	 * 		rotPorColuna->numero de colunas na noc
	 * 		_sequencia->Sequencia dos vertices/cores e seus vertices adjacentes
	 * 		_algo->algoritmo a ser utilizado no calculo (e.g. exaustivo/taboo search/simulated annealing)
	 * 		_comEstTempo->solicita o calculo de estimativa de tempo
	 */
	private void cwmWindowNoC(int rotPorLinhas, int rotPorColuna,int rotPorAltura, CWM_Sequencia _sequencia, int _algo, boolean _comEstTempo)
	{
		this.seq = _sequencia;
		this.roteadoresPorLinha = rotPorLinhas;
		this.roteadoresPorColuna = rotPorColuna;
		this.roteadoresPorAltura =rotPorAltura;
		this.needScroll = false;
		this.comEstimativaDeTempo = _comEstTempo;
		
		switch(_algo)
		{
			case Algoritmo.ExhaustiveSearch:
				setTitle("CWM -> NoC - Exhaustive Search");
				break;

			case Algoritmo.SimulatedAnnealing:
				setTitle("CWM -> NoC - Simulated Annealing");
				break;
				
			case Algoritmo.TabooSearch:
				setTitle("CWM -> NoC - Taboo Search");
				break;

			case Algoritmo.HeuristicSearch:
				setTitle("CWM -> NoC - Heuristic Search");
				break;

			case Algoritmo.HeuristicSearch_SA:
				setTitle("CWM -> NoC - Heuristic Search + SA");
				break;

			case Algoritmo.HeuristicSearch_Taboo:
				setTitle("CWM -> NoC - Heuristic Search + Taboo");
				break;
				
			case Algoritmo.ManualSearch:
				setTitle("CWM -> NoC - Manual Search");
				break;

			default:
				setTitle("CWM -> NoC");
				break;
		}
		numPixelLinha = 120 + roteadoresPorColuna*CWM_NoC.TILE_SIZE_X;
		numPixelColuna = 205 + roteadoresPorLinha*CWM_NoC.TILE_SIZE_Y;
		if(getWindowPrincipal().ehTopologiaMesh())
		{
			numPixelColuna = numPixelColuna - CWM_NoC.CHANNEL_SIZE_X;
			numPixelLinha = numPixelLinha - CWM_NoC.CHANNEL_SIZE_Y;
		}
		numPixelCanvasColuna = numPixelColuna;
		numPixelCanvasLinha = numPixelLinha;
		Dimension dimTela = Toolkit.getDefaultToolkit().getScreenSize();
		//condi��es para inser��o de scroll no Canvas da NoC
		if(dimTela.height <= numPixelColuna)
		{
			if(dimTela.width <= numPixelLinha)
			{
				setSize(dimTela.width, dimTela.height);
				numPixelLinha = dimTela.width;
				numPixelColuna = dimTela.height;
			}
			else
			{
				setSize(numPixelLinha, dimTela.height);
				numPixelColuna = dimTela.height;
			}
			needScroll = true;
		}
		else
		{
			if(dimTela.width <= numPixelLinha)
			{
				setSize(dimTela.width, numPixelColuna);
				numPixelLinha = dimTela.width;
				needScroll = true;
			}
			else
				setSize(numPixelLinha, numPixelColuna);
		}
		setLocation(0, 0);
		getContentPane().setLayout(null);
		setMenuBar(new CWM_MenuNoC(this));
		addWindowListener
		(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					setVisible(false); 
				}
			}
		);
		addComponentes();
		setVisible(true);
		setResizable(false);
	}

	/*
	 * Objetivos:
	 * 		Retornar a janela q esta sendo utilizada. Se a janela principal ou 
	 * 			a janela de calculo da NoC.
	 * Parametros:
	 * 		Nao ha...
	 */
	public WindowPrincipal getWindowPrincipal()
	{
		if(noc!=null)
			return noc.getMainWindow();
		return WP;
	}

	/*
	 * Objetivos:
	 * 		Retorna o numero de roteadores por linha (X m�ximo)
	 * Parametros:
	 * 		Nao ha...
	 */
	/*public int getNumLinhas()
	{
		return roteadoresPorLinha;
	}*/
	public int getNumAltura()
	{
		return roteadoresPorAltura;
	}

	/*
	 * Objetivos:
	 * 		Retorna o numero de roteadores por coluna(Y m�ximo)
	 * Parametros:
	 * 		Nao ha...
	 */
	public int getNumColunas() 
	{
		return roteadoresPorColuna;
	}

	/*
	 * Objetivos:
	 * 		Desenha o quadrado de cada posicao da NoC, caso o construtor da classe
	 * 			tenha sido chamada passando a NoC. Do contr�rio desenha a evolucao
	 * 			do calculo de melhor posicionamento.
	 * Parametros:
	 * 		Nao ha...
	 */
	private void addControlView() {
		Dimension dim = getSize();
		dim.width += 100;
		setSize(dim);

		JLabel lz = new JLabel("Z:");
		lz.setBounds(numPixelCanvasLinha, 20, 20, 20);
		getContentPane().add(lz);

		tz = new JComboBox();

		for (int i = 0; i < noc.getNumeroAltura(); i++)
			tz.addItem(WindowPrincipal.makeObj(i));

		tz.setSelectedIndex(noc.getNumeroAltura() - 1);
		tz.setBounds(numPixelCanvasLinha + 25, 20, 40, 20);
		tz.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				viewZ = WindowPrincipal.getUnsignedNumber(tz.getSelectedItem().toString());
				if (viewZ >= noc.getNumeroAltura())
					viewZ = noc.getNumeroAltura() - 1;

				tz.setSelectedIndex(viewZ);

				pbNetwork.removeAll();
				noc.setView(0, 0, viewZ);
				getContentPane().add(pbNetwork);
				Dnc = new CWM_DesenhoNoC(noc, 15, 15, numPixelCanvasLinha - 45, numPixelCanvasColuna - 130);
				pbNetwork.add(Dnc);

			}
		});
		getContentPane().add(tz);
	}
	public void addComponentes()
	{
		getGraphics();
		if(noc!=null) {
			addControlView();
			addCanvas(10, 10, numPixelLinha, numPixelColuna-25);
		}else
			addEvolucao(10, numPixelColuna/2-30, numPixelLinha, 50);
	}

	/*
	 * Objetivos:
	 * 		Desenha o quadrado de cada posicao da NoC
	 * Parametros:
	 * 		Nao ha...
	 */
	public void addCanvas(int x, int y, int dimx, int dimy)
	{
		Label energia;
		
		if(comEstimativaDeTempo)
			energia = new Label(" Energy = " + String.format("%.2f", new Double(noc.getEnergiaConsumidaMapeamento()-noc.getEnergiaIdle()/1000)) + "uJ  -  Idle Energy = " + String.format("%.2f", new Double(noc.getEnergiaIdle()/1000)) + "uJ");
		else	
			energia = new Label(" Energy = " + String.format("%.2f", new Double(noc.getEnergiaConsumidaMapeamento()/1000)) + "uJ");

		energia.setBackground(new Color(80, 80, 80));
		energia.setFont(new Font("Arial", Font.BOLD, 12));
		energia.setForeground(Color.WHITE);
		energia.setBounds(x+2, dimy-69, dimx-29, 28);
		getContentPane().add(energia);
		// Panel Border do desenho da rede
		
		pbNetwork.setBounds(x, y-5, dimx-25, dimy-80);
		pbNetwork.setBorder(BorderFactory.createTitledBorder("NoC"));
		getContentPane().add(pbNetwork);
		Dnc = new CWM_DesenhoNoC(noc, x+10, y+10, numPixelCanvasLinha-45, numPixelCanvasColuna-130);
		if(!needScroll)
			pbNetwork.add(Dnc);
		else
		{
			scroll = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
			scroll.setBounds(x+10, y+10, dimx-45, dimy-105);
			scroll.add(Dnc);
			pbNetwork.add(scroll);
		}
	}

	/*
	 * Objetivos:
	 * 		Desenha a evolucaodo calculo de melhor posicionamento.
	 * Parametros:
	 * 		Nao ha...
	 */
	public void addEvolucao(int x, int y, int dimx, int dimy)
	{
		EPos = new EvolucaoAlgoritmoMapeamento(x+10, y, numPixelLinha-40, dimy);
		getContentPane().add(EPos);
	}

	/*
	 * Objetivos:
	 * 		Retorna a sequencia/vertice que estah sendo utilizado no calculo.
	 * Parametros:
	 * 		Nao ha...
	 */
	public CWM_Sequencia getSequencia()
	{
		return seq;
	}

	/*
	 * Objetivos:
	 * 		Retorna a NoC que estah sendo utilizada no calculo.
	 * Parametros:
	 * 		Nao ha...
	 */
	public CWM_NoC getNoc()
	{
		return noc;
	}

	/*
	 * Objetivos:
	 * 		Retorna o quanto o calculo jah evoluiu.
	 * Parametros:
	 * 		Nao ha...
	 */
	public EvolucaoAlgoritmoMapeamento getEvolucaoMapeamento()
	{
		return EPos;
	}
}
