package cafes.model.CDM;

import java.awt.*;

class CDM_DesenhoNoC extends Canvas
{
	private static final long serialVersionUID = 3808129866613125387L;
	private CDM_NoC noc;

	public CDM_DesenhoNoC(CDM_NoC noc, int x, int y, int dimx, int dimy)
	{
		this.noc = noc;
		getGraphics();
		setBounds(x, y, dimx, dimy);
		setBackground(new Color(180, 180, 180));
	}
	public void paint(Graphics g)
	{
		noc.desenhaNoC(g);
	}
}
