package cafes.model.CDCM;

import java.io.*;

class CDCM_Buffer implements Serializable
{
	private static final long serialVersionUID = 3857470148420119675L;
	private long totalPhits;		// Totalização de phits do link
	private double energiaBuffer;	// Consumo de energia de um phit nos buffer - Parâmetros da Noc 
	private double bufferSize;		// Numero de phits do buffer - Parâmetros da Noc
	private CDCM_ListaVerticeNoC inicio, fim;

	public CDCM_Buffer(double energiaBuffer, double bufferSize)
	{
		totalPhits = 0;
		inicio = null;
		fim = null;
		this.energiaBuffer = energiaBuffer;
		this.bufferSize = bufferSize;
	}
	public CDCM_Buffer(CDCM_Buffer b)
	{
		totalPhits = b.totalPhits;
		inicio = b.inicio;
		fim = b.fim;
		energiaBuffer = b.energiaBuffer;
		bufferSize = b.bufferSize;
	}
	public long insereVertice(CDCM_Vertice vertice, long cicloInicial, long ciclosGastos)
	{
//	  Retorna o ciclo final da mensagem no recurso
		long cicloFinal = cicloInicial + ciclosGastos - 1; // Subtrai um ciclo!

		CDCM_ListaVerticeNoC verticeNoC;

		if(inicio == null)
		{
			verticeNoC = new CDCM_ListaVerticeNoC(vertice, cicloInicial, cicloFinal);
			inicio = verticeNoC;
			fim = verticeNoC;
		}
		else
		{
			if(fim.getCicloFinal() >= cicloInicial)
			{
				cicloFinal = (cicloFinal - cicloInicial) + fim.getCicloFinal() + 1;
				cicloInicial = fim.getCicloFinal() + 1;
			}
			verticeNoC = new CDCM_ListaVerticeNoC(vertice, cicloInicial, cicloFinal);
			verticeNoC.setProx(null);
			fim.setProx(verticeNoC);
			fim = verticeNoC;
		}
		return cicloInicial;
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
		CDCM_ListaVerticeNoC p = inicio;
		
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
	}
}

