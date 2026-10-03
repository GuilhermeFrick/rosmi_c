package cafes.model.CWM;

/*
 * Autor: 
 * 		Edson Moreno
 * 		C�sar Marcon
 * Objetivo: 
 * 		Criar um algoritmo heur�stico que possa ser utilizado para definir o mapeamento de PE sobre a NoC com menor numero 
 *	  de iteracoes e da forma mais proxima da otima possivel. 
 */
import java.util.*;
import javax.swing.*;

import cafes.common.*;

class CWM_HeuristicSearch
{
// Vetor q contem os pares q se comunicam, ao final estarao ordenados descrescentemente pelo peso das comunicacoes			
	private Vector<vertexPairs> pairWeight = new Vector<vertexPairs>(100, 5);

// Vetor contem a lista das chaves disponiveis na noc
	private Vector<SwitchSettings> SwitchList = new Vector<SwitchSettings>(100, 5);

	/*
	 *  Vetor contem todos os vertices com os pares q este se comunica de forma sumarizada.
	 */
	private Vector<vertexCounter> bindVertex = new Vector<vertexCounter>(100, 5);

	private CWM_Grafo g;
	protected CWM_NoC noc;
	private CWM_VerticeNoC minimumMapping[][][];
	private boolean comEstimativaDeTempo;
	private int nbrOfVertex;
	private int remainingVertex;
	protected int availableSP2;
	protected int availableSP3;
	protected int availableSP4;
	
	/*
	 * Vetores utilizados para atribuicao de chaves de 2, 3 e 4 portas
	 * 		a cada um dos vertices.  
	 */
	private Vector<String> waittingList4, waittingList3, waittingList2;
	private Vector<String> S4P, S3P, S2P;
		
	private double EnergiaLinkLocal;
	private double EnergiaLinkVertical;
	private double EnergiaLinkHorizontal;
	private double EnergiaLinkLongitudinal;
	private double EnergiaBuffer;
	private double EnergiaControle;

	
	/*
	 * Objetivo: Criar uma estrutura de chaves que possa ser utilizada para futura
	 * pesquisa de melhor posicinamento de vertices.
	 */
	public class SwitchSettings
	{
		protected LinhaColunaAltura position; // LinhaColuna da switch
		protected int numberOfLinks; // numero de links desta switch
		protected boolean occupied; // informa se a switch jah esta ocupada por algum vertice

		/*
		 * Construtor da classe.
		 */
		public SwitchSettings(LinhaColunaAltura _lc)
		{
			position = _lc;
			numberOfLinks = defineNbrOfLinks(_lc);
			occupied = false;
			switch(numberOfLinks)
			{
				case 2:
					availableSP2++;
					break;

				case 3:
					availableSP3++;
					break;

				case 4:
					availableSP4++;
					break;
			}
		}
		private int defineNbrOfLinks(LinhaColunaAltura _lc)
		{
			CWM_NoC _noc = noc;
			int _nocCols = _noc.getNumeroColunas(), _nocRows = noc.getNumeroLinhas(), nl;
			if(noc.getMainWindow().ehTopologiaMesh()){
				if ((_lc.getLinha() == 0 && _lc.getColuna() == 0)
						|| (_lc.getLinha() == 0 && _lc.getColuna() == (_nocCols - 1))
						|| (_lc.getLinha() == (_nocRows - 1) && _lc.getColuna() == 0)
						|| (_lc.getLinha() == (_nocRows - 1) && _lc.getColuna() == (_nocCols - 1)))
				{
					nl = 2;
				} else if ((_lc.getLinha() == 0 && (_lc.getColuna() != 0 || _lc
						.getColuna() != _nocCols - 1))
						|| (_lc.getLinha() == (_nocRows - 1) && (_lc.getColuna() != 0 || _lc
								.getColuna() != _nocCols - 1))
						|| (_lc.getColuna() == 0 && (_lc.getLinha() != 0 || _lc.getLinha() != _nocRows - 1))
						|| (_lc.getColuna() == (_nocCols - 1) && (_lc.getLinha() != 0 || _lc
								.getLinha() != _nocRows - 1)))
				{
					nl = 3;
				} else
					nl = 4;
			}
			else
				nl=4;
			return nl;
		}
		public void assignVertex()
		{
			occupied = true;
		}
	}
	/*
	 * Objetivo: Algoritmo a ser utilizado pelos objetos do tipo vector para
	 * ordenacao da classe vertexPairs. Parametros: obj1 -> instancia do
	 * vertexPairs a ser comparada com o obj2 obj2 -> instancia do vertexPairs a
	 * ser comparada com o obj2
	 */
	public class vertexPairComparer implements Comparator<vertexPairs>
	{
		public int compare(vertexPairs obj1, vertexPairs obj2)
		{
			long i1 = obj1.phits;
			long i2 = obj2.phits;

			return (int) Math.abs(i2) - (int) Math.abs(i1);
		}
	}

