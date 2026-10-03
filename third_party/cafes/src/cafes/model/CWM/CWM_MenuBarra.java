package cafes.model.CWM;

import java.awt.*;

class CWM_MenuBarra extends MenuBar
{
	private static final long serialVersionUID = -6878871773444070165L;

	public CWM_MenuBarra(CWM_MappingCost win)
	{
		add(new CWM_MenuArquivo(win, "File"));
		add(new CWM_MenuFerramentas(win, "Tools"));
	}
}
