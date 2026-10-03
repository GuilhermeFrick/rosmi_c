package cafes.model.CWM;

import java.awt.*;
import javax.swing.JOptionPane;

public class CWM_Sequencia extends CWM_Grafo
{
	private static final long serialVersionUID = -8598933066359974496L;
	private CWM_MappingCost win;
	private CWM_DesenhaSequencia dSeq;
	private static CWM_Vertice verticeSelecionado;

	public CWM_Sequencia(CWM_DesenhaSequencia dSeq, CWM_MappingCost win)
	{
		super();
		this.dSeq = dSeq;
		this.win = win;
		verticeSelecionado = null;
	}

	public CWM_DesenhaSequencia getDesenhaSequencia()
	{
		return dSeq;
	}

	public void setDesenhaSequencia(CWM_DesenhaSequencia DS)
	{
		this.dSeq = DS;
	}

	public void setaVerticeSelecionado(int x, int y)
	{
		CWM_Vertice p = selecionaVertice(x, y);

		if(p != null)
			verticeSelecionado = p;
	}

	public void moveNodoSequencia(int x, int y)
	{
		CWM_Vertice auxiliar;

		try
		{
			auxiliar = selecionaVertice(x, y);
			if(auxiliar == null)
			{
				if(x >= 1 && y >= 1)
				{
					if(verticeSelecionado != null) // Insere um novo nodo
					{
						verticeSelecionado.setX(x);
						verticeSelecionado.setY(y);
						dSeq.update();
						dSeq.setSalvo(false);
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("PROBLEMAS 7!");
		}
	}

// M�todo que verifica se deve ser inserido um nodo na lista
	public void insereSequencia(int x, int y)
	{
		CWM_Vertice auxiliar = selecionaVertice(x, y);

		if(auxiliar == null)
		{
			if(x >= 1 && y >= 1)
			{
				CWM_CaixaInfo CI = new CWM_CaixaInfo(win);
				String nomeCore = CI.getNome();

				if(nomeCore != null)
				{
					if(jaExisteCore(nomeCore))
					{
						JOptionPane.showMessageDialog(null, "It is not acceptable to have the same name for two different cores.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					insereVerticeGrafo(new CWM_Vertice(x, y, nomeCore));
					dSeq.setSalvo(false);
				}
			}
		}
		else
		{
			if(auxiliar.equals(verticeSelecionado))
				return;
			if(verticeSelecionado != null && verticeSelecionado != auxiliar) 
			{
				boolean estaNaLista = false;
				long peso = -1;

				if(verticeSelecionado.jaEstaNaListaDeAdjacentes(auxiliar)) 
				{
					estaNaLista = true;
					peso = verticeSelecionado.getPesoAssociado(auxiliar);
				}
				CWM_CaixaPeso CP = new CWM_CaixaPeso(win, peso);
				peso = CP.getPeso();
				if(peso > 0)
				{
					if(estaNaLista == true)
						verticeSelecionado.deletaVerticeAdjacente(auxiliar);
					verticeSelecionado.insereNaListaDeAdjacentes(new CWM_VerticeAdjacente(auxiliar, peso));
					dSeq.setSalvo(false);
				}
			}
		}
	}

	// M�todo que verifica se existe algum nodo apontado na posi��o (x, y).
	public CWM_Vertice selecionaVertice(int x, int y)
	{
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			if(p.acessouCirculo(x, y))
				return p;
			p = p.getProx();
		}
		return null;
	}

	public CWM_Vertice getVerticeSelecionado()
	{
		return verticeSelecionado;
	}
	//M�todo para exibi��o do grafo

	public void exibeNodos(Graphics g)
	{
		CWM_Vertice p;

		// Exibe todos os v�rtices
		p = getInicio();
		while(p != null)
		{
			boolean paraCor;

			if(p == verticeSelecionado)
				paraCor = true;
			else
				paraCor = false;
			p.desenhaCirculo(g, paraCor);
			p = p.getProx();
		}
		// Exibe todas as arestas
		p = getInicio();
		while(p != null)
		{
			CWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();

			while(adj != null)
			{
				exibeAresta(g, p, adj);
				adj = adj.getProx();
			}
			p = p.getProx();
		}
	}

	public void exibeAresta(Graphics g, CWM_Vertice v, CWM_VerticeAdjacente adj)
	{
		while(adj != null)
		{
			try {
			CWM_Circulo.arrowAndLabelBetweenCircles(g, v.getX(), v.getY(), adj.getX(), adj.getY(), adj.getStrPhits());
			}catch(Exception e) {
			
			}
			adj = adj.getProx();
			
		}
	}

	public CWM_Vertice getPrimeiroVerticeCWM()
	{
		return getInicio();
	}

	public CWM_Vertice getProxVerticeCWM(CWM_Vertice p)
	{
		return p.getProx();
	}

	public void deleta()
	{
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			deletaVertice(p);
			p = p.getProx();
		}
		verticeSelecionado = null;
	}

	public void deletaVertice(CWM_Vertice verticeParaDeletar)
	{
		// Remove da lista de adjacentes
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			CWM_VerticeAdjacente adjAnt = null;
			CWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();

			while(adj != null)
			{
				if(adj.equals(verticeParaDeletar))
				{
					if(adj == p.getVerticeAdjacenteInicial())
						p.setVerticeAdjacenteInicial(adj.getProx());
					if(adj.getProx() == null)
					{
						if(adjAnt != null)
							adjAnt.setProx(null);
						p.setVerticeAdjacenteFinal(adjAnt);
					}
					else
					{
						if(adjAnt != null)
							adjAnt.setProx(adj.getProx());
					}
					break; // O v�rtice s� pode estar uma �nica vez na lista de cada v�rtice
				}
				adjAnt = adj;
				adj = adj.getProx();
			}
			p = p.getProx();
		}

		// Remove da lista de v�rtices
		CWM_Vertice pAnt = null;
		p = getInicio();
		while(p != null)
		{
			if(p.equals(verticeParaDeletar))
			{
				dSeq.setSalvo(false);
				if(p == getInicio())
					setInicio(p.getProx());
				if(p.getProx() == null) // se caso o nodo selecionado for o ultimo da lista
				{
					if(pAnt != null)
						pAnt.setProx(null);
					setFim(pAnt);
				}
				else
				{
					if(pAnt != null)
						pAnt.setProx(p.getProx());
				}
				break; // O v�rtice s� pode estar uma �nica vez na lista de v�rtices
			}
			pAnt = p;
			p = p.getProx();
		}
	}

	public boolean pontoSobreSeta(int x, int y)
	{
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			CWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();

			while(adj != null)
			{
				int xDest = adj.getX();
				int yDest = adj.getY();

				if(p.xyBelongsToLine(x, y, xDest, yDest))
					return true;
				adj = adj.getProx();
			}
			p = p.getProx();
		}
		return false;
	}

	public CWM_Vertice verticeDoPontoSobreSeta(int x, int y)
	{
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			CWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();

			while(adj != null)
			{
				int xDest = adj.getX();
				int yDest = adj.getY();

				if(p.xyBelongsToLine(x, y, xDest, yDest))
					return p;
				adj = adj.getProx();
			}
			p = p.getProx();
		}
		return null;
	}

