package cafes.model.ECWM;

import cafes.common.*;
import cafes.NoC.*;

class ECWM_VerticeNoC implements java.io.Serializable
{
	private static final long serialVersionUID = 6851592292780246901L;
	private double energiaControleRoteador;	 				// Energia consumida pelo controle do roteador
	private double energiaControle;							// Consumo de energia de um phit no controle - Par�metros da Noc
	private double energiaControleRoteadorComChaveamento;	// Energia consumida pelo controle do roteador considerando apenas os phits que variam
	private double energiaControleComChaveamento;			// Consumo de energia de um phit no controle considerando apenas os phits que variam - Par�metros da Noc
	private double energiaBufferRoteador;	 				// Energia consumida pelos buffers do roteador
	private double energiaBuffer;							// Consumo de energia de um phit nos buffer - Par�metros da Noc 
	private double energiaBufferRoteadorComChaveamento;	 	// Energia consumida pelos buffers do roteador considerando apenas os phits que variam
	private double energiaBufferComChaveamento;				// Consumo de energia de um phit nos buffer considerando apenas os phits que variam - Par�metros da Noc 
	private double bufferSize;					// Numero de phits do buffer - Par�metros da Noc
	private String core;						// Core que est� conectado ao roteador da NoC
	protected ECWM_Link linkEntrada[] = new ECWM_Link[8];	// Cinco sa�das de comunica��o
	
