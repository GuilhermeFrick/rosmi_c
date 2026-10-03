package cafes.model.CDCM;

import java.awt.*;
import java.awt.event.*;

class CDCM_MenuVerticesInicioFim extends Menu implements ActionListener
{
	private static final long serialVersionUID = 5368248221172276801L;
	private String insereInicio="Insert START vertex";
	private String insereFim="Insert END vertex";
	private MenuItem mInsereInicio, mInsereFim;
	private CDCM_MappingCost win;

	public CDCM_MenuVerticesInicioFim(CDCM_MappingCost win, String titulo)
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
			if(win.getSequencia().encontraVerticeNoGrafo(CDCM_Grafo.START)==null)
			{
				win.getSequencia().insereVerticeGrafo(new CDCM_Vertice(CDCM_Grafo.START));
				win.update();
			}
		}
		if(e.getActionCommand().equals(insereFim))
		{
			if(win.getSequencia().encontraVerticeNoGrafo(CDCM_Grafo.END)==null)
			{
				win.getSequencia().insereVerticeGrafo(new CDCM_Vertice(CDCM_Grafo.END));
				win.update();
			}
		}
	}
}
