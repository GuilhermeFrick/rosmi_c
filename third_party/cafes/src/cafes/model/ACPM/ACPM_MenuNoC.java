package cafes.model.ACPM;

import java.awt.*;

class ACPM_MenuNoC extends MenuBar
{
	private static final long serialVersionUID = -6209307864024132079L;

	public ACPM_MenuNoC(ACPM_WindowNoC win)
	{
		add(new ACPM_MenuArquivoNoC(win, "File"));
		add(new ACPM_MenuFerramentasNoC(win, "Tools"));
	}
}
