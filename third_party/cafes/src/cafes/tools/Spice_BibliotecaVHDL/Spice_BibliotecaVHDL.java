package cafes.tools.Spice_BibliotecaVHDL;

import java.awt.FileDialog;
import java.io.*;
import cafes.common.BuffReader;
import cafes.common.Token;
import cafes.ui.WindowPrincipal;

public class Spice_BibliotecaVHDL
{
	private static byte byteSalvo;

	public boolean executar(WindowPrincipal WP, int tipo)
	{
		switch(tipo)
		{
			case 0: // Carregar arquivo batch
				FileDialog batchFile = new FileDialog(WP, "Batch File", FileDialog.LOAD);
				batchFile.setVisible(true);
				if(batchFile.getFile()==null)
					return false;
				BufferedReader br = BuffReader.openFile(batchFile.getDirectory() + batchFile.getFile());
				while(true)
				{
					try
					{
						String line = br.readLine();
						if(line==null)
							break;
						String spiceFile = Token.getToken(line, 0);
						String vhdlFile = Token.getToken(line, 1);
						executar(spiceFile, vhdlFile);
					}
					catch(IOException e)
					{
						System.out.println("ERRO ao gerar a biblioteca VHDL!");
						e.printStackTrace();
						return false;
					}
				}
				try
				{
					br.close();
				}
				catch(IOException e)
				{
					System.out.println("ERRO ao gerar a biblioteca VHDL!");
					e.printStackTrace();
					return false;
				}
				break;
				
			case 1:
				FileDialog spiceFile = new FileDialog(WP, "Output Spice File", FileDialog.LOAD);
				spiceFile.setVisible(true);
				if(spiceFile.getFile()==null)
					return false;
				String sf = spiceFile.getDirectory() + spiceFile.getFile();
				FileDialog vhdlFile = new FileDialog(WP, "VHDL File", FileDialog.LOAD);
				vhdlFile.setVisible(true);
				if(vhdlFile.getFile()==null)
					return false;
				String vf = vhdlFile.getDirectory() + vhdlFile.getFile();
				try
				{
					executar(sf, vf);
				}
				catch(IOException e)
				{
					System.out.println("ERRO ao gerar a biblioteca VHDL!");
					e.printStackTrace();
					return false;
				}
				break;
		}
		return true;
	}
	public void executar(String arquivoPotenciaSpice, String arquivoPortaVHDL) throws IOException
	{
		String arquivoPortaVHDL_BackUp;

		arquivoPortaVHDL_BackUp = fazBackup(arquivoPortaVHDL);
		criaArquivoVHDL(arquivoPortaVHDL_BackUp, arquivoPotenciaSpice, arquivoPortaVHDL);
	}
	public static void criaArquivoVHDL(String arquivoVHDLIn, String arquivoSpiceIn, String arquivoVHDLOut) throws IOException
	{
		final int INICIO = 0;
		final int WHEN = 1;
		final int TRANSICAO = 2;
		final int SETA = 3;
		final int ENERGIA_DINAMICA = 4;
		final int POTENCIA_ESTATICA = 5;
		final int VALOR_ENERGIA_DINAMICA = 6;
		final int VALOR_POTENCIA_ESTATICA = 7;

		DataInputStream disVHDL = null;
		DataInputStream disSpice = null;
		DataOutputStream dosVHDL = null;
		try
		{
			int estadoVHDL = INICIO;

			disVHDL = new DataInputStream(new FileInputStream(arquivoVHDLIn));
			disSpice = new DataInputStream(new FileInputStream(arquivoSpiceIn));
			dosVHDL = new DataOutputStream(new FileOutputStream(arquivoVHDLOut));

			while(disVHDL.available()>0)
			{
				String strVHDL = readWriteAnalyse(disVHDL, dosVHDL);

				if(strVHDL==null)
					break;
				switch(estadoVHDL)
				{
					case INICIO:
					default:
						if(strVHDL.equalsIgnoreCase("when"))
							estadoVHDL = WHEN;
						break;

					case WHEN: //  when
						estadoVHDL = TRANSICAO;
						break;

					case TRANSICAO: //  when "????"
						if(strVHDL.equalsIgnoreCase("=>"))
							estadoVHDL = SETA;
						else
							estadoVHDL = INICIO;
						break;

					case SETA: //  when "????" =>
						if(strVHDL.equalsIgnoreCase("energiaDin"))
							estadoVHDL = ENERGIA_DINAMICA;
						else
						{
							if(strVHDL.equalsIgnoreCase("potenciaEst"))
								estadoVHDL = POTENCIA_ESTATICA;
							else
								estadoVHDL = INICIO;
						}
						break;

					case ENERGIA_DINAMICA: //  when "????" => energiaDin
						if(strVHDL.equalsIgnoreCase(":="))
							estadoVHDL = VALOR_ENERGIA_DINAMICA;
						else
							estadoVHDL = INICIO;
						break;

					case VALOR_ENERGIA_DINAMICA: //  when "????" => energiaDin :=
						strVHDL = getEnergiaDinamica(disSpice) + ";";
						estadoVHDL = INICIO;
						break;

					case POTENCIA_ESTATICA: //  when "????" => potenciaEst
						if(strVHDL.equalsIgnoreCase(":="))
							estadoVHDL = VALOR_POTENCIA_ESTATICA;
						else
							estadoVHDL = INICIO;
						break;

					case VALOR_POTENCIA_ESTATICA: //  when "????" => potenciaEst :=
						strVHDL = getPotenciaEstatica(disSpice) + ";";
						estadoVHDL = INICIO;
						break;
				}
				dosVHDL.writeBytes(strVHDL);
				dosVHDL.writeByte(byteSalvo);
			}
			disVHDL.close();
			disSpice.close();
			dosVHDL.close();
		}
		catch(IOException e)
		{
			if(disVHDL != null)
				disVHDL.close();
			if(disSpice != null)
				disSpice.close();
			if(dosVHDL != null)
				dosVHDL.close();
		}
	}
	public static boolean isSpace(byte b)
	{
		switch(b)
		{
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				return true;
		}
		return false;
	}
	public static String readWriteAnalyse(DataInputStream dis, DataOutputStream dos) throws IOException
	{
		byte bArray[] = new byte[1000];
		int posicao = 0;

		while(dis.available()>0)
		{
			byte b = dis.readByte();

			if(isSpace(b))
			{
				if(posicao>0)
				{
					byteSalvo = b;
					return new String(bArray).trim();
				}
				dos.writeByte(b);
			}
			else
				bArray[posicao++] = b;
		}
		return null;
	}
	public static String getEnergiaDinamica(DataInputStream dis) throws IOException
	{
		return getPotenciaOuEnergia(dis, false);
	}
	public static String getPotenciaEstatica(DataInputStream dis) throws IOException
	{
		return getPotenciaOuEnergia(dis, true);
	}
	public static String readFloat(DataInputStream dis) throws IOException
	{
		String str = readString(dis);
		try
		{
			Float.parseFloat(str);
		}
		catch(NumberFormatException e)
		{
			return null;
		}
		return str;
	}
	public static String readString(DataInputStream dis) throws IOException
	{
		byte bArray[] = new byte[1000];
		int posicao = 0;

		while(dis.available()>0)
		{
			byte b = dis.readByte();
			
			if(isSpace(b))
			{
				if(posicao>0)
					return new String(bArray).trim();
			}
			else
				bArray[posicao++] = b;
		}
		return null;
	}
	public static String getPotenciaOuEnergia(DataInputStream dis, boolean ehPotencia) throws IOException
	{
		String potenciaMediaStr = "potmedia";
		
		while(dis.available()>0)
		{
			String str = readString(dis);
			
			if(str.length()<potenciaMediaStr.length())
				continue;
			if(str.substring(0, potenciaMediaStr.length()).equalsIgnoreCase(potenciaMediaStr))
			{
//  potmedia???=  4.5964E-05  from=  4.5000E-08	 to=  5.5000E-08
				str = readFloat(dis);
				if(str==null)
					continue;
				if(ehPotencia)	
					return str;
				readString(dis);
				String strInicio = readFloat(dis);
				if(strInicio==null)
				{
					System.out.print("Problema com o arquivo de entrada faltando ");
					System.out.print(dis.available());
					System.out.println(" bytes");
					
					throw new IOException();
				}
				readString(dis);
				String strFim = readFloat(dis);
				if(strFim==null)
				{
					System.out.print("Problema com o arquivo de entrada faltando ");
					System.out.print(dis.available());
					System.out.println(" bytes");
					
					throw new IOException();
				}
				float tempoInicio = Float.parseFloat(strInicio);
				float tempoFim = Float.parseFloat(strFim);
				float deltaTempo = tempoFim - tempoInicio;
				float energia = Float.parseFloat(str);
				energia = energia * deltaTempo;
				return floatToStringFormatado(energia);
			}
		}
		return null;
	}
	public static String floatToStringFormatado(float energia)
	{
		String str = new Float(energia).toString();
		String str2 = "";
		int estado = 0;
		boolean encontrouE = false;

		for(int i=0; i<str.length(); i++)
		{
			String caracter = str.substring(i, i+1);
			
			switch(estado)
			{
				case 0: // Espera casa decimal
					str2 = str2 + caracter;
					estado++;
					break;

				case 1: // Espera o ponto
					if(caracter.equalsIgnoreCase("E"))
					{
						encontrouE = true;
						str2 = str2 + ".";
						for(int j=2; j<5; j++)
							str2 = str2 + "0";
						estado = 10;
					}
					str2 = str2 + caracter;
					estado++;
					break;

				case 2: // Espera o primeira casa depois do ponto
				case 3: // Espera o segunda casa depois do ponto
				case 4: // Espera o terceira casa depois do ponto
				case 5: // Espera o quarta casa depois do ponto
					if(caracter.equalsIgnoreCase("E"))
					{
						encontrouE = true;
						for(int j=estado; j<=5; j++)
							str2 = str2 + "0";
					}
					str2 = str2 + caracter;
					estado++;
					break;

				default: // Espera demais caracteres
					if(encontrouE==true || caracter.equalsIgnoreCase("E"))
					{
						encontrouE = true;
						str2 = str2 + caracter;
						estado++;
					}
					break;
			}
		}
		return str2;
	}
	public static String fazBackup(String arquivo) throws IOException
	{
		String arquivoBackUp = arquivo + ".bak";
		
		DataInputStream dis = new DataInputStream(new FileInputStream(arquivo)); 
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(arquivoBackUp)); 
		while(dis.available()>0)
			dos.writeByte(dis.readByte());
		dis.close();
		dos.close();

		return arquivoBackUp;
	}
}

