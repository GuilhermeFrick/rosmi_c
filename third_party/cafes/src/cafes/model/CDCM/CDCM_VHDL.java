package cafes.model.CDCM;

import java.awt.*;
import java.io.*;
import java.util.*;

import cafes.common.*;

class DoubleString {
	private String	string, core;

	public DoubleString(String string, String core) {
		this.string = string;
		this.core = core;
	}

	public String getString() {
		return string;
	}

	public String getCore() {
		return core;
	}
}

public class CDCM_VHDL {
	private CDCM_WindowNoC					win;
	private CDCM_Ordenado					ordenado;
	private CDCM_Grafo						grafo;
	private Vector<String>					vetorString;
	private DataOutputStream				ds;
	private HashMap<String, DoubleString>	flagsLogic;

	public CDCM_VHDL(CDCM_Ordenado ordenado, CDCM_WindowNoC win) {
		this.win = win;
		this.ordenado = ordenado;
		this.grafo = ordenado.getGrafo();
		vetorString = new Vector<String>();
		this.ds = abreArquivo();
		criaListaStringsDependencia();
		this.flagsLogic = new HashMap<String, DoubleString>();
	}

	private int toX(int coluna) // A HERMES est� em XY, enquanto o CAFES est�
								// com linha coluna
	{
		return coluna;
	}

	private int toZ(int altura) // A HERMES est� em XY, enquanto o CAFES est�
	// com linha coluna
	{
		return altura;
	}

	private int toY(int linha) // A HERMES est� em XY, enquanto o CAFES est� com
								// linha coluna
	{
		return win.getNumLinhas() - linha - 1;
	}

	private DataOutputStream abreArquivo() {
		try {
			FileDialog d = new FileDialog(win, "Arquivo de Tr�fego VHDL", FileDialog.SAVE);
			if (ArquivoModelo.getArquivo() != null) {
				d.setDirectory(ArquivoModelo.getDiretorio() + "Vhdl");
				d.setFile(ArquivoModelo.appenda(".vhd"));
			}
			d.setVisible(true);
			if (d.getFile() == null)
				return null;
			ArquivoModelo.setArquivo(d.getFile());
			String fileS = d.getDirectory() + d.getFile();
			OutputStream fileOut = new FileOutputStream(fileS);
			DataOutputStream dataOutSt = new DataOutputStream(fileOut);
			return dataOutSt;
		} catch (Exception e) {
			System.out.println("Problems while generating VHDL file");
			e.printStackTrace();
			return null;
		}
	}

	private String criaStringDependencia(String origem, String destino, int id) {
		String str = "enviou_" + origem + "_" + destino + "_" + id;
		return str;
	}

	private void criaListaStringsDependencia() {
		CDCM_Vertice p = grafo.getInicio();

		while (p != null) {
			if (p.getID() != CDCM_Grafo.START && p.getID() != CDCM_Grafo.END) {
				String str = criaStringDependencia(p.getCoreOrigem(), p.getCoreDestino(), p.getID());
				vetorString.add(str);
			}
			p = p.getProx();
		}
	}

	private String pacoteCOMM() {
		String str = "library IEEE;";
		str += "\nuse IEEE.STD_LOGIC_1164.all;";
		str += "\n";
		str += "\npackage COMM is";
		str += "\n\tprocedure Computacao(constant tempo: in TIME);";
		str += "\nend COMM;";
		str += "\n";
		str += "\npackage body COMM is";
		str += "\n";
		str += "\n\tprocedure Computacao(constant tempo: in TIME) is";
		str += "\n\tbegin";
		str += "\n\t\twait for tempo;";
		str += "\n\tend procedure Computacao;";
		str += "\n";
		str += "\nend package body COMM;";

		return str;
	}

