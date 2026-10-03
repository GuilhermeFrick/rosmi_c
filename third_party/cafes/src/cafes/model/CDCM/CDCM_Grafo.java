package cafes.model.CDCM;

import java.util.Vector;

public class CDCM_Grafo implements java.io.Serializable
{
	private static final long serialVersionUID = 443432607627393031L;
	public static final int START = -1;
	public static final int END = -2;
	private CDCM_Vertice inicio, fim;
	private CDCM_Ordenado CDL;
	
	public CDCM_Grafo()
	{
		inicio = null;
		fim = null;
		CDL = null;
	}
	// Insere um nodo sempre ao final da Grafo. Com exceção ao vertice especial START
	public void insereVerticeGrafo(CDCM_Vertice n)
	{
		switch(n.getID())
		{
			case CDCM_Grafo.START:
				n.setProx(inicio);
				inicio = n;
				if(fim == null)
					fim = n;
				return;

			case CDCM_Grafo.END:
			default:
				if(inicio == null)
					inicio = n;
				else
					fim.setProx(n);			// insere o nodo
				fim = n;					// atualiza o ponteiro fim
		}
	}
// O vértice END nem sempre é o último, pois o usuário pode colocar na ordem que desejar
	public CDCM_Vertice getEndVertex()
	{
		CDCM_Vertice p = inicio;
		
		while(p != null)
		{
			if(p.getID() == CDCM_Grafo.END)
				return p;
			p = p.getProx();
		}
		return null;
	}
	public CDCM_Vertice getInicio()
	{
		return inicio;
	}
	public CDCM_Vertice getFim()
	{
		return fim;
	}
	public void setInicio(CDCM_Vertice inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(CDCM_Vertice fim)
	{
		this.fim = fim;
	}
	public int getNumeroCores()
	{
		int numeroCores = 0;
		Vector<String> v = new Vector<String>(10, 2);
		CDCM_Vertice p = inicio;

		while(p != null)
		{
			if(p.getID() == CDCM_Grafo.END || p.getID() == CDCM_Grafo.START)
			{
				p = p.getProx();
				continue;
			}
			String coreOrigem = new String(p.getCoreOrigem());
			String coreDestino = new String(p.getCoreDestino());
			if(!v.contains(coreOrigem))
			{
				numeroCores++;
				v.add(coreOrigem);
			}
			if(!v.contains(coreDestino))
			{
				numeroCores++;
				v.add(coreDestino);
			}
			p = p.getProx();
		}
		return numeroCores;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// ASAP
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void executaASAPeCriaListaDeNiveis(boolean verbose)
	{
		CDL = new CDCM_Ordenado(this);
		CDL.executaASAPeCriaListaDeNiveis(verbose);
	}
	public void exibeListaNiveis()
	{
		CDL.exibeListaNiveis();
	}
	public CDCM_Ordenado_ListaVertices procuraVerticeListaVertices(CDCM_Vertice vertice)
	{
		return CDL.procuraVerticeListaVertices(vertice);
	}
	public CDCM_Ordenado_VetorNiveis [] getVetorNiveis()
	{
		return CDL.getVetorNiveis();
	}
	public long executaCDCM(CDCM_NoC noc)
	{
		return CDL.executaCDCM(noc);
	}
	public void percorreListaDeNiveisGeraVHDL(CDCM_WindowNoC win)
	{		
		CDL.percorreListaDeNiveisGeraVHDL(win);
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void ExibeVertices()
	{
		CDCM_Vertice p = inicio;
	
		while(p != null)
		{
			p.exibe();
			p = p.getProx();
		}
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}
}
