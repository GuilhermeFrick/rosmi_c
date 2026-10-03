package cafes.model.CDCM;

import java.awt.Graphics;
import java.awt.*;
import java.util.Vector;

public class CDCM_ListaComunicacao extends Vector<CDCM_Vertice>
{
	private static final long serialVersionUID = -2424942192679927565L;
	private CDCM_ListaTemporaria listaTemporaria;
	private long tempoTotalComunicacao;
	private CDCM_Sequencia seq;
	
	public CDCM_ListaComunicacao(CDCM_Sequencia seq)
	{
		super(10, 2);
		this.seq = seq;
		tempoTotalComunicacao = 0;
		listaTemporaria = null;
	}
	public void coloreGrafo(Graphics g)
	{
		CDCM_Vertice p;
		CDCM_Vertice pAnt = null;

		for(int i=0; i<elementCount; i++)
		{
			p = elementAt(i);
			if(pAnt!=null)
				seq.coloreAresta(g, Color.blue, pAnt, p);
			pAnt = p;
		}
	}
	private void salvaListaComunicacao()
	{
		clear();
		add(seq.getInicio());
		for(int i=0; i<listaTemporaria.size(); i++)
			add(listaTemporaria.elementAt(i));
		add(seq.getEndVertex());
		tempoTotalComunicacao = listaTemporaria.getTempoAcumuladoComunicacao();
	}
	public void executaAnaliseTemporal()
	{
		listaTemporaria = new CDCM_ListaTemporaria(seq);
		AnaliseTemporal(seq.getInicio());
	}
	private void AnaliseTemporal(CDCM_Vertice inicio)
	{
		CDCM_VerticeDependente p=inicio.getVerticeDependenteInicial();

		while(p!=null)
		{
			CDCM_Vertice v = p.getVertice();
			if(v.getID()!=CDCM_Grafo.START && v.getID()!=CDCM_Grafo.END)
			{
				listaTemporaria.insereListaTemporaria(v);
				AnaliseTemporal(v); // Escalona todos os adjacentes com um nivel abaixo
				listaTemporaria.removeListaTemporaria(v);
			}
			p = p.getProx();
		}
		if(tempoTotalComunicacao<listaTemporaria.getTempoAcumuladoComunicacao())
			salvaListaComunicacao();
	}
}
