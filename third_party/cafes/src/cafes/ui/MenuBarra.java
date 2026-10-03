package cafes.ui;

import java.awt.*;

class MenuBarra extends MenuBar
{
	private static final long serialVersionUID = -2919284795710757018L;

	public MenuBarra(WindowPrincipal WP)
	{
		add(new MenuArquivo(WP, "File"));
		add(new MenuTools(WP, "Tools"));
		add(new MenuAjuda("Help"));
	}
}