	private String arquiteturaTB() {
		double periodoDoRelogio = 1000.0 / win.getWindowPrincipal().getClockCycle(); // Per�odo
																						// em
																						// ns

		String str = "\n\n--------------------------------------------------------------";
		str += "\n--------------------------------------------------------------";
		str += "\nlibrary IEEE;";
		str += "\nuse IEEE.std_logic_1164.all;";
		str += "\nuse IEEE.std_logic_arith.all;";
		str += "\nuse IEEE.std_logic_unsigned.all;";
		str += "\nuse work.HermesPackage.all;";
		str += "\nuse work.COMM.all;";
		str += "\nuse STD.TEXTIO.all;";
		str += "\n";
		str += "\nentity tbNoC is";
		str += "\nend tbNoC;";
		str += "\n";
		str += "\narchitecture tbNoC of tbNoC is";
		str += "\n";
		for (int z = 0; z < win.getNumAltura(); z++) {
		for (int x = 0; x < win.getNumColunas(); x++) {
			for (int y = 0; y < win.getNumLinhas(); y++)
				str += "\n\tfile ARQ_OUT" +z+ x + y + ": TEXT open WRITE_MODE is \"out" +z+ x + y + ".txt\";";
		}
		}
		str += "\n";
		str += "\n\tsignal clock: regNrot;";
		str += "\n\tsignal reset: std_logic;";
		str += "\n\tsignal clock_rx,rx,credit_o: regNrot;";
		str += "\n\tsignal data_in: arrayNrot_regflit;";
		str += "\n\tsignal clock_tx,tx,credit_i: regNrot;";
		str += "\n\tsignal data_out: arrayNrot_regflit;";
		str += "\n";
		for (int i = 0; i < vetorString.size(); i++) {
			str += "\n\tsignal ";
			str += vetorString.get(i);
			str += ": BOOLEAN := FALSE;";
		}
		str += "\n";
		str += "\nbegin";
		str += "\n";
		str += "\n\tNOC: Entity work.NOC(NOC)";
		str += "\n\tport map(";
		str += "\n\t\tclock	 => clock,";
		str += "\n\t\treset	 => reset,";
		str += "\n\t\tclock_rxLocal => clock_tx,";
		str += "\n\t\trxLocal	   => tx,";
		str += "\n\t\tdata_inLocal  => data_out,";
		str += "\n\t\tcredit_oLocal => credit_i,";
		str += "\n\t\tclock_txLocal => clock_rx,";
		str += "\n\t\ttxLocal	   => rx,";
		str += "\n\t\tdata_outLocal => data_in,";
		str += "\n\t\tcredit_iLocal => credit_o);";
		str += "\n";
		for (int z = 0; z < win.getNumAltura(); z++) {
		for (int x = 0; x < win.getNumColunas(); x++) {
			for (int y = 0; y < win.getNumLinhas(); y++) {
				str += "\n\t-- Gera o clock do roteador " +z+ x + y;
				str += "\n\tprocess";
				str += "\n\tbegin";
				str += "\n\t\tclock(N" + String.format("%01d", z)+ String.format("%01d", x) + String.format("%01d", y) + ") <= '1', '0' after " + periodoDoRelogio / 2 + "ns;";
				str += "\n\t\twait for " + periodoDoRelogio + "ns;";
				str += "\n\tend process;";
				str += "\n\tclock_tx(N" + String.format("%01d", z)+ String.format("%01d", x) + String.format("%01d", y) + ") <= clock(N" + String.format("%01d", z)+ String.format("%01d", x) + String.format("%01d", y) + ");";
				str += "\n";
			}
		}
		}
		str += "\n\t-- Gera o reset";
		str += "\n\treset <='1','0' after 20 ns;";
		str += "\n";

		str += "\n--------------------------------------------------------------";
		str += "\n-- GERACAO DE PACOTES";
		str += "\n--------------------------------------------------------------";
		for (int alt = 0; alt < win.getNumAltura(); alt++) {
			for (int col = 0; col < win.getNumColunas(); col++) {
				for (int lin = 0; lin < win.getNumLinhas(); lin++) {

					int yOrig = toY(lin);
					int xOrig = toX(col);
					int zOrig = toZ(alt);

					String coreOrigem = win.getNoc().matriz[lin][col][alt].getCoreName();
					boolean primeiro = true;

					for (int i = 0; i < ordenado.getVetorNiveis().length; i++) {
						CDCM_Ordenado_ListaVertices olv = ordenado.getVetorNiveis()[i].getInicio();
						while (olv != null) {
							CDCM_Vertice verticeAtual = olv.getVertice();

							if (verticeAtual != null) {
								if (verticeAtual.getCoreOrigem() != null) {
									if (verticeAtual.getCoreOrigem().equals(coreOrigem)) {
										String coreDestino = verticeAtual.getCoreDestino();
										int xDest = toX(win.getNoc().DescobreColuna(coreDestino));
										int yDest = toY(win.getNoc().DescobreLinha(coreDestino));
										int zDest = toZ(win.getNoc().DescobreAltura(coreDestino));
										int computacao = verticeAtual.getComputacao();
										long phits = verticeAtual.getPhits();
										int id = verticeAtual.getID();

										str += roteador(xOrig, yOrig,zOrig, coreOrigem, coreDestino, computacao, id, phits, xDest, yDest,zDest, primeiro, olv);
										primeiro = false;
									}
								}
							}
							olv = olv.getProx();
						}
					}
					if (primeiro == false) // Entrou pelo menos uma vez
					{
						str += "\n\tend process;";
						str += "\n";
					}
				}
			}
		}

		str += "\n--------------------------------------------------------------";
		str += "\n-- RECEPCAO DE PACOTES";
		str += "\n--------------------------------------------------------------";
		for (int alt = 0; alt < win.getNumAltura(); alt++) {
		for (int col = 0; col < win.getNumColunas(); col++) {
			for (int lin = 0; lin < win.getNumLinhas(); lin++) {
				int yOrig = toY(lin);
				int xOrig = toX(col);
				int zOrig = toZ(alt);

				str += finalRoteador(xOrig, yOrig,zOrig);
			}
		}
		}
		str += "\n";
		str += "\nend tbNoC;";
		str += "\n";

		return str;
	}

