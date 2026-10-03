package cafes.model.ACPM;

import java.awt.*;

class ACPM_DesenhoNoC extends Canvas
{
	private static final long serialVersionUID = -4242384564985875234L;
	private ACPM_NoC noc;

	public ACPM_DesenhoNoC(ACPM_NoC noc, int x, int y, int dimx, int dimy)
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
