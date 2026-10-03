package cafes.model.CDM;

public class CDM_Ordenado_VetorNiveis implements java.io.Serializable
{
	private static final long serialVersionUID = -1350589444680476791L;
	private CDM_Ordenado_ListaVertices inicio, fim;
	private int nivel;

	public CDM_Ordenado_VetorNiveis(int nivel)
	{
		inicio = null;
		fim = null;
		this.nivel = nivel;
	}
	public int getNivel()
	{
		return nivel;
	}
	public void setNivel(int nivel)
	{
		this.nivel = nivel;
	}
	public void insereListaVertices(CDM_Ordenado_ListaVertices n)
	{
		if(inicio==null)
			inicio = n;
		else
			fim.setProx(n);
		fim = n;
	}
	public CDM_Ordenado_ListaVertices getInicio()
	{
		return inicio;
	}
	public CDM_Ordenado_ListaVertices getFim()
	{
		return fim;
	}
	public void setInicio(CDM_Ordenado_ListaVertices inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(CDM_Ordenado_ListaVertices fim)
	{
		this.fim = fim;
	}
}
