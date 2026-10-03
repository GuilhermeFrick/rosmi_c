package cafes.model.CDM;

import java.awt.*;

import cafes.common.*;

class CDM_Circulo extends Circulo
{
	private static final long serialVersionUID = -4289151544487800566L;
	private CDM_InfoNodo inf;

	public CDM_Circulo(int id)
	{
		this.inf = new CDM_InfoNodo(id);
	}
	public CDM_Circulo(int x, int y, int id)
	{
		super(x, y);
		this.inf = new CDM_InfoNodo(id);
	}
	public CDM_Circulo(int x, int y, CDM_InfoNodo inf)
	{
		super(x, y);
		this.inf = inf;
	}
	public void setInf(CDM_InfoNodo inf)
	{
		this.inf = inf;
	}
	public CDM_InfoNodo getInf()
	{
		return inf;
	}
	public int getID()
	{
		return inf.getID();
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
	public String getStringPhits()
	{
		return String.valueOf(inf.getPhits());
	}
	@Override
	protected void desenhaConteudo(Graphics g)
	{
		String nome;
		g.setColor(Color.WHITE);
		switch (getID())
		{
			case CDM_Grafo.START: // Vértice START
				nome = "START";
				g.setFont(new Font("Arial", Font.BOLD, 12));
				g.drawString(nome, x - (int) (nome.length() * 3.2), y + 6);
				break;

			case CDM_Grafo.END: // Vértice END
				nome = "END";
				g.setFont(new Font("Arial", Font.BOLD, 12));
				g.drawString(nome, x - (int) (nome.length() * 3.2), y + 6);
				break;

			default:
				g.setFont(new Font("Arial", Font.BOLD, 10));
				String origemS = inf.getOrigem();
				String destinoS = inf.getDestino();
				String phitsS = String.valueOf(inf.getPhits());
				String str = origemS + "->" + destinoS;
				g.drawString(str, x - (int) (str.length() * 2.7), y + 6);
				g.drawString(phitsS, x - (int) (phitsS.length() * 2.7), y + 17);
				break;
		}
	}
}
