package cafes.NoC;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class NoCParameters implements Serializable
{
	private static final long serialVersionUID = -6828270192440080806L;
	private TextField numeroLinhas, numeroColunas,numeroAltura;
	private TextField tileWidth, tileHeigth,tileLongitudinal;
	private TextField bufferSize, clockCycle;
	private TextField ElPhit, EcPhit, EsPhit, EbPhit, EtPhit,PRouter;
	private TextField EsS_Phit, EbS_Phit, ElS_Phit, EcS_Phit , EtS_Phit;
	private TextField linkingCycles, routingCycles;
	private String strMeshTopology =  "Mesh (XY routing, wormhole switching)";
	private String strTorusTopology = "Torus (XY routing, wormhole switching)";
	private String topologia;

	public NoCParameters()
	{
		numeroLinhas = new TextField(); 
		numeroColunas = new TextField();
		numeroAltura = new TextField();
		tileWidth = new TextField(); 
		tileHeigth = new TextField();
		tileLongitudinal = new TextField();
		bufferSize = new TextField();
		clockCycle = new TextField();
		ElPhit = new TextField();
		EcPhit = new TextField();
		EsPhit = new TextField();
		EbPhit = new TextField();
		EtPhit = new TextField();
		ElS_Phit = new TextField();
		EcS_Phit = new TextField();
		EtS_Phit = new TextField();
		EsS_Phit = new TextField();
		EbS_Phit = new TextField();
		PRouter = new TextField();
		linkingCycles = new TextField();
		routingCycles = new TextField();
		setParametrosDeEnergiaDefault();
	}
	public void setParametrosDeEnergiaDefault()
	{
		numeroLinhas.setText("2"); 
		numeroColunas.setText("3");
		numeroAltura.setText("2");
		tileWidth.setText("1"); 
		tileHeigth.setText("1");
		tileLongitudinal.setText("1");
		bufferSize.setText("4");
		clockCycle.setText("100");
		ElPhit.setText("0.1");       // 0.07
		EcPhit.setText("0.05");       // 0.05
		EsPhit.setText("0.5");       // 0.67
		EbPhit.setText("1.5");       // 0.207
		EtPhit.setText("0.1");
		ElS_Phit.setText("0.0");
		EcS_Phit.setText("0.0");
		EtS_Phit.setText("0.0");
		EsS_Phit.setText("0.0");
		EbS_Phit.setText("0.0");
		PRouter.setText("10.5");
		linkingCycles.setText("1");
		routingCycles.setText("3");
	}
	private int getInteger(TextField tf, String msg1, String msg2, int tipo)
	{
		try
		{
			return Integer.parseInt(tf.getText());
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, msg1, msg2, tipo);
		}
		return -1;
	}
	private double getDouble(TextField tf, String msg1, String msg2, int tipo)
	{
		try
		{
			return Double.valueOf(tf.getText()).doubleValue();
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, msg1, msg2, tipo);
		}
		return -1;
	}
	public int getNumColunas()
	{
		return getInteger(numeroColunas, "'Column field is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public int getNumLinhas()
	{
		return getInteger(numeroLinhas, "'Line field is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public int getTileLongitudinal(){
		return getInteger(tileLongitudinal, "'Line field is empty.", "Warning", JOptionPane.WARNING_MESSAGE);		
	}
	public int getNumAltura()
	{
		return getInteger(numeroAltura, "'Line field is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public int getTileWidth()
	{
		return getInteger(tileWidth, "'Tile width field is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public int getTileHeigth()
	{
		return getInteger(tileHeigth, "'Tile heigth field is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public int getBufferSize()
	{
		return getInteger(bufferSize, "Fills the field of buffer size", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getClockCycle()
	{
		return getDouble(clockCycle, "Fills the field of routing time", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaLinkPhit()
	{
		return getDouble(ElPhit, "Fills the field of link dynamic energy by phit (ElPhit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaLocalLinkPhit()
	{
		return getDouble(EcPhit, "Fills the field of local link dynamic energy by phit (EcPhit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaControlePhit()
	{
		return getDouble(EsPhit, "Fills the field of switching dynamic energy by phit(EsPhit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaBufferPhit()
	{
		return getDouble(EbPhit, "Fills the field of buffer dynamic energy by phit(EbPhit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaLinkLongitudinalPhit()
	{
		return getDouble(EtPhit, "Fills the field of buffer dynamic energy by phit(EtPhit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaLinkComChaveamentoPhit()
	{
		return getDouble(ElS_Phit, "Fills the field of link dynamic energy by phit(ElS_Phit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaLocalLinkComChaveamentoPhit()
	{
		return getDouble(EcS_Phit, "Fills the field of local link dynamic energy by phit(EcS_Phit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaLocalLinkComChaveamentoLongitudinalPhit()
	{
		return getDouble(EtS_Phit, "Fills the field of local link dynamic energy by phit(EtS_Phit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaControleComChaveamentoPhit()
	{
		return getDouble(EsS_Phit, "Fills the field of switching dynamic energy by phit(EsS_Phit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getEnergiaBufferComChaveamentoPhit()
	{
		return getDouble(EbS_Phit, "Fills the field of buffer dynamic energy by phit(EbS_Phit)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public double getPotRoteador()
	{
		return getDouble(PRouter, "Fills the field of static power (PsRouter)", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public int getLinkingCycles()
	{
		return getInteger(linkingCycles, "Fills the field of linking cycles", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	public int getRoutingCycles()
	{
		return getInteger(routingCycles, "Fills the field of routingcycles", "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void insereTopologia(JComboBox topo, int xi, int yi, int xf, int yf)
	{
		topo.setBounds(xi, yi, xf, yf);
		topo.setToolTipText("Select the topology");
		topo.addItem(strMeshTopology);
		topo.addItem(strTorusTopology);
		setDefaultTopology();
	}
	public void setDefaultTopology()
	{
		setTopologia(strMeshTopology);
	}
	public void setTopologia(String topologia)
	{
		this.topologia = topologia;
	}
	public boolean ehTopologiaMesh()
	{
		return topologia.equals(strMeshTopology);
	}
	public boolean ehTopologiaTorus()
	{
		return topologia.equals(strTorusTopology);
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public String getNumColunasStr()
	{
		return numeroColunas.getText();
	}
	public String getNumAlturaStr()
	{
		return numeroAltura.getText();
	}
	public String getNumLinhasStr()
	{
		return numeroLinhas.getText();
	}
	public String getTileWidthStr()
	{
		return tileWidth.getText();
	}
	public String getTileHeigthStr()
	{
		return tileHeigth.getText();
	}
	public String getTileLongitudinalStr()
	{
		return tileLongitudinal.getText();
	}
	public String getBufferSizeStr()
	{
		return bufferSize.getText();
	}
	public String getClockCycleStr()
	{
		return clockCycle.getText();
	}
	public String getEnergiaLinkPhitStr()
	{
		return ElPhit.getText();
	}
	public String getEnergiaLocalLinkPhitStr()
	{
		return EcPhit.getText();
	}
	public String getEnergiaControlePhitStr()
	{
		return EsPhit.getText();
	}
	public String getEnergiaBufferPhitStr()
	{
		return EbPhit.getText();
	}
	public String getEnergiaBufferLongitudinalPhitStr()
	{
		return EtPhit.getText();
	}
	public String getEnergiaLinkComChaveamentoPhitStr()
	{
		return ElS_Phit.getText();
	}
	public String getEnergiaLocalLinkComChaveamentoPhitStr()
	{
		return EcS_Phit.getText();
	}
	public String getEnergiaLocalLinkComChaveamentoPhitLongitudinalStr()
	{
		return EtS_Phit.getText();
	}
	public String getEnergiaControleComChaveamentoPhitStr()
	{
		return EsS_Phit.getText();
	}
	public String getEnergiaBufferComChaveamentoPhitStr()
	{
		return EbS_Phit.getText();
	}
	public String getPotRoteadorStr()
	{
		return PRouter.getText();
	}
	public String getLinkingCyclesStr()
	{
		return linkingCycles.getText();
	}
	public String getRoutingCyclesStr()
	{
		return routingCycles.getText();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public TextField getTileWidthTextField()
	{
		return tileWidth;
	}

	public TextField getTileHeigthTextField()
	{
		return tileHeigth;
	}
	public TextField getTileLongitudinalTextField()
	{
		return tileLongitudinal;
	}
	public TextField getNumColunasTextField()
	{
		return numeroColunas;
	}
	public TextField getNumAlturaTextField()
	{
		return numeroAltura;
	}
	public TextField getNumLinhasTextField()
	{
		return numeroLinhas;
	}
	public TextField getBufferSizeTextField()
	{
		return bufferSize;
	}
	public TextField getClockCycleTextField()
	{
		return clockCycle;
	}
	public TextField getEnergiaLinkPhitTextField()
	{
		return ElPhit;
	}
	public TextField getEnergiaLocalLinkPhitTextField()
	{
		return EcPhit;
	}
	public TextField getEnergiaControlePhitTextField()
	{
		return EsPhit;
	}
	public TextField getEnergiaBufferPhitTextField()
	{
		return EbPhit;
	}
	public TextField getEnergiaBufferLongitudinalPhitTextField()
	{
		return EtPhit;
	}
	public TextField getEnergiaLinkComChaveamentoPhitTextField()
	{
		return ElS_Phit;
	}
	public TextField getEnergiaLocalLinkComChaveamentoPhitTextField()
	{
		return EcS_Phit;
	}
	public TextField getEnergiaLocalLinkComChaveamentoPhitLongitudinalTextField()
	{
		return EtS_Phit;
	}
	public TextField getEnergiaControleComChaveamentoPhitTextField()
	{
		return EsS_Phit;
	}
	public TextField getEnergiaBufferComChaveamentoPhitTextField()
	{
		return EbS_Phit;
	}
	public TextField getPotRoteadorTextField()
	{
		return PRouter;
	}
	public TextField getLinkingCyclesTextField()
	{
		return linkingCycles;
	}
	public TextField getRoutingCyclesTextField()
	{
		return routingCycles;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void setNumColunas(String str)
	{
		numeroColunas.setText(str);
	}
	public void setNumAltura(String str)
	{
		numeroAltura.setText(str);
	}
	public void setNumLinhas(String str)
	{
		numeroLinhas.setText(str);
	}
	public void setTileWidth(String str)
	{
		tileWidth.setText(str);
	}
	public void setTileHeigth(String str)
	{
		tileHeigth.setText(str);
	}
	public void setTileLongitudinal(String str)
	{
		tileLongitudinal.setText(str);
	}
	public void setBufferSize(String str)
	{
		bufferSize.setText(str);
	}
	public void setClockCycle(String str)
	{
		clockCycle.setText(str);
	}
	public void setEnergiaLinkPhit(String str)
	{
		ElPhit.setText(str);
	}
	public void setEnergiaLocalLinkPhit(String str)
	{
		EcPhit.setText(str);
	}
	public void setEnergiaControlePhit(String str)
	{
		EsPhit.setText(str);
	}
	public void setEnergiaBufferPhit(String str)
	{
		EbPhit.setText(str);
	}
	public void setEnergiaBufferLongitudinalPhit(String str)
	{
		EtPhit.setText(str);
	}
	public void setEnergiaLinkComChaveamentoPhit(String str)
	{
		ElS_Phit.setText(str);
	}
	public void setEnergiaLocalLinkComChaveamentoPhit(String str)
	{
		EcS_Phit.setText(str);
	}
	public void setEnergiaLocalLinkComChaveamentoPhitLongitudinal(String str)
	{
		EtS_Phit.setText(str);
	}
	public void setEnergiaControleComChaveamentoPhit(String str)
	{
		EsS_Phit.setText(str);
	}
	public void setEnergiaBufferComChaveamentoPhit(String str)
	{
		EbS_Phit.setText(str);
	}
	public void setPotRoteador(String str)
	{
		PRouter.setText(str);
	}
	public void setLinkingCycles(String str)
	{
		linkingCycles.setText(str);
	}
	public void setRoutingCycles(String str)
	{
		routingCycles.setText(str);
	}
}