	/*
	 * Objetivo: Armazenar os pares de vertices que se comunicacam sumarizando a
	 * qtde de phits trocada entre eles.
	 */
	public class vertexPairs
	{
		public String v1, v2;

		public long phits;

		/*
		 * Objetivo: Inicializa a classe com o par q se comunica e a qtde de phits q
		 * trocam. Parametros: _v1 -> vertice 1 do par (nao ha conceito de origem e
		 * destino) _v2 -> vertice 2 do par (nao ha conceito de origem e destino)
		 * _phits -> qtde de phits q trocaram
		 */
		public vertexPairs(String _v1, String _v2, long _phits)
		{
			this.v1 = _v1;
			this.v2 = _v2;
			this.phits = _phits;
		}

		/*
		 * Objetivo: Retorna a comparacao se um par que se comunica eh igual ao par
		 * que estah sendo enviado para comparacao Parametros: _v1 -> vertice 1 do
		 * par a ser comparado (nao ha conceito de origem e destino) _v2 -> vertice
		 * 2 do par a ser comparado (nao ha conceito de origem e destino)
		 */
		public boolean equals(String _v1, String _v2)
		{
			return ((_v1.equals(this.v1) && (_v2.equals(this.v2))) || ((_v1.equals(this.v2)) && (_v2.equals(this.v1))));
		}

		public boolean equals(Object o)
		{
			vertexPairs localvp;
			if(o instanceof vertexPairs)
			{
				localvp=(vertexPairs) o;
				return ((localvp.v1.equals(this.v1) && (localvp.v2.equals(this.v2))) || ((localvp.v1.equals(this.v2)) && (localvp.v2.equals(this.v1))));
			}
			return false;
		}

		/*
		 * Objetivo: Informa se um dado vertice faz parte do par que se comunica.
		 * Parametros: _v -> nome do vertice a ser avaliado
		 */
		public boolean contains(String _v)
		{
			return (_v.equals(this.v1) || _v.equals(this.v2));
		}

	}

	/*
	 * Objetivo: Criar uma estrutura para ser utilizada na associa��o de IPs as
	 * chaves de 2, 3 ou 4 portas.
	 */
	public class vertexCounter
	{
		public String vertice;
		public int counter, usingSwitch, waitingForSwitch;
		public LinhaColunaAltura lc;
		public boolean assigned;

		Vector<vertexPairs> neighbor = new Vector<vertexPairs>(100, 5);

		/*
		 * Objetivo: Construtor da classe Parametros: _v -> Nome do vertice
		 */
		public vertexCounter(String _source, String _target, long _phits)
		{
			vertice = _source;
			counter = 0;
			usingSwitch = 0;
			waitingForSwitch = 0;
			assigned = false;
			lc = new LinhaColunaAltura(-1, -1,-1);
			inc(_target, _phits);
		}

		public void inc(String _v, long _weight)
		{
			int index = neighbor.indexOf(new vertexPairs(vertice, _v, _weight));
			if(index==-1)
			{
				neighbor.add(new vertexPairs(vertice, _v, _weight));
				counter++;
			}
			else
			{
				neighbor.get(index).phits += _weight;
			}			
		}
		public boolean equals(Object o)
		{
			vertexCounter localvc;
			if(o instanceof vertexCounter)
			{
				localvc=(vertexCounter) o;
				return localvc.vertice.equals(this.vertice);
			}
			return false;
		}
		
		public void assignToSwitch(SwitchSettings _ss)
		{
			assigned = true;
			lc = _ss.position;			
			_ss.assignVertex();
		}
	}

