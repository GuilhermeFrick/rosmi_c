package cafes.model.CDCM.app;

class VertexContent implements java.io.Serializable
{
	private static final long serialVersionUID = 8995818649596303928L;
	public static final int START = -1;
	public static final int END = -2;
	private String source;		// core de origem da comunicação
	private String target;		// core de destino da comunicação
	private long communicationVolume;			// Número de phits da comunicação
	private long computationTime;	// Numero de ciclos de computaçao do core origem antes de enviar a mensagem
	private int id;	 			// Atributo que distingüe um nodo de todos os outros
	private static int idGlobal = 0;

	public VertexContent(int id, String source, String target, long computationTime, long communicationVolume)
	{
		setSource(source);
		setTarget(target);
		setCommunicationVolume(communicationVolume);
		setComputationTime(computationTime);
		this.id = id;
	}
	public static void resetGlobalId()
	{
		idGlobal = 0;
	}
	public VertexContent(String source, String target, long communicationVolume, long computationTime)
	{
		this(idGlobal++, source, target, computationTime, communicationVolume);
	}
	public VertexContent(int id)
	{
		this(id, null, null, -1, 0);
	}
	public void setSource(String source)
	{
		this.source = source;
	}
	public String getSource()
	{
		return source;
	}
	public void setTarget(String target)
	{
		this.target = target;
	}
	public String getTarget()
	{
		return target;
	}
	public void setCommunicationVolume(long communicationVolume)
	{
		this.communicationVolume = communicationVolume;
	}
	public long getCommunicationVolume()
	{
		return communicationVolume;
	}
	public void setComputationTime(long computationTime)
	{
		this.computationTime = computationTime;
	}
	public long getComputationTime()
	{
		return computationTime;
	}
	public int getID()
	{
		return id;
	}
	public String getStringID()
	{
		switch(id)
		{
			case START:
				return "START";

			case END:
				return "END";
		}
		return "" + id;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public String toString()
	{
		return " " + id  + " " + source + " - " + target + " " + computationTime + " : " + communicationVolume;
	}
	public String toStringExibe()
	{
		return "\nID:" + id  + "  " + source + " -> " + target + " (" + computationTime + ")" + "[" + communicationVolume + "]";
	}
	public void exibe()
	{
		System.out.println(toStringExibe());
	}
}