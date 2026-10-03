package cafes.model.CWM;

class CWM_ListaTempo
{
	private CWM_ListaTempo prox, ant;
	private long cicloInicial, cicloFinal;
	
	public CWM_ListaTempo(long cicloInicial, long cicloFinal)
	{
		setCicloInicial(cicloInicial);
		setCicloFinal(cicloFinal);
	}
	public void setCicloInicial(long cicloInicial)
	{
		if(cicloInicial<0)
			cicloInicial = 0;
		this.cicloInicial = cicloInicial;
	}
	public void setCicloFinal(long cicloFinal)
	{
		this.cicloFinal = cicloFinal;
	}
	public long getCicloInicial()
	{
		return cicloInicial;
	}
	public long getCicloFinal()
	{
		return cicloFinal;
	}
	public CWM_ListaTempo getAnt()
	{
		return ant;
	}
	public CWM_ListaTempo getProx()
	{
		return prox;
	}
	public void setAnt(CWM_ListaTempo ant)
	{
		this.ant = ant;
	}
	public void setProx(CWM_ListaTempo prox)
	{
		this.prox = prox;
	}
}
