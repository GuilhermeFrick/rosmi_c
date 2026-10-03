package cafes.model.ECWM;

import cafes.ui.WindowPrincipal;

import java.awt.event.*;

import javax.swing.*;

import java.awt.*;

import cafes.model.*;
import cafes.model.CWM.*;

class ECWM_WindowNoC extends JFrame
{
	private static final long serialVersionUID = 5675918674972452641L;
	private ScrollPane scroll;
	private boolean needScroll;
	private int numPixelColuna, numPixelCanvasColuna;
	private int numPixelLinha, numPixelCanvasLinha;
	private int roteadoresPorColuna, roteadoresPorLinha, roteadoresPorAltura; 
	private ECWM_DesenhoNoC Dnc;
	private ECWM_Sequencia seq;
	private ECWM_NoC noc;
	private WindowPrincipal WP;
	private EvolucaoAlgoritmoMapeamento EPos;
	private boolean comEstimativaDeTempo;
	private JComboBox tz;
	private int viewZ=1;
	private JLayeredPane pbNetwork = new JLayeredPane();

	public ECWM_WindowNoC(ECWM_NoC noc, int roteadoresPorLinha, int roteadoresPorColuna,  int roteadoresPorAltura,ECWM_Sequencia seq, boolean comEstimativaDeTempo)
	{
		this(noc, roteadoresPorLinha, roteadoresPorColuna,roteadoresPorAltura, seq, 0,  comEstimativaDeTempo);
	}
	public ECWM_WindowNoC(ECWM_NoC noc, int roteadoresPorLinha, int roteadoresPorColuna,int roteadoresPorAltura, ECWM_Sequencia seq, int algo, boolean comEstimativaDeTempo)
	{
		super();
		this.noc = noc;
		ecwmWindowNoC(roteadoresPorLinha, roteadoresPorColuna, roteadoresPorAltura,seq, algo,  comEstimativaDeTempo);
	}
	public ECWM_WindowNoC(WindowPrincipal WP, int roteadoresPorLinha, int roteadoresPorColuna, int roteadoresPorAltura,ECWM_Sequencia seq, boolean comEstimativaDeTempo)
	{
		this(WP, roteadoresPorLinha, roteadoresPorColuna, roteadoresPorAltura,seq, 0,  comEstimativaDeTempo);
	}
	public ECWM_WindowNoC(WindowPrincipal WP, int roteadoresPorLinha, int roteadoresPorColuna,int roteadoresPorAltura, ECWM_Sequencia seq, int algo, boolean comEstimativaDeTempo)
	{
		super();
		this.WP = WP;
		ecwmWindowNoC(roteadoresPorLinha, roteadoresPorColuna,roteadoresPorAltura, seq, algo,  comEstimativaDeTempo);
	}
	public void ecwmWindowNoC(int rotPorLinha, int rotPorColuna, int roteadoresPorAltura,ECWM_Sequencia sequencia, int algo, boolean comEstTempo)
	{
		this.seq = sequencia;
		this.roteadoresPorLinha = rotPorLinha;
		this.roteadoresPorColuna = rotPorColuna;
		this.roteadoresPorAltura = roteadoresPorAltura;
		this.needScroll = false;
		this.comEstimativaDeTempo = comEstTempo;
		
		switch(algo)
		{
			case Algoritmo.ExhaustiveSearch:
				setTitle("ECWM -> NoC - Exhaustive Search");
				break;

			case Algoritmo.SimulatedAnnealing:
				setTitle("ECWM -> NoC - Simulated Annealing");
				break;
				
			default:
				setTitle("ECWM -> NoC");
				break;
		}
		numPixelLinha = 120 + roteadoresPorColuna*ECWM_NoC.TILE_SIZE_X;
		numPixelColuna = 205 + roteadoresPorLinha*ECWM_NoC.TILE_SIZE_Y;
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
		setMenuBar(new ECWM_MenuNoC(this));
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
	public WindowPrincipal getWindowPrincipal()
	{
		if(noc!=null)
			return noc.getMainWindow();
		return WP;
	}
	public int getNumLinhas()
	{
		return roteadoresPorLinha;
	}
	public int getNumColunas() 
	{
		return roteadoresPorColuna;
	}
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
				Dnc = new ECWM_DesenhoNoC(noc, 15, 15, numPixelCanvasLinha - 45, numPixelCanvasColuna - 130);
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
		Dnc = new ECWM_DesenhoNoC(noc, 15,15, numPixelCanvasLinha-45, numPixelCanvasColuna-130);
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
	public void addEvolucao(int x, int y, int dimx, int dimy)
	{
		EPos = new EvolucaoAlgoritmoMapeamento(x+10, y, numPixelLinha-40, dimy);
		getContentPane().add(EPos);
	}
	public ECWM_Sequencia getSequencia()
	{
		return seq;
	}
	public ECWM_NoC getNoc()
	{
		return noc;
	}
	public EvolucaoAlgoritmoMapeamento getEvolucaoMapeamento()
	{
		return EPos;
	}
}
