package cafes.model.CWM;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Label;
import java.awt.TextField;
import java.awt.Button;
import java.awt.List;
import java.awt.Checkbox;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

import javax.swing.JOptionPane;
import java.awt.Font;

import cafes.model.*;

public class CWM_CasesExecutor extends Frame implements ActionListener, ItemListener
{
	private static final long serialVersionUID = 4398233044233318784L;
	private CWM_MappingCost mc = null;
	private Panel pnl_Search = null;
	private Label lbl_search = null;
	private TextField txt_filepath = null;
	private Button btn_browse = null;
	private List lst_search = null;
	private List lst_cases2exec = null;
	private Button btn_delete = null;
	private Button btn_insert = null;
	private Panel pnl_list = null;
	private Panel pnl_algorithms = null;
	private Label lbl_list = null;
	private Label lbl_cases = null;
	private Button btn_execute = null;
	private Checkbox ckbox_Exaustive = null;
	private Checkbox ckbox_Heuristic = null;
	private Checkbox ckbox_SA = null;
	private Checkbox ckbox_Taboo = null;
	private Checkbox ckbox_Heu_SA = null;
	private Checkbox ckbox_Heu_Taboo = null;
	
	private Vector<String> vct_listResult;
	private Vector<String> vct_casesSelected;
	private TextField txt_iteracoes_SA = null;
	private Label lbl_iteracoes = null;
	private Label label = null;
	private TextField txt_temperatura_SA = null;
	private Button btn_default_SA = null;
	private Label label1 = null;
	private TextField txt_vizinhanca_taboo = null;
	private Button btn_default_Taboo = null;
	private Label label2 = null;
	private Label label3 = null;
	private TextField txt_temperatura_heu_SA = null;
	private TextField txt_vizinhanca_heu_taboo = null;
	private Button btn_default_Heu_SA = null;
	private Button btn_default_Heu_Taboo = null;
	private Label label4 = null;
	private TextField txt_iteracoes_Heu_SA = null;
	private Label label5 = null;
	private TextField txt_iteracoes_Heu_Taboo = null;
	private TextField txt_iteracoes_Taboo = null;
	private Label label6 = null;
	private Checkbox ckbox_Bad_Mapping = null;
	/**
	 * This is the default constructor
	 */
	public CWM_CasesExecutor(CWM_MappingCost _mc)
	{
		super();
		initialize();
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
		vct_listResult = new Vector<String>(100, 5);
		vct_casesSelected = new Vector<String>(100, 5);
		mc=_mc;
		setVisible(true);		
		setResizable(false);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{
		this.setLayout(null);
		this.setSize(450, 439);
		this.setName("main frame");
		this.setTitle("Selection window for cases execution");
		this.add(getPnl_Search(), null);
		this.add(getPnl_list(), null);
		this.add(getPnl_algorithms(), null);
		this.add(getBtn_execute(), null);
	}

	/**
	 * This method initializes pnl_Search	
	 * 	
	 * @return java.awt.Panel	
	 */
	private Panel getPnl_Search()
	{
		if (pnl_Search == null)
		{
			lbl_search = new Label();
			lbl_search.setBounds(new java.awt.Rectangle(2,3,45,23));
			lbl_search.setText("Search:");
			pnl_Search = new Panel();
			pnl_Search.setLayout(null);
			pnl_Search.setSize(new java.awt.Dimension(435,27));
			pnl_Search.setLocation(new java.awt.Point(7,29));
			pnl_Search.add(lbl_search, null);
			pnl_Search.add(getTxt_filepath(), null);
			pnl_Search.add(getBtn_browse(), null);
		}
		return pnl_Search;
	}

	/**
	 * This method initializes txt_filepath	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTxt_filepath()
	{
		if (txt_filepath == null)
		{
			txt_filepath = new TextField();
			txt_filepath.setBounds(new java.awt.Rectangle(47,3,317,23));
			txt_filepath.setEditable(false);
		}
		return txt_filepath;
	}

	/**
	 * This method initializes btn_browse	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getBtn_browse()
	{
		if (btn_browse == null)
		{
			btn_browse = new Button();
			btn_browse.setBounds(new java.awt.Rectangle(365,1,68,23));
			btn_browse.setLabel("Browse...");
			btn_browse.addActionListener(this);
		}
		return btn_browse;
	}

	/**
	 * This method initializes lst_search	
	 * 	
	 * @return java.awt.List	
	 */
	private List getLst_search()
	{
		if (lst_search == null)
		{
			lst_search = new List();
			lst_search.setSize(new java.awt.Dimension(199,130));
			lst_search.setMultipleMode(true);
			lst_search.setLocation(new java.awt.Point(2,23));
		}
		return lst_search;
	}

	/**
	 * This method initializes lst_cases2exec	
	 * 	
	 * @return java.awt.List	
	 */
	private List getLst_cases2exec()
	{
		if (lst_cases2exec == null)
		{
			lst_cases2exec = new List();
			lst_cases2exec.setLocation(new java.awt.Point(232,23));
			lst_cases2exec.setMultipleMode(true);
			lst_cases2exec.setSize(new java.awt.Dimension(199,130));
		}
		return lst_cases2exec;
	}

	/**
	 * This method initializes btn_insert	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getBtn_insert()
	{
		if (btn_delete == null)
		{
			btn_delete = new Button();
			btn_delete.setLabel("<<");
			btn_delete.setLocation(new java.awt.Point(202,84));
			btn_delete.setSize(new java.awt.Dimension(28,23));
			btn_delete.addActionListener(this);
		}
		return btn_delete;
	}

	/**
	 * This method initializes button	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getButton()
	{
		if (btn_insert == null)
		{
			btn_insert = new Button();
			btn_insert.setLabel(">>");
			btn_insert.setLocation(new java.awt.Point(202,60));
			btn_insert.setSize(new java.awt.Dimension(28,23));
			btn_insert.addActionListener(this);
		}
		return btn_insert;
	}

	/**
	 * This method initializes pnl_list	
	 * 	
	 * @return java.awt.Panel	
	 */
	private Panel getPnl_list()
	{
		if (pnl_list == null)
		{
			lbl_cases = new Label();
			lbl_cases.setName("");
			lbl_cases.setSize(new java.awt.Dimension(66,23));
			lbl_cases.setLocation(new java.awt.Point(298,1));
			lbl_cases.setText("Cases list");
			lbl_list = new Label();
			lbl_list.setText("File list");
			lbl_list.setLocation(new java.awt.Point(73,1));
			lbl_list.setSize(new java.awt.Dimension(56,23));
			pnl_list = new Panel();
			pnl_list.setLayout(null);
			pnl_list.setBounds(new java.awt.Rectangle(7,55,435,156));
			pnl_list.add(getLst_search(), null);
			pnl_list.add(getLst_cases2exec(), null);
			pnl_list.add(getBtn_insert(), null);
			pnl_list.add(getButton(), null);
			pnl_list.add(lbl_list, null);
			pnl_list.add(lbl_cases, null);
		}
		return pnl_list;
	}

	/**
	 * This method initializes pnl_algorithms	
	 * 	
	 * @return java.awt.Panel	
	 */
	private Panel getPnl_algorithms()
	{
		if (pnl_algorithms == null)
		{
			label6 = new Label();
			label6.setBounds(new java.awt.Rectangle(136,52,78,21));
			label6.setText("Nro Iteracoes:");
			label5 = new Label();
			label5.setBounds(new java.awt.Rectangle(136,126,68,21));
			label5.setText("Nro Iteracoes:");
			label4 = new Label();
			label4.setBounds(new java.awt.Rectangle(136,102,66,21));
			label4.setText("Nro Iteracoes:");
			label3 = new Label();
			label3.setBounds(new java.awt.Rectangle(247,126,70,21));
			label3.setText("Vizinhança:");
			label2 = new Label();
			label2.setBounds(new java.awt.Rectangle(247,102,70,21));
			label2.setText("Temperatura:");
			label1 = new Label();
			label1.setBounds(new java.awt.Rectangle(247,52,70,21));
			label1.setText("Vizinhança:");
			label = new Label();
			label.setBounds(new java.awt.Rectangle(247,27,70,21));
			label.setText("Temperatura:");
			lbl_iteracoes = new Label();
			lbl_iteracoes.setBounds(new java.awt.Rectangle(136,27,78,21));
			lbl_iteracoes.setText("Nro Iteracoes:");
			pnl_algorithms = new Panel();
			pnl_algorithms.setLayout(null);
			pnl_algorithms.setLocation(new java.awt.Point(8,214));
			pnl_algorithms.setSize(new java.awt.Dimension(434,187));
			pnl_algorithms.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
			pnl_algorithms.add(getCkbox_Exaustivo(), null);
			pnl_algorithms.add(getCheckbox1(), null);
			pnl_algorithms.add(getCheckbox2(), null);
			pnl_algorithms.add(getCheckbox(), null);
			pnl_algorithms.add(getCheckbox3(), null);
			pnl_algorithms.add(getCheckbox4(), null);
			pnl_algorithms.add(getTxt_iteracoes_SA(), null);
			pnl_algorithms.add(lbl_iteracoes, null);
			pnl_algorithms.add(label, null);
			pnl_algorithms.add(getTxt_temperatura_SA(), null);
			pnl_algorithms.add(getBtn_default_SA(), null);
			pnl_algorithms.add(label1, null);
			pnl_algorithms.add(getTxt_vizinhanca_taboo(), null);
			pnl_algorithms.add(getButton2(), null);
			pnl_algorithms.add(label2, null);
			pnl_algorithms.add(label3, null);
			pnl_algorithms.add(getTxt_temperatura_heu_SA(), null);
			pnl_algorithms.add(getTxt_vizinhanca_heu_taboo(), null);
			pnl_algorithms.add(getBtn_default_Heu_SA(), null);
			pnl_algorithms.add(getButton22(), null);
			pnl_algorithms.add(label4, null);
			pnl_algorithms.add(getTxt_iteracoes_Heu_SA(), null);
			pnl_algorithms.add(label5, null);
			pnl_algorithms.add(getTxt_iteracoes_Heu_Taboo(), null);
			pnl_algorithms.add(getTxt_iteracoes_Taboo(), null);
			pnl_algorithms.add(label6, null);
			pnl_algorithms.add(getCheckbox5(), null);
		}
		return pnl_algorithms;
	}

	/**
	 * This method initializes btn_execute	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getBtn_execute()
	{
		if (btn_execute == null)
		{
			btn_execute = new Button();
			btn_execute.setBounds(new java.awt.Rectangle(162,402,106,23));
			btn_execute.setLabel("Execute cases ...");
			btn_execute.addActionListener(this);
		}
		return btn_execute;
	}

	/**
	 * This method initializes ckbox_Exaustivo	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	private Checkbox getCkbox_Exaustivo()
	{
		if (ckbox_Exaustive == null)
		{
			ckbox_Exaustive = new Checkbox();
			ckbox_Exaustive.setLabel("Exaustivo");
			ckbox_Exaustive.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
			ckbox_Exaustive.setBounds(new java.awt.Rectangle(5,2,94,23));
		}
		return ckbox_Exaustive;
	}

	/**
	 * This method initializes checkbox	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	private Checkbox getCheckbox()
	{
		if (ckbox_Heuristic == null)
		{
			ckbox_Heuristic = new Checkbox();
			ckbox_Heuristic.setLabel("Heuristic");
			ckbox_Heuristic.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
			ckbox_Heuristic.setName("ckbox_Heuristic");
			ckbox_Heuristic.setBounds(new java.awt.Rectangle(5,77,94,23));
		}
		return ckbox_Heuristic;
	}

	/**
	 * This method initializes checkbox1	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	private Checkbox getCheckbox1()
	{
		if (ckbox_SA == null)
		{
			ckbox_SA = new Checkbox();
			ckbox_SA.setLabel("Simulated Annealing");
			ckbox_SA.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
			ckbox_SA.setBounds(new java.awt.Rectangle(5,27,125,23));
			ckbox_SA.addItemListener(this);
		}
		return ckbox_SA;
	}

	/**
	 * This method initializes checkbox2	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	private Checkbox getCheckbox2()
	{
		if (ckbox_Taboo == null)
		{
			ckbox_Taboo = new Checkbox();
			ckbox_Taboo.setLabel("Taboo Search");
			ckbox_Taboo.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
			ckbox_Taboo.setBounds(new java.awt.Rectangle(5,52,94,23));
			ckbox_Taboo.addItemListener(this);
		}
		return ckbox_Taboo;
	}

	/**
	 * This method initializes checkbox3	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	private Checkbox getCheckbox3()
	{
		if (ckbox_Heu_SA == null)
		{
			ckbox_Heu_SA = new Checkbox();
			ckbox_Heu_SA.setLabel("Heuristic + SA");
			ckbox_Heu_SA.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
			ckbox_Heu_SA.setName("ckbox_Heu_SA");
			ckbox_Heu_SA.setBounds(new java.awt.Rectangle(5,102,94,23));
			ckbox_Heu_SA.addItemListener(this);
		}
		return ckbox_Heu_SA;
	}

	/**
	 * This method initializes checkbox4	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	private Checkbox getCheckbox4()
	{
		if (ckbox_Heu_Taboo == null)
		{
			ckbox_Heu_Taboo = new Checkbox();
			ckbox_Heu_Taboo.setBounds(new java.awt.Rectangle(5,127,119,23));
			ckbox_Heu_Taboo.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 10));
			ckbox_Heu_Taboo.setName("ckbox_Heu_Taboo");
			ckbox_Heu_Taboo.setLabel("Heuristic + Taboo");
			ckbox_Heu_Taboo.addItemListener(this);
		}
		return ckbox_Heu_Taboo;
	}
	
	public void actionPerformed(ActionEvent e)
	{
	  if(e.getSource().getClass()==Button.class)
	  {
	  	// ACAO PARA O BOTAO DE PROCURA DE DIRETORIO
	 		if(e.getSource().equals(btn_browse))
	 		{
	 			FileDialog dlg_files;
				dlg_files = new FileDialog(this, "Browse CWM Files", FileDialog.LOAD);
				dlg_files.setDirectory("."); 
				dlg_files.setFile("*.CWG");
				dlg_files.setVisible(true);
				
				if(dlg_files.getFile()==null)
					return;

				txt_filepath.setText(dlg_files.getDirectory());
				File filepath = new File(dlg_files.getDirectory());
				String filesname[] = filepath.list();
				
				for(int i=0;i<filesname.length; i++)					
				{					
					filepath=new File(filesname[i]);
					if(filesname[i].endsWith(".CWG")||filesname[i].endsWith(".cwg"))
					{
						if(!vct_listResult.contains(dlg_files.getDirectory()+filesname[i]))
						{
							lst_search.add(filesname[i]);
							vct_listResult.add(dlg_files.getDirectory()+filesname[i]);
						}
					}
				}
	 		}

	 		// ACAO PARA O BOTAO DE INSERCAO...
	 		if(e.getSource().equals(btn_insert))
	 		{
	 			if((lst_search.getItemCount()!=0) && (lst_search.getSelectedItems()!=null))
	 			{
	 				int indexes[]=lst_search.getSelectedIndexes();
	 				for(int i=0; i<indexes.length; i++)
	 				{
	 					lst_cases2exec.add(lst_search.getItem(indexes[i]));
	 					vct_casesSelected.add(vct_listResult.get(indexes[i]));
	 				}
 					
	 				int j=indexes.length-1;
	 				while(j>=0)
	 				{
	 					lst_search.remove(indexes[j]);
	 					vct_listResult.remove(indexes[j]);
	 					j--;
	 				}
	 			}
	 		}
	 		
	 		// ACAO PARA O BOTAO DELETE
	 		if(e.getSource().equals(btn_delete))
	 		{	 			
	 			if(lst_cases2exec.getItemCount()!=0)
	 			{
	 				int indexes[]=lst_cases2exec.getSelectedIndexes();
	 				for(int i=0; i<indexes.length; i++)
	 				{	 										
	 					lst_search.add(lst_cases2exec.getItem(indexes[i]));
						vct_listResult.add(vct_casesSelected.get(indexes[i]));
	 				}
	 				
	 				int j=indexes.length-1;
	 				while(j>=0)
	 				{
						lst_cases2exec.remove(indexes[j]);
 						vct_casesSelected.remove(indexes[j]);
	 					j--;
	 				}
	 			}
	 		}
	 		
	 		if(e.getSource().equals(btn_execute))
	 		{
	 			if(lst_cases2exec.getItemCount()==0)
	 			{
	 				JOptionPane.showMessageDialog(null, "There is no CWG to compute.", "Error", JOptionPane.ERROR_MESSAGE);
	 				return;
	 			}

	 			FileDialog dlg_files;
				dlg_files = new FileDialog(this, "Save result file at ...", FileDialog.SAVE);
				dlg_files.setDirectory("."); 
				dlg_files.setVisible(true);
				
				if(dlg_files.getFile()==null)
				{
	 				JOptionPane.showMessageDialog(null, "Define a log file.", "Error", JOptionPane.ERROR_MESSAGE);
	 				return;
				}
				
				executeCases(dlg_files.getDirectory()+dlg_files.getFile());
				
				JOptionPane.showMessageDialog(null, "All cases executed.", "Finished", JOptionPane.INFORMATION_MESSAGE);				
				
	 		}
	 		
	 		if(e.getSource().equals(btn_default_SA))
	 		{
	 			if(ckbox_SA.getState())
	 			{
	 				txt_iteracoes_SA.setText("100");
	 				txt_temperatura_SA.setText("1000");
	 			}
	 		}

	 		if(e.getSource().equals(btn_default_Taboo))
	 		{
	 			if(ckbox_Taboo.getState())
	 			{
	 				txt_iteracoes_Taboo.setText("100");
	 				txt_vizinhanca_taboo.setText("1000");
	 			}
	 		}

	 		if(e.getSource().equals(btn_default_Heu_SA))
	 		{
	 			if(ckbox_Heu_SA.getState())
	 			{
	 				txt_temperatura_heu_SA.setText("1000");
	 			}
	 		}

	 		if(e.getSource().equals(btn_default_Heu_Taboo))
	 		{
	 			if(ckbox_Heu_Taboo.getState())
	 			{
	 				txt_iteracoes_Heu_Taboo.setText("100");
	 				txt_vizinhanca_heu_taboo.setText("1000");
	 			}
	 		}
	  }
	}
	
  public void itemStateChanged(ItemEvent e) {
	  if(e.getSource().getClass()==Checkbox.class)
	  {
	  	// ACAO PARA O BOTAO DE PROCURA DE DIRETORIO
	 		if(e.getSource().equals(ckbox_SA))
	 		{
	 			txt_iteracoes_SA.setEditable(ckbox_SA.getState());
	 			txt_iteracoes_SA.setEnabled(ckbox_SA.getState());
	 			txt_temperatura_SA.setEditable(ckbox_SA.getState());
	 			txt_temperatura_SA.setEnabled(ckbox_SA.getState());
	 		}
	 		
	 		if(e.getSource().equals(ckbox_Taboo))
	 		{
	 			txt_iteracoes_Taboo.setEditable(ckbox_Taboo.getState());
	 			txt_iteracoes_Taboo.setEnabled(ckbox_Taboo.getState());
	 			txt_vizinhanca_taboo.setEditable(ckbox_Taboo.getState());
	 			txt_vizinhanca_taboo.setEnabled(ckbox_Taboo.getState());
	 		}

	 		if(e.getSource().equals(ckbox_Heu_SA))
	 		{
		 		txt_temperatura_heu_SA.setEditable(ckbox_Heu_SA.getState());
	 			txt_temperatura_heu_SA.setEnabled(ckbox_Heu_SA.getState());
	 		}

	 		if(e.getSource().equals(ckbox_Heu_Taboo))
	 		{
	 			txt_iteracoes_Heu_Taboo.setEditable(ckbox_Heu_Taboo.getState());
	 			txt_iteracoes_Heu_Taboo.setEnabled(ckbox_Heu_Taboo.getState());
	 			txt_vizinhanca_heu_taboo.setEditable(ckbox_Heu_Taboo.getState());
	 			txt_vizinhanca_heu_taboo.setEnabled(ckbox_Heu_Taboo.getState());
	 		}
	  }
  }

	/**
	 * This method initializes txt_iteracoes	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTxt_iteracoes_SA()
	{
		if (txt_iteracoes_SA == null)
		{
			txt_iteracoes_SA = new TextField();
			txt_iteracoes_SA.setLocation(new java.awt.Point(205,27));
			txt_iteracoes_SA.setEditable(false);
			txt_iteracoes_SA.setEnabled(false);
			txt_iteracoes_SA.setText("100");
			txt_iteracoes_SA.setSize(new java.awt.Dimension(42,21));
		}
		return txt_iteracoes_SA;
	}

	/**
	 * This method initializes textField	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTxt_temperatura_SA()
	{
		if (txt_temperatura_SA == null)
		{
			txt_temperatura_SA = new TextField();
			txt_temperatura_SA.setLocation(new java.awt.Point(317,27));
			txt_temperatura_SA.setEditable(false);
			txt_temperatura_SA.setEnabled(false);
			txt_temperatura_SA.setText("1000");
			txt_temperatura_SA.setSize(new java.awt.Dimension(42,21));
		}
		return txt_temperatura_SA;
	}

	/**
	 * This method initializes btn_default_SA	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getBtn_default_SA()
	{
		if (btn_default_SA == null)
		{
			btn_default_SA = new Button();
			btn_default_SA.setBounds(new java.awt.Rectangle(359,27,48,21));
			btn_default_SA.setLabel("Default");
			btn_default_SA.addActionListener(this);
		}
		return btn_default_SA;
	}

	/**
	 * This method initializes textField1	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTxt_vizinhanca_taboo()
	{
		if (txt_vizinhanca_taboo == null)
		{
			txt_vizinhanca_taboo = new TextField();
			txt_vizinhanca_taboo.setLocation(new java.awt.Point(317,52));
			txt_vizinhanca_taboo.setEnabled(false);
			txt_vizinhanca_taboo.setEditable(false);
			txt_vizinhanca_taboo.setText("1000");
			txt_vizinhanca_taboo.setSize(new java.awt.Dimension(42,21));
		}
		return txt_vizinhanca_taboo;
	}

	/**
	 * This method initializes button	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getButton2()
	{
		if (btn_default_Taboo == null)
		{
			btn_default_Taboo = new Button();
			btn_default_Taboo.setBounds(new java.awt.Rectangle(359,52,48,21));
			btn_default_Taboo.setLabel("Default");
			btn_default_Taboo.addActionListener(this);
		}
		return btn_default_Taboo;
	}

	/**
	 * This method initializes textField2	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTxt_temperatura_heu_SA()
	{
		if (txt_temperatura_heu_SA == null)
		{
			txt_temperatura_heu_SA = new TextField();
			txt_temperatura_heu_SA.setLocation(new java.awt.Point(317,102));
			txt_temperatura_heu_SA.setEditable(false);
			txt_temperatura_heu_SA.setEnabled(false);
			txt_temperatura_heu_SA.setText("1000");
			txt_temperatura_heu_SA.setSize(new java.awt.Dimension(42,21));
		}
		return txt_temperatura_heu_SA;
	}

	/**
	 * This method initializes textField3	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTxt_vizinhanca_heu_taboo()
	{
		if (txt_vizinhanca_heu_taboo == null)
		{
			txt_vizinhanca_heu_taboo = new TextField();
			txt_vizinhanca_heu_taboo.setLocation(new java.awt.Point(317,126));
			txt_vizinhanca_heu_taboo.setEditable(false);
			txt_vizinhanca_heu_taboo.setEnabled(false);
			txt_vizinhanca_heu_taboo.setText("1000");
			txt_vizinhanca_heu_taboo.setSize(new java.awt.Dimension(42,21));
		}
		return txt_vizinhanca_heu_taboo;
	}

	/**
	 * This method initializes button1	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getBtn_default_Heu_SA()
	{
		if (btn_default_Heu_SA == null)
		{
			btn_default_Heu_SA = new Button();
			btn_default_Heu_SA.setBounds(new java.awt.Rectangle(359,102,48,21));
			btn_default_Heu_SA.setLabel("Default");
			btn_default_Heu_SA.addActionListener(this);
		}
		return btn_default_Heu_SA;
	}

	/**
	 * This method initializes button2	
	 * 	
	 * @return java.awt.Button	
	 */
	private Button getButton22()
	{
		if (btn_default_Heu_Taboo == null)
		{
			btn_default_Heu_Taboo = new Button();
			btn_default_Heu_Taboo.setBounds(new java.awt.Rectangle(359,126,48,21));
			btn_default_Heu_Taboo.setLabel("Default");
			btn_default_Heu_Taboo.addActionListener(this);
		}
		return btn_default_Heu_Taboo;
	}

	/**
	 * This method initializes textField	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTxt_iteracoes_Heu_SA()
	{
		if (txt_iteracoes_Heu_SA == null)
		{
			txt_iteracoes_Heu_SA = new TextField();
			txt_iteracoes_Heu_SA.setEditable(false);
			txt_iteracoes_Heu_SA.setText("1");
			txt_iteracoes_Heu_SA.setLocation(new java.awt.Point(205,102));
			txt_iteracoes_Heu_SA.setSize(new java.awt.Dimension(42,21));
			txt_iteracoes_Heu_SA.setEnabled(false);
		}
		return txt_iteracoes_Heu_SA;
	}
	
	private void executeCases(String _logfile)
	{
		mc.setDisplayGraph(false);
		
		try
		{
			FileOutputStream fos=new FileOutputStream(_logfile,false);
			fos.write(("Caso de teste; Algoritmo; posicoes na noc; nro de cores; energia; tempo de computação\n").getBytes());
			fos.close();
		}
		catch(Exception e) 
		{
			System.out.println("Problems during " + _logfile + " generation.");
			e.printStackTrace();
		}

		for(int i=0; i<vct_casesSelected.size(); i++)
		{
			String casefile=vct_casesSelected.get(i);
			int lengthdir=vct_casesSelected.get(i).length()-lst_cases2exec.getItem(i).length();			
			String directory=vct_casesSelected.get(i).substring(0, lengthdir);
			CWM_GrafoFormatoTextual.leArquivoSemDialog(mc.getWindowPrincipal(), mc.getSequencia(), directory, lst_cases2exec.getItem(i));

			
			if(ckbox_Exaustive.getState())
			{
				execAlgorithm(_logfile, Algoritmo.ExhaustiveSearch, 0, 0, casefile);
			}
			if(ckbox_SA.getState())
			{
				int iteracoes=(txt_iteracoes_SA.getText().trim().length()!=0)?Integer.parseInt(txt_iteracoes_SA.getText().trim()):-1;
				int temperatura=(txt_temperatura_SA.getText().trim().length()!=0)?Integer.parseInt(txt_temperatura_SA.getText().trim()):-1;
				
				execAlgorithm(_logfile, Algoritmo.SimulatedAnnealing, iteracoes, temperatura, casefile);
			}
			if(ckbox_Taboo.getState())
			{
				int iteracoes=(txt_iteracoes_Taboo.getText().trim().length()!=0)?Integer.parseInt(txt_iteracoes_Taboo.getText().trim()):-1;
				int vizinhanca=(txt_vizinhanca_taboo.getText().trim().length()!=0)?Integer.parseInt(txt_vizinhanca_taboo.getText().trim()):-1;
				
				execAlgorithm(_logfile, Algoritmo.TabooSearch, iteracoes, vizinhanca, casefile);
			}
			if(ckbox_Heuristic.getState())
			{
				execAlgorithm(_logfile, Algoritmo.HeuristicSearch, 0, 0, casefile);
			}
			if(ckbox_Heu_SA.getState())
			{
				int iteracoes=(txt_iteracoes_Heu_SA.getText().trim().length()!=0)?Integer.parseInt(txt_iteracoes_Heu_SA.getText().trim()):-1;
				int temperatura=(txt_temperatura_heu_SA.getText().trim().length()!=0)?Integer.parseInt(txt_temperatura_heu_SA.getText().trim()):-1;
				
				execAlgorithm(_logfile, Algoritmo.HeuristicSearch_SA, iteracoes, temperatura, casefile);
			}
			if(ckbox_Heu_Taboo.getState())
			{
				int iteracoes=(txt_iteracoes_Heu_Taboo.getText().trim().length()!=0)?Integer.parseInt(txt_iteracoes_Heu_Taboo.getText().trim()):-1;
				int vizinhanca=(txt_vizinhanca_heu_taboo.getText().trim().length()!=0)?Integer.parseInt(txt_vizinhanca_heu_taboo.getText().trim()):-1;
				
				execAlgorithm(_logfile, Algoritmo.HeuristicSearch_Taboo, iteracoes, vizinhanca, casefile);
			}
			if(ckbox_Bad_Mapping.getState())
			{
				int iteracoes=100;
				int Temperatura=1000;
				
				execAlgorithm(_logfile, Algoritmo.BadMappingSearch, iteracoes, Temperatura, casefile);
			}
		}
		mc.setDisplayGraph(true);
	}
	
	private void execAlgorithm(String _logfile, int _algorithm, int _iteracoes, int _temperatura_ou_vizinha, String casefile)
	{
		CWM_2NoC cn=new CWM_2NoC(mc.getSequencia(), mc.getWindowPrincipal(), _algorithm, false, false, _iteracoes, _temperatura_ou_vizinha);
		String texto=casefile+";";
		
		switch (_algorithm)
		{
			case Algoritmo.ExhaustiveSearch:
//			texto+="Exaustivo";
			texto+="EX;";
				break;
			case Algoritmo.SimulatedAnnealing:
				texto+="SA;";
//				texto+="Simulated Annealing";
//				texto+="\nParametros:\n Iteracoes: "+cn.iteracoes+", Temperatura: "+cn.temperatura;
				break;
			case Algoritmo.TabooSearch:
			texto+="TS;";
//			texto+="Taboo Search";
//				texto+="\nParametros:\n Iteracoes: "+cn.iteracoes+", Vizinhança: "+cn.temperatura;
				break;
			case Algoritmo.HeuristicSearch:
				texto+="HE;";
//				texto+="Heuristic";
				break;
			case Algoritmo.HeuristicSearch_SA:
			texto+="HS;";
//			texto+="Heuristic com Simulated Annealing";
//				texto+="\nParametros:\n Iteracoes: "+cn.iteracoes+", Temperatura: "+cn.temperatura;
				break;
			case Algoritmo.HeuristicSearch_Taboo:
				texto+="HT;";
//				texto+="Heuristic com Taboo Search";
//					texto+="\nParametros:\n Iteracoes: "+cn.iteracoes+", Vizinhança: "+cn.temperatura;
					break;
			case Algoritmo.BadMappingSearch:
				texto+="BM;";
//				texto+="Heuristic com Taboo Search";
//					texto+="\nParametros:\n Iteracoes: "+cn.iteracoes+", Vizinhança: "+cn.temperatura;
					break;
		}
		
		try
		{			
			FileOutputStream fileOut= new FileOutputStream(_logfile, true);
			texto+=mc.getWindowPrincipal().getNumColunas()*mc.getWindowPrincipal().getNumLinhas()+";";
			texto+=mc.getSequencia().numCores()+";";
			texto+=cn.energyConsumed+";";
			texto+=cn.computationTimems+"\n";
//			texto+="Energia consumida:\n "+cn.energyConsumed+"\n";
//			texto+="Tempo de computação:\n "+cn.computationTimems+" ms\n";
//			texto+="Distribuição dos IPs pela NoC:\n"+cn.distribution+"\n";
			fileOut.write(texto.getBytes());			
			fileOut.close();
		}
		catch(Exception e) 
		{
			System.out.println("Problems during " + _logfile + " generation.");
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes textField	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTxt_iteracoes_Heu_Taboo()
	{
		if (txt_iteracoes_Heu_Taboo == null)
		{
			txt_iteracoes_Heu_Taboo = new TextField();
			txt_iteracoes_Heu_Taboo.setEditable(false);
			txt_iteracoes_Heu_Taboo.setText("100");
			txt_iteracoes_Heu_Taboo.setLocation(new java.awt.Point(205,126));
			txt_iteracoes_Heu_Taboo.setSize(new java.awt.Dimension(42,21));
			txt_iteracoes_Heu_Taboo.setEnabled(false);
		}
		return txt_iteracoes_Heu_Taboo;
	}

	/**
	 * This method initializes textField1	
	 * 	
	 * @return java.awt.TextField	
	 */
	private TextField getTxt_iteracoes_Taboo()
	{
		if (txt_iteracoes_Taboo == null)
		{
			txt_iteracoes_Taboo = new TextField();
			txt_iteracoes_Taboo.setBounds(new java.awt.Rectangle(205,52,42,21));
			txt_iteracoes_Taboo.setEditable(false);
			txt_iteracoes_Taboo.setText("100");
			txt_iteracoes_Taboo.setEnabled(false);
		}
		return txt_iteracoes_Taboo;
	}

	/**
	 * This method initializes checkbox	
	 * 	
	 * @return java.awt.Checkbox	
	 */
	private Checkbox getCheckbox5()
	{
		if (ckbox_Bad_Mapping == null)
		{
			ckbox_Bad_Mapping = new Checkbox();
			ckbox_Bad_Mapping.setBounds(new java.awt.Rectangle(5,156,102,21));
			ckbox_Bad_Mapping.setName("ckbox_Heu_Taboo");
			ckbox_Bad_Mapping.setLabel("Bad Mapping");
			ckbox_Bad_Mapping.setFont(new Font("Dialog", Font.PLAIN, 10));
		}
		return ckbox_Bad_Mapping;
	}
	
}  //  @jve:decl-index=0:visual-constraint="29,10"
