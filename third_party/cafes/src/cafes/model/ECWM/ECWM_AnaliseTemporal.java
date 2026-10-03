package cafes.model.ECWM;

import java.io.*;
import java.awt.*;

import cafes.common.*;
import cafes.NoC.*;

class ECWM_AnaliseTemporal
{
	private ECWM_WindowNoC win;
	private ECWM_NoC noc;
	private ECWM_Grafo grafo;
	private ECWM_RecursosNoC matLink[][][][];
	private ECWM_RecursosNoC matRoteador[][][][];
	private long cicloMaximo, cicloInicialComunicacao, cicloFinalComunicacao;
	
	public ECWM_AnaliseTemporal(ECWM_NoC noc, int numeroLinhas, int numeroColunas, int numeroAltura)
	{
		this.noc = noc;
		cria(numeroLinhas, numeroColunas, numeroAltura);
	}
	public ECWM_AnaliseTemporal(ECWM_WindowNoC win, ECWM_NoC noc, ECWM_Grafo grafo, boolean ehTopologiaMesh)
	{
		this.win = win;
		this.noc = noc;
		this.grafo = grafo;
		
		cria(noc.getNumeroLinhas(), noc.getNumeroColunas(), noc.getNumeroAltura());
		if(win==null)
			computaCiclosAplicacao(null, null, grafo, ehTopologiaMesh);
		else
			ImprimeAnaliseTiming(ehTopologiaMesh);
	}
	private void cria(int numeroLinhas, int numeroColunas, int numeroAltura)
	{
		this.cicloMaximo = 0;
		
		matLink = new ECWM_RecursosNoC[numeroLinhas][numeroColunas][numeroAltura][8];  // Considerando in_out dos links locais e apenas in dos links entre roteadores
		matRoteador = new ECWM_RecursosNoC[numeroLinhas][numeroColunas][numeroAltura][8]; // Considerando 5 buffers de entrada e circuitos independentes
		for(int l=0; l<numeroLinhas; l++)
		{
			for(int c=0; c<numeroColunas; c++)
				for(int a=0; a<numeroAltura; a++)
			{
				for(int k=0; k<matLink[l][c][a].length; k++)
					matLink[l][c][a][k] = new ECWM_RecursosNoC();
				for(int k=0; k<matRoteador[l][c][a].length; k++)
					matRoteador[l][c][a][k] = new ECWM_RecursosNoC();
			}
		}
	}
	public long getCiclos()
	{
		return cicloMaximo;
	}
	private void ImprimeAnaliseTiming(boolean ehTopologiaMesh)
	{
		try
		{
			FileDialog d = new FileDialog(win, "Execute and save ECWM timing analisys", FileDialog.SAVE);
			if(ArquivoModelo.getArquivo()!=null)
			{
				d.setDirectory(ArquivoModelo.getDiretorio() + "Timing"); 
				d.setFile(ArquivoModelo.appenda(".timing"));
			}
			d.setVisible(true);
			if(d.getFile()==null)
				return;
			ArquivoModelo.setArquivo(d.getFile());
			String fileS = d.getDirectory() + d.getFile();
			OutputStream fileOut = new FileOutputStream(fileS);
			DataOutputStream ds = new DataOutputStream(fileOut);
			OutputStream fileOutRes = new FileOutputStream(fileS+".res");
			DataOutputStream dsRes = new DataOutputStream(fileOutRes);
			String string = 
				"+----------------------------+-----------------+----------------+----------------+----------------+\n" +
				"|   Origem    ->   Destino   |      Phits      |     Inicio     |      Fim       |    Diferen�a   |\n" +
				"+----------------------------+-----------------+----------------+----------------+----------------+\n";
			ds.write(string.getBytes());
			dsRes.write(string.getBytes());
			computaCiclosAplicacao(ds, dsRes, grafo, ehTopologiaMesh);
			string = 
				"+----------------------------+-----------------+----------------+----------------+----------------+\n" +
				"| Tempo M�ximo: " + cicloMaximo;
			ds.write(string.getBytes());
			dsRes.write(string.getBytes());
		}
		catch(Exception e)
		{
			System.out.println("Problems while generating ECWM timing analisys");
			e.printStackTrace();
			return;
		}
	}
	private void computaCiclosAplicacao(DataOutputStream ds, DataOutputStream dsRes, ECWM_Grafo g, boolean ehTopologiaMesh)
	{
		ECWM_Vertice p = g.getInicio();
		while(p!=null)
		{
			ECWM_VerticeAdjacente v = p.getVerticeAdjacenteInicial();
			while(v!=null)
			{
				if(ehTopologiaMesh)
					topologiaMesh(ds, dsRes, p, v);
				else
					topologiaTorus(ds, dsRes, p, v);
				v = v.getProx();
			}
			p = p.getProx();
		}
	}
	public void topologiaTorus(DataOutputStream ds, DataOutputStream dsRes, ECWM_Vertice p, ECWM_VerticeAdjacente v)
	{
		int linhaOrigem, colunaOrigem, linhaDestino, colunaDestino, alturaOrigem, alturaDestino;
		String origem = p.getInf();
		String destino = v.getInf();
		long ciclo;
		long phitsComChaveamento = (long)(v.getPhits() * (v.getPercentualChaveamentoPhits()/100.0));
		long phitsSemChaveamento = v.getPhits() - phitsComChaveamento;
		
		linhaOrigem = noc.DescobreLinha(origem);
		colunaOrigem = noc.DescobreColuna(origem);
		alturaOrigem = noc.DescobreAltura(origem);
		linhaDestino = noc.DescobreLinha(destino);
		colunaDestino = noc.DescobreColuna(destino);
		alturaDestino = noc.DescobreAltura(destino);
		
		long cicloInicial = matLink[linhaOrigem][colunaOrigem][alturaOrigem][Router.LOCAL_IN].getCicloFinal();
		ciclo = link(ds, Router.LOCAL_IN, origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, cicloInicial);
		cicloInicial = cicloInicialComunicacao;
		ciclo = roteador(ds, Router.LOCAL_IN, origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
		
		if(Torus.deveAvancarPonteiro(colunaOrigem, colunaDestino, noc.getNumeroColunas()))
		{
			while(colunaOrigem!=colunaDestino)
			{
				ciclo = link(ds, Router.OESTE,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
				colunaOrigem = Torus.incrementaPonteiro(colunaOrigem, noc.getNumeroColunas());
				ciclo = roteador(ds, Router.OESTE,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
		}
		else
		{
			while(colunaOrigem!=colunaDestino)
			{
				ciclo = link(ds, Router.LESTE,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
				colunaOrigem = Torus.decrementaPonteiro(colunaOrigem, noc.getNumeroColunas());
				ciclo = roteador(ds, Router.LESTE,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
		}
		if(Torus.deveAvancarPonteiro(linhaOrigem, linhaDestino, noc.getNumeroLinhas()))
		{
			while(linhaOrigem!=linhaDestino)
			{
				ciclo = link(ds, Router.NORTE,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
				linhaOrigem = Torus.incrementaPonteiro(linhaOrigem, noc.getNumeroLinhas());
				ciclo = roteador(ds, Router.NORTE,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
		}
		else
		{
			while(linhaOrigem!=linhaDestino)
			{
				ciclo = link(ds, Router.SUL,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
				linhaOrigem = Torus.decrementaPonteiro(linhaOrigem, noc.getNumeroLinhas());
				ciclo = roteador(ds, Router.SUL,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
		}
		
		if(Torus.deveAvancarPonteiro(alturaOrigem, alturaDestino, noc.getNumeroAltura()))
		{
			while(alturaOrigem!=alturaDestino)
			{
				ciclo = link(ds, Router.SUPERIOR,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
				alturaOrigem = Torus.incrementaPonteiro(alturaOrigem, noc.getNumeroAltura());
				ciclo = roteador(ds, Router.SUPERIOR,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
		}
		else
		{
			while(alturaOrigem!=alturaDestino)
			{
				ciclo = link(ds, Router.INFERIOR,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
				alturaOrigem = Torus.decrementaPonteiro(alturaOrigem, noc.getNumeroAltura());
				ciclo = roteador(ds, Router.INFERIOR,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
		}
		link(ds, Router.LOCAL_OUT, origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
		imprimeFormatado(dsRes, origem, destino, v.getPhits(), cicloInicial, cicloFinalComunicacao, cicloFinalComunicacao-cicloInicial);
	}
	public void topologiaMesh(DataOutputStream ds, DataOutputStream dsRes, ECWM_Vertice p, ECWM_VerticeAdjacente v)
	{
		int linhaOrigem, colunaOrigem, linhaDestino, colunaDestino, alturaOrigem, alturaDestino;
		String origem = p.getInf();
		String destino = v.getInf();
		long ciclo;
		long phitsComChaveamento = (long)(v.getPhits() * (v.getPercentualChaveamentoPhits()/100.0));
		long phitsSemChaveamento = v.getPhits() - phitsComChaveamento;
		
		linhaOrigem = noc.DescobreLinha(origem);
		colunaOrigem = noc.DescobreColuna(origem);
		alturaOrigem = noc.DescobreAltura(origem);
		linhaDestino = noc.DescobreLinha(destino);
		colunaDestino = noc.DescobreColuna(destino);
		alturaDestino = noc.DescobreAltura(destino);
		
		long cicloInicial = matLink[linhaOrigem][colunaOrigem][alturaOrigem][Router.LOCAL_IN].getCicloFinal();
		ciclo = link(ds, Router.LOCAL_IN, origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, cicloInicial);
		cicloInicial = cicloInicialComunicacao;
		ciclo = roteador(ds, Router.LOCAL_IN, origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
		while(colunaOrigem!=colunaDestino)
		{
			if(colunaOrigem>colunaDestino)
			{
				ciclo = link(ds, Router.OESTE,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
				colunaOrigem--;
				ciclo = roteador(ds, Router.OESTE,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
			else
			{
				ciclo = link(ds, Router.LESTE,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
				colunaOrigem++;
				ciclo = roteador(ds, Router.LESTE,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
		}
		while(linhaOrigem!=linhaDestino) // Algoritmo XY
		{
			if(linhaOrigem>linhaDestino)
			{
				ciclo = link(ds, Router.NORTE,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
				linhaOrigem--;
				ciclo = roteador(ds, Router.NORTE,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
			else
			{
				ciclo = link(ds, Router.SUL,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
				linhaOrigem++;
				ciclo = roteador(ds, Router.SUL,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
		}
		
		
		while(alturaOrigem!=alturaDestino) // Algoritmo XY
		{
			if(alturaOrigem>alturaDestino)
			{
				ciclo = link(ds, Router.SUPERIOR,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
				alturaOrigem--;
				ciclo = roteador(ds, Router.SUPERIOR,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
			else
			{
				ciclo = link(ds, Router.INFERIOR,  origem, destino, linhaOrigem, colunaOrigem,alturaOrigem, phitsComChaveamento, phitsSemChaveamento, ciclo);
				alturaOrigem++;
				ciclo = roteador(ds, Router.INFERIOR,  origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
			}
		}
		
		
		link(ds, Router.LOCAL_OUT, origem, destino, linhaOrigem, colunaOrigem, alturaOrigem,phitsComChaveamento, phitsSemChaveamento, ciclo);
		imprimeFormatado(dsRes, origem, destino, v.getPhits(), cicloInicial, cicloFinalComunicacao, cicloFinalComunicacao-cicloInicial);
	}
	private long link(DataOutputStream ds, int direcao, String origem, String destino, int linha, int coluna, int altura,long phitsComChaveamento, long phitsSemChaveamento, long cicloInicial)
	{
		long ciclosPorPhit = noc.getLinkingCycles();
		long phits = phitsComChaveamento+phitsSemChaveamento;
		
		noc.computaEnergiaLink(linha, coluna,altura, direcao, phitsComChaveamento, phitsSemChaveamento);
		if(cicloInicial<0)
			cicloInicial = ciclosPorPhit * -1;
		if(phitsComChaveamento<0 || phitsSemChaveamento<0 || ciclosPorPhit<0)  // Prote��o para valores inv�lidos
			return cicloInicial;
		long cicloFinal = cicloInicial + phits * ciclosPorPhit;
		ECWM_ListaTempo nodo = matLink[linha][coluna][altura][direcao].insereComunicacao(cicloInicial+ciclosPorPhit, cicloFinal, ciclosPorPhit);
		cicloInicialComunicacao = nodo.getCicloInicial();
		cicloFinalComunicacao = nodo.getCicloFinal();
		
		return linkOuRoteador(ds, nodo, origem, destino, phits);
	}
	private long roteador(DataOutputStream ds, int direcao, String origem, String destino, int linha, int coluna, int altura, long phitsComChaveamento, long phitsSemChaveamento, long cicloInicial)
	{
		long ciclosPorPhit = noc.getRoutingCycles();
		long phits = phitsComChaveamento+phitsSemChaveamento;
		
		noc.computaEnergiaRoteador(linha, coluna,altura, phitsComChaveamento, phitsSemChaveamento);
		if(cicloInicial<0)
			cicloInicial = ciclosPorPhit * -1;
		if(phitsComChaveamento<0 || phitsSemChaveamento<0 || ciclosPorPhit<0)  // Prote��o para valores inv�lidos
			return cicloInicial;
		long cicloFinal = cicloInicial + phits * ciclosPorPhit;
		ECWM_ListaTempo nodo = matRoteador[linha][coluna][altura][direcao].insereComunicacao(cicloInicial+ciclosPorPhit, cicloFinal, ciclosPorPhit);

		return linkOuRoteador(ds, nodo, origem, destino, phits);
	}
	private long linkOuRoteador(DataOutputStream ds, ECWM_ListaTempo nodo, String origem, String destino, long phits)
	{
		if(ds!=null)
			imprimeFormatado(ds, origem, destino, phits, nodo.getCicloInicial(), nodo.getCicloFinal(), nodo.getCicloFinal()-nodo.getCicloInicial());
		if(cicloMaximo<nodo.getCicloFinal())
			cicloMaximo = nodo.getCicloFinal();
		return nodo.getCicloInicial();
	}
	private void imprimeFormatado(DataOutputStream ds, String origem, String destino, long phits, long cicloInicial, long cicloFinal, long ciclos)
	{
		String string = "|" + StringFormat.format(origem, StringFormat.CENTER, 13) +
						"->" + StringFormat.format(destino, StringFormat.CENTER, 13) +
						"|" + StringFormat.format(phits, StringFormat.RIGTH, 16) +
						" |" + StringFormat.format(cicloInicial, StringFormat.RIGTH, 15) +
						" |" + StringFormat.format(cicloFinal, StringFormat.RIGTH, 15) +
						" |" + StringFormat.format(ciclos, StringFormat.RIGTH, 15) +
						" |\n";
		printFile(ds, string);
	}
	private void printFile(DataOutputStream ds, String str)
	{
		if(ds==null)
			return;
		try
		{
			ds.write(str.getBytes());
		}
		catch(Exception e)
		{
			System.out.println("Problems while generating ECWM timing analisys 2");
			e.printStackTrace();
		}
	}
}
