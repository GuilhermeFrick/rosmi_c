package cafes.model.CDCM;

import java.awt.*;

class CDCM_MenuBarra extends MenuBar
{
	private static final long serialVersionUID = -7149793332790359493L;

	public CDCM_MenuBarra(CDCM_MappingCost win)
	{
		add(new CDCM_MenuArquivo(win, "File"));
		add(new CDCM_MenuVerticesInicioFim(win, "Special Vertices"));
		add(new CDCM_MenuFerramentas(win, "Tools"));
	}
}
