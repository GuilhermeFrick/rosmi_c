package cafes.model.ACPM;

public class ACPM_ListaVerticeNoC implements java.io.Serializable
{
	private static final long serialVersionUID = -5537358824499696144L;
	private ACPM_Vertice vertice;
	private long cicloInicial;	   // Ciclo inicial que o vertice usou o recurso
	private long cicloFinal;				// Ciclo final que o vertice usou o recurso
	private ACPM_ListaVerticeNoC prox;
	private ACPM_Tag tag;

	public ACPM_ListaVerticeNoC(ACPM_Tag tag, ACPM_Vertice vertice, long cicloInicial, long cicloFinal)
	{
		this.tag = tag; 
		this.vertice = vertice; 
		this.cicloInicial = cicloInicial; 
		this.cicloFinal = cicloFinal; 
		prox = null;
	}
	public ACPM_Tag getTag()
	{
		return tag;
	}
	public ACPM_Vertice getVertice()
	{
		return vertice;
	}
	public void setVertice(ACPM_Vertice vertice)
	{
		this.vertice = vertice;
	}
	public long getCicloInicial()
	{
		return cicloInicial;
	}
	public void setCicloInicial(long cicloInicial)
	{
		this.cicloInicial = cicloInicial;
	}
	public long getCicloFinal()
	{
		return cicloFinal;
	}
	public void setCicloFinal(long cicloFinal)
	{
		this.cicloFinal = cicloFinal;
	}
	public ACPM_ListaVerticeNoC getProx()
	{
		return prox;
	}
	public void setProx(ACPM_ListaVerticeNoC prox)
	{
		this.prox = prox;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 DEPURAÇÃO
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	/
	public void exibe()
	{
		System.out.print(" {Tag: " + tag.formataStringTag() + ": " + vertice.getCoreOrigem() + "->" +  
						vertice.getCoreDestino() + " [" + cicloInicial + "," +
						cicloFinal + "] " + vertice.getPhits() + "}");
	}
}
