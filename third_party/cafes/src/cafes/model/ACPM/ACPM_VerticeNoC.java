package cafes.model.ACPM;

import cafes.common.*;
import cafes.NoC.*;

class ACPM_VerticeNoC implements java.io.Serializable
{
	private static final long serialVersionUID = 7492399257414095703L;
	private double energiaControleRoteador;				// Energia consumida pelo controle do roteador
	private double energiaControle;						// Consumo de energia de um phit no controle - Parâmetros da Noc
	private String core;								// Core que está conectado ao roteador da NoC
	private ACPM_Buffer buffer[] = new ACPM_Buffer[8];	// Os buffers somente são usados apenas nas entradas da NoC
	protected ACPM_Link linkEntrada[] = new ACPM_Link[8];	// Cinco entradas de comunicação, mais uma saída do link local

	public ACPM_VerticeNoC(String core, LinhaColunaAltura lc, ACPM_NoC noc)
	{
		this.core = core;
		energiaControleRoteador = 0;

		energiaControle = noc.getEnergiaControle();
		double energiaBuffer = noc.getEnergiaBuffer();
		double bufferSize = noc.getBufferSize();
		double energiaLinkVertical = noc.getEnergiaLinkVertical();
		double energiaLinkHorizontal = noc.getEnergiaLinkHorizontal();
		double energiaLinkLongitudinal= noc.getEnergiaLinkLongitudinal();
		double energiaLocalLink = noc.getEnergiaLocalLink();

		for(int i=0; i<buffer.length; i++)
			buffer[i] = new ACPM_Buffer(energiaBuffer, bufferSize);
		
		if(lc.getAltura()==0) // Ponta superior
			linkEntrada[Router.SUPERIOR] = new ACPM_Link(energiaLinkLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.SUPERIOR] = new ACPM_Link(energiaLinkLongitudinal);
		if(lc.getAltura()>=noc.getNumeroAltura()) // Ponta inferior
			linkEntrada[Router.INFERIOR] = new ACPM_Link(energiaLinkLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.INFERIOR] = new ACPM_Link(energiaLinkLongitudinal);
		
		
		if(lc.getLinha()==0) // Ponta superior
			linkEntrada[Router.NORTE] = new ACPM_Link(energiaLinkVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.NORTE] = new ACPM_Link(energiaLinkVertical);
		if(lc.getLinha()>=noc.getNumeroLinhas()) // Ponta inferior
			linkEntrada[Router.SUL] = new ACPM_Link(energiaLinkVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.SUL] = new ACPM_Link(energiaLinkVertical);
		if(lc.getColuna()>=noc.getNumeroColunas()) // Ponta direita
			linkEntrada[Router.LESTE] = new ACPM_Link(energiaLinkHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.LESTE] = new ACPM_Link(energiaLinkHorizontal);
		if(lc.getColuna()==0) // Ponta esquerda
			linkEntrada[Router.OESTE] = new ACPM_Link(energiaLinkHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.OESTE] = new ACPM_Link(energiaLinkHorizontal);
		linkEntrada[Router.LOCAL_IN] = new ACPM_Link(energiaLocalLink);
		linkEntrada[Router.LOCAL_OUT] = new ACPM_Link(energiaLocalLink);
	}
	public ACPM_VerticeNoC(ACPM_VerticeNoC p)
	{
		core = new String(p.core);
		energiaControleRoteador = p.energiaControleRoteador;
		energiaControle = p.energiaControle;
		for(int i=0; i<buffer.length; i++)
			buffer[i] = new ACPM_Buffer(p.buffer[i]);
		for(int i=0; i<linkEntrada.length; i++)
			linkEntrada[i] = new ACPM_Link(p.linkEntrada[i]);
	}
	public String getCoreName()
	{
		return core;
	}
	public void computaRoteador(ACPM_Tag tag, ACPM_Vertice v, long cicloInicial, long cicloFinal, int posicao, long phits)
	{
		energiaControleRoteador = energiaControleRoteador + phits * energiaControle;
		buffer[posicao].somaPhits(phits);
		buffer[posicao].insereVertice(tag, v, cicloInicial, cicloFinal);
	}
	public long computaLink(ACPM_Tag tag, ACPM_Vertice v, long cicloInicial, long ciclosLink, int posicao, long phits)
	{
		long ciclosPacote = (int)(ciclosLink * phits);

		linkEntrada[posicao].somaPhits(phits);
		return linkEntrada[posicao].insereVertice(tag, v, cicloInicial, ciclosPacote);
	}
	public double getEnergiaControleRoteador()
	{
		return energiaControleRoteador;
	}
	public double getEnergiaBufferRoteador()
	{
		double energia = 0;

		for(int k=0; k<buffer.length; k++)
			energia = energia + buffer[k].energiaBuffer();
		return energia;
	}
	public double getEnergiaLinksEntradaRoteador()
	{
		double energia = 0;

		for(int k=0; k<linkEntrada.length; k++)
			energia = energia + linkEntrada[k].energiaLink();
		return energia;
	}
	public double energiaTotalRoteador()
	{
		double energia = 0;

		energia = energia + getEnergiaControleRoteador();
		energia = energia + getEnergiaBufferRoteador();
		energia = energia + getEnergiaLinksEntradaRoteador();

		return energia;
	}
	public void limpaEnergiaDinamica()
	{
		energiaControleRoteador = 0;
		for(int k=0; k<buffer.length; k++)
			buffer[k].zeraTrafego();
		for(int k=0; k<linkEntrada.length; k++)
			linkEntrada[k].zeraTrafego();
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibeCore()
	{
		System.out.print("\t(core: " + core + ")");
	}
	public void exibe()
	{
		boolean primeiraVez;

		exibeCore();
		System.out.print(" - Energia do Controle: " + energiaControleRoteador/1000 + " uJ");
		String direcaoLinkEntrada[] = new String[linkEntrada.length];
		direcaoLinkEntrada[Router.NORTE] = "NORTE";
		direcaoLinkEntrada[Router.SUL] = "SUL";
		direcaoLinkEntrada[Router.LESTE] = "LESTE";
		direcaoLinkEntrada[Router.OESTE] = "OESTE";
		direcaoLinkEntrada[Router.LOCAL_IN] = "LOCAL_IN";
		direcaoLinkEntrada[Router.SUPERIOR] = "SUPERIOR";
		direcaoLinkEntrada[Router.INFERIOR] = "INFERIOR";
		primeiraVez = true;
		for(int k=0; k<linkEntrada.length-1; k++)
		{
			if(linkEntrada[k].energiaLink()<=0)
				continue;
			if(primeiraVez)
			{
				primeiraVez = false;
				System.out.print("\n\t\tLink de Entrada");
			}
			System.out.print("\n\t\t\t" + direcaoLinkEntrada[k] + ": " + linkEntrada[k].energiaLink()/1000 + "uJ");
			linkEntrada[k].exibe();
		}
		String direcaoBuffers[] = new String[buffer.length];
		direcaoBuffers[Router.NORTE] = "NORTE";
		direcaoBuffers[Router.SUL] = "SUL";
		direcaoBuffers[Router.LESTE] = "LESTE";
		direcaoBuffers[Router.OESTE] = "OESTE";
		direcaoBuffers[Router.SUPERIOR] = "SUPERIOR";
		direcaoBuffers[Router.INFERIOR] = "INFERIOR";
		direcaoBuffers[Router.LOCAL_IN] = "LOCAL_IN";
		primeiraVez = true;
		for(int k=0; k<buffer.length; k++)
		{
			if(buffer[k].energiaBuffer()<=0)
				continue;
			if(primeiraVez)
			{
				primeiraVez = false;
				System.out.print("\n\t\tBuffer");
			}
			System.out.print("\n\t\t\t" + direcaoBuffers[k] + ": " + buffer[k].energiaBuffer()/1000 + "uJ");
			buffer[k].exibe();
		}
		if(linkEntrada[Router.LOCAL_OUT].energiaLink()>0)
		{
			direcaoLinkEntrada[Router.LOCAL_OUT] = "LOCAL_OUT";
			System.out.print("\n\t\tLink de Saída");
			System.out.print("\n\t\t\t" + direcaoLinkEntrada[Router.LOCAL_OUT] + ": " + linkEntrada[Router.LOCAL_OUT].energiaLink());
			linkEntrada[Router.LOCAL_OUT].exibe();
		}
	}
}