	/*
	 * Objetivos: Construtor da classe CWM_HeuristicSearch Parametros: _noc ->
	 * Inst�ncia CWM_NoC para o mapeamento dos vertices _g -> Grafo dos vertices e
	 * arcos do modelo carregado _comEstimativaDeTempo -> Se deve realizar
	 * estimativa de tempo (true) ou nao (false)
	 */
	public CWM_HeuristicSearch(CWM_NoC _noc, CWM_Grafo _g, boolean _comEstimativaDeTempo)
	{
		this.noc = _noc;
		this.g = _g;
		this.comEstimativaDeTempo = _comEstimativaDeTempo;
		this.remainingVertex = 0;
		
		EnergiaBuffer=_noc.getEnergiaBuffer();
		EnergiaControle=_noc.getEnergiaControle();
		EnergiaLinkVertical=_noc.getEnergiaLinkVertical();
		EnergiaLinkHorizontal=_noc.getEnergiaLinkHorizontal();
		EnergiaLinkLongitudinal = noc.getEnergiaLinkLongitudinal();
		EnergiaLinkLocal=_noc.getEnergiaLocalLink();


		CWM_Vertice localVertice = _g.getInicio();
		CWM_VerticeAdjacente localAdjacente;
		availableSP4=availableSP3=availableSP2=0;

		/*
		 * Cria a lista de pares q se comunicam a ser utilizado como parametro para
		 * definir a ordem de prioridade de associacao as chaves de 2, 3 e 4 portas.
		 */
		
		vertexPairs vp;
		while (localVertice != null)
		{
			this.nbrOfVertex++;
			localAdjacente = localVertice.getVerticeAdjacenteInicial();
			while (localAdjacente != null)
			{
			
				vp = new vertexPairs(localVertice.getInf(),localAdjacente.getInf(), localAdjacente.getPhits());
				if(! pairWeight.contains(vp)) pairWeight.add(vp);
			
				localAdjacente = localAdjacente.getProx();

			}

			localVertice = localVertice.getProx();
		}
		Collections.sort(pairWeight, new vertexPairComparer());
	}

	public void algoritmo()
	{
		/*
		 * Cria��o da lista de chaves dispon�veis na NoC informada. Esta estrutura ser� utilizada
		 * 		para controlar as chaves que j� foram ocupadas e por quais vertices. 
		 */
		createSwitchList();
		// remainingVertex � utilizada para saber qtos vertices ainda nao foram mapeados para alguma chave da NoC
		remainingVertex = nbrOfVertex; 
		waittingList4 = new Vector<String>(nbrOfVertex, 5);
		waittingList3 = new Vector<String>(nbrOfVertex, 5);
		waittingList2 = new Vector<String>(nbrOfVertex, 5);
		S4P = new Vector<String>(availableSP4, 5);
		S3P = new Vector<String>(availableSP3, 5);
		S2P = new Vector<String>(availableSP2, 5);
		/*
		 * Looping de cria��o da lista de vertices a serem associados a NoC para
		 * 		futura associa��o de cada vertice a uma chave de tantas conexoes.
		 */
		for (int i = 0; i < pairWeight.size(); i++)
		{
			if (bindVertex.size() == 0)
			{
				bindVertex.add(new vertexCounter(pairWeight.get(i).v1,
																				 pairWeight.get(i).v2, 
																				 pairWeight.get(i).phits));
				bindVertex.add(new vertexCounter(pairWeight.get(i).v2,
																				 pairWeight.get(i).v1, 
																				 pairWeight.get(i).phits));
			} 
			else
			{
				insertAtBindVertex(pairWeight.get(i).v1,
													 pairWeight.get(i).v2, 
													 pairWeight.get(i).phits);
			}
		}
		// Ordena a lista dos vizinhos de cada vertice de acordo com o peso da comunicacao
		for(int i=0; i<bindVertex.size(); i++)
			Collections.sort(bindVertex.get(i).neighbor, new vertexPairComparer());
		// Fase de associacao do IP as posicoes da NoC
		// passo) associa os vertices q usam 4 portas e seus vizinhos
		if(remainingVertex>0) 
		{
			for (int i = 0; i < S4P.size(); i++)
			{
				positionateVertex(S4P.get(i));
				if(remainingVertex == 0)
					break;
			}
		}
  	// passo) associa os vertices q usam 3 portas e seus vizinhos
		if(remainingVertex>0)
		{
			for (int i = 0; i < S3P.size(); i++)
			{
				positionateVertex(S3P.get(i));
				if (remainingVertex == 0)
					break;
			}
		}
		// passo) associa os vertices q usam 2 portas e seus vizinhos
		if(remainingVertex>0)
		{
			for (int i = 0; i < S2P.size(); i++)
			{
				positionateVertex(S2P.get(i));
				if (remainingVertex == 0)
					break;
			}
		}
		if(remainingVertex>0)
		{
			for(int i=0; i<bindVertex.size(); i++)
			{				
				if(!bindVertex.get(i).assigned)
				{
					positionateVertex(bindVertex.get(i).vertice);
					
					if (remainingVertex == 0)
						break;
				}
			}
		}
		saveHeuristic();
		double energia = noc.computa(g, comEstimativaDeTempo);
		noc.setEnergiaConsumidaMapeamento(energia);
		noc.salvaPosicionamento();
	}
	public int numeroTotalCombinacoes()
	{
		return nbrOfVertex;
	}
	public boolean isAValidPosition(LinhaColunaAltura _lc)
	{
		boolean isValidSwitch=true;
		if(_lc.getColuna()<0 ||
			 _lc.getColuna()>=noc.getNumeroColunas() ||
			 _lc.getLinha()<0 ||
			 _lc.getLinha()>=noc.getNumeroLinhas())
			isValidSwitch=false; 

		return isValidSwitch;
	}
	public LinhaColunaAltura sumLC(LinhaColunaAltura _lc, int _rows, int _cols)
	{
		LinhaColunaAltura localLC= new LinhaColunaAltura(_lc);
		int linha, coluna;
		linha = _lc.getLinha()+_rows;
		coluna= _lc.getColuna()+_cols;
		if(noc.getMainWindow().ehTopologiaTorus())
		{
			if(linha < 0)
				linha += noc.getNumeroLinhas();
			if(linha > noc.getNumeroLinhas()-1)
				linha = linha%noc.getNumeroLinhas();
			if(_rows >= noc.getNumeroLinhas()/2)
				linha = -1;
			
			if(coluna < 0)
				coluna += noc.getNumeroColunas();
			if(coluna > noc.getNumeroColunas()-1)
				coluna = coluna%noc.getNumeroColunas(); 
			if(_cols >= noc.getNumeroColunas()/2)
				coluna=-1;
		}
		if(noc.getMainWindow().ehTopologiaMesh())
		{
			if(linha<0 ||_rows>=noc.getNumeroLinhas())
				linha = -1;
			if(coluna<0||_cols>=noc.getNumeroColunas())
				coluna = -1;
		}
		localLC.setLinha(linha);
		localLC.setColuna(coluna);
		
		return localLC;
	}
	
