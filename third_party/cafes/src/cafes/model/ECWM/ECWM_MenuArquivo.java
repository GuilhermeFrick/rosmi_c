package cafes.model.ECWM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class ECWM_MenuArquivo extends Menu implements ActionListener
{
	private static final long serialVersionUID = -7677456528764437064L;
	private String newGraphStr="New graph";
	private String saveGraphStr="Save graph";
	private String loadGraphStr="Load graph";
	private String exitStr="Exit";
	private MenuItem mNewGraph, mSaveGraph, mLoadGraph, mExit;
	private ECWM_MappingCost win;

	public ECWM_MenuArquivo(ECWM_MappingCost win, String titulo)
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
			ECWM_GrafoFormatoTextual.gravaArquivo(win.getWindowPrincipal(), win, win.getSequencia(), "Saving ECWG", null);
			win.setSalvo();
		}
		if(e.getActionCommand().equals(loadGraphStr))
		{
			if(win.isSalvo() || JOptionPane.showConfirmDialog(null, "All data will be lost!\n Save the data before to continue.\nClick OK to continue or CANCEL to return", "Warning", JOptionPane.OK_CANCEL_OPTION)==0)
			{
				ECWM_GrafoFormatoTextual.leArquivo(win.getWindowPrincipal(), win, win.getSequencia(), "Loading ECWG");
				win.setSalvo();
				win.update();
			}
		}
		if(e.getActionCommand().equals(exitStr))
			win.dispose();
	}
}
