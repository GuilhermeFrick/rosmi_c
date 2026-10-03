package cafes.common;

import java.util.*;

public class Randomico extends Random
{
	private static final long serialVersionUID = -2101656360380656895L;
	private LinhaColunaAltura lc[];
	private int lcPosition;

	public Randomico()
	{
		super();
	}
	public Randomico(long seed)
	{
		super(seed);
	}
	public int randomNumber(int maximo)
	{
		if(maximo == 0)
			return 0;
		int proxInt = nextInt();
		if(proxInt < 0)
			proxInt *= -1;
		proxInt = proxInt % maximo;

		return proxInt;
	}
	public int randomNumberExcept(int maximo, int except)
	{
		if(maximo == 0)
			return 0;
		int proxInt, trys = 3;
		while(true)
		{
			proxInt = nextInt();
			if(proxInt < 0)
				proxInt *= -1;
			proxInt = proxInt % maximo;
			if(proxInt != except)
				return proxInt;
			if(trys <= 0)
				break;
			trys--;
		}
		return proxInt;
	}
	public int randomNumber(int minimo, int maximo)
	{
		if(minimo > maximo)
			throw new RuntimeException("Error: Minimum value=" + minimo + " greater than maximum valeu=" + maximo);
		int proxInt = minimo - 1;

		if(minimo == maximo)
			return minimo;
		while(proxInt < minimo || proxInt > maximo)
		{
			proxInt = nextInt();
			if(proxInt > minimo && proxInt > maximo && maximo != 0)
				proxInt = proxInt % maximo;
		}
		return proxInt;
	}
	public void fillIntVectorRandomly(int[] vetorInts, int maximo)
	{
		if(vetorInts.length <= 0)
			return;
		int vetAux[] = new int[maximo];
		int nElements=0;

		for(int i = 0; i < vetAux.length; i++)
			vetAux[i] = i;
		while(true)
		{
			int proxInt = randomNumber(maximo);

			vetorInts[nElements] = vetAux[proxInt];
			maximo--;
			vetAux[proxInt] = vetAux[maximo];
			if(++nElements >= vetAux.length)
				return;
		}
	}
	public LinhaColunaAltura getElementLinhaColunaVector()
	{
		if(++lcPosition>=lc.length)
			lcPosition = 0;
		return lc[lcPosition];
	}
	public void fillLinhaColunaVectorRandomly(int maxLinha, int maxColuna, int maxAltura)
	{
		int maxVector = maxColuna*maxLinha*maxAltura;

		if(maxVector<=0)
			return;
		lcPosition = -1;
		lc = new LinhaColunaAltura[maxVector];
		LinhaColunaAltura vetAux[] = new LinhaColunaAltura[maxVector];
		int nElements=0;

		for(int i=0; i<vetAux.length; i++)
		{
			int l = i/maxLinha;
			int c = i%maxColuna;
			int a = i%maxAltura;
			vetAux[i] = new LinhaColunaAltura(l, c,a);
		}
		while(true)
		{
			int proxInt=randomNumber(maxVector);

			lc[nElements] = vetAux[proxInt];
			maxVector--;
			vetAux[proxInt] = vetAux[maxVector];
			if(++nElements>=lc.length)
				return;
		}
	}
}