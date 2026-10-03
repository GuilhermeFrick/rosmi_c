package cafes.model.CDCM.app;

import cafes.common.*;

public class Constraints
{
	protected int numberOfProcessors;
	protected int numberOfGraphLevels;
	protected double dependenceDegree; // [0.0%, 100.0%]
	protected double minProbabilityOfEndVertexMeeting; // [0.0%, 100.0%]
	protected double maxProbabilityOfEndVertexMeeting; // [0.0%, 100.0%]
	protected RandRangeGauss parallelCommunicationRandGauss;
	protected RandRangeGauss computationTimeRandGauss;
	protected RandRangeGauss communicationVolumeRandGauss;
	
	public void setNumberofProcessors(int numberOfProcessors)
	{
		if(numberOfProcessors <= 0)
			throw new RuntimeException("Invalid Number of Processors!");
		this.numberOfProcessors = numberOfProcessors;
	}
	public void setNumberOfGraphLevels(int numberOfGraphLevels)
	{
		if(numberOfGraphLevels <= 0)
			throw new RuntimeException("Number of levels must be greater or equal 1!");
		this.numberOfGraphLevels = numberOfGraphLevels;
	}
	public void setDependenceDegree(double dependenceDegree)
	{
		if(dependenceDegree < 0 || dependenceDegree > 100)
			throw new RuntimeException("Dependence degree must be inside the range [0.0, 100.0]!");
		this.dependenceDegree = dependenceDegree;
	}
	public void setRangeProbabilityOfEndVertexMeeting(double minimum, double maximum)
	{
		if(minimum < 0 || minimum > 100)
			throw new RuntimeException("Minimum probability must be inside the range [0.0, 100.0]!");
		if(maximum < 0 || maximum > 100)
			throw new RuntimeException("Maximum probability must be inside the range [0.0, 100.0]!");
		if(minimum > maximum)
			throw new RuntimeException("Minimum probability must be lesser than the maximum probability!");
		this.minProbabilityOfEndVertexMeeting = minimum;
		this.maxProbabilityOfEndVertexMeeting = maximum;
	}
	public void setParallelCommunicationConstraints(double parallelCommunicationMean, double parallelCommunicationStandardDeviation, double minimumParallelCommunication, double maximumParallelCommunication)
	{
		if(parallelCommunicationMean < 1)
			throw new RuntimeException("Minimum parallel communication mean must be greater than 1!");
		if(parallelCommunicationStandardDeviation <= 0)
			throw new RuntimeException("Minimum parallel communication stadard deviation must be greater than 0!");
		if(minimumParallelCommunication < 1)
			throw new RuntimeException("Minimum parallel communication must be greater than 1!");
		if(maximumParallelCommunication < 1)
			throw new RuntimeException("Maximum parallel communication must be greater than 1!");
		if(minimumParallelCommunication > maximumParallelCommunication)
			throw new RuntimeException("Minimum lesser or equal than maximum parallel communication!");
		if(minimumParallelCommunication > parallelCommunicationMean || parallelCommunicationMean > maximumParallelCommunication)
			throw new RuntimeException("Parallel communication mean must be inside the range [minimum, maximum] parallel communication!");
		parallelCommunicationRandGauss = new RandRangeGauss(parallelCommunicationMean, parallelCommunicationStandardDeviation, minimumParallelCommunication, maximumParallelCommunication);			
	}
	public void setComputationTimeConstraints(double computationTimeMean, double computationTimeStandardDeviation, double minimumComputationTime, double maximumComputationTime)
	{
		if(computationTimeMean < 0)
			throw new RuntimeException("Minimum computation time mean must be greater than 0!");
		if(computationTimeStandardDeviation < 0)
			throw new RuntimeException("Minimum computation time stadard deviation must be greater than 0!");
		if(minimumComputationTime < 0)
			throw new RuntimeException("Minimum computation time must be greater than 0!");
		if(maximumComputationTime < 0)
			throw new RuntimeException("Maximum computation time must be greater than 0!");
		if(minimumComputationTime > maximumComputationTime)
			throw new RuntimeException("Minimum lesser or equal than maximum computation time!");
		if(minimumComputationTime > computationTimeMean || computationTimeMean > maximumComputationTime)
			throw new RuntimeException("Computation time mean must be inside the range [minimum, maximum] computation time!");
		computationTimeRandGauss = new RandRangeGauss(computationTimeMean, computationTimeStandardDeviation, minimumComputationTime, maximumComputationTime);			
	}
	public void setCommunicationVolumeConstraints(double communicationVolumeMean, double communicationVolumeStandardDeviation, double minimumCommunicationVolume, double maximumCommunicationVolume)
	{
		if(communicationVolumeMean < 0)
			throw new RuntimeException("Minimum communication volume mean must be greater than 0!");
		if(communicationVolumeStandardDeviation < 0)
			throw new RuntimeException("Minimum communication volume stadard deviation must be greater than 0!");
		if(minimumCommunicationVolume < 0)
			throw new RuntimeException("Minimum communication volume must be greater than 0!");
		if(maximumCommunicationVolume < 0)
			throw new RuntimeException("Maximum communication volume must be greater than 0!");
		if(minimumCommunicationVolume > maximumCommunicationVolume)
			throw new RuntimeException("Minimum lesser or equal than maximum communication volume!");
		if(minimumCommunicationVolume > communicationVolumeMean || communicationVolumeMean > maximumCommunicationVolume)
			throw new RuntimeException("Communication volume mean must be inside the range [minimum, maximum] communication volume!");
		communicationVolumeRandGauss = new RandRangeGauss(communicationVolumeMean, communicationVolumeStandardDeviation, minimumCommunicationVolume, maximumCommunicationVolume);			
	}
}
