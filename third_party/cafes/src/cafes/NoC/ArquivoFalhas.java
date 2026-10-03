package cafes.NoC;

import java.io.*;
import cafes.common.*;

public class ArquivoFalhas
{
	private String stringTipos[] = {"N", "S", "E", "W", "L", "R", "T", "X"};
	private BufferedReader bin;
	private String nomeArquivoFalhas;
	private String linhaArquivo;

	// amory
	/*public ArquivoFalhas()
	{
		this("faults.txt");
	}*/
	public ArquivoFalhas(String nomeArquivoFalhas)
	{
		this.nomeArquivoFalhas = nomeArquivoFalhas;
		//System.out.println("aonde estou 2" + this.nomeArquivoFalhas);
		NocComFalhas.reset();
		leArquivo();
	}
	private void leArquivo()
	{
		bin = BuffReader.openFile(nomeArquivoFalhas);
		if(bin == null)
			return;
		try
		{
			while(true)
			{
				linhaArquivo = bin.readLine();
				if(linhaArquivo == null)
					break;
				if(trataLinha() == false)
					break;
			}
			bin.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private boolean trataLinha()
	{
		try
		{
			findToken("R");
			findToken("[");
			int linha = getInteger();
			findToken(",");
			int coluna = getInteger();
			findToken(",");
			int altura = getInteger();
			findToken("]");
			findToken(":");
			RoteadorComFalhas rcf = new RoteadorComFalhas(linha, coluna,altura);
			while(true)
			{
				String link = findTokens(stringTipos);

				if(link == null)
					break;
				if(link.equalsIgnoreCase("I"))
					rcf.setFalhaInferior();
				if(link.equalsIgnoreCase("U"))
					rcf.setFalhaSuperior();
				if(link.equalsIgnoreCase("N"))
					rcf.setFalhaNorte();
				else if(link.equalsIgnoreCase("S"))
					rcf.setFalhaSul();
				else if(link.equalsIgnoreCase("E"))
					rcf.setFalhaLeste();
				else if(link.equalsIgnoreCase("W"))
					rcf.setFalhaOeste();
				else if(link.equalsIgnoreCase("L"))
					rcf.setFalhaLocal();
				else if(link.equalsIgnoreCase("R"))
					rcf.setFalhaRoteador();
				else if(link.equalsIgnoreCase("T"))
					rcf.setFalhaTile();
				else if(link.equalsIgnoreCase("X"))
					rcf.setTileDeEspera();
				findToken(",");
			}
			NocComFalhas.add(rcf);
		}
		catch(CommentException ce)         { return true;  }
		catch(NumberFormatException nfe)   { return false; }
		catch(InvalidSintaxException ise)  { return false; }
		return true;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Function: findToken
// Description: Pesquisa por um token fornecido no parâmetro token dentro do String fornecido no parâmetro line.
//	            O token deve ser o primeiro String dentro de line. Caso for encontrado retorna um substring de line
//	            sem o token e, eventuais espaços colocados antes do mesmo. Caso não encontrar o token ou este não for
//	            o primeiro string encontrado, então o método lança a exceção InvalidSintaxException
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private boolean findToken(String token) throws InvalidSintaxException, CommentException
	{
		String stringTokens[] = new String[1];
		
		stringTokens[0] = token;
		String retorno = findTokens(stringTokens);
		if(retorno == null)
			return false;
		return retorno.equals(token);
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Function: findTokens
// Description: Pesquisa por um token fornecido no parâmetro token dentro do String fornecido no parâmetro line.
//			    O token deve ser o primeiro String dentro de line. Caso for encontrado retorna um substring de line
//			    sem o token e, eventuais espaços colocados antes do mesmo. Caso não encontrar o token ou este não for
//			    o primeiro string encontrado, então o método lança a exceção InvalidSintaxException
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private String findTokens(String tokens[]) throws InvalidSintaxException, CommentException
	{
		byte arrayLine[] = Token.charArrayToByteArray(linhaArquivo.toCharArray());
		byte arrayTesteToken[] = new byte[arrayLine.length];
		int posicaoLine = 0, posicaoTesteToken = 0;
		boolean temPeloMenosUmString = false;

		while(posicaoLine < arrayLine.length)
		{
			if(Token.isSpaceMin(arrayLine[posicaoLine]))
				posicaoLine++;
			else
			{
				if(arrayLine[posicaoLine] == '#')
					throw new CommentException();
				temPeloMenosUmString = true;
				arrayTesteToken[posicaoTesteToken++] = arrayLine[posicaoLine++];
				for(int tokenTestado = 0; tokenTestado < tokens.length; tokenTestado++)
				{
					byte arrayToken[] = Token.charArrayToByteArray(tokens[tokenTestado].toCharArray());
					
					if(posicaoTesteToken >= arrayToken.length)
					{
						if(isSubByteArraysEquals(arrayTesteToken, arrayToken, posicaoTesteToken))
						{
							byte str_tmp[] = new byte[arrayLine.length];
							int numBytes = arrayLine.length - posicaoLine;

							System.arraycopy(arrayLine, posicaoLine, str_tmp, 0, numBytes);
							str_tmp[numBytes] = '\0';
							linhaArquivo = new String(str_tmp).trim();
							return tokens[tokenTestado];
						}
					}
				}
			}
		}
		if(linhaArquivo.length() > 0 && (tokens.length == 1 || temPeloMenosUmString))
			throw new InvalidSintaxException();
		linhaArquivo = "";
		return null;
	}
	private boolean isSubByteArraysEquals(byte arrayA[], byte arrayB[], int numBytes)
	{
		for(int i = 0; i < numBytes; i++)
		{
			if(i >= arrayA.length)
				return false;
			if(i >= arrayB.length)
				return false;
			if(Character.toUpperCase(arrayA[i]) != Character.toUpperCase(arrayB[i]))
				return false;
		}
		return true;
	}
	private int getInteger() throws NumberFormatException
	{
		String stringInteira = Token.getStringInteger(linhaArquivo, 0);
		if(stringInteira == null)
			throw new NumberFormatException();
		linhaArquivo = linhaArquivo.substring(stringInteira.length());
		return new Integer(stringInteira).intValue();
	}
}
