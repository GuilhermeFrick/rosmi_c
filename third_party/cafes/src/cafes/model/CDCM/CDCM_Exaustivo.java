package cafes.model.CDCM;

import cafes.common.*;

class CDCM_Exaustivo
{
	private CDCM_Grafo grafo;
	private CDCM_NoC noc;
	private long cicloMinimoCustoMapeamento;

	public CDCM_Exaustivo(CDCM_NoC noc, CDCM_Grafo grafo)
	{
		this.noc = noc;
		this.grafo = grafo;
	}
	public String[] criaSubVetorDeCores(String vetCore[], int excluir, int nElementos)
	{
		String subVetCore[] = new String[nElementos];

		for(int i=0, j=0; i<nElementos; i++, j++)
		{
			if(j==excluir)
				j++;
			subVetCore[i] = vetCore[j];
		}
		return subVetCore;
	}
	public long getCicloMinimoCustoMapeamento()
	{
		return cicloMinimoCustoMapeamento;
	}
	public LinhaColunaAltura[] criaSubVetorDeLinhaColuna(LinhaColunaAltura vetLinhaColuna[], int excluir, int nElementos)
	{
		LinhaColunaAltura subVetLinhaColuna[] = new LinhaColunaAltura[nElementos];

		for(int i=0, j=0; i<nElementos; i++, j++)
		{
			if(j==excluir)
				j++;
			subVetLinhaColuna[i] = vetLinhaColuna[j];
		}
		return subVetLinhaColuna;
	}
	public void computaSalva(CDCM_Grafo g, boolean linhaComando)
	{
		double energia = noc.computa(g, linhaComando);
		
		if(noc.getEnergiaConsumidaMapeamento() > energia)
		{
			noc.setEnergiaConsumidaMapeamento(energia);
			noc.salvaPosicionamento();
			cicloMinimoCustoMapeamento = noc.getCiclo();
		}
	}
	public long algoritmo(LinhaColunaAltura[] vetLinhaColuna, String[] vetCore, int nElementos, boolean linhaComando)
	{
		if(nElementos == 1)
		{
			noc.conectaLinhaColunaComCore(vetLinhaColuna[0], vetCore[0]);
			computaSalva(grafo, linhaComando);
			return cicloMinimoCustoMapeamento;
		}
		for(int i = 0; i < nElementos; i++)
		{
			noc.conectaLinhaColunaComCore(vetLinhaColuna[i], vetCore[0]);
			LinhaColunaAltura subVetLinhaColuna[] = criaSubVetorDeLinhaColuna(vetLinhaColuna, i, nElementos-1);
			String subVetCore[] = criaSubVetorDeCores(vetCore, 0, nElementos-1);
			algoritmo(subVetLinhaColuna, subVetCore, nElementos-1, linhaComando);
		}
		return cicloMinimoCustoMapeamento;
	}
}