	public LinhaColunaAltura[] neighborsWithHops(LinhaColunaAltura _lc, int _nrHops)
	{
		int linha=_nrHops, coluna=0;
		Vector<LinhaColunaAltura> localVct= new Vector<LinhaColunaAltura>(100,5);
		LinhaColunaAltura localLC;

		while(coluna<=_nrHops)
		{
			localLC=sumLC(new LinhaColunaAltura(_lc),linha,coluna);
			if((isAValidPosition(localLC))&&(!localVct.contains(localLC))) localVct.add(localLC);
			
			localLC=sumLC(new LinhaColunaAltura(_lc),(-1)*linha,coluna);
			if((isAValidPosition(localLC))&&(!localVct.contains(localLC))) localVct.add(localLC);
			
			localLC=sumLC(new LinhaColunaAltura(_lc),linha,(-1)*coluna);
			if((isAValidPosition(localLC))&&(!localVct.contains(localLC))) localVct.add(localLC);
			
			localLC=sumLC(new LinhaColunaAltura(_lc),(-1)*linha,(-1)*coluna);
			if((isAValidPosition(localLC))&&(!localVct.contains(localLC))) localVct.add(localLC);
			
			linha--;
			coluna++;				
		}
		
		LinhaColunaAltura result[] = new LinhaColunaAltura[localVct.size()];
		for(int i=0;i<localVct.size(); i++)
			result[i]=localVct.get(i);
		return result;
	}
	
	public int calcHopsVertical(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2)
	{
		int linha;
		
		if(_lc1.getLinha()>_lc2.getLinha()) linha=_lc1.getLinha()-_lc2.getLinha();
		else linha=_lc2.getLinha()-_lc1.getLinha();
		
		return (linha);
	}
	public int calcHopsLongitudinal(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2)
	{
		int altura;
		
		if(_lc1.getAltura()>_lc2.getAltura()) altura=_lc1.getAltura()-_lc2.getAltura();
		else altura=_lc2.getAltura()-_lc1.getAltura();
		
		return (altura);
	}

