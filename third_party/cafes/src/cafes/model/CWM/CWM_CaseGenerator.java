package cafes.model.CWM;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;
import javax.swing.*;

import cafes.ui.WindowPrincipal;
import cafes.common.Randomico;

class CWM_CaseGenerator extends JFrame implements ActionListener
{
	private static final long serialVersionUID = -2362346517348182383L;
	private static String net_sizeStr = "#_NoC_Size";
	private static String nodesStr = "#_CWG_Vertices";
//	private static String GraphicStr = "#_CWG_Graphic";
	private static String EdgeStr = "#_CWG_Edges";
//	private static String placementStr = "#_CWG2NoC_Mapping";
	private static String paragrafoStr = "\n";
	private static String paragrafo3Str = "\n\n";	
	private String where;
	private WindowPrincipal wp;
	private CWM_DesenhaSequencia desSeq;
	private JLayeredPane jlPaneNoC, jlPaneVertex;
	private TextField numeroLinhas, numeroColunas;
	
	//	Numero de vértices é igual ou menor que o (número de linhas* número de colunas) definido anteriormente 	
	private TextField numeroVertices;
	
	//	Peso mínimo de arestas é maior q 0 ou +.
	//	Não há um valor preciso de peso máximo
	private TextField pesoMinimoArestas, pesoMaximoArestas;

	//	Numero mínimo de arestas é 1 ou +.
	//	Numero maximo de arestas é igual ao número de vertices.
	private TextField numeroMinimoArestas, numeroMaximoArestas;
	
	private JButton jbOK;

