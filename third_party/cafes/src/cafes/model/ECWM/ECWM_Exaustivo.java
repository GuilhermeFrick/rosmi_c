package cafes.model.ECWM;

import cafes.common.*;

class ECWM_Exaustivo
{
	private ECWM_Grafo grafo;
	private ECWM_NoC noc;
	private boolean comEstimativaDeTempo;

	public ECWM_Exaustivo(ECWM_NoC noc, ECWM_Grafo grafo, boolean comEstimativaDeTempo)
	{
		this.noc = noc;
		this.grafo = grafo;
		this.comEstimativaDeTempo = comEstimativaDeTempo;
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
	public void computaSalva(ECWM_Grafo g)
	{
		double energia = noc.computa(g, comEstimativaDeTempo);
		
		if(noc.getEnergiaConsumidaMapeamento()>energia)
		{
			noc.setEnergiaConsumidaMapeamento(energia);
			noc.salvaPosicionamento();
			if(comEstimativaDeTempo)
				noc.salvaCiclosOperacao();
		}
	}
	public void algoritmo(LinhaColunaAltura[] vetLinhaColuna, String[] vetCore, int nElementos)
	{
		if(nElementos==1)
		{
			noc.conectaLinhaColunaComCore(vetLinhaColuna[0], vetCore[0]);
			computaSalva(grafo);
			return;
		}
		for(int i=0; i<nElementos; i++)
		{
			noc.conectaLinhaColunaComCore(vetLinhaColuna[i], vetCore[0]);
			LinhaColunaAltura subVetLinhaColuna[] = criaSubVetorDeLinhaColuna(vetLinhaColuna, i, nElementos-1);
			String subVetCore[] = criaSubVetorDeCores(vetCore, 0, nElementos-1);
			algoritmo(subVetLinhaColuna, subVetCore, nElementos-1);
		}
	}
}