	private String finalRoteador(int xOrig, int yOrig,int zOrig) {
		/*
		 * The flagging logic is generated by roteador() for every package it
		 * sends, to this function is left the duty of eating the package and
		 * placing the logic generated by the sender in the receiver.
		 */

		String str = "";

		if (flagsLogic.containsKey(String.format("%01X", xOrig) + String.format("%01X", yOrig)+ String.format("%01X", zOrig))) {
			String coreOrigem = flagsLogic.get(String.format("%01X", xOrig) + String.format("%01X", yOrig)+ String.format("%01X", zOrig)).getCore();

			str += "\n\n--------------------------------------------------------------";
			str += "\n--------------------------------------------------------------";
			str += "\n-- " + xOrig + yOrig+ zOrig;
			str += "\n--------------------------------------------------------------";
			// Process Header
			str += "\n\t" + coreOrigem + "_I_" + xOrig + yOrig+zOrig + ": process -- Roteador_" + xOrig + yOrig+ zOrig + " recebe os pacotes";

			str += "\n\t\tvariable source, target, size, data, node: std_logic_vector(7 downto 0);";
			str += "\n\t\tvariable linha: line;";
			str += "\n\tbegin";
			str += "\n\t\tcredit_o(N" + xOrig + yOrig+ zOrig + ") <= '1';";
			str += "\n\t\twait until reset ='0';";
			str += "\n\t\tloop";

			// Read package destination
			str += "\n\t\t\twait until rx(N" + xOrig + yOrig+ zOrig + ") = '1' and clock_rx(N" + xOrig + yOrig + zOrig + ") = '0';";
			str += "\n\t\t\ttarget := data_in(N" + xOrig + yOrig+ zOrig  + "); -- Package Destination";
			str += "\n\t\t\twrite(linha, CONV_STRING_8BITS(target));";
			str += "\n\t\t\twriteline(ARQ_OUT" + xOrig + yOrig + zOrig + ", linha);";

			// Read package size
			str += "\n\t\t\twait until clock_rx(N" + xOrig + yOrig+ zOrig  + ") = '1';";
			str += "\n\t\t\twait until clock_rx(N" + xOrig + yOrig + zOrig + ") = '0';";
			str += "\n\t\t\tsize := data_in(N" + xOrig + yOrig + zOrig + "); -- Package size";
			str += "\n\t\t\twrite(linha, CONV_STRING_8BITS(size));";
			str += "\n\t\t\twriteline(ARQ_OUT" + xOrig + yOrig + zOrig + ", linha);";

			// Read package source
			str += "\n\t\t\twait until clock_rx(N" + xOrig + yOrig + zOrig + ") = '1';";
			str += "\n\t\t\twait until clock_rx(N" + xOrig + yOrig + zOrig + ") = '0';";
			str += "\n\t\t\tsource := data_in(N" + xOrig + yOrig+ zOrig  + "); -- Package source";
			str += "\n\t\t\twrite(linha, CONV_STRING_8BITS(source));";
			str += "\n\t\t\twriteline(ARQ_OUT" + xOrig + yOrig+ zOrig  + ", linha);";

			// Read node number
			str += "\n\t\t\twait until clock_rx(N" + xOrig + yOrig + zOrig + ") = '1';";
			str += "\n\t\t\twait until clock_rx(N" + xOrig + yOrig+ zOrig  + ") = '0';";
			str += "\n\t\t\tnode := data_in(N" + xOrig + yOrig+ zOrig  + "); -- Package refered node";
			str += "\n\t\t\twrite(linha, CONV_STRING_8BITS(node));";
			str += "\n\t\t\twriteline(ARQ_OUT" + xOrig + yOrig + zOrig + ", linha);";

			// Eat the rest of the package
			str += "\n\t\t\twait until clock_rx(N" + xOrig + yOrig+ zOrig  + ") = '1';";
			str += "\n\t\t\twhile rx(N" + xOrig + yOrig+ zOrig  + ") = '1' loop -- Keep eating phits from package";
			str += "\n\t\t\t\twait until clock_rx(N" + xOrig + yOrig+ zOrig  + ") = '0';";
			str += "\n\t\t\t\tdata := data_in(N" + xOrig + yOrig + zOrig + "); -- Package data";
			str += "\n\t\t\t\twrite(linha, CONV_STRING_8BITS(data));";
			str += "\n\t\t\t\twriteline(ARQ_OUT" + xOrig + yOrig + zOrig + ", linha);";
			str += "\n\t\t\t\twait until clock_rx(N" + xOrig + yOrig+ zOrig  + ") = '1';";
			str += "\n\t\t\tend loop;";
			str += "\n";

			str += flagsLogic.get(String.format("%01X", xOrig) + String.format("%01X", yOrig) + String.format("%01X", zOrig)).getString();
			str += "\n\t\t\twait until clock_rx(N" + xOrig + yOrig + zOrig + ") = '1';";

			str += "\n\t\tend loop;";
			str += "\n\tend process;";
			str += "\n";
		}
		return str;
	}