	public CWM_CaseGenerator(WindowPrincipal _wp, int IniX, int IniY)
	{
		super("CWM Case Generator");
		setSize(350, 300);
		where="";
		this.wp=_wp;

		setLocation(IniX, IniY);
		getContentPane().setLayout(null);
		addWindowListener
		(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					dispose();
				}
			}
		);
		insertNoCParameters();
		insertVertexParameters();
		insertButtons();
		setVisible(true);
		setResizable(false);
	}
	
	private Label insertLabel(String name, int xi, int yi, int largura, int altura)
	{
		Label l = new Label(name);
		l.setFont(new Font("Arial", Font.BOLD, 12));
		l.setBounds(xi, yi, largura, altura);
		getContentPane().add(l);
		return l;
	}
	
	private TextField insertTextField(TextField tf, int xi, int yi, int largura, int altura)
	{
		tf.setBounds(xi, yi, largura, altura);
		tf.addActionListener(this);
		getContentPane().add(tf);
		return tf;
	}
	
	private void insertPanel(JLayeredPane parametros, String nome, int _xLocation, int _yLocation, int _width, int _heigth)	
	{
		parametros.setBounds(_xLocation, _yLocation, _width, _heigth);
		parametros.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), nome));
		getContentPane().add(parametros);
	}

	private void insertButton(JButton jb, int xi, int yi, int largura, int altura, String texto)
	{
		jb.setBackground(new Color(0, 0, 80));
		jb.setForeground(Color.WHITE);
		jb.setFont(new Font("Arial", Font.BOLD, 12));
		jb.setBounds(xi, yi, largura, altura);
		jb.setText(texto);
		jb.addActionListener(this);
		getContentPane().add(jb);
	}

	private void insertNoCParameters()
	{
		int xBase=0, yBase=0;
		Label l;
		TextField tf;
		numeroLinhas = new TextField();
		numeroColunas = new TextField();
		
		l=insertLabel("NoC Size:", xBase+10, yBase+20, 70, 20);		
		tf=insertTextField(numeroLinhas, l.getX()+l.getWidth()+2, l.getY(), 30, 20);
		tf.setText(wp.getNoCPar().getNumLinhasStr());
		l=insertLabel("(lines)", tf.getX()+tf.getWidth()+2, tf.getY(), 50, 20);
		
		tf=insertTextField(numeroColunas, tf.getX(), tf.getY()+tf.getHeight()+2, 30, 20);
		tf.setText(wp.getNoCPar().getNumColunasStr());
		l=insertLabel("(columns)", tf.getX()+tf.getWidth()+2, tf.getY(), 60, 20);
		
		jlPaneNoC= new JLayeredPane();
		
		insertPanel(jlPaneNoC, "NoC Size Parameter", xBase, yBase, l.getX()+l.getWidth()+10, l.getY()+l.getHeight()+10);		 
	}
	
	private void insertVertexParameters()
	{
		int xBase, yBase;
		xBase=0;
		yBase=jlPaneNoC.getY()+jlPaneNoC.getHeight()+2;
		
		Label l;
		TextField tf;

		numeroVertices = new TextField();
		pesoMinimoArestas = new TextField();
		pesoMaximoArestas = new TextField();
		numeroMinimoArestas = new TextField();
		numeroMaximoArestas = new TextField();		

		l=insertLabel("Vertex amount:", xBase+10, yBase+20, 100, 20);
		tf=insertTextField(numeroVertices, l.getX()+l.getWidth()+2, yBase+20, 30, 20);		 
		tf.setText(""+wp.getNoCPar().getNumColunas()*wp.getNoCPar().getNumLinhas());
		l=insertLabel("(lower or equal to lines*columns)", tf.getX()+tf.getWidth()+2, yBase+20, 170, 20);
		
		l=insertLabel("Amount of Edges:", xBase+10, l.getY()+l.getHeight()+6, 100, 20);
		tf=insertTextField(numeroMinimoArestas, l.getX()+l.getWidth()+2, l.getY(), 30, 20);
		tf.setText("1");
		l=insertLabel("(minimum entering the vertex)", tf.getX()+tf.getWidth()+2, tf.getY(), 180, 20);
		tf=insertTextField(numeroMaximoArestas, tf.getX(), tf.getY()+tf.getHeight()+2, 30, 20);
		tf.setText(""+wp.getNoCPar().getNumColunas()*wp.getNoCPar().getNumLinhas());
		l=insertLabel("(maximum leaving the vertex)", tf.getX()+tf.getWidth()+2, tf.getY(), 180, 20);
		
		l=insertLabel("Communication Weigth:", xBase+10, l.getY()+l.getHeight()+6, 140, 20); //weight!!!!
		tf=insertTextField(pesoMinimoArestas, l.getX()+l.getWidth()+2, l.getY(), 50, 20);
		tf.setText("10");
		l=insertLabel("(minimum weigth)", tf.getX()+tf.getWidth()+2, tf.getY(), 120, 20);
		tf=insertTextField(pesoMaximoArestas, tf.getX(), tf.getY()+tf.getHeight()+2, 50, 20);
		tf.setText("99999");
		l=insertLabel("(maximum weigth)", tf.getX()+tf.getWidth()+2, tf.getY(), 120, 20);

		jlPaneVertex=new JLayeredPane();
		insertPanel(jlPaneVertex, "Vertex Parameters", xBase, yBase, l.getX()+l.getWidth()+10, 150);
	}
	
	private void insertButtons()
	{
		jbOK = new JButton();
		insertButton(jbOK,jlPaneVertex.getX()+((jlPaneVertex.getWidth()-160)/2),jlPaneVertex.getY()+jlPaneVertex.getHeight()+10,160,30, "Generate CWM Graph");
	}
	
	public CWM_DesenhaSequencia getDesenhaSequencia()
	{
		return desSeq;
	}
	public boolean isSalvo()
	{
		return desSeq.isSalvo();
	}
	public void setSalvo(boolean val)
	{
		desSeq.setSalvo(val);
	}
	public void update()
	{
		desSeq.update();
	}
	public CWM_Sequencia getSequencia()
	{
		return desSeq.getSequencia();
	}
	public void setDesenhaSequencia(CWM_DesenhaSequencia dseq)
	{
		desSeq = dseq;
	}

	public void actionPerformed(ActionEvent e)
	{
	  if(e.getSource().getClass()==JButton.class)
	  {
	 		if(e.getSource().equals(jbOK))
	 		{
	 			if(allFieldsAreNotEmpty() && allFieldsAreCoherent())
	 			{
	 				generateGraph();
	 			}	 			
	 		}
	  }
	}
	
	private boolean allFieldsAreNotEmpty()
	{
		String text=new String("");
		if(numeroLinhas.getText().length()==0)
			text += "  NoC - Number of lines \n"; 
		if(numeroColunas.getText().length()==0)
			text += "  NoC - Number of columns \n"; 
		if(numeroVertices.getText().length()==0)
			text += "  Vertex - Number of lines \n"; 
		if(numeroMinimoArestas.getText().length()==0)
			text += "  NoC - Number of lines \n"; 
		if(numeroMaximoArestas.getText().length()==0)
			text += "  NoC - Number of lines \n"; 
		if(pesoMinimoArestas.getText().length()==0)
			text += "  NoC - Number of lines \n"; 
		if(pesoMaximoArestas.getText().length()==0)
			text += "  NoC - Number of lines";
		if(text.length()!=0)
		{
			text="The following Fields are empty:\n"+text;
			JOptionPane.showMessageDialog(null, text, "Fields Empty", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private boolean allFieldsAreCoherent()
	{
		
		numeroVertices.setBackground(Color.WHITE);
		numeroColunas.setBackground(Color.WHITE);
		numeroLinhas.setBackground(Color.WHITE);
		numeroMaximoArestas.setBackground(Color.WHITE);
		numeroMinimoArestas.setBackground(Color.WHITE);
		pesoMaximoArestas.setBackground(Color.WHITE);
		pesoMinimoArestas.setBackground(Color.WHITE);
		
		if(Integer.parseInt(numeroVertices.getText())>(Integer.parseInt(numeroColunas.getText())*Integer.parseInt(numeroLinhas.getText())))
		{
			numeroVertices.setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "The number of vertices is greater then the number of positions.", "Coherency Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}		
		
		if(Integer.parseInt(numeroMinimoArestas.getText())<=0)
		{
			numeroMinimoArestas.setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "The minimum number of Edges has to be greater or equal to 1 number .", "Coherency Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}		

		if(Integer.parseInt(numeroMaximoArestas.getText())>Integer.parseInt(numeroVertices.getText()))
		{
			numeroMaximoArestas.setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "The maximum number of Edges has to be lower or equal to the number of vertices.", "Coherency Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if(Integer.parseInt(pesoMinimoArestas.getText())<=0)
		{
			pesoMinimoArestas.setBackground(Color.RED);
			JOptionPane.showMessageDialog(null, "The minimum weigth has to be greater or equal to 1 number.", "Coherency Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	private void generateGraph()
	{
		String temp=new String("");
		FileDialog d = new FileDialog(this,"Define file name", FileDialog.SAVE);
		d.setFile("*.CWG");
		if(where.equals(""))
			d.setDirectory(".");
		else
			d.setDirectory(where);
		d.setVisible(true);
		
		where=d.getDirectory();
		int minAresta, maxAresta, minPeso, maxPeso, numVertices;
		minAresta=Integer.parseInt(numeroMinimoArestas.getText());
		maxAresta=Integer.parseInt(numeroMaximoArestas.getText());
		minPeso=Integer.parseInt(pesoMinimoArestas.getText());
		maxPeso=Integer.parseInt(pesoMaximoArestas.getText());
		numVertices=Integer.parseInt(numeroVertices.getText());
		
		if(d.getFile()==null)
			return;

		String fileS = d.getDirectory() + d.getFile();
		File file = new File(fileS);
		FileOutputStream fileOut;
		
		try
		{
			String net_sizeStrWrite = new String(net_sizeStr + " (lines columns)");
			String nodesStrWrite = new String(nodesStr + " (list of: vertices)");
//			String GraphicStrWrite = new String(GraphicStr + " (list of: core x y)");
			String EdgeStrWrite = new String(EdgeStr + " (list of: sourceVertex - targetVertex numberOfPhitsTransmited)");
//			String placementStrWrite = new String("\n" + placementStr + " (matrix of: vertices)");

			fileOut = new FileOutputStream(file);
			fileOut.write(net_sizeStrWrite.getBytes());
			String dim = "\n " + numeroLinhas.getText() + " " + numeroColunas.getText();
			fileOut.write(dim.getBytes());
			fileOut.write(paragrafo3Str.getBytes());

//			fileOut.write(GraphicStrWrite.getBytes());
			
//			fileOut.write(paragrafo3Str.getBytes());
			
			fileOut.write(nodesStrWrite.getBytes());
			temp="\n";
			for(int i=0; i<numVertices; i++)
				temp+=" T"+i+"\n";
			fileOut.write(temp.getBytes());
			fileOut.write(paragrafo3Str.getBytes());			
			fileOut.write(EdgeStrWrite.getBytes());
			fileOut.write(paragrafoStr.getBytes());
			temp = graphGen(numVertices,minAresta,maxAresta,minPeso,maxPeso);
			fileOut.write(temp.getBytes());
			fileOut.write(paragrafoStr.getBytes());
			/* 
			if(placement!=null)
			{
				fileOut.write(placementStrWrite.getBytes());
				fileOut.write(placement.getBytes());
			}
			*/
			fileOut.close();
		}
		catch(Exception e) 
		{
			System.out.println("Problems during " + fileS + " generation.");
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	private String graphGen(int _numVertices, int _minAresta, int _maxAresta, int _minPeso, int _maxPeso)
	{
		String _result="";
		Vector<String> mainV = new Vector<String>(_numVertices);
		Vector<String> copyV = new Vector<String>(_numVertices);
		Randomico rand=new Randomico();
		int arestas, peso, target;
		
		for(int i=0; i<_numVertices; i++)
			mainV.add("T" + i);
		
		for(int index = 0; index < _numVertices; index++)
		{
			copyV =(Vector<String>)mainV.clone();
			copyV.removeElementAt(index);

			arestas=rand.randomNumber(_minAresta, _maxAresta);

			int maxIndex = copyV.size() - 1;
			while(arestas > 0)
			{
				target=rand.randomNumber(maxIndex);
				peso=rand.randomNumber(_minPeso,_maxPeso);
				System.out.println("maxInex: " + maxIndex + "  target: " +target);
				_result+=" T"+index+" - "+copyV.get(target)+" "+peso+"\n";
				copyV.removeElementAt(target);
				arestas--;
				maxIndex--;
			}
			
			copyV.removeAllElements();
		}
		return _result;
	}
	
}
	
	
