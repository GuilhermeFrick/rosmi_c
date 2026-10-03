package cafes.model.ACPM;

import cafes.ui.WindowPrincipal;
import cafes.common.*;

import java.awt.*;
import java.io.*;

import javax.swing.*;


class ACPM_GrafoFormatoTextual
{
	private static String net_sizeStr = "#_NoC_Size";
	private static String GraphicStr = "#_ACPG_TagsGraphic";
	private static String nodesStr = "#_ACPG_VerticesGraphic";
	private static String placementStr = "#_ACPG2NoC_Mapping";
	private static String paragrafoStr = "\n";
	private static String paragrafo3Str = "\n\n";

	public static boolean isToken(String teste)
	{
		if(teste==null)
			return false;
		if(teste.equals(net_sizeStr))
			return true;
		if(teste.equals(nodesStr))
			return true;
		if(teste.equals(GraphicStr))
			return true;
		if(teste.equals(placementStr))
			return true;
		return false;
	}
	public static void appendaMapeamento(JFrame win, String title, String placement)
	{
		FileDialog d = new FileDialog(win, title, FileDialog.SAVE);
		if(ArquivoModelo.getArquivo()!=null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".ACPG"));
		}
		d.setVisible(true);
		if(d.getFile()==null)
			return;
		ArquivoModelo.setDiretorioArquivo(d.getDirectory(), d.getFile());
		String fileS = d.getDirectory() + d.getFile();
		try
		{
			RandomAccessFile fileOut = new RandomAccessFile(new File(fileS), "rw");

			fileOut.seek(fileOut.length()); // Manda para o final do arquivo
			String placementStrWrite = new String("\n" + placementStr + " (matrix of: cores)");
			fileOut.write(placementStrWrite.getBytes());
			fileOut.write(paragrafoStr.getBytes());
			fileOut.write(placement.getBytes());
		}
		catch(Exception e) 
		{
			System.out.println("Problems during " + fileS + " generation.");
			e.printStackTrace();
		}
	}
	public static void gravaArquivo(WindowPrincipal WP, JFrame win, ACPM_Sequencia seq, String title, String placement)
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura= WP.getNumAltura();

		FileDialog d = new FileDialog(win, title, FileDialog.SAVE);
		if(ArquivoModelo.getArquivo()!=null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".acpg"));
		}
		d.setVisible(true);
		if(d.getFile()==null)
			return;
		ArquivoModelo.setDiretorioArquivo(d.getDirectory(), d.getFile());
		String fileS = d.getDirectory() + d.getFile();
		File file = new File(fileS);
		FileOutputStream fileOut;

		try
		{
			String net_sizeStrWrite = new String(net_sizeStr + " (lines columns)");
			String nodesStrWrite = new String(nodesStr + " (list of: vertices --> Tag sourceCore - targetCore phits (x y)");
			String GraphicStrWrite = new String(GraphicStr + " (list of: Tag x y)");
			String placementStrWrite = new String("\n" + placementStr + " (matrix of: cores)");

			fileOut = new FileOutputStream(file);
			
			fileOut.write(net_sizeStrWrite.getBytes());
			String dim = "\n " + numLinhas + " " + numColunas+ " " + numAltura;
			fileOut.write(dim.getBytes());
			fileOut.write(paragrafo3Str.getBytes());
			
			fileOut.write(GraphicStrWrite.getBytes());
			fileOut.write(seq.stringLayout().getBytes());
			fileOut.write(paragrafo3Str.getBytes());
			
			fileOut.write(nodesStrWrite.getBytes());
			fileOut.write(seq.stringVertices().getBytes());
			fileOut.write(paragrafo3Str.getBytes());
			
			if(placement!=null)
			{
				fileOut.write(placementStrWrite.getBytes());
				fileOut.write(placement.getBytes());
			}
			fileOut.close();
		}
		catch(Exception e) 
		{
			System.out.println("Problems during " + fileS + " generation.");
			e.printStackTrace();
		}
	}
	private static int converteStringTagParaInt(String strTagTime)
	{
		String strTag = strTagTime.trim();
		
		try
		{
			if(strTag.equals("START"))
				return ACPM_Grafo.START;
			if(strTag.equals("END"))
				return ACPM_Grafo.END;
			return Integer.parseInt(strTag);
		}
		catch(NumberFormatException n)
		{
			return -10;
		}
	}
	public static void leArquivo(WindowPrincipal WP, JFrame win, ACPM_Sequencia seq, String title)
	{
 		FileDialog d = new FileDialog(win, title, FileDialog.LOAD);
		if(ArquivoModelo.getArquivo()!=null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".ACPG"));
		}
		d.setVisible(true);
		if(d.getFile()==null)
			return;
		ArquivoModelo.setDiretorioArquivo(d.getDirectory(), d.getFile());
		String fileS = d.getDirectory() + d.getFile();
