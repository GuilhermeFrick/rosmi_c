package cafes.model.ACPM;

class ACPM_Vertice extends ACPM_CirculoVertice
{
	private static final long serialVersionUID = 413613982184648407L;

	public static int tempoComputacao = 1;					// Atribui um tempo de computação qualquer para todos os núcleos, apenas para que a mensagem não saia exatamente quando chegou
	private ACPM_Vertice prox;								// Monta o grafo através de uma lista de vértices
	private long cicloInicial, cicloFinal;					// Marca o início e o fim do envio da mensagem na infraestrutura de comunicação

	public ACPM_Vertice(int x, int y, String origem, String destino, long phits)
	{
		super(x, y, new ACPM_InfoNodo(origem, destino, phits));
		acpmInicio();
	}
	public void acpmInicio()
	{
		this.prox = null;
		resetCiclos();
	}
	public void resetCiclos()
	{
		this.cicloInicial = Integer.MIN_VALUE;
		this.cicloFinal = Integer.MAX_VALUE;
	}
	public void setCicloInicial(long ciclo)
	{
		cicloInicial = ciclo;
	}
	public long getCicloInicial()
	{
		return cicloInicial;
	}
	public void setCicloFinal(long ciclo)
	{
		cicloFinal = ciclo;
	}
	public long getCicloFinal()
	{
		return cicloFinal;
	}
	public ACPM_Vertice getProx()
	{
		return prox;
	}
	public void setProx(ACPM_Vertice prox)
	{
		this.prox = prox;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		getInf().exibe();
		System.out.println();
	}
}