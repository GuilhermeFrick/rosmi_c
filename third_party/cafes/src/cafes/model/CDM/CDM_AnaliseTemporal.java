package cafes.model.CDM;

import java.io.*;
import java.awt.*;

import cafes.common.*;

class CDM_AnaliseTemporal
{
	private CDM_WindowNoC win;
	private CDM_Sequencia seq;
	
	public CDM_AnaliseTemporal(CDM_WindowNoC win, CDM_Sequencia seq, CDM_NoC noc, boolean comContencao)
	{
		this.win = win;
		this.seq = seq;
		seq.executaASAPeCriaListaDeNiveis();	// Executa Novamente para limpar a anterior	
		noc.setCiclo(seq.executaCDM(noc, comContencao));
		ImprimeAnaliseTiming();
		noc.exibe();
	}
	private void ImprimeAnaliseTiming()
	{
		try
		{
			FileDialog d = new FileDialog(win, "CDM timing analisys", FileDialog.SAVE);
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
				"+------+----------------------------+-----------------+----------------+----------------+----------------+----------------+\n" +
				"|  ID  |   Origem    ->   Destino   |      Phits      |     Inicio     |     Saida      |       Fim      |   Diferença    |\n" +
				"+------+----------------------------+-----------------+----------------+----------------+----------------+----------------+\n";
			ds.write(string.getBytes());
			long cicloMaximo = lacoAnaliseTiming(ds);
			string = 
				"+------+----------------------------+-----------------+----------------+----------------+----------------+----------------+\n" +
				"| Tempo Máximo: " + cicloMaximo;
			ds.write(string.getBytes());
		}
		catch(Exception e)
		{
			System.out.println("Problems while generating CDM timing analisys");
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
			CDM_Ordenado_ListaVertices p = seq.getVetorNiveis()[i].getInicio();
			
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
	private long imprimeFormatado(DataOutputStream ds, CDM_Vertice vertice)
	{
		String ID = vertice.formataStringID();
		String origem = vertice.getCoreOrigem();
		String destino = vertice.getCoreDestino();
		long phits = vertice.getPhits();
		long cicloInicial = vertice.getCicloInicial();
		long cicloFinalLocal = vertice.getCicloFinalLocal();
		long cicloFinal = vertice.getCicloFinal();
		long ciclos = cicloFinal - cicloInicial;
		
		String string = "|" + StringFormat.format(ID, StringFormat.CENTER, 6) +
						"|" + StringFormat.format(origem, StringFormat.CENTER, 13) +
						"->" + StringFormat.format(destino, StringFormat.CENTER, 13) +
						"|" + StringFormat.format(phits, StringFormat.RIGTH, 16) +
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
			System.out.println("Problems while generating CDM timing analisys 2");
			e.printStackTrace();
		}
	}
}
