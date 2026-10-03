package cafes.model.CDCM.app;

import java.util.*;

public class Vertex
{
	private ArrayList<Vertex> fatherVertices;
	private ArrayList<Vertex> dependentVertices;
	private VertexContent info;
	private static Vertex start;
	private static Vertex end;

	public Vertex(int id) // Used only to create START and END vertices
	{
		info = new VertexContent(id);
		if(id == VertexContent.START)
		{
			dependentVertices = new ArrayList<Vertex>();
			start = this;
		}
		else
		{
			fatherVertices = new ArrayList<Vertex>();
			end = this;
		}
	}
	public Vertex(String source, String target, long computationTime, long communicationVolume) // Used to create the remaining vertices
	{
		info = new VertexContent(source, target, computationTime, communicationVolume);
		fatherVertices = new ArrayList<Vertex>();
		dependentVertices = new ArrayList<Vertex>();
		dependentVertices.add(end);
		end.addFatherVertex(this);
	}
	public boolean addFatherVertex(Vertex father)
	{
		return fatherVertices.add(father);
	}
	public boolean superFatherVertex(Vertex testableFather)
	{
		if(fatherVertices == null)
			return false;
		Vertex father = this.getFatherVertex(0);
		do
		{
			if(father == testableFather)
				return true;
			father = father.getFatherVertex(0);
		}
		while(father != start && father != null);
		return false;
	}
	public boolean removeFatherVertex(Vertex father)
	{
		return fatherVertices.remove(father);
	}
	public Vertex getFatherVertex(int index)
	{
		return fatherVertices.get(index);
	}
	public ArrayList<Vertex> getFatherVertices()
	{
		return fatherVertices;
	}
	public boolean addDependentVertex(Vertex dependenteVertex)
	{
		if(dependenteVertex.getInfoNodo().getID() != VertexContent.END)
		{
			if(dependentVertices.contains(end))
			{
				dependentVertices.remove(end);
				end.removeFatherVertex(this);
			}
		}
		return dependentVertices.add(dependenteVertex);
	}
	public Vertex getDependentVertex(int index)
	{
		return dependentVertices.get(index);
	}
	public VertexContent getInfoNodo()
	{
		return info;
	}
	public String toString()
	{
		String str = " " + info.getStringID();

		if(dependentVertices == null)
			return str;
		for(Vertex vertex: dependentVertices)
			str = str + " " + vertex.getInfoNodo().getStringID();
		return str;
	}
	public String getDepurString()
	{
		String str = "\nID: " + info.getStringID();

		if(fatherVertices != null)
		{
			str = str + "\tFatherVertices:";
			for(Vertex vertex: fatherVertices)
				str = str + " " + vertex.getInfoNodo().getStringID();
		}
		if(dependentVertices == null)
			return str;
		str = str + "\n\tDependentVertices:";
		for(Vertex vertex: dependentVertices)
			str = str + " " + vertex.getInfoNodo().getStringID();
		return str;
	}
}
