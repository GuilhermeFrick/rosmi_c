package cafes.model.ECWM;

class ECWM_VerticeAdjacente implements java.io.Serializable
{
	private static final long serialVersionUID = 3124084278156330089L;
	private ECWM_Vertice vertice;				// Qual é o vértice associado ao vértice adjacente
	private ECWM_VerticeAdjacente prox;			// Aponta para o próximo vértice adjacente da lista
	private long phits;							// Número de phits de todas as mensagens
	private double percentualChaveamentoPhits;	// Percentual de phits que estão chaveando

	public ECWM_VerticeAdjacente(ECWM_Vertice vertice, long phits, double percentualChaveamentoPhits)
	{
		this.vertice = vertice;
		this.prox = null;
		this.phits = phits;
		this.percentualChaveamentoPhits = percentualChaveamentoPhits;
	}
	public ECWM_Vertice getVertice()
	{
		return vertice;
	}
	public ECWM_VerticeAdjacente getProx()
	{
		return prox;
	}
	public void setProx(ECWM_VerticeAdjacente prox)
	{
		this.prox = prox;
	}
	public int getX()
	{
		return vertice.getX();
	}
	public int getY()
	{
		return vertice.getY();
	}
	public String getInf()
	{
		return vertice.getInf();
	}
	public long getPhits()
	{
		return phits;
	}
	public String getStrPhits()
	{
		return Long.toString(phits);
	}
	public void setPhits(long phits)
	{
		this.phits = phits;
	}
	public void setPhits(String phitsStr)
	{
		this.phits = Long.parseLong(phitsStr);
	}
	public double getPercentualChaveamentoPhits()
	{
		return percentualChaveamentoPhits;
	}
	public String getStrPercentualChaveamentoPhits()
	{
		return Double.toString(percentualChaveamentoPhits);
	}
	public void setPercentualChaveamentoPhits(long percentualChaveamentoPhits)
	{
		this.percentualChaveamentoPhits = percentualChaveamentoPhits;
	}
	public void setPercentualChaveamentoPhits(String percentualChaveamentoPhitsStr)
	{
		this.percentualChaveamentoPhits = Double.parseDouble(percentualChaveamentoPhitsStr);
	}
}

