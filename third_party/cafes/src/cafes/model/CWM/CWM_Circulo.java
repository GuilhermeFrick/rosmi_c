package cafes.model.CWM;

/*
 * Autor: 
 * 		César Marcon
 * Revisor:
 * 		Edson Moreno
 * Objetivo: 
 * 		Armazenar informações a serem utilizadas no momento de desenhar
 * 			os vertices na tela			
 */
import java.awt.*;
import cafes.common.*;

public class CWM_Circulo extends Circulo
{
	private static final long serialVersionUID = -1905892866885123480L;
	private String inf;

	/*
	 * Objetivo:
	 * 		Inicializar o vertice
	 * Parametros:
	 * 		x -> posicao x a ser apresentado o circulo na tela
	 * 		y -> posicao y a ser apresentado o circulo na tela
	 * 		inf -> nome a ser apresentado no interior do circulo
	 */
	public CWM_Circulo(int x, int y, String inf)
	{
		super(x, y);
		this.inf = inf;
	}
	/*
	 * Objetivos:
	 * 		Define o valor de inf
	 * Parametros:
	 * 		Nao ha...
	 */
	public void setInf(String inf)
	{
		this.inf = inf;
	}
	/*
	 * Objetivos:
	 * 		Retornar o valor inf do circulo
	 * Parametros:
	 * 		Nao ha...
	 */
	public String getInf()
	{
		return inf;
	}
	@Override
	protected void desenhaConteudo(Graphics g)
	{
		g.setFont(new Font("Arial", Font.BOLD, 10));
		g.drawString(inf, x - (inf.length() * 3), y + 4);
	}
}
