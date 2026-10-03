package cafes.model.ACPM;

class ACPM_InfoNodo implements java.io.Serializable
{
	private static final long serialVersionUID = 2831219075393971873L;
	private String origem;	// core de origem da comunicação
	private String destino;	// core de destino da comunicação
	private long phits;		// Número de phits da comunicação

	public ACPM_InfoNodo(String origem, String destino, long phits)
	{
		this.origem = origem;
		this.destino = destino;
		this.phits = phits;
	}
	public ACPM_InfoNodo()
	{
		this.origem = null;
		this.destino = null;
		this.phits = 0;
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
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 DEPURAÇÃO
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	/
	public void exibe()
	{
		System.out.print("\t" + origem + " -> " + destino + " [" + phits + "]\t");
	}
}