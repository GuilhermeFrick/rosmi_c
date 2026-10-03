package cafes.model.ECWM;

import java.awt.*;
import java.awt.event.*;

class ECWM_MenuFerramentasNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = 1551771083035440526L;
	private String minimumExecutionTimeStr="Minimum Execution Time";
	private MenuItem MinimumExecutionTime;
	private ECWM_WindowNoC win;

	public ECWM_MenuFerramentasNoC(ECWM_WindowNoC win, String titulo)
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
			new ECWM_AnaliseTemporal(win, win.getNoc(), win.getSequencia(), win.getWindowPrincipal().ehTopologiaMesh());
	}
}
