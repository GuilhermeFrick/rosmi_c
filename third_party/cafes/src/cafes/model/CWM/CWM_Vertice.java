package cafes.model.CWM;

import cafes.common.Vertice;

/*
 * Autor: 
 * 		César Marcon
 * Revisor:
 * 		Edson Moreno
 * Objetivo: 
 * 		Permite que cada vertice (nucleo IP) organize sua lista de adjacencia.
 * 		Classe estende CWM_Circulo para que possua representacoes graficas.
 */
public class CWM_Vertice extends CWM_Circulo implements Vertice
{
	private static final long serialVersionUID = -8581692803461493024L;
	private CWM_VerticeAdjacente vertAdjInicial;	// Aponta para o primeiro vértices da lista de adjacentes ao atual
	private CWM_VerticeAdjacente vertAdjFinal; 	// Aponta para o último vértices da lista de adjacentes ao atual
	private CWM_Vertice prox;						// Monta o grafo através de uma lista de vértices

	/*
	 *Objetivo:
	 *	Construir o objeto CWM_Vertice
	 *Parametros:
	 *		x -> posicao x da criacao do objeto
	 *		y -> posicao y da criacao do objeto
	 *		inf -> nome do objeto que se esta criando
	 */
	public CWM_Vertice(int x, int y, String inf)
	{
		super(x, y, inf);
		this.vertAdjInicial = null;
		this.vertAdjFinal = null;
		this.prox = null;
	}

	/*
	 *Objetivo:
	 *		Construir o objeto CWM_Vertice apartir de um objeto jah existente.
	 *			Nao eh copiada sua lista de adjacencias, apenas sua posicao e
	 *			nome.
	 *Parametros:
	 *		p -> objeto CWM_Vertice original que serah utilizado para copia
	 */
	public CWM_Vertice(CWM_Vertice p)
	{
		super(p.getX(), p.getY(), p.getInf());
		this.vertAdjInicial = null;
		this.vertAdjFinal = null;
		this.prox = null;
	}

	/*
	 *Objetivo:
	 *		Insere um vertice na lista de adjacencias deste vertice
	 *Parametros:
	 *		p -> vertice adjacente ao qual este vertice esta conectado
	 */
	public void insereNaListaDeAdjacentes(CWM_VerticeAdjacente p)
	{
		if(vertAdjInicial == null) // Será o primeiro nodo adjacente
			vertAdjInicial = p;
		else
			vertAdjFinal.setProx(p);
		vertAdjFinal = p;
	}

	/*
	 *Objetivo:
	 *		Adicionar um vertice a lista de vertices adjacentes deste vertice
	 *Parametros:
	 *		verticeDestino -> vertice a ser adicionado na lista de vertices deste vertice
	 *		phits -> o peso da comunicacao entre este vertice e o verticeDestino
	 */
	public void incrementaNaListaDeAdjacentes(CWM_Vertice verticeDestino, long phits)
	{
		CWM_VerticeAdjacente adj = verticeDaListaDeAdjacentes(verticeDestino);

		if(adj == null) // Se não existe insere
			insereNaListaDeAdjacentes(new CWM_VerticeAdjacente(verticeDestino, phits));
		else            // Se já existe incrementa o número de phits
			adj.incrementaPhits(phits);
	}

	/*
	 * Objetivo:
	 * 		Eliminar um vertice da lista de vertices adjacentes
	 * Parametros:
	 * 		verticeParaDeletar -> vertice a ser eliminado da lista de vertices
	 * 			adjacentes deste vertice
	 */
	public void deletaVerticeAdjacente(CWM_Vertice verticeParaDeletar)
	{
		CWM_VerticeAdjacente adjAnt = null;
		CWM_VerticeAdjacente adj = vertAdjInicial;

		while(adj != null)
		{
			if(adj.getVertice() == verticeParaDeletar)
			{
				if(adj == vertAdjInicial)
					vertAdjInicial = adj.getProx();
				if(adj.getProx() == null)
				{
					if(adjAnt != null)
						adjAnt.setProx(null);
					vertAdjFinal = adjAnt;
				}
				else
				{
					if(adjAnt != null)
						adjAnt.setProx(adj.getProx());
				}
			}
			adjAnt = adj;
			adj = adj.getProx();
		}
	}

