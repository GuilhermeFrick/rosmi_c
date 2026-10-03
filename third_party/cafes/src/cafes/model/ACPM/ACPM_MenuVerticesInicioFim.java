package cafes.model.ACPM;

import java.awt.*;
import java.awt.event.*;

class ACPM_MenuVerticesInicioFim extends Menu implements ActionListener
{
	private static final long serialVersionUID = 3587557236205481655L;
	private String insereInicio="Insert START vertex";
	private String insereFim="Insert END vertex";
	private MenuItem mInsereInicio, mInsereFim;
	private ACPM_MappingCost win;

	public ACPM_MenuVerticesInicioFim(ACPM_MappingCost win, String titulo)
	{
		super(titulo);
		this.win = win;
		mInsereInicio = new MenuItem(insereInicio);
		mInsereFim = new MenuItem(insereFim);

		add(mInsereInicio);
		add(mInsereFim);

		mInsereInicio.addActionListener(this);
		mInsereFim.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(insereInicio))
		{
			if(win.getSequencia().encontraTagNoGrafo(ACPM_Grafo.START)==null)
			{
				win.getSequencia().insereTagStartGrafo();
				win.update();
			}
		}
		if(e.getActionCommand().equals(insereFim))
		{
			if(win.getSequencia().encontraTagNoGrafo(ACPM_Grafo.END)==null)
			{
				win.getSequencia().insereTagEndGrafo();
				win.update();
			}
		}
	}
}