	private String roteador(int xOrig, int yOrig, int zOrig,String coreOrigem, String coreDestino, int computacao, int id, long phits, int xDest, int yDest, int zDest,boolean primeiro, CDCM_Ordenado_ListaVertices olv) {
		String tamanhoStr = StringFormat.toHexa(phits - 2, 2);
		long phitsDados = phits - 4;

		String str = "";

		if (primeiro == true) {
			str += "\n\n--------------------------------------------------------------";
			str += "\n--------------------------------------------------------------";
			str += "\n-- " + xOrig + yOrig+ zOrig ;
			str += "\n--------------------------------------------------------------";
			str += "\n\t" + coreOrigem + "_" + xOrig + yOrig + zOrig + ": process -- Gera os pacotes de sa�da do roteador_" + xOrig + yOrig+ zOrig ;
			str += "\n\t\tvariable numPhits: integer;";
			str += "\n\tbegin";
			str += "\n\t\ttx(N" + xOrig + yOrig + zOrig + ") <= '0';";
			str += "\n\t\twait until reset = '0';";
		}

		CDCM_Ordenado_Vertice p;
		if ((p = olv.getInicio()) != null) {
			CDCM_Vertice vertDep = p.getVertice();
			str += "\n";
			str += "\n\t\twait until ";
			str += criaStringDependencia(vertDep.getCoreOrigem(), vertDep.getCoreDestino(), vertDep.getID()) + " = true";
			while ((p = p.getProx()) != null) {
				vertDep = p.getVertice();
				str += " and ";
				str += criaStringDependencia(vertDep.getCoreOrigem(), vertDep.getCoreDestino(), vertDep.getID()) + " = true";

			}
			str += ";";
		}

		str += "\n\t\tComputacao(" + computacao + "ns);";
		str += "\n\t\twait until credit_i(N" + xOrig + yOrig + zOrig + ") = '1' and clock_tx(N" + xOrig + yOrig + zOrig + ") = '1';";
		str += "\n\t\tdata_out(N" + xOrig + yOrig+ zOrig  + ") <= x\"0" + ((xDest << 4) +(yDest << 2)+ zDest) + "\";  -- Destino";
		str += "\n\t\ttx(N" + xOrig + yOrig +zOrig+ ") <= '1';";
		str += "\n\t\twait until clock_tx(N" + xOrig + yOrig+zOrig + ") = '0';";
		str += "\n\t\twait until credit_i(N" + xOrig + yOrig +zOrig+ ") = '1' and clock_tx(N" + xOrig + yOrig +zOrig+ ") = '1';";
		str += "\n\t\tdata_out(N" + xOrig + yOrig+zOrig + ") <= x\"" + tamanhoStr + "\";  -- Tamanho";
		str += "\n\t\twait until clock_tx(N" + xOrig + yOrig+zOrig + ") ='0';";
		str += "\n\t\twait until credit_i(N" + xOrig + yOrig+zOrig + ") = '1' and clock_tx(N" + xOrig + yOrig +zOrig+ ") = '1';";
		str += "\n\t\tdata_out(N" + xOrig + yOrig +zOrig+ ") <= x\"" + xOrig + yOrig+zOrig + "\";  -- Origem";
		str += "\n\t\twait until clock_tx(N" + xOrig + yOrig+zOrig + ") = '0';";
		str += "\n\t\twait until credit_i(N" + xOrig + yOrig +zOrig+ ") = '1' and clock_tx(N" + xOrig + yOrig+zOrig + ") = '1';";
		str += "\n\t\tdata_out(N" + xOrig + yOrig +zOrig+ ") <= x\"" + String.format("%02X", id) + "\";  -- Node";
		str += "\n\t\twait until clock_tx(N" + xOrig + yOrig+zOrig + ") = '0';";
		str += "\n\t\tnumPhits := " + phitsDados + ";";
		str += "\n\t\twhile numPhits>0 loop";
		str += "\n\t\t\tnumPhits := numPhits - 1;";
		str += "\n\t\t\twait until credit_i(N" + xOrig + yOrig+zOrig + ") = '1' and clock_tx(N" + xOrig + yOrig+zOrig + ") = '1';";
		str += "\n\t\t\tdata_out(N" + xOrig + yOrig +zOrig+ ") <= CONV_STD_LOGIC_VECTOR(numPhits, 8);";
		str += "\n\t\t\twait until clock_tx(N" + xOrig + yOrig+zOrig + ") = '0';";
		str += "\n\t\tend loop;";
		str += "\n\t\ttx(N" + xOrig + yOrig+zOrig + ") <= '0';";

		// Generate the receiving part's flag logic and store for further usage.
		String a = "";
		if (flagsLogic.containsKey(String.format("%01X", xDest) + String.format("%01X", yDest)+ String.format("%01X", zDest)))
			a += flagsLogic.get(String.format("%01X", xDest) + String.format("%01X", yDest)+ String.format("%01X", zDest)).getString();
		a += "\t\tif source = x\"" + String.format("%01X", xOrig) + String.format("%01X", yOrig)+ String.format("%01X", zOrig) + "\" and node = x\"" + String.format("%02X", id) + "\" then";
		a += "\n\t\t\t" + criaStringDependencia(coreOrigem, coreDestino, id) + " <= true;";
		a += "\n\t\tend if;";
		a += "\n";
		flagsLogic.put(String.format("%01X", xDest) + String.format("%01X", yDest) + String.format("%01X", zDest), new DoubleString(a, coreDestino));

		return str;
	}

	public void percorreListaDeNiveisGeraVHDL() {
		try {
			String str = pacoteCOMM();
			str += arquiteturaTB();

			ds.write(str.getBytes());
		} catch (Exception e) {
			System.out.println("Problems while writing VHDL file");
			e.printStackTrace();
			return;
		}
	}
}
