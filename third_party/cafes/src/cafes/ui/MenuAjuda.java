package cafes.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class MenuAjuda extends Menu implements ActionListener
{
	private static final long serialVersionUID = -6420243141815455901L;
	private MenuItem mCAFES, mCWM, mECWM, mCDM, mCDCM, mACPM, mCTM, Identificacao;

	public MenuAjuda(String titulo)
	{
		super(titulo);

		mCAFES = new MenuItem("CAFES");
		mCWM = new MenuItem("CWM");
		mECWM = new MenuItem("ECWM");
		mCDM = new MenuItem("CDM");
		mCDCM = new MenuItem("CDCM");
		mACPM = new MenuItem("ACPM");
		mCTM = new MenuItem("CTM");
		Identificacao = new MenuItem("About");

		add(mCAFES);
		addSeparator();
		add(mCWM);
		add(mECWM);
		add(mCDM);
		add(mCDCM);
		add(mACPM);
		add(mCTM);
		addSeparator();
		add(Identificacao);

		mCAFES.addActionListener(this);
		mCWM.addActionListener(this);
		mECWM.addActionListener(this);
		mCDM.addActionListener(this);
		mCDCM.addActionListener(this);
		mACPM.addActionListener(this);
		mCTM.addActionListener(this);
		Identificacao.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		JTextPane j = new JTextPane();

		if(e.getSource().equals(mCAFES))
		{
			j.replaceSelection("\n\tCAFES é um framework para análise da comuniação de sistemas embarcados em redes intrachip.\n\tO framework explora tempo de execução da aplicação devido a latência da rede e consumo de energia da infra-estrutura de comunicação.");
			new WindowAjuda(j);
		}
		if(e.getSource().equals(mCWM))
		{
			j.replaceSelection("\n\tCommunication Weighted Model (CWM) é um modelo que contém informações sobre o volume de phits da comunicação entre cada núcleo da aplicação.");
			new WindowAjuda(j);
		}
		if(e.getSource().equals(mECWM))
		{
			j.replaceSelection("\n\tExtended Communication Weighted Model (ECWM) é um modelo que contém informações sobre o volume de phits da comunicação e o número de transições de phits entre cada núcleo da aplicação.");
			new WindowAjuda(j);
		}
		if(e.getSource().equals(mCDM))
		{
			j.replaceSelection("\n\tCommunication Dependence Model (CDM) é um modelo que contém informações sobre o volume de phits da comunicação e a dependênica entre as mensagens dos núcleos da aplicação.");
			new WindowAjuda(j);
		}
		if(e.getSource().equals(mCDCM))
		{
			j.replaceSelection("\n\tCommunication Dependence and Computation Model (CDMM) é um modelo que contém informações sobre o volume de phits da comunicação, a dependênica entre as mensagens transmitidas entre os núcleos e o tempo de computação de cada núcleo após ter todas as suas dependências resolvidas e antes de enviar uma mensagem.");
			new WindowAjuda(j);
		}
		if(e.getSource().equals(mACPM))
		{
			j.replaceSelection("\n\tApplication Communication Pattern Model (ACPM) é um modelo que contém a informação volume da comunicação e ordem das mensagem.");
			new WindowAjuda(j);
		}
		if(e.getSource().equals(mCTM))
		{
			j.replaceSelection("\n\tCommunication Task Model (CTM) é um modelo que contém informações sobre o escalonamento de tarefas da aplicação. Entre cada tarefa é informado também o volume de phits transmitido.");
			new WindowAjuda(j);
		}
		if(e.getSource().equals(Identificacao))
		{
			j.replaceSelection
			(
				"\nCAFES - Communication Analisys for Embedded Systems" +
				"\nVersion: 4.0.1 - Date 12/12/2014" +
				"\n\nAuthors:" +
				"\n\tCesar Augusto Missio Marcon" +
				"\n\tMarco Pokorski Stefani" +
				"\n\tEdson Moreno" +
				"\n\tMarcos Luiggi Lemos Sartori" +
				"\n\nCollaborators:" +
				"\n\tJose Carlos Sant'ana Palma" +
				"\n\tIgor Reis" +
				"\n\tAlexandre Amory"
			);
			new WindowAjuda(j);
		}
	}
}
