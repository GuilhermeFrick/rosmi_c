package cafes.model.CWM;

/*
 * Autor: 
 * 		C�sar Marcon
 * Revisor:
 * 		Edson Moreno
 * Objetivo:
 * 		Modelar a chave com seus links, associados seus valores base para 
 * 			c�lculo de energia total consumida em cada chave.  
 */

import cafes.common.*;
import cafes.NoC.*;

class CWM_VerticeNoC implements java.io.Serializable
{
	private static final long serialVersionUID = 1044370978925267995L;
	private double energiaControleRoteador;	 	// Energia consumida pelo controle do roteador
	private double energiaBufferRoteador;	 	// Energia consumida pelos buffers do roteador
	private double energiaBuffer;				// Consumo de energia de um phit nos buffer - Par�metros da Noc 
	private double energiaControle;				// Consumo de energia de um phit no controle - Par�metros da Noc
	private String core;						// Core que est� conectado ao roteador da NoC
	protected CWM_Link linkEntrada[] = new CWM_Link[8];	// Cinco sa�das de comunica��o
	
	/*
	 * Objetivo:
	 * 		Cria uma chave associando um core, a posicao linha e coluna 
	 * 			da chave na noc e a noc a qual ele estah associado.
	 * Parametros:
	 * 		core -> nome do vertice associado a chave
	 * 		lc -> posicao linha e coluna da chave
	 * 		noc -> noc a qual a chave esta associada
	 */
	public CWM_VerticeNoC(String core, LinhaColunaAltura lc, CWM_NoC noc)
	{
		this.core = core;
		energiaControleRoteador = 0;
		energiaBufferRoteador = 0;
		energiaBuffer = noc.getEnergiaBuffer();
		energiaControle = noc.getEnergiaControle();
		double energiaLinkVertical = noc.getEnergiaLinkVertical();
		double energiaLinkHorizontal = noc.getEnergiaLinkHorizontal();
		double energiaLinkLongitudinal = noc.getEnergiaLinkLongitudinal();
		double energiaLocalLink = noc.getEnergiaLocalLink(); 
		
		if(lc.getAltura()==0) // Ponta superior
			linkEntrada[Router.SUPERIOR] = new CWM_Link(energiaLinkLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.SUPERIOR] = new CWM_Link(energiaLinkLongitudinal);
		if(lc.getAltura()>=noc.getNumeroAltura()) // Ponta inferior
			linkEntrada[Router.INFERIOR] = new CWM_Link(energiaLinkLongitudinal*noc.getNumeroAltura());
		else
			linkEntrada[Router.INFERIOR] = new CWM_Link(energiaLinkLongitudinal);
		
		
		if(lc.getLinha()==0) // Ponta superior
			linkEntrada[Router.NORTE] = new CWM_Link(energiaLinkVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.NORTE] = new CWM_Link(energiaLinkVertical);
		if(lc.getLinha()>=noc.getNumeroLinhas()) // Ponta inferior
			linkEntrada[Router.SUL] = new CWM_Link(energiaLinkVertical*noc.getNumeroColunas());
		else
			linkEntrada[Router.SUL] = new CWM_Link(energiaLinkVertical);
		if(lc.getColuna()>=noc.getNumeroColunas()) // Ponta direita
			linkEntrada[Router.LESTE] = new CWM_Link(energiaLinkHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.LESTE] = new CWM_Link(energiaLinkHorizontal);
		if(lc.getColuna()==0) // Ponta esquerda
			linkEntrada[Router.OESTE] = new CWM_Link(energiaLinkHorizontal*noc.getNumeroColunas());
		else
			linkEntrada[Router.OESTE] = new CWM_Link(energiaLinkHorizontal);
		linkEntrada[Router.LOCAL_IN] = new CWM_Link(energiaLocalLink);
		linkEntrada[Router.LOCAL_OUT] = new CWM_Link(energiaLocalLink);
	}
	
	/*
	 * Objetivo:
	 * 		Clona uma chave existente, copiando a informacao do core associado,
	 * 			energia consumida e links.
	 * Parametros:
	 * 		p -> chave a ser clonado
	 */
	public CWM_VerticeNoC(CWM_VerticeNoC p)
	{
		core = new String(p.core);
		energiaControleRoteador = p.energiaControleRoteador;
		energiaBufferRoteador = p.energiaBufferRoteador;
		energiaBuffer = p.energiaBuffer;
		energiaControle = p.energiaControle;
		for(int i=0; i<linkEntrada.length; i++)
			linkEntrada[i] = new CWM_Link(p.linkEntrada[i]);
	}

	/*
	 * Objetivo:
	 * 		Reseta os valores de energia consumida na chave
	 * Parametros:
	 * 		Nao ha...
	 */
	private void zeraEnergiaRoteador()
	{
		energiaControleRoteador = 0;
		energiaBufferRoteador = 0;
	}
	
