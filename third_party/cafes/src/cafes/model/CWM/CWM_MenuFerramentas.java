package cafes.model.CWM;

import java.awt.*;
import java.awt.event.*;

import cafes.model.*;

class CWM_MenuFerramentas extends Menu implements ActionListener
{
	private static final long serialVersionUID = 8259840163382680712L;
	private String ExhaustiveSearchStr="Exhaustive Search Mapping Algorithm";
	private MenuItem ExhaustiveSearchMappingAlgorithm;
	
	private String SimulatedAnnealingStr="Simulated Annealing Mapping Algorithm";
	private MenuItem SimulatedAnnealingMappingAlgorithm;
	
	private String ExhaustiveSearchWithTimeEstimationStr="Exhaustive Search Mapping Algorithm (with timing estimation)";
	private MenuItem ExhaustiveSearchMappingAlgorithmWithTimeEstimation;
	
	private String SimulatedAnnealingWithTimeEstimationStr="Simulated Annealing Mapping Algorithm (with timing estimation)";
	private MenuItem SimulatedAnnealingMappingAlgorithmWithTimeEstimation;
	
	private String TabooSearchStr="Taboo Search Mapping Algorithm";
	private MenuItem TabooSearch;
	
	private String TabooSearchWithTimeEstimationStr="Taboo Search Mapping Algorithm (with timing estimation)";
	private MenuItem TabooSearchWithTimeEstimation;

	private String HeuristicSearchStr="Heuristic Search Mapping Algorithm";
	private MenuItem HeuristicSearch;
	
	private String HeuristicTwoSearchStr="Heuristic Two Search Mapping Algorithm - All possible swaps";
	private MenuItem HeuristicTwoSearch;
	
	private String HeuristicThreeSearchStr="Heuristic Three Search Mapping Algorithm - Heuristic 3";
	private MenuItem HeuristicThreeSearch;
	
	private String HeuristicSearchSAStr="Heuristic Search Mapping + SA Algorithm";
	private MenuItem HeuristicSearch_SA;
	
	private String HeuristicSearchTabooStr="Heuristic Search Mapping + Taboo Algorithm";
	private MenuItem HeuristicSearch_Taboo;
	
	private String BadMappingSearchStr="Bad Mapping Algorithm";
	private MenuItem BadMappingSearch;
	
	private String ManualSearchStr="Manual Search Mapping Algorithm - LCF + Heuristic 2 + SM";
	private MenuItem ManualSearch;
	
	private String ComputeMappingStr="Compute Mapping";
	private MenuItem ComputeMapping;
	
	private String CWMCaseStr="Random CWM Case Generator";
	private MenuItem CWMCaseGen;
	
	private String execCasesStr="Execute a set of computations";
	private MenuItem execCases;
	
	private CWM_MappingCost win;

