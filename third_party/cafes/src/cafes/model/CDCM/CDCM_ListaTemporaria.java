package cafes.model.CDCM;

import java.util.Vector;

import cafes.NoC.*;

public class CDCM_ListaTemporaria extends Vector<CDCM_Vertice>
{
	private static final long serialVersionUID = 8097562649591853323L;
	private long acumuladorComputacao;
	private long acumuladorComunicacao;
	private double ciclosComunicacao;
	
	public CDCM_ListaTemporaria(CDCM_Sequencia seq)
	{
		super(10, 2);
		cdcmListaTemporaria();
		NoCParameters nocPar = seq.getMappingCost().getWindowPrincipal().getNoCPar();
		int numColunas = nocPar.getNumColunas();
		int numLinhas = nocPar.getNumLinhas();
		long linkingCycles = nocPar.getLinkingCycles();
		long routingCycles = nocPar.getRoutingCycles();
		long numCiclosLinkMaiorCaminho = (numColunas + numLinhas + 1) * linkingCycles; 
		long numCiclosRoteadorMaiorCaminho = (numColunas + numLinhas) * routingCycles; 
		double percentualSobreMaiorCaminho = 0.3; 
		this.ciclosComunicacao = (numCiclosLinkMaiorCaminho + numCiclosRoteadorMaiorCaminho) * percentualSobreMaiorCaminho;
	}
	public CDCM_ListaTemporaria()
	{
		super(10, 2);
		cdcmListaTemporaria();
	}
	public void cdcmListaTemporaria()
	{
		acumuladorComputacao = 0;
		acumuladorComunicacao = 0;
	}
	public void insereListaTemporaria(CDCM_Vertice v)
	{
		add(v);
		acumuladorComputacao = acumuladorComputacao + v.getComputacao();
		acumuladorComunicacao = acumuladorComunicacao + v.getPhits();
	}
	public void removeListaTemporaria(CDCM_Vertice v)
	{
		remove(v);
		acumuladorComputacao = acumuladorComputacao - v.getComputacao();
		acumuladorComunicacao = acumuladorComunicacao - v.getPhits();
	}
	public long getTempoAcumuladoComputacao()
	{
		return acumuladorComputacao;
	}
	public long getTempoAcumuladoComunicacao()
	{
		return (long)(acumuladorComunicacao * ciclosComunicacao);
	}
	public long getTempoAcumuladoComunicacaoComComputacao()
	{
		return getTempoAcumuladoComunicacao() + getTempoAcumuladoComputacao();
	}
}
