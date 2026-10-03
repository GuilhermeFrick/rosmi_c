package cafes.model.ACPM;

import java.awt.*;
import java.awt.event.*;

class ACPM_MenuFerramentasNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = 3415959818947086929L;
	private String executionTimeStr="Execution Time";
	private MenuItem ExecutionTime;
	private ACPM_WindowNoC win;

	public ACPM_MenuFerramentasNoC(ACPM_WindowNoC win, String titulo)
	{
		super(titulo);
		this.win = win;
		ExecutionTime = new MenuItem(executionTimeStr);
		add(ExecutionTime);
		ExecutionTime.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(executionTimeStr))
			new ACPM_AnaliseTemporal(win, win.getSequencia(), win.getNoc());
	}
}
