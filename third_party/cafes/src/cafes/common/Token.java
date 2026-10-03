package cafes.common;

public class Token
{
	private static int posicaoToken = 0;
	private static int posicaoTks = 0;
	public static final int LINE_MAX = 1000;

	public static boolean numeroParentesesImpar(String line)
	{
		return numeroParentesesImpar(charArrayToByteArray(line.toCharArray()));
	}
	public static boolean numeroParentesesImpar(byte line[])
	{
		int parenteses=0;

		for(int i=0; i<line.length; i++)
		{
			if(line[i]=='(')
				parenteses++;
			if(line[i]==')')
				parenteses--;
		}
		return ((parenteses%2)!=0);
	}
	public static int naoTemPontoEVirgula(String line)
	{
		return naoTemPontoEVirgula(charArrayToByteArray(line.toCharArray()));
	}
	public static int naoTemPontoEVirgula(byte line[])
	{
		for(int i = 0; i < line.length; i++)
		{
			if(line[i] == ';')
				return 0;
		}
		return 1;
	}
	public static boolean isSpaceMin(byte c)
	{
		switch(c)
		{
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				return true;
		}
		return false;
	}
	public static String getStringInteger(String line, int tok_num)
	{
		return getTokenMinAux(charArrayToByteArray(line.toCharArray()), tok_num, false);

	}
	public static String getToken(String line, int tok_num)
	{
		return getTokenMin(charArrayToByteArray(line.toCharArray()), tok_num);
	}
	private static String getTokenMin(byte line[], int tok_num)
	{
		return getTokenMinAux(line, tok_num, true);
	}
	private static String getTokenMinAux(byte line[], int tok_num, boolean minimumSpace)
	{
		byte str_tmp[] = new byte[LINE_MAX];
		int sz = 0;
		boolean found = false, whileSpace = false;

		posicaoToken = 0;
		posicaoTks = 0;
		if(posicaoToken < line.length)
		{
			if(minimumSpace && isSpaceMin(line[posicaoToken]) || !minimumSpace && isSpace(line[posicaoToken]))
					whileSpace = true;
		}
		while(posicaoToken < line.length && posicaoTks <= tok_num)
		{
			if(minimumSpace && isSpaceMin(line[posicaoToken]) || !minimumSpace && isSpace(line[posicaoToken]))
			{
				if(!whileSpace)
				{
					posicaoTks++;
					whileSpace = true;
				}
			}
			else
			{
				whileSpace = false;
				if(tok_num == posicaoTks)
				{
					str_tmp[sz++] = line[posicaoToken];
					found = true;
				}
			}
			posicaoToken++;
		}
		if(found)
		{
			str_tmp[sz] = '\0';
			return new String(str_tmp).trim();
		}
		return null;
	}
	public static boolean isSpace(byte c)
	{
		switch(c)
		{
			case ' ':
			case '\t':
			case '\r':
			case '\n':
			case '(':
			case ')':
			case '[':
			case ']':
			case ',' :
			case ';' :
			case '=' :
			case '>' :
			case '.' :
				return true;

			default:
				return false;
		}
	}
	public static byte[] charArrayToByteArray(char array[])
	{
		byte b[] = new byte[array.length];

		for(int i=0; i<array.length; i++)
			b[i] = (byte)array[i];

		return b;
	}
}
