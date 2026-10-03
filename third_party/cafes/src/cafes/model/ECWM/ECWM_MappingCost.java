package cafes.model.ECWM;

import cafes.ui.WindowPrincipal;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

public class ECWM_MappingCost extends JFrame
{
	private static final long serialVersionUID = -4059155750109800929L;
	private WindowPrincipal WP;
	private ECWM_DesenhaSequencia desSeq;
	private ScrollPane SP;
	private int iniPanelX, iniPanelY, fimPanelX, fimPanelY;

	public ECWM_MappingCost(WindowPrincipal WP, int IniX, int IniY, int numPixelColuna, int numPixelLinha)
	{
		super("ECWM Mapping");
		this.WP = WP;
		iniPanelX = IniX + 10;
		iniPanelY = IniY + 10;
		fimPanelX = numPixelColuna - 20;
		fimPanelY = numPixelLinha - 70;

		setLocation(IniX, IniY);
		getContentPane().setLayout(null);
		setMenuBar(new ECWM_MenuBarra(this));
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
		desSeq = new ECWM_DesenhaSequencia(this, iniPanelX-8, iniPanelY-8, fimPanelX-20+ScrollEmX, fimPanelY-20+ScrollEmY);
		SP.add(desSeq);
		getContentPane().add(SP);
	}
	public WindowPrincipal getWindowPrincipal()
	{
		return WP;
	}
	public ECWM_DesenhaSequencia getDesenhaSequencia()
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
	public ECWM_Sequencia getSequencia()
	{
		return desSeq.getSequencia();
	}
	public void setDesenhaSequencia(ECWM_DesenhaSequencia dseq)
	{
		desSeq = dseq;
	}
}
