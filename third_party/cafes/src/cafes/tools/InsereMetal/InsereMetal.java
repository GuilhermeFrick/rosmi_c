package cafes.tools.InsereMetal;

import cafes.ui.WindowPrincipal;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import cafes.common.*;

public class InsereMetal
{
	public InsereMetal(WindowPrincipal WP)
	{
		FileDialog fd = new FileDialog(WP, "VHDL input NoC file", FileDialog.LOAD);
		fd.setVisible(true);
		if(fd.getFile()==null)
			return;
		BufferedReader br = BuffReader.openFile(fd.getDirectory() + fd.getFile());
		DataOutputStream dos = abreArquivoOut(fd.getDirectory() + fd.getFile() + ".out");
		try
		{
			if(executar(br, dos)==false)
				JOptionPane.showMessageDialog(null, "ERRO na insersão de linhas de metal!", "Insersão de Metal", 0);
			else
				JOptionPane.showMessageDialog(null, "Insersão efetuada com sucesso!", "Insersão de Metal", 0);
				
		}
		catch(Exception e)
		{
			System.out.println("Problemas na insersão de metal!");
		}
	}
	public boolean executar(BufferedReader br, DataOutputStream dos) throws Exception 
	{
		String line;
		String tipoMetal;
		String Metal1 = "Metal1";
		String Metal2 = "Metal2";
		int contador = 0;

		while(true)
		{
			line = br.readLine();
			if(line==null)
				break;
			String str = Token.getToken(line, 0);
			if(str!=null)
			{
				if(str.equals("begin"))
				{
					dos.writeBytes(line + "\n");
					break;
				}
			}
			dos.writeBytes(line + "\n");
		}
		while(true)
		{
			String str = Token.getToken(line, 0);
			if(str==null)
			{
				dos.writeBytes(line + "\n");
				continue;
			}
			String str1 = Token.getToken(line, 1);
			if(str1!=null && str1.equals("entradas"))
			{
				dos.writeBytes(line + "\n");
				break;
			}
			line = br.readLine();
			dos.writeBytes(line + "\n");
			String sinalEsq;
			String sinalDir;
			for(int i=0; i<13; i++)
			{
				line = br.readLine();
				if(line==null)
					return false;
				switch(i)
				{
					case 0: // Entity
					case 2: // port map
					case 3: // clock
					case 4: // reset
						dos.writeBytes(line + "\n");
						break;

					case 1: // generic map
						break;

					case 5: // clock_rx
					case 6: // rx
					case 8: // credit_o
					case 9: // clock_tx
					case 10: // tx
					case 12: // credit_i
						sinalEsq = Token.getToken(line, 0);
						sinalDir = filtraSinal(Token.getToken(line, 2));
						for(int j=4; j>=0; j--)
							dos.writeBytes(sinalEsq + "_" + j + " => " + sinalDir + "(" + j + ")," + "\n");
						break;

					case 7: // data_in
					case 11: // data_out
						sinalEsq = Token.getToken(line, 0);
						sinalDir = filtraSinal(Token.getToken(line, 2));
						for(int j=4; j>=0; j--)
						{
							for(int k=7; k>=0; k--)
								dos.writeBytes(sinalEsq + "_" + j + "_" + k + " => " + sinalDir + "(" + j + ")" + "(" + k + ")," + "\n");
						}
						break;
				}
			}
			line = br.readLine();
			if(line==null)
				return false;
		}
		while(true)
		{
			line = br.readLine();
			if(line==null)
				return false;
			String str = Token.getToken(line, 0);
			if(str==null || str.equals("--"))
			{
				dos.writeBytes(line + "\n");
				continue;
			}
			if(str.equals("end"))
			{
				dos.writeBytes(line + "\n");
				break;
			}
			if((contador%24)<16)
				tipoMetal = Metal2;
			else
				tipoMetal = Metal1;
			if(((contador%24)%4)==0)
				dos.writeBytes("\n");
			String sSaida = sinalSaida(line);
			String sEntrada = sinalEntrada(line);
			if(((contador%24)%4)==2)
			{
				String sE;
				
				for(int i=0; i<8; i++)
				{
					if(sEntrada.equals("(others=>'0')"))
						sE = "'0'";
					else
						sE = sEntrada + "(" + i + ")";
					dos.writeBytes("\tm" + contador + "_" + i + ": Entity work." +  tipoMetal + "(" + tipoMetal + ") port map(Y=>" + sSaida + "(" + i + ")" + ", A=>" + sE + ");\n");
				}
			}
			else
				dos.writeBytes("\tm" + contador + ": Entity work." +  tipoMetal + "(" + tipoMetal + ") port map(Y=>" + sSaida + ", A=>" + sEntrada + ");\n");
			contador++;
		}
		br.close();
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
	String sinalSaida(String strIn)
	{
		char p[] = strIn.toCharArray();
				
		for(int i=0; i<p.length; i++)
		{
			if(p[i]=='<' &&  p[i+1]=='=')
				return filtraSinal(strIn.substring(0, i));
		}
		return null;
	}
	String sinalEntrada(String strIn)
	{
		char p[] = strIn.toCharArray();
				
		for(int i=0; i<p.length; i++)
		{
			if(p[i]=='<' &&  p[i+1]=='=')
				return filtraSinal(strIn.substring(i+2));
		}
		return null;
	}
	String filtraSinal(String strIn)
	{
		char p[] = strIn.toCharArray();
		String str = "";
		
		for(int i=0; i<p.length; i++)
		{
			if(p[i]!=' ' && p[i]!='\t' && p[i]!=';' && p[i]!=',')
				str = str + p[i];
		}
		return str;
	}
}
