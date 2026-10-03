package cafes.model.ECWM;

import java.awt.*;
import javax.swing.JOptionPane;

class ECWM_Sequencia extends ECWM_Grafo
{
	private static final long serialVersionUID = 1004253756518036859L;
	private ECWM_MappingCost win;
	private ECWM_DesenhaSequencia dSeq;
	private static ECWM_Vertice verticeSelecionado;

	public ECWM_Sequencia(ECWM_DesenhaSequencia dSeq, ECWM_MappingCost win)
	{
		super();
		this.dSeq = dSeq;
		this.win = win;
		verticeSelecionado = null;
	}
	public ECWM_DesenhaSequencia getDesenhaSequencia()
	{
		return dSeq;
	}
	public void setDesenhaSequencia(ECWM_DesenhaSequencia DS)
	{ 
		this.dSeq = DS;
	}
	public void setaVerticeSelecionado(int x, int y)
	{
		ECWM_Vertice p = selecionaVertice(x, y);
		
		if(p!=null)
			verticeSelecionado = p;
	}
	public void moveNodoSequencia(int x, int y)
	{
		ECWM_Vertice auxiliar;
		
		try
		{
			auxiliar = selecionaVertice(x, y);
			if(auxiliar==null)
			{
				if(x>=1 && y>=1)
				{
					if(verticeSelecionado!=null) // Insere um novo nodo
					{
						verticeSelecionado.setX(x);
						verticeSelecionado.setY(y);
						dSeq.update();
						dSeq.setNaoSalvo();
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("PROBLEMAS 7!");
		}
	}
// Método que verifica se deve ser inserido um nodo na lista
	public void insereSequencia(int x, int y)
	{
		ECWM_Vertice auxiliar = selecionaVertice(x, y);
		
		if(auxiliar==null)
		{
			if(x>=1 && y>=1)
			{
				ECWM_CaixaInfo CI = new ECWM_CaixaInfo(win);
				String nomeCore = CI.getNome();
				
				if(nomeCore!=null)
				{
					if(jaExisteCore(nomeCore))
					{
						JOptionPane.showMessageDialog(null, "It is not acceptable to have the same name for two different cores.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					insereVerticeGrafo(new ECWM_Vertice(x, y, nomeCore));
					dSeq.setNaoSalvo();
				}
			}
		}
		else
		{
			if(auxiliar.equals(verticeSelecionado))
				return;
			if(verticeSelecionado!=null && verticeSelecionado!=auxiliar)
			{
				boolean estaNaLista = false;
				long peso = -1;
				double percentualChaveamentoPhits = -1.0;
				
				if(verticeSelecionado.jaEstaNaListaDeAdjacentes(auxiliar))
				{
					estaNaLista = true;
					peso = verticeSelecionado.getPesoAssociado(auxiliar);
					percentualChaveamentoPhits  = verticeSelecionado.getPercentualChaveamentoPhitsAssociado(auxiliar);
				}
				ECWM_CaixaPeso CP = new ECWM_CaixaPeso(win, peso, percentualChaveamentoPhits);
				peso = CP.getPhits();
				percentualChaveamentoPhits = CP.getPercentualChaveamentoPhits(); 
				if(peso>0 && percentualChaveamentoPhits>=0.0 && percentualChaveamentoPhits<=100.0)
				{
					if(estaNaLista==true)
						verticeSelecionado.deletaVerticeAdjacente(auxiliar);
					verticeSelecionado.insereNaListaDeAdjacentes(new ECWM_VerticeAdjacente(auxiliar, peso, percentualChaveamentoPhits));
					dSeq.setNaoSalvo();
				}
			}
		}
	}
	// Método que verifica se existe algum nodo apontado na posição (x, y).
	public ECWM_Vertice selecionaVertice(int x, int y)
	{
		ECWM_Vertice p=getInicio();
		
		while(p!=null)
		{
			if(p.acessouCirculo(x, y))
				return p;
			p = p.getProx();
		}
		return null;
	}
	public ECWM_Vertice getVerticeSelecionado()
	{
		return verticeSelecionado;
	}
	//Médoto para exibição do grafo
	public void exibeNodos(Graphics g)
	{
		ECWM_Vertice p;

		// Exibe todos os vértices
		p = getInicio();
		while(p!=null)
		{
			boolean paraCor;
			
			if(p==verticeSelecionado)
				paraCor = true;
			else
				paraCor = false;
			p.desenhaCirculo(g, p.getInf(), paraCor);
			p = p.getProx();
		}
		// Exibe todas as arestas
		p = getInicio();
		while(p!=null)
		{
			ECWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();
			
			while(adj!=null)
			{
				exibeAresta(g, p, adj);
				adj = adj.getProx();
			}
			p = p.getProx();
		}
	}
	public void exibeAresta(Graphics g, ECWM_Vertice v, ECWM_VerticeAdjacente adj)
	{
		while(adj!=null)
		{
			String label = new String(adj.getStrPhits() + " (" + adj.getStrPercentualChaveamentoPhits() + "%)");
			ECWM_Circulo.arrowAndLabelBetweenCircles(g, v.getX(), v.getY(), adj.getX(), adj.getY(), label);
			adj = adj.getProx();
		}
	}
	public ECWM_Vertice getPrimeiroVerticeECWM()
	{
		return getInicio();
	}
	public ECWM_Vertice getProxVerticeECWM(ECWM_Vertice p)
	{
		return p.getProx();
	}
	public void deleta()
	{
		ECWM_Vertice p = getInicio();
		while(p!=null)
		{
			deletaVertice(p);
			p = p.getProx();
		}
		verticeSelecionado = null;
	}
	public void deletaVertice(ECWM_Vertice verticeParaDeletar)
	{
		// Remove da lista de adjacentes
		ECWM_Vertice p = getInicio();
		
		while(p!=null)
		{
			ECWM_VerticeAdjacente adjAnt = null;
			ECWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();
			
			while(adj!=null)
			{
				if(adj.equals(verticeParaDeletar))
				{
					if(adj==p.getVerticeAdjacenteInicial())
						p.setVerticeAdjacenteInicial(adj.getProx());
					if(adj.getProx()==null)
					{
						if(adjAnt!=null)
							adjAnt.setProx(null);
						p.setVerticeAdjacenteFinal(adjAnt);
					}
					else
					{
						if(adjAnt!=null)
							adjAnt.setProx(adj.getProx());
					}
					break; // O vértice só pode estar uma única vez na lista de cada vértice
				}
				adjAnt = adj;
				adj = adj.getProx();
			}
			p = p.getProx();
		}

		// Remove da lista de vértices
		ECWM_Vertice pAnt = null;
		p = getInicio();
		while(p!=null)
		{
			if(p.equals(verticeParaDeletar))
			{
				dSeq.setNaoSalvo();
				if(p==getInicio())
					setInicio(p.getProx());
				if(p.getProx()==null)	// se caso o nodo selecionado for o ultimo da lista
				{
					if(pAnt!=null)
						pAnt.setProx(null);
					setFim(pAnt);
				}
				else
				{
					if(pAnt!=null)
						pAnt.setProx(p.getProx());
				}
				break; // O vértice só pode estar uma única vez na lista de vértices
			}
			pAnt = p;
			p = p.getProx();
		}
	}
	public boolean pontoSobreSeta(int x, int y)
	{
		ECWM_Vertice p=getInicio();
		
		while(p!=null)
		{
			ECWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();
			
			while(adj!=null)
			{
				int xDest=adj.getX();
				int yDest=adj.getY();
				
				if(p.xyBelongsToLine(x, y, xDest, yDest))
					return true;
				adj = adj.getProx();
			}
			p = p.getProx();
		}
		return false;
	}
	public ECWM_Vertice verticeDoPontoSobreSeta(int x, int y)
	{
		ECWM_Vertice p=getInicio();
		
		while(p!=null)
		{
			ECWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();
			
			while(adj!=null)
			{
				int xDest=adj.getX();
				int yDest=adj.getY();
				
				if(p.xyBelongsToLine(x, y, xDest, yDest))
					return p;
				adj = adj.getProx();
			}
			p = p.getProx();
		}
		return null;
	}
	public ECWM_VerticeAdjacente verticeAdjacenteDoPontoSobreSeta(int x, int y)
	{
		ECWM_Vertice p=getInicio();
		
		while(p!=null)
		{
			ECWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();
			
			while(adj!=null)
			{
				int xDest=adj.getX();
				int yDest=adj.getY();

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
		ECWM_Vertice v=verticeDoPontoSobreSeta(x, y);
		if(v==null)
			return;
		ECWM_VerticeAdjacente va=verticeAdjacenteDoPontoSobreSeta(x, y);
		if(va==null)
			return;
		ECWM_CaixaInfoSeta CI = new ECWM_CaixaInfoSeta(va, win);
		String phitsStr = CI.getValorPhits();
		String chaveamentoStr = CI.getValorChaveamento();
		if(phitsStr!=null)
		{
			if(phitsStr.equals("deletar") || chaveamentoStr.equals("deletar"))
				v.deletaVerticeAdjacente(va.getVertice());
			else
			{
				va.setPhits(phitsStr);
				va.setPercentualChaveamentoPhits(chaveamentoStr);
			}
			dSeq.setNaoSalvo();
		}
	}
	public void editaNodoExistente(ECWM_Vertice n)
	{
		ECWM_CaixaInfo CI = new ECWM_CaixaInfo(n, win);
		String nome = CI.getNome();
		if(nome!=null)
		{
			dSeq.setNaoSalvo();
			verticeSelecionado.setInf(nome);
		}
	}
	public boolean grafoVazio()
	{
		return getInicio()==null;
	}
	public int getNumeroDeVertices()
	{
		int numero = 0;
		ECWM_Vertice p = getInicio();
		
		while(p!=null)
		{
			numero++;
			p = p.getProx();
		}
		return numero;
	}
	public String stringLayout()
	{
		String str = "";
		ECWM_Vertice p = getInicio();

		while(p!=null)
		{
			str = str.concat("\n " + p.getInf() + " " + p.getX() + " " + p.getY());
			p = p.getProx();
		}
		return str;
	}
	public String stringVertices()
	{
		String nodos = "";
		ECWM_Vertice p = getInicio();

		while(p!=null)
		{
			nodos = nodos.concat("\n " + new String(p.getInf()));
			p = p.getProx();
		}
		return nodos;
	}
	public String stringArestas()
	{
		String s = "";
		ECWM_Vertice p = getInicio();
		
		while(p!=null)
		{
			String coreOrigem = p.getInf();
			ECWM_VerticeAdjacente adj = p.getVerticeAdjacenteInicial();
			
			while(adj!=null)
			{
				String coreDestino = adj.getInf();
				long peso = adj.getPhits();
				double chaveando = adj.getPercentualChaveamentoPhits(); 

				s = s.concat(" " + coreOrigem + " - " + coreDestino + " " + peso + " " + chaveando + " " + "\n");
				adj = adj.getProx();
			}
			p = p.getProx();
		}
		return s;
	}
}
