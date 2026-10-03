package cafes.model.CDM;

import java.awt.*;
import java.awt.event.*;

class CDM_MenuFerramentasNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = -6555741610977252249L;
	private String minimumExecutionTimeStr="Execution Time Without Contention";
	private MenuItem MinimumExecutionTime;
	private String executionTimeStr="Execution Time (Worst case)";
	private MenuItem ExecutionTime;
	private CDM_WindowNoC win;

	public CDM_MenuFerramentasNoC(CDM_WindowNoC win, String titulo)
	{
		super(titulo);
		this.win = win;
		MinimumExecutionTime = new MenuItem(minimumExecutionTimeStr);
		add(MinimumExecutionTime);
		MinimumExecutionTime.addActionListener(this);
		ExecutionTime = new MenuItem(executionTimeStr);
		add(ExecutionTime);
		ExecutionTime.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(minimumExecutionTimeStr))
		{
			new CDM_AnaliseTemporal(win, win.getSequencia(), win.getNoc(), false);
		}
		if(e.getActionCommand().equals(executionTimeStr))
		{
			new CDM_AnaliseTemporal(win, win.getSequencia(), win.getNoc(), true);
		}
	}
}
