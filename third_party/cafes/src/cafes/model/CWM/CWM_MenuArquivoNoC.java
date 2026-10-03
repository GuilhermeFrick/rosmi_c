package cafes.model.CWM;

import java.awt.*;
import java.awt.event.*;

class CWM_MenuArquivoNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = 3368033363799437063L;
	private String appendGraphStr="Append mapping on file";
	private String exitStr="Exit";
	private MenuItem mAppendGraph, mExit;
	private CWM_WindowNoC win;

	public CWM_MenuArquivoNoC(CWM_WindowNoC win, String titulo)
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
			CWM_GrafoFormatoTextual.appendaMapeamento(win, "Mapping appending", str);
		}
		if(e.getActionCommand().equals(exitStr))
			win.dispose();
	}
}
