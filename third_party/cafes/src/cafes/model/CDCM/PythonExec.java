package cafes.model.CDCM;
import java.io.*;

public class PythonExec {

	/**
	 * @param args
	 * Can be used to exec system calls. In this case is used to call python scripts. 
	 * by Amory. 
	 */
	public static void main(String args) {
        try 
        {
            Runtime r = Runtime.getRuntime();
            Process p;
            if (System.getProperty("os.name").indexOf("Windows") != -1) {
               p = r.exec("cmd /c " + args);
            } else {
               p = r.exec("python " + args);
            }
            p.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null)
            {
                System.out.println(line);
            }
            p.waitFor();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
	}

}
