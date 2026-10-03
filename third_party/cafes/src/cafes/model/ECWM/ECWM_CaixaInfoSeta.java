package cafes.model.ECWM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ECWM_CaixaInfoSeta extends Dialog implements ActionListener
{
	private static final long serialVersionUID = -7473537998016606344L;
	private TextField campoPhits, campoChaveamento;
	private String valorPhits, valorChaveamento;
	private Panel painel;
	private Button continuar, deletar;
	private ECWM_VerticeAdjacente verticeAdjacente;

	public ECWM_CaixaInfoSeta(ECWM_VerticeAdjacente verticeAdjacente, ECWM_MappingCost mapCost)
	{
		super(mapCost, "ECWM Arrow");

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
		setSize(200, 130);
		setModal(true);
		setVisible(true);
		setResizable(false);
	}
	private void addLabelNumber(String label, TextField tf, int xi, int yi, int largura, int altura)
	{
		int labelSize = label.length()*6;
		Label l = new Label(label);
		
		l.setBounds(xi, yi, labelSize, altura);
		painel.add(l);
		tf.setBounds(xi+labelSize, yi, largura-labelSize, altura);
		tf.addActionListener(this);
		painel.add(tf);
	}
	private void addNewPanel()
	{
		int xIni=10, xFim=170, yIni=10, deltaY=25;
		
		painel = new Panel();
		painel.setLayout(null);
		campoPhits = new TextField(verticeAdjacente.getStrPhits());
		addLabelNumber("Phits:", campoPhits, xIni, yIni, xFim, deltaY);
		campoChaveamento = new TextField(verticeAdjacente.getStrPercentualChaveamentoPhits());
		addLabelNumber("Phit variation (%):", campoChaveamento, xIni, yIni+(deltaY+5), xFim, deltaY);
		continuar = new Button("Enter");
		continuar.setBounds(xIni, yIni+2*(deltaY+5), xIni+70, deltaY);
		continuar.addActionListener(this);
		painel.add(continuar);

		deletar = new Button("Delete");
		deletar.setBounds(xIni+80, yIni+2*(deltaY+5), xIni+80, deltaY);
		deletar.addActionListener(this);
		painel.add(deletar);
	}
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if(e.getSource().equals(continuar))
			{
				valorPhits = String.valueOf(campoPhits.getText()).toString();
				valorChaveamento = String.valueOf(campoChaveamento.getText()).toString();
				if(valorPhits.length()<1 || valorPhits.length()>8 || valorChaveamento.length()<1 || valorChaveamento.length()>8)
				{
					JOptionPane.showMessageDialog(null, "A label has to have at least 1 digit and at most 8 digits!", "Error", JOptionPane.ERROR_MESSAGE);
					valorPhits = null;
					valorChaveamento = null;
				}
				if(valorPhits!=null && valorChaveamento!=null )
					setVisible(false);
			}
			if(e.getSource().equals(deletar))
			{
				valorPhits = new String("deletar");
				valorChaveamento = new String("deletar");
				setVisible(false);
			}
		}
		catch(Exception p)
		{
			p.printStackTrace();
		}
	}
	public void setValorPhits(String nome)
	{
		this.valorPhits = new String(nome);
	}
	public String getValorPhits()
	{
		return valorPhits;
	}
	public void setValorChaveamento(String nome)
	{
		this.valorChaveamento = new String(nome);
	}
	public String getValorChaveamento()
	{
		return valorChaveamento;
	}
}