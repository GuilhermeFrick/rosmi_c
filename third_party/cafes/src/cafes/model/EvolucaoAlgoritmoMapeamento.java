package cafes.model;

import java.awt.*;

public class EvolucaoAlgoritmoMapeamento extends Canvas
{
	private static final long serialVersionUID = 6959478452187971545L;
	private int andamento;
	private int dimx;

	public EvolucaoAlgoritmoMapeamento(int x, int y, int dimx, int dimy)
	{
		this.dimx = dimx;
		andamento = 1;
		getGraphics();
		setBounds(x, y, dimx, dimy);
	}
	public void incrementaEvolucaoAlgoritmoMapeamento()
	{
		andamento++;
	}
	public int getEvolucaoAlgoritmoMapeamento()
	{
		return andamento;
	}
	public void paint(Graphics g)
	{
		g.setColor(new Color(180, 180, 180));
		g.fillRect(1, 20, dimx, 10);
		g.setColor(new Color(0, 0, 0));
		g.setFont(new Font("Arial", Font.BOLD, 12));
		g.drawString("Computing . . .", dimx/2-45, 12);
		g.fillRect(1,20, dimx/47*andamento, 10);
	}
	public void update()
	{
		paint(getGraphics());
		repaint();
	}
}