package cafes.model.CDM;

import java.awt.*;
import java.awt.event.*;

import cafes.model.*;
import cafes.model.CWM.*;

class CDM_MenuFerramentas extends Menu implements ActionListener
{
	private static final long serialVersionUID = 2376439281778764000L;
	private String ExhaustiveSearchStr="Exhaustive Search Mapping Algorithm";
	private MenuItem ExhaustiveSearchMappingAlgorithm;
	private String SimulatedAnnealingStr="Simulated Annealing Mapping Algorithm";
	private MenuItem SimulatedAnnealingMappingAlgorithm;
	private String TabooSearchStr="Taboo Search Mapping Algorithm";
	private MenuItem TabooSearch;
	private String ComputeMappingStr="Compute Mapping";
	private MenuItem ComputeMapping;
	private String ConverteCDMParaCWMStr="CDM to CWM";
	private MenuItem ConverteCDMParaCWM;
	private CDM_MappingCost win;

	public CDM_MenuFerramentas(CDM_MappingCost win, String titulo)
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

		addSeparator();
		ComputeMapping = new MenuItem(ComputeMappingStr);
		add(ComputeMapping);
		ComputeMapping.addActionListener(this);

		addSeparator();
		ConverteCDMParaCWM = new MenuItem(ConverteCDMParaCWMStr);
		add(ConverteCDMParaCWM);
		ConverteCDMParaCWM.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(ExhaustiveSearchStr))
		{
			new CDM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.ExhaustiveSearch);
			return;
		}
		if(e.getActionCommand().equals(SimulatedAnnealingStr))
		{
			new CDM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.SimulatedAnnealing);
			return;
		}
		if(e.getActionCommand().equals(TabooSearchStr))
		{
			new CDM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.TabooSearch);
			return;
		}
		if(e.getActionCommand().equals(ComputeMappingStr))
		{
			new CDM_2NoC(win.getSequencia(), win.getWindowPrincipal());
			return;
		}
		if(e.getActionCommand().equals(ConverteCDMParaCWMStr))
		{
			CWM_MappingCost mapCost = new CWM_MappingCost(win.getWindowPrincipal(), 0, 0, 1024, 768);
			win.CDM2CWM(mapCost);
			mapCost.update();
			return;
		}
	}
}