	public CWM_MenuFerramentas(CWM_MappingCost win, String titulo)
	{
		super(titulo);
		this.win = win;

		ExhaustiveSearchMappingAlgorithm = new MenuItem(ExhaustiveSearchStr);
		add(ExhaustiveSearchMappingAlgorithm);
		ExhaustiveSearchMappingAlgorithm.addActionListener(this);

		SimulatedAnnealingMappingAlgorithm = new MenuItem(SimulatedAnnealingStr);
		add(SimulatedAnnealingMappingAlgorithm);
		SimulatedAnnealingMappingAlgorithm.addActionListener(this);

		ExhaustiveSearchMappingAlgorithmWithTimeEstimation = new MenuItem(ExhaustiveSearchWithTimeEstimationStr);
		add(ExhaustiveSearchMappingAlgorithmWithTimeEstimation);
		ExhaustiveSearchMappingAlgorithmWithTimeEstimation.addActionListener(this);

		SimulatedAnnealingMappingAlgorithmWithTimeEstimation = new MenuItem(SimulatedAnnealingWithTimeEstimationStr);
		add(SimulatedAnnealingMappingAlgorithmWithTimeEstimation);
		SimulatedAnnealingMappingAlgorithmWithTimeEstimation.addActionListener(this);

		TabooSearch = new MenuItem(TabooSearchStr);
		add(TabooSearch);
		TabooSearch.addActionListener(this);

		TabooSearchWithTimeEstimation = new MenuItem(TabooSearchWithTimeEstimationStr);
		add(TabooSearchWithTimeEstimation);
		TabooSearchWithTimeEstimation.addActionListener(this);

		HeuristicSearch = new MenuItem(HeuristicSearchStr);
		add(HeuristicSearch);
		HeuristicSearch.addActionListener(this);

		HeuristicTwoSearch = new MenuItem(HeuristicTwoSearchStr);
		add(HeuristicTwoSearch);
		HeuristicTwoSearch.addActionListener(this);

		HeuristicThreeSearch = new MenuItem(HeuristicThreeSearchStr);
		add(HeuristicThreeSearch);
		HeuristicThreeSearch.addActionListener(this);

		HeuristicSearch_SA = new MenuItem(HeuristicSearchSAStr);
		add(HeuristicSearch_SA);
		HeuristicSearch_SA.addActionListener(this);

		HeuristicSearch_Taboo = new MenuItem(HeuristicSearchTabooStr);
		add(HeuristicSearch_Taboo);
		HeuristicSearch_Taboo.addActionListener(this);

		BadMappingSearch = new MenuItem(BadMappingSearchStr);
		add(BadMappingSearch);
		BadMappingSearch.addActionListener(this);

		ManualSearch = new MenuItem(ManualSearchStr);
		add(ManualSearch);
		ManualSearch.addActionListener(this);

		addSeparator();
		ComputeMapping = new MenuItem(ComputeMappingStr);
		add(ComputeMapping);
		ComputeMapping.addActionListener(this);

		addSeparator();
		CWMCaseGen = new MenuItem(CWMCaseStr);
		add(CWMCaseGen);
		CWMCaseGen.addActionListener(this);

		execCases = new MenuItem(execCasesStr);
		add(execCases);
		execCases.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(ExhaustiveSearchStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.ExhaustiveSearch, false, true);
			return;
		}
		if(e.getActionCommand().equals(SimulatedAnnealingStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.SimulatedAnnealing, false, true);
			return;
		}
		if(e.getActionCommand().equals(ExhaustiveSearchWithTimeEstimationStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.ExhaustiveSearch, true, true);
			return;
		}
		if(e.getActionCommand().equals(SimulatedAnnealingWithTimeEstimationStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.SimulatedAnnealing, true, true);
			return;
		}
		if(e.getActionCommand().equals(TabooSearchStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.TabooSearch, false, true);
			return;
		}
		if(e.getActionCommand().equals(HeuristicSearchStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.HeuristicSearch, false, true);
			return;
		}
		if(e.getActionCommand().equals(HeuristicTwoSearchStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.HeuristicTwoSearch, false, true);
			return;
		}
		if(e.getActionCommand().equals(HeuristicThreeSearchStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.HeuristicThreeSearch, false, true);
			return;
		}
		if(e.getActionCommand().equals(HeuristicSearchSAStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.HeuristicSearch_SA, false, true);
			return;
		}
		if(e.getActionCommand().equals(HeuristicSearchTabooStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.HeuristicSearch_Taboo, false, true);
			return;
		}
		if(e.getActionCommand().equals(BadMappingSearchStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.BadMappingSearch, false, true);
			return;
		}
		if(e.getActionCommand().equals(ManualSearchStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.ManualSearch, false, true);
			return;
		}
		if(e.getActionCommand().equals(TabooSearchWithTimeEstimationStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.TabooSearch, true, true);
			return;
		}
		if(e.getActionCommand().equals(ComputeMappingStr))
		{
			new CWM_2NoC(win.getSequencia(), win.getWindowPrincipal());
			return;
		}
		if(e.getActionCommand().equals(CWMCaseStr))
		{
			new CWM_CaseGenerator(win.getWindowPrincipal(), 0, 0);
			return;
		}
		if(e.getActionCommand().equals(execCasesStr))
		{
			new CWM_CasesExecutor(win);
			return;
		}
	}
}
