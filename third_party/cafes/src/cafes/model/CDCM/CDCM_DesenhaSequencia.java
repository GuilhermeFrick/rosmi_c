package cafes.model.CDCM;

import java.awt.*;
import java.awt.event.*;
import cafes.model.CDM.*;
import cafes.model.CWM.*;

class CDCM_DesenhaSequencia extends Canvas implements MouseListener, KeyListener
{
	private static final long serialVersionUID = 4488543768052347593L;
	private CDCM_Sequencia seq;	  // Objeto que contém todas as informações do grafo
	private static boolean shiftPressed; // Variável para monitorar a tecla shift
	private int iniX, iniY, fimX, fimY;
	private boolean estaSalvo = true;
	
	public CDCM_DesenhaSequencia(CDCM_MappingCost win)
	{
		this.seq = new CDCM_Sequencia(this, win);
		seq.insereVerticeStartGrafo();
		seq.insereVerticeEndGrafo();
	}
	public CDCM_DesenhaSequencia(CDCM_MappingCost win, int iniX, int iniY, int fimX, int fimY)
	{
		this.iniX = iniX;
		this.iniY = iniY;
		this.fimX = fimX;
		this.fimY = fimY;
		this.seq = new CDCM_Sequencia(this, win);
		desenhaSequenciaCDCM();
	}
	public CDCM_Sequencia getSequencia()
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
	private void desenhaSequenciaCDCM()
	{
		shiftPressed = false;
		setBounds(iniX, iniY, fimX, fimY);
		setBackground(new Color(140, 140, 140));
		addMouseListener(this);
		addKeyListener(this);
		setVisible(true);
		seq.insereVerticeStartGrafo();
		seq.insereVerticeEndGrafo();
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
	public void CDCM2CWM(CWM_DesenhaSequencia dSeq)
	{
		seq.CDCM2CWM(dSeq.getSequencia());
	}
	public void CDCM2CDM(CDM_DesenhaSequencia dSeq)
	{
		seq.CDCM2CDM(dSeq.getSequencia());
	}
	public void executaASAPeCriaListaDeNiveis()
	{		
		seq.executaASAPeCriaListaDeNiveis(true);
	}
	public void executaAnaliseTemporal()
	{
		seq.executaAnaliseTemporal();
	}
	public void removeAnaliseTemporal()
	{		
		seq.removeAnaliseTemporal();
	}
	public void mousePressed(MouseEvent e)
	{
		if(seq.pontoSobreSeta(e.getX(), e.getY()))
		{
			seq.editaSetaExistente(e.getX(), e.getY());
			update();
			return;
		}
		if(shiftPressed==false)
			seq.setaVerticeSelecionado(e.getX(), e.getY());
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
		seq.editaNodoExistente(seq.getVerticeSelecionado());
		update();
	}
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode()==KeyEvent.VK_SHIFT)
			shiftPressed = true;
		if(e.getKeyCode()==KeyEvent.VK_DELETE)
		{
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
		if(seq.getListaComputacao()!=null)
			executaAnaliseTemporal();
		seq.coloreGrafo(g);
	}
}