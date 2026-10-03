package cafes.model.CDCM;

import cafes.ui.WindowPrincipal;

import java.awt.event.*;

import javax.swing.*;

import java.awt.*;

import cafes.model.*;
import cafes.model.CWM.*;

class CDCM_WindowNoC extends JFrame {
	private static final long serialVersionUID = 4129981661180788156L;
	private ScrollPane scroll;
	private boolean needScroll;
	private int numPixelColuna, numPixelCanvasColuna, numPixelAltura;
	private int numPixelLinha, numPixelCanvasLinha;
	private int roteadoresPorColuna, roteadoresPorLinha, roteadoresPorAltura;
	private CDCM_DesenhoNoC Dnc;
	private CDCM_Sequencia seq;
	private CDCM_NoC noc;
	private WindowPrincipal WP;
	private EvolucaoAlgoritmoMapeamento EPos;

	private int viewX = 0, viewY = 0, viewZ = 1;
	private JComboBox /* tx,ty, */tz;
	private JLayeredPane pbNetwork;

	public CDCM_WindowNoC(WindowPrincipal WP, int roteadoresPorLinha,
			int roteadoresPorColuna, int roteadoresPorAltura, CDCM_Sequencia seq) {
		this.WP = WP;
		this.seq = seq;
		this.roteadoresPorLinha = roteadoresPorLinha;
		this.roteadoresPorColuna = roteadoresPorColuna;
		this.roteadoresPorAltura = roteadoresPorAltura;

	}

	/*
	 * public CDCM_WindowNoC(WindowPrincipal WP, int roteadoresPorLinha, int
	 * roteadoresPorColuna, CDCM_Sequencia seq) { this(WP, roteadoresPorLinha,
	 * roteadoresPorColuna, seq, 0); }
	 */
	public CDCM_WindowNoC(WindowPrincipal WP, int roteadoresPorLinha,
			int roteadoresPorColuna, int roteadoresPorAltura,
			CDCM_Sequencia seq, int algo) {
		super();
		this.WP = WP;
		cdcmWindowNoC(roteadoresPorLinha, roteadoresPorColuna,
				roteadoresPorAltura, seq, algo);
	}

	public CDCM_WindowNoC(CDCM_NoC noc, int roteadoresPorLinha,
			int roteadoresPorColuna, int roteadoresPorAltura, CDCM_Sequencia seq) {
		this(noc, roteadoresPorLinha, roteadoresPorColuna, roteadoresPorAltura,
				seq, 0);
	}

	public CDCM_WindowNoC(CDCM_NoC noc, int roteadoresPorLinha,
			int roteadoresPorColuna, int roteadoresPorAltura,
			CDCM_Sequencia seq, int algo) {
		super();
		this.noc = noc;
		cdcmWindowNoC(roteadoresPorLinha, roteadoresPorColuna,
				roteadoresPorAltura, seq, algo);
	}

	

