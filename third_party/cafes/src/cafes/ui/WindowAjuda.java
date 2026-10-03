package cafes.ui;

import java.awt.event.*;
import javax.swing.*;

class WindowAjuda extends JFrame
{
	private static final long serialVersionUID = -7602677508420537328L;

	public WindowAjuda(JTextPane textoAjuda)
	{
		super();
		setSize(350, 250);
		setLocation(180, 80);
		addWindowListener
		(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					setVisible(false);
				}
			}
		);
		textoAjuda.setEditable(false);
		textoAjuda.setAutoscrolls(true);
		getContentPane().add(textoAjuda);
		setVisible(true);
		setResizable(false);
	}
}