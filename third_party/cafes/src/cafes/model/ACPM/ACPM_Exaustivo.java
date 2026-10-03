package cafes.model.ACPM;
import cafes.common.*;

class ACPM_Exaustivo
{
	private ACPM_Grafo grafo;
	private ACPM_NoC noc;

	public ACPM_Exaustivo(ACPM_NoC noc, ACPM_Grafo grafo)
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
	public void computaSalva(ACPM_Grafo g)
	{
		double energia = noc.computa(g);
		
		if(noc.getEnergiaConsumidaMapeamento()>energia)
		{
			noc.setEnergiaConsumidaMapeamento(energia);
			noc.salvaPosicionamento();
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
