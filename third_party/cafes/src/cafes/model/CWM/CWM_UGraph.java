package cafes.model.CWM;

/*
 * Autor: 
 * 		Edson Moreno
 * 		César Marcon
 * Objetivo: 
 * 		Criar um algoritmo heurístico que possa ser utilizado para definir
 * 			o mapeamento de PE sobre a NoC com menor numero de iteracoes e
 * 			da forma mais proxima da otima possivel. 
 */

import java.util.Vector;

import cafes.common.*;

class CWM_UGraph {
	private Vector<vertexLinks> vertexBind;
	private CWM_Grafo g;
	private CWM_NoC noc;
	private Vector<pairComm> vctPair;
	protected double EnergiaBuffer;
	protected double EnergiaControle;
	protected double EnergiaLinkVertical;
	protected double EnergiaLinkHorizontal;
	protected double EnergiaLinkLongitudinal;
	protected double EnergiaLinkLocal;

	public CWM_UGraph(CWM_Grafo _g, CWM_NoC _noc) {
		g = _g;
		noc = _noc;
		vertexBind = new Vector<vertexLinks>(_noc.getNumeroColunas() * _noc.getNumeroLinhas() * _noc.getNumeroAltura(), 5);
		vctPair = new Vector<pairComm>(_noc.getNumeroColunas() * _noc.getNumeroLinhas() * _noc.getNumeroAltura(), 5);

		EnergiaBuffer = noc.getEnergiaBuffer();
		EnergiaControle = noc.getEnergiaControle();
		EnergiaLinkVertical = noc.getEnergiaLinkVertical();
		EnergiaLinkHorizontal = noc.getEnergiaLinkHorizontal();
		EnergiaLinkLocal = noc.getEnergiaLocalLink();
		EnergiaLinkLongitudinal = noc.getEnergiaLinkLongitudinal();

		CWM_Vertice v = g.getInicio();
		CWM_VerticeAdjacente va;

		while (v != null) {
			va = v.getVerticeAdjacenteInicial();
			while (va != null) {
				insertVertexAndNeighbor(v.getInf(), va.getInf(), va.getPhits());
				insertVertexAndNeighbor(va.getInf(), v.getInf(), va.getPhits());
				va = va.getProx();
			}
			v = v.getProx();
		}
	}

	private class vertexLinks {
		public String name;
		public LinhaColunaAltura lc;
		Vector<vertexNeighbor> neighbors = new Vector<vertexNeighbor>(20, 5);

		public vertexLinks(String _name) {
			name = _name;
			lc = new LinhaColunaAltura(-1, -1, -1);
		}

		// public vertexLinks(String _name, LinhaColuna _lc)
		// {
		// name = _name;
		// lc = new LinhaColuna(_lc);
		// }
		// public vertexLinks(String _name, String _neighbor, int _phits)
		// {
		// name = _name;
		// insertNeigbor(_neighbor, _phits);
		// }
		public void insertNeigbor(String _neighbor, long _phits) {
			int index = -1;
			vertexNeighbor vn = new vertexNeighbor(_neighbor, _phits);

			if (neighbors.isEmpty())
				neighbors.add(vn);
			else {
				if ((index = neighbors.indexOf(vn)) != -1)
					neighbors.get(index).inc(_phits);
				else
					neighbors.add(vn);
			}
		}

		public int calcHops(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2) {
			int linha, coluna;

			if (_lc1.getColuna() > _lc2.getColuna())
				coluna = _lc1.getColuna() - _lc2.getColuna();
			else
				coluna = _lc2.getColuna() - _lc1.getColuna();

			if (_lc1.getLinha() > _lc2.getLinha())
				linha = _lc1.getLinha() - _lc2.getLinha();
			else
				linha = _lc2.getLinha() - _lc1.getLinha();

			return (linha + coluna + 1);
		}

		public int calcHopsVertical(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2) {
			int linha;

			if (_lc1.getLinha() > _lc2.getLinha())
				linha = _lc1.getLinha() - _lc2.getLinha();
			else
				linha = _lc2.getLinha() - _lc1.getLinha();

			return linha;
		}

		public int calcHopsHorizontal(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2) {
			int coluna;

			if (_lc1.getColuna() > _lc2.getColuna())
				coluna = _lc1.getColuna() - _lc2.getColuna();
			else
				coluna = _lc2.getColuna() - _lc1.getColuna();

			return (coluna);
		}

