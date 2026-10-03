package cafes.model.ACPM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class ACPM_MenuArquivo extends Menu implements ActionListener
{
	private static final long serialVersionUID = -3850362647600873758L;
	private String newGraphStr="New graph";
	private String saveGraphStr="Save graph";
	private String loadGraphStr="Load graph";
	private String exitStr="Exit";
	private MenuItem mNewGraph, mSaveGraph, mLoadGraph, mExit;
	private ACPM_MappingCost win;

	public ACPM_MenuArquivo(ACPM_MappingCost win, String titulo)
	{
		super(titulo);
		this.win = win;
		mNewGraph = new MenuItem(newGraphStr);
		mSaveGraph = new MenuItem(saveGraphStr);
		mLoadGraph = new MenuItem(loadGraphStr);
		mExit = new MenuItem(exitStr);

		add(mNewGraph);
		add(mSaveGraph);
		add(mLoadGraph);
		addSeparator();
		add(mExit);

		mNewGraph.addActionListener(this);
		mSaveGraph.addActionListener(this);
		mLoadGraph.addActionListener(this);
		mExit.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(newGraphStr))
		{
			if(win.isSalvo() || JOptionPane.showConfirmDialog(null, "All data will be lost!\n Save the data before to continue.\nClick OK to continue or CANCEL to return", "Warning", JOptionPane.OK_CANCEL_OPTION)==0)
			{
				win.getSequencia().deleta();
				win.setSalvo();
				win.update();
			}
		}
		if(e.getActionCommand().equals(saveGraphStr))
		{
			ACPM_GrafoFormatoTextual.gravaArquivo(win.getWindowPrincipal(), win, win.getSequencia(), "Saving ACPG", null);
			win.setSalvo();
		}
		if(e.getActionCommand().equals(loadGraphStr))
		{
			if(win.isSalvo() || JOptionPane.showConfirmDialog(null, "All data will be lost!\n Save the data before to continue.\nClick OK to continue or CANCEL to return", "Warning", JOptionPane.OK_CANCEL_OPTION)==0)
			{
				ACPM_GrafoFormatoTextual.leArquivo(win.getWindowPrincipal(), win, win.getSequencia(), "Loading ACPG");
				win.update();
				win.setSalvo();
			}
		}
		if(e.getActionCommand().equals(exitStr))
			win.dispose();
	}
}
