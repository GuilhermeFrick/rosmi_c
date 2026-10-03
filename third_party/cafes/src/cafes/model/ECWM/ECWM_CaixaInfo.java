package cafes.model.ECWM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ECWM_CaixaInfo extends Dialog implements ActionListener
{
	private static final long serialVersionUID = -4588230989470087776L;
	private TextField campoNome;
	private Panel painel;
	private Button continuar;
	private String nome;
	private ECWM_Circulo nodo;

	public ECWM_CaixaInfo(JFrame f)
	{
		super(f, "ECWM Vertex");
		caixaInfo(null);
	}
	public ECWM_CaixaInfo(ECWM_Circulo nodo, JFrame f)
	{
		super(f, "ECWM Vertex");
		caixaInfo(nodo);
	}
	private void caixaInfo(ECWM_Circulo n)
	{
		this.nodo = n;
		addWindowListener
		(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{ 
					setNome(null);
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
		if(nodo==null)
			campoNome = new TextField();
		else
			campoNome = new TextField(nodo.getInf());
		addLabelNumber("Core name:", campoNome, xIni, yIni, xFim, deltaY);
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
				nome = String.valueOf(campoNome.getText()).toString();
				if(nome.length()<1 || nome.length()>8)
				{
					JOptionPane.showMessageDialog(null, "A label has to have at least 1 digit and at most 8 digits!", "Error", JOptionPane.ERROR_MESSAGE);
					nome = null;
				}
				if(nome!=null)
					setVisible(false);
			}
		}
		catch(Exception p)
		{
			p.printStackTrace();
		}
	}
	public void setNome(String nome)
	{
		this.nome = nome;
	}
	public String getNome()
	{
		return nome;
	}
}