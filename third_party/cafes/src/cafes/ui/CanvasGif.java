package cafes.ui;

import java.awt.*;

class CanvasGif extends Canvas
{
	private static final long serialVersionUID = -1933038413361038999L;
	private Image imagem;

	public CanvasGif()
	{
		super();
	}
	public void paint(Graphics g)
	{
		imagem = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("cafes.gif"));
		g.drawImage(imagem, 0, 0, this);
	}
	public void update(Graphics g)
	{
		paint(g);
	}
}
