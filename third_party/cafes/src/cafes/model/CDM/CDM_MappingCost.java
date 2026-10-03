package cafes.model.CDM;

import cafes.ui.WindowPrincipal;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import cafes.model.CWM.*;

public class CDM_MappingCost extends JFrame
{
	private static final long serialVersionUID = -7807370023590160578L;
	private WindowPrincipal WP;
	private CDM_DesenhaSequencia desSeq;
	private ScrollPane SP;
	private int iniPanelX, iniPanelY, fimPanelX, fimPanelY;

	public CDM_MappingCost(WindowPrincipal WP, int IniX, int IniY, int numPixelColuna, int numPixelLinha)
	{
		super("CDM Mapping");
		this.WP = WP;
		iniPanelX = IniX + 10;
		iniPanelY = IniY + 10;
		fimPanelX = numPixelColuna - 20;
		fimPanelY = numPixelLinha - 70;

		setLocation(IniX, IniY);
		getContentPane().setLayout(null);
		setMenuBar(new CDM_MenuBarra(this));
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
	private void insereScrollPanel()
	{
		int ScrollEmX=2000, ScrollEmY=2000;
		
		getGraphics();
		SP = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
		SP.setBounds(iniPanelX, iniPanelY, fimPanelX, fimPanelY);
		desSeq = new CDM_DesenhaSequencia(this, iniPanelX-8, iniPanelY-8, fimPanelX-20+ScrollEmX, fimPanelY-20+ScrollEmY);
		SP.add(desSeq);
		getContentPane().add(SP);
	}
	public WindowPrincipal getWindowPrincipal()
	{
		return WP;
	}
	public CDM_DesenhaSequencia getDesenhaSequencia()
	{
		return desSeq;
	}
	public boolean isSalvo()
	{
		return desSeq.isSalvo();
	}
	public void setSalvo()
	{
		desSeq.setSalvo();
	}
	public void setNaoSalvo()
	{
		desSeq.setNaoSalvo();
	}
	public void update()
	{
		desSeq.update();
	}
	public CDM_Sequencia getSequencia()
	{
		return desSeq.getSequencia();
	}
	public void setDesenhaSequencia(CDM_DesenhaSequencia dseq)
	{
		desSeq = dseq;
	}
	public void CDM2CWM(CWM_MappingCost mapCost)
	{
		desSeq.CDM2CWM(mapCost.getDesenhaSequencia());
	}
	public int getIniX()
	{
		return iniPanelX;
	}
	public int getIniY()
	{
		return iniPanelY;
	}
	public int getFimX()
	{
		return fimPanelX;
	}
	public int getFimY()
	{
		return fimPanelY;
	}
}
