package cafes.model.CDCM.app;

import java.io.*;
import java.util.*;

import cafes.common.RandRangeGauss;
import cafes.common.Randomico;

public class Description extends Constraints
{
	private String fileName;
	private ArrayList<String> processors = new ArrayList<String>();
	private ArrayList<Level> levels = new ArrayList<Level>();
	private Vertex startVertex = new Vertex(VertexContent.START);
	private Vertex endVertex = new Vertex(VertexContent.END);

	public Description(String fileName)
	{
		this.fileName = fileName;
	}
	private boolean generateAccordingToPercentProbability(double probability)
	{
		Randomico rand = new Randomico();
		int randNumber = rand.randomNumber(0, 100);
		if(randNumber > probability)
			return true;
		return false;
	}
	private int dependentVerticesProbability(int actualLevel)
	{
		if(actualLevel >= numberOfGraphLevels)
			return 0;
		double step = numberOfGraphLevels > 2 ? ( (maxProbabilityOfEndVertexMeeting - minProbabilityOfEndVertexMeeting) / (numberOfGraphLevels - 2.0)) : minProbabilityOfEndVertexMeeting;
		double probabilityToNotHaveDependentVertex = step * (actualLevel - 1) + minProbabilityOfEndVertexMeeting;
		if(generateAccordingToPercentProbability(probabilityToNotHaveDependentVertex))
		{
			Randomico rand = new Randomico();
			
			if(rand.nextBoolean())
				return 1;
			return 2;			
		}
		return 0;
	}
	private Level createCDCGLevels(int levelNumber, int numberOfVertices)
	{
		Level level = new Level();
		Level fatherLevel = levelNumber - 1 >= 0 ? levels.get(levelNumber - 1) : null;
		for(int i = 0; i < fatherLevel.size(); i++)
		{
			Vertex fatherVertex = fatherLevel.get(i);
			VertexContent infoNodo = fatherVertex.getInfoNodo();
			String sourceProcessor = infoNodo.getSource();
			String targetProcessor = infoNodo.getTarget();
			Vertex vertex = null;
			switch(dependentVerticesProbability(levelNumber))
			{
				default:
				case 0: // Does not generate dependent vertex. 
					break;

				case 1: // Generate only one dependent vertex (randomly from source or target processor)
					Randomico rand = new Randomico();
					if(rand.nextBoolean())
						vertex = generateCDCM_Vertice(fatherVertex, sourceProcessor, computationTimeRandGauss, communicationVolumeRandGauss);
					else
						vertex = generateCDCM_Vertice(fatherVertex, targetProcessor, computationTimeRandGauss, communicationVolumeRandGauss);
					level.add(vertex);
					break;

				case 2: // Generate two dependent vertices (from source and target processors)
					vertex = generateCDCM_Vertice(fatherVertex, sourceProcessor, computationTimeRandGauss, communicationVolumeRandGauss);
					level.add(vertex);
					vertex = generateCDCM_Vertice(fatherVertex, targetProcessor, computationTimeRandGauss, communicationVolumeRandGauss);
					level.add(vertex);
					break;
			}
		}
		return level;
	}
	private Vertex generateCDCM_Vertice(Vertex fatherVertex, String sourceProcessor, RandRangeGauss computationTimeRandGauss, RandRangeGauss communicationVolumeRandGauss)
	{
		long computationTime = (long)computationTimeRandGauss.next();
		long communicationVolume = (long)communicationVolumeRandGauss.next();
		Randomico rand = new Randomico();
		int sourceIndex = processors.indexOf(sourceProcessor);
		int targetIndex = rand.randomNumberExcept(processors.size(), sourceIndex);
		String newTargetProcessor = processors.get(targetIndex);
		Vertex vertex = new Vertex(sourceProcessor, newTargetProcessor, computationTime, communicationVolume);
		fatherVertex.addDependentVertex(vertex);
		vertex.addFatherVertex(fatherVertex);
		
		return vertex;
	}
	private Level createCDCGLevel_0(int numberOfVertices)
	{
		ArrayList<Integer> processadoresNaoUsados = new ArrayList<Integer>();
		for(int k = 0; k < processors.size(); k++)
			processadoresNaoUsados.add(k);
		Randomico rand = new Randomico();
		Level level_0 = new Level();
		for(int i = 0; i < numberOfVertices && processadoresNaoUsados.size() > 0; i++)
		{
			int randNumberSourceProc = rand.randomNumber(processadoresNaoUsados.size());
			int sourceProcessor = processadoresNaoUsados.get(randNumberSourceProc);
			String sourceProcessorName = processors.get(sourceProcessor);
			int randNumberTargetProc = rand.randomNumberExcept(processors.size(), sourceProcessor);
			String targetProcessorName = processors.get(randNumberTargetProc);
			
			processadoresNaoUsados.remove(randNumberSourceProc);
			long computationTime = (long)computationTimeRandGauss.next();
			long communicationVolume = (long)communicationVolumeRandGauss.next();
			Vertex vertex = new Vertex(sourceProcessorName, targetProcessorName, computationTime, communicationVolume);
			startVertex.addDependentVertex(vertex);
			vertex.addFatherVertex(startVertex);
			level_0.add(vertex);
		}
		return level_0;
	}
	private void createProcessors()
	{
		for(int i = 0; i < numberOfProcessors; i++)
			processors.add("P" + i);
	}
	private void createBasicCDCG()
	{
		int numberOfVertices = (int)parallelCommunicationRandGauss.next();
		Level level_0 = createCDCGLevel_0(numberOfVertices);
		levels.add(level_0);
		for(int i = 1; i <= numberOfGraphLevels; i++)
		{
			numberOfVertices = (int)parallelCommunicationRandGauss.next();
			Level level = createCDCGLevels(i, numberOfVertices);
			if(level.size() <= 0)
				break;
			levels.add(level);
		}	
	}
	private void generateCDCGDependences(Vertex vertex, int vertexLevel)
	{
		ArrayList<Vertex> candidateVertices = new ArrayList<Vertex>();
		String sourceVertex = vertex.getInfoNodo().getSource();
		int endVertexLevel = levels.size();
		for(int index = 0; index < endVertex.getFatherVertices().size(); index++)
		{
			Vertex v = endVertex.getFatherVertex(index);
			int actualLevel = endVertexLevel - 1;
			do
			{
				if(v == vertex)
					break;
				if(vertex.superFatherVertex(v))
					break;
				if(actualLevel < vertexLevel)
				{
					String source = v.getInfoNodo().getSource();
					String target = v.getInfoNodo().getTarget();
					
					if(sourceVertex.equals(source) || sourceVertex.equals(target))
					{
						if(!candidateVertices.contains(v))
							candidateVertices.add(v);
						break;
					}
				}
				v = v.getFatherVertex(0);  // The remaining vertices have only one father vertex (the index 0)
				actualLevel--;
			}
			while(v != startVertex);
		}
		for(Vertex v : candidateVertices)
		{
			if(generateAccordingToPercentProbability(100 - dependenceDegree))
			{
				v.addDependentVertex(vertex);
				vertex.addFatherVertex(v);
// Depuração --> TIRAR				
				System.out.print("\n" + v.getInfoNodo().getStringID() + " -> "+ vertex.getInfoNodo().getStringID());
// Depuração --> TIRAR				
			}
		}
	}
	private void generateCDCGDependences()
	{
// Depuração --> TIRAR				
		System.out.println(startVertex.getDepurString());
		for(Level level: levels)
		{
			for(Vertex vertex: level)
				System.out.println(vertex.getDepurString());
		}
		System.out.println(endVertex.getDepurString());
// Depuração --> TIRAR				

		for(int levelIndex = 2; levelIndex < levels.size(); levelIndex++)
		{
			Level level = levels.get(levelIndex);
			for(Vertex vertex: level)
				generateCDCGDependences(vertex, levelIndex);
		}
	}
	private void writeOutputFile()
	{
		File file = new File(fileName);
		try
		{
			OutputStream fileOut = new FileOutputStream(file);
			DataOutputStream ds = new DataOutputStream(fileOut);
			ds.writeBytes(toString());
			ds.close();
		}
		catch(Exception e)
		{			
		}
	}
	public void createCDCG(SyntheticApplicationGeneratorGui gui)
	{
		setConstraints(gui);
		createProcessors();
		createBasicCDCG();
		generateCDCGDependences();
		writeOutputFile();
	}
	public void setConstraints(SyntheticApplicationGeneratorGui gui)
	{
		setNumberofProcessors(gui.getNumberOfProcessors());
		setNumberOfGraphLevels(gui.getNumberOfGraphLevels());
		setDependenceDegree(gui.getDependeceDegree());
		setRangeProbabilityOfEndVertexMeeting(gui.getMinimumProbabilityOfEndVertexMeeting(), gui.getMaximumProbabilityOfEndVertexMeeting());
		setParallelCommunicationConstraints(gui.getParallelCommunicationMean(), gui.getParallelCommunicationStandardDeviation(), gui.getParallelCommunicationMinimum(), gui.getParallelCommunicationMaximum());
		setComputationTimeConstraints(gui.getComputationTimeMean(), gui.getComputationTimeStandardDeviation(), gui.getComputationTimeMinimum(), gui.getComputationTimeMaximum());
		setCommunicationVolumeConstraints(gui.getCommunicationVolumeMean(), gui.getCommunicationVolumeStandardDeviation(), gui.getCommunicationVolumeMean(), gui.getCommunicationVolumeMaximum());
	}
	public String toString()
	{
		String str = "";
		double numberOfProcessors = processors.size();
		int numberOfLines = (int)Math.sqrt(numberOfProcessors);
		if(Math.sqrt(numberOfProcessors) > numberOfLines)
			numberOfLines++;
		int numberOfColumns = (int)(numberOfProcessors / numberOfLines);
		if(numberOfProcessors / numberOfLines > numberOfColumns)
			numberOfColumns++;

		str = str + "#_NoC_Size (lines columns)";
		str = str + "\n " + numberOfLines + " " + numberOfColumns;
		str = str + "\n";

		str = str + "\n#_CDCG_Graphic (list of: IDCore x y)";
		int centerX = 300;
		int posY = 50;
		str = str + "\n " + startVertex.getInfoNodo().getStringID() + " " + centerX + " " + posY;
		posY = posY + 100;
		for(Level level: levels)
		{
			int posX = 50;
			for(Vertex vertex: level)
			{
				str = str + "\n " + vertex.getInfoNodo().getStringID() + " " + posX + " " + posY;
				posX = posX + 100;
			}
			posY = posY + 100;
		}
		str = str + "\n " + endVertex.getInfoNodo().getStringID() + " " + centerX + " " + posY;

		str = str + "\n";
		str = str + "\n#_CDCG_Vertices (list of: vertices --> IDCore sourceCore - targetCore phits : computation)";
		for(Level level: levels)
		{
			for(Vertex vertex: level)
				str = str + "\n" + vertex.getInfoNodo();
		}

		str = str + "\n";
		str = str + "\n#_CDCG_Edges (list of: dependent vertices)";
		str = str + "\n" + startVertex;
		str = str + "\n" + endVertex;
		for(Level level: levels)
		{
			for(Vertex vertex: level)
				str = str + "\n" + vertex;
		}
		str = str + "\n";
		return str;
	}
}