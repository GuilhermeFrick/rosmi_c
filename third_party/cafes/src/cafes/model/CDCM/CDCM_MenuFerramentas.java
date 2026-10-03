package cafes.model.CDCM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JOptionPane;

import cafes.model.*;
import cafes.model.CDCM.app.SyntheticApplicationGeneratorGui;
import cafes.model.CDM.*;
import cafes.model.CWM.*;

class CDCM_MenuFerramentas extends Menu implements ActionListener
{
	private static final long serialVersionUID = 6457627689829923723L;
	private String ExhaustiveSearchStr="Exhaustive Search Mapping Algorithm";
	private MenuItem ExhaustiveSearchMappingAlgorithm;
	private String SimulatedAnnealingStr="Simulated Annealing Mapping Algorithm";
	private MenuItem SimulatedAnnealingMappingAlgorithm;
	private String TabooSearchStr="Taboo Search Mapping Algorithm";
	private MenuItem TabooSearch;
	private String ComputeMappingStr="Compute Mapping";
	private MenuItem ComputeMapping;
	
	private String ConverteCDCMParaCDMStr="CDCM to CDM";
	private MenuItem ConverteCDCMParaCDM;
	
	private String ConverteCDCMParaACPMStr="CDCM to ACPM";
	private MenuItem ConverteCDCMParaACPM;
	
	private String ConverteCDCMParaCWMStr="CDCM to CWM";
	private MenuItem ConverteCDCMParaCWM;
	
	private String AnalisysTimingStr="Computation and Communication Analisys";
	private MenuItem AnalisysTiming;

	private String AnalisysCleaningStr="Clear Analisys";
	private MenuItem AnalisysCleaning;

	private String syntheticApplicationGeneratorStr ="Synthetic Application Generator";
	private MenuItem syntheticApplicationGenerator;

	private CDCM_MappingCost win;

	public CDCM_MenuFerramentas(CDCM_MappingCost win, String titulo)
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
		ConverteCDCMParaCDM = new MenuItem(ConverteCDCMParaCDMStr);
		add(ConverteCDCMParaCDM);
		ConverteCDCMParaCDM.addActionListener(this);

		ConverteCDCMParaACPM = new MenuItem(ConverteCDCMParaACPMStr);
		add(ConverteCDCMParaACPM);
		ConverteCDCMParaACPM.addActionListener(this);

		ConverteCDCMParaCWM = new MenuItem(ConverteCDCMParaCWMStr);
		add(ConverteCDCMParaCWM);
		ConverteCDCMParaCWM.addActionListener(this);

		addSeparator();
		AnalisysTiming = new MenuItem(AnalisysTimingStr);
		add(AnalisysTiming);
		AnalisysTiming.addActionListener(this);

		AnalisysCleaning = new MenuItem(AnalisysCleaningStr);
		add(AnalisysCleaning);
		AnalisysCleaning.addActionListener(this);

		addSeparator();
		syntheticApplicationGenerator = new MenuItem(syntheticApplicationGeneratorStr);
		add(syntheticApplicationGenerator);
		syntheticApplicationGenerator.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(ExhaustiveSearchStr))
		{
			new CDCM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.ExhaustiveSearch);
			return;
		}
		if(e.getActionCommand().equals(SimulatedAnnealingStr))
		{
			new CDCM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.SimulatedAnnealing);
			return;
		}
		if(e.getActionCommand().equals(TabooSearchStr))
		{
			new CDCM_2NoC(win.getSequencia(), win.getWindowPrincipal(), Algoritmo.TabooSearch);
			return;
		}
		if(e.getActionCommand().equals(ComputeMappingStr))
		{
			new CDCM_2NoC(win.getSequencia(), win.getWindowPrincipal());
			return;
		}
		if(e.getActionCommand().equals(ConverteCDCMParaCDMStr))
		{
			CDM_MappingCost mapCost = new CDM_MappingCost(win.getWindowPrincipal(), 0, 0, 1024, 768);
			win.CDCM2CDM(mapCost);
			mapCost.update();
			return;
		}
		if(e.getActionCommand().equals(ConverteCDCMParaACPMStr))
		{
			return;
		}
		if(e.getActionCommand().equals(ConverteCDCMParaCWMStr))
		{
			CWM_MappingCost mapCost = new CWM_MappingCost(win.getWindowPrincipal(), 0, 0, 1024, 768);
			win.CDCM2CWM(mapCost);
			mapCost.update();
			return;
		}
		if(e.getActionCommand().equals(AnalisysTimingStr))
		{
			win.executaAnaliseTemporal();
			win.update();
			JOptionPane.showMessageDialog(null, 
							"Blue: Communication path" +
							"\nRed: Computation path" +
							"\nYellow: Computation and Communication path"
							, "Computation and Communication path analisys", JOptionPane.NO_OPTION);
			return;
		}
		if(e.getActionCommand().equals(AnalisysCleaningStr))
		{
			win.removeAnaliseTemporal();
			win.update();
			return;
		}
		if(e.getActionCommand().equals(syntheticApplicationGenerator.getActionCommand()))
		{
			new SyntheticApplicationGeneratorGui(syntheticApplicationGenerator.getActionCommand(), 720, 310);
			return;
		}
	}
}
