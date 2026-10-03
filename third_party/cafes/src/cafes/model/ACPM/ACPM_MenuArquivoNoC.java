package cafes.model.ACPM;

import java.awt.*;
import java.awt.event.*;

class ACPM_MenuArquivoNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = -86682685405197935L;
	private String appendGraphStr="Append mapping on file";
	private String exitStr="Exit";
	private MenuItem mAppendGraph, mExit;
	private ACPM_WindowNoC win;

	public ACPM_MenuArquivoNoC(ACPM_WindowNoC win, String titulo)
	{
		super(titulo);
		this.win = win;
		mAppendGraph = new MenuItem(appendGraphStr);
		mExit = new MenuItem(exitStr);

		add(mAppendGraph);
		addSeparator();
		add(mExit);

		mAppendGraph.addActionListener(this);
		mExit.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(appendGraphStr))
		{
			String str = win.getNoc().imprimeNoC(); 
			ACPM_GrafoFormatoTextual.appendaMapeamento(win, "Mapping appending", str);
		}
		if(e.getActionCommand().equals(exitStr))
			win.dispose();
	}
}
