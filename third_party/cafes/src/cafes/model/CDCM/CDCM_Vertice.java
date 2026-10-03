package cafes.model.CDCM;

import javax.swing.JOptionPane;

class CDCM_Vertice extends CDCM_Circulo
{
	private static final long serialVersionUID = -8366684338934877512L;
	public static int id = 0;								// Identificador do vértice

	private CDCM_VerticeDependente verticeDependenteInicial;// Aponta para o primeiro vértices da lista de dependente ao atual
	private CDCM_VerticeDependente verticeDependenteFinal;	// Aponta para o último vértices da lista de dependente ao atual
	private CDCM_Vertice prox;								// Monta o grafo através de uma lista de vértices
	private int nivel;										// Indica quantos níveis precedências existem. Somente é valido após o escalonamento ASAP
	private long cicloInicial, cicloFinal;					// Marca o início e o fim do envio da mensagem na infraestrutura de comunicação 
	private long cicloFinalLocal;							// Marca a saída da mensagem no núcleo local (cicloInicial < cicloFinalLocal < cicloFinal)  
	
	public CDCM_Vertice(int x, int y, String origem, String destino, long phits, int computacao)
	{
		super(x, y, new CDCM_InfoNodo(origem, destino, phits, CDCM_Vertice.id, computacao));
		CDCM_Vertice.id++;
		cdmInicio();
	}
	public CDCM_Vertice(int x, int y, int id, String origem, String destino, long phits, int computacao)
	{
		super(x, y, new CDCM_InfoNodo(origem, destino, phits, id, computacao));
		if(id < CDCM_Vertice.id)
		{
			JOptionPane.showMessageDialog(null, "Invalid vertex id (" + id + "). Probably less than previous one ", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		CDCM_Vertice.id = id;
		cdmInicio();
	}
	public CDCM_Vertice(CDCM_Vertice p)
	{
		super(p.getX(), p.getY(), p.getInf());
		cdmInicio();
	}
	public CDCM_Vertice(int x, int y, int id)
	{
		super(x, y, id);
		cdmInicio();
	}
	public CDCM_Vertice(int id)
	{
		super(id);
		cdmInicio();
	}
	public void cdmInicio()
	{
		this.verticeDependenteInicial = null;
		this.verticeDependenteFinal = null;
		this.prox = null;
		resetCiclos();
	}
	public void resetCiclos()
	{
		this.cicloInicial = Long.MIN_VALUE;
		this.cicloFinal = Long.MAX_VALUE;
		this.cicloFinalLocal = Long.MAX_VALUE;
	}
	public void setCicloInicial(long ciclo) { cicloInicial = ciclo;	}
	public long getCicloInicial() {	return cicloInicial; }
	public void setCicloFinalLocal(long ciclo) { cicloFinalLocal = ciclo; }
	public long getCicloFinalLocal() { return cicloFinalLocal; }
	public void setCicloFinal(long ciclo) { cicloFinal = ciclo; }
	public long getCicloFinal() { return cicloFinal; }
	public void setNivel(int nivel) { this.nivel = nivel; }
	public int getNivel() { return nivel; }

	public void insereNaListaDeDependentes(CDCM_VerticeDependente p)
	{
		if(verticeDependenteInicial == null) // Será o primeiro nodo dependente
			verticeDependenteInicial = p;
		else
			verticeDependenteFinal.setProx(p);
		verticeDependenteFinal = p;
	}
	public void deletaVerticeDependente(CDCM_Vertice verticeParaDeletar)
	{
		CDCM_VerticeDependente depAnt = null;
		CDCM_VerticeDependente dep = verticeDependenteInicial;
		
		while(dep != null)
		{
			if(dep.getVertice() == verticeParaDeletar)
			{
				if(dep == verticeDependenteInicial)
					verticeDependenteInicial = dep.getProx();
				if(dep.getProx()==null)
				{
					if(depAnt != null)
						depAnt.setProx(null);
					verticeDependenteFinal = depAnt;
				}
				else
				{
					if(depAnt!=null)
						depAnt.setProx(dep.getProx());
				}
			}
			depAnt = dep;
			dep = dep.getProx();
		}
	}
	public boolean jaEstaNaListaDeDependentes(CDCM_Vertice vertex)
	{
		CDCM_VerticeDependente p = verticeDependenteInicial;
		
		while(p != null)
		{
			if(p.getVertice() == vertex)
				return true;
			p = p.getProx();
		}
		return false;
	}
	public CDCM_Vertice getProx() { return prox; }
	public void setProx(CDCM_Vertice prox) { this.prox = prox; }
	public CDCM_VerticeDependente getVerticeDependenteInicial() { return verticeDependenteInicial; }
	public void setVerticeDependenteInicial(CDCM_VerticeDependente verticeDependenteInicial) { this.verticeDependenteInicial = verticeDependenteInicial; }
	public CDCM_VerticeDependente getVerticeDependenteFinal() { return verticeDependenteFinal; }
	public void setVerticeDependenteFinal(CDCM_VerticeDependente verticeDependenteFinal) { this.verticeDependenteFinal = verticeDependenteFinal; }
	public String formataStringID()
	{
		int ident = getID();
		
		switch(ident)
		{
			case CDCM_Grafo.START:
				return "START";
			
			case CDCM_Grafo.END:
				return "END";
		}
		return String.valueOf(ident);
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		System.out.print("Nivel: " + nivel);
		getInf().exibe();
		System.out.println();
	}
}