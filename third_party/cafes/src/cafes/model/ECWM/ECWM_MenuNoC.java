package cafes.model.ECWM;

import java.awt.*;

class ECWM_MenuNoC extends MenuBar
{
	private static final long serialVersionUID = 624515330542674935L;

	public ECWM_MenuNoC(ECWM_WindowNoC win)
	{
		add(new ECWM_MenuArquivoNoC(win, "File"));
		add(new ECWM_MenuFerramentasNoC(win, "Tools"));
	}
}
