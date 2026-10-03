package cafes.common;

public class RandGauss extends Randomico
{
	private static final long serialVersionUID = -4450077648443242551L;
	protected double mean;
	protected double standardDeviation;

	public RandGauss()
	{
		super();
	}
	public RandGauss(double mean, double standardDeviation)
	{
		this();
		this.mean = mean;
		this.standardDeviation = standardDeviation;
	}
	public double next()
	{
		return standardDeviation * nextGaussian() + mean;
	}
}
