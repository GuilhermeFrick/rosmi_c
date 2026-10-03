package cafes.ui;

import java.awt.event.*;

import javax.swing.*;

import java.awt.*;

import cafes.NoC.*;
import cafes.model.ACPM.ACPM_MappingCost;
import cafes.model.CDM.*;
import cafes.model.CWM.*;
//import cafes.model.ACPM.*;
//import cafes.model.ECWM.*;
import cafes.model.CDCM.*;
import cafes.model.ECWM.ECWM_MappingCost;

public class WindowPrincipal extends JFrame implements ActionListener
{
	private static final long serialVersionUID = -9098877568248247134L;
	private static final int larguraCaixa = 345;
	private int xBaseInicial=10, yBaseInicial, yBaseAposLogo, xBase, yBase, xFinal;
	private JButton CWMMapping, ECWMMapping, CDMMapping, CDCMMapping, ACPMMapping, CTMMapping;
	private NoCParameters NoCPar;

	public Color getHighLinkColor(){
		return new Color(79,148,205);
	}
	public WindowPrincipal(String titulo, String fileCommandLine[])
	{
		NoCPar = new NoCParameters();
		NoCPar.setDefaultTopology();
		new CDCM_MappingCost(this, fileCommandLine);
	}
	
	public static int getUnsignedNumber(String text) {
		try {
			int n = Integer.parseInt(text);
			if (n < 0)
				return 0;
			return n;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static Object makeObj(final int item) {
		return new Object() {
			public String toString() {
				return new Integer(item).toString();
			}
		};
	}
	public WindowPrincipal(String titulo)
	{
		super(titulo);
		setSize(785, 525);
		getContentPane().setLayout(null);
		addWindowListener
		(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					System.exit(0);
				}
			}
		);
		setMenuBar(new MenuBarra(this));
		NoCPar = new NoCParameters();

		yBaseInicial = yBase = 5;
		insereLogoCafes();
		yBaseAposLogo = yBase;

// Insere painel de Ferramentas
		xBase = xBaseInicial + 10;
		yBase += 20;
		xFinal = 380;
		insereFerramenta(CWMMapping = new JButton("Communication Weight Model (CWM)"));
		insereFerramenta(ECWMMapping = new JButton("Extended Communication Weight Model (ECWM)"));
		insereFerramenta(CDMMapping = new JButton("Communication Dependence Model (CDM)"));
		insereFerramenta(CDCMMapping = new JButton("Communication Dependence and Computation Model (CDCM)"));
		insereFerramenta(ACPMMapping = new JButton("Application Communication Pattern Model (ACPM)"));
		insereFerramenta(CTMMapping = new JButton("Communication Task Model (CTM)"));
		insereBordaFerramenta(new JLayeredPane(), "Application Models");

// Insere Par�metros
		insereParametrosTopologiaNoc();
		insereParametrosTempoNoc();
		insereParametrosDeEnergia();

		setVisible(true);
		setResizable(false);
	}
	private void insereLogoCafes()	// Cria e insere o Logotipo do programa
	{
		JLayeredPane logotipo = new JLayeredPane();
		logotipo.setBounds(xBaseInicial+25, yBase, 150, 70);
		logotipo.setBorder(BorderFactory.createRaisedBevelBorder());
		getContentPane().add(logotipo);

		CanvasGif CG = new CanvasGif();
		CG.setBounds(xBaseInicial, yBase, 130, 60);
		logotipo.add(CG);
		yBase += 120;
	}
	private void insereFerramenta(JButton botao)
	{
		botao.setBackground(new Color(0, 0, 80));
		botao.setForeground(Color.WHITE);
		botao.setFont(new Font("Arial", Font.BOLD, 12));
		botao.setBounds(xBase, yBase, xFinal, 30);
		botao.addActionListener(this);
		getContentPane().add(botao);
		yBase = yBase + 30;
	}
	private void insereBordaFerramenta(JLayeredPane ferramentas, String nome)
	{
		xFinal = xFinal + 20;
		ferramentas.setBounds(xBaseInicial, yBaseAposLogo, xFinal, yBase-yBaseAposLogo+10);
		ferramentas.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), nome));
		getContentPane().add(ferramentas);
		yBase = yBase + 30;
	}
	private void insereParametrosTopologiaNoc()
	{
		xBaseInicial = xFinal + 20;
		xBase = xBaseInicial + 10;
		yBaseInicial = yBase = 5;
		yBase += 20;
		insereDimensaoNoC("NoC size (lines.columns.height)");
		insereDimensaoTile("Tile size (width.height.depth)");
		insereTamanhoBuffer("Buffer length");
		insereTopologia();
		inserePainelParametros(new JLayeredPane(), "NoC Topology Parameters");
	}
	private void insereParametrosTempoNoc()
	{
		yBase += 20;
		int yBaseSaved = yBase;
		yBase += 20;
		insereTempoRelogio("Clock cycle frequency");
		insereCiclosLink("Number of cycles for linking");
		insereCiclosRoteamento("Number of cycles for routing");
		yBaseInicial = yBaseSaved;
		inserePainelParametrosTempo(new JLayeredPane(), "NoC Timing Parameters");
	}
	private void inserePainelParametrosTempo(JLayeredPane parametros, String nome)
	{
		parametros.setBounds(xBaseInicial, yBaseInicial, larguraCaixa, yBase-yBaseInicial+10);
		parametros.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), nome));
		getContentPane().add(parametros);
	}
	private void insereLabel(String name, int xi, int yi, int largura, int altura)
	{
		Label l = new Label(name);
		l.setFont(new Font("Arial", Font.BOLD, 12));
		l.setBounds(xi, yi, largura, altura);
		getContentPane().add(l);
	}
	private void insereTextField(TextField tf, int xi, int yi, int largura, int altura)
	{
		tf.setBounds(xi, yi, largura, altura);
		getContentPane().add(tf);
	}
	private void insereDimensaoNoC(String nome)
	{
		insereLabel(nome, xBase, yBase, 180, 20);
		insereTextField(NoCPar.getNumLinhasTextField(), xBase+180, yBase, 30, 20);
		insereLabel(".", xBase+212, yBase, 4, 20);
		insereTextField(NoCPar.getNumColunasTextField(), xBase+220, yBase, 30, 20);
		insereLabel(".", xBase+252, yBase, 4, 20);
		insereTextField(NoCPar.getNumAlturaTextField(), xBase+260, yBase, 30, 20);
		yBase += 25;
	}
	private void insereDimensaoTile(String nome)
	{
		insereLabel(nome, xBase, yBase, 180, 20);
		insereTextField(NoCPar.getTileWidthTextField(), xBase+180, yBase, 30, 20);
		insereLabel(".", xBase+212, yBase, 4, 20);
		insereTextField(NoCPar.getTileHeigthTextField(), xBase+220, yBase, 30, 20);
		insereLabel(".", xBase+252, yBase, 4, 20);
		insereTextField(NoCPar.getTileLongitudinalTextField(), xBase+260, yBase, 30, 20);
		
		yBase += 25;
	}
	private void insereTamanhoBuffer(String nome)
	{
		int largura=110;

		insereLabel(nome, xBase, yBase, largura, 20);
		insereTextField(NoCPar.getBufferSizeTextField(), xBase+largura+50, yBase, 60, 20);
		insereLabel("(phits)", xBase+224, yBase, 40, 20);
		yBase += 25;
	}
	private void insereTempoRelogio(String nome)
	{
		int largura=130;

		insereLabel(nome, xBase, yBase, largura, 20);
		insereTextField(NoCPar.getClockCycleTextField(), xBase+largura+40, yBase, 50, 20);
		insereLabel("(MHz)", xBase+224, yBase, 35, 20);
		yBase += 25;
	}
	private void insereCiclosLink(String nome)
	{
		int largura=170;

		insereLabel(nome, xBase, yBase, largura, 20);
		insereTextField(NoCPar.getLinkingCyclesTextField(), xBase+largura+20, yBase, 30, 20);
		yBase += 25;
	}
	private void insereCiclosRoteamento(String nome)
	{
		int largura=170;

		insereLabel(nome, xBase, yBase, largura, 20);
		insereTextField(NoCPar.getRoutingCyclesTextField(), xBase+largura+20, yBase, 30, 20);
		yBase += 25;
	}
	private void insereTopologia()
	{
		JComboBox topo = new JComboBox();

		insereLabel("Topology", xBase, yBase, 56, 20);
		NoCPar.insereTopologia(topo, xBase+67, yBase, 255, 20);
		getContentPane().add(topo);
		topo.addActionListener(this);
		yBase += 25;
	}
	private void inserePainelParametros(JLayeredPane parametros, String nome)
	{
		parametros.setBounds(xBaseInicial, yBaseInicial, larguraCaixa, yBase-yBaseInicial+10);
		parametros.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), nome));
		getContentPane().add(parametros);
	}
	protected void insereParametrosDeEnergia()
	{
		int ySaved = yBase + 20;

		insereParametrosDeEnergiaWithTrafficSwitching();

		yBase = ySaved + 20;
		xBase = xBase + 155;
		insereParametrosDeEnergiaWithoutTrafficSwitching();
		insereParametrosDeEnergiaIdle();

		xBaseInicial = xFinal + 20;
		yBaseInicial = ySaved;
		inserePainelParametrosEnergia(new JLayeredPane(), "NoC Energy Parameters");
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected void insereParametrosDeEnergiaWithTrafficSwitching()
	{
		yBaseInicial = yBase = yBase + 20;
		yBase += 20;
		insereItalicLabel("	 Without Transition");
		insereElPhit("ElPhit");
		insereEcPhit("EcPhit");
		insereEsPhit("EsPhit");
		insereEbPhit("EbPhit");
		insereEtPhit("EtPhit");
	}
	private void insereItalicLabel(String nome)
	{
		Label l = new Label(nome);

		l.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
		l.setBounds(xBase, yBase, nome.length()*5+6, 24);
		getContentPane().add(l);
		yBase += 25;
	}
	private void insereElPhit(String nome)
	{
		insereLabel(nome, xBase, yBase, 38, 20);
		insereTextField(NoCPar.getEnergiaLinkPhitTextField(), xBase+42, yBase, 50, 20);
		insereLabel("(nJ/mm)", xBase+94, yBase, 50, 20);
		yBase += 25;
	}
	private void insereEcPhit(String nome)
	{
		insereLabel(nome, xBase, yBase, 38, 20);
		insereTextField(NoCPar.getEnergiaLocalLinkPhitTextField(), xBase+42, yBase, 50, 20);
		insereLabel("(nJ)", xBase+94, yBase, 27, 20);
		yBase += 25;
	}
	private void insereEsPhit(String nome)
	{
		insereLabel(nome, xBase, yBase, 38, 20);
		insereTextField(NoCPar.getEnergiaControlePhitTextField(), xBase+42, yBase, 50, 20);
		insereLabel("(nJ)", xBase+94, yBase, 27, 20);
		yBase += 25;
	}
	private void insereEbPhit(String nome)
	{
		insereLabel(nome, xBase, yBase, 38, 20);
		insereTextField(NoCPar.getEnergiaBufferPhitTextField(), xBase+42, yBase, 50, 20);
		insereLabel("(nJ)", xBase+94, yBase, 27, 20);
		yBase += 25;
	}
	
	private void insereEtPhit(String nome)
	{
		insereLabel(nome, xBase, yBase, 38, 20);
		insereTextField(NoCPar.getEnergiaBufferLongitudinalPhitTextField(), xBase+42, yBase, 50, 20);
		insereLabel("(nJ)", xBase+94, yBase, 27, 20);
		yBase += 25;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected void insereParametrosDeEnergiaWithoutTrafficSwitching()
	{
		insereItalicLabel("	With Transition");
		insereElS_Phit("ElS_Phit");
		insereEcS_Phit("EcS_Phit");
		insereEsS_Phit("EsS_Phit");
		insereEbS_Phit("EbS_Phit");
		insereEtS_Phit("EtS_Phit");
	}
	private void insereElS_Phit(String nome)
	{
		insereLabel(nome, xBase, yBase, 52, 20);
		insereTextField(NoCPar.getEnergiaLinkComChaveamentoPhitTextField(), xBase+56, yBase, 50, 20);
		insereLabel("(nJ/mm)", xBase+108, yBase, 50, 20);
		yBase += 25;
	}
	private void insereEcS_Phit(String nome)
	{
		insereLabel(nome, xBase, yBase, 52, 20);
		insereTextField(NoCPar.getEnergiaLocalLinkComChaveamentoPhitTextField(), xBase+56, yBase, 50, 20);
		insereLabel("(nJ)", xBase+108, yBase, 27, 20);
		yBase += 25;
	}
	private void insereEsS_Phit(String nome)
	{
		insereLabel(nome, xBase, yBase, 52, 20);
		insereTextField(NoCPar.getEnergiaControleComChaveamentoPhitTextField(), xBase+56, yBase, 50, 20);
		insereLabel("(nJ)", xBase+108, yBase, 27, 20);
		yBase += 25;
	}
	private void insereEbS_Phit(String nome)
	{
		insereLabel(nome, xBase, yBase, 52, 20);
		insereTextField(NoCPar.getEnergiaBufferComChaveamentoPhitTextField(), xBase+56, yBase, 50, 20);
		insereLabel("(nJ)", xBase+108, yBase, 27, 20);
		yBase += 25;
	}
	private void insereEtS_Phit(String nome)
	{
		insereLabel(nome, xBase, yBase, 52, 20);
		insereTextField(NoCPar.getEnergiaLocalLinkComChaveamentoPhitLongitudinalTextField(), xBase+56, yBase, 50, 20);
		insereLabel("(nJ)", xBase+108, yBase, 27, 20);
		yBase += 25;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected void insereParametrosDeEnergiaIdle()
	{
		//insereItalicLabel();
		inserePRouter("Idle PRouter");
	}
	private void inserePRouter(String nome)
	{
//		Label l = new Label("		 Idle");
//
//		l.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
//		l.setBounds(xBase-50, yBase, nome.length()*5+6, 24);
//		getContentPane().add(l);

		xBase-=75;
		insereLabel(nome, xBase-20, yBase, 70, 20);
		insereTextField(NoCPar.getPotRoteadorTextField(), xBase+56, yBase, 50, 20);
		insereLabel("(mW)", xBase+108, yBase, 33, 20);
		yBase += 25;
	}
	private void inserePainelParametrosEnergia(JLayeredPane parametros, String nome)
	{
		parametros.setBounds(xBaseInicial, yBaseInicial, larguraCaixa, yBase-yBaseInicial+10);
		parametros.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), nome));
		getContentPane().add(parametros);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public NoCParameters getNoCPar()
	{
		return NoCPar;
	}
	public int getNumColunas()
	{
		return NoCPar.getNumColunas();
	}
	public int getNumLinhas()
	{
		return NoCPar.getNumLinhas();
	}
	public int getNumAltura()
	{
		return NoCPar.getNumAltura();
	}
	public int getTileLongitudinal(){
		return NoCPar.getTileLongitudinal();
	}
	public int getTileWidth()
	{
		return NoCPar.getTileWidth();
	}
	public int getTileHeigth()
	{
		return NoCPar.getTileHeigth();
	}
	public int getBufferSize()
	{
		return NoCPar.getBufferSize();
	}
	public double getClockCycle()
	{
		return NoCPar.getClockCycle();
	}
	public double getEnergiaLinkLongitudinalPhit(){
		return NoCPar.getEnergiaLinkLongitudinalPhit();
	}
	public double getEnergiaLocalLinkComChaveamentoLongitudinalPhit() {
		return NoCPar.getEnergiaLocalLinkComChaveamentoLongitudinalPhit();
	}
	public double getEnergiaLinkPhit()
	{
		return NoCPar.getEnergiaLinkPhit();
	}
	public double getEnergiaLinkComChaveamentoPhit()
	{
		return NoCPar.getEnergiaLinkComChaveamentoPhit();
	}
	public double getEnergiaLocalLinkPhit()
	{
		return NoCPar.getEnergiaLocalLinkPhit();
	}
	public double getEnergiaLocalLinkComChaveamentoPhit()
	{
		return NoCPar.getEnergiaLocalLinkComChaveamentoPhit();
	}
	public double getEnergiaControlePhit()
	{
		return NoCPar.getEnergiaControlePhit();
	}
	public double getEnergiaControleComChaveamentoPhit()
	{
		return NoCPar.getEnergiaControleComChaveamentoPhit();
	}
	public double getEnergiaBufferPhit()
	{
		return NoCPar.getEnergiaBufferPhit();
	}
	public double getEnergiaBufferComChaveamentoPhit()
	{
		return NoCPar.getEnergiaBufferComChaveamentoPhit();
	}
	public double getPotRoteador()
	{
		return NoCPar.getPotRoteador();
	}
	public int getLinkingCycles()
	{
		return NoCPar.getLinkingCycles();
	}
	public int getRoutingCycles()
	{
		return NoCPar.getRoutingCycles();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public boolean ehTopologiaMesh()
	{
		return NoCPar.ehTopologiaMesh();
	}
	public boolean ehTopologiaTorus()
	{
		return NoCPar.ehTopologiaTorus();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void setParametrosDeEnergiaDefault()
	{
		NoCPar.setParametrosDeEnergiaDefault();
	}
	public void setNumColunas(String str)
	{
		NoCPar.setNumColunas(str);
	}
	public void setNumAltura(String str)
	{
		NoCPar.setNumAltura(str);
	}
	public void setNumLinhas(String str)
	{
		NoCPar.setNumLinhas(str);
	}
	public void setTileWidth(String str)
	{
		NoCPar.setTileWidth(str);
	}
	public void setTileHeigth(String str)
	{
		NoCPar.setTileHeigth(str);
	}
	public void setTileLongitudinal(String str)
	{
		NoCPar.setTileLongitudinal(str);
	}
	public void setBufferSize(String str)
	{
		NoCPar.setBufferSize(str);
	}
	public void setClockCycle(String str)
	{
		NoCPar.setClockCycle(str);
	}
	public void setEnergiaLinkPhit(String str)
	{
		NoCPar.setEnergiaLinkPhit(str);
	}
	public void setEnergiaLinkComChaveamentoPhit(String str)
	{
		NoCPar.setEnergiaLinkComChaveamentoPhit(str);
	}
	public void setEnergiaLocalLinkPhit(String str)
	{
		NoCPar.setEnergiaLocalLinkPhit(str);
	}
	public void setEnergiaLocalLinkComChaveamentoPhit(String str)
	{
		NoCPar.setEnergiaLocalLinkComChaveamentoPhit(str);
	}
	public void setEnergiaControlePhit(String str)
	{
		NoCPar.setEnergiaControlePhit(str);
	}
	public void setEnergiaControleComChaveamentoPhit(String str)
	{
		NoCPar.setEnergiaControleComChaveamentoPhit(str);
	}
	public void setEnergiaBufferPhit(String str)
	{
		NoCPar.setEnergiaBufferPhit(str);
	}
	public void setEnergiaBufferComChaveamentoPhit(String str)
	{
		NoCPar.setEnergiaBufferComChaveamentoPhit(str);
	}
	public void setPotRoteador(String str)
	{
		NoCPar.setPotRoteador(str);
	}
	public void setLinkingCycles(String str)
	{
		NoCPar.setLinkingCycles(str);
	}
	public void setRoutingCycles(String str)
	{
		NoCPar.setRoutingCycles(str);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().getClass()==JComboBox.class)
		{
			JComboBox cb = (JComboBox)e.getSource();
			String str = (String)cb.getSelectedItem();

			NoCPar.setTopologia(str);
		}
		if(e.getSource().getClass()==JButton.class)
		{
			if(e.getSource().equals(CWMMapping))
				new CWM_MappingCost(this, 0, 0, 1024, 768);
			if(e.getSource().equals(ECWMMapping))
				new ECWM_MappingCost(this, 0, 0, 1024, 768);
			if(e.getSource().equals(CDMMapping))
				new CDM_MappingCost(this, 0, 0, 1024, 768);
			if(e.getSource().equals(CDCMMapping))
				new CDCM_MappingCost(this, 0, 0, 1024, 768);
			if(e.getSource().equals(ACPMMapping))
				new ACPM_MappingCost(this, 0, 0, 1024, 768);
			if(e.getSource().equals(CTMMapping))
			{
				JOptionPane.showMessageDialog(null, "Model under construction!", "Information", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
}

