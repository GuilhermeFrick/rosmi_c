package cafes.model.CDM;

public class CDM_VerticeDependente implements java.io.Serializable
{
	private static final long serialVersionUID = 3571874097542262953L;
	private CDM_Vertice vertice;		// Qual é o vértice associado ao vértice dependente
	private CDM_VerticeDependente prox;	// Aponta para o próximo vértice dependente da lista

	public CDM_VerticeDependente(CDM_Vertice vertice)
	{
		this.vertice = vertice;
		this.prox = null;
	}
	public CDM_Vertice getVertice()
	{
		return vertice;
	}
	public CDM_VerticeDependente getProx()
	{
		return prox;
	}
	public void setProx(CDM_VerticeDependente prox)
	{
		this.prox = prox;
	}
	public int getX()
	{
		return vertice.getX();
	}
	public int getY()
	{
		return vertice.getY();
	}
}