	public int calcHopsHorizontal(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2)
	{
		int coluna;
		
		if(_lc1.getColuna()>_lc2.getColuna()) coluna=_lc1.getColuna()-_lc2.getColuna();
		else coluna=_lc2.getColuna()-_lc1.getColuna();
		
		return (coluna);
	}

	public int calcHops(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2)
	{
		int linha, coluna,altura;
		
		if(_lc1.getAltura()>_lc2.getAltura()) altura=_lc1.getAltura()-_lc2.getAltura();
		else altura=_lc2.getAltura()-_lc1.getAltura();
		
		if(_lc1.getColuna()>_lc2.getColuna()) coluna=_lc1.getColuna()-_lc2.getColuna();
		else coluna=_lc2.getColuna()-_lc1.getColuna();
		
		if(_lc1.getLinha()>_lc2.getLinha()) linha=_lc1.getLinha()-_lc2.getLinha();
		else linha=_lc2.getLinha()-_lc1.getLinha();
		
		return (linha+coluna+1+altura);
	}

	/*
	 * Objetivo: 
	 * 		Posiciona um vertice de acordo com a sua necessidade de chave
	 * 			e a necessidade de seus vizinhos. Se nao acha o posicionamento 
	 * 			perfeito, procura algo alternativo. 
	 * Parametros: 
	 * 		_v: nome do vertice 
	 */
	private void positionateVertex(String _v)
	{
		int assigned=0;
		
		vertexCounter vc=findBindVertex(_v), vcn=null, vcnaux=null;
		for(int i=0; i<vc.neighbor.size(); i++)
		{
			if(assigned==0){
				vcn=findBindVertex(vc.neighbor.get(i).v2);
				if(vcn.assigned) assigned++;
			}
			else
			{
				vcnaux=findBindVertex(vc.neighbor.get(i).v2);
				if(vcnaux.assigned) assigned++;
			}			
		}
		
		LinhaColunaAltura lc;
		SwitchSettings ss;
		
		switch (assigned)
		{
		case 0:
			lc=findFreeSwitch(vc.usingSwitch);
			break;
		case 1:
			if(vcn != null)
				lc = findNearestFreeSwitch(vc.usingSwitch, vcn.lc);
			else
				lc = null;
			break;			
		default:
			lc=findBestFreeSwitch(vc);
			break;
		}
		
		if(lc!=null)
		{
			ss=findSwitch(lc);						
			vc.assignToSwitch(ss);
			remainingVertex--;
		}
		else
		{
			JOptionPane.showMessageDialog(null,"Nao foi possivel posicionar o vertice "+_v,"Posicionamento de vertices - "+assigned,JOptionPane.ERROR_MESSAGE);			
		}
		
	}

	/*
	 * Objetivo: 
	 * 		Cria lista ordenada de vertices e seus links sumarizados 
	 * Parametros: 
	 * 		_v1: nome do vertice da lista bindVertex
	 * 		_v2: nome do vertice ao qual _v1 se comunica
	 * 		_phits: peso da comunicacao entre _v1 e _v2
	 */
	private void insertAtBindVertex(String _v1, String _v2, long _phits)
	{
		boolean achou1=false;
		boolean achou2=false;
		
		for (int j = 0; j < bindVertex.size(); j++)
		{
			if (bindVertex.get(j).vertice.equals(_v1))
			{
				achou1 = true;
				bindVertex.get(j).inc(_v2, _phits);
				calculateSwitch(bindVertex.get(j));
			}

			if (bindVertex.get(j).vertice.equals(_v2))
			{
				achou2 = true;
				bindVertex.get(j).inc(_v1, _phits);
				calculateSwitch(bindVertex.get(j));
			}
			
			if(achou1 && achou2) break;
			
		}
		if (!achou1)
			bindVertex.add(new vertexCounter( _v1, _v2, _phits));
		if (!achou2)
			bindVertex.add(new vertexCounter( _v2, _v1, _phits));
	}
	
