package cafes.model.ACPM;

import java.io.*;

class ACPM_Link implements Serializable
{
	private static final long serialVersionUID = -8806294820343473337L;
	private long totalPhits;	// Totalização de phits do link
	private double energiaPhit;	// Energia que consome um link entre roteadores ou entre roteador e core local
	private ACPM_ListaVerticeNoC inicio, fim;

	public ACPM_Link(double energiaPhit)
	{
		this.totalPhits = 0;
		this.energiaPhit = energiaPhit;
		inicio = null;
		fim = null;
	}
	public ACPM_Link(ACPM_Link a)
	{
		totalPhits = a.totalPhits;
		energiaPhit = a.energiaPhit;
		inicio = a.inicio;
		fim = a.fim;
	}
	public long cicloFinalMensagemConcorrente(ACPM_Tag tag, long cicloInicial, long cicloFinal)
	{
// A idéia aqui é postergar a mensagem antes de receber o recurso pelo tempo que mensagens com a mesma 
// tag estiverem com o recurso		
		long ciclosMensagem = cicloFinal - cicloInicial;
		ACPM_ListaVerticeNoC p = inicio;
		
		while(p!=null)
		{
			if(p.getTag()==tag)
			{
				if(cicloInicial<=p.getCicloFinal() && cicloFinal>=p.getCicloInicial())
				{
					if(cicloInicial>=p.getCicloInicial())
					cicloFinal = ciclosMensagem + p.getCicloFinal();
					else
					cicloFinal = (cicloFinal-p.getCicloInicial()) + p.getCicloFinal();
				}
			}
			p = p.getProx();
		}
		return cicloFinal;
	}
	public long insereVertice(ACPM_Tag tag, ACPM_Vertice vertice, long cicloInicial, long ciclosGastos)
	{
		long cicloFinal = cicloFinalMensagemConcorrente(tag, cicloInicial, cicloInicial + ciclosGastos);
		ACPM_ListaVerticeNoC listaVertice = new ACPM_ListaVerticeNoC(tag, vertice, cicloInicial, cicloFinal);
		if(inicio==null)
			inicio = listaVertice;
		else
			fim.setProx(listaVertice);
		fim = listaVertice;
		return listaVertice.getCicloFinal();
	}
	//elas tem que ser postergadas do tempo da mensagem não dependente com maior tempo.
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
		ACPM_ListaVerticeNoC p = inicio;
		
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
	}
}

