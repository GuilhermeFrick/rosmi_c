package cafes.model.CDCM;

import java.awt.*;

class CDCM_DesenhoNoC extends Canvas
{
	private static final long serialVersionUID = -6420294210939320450L;
	private CDCM_NoC noc;

	public CDCM_DesenhoNoC(CDCM_NoC noc, int x, int y, int dimx, int dimy)
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
