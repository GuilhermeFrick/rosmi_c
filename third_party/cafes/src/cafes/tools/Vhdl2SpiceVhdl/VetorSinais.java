package cafes.tools.Vhdl2SpiceVhdl;

public class VetorSinais
{
	private Sinais sinais[];
	private int size;
	
	public VetorSinais(int tamMax)
	{
		sinais = new Sinais[tamMax];
		this.size = 0;
	}
	public void add(Sinais s)
	{
	sinais[size++] = s;
	}
	public Sinais get(int p)
	{
	return sinais[p];
	}
	public int size()
	{
	return size;
	}
	public void incrementa(int p)
	{
	sinais[p].incrementa();
	}
}
