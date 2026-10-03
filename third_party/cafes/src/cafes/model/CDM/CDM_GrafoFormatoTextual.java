package cafes.model.CDM;

import cafes.ui.WindowPrincipal;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import cafes.common.*;

class CDM_GrafoFormatoTextual
{
	private static String net_sizeStr = "#_NoC_Size";
	private static String nodesStr = "#_CDG_Vertices";
	private static String GraphicStr = "#_CDG_Graphic";
	private static String EdgeStr = "#_CDG_Edges";
	private static String placementStr = "#_CDG2NoC_Mapping";
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
		if(teste.equals(EdgeStr))
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
			d.setFile(ArquivoModelo.appenda(".CDG"));
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
			//TODO
			fileOut.close();
		}
		catch(Exception e) 
		{
			System.out.println("Problems during " + fileS + " generation.");
			e.printStackTrace();
		}
	}
	public static void gravaArquivo(WindowPrincipal WP, JFrame win, CDM_Sequencia seq, String title, String placement)
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura= WP.getNumAltura();

		FileDialog d = new FileDialog(win, title, FileDialog.SAVE);
		if(ArquivoModelo.getArquivo()!=null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".cdg"));
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
			String nodesStrWrite = new String(nodesStr + " (list of: vertices --> IDCore sourceCore - targetCore phits)");
			String GraphicStrWrite = new String(GraphicStr + " (list of: IDcore x y)");
			String EdgeStrWrite = new String(EdgeStr + " (list of: dependent vertices)");
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
			
			fileOut.write(EdgeStrWrite.getBytes());
			fileOut.write(paragrafoStr.getBytes());
			fileOut.write(seq.stringArestas().getBytes());
			fileOut.write(paragrafoStr.getBytes());
			
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
	private static int converteStringIDParaInt(String strIdentificacao)
	{
		String strID = strIdentificacao.trim();
		
		try
		{
			if(strID.equals("START"))
			return CDM_Grafo.START;
			if(strID.equals("END"))
				return CDM_Grafo.END;
			return Integer.parseInt(strID);
		}
		catch(NumberFormatException n)
		{
			return -10;
		}
	}
	public static void leArquivo(WindowPrincipal WP, JFrame win, CDM_Sequencia seq, String title)
	{
 		FileDialog d = new FileDialog(win, title, FileDialog.LOAD);
		if(ArquivoModelo.getArquivo()!=null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".CDG"));
		}
		d.setVisible(true);
		if(d.getFile()==null)
			return;
		ArquivoModelo.setDiretorioArquivo(d.getDirectory(), d.getFile());
		String fileS = d.getDirectory() + d.getFile();
//		String fileS= "F:\\CAFES\\V_3.7\\Arquivos\\CDG\\b.cdg";
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
				if(readStr != null)
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
						numAltura= Integer.parseInt(numAlturaStr.trim());
						readStr = b.readNextString();
					}
					else if(readStr.equals(nodesStr)) // L� apenas os v�rtices da NoC
					{
						int coluna=CDM_Circulo.getRaio()*2, linha=CDM_Circulo.getRaio()*2;
						
						b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
						if(seq.encontraVerticeNoGrafo(CDM_Grafo.START)==null)
							seq.insereVerticeStartGrafo();
						if(seq.encontraVerticeNoGrafo(CDM_Grafo.END)==null)
							seq.insereVerticeEndGrafo();
						while(true)
						{
							readStr = b.readNextString();
							if(readStr==null || readStr.equals("") || b.reachEnd() || isToken(readStr))
								break;
							String readStrOrigem = new String(b.readNextString());
							b.readNextString(); // Para tirar o '-'
							String readStrDestino = new String(b.readNextString());
							String readStrPhits = new String(b.readNextString());
							int id = converteStringIDParaInt(readStr);
							long phits = Long.parseLong(readStrPhits);
							CDM_Vertice vertice = seq.encontraVerticeNoGrafo(id);
							if(vertice==null)
							{
								vertice = new CDM_Vertice(coluna, linha, id+1, readStrOrigem, readStrDestino, phits);
								seq.insereVerticeGrafo(vertice);
							}
							else
							{
								vertice.setCoreOrigem(readStrOrigem);
								vertice.setCoreDestino(readStrDestino);
								vertice.setPhits(phits);
							}
							coluna = coluna + CDM_Circulo.getRaio()*3;
							if(coluna+CDM_Circulo.getRaio()*2 >= seq.getDesenhaSequencia().getMaxColuna())
							{
								coluna = CDM_Circulo.getRaio()*3;
								linha = linha + CDM_Circulo.getRaio()*3;
							}
						}
						int numCores = seq.getNumeroCores();
						if(numColunas*numLinhas*numAltura<numCores)
						{
							JOptionPane.showMessageDialog(null, "NoC dimensions are not enough! \nIncrease columns and lines to cover " + numCores + " vertices.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
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
							int id = converteStringIDParaInt(readStr);
							x = Integer.parseInt(b.readNextString().trim());
							y = Integer.parseInt(b.readNextString().trim());
							CDM_Vertice vertice = seq.encontraVerticeNoGrafo(id);
							if(CDM_Vertice.id<=id)
								CDM_Vertice.id = id+1;
							if(vertice!=null)
							{
								JOptionPane.showMessageDialog(null, "Invalid id (" + id + "). Vertex was declared before.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							vertice = new CDM_Vertice(x, y, id);
							seq.insereVerticeGrafo(vertice);
							readStr = b.readNextString();
						}
					}
					else if(readStr.equals(EdgeStr))
					{
						b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
						readStr = b.readNextString();
						while(!isToken(readStr) && !b.reachEnd()) 
						{
							CDM_Vertice vertice = seq.encontraVerticeNoGrafo(converteStringIDParaInt(readStr));
							if(vertice==null)
							{
								readStr = b.readNextString();
								continue;
							}
							do
							{
								readStr = b.readNextStringInLine();
								if(readStr==null)
									continue;
								if(readStr.equals(""))
								{
									readStr = b.readNextString();
									break;  // Terminou todas as depend�ncias do v�rtice
								}
								CDM_Vertice verticeDep = seq.encontraVerticeNoGrafo(converteStringIDParaInt(readStr));
								vertice.insereNaListaDeDependentes(new CDM_VerticeDependente(verticeDep));
							} while(!b.reachEnd()); 
						}
					}
					else if(readStr.equals(placementStr))
					{
						int linha=0, coluna=0, altura=0;
						boolean podeIncrementarLinha = false;
						new CDM_CoreMapping(numColunas*numLinhas*numAltura);
	
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
								CDM_CoreMapping.insertCorePosition(linha, coluna, altura,readStr);
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
