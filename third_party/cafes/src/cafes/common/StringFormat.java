package cafes.common;

public class StringFormat
{
	public static final int CENTER = 0;
	public static final int LEFT = 1;
	public static final int RIGTH = 2;

	public static String toHexa(long value, int tamanho)
	{
		return format(Long.toHexString(value), RIGTH, tamanho, '0');
	}
	public static String toHexa(int value, int tamanho)
	{
		return format(Integer.toHexString(value), RIGTH, tamanho, '0');
	}
	public static String format(int value, int tipo, int tamanho)
	{
		return format(Integer.toString(value), tipo, tamanho, ' ');
	}
	public static String format(long value, int tipo, int tamanho)
	{
		return format(Long.toString(value), tipo, tamanho, ' ');
	}
	public static String format(String str, int tipo, int tamanho)
	{
		return format(str, tipo, tamanho, ' ');
	}
	public static String format(String str, int tipo, int tamanho, char base)
	{
		if(str.length()>tamanho)
			tamanho = str.length();
		byte aux[] = new byte[tamanho];
		for(int i=0; i<aux.length; i++)
			aux[i] = (byte)base;
		switch(tipo)
		{
			default:
			case CENTER:
				for(int i=(tamanho-str.length())/2, j=0; j<str.length(); i++, j++)
					aux[i] = (byte)str.charAt(j);
				break;

			case LEFT:
				for(int i=0; i<str.length(); i++)
					aux[i] = (byte)str.charAt(i);
				break;

			case RIGTH:
				for(int i=aux.length-1, j=str.length()-1; j>=0; i--, j--)
					aux[i] = (byte)str.charAt(j);
				break;
		}
		return new String(aux);
	}
}
