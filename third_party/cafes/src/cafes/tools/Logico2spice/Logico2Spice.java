package cafes.tools.Logico2spice;

import java.awt.FileDialog;
import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import cafes.common.BuffReader;
import cafes.common.Token;
import cafes.ui.WindowPrincipal;

public class Logico2Spice
{
	Vector<String> vetorFloat;
	
	public Logico2Spice(WindowPrincipal WP)
	{
		vetorFloat = new Vector<String>();
		FileDialog fd = new FileDialog(WP, "VHDL waveform file", FileDialog.LOAD);
		fd.setVisible(true);
		if(fd.getFile()==null)
			return;
		BufferedReader br = BuffReader.openFile(fd.getDirectory() + fd.getFile());
		DataOutputStream dos = abreArquivoOut(fd.getDirectory() + fd.getFile() + ".out");
		int periodo = 10;
		try
		{
			if(executar(br, dos, periodo)==false)
				JOptionPane.showMessageDialog(null, "ERRO na conversão do waveform!", "Waveform Conversion", 0);
			else
				JOptionPane.showMessageDialog(null, "Conversão efetuada com sucesso!", "Waveform Conversion", 0);
				
		}
		catch(Exception e)
		{
			System.out.println("Problemas na conversão!");
		}
	}
	public boolean executar(BufferedReader br, DataOutputStream dos, int periodo) throws Exception 
	{
		String line;
		String line0 = "";
		int numeroSinais = 0;
		int linhas = 0;

		line = br.readLine();
		if(line==null)
			return false;
		while(true)	// Lê o nome dos sinais
		{
			String str = Token.getToken(line, numeroSinais + 2);
			if(str==null)
				break;
			line0 = line0 + filtraSinal(str) + " ";
			numeroSinais++;
		}
		while(true)
		{
			line = br.readLine();
			if(line==null)
				break;
			int inicio = 2;
			String str = Token.getToken(line, inicio);
			if(Character.isLetter(str.charAt(0)))
				inicio++;
			for(int i=0; i<numeroSinais; i++)
			{
				int posicao = linhas*numeroSinais+i;
				
				str = Token.getToken(line, i + inicio);
				if(str.equals("0"))
					vetorFloat.add(posicao, "0.0");
				else
					vetorFloat.add(posicao, "3.3");
			}
			linhas++;
		}
		br.close();
		for(int i=0; i<numeroSinais; i++)
		{
			int k = 0;
			int t = 0;
			String str = Token.getToken(line0, i);
			dos.writeBytes("V" + str + " " +  str + " 0 pwl( ");
			for(int l=0; l<linhas; l++)
			{
				int posicao = (l*numeroSinais) + i; 
				if(++k > 3)
				{ 
					dos.writeBytes("\n+  "); // Limita o tamanho da linha
					k = 0;
				}
				dos.writeBytes(t + "n " + vetorFloat.get(posicao) + " " + (t+periodo-0.1) + "n " + vetorFloat.get(posicao) + "   ");
				t = t + periodo;
			}
			dos.writeBytes(" )\n");
		}
		return true;
	}
	public static DataOutputStream abreArquivoOut(String str)
	{
		String arquivo = new String(str);
		DataOutputStream out = null;
		
		try
		{
			out = new DataOutputStream(new FileOutputStream(arquivo));
		}
		catch(Exception e)
		{
			System.out.println("Nao eh possivel abrir arquivo: " + arquivo);
		}
		return out;
	}
	String filtraSinal(String strIn)
	{
		char p[] = strIn.toCharArray();
		String str = "";
				
		for(int i=0; i<p.length; i++)
		{
			if(p[i]=='(' || p[i]==')')
				str = str + '_';
			else
			{
				if(p[i]=='/')
					str = "";
				else
					str = str + p[i];
			}
		}
		return str;
	}
}