		public double calcEnergy(boolean debug) {
			double calc = 0;
			double nphits, vhops, hhops, nhops, lonvalue;
			double value, llvalue, vlvalue, hlvalue, rvalue;
			vertexNeighbor vn;

			for (int i = 0; i < neighbors.size(); i++) {
				vn = neighbors.get(i);

				nphits = vn.phits;
				nhops = calcHops(this.lc, locate(neighbors.get(i).name).lc);
				vhops = calcHopsVertical(this.lc, locate(neighbors.get(i).name).lc);
				hhops = calcHopsHorizontal(this.lc, locate(neighbors.get(i).name).lc);

				llvalue = (2 * EnergiaLinkLocal * nphits); // custo do link
															// local
				vlvalue = ((vhops) * (EnergiaLinkVertical * nphits)); // custo
																		// do
																		// link
																		// vertical
				hlvalue = ((hhops) * (EnergiaLinkHorizontal * nphits)); // custo
																		// do
																		// link
																		// horizontal
				rvalue = ((nhops * nphits) * (EnergiaBuffer + EnergiaControle)); // custo
																					// do
																					// roteamento
				lonvalue = ((vhops) * (EnergiaLinkLongitudinal * nphits));
				value = llvalue + vlvalue + hlvalue + rvalue + lonvalue;
				if (debug)
					managePair(this.name, vn.name, value);
				calc += value;
			}
			return calc;
		}

		public boolean equals(Object o) {
			vertexLinks localvn;
			if (o instanceof vertexLinks) {
				localvn = (vertexLinks) o;
				return localvn.name.equals(this.name);
			}
			return false;
		}
	}

	private class vertexNeighbor {
		public String name;
		public long phits;

		// public vertexNeighbor(String _name)
		// {
		// this(_name, 0);
		// }
		public vertexNeighbor(String _name, long _phits) {
			name = _name;
			phits = _phits;
		}

		public boolean equals(Object o) {
			vertexNeighbor localvn;
			if (o instanceof vertexNeighbor) {
				localvn = (vertexNeighbor) o;
				return localvn.name.equals(this.name);
			}
			return false;
		}

		public void inc(long _phits) {
			phits += _phits;
		}
	}

	private void insertVertexAndNeighbor(String _vertex, String _neighbor, long _phits) {
		vertexLinks vl = new vertexLinks(_vertex);
		int index;

		if (vertexBind.isEmpty())
			vertexBind.add(vl);
		else {
			if ((index = vertexBind.indexOf(vl)) == -1)
				vertexBind.add(vl);
			else
				vl = vertexBind.get(index);
		}
		vl.insertNeigbor(_neighbor, _phits);
	}

	public vertexLinks locate(String _name) {
		vertexLinks vl = new vertexLinks(_name);
		int index = vertexBind.indexOf(vl);

		if (index == -1)
			return null;
		return vertexBind.get(index);
	}

	public vertexLinks locate(LinhaColunaAltura _lc) {
		for (int i = 0; i < vertexBind.size(); i++) {
			if (vertexBind.get(i).lc.equals(_lc))
				return vertexBind.get(i);
		}
		return null;
	}

	public void setposition(String _name, LinhaColunaAltura _lc) {
		vertexLinks vl;
		vl = locate(_name);
		if (vl != null) {
			vl.lc.setColuna(_lc.getColuna());
			vl.lc.setLinha(_lc.getLinha());
			vl.lc.setAltura(_lc.getAltura());
		}
	}

	public LinhaColunaAltura getposition(String _name) {
		int index;
		vertexLinks vl = new vertexLinks(_name);
		if ((index = vertexBind.indexOf(vl)) == -1)
			return null;
		return vertexBind.get(index).lc;
	}

	public double calcCostFor(String _name) {
		vertexLinks vl;
		vl = locate(_name);
		if (vl != null)
			return vl.calcEnergy(false);
		return 0;
	}

	public double calcCostFor(LinhaColunaAltura _lc) {
		vertexLinks vl;
		vl = locate(_lc);
		if (vl != null)
			return vl.calcEnergy(false);
		return 0;
	}

	private double calcCostFor(vertexLinks _vl) {
		if (_vl != null)
			return _vl.calcEnergy(false);
		return 0;
	}

	public void swap(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2) {
		vertexLinks vl1, vl2;
		vl1 = locate(_lc1);
		vl2 = locate(_lc2);
		if (vl1 != null)
			vl1.lc = new LinhaColunaAltura(_lc2);
		if (vl2 != null)
			vl2.lc = new LinhaColunaAltura(_lc1);
	}

