package cafes.model.CDM;

import java.awt.*;

class CDM_MenuNoC extends MenuBar
{
	private static final long serialVersionUID = -8114380040403509542L;

	public CDM_MenuNoC(CDM_WindowNoC win)
	{
		add(new CDM_MenuArquivoNoC(win, "File"));
		add(new CDM_MenuFerramentasNoC(win, "Tools"));
	}
}
