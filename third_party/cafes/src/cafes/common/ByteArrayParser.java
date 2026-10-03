package cafes.common;

public class ByteArrayParser
{
	private byte byteArray[];
	private int pointer;
	private boolean reachEndFlag;

	public ByteArrayParser(byte byteArray[])
	{
		this.byteArray = byteArray;
		reset();
	}
	public int flushLineAndCountString()
	{
		int counter = 0;

		if(!reachEnd())
		{
			while(byteArray[pointer-1]!='\n')
			{
				readNextString();
				counter++;
				if(reachEnd())
					break;
			}
		}
		return counter-1;
	}
	public void reset()
	{
		pointer = 0;
		reachEndFlag = false;
	}
	public boolean reachEnd()
	{
		return reachEndFlag;
	}
	private boolean chegouAoFim()
	{
		if(pointer>=byteArray.length)
		{
			reachEndFlag = true;
			return true;
		}
		return false;
	}
	public boolean isSpace()
	{
		if(chegouAoFim())
			return false;
		switch(byteArray[pointer])
		{
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				return true;
		}
		return false;
	}
	public boolean isNewLine()
	{
		if(chegouAoFim())
			return false;
		if(byteArray[pointer]=='\n')
			return true;
		return false;
	}
	public String readNextString()
	{
		int inicialPointer;
		String str = new String(byteArray);
		boolean ehEspaco = false;

		while(!reachEnd()) // Elimina os espaços
		{
			if(chegouAoFim())
				return null;
			if(!isSpace())
				break;
			pointer++;
		}
		inicialPointer = pointer;
		while(!reachEnd()) // Monta a string
		{
			if(chegouAoFim())
				return null;
			if(isSpace())
				ehEspaco = true;
			pointer++;
			if(ehEspaco)
				break;
		}
		String outString = null;
		try
		{
			if(pointer > inicialPointer)
				outString = str.substring(inicialPointer, pointer-1);
		}
		catch(Exception e)
		{
			System.out.println("String: " + str + ", inicialPointer: " + inicialPointer + ", pointer: " + pointer);
			e.printStackTrace();
		}
		return outString;
	}
	public void readUntilNotEndOfLine()
	{
		pointer--; // Para não perder os tokens sem comentário
		while(!reachEnd())
		{
			if(chegouAoFim())
				return;
			if(isNewLine())
				return;
			pointer++;
		}
	}
	public String readNextStringInLine()
	{
		int inicialPointer = pointer;
		String str = new String(byteArray);
		boolean ehEspaco = false;

		while(!reachEnd())
		{
			if(chegouAoFim())
				return null;
			if(isSpace())
				ehEspaco = true;
			if(isNewLine())
			{
				if(inicialPointer!=pointer)
					return str.substring(inicialPointer, pointer);
				pointer++;
				chegouAoFim();
				return "";
			}
			pointer++;
			if(ehEspaco)
				break;
		}
		return str.substring(inicialPointer, pointer-1);
	}
}
