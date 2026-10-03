package cafes.model.CWM;

import java.awt.*;

class CWM_DesenhoNoC extends Canvas
{
	private static final long serialVersionUID = 4813212087513792707L;
	private CWM_NoC noc;

	public CWM_DesenhoNoC(CWM_NoC noc, int x, int y, int dimx, int dimy)
	{
		this.noc = noc;
		setBounds(x, y, dimx, dimy);
		setBackground(new Color(180, 180, 180));
	}
	public void paint(Graphics g)
	{
		noc.desenhaNoC(getGraphics());//g);
	}
}
