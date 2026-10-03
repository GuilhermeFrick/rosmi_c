package cafes.model.CDM;

import java.io.*;

class CDM_Link implements Serializable
{
	private static final long serialVersionUID = -1221636765158044672L;
	private long totalPhits;	// Totalização de phits do link
	private double energiaPhit;	// Energia que consome um link entre roteadores ou entre roteador e core local
	private CDM_ListaVerticeNoC inicio, fim;

	public CDM_Link(double energiaPhit)
	{
		this.totalPhits = 0;
		this.energiaPhit = energiaPhit;
		inicio = null;
		fim = null;
	}
	public CDM_Link(CDM_Link a)
	{
		totalPhits = a.totalPhits;
		energiaPhit = a.energiaPhit;
		inicio = a.inicio;
		fim = a.fim;
	}
	public long cicloFinalMensagemNaoDependente(CDM_Grafo g, CDM_Vertice vertice, long cicloInicial, long ciclosLink)
	{
		long cicloFinal = cicloInicial;

		CDM_Ordenado_ListaVertices lvd = g.procuraVerticeListaVertices(vertice);
		if(lvd==null)
			return -1;
		CDM_Ordenado_Vertice p=lvd.getInicio();
		while(p!=null)
		{
			CDM_Vertice v = p.getVertice();
			CDM_ListaVerticeNoC lvn = inicio;
			while(lvn!=null)
			{
				if(lvn.getVertice()==v)  // É dependente
					break;
				
				CDM_Vertice vert = g.procuraVerticeListaVertices(lvn.getVertice()).getVertice();
				long cicloAuxiliar;
				if(vertice.getCoreOrigem().equals(vert.getCoreOrigem())) // Se dependência for no envio
					cicloAuxiliar = vert.getCicloFinalLocal();
				else // Se dependência for no recebimento
					cicloAuxiliar = vert.getCicloFinal();
				
				long cicloLido = cicloAuxiliar + ciclosLink;

				if(cicloFinal<cicloLido)
					cicloFinal = cicloLido;
				lvn = lvn.getProx();
			}
			p = p.getProx();
		}
		return cicloFinal;
	}
	public long insereVertice(CDM_Grafo g, CDM_Vertice vertice, long cicloInicial, long ciclosGastos, long ciclosLink, boolean comContencao)
	{
		long cicloFinal;
		if(comContencao)
			cicloInicial = cicloFinalMensagemNaoDependente(g, vertice, cicloInicial, ciclosLink);
		cicloFinal = cicloInicial + ciclosGastos;
		CDM_ListaVerticeNoC listaVertice = new CDM_ListaVerticeNoC(vertice, cicloInicial, cicloFinal);
		if(inicio==null)
			inicio = listaVertice;
		else
			fim.setProx(listaVertice);
		fim = listaVertice;
		return listaVertice.getCicloFinal();
	}
	//elas tem que ser postergadas do tempo da mensagem não dependente com maior tempo.
	public void somaPhits(long phits)
	{
		totalPhits = totalPhits + phits; 
	}
	public void zeraTrafego()
	{
		inicio = null;
		fim = null;
		totalPhits = 0;
	}
	public double energiaLink()
	{
		return totalPhits * energiaPhit;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// DEPURAÇÃO
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		CDM_ListaVerticeNoC p = inicio;
		
		while(p!=null)
		{
			p.exibe();
			p = p.getProx();
		}
	}
}

