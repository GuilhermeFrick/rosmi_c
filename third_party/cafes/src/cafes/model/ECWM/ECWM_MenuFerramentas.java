package cafes.model.ECWM;

import java.awt.*;
import java.awt.event.*;

import cafes.model.*;

class ECWM_MenuFerramentas extends Menu implements ActionListener
{
	private static final long serialVersionUID = 8266915092754825577L;
	private String ExhaustiveSearchStr="Exhaustive Search Mapping Algorithm";
	private MenuItem ExhaustiveSearchMappingAlgorithm;
	
	private String SimulatedAnnealingStr="Simulated Annealing Mapping Algorithm";
	private MenuItem SimulatedAnnealingMappingAlgorithm;
	
	private String TabooSearchStr="Taboo Search Mapping Algorithm";
	private MenuItem TabooSearch;
	
	private String ExhaustiveSearchWithTimeEstimationStr="Exhaustive Search Mapping Algorithm (with timing estimation)";
	private MenuItem ExhaustiveSearchMappingAlgorithmWithTimeEstimation;
	
	private String SimulatedAnnealingWithTimeEstimationStr="Simulated Annealing Mapping Algorithm (with timing estimation)";
	private MenuItem SimulatedAnnealingMappingAlgorithmWithTimeEstimation;
	
	private String TabooSearchWithTimeEstimationStr="Taboo Search Mapping Algorithm (with timing estimation)";
	private MenuItem TabooSearchWithTimeEstimation;
	
	private String ComputeMappingStr="Compute Mapping";
	private MenuItem ComputeMapping;
	
	private ECWM_MappingCost win;

	public ECWM_MenuFerramentas(ECWM_MappingCost win, String titulo)
	{
		super(titulo);
		this.win = win;

		ExhaustiveSearchMappingAlgorithm = new MenuItem(ExhaustiveSearchStr);
		add(ExhaustiveSearchMappingAlgorithm);
		ExhaustiveSearchMappingAlgorithm.addActionListener(this);

		SimulatedAnnealingMappingAlgorithm = new MenuItem(SimulatedAnnealingStr);
		add(SimulatedAnnealingMappingAlgorithm);
		SimulatedAnnealingMappingAlgorithm.addActionListener(this);

		TabooSearch = new MenuItem(TabooSearchStr);
		add(TabooSearch);
		TabooSearch.addActionListener(this);

		ExhaustiveSearchMappingAlgorithmWithTimeEstimation = new MenuItem(ExhaustiveSearchWithTimeEstimationStr);
		add(ExhaustiveSearchMappingAlgorithmWithTimeEstimation);
		ExhaustiveSearchMappingAlgorithmWithTimeEstimation.addActionListener(this);

		SimulatedAnnealingMappingAlgorithmWithTimeEstimation = new MenuItem(SimulatedAnnealingWithTimeEstimationStr);
		add(SimulatedAnnealingMappingAlgorithmWithTimeEstimation);
		SimulatedAnnealingMappingAlgorithmWithTimeEstimation.addActionListener(this);

		TabooSearchWithTimeEstimation = new MenuItem(TabooSearchWithTimeEstimationStr);
		add(TabooSearchWithTimeEstimation);
		TabooSearchWithTimeEstimation.addActionListener(this);

		addSeparator();
		ComputeMapping = new MenuItem(ComputeMappingStr);
		add(ComputeMapping);
		ComputeMapping.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(ExhaustiveSearchStr))
		{
			new ECWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.ExhaustiveSearch, false);
			return;
		}
		if(e.getActionCommand().equals(SimulatedAnnealingStr))
		{
			new ECWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.SimulatedAnnealing, false);
			return;
		}
		if(e.getActionCommand().equals(TabooSearchStr))
		{
			new ECWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.TabooSearch, false);
			return;
		}
		if(e.getActionCommand().equals(ExhaustiveSearchWithTimeEstimationStr))
		{
			new ECWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.ExhaustiveSearch, true);
			return;
		}
		if(e.getActionCommand().equals(SimulatedAnnealingWithTimeEstimationStr))
		{
			new ECWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.SimulatedAnnealing, true);
			return;
		}
		if(e.getActionCommand().equals(TabooSearchWithTimeEstimationStr))
		{
			new ECWM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.TabooSearch, true);
			return;
		}
		if(e.getActionCommand().equals(ComputeMappingStr))
		{
			new ECWM_2NoC(win.getSequencia(), win.getWindowPrincipal());
			return;
		}
	}
}
