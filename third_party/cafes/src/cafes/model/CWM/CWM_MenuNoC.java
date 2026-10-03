package cafes.model.CWM;

import java.awt.*;

class CWM_MenuNoC extends MenuBar
{
	private static final long serialVersionUID = 7727959577576509113L;

	public CWM_MenuNoC(CWM_WindowNoC win)
	{
		add(new CWM_MenuArquivoNoC(win, "File"));
		add(new CWM_MenuFerramentasNoC(win, "Tools"));
	}
}
