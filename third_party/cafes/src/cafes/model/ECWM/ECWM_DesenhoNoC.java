package cafes.model.ECWM;

import java.awt.*;

class ECWM_DesenhoNoC extends Canvas
{
	private static final long serialVersionUID = 7232220477678270569L;
	private ECWM_NoC noc;

	public ECWM_DesenhoNoC(ECWM_NoC noc, int x, int y, int dimx, int dimy)
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
