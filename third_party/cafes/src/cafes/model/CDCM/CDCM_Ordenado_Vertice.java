package cafes.model.CDCM;

public class CDCM_Ordenado_Vertice implements java.io.Serializable
{
	private static final long serialVersionUID = 5514127141855273751L;
	private CDCM_Vertice vertice;
	private CDCM_Ordenado_Vertice prox;

	public CDCM_Ordenado_Vertice(CDCM_Vertice vertice)
	{
		this.vertice = vertice; 
		prox = null;
	}
	public CDCM_Ordenado_Vertice(CDCM_Ordenado_Vertice p)
	{
		this.vertice = p.getVertice(); 
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
	public CDCM_Ordenado_Vertice getProx()
	{
		return prox;
	}
	public void setProx(CDCM_Ordenado_Vertice prox)
	{
		this.prox = prox;
	}
}
