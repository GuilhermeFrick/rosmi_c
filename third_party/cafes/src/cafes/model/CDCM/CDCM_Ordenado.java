package cafes.model.CDCM;

public class CDCM_Ordenado implements java.io.Serializable
{
	private static final long serialVersionUID = 3545965392664435431L;
	private int numeroDeNiveis;		// N�mero de n�veis ap�s o escalonamento ASAP
	private CDCM_Ordenado_VetorNiveis vetorNiveis[];
	private CDCM_Grafo grafo;
	
	public CDCM_Ordenado(CDCM_Grafo grafo)
	{
		this.grafo = grafo;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	 VHDL
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void percorreListaDeNiveisGeraVHDL(CDCM_WindowNoC win)
	{
		CDCM_VHDL aux = new CDCM_VHDL(this, win); 
		
		aux.percorreListaDeNiveisGeraVHDL();
	}
	public CDCM_Grafo getGrafo()
	{
		return grafo;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// ASAP
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void executaASAPeCriaListaDeNiveis(boolean verbose)
	{
		resetCiclos();
		executaASAP();
		criaListaDeNiveis();
		if (verbose)
			exibeListaNiveis();
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Executa o escalonamento ASAP
	private void executaASAP()
	{
		ASAP(grafo.getInicio(), 1);
	}
// Escalonamento As Soon As Possible
	private void ASAP(CDCM_Vertice inicio, int nivel)
	{
		CDCM_VerticeDependente p=inicio.getVerticeDependenteInicial();

		while(p!=null)
		{
			CDCM_Vertice v = p.getVertice();
			
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
		vetorNiveis = new CDCM_Ordenado_VetorNiveis[numeroDeNiveis];
		for(int i = 0; i < vetorNiveis.length; i++)
			vetorNiveis[i] = new CDCM_Ordenado_VetorNiveis(i);
		CDCM_Vertice p = grafo.getInicio();
		while(p != null)
		{
			CDCM_Ordenado_ListaVertices lv = new CDCM_Ordenado_ListaVertices(p); 
			vetorNiveis[p.getNivel()].insereListaVertices(lv);
			p = p.getProx();
		}
		criaListaDependencia(grafo.getInicio(), null);
	}
	private void criaListaDependencia(CDCM_Vertice vertice, CDCM_Vertice dependencia)
	{
		if(vertice == null)  // Chegou at� o final por um caminho
			return;
		insereVerticeNaListaDependencia(vertice, dependencia);
		CDCM_VerticeDependente k = vertice.getVerticeDependenteInicial();
		while(k != null)
		{
			criaListaDependencia(k.getVertice(), vertice);
			k = k.getProx();
		}
	}
	private void insereVerticeNaListaDependencia(CDCM_Vertice vertice, CDCM_Vertice dependencia)
	{
		if(vertice == null)
			return;
		if(dependencia == null)
			return;
		if(dependencia.getID() == CDCM_Grafo.START) // Apenas para n�o incluir o v�rtice START
			return;
		CDCM_Ordenado_ListaVertices lv = procuraVerticeListaVertices(vertice);
		if(lv == null)
			return;
		lv.insereVerticeNaListaDependencia(dependencia);
	}
	public CDCM_Ordenado_ListaVertices procuraVerticeListaVertices(CDCM_Vertice vertice)
	{
		for(int i = 0; i < vetorNiveis.length; i++)
		{
			CDCM_Ordenado_ListaVertices lv = vetorNiveis[i].getInicio();
			
			while(lv != null)
			{
				if(lv.getVertice() == vertice)
					return lv;
				lv = lv.getProx();
			}
		}
		return null;
	}
	public CDCM_Ordenado_VetorNiveis [] getVetorNiveis()
	{
		return vetorNiveis;
	}
	private void resetCiclos()
	{
		CDCM_Vertice p=grafo.getInicio();
		
		numeroDeNiveis = 0;
		while(p!=null)
		{
			p.resetCiclos();
			p = p.getProx();
		}
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public long executaCDCM(CDCM_NoC noc)
	{
		executaASAPeCriaListaDeNiveis(false);
		noc.limpaEnergiaDinamica();
		long cicloAuxiliar;
		long maiorCiclo = 0;
		
		for(int i = 1; i < vetorNiveis.length - 1; i++) // START e END n�o precisam ser executados
		{
			CDCM_Ordenado_ListaVertices p = vetorNiveis[i].getInicio();
			
			while(p != null)
			{
				p.getVertice().resetCiclos();
				p = p.getProx();
			}
		}
		for(int i = 1; i < vetorNiveis.length - 1; i++) // START e END n�o precisam ser executados
		{
			CDCM_Ordenado_ListaVertices p;
			
			vetorNiveis[i].reordenaListaPorMenorTempo();
			p = vetorNiveis[i].getInicio();
//			System.out.println(vetorNiveis[i]);
			while(p != null)
			{
				cicloAuxiliar = p.executaVerticeNaNoC(noc);
				if(cicloAuxiliar > maiorCiclo)
					maiorCiclo = cicloAuxiliar;
				p = p.getProx();
			}
		}
		return maiorCiclo;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//DEPURACAO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibeListaNiveis()
	{
		System.out.println();
		System.out.println("+-------+-----+----------------------+------------+-----------+---------------------------------------------------------------------+");
		System.out.println("|  ID   |Nivel|   Origem -> Destino  | Computacao |   Phits   | Lista de depend�ncia: ID(origem->destino), ...                      |");
		System.out.println("+-------+-----+----------------------+------------+-----------+---------------------------------------------------------------------+");
		for(int i=0; i<vetorNiveis.length; i++)
		{
			CDCM_Ordenado_ListaVertices lv = vetorNiveis[i].getInicio();
			
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