	/*
	 * Objetivo: 
	 * 		Calcula a necessidade de um determinado vertice para saber
	 * 			a qual chave deve ser associado. 
	 * Parametros: 
	 * 		_v: nome do vertice
	 */
	private void calculateSwitch(vertexCounter _vc)
	{
		if(_vc.counter==2)
		{
			if (availableSP2>0) insereEmS2P(_vc);
			else insereEmWL2(_vc);
		}
		
		if (_vc.counter==3)
		{
			if (availableSP3>0) insereEmS3P(_vc);
			else insereEmWL3(_vc);
		}
		
		if ((_vc.counter > 3) && (_vc.usingSwitch!=4))
		{
			if(availableSP4>0) insereEmS4P(_vc);
			else insereEmWL4(_vc);			
		}		
	}
	
	
	/*
	 * Objetivo: 
	 * 		Insere vertice na lista de chaves de 2 portas 
	 * Parametros: 
	 * 		_v: nome do vertice
	 */
	private void insereEmS2P(vertexCounter _vc) 
	{
		S2P.add(_vc.vertice);
		availableSP2--;
		
		if(waittingList2.contains(_vc.vertice))
			waittingList2.remove(_vc.vertice);
		
		_vc.usingSwitch=2;
		_vc.waitingForSwitch=0;

	}
	
	/*
	 * Objetivo: 
	 * 		Insere vertice na lista de espera de insercao na fila 
	 * 			de chaves de 2 portas 
	 * Parametros: 
	 * 		_v: nome do vertice
	 */
	private void insereEmWL2(vertexCounter _vc)
	{
		_vc.waitingForSwitch=2;
		_vc.usingSwitch=0;
		waittingList2.add(_vc.vertice);		
	}

	/*
	 * Objetivo: 
	 * 		Insere vertice na lista de chaves de 3 portas 
	 * Parametros: 
	 * 		_v: nome do vertice
	 */
	private void insereEmS3P(vertexCounter _vc)
	{
		S3P.add(_vc.vertice);		
		availableSP3--;
		
		if (waittingList2.contains(_vc.vertice)) waittingList2.remove(_vc.vertice);
		if (waittingList3.contains(_vc.vertice)) waittingList3.remove(_vc.vertice);
		if (S2P.contains(_vc.vertice)) removeDeS2P(_vc.vertice);
		
		_vc.usingSwitch = 3;
		_vc.waitingForSwitch=0;
		
	}
	
	/*
	 * Objetivo: 
	 * 		Retira um vertice da lista de chaves de 2 portas. 
	 * Parametros: 
	 * 		_v: nome do vertice
	 */
	private void removeDeS2P(String _v)
	{
		availableSP2++;
		S2P.remove(_v);
		
		if(waittingList2.size()>0)
		{
			insereEmS2P(findBindVertex(waittingList2.get(0)));
			availableSP2--;
		}
	}
	
	/*
	 * Objetivo: 
	 * 		Insere um vertice na lista de espera de chaves de 3 portas 
	 * Parametros: 
	 * 		_v: nome do vertice
	 */
private void insereEmWL3(vertexCounter _vc)
	{
		if(waittingList2.contains(_vc.vertice))
			waittingList2.remove(_vc.vertice);
		waittingList3.add(_vc.vertice);		
		_vc.waitingForSwitch=3;
	}

	/*
	 * Objetivo: 
	 * 		Insere um vertice na lista de chaves de 4 portas. 
	 * Parametros: 
	 * 		_v: nome do vertice
	 */
private void insereEmS4P(vertexCounter _vc)
	{
		S4P.add(_vc.vertice);
		availableSP4--;
		
		if (waittingList3.contains(_vc.vertice)) waittingList3.remove(_vc.vertice);
		if (waittingList4.contains(_vc.vertice)) waittingList4.remove(_vc.vertice);
		if (S3P.contains(_vc.vertice)) removeDeS3P(_vc.vertice);
		if (S2P.contains(_vc.vertice)) removeDeS2P(_vc.vertice);
		
		_vc.usingSwitch=4;
		_vc.waitingForSwitch=0;

	}

	/*
	 * Objetivo: 
	 * 		Remove um vertice da lista de chaves de 3 portas 
	 * Parametros: 
	 * 		_v: nome do vertice
	 */
	private void removeDeS3P(String _v)
	{
		availableSP3++;
		S3P.remove(_v);
		
		if(waittingList3.size()>0)
		{
			insereEmS3P(findBindVertex(waittingList3.get(0)));
			availableSP3--;
		}
	}

	/*
	 * Objetivo: 
	 * 		Insere vertice na lista de espera de insercao na fila 
	 * 			de chaves de 4 portas 
	 * Parametros: 
	 * 		_v: nome do vertice
	 */
	private void insereEmWL4(vertexCounter _vc)
	{
		if(waittingList3.contains(_vc.vertice)) waittingList3.remove(_vc.vertice);			
			
		_vc.waitingForSwitch=4;
		waittingList4.add(_vc.vertice);
	}