	private void addControlView() {
		Dimension dim = getSize();
		dim.width += 100;
		setSize(dim);

		/*
		 * JLabel lx = new JLabel("X:"); lx.setBounds(numPixelCanvasLinha, 10,
		 * 20, 20); getContentPane().add(lx);
		 * 
		 * tx = new JTextField("0"); tx.setBounds(numPixelCanvasLinha+25, 10,
		 * 20, 20); getContentPane().add(tx);
		 * 
		 * JLabel ly = new JLabel("Y:"); ly.setBounds(numPixelCanvasLinha, 35,
		 * 20, 20); getContentPane().add(ly);
		 * 
		 * ty = new JTextField("0"); ty.setBounds(numPixelCanvasLinha+25, 35,
		 * 20, 20); getContentPane().add(ty);
		 */

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
				// viewX = getNumber(tx.getText());
				// viewY = getNumber(ty.getText());
				viewZ = WindowPrincipal.getUnsignedNumber(tz.getSelectedItem().toString());
				if (viewZ >= noc.getNumeroAltura())
					viewZ = noc.getNumeroAltura() - 1;

				// reseta se colocou lixo..
				// tx.setText(new Integer(viewX).toString());
				// ty.setText(new Integer(viewY).toString());
				tz.setSelectedIndex(viewZ);

				pbNetwork.removeAll();
				noc.setView(viewX, viewY, viewZ);
				Dnc = new CDCM_DesenhoNoC(noc, 10, 20,
						numPixelCanvasLinha - 45, numPixelCanvasColuna - 130);
				if (!needScroll)
					pbNetwork.add(Dnc);

			}
		});
		getContentPane().add(tz);
	}

	private void cdcmWindowNoC(int rotPorLinha, int rotPorColuna,
			int rotPorAltura, CDCM_Sequencia sequencia, int algo) {
		this.seq = sequencia;
		this.roteadoresPorLinha = rotPorLinha;
		this.roteadoresPorColuna = rotPorColuna;
		this.roteadoresPorAltura = rotPorAltura;
		this.needScroll = false;

		switch (algo) {
		case Algoritmo.ExhaustiveSearch:
			setTitle("CDCM -> NoC - Exhaustive Search");
			break;

		case Algoritmo.SimulatedAnnealing:
			setTitle("CDCM -> NoC - Simulated Annealing");
			break;

		default:
			setTitle("CDCM -> NoC");
			break;
		}
		numPixelLinha = 120 + roteadoresPorColuna * CDCM_NoC.TILE_SIZE_X;
		numPixelColuna = 205 + roteadoresPorLinha * CDCM_NoC.TILE_SIZE_Y;
		// TODOMPS
		numPixelAltura = 0;// 205 + roteadoresPorAltura*CDCM_NoC.TILE_SIZE_Z;
		if (getWindowPrincipal().ehTopologiaMesh()) {
			numPixelColuna = numPixelColuna - CWM_NoC.CHANNEL_SIZE_X;
			numPixelLinha = numPixelLinha - CWM_NoC.CHANNEL_SIZE_Y;
		}
		numPixelCanvasColuna = numPixelColuna;
		numPixelCanvasLinha = numPixelLinha;
		Dimension dimTela = Toolkit.getDefaultToolkit().getScreenSize();
		// condi��es para inser��o de scroll no Canvas da NoC
		if (dimTela.height <= numPixelColuna) {
			if (dimTela.width <= numPixelLinha) {
				setSize(dimTela.width, dimTela.height);
				numPixelLinha = dimTela.width;
				numPixelColuna = dimTela.height;
			} else {
				setSize(numPixelLinha, dimTela.height);
				numPixelColuna = dimTela.height;
			}
			needScroll = true;
		} else {
			if (dimTela.width <= numPixelLinha) {
				setSize(dimTela.width, numPixelColuna);
				numPixelLinha = dimTela.width;
				needScroll = true;
			} else
				setSize(numPixelLinha, numPixelColuna);
		}

		setLocation(0, 0);
		getContentPane().setLayout(null);
		setMenuBar(new CDCM_MenuNoC(this));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});
		addComponentes();
		setVisible(true);
		setResizable(false);
	}

	public WindowPrincipal getWindowPrincipal() {
		if (noc != null)
			return noc.getMainWindow();
		return WP;
	}

	public int getNumLinhas() {
		return roteadoresPorLinha;
	}

	public int getNumColunas() {
		return roteadoresPorColuna;
	}

	public int getNumAltura() {
		return roteadoresPorAltura;
	}

	public void addComponentes() {

		getGraphics();
		if (noc != null) {
			addControlView();
			addCanvas(10, 10, numPixelLinha, numPixelColuna - 25);
		} else
			addEvolucao(10, numPixelColuna / 2 - 30, numPixelLinha, 50);
	}

	public void addCanvas(int x, int y, int dimx, int dimy) {
		Label energia = new Label(
				" Energy = "
						+ String.format("%.2f", new Double(
								noc.energiaNoC() / 1000))
						+ "uJ  -  Idle Energy = "
						+ String.format("%.2f", new Double(
								noc.getEnergiaIdle() / 1000)) + "uJ");

		energia.setBackground(new Color(80, 80, 80));
		energia.setFont(new Font("Arial", Font.BOLD, 12));
		energia.setForeground(Color.WHITE);
		energia.setBounds(x + 2, dimy - 69, dimx - 29, 28);
		getContentPane().add(energia);
		// Panel Border do desenho da rede
		pbNetwork = new JLayeredPane();
		pbNetwork.setBounds(x, y - 5, dimx - 25, dimy - 80);
		pbNetwork.setBorder(BorderFactory.createTitledBorder("NoC"));
		getContentPane().add(pbNetwork);
		Dnc = new CDCM_DesenhoNoC(noc, x + 10, y + 10,
				numPixelCanvasLinha - 45, numPixelCanvasColuna - 130);
		if (!needScroll)
			pbNetwork.add(Dnc);
		else {
			scroll = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
			scroll.setBounds(x + 10, y + 10, dimx - 45, dimy - 105);
			scroll.add(Dnc);
			pbNetwork.add(scroll);
		}
	}

	public void addEvolucao(int x, int y, int dimx, int dimy) {
		EPos = new EvolucaoAlgoritmoMapeamento(x + 10, y, numPixelLinha - 40,
				dimy);
		getContentPane().add(EPos);
	}

	public CDCM_Sequencia getSequencia() {
		return seq;
	}

	public CDCM_NoC getNoc() {
		return noc;
	}

	public EvolucaoAlgoritmoMapeamento getEvolucaoMapeamento() {
		return EPos;
	}
}
