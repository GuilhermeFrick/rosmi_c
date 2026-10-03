package cafes.model.CDCM;

import java.awt.*;
import java.awt.event.*;

class CDCM_MenuArquivoNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = -7200515898986245576L;
	private String appendGraphStr="Append mapping on file";
	private String exitStr="Exit";
	private MenuItem mAppendGraph, mExit;
	private CDCM_WindowNoC win;

	public CDCM_MenuArquivoNoC(CDCM_WindowNoC win, String titulo)
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
			CDCM_GrafoFormatoTextual.appendaMapeamento(win, "Mapping appending", str);
		}
		if(e.getActionCommand().equals(exitStr))
			win.dispose();
	}
}
