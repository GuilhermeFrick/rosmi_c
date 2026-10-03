package cafes.model.CDCM;

class CDCM_InfoNodo implements java.io.Serializable
{
	private static final long serialVersionUID = 8995818649596303928L;
	private String origem;		// core de origem da comunicação
	private String destino;		// core de destino da comunicação
	private long phits;			// Número de phits da comunicação
	private int computacao;		// Numero de ciclos de computaçao do core origem antes de enviar a mensagem
	private int id;	 			// Atributo que distingüe um nodo de todos os outros

	public CDCM_InfoNodo(String origem, String destino, long phits, int id, int computacao)
	{
		this.origem = origem;
		this.destino = destino;
		this.phits = phits;
		this.computacao = computacao;
		this.id = id;
	}
	public CDCM_InfoNodo(int id)
	{
		this.origem = null;
		this.destino = null;
		this.phits = 0;
		this.computacao = -1;
		this.id = id;
	}
	public void setOrigem(String origem)
	{
		this.origem = origem;
	}
	public String getOrigem()
	{
		return origem;
	}
	public void setDestino(String destino)
	{
		this.destino = destino;
	}
	public String getDestino()
	{
		return destino;
	}
	public void setPhits(long phits)
	{
		this.phits = phits;
	}
	public long getPhits()
	{
		return phits;
	}
	public void setComputacao(int computacao)
	{
		this.computacao = computacao;
	}
	public int getComputacao()
	{
		return computacao;
	}
	public int getID()
	{
		return id;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 DEPURAÇÃO
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	/
	public void exibe()
	{
		System.out.print(", ID:" + id  + "  " + origem + " -> " + destino + " (" + computacao + ")" + "[" + phits + "]\t");
	}
}