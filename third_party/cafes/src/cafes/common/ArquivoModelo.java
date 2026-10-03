package cafes.common;

public class ArquivoModelo
{
	private static Arquivo arquivo = new Arquivo();

	public static void setDiretorioArquivo(String dir, String arq)
	{
		arquivo.setDiretorioArquivo(dir, arq);
	}
	public static void setArquivo(String arq)
	{
		arquivo.setArquivo(arq);
	}
	public static String getDiretorio()
	{
		if(arquivo == null)
			return null;
		return arquivo.getDiretorio();
	}
	public static String getArquivo()
	{
		if(arquivo == null)
			return null;
		return arquivo.getArquivo();
	}
	public static String getNome()
	{
		if(arquivo == null)
			return null;
		return arquivo.getNome();
	}
	public static String getExtensao()
	{
		if(arquivo == null)
			return null;
		return arquivo.getExtensao();
	}
	public static String appenda(String extensao)
	{
		if(arquivo == null)
			return null;
		return arquivo.getNome() + extensao;
	}
}
