package cafes.model.ACPM;

import java.io.*;

class ACPM_Buffer implements Serializable
{
	private static final long serialVersionUID = -7730241004020747423L;
	private long totalPhits;		// Totalização de phits do link
	private double energiaBuffer;	// Consumo de energia de um phit nos buffer - Parâmetros da Noc 
	private double bufferSize;		// Numero de phits do buffer - Parâmetros da Noc
	private ACPM_ListaVerticeNoC inicio, fim;

	public ACPM_Buffer(double energiaBuffer, double bufferSize)
	{
		totalPhits = 0;
		inicio = null;
		fim = null;
		this.energiaBuffer = energiaBuffer;
		this.bufferSize = bufferSize;
	}
	public ACPM_Buffer(ACPM_Buffer b)
	{
		totalPhits = b.totalPhits;
		inicio = b.inicio;
		fim = b.fim;
		energiaBuffer = b.energiaBuffer;
		bufferSize = b.bufferSize;
	}
	public void insereVertice(ACPM_Tag tag, ACPM_Vertice vertice, long cicloInicial, long cicloFinal)
	{
		ACPM_ListaVerticeNoC listaVertice = new ACPM_ListaVerticeNoC(tag, vertice, cicloInicial, cicloFinal);
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
		ACPM_ListaVerticeNoC p = inicio;
		
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
	}
}