//		String fileS= "F:\\CAFES\\V_3.7\\Arquivos\\ACPG\\b.acpg";
		File file = new File(fileS);

		try
		{
			DataInputStream fileIn = new DataInputStream(new FileInputStream(file));
			byte bytesArq[] = new byte[fileIn.available()];
			fileIn.readFully(bytesArq);
			int numLinhas=0, numColunas=0,numAltura=0;
			ByteArrayParser b = new ByteArrayParser(bytesArq);
			String readStr = new String("");
			seq.deleta();
			
			while(!b.reachEnd())
			{
				while(!isToken(readStr) && !b.reachEnd())
					readStr = b.readNextString();
				if(readStr !=  null)
				{
					if(readStr.equals(net_sizeStr)) // L� o tamanho da NoC
					{
						b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
						String numLinhasStr = b.readNextString();
						String numColunasStr = b.readNextString();
						String numAlturaStr ="1";
						if( !b.isNewLine() )
							numAlturaStr = b.readNextString();
						WP.setNumColunas(numColunasStr);
						WP.setNumLinhas(numLinhasStr);
						WP.setNumAltura(numAlturaStr);
						numColunas = Integer.parseInt(numColunasStr.trim());
						numLinhas = Integer.parseInt(numLinhasStr.trim());
						numAltura = Integer.parseInt(numAlturaStr.trim());
						readStr = b.readNextString();
					}
					else if(readStr.equals(GraphicStr))
					{
						int x, y;
						
						b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
						readStr = b.readNextString();
						while(!isToken(readStr) && !b.reachEnd()) 
						{
							if(readStr.equals(""))
							{
								readStr = b.readNextString();
								continue;
							}
							int tag = converteStringTagParaInt(readStr);
							x = Integer.parseInt(b.readNextString().trim());
							y = Integer.parseInt(b.readNextString().trim());
							ACPM_Tag tagTime = new ACPM_Tag(x, y, tag);
							seq.atualizaTagGlobal(tag);
							seq.insereTagNoGrafo(tagTime);
							readStr = b.readNextString();
						}
					}
					else if(readStr.equals(nodesStr)) // L� apenas os v�rtices da NoC
					{
						b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
						if(seq.encontraTagNoGrafo(ACPM_Grafo.START)==null)
							seq.insereTagStartGrafo();
						if(seq.encontraTagNoGrafo(ACPM_Grafo.END)==null)
							seq.insereTagEndGrafo();
						while(true)
						{
							readStr = b.readNextString();
							if(readStr==null || readStr.equals("") || b.reachEnd() || isToken(readStr))
								break;
							int tag = converteStringTagParaInt(readStr);
							ACPM_Tag tagTime = seq.encontraTagNoGrafo(tag);
							if(tagTime==null)
							{
								tagTime = new ACPM_Tag(tag); 
								seq.atualizaTagGlobal(tag);
								seq.insereTagNoGrafo(tagTime);
							}
							String readStrOrigem = new String(b.readNextString());
							b.readNextString(); // Para tirar o '-'
							String readStrDestino = new String(b.readNextString());
							String readStrPhits = new String(b.readNextString());
							b.readNextString(); // Para tirar o ':'
							long phits = Long.parseLong(readStrPhits);
							int coluna = Integer.parseInt(b.readNextString());
							int linha = Integer.parseInt(b.readNextString());
							ACPM_Vertice vertice = new ACPM_Vertice(coluna, linha, readStrOrigem, readStrDestino, phits);
							tagTime.insereNaListaDeVertices(vertice);
						}
						int numCores = seq.getNumeroCores();
						if(numColunas*numLinhas*numAltura<numCores)
						{
							JOptionPane.showMessageDialog(null, "NoC dimensions are not enough! \nIncrease columns and lines to cover " + numCores + " vertices.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
					else if(readStr.equals(placementStr))
					{
						int linha=0, coluna=0, altura=0;
						boolean podeIncrementarLinha = false;
						new ACPM_CoreMapping(numColunas*numLinhas*numAltura);
	
						b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
						readStr = b.readNextStringInLine();
						while(!b.reachEnd()) 
						{
							if(readStr==null)
								continue;
							if(readStr.equals(""))
							{
								if(podeIncrementarLinha)
								{
									coluna = 0;
									linha++;
									podeIncrementarLinha = false;
								}
							}
							else
							{
								ACPM_CoreMapping.insertCorePosition(linha, coluna, altura,readStr);
								coluna++;
								podeIncrementarLinha = true;
							}
							readStr = b.readNextStringInLine();
						}
					}
				}
			}
		}
		catch(Exception e) 
		{
			JOptionPane.showMessageDialog(null, "Problems during " + fileS + " loading.", "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println("Problems during " + fileS + " loading.");
			e.printStackTrace();
		}
	}
}
