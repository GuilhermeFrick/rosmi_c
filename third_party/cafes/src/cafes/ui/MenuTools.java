package cafes.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.JOptionPane;
import cafes.NoC.*;
import cafes.tools.InsereMetal.*;
import cafes.tools.Logico2spice.*;
import cafes.tools.Spice_BibliotecaVHDL.*;
import cafes.tools.Vhdl2SpiceVhdl.*;

class MenuTools extends Menu implements ActionListener
{
	private static final long serialVersionUID = 2980987313740516955L;
	private String Vhdl2SpiceVhdl="VHDL(Leonardo) to SPICE and VHDL(power)";
	private String Spice2BibliotecaVhdlBatch="SPICE to VHDL Power library (Batch file)";
	private String Spice2BibliotecaVhdl="SPICE to VHDL Power library (only one)";
	private String Logic2Spice="Waveform - Logic to Spice";
	private String InsereMetalStr="Insert Metal Lines";
	private String LeArquivoFalhas="Read File of Faults";

	private MenuItem mVhdl2SpiceVhdl;
	private MenuItem mSpice2BibliotecaVhdlBatch;
	private MenuItem mSpice2BibliotecaVhdl;
	private MenuItem mLogic2Spice;
	private MenuItem mInsereMetalStr;
	private MenuItem mLeArquivoFalhas;
	private WindowPrincipal WP;

	public MenuTools(WindowPrincipal WP, String titulo)
	{
		super(titulo);
		this.WP = WP;

		mVhdl2SpiceVhdl = new MenuItem(Vhdl2SpiceVhdl);
		add(mVhdl2SpiceVhdl);
		mVhdl2SpiceVhdl.addActionListener(this);

		addSeparator();
		mSpice2BibliotecaVhdl = new MenuItem(Spice2BibliotecaVhdl);
		add(mSpice2BibliotecaVhdl);
		mSpice2BibliotecaVhdl.addActionListener(this);

		mSpice2BibliotecaVhdlBatch = new MenuItem(Spice2BibliotecaVhdlBatch);
		add(mSpice2BibliotecaVhdlBatch);
		mSpice2BibliotecaVhdlBatch.addActionListener(this);

		addSeparator();
		mLogic2Spice = new MenuItem(Logic2Spice);
		add(mLogic2Spice);
		mLogic2Spice.addActionListener(this);

		addSeparator();
		mInsereMetalStr = new MenuItem(InsereMetalStr);
		add(mInsereMetalStr);
		mInsereMetalStr.addActionListener(this);
		
		addSeparator();
		mLeArquivoFalhas = new MenuItem(LeArquivoFalhas);
		add(mLeArquivoFalhas);
		mLeArquivoFalhas.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(LeArquivoFalhas))
		{	// by amory
			FileDialog d = new FileDialog(WP, "Fault Config File", FileDialog.LOAD);

			d.setVisible(true);
			if(d.getFile()==null)
				return;
			String fileName = d.getDirectory() + d.getFile();			
			new ArquivoFalhas(fileName);
			return;
		}
		if(e.getActionCommand().equals(InsereMetalStr))
		{
			new InsereMetal(WP);
			return;
		}
		if(e.getActionCommand().equals(Logic2Spice))
		{
			new Logico2Spice(WP);
			return;
		}
		if(e.getActionCommand().equals(Spice2BibliotecaVhdlBatch))
		{
			Spice_BibliotecaVHDL sbv = new Spice_BibliotecaVHDL();
			if(sbv.executar(WP, 0)==false)
				JOptionPane.showMessageDialog(null, "ERRO na geração da biblioteca!", "VHDL Library generation", 0);
			else
				JOptionPane.showMessageDialog(null, "Geração da biblioteca efetuada com sucesso!", "VHDL Library generation", 0);
			return;
		}
		if(e.getActionCommand().equals(Spice2BibliotecaVhdl))
		{
			Spice_BibliotecaVHDL sbv = new Spice_BibliotecaVHDL();
			if(sbv.executar(WP, 1)==false)
				JOptionPane.showMessageDialog(null, "ERRO na geração da biblioteca!", "VHDL Library generation", 0);
			else
				JOptionPane.showMessageDialog(null, "Geração da biblioteca efetuada com sucesso!", "VHDL Library generation", 0);
			return;
		}
		if(e.getActionCommand().equals(Vhdl2SpiceVhdl))
		{
			FileDialog d = new FileDialog(WP, "VHDL Input File", FileDialog.LOAD);

			d.setVisible(true);
			if(d.getFile()==null)
				return;
			String s[] = new String[2];

			s[0] = d.getDirectory() + d.getFile();
			s[1] = "0";

			try
			{
				Vhdl2SpiceVhdl v = new Vhdl2SpiceVhdl();
				int retorno = v.executar(s);
				if(retorno<0)
					JOptionPane.showMessageDialog(null, "ERRO na conversão!", "VHDL to SPICE and VHDL", 0);
				else
					JOptionPane.showMessageDialog(null, "Conversão efetuada com sucesso!\n\t" + retorno + " transistores.", "VHDL to SPICE and VHDL", 0);
			}
			catch(IOException ioe)
			{
				System.out.println("\nProblemas com o conversor VHDL(Leonardo) para SPICE e VHDL(potência)!");
				ioe.printStackTrace();
			}
			return;
		}
	}
}
