package cafes.common;

import java.awt.*;

public class Draw
{
	public static final int Up = 1;
	public static final int Inside = 2;
	public static final int Down = 3;
	public static final int Start = 4;
	public static final int Center = 5;
	public static final int End = 6;
	public static final int Up2 = 7;
	public static final int Down2 = 8;
	public static final int Up3 = 9;
	public static final int Down3 = 10;
	public static final int Up4 = 11;
	public static final int Down4 = 12;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// AUXILIAR
////////////////////////////////////////////////////////////////////////////////////////////////////////////////	//
	public static double hypotenusa(double cateto1, double cateto2)
	{
		return Math.sqrt(cateto1 * cateto1 + cateto2 * cateto2);
	}
	public static Font getFont(Graphics g,String name, int style, int height) {
	    int size = height;
	    Boolean up = null;
	    while (true) {
	        Font font = new Font(name, style, size);
	        int testHeight = g.getFontMetrics(font).getHeight();
	        if (testHeight < height && up != Boolean.FALSE) {
	            size++;
	            up = Boolean.TRUE;
	        } else if (testHeight > height && up != Boolean.TRUE) {
	            size--;
	            up = Boolean.FALSE;
	        } else {
	            return font;
	        }
	    }
	}
	public static int toMaxInt(double value)
	{
		double dif = value - (int) value;

		if(Math.abs(dif) > 0.0)
			value = (int) value + 1;
		return (int) value;
	}
	private static void circleLink(Graphics g, int x, int y, int width, int height) {
		g.setColor(Color.white);
		g.fillOval(x, y, width, height);
		g.setColor(Color.BLACK);
		g.drawOval(x, y, width, height);
	}
	public static void downLink(Graphics g, Color color, int x, int y, int radius) {		
		circleLink(g,x,y,radius,radius);
		g.setFont(getFont(g, "monospaced", Font.BOLD, radius));
		label(g, Color.black, (int)(x+(radius/1.5)), (int)(y+(radius/3)), ".");
	}
	public static void upperLink(Graphics g, Color color, int x, int y, int radius) {
		
		circleLink(g,x,y,radius,radius);
		g.setFont(getFont(g, "monospaced", Font.BOLD, radius));
		label(g, Color.black, (int)(x+(radius/1.5)), (int)(y+(radius/1.8)), "X");
	}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Lozenge
////////////////////////////////////////////////////////////////////////////////////////////////////////////////	//
	public static void lozenge(Graphics g, Color color, int x, int y, int width, int eigth)
	{
		int[] pX = new int[5];
		int[] pY = new int[5];

		pX[0] = pX[4] = toMaxInt(x - width / 2);
		pX[1] = x;
		pX[2] = toMaxInt(x + width / 2);
		pX[3] = x;
		pY[0] = pY[4] = y;
		pY[1] = toMaxInt(y - eigth / 2);
		pY[2] = y;
		pY[3] = toMaxInt(y + eigth / 2);

		g.setColor(color);
		g.fillPolygon(pX, pY, pX.length);
	}

	public static void label(Graphics g, Color color, int x, int y, String label)
	{
		g.setColor(color);
		g.drawString(label, toMaxInt(x - label.length() / 2 - 5), toMaxInt(y + 5));
	}

