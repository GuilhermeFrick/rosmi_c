package cafes.model.CDM;

public class CDM_Ordenado_Vertice implements java.io.Serializable
{
	private static final long serialVersionUID = -8951287658989502347L;
	private CDM_Vertice vertice;
	private CDM_Ordenado_Vertice prox;

	public CDM_Ordenado_Vertice(CDM_Vertice vertice)
	{
		this.vertice = vertice; 
		prox = null;
	}
	public CDM_Ordenado_Vertice(CDM_Ordenado_Vertice p)
	{
		this.vertice = p.getVertice(); 
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
	public CDM_Ordenado_Vertice getProx()
	{
		return prox;
	}
	public void setProx(CDM_Ordenado_Vertice prox)
	{
		this.prox = prox;
	}
}