	/*
	 * Objetivo:
	 * 		Retorna o consumo total de energia ocorrido na chave
	 * Parametros:
	 * 		Nao ha...
	 */
	public double getEnergiaRoteador()
	{
		return energiaBufferRoteador + energiaControleRoteador;
	}
	
	/*
	 * Objetivos:
	 * 		Incrementa o consumo de energia na chave com relacao 
	 * 			a quantidade de phits que passam por esta chave
	 * Parametros:
	 * 		phits -> quantidade de phits a ser levada em consideracao para o
	 * 			calculo de consumo de energia  
	 */
	public void computaEnergiaRoteador(long phits)
	{
		computaEnergiaBufferRoteador(phits);
		computaEnergiaControleRoteador(phits);
	}
	
	/*
	 * Objetivo:
	 * 		Calcular o consumo de energia para os phits armazenados no buffer
	 * Parametros:
	 * 		phits -> quantidade de phits que estao passando neste momento pela
	 * 			chave
	 */
	public void computaEnergiaBufferRoteador(long phits)
	{
		energiaBufferRoteador = energiaBufferRoteador + phits * energiaBuffer;
	}

	/*
	 * Objetivo:
	 * 		Retorna a energia consumida pelo roteador no que se refere a 
	 * 			bufferizacao
	 * Parametros:
	 * 		Nao ha... 
	 */
	public double getEnergiaBufferRoteador()
	{
		return energiaBufferRoteador;
	}

	/*
	 * Objetivos:
	 * 		Calcular o consumo de energia para o roteamento de phits na chave
	 * Parametros:
	 * 		phits -> quantidade de phits que estao passando neste momento pela
	 * 			chave
	 */
	public void computaEnergiaControleRoteador(long phits)
	{
		energiaControleRoteador = energiaControleRoteador + phits * energiaControle;
	}

	/*
	 * Objetivos:
	 * 		Retorna a energia consumida pelo roteador para o roteamento dos
	 * 			phits
	 * Parametros:
	 * 		Nao ha... 
	 */
	public double getEnergiaControleRoteador()
	{
		return energiaControleRoteador;
	}

	/*
	 * Objetivos:
	 * 		Retorna o nome do core associado a esta chave
	 * Parametros:
	 * 		Nao ha...
	 */
	public String getCoreName()
	{
		return core;
	}

	/*
	 * Objetivos:
	 * 		Direciona a quantidade de phits a serem transmitidos para um
	 * 			determinado link para que seja calculado o consumo de energia
	 * 			necessario para esta transmissao.
	 * Parametros:
	 * 		posicao -> porta por onde serao transmitidos os phits 
	 * 			(norte/sul/leste/oeste/local)
	 * 		phits -> a quantidade de phits a ser transmitida 
	 */
	public void computaEnergiaLink(int posicao, long phits)
	{
		linkEntrada[posicao].somaPhits(phits);
	}

	/*
	 * Objetivo:
	 * 		Calcular a quantidade total de energia consumida no roteador, 
	 * 			representada pela energia consumida nas portas (links) +
	 * 			a energia necessaria para roteamento + bufferizacao
	 * Parametros:
	 * 		Nao ha...
	 */
	public double energiaTotalRoteador()
	{
		double energia=0;

		for(int k=0; k<linkEntrada.length; k++)
			energia = energia + linkEntrada[k].energiaLink();

		return (energia + getEnergiaRoteador());
	}

	/*
	 * Objetivos:
	 * 		Reseta a quantidade de energia consumida nas portas (links), no
	 * 			roteamento e na bufferizacao.
	 * Parametros:
	 * 		Nao ha...
	 */
	public void limpaEnergiaDinamica()
	{
		for(int k=0; k<linkEntrada.length; k++)
			linkEntrada[k].zeraTrafego();
		zeraEnergiaRoteador();
	}

	/*
	 * Objetivos:
	 * 		Exibir o nome do core associado a esta chave.
	 * Parametros:
	 * 		Nao ha...
	 */
	public void exibeCore()
	{
		System.out.println("\t\tcore :" + core);
	}
	
	/*
	 * Objetivos:
	 * 		Exibir o consumo de energia da chave e de cada porta/link da
	 * 			chave. Adicionalmente retorna a energia total consumida
	 * 			na chave (chave + portas) 
	 * Parametros:
	 * 		Nao ha...
	 */
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

		energia = getEnergiaRoteador();
		System.out.println(" - Energia: " + energia/1000 + "uJ");
		exibeCore();
		for(int k=0; k<linkEntrada.length; k++)
		{
			System.out.println("\t\tLink de Entrada(" + direcaoLinkEntrada[k] + ")");
			energia = energia + linkEntrada[k].exibe();
		}
		return energia;
	}
}