	public void linkVertex(LinhaColunaAltura[] _vetLC, String[] _vetCore) {
		vertexLinks vl;
		for (int i = 0; i < _vetCore.length; i++) {
			if (!_vetCore[i].equals("-")) {
				vl = locate(_vetCore[i]);
				if (vl != null) {
					vl.lc.setLinha(_vetLC[i].getLinha());
					vl.lc.setColuna(_vetLC[i].getColuna());
					vl.lc.setAltura(_vetLC[i].getAltura());
				}
			}
		}
	}

	public void linkVertex(CWM_VerticeNoC[][][] matLC) {
		for (int linha = 0; linha < matLC.length; linha++) {
			for (int coluna = 0; coluna < matLC[linha].length; coluna++)
				for (int altura = 0; altura < matLC[linha][coluna].length; altura++) {
					CWM_VerticeNoC roteador = matLC[linha][coluna][altura];

					if (roteador == null)
						continue;
					if (!roteador.getCoreName().equals("-")) {
						vertexLinks vl = locate(roteador.getCoreName());

						if (vl != null) {
							vl.lc.setLinha(linha);
							vl.lc.setColuna(coluna);
							vl.lc.setAltura(altura);
						}
					}
				}
		}
	}

	/*
	 * If the return value is lower then zero, it means that the swap saves
	 * energy
	 */
	public double saveEnergyIfSwap(LinhaColunaAltura _lc1, LinhaColunaAltura _lc2) {
		vertexLinks vl1, vl2;
		double cost1 = 0, cost2 = 0;

		vl1 = locate(_lc1);
		vl2 = locate(_lc2);
		cost1 = calcCostFor(vl1) + calcCostFor(vl2);

		swap(_lc1, _lc2);
		vl1 = locate(_lc1);
		vl2 = locate(_lc2);

		cost2 = calcCostFor(vl1) + calcCostFor(vl2);
		swap(_lc1, _lc2);

		return cost2 - cost1;
	}

	public void saveMapping(CWM_VerticeNoC[][][] target) {
		for (int linha = 0; linha < target.length; linha++) {
			for (int coluna = 0; coluna < target[linha].length; coluna++)
				for (int altura = 0; altura < target[linha][coluna].length; altura++)
					target[linha][coluna][altura] = new CWM_VerticeNoC("-", new LinhaColunaAltura(linha, coluna, altura), noc);
		}
		for (int i = 0; i < vertexBind.size(); i++) {
			vertexLinks vl = vertexBind.get(i);
			LinhaColunaAltura linCol = vl.lc;
			int lin = linCol.getLinha();
			int col = linCol.getColuna();
			int alt = linCol.getAltura();

			if (lin < 0 || col < 0 || alt < 0)
				continue;
			target[lin][col][alt] = new CWM_VerticeNoC(vl.name, new LinhaColunaAltura(lin, col, alt), noc);
		}
	}

	public class pairComm {
		public String v1, v2;
		public double energy;

		public pairComm(String _v1, String _v2, double _energy) {
			v1 = _v1;
			v2 = _v2;
			energy = _energy;
		}

		public boolean equals(Object o) {
			if (o instanceof pairComm) {
				pairComm pc = (pairComm) o;
				return ((pc.v1.equals(v1) && pc.v2.equals(v2)) || (pc.v1.equals(v2) && pc.v2.equals(v1)));
			}
			return false;
		}
	}

	protected void managePair(String _v1, String _v2, double _energy) {
		pairComm pc = new pairComm(_v1, _v2, _energy);
		if (vctPair.isEmpty() || (!vctPair.contains(pc))) {
			vctPair.add(new pairComm(_v1, _v2, _energy));
		} else {
			int index = vctPair.indexOf(pc);
			pc = vctPair.get(index);
			pc.energy = _energy;
		}
	}

	public double exibeCustos() {
		vertexLinks vl;
		double count = 0;

		for (int i = 0; i < vertexBind.size(); i++) {
			vl = vertexBind.get(i);
			vl.calcEnergy(true);
		}

		for (int i = 0; i < vctPair.size(); i++)
			count += vctPair.get(i).energy;

		return count;
	}

	public boolean equals(CWM_VerticeNoC[][] _mapping) {
		vertexLinks vl;

		for (int i = 0; i < vertexBind.size(); i++) {
			vl = vertexBind.get(i);
			if (!_mapping[vl.lc.getLinha()][vl.lc.getColuna()].getCoreName().equals(vl.name))
				return false;
		}
		return true;
	}
}
