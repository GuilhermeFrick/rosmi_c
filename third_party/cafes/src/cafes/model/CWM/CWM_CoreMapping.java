package cafes.model.CWM;

import cafes.common.*;

class CWM_CoreMapping
{
	private static LinhaColunaAltura vet[];
	private static String core[];
	private static int pointer;
	
	public CWM_CoreMapping(int size)
	{
		//Cria vetor LinhaxColuna onde a 
		//  posição 0 = linha(0) e coluna(0) e 
		//  posição 1 = linha(0) e coluna(0) e 
		vet = new LinhaColunaAltura[size]; 
		core = new String[size];
		pointer = 0;
	}
	public static int numberOfElement()
	{
		return pointer;
	}
	public static LinhaColunaAltura positionOfElement(int element)
	{
		return vet[element];
	}
	public static String coreOfElement(int element)
	{
		return core[element];
	}
	public static void insertCorePosition(int linha, int coluna,int altura, String c)
	{
		vet[pointer] = new LinhaColunaAltura(linha, coluna,altura);
		core[pointer] = new String(c);
		pointer++;
	}
	public static LinhaColunaAltura positionOfCore(String c)
	{
		for(int i=0; i<core.length; i++)
		{
			if(c.equals(core[i]))
				return vet[i];
		}
		return null;
	}
	public static String coreOfPosition(LinhaColunaAltura pos)
	{
		for(int i=0; i<vet.length; i++)
		{
			if((pos.getLinha()==vet[i].getLinha()) && (pos.getColuna()==vet[i].getColuna()))
				return core[i];
		}
		return null;
	}
}
