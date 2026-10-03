package cafes.model.CTM;

import java.awt.*;
import java.awt.event.*;

class MenuEval extends Menu implements ActionListener
{
	private static final long serialVersionUID = 3456999975716673768L;
	private MenuItem geracaoCWG, analisePos;

	public MenuEval(String titulo)
	{
		super(titulo);

		geracaoCWG = new MenuItem("CWG generation");
		analisePos = new MenuItem("Mapping analysis");

		add(geracaoCWG);
		add(analisePos);

		geracaoCWG.addActionListener(this);
		analisePos.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)	{  }
}

