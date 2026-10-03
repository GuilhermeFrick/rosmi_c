package cafes.model.ACPM;

import java.awt.*;
import cafes.common.*;

class ACPM_CirculoVertice extends Circulo
{
	private static final long serialVersionUID = 9005700169948437860L;
	protected ACPM_InfoNodo inf;

	public ACPM_CirculoVertice(int x, int y, ACPM_InfoNodo inf)
	{
		super(x, y);
		this.inf = inf;
	}
	public void setInf(ACPM_InfoNodo inf)
	{
		this.inf = inf;
	}
	public ACPM_InfoNodo getInf()
	{
		return inf;
	}
	public String getCoreOrigem()
	{
		return inf.getOrigem();
	}
	public void setCoreOrigem(String coreOrigem)
	{
		inf.setOrigem(coreOrigem);
	}
	public String getCoreDestino()
	{
		return inf.getDestino();
	}
	public void setCoreDestino(String coreDestino)
	{
		inf.setDestino(coreDestino);
	}
	public long getPhits()
	{
		return inf.getPhits();
	}
	public void setPhits(long phits)
	{
		inf.setPhits(phits);
	}

	public String getStringPhits() { return String.valueOf(inf.getPhits()); }

	public static int getRaio() { return raio; }

	@Override
	protected void desenhaConteudo(Graphics g)
	{
		g.setFont(new Font("Arial", Font.BOLD, 10));
		String origemS = inf.getOrigem();
		String destinoS = inf.getDestino();
		String phitsS = String.valueOf(inf.getPhits());
		String str = origemS + "->" + destinoS;
		g.drawString(str, x - (int) (str.length() * 2.7), y + 6);
		g.drawString(phitsS, x - (int) (phitsS.length() * 2.7), y + 17);
	}
}
