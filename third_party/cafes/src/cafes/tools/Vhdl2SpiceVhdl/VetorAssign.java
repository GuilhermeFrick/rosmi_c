package cafes.tools.Vhdl2SpiceVhdl;

import cafes.common.*;

public class VetorAssign
{
	private Assign assign[];
	private int indiceAssigns;
	
	VetorAssign(int tam)
	{
		assign = new Assign[tam];
		indiceAssigns = 0;
	}
	public String TrocaAssigns(String str)
	{
		if(indiceAssigns==0)
			return str;
		for(int i=0; i<indiceAssigns; i++)
		{
			if(assign[i].getVariavel().equals(str))
				return assign[i].getValor();
		}
		return str;
	}
	public void ArmazenaAssign(String line)
	{
		if(indiceAssigns>assign.length)
		{
			System.out.println("\nNúmero de assigns maior que o alocado!\n\n");
			System.exit(-1);
		}
		String nome = Token.getToken(line, 0);
		String valor = Token.getToken(line, 2);
		if(valor.equals("'0'"))
			valor = "0";
		else
		{
			if(valor.equals("'1'"))
				valor = "VCC";
		}
		assign[indiceAssigns] = new Assign(nome, valor);
		indiceAssigns++;
	}
}
