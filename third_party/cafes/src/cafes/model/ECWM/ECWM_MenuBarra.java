package cafes.model.ECWM;

import java.awt.*;

class ECWM_MenuBarra extends MenuBar
{
	private static final long serialVersionUID = 4446035716029360665L;

	public ECWM_MenuBarra(ECWM_MappingCost win)
	{
		add(new ECWM_MenuArquivo(win, "File"));
		add(new ECWM_MenuFerramentas(win, "Tools"));
	}
}
