package cafes.model.CDM;

import java.awt.*;

class CDM_MenuBarra extends MenuBar
{
	private static final long serialVersionUID = -181029398555389869L;

	public CDM_MenuBarra(CDM_MappingCost win)
	{
		add(new CDM_MenuArquivo(win, "File"));
		add(new CDM_MenuVerticesInicioFim(win, "Special Vertices"));
		add(new CDM_MenuFerramentas(win, "Tools"));
	}
}
