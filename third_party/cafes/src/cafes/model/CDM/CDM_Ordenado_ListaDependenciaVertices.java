package cafes.model.CDM;

public class CDM_Ordenado_ListaDependenciaVertices implements java.io.Serializable
{
	private static final long serialVersionUID = -1791136144979607136L;
	private CDM_Vertice inicio;
	private CDM_Vertice fim;
	private CDM_Ordenado_ListaDependenciaVertices prox;

	public CDM_Ordenado_ListaDependenciaVertices()
	{
		inicio = null;
		fim = null;
		this.prox = null;
	}
	public void setProx(CDM_Ordenado_ListaDependenciaVertices prox)
	{
		this.prox = prox;
	}
	public CDM_Ordenado_ListaDependenciaVertices getProx()
	{
		return prox;
	}
	public void insereVerticeLista(CDM_Vertice n)
	{
		if(inicio==null)
			inicio = n;
		else
			fim.setProx(n);
		fim = n;
	}
	public CDM_Vertice getInicio()
	{
		return inicio;
	}
	public CDM_Vertice getFim()
	{
		return fim;
	}
	public void setInicio(CDM_Vertice inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(CDM_Vertice fim)
	{
		this.fim = fim;
	}
}
