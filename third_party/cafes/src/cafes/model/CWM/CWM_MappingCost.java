package cafes.model.CWM;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import cafes.ui.WindowPrincipal;

public class CWM_MappingCost extends JFrame
{
	private static final long serialVersionUID = 3797306137901650244L;
	private WindowPrincipal WP;
	private CWM_DesenhaSequencia desSeq;
	private ScrollPane SP;
	private int iniPanelX, iniPanelY, fimPanelX, fimPanelY;
	private boolean displayGraph;

	public CWM_MappingCost(WindowPrincipal WP, int IniX, int IniY, int numPixelColuna, int numPixelLinha)
	{
		super("CWM Mapping");
		this.WP = WP;
		displayGraph = true;
		iniPanelX = IniX + 10;
		iniPanelY = IniY + 10;
		fimPanelX = numPixelColuna - 20;
		fimPanelY = numPixelLinha - 70;

		setLocation(IniX, IniY);
		getContentPane().setLayout(null);
		setMenuBar(new CWM_MenuBarra(this));
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
		insereScrollPanel();
		setSize(numPixelColuna, numPixelLinha);
		setVisible(true);
		setResizable(false);
	}
	
	public void setDisplayGraph(boolean _visible)
	{
		displayGraph = _visible;
	}
	
	public boolean getDisplayGraph()
	{
		return displayGraph;
	}
	
	private void insereScrollPanel()
	{
		int ScrollEmX=2000, ScrollEmY=2000;
		
		getGraphics();
		SP = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
		SP.setBounds(iniPanelX, iniPanelY, fimPanelX, fimPanelY);
		desSeq = new CWM_DesenhaSequencia(this, iniPanelX-8, iniPanelY-8, fimPanelX-20+ScrollEmX, fimPanelY-20+ScrollEmY);
		SP.add(desSeq);
		getContentPane().add(SP);
	}
	public WindowPrincipal getWindowPrincipal()
	{
		return WP;
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
		if(displayGraph)
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
}
