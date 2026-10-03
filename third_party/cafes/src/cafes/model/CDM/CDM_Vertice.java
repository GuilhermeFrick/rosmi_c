package cafes.model.CDM;

import javax.swing.JOptionPane;

public class CDM_Vertice extends CDM_Circulo
{
	private static final long serialVersionUID = -3726300939175351561L;
	public static int id = 0;								// Identificador do vértice
	public static int tempoComputacao = 1;					// Atribui um tempo de computação qualquer para todos os núcleos, apenas para que a mensagem não saia exatamente quando chegou

	private CDM_VerticeDependente verticeDependenteInicial;	// Aponta para o primeiro vértices da lista de dependente ao atual
	private CDM_VerticeDependente verticeDependenteFinal;	// Aponta para o último vértices da lista de dependente ao atual
	private CDM_Vertice prox;								// Monta o grafo através de uma lista de vértices
	private int nivel;										// Indica quantos níveis precedências existem. Somente é valido após o escalonamento ASAP
	private long cicloInicial, cicloFinal;					// Marca o início e o fim do envio da mensagem na infraestrutura de comunicação 
	private long cicloFinalLocal;							// Marca a saída da mensagem no núcleo local (cicloInicial < cicloFinalLocal < cicloFinal)  

	public CDM_Vertice(int x, int y, String origem, String destino, long phits)
	{
		super(x, y, new CDM_InfoNodo(origem, destino, phits, CDM_Vertice.id));
		CDM_Vertice.id++;
		cdmInicio();
	}
	public CDM_Vertice(int x, int y, int id, String origem, String destino, long phits)
	{
		super(x, y, new CDM_InfoNodo(origem, destino, phits, id));
		if(id<CDM_Vertice.id)
		{
			JOptionPane.showMessageDialog(null, "Invalid vertex id (" + id + "). Probably less than previous one ", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		CDM_Vertice.id = id;
		cdmInicio();
	}
	public CDM_Vertice(CDM_Vertice p)
	{
		super(p.getX(), p.getY(), p.getInf());
		cdmInicio();
	}
	public CDM_Vertice(int x, int y, int id)
	{
		super(x, y, id);
		cdmInicio();
	}
	public CDM_Vertice(int id)
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
		this.cicloInicial = Integer.MIN_VALUE;
		this.cicloFinal = Integer.MAX_VALUE;
		this.cicloFinalLocal = Integer.MAX_VALUE;
	}
	public void setCicloInicial(long ciclo)
	{
		cicloInicial = ciclo;
	}
	public long getCicloInicial()
	{
		return cicloInicial;
	}
	public void setCicloFinalLocal(long ciclo)
	{
		cicloFinalLocal = ciclo;
	}
	public long getCicloFinalLocal()
	{
		return cicloFinalLocal;
	}
	public void setCicloFinal(long ciclo)
	{
		cicloFinal = ciclo;
	}
	public long getCicloFinal()
	{
		return cicloFinal;
	}
	public void setNivel(int nivel)
	{
		this.nivel = nivel;
	}
	public int getNivel()
	{
		return nivel;
	}
	public void insereNaListaDeDependentes(CDM_VerticeDependente p)
	{
		if(verticeDependenteInicial==null) // Será o primeiro nodo dependente
			verticeDependenteInicial = p;
		else
			verticeDependenteFinal.setProx(p);
		verticeDependenteFinal = p;
	}
	public void deletaVerticeDependente(CDM_Vertice verticeParaDeletar)
	{
		CDM_VerticeDependente depAnt = null;
		CDM_VerticeDependente dep = verticeDependenteInicial;
		
		while(dep!=null)
		{
			if(dep.getVertice()==verticeParaDeletar)
			{
				if(dep==verticeDependenteInicial)
					verticeDependenteInicial = dep.getProx();
				if(dep.getProx()==null)
				{
					if(depAnt!=null)
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
	public boolean jaEstaNaListaDeDependentes(CDM_Vertice vertex)
	{
		CDM_VerticeDependente p=verticeDependenteInicial;
		
		while(p!=null)
		{
			if(p.getVertice()==vertex)
				return true;
			p = p.getProx();
		}
		return false;
	}
	public CDM_Vertice getProx()
	{
		return prox;
	}
	public void setProx(CDM_Vertice prox)
	{
		this.prox = prox;
	}
	public CDM_VerticeDependente getVerticeDependenteInicial()
	{
		return verticeDependenteInicial;
	}
	public void setVerticeDependenteInicial(CDM_VerticeDependente verticeDependenteInicial)
	{
		this.verticeDependenteInicial = verticeDependenteInicial;
	}
	public CDM_VerticeDependente getVerticeDependenteFinal()
	{
		return verticeDependenteFinal;
	}
	public void setVerticeDependenteFinal(CDM_VerticeDependente verticeDependenteFinal)
	{
		this.verticeDependenteFinal = verticeDependenteFinal;
	}
	public String formataStringID()
	{
		int ident = getID();
		
		switch(ident)
		{
			case CDM_Grafo.START:
				return new String("START");
			
			case CDM_Grafo.END:
				return new String("END");
		}
		return new String(new Integer(ident).toString());
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