	/*
	 * Objetivo:
	 * 		Confirmar se um dado vertice faz parte da lista de vertices 
	 * 			adjacentes deste vertice
	 * Parametros:
	 * 		vertex -> vertice a ser confirmado como presente ou nao na lista
	 * 			de vertices adjacentes. Se estiver na lista retorna um "ponteiro"
	 * 			para a vertice adjacente, do contrario retorna null.
	 */
	public CWM_VerticeAdjacente verticeDaListaDeAdjacentes(CWM_Vertice vertex)
	{
		CWM_VerticeAdjacente p=vertAdjInicial;
		
		while(p != null)
		{
			if(p.getVertice()==vertex)
				return p;
			p = p.getProx();
		}
		return null;
	}

	/*
	 * Objetivo:
	 * 		Verifica se um determinado vertice esta na lista de vertices
	 * 			adjacentes de um vertice
	 * Parametros:
	 * 		vertex -> vertice a ser confirmado como presente na lista de
	 * 			adjacentes. Caso exista, a funcao retorna true, do contrario
	 * 			retorna false.
	 */
	public boolean jaEstaNaListaDeAdjacentes(CWM_Vertice vertex)
	{
		if(verticeDaListaDeAdjacentes(vertex) != null)
			return true;
		return false;
	}
	/*
	 * Objetivo:
	 * 		Dado um vertice adjacente, capturar o "peso" da comunicacao
	 * 			entre o vertice original e este adjacente.
	 * Parametros:
	 * 		vertex -> vertice a ser utilizado na pesquisa. Ao encontrar
	 * 			retorna o peso da comunicacao (phits). Caso este vertice
	 * 			nao seja encontrado na lista de vertices adjacentes eh
	 * 			retornado o valor -1.
	 */
	public long getPesoAssociado(CWM_Vertice vertex)
	{
		CWM_VerticeAdjacente p=vertAdjInicial;
		
		while(p != null)
		{
			if(p.getVertice() == vertex)
				return p.getPhits();
			p = p.getProx();
		}
		return -1;
	}

	/*
	 * Objetivo:
	 * 		Retorna o proximo vertice origina da lista de vertices originais
	 * Parametros:
	 * 		Nao ha...
	 */
	public CWM_Vertice getProx()
	{
		return prox;
	}

	/*
	 * Objetivo:
	 * 		Definir o proximo vertice original
	 * Parametro:
	 * 		prox -> vertice a ser inserido na lista de vertices originais
	 */
	public void setProx(Vertice prox)
	{
		if(prox instanceof CWM_Vertice)
			setProx(prox);
	}

	public void setProx(CWM_Vertice prox)
	{
		this.prox = prox;
	}

	/*
	 * Objetivo:
	 * 		Retornar o ponteiro para o primeiro vertice adjacente
	 * Parametros:
	 * 		Nao ha...
	 */
	public CWM_VerticeAdjacente getVerticeAdjacenteInicial()
	{
		return vertAdjInicial;
	}

	/*
	 * Objetivos:
	 * 		Definir um vertice como sendo o primeiro vertice adjacente da lista
	 * 			de vertices adjacentes.
	 * Parametros:
	 * 		vertAdjInicial -> vertice adjacente a ser definido como primeiro
	 * 			vertice da lista de adjacentes
	 */
	public void setVerticeAdjacenteInicial(CWM_VerticeAdjacente vertAdjInicial)
	{
		this.vertAdjInicial = vertAdjInicial;
	}

	/*
	 * Objetivo:
	 * 		Retornar o ultimo vertice adjacente da lista de adjacentes deste
	 * 			vertice
	 * Parametros:
	 * 		Nao ha...
	 */
	public CWM_VerticeAdjacente getVerticeAdjacenteFinal()
	{
		return vertAdjFinal;
	}

	/*
	 * Objetivo:
	 * 		Definir o ultimo vertice adjacente da
	 * Parametros:
	 * 		vertAdjFinal -> vertice adjacente a ser incluido como ultimo
	 * 			na lista de vertices adjacentes
	 */
	public void setVerticeAdjacenteFinal(CWM_VerticeAdjacente vertAdjFinal)
	{
		this.vertAdjFinal = vertAdjFinal;
	}

	/* 
	 * Objetivo:
	 * 		Apresentar a lista de vertices adjacentes...
	 * Parametros:
	 * 		Nao ha... 
	 */
	public void exibe()
	{
		System.out.println("[" + this + "]" + getInf());
		if(vertAdjFinal != null)
			System.out.println("\t[" + vertAdjFinal.getVertice() + "][" + vertAdjFinal.getVertice()	+ "]");
		if(prox != null)
			System.out.println("\t[" + prox + "]");
	}
}