package cafes.model.CDM;

public class CDM_ListaVerticeNoC implements java.io.Serializable
{
	private static final long serialVersionUID = 2196021770753673503L;
	private CDM_Vertice vertice;
	private long cicloInicial;	   // Ciclo inicial que o vertice usou o recurso
	private long cicloFinal;				// Ciclo final que o vertice usou o recurso
	private CDM_ListaVerticeNoC prox;

	public CDM_ListaVerticeNoC(CDM_Vertice vertice, long cicloInicial, long cicloFinal)
	{
		this.vertice = vertice; 
		this.cicloInicial = cicloInicial; 
		this.cicloFinal = cicloFinal; 
		prox = null;
	}
	public CDM_Vertice getVertice()
	{
		return vertice;
	}
	public void setVertice(CDM_Vertice vertice)
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
	public void setCicloFinal(int cicloFinal)
	{
		this.cicloFinal = cicloFinal;
	}
	public CDM_ListaVerticeNoC getProx()
	{
		return prox;
	}
	public void setProx(CDM_ListaVerticeNoC prox)
	{
		this.prox = prox;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 DEPURAÇÃO
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	/
	public void exibe()
	{
		System.out.print(" (ID:" + vertice.formataStringID()  + "  " + vertice.getCoreOrigem() + "->" + 
						vertice.getCoreDestino() + " [" + cicloInicial + "," + cicloFinal + "] " + 
						vertice.getPhits() + ")");
	}
}
