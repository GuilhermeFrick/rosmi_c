package cafes.model.CDM;

import java.awt.*;

import cafes.model.CWM.*;

public class CDM_Sequencia extends CDM_Grafo
{
	private static final long serialVersionUID = 939530370157348761L;
	private CDM_MappingCost win;
	private CDM_DesenhaSequencia dSeq;
	private static CDM_Vertice verticeSelecionado;

	public CDM_Sequencia(CDM_DesenhaSequencia dSeq, CDM_MappingCost win)
	{
		super();
		this.dSeq = dSeq;
		this.win = win;
		verticeSelecionado = null;
	}
	public CDM_DesenhaSequencia getDesenhaSequencia()
	{
		return dSeq;
	}
	public void setDesenhaSequencia(CDM_DesenhaSequencia DS)
	{ 
		this.dSeq = DS;
	}
	public void setaVerticeSelecionado(int x, int y)
	{
		CDM_Vertice p = selecionaVertice(x, y);
		
		if(p!=null)
			verticeSelecionado = p;
	}
	public void moveNodoSequencia(int x, int y)
	{
		CDM_Vertice auxiliar;
		
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
	public void CDM2CWM(CWM_Sequencia seq)
	{
		int coluna=CWM_Circulo.getRaio()*2, linha=CWM_Circulo.getRaio()*2;
	
		CDM_Vertice p=getInicio();
		while(p!=null)
		{
			long phits = p.getPhits();
			if(phits>0)
			{
				String coreOrigem = p.getCoreOrigem();
				CWM_Vertice verticeCoreOrigem = seq.verticeDoCore(coreOrigem);
				if(verticeCoreOrigem==null)
				{
					verticeCoreOrigem = new CWM_Vertice(coluna, linha, coreOrigem);
					seq.insereVerticeGrafo(verticeCoreOrigem);
					coluna = coluna + CWM_Circulo.getRaio()*3;
					if(coluna+CWM_Circulo.getRaio()*2>=seq.getDesenhaSequencia().getMaxColuna())
					{
						coluna = CWM_Circulo.getRaio()*3;
						linha = linha + CWM_Circulo.getRaio()*3;
					}
				}
				String coreDestino = p.getCoreDestino();
				CWM_Vertice verticeCoreDestino = seq.verticeDoCore(coreDestino);
				if(verticeCoreDestino==null)
				{
					verticeCoreDestino = new CWM_Vertice(coluna, linha, coreDestino);
					seq.insereVerticeGrafo(verticeCoreDestino);
					coluna = coluna + CWM_Circulo.getRaio()*3;
					if(coluna+CWM_Circulo.getRaio()*2>=seq.getDesenhaSequencia().getMaxColuna())
					{
						coluna = CWM_Circulo.getRaio()*3;
						linha = linha + CWM_Circulo.getRaio()*3;
					}
					verticeCoreOrigem.insereNaListaDeAdjacentes(new CWM_VerticeAdjacente(verticeCoreDestino, phits));
				}
				else
					verticeCoreOrigem.incrementaNaListaDeAdjacentes(verticeCoreDestino, phits);
			}
			p = p.getProx();
		}
	}
	public void insereVerticeStartGrafo()
	{
		insereVerticeGrafo(new CDM_Vertice(win.getIniX()+(win.getFimX()-win.getIniX()-CDM_Circulo.getRaio())/2, CDM_Circulo.getRaio(), CDM_Grafo.START));
	}
	public void insereVerticeEndGrafo()
	{
		insereVerticeGrafo(new CDM_Vertice(win.getIniX()+(win.getFimX()-win.getIniX()-CDM_Circulo.getRaio())/2, win.getFimY()-2*CDM_Circulo.getRaio(), CDM_Grafo.END));
	}
// M�todo que verifica se deve ser inserido um nodo na lista
	public void insereSequencia(int x, int y)
	{
		CDM_Vertice auxiliar = selecionaVertice(x, y);
		
		if(auxiliar==null)
		{
			if(x>=1 && y>=1)
			{
				CDM_CaixaInfo CI = new CDM_CaixaInfo(win);
				String strCoreOrigem = CI.getCoreOrigem();
				String strCoreDestino = CI.getCoreDestino();
				long phits = CI.getPhits();
				
				if(strCoreOrigem!=null && strCoreDestino!=null && phits>0)
				{
					insereVerticeGrafo(new CDM_Vertice(x, y, strCoreOrigem, strCoreDestino, phits));
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
				if(verticeSelecionado.jaEstaNaListaDeDependentes(auxiliar))
					verticeSelecionado.deletaVerticeDependente(auxiliar);
				verticeSelecionado.insereNaListaDeDependentes(new CDM_VerticeDependente(auxiliar));
				dSeq.setNaoSalvo();
			}
		}
	}
	// M�todo que verifica se existe algum vertice com o id desejado
	public CDM_Vertice encontraVerticeNoGrafo(int id)
	{
		CDM_Vertice p=getInicio();
		
		while(p!=null)
		{
			if(p.getID()==id)
				return p;
			p = p.getProx();
		}
		return null;
	}
	// M�todo que verifica se existe algum nodo apontado na posi��o (x, y).
	public CDM_Vertice selecionaVertice(int x, int y)
	{
		CDM_Vertice p=getInicio();
		
		while(p!=null)
		{
			if(p.acessouCirculo(x, y))
				return p;
			p = p.getProx();
		}
		return null;
	}
	public CDM_Vertice getVerticeSelecionado()
	{
		return verticeSelecionado;
	}
	//M�doto para exibi��o do grafo
	public void exibeNodos(Graphics g)
	{
		CDM_Vertice p;

		// Exibe todos os v�rtices
		p = getInicio();
		while(p!=null)
		{
			boolean paraCor;
			
			if(p==verticeSelecionado)
				paraCor = true;
			else
				paraCor = false;
			p.desenhaCirculo(g, paraCor);
			p = p.getProx();
		}
		// Exibe todas as arestas
		p = getInicio();
		while(p!=null)
		{
			CDM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
			while(dep!=null)
			{
				exibeAresta(g, p, dep);
				dep = dep.getProx();
			}
			p = p.getProx();
		}
	}
	public void exibeAresta(Graphics g, CDM_Vertice v, CDM_VerticeDependente dep)
	{
		while(dep!=null)
		{
			try {
			CDM_Circulo.arrowBetweenCircles(g, v.getX(), v.getY(), dep.getX(), dep.getY());
			}catch(Exception e) {}
			dep = dep.getProx();
		}
	}
	public CDM_Vertice getPrimeiroVerticeCDM()
	{
		return getInicio();
	}
	public CDM_Vertice getProxVerticeCDM(CDM_Vertice p)
	{
		return p.getProx();
	}
	public void deleta()
	{
		CDM_Vertice p = getInicio();
		while(p!=null)
		{
			deletaVertice(p);
			p = p.getProx();
		}
		CDM_Vertice.id = 0;
		verticeSelecionado = null;
	}
	public void deletaVertice(CDM_Vertice verticeParaDeletar)
	{
		// Remove da lista de dependente
		CDM_Vertice p = getInicio();
		
		while(p!=null)
		{
			CDM_VerticeDependente depAnt = null;
			CDM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
			while(dep!=null)
			{
				if(dep.equals(verticeParaDeletar))
				{
					if(dep==p.getVerticeDependenteInicial())
						p.setVerticeDependenteInicial(dep.getProx());
					if(dep.getProx()==null)
					{
						if(depAnt!=null)
							depAnt.setProx(null);
						p.setVerticeDependenteFinal(depAnt);
					}
					else
					{
						if(depAnt!=null)
							depAnt.setProx(dep.getProx());
					}
					break; // O v�rtice s� pode estar uma �nica vez na lista de cada v�rtice
				}
				depAnt = dep;
				dep = dep.getProx();
			}
			p = p.getProx();
		}

		// Remove da lista de v�rtices
		CDM_Vertice pAnt = null;
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
				break; // O v�rtice s� pode estar uma �nica vez na lista de v�rtices
			}
			pAnt = p;
			p = p.getProx();
		}
	}
	public boolean pontoSobreSeta(int x, int y)
	{
		CDM_Vertice p=getInicio();
		
		while(p!=null)
		{
			CDM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
			while(dep!=null)
			{
				int xDest=dep.getX();
				int yDest=dep.getY();
				
				if(p.xyBelongsToLine(x, y, xDest, yDest))
					return true;
				dep = dep.getProx();
			}
			p = p.getProx();
		}
		return false;
	}
	public CDM_Vertice verticeDoPontoSobreSeta(int x, int y)
	{
		CDM_Vertice p=getInicio();
		
		while(p!=null)
		{
			CDM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
			while(dep!=null)
			{
				int xDest=dep.getX();
				int yDest=dep.getY();
				
				if(p.xyBelongsToLine(x, y, xDest, yDest))
					return p;
				dep = dep.getProx();
			}
			p = p.getProx();
		}
		return null;
	}
	public CDM_VerticeDependente VerticeDependenteDoPontoSobreSeta(int x, int y)
	{
		CDM_Vertice p=getInicio();
		
		while(p!=null)
		{
			CDM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
			while(dep!=null)
			{
				int xDest=dep.getX();
				int yDest=dep.getY();

				if(p.xyBelongsToLine(x, y, xDest, yDest))
					return dep;
				dep = dep.getProx();
			}
			p = p.getProx();
		}
		return null;
	}
	public void editaSetaExistente(int x, int y)
	{
		CDM_Vertice v=verticeDoPontoSobreSeta(x, y);
		if(v==null)
			return;
		CDM_VerticeDependente va=VerticeDependenteDoPontoSobreSeta(x, y);
		if(va==null)
			return;
		new CDM_CaixaInfoSeta(v, va.getVertice(), win);
	}
	public void editaNodoExistente(CDM_Vertice n)
	{
		CDM_CaixaInfo CI = new CDM_CaixaInfo(n, win);
		String coreOrigem = CI.getCoreOrigem();
		String coreDestino = CI.getCoreDestino();
		long phits = CI.getPhits();
		if(coreOrigem!=null && coreDestino!=null && phits>0)
		{
			verticeSelecionado.setCoreOrigem(coreOrigem);
			verticeSelecionado.setCoreDestino(coreDestino);
			verticeSelecionado.setPhits(phits);
			dSeq.setNaoSalvo();
		}
	}
	public boolean grafoVazio()
	{
		return getInicio()==null;
	}
	public String stringLayout()
	{
		String str = "";
		CDM_Vertice p = getInicio();

		while(p!=null)
		{
			str = str.concat("\n " + p.formataStringID() + " " + p.getX() + " " + p.getY());
			p = p.getProx();
		}
		return str;
	}
	public String stringVertices()
	{
		String nodos = "";
		CDM_Vertice p = getInicio();

		while(p!=null)
		{
			if(p.getID()!=CDM_Grafo.START && p.getID()!=CDM_Grafo.END)
				nodos = nodos.concat("\n " + p.formataStringID() + " " + p.getCoreOrigem() + " - " + p.getCoreDestino()+ " " + p.getPhits());
			p = p.getProx();
		}
		return nodos;
	}
	public String stringArestas()
	{
		String s = "";
		CDM_Vertice p = getInicio();
		
		while(p!=null)
		{
			CDM_VerticeDependente q = p.getVerticeDependenteInicial();
			
			s = s.concat(" " + p.formataStringID());
			while(q!=null)
			{
				try {
				s = s.concat(" " + q.getVertice().formataStringID());
				}catch(Exception e){}
				q = q.getProx();
			}
			s = s.concat("\n");
			p = p.getProx();
		}
		return s;
	}
}
