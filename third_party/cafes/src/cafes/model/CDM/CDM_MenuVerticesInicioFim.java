package cafes.model.CDM;

import java.awt.*;
import java.awt.event.*;

class CDM_MenuVerticesInicioFim extends Menu implements ActionListener
{
	private static final long serialVersionUID = 26143020884758732L;
	private String insereInicio="Insert START vertex";
	private String insereFim="Insert END vertex";
	private MenuItem mInsereInicio, mInsereFim;
	private CDM_MappingCost win;

	public CDM_MenuVerticesInicioFim(CDM_MappingCost win, String titulo)
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
			if(win.getSequencia().encontraVerticeNoGrafo(CDM_Grafo.START)==null)
			{
				win.getSequencia().insereVerticeGrafo(new CDM_Vertice(CDM_Grafo.START));
				win.update();
			}
		}
		if(e.getActionCommand().equals(insereFim))
		{
			if(win.getSequencia().encontraVerticeNoGrafo(CDM_Grafo.END)==null)
			{
				win.getSequencia().insereVerticeGrafo(new CDM_Vertice(CDM_Grafo.END));
				win.update();
			}
		}
	}
}
