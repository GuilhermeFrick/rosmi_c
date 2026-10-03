package cafes.model.CDM;

import cafes.ui.WindowPrincipal;
import javax.swing.JOptionPane;

class CDM_2NoC
{
	public CDM_2NoC(CDM_Sequencia seq, WindowPrincipal WP, int algo)
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numCores = seq.getNumeroCores();
		int numAltura= WP.getNumAltura();
		if(!podeContinuarAlgoritmo(seq, numColunas, numLinhas, numAltura,numCores))
			return;
		CDM_WindowNoC CDM_WN = new CDM_WindowNoC(WP, numLinhas, numColunas,numAltura, seq, algo);
		CDM_NoC noc = new CDM_NoC(WP, CDM_WN, numLinhas, numColunas, numAltura);
		CDM_WN.getEvolucaoMapeamento().paint(CDM_WN.getEvolucaoMapeamento().getGraphics());
		noc.algoritmoPosicionamento(seq, algo);
		noc.copiaMatComMatSalva();
		CDM_WN.dispose();
		CDM_WN = new CDM_WindowNoC(noc, numLinhas, numColunas,numAltura, seq, algo);
	}
	public CDM_2NoC(CDM_Sequencia seq, WindowPrincipal WP)
	{
		int numeroElementos = CDM_CoreMapping.numberOfElement();
		
		if(numeroElementos<=0)
		{
			JOptionPane.showMessageDialog(null, "There is no mapping.\n", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura = WP.getNumAltura();
		int numCores = seq.getNumeroCores();
		if(!podeContinuarAlgoritmo(seq, numColunas, numLinhas,numAltura, numCores))
			return;
		CDM_WindowNoC CDM_WN = new CDM_WindowNoC(WP, numLinhas, numColunas,numAltura, seq);
		CDM_NoC noc = new CDM_NoC(WP, CDM_WN, numLinhas, numColunas ,numAltura);
		for(int i=0; i<numeroElementos; i++)
			noc.conectaLinhaColunaComCore(CDM_CoreMapping.positionOfElement(i), CDM_CoreMapping.coreOfElement(i));
		noc.copiaMatSalvaComMat();
		seq.executaASAPeCriaListaDeNiveis();		
		noc.setCiclo(seq.executaCDM(noc, true));
		CDM_WN.dispose();
		new CDM_WindowNoC(noc, numLinhas, numColunas, numAltura,seq);
		noc.setEnergiaConsumidaMapeamento(noc.energiaTotalNoC());
		noc.exibe();
	}
	private boolean podeContinuarAlgoritmo(CDM_Sequencia seq, int numColunas, int numLinhas,int numAltura,int numCores)
	{
		if(seq.grafoVazio())
		{
			JOptionPane.showMessageDialog(null, "There is no CDG to compute.", "Error", JOptionPane.ERROR_MESSAGE);
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
