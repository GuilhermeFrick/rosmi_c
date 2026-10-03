package cafes.common;

public class Arquivo
{
	private String diretorio;
	private String arquivo;
	private String nome;
	private String extensao;

	public Arquivo(String diretorio, String arquivo)
	{
		setDiretorioArquivo(diretorio, arquivo);
	}
	public Arquivo(String arquivo)
	{
		setArquivo(arquivo);
	}
	public Arquivo()
	{
		this.diretorio = null;
		this.arquivo = null;
		this.nome = null;
		this.extensao = null;
	}
	public void setArquivo(String arquivo)
	{
		this.arquivo = arquivo;
		extraiNomeExtensao();
	}
	public void setDiretorioArquivo(String diretorio, String arquivo)
	{
		this.diretorio = diretorio;
		this.arquivo = arquivo;
		extraiNomeExtensao();
	}
	private void extraiNomeExtensao()
	{
		if(arquivo==null)
			return;
		int k;

		for(k=arquivo.length()-1; k>0; k--)
		{
			if(arquivo.charAt(k) == '.')
				break;
		}
		if(k==0)
		{
			extensao = null;
			nome = new String(arquivo);
		}
		else
		{
			nome = arquivo.substring(0, k);
			if(k+1>=arquivo.length())
				extensao = null;
			else
				extensao = arquivo.substring(k+1, arquivo.length());
		}
	}
	public String getExtensao()
	{
		return extensao;
	}
	public String getNome()
	{
		return nome;
	}
	public String getDiretorioNome()
	{
		return diretorio + nome;
	}
	public String getDiretorioArquivo()
	{
		return diretorio + arquivo;
	}
	public String getDiretorio()
	{
		return diretorio;
	}
	public String getArquivo()
	{
		return arquivo;
	}
}
