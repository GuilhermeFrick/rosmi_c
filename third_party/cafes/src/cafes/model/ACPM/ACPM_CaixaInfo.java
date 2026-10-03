package cafes.model.ACPM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ACPM_CaixaInfo extends Dialog implements ActionListener
{
	private static final long serialVersionUID = -5664631496745791653L;
	private TextField campoCoreOrigem, campoCoreDestino, campoPhits;
	private String coreOrigem, coreDestino;
	private long phits;
	private Panel painel;
	private Button continuar;
	private ACPM_CirculoVertice nodo;

	public ACPM_CaixaInfo(JFrame f)
	{
		super(f, "ACPM Vertex");
		caixaInfo(null);
	}
	public ACPM_CaixaInfo(ACPM_CirculoVertice nodo, JFrame f)
	{
		super(f, "ACPM Vertex");
		caixaInfo(nodo);
	}
	private void caixaInfo(ACPM_CirculoVertice n)
	{
		this.nodo = n;
		addWindowListener(new WindowAdapter()	{
			public void windowClosing(WindowEvent e)	{ 
				setVisible(false);
				paintAll(getGraphics());
			}
		});
		addNewPanel();
		add(painel);
		Dimension resolucao = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-130)/2, (resolucao.height-460)/2);
		setSize(178, 158);
		setModal(true);
		setVisible(true);
		setResizable(false);
	}
	public String getCoreOrigem()
	{
		return coreOrigem;
	}
	public String getCoreDestino()
	{
		return coreDestino;
	}
	public long getPhits()
	{
		return phits;
	}
	private void addLabelNumber(String label, TextField tf, int xi, int yi, int largura, int altura)
	{
		int labelSize = (int)(label.length()*6.3)+6;
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
		if(nodo==null)
		{
			campoCoreOrigem = new TextField(""); 
			campoCoreDestino = new TextField("");
			campoPhits = new TextField("");
		}
		else
		{	
			campoCoreOrigem = new TextField(nodo.getCoreOrigem()); 
			campoCoreDestino = new TextField(nodo.getCoreDestino());
			campoPhits = new TextField(nodo.getStringPhits());
		}
		addLabelNumber("Source core:", campoCoreOrigem, xIni, yIni, xFim, deltaY);
		addLabelNumber("Target core:", campoCoreDestino, xIni, yIni+(deltaY+5), xFim, deltaY);
		addLabelNumber("Phits:", campoPhits, xIni, yIni+2*(deltaY+5), xFim, deltaY);
		continuar = new Button("Enter");
		continuar.setBounds(xIni, yIni+3*(deltaY+5), xFim, deltaY);
		continuar.addActionListener(this);
		painel.add(continuar);
	}
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if(e.getSource().equals(continuar))
			{
				coreOrigem = String.valueOf(campoCoreOrigem.getText()).toString();
				coreDestino = String.valueOf(campoCoreDestino.getText()).toString();
				if(coreOrigem.length()<1 || coreOrigem.length()>8 || coreDestino.length()<1 || coreDestino.length()>8)
				{
					JOptionPane.showMessageDialog(null, "Core mane has to have at least 1 digit and at most 8 digits!", "Error", JOptionPane.ERROR_MESSAGE);
					coreOrigem = null;
				}
				phits = Long.parseLong(campoPhits.getText());
				if(phits<=0)
				{
					JOptionPane.showMessageDialog(null, "Number of phits has to be an integer greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
					phits = 0;
				}
				if(coreOrigem!=null && coreDestino!=null && phits>0)
					setVisible(false);
			}
		}
		catch(NumberFormatException p)
		{
			JOptionPane.showMessageDialog(null, "Phits field have to be integer!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}