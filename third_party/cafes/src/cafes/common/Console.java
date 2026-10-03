package cafes.common;

/*
	Conjunto de métodos auxiliares para a leitura de strings, inteiros e doubles.
*/
import java.io.*;

public class Console
{
// Lê uma string terminada por ENTER
	public static String readString()
	{
		String str = "";
		char c=' ';

		for(;;)
		{
			try
			{
				c = (char)System.in.read();
			} catch(IOException e) {	}
			if(c<=0 || c=='\n')
				break;
			str += c;
		}
		return str;
	}
	public static String readString(String prompt)
	{
		System.out.print(prompt + " ");
		return readString();
	}
	public static String getString()
	{
		byte vetorBytes[] = new byte[200];
		int size=0;

		try
		{
			size = System.in.read(vetorBytes);
		}
		catch(IOException e) {	}
		String str = new String(vetorBytes);

		return str.substring(0, size-2); // Retira o ENTER
	}
	public static String getString(String prompt)
	{
		System.out.print(prompt + " ");
		return getString();
	}

// Lê uma palavra do console (uma seqüência de caracateres terminados por espaço).
	public static String readWord()
	{
		char c;
		String str = "";

		for(;;)
		{
			try
			{
				c = (char)System.in.read();
				if(c<=0 || Character.isWhitespace(c))
					break;
				str += c;
			}
			catch(IOException e) {	 }
		}
		return str;
	}

// Lê um inteiro do console
	public static int readInt(String prompt)
	{
		while(true)
		{
			System.out.print(prompt + " ");
			try
			{
				return Integer.parseInt(readString().trim());
			}
			catch(NumberFormatException e)
			{
				System.out.println("Nao e' um inteiro. Digite novamente!");
			}
		}
	}

// Lê um double do console
	public static double readDouble(String prompt)
	{
		while(true)
		{
			System.out.print(prompt + " ");
			try
			{
				return Double.valueOf(readString().trim()).doubleValue();
			}
			catch(NumberFormatException e)
			{
				System.out.println("Nao e' um double. Digite novamente!");
			}
		}
	}
}
