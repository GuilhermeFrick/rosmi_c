package cafes.model.ACPM;

import java.util.Vector;
import cafes.model.CDM.*;

public class ACPM_Grafo implements java.io.Serializable
{
	private static final long serialVersionUID = 5231872215589810477L;
	public static final int START = -1;
	public static final int END = -2;
	private ACPM_Tag inicio, fim;
	private int tagGlobal;
	
	public ACPM_Grafo()
	{
		inicio = null;
		fim = null;
		tagGlobal = 0;
	}
	public void deleta()
	{
		inicio = null;
		fim = null;
		tagGlobal = 0;
	}
	// Insere um nodo sempre ao final da Grafo. Com exceção ao vertice especial START
	public void insereTagNoGrafo(ACPM_Tag t)
	{
		switch(t.getTag())
		{
			case ACPM_Grafo.START:  // Tem que ser sempre o início do grafo
				if(inicio==null)
					fim = t;
				t.setProx(inicio);
				inicio = t;
				return;

			case ACPM_Grafo.END:  
				if(inicio==null)  // Primeiro vertice a ser inserido
				{
					t.setProx(null);
					inicio = t;
					fim = t;
					return;
				}
				fim.setProx(t);
				fim = t;
				t.setProx(null);
				return;

			default:
				if(inicio==null)  // Primeiro vertice a ser inserido
				{
					t.setProx(null);
					inicio = t;
					fim = t;
					return;
				}
				if(fim.getTag()==ACPM_Grafo.END) // Inserido após vértice END ser inserido
				{
					ACPM_Tag p = inicio;
					while(p.getProx()!=fim)
						p = p.getProx();
					p.setProx(t);
					t.setProx(fim);
					return;
				}
				fim.setProx(t);
				fim = t;
				t.setProx(null);
		}
	}
	public void deletaTag(ACPM_Tag tagParaDeletar)
	{
		ACPM_Tag p = getInicio();
		while(p.getProx()!=null)
		{
			if(p.getProx().getTag()==ACPM_Grafo.END)
				return;
			if(p.getProx()==tagParaDeletar)
			{
				ACPM_Tag proxima = p.getProx().getProx();
				tagGlobal--;
				p.setProx(proxima);
				subtraiTags(proxima);
				return;
			}
			p = p.getProx();
		}
	}
	public void subtraiTags(ACPM_Tag ini)
	{
		ACPM_Tag p = ini;
		
		while(p.getTag()!=ACPM_Grafo.END)
		{
			p.tag--;
			p = p.getProx();
		}
	}
	public int getAndIncTagGlobal()
	{
		int tag = tagGlobal;
		
		tagGlobal++;
		
		return tag;
	}
	public void atualizaTagGlobal(int novaTag)
	{
		if(tagGlobal<novaTag)
			tagGlobal = novaTag+1;
	}
	public ACPM_Tag getInicio()
	{
		return inicio;
	}
	public ACPM_Tag getFim()
	{
		return fim;
	}
	public void setInicio(ACPM_Tag inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(ACPM_Tag fim)
	{
		this.fim = fim;
	}
	public int getNumeroCores()
	{
		int numeroCores = 0;
		Vector<String> v = new Vector<String>(10, 2);
		
		ACPM_Tag p=getInicio();
		while(p!=null)
		{
			if(p.getTag()==ACPM_Grafo.END || p.getTag()==ACPM_Grafo.START)
			{
				p = p.getProx();
				continue;
			}
			ACPM_Vertice vert = p.getVerticeInicial();
			while(vert!=null)
			{
				boolean achouCoreOrigem=false, achouCoreDestino=false;
				
				String coreOrigem = new String(vert.getCoreOrigem());
				String coreDestino = new String(vert.getCoreDestino());
				
				for(int i=0; i<v.size(); i++)
				{
					if(v.elementAt(i).equals(coreOrigem))
						achouCoreOrigem = true;
					if(v.elementAt(i).equals(coreDestino ))
						achouCoreDestino = true;
				}
				if(achouCoreOrigem==false)
				{
					numeroCores++;
					v.add(coreOrigem);
				}
				if(achouCoreDestino==false)
				{
					numeroCores++;
					v.add(coreDestino);
				}
				vert = vert.getProx();
			}
			p = p.getProx();
		}
		return numeroCores;
	}
	public long executaACPM(ACPM_NoC noc)
	{
		long maxCicloTotal = 0;
		long maxCicloTag;
		noc.limpaEnergiaDinamica();

		ACPM_Tag p = getInicio();
		while(p!=null)
		{
			if(p.getTag()==ACPM_Grafo.END || p.getTag()==ACPM_Grafo.START)
			{
				p = p.getProx();
				continue;
			}
			maxCicloTotal = maxCicloTotal + CDM_Vertice.tempoComputacao;
			maxCicloTag = maxCicloTotal;
			ACPM_Vertice v = p.getVerticeInicial();
			while(v!=null)
			{
				v.setCicloInicial(maxCicloTotal);  // Todos os vértices começam com o ciclo inicial da tag
				long cicloFinal = noc.topologia(p, v);
				v.setCicloFinal(cicloFinal);
				if(maxCicloTag<cicloFinal)
					maxCicloTag = cicloFinal;
				v = v.getProx();
			}
			maxCicloTotal = maxCicloTag; 
			p = p.getProx();
		}
		return maxCicloTotal;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void ExibeVertices()
	{
		ACPM_Tag p=inicio;
	
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}
}
