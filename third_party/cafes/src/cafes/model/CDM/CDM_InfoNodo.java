package cafes.model.CDM;

class CDM_InfoNodo implements java.io.Serializable
{
	private static final long serialVersionUID = -7640732782421765350L;
	private String origem;	// core de origem da comunicação
	private String destino;	// core de destino da comunicação
	private long phits;		// Número de phits da comunicação
	private int id;	 		// Atributo que distingüe um nodo de todos os outros

	public CDM_InfoNodo(String origem, String destino, long phits, int id)
	{
		this.origem = origem;
		this.destino = destino;
		this.phits = phits;
		this.id = id;
	}
	public CDM_InfoNodo(int id)
	{
		this.origem = null;
		this.destino = null;
		this.phits = 0;
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
	public int getID()
	{
		return id;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 DEPURAÇÃO
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	/
	public void exibe()
	{
		System.out.print(", ID:" + id  + "  " + origem + " -> " + destino + " [" + phits + "]\t");
	}
}