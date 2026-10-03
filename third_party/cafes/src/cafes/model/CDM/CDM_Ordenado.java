package cafes.model.CDM;

public class CDM_Ordenado implements java.io.Serializable
{
	private static final long serialVersionUID = 4160141221940163474L;
	private int numeroDeNiveis;		// Número de níveis após o escalonamento ASAP
	private CDM_Ordenado_VetorNiveis vetorNiveis[];
	private CDM_Grafo grafo;
	
	public CDM_Ordenado(CDM_Grafo grafo)
	{
		this.grafo = grafo;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// ASAP
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void executaASAPeCriaListaDeNiveis()
	{
		resetCiclos();
		executaASAP();
		criaListaDeNiveis();
		exibeListaNiveis();
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Executa o escalonamento ASAP
	private void executaASAP()
	{
		ASAP(grafo.getInicio(), 1);
	}
// Escalonamento As Soon As Possible
	private void ASAP(CDM_Vertice inicio, int nivel)
	{
		CDM_VerticeDependente p=inicio.getVerticeDependenteInicial();

		while(p!=null)
		{
			CDM_Vertice v = p.getVertice();
			
			if(v.getNivel()<nivel)
				v.setNivel(nivel);
			ASAP(v, nivel+1); // Escalona todos os adjacentes com um nivel abaixo
			p = p.getProx();
		}
		if(numeroDeNiveis<nivel)
			numeroDeNiveis = nivel;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private void criaListaDeNiveis()
	{
		vetorNiveis = new CDM_Ordenado_VetorNiveis[numeroDeNiveis];
		for(int i=0; i<vetorNiveis.length; i++)
			vetorNiveis[i] = new CDM_Ordenado_VetorNiveis(i);
		CDM_Vertice p = grafo.getInicio();
		while(p!=null)
		{
			CDM_Ordenado_ListaVertices lv = new CDM_Ordenado_ListaVertices(p); 
			vetorNiveis[p.getNivel()].insereListaVertices(lv);
			p = p.getProx();
		}
		criaListaDependencia(grafo.getInicio(), null);
	}
	private void criaListaDependencia(CDM_Vertice vertice, CDM_Vertice dependencia)
	{
		if(vertice==null)  // Chegou até o final por um caminho
			return;
		insereVerticeNaListaDependencia(vertice, dependencia);
		CDM_VerticeDependente k=vertice.getVerticeDependenteInicial();
		while(k!=null)
		{
			criaListaDependencia(k.getVertice(), vertice);
			k = k.getProx();
		}
	}
	private void insereVerticeNaListaDependencia(CDM_Vertice vertice, CDM_Vertice dependencia)
	{
		if(vertice==null)
			return;
		if(dependencia==null)
			return;
		if(dependencia.getID()==CDM_Grafo.START) // Apenas para não incluir o vértice START em todas as dependências
			return;
		CDM_Ordenado_ListaVertices lv = procuraVerticeListaVertices(vertice);
		if(lv==null)
			return;
		lv.insereVerticeNaListaDependencia(dependencia);
		CDM_Ordenado_ListaVertices lvd = procuraVerticeListaVertices(dependencia);
		if(lvd==null)
			return;
		lv.insereListaDependenciaNaListaDependencia(lvd);
	}
	public CDM_Ordenado_ListaVertices procuraVerticeListaVertices(CDM_Vertice vertice)
	{
		for(int i=0; i<vetorNiveis.length; i++)
		{
			CDM_Ordenado_ListaVertices lv = vetorNiveis[i].getInicio();
			
			while(lv!=null)
			{
				if(lv.getVertice()==vertice)
					return lv;
				lv = lv.getProx();
			}
		}
		return null;
	}
	public CDM_Ordenado_VetorNiveis [] getVetorNiveis()
	{
		return vetorNiveis;
	}
	private void resetCiclos()
	{
		CDM_Vertice p=grafo.getInicio();
		
		numeroDeNiveis = 0;
		while(p!=null)
		{
			p.resetCiclos();
			p = p.getProx();
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public long executaCDM(CDM_NoC noc, boolean comContencao)
	{
		noc.limpaEnergiaDinamica();
		noc.setComSemContencao(comContencao);
		long cicloAuxiliar;
		long maiorCiclo = 0;

		for(int i=1; i<vetorNiveis.length-1; i++) // START e END não precisam ser executados
		{
			CDM_Ordenado_ListaVertices p = vetorNiveis[i].getInicio();
			
			while(p!=null)
			{
				p.getVertice().resetCiclos();
				cicloAuxiliar = p.executaVerticeNaNoC(noc);
				if(cicloAuxiliar>maiorCiclo)
					maiorCiclo = cicloAuxiliar;
				p = p.getProx();
			}
		}
		return maiorCiclo;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibeListaNiveis()
	{
		System.out.println();
		System.out.println("+-------+-----+----------------------+------------+-----------+---------------------------------------------------------------------+");
		System.out.println("|  ID   |Nível|   Origem -> Destino  | Computacao |   Phits   | Lista de dependência: ID(origem->destino), ...                      |");
		System.out.println("+-------+-----+----------------------+------------+-----------+---------------------------------------------------------------------+");
		for(int i=0; i<vetorNiveis.length; i++)
		{
			CDM_Ordenado_ListaVertices lv = vetorNiveis[i].getInicio();
			
			while(lv!=null)
			{
				lv.exibe();
				lv = lv.getProx();
			}
		}
		System.out.println("+-------+-----+----------------------+------------+-----------+---------------------------------------------------------------------+");
		System.out.println();
	}
}
