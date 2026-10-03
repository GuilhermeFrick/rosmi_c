package cafes.model.ECWM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ECWM_CaixaPeso extends Dialog implements ActionListener
{
	private static final long serialVersionUID = 2696183673697977327L;
	private TextField campoPhits, campoChaveamento;
	private long phits;
	private double percentualChaveamentoPhits;
	private Panel painel;
	private Button continuar;

	public ECWM_CaixaPeso(JFrame f, long phits, double percentualChaveamentoPhits)
	{
		super(f, "ECWM Arrow");
		this.phits = phits;
		this.percentualChaveamentoPhits = percentualChaveamentoPhits;
		addWindowListener
		(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{ 
					setPhits(0);
					setPercentualChaveamentoPhits(0);
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
		if(phits>0)
			campoPhits = new TextField(new Long(phits).toString());
		else
			campoPhits = new TextField();
		addLabelNumber("Phits:", campoPhits, xIni, yIni, xFim, deltaY);
		if(percentualChaveamentoPhits>0)
			campoChaveamento = new TextField(new Double(percentualChaveamentoPhits).toString());
		else
			campoChaveamento = new TextField();
		addLabelNumber("Phit variation (%):", campoChaveamento, xIni, yIni+(deltaY+5), xFim, deltaY);
		continuar = new Button("Enter");
		continuar.setBounds(xIni, yIni+2*(deltaY+5), xFim, deltaY);
		continuar.addActionListener(this);
		painel.add(continuar);
	}
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if(e.getSource().equals(continuar))
			{
				phits = Long.parseLong(campoPhits.getText());
				percentualChaveamentoPhits = Double.valueOf(campoChaveamento.getText()).doubleValue();
				if(phits<=0 || percentualChaveamentoPhits<0.0 || percentualChaveamentoPhits>100.0)
				{
					JOptionPane.showMessageDialog(null, "Number of phits has to be an integer greater than 0, and switching percentage has to be in range [0, 100]!", "Error", JOptionPane.ERROR_MESSAGE);
					phits = 0;
					percentualChaveamentoPhits = 0.0;
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
	public void setPhits(long phits)
	{
		this.phits = phits;
	}
	public long getPhits()
	{
		return phits;
	}
	public void setPercentualChaveamentoPhits(double percentualChaveamentoPhits)
	{
		this.percentualChaveamentoPhits = percentualChaveamentoPhits;
	}
	public double getPercentualChaveamentoPhits()
	{
		return percentualChaveamentoPhits;
	}
}