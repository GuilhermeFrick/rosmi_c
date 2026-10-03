package cafes.model.CWM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CWM_CaixaInfoSeta extends Dialog implements ActionListener
{
	private static final long serialVersionUID = 1800998784250231822L;
	private TextField campoNome;
	private Panel painel;
	private Button continuar, deletar;
	private String valor;
	private CWM_VerticeAdjacente verticeAdjacente;

	public CWM_CaixaInfoSeta(CWM_VerticeAdjacente verticeAdjacente, CWM_MappingCost mapCost)
	{
		super(mapCost, "CWM Arrow");
		this.verticeAdjacente = verticeAdjacente;
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
		setSize(178, 98);
		setModal(true);
		setVisible(true);
		setResizable(false);
	}
	private void addLabelNumber(String label, TextField tf, int xi, int yi, int largura, int altura)
	{
		int labelSize = label.length()*7;
		Label l = new Label(label);
		
		l.setBounds(xi, yi, labelSize, altura);
		painel.add(l);
		tf.setBounds(xi+labelSize, yi, largura-labelSize, altura);
		tf.addActionListener(this);
		painel.add(tf);
	}
	private void addNewPanel()
	{
		int xIni=10, xFim=150, yIni=10, deltaY=25;
		
		painel = new Panel();
		painel.setLayout(null);
		campoNome = new TextField(verticeAdjacente.getStrPhits());
		addLabelNumber("arrow value:", campoNome, xIni, yIni, xFim, deltaY);
		continuar = new Button("Enter");
		continuar.setBounds(xIni, yIni+(deltaY+5), xIni+50, deltaY);
		continuar.addActionListener(this);
		painel.add(continuar);

		deletar = new Button("Delete");
		deletar.setBounds(xIni+70, yIni+(deltaY+5), xIni+70, deltaY);
		deletar.addActionListener(this);
		painel.add(deletar);
	}
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if(e.getSource().equals(continuar))
			{
				valor = String.valueOf(campoNome.getText()).toString();
				if(valor.length()<1 || valor.length()>8)
				{
					JOptionPane.showMessageDialog(null, "A label has to have at least 1 digit and at most 8 digits!", "Error", JOptionPane.ERROR_MESSAGE);
					valor = null;
				}
				if(valor!=null)
					setVisible(false);
			}
			if(e.getSource().equals(deletar))
			{
				valor = new String("deletar");
				setVisible(false);
			}
		}
		catch(Exception p)
		{
			p.printStackTrace();
		}
	}
	public void setValor(String nome)
	{
		this.valor = nome;
	}
	public String getValor()
	{
		return valor;
	}
}