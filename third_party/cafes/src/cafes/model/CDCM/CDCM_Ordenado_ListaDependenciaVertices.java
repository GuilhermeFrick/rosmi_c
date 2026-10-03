package cafes.model.CDCM;

public class CDCM_Ordenado_ListaDependenciaVertices implements java.io.Serializable
{
	private static final long serialVersionUID = 497210286450081142L;
	private CDCM_Vertice inicio;
	private CDCM_Vertice fim;
	private CDCM_Ordenado_ListaDependenciaVertices prox;

	public CDCM_Ordenado_ListaDependenciaVertices()
	{
		inicio = null;
		fim = null;
		this.prox = null;
	}
	public void setProx(CDCM_Ordenado_ListaDependenciaVertices prox)
	{
		this.prox = prox;
	}
	public CDCM_Ordenado_ListaDependenciaVertices getProx()
	{
		return prox;
	}
	public void insereVerticeLista(CDCM_Vertice n)
	{
		if(inicio==null)
			inicio = n;
		else
			fim.setProx(n);
		fim = n;
	}
	public CDCM_Vertice getInicio()
	{
		return inicio;
	}
	public CDCM_Vertice getFim()
	{
		return fim;
	}
	public void setInicio(CDCM_Vertice inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(CDCM_Vertice fim)
	{
		this.fim = fim;
	}
}
