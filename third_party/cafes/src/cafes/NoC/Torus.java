package cafes.NoC;

public class Torus
{
	public static boolean deveAvancarPonteiro(int origem, int destino, int tamanho)
	{
		boolean externo;
		
		int distancia = Math.abs(origem-destino);
		if(distancia < tamanho - distancia)
			externo = true;
		else
			externo = false;
		if(destino > origem)
		{
			if(externo)
				return false;
			return true;
		}
		if(externo)
			return true;
		return false;
	}
	public static int incrementaPonteiro(int ponteiro, int tamanho)
	{
		ponteiro = ponteiro + 1;
		if(ponteiro >= tamanho)
			ponteiro = 0;
		return ponteiro;
	}
	public static int decrementaPonteiro(int ponteiro, int tamanho)
	{
		ponteiro = ponteiro - 1;
		if(ponteiro < 0)
			ponteiro = tamanho-1;
		return ponteiro;
	}
 }