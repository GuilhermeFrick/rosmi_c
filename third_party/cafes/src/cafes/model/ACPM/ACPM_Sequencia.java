package cafes.model.ACPM;

import java.awt.*;
import cafes.common.*;
import cafes.model.CWM.*;

public class ACPM_Sequencia extends ACPM_Grafo
{
	private static final long serialVersionUID = -530750195647156276L;
	private ACPM_MappingCost win;
	private ACPM_DesenhaSequencia dSeq;
	private static ACPM_Tag tagSelecionado;
	private static ACPM_Vertice verticeSelecionado;

	public ACPM_Sequencia(ACPM_DesenhaSequencia dSeq, ACPM_MappingCost win)
	{
		super();
		this.dSeq = dSeq;
		this.win = win;
		tagSelecionado = null;
	}
	public ACPM_DesenhaSequencia getDesenhaSequencia()
	{
		return dSeq;
	}
	public void setDesenhaSequencia(ACPM_DesenhaSequencia DS)
	{
		this.dSeq = DS;
	}
	public void setaVerticeSelecionado(int x, int y)
	{
		ACPM_Tag t = selecionaTag(x, y);

		if(t!=null)
		{
			verticeSelecionado = null;
			tagSelecionado = t;
		}
	}
	public void setaTagSelecionada(int x, int y)
	{
		ACPM_Vertice v = selecionaVertice(x, y);

		if(v!=null)
		{
			tagSelecionado = null;
			verticeSelecionado = v;
		}
	}
	public void moveNodoSequencia(int x, int y)
	{
		try
		{
			ACPM_Tag TagAux = selecionaTag(x, y);
			if(TagAux==null && (x>=1 && y>=1) && tagSelecionado!=null)
			{
				tagSelecionado.setX(x);
				tagSelecionado.setY(y);
				dSeq.update();
				dSeq.setNaoSalvo();
			}
			ACPM_Vertice VertAux = selecionaVertice(x, y);
			if(VertAux==null && (x>=1 && y>=1) && verticeSelecionado!=null)
			{
				verticeSelecionado.setX(x);
				verticeSelecionado.setY(y);
				dSeq.update();
				dSeq.setNaoSalvo();
			}
		}
		catch(Exception e)
		{
			System.out.println("PROBLEMAS 7!");
		}
	}
	public void insereTagStartGrafo()
	{
		ACPM_Tag startV = new ACPM_Tag(win.getIniX()+2*ACPM_CirculoVertice.getRaio(), ACPM_CirculoVertice.getRaio(), ACPM_Grafo.START);
		insereTagNoGrafo(startV);
	}
	public void insereTagEndGrafo()
	{
		ACPM_Tag endV = new ACPM_Tag(win.getIniX()+2*ACPM_CirculoVertice.getRaio(), win.getFimY()-2*ACPM_CirculoVertice.getRaio(), ACPM_Grafo.END);
		insereTagNoGrafo(endV);
	}
// Método que verifica se deve ser inserido um nodo na lista
	public void insereSequencia(int x, int y)
	{
		ACPM_Tag tagAux = selecionaTag(x, y);				// Se clicou em cima de uma Tag
		ACPM_Vertice verticeAux = selecionaVertice(x, y);	// Se clicou em cima de um vértice

		if(tagAux==null && verticeAux==null)
		{
			if(x>=1 && y>=1)
			{
				if(tagSelecionado!=null && tagSelecionado.getTag()!=ACPM_Grafo.START && tagSelecionado.getTag()!=ACPM_Grafo.END)
				{
					ACPM_CaixaInfo CI = new ACPM_CaixaInfo(win);
					String strCoreOrigem = CI.getCoreOrigem();
					String strCoreDestino = CI.getCoreDestino();
					long phits = CI.getPhits();

					if(strCoreOrigem!=null && strCoreDestino!=null && phits>0)
					{
						ACPM_Vertice vertice = new ACPM_Vertice(x, y, strCoreOrigem, strCoreDestino, phits);
						tagSelecionado.insereNaListaDeVertices(vertice);
						dSeq.setNaoSalvo();
					}
				}
				else
				{
					ACPM_Tag t = new ACPM_Tag(x, y, getAndIncTagGlobal());
					insereTagNoGrafo(t);
					tagSelecionado = t;
				}
			}
		}
		else
		{
			if(tagAux==tagSelecionado)
				return;
			if(verticeAux==verticeSelecionado)
				return;
/*
			if(tagSelecionado!=null && tagSelecionado!=tagAux)
			{
				if(tagSelecionado.jaEstaNaListaDeDependentes(auxiliar))
					tagSelecionado.deletaVerticeDependente(auxiliar);
				tagSelecionado.insereNaListaDeDependentes(new ACPM_VerticeDependente(auxiliar));
				dSeq.setNaoSalvo();
			}
*/
		}
	}
	// Método que verifica se existe a tag desejada
	public ACPM_Tag encontraTagNoGrafo(int tag)
	{
		ACPM_Tag p=getInicio();

		while(p!=null)
		{
			if(p.getTag()==tag)
				return p;
			p = p.getProx();
		}
		return null;
	}
	// Método que verifica se existe algum vertice com o tag desejado
	public ACPM_Vertice encontraVerticeNoGrafo(ACPM_Tag p, String coreOrigem)
	{
		ACPM_Vertice v = p.getVerticeInicial();
		while(v!=null)
		{
			if(v.getCoreOrigem().equals(coreOrigem))
				return v;
			v = v.getProx();
		}
		return null;
	}
	// Método que verifica se existe algum nodo apontado na posição (x, y).
	public ACPM_Tag selecionaTag(int x, int y)
	{
		ACPM_Tag p=getInicio();

		while(p!=null)
		{
			if(p.acessouCirculo(x, y))
				return p;
			p = p.getProx();
		}
		return null;
	}
	public ACPM_Tag getTagSelecionada()
	{
		return tagSelecionado;
	}
	// Método que verifica se existe algum nodo apontado na posição (x, y).
	public ACPM_Vertice selecionaVertice(int x, int y)
	{
		ACPM_Tag p = getInicio();
		while(p!=null)
		{
			if(p.getTag()==ACPM_Grafo.END || p.getTag()==ACPM_Grafo.START)
			{
				p = p.getProx();
				continue;
			}
			ACPM_Vertice v = p.getVerticeInicial();
			while(v!=null)
			{
				if(v.acessouCirculo(x, y))
					return v;
				v = v.getProx();
			}
			p = p.getProx();
		}
		return null;
	}
	public ACPM_Vertice getVerticeSelecionado()
	{
		return verticeSelecionado;
	}
	//Médoto para exibição do grafo
	public void exibeNodos(Graphics g)
	{
		ACPM_Tag p = getInicio();
		ACPM_Tag pAnt = null;
		while(p!=null)
		{
			boolean paraCor;

			if(p==tagSelecionado)
				paraCor = true;
			else
				paraCor = false;
			p.desenhaCirculo(g, paraCor);
			if(pAnt!=null)
				exibeAresta(g, pAnt, p);
			if(p.getTag()==ACPM_Grafo.END || p.getTag()==ACPM_Grafo.START)
			{
				pAnt = p;
				p = p.getProx();
				continue;
			}
			ACPM_Vertice v = p.getVerticeInicial();
			while(v!=null)
			{
				v.desenhaCirculo(g, paraCor);
				exibeAresta(g, p, v);
				v = v.getProx();
			}
			pAnt = p;
			p = p.getProx();
		}
	}
	public void exibeAresta(Graphics g, ACPM_Tag t1, ACPM_Tag t2)
	{
		Circulo.arrowBetweenCircles(g, t1.getX(), t1.getY(), t2.getX(), t2.getY());
	}
	public void exibeAresta(Graphics g, ACPM_Tag t, ACPM_Vertice v)
	{
		Circulo.arrowBetweenCircles(g, t.getX(), t.getY(), v.getX(), v.getY());
	}
	public void deleta()
	{
		super.deleta();
		tagSelecionado = null;
	}
	public void deletaTag(ACPM_Tag tagParaDeletar)
	{
		super.deletaTag(tagParaDeletar);
		tagSelecionado = null;
	}
	public void deletaVertice(ACPM_Vertice verticeParaDeletar)
	{
		// Remove da lista de vértices
		ACPM_Tag p = getInicio();
		while(p!=null)
		{
			if(p.getTag()==ACPM_Grafo.END || p.getTag()==ACPM_Grafo.START)
			{
				p = p.getProx();
				continue;
			}
			p.deletaVertice(verticeParaDeletar);
			p = p.getProx();
		}
	}
	public void editaNodoExistente(ACPM_Vertice n)
	{
		ACPM_CaixaInfo CI = new ACPM_CaixaInfo(n, win);
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
	public void ACPM2CWM(CWM_Sequencia seq)
	{
		int coluna=CWM_Circulo.getRaio()*2, linha=CWM_Circulo.getRaio()*2;

		ACPM_Tag p=getInicio();
		while(p!=null)
		{
			if(p.getTag()==ACPM_Grafo.END || p.getTag()==ACPM_Grafo.START)
			{
				p = p.getProx();
				continue;
			}
			ACPM_Vertice v = p.getVerticeInicial();
			while(v!=null)
			{
				long phits = v.getPhits();
				if(phits>0)
				{
					String coreOrigem = v.getCoreOrigem();
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
					String coreDestino = v.getCoreDestino();
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
				v = v.getProx();
			}
			p = p.getProx();
		}
	}
	public String stringLayout()
	{
		String str = "";
		ACPM_Tag p = getInicio();

		while(p!=null)
		{
			str = str.concat("\n " + p.formataStringTag() + " " + p.getX() + " " + p.getY());
			p = p.getProx();
		}
		return str;
	}
	public String stringVertices()
	{
		String nodos = "";
		ACPM_Tag p = getInicio();

		while(p!=null)
		{
			if(p.getTag()==ACPM_Grafo.END || p.getTag()==ACPM_Grafo.START)
			{
				p = p.getProx();
				continue;
			}
			ACPM_Vertice v = p.getVerticeInicial();
			while(v!=null)
			{
				nodos = nodos.concat("\n " + p.formataStringTag() + "\t" + v.getCoreOrigem() + " - " + v.getCoreDestino()+ "\t" + v.getPhits());
				nodos = nodos.concat("\t: " + v.getX() + "\t" + v.getY());
				v = v.getProx();
			}
			p = p.getProx();
		}
		return nodos;
	}
}
