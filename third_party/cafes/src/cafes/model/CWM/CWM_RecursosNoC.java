package cafes.model.CWM;

class CWM_RecursosNoC
{
	private CWM_ListaTempo inicio, fim;
	
	public CWM_RecursosNoC()
	{
		inicio = null;
		fim = null;
	}
	public CWM_ListaTempo getInicio()
	{
		return inicio;
	}
	public void setInicio(CWM_ListaTempo inicio)
	{
		this.inicio = inicio;
	}
	public CWM_ListaTempo getFim()
	{
		return fim;
	}
	public void setFim(CWM_ListaTempo fim)
	{
		this.fim = fim;
	}
	public void insereAntes(CWM_ListaTempo novo, CWM_ListaTempo base)
	{
		if(base==getInicio()) // troca primeiro nodo
			setInicio(novo);
		else
		{
			base.getAnt().setProx(novo);
			novo.setAnt(base.getAnt());
		}
		novo.setProx(base);
		base.setAnt(novo);
	}
	public void deleta(CWM_ListaTempo nodo)
	{
		if(inicio==nodo) // Primeiro da lista
		{
			if(inicio.getProx()==null) // Só um nodo na lista
			{
				inicio = null;
				fim = null;
				return;
			}
			// tem mais nodos na lista
			inicio = inicio.getProx(); 
			inicio.setAnt(null);
			return;
		}
		if(fim==nodo) // Último da lista
		{
			fim = fim.getAnt(); 
			fim.setProx(null);
			return;
		}
		// Nodo no meio da lista
		nodo.getAnt().setProx(nodo.getProx());
		nodo.getProx().setAnt(nodo.getAnt());
		return;
	}
	public CWM_ListaTempo insereComunicacao(long cicloInicial, long cicloFinal, long ciclosPorPhit)
	{
		CWM_ListaTempo novoIntervalo = new CWM_ListaTempo(cicloInicial, cicloFinal);
		insereIntervaloTempo(novoIntervalo, ciclosPorPhit);
		return novoIntervalo;
	}
	private boolean insereIntervaloTempo(CWM_ListaTempo novoIntervalo, long ciclosPorPhit)
	{
		if(inicio==null)
			inicio = novoIntervalo;
		else
		{
			CWM_ListaTempo p = inicio;
			
			while(p!=null)
			{
				if(inseriuNodo(p, novoIntervalo, ciclosPorPhit)==true)
					return true; // Não precisa inserir nodo, já foi inserido ou nodo anterior teve limites alterados
				p = p.getProx();
			}
			fim.setProx(novoIntervalo);
			novoIntervalo.setAnt(fim);
		}
		fim = novoIntervalo;
		return true;
	}
	private boolean inseriuNodo(CWM_ListaTempo p, CWM_ListaTempo novoIntervalo, long ciclosPorPhit)
	{
		if(novoIntervalo.getCicloFinal() == p.getCicloInicial())
		{
			novoIntervalo.setCicloFinal(p.getCicloFinal()); // Aumenta o intervalo aumentado o ciclo final
			deleta(p);
			return insereIntervaloTempo(novoIntervalo, ciclosPorPhit);
		}
		if(novoIntervalo.getCicloInicial() == p.getCicloFinal())
		{
			novoIntervalo.setCicloInicial(p.getCicloInicial());
			deleta(p);
			return insereIntervaloTempo(novoIntervalo, ciclosPorPhit);
		}
		if(novoIntervalo.getCicloInicial() < p.getCicloInicial() && p.getCicloInicial() < novoIntervalo.getCicloFinal())
		{
			novoIntervalo.setCicloFinal(p.getCicloFinal() + novoIntervalo.getCicloFinal()-p.getCicloInicial() + ciclosPorPhit);
			deleta(p);
			return insereIntervaloTempo(novoIntervalo, ciclosPorPhit);
		}
		if(novoIntervalo.getCicloInicial() == p.getCicloInicial())
		{
			novoIntervalo.setCicloFinal(novoIntervalo.getCicloFinal() + p.getCicloFinal()-p.getCicloInicial() + ciclosPorPhit);
			deleta(p);
			return insereIntervaloTempo(novoIntervalo, ciclosPorPhit);
		}
		if(novoIntervalo.getCicloInicial() > p.getCicloInicial() && novoIntervalo.getCicloInicial() < p.getCicloFinal())
		{
			novoIntervalo.setCicloFinal(p.getCicloFinal() + novoIntervalo.getCicloFinal()-novoIntervalo.getCicloInicial() + ciclosPorPhit);
			novoIntervalo.setCicloInicial(p.getCicloInicial());
			deleta(p);
			return insereIntervaloTempo(novoIntervalo, ciclosPorPhit);
		}
// novoIntervalo está antes do nodo testado. Inserir na novoIntervalo lista
		if(p.getCicloInicial() > novoIntervalo.getCicloFinal())
		{
			insereAntes(novoIntervalo, p);
			return true;
		}
//		 novoIntervalo está depois do nodo testado. Tem que testar se está entre dois nodos ou é o último
//		if(p.getCicloFinal() < novoIntervalo.getCicloInicial())
			return false;
	}
	public long getCicloFinal()
	{
		if(fim==null)
			return -1;
		return fim.getCicloFinal();
	}
}