	public static void lozengeAndLabel(Graphics g, Color colorLozenge, Color colorlabel, int x, int y, int width, int eigth, String label)
	{
		lozenge(g, colorLozenge, x, y, width, eigth);
		Font salva = g.getFont();
		g.setFont(new Font("Arial", Font.BOLD, 14));
		label(g, colorlabel, x, y, label);
		g.setFont(salva);
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 ARROW_AND_LABEL
////////////////////////////////////////////////////////////////////////////////////////////////////////////////	//
	public static void arrowAndLabel(Graphics g, double xi, double yi, double xf, double yf, String labelStr)
	{
		arrowAndLabel(g, xi, yi, xf, yf, 0.0, labelStr);
	}

	public static void arrowAndLabel(Graphics g, double xi, double yi, double xf, double yf, double offset, String labelStr) {
		double altura = 10.0, espessura = 1.0, largura = 5.0;
		Color colorLabel = Color.blue, colorArrow = Color.black;
		int StartCenterEnd, UpInsideDown;

		StartCenterEnd = Draw.End;
		if (xi < xf) //setas da esquerda para a direita
		{
			if (yi < yf) //setas na diagonal pra baixo e direita
			{
				if ((xf - xi) < (yf - yi)) // setas mais vertical do que horizontal
				{
					UpInsideDown = Draw.Down;
				} else {
					UpInsideDown = Draw.Down2;  // setas mais horizontal do que vertical
				}
			} else //setas na diagonal pra cima e direita
			if ((xf - xi) > (yi - yf)) // setas mais horizontal do que vertical
			{
				UpInsideDown = Draw.Down3;
			} else {
				UpInsideDown = Draw.Down4;  // setas mais vertical do que horizontal
			}
		} else //setas da direita para a esquerda
		if (yi > yf) //setas na diagonal pra cima e direita
		{
			if ((xi - xf) < (yi - yf)) // setas mais vertical do que horizontal
			{
				UpInsideDown = Draw.Up;
			} else {
				UpInsideDown = Draw.Up2;  // setas mais horizontal do que vertical
			}
		} else //setas na diagonal pra baixo e direita
		if ((xi - xf) > (yf - yi)) // setas mais horizontal do que vertical
		{
			UpInsideDown = Draw.Up3;
		} else {
			UpInsideDown = Draw.Up4;  // setas mais vertical do que horizontal
		}
		arrowAndLabel(g, colorArrow, altura, largura, espessura, xi, yi, xf, yf, offset, labelStr, StartCenterEnd, UpInsideDown, colorLabel);
	}

	public static void arrowAndLabel(Graphics g, Color colorArrow, double altura, double largura, double espessura, double xi, double yi, double xf, double yf, double offset, String labelStr, int StartCenterEnd, int UpInsideDown, Color colorLabel) {
		arrow(g, colorArrow, altura, largura, espessura, xi, yi, xf, yf);
		label(g, labelStr, colorLabel, espessura, altura + offset, StartCenterEnd, UpInsideDown, xi, yi, xf, yf);
	}

  ////////////////
 //   ARROW	//
////////////////
	public static void arrow(Graphics g, double xi, double yi, double xf, double yf) {
		double altura = 10.0, espessura = 1.0, largura = 5.0;

		arrow(g, Color.black, altura, largura, espessura, xi, yi, xf, yf);
	}

	public static void arrow(Graphics g, Color color, double altura, double largura, double espessura, double xi, double yi, double xf, double yf) {
		double l_xf = xf, l_yf = yf;

		if (yf == yi) // Paralela ao eixo dos x
		{
			if (xf == xi) // � um ponto
			{
				return;
			}
			if (xf > xi) {
				l_xf = xf - altura;
			} else {
				l_xf = xf + altura;
			}
		} else {
			if (xf == xi) // Paralela ao eixo dos y
			{
				if (yf > yi) {
					l_yf = yf - altura;
				} else {
					l_yf = yf + altura;
				}
			} else {
				double alfa;

				if (xf < xi && yf < yi) {
					alfa = Math.atan((yf - yi) / (xf - xi));
					l_xf = xf + (altura * Math.cos(alfa));
					l_yf = yf + (altura * Math.sin(alfa));
				} else {
					if (xf > xi && yf > yi) {
						alfa = Math.atan((yf - yi) / (xf - xi));
						l_xf = xf - (altura * Math.cos(alfa));
						l_yf = yf - (altura * Math.sin(alfa));
					} else {
						if (xf > xi && yf < yi) {
							alfa = Math.atan((yf - yi) / (xf - xi));
							l_xf = xf - (altura * Math.cos(alfa));
							l_yf = yf - (altura * Math.sin(alfa));
						} else {
							alfa = Math.atan((yf - yi) / (xf - xi));
							l_xf = xf + (altura * Math.cos(alfa));
							l_yf = yf + (altura * Math.sin(alfa));
						}
					}
				}
			}
		}
		triangle(g, color, altura, largura, xi, yi, l_xf, l_yf);
		line(g, color, espessura, xi, yi, l_xf, l_yf);
	}

  /////////////////
 //	LABEL	//
/////////////////
	public static void label(Graphics g, String labelStr, Color colorLabel, double espessura, double altura, int StartCenterEnd, int UpInsideDown, double xi, double yi, double xf, double yf) {
		if (yf == yi) // Paralela ao eixo dos x
		{
			if (xf == xi) // � um ponto
			{
				return;
			}
			if (xf > xi) {
				xf = xf - (altura + 12.0);
			} else {
				xf = xf + (altura + 12.0);
			}
		} else {
			if (xf == xi) // Paralela ao eixo dos y
			{
				if (yf > yi) {
					yf = yf - (altura + 12.0);
				} else {
					yf = yf + (altura + 12.0);
				}
			} else {
				double alfa;

				if ((xf < xi && yf < yi) || (xf > xi && yf > yi)) {
					alfa = Math.atan((yf - yi) / (xf - xi));
					xf = xf - ((altura + 12.0) * Math.cos(alfa));
					yf = yf - ((altura + 12.0) * Math.sin(alfa));
				} else {
					alfa = Math.atan((xi - xf) / (yf - yi));
					xf = xf - ((altura + 12.0) * Math.cos(alfa));
					yf = yf - ((altura + 12.0) * Math.sin(alfa));
				}
			}
		}
		double x, y;

		switch (StartCenterEnd) {
			case Start:
				x = xi;
				y = yi;
				break;

			default:
			case Center:
				x = (xf + xi) / 2.0;
				y = (yf + yi) / 2.0;
				break;

			case End:
				x = xf;
				y = yf;
				break;
		}
		switch (UpInsideDown) {
			default:
			case Up:				  //setas da direita para a esquerda
				y = y + espessura + 70;
				x = x + espessura + 20.0;
				break;

			case Up2:				  //setas da direita para a esquerda
				y = y + 20.0;
				x = x + espessura + 50;
				break;

			case Up3:				  //setas da direita para a esquerda
				y = y + 25.0;
				x = x + espessura + 30;
				break;

			case Up4:				  //setas da direita para a esquerda
				y = y + espessura - 10;
				x = x + 30.0;
				break;

			case Inside:
				break;

			case Down:			   //setas da esquerda para a direita
				y = y + espessura + 5;
				x = x + 10.0;
				//y = y + espessura + 4.0;
				//x = x + 4.0;
				break;

			case Down2:			   //setas da esquerda para a direita
				y = y + 15.0;
				x = x + espessura - 20;
				break;

			case Down3:			   //setas da esquerda para a direita
				y = y - espessura + 70.0;
				x = x + espessura - 10;
				break;

			case Down4:			   //setas da esquerda para a direita
				y = y + espessura + 60;
				x = x - espessura + 30;
				break;
		}
		Color salva = g.getColor();
		g.setColor(colorLabel);
		g.drawString(labelStr, (int) Math.round(x), (int) Math.round(y));
		g.setColor(salva);
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// TRIANGLE
////////////////////////////////////////////////////////////////////////////////////////////////////////////////	//
	public static void triangle(Graphics g, double xi, double yi, double xf, double yf) {
		triangle(g, Color.black, 1.0, 1.0, xi, yi, xf, yf);
	}

	public static void triangle(Graphics g, Color color, double altura, double espessura, double xi, double yi, double xf, double yf) {
		int[] pX = new int[4];
		int[] pY = new int[4];

		if (yf == yi) // Paralela ao eixo dos x
		{
			if (xf == xi) {
				return;
			}
			pX[0] = pX[2] = pX[3] = toMaxInt(xf);
			if (xf > xi) // � um ponto
			{
				pX[1] = toMaxInt(xf + altura);
			} else {
				pX[1] = toMaxInt(xf - altura);
			}
			pY[0] = pY[3] = toMaxInt(yf - espessura);
			pY[1] = toMaxInt(yf);
			pY[2] = toMaxInt(yf + espessura);
		} else {
			if (xf == xi) // Paralela ao eixo dos y
			{
				pY[0] = pY[2] = pY[3] = toMaxInt(yf);
				if (yf > yi) {
					pY[1] = toMaxInt(yf + altura);
				} else {
					pY[1] = toMaxInt(yf - altura);
				}
				pX[0] = pX[3] = toMaxInt(xf + espessura);
				pX[1] = toMaxInt(xf);
				pX[2] = toMaxInt(xf - espessura);
			} else {
				if ((xf < xi && yf < yi) || (xf > xi && yf > yi)) {
					double alfa = Math.atan((yf - yi) / (xf - xi));
					double reta = yf / Math.sin(alfa);
					double reta2 = hypotenusa(reta, espessura);
					double theta = Math.asin(espessura / reta2);
					double gama = alfa - theta;
					double retaX = reta * Math.cos(alfa);
					double diferenca = retaX - xf;

					double px0 = reta2 * Math.cos(gama) - diferenca;
					double py0 = reta2 * Math.sin(gama);
					pX[0] = pX[3] = toMaxInt(px0);
					pY[0] = pY[3] = toMaxInt(py0);
					pX[2] = toMaxInt((2.0 * xf) - px0);
					pY[2] = toMaxInt((2.0 * yf) - py0);

					double reta3;
					if (yf > yi) {
						reta3 = reta2 + altura;
					} else {
						reta3 = reta2 - altura;
					}
					pX[1] = toMaxInt(reta3 * Math.cos(alfa) - diferenca);
					pY[1] = toMaxInt(reta3 * Math.sin(alfa));
				} else {
					double alfa = Math.abs(Math.atan((xf - xi) / (yf - yi)));
					double reta = xf / Math.sin(alfa);
					double reta2 = hypotenusa(reta, espessura);
					double theta = Math.asin(espessura / reta2);
					double gama = alfa - theta;
					double retaX = reta * Math.cos(alfa);
					double diferenca = retaX + yf;

					double px0 = reta2 * Math.sin(gama);
					double py0 = diferenca - Math.round(reta2 * Math.cos(gama));
					pX[0] = pX[3] = toMaxInt(px0);
					pY[0] = pY[3] = toMaxInt(py0);
					pX[2] = toMaxInt((2.0 * xf) - px0);
					pY[2] = toMaxInt((2.0 * yf) - py0);

					double reta3;
					if (xf > xi) {
						reta3 = reta + altura;
					} else {
						reta3 = reta - altura;
					}
					double px1 = reta3 * Math.sin(alfa);
					double a = (yi - yf) / (xi - xf);
					double b = yi - (a * xi);
					pX[1] = toMaxInt(px1);
					pY[1] = toMaxInt((a * px1) + b);
				}
			}
		}
		g.setColor(color);
		g.fillPolygon(pX, pY, pX.length);
	}

  ///////////////
 //	LINE   //
///////////////
	public static void line(Graphics g, double xi, double yi, double xf, double yf) {
		line(g, Color.black, 1.0, xi, yi, xf, yf);
	}

	public static void line(Graphics g, Color color, double espessura, double xi, double yi, double xf, double yf) {
		int[] pX = new int[5];
		int[] pY = new int[5];

		if (yf == yi) // Paralela ao eixo dos x
		{
			if (xf == xi) // � um ponto
			{
				return;
			}
			pX[0] = pX[3] = pX[4] = toMaxInt(xi);
			pX[1] = pX[2] = toMaxInt(xf);
			pY[0] = pY[4] = toMaxInt(yi - espessura);
			pY[3] = toMaxInt(yi + espessura);
			pY[1] = toMaxInt(yf - espessura);
			pY[2] = toMaxInt(yf + espessura);
		} else {
			if (xf == xi) // Paralela ao eixo dos y
			{
				pY[0] = pY[3] = pY[4] = toMaxInt(yi);
				pY[1] = pY[2] = toMaxInt(yf);
				pX[0] = pX[4] = toMaxInt(xi + espessura);
				pX[3] = toMaxInt(xi - espessura);
				pX[1] = toMaxInt(xf + espessura);
				pX[2] = toMaxInt(xf - espessura);
			} else {
				double alfa = Math.atan((yf - yi) / (xf - xi));
				double a = toMaxInt(espessura * Math.sin(alfa));
				double b = toMaxInt(espessura * Math.cos(alfa));

				if (xf < xi && yf < yi) {
					pX[0] = pX[4] = toMaxInt(xi - a);
					pX[1] = toMaxInt(xf - a);
					pX[2] = toMaxInt(xf + a);
					pX[3] = toMaxInt(xi + a);
					pY[0] = pY[4] = toMaxInt(yi + b);
					pY[1] = toMaxInt(yf + b);
					pY[2] = toMaxInt(yf - b);
					pY[3] = toMaxInt(yi - b);
				} else {
					if (xf > xi && yf > yi) {
						pX[0] = pX[4] = toMaxInt(xi + a);
						pX[1] = toMaxInt(xf + a);
						pX[2] = toMaxInt(xf - a);
						pX[3] = toMaxInt(xi - a);
						pY[0] = pY[4] = toMaxInt(yi - b);
						pY[1] = toMaxInt(yf - b);
						pY[2] = toMaxInt(yf + b);
						pY[3] = toMaxInt(yi + b);
					} else {
						if (xf > xi && yf < yi) {
							pX[0] = pX[4] = toMaxInt(xi - a);
							pX[1] = toMaxInt(xf - a);
							pX[2] = toMaxInt(xf + a);
							pX[3] = toMaxInt(xi + a);
							pY[0] = pY[4] = toMaxInt(yi - b);
							pY[1] = toMaxInt(yf - b);
							pY[2] = toMaxInt(yf + b);
							pY[3] = toMaxInt(yi + b);
						} else {
							pX[0] = pX[4] = toMaxInt(xi + a);
							pX[1] = toMaxInt(xf + a);
							pX[2] = toMaxInt(xf - a);
							pX[3] = toMaxInt(xi - a);
							pY[0] = pY[4] = toMaxInt(yi + b);
							pY[1] = toMaxInt(yf + b);
							pY[2] = toMaxInt(yf - b);
							pY[3] = toMaxInt(yi - b);
						}
					}
				}
			}
		}
		g.setColor(color);
		g.fillPolygon(pX, pY, pX.length);
	}

  /////////////
 //  RETAS  //
/////////////
	public static double distancia(double xi, double yi, double xf, double yf) {
		double x = Math.abs(xf - xi);
		double y = Math.abs(yf - yi);

		return Math.sqrt(x * x + y * y);
	}
}
