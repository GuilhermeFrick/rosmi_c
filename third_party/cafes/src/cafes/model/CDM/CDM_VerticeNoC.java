package cafes.model.CDM;

import cafes.common.*;
import cafes.NoC.*;

class CDM_VerticeNoC implements java.io.Serializable
{
	private static final long serialVersionUID = 1804452938918902577L;
	private double energiaControleRoteador;	 		 	// Energia consumida pelo controle do roteador
	private double energiaControle;						// Consumo de energia de um phit no controle - Par�metros da Noc
	private String core;								// Core que est� conectado ao roteador da NoC
	private CDM_Buffer buffer[] = new CDM_Buffer[8];	// Os buffers somente s�o usados apenas nas entradas da NoC
	protected CDM_Link linkEntrada[] = new CDM_Link[8];	// Cinco entradas de comunica��o, mais uma sa�da do link local
	
	public CDM_VerticeNoC(String core, LinhaColunaAltura lc, CDM_NoC noc)
	{
		this.core = core;
		energiaControleRoteador = 0;

		energiaControle = noc.getEnergiaControle();
		double energiaBuffer = noc.getEnergiaBuffer();
		double bufferSize = noc.getBufferSize();
		double energiaLinkVertical = noc.getEnergiaLinkVertical();
		double energiaLinkHorizontal = noc.getEnergiaLinkHorizontal();
		double energiaLinkLongitudinal = noc.getEnergiaLinkLongitudinal();
		double energiaLocalLink = noc.getEnergiaLocalLink(); 

		for(int i=0; i<buffer.length; i++)
			buffer[i] = new CDM_Buffer(energiaBuffer, bufferSize);
		
		if(lc.getAltura()==0) // Ponta superior
			linkEntrada[Router.SUPERIOR] = new CDM_Link(energiaLinkLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.SUPERIOR] = new CDM_Link(energiaLinkLongitudinal);
		if(lc.getAltura()>=noc.getNumeroAltura()) // Ponta inferior
			linkEntrada[Router.INFERIOR] = new CDM_Link(energiaLinkLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.INFERIOR] = new CDM_Link(energiaLinkLongitudinal);
		
		
		if(lc.getLinha()==0) // Ponta superior
			linkEntrada[Router.NORTE] = new CDM_Link(energiaLinkVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.NORTE] = new CDM_Link(energiaLinkVertical);
		if(lc.getLinha()>=noc.getNumeroLinhas()) // Ponta inferior
			linkEntrada[Router.SUL] = new CDM_Link(energiaLinkVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.SUL] = new CDM_Link(energiaLinkVertical);
		if(lc.getColuna()>=noc.getNumeroColunas()) // Ponta direita
			linkEntrada[Router.LESTE] = new CDM_Link(energiaLinkHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.LESTE] = new CDM_Link(energiaLinkHorizontal);
		if(lc.getColuna()==0) // Ponta esquerda
			linkEntrada[Router.OESTE] = new CDM_Link(energiaLinkHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.OESTE] = new CDM_Link(energiaLinkHorizontal);
		linkEntrada[Router.LOCAL_IN] = new CDM_Link(energiaLocalLink);
		linkEntrada[Router.LOCAL_OUT] = new CDM_Link(energiaLocalLink);
	}
	public CDM_VerticeNoC(CDM_VerticeNoC p)
	{
		core = new String(p.core);
		energiaControleRoteador = p.energiaControleRoteador;
		energiaControle = p.energiaControle;
		for(int i=0; i<buffer.length; i++)
			buffer[i] = new CDM_Buffer(p.buffer[i]);
		for(int i=0; i<linkEntrada.length; i++)
			linkEntrada[i] = new CDM_Link(p.linkEntrada[i]);
	}
	public String getCoreName()
	{
		return core;
	}
	public void computaRoteador(CDM_Vertice p, long cicloInicial, long cicloFinal, int posicao, long phits)
	{
		energiaControleRoteador = energiaControleRoteador + phits * energiaControle;
		buffer[posicao].somaPhits(phits);
		buffer[posicao].insereVertice(p, cicloInicial, cicloFinal);
	}
	public long computaLink(CDM_Grafo g, CDM_Vertice p, long cicloInicial, long ciclosLink, int posicao, long phits, boolean comContencao)
	{
		long ciclosPacote = (int)(ciclosLink * (phits-1)); 

		linkEntrada[posicao].somaPhits(phits);
		return linkEntrada[posicao].insereVertice(g, p, cicloInicial, ciclosPacote, ciclosLink, comContencao);
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
// DEPURA��O
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibeCore()
	{
		System.out.print("\t(core: " + core + ")");
	}
	public void exibe()
	{
		boolean primeiraVez;
		
		exibeCore();
		System.out.print(" (Energia do Controle: " + energiaControleRoteador + ")");
		String direcaoLinkEntrada[] = new String[linkEntrada.length];
		direcaoLinkEntrada[Router.NORTE] = "NORTE";
		direcaoLinkEntrada[Router.SUL] = "SUL";
		direcaoLinkEntrada[Router.LESTE] = "LESTE";
		direcaoLinkEntrada[Router.OESTE] = "OESTE";
		direcaoLinkEntrada[Router.SUPERIOR] = "SUPERIOR";
		direcaoLinkEntrada[Router.INFERIOR] = "INFERIOR";
		direcaoLinkEntrada[Router.LOCAL_IN] = "LOCAL_IN";
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
			System.out.print("\n\t\t\t" + direcaoLinkEntrada[k] + ": " + linkEntrada[k].energiaLink());
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
			System.out.print("\n\t\t\t" + direcaoBuffers[k] + ": " + buffer[k].energiaBuffer());
			buffer[k].exibe();
		}
		if(linkEntrada[Router.LOCAL_OUT].energiaLink()>0)
		{
			direcaoLinkEntrada[Router.LOCAL_OUT] = "LOCAL_OUT";
			System.out.print("\n\t\tLink de Sa�da");
			System.out.print("\n\t\t\t" + direcaoLinkEntrada[Router.LOCAL_OUT] + ": " + linkEntrada[Router.LOCAL_OUT].energiaLink());
			linkEntrada[Router.LOCAL_OUT].exibe();
		}
	}
}