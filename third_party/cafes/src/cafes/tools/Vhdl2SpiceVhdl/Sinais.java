package cafes.tools.Vhdl2SpiceVhdl;

public class Sinais
{
	private String sinal;
	private int numero;
	
	public Sinais()
	{
	numero = 0;
	}
	public Sinais(String sinal)
	{
		this.sinal = sinal;
	numero = 0;
	}
	public Sinais(String sinal, int numero)
	{
		this.sinal = sinal;
	this.numero = numero;
	}
	public String getSinal()
	{
	return sinal;
	}
	public int getNumero()
	{
	return numero;
	}
	public void setSinal(String sinal)
	{
	this.sinal = sinal;
	}
	public void setNumero(int numero)
	{
	this.numero = numero;
	}
	public void incrementa()
	{
	this.numero++;
	}
}
