package cafes.model.CDCM;

import java.awt.*;
import java.awt.event.*;

public class CDCM_CaixaInfoSeta extends Dialog implements ActionListener
{
	private static final long serialVersionUID = 1079524376607969102L;
	private CDCM_MappingCost mapCost;
	private Panel painel;
	private Button cancelar, deletar;
	private CDCM_Vertice vertice, verticeDependente;

	public CDCM_CaixaInfoSeta(CDCM_Vertice vertice, CDCM_Vertice verticeDependente, CDCM_MappingCost mapCost)
	{
		super(mapCost, "CDCM Arrow");

		this.mapCost = mapCost;
		this.vertice = vertice;
		this.verticeDependente = verticeDependente;
		addWindowListener
		(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{ 
					setVisible(false);
					paintAll(getGraphics());
				}
			}
		);
		addNewPanel();
		add(painel);
		Dimension resolucao = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-130)/2, (resolucao.height-460)/2);
		setSize(168, 70);
		setModal(true);
		setVisible(true);
		setResizable(false);
	}
	private void addNewPanel()
	{
		int xIni=10, yIni=10, deltaY=25;
		
		painel = new Panel();
		painel.setLayout(null);
		cancelar = new Button("Cancel");
		cancelar.setBounds(xIni, yIni, xIni+60, deltaY);
		cancelar.addActionListener(this);
		painel.add(cancelar);

		deletar = new Button("Delete");
		deletar.setBounds(xIni+70, yIni, xIni+60, deltaY);
		deletar.addActionListener(this);
		painel.add(deletar);
	}
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if(e.getSource().equals(cancelar))
				setVisible(false);
			if(e.getSource().equals(deletar))
			{
				vertice.deletaVerticeDependente(verticeDependente);
				setVisible(false);
				mapCost.getDesenhaSequencia().setNaoSalvo();
			}
		}
		catch(Exception p)
		{
			p.printStackTrace();
		}
	}
}