	public CWM_VerticeAdjacente verticeAdjacenteDoPontoSobreSeta(int x, int y)
	{
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			CWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();

			while(adj != null)
			{
				int xDest = adj.getX();
				int yDest = adj.getY();

				if(p.xyBelongsToLine(x, y, xDest, yDest))
					return adj;
				adj = adj.getProx();
			}
			p = p.getProx();
		}
		return null;
	}

	public void editaSetaExistente(int x, int y)
	{
		CWM_Vertice v = verticeDoPontoSobreSeta(x, y);
		if(v == null)
			return;
		CWM_VerticeAdjacente va = verticeAdjacenteDoPontoSobreSeta(x, y);
		if(va == null)
			return;
		CWM_CaixaInfoSeta CI = new CWM_CaixaInfoSeta(va, win);
		String nome = CI.getValor();
		if(nome != null)
		{
			if(nome.equals("deletar"))
				v.deletaVerticeAdjacente(va.getVertice());
			else
				va.setPhits(nome);
			dSeq.setSalvo(false);
		}
	}

	public void editaNodoExistente(CWM_Vertice n)
	{
		CWM_CaixaInfo CI = new CWM_CaixaInfo(n, win);
		String nome = CI.getNome();
		if(nome != null)
		{
			dSeq.setSalvo(false);
			verticeSelecionado.setInf(nome);
		}
	}

	public boolean grafoVazio()
	{
		return getInicio() == null;
	}

	public int getNumeroDeVertices()
	{
		int numero = 0;
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			numero++;
			p = p.getProx();
		}
		return numero;
	}

	public String stringLayout()
	{
		String str = "";
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			str = str.concat("\n " + p.getInf() + " " + p.getX() + " " + p.getY());
			p = p.getProx();
		}
		return str;
	}

	public String stringVertices()
	{
		String nodos = "";
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			nodos = nodos.concat("\n " + new String(p.getInf()));
			p = p.getProx();
		}
		return nodos;
	}

	public String stringArestas()
	{
		String s = "";
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			String coreOrigem = p.getInf();
			CWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();

			while(adj != null)
			{
				try {
				String coreDestino = adj.getInf();
				long peso = adj.getPhits();

				s = s.concat(" " + coreOrigem + " - " + coreDestino + " " + peso + "\n");
				}catch(Exception e){}
				adj = adj.getProx();
			}
			p = p.getProx();
		}
		return s;
	}

	public int numCores()
	{
		int count = 0;
		CWM_Vertice p = getInicio();

		while(p != null)
		{
			count++;
			p = p.getProx();
		}
		return count;
	}
}
