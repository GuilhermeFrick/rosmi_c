package cafes.model.CDCM;

import cafes.common.*;

public class CDCM_Ordenado_ListaVertices implements java.io.Serializable
{
	private static final long serialVersionUID = -1063837809218087511L;
	private CDCM_Vertice vertice;		   // Um dos vértices do nível
	private CDCM_Ordenado_Vertice inicio, fim;  // Lista de depêndencias do vértice
	private CDCM_Ordenado_ListaVertices ant, prox;

	public CDCM_Ordenado_ListaVertices(CDCM_Vertice vertice)
	{
		inicio = null;
		fim = null;
		ant = null;
		prox = null;
		this.vertice = vertice;
	}
	public CDCM_Ordenado_ListaVertices(CDCM_Ordenado_ListaVertices v)
	{
		inicio = v.getInicio();
		fim = v.getFim();
		ant = v.getAnt();
		prox = v.getProx();
		this.vertice = v.getVertice();
	}
	public CDCM_Vertice getVertice()
	{
		return vertice;
	}
	public void setVertice(CDCM_Vertice vertice)
	{
		this.vertice = vertice;
	}
	private boolean verticeEstaNaListaDependecia(CDCM_Ordenado_Vertice verticeOdenado)
	{
		CDCM_Ordenado_Vertice p = inicio;
		while(p!=null)
		{
			if(p.getVertice()==verticeOdenado.getVertice())
				return true;
			p = p.getProx();
		}
		return false;
	}
	public void insereVerticeNaListaDependencia(CDCM_Vertice v)
	{
		insereVerticeNaListaDependencia(new CDCM_Ordenado_Vertice(v));
	}
	public void insereVerticeNaListaDependencia(CDCM_Ordenado_Vertice verticeOdenado)
	{
		if(verticeEstaNaListaDependecia(verticeOdenado))
			return;
		CDCM_Ordenado_Vertice p = new CDCM_Ordenado_Vertice(verticeOdenado);
		if(inicio==null)
			inicio = p;
		else
			fim.setProx(p);		// insere o nodo
		fim = p;				// atualiza o ponteiro fim
	}
	public long executaVerticeNaNoC(CDCM_NoC noc)
	{
		String coreOrigem = vertice.getCoreOrigem();
		String coreDestino = vertice.getCoreDestino();
		int linhaOrigem = noc.DescobreLinha(coreOrigem);
		int colunaOrigem = noc.DescobreColuna(coreOrigem);
		int alturaOrigem = noc.DescobreAltura(coreOrigem);
		int linhaDestino = noc.DescobreLinha(coreDestino);
		int colunaDestino = noc.DescobreColuna(coreDestino);
		int alturaDestino = noc.DescobreAltura(coreDestino);

		if(linhaOrigem >= 0 && colunaOrigem >= 0 && alturaOrigem >= 0 && linhaDestino >= 0 && colunaDestino >=0&& alturaDestino >=0)
		{
			long cicloFinal = noc.topologia(vertice, coreOrigem, coreDestino, linhaOrigem, colunaOrigem,alturaOrigem, linhaDestino, colunaDestino,alturaDestino);

			vertice.setCicloFinal(cicloFinal);
		
			return cicloFinal;
		}
		return noc.getCiclo();
	}
	public CDCM_Ordenado_ListaVertices getAnt()
	{
		return ant;
	}
	public void setAnt(CDCM_Ordenado_ListaVertices ant)
	{
		this.ant = ant;
	}
	public CDCM_Ordenado_ListaVertices getProx()
	{
		return prox;
	}
	public void setProx(CDCM_Ordenado_ListaVertices prox)
	{
		this.prox = prox;
	}
	public CDCM_Ordenado_Vertice getInicio()
	{
		return inicio;
	}
	public CDCM_Ordenado_Vertice getFim()
	{
		return fim;
	}
	public void setInicio(CDCM_Ordenado_Vertice inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(CDCM_Ordenado_Vertice fim)
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
		CDCM_Ordenado_Vertice p = inicio;
		while(p!=null)
		{
			CDCM_Vertice k = p.getVertice();
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
						"|" + StringFormat.format(vertice.getStringComputacao(), StringFormat.CENTER, 12) +
						"|" + StringFormat.format(vertice.getStringPhits(), StringFormat.CENTER, 11) +
						"|" + StringFormat.format(listaDependencia, StringFormat.LEFT, 69) +
						"|";
		System.out.println(string);
	}
}
