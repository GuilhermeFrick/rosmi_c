package cafes.model.CDM;

import java.io.*;

class CDM_Buffer implements Serializable
{
	private static final long serialVersionUID = 8152779354648235675L;
	private long totalPhits;		// Totalização de phits do link
	private double energiaBuffer;	// Consumo de energia de um phit nos buffer - Parâmetros da Noc 
	private double bufferSize;		// Numero de phits do buffer - Parâmetros da Noc
	private CDM_ListaVerticeNoC inicio, fim;

	public CDM_Buffer(double energiaBuffer, double bufferSize)
	{
		totalPhits = 0;
		inicio = null;
		fim = null;
		this.energiaBuffer = energiaBuffer;
		this.bufferSize = bufferSize;
	}
	public CDM_Buffer(CDM_Buffer b)
	{
		totalPhits = b.totalPhits;
		inicio = b.inicio;
		fim = b.fim;
		energiaBuffer = b.energiaBuffer;
		bufferSize = b.bufferSize;
	}
	public void insereVertice(CDM_Vertice vertice, long cicloInicial, long cicloFinal)
	{
		CDM_ListaVerticeNoC listaVertice = new CDM_ListaVerticeNoC(vertice, cicloInicial, cicloFinal);
		if(inicio==null)
			inicio = listaVertice;
		else
			fim.setProx(listaVertice);
		fim = listaVertice;
	}
	public void somaPhits(long phits)
	{
		totalPhits = totalPhits + phits; 
	}
	public void zeraTrafego()
	{
		inicio = null;
		fim = null;
		totalPhits = 0;
	}
	public double energiaBuffer()
	{
		return totalPhits * energiaBuffer * bufferSize;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		CDM_ListaVerticeNoC p = inicio;
		
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
	}
}

