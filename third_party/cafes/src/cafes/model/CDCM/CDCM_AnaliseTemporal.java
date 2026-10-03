package cafes.model.CDCM;

import java.io.*;
import java.awt.*;

import cafes.common.*;

class CDCM_AnaliseTemporal
{
	private CDCM_WindowNoC win;
	private CDCM_Sequencia seq;
	
	public CDCM_AnaliseTemporal(CDCM_WindowNoC win, CDCM_Sequencia seq, CDCM_NoC noc)
	{
		this.win = win;
		this.seq = seq;
		seq.executaASAPeCriaListaDeNiveis(true);	// Executa Novamente para limpar a anterior	
		noc.setCiclo(seq.executaCDCM(noc));
		ImprimeAnaliseTiming();
		noc.exibe();
	}
	private void ImprimeAnaliseTiming()
	{
		try
		{
			FileDialog d = new FileDialog(win, "CDCM timing analisys", FileDialog.SAVE);
			if(ArquivoModelo.getArquivo()!=null)
			{
				d.setDirectory(ArquivoModelo.getDiretorio() + "Timing"); 
				d.setFile(ArquivoModelo.appenda(".timing"));
			}
			d.setVisible(true);
			if(d.getFile()==null)
				return;
			ArquivoModelo.setArquivo(d.getFile());
			String fileS = d.getDirectory() + d.getFile();
			OutputStream fileOut = new FileOutputStream(fileS);
			DataOutputStream ds = new DataOutputStream(fileOut);
			String string = 
				"+------+----------------------------+------------+------------+----------------+----------------+----------------+----------------+\n" +
				"|  ID  |   Origem    ->   Destino   | Computação |   Phits    |     Inicio     |     Saída      |     Fim        |   Diferença    |\n" +
				"+------+----------------------------+------------+------------+----------------+----------------+----------------+----------------+\n";
			ds.write(string.getBytes());
			long cicloMaximo = lacoAnaliseTiming(ds);
			string = 
				"+------+----------------------------+------------+------------+----------------+----------------+----------------+----------------+\n" +
				"| Tempo Máximo: " + cicloMaximo;
			ds.write(string.getBytes());
		}
		catch(Exception e)
		{
			System.out.println("Problems while generating CDCM timing analisys");
			e.printStackTrace();
			return;
		}
	}
	public long lacoAnaliseTiming(DataOutputStream ds)
	{
		long cicloMaximo = -1;
		long ciclo;

		for(int i=1; i<seq.getVetorNiveis().length-1; i++) // START e END não precisam ser executados
		{
			CDCM_Ordenado_ListaVertices p = seq.getVetorNiveis()[i].getInicio();
			
			while(p!=null)
			{
				ciclo = imprimeFormatado(ds, p.getVertice());
				if(ciclo>cicloMaximo)
					cicloMaximo = ciclo;
				p = p.getProx();
			}
		}
		return cicloMaximo;
	}
	private long imprimeFormatado(DataOutputStream ds, CDCM_Vertice vertice)
	{
		String ID = vertice.formataStringID();
		String origem = vertice.getCoreOrigem();
		String destino = vertice.getCoreDestino();
		long phits = vertice.getPhits();
		long computacao = vertice.getComputacao();
		long cicloInicial = vertice.getCicloInicial();
		long cicloFinalLocal = vertice.getCicloFinalLocal();
		long cicloFinal = vertice.getCicloFinal();
		long ciclos = cicloFinal - cicloInicial;
		
		String string = "|" + StringFormat.format(ID, StringFormat.CENTER, 6) +
						"|" + StringFormat.format(origem, StringFormat.RIGTH, 13) +
						"->" + StringFormat.format(destino, StringFormat.LEFT, 13) +
						"|" + StringFormat.format(computacao, StringFormat.RIGTH, 12) +
						"|" + StringFormat.format(phits, StringFormat.RIGTH, 11) +
						" |" + StringFormat.format(cicloInicial, StringFormat.RIGTH, 15) +
						" |" + StringFormat.format(cicloFinalLocal, StringFormat.RIGTH, 15) +
						" |" + StringFormat.format(cicloFinal, StringFormat.RIGTH, 15) +
						" |" + StringFormat.format(ciclos, StringFormat.RIGTH, 15) +
						" |\n";
		printFile(ds, string);
		
		return cicloFinal;
	}
	private void printFile(DataOutputStream ds, String str)
	{
		try
		{
			ds.write(str.getBytes());
		}
		catch(Exception e)
		{
			System.out.println("Problems while generating CDCM timing analisys 2");
			e.printStackTrace();
		}
	}
}
