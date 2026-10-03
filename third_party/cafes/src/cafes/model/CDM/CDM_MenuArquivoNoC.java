package cafes.model.CDM;

import java.awt.*;
import java.awt.event.*;

class CDM_MenuArquivoNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = 1368673426197034945L;
	private String appendGraphStr="Append mapping on file";
	private String exitStr="Exit";
	private MenuItem mAppendGraph, mExit;
	private CDM_WindowNoC win;

	public CDM_MenuArquivoNoC(CDM_WindowNoC win, String titulo)
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
			CDM_GrafoFormatoTextual.appendaMapeamento(win, "Mapping appending", str);
		}
		if(e.getActionCommand().equals(exitStr))
			win.dispose();
	}
}
