package cafes.model.ECWM;

import cafes.ui.WindowPrincipal;
import javax.swing.JOptionPane;

class ECWM_2NoC
{
	public ECWM_2NoC(ECWM_Sequencia seq, WindowPrincipal WP, int algo, boolean comEstimativaDeTempo)
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura = WP.getNumAltura();
	
		if(seq.grafoVazio())
		{
			JOptionPane.showMessageDialog(null, "There is no ECWG to compute.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(numColunas*numLinhas*numAltura<seq.getNumeroDeVertices())
		{
			JOptionPane.showMessageDialog(null, "NoC dimensions are not enough! \nIncrease columns and lines to cover "+seq.getNumeroDeVertices()+" vertices.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		seq.ExibeVertices();
		ECWM_WindowNoC ECWM_WN = new ECWM_WindowNoC(WP, numLinhas, numColunas, numAltura,seq, algo, comEstimativaDeTempo);
		ECWM_NoC n = new ECWM_NoC(WP, ECWM_WN, numLinhas, numColunas, numAltura);
		ECWM_WN.getEvolucaoMapeamento().paint(ECWM_WN.getEvolucaoMapeamento().getGraphics());
		n.algoritmoPosicionamento(seq, algo, comEstimativaDeTempo);
		n.copiaMatComMatSalva();
		ECWM_WN.dispose();
		ECWM_WN = new ECWM_WindowNoC(n, n.getNumeroLinhas(), n.getNumeroColunas(), n.getNumeroAltura(),seq, algo, comEstimativaDeTempo);
	}
	public ECWM_2NoC(ECWM_Sequencia seq, WindowPrincipal WP)
	{
		if(ECWM_CoreMapping.numberOfElement()<=0)
		{
			JOptionPane.showMessageDialog(null, "There is no mapping.\n", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura = WP.getNumLinhas();
		ECWM_WindowNoC ECWM_WN = new ECWM_WindowNoC(WP, numLinhas, numColunas,numAltura, seq, true);
		ECWM_NoC noc = new ECWM_NoC(WP, ECWM_WN, numLinhas, numColunas,numAltura);
		for(int i=0; i<ECWM_CoreMapping.numberOfElement(); i++)
			noc.conectaLinhaColunaComCore(ECWM_CoreMapping.positionOfElement(i), ECWM_CoreMapping.coreOfElement(i));
		noc.copiaMatSalvaComMat();
		seq.executaECWM(noc, WP.ehTopologiaMesh());
		noc.salvaCiclosOperacao();
		noc.setEnergiaConsumidaMapeamento(noc.energiaNoC() + noc.getEnergiaIdle());
		ECWM_WN.dispose();
		new ECWM_WindowNoC(noc, numLinhas, numColunas,numAltura, seq, true);
		noc.exibe();
	}
}