	/*
	 * Objetivo: Salvar a associa��o de Vertices realizado pelo algoritmo
	 * Heuristico Parametros: Nao ha...
	 */
	private void saveHeuristic()
	{
		for (int linha = 0; linha < this.noc.getNumeroLinhas(); linha++)
			for (int coluna = 0; coluna < this.noc.getNumeroColunas(); coluna++)
				for (int altura = 0; altura < this.noc.getNumeroAltura(); altura++)
				noc.conectaLinhaColunaComCore(new LinhaColunaAltura(linha, coluna,altura), "-");
		
		vertexCounter ss;
		for (int i = 0; i < bindVertex.size(); i++)
		{
			ss = bindVertex.get(i);
			if(ss.assigned)
				noc.conectaLinhaColunaComCore(ss.lc, ss.vertice);			
		}
	}
	
private LinhaColunaAltura findFreeSwitch(int _np)
{
	SwitchSettings ss;
	int linha, coluna,altura;
	if(_np!=4)
	{
		for(int i=0; i<SwitchList.size(); i++)
		{
			ss=SwitchList.get(i);
			if(_np==3 || _np==2)
			{
				if((ss.numberOfLinks==_np) && (!ss.occupied))
					return ss.position;
			}
			else
			{
				if(!ss.occupied)
					return ss.position;
			}
		}
	}
	else
	{
		linha=(noc.getNumeroLinhas()%2==1)?(noc.getNumeroLinhas()-1)/2:noc.getNumeroLinhas()/2;
		coluna=(noc.getNumeroColunas()%2==1)?(noc.getNumeroColunas()-1)/2:noc.getNumeroColunas()/2;
		altura=(noc.getNumeroAltura()%2==1)?(noc.getNumeroAltura()-1)/2:noc.getNumeroAltura()/2;
		LinhaColunaAltura lc= new LinhaColunaAltura(linha,coluna,altura), lcList[];

		ss=findSwitch(lc);
		if((ss.numberOfLinks==_np) && (!ss.occupied))
			return lc;
		int nHops=0;
		while(true)
		{
			nHops++;
			lcList=neighborsWithHops(lc,nHops);
			if(lcList.length==0)
				break;
			for(int i=0; i<lcList.length; i++)
			{
				ss=findSwitch(lcList[i]);
				if((ss.numberOfLinks==_np) && (!ss.occupied))
					return lcList[i];
			}
		}
	}
	return null;
}

private LinhaColunaAltura findNearestFreeSwitch(int _np, LinhaColunaAltura _lc)
{
	SwitchSettings ss;
	Vector<LinhaColunaAltura> position = new Vector<LinhaColunaAltura>(100,5);
	int distance=1000000, index=-1, aux;
	
	for(int i=0; i<SwitchList.size(); i++)
	{
		ss=SwitchList.get(i);
		if(_np!=0)
		{
			if((ss.numberOfLinks>=_np) && (!ss.occupied))
				position.add(ss.position);
		}
		else
		{
			if(!ss.occupied)
				position.add(ss.position);
		}
	}
	if(position.size()!=0)
	{
		for(int i=0; i<position.size(); i++)
		{
			if((aux=calcHops(_lc,position.get(i)))<=distance)
			{
				distance=aux;
				index=i;
			}
		}				
	}
	if(index==-1)
		return null;
	return position.get(index);
}

private LinhaColunaAltura findBestFreeSwitch(vertexCounter _vc)
{
	SwitchSettings ss;
	Vector<LinhaColunaAltura> position = new Vector<LinhaColunaAltura>(100,5);
	Vector<vertexCounter> neighbors = new Vector<vertexCounter>(_vc.neighbor.size(),5);
	Vector<vertexPairs> pair = new Vector<vertexPairs>(_vc.neighbor.size(),5);
	vertexCounter vc;
	
	for(int i=0; i<_vc.neighbor.size(); i++)
	{
		vc=findBindVertex(_vc.neighbor.get(i).v2);
		if(vc.assigned)
		{
			neighbors.add(vc);
			pair.add((_vc.neighbor.get(i)));
		}
	}
	
	int index=-1;
	
	for(int i=0; i<SwitchList.size(); i++)
	{
		ss=SwitchList.get(i);
		if(_vc.usingSwitch!=0)
		{
			if((ss.numberOfLinks>=_vc.usingSwitch) && (!ss.occupied))
				position.add(ss.position);
		}
		else
		{
			if(!ss.occupied)
				position.add(ss.position);
		}
	}
	
	if(position.size()!=0)
	{
		for(int i=0; i<position.size(); i++)
		{
			double calc=0;
			double dvalue=0;
			double llvalue, vlvalue, hlvalue, rvalue, value=-1,longlvalue,lhhops;
			long nphits, vhops, hhops, nhops;
			
			for(int j=0; j<neighbors.size(); j++)
			{

				nphits=pair.get(j).phits;				
				nhops=calcHops(neighbors.get(j).lc,position.get(i));
				vhops=calcHopsVertical(neighbors.get(j).lc,position.get(i));
				hhops=calcHopsHorizontal(neighbors.get(j).lc,position.get(i));
				lhhops=calcHopsLongitudinal(neighbors.get(j).lc,position.get(i));
				
				llvalue=(2*EnergiaLinkLocal*nphits); // custo do link local
				vlvalue=((vhops)*(EnergiaLinkVertical*nphits)); // custo do link vertical
				hlvalue=((hhops)*(EnergiaLinkHorizontal*nphits)); // custo do link horizontal
				longlvalue=((lhhops)*(EnergiaLinkLongitudinal*nphits)); // custo do link longitudinal
				rvalue=((nhops*nphits)*(EnergiaBuffer+EnergiaControle)); // custo do roteamento

				dvalue=llvalue+vlvalue+hlvalue+rvalue+longlvalue;
				
				calc+=dvalue;
			}
			
			if((calc<=value) || (value==-1))
			{
				value=calc;
				index=i;
			}
		}				
	}
	if(index==-1)
		return null;
	return position.get(index);
}

