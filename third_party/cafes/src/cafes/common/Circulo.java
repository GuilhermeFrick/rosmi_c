package cafes.common;

import java.awt.*;
import java.io.*;

/**
 * Representa um circulo desenhado na tela.
 * Pode ser utilizado na exibição de um grafo
 *
 * @author Marcos Sartori
 */
public abstract class Circulo implements Serializable
{
	private static final long serialVersionUID = 1516163192550227530L;
	protected static int raio = 30;
	protected int x, y;

	/*
	 * Objetivo:
	 *   Inicializar o vertice
	 * Parametros:
	 *    x -> posicao x a ser apresentado o circulo na tela
	 *    y -> posicao y a ser apresentado o circulo na tela
	 */
	public Circulo(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public Circulo()
	{
		this(raio, raio);
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	/**
	 * Retorna o ponto topo a esquerda de X, centro do circulo
	 * @param x Valor de x central do circulo
	 */
	public static int xCentroParaTopoEsquerda(int x)
	{
		return (int) Math.round(x - (raio * Math.sin(Math.PI / 4) + 8));
	}

	/**
	 * 	Retorna o ponto topo a esquerda de Y, centro do circulo
	 * @param y Valor de y central do circulo
	 */
	public static int yCentroParaTopoEsquerda(int y)
	{
		return (int) Math.round(y - (raio * Math.cos(Math.PI / 4) + 8));
	}

	public static int getRaio()
	{
		return raio;
	}

	public void setRaio(int raio)
	{
		Circulo.raio = raio;
	}
// Método que verifica se a coordenada faz parte de um nodo

	public boolean acessouCirculo(int xp, int yp)
	{
		if(Draw.distancia(xp, yp, x, y) <= raio)
			return true;
		return false;
	}

	public boolean xyBelongsToLine(int xp, int yp, int xDest, int yDest)
	{
		double tolerancia = 3.0;

		if(acessouCirculo(xp, yp))
			return false;  // Se está dentro do círculo que define o vértice origem não considera fazer parte da reta
		if(Draw.distancia(xp, yp, xDest, yDest) <= raio)
			return false;  // Se está dentro do círculo que define o vértice destino não considera fazer parte da reta
		if((xp > (x + tolerancia) && xp > (xDest + tolerancia)) || (xp < (x - tolerancia) && xp < (xDest - tolerancia)))
			return false;  // Somente vale o segmento de reta entre vértices
		if((yp > (y + tolerancia) && yp > (yDest + tolerancia)) || (yp < (y - tolerancia) && yp < (yDest - tolerancia)))
			return false;  // Somente vale o segmento de reta entre vértices
		if(Math.abs(x - xDest) > (tolerancia * 2))
		{
			double aux1, aux2, yEncontradoMais, yEncontradoMenos;
			double a = (double) (y - yDest) / (x - xDest);
			double b = y - a * x;

			aux1 = a * (xp + tolerancia) + b;
			aux2 = a * (xp - tolerancia) + b;
			yEncontradoMais = Math.max(aux1, aux2) + tolerancia;
			yEncontradoMenos = Math.min(aux1, aux2) - tolerancia;
			if(yp > yEncontradoMais || yp < yEncontradoMenos)
				return false;
		}
		if((yp > yDest && yp > y) || (yp < yDest && yp < y))
			return false;
		return true;
	}

	/**
	 * Desenha o circulo
	 *
	 * @param g graphico onde o circulo sera desenhado.
	 * @param paraCor Diferencia se o circulo estah selecionado ou nao.
	 * Se o valor for true o circulo nao esta selecionado.
	 */
	public void desenhaCirculo(Graphics g, boolean paraCor)
	{
		int xTE = xCentroParaTopoEsquerda(x);
		int yTE = yCentroParaTopoEsquerda(y);

		if(!paraCor)
			g.setColor(new Color(120, 0, 0));
		else
			g.setColor(new Color(240, 0, 0));
		g.fillArc(xTE, yTE, raio * 2 + 1, raio * 2 + 1, 0, 360);
		if(!paraCor)
			g.setColor(new Color(255, 255, 255));
		else
			g.setColor(new Color(0, 0, 0));
		g.drawArc(xTE, yTE, raio * 2, raio * 2, 0, 360);
		g.setColor(Color.WHITE);
		desenhaConteudo(g);
	}

	abstract protected void desenhaConteudo(Graphics g);

	public static void arrowBetweenCircles(Graphics g, Color cor, double xi, double yi, double xf, double yf)
	{
		double altura = 10.0, largura = 5.0, espessura = 1.0;

		arrowBetweenCircles(g, cor, altura, largura, espessura, xi, yi, xf, yf);
	}

	public static void arrowBetweenCircles(Graphics g, double xi, double yi, double xf, double yf)
	{
		double altura = 10.0, largura = 5.0, espessura = 1.0;

		arrowBetweenCircles(g, Color.black, altura, largura, espessura, xi, yi, xf, yf);
	}

	/**
	 * Desenha uma seta entre circulos que tenham ligacao.
	 *
	 * @param g grafico a ser desenhada seta
	 * @param cor Cor da seta
	 * @param xi posicao x inicial de partida da seta
	 * @param yi posicao y inicial de partida da seta
	 * @param xf posicao x alvo da seta
	 * @param yf posicao y alvo da seta
	 */
	public static void arrowBetweenCircles(Graphics g, Color cor, double altura, double largura, double espessura, double xi, double yi, double xf, double yf)
	{
		double l_xi = xi, l_yi = yi, l_xf = xf, l_yf = yf;

		if(yf == yi) // Paralela ao eixo dos x
		{
			if(xf == xi) // É um ponto
				return;
			if(xf > xi)
			{
				l_xf = xf - raio;
				l_xi = xi + raio;
			}
			else
			{
				l_xf = xf + raio;
				l_xi = xi - raio;
			}
		}
		else
		{
			if(xf == xi) // Paralela ao eixo dos x
			{
				if(yf > yi)
				{
					l_yf = yf - raio;
					l_yi = yi + raio;
				}
				else
				{
					l_yf = yf + raio;
					l_yi = yi - raio;
				}
			}
			else
			{
				double alfa = Math.atan((yf - yi) / (xf - xi));

				if(xf < xi && yf < yi)
				{
					l_xi = xi - raio * Math.cos(alfa);
					l_yi = yi - raio * Math.sin(alfa);
					l_xf = xf + (raio * Math.cos(alfa));
					l_yf = yf + (raio * Math.sin(alfa));
				}
				else
				{
					if(xf > xi && yf > yi)
					{
						l_xi = xi + raio * Math.cos(alfa);
						l_yi = yi + raio * Math.sin(alfa);
						l_xf = xf - (raio * Math.cos(alfa));
						l_yf = yf - (raio * Math.sin(alfa));
					}
					else
					{
						if(xf > xi && yf < yi)
						{
							l_xi = xi + raio * Math.cos(alfa);
							l_yi = yi + raio * Math.sin(alfa);
							l_xf = xf - (raio * Math.cos(alfa));
							l_yf = yf - (raio * Math.sin(alfa));
						}
						else
						{
							l_xi = xi - raio * Math.cos(alfa);
							l_yi = yi - raio * Math.sin(alfa);
							l_xf = xf + (raio * Math.cos(alfa));
							l_yf = yf + (raio * Math.sin(alfa));
						}
					}
				}
			}
		}
		Draw.arrow(g, cor, altura, largura, espessura, l_xi, l_yi, l_xf, l_yf);
	}

	public static void arrowAndLabelBetweenCircles(Graphics g, double xi, double yi, double xf, double yf, String labelStr)
	{
		double l_xi = xi, l_yi = yi, l_xf = xf, l_yf = yf;

		if(yf == yi) // Paralela ao eixo dos x
		{
			if(xf == xi) // É um ponto
				return;
			if(xf > xi)
			{
				l_xf = xf - raio;
				l_xi = xi + raio;
			}
			else
			{
				l_xf = xf + raio;
				l_xi = xi - raio;
			}
		}
		else
		{
			if(xf == xi) // Paralela ao eixo dos x
			{
				if(yf > yi)
				{
					l_yf = yf - raio;
					l_yi = yi + raio;
				}
				else
				{
					l_yf = yf + raio;
					l_yi = yi - raio;
				}
			}
			else
			{
				double alfa = Math.atan((yf - yi) / (xf - xi));

				if(xf < xi && yf < yi)
				{
					l_xi = xi - raio * Math.cos(alfa);
					l_yi = yi - raio * Math.sin(alfa);
					l_xf = xf + (raio * Math.cos(alfa));
					l_yf = yf + (raio * Math.sin(alfa));
				}
				else
				{
					if(xf > xi && yf > yi)
					{
						l_xi = xi + raio * Math.cos(alfa);
						l_yi = yi + raio * Math.sin(alfa);
						l_xf = xf - (raio * Math.cos(alfa));
						l_yf = yf - (raio * Math.sin(alfa));
					}
					else
					{
						if(xf > xi && yf < yi)
						{
							l_xi = xi + raio * Math.cos(alfa);
							l_yi = yi + raio * Math.sin(alfa);
							l_xf = xf - (raio * Math.cos(alfa));
							l_yf = yf - (raio * Math.sin(alfa));
						}
						else
						{
							l_xi = xi - raio * Math.cos(alfa);
							l_yi = yi - raio * Math.sin(alfa);
							l_xf = xf + (raio * Math.cos(alfa));
							l_yf = yf + (raio * Math.sin(alfa));
						}
					}
				}
			}
		}
		Draw.arrowAndLabel(g, l_xi, l_yi, l_xf, l_yf, raio, labelStr);
	}
}
