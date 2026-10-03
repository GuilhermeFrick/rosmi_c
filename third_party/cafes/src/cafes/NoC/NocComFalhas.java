package cafes.NoC;

public class NocComFalhas
{
	private static RoteadorComFalhas inicio, fim;
	
	public static void reset()
	{
		inicio = null;
		fim = null;
	}
	public static void add(RoteadorComFalhas roteador)
	{
		if(inicio == null)
			inicio = roteador;
		else
			fim.setProx(roteador);
		fim = roteador;
	}
	public static boolean temFalha(String strTipo, int linha, int coluna, int altura)
	{
		RoteadorComFalhas rcf = findRouter(linha, coluna,altura);
		if(rcf == null)
			return false;
		if(strTipo.equalsIgnoreCase("I"))
			return rcf.linkInferiorComFalha();
		if(strTipo.equalsIgnoreCase("U"))
			return rcf.linkSuperiorComFalha();
		if(strTipo.equalsIgnoreCase("N"))
			return rcf.linkNorteComFalha();
		if(strTipo.equalsIgnoreCase("S"))
			return rcf.linkSulComFalha();
		if(strTipo.equalsIgnoreCase("E"))
			return rcf.linkLesteComFalha();
		if(strTipo.equalsIgnoreCase("W"))
			return rcf.linkOesteComFalha();
		if(strTipo.equalsIgnoreCase("L"))
			return rcf.linkLocalComFalha();
		if(strTipo.equalsIgnoreCase("R"))
			return rcf.roteadorComFalha();
		if(strTipo.equalsIgnoreCase("T"))
			return rcf.tileComFalha();
		return false;
	}
	public static boolean temTileDeEspera(int linha, int coluna, int altura)
	{
		RoteadorComFalhas rcf = findRouter(linha, coluna,  altura);
		if(rcf == null)
			return false;
		return rcf.tileDeEspera();
	}
	public static RoteadorComFalhas findRouter(int linha, int coluna, int altura)
	{
		RoteadorComFalhas p = inicio;
		while(p != null)
		{
			if(p.getLinha() == linha && p.getColuna() == coluna&& p.getAltura() == altura)
				return p;
			p = p.getProx();
		}
		return null;
	}
}
