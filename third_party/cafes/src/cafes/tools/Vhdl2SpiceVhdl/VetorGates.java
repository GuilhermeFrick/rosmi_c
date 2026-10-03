package cafes.tools.Vhdl2SpiceVhdl;

import java.util.*;

public class VetorGates
{
	private Vector<Gate> gateVector = new Vector<Gate>();
	private int contadorTransistores;

	public VetorGates()
	{
		contadorTransistores = 0;
		
		gateVector.add(new Gate("inv01",	"Inv",	   2));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("tri01",	"TriState",  4));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("mux21",	"Mux2to1",   6));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("buf02",	"Buf",	   6));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("nand02",   "Nand2",	 4));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("and02",	"And2",	  6));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("nor02",	"Nor2",	  4));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("or02",	 "Or2",	   6));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("xor2",	 "Xor2",	 22));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("dffs",	 "DffSet",   26));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("dffr",	 "DffReset", 26));	 // Implementada na biblioteca VHDL  
		gateVector.add(new Gate("trg",	  "trg",	   2));
		gateVector.add(new Gate("nand03",   "nand3",	 6));
		gateVector.add(new Gate("nand04",   "nand4",	 8));
		gateVector.add(new Gate("nand05",   "nand5",	10));
		gateVector.add(new Gate("nand06",   "nand6",	18));
		gateVector.add(new Gate("nand07",   "nand7",	20));
		gateVector.add(new Gate("nand08",   "nand8",	22));
		gateVector.add(new Gate("nand09",   "nand9",	24));
		gateVector.add(new Gate("and03",	"and3",	  8));
		gateVector.add(new Gate("and04",	"and4",	 10));
		gateVector.add(new Gate("and05",	"and5",	 12));
		gateVector.add(new Gate("and06",	"and6",	 16));
		gateVector.add(new Gate("and07",	"and7",	 18));
		gateVector.add(new Gate("and08",	"and8",	 20));
		gateVector.add(new Gate("and09",	"and9",	 22));
		gateVector.add(new Gate("nor03",	"nor3",	  6));
		gateVector.add(new Gate("nor04",	"nor4",	  8));
		gateVector.add(new Gate("nor05",	"nor5",	 16));
		gateVector.add(new Gate("nor06",	"nor6",	 18));
		gateVector.add(new Gate("nor07",	"nor7",	 20));
		gateVector.add(new Gate("nor08",	"nor8",	 22));
		gateVector.add(new Gate("nor09",	"nor9",	 24));
		gateVector.add(new Gate("or03",	 "or3",	   8));
		gateVector.add(new Gate("or04",	 "or4",	  10));
		gateVector.add(new Gate("or05",	 "or5",	  14));
		gateVector.add(new Gate("or06",	 "or6",	  16));
		gateVector.add(new Gate("or07",	 "or7",	  18));
		gateVector.add(new Gate("or08",	 "or8",	  20));
		gateVector.add(new Gate("or09",	 "or9",	  22));
		gateVector.add(new Gate("xnor2",	"nxor2",	24));
		gateVector.add(new Gate("latch",	"latch",	12));
		gateVector.add(new Gate("dff",	  "dff",	  24));
		gateVector.add(new Gate("dffset_P", "dffset_P", 26));
		
	}
	public int getContadorTransistores()
	{
		return contadorTransistores;
	}
	public String retornaGate(String str)
	{
		for(int i=0; i<gateVector.size(); i++)
		{
			Gate aux = gateVector.get(i);
			
			if(aux.getName().equals(str))
			{
				contadorTransistores = contadorTransistores + aux.getNumTransistores();
				return aux.getGate();
			}
		}
		return null;
	}
}
