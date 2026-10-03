package cafes.model.CDM;

import cafes.common.*;

public class CDM_Ordenado_ListaVertices implements java.io.Serializable
{
	private static final long serialVersionUID = -747057440524431526L;
	private CDM_Vertice vertice;		   // Um dos vértices do nível
	private CDM_Ordenado_Vertice inicio, fim;  // Lista de depêndencias do vértice
	private CDM_Ordenado_ListaVertices prox;

	public CDM_Ordenado_ListaVertices(CDM_Vertice vertice)
	{
		inicio = null;
		fim = null;
		prox = null;
		this.vertice = vertice;
	}
	public CDM_Vertice getVertice()
	{
		return vertice;
	}
	public void setVertice(CDM_Vertice vertice)
	{
		this.vertice = vertice;
	}
	private boolean verticeEstaNaListaDependecia(CDM_Ordenado_Vertice verticeOdenado)
	{
		CDM_Ordenado_Vertice p = inicio;
		while(p!=null)
		{
			if(p.getVertice()==verticeOdenado.getVertice())
				return true;
			p = p.getProx();
		}
		return false;
	}
	public void insereVerticeNaListaDependencia(CDM_Vertice v)
	{
		insereVerticeNaListaDependencia(new CDM_Ordenado_Vertice(v));
	}
	public void insereVerticeNaListaDependencia(CDM_Ordenado_Vertice verticeOdenado)
	{
		if(verticeEstaNaListaDependecia(verticeOdenado))
			return;
		CDM_Ordenado_Vertice p = new CDM_Ordenado_Vertice(verticeOdenado);
		if(inicio==null)
			inicio = p;
		else
			fim.setProx(p);		// insere o nodo
		fim = p;				// atualiza o ponteiro fim
	}
	public void insereListaDependenciaNaListaDependencia(CDM_Ordenado_ListaVertices lvd)
	{
		CDM_Ordenado_Vertice p = lvd.inicio;
		while(p!=null)
		{
			insereVerticeNaListaDependencia(new CDM_Ordenado_Vertice(p));
			p = p.getProx();
		}
	}
	public void atribuiCicloInicial()
	{
		CDM_Ordenado_Vertice p = inicio;
		long cicloInicial = vertice.getCicloInicial();
		while(p!=null)
		{
			long cicloFinal;
			
			if(vertice.getCoreOrigem().equals(p.getVertice().getCoreOrigem())) // Se dependência for no envio
				cicloFinal = p.getVertice().getCicloFinalLocal();
			else // Se dependência for no recebimento
				cicloFinal = p.getVertice().getCicloFinal();
			
			if(cicloInicial<cicloFinal)
				cicloInicial = cicloFinal;
			p = p.getProx();
		}
		if(cicloInicial==Integer.MIN_VALUE) // Ainda não tem dependentes
			cicloInicial = 0;
		vertice.setCicloInicial(cicloInicial+CDM_Vertice.tempoComputacao);
	}
	public long executaVerticeNaNoC(CDM_NoC noc)
	{
		atribuiCicloInicial();
		long cicloFinal = noc.topologia(vertice);
		vertice.setCicloFinal(cicloFinal);
		
		return cicloFinal;
	}
	public CDM_Ordenado_ListaVertices getProx()
	{
		return prox;
	}
	public void setProx(CDM_Ordenado_ListaVertices prox)
	{
		this.prox = prox;
	}
	public CDM_Ordenado_Vertice getInicio()
	{
		return inicio;
	}
	public CDM_Ordenado_Vertice getFim()
	{
		return fim;
	}
	public void setInicio(CDM_Ordenado_Vertice inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(CDM_Ordenado_Vertice fim)
	{
		this.fim = fim;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	DEPURAÇÃO
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	/
	public void exibe()
	{
		if(vertice==null)
			return;
		String listaDependencia = new String("");
		CDM_Ordenado_Vertice p = inicio;
		while(p!=null)
		{
			CDM_Vertice k = p.getVertice();
			if(k!=null)
				listaDependencia = listaDependencia + " " + k.formataStringID() + "(" + k.getCoreOrigem() + "->" + k.getCoreDestino() + "),";
			p = p.getProx();
		}
		String coreOrigem, coreDestino;
		
		if(vertice.getCoreOrigem()==null)
			coreOrigem = new String("null");
		else
			coreOrigem = new String(vertice.getCoreOrigem());
		if(vertice.getCoreDestino()==null)
			coreDestino = new String("null");
		else
			coreDestino = new String(vertice.getCoreDestino());

		String string = "|" + StringFormat.format(vertice.formataStringID(), StringFormat.CENTER, 7) +
						"|" + StringFormat.format(vertice.getNivel(), StringFormat.CENTER, 5) +
						"|" + StringFormat.format(coreOrigem, StringFormat.RIGTH, 9) +
						" -> " + StringFormat.format(coreDestino, StringFormat.LEFT, 9) +
						"|" + StringFormat.format(vertice.getStringPhits(), StringFormat.CENTER, 12) +
						"|" + StringFormat.format(listaDependencia, StringFormat.LEFT, 69) +
						"|";
		System.out.println(string);
	}
}
