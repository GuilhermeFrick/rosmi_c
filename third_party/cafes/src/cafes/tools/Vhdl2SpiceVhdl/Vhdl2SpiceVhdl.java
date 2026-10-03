package cafes.tools.Vhdl2SpiceVhdl;

import java.io.*;
import cafes.common.*;

public class Vhdl2SpiceVhdl
{
	private static final String VCC = "VCC";

	private static final int ESPERA_PACKAGE = 0;
	private static final int POE_FUNOUT = 1;
	private static final int ESPERA_ENTITY = 2;
	private static final int PEGA_INPUT_OUTPUT = 3;
	private static final int ESPERA_BEGIN = 4;
	private static final int PEGA_ASSIGN = 5;
	private static final int PEGA_PORTAS = 6;
	
	public static void imprimeFim(String strInput, String strOutput, DataOutputStream out) throws IOException
	{
	String strAux;
		String str =  "\n\n*********************************************************************************************";
		
		str = str + "\n\n.options nomod nopage post=2";
		str = str + "\n\n.tran 0.1n 10000n\n";
		str = str + "\n.plot\n";
		str = str + "+ I(" + VCC + ")\n";
		str = str + "+ V(" + VCC + ")\n";
		int idx = 0;
		while(true)
		{
			strAux = Token.getToken(strInput, idx++);
			if(strAux==null)
				break;
			str = str + "+ V(" + strAux + ")\n";
		} 
		idx = 0;
		while(true)
		{
			strAux = Token.getToken(strOutput, idx++);
			if(strAux==null)
				break;
			str = str + "+ V(" + strAux + ")\n";
		}
		str = str + "\n.end\n";
		out.writeBytes(str);
	}
	public static String stringLibs()
	{
	String str = "\n" + ".inc ../../library  \t\t * Modelo CMOS";

	str = str +  "\n" + ".inc ../../cells.lib\t\t * Biblioteca de células";
	str = str +  "\n" + ".inc input.txt	  \t\t * Entrada de sinais";
	str = str +  "\n";
	
	return str;
	}
	public static DataOutputStream abreArquivoOut(String str, String aux)
	{
		String arquivo = new String(str + aux);
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
	public static void erroFormato()
	{
		System.out.println("Usar: Vhdl2SpiceVhdl arquivo_VHDL <formato_VHDL>\n");
		System.out.println("\tformato_VHDL:");
		System.out.println("\t\t0 (default): {porta}");
		System.out.println("\t\t1: work.components.{porta}");
	}
	@SuppressWarnings("fallthrough")
	public int executar(String s[]) throws IOException
	{
		System.out.println("\nConversor VHDL Estrutural para SPICE e VHDL Estrutural com funout - Versao 1.1\n");

		if(s.length==0)
		{
			erroFormato();
			return -1;
		}
		BufferedReader bin = BuffReader.openFile(s[0]);
		if(bin==null)
			return -1;
		DataOutputStream out1 = abreArquivoOut(s[0], ".out.vhd");
		if(out1==null)
			return -1;
		DataOutputStream out2 = abreArquivoOut(s[0], ".sp");
		if(out2==null)
			return -1;
		int estado = ESPERA_ENTITY;
		int offSetPorta = 6;
		int offSetFunOut = 0;
		int offSetPortMap = 3;
		if(s.length==2)
		{
			switch(Integer.valueOf(s[1]).intValue())
			{
				case 0:
					estado = ESPERA_ENTITY;
					offSetPorta = 6;
					offSetFunOut = 0;
					offSetPortMap = 3;
					break;
					
				case 1:
					estado = ESPERA_PACKAGE;
					offSetPorta = 8;
					offSetFunOut = 2;
					offSetPortMap = 5;
					break;

				default:
					erroFormato();
					return -1;
			}
		}
		FunOut funOut = new FunOut(bin, offSetFunOut);
		bin.close();
		bin = BuffReader.openFile(s[0]);
		if(bin==null)
			return -1;
		String lineOut = "";
		String strAux = "";
		String str = "";
		String strInput = "";
		String strOutput = "";
		VetorGates vetGates = new VetorGates();
		VetorAssign vetAssign = new VetorAssign(200);
		int count = 0;
		
		while(true)
		{
			String line = bin.readLine();
			if(line==null)
				break;
			switch(estado)
			{
				case ESPERA_PACKAGE:
					out1.writeBytes(line + "\n");
					str = Token.getToken(line, 0);
					if(str!=null)
					{
						if(str.equals("package"))
							estado = POE_FUNOUT;
					}
					break;

				case POE_FUNOUT:
					str = Token.getToken(line, 0);
					if(str!=null)
					{
						if(str.equals("package"))
						{
							out1.writeBytes(line + "\n");
							estado = ESPERA_ENTITY;
							break;
						}
						if(str.equals("component"))
						{
							str = Token.getToken(line, 1);
							if(str!=null)
							{
								str = new String(str.substring(0,1).toUpperCase() + str.substring(1));
	   							lineOut = "   component " + str;
	   							out1.writeBytes(lineOut + "\n");
								break;
							}
						}
						else
						{
							out1.writeBytes(line + "\n");
							str = Token.getToken(line, 2);
							if(str!=null)
							{
								if(str.equals("OUT"))
								{
									str = Token.getToken(line, 0);
									lineOut = "	 FunOut_" + str + " : in natural ;";
									out1.writeBytes(lineOut + "\n");
								}
							}
						}
					}
					break;

				case ESPERA_ENTITY:
					out1.writeBytes(line + "\n");
					str = Token.getToken(line, 0);
					if(str!=null)
					{
						if(str.equals("entity"))
						{
							str = Token.getToken(line, 1);
							if(str!=null)
							{
								out2.writeBytes(stringLibs());
								out2.writeBytes("\n" + "* .model " + str + "\n");
							}
							estado = PEGA_INPUT_OUTPUT;
						}
					}
					break;

				case PEGA_INPUT_OUTPUT:
					out1.writeBytes(line + "\n");
					str = Token.getToken(line, 0);
					if(str!=null)
					{
						if(str.equals("architecture"))
						{
							strAux = "* .inputs " + strInput + "\n";
							strAux = strAux + "* .outputs " + strOutput + "\n\n";
							strAux = strAux + VCC + " " + VCC + " 0 dc 3.3V\n\n";
							strAux =  strAux + "*********************************************************************************************";
							out2.writeBytes(strAux);
							estado = ESPERA_BEGIN;
							break;
						}
					}
					str = Token.getToken(line, 2);
					if(str!=null)
					{
						String porta = Token.getToken(line, 0);
						if(str.equals("IN"))
							strInput = strInput + porta + " ";
						else
						{
							if(str.equals("OUT"))
								strOutput = strOutput + porta + " ";
						}
					}
					break;

				case ESPERA_BEGIN:
					out1.writeBytes(line + "\n");
					str = Token.getToken(line, 0);
					if(str!=null)
					{
						if(str.equals("begin"))
						{
							out1.writeBytes("   P: entity PowerEstimation;\n" + "\n");
							estado = PEGA_ASSIGN;
						}
					}
					break;

				case PEGA_ASSIGN:
					str = Token.getToken(line, offSetPortMap);
					if(str!=null)
					{
						vetAssign.ArmazenaAssign(line);
						if(str.equals("port"))
							estado = PEGA_PORTAS;
						else
							out1.writeBytes(line + "\n");
					}
					else
						out1.writeBytes(line + "\n");
					if(estado!=PEGA_PORTAS)
						break;
				default:
				case PEGA_PORTAS:
					if(Token.numeroParentesesImpar(line)) // Concatena em uma única linha, caso o número de parênteses for ímpar
					{
						do
						{
							String line2 = bin.readLine();
							int i=0;
							while(line2.charAt(i)==' ')
								i++;
							line = line + line2.substring(i);
						} while(Token.numeroParentesesImpar(line));
					}
					str = Token.getToken(line, offSetFunOut+2);
					if(str!=null)
					{
						boolean primeiro = true;
						String gate = vetGates.retornaGate(str);

						if(gate==null)
						{
							System.out.println("Porta Invalida: " + str + "\n");
							System.err.println("Conversao invalida\n");
							return -1;
						}
						strAux = Token.getToken(line, 0);
						if(offSetFunOut==0)
							lineOut = "   " + strAux + ": Entity work." + gate + "(" + gate + ") port map(";
						else
							lineOut = "   " + strAux + ": " + gate + " port map(";
						out2.writeBytes("\nX" + count + "\t");

						int idx = offSetPorta;
						str = Token.getToken(line, idx);
						while(str!=null)
						{
							strAux = Token.getToken(line, idx-1);
							if(primeiro==false)
								lineOut = lineOut + ", ";
							primeiro = false;
							lineOut = lineOut + strAux + "=>" + str;
							if(!str.equals("OPEN"))
							{
								if(idx==offSetPorta)
								{
									lineOut = lineOut + ", FunOut_" + strAux + "=>";
									lineOut = funOut.retornaFunOut(lineOut, str);
								}
								if(gate.equals("dff") || gate.equals("DffReset") || gate.equals("DffSet") || gate.equals("dffset_P"))
								{
									if(idx==offSetPorta+2)
									{
										lineOut = lineOut + ", FunOut_" + strAux + "=>";
										lineOut = funOut.retornaFunOut(lineOut, str);
									}
								}
							}
							if(str.equals("OPEN"))
								str = str + new String(new Integer(count).toString());
							str = vetAssign.TrocaAssigns(str);
							str = str + "\t";
							out2.writeBytes(str);
			  
							idx += 2;
							str = Token.getToken(line, idx);
						}
						out2.writeBytes("vcc\t0\t");
						out2.writeBytes(gate);
//						funOut.imprimeFunOut(out2, gate, line, count, offSetFunOut);
						count++;
						lineOut = lineOut + ");\n";
						out1.writeBytes(lineOut);
					}
					else
						out1.writeBytes(line + "\n");
					break;
			}
		}
		imprimeFim(strInput, strOutput, out2);
		System.out.println(vetGates.getContadorTransistores() + " TRANSISTORES");
		bin.close();
		out1.close();
		out2.close();
		
		return vetGates.getContadorTransistores();
	}
}
