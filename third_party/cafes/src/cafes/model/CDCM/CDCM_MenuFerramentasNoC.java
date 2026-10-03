package cafes.model.CDCM;

import java.awt.*;
import java.awt.event.*;

class CDCM_MenuFerramentasNoC extends Menu implements ActionListener
{
	private static final long serialVersionUID = 3054597419544184593L;
	private String executionTimeStr="Execution Time";
	private MenuItem ExecutionTime;
	private String ConverteCDCMParaVHDLStr="CDCM to VHDL";
	private MenuItem ConverteCDCMParaVHDL;
	private CDCM_WindowNoC win;

	public CDCM_MenuFerramentasNoC(CDCM_WindowNoC win, String titulo)
	{
		super(titulo);
		this.win = win;
		ExecutionTime = new MenuItem(executionTimeStr);
		add(ExecutionTime);

		addSeparator();
		ConverteCDCMParaVHDL = new MenuItem(ConverteCDCMParaVHDLStr);
		add(ConverteCDCMParaVHDL);
		ConverteCDCMParaVHDL.addActionListener(this);

		ExecutionTime.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(executionTimeStr))
		{
			new CDCM_AnaliseTemporal(win, win.getSequencia(), win.getNoc());
		}
		if(e.getActionCommand().equals(ConverteCDCMParaVHDLStr))
		{
			win.getSequencia().percorreListaDeNiveisGeraVHDL(win);
			return;
		}
	}
}
