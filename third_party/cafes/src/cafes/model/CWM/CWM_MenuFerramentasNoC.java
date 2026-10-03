package cafes.model.CWM;

import java.awt.*;
import java.awt.event.*;

class CWM_MenuFerramentasNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = -5942286658320495511L;
	private String minimumExecutionTimeStr="Minimum Execution Time";
	private MenuItem MinimumExecutionTime;
	private CWM_WindowNoC win;

	public CWM_MenuFerramentasNoC(CWM_WindowNoC win, String titulo)
	{
		super(titulo);
		this.win = win;
		MinimumExecutionTime = new MenuItem(minimumExecutionTimeStr);
		add(MinimumExecutionTime);
		MinimumExecutionTime.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(minimumExecutionTimeStr))
			new CWM_AnaliseTemporal(win, win.getNoc(), win.getSequencia(), win.getWindowPrincipal().ehTopologiaMesh());
	}
}
