package cafes.model.CWM;

/*
 * Autor: 
 * 		César Marcon
 * Revisor:
 * 		Edson Moreno
 * Objetivo:
 * 		Permitir a criacao da lista de vertices adjacentes a um dado vertice
 * 			original.
 */
public class CWM_VerticeAdjacente implements java.io.Serializable
{
	private static final long serialVersionUID = 8203153475500758692L;
	private CWM_Vertice vertice;		// Qual é o vértice associado ao vértice adjacente
	private CWM_VerticeAdjacente prox;	// Aponta para o próximo vértice adjacente da lista
	private long phits;					// Número de phits de todas as mensagens

	/*
	 * Objetivo:
	 * 		Criar o vertice adjacente de um vertice original
	 * Parametros:
	 * 		vertice -> vertice adjacente do vertice original
	 * 		phits -> "PESO" da comunicacao entre o vertice origem e o adjacente (destino) 
	 */
	public CWM_VerticeAdjacente(CWM_Vertice vertice, long phits)
	{
		this.vertice = vertice;
		this.prox = null;
		this.phits = phits;
	}

	/*
	 * Objetivo:
	 * 		Retornar o vertice que é adjacente ao vertice original
	 * Parametros:
	 * 		Nao ha... 
	 */
	public CWM_Vertice getVertice()
	{
		return vertice;
	}

	/*
	 * Objetivo:
	 * 		Retornar o proximo vertice que é adjacente ao vertice original
	 * Parametros:
	 * 		Nao ha... 
	 */
	public CWM_VerticeAdjacente getProx()
	{
		return prox;
	}

	/*
	 * Objetivo:
	 * 		Definir o proximo vertice adjacente ao vertice original
	 * Parametros:
	 * 		prox -> vertice adjacente ao vertice original 
	 */
	public void setProx(CWM_VerticeAdjacente prox)
	{
		this.prox = prox;
	}

	/*
	 * Objetivo:
	 * 		Retornar a posicao grafica X do vertice adjacente 
	 * Parametros:
	 * 		Nao ha... 
	 */
	public int getX()
	{
		return vertice.getX();
	}

	/*
	 * Objetivo:
	 * 		Retornar a posicao grafica Y do vertice adjacente 
	 * Parametros:
	 * 		Nao ha... 
	 */
	public int getY()
	{
		return vertice.getY();
	}

	/*
	 * Objetivo:
	 * 		Retornar a informacao/nome do vertice adjacente que estah 
	 * 			sendo consultado 
	 * Parametros:
	 * 		Nao ha... 
	 */
	public String getInf()
	{
		return vertice.getInf();
	}

	/*
	 * Objetivo:
	 * 		Retornar o peso da comunicacao entre os vertices independente
	 * 			da sua disposicao na NoC 
	 * Parametros:
	 * 		Nao ha... 
	 */
	public long getPhits()
	{
		return phits;
	}

	/*
	 * Objetivo:
	 * 		Retornar o peso da comunicacao entre os vertices independente
	 * 			da sua disposicao na NoC em formato string 
	 * Parametros:
	 * 		Nao ha... 
	 */
	public String getStrPhits()
	{
		return Long.toString(phits);
	}

	/*
	 * Objetivo:
	 * 		Define o peso da comunicacao entre o vertice original e o adjacente 
	 * Parametros:
	 * 		phits -> peso da comunicacao entre os vertices 
	 */
	public void setPhits(long phits)
	{
		this.phits = phits;
	}

	/*
	 * Objetivo:
	 * 		Define o peso da comunicacao entre o vertice original e o adjacente
	 * 			a partir de uma string 
	 * Parametros:
	 * 		phitsStr -> peso da comunicacao entre os vertices em string
	 */
	public void setPhits(String phitsStr)
	{
		this.phits = Long.parseLong(phitsStr);
	}

	/*
	 * Objetivo:
	 * 		Incrementa o valor peso da comunicacao 
	 * Parametros:
	 * 		ph -> valor a ser incrementado no phit original 
	 */
	public void incrementaPhits(long ph)
	{
		this.phits = this.phits + ph;
	}
}

