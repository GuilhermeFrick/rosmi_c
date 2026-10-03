package cafes.tools.Vhdl2SpiceVhdl;

public class Gate
{
	private String name;
	private String gate;
	private int numTransistores;

	public Gate(String name, String gate, int numTransistores)
	{
		this.name = name;
		this.gate = gate;
		this.numTransistores = numTransistores;
	}
	public String getName()
	{
	return name;
	}
	public String getGate()
	{
	return gate;
	}
	public int getNumTransistores()
	{
	return numTransistores;
	}
}
