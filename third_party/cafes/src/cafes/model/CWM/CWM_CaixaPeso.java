package cafes.model.CWM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CWM_CaixaPeso extends Dialog implements ActionListener
{
	private static final long serialVersionUID = 4526690734284450704L;
	private TextField campoPeso;
	private Panel painel;
	private Button continuar;
	private long peso;

	public CWM_CaixaPeso(JFrame f, long peso)
	{
		super(f, "CWM Vertex");
		this.peso = peso;
		addWindowListener
		(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{ 
					setPeso(0);
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
		if(peso>0)
			campoPeso = new TextField(new Long(peso).toString());
		else
			campoPeso = new TextField();
		addLabelNumber("weight:", campoPeso, xIni, yIni, xFim, deltaY);
		continuar = new Button("Enter");
		continuar.setBounds(xIni, yIni+(deltaY+5), xFim, deltaY);
		continuar.addActionListener(this);
		painel.add(continuar);
	}
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if(e.getSource().equals(continuar))
			{
				peso = Long.parseLong(campoPeso.getText());
				if(peso<=0)
				{
					JOptionPane.showMessageDialog(null, "Number of phits has to be an integer greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
					peso = 0;
				}
				else
					setVisible(false);
			}
		}
		catch(NumberFormatException p)
		{
			JOptionPane.showMessageDialog(null, "Use number format!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception p)
		{
			p.printStackTrace();
		}
	}
	public void setPeso(long peso)
	{
		this.peso = peso;
	}
	public long getPeso()
	{
		return peso;
	}
}