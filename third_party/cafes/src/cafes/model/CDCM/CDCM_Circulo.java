package cafes.model.CDCM;

import java.awt.*;

import cafes.common.*;

class CDCM_Circulo extends Circulo
{
	private static final long serialVersionUID = -6704513207445412303L;
	private CDCM_InfoNodo inf;

	public CDCM_Circulo(int id)
	{
		this.inf = new CDCM_InfoNodo(id);
	}

	public CDCM_Circulo(int x, int y, int id)
	{
		super(x, y);
		this.inf = new CDCM_InfoNodo(id);
	}

	public CDCM_Circulo(int x, int y, CDCM_InfoNodo inf)
	{
		super(x, y);
		this.inf = inf;
	}

	public void setInf(CDCM_InfoNodo inf)
	{
		this.inf = inf;
	}

	public CDCM_InfoNodo getInf()
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

	public int getComputacao()
	{
		return inf.getComputacao();
	}

	public void setComputacao(int computacao)
	{
		inf.setComputacao(computacao);
	}

	public String getStringComputacao()
	{
		return String.valueOf(inf.getComputacao());
	}

	@Override
	protected void desenhaConteudo(Graphics g)
	{
		String nome;

		switch(getID())
		{
			case CDCM_Grafo.START: // Vértice START
				nome = "START";
				g.setFont(new Font("Arial", Font.BOLD, 12));
				g.drawString(nome, x - (int) (nome.length() * 3.2), y + 6);
				break;

			case CDCM_Grafo.END: // Vértice END
				nome = "END";
				g.setFont(new Font("Arial", Font.BOLD, 12));
				g.drawString(nome, x - (int) (nome.length() * 3.2), y + 6);
				break;

			default:
				g.setFont(new Font("Arial", Font.BOLD, 10));
				String origemS = inf.getOrigem();
				String destinoS = inf.getDestino();
				String phitsS = String.valueOf(inf.getPhits());
				String computacaoS = String.valueOf(inf.getComputacao());
				String str = origemS + "->" + destinoS;
				computacaoS = "(" + computacaoS + ")";
				g.drawString(computacaoS, x - (int) (computacaoS.length() * 2.1), y - 9);
				g.drawString(str, x - (int) (str.length() * 2.7), y + 6);
				g.drawString(phitsS, x - (int) (phitsS.length() * 2.6), y + 18);
				break;
		}
	}
}
