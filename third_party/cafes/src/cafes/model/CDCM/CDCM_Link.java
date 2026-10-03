package cafes.model.CDCM;

import java.io.*;

class CDCM_Link implements Serializable
{
	private static final long serialVersionUID = 4995700994359632165L;
	private long totalPhits;	// Totalização de phits do link
	private double energiaPhit;	// Energia que consome um link entre roteadores ou entre roteador e core local
	private CDCM_ListaVerticeNoC inicio, fim;

	public CDCM_Link(double energiaPhit)
	{
		this.totalPhits = 0;
		this.energiaPhit = energiaPhit;
		inicio = null;
		fim = null;
	}
	public CDCM_Link(CDCM_Link a)
	{
		totalPhits = a.totalPhits;
		energiaPhit = a.energiaPhit;
		inicio = a.inicio;
		fim = a.fim;
	}
	public void deletar(CDCM_ListaVerticeNoC verticeDeletar)
	{
		CDCM_ListaVerticeNoC anterior = inicio;
		CDCM_ListaVerticeNoC p = inicio;
		while(p!=null)
		{
			if(p==verticeDeletar)
			{
			if(p==inicio)
			{
				if(p==fim)
				{
				inicio = null;
				fim = null;
				return;
				}
				inicio = p.getProx();
			return;
			}
			if(p==fim)
			{
				anterior.setProx(null);
				fim = anterior;
				return;
			}
			anterior.setProx(p.getProx());
			return;
			}
			anterior = p;
			p = p.getProx();
		}
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
	public double energiaLink()
	{
		return totalPhits * energiaPhit;
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

