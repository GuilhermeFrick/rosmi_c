package cafes.model.ACPM;

import java.io.*;
import java.awt.*;
import cafes.common.*;

class ACPM_AnaliseTemporal
{
	private ACPM_WindowNoC win;
	private ACPM_Sequencia seq;
	
	public ACPM_AnaliseTemporal(ACPM_WindowNoC win, ACPM_Sequencia seq, ACPM_NoC noc)
	{
		this.win = win;
		this.seq = seq;
		seq.executaACPM(noc);
		ImprimeAnaliseTiming();
	}
	private void ImprimeAnaliseTiming()
	{
		try
		{
			FileDialog d = new FileDialog(win, "ACPM timing analisys", FileDialog.SAVE);
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
				"+-------+----------------------------+-----------------+----------------+----------------+----------------+\n" +
				"|  Tag  |   Origem    ->   Destino   |      Phits      |     Inicio     |       Fim      |   Diferença    |\n" +
				"+-------+----------------------------+-----------------+----------------+----------------+----------------+\n";
			ds.write(string.getBytes());
			long cicloMaximo = lacoAnaliseTiming(ds);
			string = 
				"+-------+----------------------------+-----------------+----------------+----------------+----------------+\n" +
				"| Tempo Máximo: " + cicloMaximo;
			ds.write(string.getBytes());
		}
		catch(Exception e)
		{
			System.out.println("Problems while generating ACPM timing analisys");
			e.printStackTrace();
			return;
		}
	}
	public long lacoAnaliseTiming(DataOutputStream ds)
	{
		long cicloMaximo = -1;
		long ciclo;
		
		ACPM_Tag p = seq.getInicio();
		while(p!=null)
		{
			if(p.getTag()==ACPM_Grafo.END || p.getTag()==ACPM_Grafo.START)
			{
				p = p.getProx();
				continue;
			}
			ACPM_Vertice v = p.getVerticeInicial();
			while(v!=null)
			{
				ciclo = imprimeFormatado(ds, p, v);
				if(ciclo>cicloMaximo)
					cicloMaximo = ciclo;
				v = v.getProx();
			}
			p = p.getProx();
		}
		return cicloMaximo;
	}
	private long imprimeFormatado(DataOutputStream ds, ACPM_Tag tag, ACPM_Vertice vertice)
	{
		String tagStr = tag.formataStringTag();
		String origem = vertice.getCoreOrigem();
		String destino = vertice.getCoreDestino();
		long phits = vertice.getPhits();
		long cicloInicial = vertice.getCicloInicial();
		long cicloFinal = vertice.getCicloFinal();
		long ciclos = cicloFinal - cicloInicial;
		
		String string = "|" + StringFormat.format(tagStr, StringFormat.CENTER, 7) +
						"|" + StringFormat.format(origem, StringFormat.CENTER, 13) +
						"->" + StringFormat.format(destino, StringFormat.CENTER, 13) +
						"|" + StringFormat.format(phits, StringFormat.RIGTH, 16) +
						" |" + StringFormat.format(cicloInicial, StringFormat.RIGTH, 15) +
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
			System.out.println("Problems while generating ACPM timing analisys 2");
			e.printStackTrace();
		}
	}
}
