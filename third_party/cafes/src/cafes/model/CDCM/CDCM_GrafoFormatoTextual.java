package cafes.model.CDCM;

import cafes.ui.WindowPrincipal;
import java.awt.*;
import java.io.*;
import javax.swing.*;

import cafes.common.*;

class CDCM_GrafoFormatoTextual
{
	private static String net_sizeStr = "#_NoC_Size";
	private static String nodesStr = "#_CDCG_Vertices";
	private static String GraphicStr = "#_CDCG_Graphic";
	private static String EdgeStr = "#_CDCG_Edges";
	private static String placementStr = "#_CDCG2NoC_Mapping";
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
		if(ArquivoModelo.getArquivo() != null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".CDCG"));
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
	public static void gravaArquivo(WindowPrincipal WP, JFrame win, CDCM_Sequencia seq, String title, String placement)
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura= WP.getNumAltura();
		FileDialog d = new FileDialog(win, title, FileDialog.SAVE);
		if(ArquivoModelo.getArquivo() != null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".CDCG"));
		}
		d.setVisible(true);
		if(d.getFile() == null)
			return;
		ArquivoModelo.setDiretorioArquivo(d.getDirectory(), d.getFile());
		String fileS = d.getDirectory() + d.getFile();
		File file = new File(fileS);
		FileOutputStream fileOut;
		try
		{
			String net_sizeStrWrite = new String(net_sizeStr + " (lines columns)");
			String nodesStrWrite = new String(nodesStr + " (list of: vertices --> IDCore sourceCore - targetCore phits : computation)");
			String GraphicStrWrite = new String(GraphicStr + " (list of: IDCore x y)");
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
			return CDCM_Grafo.START;
			if(strID.equals("END"))
				return CDCM_Grafo.END;
			return Integer.parseInt(strID);
		}
		catch(NumberFormatException n)
		{
			return -10;
		}
	}
	public static void leArquivo(WindowPrincipal WP, JFrame win, CDCM_Sequencia seq, String title)
	{
		FileDialog d = new FileDialog(win, title, FileDialog.LOAD);
		if(ArquivoModelo.getArquivo() != null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".CDCG"));
		}
		d.setVisible(true);
		if(d.getFile() == null)
			return;
		ArquivoModelo.setDiretorioArquivo(d.getDirectory(), d.getFile());
		String fileName = d.getDirectory() + d.getFile();
//		String fileName= "F:\\CAFES\\V_3.7\\Arquivos\\CDCG\\b.cdg";
		leArquivo(WP, seq, fileName);
	}
	public static void leArquivo(WindowPrincipal WP, CDCM_Sequencia seq, String fileName)
	{
		File file = new File(fileName);
		try
		{
			DataInputStream fileIn = new DataInputStream(new FileInputStream(file));
			byte bytesArq[] = new byte[fileIn.available()];
			fileIn.readFully(bytesArq);
			int numLinhas=0, numColunas=0, numAltura=0;
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
						numAltura = Integer.parseInt(numAlturaStr.trim());
						readStr = b.readNextString();
					}
					else if(readStr.equals(nodesStr)) // L� apenas os v�rtices da NoC
					{
						int coluna=CDCM_Circulo.getRaio()*2, linha=CDCM_Circulo.getRaio()*2;
						
						b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
						if(seq.encontraVerticeNoGrafo(CDCM_Grafo.START)==null)
							seq.insereVerticeStartGrafo();
						if(seq.encontraVerticeNoGrafo(CDCM_Grafo.END)==null)
							seq.insereVerticeEndGrafo();
						while(true)
						{
							readStr = b.readNextString();
							if(readStr==null || readStr.equals("") || b.reachEnd() || isToken(readStr))
								break;
							int id = converteStringIDParaInt(readStr);
							String readStrOrigem = new String(b.readNextString());
							b.readNextString(); // Para tirar o '-'
							String readStrDestino = new String(b.readNextString());
							String readStrPhits = new String(b.readNextString());
							long phits = Long.parseLong(readStrPhits);
							b.readNextString(); // Para tirar o ':'
							String readStrComputacao = new String(b.readNextString());
							int computacao = Integer.parseInt(readStrComputacao);
							CDCM_Vertice vertice = seq.encontraVerticeNoGrafo(id);
							if(vertice==null)
							{
								vertice = new CDCM_Vertice(coluna, linha, id+1, readStrOrigem, readStrDestino, phits, computacao);
								seq.insereVerticeGrafo(vertice);
							}
							else
							{
								vertice.setCoreOrigem(readStrOrigem);
								vertice.setCoreDestino(readStrDestino);
								vertice.setPhits(phits);
								vertice.setComputacao(computacao);
							}
							coluna = coluna + CDCM_Circulo.getRaio()*3;
							if(coluna+CDCM_Circulo.getRaio()*2 >= seq.getDesenhaSequencia().getMaxColuna())
							{
								coluna = CDCM_Circulo.getRaio()*3;
								linha = linha + CDCM_Circulo.getRaio()*3;
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
							CDCM_Vertice vertice = seq.encontraVerticeNoGrafo(id);
							if(CDCM_Vertice.id<=id)
								CDCM_Vertice.id = id+1;
							if(vertice!=null)
							{
								JOptionPane.showMessageDialog(null, "Invalid id (" + id + "). Vertex was declared before.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							vertice = new CDCM_Vertice(x, y, id);
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
							CDCM_Vertice vertice = seq.encontraVerticeNoGrafo(converteStringIDParaInt(readStr));
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
								CDCM_Vertice verticeDep = seq.encontraVerticeNoGrafo(converteStringIDParaInt(readStr));
								vertice.insereNaListaDeDependentes(new CDCM_VerticeDependente(verticeDep));
							} while(!b.reachEnd()); 
						}
					}
					else if(readStr.equals(placementStr))
					{
						int linha=0, coluna=0, altura=0;
						boolean podeIncrementarLinha = false;
						new CDCM_CoreMapping(numColunas*numLinhas*numAltura);
	
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
								CDCM_CoreMapping.insertCorePosition(linha, coluna, altura,readStr);
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
			JOptionPane.showMessageDialog(null, "Problems during " + fileName + " loading.", "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println("Problems during " + fileName + " loading.");
			e.printStackTrace();
		}
	}
}
