package cafes.common;


public class RandRangeGauss extends RandGauss
{
	private static final long serialVersionUID = 4578894425950598842L;
	private double minimum;
	private double maximum;

	public RandRangeGauss(double mean, double standardDeviation, double minimum, double maximum)
	{
		super(mean, standardDeviation);
		this.minimum = minimum;
		this.maximum = maximum;
	}
	public double next()
	{
		double ret;
		int limit = 10; // Número máximo de vezes que o laço de Gaussiana pode ser executado

		if(maximum == minimum)
			return minimum;
		do
		{
			if(--limit <= 0)
				return mean;
			ret = super.next();
		}
		while(ret > maximum || ret < minimum);
		
		return ret;
	}
}
