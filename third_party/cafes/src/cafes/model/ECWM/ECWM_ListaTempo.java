package cafes.model.ECWM;

class ECWM_ListaTempo
{
	private ECWM_ListaTempo prox, ant;
	private long cicloInicial, cicloFinal;
	
	public ECWM_ListaTempo(long cicloInicial, long cicloFinal)
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
	public ECWM_ListaTempo getAnt()
	{
		return ant;
	}
	public ECWM_ListaTempo getProx()
	{
		return prox;
	}
	public void setAnt(ECWM_ListaTempo ant)
	{
		this.ant = ant;
	}
	public void setProx(ECWM_ListaTempo prox)
	{
		this.prox = prox;
	}
}
