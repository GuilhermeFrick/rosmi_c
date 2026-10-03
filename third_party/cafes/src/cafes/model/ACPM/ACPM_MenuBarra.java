package cafes.model.ACPM;

import java.awt.*;

class ACPM_MenuBarra extends MenuBar
{
	private static final long serialVersionUID = 6357022002629265616L;

	public ACPM_MenuBarra(ACPM_MappingCost win)
	{
		add(new ACPM_MenuArquivo(win, "File"));
		add(new ACPM_MenuVerticesInicioFim(win, "Special Vertices"));
		add(new ACPM_MenuFerramentas(win, "Tools"));
	}
}
