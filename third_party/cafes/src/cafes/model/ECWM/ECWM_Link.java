package cafes.model.ECWM;

import java.io.*;

class ECWM_Link implements Serializable
{
	private static final long serialVersionUID = 6156683063580927321L;
	private long totalPhits;					// Totalização de phits do link sem chaveamento
	private double energiaPhit;					// Energia que consome um link entre roteadores ou entre roteador e core local
	private long totalPhitsComChaveamento;		// Totalização de phits do link que estão chaveando
	private double energiaPhitComChaveamento;	// Energia que consome um phit com chaveamento de um link entre roteadores ou entre roteador e core local

	public ECWM_Link(double energiaPhit, double energiaPhitComChaveamento)
	{
		this.totalPhits = 0;
		this.totalPhitsComChaveamento = 0;
		this.energiaPhit = energiaPhit;
		this.energiaPhitComChaveamento = energiaPhitComChaveamento;
	}
	public ECWM_Link(ECWM_Link a)
	{
		this.totalPhits = a.totalPhits;
		this.totalPhitsComChaveamento = a.totalPhitsComChaveamento;
		this.energiaPhit = a.energiaPhit;
		this.energiaPhitComChaveamento = a.energiaPhitComChaveamento;
	}
	public void somaPhits(long phitsSemChaveamento, long phitsComChaveamento)
	{
		totalPhits = totalPhits + phitsSemChaveamento;
		totalPhitsComChaveamento = totalPhitsComChaveamento + phitsComChaveamento;  
	}
	public void zeraTrafego()
	{
		totalPhits = 0;
		totalPhitsComChaveamento = 0;
	}
	private double energiaChaveamentoLink()
	{
		return totalPhitsComChaveamento * energiaPhitComChaveamento;
	}
	private double energiaPhitsLink()
	{
		return totalPhits * energiaPhit;
	}
	public double energiaLink()
	{
		return energiaPhitsLink() + energiaChaveamentoLink();
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public double exibe()
	{
		System.out.println("\t\t\tEnergia dinamica devido bits:" + energiaPhitsLink()/1000 + "uJ");
		System.out.println("\t\t\tEnergia dinamica devido chaveamento:" + energiaChaveamentoLink()/1000 + "uJ");
		return energiaLink();
	}
}