	/*
	 * Objetivo: 
	 * 		Encontrar um determinado vertice na lista de vertices
	 * 		(bindVertex) a partir do nome informado. 
	 * Parametros: 
	 * 		_v: nome do vertice a ser pesquisado
	 */
	private vertexCounter findBindVertex(String _v)
	{
		for (int i = 0; i < bindVertex.size(); i++)
			if (bindVertex.get(i).vertice.equals(_v))
				return bindVertex.get(i);
		return null;
	}

	/*
	 * Objetivo: 
	 * 		Criar uma lista de chaves q compoem a noc, armazendo informacoes
	 * 			sobre os vizinhos de cada chave. Esta estrutura ser� utilizada mais adiante
	 * 			para melhor posicinamento dos vertices sobre a noc. 
	 * Parametros: 
	 * 		N�o h�. 
	 * 
	 */
	private void createSwitchList()
	{
		for (int linha = 0; linha < this.noc.getNumeroLinhas(); linha++)
			for (int coluna = 0; coluna < this.noc.getNumeroColunas(); coluna++)
				for (int altura = 0; altura < this.noc.getNumeroAltura(); altura++)
				SwitchList.add(new SwitchSettings(new LinhaColunaAltura(linha, coluna,altura)));
	}

	/*
	 * Objetivo: 
	 * 		Encontrar uma chave na estrutura de chaves (SwitchList) que
	 * 			esteja em uma determinada posicao da noc. 
	 * 	Parametros: 
	 * 		_lc: LinhaColuna da chave.
	 */
	public SwitchSettings findSwitch(LinhaColunaAltura _lc)
	{
		for(int i = 0; i < SwitchList.size(); i++)
		{
			if((SwitchList.get(i).position.getLinha() == _lc.getLinha()) &&
				(SwitchList.get(i).position.getColuna() == _lc.getColuna())&&
				(SwitchList.get(i).position.getAltura() == _lc.getAltura()))
				return SwitchList.get(i);
		}
		return null;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DEPURA��O
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void exibe()
	{
		System.out.println("\nNoC[vMS] (" + noc.getNumeroLinhas() + "x"
				+ noc.getNumeroColunas() + "x"
						+ noc.getNumeroAltura() + ")");
		for (int linha = 0; linha < minimumMapping.length; linha++)
		{
			for (int coluna = 0; coluna < minimumMapping[linha].length; coluna++)
				for (int altura = 0; altura < minimumMapping[linha][coluna].length; altura++)
			{
				if (minimumMapping[linha][coluna][altura] == null)
					return;
				System.out.println("\tRoteador(" + linha + ", " + coluna  + ", " + altura + ")");
				minimumMapping[linha][coluna][altura].exibe();
			}
		}
	}
}
