package cafes.model.ECWM;

public class ECWM_Grafo implements java.io.Serializable
{
	private static final long serialVersionUID = -8848719478357883508L;
	private ECWM_Vertice inicio;
	private ECWM_Vertice fim;

	public ECWM_Grafo()
	{
		inicio = null;
		fim = null;
	}
	public void executaECWM(ECWM_NoC noc)
	{
		ECWM_Vertice p = inicio;

		while(p!=null)
		{
			ECWM_VerticeAdjacente v = p.getVerticeAdjacenteInicial();

			while(v!=null)
			{
				noc.topologia(p, v);
				v = v.getProx();
			}
			p = p.getProx();
		}
	}
	public void executaECWM(ECWM_NoC noc, boolean ehTopologiaMesh)
	{
		ECWM_AnaliseTemporal at = new ECWM_AnaliseTemporal(noc, noc.getNumeroLinhas(), noc.getNumeroColunas(), noc.getNumeroAltura());
		ECWM_Vertice p = inicio;

		while(p != null)
		{
			ECWM_VerticeAdjacente v = p.getVerticeAdjacenteInicial();

			while(v != null)
			{
				if(ehTopologiaMesh)
					at.topologiaMesh(null, null, p, v);
				else
					at.topologiaTorus(null, null, p, v);
				v = v.getProx();
			}
			p = p.getProx();
		}
		noc.armazenaCiclosOperacao(at.getCiclos());
	}
	// Insere um nodo sempre ao final da Grafo
	public void insereVerticeGrafo(ECWM_Vertice n)
	{
		if(inicio==null)
			inicio = n;
		else
			fim.setProx(n);				//insere o nodo
		fim = n;					//atualiza o ponteiro fim
	}
	public boolean jaExisteCore(String str)
	{
		if(verticeDoCore(str)!=null)
			return true;
		return false;
	}
	public ECWM_Vertice verticeDoCore(String str)
	{
		ECWM_Vertice p = inicio;
		
		while(p!=null)
		{
			if(p.getInf().equals(str))
				return p;
			p = p.getProx();
		}
		return null;
	}
	public ECWM_Vertice getInicio()
	{
		return inicio;
	}
	public ECWM_Vertice getFim()
	{
		return fim;
	}
	public void setInicio(ECWM_Vertice inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(ECWM_Vertice fim)
	{
		this.fim = fim;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//DEPURA��O
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void ExibeVertices()
	{
		ECWM_Vertice p=inicio;
	
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}
}
