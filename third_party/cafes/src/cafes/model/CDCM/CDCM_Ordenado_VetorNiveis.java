package cafes.model.CDCM;

public class CDCM_Ordenado_VetorNiveis implements java.io.Serializable
{
	private static final long serialVersionUID = -8325348225188189226L;
	private CDCM_Ordenado_ListaVertices inicio, fim;
	private int nivel;

	public CDCM_Ordenado_VetorNiveis(int nivel)
	{
		inicio = null;
		fim = null;
		this.nivel = nivel;
	}
	public int getNivel()
	{
		return nivel;
	}
	public void setNivel(int nivel)
	{
		this.nivel = nivel;
	}
	public void insereListaVertices(CDCM_Ordenado_ListaVertices n)
	{
		if(inicio == null)
			inicio = n;
		else
			fim.setProx(n);
		n.setAnt(fim);
		fim = n;
	}
	public CDCM_Ordenado_ListaVertices getInicio()
	{
		return inicio;
	}
	public CDCM_Ordenado_ListaVertices getFim()
	{
		return fim;
	}
	public void setInicio(CDCM_Ordenado_ListaVertices inicio)
	{
		this.inicio = inicio;
	}
	public void setFim(CDCM_Ordenado_ListaVertices fim)
	{
		this.fim = fim;
	}
	public void trocaPosicaoNaLista(CDCM_Ordenado_ListaVertices v1, CDCM_Ordenado_ListaVertices v2)
	{
		if(v1 == v2)
			return;
		CDCM_Ordenado_ListaVertices vAux = new CDCM_Ordenado_ListaVertices(v1);

		v1.setInicio(v2.getInicio());
		v1.setFim(v2.getFim());
		v1.setVertice(v2.getVertice());

		v2.setInicio(vAux.getInicio());
		v2.setFim(vAux.getFim());
		v2.setVertice(vAux.getVertice());
	}
	public void atribuiCicloInicialParaTodosVerticeDoNivel()
	{
		CDCM_Ordenado_ListaVertices lv = inicio;
		
		while(lv != null)
		{
			CDCM_Vertice vertice = lv.getVertice();
			long cicloInicial = vertice.getCicloInicial();
			CDCM_Ordenado_Vertice p = lv.getInicio();
			
			while(p != null)
			{
				long cicloAuxiliar;
				CDCM_Vertice dependencia = p.getVertice();
				
				if(vertice.getCoreOrigem().equals(dependencia.getCoreOrigem()))
				{
// Se dependência for no envio, então basta esperar que a mensagem saia do link local para enviar uma nova mensagem
					cicloAuxiliar = dependencia.getCicloFinalLocal();
				}
				else
				{
// Se dependência for na recepção, então deve ser esperada toda a mensagem chegar para enviar uma nova mensagem
					cicloAuxiliar = dependencia.getCicloFinal();
				}				
				if(cicloInicial < cicloAuxiliar)
					cicloInicial = cicloAuxiliar;
				p = p.getProx();
			}
			if(cicloInicial <= Long.MIN_VALUE)
			{
// Ainda não tem dependências. Neste caso, é atribuído -1 para que quando for computado o ciclo inicial seja somado
// com 1 de forma que o primeiro ciclo de vértices que não tem dependências é dado apenas pelo tempo de computação
				cicloInicial = -1;
			}
// O ciclo inicial de um vértice é calculado tendo como base o maior ciclo final dos vértices que este depende
// somado de mais um ciclo para gerar o slot do próprio vértice e somado ao tempo de computação necessário para 
// processar a mensagem, considerando
			vertice.setCicloInicial(cicloInicial + 1 + vertice.getComputacao());
			lv = lv.getProx();
		}
	}
	private void acumulaTempoComputacaoVerticesIguaisMesmoNivel()
	{
		CDCM_Ordenado_ListaVertices base = inicio;
		while(base != null)
		{
			CDCM_Vertice verticeBase = base.getVertice();
			CDCM_Ordenado_ListaVertices resto = base.getProx();

			while(resto != null)
			{
				CDCM_Vertice verticeResto = resto.getVertice();
				
				if(verticeBase.getInf().getOrigem().equals(verticeResto.getInf().getOrigem()))
				{
// O vértice base e o vértice resto referem-se ao mesmo módulo. Como os dois serão lançados na rede simultaneamente, o tempo 
// de computação de cada vértice é somado, que equivaleria a ter dois processamentos distintos. Um para cada mensagem.
					verticeBase.setCicloInicial(verticeBase.getCicloInicial() + verticeResto.getComputacao());
				}
				resto = resto.getProx();
			}
			base = base.getProx();
		}
	}
// A idéia aqui é reordenar o nível de forma que os vértices com tempo menor sejam mapeados na NoC antes dos demais
// O motivo é que desta forma reduz o erro na NoC com relação a estimativas de onde uma mensagem pode ser lançada 
// antes, mas o seu tempo verdadeiro seria depois, implicando que a mensagem lançada depois não ganhe o recurso.
// Para conseguir esta reordenação, todos os vértices devem ter o seu tempo de início atualizados com a execução
// do nível anterior.
	public void reordenaListaPorMenorTempo()
	{
		atribuiCicloInicialParaTodosVerticeDoNivel();
		acumulaTempoComputacaoVerticesIguaisMesmoNivel();
		CDCM_Ordenado_ListaVertices base = inicio;

		while(base != null)
		{
			CDCM_Ordenado_ListaVertices resto = base.getProx();

			while(resto != null)
			{
				if(base.getVertice().getCicloInicial() > resto.getVertice().getCicloInicial())
					trocaPosicaoNaLista(base, resto);
				resto = resto.getProx();
			}
			base = base.getProx();
		}
	}
	public String toString()
	{
		String str = "";
		CDCM_Ordenado_ListaVertices base = inicio;

		while(base != null)
		{
			CDCM_Vertice vertice = base.getVertice();
			str = str + vertice.getID() + ": " + vertice.getCoreOrigem() + "->" + vertice.getCoreDestino() +
					"[" + vertice.getComputacao() + ", " + vertice.getPhits() + "]" + "   ";
			base = base.getProx();
		}
		return str;
	}
}
