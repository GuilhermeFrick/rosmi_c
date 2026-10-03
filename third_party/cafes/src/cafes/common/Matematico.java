package cafes.common;

public class Matematico
{
	public static long fatorial(long fat)
	{
		if(fat<=1)
			return 1;
		return fat * fatorial(fat-1);
	}
}
