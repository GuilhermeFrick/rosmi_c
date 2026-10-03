package cafes.model.CDCM;

import cafes.common.*;
import cafes.NoC.*;

public class CDCM_VerticeNoC implements java.io.Serializable
{
	private static final long serialVersionUID = 6392577948229592040L;
	private double energiaControleRoteador;	 		 	// Energia consumida pelo controle do roteador
	private double energiaControle;						// Consumo de energia de um phit no controle - Par�metros da Noc
	private String core;								// Core que est� conectado ao roteador da NoC
	private CDCM_Buffer buffer[] = new CDCM_Buffer[8];	// Os buffers somente s�o usados apenas nas entradas da NoC
	protected CDCM_Link linkEntrada[] = new CDCM_Link[8];	// Cinco entradas de comunica��o, mais uma sa�da do link local
	
	public CDCM_VerticeNoC(String core, LinhaColunaAltura lc, CDCM_NoC noc)
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

		for(int i = 0; i < buffer.length; i++)
			buffer[i] = new CDCM_Buffer(energiaBuffer, bufferSize);

		if(lc.getLinha() == 0) // Ponta superior
			linkEntrada[Router.NORTE] = new CDCM_Link(energiaLinkVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.NORTE] = new CDCM_Link(energiaLinkVertical);
		if(lc.getLinha() >= noc.getNumeroLinhas()) // Ponta inferior
			linkEntrada[Router.SUL] = new CDCM_Link(energiaLinkVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.SUL] = new CDCM_Link(energiaLinkVertical);
		if(lc.getColuna() >= noc.getNumeroColunas()) // Ponta direita
			linkEntrada[Router.LESTE] = new CDCM_Link(energiaLinkHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.LESTE] = new CDCM_Link(energiaLinkHorizontal);
		if(lc.getColuna() == 0) // Ponta esquerda
			linkEntrada[Router.OESTE] = new CDCM_Link(energiaLinkHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.OESTE] = new CDCM_Link(energiaLinkHorizontal);
		
		
		if(lc.getAltura() == 0) // Ponta SUPERIOR
			linkEntrada[Router.SUPERIOR] = new CDCM_Link(energiaLinkLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.SUPERIOR] = new CDCM_Link(energiaLinkLongitudinal);
		if(lc.getAltura() >= noc.getNumeroAltura()) // Ponta inferior
			linkEntrada[Router.INFERIOR] = new CDCM_Link(energiaLinkLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.INFERIOR] = new CDCM_Link(energiaLinkLongitudinal);
		
		
		linkEntrada[Router.LOCAL_IN] = new CDCM_Link(energiaLocalLink);
		linkEntrada[Router.LOCAL_OUT] = new CDCM_Link(energiaLocalLink);
	}
	public CDCM_VerticeNoC(CDCM_VerticeNoC p)
	{
		if(p == null)
			return;
		if(p.core == null)
			return;
		core = new String(p.core);
		energiaControleRoteador = p.energiaControleRoteador;
		energiaControle = p.energiaControle;
		for(int i = 0; i < buffer.length; i++)
			buffer[i] = new CDCM_Buffer(p.buffer[i]);
		for(int i = 0; i < linkEntrada.length; i++)
			linkEntrada[i] = new CDCM_Link(p.linkEntrada[i]);
	}
	public String getCoreName()
	{
		return core;
	}
	public long computaRoteador(CDCM_Vertice p, long cicloInicial, long ciclosRouting, int posicao, long phits)
	{
// According to Wormhole model, just the first phit waste ciclesRouting time to route the remaining phit follows the first routing
		long ciclosPacote = ciclosRouting + (phits - 1);

		energiaControleRoteador = energiaControleRoteador + phits * energiaControle;
		if(buffer[posicao] == null)
			throw new RuntimeException("Buffer de Entrada ID=" + p.getID() + " n�o alocado!");
		buffer[posicao].somaPhits(phits);
		return buffer[posicao].insereVertice(p, cicloInicial, ciclosPacote) + ciclosRouting;
	}
	public long computaLink(CDCM_Vertice p, long cicloInicial, long ciclosLink, int posicao, long phits)
	{
// Each phit takes a ciclosLink clock cycles to be transmitted through a link
		long ciclosPacote = ciclosLink * phits; 

		if(linkEntrada[posicao] == null)
			throw new RuntimeException("Link de Entrada ID=" + p.getID() + " n�o alocado!");
		linkEntrada[posicao].somaPhits(phits);
		return linkEntrada[posicao].insereVertice(p, cicloInicial, ciclosPacote) + ciclosLink;
	}
	public double getEnergiaControleRoteador()
	{
		return energiaControleRoteador;
	}
	public double getEnergiaBufferRoteador()
	{
		double energia = 0;
		
		for(int k = 0; k < buffer.length; k++)
		{
			if(buffer[k] != null)
				energia = energia + buffer[k].energiaBuffer();
		}
		return energia;
	}
	public double getEnergiaLinksEntradaRoteador()
	{
		double energia = 0;

		for(int k = 0; k < linkEntrada.length; k++)
		{
			if(linkEntrada[k] != null)
				energia = energia + linkEntrada[k].energiaLink();
		}
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
		for(int k = 0; k < buffer.length; k++)
		{
			if(buffer[k] != null)
				buffer[k].zeraTrafego();
		}
		for(int k = 0; k < linkEntrada.length; k++)
		{
			if(linkEntrada[k] != null)
				linkEntrada[k].zeraTrafego();
		}
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURA��O
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibeCore()
	{
		System.out.print("\t(core: " + core + ")");
	}
	public double exibe()
	{
		double energia = 0;
		boolean primeiraVez;
		
		exibeCore();
		energia = energia + energiaControleRoteador;
		System.out.print(" - Energia do Controle: " + energiaControleRoteador/1000 + "uJ");
		String direcaoLinkEntrada[] = new String[linkEntrada.length];
		direcaoLinkEntrada[Router.NORTE] = "NORTE";
		direcaoLinkEntrada[Router.SUL] = "SUL";
		direcaoLinkEntrada[Router.LESTE] = "LESTE";
		direcaoLinkEntrada[Router.OESTE] = "OESTE";
		direcaoLinkEntrada[Router.LOCAL_IN] = "LOCAL_IN";
		direcaoLinkEntrada[Router.INFERIOR] = "INFERIOR";
		direcaoLinkEntrada[Router.SUPERIOR] = "SUPERIOR";
		primeiraVez = true;
		for(int k = 0; k < linkEntrada.length-1; k++)
		{
			if(linkEntrada[k] == null)
				continue;
			if(linkEntrada[k].energiaLink() <= 0)
				continue;
			if(primeiraVez)
			{
				primeiraVez = false;
				System.out.print("\n\t\tLink de Entrada");
			}
			energia = energia + linkEntrada[k].energiaLink();
			System.out.print("\n\t\t\t" + direcaoLinkEntrada[k] + ": " + linkEntrada[k].energiaLink()/1000 + "uJ");
			linkEntrada[k].exibe();
		}
		String direcaoBuffers[] = new String[buffer.length];
		direcaoBuffers[Router.NORTE] = "NORTE";
		direcaoBuffers[Router.SUL] = "SUL";
		direcaoBuffers[Router.LESTE] = "LESTE";
		direcaoBuffers[Router.OESTE] = "OESTE";
		direcaoBuffers[Router.LOCAL_IN] = "LOCAL_IN";
		direcaoBuffers[Router.INFERIOR] = "INFERIOR";
		direcaoBuffers[Router.SUPERIOR] = "SUPERIOR";
		primeiraVez = true;
		for(int k=0; k<buffer.length; k++)
		{
			if(buffer[k] == null)
				continue;
			if(buffer[k].energiaBuffer()<=0)
				continue;
			if(primeiraVez)
			{
				primeiraVez = false;
				System.out.print("\n\t\tBuffer");
			}
			energia = energia + buffer[k].energiaBuffer();
			System.out.print("\n\t\t\t" + direcaoBuffers[k] + ": " + buffer[k].energiaBuffer()/1000 + "uJ");
			buffer[k].exibe();
		}
		if(linkEntrada[Router.LOCAL_OUT] != null && linkEntrada[Router.LOCAL_OUT].energiaLink() > 0)
		{
			energia = energia + linkEntrada[Router.LOCAL_OUT].energiaLink();
			direcaoLinkEntrada[Router.LOCAL_OUT] = "LOCAL_OUT";
			System.out.print("\n\t\tLink de Saida");
			System.out.print("\n\t\t\t" + direcaoLinkEntrada[Router.LOCAL_OUT] + ": " + linkEntrada[Router.LOCAL_OUT].energiaLink()/1000 + "uJ");
			linkEntrada[Router.LOCAL_OUT].exibe();
		}
		return energia;
	}
}