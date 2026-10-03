package cafes.model.CWM;

/*
 * Autor: 
 * 		César Marcon
 * Revisor:
 * 		Edson Moreno
 * Objetivo:
 * 		Controlar a quantidade de operacoes realizadas em cada uma das portas
 * 			de cada um dos roteadores. Cada instancia representa um porta do
 * 			roteador.
 */

import java.io.*;

class CWM_Link implements Serializable
{
	private static final long serialVersionUID = 4956969960516991965L;
	private long totalPhits;	// Totalização de phits do link
	private double energiaPhit;	// Energia que consome um link entre roteadores ou entre roteador e core local

	/*
	 * Objetivo:
	 * 		Criar uma porta inicializando com o valor a ser utilizado para cada
	 * 			operacao entre roteadores ou roteador IP.
	 * Parametros:
	 * 		energiaPhit -> valor base a ser utilizado para calculo da energia
	 * 			consumida.
	 */
	public CWM_Link(double energiaPhit)
	{
		totalPhits = 0;
		this.energiaPhit = energiaPhit;
	}

	/*
	 * Objetivo:
	 * 		clona um Link jah existente
	 * Parametros:
	 * 		a -> link a ser clonado
	 */
	public CWM_Link(CWM_Link a)
	{
		totalPhits = a.totalPhits;
		energiaPhit = a.energiaPhit;
	}
	
	/*
	 * Objetivo:
	 * 		Incrementar e armazenar a quantidade de phits que passaram 
	 * 			pelo link.
	 * Parametros:
	 * 		phits -> valor a incrementado ao total de phits deste link  
	 */
	public void somaPhits(long phits)
	{
		totalPhits = totalPhits + phits; 
	}
	
	/*
	 * Objetivo:
	 * 		Resetar a quantidade de phits que passaram pelo link.
	 * Parametros:
	 * 		Nao ha...
	 */
	public void zeraTrafego()
	{
		totalPhits = 0;
	}
	
	/*
	 * Objetivo:
	 * 		Calcular a quantidade de energia consumida no link multiplicando 
	 * 			a quantidade de transacoes realizadas neste link com a energia
	 * 			necessaria para transacionar cada phit.
	 * Parametros:
	 * 		Nao ha...
	 */
	public double energiaLink()
	{
		return totalPhits * energiaPhit;
	}

	/*
	 * Objetivo:
	 * 		Apresenta o valor da energia base de cada transacao adotada 
	 * 			para este link e a energia total consumida neste link. 
	 * 			Adicionalmente retorna a energia consumida neste link.
	 * Parametros:
	 * 		Nao ha...
	 */
	public double exibe()
	{
		System.out.println("\t\t\tEnergia dinamica:" + energiaLink()/1000 + "uJ");
		return energiaLink();
	}
}