	public ECWM_VerticeNoC(String core, LinhaColunaAltura lc, ECWM_NoC noc)
	{
		energiaControleRoteador = 0;
		energiaControle = noc.getEnergiaControle();
		energiaControleRoteadorComChaveamento = 0;
		energiaControleComChaveamento = noc.getEnergiaControleComChaveamento();
		energiaBufferRoteador = 0;
		energiaBuffer = noc.getEnergiaBuffer();
		energiaBufferRoteadorComChaveamento = 0;
		energiaBufferComChaveamento = noc.getEnergiaBufferComChaveamento();
		this.core = core;
		double energiaLinkVertical = noc.getEnergiaLinkVertical();
		double energiaLinkHorizontal = noc.getEnergiaLinkHorizontal();
		double energiaLinkComChaveamentoVertical = noc.getEnergiaLinkComChaveamentoVertical();
		double energiaLinkComChaveamentoHorizontal = noc.getEnergiaLinkComChaveamentoHorizontal();
		double energiaLocalLink = noc.getEnergiaLocalLink(); 
		double energiaLocalLinkComChaveamento = noc.getEnergiaLocalLinkComChaveamento();
		
		double energiaLinkComChaveamentoLongitudinal = noc.getEnergiaLinkComChaveamentoLongitudinal();
		double energiaLinkLongitudinal = noc.getEnergiaLinkLongitudinal();
		
		
		if(lc.getColuna()>=noc.getNumeroColunas()) // Ponta direita
			linkEntrada[Router.LESTE] = new ECWM_Link(energiaLinkHorizontal*noc.getNumeroColunas(), energiaLinkComChaveamentoHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.LESTE] = new ECWM_Link(energiaLinkHorizontal, energiaLinkComChaveamentoHorizontal);
		if(lc.getLinha()==0) // Ponta superior
			linkEntrada[Router.NORTE] = new ECWM_Link(energiaLinkVertical*noc.getNumeroColunas(), energiaLinkComChaveamentoVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.NORTE] = new ECWM_Link(energiaLinkVertical, energiaLinkComChaveamentoVertical);
		if(lc.getColuna()==0) // Ponta esquerda
			linkEntrada[Router.OESTE] = new ECWM_Link(energiaLinkHorizontal*noc.getNumeroColunas(), energiaLinkComChaveamentoHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.OESTE] = new ECWM_Link(energiaLinkHorizontal, energiaLinkComChaveamentoHorizontal);
		if(lc.getLinha()>=noc.getNumeroLinhas()) // Ponta inferior
			linkEntrada[Router.SUL] = new ECWM_Link(energiaLinkVertical*noc.getNumeroColunas(), energiaLinkComChaveamentoVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.SUL] = new ECWM_Link(energiaLinkVertical, energiaLinkComChaveamentoVertical);
		
		if(lc.getAltura()==0) // Ponta superior
			linkEntrada[Router.SUPERIOR] = new ECWM_Link(energiaLinkLongitudinal*noc.getNumeroAltura(), energiaLinkComChaveamentoLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.SUPERIOR] = new ECWM_Link(energiaLinkLongitudinal, energiaLinkComChaveamentoLongitudinal);
		if(lc.getAltura()>=noc.getNumeroAltura()) // Ponta inferior
			linkEntrada[Router.INFERIOR] = new ECWM_Link(energiaLinkLongitudinal*noc.getNumeroColunas(), energiaLinkComChaveamentoLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.INFERIOR] = new ECWM_Link(energiaLinkLongitudinal, energiaLinkComChaveamentoLongitudinal);
		
		
		linkEntrada[Router.LOCAL_IN] = new ECWM_Link(energiaLocalLink, energiaLocalLinkComChaveamento);
		linkEntrada[Router.LOCAL_OUT] = new ECWM_Link(energiaLocalLink, energiaLocalLinkComChaveamento);
	}
	public ECWM_VerticeNoC(ECWM_VerticeNoC p)
	{
		energiaControleRoteador = p.energiaControleRoteador;
		energiaControle = p.energiaControle;
		energiaControleRoteadorComChaveamento = p.energiaControleRoteadorComChaveamento;
		energiaControleComChaveamento = p.energiaControleComChaveamento;
		energiaBufferRoteador = p.energiaBufferRoteador;
		energiaBuffer = p.energiaBuffer;
		energiaBufferRoteadorComChaveamento = p.energiaBufferRoteadorComChaveamento;
		energiaBufferComChaveamento = p.energiaBufferComChaveamento;
		bufferSize = p.bufferSize;
		core = new String(p.core);
		for(int i=0; i<linkEntrada.length; i++)
			linkEntrada[i] = new ECWM_Link(p.linkEntrada[i]);
	}
	private void zeraEnergiaRoteador()
	{
		energiaControleRoteador = 0;
		energiaControleRoteadorComChaveamento = 0;
		energiaBufferRoteador = 0;
		energiaBufferRoteadorComChaveamento = 0;
	}
	private double getEnergiaDoChaveamentoRoteador()
	{
		return energiaControleRoteadorComChaveamento + energiaBufferRoteadorComChaveamento;
	}
	private double getEnergiaDosPhitsRoteador()
	{
		return energiaControleRoteador + energiaBufferRoteador;
	}
	public double getEnergiaRoteador()
	{
		return getEnergiaDosPhitsRoteador() + getEnergiaDoChaveamentoRoteador();
	}
	public void computaEnergiaRoteador(long phitsSemChaveamento, long phitsComChaveamento)
	{
		computaEnergiaBufferRoteador(phitsSemChaveamento, phitsComChaveamento);
		computaEnergiaControleRoteador(phitsSemChaveamento, phitsComChaveamento);
	}
	public void computaEnergiaBufferRoteador(long phitsSemChaveamento, long phitsComChaveamento)
	{
		energiaBufferRoteador = energiaBufferRoteador + phitsSemChaveamento * energiaBuffer * bufferSize;
		energiaBufferRoteadorComChaveamento = energiaBufferRoteadorComChaveamento + phitsComChaveamento * energiaBufferComChaveamento * bufferSize;
	}
	public double getEnergiaBufferRoteador()
	{
		return energiaBufferRoteador + energiaBufferRoteadorComChaveamento;
	}
	public void computaEnergiaControleRoteador(long phitsSemChaveamento, long phitsComChaveamento)
	{
		energiaControleRoteador = energiaControleRoteador + phitsSemChaveamento * energiaControle;
		energiaControleRoteadorComChaveamento = energiaControleRoteadorComChaveamento + phitsComChaveamento * energiaControleComChaveamento;
	}
	public double getEnergiaControleRoteador()
	{
		return energiaControleRoteador + energiaControleRoteadorComChaveamento;
	}
	public String getCoreName()
	{
		return core;
	}
	public void computaEnergiaLink(int posicao, long phitsSemChaveamento, long phitsComChaveamento)
	{
		linkEntrada[posicao].somaPhits(phitsSemChaveamento, phitsComChaveamento);
	}
	public double energiaTotalRoteador()
	{
		double energia=0;

		for(int k=0; k<linkEntrada.length; k++)
			energia = energia + linkEntrada[k].energiaLink();

		return (energia + getEnergiaRoteador());
	}
	public void limpaEnergiaDinamica()
	{
		for(int k=0; k<linkEntrada.length; k++)
			linkEntrada[k].zeraTrafego();
		zeraEnergiaRoteador();
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURA��O
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibeCore()
	{
		System.out.println("\t\tcore :" + core);
	}
	public double exibe()
	{
		double energia;
		String direcaoLinkEntrada[] = new String[linkEntrada.length];

		direcaoLinkEntrada[Router.NORTE] = "NORTE";
		direcaoLinkEntrada[Router.SUL] = "SUL";
		direcaoLinkEntrada[Router.LESTE] = "LESTE";
		direcaoLinkEntrada[Router.OESTE] = "OESTE";
		direcaoLinkEntrada[Router.LOCAL_IN] = "LOCAL_IN";
		direcaoLinkEntrada[Router.LOCAL_OUT] = "LOCAL_OUT";
		direcaoLinkEntrada[Router.SUPERIOR] = "SUPERIOR";
		direcaoLinkEntrada[Router.INFERIOR] = "INFERIOR";

		energia = getEnergiaDoChaveamentoRoteador();
		energia = energia + getEnergiaDosPhitsRoteador();
		System.out.print("Energia Phits: " + getEnergiaDosPhitsRoteador()/1000 + "uJ");
		System.out.println(" - Energia Chaveamento: " + getEnergiaDoChaveamentoRoteador()/1000 + "uJ");
		exibeCore();
		for(int k=0; k<linkEntrada.length; k++)
		{
			System.out.println("\t\tLink de Entrada(" + direcaoLinkEntrada[k] + ")");
			energia = energia + linkEntrada[k].exibe();
		}
		return energia;
	}
}