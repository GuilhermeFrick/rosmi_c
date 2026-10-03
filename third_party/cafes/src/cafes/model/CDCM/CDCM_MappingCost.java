package cafes.model.CDCM;

import cafes.ui.WindowPrincipal;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import cafes.model.Algoritmo;
import cafes.model.CDM.*;
import cafes.model.CWM.*;
import cafes.model.CDCM.CombinationGenerator;
import cafes.model.CDCM.PythonExec;

public class CDCM_MappingCost extends JFrame implements Cloneable
{
	private static final long serialVersionUID = -7210574571563063265L;
	private WindowPrincipal WP;
	private CDCM_DesenhaSequencia desSeq;
	private ScrollPane SP;
	private int iniPanelX, iniPanelY, fimPanelX, fimPanelY;
	// fault related attributes
	private int nFaults; // max number of faults per scenario
	private int nNoFault; // number of times cafes without faults is executed
	private float[] percentWithFault; // percent of simulated fault scenarios with single, double, triple, ... ,faults
	private int nSpares; // number of spare tiles
	private int spares[][]; // specify the spare tiles
	private String faultSimulationMethod; // must be 'percent' or 'min_sample_size'

	public CDCM_MappingCost(WindowPrincipal WP, int IniX, int IniY, int numPixelColuna, int numPixelLinha)
	{
		super("CDCM Mapping");
		this.WP = WP;
		iniPanelX = IniX + 10;
		iniPanelY = IniY + 10;
		fimPanelX = numPixelColuna - 20;
		fimPanelY = numPixelLinha - 70;

		setLocation(IniX, IniY);
		getContentPane().setLayout(null);
		setMenuBar(new CDCM_MenuBarra(this));
		addWindowListener
		(
			new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					dispose(); 
				}
			}
		);
		insereScrollPanel();
		setSize(numPixelColuna, numPixelLinha);
		setVisible(true);
		setResizable(false);
	}

	/*
	 * this part of the code is related to fault simulation. By Amory 
	 */
	public CDCM_MappingCost(WindowPrincipal WP, String fileCommandLine[])
	{
		this.WP = WP;
		desSeq = new CDCM_DesenhaSequencia(this);
		if(fileCommandLine.length == 1) // Lê apenas um único arquivo pela linha de comando
		{
			CDCM_GrafoFormatoTextual.leArquivo(WP, getSequencia(), fileCommandLine[0]);
			new CDCM_2NoC(getSequencia(), WP, Algoritmo.SimulatedAnnealing, true,"");
		}
		else
		{
			if(fileCommandLine.length == 4) 
			{
				long tInicial=0, tFinal=0;
				tInicial = new Date().getTime();
				// read the application file
				CDCM_GrafoFormatoTextual.leArquivo(WP, getSequencia(), fileCommandLine[0]);
				// generate basic dir structure
				String diretorioRaiz = fileCommandLine[1] + "/" + fileCommandLine[2];
				File file = new File(diretorioRaiz);
				if (!file.exists())
					if(! file.mkdir()){
						System.out.printf("Failed to create directory %s!", diretorioRaiz);
					}
				file = new File(diretorioRaiz + "/no_fault");
				if (!file.exists())
					if(! file.mkdir()){
						System.out.printf("Failed to create directory %s/no_fault!", diretorioRaiz);
					}
				file = new File(diretorioRaiz + "/with_fault");
				if (!file.exists())
					if(! file.mkdir()){
						System.out.printf("Failed to create directory %s/with_fault!", diretorioRaiz);
					}
				System.out.println("---------------------------------------------------------------------------");
				readFaultConfigFile(fileCommandLine[3]);
				System.out.println("setting up fault-free scenarios ...");
				generateDirsWithoutFault(diretorioRaiz);
				System.out.println("setting up faulty scenarios ...");
				System.out.println("---------------------------------------------------------------------------");
				if (this.faultSimulationMethod.equals("percent")){
					generateDirsWithFault_Percent(diretorioRaiz);
				}
				else{
					generateDirsWithFault_MinSample(diretorioRaiz);
				}
			    // print the number of scenarios to be executed
			    int nlines = this.WP.getNumLinhas();
			    int ncols = this.WP.getNumColunas();
			    int numCombs, numDirs;
			    numDirs = new File(diretorioRaiz + "/no_fault").listFiles().length;
			    System.out.printf("Number of scenarios with no faults is %d\n", numDirs);
			    for(int i = 0; i < this.nFaults; i++)
				{
			    	numCombs = (int) combinations((long)(nlines*ncols-this.nSpares),(long)(i+1));
				    numDirs = new File(diretorioRaiz + "/with_fault/"+Integer.toString(i+1)).listFiles().length;
				    System.out.printf("Number of scenarios for %d fault is %d combinations where %d are executed\n", i+1,numCombs,numDirs);
				}
				System.out.println("---------------------------------------------------------------------------");
				System.out.println("executing all scenarios ...");
				runFaultScenarios(diretorioRaiz);
				System.out.println("---------------------------------------------------------------------------");
				System.out.println("executing statitistics ...");
				runStatistics(diretorioRaiz);
				tFinal = new Date().getTime();
				System.out.println("Execution Time of Fault Analysis (CPU time): " + (tFinal - tInicial) + "ms");
			}
		}
		new RuntimeException("Erro de formato!");
		
	}

	
	// Read the fault configuration file. The file format is:
	//   - number of faults per scenario, called 'n'
	//   - number of cafes execution without fault. Integer value
	//   - fault simulation method: percent, min_sample_size
	//   If fault simulation method is 'percent'
	//   	- percentage of total faults to be simulated assuming 1 fault per scenario. Float value between 0.0 to 1.0. Use 1.0 to simulate all possible scenarios.
	//   	- percentage of total faults to be simulated assuming 2 faults per scenario. Float value between 0.0 to 1.0. Use 1.0 to simulate all possible scenarios.
	//   	- percentage of total faults to be simulated assuming 'n' faults per scenario. Float value between 0.0 to 1.0. Use 1.0 to simulate all possible scenarios.
	//	 Else if fault simulation method is 'min_sample_size'
	//   	- minimal number of faults to be simulated assuming 1 fault per scenario. Integer value between 1 to x. x must be lower than the total number of possible scenarios.
	//   	- minimal number of faults to be simulated assuming 2 faults per scenario. Integer value between 1 to x. x must be lower than the total number of possible scenarios.
	//   	- minimal number of faults to be simulated assuming 'n' faults per scenario. Integer value between 1 to x. x must be lower than the total number of possible scenarios.
	//   - number of spare tiles, called 'm'. If it is zero, the rest of the file is ignored
	//   - spare tile # 1. Example: 1,0
	//   - spare tile # 2 
	//   - spare tile # 'm' 
	// 
	private void readFaultConfigFile(String faultConfigFile)
	{
	    int nlines = this.WP.getNumLinhas();
	    int ncols = this.WP.getNumColunas();
	    try {
		    BufferedReader in = new BufferedReader(new FileReader(faultConfigFile));
		    String str;
		    str = in.readLine();
		    this.nFaults = Integer.parseInt(str.trim());
		    if (this.nFaults > 3){
		    	System.out.printf("WARNING: %d number of faults. The simulation time migth be too big. We suggest up to three faults.\n", this.nFaults);
		    }
		    if (this.nFaults > nlines*ncols){
		    	System.out.printf("ERROR: number of faults %d is bigger than the number of tiles %d.\n", this.nFaults,nlines*ncols);
		    	System.exit(1);
		    }
		    this.percentWithFault = new float[this.nFaults];
		    //this.nWithFault = new int[this.nFaults];
		    str = in.readLine();
		    this.nNoFault = Integer.parseInt(str.trim());
		    this.faultSimulationMethod = in.readLine();
		    if (! this.faultSimulationMethod.equals("percent")){
		    	if (! this.faultSimulationMethod.equals("min_sample_size")){
		    		System.out.printf("ERROR: Fault simulation method must be 'percent' or 'min_sample_size'. Got %s\n", this.faultSimulationMethod);
		    		System.exit(1);
		    	}
		    }
		    if (this.faultSimulationMethod.equals("percent")){
			    for(int i = 0; i < this.nFaults; i++)
				{
				    str = in.readLine();
				    this.percentWithFault[i] = Float.valueOf(str.trim()).floatValue();
				    if ((this.percentWithFault[i] > 1.0) || (this.percentWithFault[i] < 0.0)){
				    	System.out.printf("ERROR: percentage of faults must be between 0 and 1.0. Got %.2f.\n", this.percentWithFault[i]);
				    	System.exit(1);
				    }
				}
		    }
		    if (this.faultSimulationMethod.equals("min_sample_size")){
			    for(int i = 0; i < this.nFaults; i++)
				{
				    str = in.readLine();
				    this.percentWithFault[i] = Float.valueOf(str.trim()).floatValue();
				    if (this.percentWithFault[i] < 1.0){
				    	System.out.printf("ERROR: number of fault simulations must be more than 1. Got %.2f.\n", this.percentWithFault[i]);
				    	System.exit(1);
				    }
				}
		    }
		    // read the spare config
		    str = in.readLine();
		    this.nSpares = Integer.parseInt(str.trim());
		    if (this.nSpares > nlines*ncols){
		    	System.out.printf("ERROR: number of spare %d is bigger than the number of tiles %d.\n", this.nSpares,nlines*ncols);
		    	System.exit(1);
		    }
		    this.spares = new int[nSpares][2];
		    for(int i = 0; i < nSpares; i++)
			{
			    str = in.readLine();
			    int idx = str.indexOf(',');
			    this.spares[i][0] = Integer.parseInt(str.substring(0, idx));
			    this.spares[i][1] = Integer.parseInt(str.substring(idx+1, str.length()));
			    if (this.spares[i][0] >= nlines){
			    	System.out.printf("ERROR: Invalid spare address: %d,%d\n", this.spares[i][0], this.spares[i][1]);
			    	System.exit(1);
			    }
			    if (this.spares[i][1] >= ncols){
			    	System.out.printf("ERROR: Invalid spare address: %d,%d\n", this.spares[i][0], this.spares[i][1]);
			    	System.exit(1);
			    }
			}

		    in.close();
		} catch (IOException e) {
			System.out.println("Unable to open file "+ faultConfigFile);
		}
	}	

	private long combinations(long n, long k) {
		
		long t= 1;
		for (long i= Math.min(k, n-k), l= 1; i >= 1; i--, n--, l++) {
			t*= n; t/= l;
		}
		
		return t;
	}

	//Generate the dir structure required to run the fault-free scenarios
	private void generateDirsWithoutFault(String diretorioRaiz)
	{
		File file = new File(diretorioRaiz);
		String dirName, scenarioName;
		
		// generate spare string
		String spare = "";
	    for(int i = 0; i < this.nSpares; i++)
		{
	    	spare += "R["+ Integer.toString(this.spares[i][0]) +", "+ Integer.toString(this.spares[i][1]) +"]: X\n";
		}

	    dirName = diretorioRaiz + "/no_fault";
	    for(int i = 0; i < this.nNoFault; i++)
		{
			// create all scenarios with no faults
	    	scenarioName = dirName + "/Scenario" + Integer.toString(i);
	    	file = new File(scenarioName);
			if (!file.exists())
				if(! file.mkdir()){
					System.out.println("Failed to create directory!");
				}
	    	file = new File(scenarioName,"faults.txt");
	    	FileOutputStream saida;
			try {
				saida = new FileOutputStream(file);
		    	saida.write(spare.getBytes());
		    	saida.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("Could not create file "+file);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Could not write in file "+file);
			}
		}
	}
	
	// Generate the dir structure required to run the faults scenarios
	private void generateDirsWithFault_Percent(String diretorioRaiz)
	{
		File file = new File(diretorioRaiz);
		String dirName, scenarioName, fileContent;
		int randomIdx, i, j, k, idx;
		long toBeDeleted,nCombinations;
		Vector<String> combList=new Vector<String>();
		
		// generate spare string
		String spare = "";
	    for(i = 0; i < this.nSpares; i++)
		{
	    	spare += "R["+ Integer.toString(this.spares[i][0]) +", "+ Integer.toString(this.spares[i][1]) +"]: X\n";
		}

	    int nlines = this.WP.getNumLinhas();
	    int ncols = this.WP.getNumColunas();

    	// generate the element list
    	String elements[] = new String[nlines*ncols-this.nSpares];
    	boolean spareTile;
    	idx=0;
    	for (j = 0; j < nlines; j++) 
    	{
    		for(k = 0; k < ncols; k++)
    		{	// only non-spare tiles can have faults. exclude the spare tiles
    			spareTile = false;
    		    for(i = 0; i < this.nSpares; i++)
    			{
    		    	//System.out.println(this.spares[i][0] + " "+ j + " " + this.spares[i][1]+ " " +k);
    			    if (this.spares[i][0] == j){
    			    	if (this.spares[i][1] == k){
    			    		spareTile = true;
    			    		break;
    			    	}
    			    }
    			}
    		    if (!spareTile){
    		    	elements[idx] = Integer.toString(j)+","+Integer.toString(k);
    		    	idx ++;
    		    }
    		}
    	}
    	idx=0;
    	
    	
    	Random randomGenerator = new Random();

    	// create the directories with fault scenarios
	    for(i = 0; i < this.nFaults; i++)
		{
	    	nCombinations = combinations((long)(nlines*ncols-this.nSpares),(long)(i+1));
		    // generate all the combinations of fault location
	    	combList.clear();
		    int[] indices;
		    CombinationGenerator x = new CombinationGenerator (elements.length, i+1);
		    StringBuffer combination;
		    while (x.hasMore ()) {
		      combination = new StringBuffer ();
		      indices = x.getNext ();
		      for (j = 0; j < indices.length; j++) {
		        combination.append (elements[indices[j]]+";");
		      }
		      //System.out.println (combination.toString ());
		      combList.add(combination.toString());
		    }
		    
			// select which combination are used and which one are removed
			toBeDeleted = (long) (nCombinations - (nCombinations *this.percentWithFault[i])); 
	    	for(k = 0; k < toBeDeleted; k++)
	    	{
	    		randomIdx = randomGenerator.nextInt(combList.size());
	    		combList.remove(randomIdx);
	    	}

	    	// create dir for all scenarios with 'i' faults
	    	dirName = diretorioRaiz + "/with_fault/" + Integer.toString(i+1);
			file = new File(dirName);
			if (!file.exists())
				if(! file.mkdir()){
					System.out.println("Failed to create directory!");
				}

	    	// create all scenarios with 'i' faults
		    for(j = 0; j < combList.size(); j++)
			{
			    // get the combination of faults for this scenario
		    	String auxStr = combList.elementAt(j);
		    	String faultLoc;
		    	int comb[][] = new int[i+1][2];
		    	k = 0;
		    	do
		    	{
		    		idx = auxStr.indexOf(';');
		    		faultLoc = auxStr.substring(0, idx);
		    		auxStr = auxStr.substring(idx+1,auxStr.length());
		    		idx = faultLoc.indexOf(',');
		    		comb[k][0] = Integer.parseInt(faultLoc.substring(0, idx));
		    		comb[k][1] = Integer.parseInt(faultLoc.substring(idx+1, faultLoc.length()));
		    		k++;
		    	}while(!auxStr.isEmpty());

		    	// create the scenario dir
		    	scenarioName = dirName + "/Scenario" + Integer.toString(j);
		    	file = new File(scenarioName);
				if (!file.exists())
					if(! file.mkdir()){
						System.out.println("Failed to create directory!");
					}
				// create the fault location file for this scenario
		    	file = new File(scenarioName,"faults.txt");
		    	FileOutputStream saida;
				try {
					saida = new FileOutputStream(file);
				
			    	for(k = 0; k < comb.length; k++)
			    	{
			    		fileContent = "R["+ Integer.toString(comb[k][0]) +", "+ Integer.toString(comb[k][1]) +"]: T\n";
			    		saida.write(fileContent.getBytes());
			    	}
			    	saida.write(spare.getBytes());
			    	saida.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.out.println("Could not create file "+file);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Could not write in file "+file);
				}

			}
		}
	}
	
	// Generate the dir structure required to run the faults scenarios
	private void generateDirsWithFault_MinSample(String diretorioRaiz)
	{
		File file = new File(diretorioRaiz);
		String dirName, scenarioName, fileContent;
		int randomIdx, i, j, k, l, idx;
		long toBeDeleted;
		String auxStr;
		Vector<Vector<String>> combList=new Vector< Vector<String> >();
		
		// generate spare string
		String spare = "";
	    for(i = 0; i < this.nSpares; i++)
		{
	    	spare += "R["+ Integer.toString(this.spares[i][0]) +", "+ Integer.toString(this.spares[i][1]) +"]: X\n";
		}

	    int nlines = this.WP.getNumLinhas();
	    int ncols = this.WP.getNumColunas();

    	// generate the element list
    	String elements[] = new String[nlines*ncols-this.nSpares];
    	boolean spareTile;
    	idx=0;
    	for (j = 0; j < nlines; j++) 
    	{
    		for(k = 0; k < ncols; k++)
    		{	// only non-spare tiles can have faults. exclude the spare tiles
    			spareTile = false;
    		    for(i = 0; i < this.nSpares; i++)
    			{
    		    	//System.out.println(this.spares[i][0] + " "+ j + " " + this.spares[i][1]+ " " +k);
    			    if (this.spares[i][0] == j){
    			    	if (this.spares[i][1] == k){
    			    		spareTile = true;
    			    		break;
    			    	}
    			    }
    			}
    		    if (!spareTile){
    		    	elements[idx] = Integer.toString(j)+","+Integer.toString(k);
    		    	idx ++;
    		    }
    		}
    	}
    	idx=0;
    	if (elements.length<=0){
    		System.out.println("Invalid number of fault combinations!");
    		System.exit(1);
    	}
    	combList.setSize(elements.length);
    	
    	
    	Random randomGenerator = new Random();

    	// create the directories with fault scenarios
	    for(i = 0; i < this.nFaults; i++)
		{
	    	/* generate all the combinations of fault location and organize them in several lists
		     * such that each list correspond to one tile. For example, the list 0 correspond
		     * to the combinations which include the tile 0 ([0,0]).
		     */
	    	combList.clear();
	    	Vector<String> xuxu;
	    	for(j = 0; j < elements.length; j++)
			{	xuxu = new Vector<String>();
	    		combList.add(xuxu);
			}
			
		    int[] indices;
		    CombinationGenerator x = new CombinationGenerator (elements.length, i+1);
		    StringBuffer combination;
		    while (x.hasMore ()) {
		      combination = new StringBuffer ();
		      indices = x.getNext ();
		      for (j = 0; j < indices.length; j++) {
		        combination.append (elements[indices[j]]+";");
		      }
		      //System.out.println (combination.toString ());
		      /* group the combinations based on their indexes.
		       * For instance. combination 0,0;2,1;1,3 are stored in the vector
		       * related to node 0,0; 2,1; and 1,3 
		       */
		      for (j = 0; j < indices.length; j++) {
		    	  combList.elementAt(indices[j]).add(combination.toString());
			  }
		      
		    }
		    
			/* select which combinations will be removed.
			 * if this.percentWithFault[i] < 1.0, then some combinations are removed, otherwise
			 * the full set of combinations (i.e. exhaustive simulation) will be performed.
			 * For instance, if this.percentWithFault[i] == 0.3, then only 30% of the full 
			 * set of combination will be executed.
			 */
			//totalCombPerElement = combList.elementAt(0).size();
			//expectedNumComb = (long)((float)totalCombPerElement * this.percentWithFault[i]);
	    	for(j = 0; j < combList.size(); j++)
	    	{	// calculate the number of combinations to be deleted from this list
	    		toBeDeleted = (long) (combList.elementAt(j).size() - (long)this.percentWithFault[i]);
	    		int nTries = 1000;
	    		if (toBeDeleted > 0)
	    		{
		    		do
		    		{
		    			nTries--;
		    			// select the combination to be deleted
		    			randomIdx = randomGenerator.nextInt(combList.elementAt(j).size());
		    			// get the combination to be deleted
				    	auxStr = combList.elementAt(j).elementAt(randomIdx);
				    	// test if the combination can be removed
				    	boolean canBeRemoved = true;
				    	// find the other lists where this combination is located and remove it too
			    		for(l = 0; l < combList.size(); l++)
			    		{
			    			idx = combList.elementAt(l).indexOf(auxStr);
			    			// to be approach the combination must be found on this list and the list must have elements to be deleted 
			    			if (idx != -1){
			    				if ((combList.elementAt(l).size() - this.percentWithFault[i]) <= 0){
			    					canBeRemoved = false;
			    					break;
			    				}
			    			}
			    		}
			    		// if the removal has been approved, then remove them
			    		if (canBeRemoved){
					    	// remove this combination
					    	combList.elementAt(j).remove(randomIdx);
					    	// remove from the duplicated lists
				    		for(l = 0; l < combList.size(); l++)
				    		{
				    			idx = combList.elementAt(l).indexOf(auxStr);
				    			if (idx != -1)
				    				combList.elementAt(l).remove(idx);
				    		}
				    		toBeDeleted--;
				    		nTries = 1000;
			    		}
		    		}while((toBeDeleted>0) && (nTries>0));
	    		}
	    	}
	    	// eliminate the duplicated combinations
	    	//Set<String> setComb = new HashSet<String>();
	    	Set<String> setComb = new HashSet<String>();
	    	for(l = 0; l < combList.size(); l++)
    		{
	    		setComb.addAll(combList.elementAt(l));
	    		//System.out.println(l + " " + combList.elementAt(l).size());
    		}
	    	Vector<String> vetComb = new Vector<String>();
	    	vetComb.addAll(setComb);
	    	
	    	// create dir for all scenarios with 'i' faults
	    	dirName = diretorioRaiz + "/with_fault/" + Integer.toString(i+1);
			file = new File(dirName);
			if (!file.exists())
				if(! file.mkdir()){
					System.out.println("Failed to create directory!");
				}

	    	// create all scenarios with 'i' faults
		    for(j = 0; j < vetComb.size(); j++)
			{
			    // get the combination of faults for this scenario
		    	auxStr = vetComb.elementAt(j);
		    	String faultLoc;
		    	int comb[][] = new int[i+1][2];
		    	k = 0;
		    	do
		    	{
		    		idx = auxStr.indexOf(';');
		    		faultLoc = auxStr.substring(0, idx);
		    		auxStr = auxStr.substring(idx+1,auxStr.length());
		    		idx = faultLoc.indexOf(',');
		    		comb[k][0] = Integer.parseInt(faultLoc.substring(0, idx));
		    		comb[k][1] = Integer.parseInt(faultLoc.substring(idx+1, faultLoc.length()));
		    		k++;
		    	}while(!auxStr.isEmpty());

		    	// create the scenario dir
		    	scenarioName = dirName + "/Scenario" + Integer.toString(j);
		    	file = new File(scenarioName);
				if (!file.exists())
					if(! file.mkdir()){
						System.out.println("Failed to create directory!");
					}
				// create the fault location file for this scenario
		    	file = new File(scenarioName,"faults.txt");
		    	FileOutputStream saida;
				try {
					saida = new FileOutputStream(file);
				
			    	for(k = 0; k < comb.length; k++)
			    	{
			    		fileContent = "R["+ Integer.toString(comb[k][0]) +", "+ Integer.toString(comb[k][1]) +"]: T\n";
			    		saida.write(fileContent.getBytes());
			    	}
			    	saida.write(spare.getBytes());
			    	saida.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.out.println("Could not create file "+file);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Could not write in file "+file);
				}

			}
		}
	}	
	/*
	// Generate the dir structure required to run the faults scenarios
	private void generateDirsWithFault(String diretorioRaiz)
	{
		File file = new File(diretorioRaiz);
		String dirName, scenarioName, fileContent;
		int randomIdx, i, j, k, l, idx;
		long toBeDeleted, totalCombPerElement, expectedNumComb;
		
		String auxStr;
		Vector<Vector<String>> combList=new Vector< Vector<String> >();
		
		// generate spare string
		String spare = "";
	    for(i = 0; i < this.nSpares; i++)
		{
	    	spare += "R["+ Integer.toString(this.spares[i][0]) +", "+ Integer.toString(this.spares[i][1]) +"]: X\n";
		}

	    int nlines = this.WP.getNumLinhas();
	    int ncols = this.WP.getNumColunas();

    	// generate the element list
    	String elements[] = new String[nlines*ncols-this.nSpares];
    	boolean spareTile;
    	idx=0;
    	for (j = 0; j < nlines; j++) 
    	{
    		for(k = 0; k < ncols; k++)
    		{	// only non-spare tiles can have faults. exclude the spare tiles
    			spareTile = false;
    		    for(i = 0; i < this.nSpares; i++)
    			{
    		    	//System.out.println(this.spares[i][0] + " "+ j + " " + this.spares[i][1]+ " " +k);
    			    if (this.spares[i][0] == j){
    			    	if (this.spares[i][1] == k){
    			    		spareTile = true;
    			    		break;
    			    	}
    			    }
    			}
    		    if (!spareTile){
    		    	elements[idx] = Integer.toString(j)+","+Integer.toString(k);
    		    	idx ++;
    		    }
    		}
    	}
    	idx=0;
    	if (elements.length<=0){
    		System.out.println("Invalid number of fault combinations!");
    		System.exit(1);
    	}
    	combList.setSize(elements.length);
    	
    	
    	Random randomGenerator = new Random();

    	// create the directories with fault scenarios
	    for(i = 0; i < this.nFaults; i++)
		{
	    	* generate all the combinations of fault location and organize them in several lists
		     * such that each list correspond to one tile. For example, the list 0 correspond
		     * to the combinations which include the tile 0 ([0,0]).
		     *
	    	combList.clear();
	    	Vector<String> xuxu;
	    	for(j = 0; j < elements.length; j++)
			{	xuxu = new Vector<String>();
	    		combList.add(xuxu);
			}
			
		    int[] indices;
		    CombinationGenerator x = new CombinationGenerator (elements.length, i+1);
		    StringBuffer combination;
		    while (x.hasMore ()) {
		      combination = new StringBuffer ();
		      indices = x.getNext ();
		      for (j = 0; j < indices.length; j++) {
		        combination.append (elements[indices[j]]+";");
		      }
		      //System.out.println (combination.toString ());
		      * group the combinations based on their indexes.
		       * For instance. combination 0,0;2,1;1,3 are stored in the vector
		       * related to node 0,0; 2,1; and 1,3 
		       *
		      for (j = 0; j < indices.length; j++) {
		    	  combList.elementAt(indices[j]).add(combination.toString());
			  }
		      
		    }
		    
			* select which combinations will be removed.
			 * if this.percentWithFault[i] < 1.0, then some combinations are removed, otherwise
			 * the full set of combinations (i.e. exhaustive simulation) will be performed.
			 * For instance, if this.percentWithFault[i] == 0.3, then only 30% of the full 
			 * set of combination will be executed.
			 *
			totalCombPerElement = combList.elementAt(0).size();
			expectedNumComb = (long)((float)totalCombPerElement * this.percentWithFault[i]);
	    	for(j = 0; j < combList.size(); j++)
	    	{	// calculate the number of combinations to be deleted from this list
	    		if (this.faultSimulationMethod.equals("percent")){
	    			toBeDeleted = combList.elementAt(j).size() - expectedNumComb;
	    		}else{
	    			toBeDeleted = (long) (combList.elementAt(j).size() - (long)this.percentWithFault[i]);
	    		}
	    		int nTries = 1000;
	    		if (toBeDeleted > 0)
	    		{
		    		do
		    		{
		    			nTries--;
		    			// select the combination to be deleted
		    			randomIdx = randomGenerator.nextInt(combList.elementAt(j).size());
		    			// get the combination to be deleted
				    	auxStr = combList.elementAt(j).elementAt(randomIdx);
				    	// test if the combination can be removed
				    	boolean canBeRemoved = true;
				    	// find the other lists where this combination is located and remove it too
			    		for(l = 0; l < combList.size(); l++)
			    		{
			    			idx = combList.elementAt(l).indexOf(auxStr);
			    			// to be approach the combination must be found on this list and the list must have elements to be deleted 
			    			if (idx != -1){
			    				if (this.faultSimulationMethod.equals("percent")){
				    				if ((combList.elementAt(l).size() - expectedNumComb) <= 0){
				    					canBeRemoved = false;
				    					break;
				    				}
			    				}else{
				    				if ((combList.elementAt(l).size() - this.percentWithFault[i]) <= 0){
				    					canBeRemoved = false;
				    					break;
				    				}
			    				}
			    			}
			    		}
			    		// if the removal has been approved, then remove them
			    		if (canBeRemoved){
					    	// remove this combination
					    	combList.elementAt(j).remove(randomIdx);
					    	// remove from the duplicated lists
				    		for(l = 0; l < combList.size(); l++)
				    		{
				    			idx = combList.elementAt(l).indexOf(auxStr);
				    			if (idx != -1)
				    				combList.elementAt(l).remove(idx);
				    		}
				    		toBeDeleted--;
				    		nTries = 1000;
			    		}
		    		}while((toBeDeleted>0) && (nTries>0));
	    		}
	    	}
	    	// eliminate the duplicated combinations
	    	//Set<String> setComb = new HashSet<String>();
	    	Set<String> setComb = new HashSet<String>();
	    	for(l = 0; l < combList.size(); l++)
    		{
	    		setComb.addAll(combList.elementAt(l));
	    		//System.out.println(l + " " + combList.elementAt(l).size());
    		}
	    	Vector<String> vetComb = new Vector<String>();
	    	vetComb.addAll(setComb);
	    	
	    	// create dir for all scenarios with 'i' faults
	    	dirName = diretorioRaiz + "/with_fault/" + Integer.toString(i+1);
			file = new File(dirName);
			if (!file.exists())
				if(! file.mkdir()){
					System.out.println("Failed to create directory!");
				}

	    	// create all scenarios with 'i' faults
		    for(j = 0; j < vetComb.size(); j++)
			{
			    // get the combination of faults for this scenario
		    	auxStr = vetComb.elementAt(j);
		    	String faultLoc;
		    	int comb[][] = new int[i+1][2];
		    	k = 0;
		    	do
		    	{
		    		idx = auxStr.indexOf(';');
		    		faultLoc = auxStr.substring(0, idx);
		    		auxStr = auxStr.substring(idx+1,auxStr.length());
		    		idx = faultLoc.indexOf(',');
		    		comb[k][0] = Integer.parseInt(faultLoc.substring(0, idx));
		    		comb[k][1] = Integer.parseInt(faultLoc.substring(idx+1, faultLoc.length()));
		    		k++;
		    	}while(!auxStr.isEmpty());

		    	// create the scenario dir
		    	scenarioName = dirName + "/Scenario" + Integer.toString(j);
		    	file = new File(scenarioName);
				if (!file.exists())
					if(! file.mkdir()){
						System.out.println("Failed to create directory!");
					}
				// create the fault location file for this scenario
		    	file = new File(scenarioName,"faults.txt");
		    	FileOutputStream saida;
				try {
					saida = new FileOutputStream(file);
				
			    	for(k = 0; k < comb.length; k++)
			    	{
			    		fileContent = "R["+ Integer.toString(comb[k][0]) +", "+ Integer.toString(comb[k][1]) +"]: T\n";
			    		saida.write(fileContent.getBytes());
			    	}
			    	saida.write(spare.getBytes());
			    	saida.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.out.println("Could not create file "+file);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Could not write in file "+file);
				}

			}
		}
	}*/
	
	private void runFaultScenarios(String diretorioRaiz)
	{
		String scenarioName;
		File file;
		int i,j;
		// run fault free scenarios
	    for(i = 0; i < this.nNoFault; i++)
		{
			// change the current dir
	    	scenarioName = diretorioRaiz + "/no_fault/Scenario" + Integer.toString(i);
	    	System.out.printf("\n---------------------------------------------------------------------------\n");
	    	System.out.printf("Executing fault-free scenario %s ...\n", scenarioName);
	    	System.out.printf("---------------------------------------------------------------------------\n");
			//String path = System.getProperties().getProperty("user.dir");
			//String fileS = d.getDirectory() + d.getFile();
			//System.out.println("aonde estou " + System.getProperties().getProperty("user.dir"));
			System.setProperty("user.dir", scenarioName);
			//System.out.println("aonde estou 2" + System.getProperties().getProperty("user.dir"));
	    	
	    	// test the fault file
	    	scenarioName =  scenarioName + "/faults.txt";
	    	file = new File(scenarioName);
			if (!file.exists())
			{
				System.out.printf("File %s not found", scenarioName);
				continue;
			}
	    	// run cafes
	    	new CDCM_2NoC(getSequencia(), WP, Algoritmo.SimulatedAnnealing, true, scenarioName);
		}
		// run fault free scenarios
	    for(j = 0; j < this.nFaults; j++)
	    {	i = 0;
		    while(true)
			{
				// change the current dir
		    	scenarioName = diretorioRaiz + "/with_fault/"+Integer.toString(j+1)+"/Scenario" + Integer.toString(i);
		    	// end the loop when there is no more scenarios 
		    	file = new File(scenarioName);
				if (!file.exists())
				{
					break;
				}
		    	System.out.printf("\n---------------------------------------------------------------------------\n");
		    	System.out.printf("Executing faulty scenario %s ...\n", scenarioName);
		    	System.out.printf("---------------------------------------------------------------------------\n");
				System.setProperty("user.dir", scenarioName);
		    	
		    	// test the fault file
		    	scenarioName =  scenarioName + "/faults.txt";
		    	file = new File(scenarioName);
				if (!file.exists())
				{
					System.out.printf("File %s not found", scenarioName);
					continue;
				}
		    	// run cafes
		    	new CDCM_2NoC(getSequencia(), WP, Algoritmo.SimulatedAnnealing, true, scenarioName);
		    	i ++;
			}
	    }
	}

	private void runStatistics(String diretorioRaiz)
	{
	    int nlines = this.WP.getNumLinhas();
	    int ncols = this.WP.getNumColunas();
	    String execParams,scriptName;
	    String cafesHome = System.getenv("CAFES_HOME");  
	    
	    // common parameters
	    execParams = " -ncols " + Integer.toString(ncols) + " -nlines " + Integer.toString(nlines) + 
		" -c " + diretorioRaiz + " -f " + Integer.toString(nFaults) +" -e -l ";

	    // run basic stats
	    System.out.println("Generating basic statistics ...");
	    scriptName = cafesHome + "/src/cafes/model/CDCM/basic_stat.py";
	    System.out.println(scriptName + execParams);
	    PythonExec.main(scriptName + execParams);

	    // run candlestick chart
	    System.out.println("Generating candlestick charts...");
	    scriptName = cafesHome + "/src/cafes/model/CDCM/candlestick.py";
	    System.out.println(scriptName + execParams);
	    PythonExec.main(scriptName + execParams);

	    // run heat chart
	    System.out.println("Generating heat charts...");
	    scriptName = cafesHome + "/src/cafes/model/CDCM/heat.py";
	    System.out.println(scriptName + execParams);
	    PythonExec.main(scriptName + execParams);

	    // executing gnuplot
	    System.out.println("Executing Gnuplot...");
	    scriptName = "gnuplot < candlestick_energy.plot";
	    System.out.println(scriptName);
	    PythonExec.main(scriptName);
	    scriptName = "gnuplot < heat_energy.plot";
	    System.out.println(scriptName);
	    PythonExec.main(scriptName);
	}
	
	/*
	 * end of the part of the code related to fault simulation. By Amory 
	 */

	
	private void insereScrollPanel()
	{
		int ScrollEmX=2000, ScrollEmY=2000;
		
		SP = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
		SP.setBounds(iniPanelX, iniPanelY, fimPanelX, fimPanelY);
		desSeq = new CDCM_DesenhaSequencia(this, iniPanelX-8, iniPanelY-8, fimPanelX-20+ScrollEmX, fimPanelY-20+ScrollEmY);
		SP.add(desSeq);
		getContentPane().add(SP);
	}
	public WindowPrincipal getWindowPrincipal()
	{
		return WP;
	}
	public CDCM_DesenhaSequencia getDesenhaSequencia()
	{
		return desSeq;
	}
	public boolean isSalvo()
	{
		return desSeq.isSalvo();
	}
	public void setSalvo()
	{
		desSeq.setSalvo();
	}
	public void setNaoSalvo()
	{
		desSeq.setNaoSalvo();
	}
	public void update()
	{
		desSeq.update();
	}
	public CDCM_Sequencia getSequencia()
	{
		return desSeq.getSequencia();
	}
	public void setDesenhaSequencia(CDCM_DesenhaSequencia dseq)
	{
		desSeq = dseq;
	}
	public void CDCM2CWM(CWM_MappingCost mapCost)
	{
		desSeq.CDCM2CWM(mapCost.getDesenhaSequencia());
	}
	public void CDCM2CDM(CDM_MappingCost mapCost)
	{
		desSeq.CDCM2CDM(mapCost.getDesenhaSequencia());
	}
	public void executaASAPeCriaListaDeNiveis()
	{		
		desSeq.executaASAPeCriaListaDeNiveis();
	}
	public void executaAnaliseTemporal()
	{		
		desSeq.executaAnaliseTemporal();
	}
	public void removeAnaliseTemporal()
	{		
		desSeq.removeAnaliseTemporal();
	}
	public int getIniX()
	{
		return iniPanelX;
	}
	public int getIniY()
	{
		return iniPanelY;
	}
	public int getFimX()
	{
		return fimPanelX;
	}
	public int getFimY()
	{
		return fimPanelY;
	}
}