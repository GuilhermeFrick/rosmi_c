package cafes;

import cafes.ui.WindowPrincipal;

public class Inicio
{
	public static void main(String args[])
	{
		String tituloPrincipal = "CAFES - Communication Analysis For Embedded Systems - V 4.0.1";
		
		String cafesHome = System.getenv("CAFES_HOME");
		if (cafesHome == null){
			System.out.printf("environment variable CAFES_HOME not defined");
		//	System.exit(1);
		}
		
		if(args != null && args.length > 0)
		{
			/*
			args.length == 1
			Run an single fault scenario
			args[0]: application file
			
			args.length == 3
			Run an entire set of fault scenarios at once. At the end, it generates charts and statistics
			args[0]: application file
			args[1]: target dir
			args[2]: case name
			args[3]: configuration file
			*/
			/*
			String str[] = new String[6];
			//str[0] = "/home2/amory/desenv/eclipse/workspace/CAFES/input_files/CDCG/SegImag.cdcg";
			str[0] = "/home/ale/desenv/workspace/CAFES/input_files/CDCG/SegImag_9_nucleos.cdcg";
			str[1] = System.getProperties().getProperty("user.dir");
			str[2] = "xuxu";
			str[3] = "fault.cfg";
			*/
			//new WindowPrincipal(tituloPrincipal, str);
			new WindowPrincipal(tituloPrincipal, args);
			
			return;
		}
		else{
			new WindowPrincipal(tituloPrincipal);
		}
	}
}
