package cafes.tools.Vhdl2SpiceVhdl;

import java.io.*;
import cafes.common.*;

public class FunOut
{
	private VetorSinais vetorSinais;	

	public FunOut(BufferedReader bin, int offset)
	{
		vetorSinais = new VetorSinais(10000);
		calculaFunOut(bin, offset);
	}
	private void calculaFunOut(BufferedReader bin, int offset)
	{
		String str = "";
		int count = 0;

		try
		{
			while(true)
			{
				String line = bin.readLine();
				if(line==null)
					break;
				str = Token.getToken(line, offset+3);
				if(str!=null)
		 		{
					if(str.equals("port"))
					{
						int posicao=offset+6, posicaoInicialSinalEntrada;

						if(Token.numeroParentesesImpar(line)) // Concatena em uma única linha, caso o número de parênteses for ímpar
						{
							do
							{
								String strAux = bin.readLine();

								int i=0;
								while(strAux.charAt(i)==' ')
									i++;
								line = line + strAux.substring(i);
							} while(Token.numeroParentesesImpar(line));
						}
						str = Token.getToken(line, offset+2); // Lê o nome da porta
						if(str.equals("dff") || str.equals("dffclear") || str.equals("dffset") || str.equals("dffset_P"))
							posicaoInicialSinalEntrada = offset+10;  // Aponta para o primeiro sinal de entrada da porta
						else
							posicaoInicialSinalEntrada = offset+8;  // Aponta para o primeiro sinal de entrada da porta
						while(true)
						{
							str = Token.getToken(line, posicao);
							if(str==null)
								break;
							Sinais sinal;
				if(str.equals("OPEN"))
					str = str + count++;
							posicao += 2; // Pega o próximo sinal da porta
							if(posicao<=posicaoInicialSinalEntrada) // É um sinal de saída
							{
								int i;

								for(i=0; i<vetorSinais.size(); i++)
								{
									sinal = vetorSinais.get(i);
									if(sinal.getSinal().equals(str)) // Verifica se saída já está no vetor de saídas, pois foi colocada como entrada
										break;
								}
								if(i>=vetorSinais.size()) // Não encotrou no vetor de saídas, coloca ao final deste
									vetorSinais.add(new Sinais(str));
							}
							else // É um sinal de entrada
							{
								int i;

								for(i=0; i<vetorSinais.size(); i++)
								{
									sinal = vetorSinais.get(i);
									if(sinal.getSinal().equals(str)) // Verifica se saída já está no vetor de saídas, pois foi colocada como entrada
									{
										vetorSinais.incrementa(i);
										break;
									}
								}
								if(i>=vetorSinais.size())  // Ainda não passou na porta que tem este sinal como saída
									vetorSinais.add(new Sinais(str, 1));
							}
						}
					}
				}
			}
		}
		catch(OutOfMemoryError o)
		{
			System.out.println("Falta de memória");
			o.fillInStackTrace();
		}
		catch(Exception e)
		{
			System.out.println("Problemas com a obtenção do FunOut");
		}
	}
	public String retornaFunOut(String strOut, String str)
	{
		for(int i=0; i<vetorSinais.size(); i++)
		{
			Sinais sinal = vetorSinais.get(i);
			
			if(sinal.getSinal().equals(str))
			{
				strOut = strOut + sinal.getNumero();
				break;
			}
		}
		return strOut;				
	}
/*
	private static void imprimeString(DataOutputStream out, String str)
	{
		try
		{
			out.writeBytes(str);
		}
		catch(Exception e)
		{
			System.out.println("Problemas na escrita em arquivo");
		}
	}
	public void imprimeFunOut(DataOutputStream out, String gate, String line, int count, int offset)
	{
		Sinais sinal;
		String str = Token.getToken(line, offset+6, true);

		for(int i=0; i<vetorSinais.size(); i++)
		{
			sinal = vetorSinais.get(i);
			if(sinal.getSinal().equals(str))
			{
				if(sinal.getNumero()==0)
					break;
				String prt =   "\n" + "R" + count + "\t" + "S_" + str + "\t" + str + "\t" + 50*sinal.getNumero();
				imprimeString(out, prt);
				prt = "\n" + "C" + count + "\t" + "S_" + str + "\t" + "0" + "\t" + 50*sinal.getNumero() + "fF";
				imprimeString(out, prt);
				break;
			}
		}
		if(gate.equals("dff") || gate.equals("DffReset") || gate.equals("DffSet") || gate.equals("dffset_P"))
		{
			str = Token.getToken(line, offset+8, true);
			for(int i=0; i<vetorSinais.size(); i++)
			{
				sinal = vetorSinais.get(i);
				if(sinal.getSinal().equals(str))
				{
					if(sinal.getNumero()==0)
						break;
					String prt =   "\n" + "R" + count + "b\t" + "S_" + str + "\t" + str + "\t" + 50*sinal.getNumero();
					imprimeString(out, prt);
					prt = "\n" + "C" + count + "b\t" + "S_" + str + "\t" + "0" + "\t" + 50*sinal.getNumero() + "fF";
					imprimeString(out, prt);
					break;
				}
			}
		}
		imprimeString(out, "\n");
	}
*/	
}
