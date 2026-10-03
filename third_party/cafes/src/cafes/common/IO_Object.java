package cafes.common;

import javax.swing.*;
import java.io.*;

public class IO_Object
{
	public static void write(String file, Object o)
	{
		try
		{
			OutputStream os = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(o);
			oos.close();
		}
		catch(IOException e)
		{
			System.err.println("IO Stream problems");
			e.printStackTrace();
		}
		catch(Exception e)
		{
			System.err.println("Exception problems");
		}
	}
	public static Object read(String file)
	{
		Object o=null;

		try
		{
			InputStream is = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(is);
			o = ois.readObject();
			ois.close();
		}
		catch(InvalidClassException e)
		{
			String str="Problems with file version (invalid class)!";

			System.out.println(str);
			JOptionPane.showMessageDialog(null, str, "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(StreamCorruptedException e)
		{
			String str="Problems with file version (corrupted stream)!";

			System.out.println(str);
			JOptionPane.showMessageDialog(null, str, "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch(ClassNotFoundException e)
		{
			System.err.println("Class not found");
			e.printStackTrace();
		}
		catch(IOException e)
		{
			System.err.println("IO Problems");
			e.printStackTrace();
		}
		catch(Exception e)
		{
			System.out.println("PROBLEM 4!");
		}
		return o;
	}
}
