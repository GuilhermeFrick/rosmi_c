package cafes.model.CWM;

import cafes.ui.WindowPrincipal;
import javax.swing.JOptionPane;
import cafes.NoC.*;

class CWM_2NoC
{
	private boolean showWindow;
	public long computationTimems;
	public double energyConsumed;
	public String distribution;
	public int iteracoes, temperatura;
	
	public CWM_2NoC(CWM_Sequencia _seq, WindowPrincipal _WP, int _algo, boolean _comEstimativaDeTempo, boolean _showWindow, int _iteracoes, int _temperatura)	
	{
		iteracoes = _iteracoes;
		temperatura = _temperatura;
		main_2NoC(_seq, _WP, _algo, _comEstimativaDeTempo, _showWindow);
	}
	public CWM_2NoC(CWM_Sequencia _seq, WindowPrincipal _WP, int _algo, boolean _comEstimativaDeTempo, boolean _showWindow)
	{
		iteracoes = -1;
		temperatura = -1;
		main_2NoC(_seq, _WP, _algo, _comEstimativaDeTempo, _showWindow);		
	}
	public CWM_2NoC(CWM_Sequencia seq, WindowPrincipal WP)
	{
		if(CWM_CoreMapping.numberOfElement()<=0)
		{
			JOptionPane.showMessageDialog(null, "There is no mapping.\n", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura = WP.getNumAltura();
		iteracoes = -1;
		temperatura = -1;
		
		// Cria a janela de exibi��o
		CWM_WindowNoC CWM_WN = new CWM_WindowNoC(WP, numLinhas, numColunas, numAltura,seq, true);
		
		// Cria a classe que realizar� o c�lculo
		CWM_NoC noc = new CWM_NoC(WP, CWM_WN, WP.getNumLinhas(), WP.getNumColunas(), WP.getNumAltura(), true);
		
		// Realiza a primeira associa��o entre IPs e pontos da rede
		for(int i=0; i<CWM_CoreMapping.numberOfElement(); i++)
			noc.conectaLinhaColunaComCore(CWM_CoreMapping.positionOfElement(i), CWM_CoreMapping.coreOfElement(i));
		
		noc.copiaMatSalvaComMat();
		seq.executaCWM(noc, WP.ehTopologiaMesh());
		noc.salvaCiclosOperacao();
		noc.setEnergiaConsumidaMapeamento(noc.energiaNoC() + noc.getEnergiaIdle());
		CWM_WN.dispose();
		new CWM_WindowNoC(noc, numLinhas, numColunas, numAltura,seq, true);
		noc.exibe();
	}
	private void main_2NoC(CWM_Sequencia seq, WindowPrincipal WP, int algo, boolean comEstimativaDeTempo, boolean _showWindow)
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura = WP.getNumAltura();

		showWindow = _showWindow;
		if(seq.grafoVazio())
		{
			JOptionPane.showMessageDialog(null, "There is no CWG to compute.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(numColunas*numLinhas*numAltura<seq.getNumeroDeVertices())
		{
			JOptionPane.showMessageDialog(null, "NoC dimensions are not enough! \nIncrease columns and lines to cover "+seq.getNumeroDeVertices()+" vertices.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		new ArquivoFalhas(""); // by amory
		if(showWindow)
		{
			seq.ExibeVertices();
			CWM_WindowNoC CWM_WN = new CWM_WindowNoC(WP, numLinhas, numColunas,numAltura, seq, algo, comEstimativaDeTempo);
			CWM_NoC noc = new CWM_NoC(WP, CWM_WN, numLinhas, numColunas,numAltura, showWindow);
			CWM_WN.getEvolucaoMapeamento().paint(CWM_WN.getEvolucaoMapeamento().getGraphics());
			noc.algoritmoPosicionamento(seq, algo, comEstimativaDeTempo);
			noc.copiaMatComMatSalva();
			CWM_WN.dispose();
			CWM_WN = new CWM_WindowNoC(noc, numLinhas, numColunas,numAltura, seq, algo, comEstimativaDeTempo);
		}
		else
		{
			CWM_NoC noc = new CWM_NoC(WP, null, numLinhas, numColunas,numAltura, showWindow);
			noc.setiteracoes(iteracoes);
			noc.setTemperatura(temperatura);
			noc.algoritmoPosicionamento(seq, algo, comEstimativaDeTempo);
			noc.copiaMatComMatSalva();
			iteracoes = noc.iteracoes;
			temperatura = noc.temperatura;
			computationTimems = noc.getComputationTime();
			energyConsumed = noc.getEnergiaConsumidaMapeamento();
			distribution = "";
			for(int linha=0; linha<noc.matriz.length; linha++)
			{
				for(int coluna=0; coluna<noc.matriz[linha].length; coluna++)
					for(int altura=0; altura<noc.matriz[linha][coluna].length; altura++)
				{
					if(!noc.matriz[linha][coluna][altura].getCoreName().equals("-"))
						distribution += "  "+noc.matriz[linha][coluna][altura].getCoreName()+" - ("+linha+","+coluna+","+altura+") \n";
				}
			}
		}
	}
}
