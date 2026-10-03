package cafes.model.ACPM;

import java.awt.*;
import cafes.common.*;

class ACPM_CirculoTag extends Circulo
{
	private static final long serialVersionUID = -1898704423242156827L;
	protected int tag;

	public ACPM_CirculoTag(int tag)
	{
		super(Circulo.raio, Circulo.raio);
		this.tag = tag;
	}
	public ACPM_CirculoTag(int x, int y, int tag)
	{
		super(x, y);
		this.tag = tag;
	}

	public int getTag() { return tag; }

	public String getStringTag() { return String.valueOf(tag); }

	@Override
	protected void desenhaConteudo(Graphics g)
	{
		String nome;

		g.setFont(new Font("Arial", Font.BOLD, 12));
		switch(tag)
		{
			case ACPM_Grafo.START: // Vértice START
				nome = "START";
				break;

			case ACPM_Grafo.END: // Vértice END
				nome = "END";
				break;

			default:
				nome = getStringTag();
				break;
		}
		g.drawString(nome, x - (int) (nome.length() * 3.2), y + 6);
	}
}