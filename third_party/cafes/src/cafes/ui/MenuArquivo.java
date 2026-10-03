package cafes.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import cafes.common.*;
import cafes.NoC.*;

class MenuArquivo extends Menu implements ActionListener
{
	private static final long serialVersionUID = 5048511665137271068L;
	private String defEnergyPar = "Default parameters";
	private String saveEnergyPar = "Save parameters";
	private String loadEnergyPar = "Load parameters";
	private String exit = "Exit";
	private MenuItem mNovo, mGravar, mLer, mSair;
	private WindowPrincipal WP;

	public MenuArquivo(WindowPrincipal WP, String titulo)
	{
		super(titulo);
		this.WP = WP;

		mNovo = new MenuItem(defEnergyPar);
		mGravar = new MenuItem(saveEnergyPar);
		mLer = new MenuItem(loadEnergyPar);
		mSair = new MenuItem(exit);

		add(mNovo);
		add(mGravar);
		add(mLer);
		addSeparator();
		add(mSair);

		mNovo.addActionListener(this);
		mGravar.addActionListener(this);
		mLer.addActionListener(this);
		mSair.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(defEnergyPar))
		{
			if(JOptionPane.showConfirmDialog(null, "All data will be lost!\n Save the data before to continue.\nClick OK to continue or CANCEL to return", "Warning", JOptionPane.OK_CANCEL_OPTION) == 0)
			{
				WP.setParametrosDeEnergiaDefault();
				WP.repaint();
			}
		}
		if(e.getActionCommand().equals(saveEnergyPar))
		{
			FileDialog d = new FileDialog(WP, "NoC parameters saving", FileDialog.SAVE);

			d.setVisible(true);
			if(d.getFile() == null)
				return;
			write(d.getDirectory() + d.getFile());
		}
		if(e.getActionCommand().equals(loadEnergyPar))
		{
			FileDialog d = new FileDialog(WP, "NoC parameters loading", FileDialog.LOAD);
			NoCParameters np;

			d.setVisible(true);
			if(d.getFile() == null)
				return;
			np = read(d.getDirectory() + d.getFile());
			if(np == null)
				return;
			WP.setNumColunas(np.getNumColunasStr());
			WP.setNumLinhas(np.getNumLinhasStr());
			WP.setNumAltura(np.getNumAlturaStr());
			WP.setTileWidth(np.getTileWidthStr());
			WP.setTileHeigth(np.getTileHeigthStr());
			WP.setTileLongitudinal(np.getTileLongitudinalStr());
			WP.setBufferSize(np.getBufferSizeStr());
			WP.setClockCycle(np.getClockCycleStr());
			WP.setEnergiaLinkPhit(np.getEnergiaLinkPhitStr());
			WP.setEnergiaLinkComChaveamentoPhit(np.getEnergiaLinkComChaveamentoPhitStr());
			WP.setEnergiaLocalLinkPhit(np.getEnergiaLocalLinkPhitStr());
			WP.setEnergiaLocalLinkComChaveamentoPhit(np.getEnergiaLocalLinkComChaveamentoPhitStr());
			WP.setEnergiaControlePhit(np.getEnergiaControlePhitStr());
			WP.setEnergiaControleComChaveamentoPhit(np.getEnergiaControleComChaveamentoPhitStr());
			WP.setEnergiaBufferPhit(np.getEnergiaBufferPhitStr());
			WP.setEnergiaBufferComChaveamentoPhit(np.getEnergiaBufferComChaveamentoPhitStr());
			WP.setPotRoteador(np.getPotRoteadorStr());
			WP.setLinkingCycles(np.getLinkingCyclesStr());
			WP.setRoutingCycles(np.getRoutingCyclesStr());
			WP.repaint();
		}
		if(e.getActionCommand().equals(exit))
			System.exit(0);
	}
	private void write(String file)
	{
		IO_Object.write(file, WP.getNoCPar());
	}
	private NoCParameters read(String file)
	{
		return (NoCParameters) IO_Object.read(file);
	}
}
