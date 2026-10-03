package cafes.model.ACPM;

import javax.swing.JOptionPane;
import cafes.ui.WindowPrincipal;

public class ACPM_2NoC
{
	public ACPM_2NoC(ACPM_Sequencia seq, WindowPrincipal WP, int algo)
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura= WP.getNumAltura();
		int numCores = seq.getNumeroCores();
		if(!podeContinuarAlgoritmo(seq, numColunas, numLinhas, numAltura,numCores))
			return;
		ACPM_WindowNoC ACPM_WN = new ACPM_WindowNoC(WP, numLinhas, numColunas, numAltura,seq, algo);
		ACPM_NoC noc = new ACPM_NoC(WP, ACPM_WN, numLinhas, numColunas,numAltura);
		ACPM_WN.getEvolucaoMapeamento().paint(ACPM_WN.getEvolucaoMapeamento().getGraphics());
		noc.algoritmoPosicionamento(seq, algo);
		noc.copiaMatComMatSalva();
		ACPM_WN.dispose();
		ACPM_WN = new ACPM_WindowNoC(noc, numLinhas, numColunas, numAltura,seq, algo);
	}
	public ACPM_2NoC(ACPM_Sequencia seq, WindowPrincipal WP)
	{
		int numeroElementos = ACPM_CoreMapping.numberOfElement();
		
		if(numeroElementos<=0)
		{
			JOptionPane.showMessageDialog(null, "There is no mapping.\n", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int numColunas = WP.getNumColunas();
		int numAltura = WP.getNumAltura();
		int numLinhas = WP.getNumLinhas();
		int numCores = seq.getNumeroCores();
		if(!podeContinuarAlgoritmo(seq, numColunas, numLinhas,numAltura, numCores))
			return;
		ACPM_WindowNoC ACPM_WN = new ACPM_WindowNoC(WP, numLinhas, numColunas, numAltura,seq);
		ACPM_NoC noc = new ACPM_NoC(WP, ACPM_WN, numLinhas, numColunas,numAltura);
		for(int i=0; i<numeroElementos; i++)
			noc.conectaLinhaColunaComCore(ACPM_CoreMapping.positionOfElement(i), ACPM_CoreMapping.coreOfElement(i));
		noc.copiaMatSalvaComMat();
		noc.setCiclo(seq.executaACPM(noc));
		ACPM_WN.dispose();
		new ACPM_WindowNoC(noc, numLinhas, numColunas,numAltura, seq);
		noc.exibe();
	}
	private boolean podeContinuarAlgoritmo(ACPM_Sequencia seq, int numColunas, int numLinhas,int numAltura, int numCores)
	{
		if(seq.grafoVazio())
		{
			JOptionPane.showMessageDialog(null, "There is no ACPG to compute.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(numColunas*numLinhas*numAltura<numCores)
		{
			JOptionPane.showMessageDialog(null, "NoC dimensions are not enough! \nIncrease columns and lines to cover " + numCores + " vertices.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		seq.ExibeVertices();
		return true;
	}
}
