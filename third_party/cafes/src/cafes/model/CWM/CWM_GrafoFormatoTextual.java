package cafes.model.CWM;

import cafes.ui.WindowPrincipal;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import cafes.common.*;

class CWM_GrafoFormatoTextual
{
	private static String net_sizeStr = "#_NoC_Size";
	private static String nodesStr = "#_CWG_Vertices";
	private static String GraphicStr = "#_CWG_Graphic";
	private static String EdgeStr = "#_CWG_Edges";
	private static String placementStr = "#_CWG2NoC_Mapping";
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
			d.setFile(ArquivoModelo.appenda(".CWG"));
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
			fileOut.close();
		}
		catch(Exception e) 
		{
			System.out.println("Problems during " + fileS + " generation.");
			e.printStackTrace();
		}
	}
	public static void gravaArquivo(WindowPrincipal WP, JFrame win, CWM_Sequencia seq, String title, String placement)
	{
		int numColunas = WP.getNumColunas();
		int numLinhas = WP.getNumLinhas();
		int numAltura = WP.getNumAltura();

		if(numColunas*numLinhas*numAltura<seq.getNumeroDeVertices())
		{
			JOptionPane.showMessageDialog(null, "Invalid size of NoC! Enter new LINES, COLUMNS and HEIGHT that covers " + seq.getNumeroDeVertices()+" vertices.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		FileDialog d = new FileDialog(win, title, FileDialog.SAVE);
		if(ArquivoModelo.getArquivo()!=null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".CWG"));
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
			String nodesStrWrite = new String(nodesStr + " (list of: vertices)");
			String GraphicStrWrite = new String(GraphicStr + " (list of: core x y)");
			String EdgeStrWrite = new String(EdgeStr + " (list of: sourceVertex - targetVertex numberOfPhitsTransmited)");
			String placementStrWrite = new String("\n" + placementStr + " (matrix of: vertices)");

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
	public static void leArquivo(WindowPrincipal WP, JFrame win, CWM_Sequencia seq, String title)
	{
		FileDialog d = new FileDialog(win, title, FileDialog.LOAD);
		if(ArquivoModelo.getArquivo()!=null)
		{
			d.setDirectory(ArquivoModelo.getDiretorio()); 
			d.setFile(ArquivoModelo.appenda(".CWG"));
		}
		d.setVisible(true);
		if(d.getFile()==null)
			return;
		leArquivoSemDialog(WP, seq, d.getDirectory(), d.getFile());
	}	

	public static void leArquivoSemDialog(WindowPrincipal WP, CWM_Sequencia seq, String directory, String filename)
	{
		ArquivoModelo.setDiretorioArquivo(directory, filename);
		String fileS = directory + filename;
		File file = new File(fileS);
		boolean temInformacaoGrafica = false;
		try
		{
			DataInputStream fileIn = new DataInputStream(new FileInputStream(file));
			byte bytesArq[] = new byte[fileIn.available()];
			fileIn.readFully(bytesArq);
			int numLinhas=0,altura=0, numColunas=0;
			ByteArrayParser b = new ByteArrayParser(bytesArq);
			String readStr = new String("");
			
			while(!b.reachEnd())
			{
				while(!isToken(readStr) && !b.reachEnd())
					readStr = b.readNextString();
				if(readStr == null)
					continue;
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
					altura = Integer.parseInt(numAlturaStr.trim());
					readStr = b.readNextString();
				}
				else if(readStr.equals(nodesStr)) // L� apenas os v�rtices da NoC ou tamb�m o posicionamento gr�fico 
				{
					int numVertices=0, coluna=CWM_Circulo.getRaio()*2, linha=CWM_Circulo.getRaio()*2;
					
					b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
					readStr = b.readNextString();
					if(temInformacaoGrafica)
						continue;
					seq.deleta();
					while(readStr!=null && !readStr.equals("") && !b.reachEnd() && !isToken(readStr))
					{
						seq.insereVerticeGrafo(new CWM_Vertice(coluna, linha, readStr));
						readStr = b.readNextString();
						coluna = coluna + CWM_Circulo.getRaio()*3;
						numVertices++;
						if(coluna+CWM_Circulo.getRaio()*2>=seq.getDesenhaSequencia().getMaxColuna())
						{
							coluna = CWM_Circulo.getRaio()*3;
							linha = linha + CWM_Circulo.getRaio()*3;
						}
					}
					if(numColunas*numLinhas*altura<numVertices)
					{
						JOptionPane.showMessageDialog(null, "Invalid size of NoC! Enter new X and Y that covers " + numVertices + " vertices.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				else if(readStr.equals(GraphicStr))
				{
					int x, y;
					
					b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
					temInformacaoGrafica = true;
					seq.deleta();
					readStr = b.readNextString();
					while(!isToken(readStr) && !b.reachEnd()) 
					{
						if(readStr.equals(""))
						{
							readStr = b.readNextString();
							continue;
						}
						x = Integer.parseInt(b.readNextString().trim());
						y = Integer.parseInt(b.readNextString().trim());
						seq.insereVerticeGrafo(new CWM_Vertice(x, y, readStr));
						readStr = b.readNextString();
					}
				}
				else if(readStr.equals(EdgeStr))
				{
					b.readUntilNotEndOfLine(); // para eliminar todo o coment�rio
					readStr = b.readNextString();
					while(!isToken(readStr) && !b.reachEnd()) 
					{
						if(readStr.equals(""))
						{
							readStr = b.readNextString();
							continue;
						}
						String origem = new String(readStr);
						b.readNextString();
						String destino = b.readNextString();
						readStr = b.readNextString();
						long peso = Long.parseLong(readStr.trim());
						CWM_Vertice verticeOrigem = seq.verticeDoCore(origem);
						CWM_Vertice verticeDestino = seq.verticeDoCore(destino);
						
						verticeOrigem.insereNaListaDeAdjacentes(new CWM_VerticeAdjacente(verticeDestino, peso));
						readStr = b.readNextString();
					}
				}
				else if(readStr.equals(placementStr))
				{
					int linha=0, coluna=0;
					boolean podeIncrementarLinha = false;
					new CWM_CoreMapping(numColunas*numLinhas*altura);

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
							CWM_CoreMapping.insertCorePosition(linha, coluna, altura,readStr);
							coluna++;
							podeIncrementarLinha = true;
						}
						readStr = b.readNextStringInLine();
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
