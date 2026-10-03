package cafes.model.CDCM;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cafes.ui.WindowPrincipal;
import javax.swing.JOptionPane;

import cafes.NoC.*;

class CDCM_2NoC
{
	public CDCM_2NoC(CDCM_Sequencia seq, WindowPrincipal WP, int algo)
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura = WP.getNumAltura();
		int numCores = seq.getNumeroCores();
		if(!podeContinuarAlgoritmo(seq, numColunas, numLinhas,numAltura, numCores, "", true))  // by amory
			return;
		CDCM_WindowNoC CDCM_WN = new CDCM_WindowNoC(WP, numLinhas, numColunas,numAltura, seq, algo);
		CDCM_NoC noc = new CDCM_NoC(WP, CDCM_WN, numLinhas, numColunas, numAltura);
		CDCM_WN.getEvolucaoMapeamento().paint(CDCM_WN.getEvolucaoMapeamento().getGraphics());
		noc.algoritmoPosicionamento(seq, algo);
		noc.copiaMatComMatSalva();
		CDCM_WN.dispose();
		new CDCM_WindowNoC(noc, numLinhas, numColunas,numAltura, seq, algo);
	}
	public CDCM_2NoC(CDCM_Sequencia seq, WindowPrincipal WP, int algo, boolean linhaComando, String faultConfigFile)  // by amory
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura = WP.getNumAltura();
		int numCores = seq.getNumeroCores();
		if(!podeContinuarAlgoritmo(seq, numColunas, numLinhas, numAltura,numCores,faultConfigFile, !linhaComando))  // by amory
			return;
		CDCM_WindowNoC CDCM_WN = new CDCM_WindowNoC(WP, numLinhas, numColunas,numAltura, seq);
		CDCM_NoC noc = new CDCM_NoC(WP, CDCM_WN, numLinhas, numColunas,numAltura);
		noc.algoritmoPosicionamento(seq, algo, linhaComando);
		// save the resulting mapping. by amory
		if (linhaComando)
		{
			String mapping = noc.imprimeNoC();
			String path = System.getProperties().getProperty("user.dir");
			File file = new File(path,"mapping.txt");
	    	FileOutputStream saida;
			try {
				saida = new FileOutputStream(file);
		    	saida.write(mapping.getBytes());
		    	saida.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Could not create file "+file);
			}
		}
	}
	public CDCM_2NoC(CDCM_Sequencia seq, WindowPrincipal WP)
	{
		int numeroElementos = CDCM_CoreMapping.numberOfElement();
		
		if(numeroElementos <= 0)
		{
			JOptionPane.showMessageDialog(null, "There is no mapping.\n", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura = WP.getNumAltura();
		int numCores = seq.getNumeroCores();
		if(!podeContinuarAlgoritmo(seq, numColunas, numLinhas,numAltura, numCores, "", true)) // by amory
			return;
		CDCM_WindowNoC CDCM_WN = new CDCM_WindowNoC(WP, numLinhas, numColunas,numAltura, seq);
		CDCM_NoC noc = new CDCM_NoC(WP, CDCM_WN, numLinhas, numColunas,numAltura);
		for(int i = 0; i < numeroElementos; i++)
			noc.conectaLinhaColunaComCore(CDCM_CoreMapping.positionOfElement(i), CDCM_CoreMapping.coreOfElement(i));
		noc.copiaMatSalvaComMat();
		seq.executaASAPeCriaListaDeNiveis(true);
		seq.exibeListaNiveis();
		noc.setCiclo(seq.executaCDCM(noc));
		CDCM_WN.dispose();
		new CDCM_WindowNoC(noc, numLinhas, numColunas,numAltura, seq);
		noc.setEnergiaConsumidaMapeamento(noc.energiaTotalNoC());
		noc.exibeNoCSalva();
	}
	 // by amory
	private boolean podeContinuarAlgoritmo(CDCM_Sequencia seq, int numColunas, int numLinhas,int numAltura, int numCores, String faultConfigFile, boolean verbose)
	{
		if(seq.grafoVazio())
		{
			JOptionPane.showMessageDialog(null, "There is no CDCG to compute.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(numColunas * numLinhas *numAltura< numCores)
		{
			JOptionPane.showMessageDialog(null, "NoC dimensions are not enough! \nIncrease columns and lines to cover " + numCores + " vertices.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		new ArquivoFalhas(faultConfigFile);  // by amory
		if(verbose)
			seq.ExibeVertices();
		return true;
	}
}
