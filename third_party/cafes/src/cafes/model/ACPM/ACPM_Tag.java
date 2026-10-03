package cafes.model.ACPM;

class ACPM_Tag extends ACPM_CirculoTag
{
	private static final long serialVersionUID = 161522766602715028L;
	private ACPM_Vertice verticeInicial;  // Aponta para o primeiro vértices da lista de vértices
	private ACPM_Vertice verticeFinal;	  // Aponta para o último vértices da lista de vértices
	private ACPM_Tag prox;

	public ACPM_Tag(int tag)
	{
		super(tag);
		acpmInicio();
	}
	public ACPM_Tag(int x, int y, int tag)
	{
		super(x, y, tag);
		acpmInicio();
	}
	public void acpmInicio()
	{
		this.verticeInicial = null;
		this.verticeFinal = null;
		this.prox = null;
	}
	public void insereNaListaDeVertices(ACPM_Vertice p)
	{
		if(verticeInicial==null) // Será o primeiro nodo dependente
			verticeInicial = p;
		else
			verticeFinal.setProx(p);
		verticeFinal = p;
	}
	public void deletaVertice(ACPM_Vertice verticeParaDeletar)
	{
		ACPM_Vertice pAnt = null;
		ACPM_Vertice p = verticeInicial;

		while(p != null)
		{
			if(p == verticeParaDeletar)
			{
				if(p == verticeInicial)
					verticeInicial = p.getProx();
				if(verticeInicial == null)
					verticeFinal = null;
				if(p.getProx() == null)
				{
					if(pAnt!=null)
						pAnt.setProx(null);
					verticeFinal = pAnt;
				}
				else
				{
					if(pAnt!=null)
						pAnt.setProx(p.getProx());
				}
			}
			pAnt = p;
			p = p.getProx();
		}
	}
	public boolean jaEstaNaListaDeVertices(ACPM_Vertice vertex)
	{
		ACPM_Vertice p=verticeInicial;

		while(p!=null)
		{
			if(p==vertex)
				return true;
			p = p.getProx();
		}
		return false;
	}
	public ACPM_Tag getProx()
	{
		return prox;
	}
	public void setProx(ACPM_Tag prox)
	{
		this.prox = prox;
	}
	public ACPM_Vertice getVerticeInicial()
	{
		return verticeInicial;
	}
	public void setVerticeInicial(ACPM_Vertice verticeInicial)
	{
		this.verticeInicial = verticeInicial;
	}
	public ACPM_Vertice getVerticeFinal()
	{
		return verticeFinal;
	}
	public void setVerticeFinal(ACPM_Vertice verticeFinal)
	{
		this.verticeFinal = verticeFinal;
	}
	public String formataStringTag()
	{
		switch(tag)
		{
			case ACPM_Grafo.START:
				return new String("START");

			case ACPM_Grafo.END:
				return new String("END");
		}
		return new String(new Integer(tag).toString());
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		ACPM_Vertice p=verticeInicial;

		System.out.println("\nTag: " + formataStringTag() + "  ");
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
	}
}