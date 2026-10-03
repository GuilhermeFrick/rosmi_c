package cafes.model.ECWM;

import java.awt.*;
import java.awt.event.*;

class ECWM_MenuArquivoNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = -8371547720990457338L;
	private String appendGraphStr="Append mapping on file";
	private String exitStr="Exit";
	private MenuItem mAppendGraph, mExit;
	private ECWM_WindowNoC win;

	public ECWM_MenuArquivoNoC(ECWM_WindowNoC win, String titulo)
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
			ECWM_GrafoFormatoTextual.appendaMapeamento(win, "Mapping appending", str);
		}
		if(e.getActionCommand().equals(exitStr))
			win.dispose();
	}
}
