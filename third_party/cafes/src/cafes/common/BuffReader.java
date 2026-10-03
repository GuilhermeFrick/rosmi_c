package cafes.common;

import java.io.*;
import java.net.URL;

public class BuffReader
{
	public static BufferedReader openFile(String str)
	{
		BufferedReader bin = null;

		try
		{
			File file = new File(str);
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			bin = new BufferedReader(new InputStreamReader(in));
		}
		catch(Exception e)
		{
			System.err.println("Nao eh possivel abrir arquivo: " + str);
		}
		return bin;
	}
	public static BufferedReader openFile(URL url)
	{
		BufferedReader bin = null;

		try
		{
			DataInputStream in = new DataInputStream(url.openStream());
			bin = new BufferedReader(new InputStreamReader(in));
		}
		catch(Exception e)
		{
			System.err.println("Nao eh possivel abrir arquivo: " + url);
		}
		return bin;
	}

}
