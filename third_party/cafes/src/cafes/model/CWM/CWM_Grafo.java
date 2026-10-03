package cafes.model.CWM;

/*
 * Autor: 
 * 		C�sar Marcon
 * Revisor:
 * 		Edson Moreno
 * Objetivo: 
 * 		Permite criar um grafo de vertices a ser utilizado no c�lculo de 
 * 			consumo de potencia e tempo. Este grafo eh composto por vertices
 * 			originais e seus vertices adjacentes.
 */

public class CWM_Grafo implements java.io.Serializable
{
	private static final long serialVersionUID = -503992656043366998L;
	private CWM_Vertice inicio;
	private CWM_Vertice fim;

	/*
	 * Objetivo:
	 * 	 	Inicializa o grafo
	 * Parametros:
	 * 		Nao ha...
	 */
	public CWM_Grafo()
	{
		inicio = null;
		fim = null;
	}
	/*
	 * Objetivo:
	 * 		Realiza o calculo de CWM a partir de um posicionamento definido.
	 * Parametros:
	 *   	noc -> Definicao do posicionamento dos vertices na NoC
	 */
	public void executaCWM(CWM_NoC noc)
	{
		CWM_Vertice p = inicio;

		while(p!=null)
		{
			CWM_VerticeAdjacente v = p.getVerticeAdjacenteInicial();

			while(v!=null)
			{
				String origem = p.getInf();
				String destino = v.getInf();

				if(noc.DescobreLinha(origem) >= 0 && noc.DescobreColuna(origem) >=0 && noc.DescobreLinha(destino) >= 0 && noc.DescobreColuna(destino) >=0)
					noc.topologia(p, v);
				v = v.getProx();
			}
			p = p.getProx();
		}
	}

	/*
	 * Objetivo:
	 * 		Realiza o calculo de CWM a partir de um posicionamento
	 * 			definido. Adicionalmente realiza o calculo de analise 
	 * 			temporal.
	 * Parametros:
	 *   	noc -> posicionamento dos vertices na NoC   
	 *   	ehTopologiaMesh -> Informa o tipo de topologia da noc. Atualmente
	 *   		as topologias disponiveis sao mesh ou torus.
	 */
	public void executaCWM(CWM_NoC noc, boolean ehTopologiaMesh)
	{
		CWM_AnaliseTemporal at = new CWM_AnaliseTemporal(noc, noc.getNumeroLinhas(), noc.getNumeroColunas(),noc.getNumeroAltura());
		CWM_Vertice p = inicio;

		while(p!=null)
		{
			CWM_VerticeAdjacente v = p.getVerticeAdjacenteInicial();

			while(v!=null)
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
	/*
	 * Objetivo:
	 * 		Insere um nodo sempre ao final da Grafo. Caso nao existam vertices
	 * 			no grafo este eh inserido como primeiro. 
	 * Parametros:
	 * 		n -> Vertice a ser inserido no grafo;
	 */
	public void insereVerticeGrafo(CWM_Vertice n)
	{
		if(inicio==null)
			inicio = n;
		else
			fim.setProx(n);				//insere o nodo
		fim = n;					//atualiza o ponteiro fim
	}
	
	/*
	 * Objetivos: 
	 * 		Verifica se um determinado vertice jah existe no grafo
	 * Parametros:
	 * 		str -> nome do vertice. Caso ele exista retorna true, do 
	 * 			contrario retorna false.
	 */
	public boolean jaExisteCore(String str)
	{
		if(verticeDoCore(str)!=null)
			return true;
		return false;
	}
	
	/*
	 * Objetivo:
	 * 		Percorrer o grafo procurando pelo vertice original com o nome 
	 * 			passado por parametro. Retorna o objeto CWM_Vertice caso o 
	 * 			vertice seja encontrado, do contrario retorna null.
	 * Parametros:
	 * 		str -> nome do vertice original a pesquisado 
	 */
	public CWM_Vertice verticeDoCore(String str)
	{
		CWM_Vertice p = inicio;
		
		while(p!=null)
		{
			if(p.getInf().equals(str))
				return p;
			p = p.getProx();
		}
		return null;
	}
	
	/*
	 * Objetivo:
	 * 		Retonar o primeiro vertice original da lista de vertices do grafo
	 * Parametros:
	 * 		Nao ha...
	 */
	public CWM_Vertice getInicio()
	{
		return inicio;
	}
	
	/*
	 * Objetivo:
	 * 		Retorna o �ltimo vertice original da lista de vertices do grafo
	 * Parametros:
	 * 		Nao ha...
	 */
	public CWM_Vertice getFim()
	{
		return fim;
	}
	
	/*
	 * Objetivo:
	 * 		Define o primeiro vertice original da lista de vertices do grafo
	 * Parametros:
	 * 		inicio -> O vertice a ser adotado como primeiro 
	 */
	public void setInicio(CWM_Vertice inicio)
	{
		this.inicio = inicio;
	}
	
	/*
	 * Objetivo:
	 * 		Definir o ultimo vertice da lista de vertices do grafo
	 * Parametros:
	 * 		fim -> O vertice a ser adotado como ultimo
	 */
	public void setFim(CWM_Vertice fim)
	{
		this.fim = fim;
	}

	/*
	 * Objetivo:
	 * 		Lista os vertices do grafo para fins de depuracao
	 * Parametros:
	 * 		Nao ha... 
	 */
	public void ExibeVertices()
	{
		CWM_Vertice p=inicio;
	
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}
}
