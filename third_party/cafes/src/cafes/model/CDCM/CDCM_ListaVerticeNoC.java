package cafes.model.CDCM;

public class CDCM_ListaVerticeNoC implements java.io.Serializable
{
	private static final long serialVersionUID = 1714970697841001574L;
	private CDCM_Vertice vertice;
	private long cicloInicial;	   // Ciclo inicial que o vertice usou o recurso
	private long cicloFinal;				// Ciclo final que o vertice usou o recurso
	private CDCM_ListaVerticeNoC prox;

	public CDCM_ListaVerticeNoC(CDCM_Vertice vertice, long cicloInicial, long cicloFinal)
	{
		this.vertice = vertice;
		this.cicloInicial = cicloInicial; 
		this.cicloFinal = cicloFinal; 
		prox = null;
	}
	public CDCM_Vertice getVertice()
	{
		return vertice;
	}
	public void setVertice(CDCM_Vertice vertice)
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
	public CDCM_ListaVerticeNoC getProx()
	{
		return prox;
	}
	public void setProx(CDCM_ListaVerticeNoC prox)
	{
		this.prox = prox;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 DEPURAÇÃO
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	/
	public void exibe()
	{
		System.out.print(" {ID:" + vertice.formataStringID()  + "  " + vertice.getCoreOrigem() + "->" + 
						vertice.getCoreDestino() + " [" + cicloInicial + "," + cicloFinal + "] " + 
						vertice.getPhits() +  " : " + vertice.getComputacao() + "}");
	}
}
