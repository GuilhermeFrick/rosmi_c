package cafes.model.CDCM;

class CDCM_VerticeDependente implements java.io.Serializable
{
	private static final long serialVersionUID = 8231811163504176394L;
	private CDCM_Vertice vertice;		// Qual é o vértice associado ao vértice dependente
	private CDCM_VerticeDependente prox;	// Aponta para o próximo vértice dependente da lista

	public CDCM_VerticeDependente(CDCM_Vertice vertice)
	{
		this.vertice = vertice;
		this.prox = null;
	}
	public CDCM_VerticeDependente(CDCM_VerticeDependente verticeDependente)
	{
		this.vertice = verticeDependente.getVertice();
		this.prox = verticeDependente.getProx();
	}
	public CDCM_Vertice getVertice()
	{
		return vertice;
	}
	public CDCM_VerticeDependente getProx()
	{
		return prox;
	}
	public void setProx(CDCM_VerticeDependente prox)
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

