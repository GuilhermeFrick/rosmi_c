package cafes.model.CDM;

import java.util.Vector;

public class CDM_Grafo implements java.io.Serializable
{
	private static final long serialVersionUID = -1427963499322893486L;
	public static final int START = -1;
	public static final int END = -2;
	private CDM_Vertice inicio, fim;
	private CDM_Ordenado CDL;
	
	public CDM_Grafo()
	{
		inicio = null;
		fim = null;
		CDL = null;
	}
	// Insere um nodo sempre ao final da Grafo. Com exceção ao vertice especial START
	public void insereVerticeGrafo(CDM_Vertice n)
	{
		switch(n.getID())
		{
			case CDM_Grafo.START:
				n.setProx(inicio);
				inicio = n;
				if(fim==null)
					fim = n;
				return;

			case CDM_Grafo.END:
			default:
				if(inicio==null)
					inicio = n;
				else
					fim.setProx(n);			// insere o nodo
				fim = n;					// atualiza o ponteiro fim
		}
	}
	public CDM_Vertice getInicio()
	{
		return inicio;
	}
	public CDM_Vertice getFim()
	{
		return fim;
	}
	public void setInicio(CDM_Vertice inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(CDM_Vertice fim)
	{
		this.fim = fim;
	}
	public int getNumeroCores()
	{
		int numeroCores = 0;
		Vector<String> v = new Vector<String>(10, 2);
		
		CDM_Vertice p = inicio;
		while(p!=null)
		{
			if(p.getID()==CDM_Grafo.END || p.getID()==CDM_Grafo.START)
			{
				p = p.getProx();
				continue;
			}
			boolean achouCoreOrigem=false, achouCoreDestino=false;
			String coreOrigem = new String(p.getCoreOrigem());
			String coreDestino = new String(p.getCoreDestino());
			
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
			p = p.getProx();
		}
		return numeroCores;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 ASAP
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	/
		public void executaASAPeCriaListaDeNiveis()
		{
			CDL = new CDM_Ordenado(this);
			CDL.executaASAPeCriaListaDeNiveis();
		}
		public CDM_Ordenado_ListaVertices procuraVerticeListaVertices(CDM_Vertice vertice)
		{
			return CDL.procuraVerticeListaVertices(vertice);
		}
		public CDM_Ordenado_VetorNiveis [] getVetorNiveis()
		{
			return CDL.getVetorNiveis();
		}
		public long executaCDM(CDM_NoC noc, boolean comContencao)
		{
			return CDL.executaCDM(noc, comContencao);
		}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void ExibeVertices()
	{
		CDM_Vertice p=inicio;
	
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("---------------------------------------------------------------------------");
	}
}
