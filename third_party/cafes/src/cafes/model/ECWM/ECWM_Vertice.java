package cafes.model.ECWM;

class ECWM_Vertice extends ECWM_Circulo
{
	private static final long serialVersionUID = 8791603806464318908L;
	private ECWM_VerticeAdjacente vertAdjInicial;	// Aponta para o primeiro vértices da lista de adjacentes ao atual
	private ECWM_VerticeAdjacente vertAdjFinal; 	// Aponta para o último vértices da lista de adjacentes ao atual
	private ECWM_Vertice prox;						// Monta o grafo através de uma lista de vértices

	public ECWM_Vertice(int x, int y, String inf)
	{
		super(x, y, inf);
		this.vertAdjInicial = null;
		this.vertAdjFinal = null;
		this.prox = null;
	}
	public ECWM_Vertice(ECWM_Vertice p)
	{
		super(p.getX(), p.getY(), p.getInf());
		this.vertAdjInicial = null;
		this.vertAdjFinal = null;
		this.prox = null;
	}
	public void insereNaListaDeAdjacentes(ECWM_VerticeAdjacente p)
	{
		if(vertAdjInicial==null) // Será o primeiro nodo adjacente
			vertAdjInicial = p;
		else
			vertAdjFinal.setProx(p);
		vertAdjFinal = p;
	}
	public void deletaVerticeAdjacente(ECWM_Vertice verticeParaDeletar)
	{
		ECWM_VerticeAdjacente adjAnt = null;
		ECWM_VerticeAdjacente adj = vertAdjInicial;
		
		while(adj!=null)
		{
			if(adj.getVertice()==verticeParaDeletar)
			{
				if(adj==vertAdjInicial)
					vertAdjInicial = adj.getProx();
				if(adj.getProx()==null)
				{
					if(adjAnt!=null)
						adjAnt.setProx(null);
					vertAdjFinal = adjAnt;
				}
				else
				{
					if(adjAnt!=null)
						adjAnt.setProx(adj.getProx());
				}
			}
			adjAnt = adj;
			adj = adj.getProx();
		}
	}
	public boolean jaEstaNaListaDeAdjacentes(ECWM_Vertice vertex)
	{
		ECWM_VerticeAdjacente p=vertAdjInicial;
		
		while(p!=null)
		{
			if(p.getVertice()==vertex)
				return true;
			p = p.getProx();
		}
		return false;
	}
	public long getPesoAssociado(ECWM_Vertice vertex)
	{
		ECWM_VerticeAdjacente p=vertAdjInicial;
		
		while(p!=null)
		{
			if(p.getVertice()==vertex)
				return p.getPhits();
			p = p.getProx();
		}
		return -1;
	}
	public double getPercentualChaveamentoPhitsAssociado(ECWM_Vertice vertex)
	{
		ECWM_VerticeAdjacente p=vertAdjInicial;
		
		while(p!=null)
		{
			if(p.getVertice()==vertex)
				return p.getPercentualChaveamentoPhits();
			p = p.getProx();
		}
		return -1.0;
	}
	public ECWM_Vertice getProx()
	{
		return prox;
	}
	public void setProx(ECWM_Vertice prox)
	{
		this.prox = prox;
	}
	public ECWM_VerticeAdjacente getVerticeAdjacenteInicial()
	{
		return vertAdjInicial;
	}
	public void setVerticeAdjacenteInicial(ECWM_VerticeAdjacente vertAdjInicial)
	{
		this.vertAdjInicial = vertAdjInicial;
	}
	public ECWM_VerticeAdjacente getVerticeAdjacenteFinal()
	{
		return vertAdjFinal;
	}
	public void setVerticeAdjacenteFinal(ECWM_VerticeAdjacente vertAdjFinal)
	{
		this.vertAdjFinal = vertAdjFinal;
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		System.out.println("[" + this + "]" + getInf());
		if(vertAdjFinal!=null)
			System.out.println("\t[" + vertAdjFinal.getVertice() + "][" + vertAdjFinal.getVertice()	+ "]");
		if(prox!=null)
			System.out.println("\t[" + prox + "]");
	}
}