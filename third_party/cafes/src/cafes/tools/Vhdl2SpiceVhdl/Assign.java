package cafes.tools.Vhdl2SpiceVhdl;

public class Assign
{
	private String variavel;
	private String valor;

	public Assign(String variavel, String valor)
	{
		this.variavel = variavel;
		this.valor = valor;
	}
	public String getValor()
	{
	return valor;
	}
	public void setValor(String str)
	{
	valor = str;
	}
	public void setValor(byte str[])
	{
	valor = new String(str);
	}
	public String getVariavel()
	{
	return variavel;
	}
	public void setVariavel(byte str[])
	{
	variavel = new String(str);
	}
	public void setVariavel(String str)
	{
	variavel = str;
	}
}
