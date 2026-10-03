package cafes.model.CDCM;

import java.awt.*;

import cafes.model.CDM.*;
import cafes.model.CWM.*;

class CDCM_Sequencia extends CDCM_Grafo
{
	private static final long serialVersionUID = -3351809048199705453L;
	private CDCM_MappingCost win;
	private CDCM_DesenhaSequencia dSeq;
	private CDCM_ListaComputacao listaComputacao;
	private CDCM_ListaComunicacao listaComunicacao;
	private CDCM_ListaComunicacaoComComputacao listaComunicacaoComComputacao;
	private static CDCM_Vertice verticeSelecionado;

	public CDCM_Sequencia(CDCM_DesenhaSequencia dSeq, CDCM_MappingCost win)
	{
		super();
		this.dSeq = dSeq;
		this.win = win;
		verticeSelecionado = null;
	}
	public CDCM_MappingCost getMappingCost()
	{
		return win;
	}
	public CDCM_DesenhaSequencia getDesenhaSequencia()
	{
		return dSeq;
	}
	public void setDesenhaSequencia(CDCM_DesenhaSequencia DS)
	{
		this.dSeq = DS;
	}
	public void setaVerticeSelecionado(int x, int y)
	{
		CDCM_Vertice p = selecionaVertice(x, y);

		if(p != null)
			verticeSelecionado = p;
	}
	public void moveNodoSequencia(int x, int y)
	{
		CDCM_Vertice auxiliar;
		
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
	public void CDCM2CWM(CWM_Sequencia seq)
	{
		int coluna = CWM_Circulo.getRaio() * 2, linha = CWM_Circulo.getRaio() * 2;
		CDCM_Vertice p = getInicio();

		while(p != null)
		{
			long phits = p.getPhits();

			if(phits > 0)
			{
				String coreOrigem = p.getCoreOrigem();
				CWM_Vertice verticeCoreOrigem = seq.verticeDoCore(coreOrigem);
				if(verticeCoreOrigem == null)
				{
					verticeCoreOrigem = new CWM_Vertice(coluna, linha, coreOrigem);
					seq.insereVerticeGrafo(verticeCoreOrigem);
					coluna = coluna + CWM_Circulo.getRaio() * 3;
					if(coluna+CWM_Circulo.getRaio() * 2 >= seq.getDesenhaSequencia().getMaxColuna())
					{
						coluna = CWM_Circulo.getRaio() * 3;
						linha = linha + CWM_Circulo.getRaio() * 3;
					}
				}
				String coreDestino = p.getCoreDestino();
				CWM_Vertice verticeCoreDestino = seq.verticeDoCore(coreDestino);
				if(verticeCoreDestino == null)
				{
					verticeCoreDestino = new CWM_Vertice(coluna, linha, coreDestino);
					seq.insereVerticeGrafo(verticeCoreDestino);
					coluna = coluna + CWM_Circulo.getRaio()*3;
					if(coluna + CWM_Circulo.getRaio() * 2 >= seq.getDesenhaSequencia().getMaxColuna())
					{
						coluna = CWM_Circulo.getRaio() * 3;
						linha = linha + CWM_Circulo.getRaio() * 3;
					}
					verticeCoreOrigem.insereNaListaDeAdjacentes(new CWM_VerticeAdjacente(verticeCoreDestino, phits));
				}
				else
					verticeCoreOrigem.incrementaNaListaDeAdjacentes(verticeCoreDestino, phits);
			}
			p = p.getProx();
		}
	}
	public void CDCM2CDM(CDM_Sequencia seq)
	{
		CDCM_Vertice p = getInicio();

		while(p != null)		// Insere os v�rtices
		{
			int id = p.getID();
			int x = p.getX();
			int y = p.getY();
			
			if(id != CDCM_Grafo.END && id != CDCM_Grafo.START)
			{
				String strCoreOrigem = p.getCoreOrigem();
				String strCoreDestino = p.getCoreDestino();
				long phits = p.getPhits();

				seq.insereVerticeGrafo(new CDM_Vertice(x, y, id, strCoreOrigem, strCoreDestino, phits));
			}
			else
			{
				if(id == CDCM_Grafo.END)
				{
					seq.getFim().setX(x);
					seq.getFim().setY(y);
				}
				else // id==CDCM_Grafo.START
				{
					seq.getInicio().setX(x);
					seq.getInicio().setY(y);
				}
			}
			p = p.getProx();
		}
		p = getInicio();
		while(p != null)		// Insere as arestas
		{
			int id = p.getID();
			
			if(id != CDCM_Grafo.END)
			{
				CDCM_VerticeDependente vd = p.getVerticeDependenteInicial();
				while(vd != null)		// Insere as arestas
				{
					CDM_Vertice origem = seq.encontraVerticeNoGrafo(id);
					CDM_Vertice destino = seq.encontraVerticeNoGrafo(vd.getVertice().getID());
					origem.insereNaListaDeDependentes(new CDM_VerticeDependente(destino));
					vd = vd.getProx();
				}
			}
			p = p.getProx();
		}
	}
	public void insereVerticeStartGrafo()
	{
		insereVerticeGrafo(new CDCM_Vertice(win.getIniX()+(win.getFimX()-win.getIniX()-CDCM_Circulo.getRaio())/2, CDCM_Circulo.getRaio(), CDCM_Grafo.START));
	}
	public void insereVerticeEndGrafo()
	{
		insereVerticeGrafo(new CDCM_Vertice(win.getIniX()+(win.getFimX()-win.getIniX()-CDCM_Circulo.getRaio())/2, win.getFimY()-2*CDCM_Circulo.getRaio(), CDCM_Grafo.END));
	}
// M�todo que verifica se deve ser inserido um nodo na lista
	public void insereSequencia(int x, int y)
	{
		CDCM_Vertice auxiliar = selecionaVertice(x, y);
		
		if(auxiliar==null)
		{
			if(x>=1 && y>=1)
			{
				CDCM_CaixaInfo CI = new CDCM_CaixaInfo(win);
				String strCoreOrigem = CI.getCoreOrigem();
				String strCoreDestino = CI.getCoreDestino();
				long phits = CI.getPhits();
				int computacao = CI.getComputacao();
				
				if(strCoreOrigem!=null && strCoreDestino!=null && phits>0)
				{
					insereVerticeGrafo(new CDCM_Vertice(x, y, strCoreOrigem, strCoreDestino, phits, computacao));
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
				verticeSelecionado.insereNaListaDeDependentes(new CDCM_VerticeDependente(auxiliar));
				dSeq.setNaoSalvo();
			}
		}
	}
	// M�todo que verifica se existe algum vertice com o id desejado
	public CDCM_Vertice encontraVerticeNoGrafo(int id)
	{
		CDCM_Vertice p=getInicio();
		
		while(p!=null)
		{
			if(p.getID()==id)
				return p;
			p = p.getProx();
		}
		return null;
	}
	// M�todo que verifica se existe algum nodo apontado na posi��o (x, y).
	public CDCM_Vertice selecionaVertice(int x, int y)
	{
		CDCM_Vertice p=getInicio();
		
		while(p!=null)
		{
			if(p.acessouCirculo(x, y))
				return p;
			p = p.getProx();
		}
		return null;
	}
	public CDCM_Vertice getVerticeSelecionado()
	{
		return verticeSelecionado;
	}
	//M�doto para exibi��o do grafo
	public void exibeNodos(Graphics g)
	{
		CDCM_Vertice p = getInicio();

		// Exibe todos os v�rtices
		while(p != null)
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
			CDCM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
			while(dep!=null)
			{
				try {
				exibeAresta(g, p, dep);
				}catch(Exception e){}
				dep = dep.getProx();
			}
			p = p.getProx();
		}
	}
	public void coloreGrafo(Graphics g)
	{
		if(listaComputacao!=null)
			listaComputacao.coloreGrafo(g);
		if(listaComunicacao!=null)
			listaComunicacao.coloreGrafo(g);
		if(listaComunicacaoComComputacao!=null)
			listaComunicacaoComComputacao.coloreGrafo(g);
	}
	public void coloreAresta(Graphics g, Color cor, CDCM_Vertice origem, CDCM_Vertice destino)
	{
		CDCM_Circulo.arrowBetweenCircles(g, cor, origem.getX(), origem.getY(), destino.getX(), destino.getY());
	}
	public void exibeAresta(Graphics g, CDCM_Vertice v, CDCM_VerticeDependente dep)
	{
		while(dep!=null)
		{
			CDCM_Circulo.arrowBetweenCircles(g, v.getX(), v.getY(), dep.getX(), dep.getY());
			dep = dep.getProx();
		}
	}
	public CDCM_Vertice getPrimeiroVerticeCDCM()
	{
		return getInicio();
	}
	public CDCM_Vertice getProxVerticeCDCM(CDCM_Vertice p)
	{
		return p.getProx();
	}
	public void deleta()
	{
		CDCM_Vertice p = getInicio();
		while(p!=null)
		{
			deletaVertice(p);
			p = p.getProx();
		}
		CDCM_Vertice.id = 0;
		verticeSelecionado = null;
		removeAnaliseTemporal();
	}
	public void deletaVertice(CDCM_Vertice verticeParaDeletar)
	{
		// Remove da lista de dependente
		CDCM_Vertice p = getInicio();
		
		while(p!=null)
		{
			CDCM_VerticeDependente depAnt = null;
			CDCM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
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
		CDCM_Vertice pAnt = null;
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
		CDCM_Vertice p=getInicio();
		
		while(p!=null)
		{
			CDCM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
			while(dep!=null)
			{
				try {
				int xDest=dep.getX();
				int yDest=dep.getY();
				
				if(p.xyBelongsToLine(x, y, xDest, yDest))
					return true;
				}catch(Exception e){}
				dep = dep.getProx();
			}
			p = p.getProx();
		}
		return false;
	}
	public CDCM_Vertice verticeDoPontoSobreSeta(int x, int y)
	{
		CDCM_Vertice p=getInicio();
		
		while(p!=null)
		{
			CDCM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
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
	public CDCM_VerticeDependente VerticeDependenteDoPontoSobreSeta(int x, int y)
	{
		CDCM_Vertice p=getInicio();
		
		while(p!=null)
		{
			CDCM_VerticeDependente dep = p.getVerticeDependenteInicial();
			
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
		CDCM_Vertice v=verticeDoPontoSobreSeta(x, y);
		if(v==null)
			return;
		CDCM_VerticeDependente va=VerticeDependenteDoPontoSobreSeta(x, y);
		if(va==null)
			return;
		new CDCM_CaixaInfoSeta(v, va.getVertice(), win);
	}
	public void editaNodoExistente(CDCM_Vertice n)
	{
		CDCM_CaixaInfo CI = new CDCM_CaixaInfo(n, win);
		String coreOrigem = CI.getCoreOrigem();
		String coreDestino = CI.getCoreDestino();
		long phits = CI.getPhits();
		int computacao = CI.getComputacao();
		if(coreOrigem!=null && coreDestino!=null && phits>0)
		{
			verticeSelecionado.setCoreOrigem(coreOrigem);
			verticeSelecionado.setCoreDestino(coreDestino);
			verticeSelecionado.setPhits(phits);
			verticeSelecionado.setComputacao(computacao);
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
		CDCM_Vertice p = getInicio();

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
		CDCM_Vertice p = getInicio();

		while(p!=null)
		{
			if(p.getID()!=CDCM_Grafo.START && p.getID()!=CDCM_Grafo.END)
				nodos = nodos.concat("\n " + p.formataStringID() + " " + p.getCoreOrigem() + " - " + 
									p.getCoreDestino() + " " + p.getPhits()+ " : " + p.getComputacao());
			p = p.getProx();
		}
		return nodos;
	}
	public String stringArestas()
	{
		String s = "";
		CDCM_Vertice p = getInicio();
		
		while(p!=null)
		{
			CDCM_VerticeDependente q = p.getVerticeDependenteInicial();
			
			s = s.concat(" " + p.formataStringID());
			while(q!=null)
			{
				s = s.concat(" " + q.getVertice().formataStringID());
				q = q.getProx();
			}
			s = s.concat("\n");
			p = p.getProx();
		}
		return s;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 Calcula o maior caminho computa��o
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	/
	public CDCM_ListaComputacao getListaComputacao()
	{
		return listaComputacao;
	}
	public CDCM_ListaComunicacao getListaComunicacao()
	{
		return listaComunicacao;
	}
	public CDCM_ListaComunicacaoComComputacao getComunicacaoComComputacao()
	{
		return listaComunicacaoComComputacao;
	}
	public void executaAnaliseTemporal()
	{
		listaComputacao = new CDCM_ListaComputacao(this);
		listaComputacao.executaAnaliseTemporal();

		listaComunicacao = new CDCM_ListaComunicacao(this);
		listaComunicacao.executaAnaliseTemporal();

		listaComunicacaoComComputacao = new CDCM_ListaComunicacaoComComputacao(this);
		listaComunicacaoComComputacao.executaAnaliseTemporal();
	}
	public void removeAnaliseTemporal()
	{		
		listaComputacao = null;
		listaComunicacao = null;
		listaComunicacaoComComputacao = null;
	}
}
