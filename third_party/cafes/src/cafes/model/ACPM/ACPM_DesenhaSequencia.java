package cafes.model.ACPM;

import java.awt.*;
import java.awt.event.*;
import cafes.model.CWM.*;

class ACPM_DesenhaSequencia extends Canvas implements MouseListener, KeyListener
{
	private static final long serialVersionUID = -4584032957456518788L;
	private ACPM_Sequencia seq;	// Objeto que contém todas as informações do grafo
	private static boolean shiftPressed;	// Variável para monitorar a tecla shift
	private int iniX, iniY, fimX, fimY;
	private boolean estaSalvo = true;
	
	public ACPM_DesenhaSequencia(ACPM_MappingCost win)
	{
		desenhaSequenciaACPM(new ACPM_Sequencia(this, win));
	}
	public ACPM_DesenhaSequencia(ACPM_MappingCost win, int iniX, int iniY, int fimX, int fimY)
	{
		this.iniX = iniX;
		this.iniY = iniY;
		this.fimX = fimX;
		this.fimY = fimY;
		desenhaSequenciaACPM(new ACPM_Sequencia(this, win));
	}
	public ACPM_Sequencia getSequencia()
	{
		return seq;
	}
	public int getMaxColuna()
	{
		return fimX;
	}
	public int getMaxLinha()
	{
		return fimY;
	}
	private void desenhaSequenciaACPM(ACPM_Sequencia sequencia)
	{
		this.seq = sequencia;
		shiftPressed = false;
		setBounds(iniX, iniY, fimX, fimY);
		setBackground(new Color(140, 140, 140));
		addMouseListener(this);
		addKeyListener(this);
		setVisible(true);
		seq.insereTagStartGrafo();
		seq.insereTagEndGrafo();
	}
	public boolean isSalvo()
	{
		return estaSalvo;
	}
	public void setSalvo()
	{
		this.estaSalvo = true;
	}
	public void setNaoSalvo()
	{
		this.estaSalvo = false;
	}
	public void update()
	{
		paint(getGraphics());
		repaint();
	}
	public void ACPM2CWM(CWM_DesenhaSequencia dSeq)
	{
		seq.ACPM2CWM(dSeq.getSequencia());
	}
	public void mousePressed(MouseEvent e)
	{
		if(shiftPressed==false)
		{
			seq.setaVerticeSelecionado(e.getX(), e.getY());
			seq.setaTagSelecionada(e.getX(), e.getY());
		}
		seq.insereSequencia(e.getX(), e.getY());
		shiftPressed = false;
		update();
	}
	public void mouseReleased(MouseEvent e)
	{
		seq.moveNodoSequencia(e.getX(), e.getY());
	}
	public void mouseClicked(MouseEvent e)
	{ 
		if(e.getClickCount()==2)
			mouseDoubleClick(e);
	}
	public void mouseExited(MouseEvent e)	{   }
	public void mouseEntered(MouseEvent e)	{   }
	public void mouseDoubleClick(MouseEvent e)
	{
		if(seq.getVerticeSelecionado()!=null)
		{
			seq.editaNodoExistente(seq.getVerticeSelecionado());
			update();
		}
	}
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode()==KeyEvent.VK_SHIFT)
			shiftPressed = true;
		if(e.getKeyCode()==KeyEvent.VK_DELETE)
		{
			seq.deletaTag(seq.getTagSelecionada());
			seq.deletaVertice(seq.getVerticeSelecionado());
			update();
		}
		if(e.getKeyCode()==KeyEvent.VK_ENTER)
		{
			seq.editaNodoExistente(seq.getVerticeSelecionado());
			update();
		}
	}
	public void keyReleased(KeyEvent e)
	{
		if(e.getKeyCode()==KeyEvent.VK_SHIFT)
			shiftPressed = false;
	}
	public void keyTyped(KeyEvent e) {   }
	public void paint(Graphics g)
	{
		seq.exibeNodos(g);		//desenha os nodos do grafo
	}
}