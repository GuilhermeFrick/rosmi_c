package cafes.model.CDCM;

import java.awt.*;

class CDCM_MenuNoC extends MenuBar
{
	private static final long serialVersionUID = 6697620484419912605L;

	public CDCM_MenuNoC(CDCM_WindowNoC win)
	{
		add(new CDCM_MenuArquivoNoC(win, "File"));
		add(new CDCM_MenuFerramentasNoC(win, "Tools"));
	}
}
