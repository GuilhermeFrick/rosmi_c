package cafes.model.CDCM.app;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

import cafes.common.Randomico;

public class SyntheticApplicationGeneratorGui extends JDialog implements ActionListener
{
	private static final long serialVersionUID = -3857661354505778831L;
	private static final int heightLabel = 20;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private class Gauss
	{
		private JTextField mean;
		private JTextField standardDeviation;
		private JTextField minimum;
		private JTextField maximum;

		public Gauss(int x, int y, int width, String title)
		{
			mean = createLabelAndTextField(x + 10, y + 1 * heightLabel, "Mean: ", 5);
			standardDeviation = createLabelAndTextField(x + 10, y + 2 * heightLabel, "Standard Deviation: ", 5);
			minimum = createLabelAndTextField(x + 10, y + 3 * heightLabel, "Minimum: ", 5);
			maximum = createLabelAndTextField(x + 10, y + 4 * heightLabel, "Maximum: ", 5);
			createParametersPanel(x, y, width, 5 * heightLabel + heightLabel / 2, title);
		}
		public double getMean()
		{
			return Double.valueOf(mean.getText());
		}
		public double getStandardDeviation()
		{
			return Double.valueOf(standardDeviation.getText());
		}
		public double getMinimum()
		{
			return Double.valueOf(minimum.getText());
		}
		public double getMaximum()
		{
			return Double.valueOf(maximum.getText());
		}
		public void setMean(double mean)
		{
			this.mean.setText(new Double(mean).toString());
		}
		public void setStandardDeviation(double standardDeviation)
		{
			this.standardDeviation.setText(new Double(standardDeviation).toString());
		}
		public void setMinimum(double minimum)
		{
			this.minimum.setText(new Double(minimum).toString());
		}
		public void setMaximum(double maximum)
		{
			this.maximum.setText(new Double(maximum).toString());
		}
	}
////////	
	public void setParallelCommunicationMean(double mean)
	{
		parallelCommunications.setMean(mean);
	}
	public double getParallelCommunicationMean()
	{
		return parallelCommunications.getMean();
	}
	public void setParallelCommunicationStandardDeviation(double standardDeviation)
	{
		parallelCommunications.setStandardDeviation(standardDeviation);
	}
	public double getParallelCommunicationStandardDeviation()
	{
		return parallelCommunications.getStandardDeviation();
	}
	public void setParallelCommunicationMinimum(double minimum)
	{
		parallelCommunications.setMinimum(minimum);
	}
	public double getParallelCommunicationMinimum()
	{
		return parallelCommunications.getMinimum();
	}
	public void setParallelCommunicationMaximum(double maximum)
	{
		parallelCommunications.setMaximum(maximum);
	}
	public double getParallelCommunicationMaximum()
	{
		return parallelCommunications.getMaximum();
	}

////////
	public void setComputationTimeMean(double mean)
	{
		computationTime.setMean(mean);
	}
	public double getComputationTimeMean()
	{
		return computationTime.getMean();
	}
	public void setComputationTimeStandardDeviation(double standardDeviation)
	{
		computationTime.setStandardDeviation(standardDeviation);
	}
	public double getComputationTimeStandardDeviation()
	{
		return computationTime.getStandardDeviation();
	}
	public void setComputationTimeMinimum(double minimum)
	{
		computationTime.setMinimum(minimum);
	}
	public double getComputationTimeMinimum()
	{
		return computationTime.getMinimum();
	}
	public void setComputationTimeMaximum(double maximum)
	{
		computationTime.setMaximum(maximum);
	}
	public double getComputationTimeMaximum()
	{
		return computationTime.getMaximum();
	}

////////
	public void setCommunicationVolumeMean(double mean)
	{
		communicationVolume.setMean(mean);
	}
	public double getCommunicationVolumeMean()
	{
		return communicationVolume.getMean();
	}
	public void setCommunicationVolumeStandardDeviation(double standardDeviation)
	{
		communicationVolume.setStandardDeviation(standardDeviation);
	}
	public double getCommunicationVolumeStandardDeviation()
	{
		return communicationVolume.getStandardDeviation();
	}
	public void setCommunicationVolumeMinimum(double minimum)
	{
		communicationVolume.setMinimum(minimum);
	}
	public double getCommunicationVolumeMinimum()
	{
		return communicationVolume.getMinimum();
	}
	public void setCommunicationVolumeMaximum(double maximum)
	{
		communicationVolume.setMaximum(maximum);
	}
	public double getCommunicationVolumeMaximum()
	{
		return communicationVolume.getMaximum();
	}
////////	
	private Gauss parallelCommunications;
	private Gauss computationTime;
	private Gauss communicationVolume;
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private class GeneralParameters
	{
		private JTextField numberOfProcessors;
		private JTextField numberOfGraphLevels;
		private JTextField dependeceDegree;

		public GeneralParameters(int x, int y, int width, String str)
		{
			numberOfProcessors = createLabelAndTextField(x + 10, y + 1 * heightLabel, "Number of processors [1, ...): ", 4);
			numberOfGraphLevels = createLabelAndTextField(x + 10, y + 2 * heightLabel, "Number of graph levels [1, ...): ", 3);
			dependeceDegree = createLabelAndTextField(x + 10, y + 3 * heightLabel, "Dependence degree [0%, 100%]: ", 6);
			createParametersPanel(x, y, width, 4 * heightLabel + heightLabel / 2, str);
		}
		public int getNumberOfProcessors()
		{
			return Integer.valueOf(numberOfProcessors.getText());
		}
		public void setNumberOfProcessors(int numberOfProcessors)
		{
			this.numberOfProcessors.setText(new Integer(numberOfProcessors).toString());
		}
		public double getDependeceDegree()
		{
			return Double.valueOf(dependeceDegree.getText());
		}
		public void setDependeceDegree(double dependeceDegree)
		{
			this.dependeceDegree.setText(new Double(dependeceDegree).toString());
		}
		public int getNumberOfGraphLevels()
		{
			return Integer.valueOf(numberOfGraphLevels.getText());
		}
		public void setNumberOfGraphLevels(int numberOfGraphLevels)
		{
			this.numberOfGraphLevels.setText(new Integer(numberOfGraphLevels).toString());
		}
	}
////////
	public int getNumberOfProcessors()
	{
		return generalParameters.getNumberOfProcessors();
	}
	public void setNumberOfProcessors(int numberOfProcessors)
	{
		generalParameters.setNumberOfProcessors(numberOfProcessors);
	}
	public int getNumberOfGraphLevels()
	{
		return generalParameters.getNumberOfGraphLevels();
	}
	public void setNumberOfGraphLevels(int numberOfGraphLevels)
	{
		generalParameters.setNumberOfGraphLevels(numberOfGraphLevels);
	}
	public double getDependeceDegree()
	{
		return generalParameters.getDependeceDegree();
	}
	public void setDependeceDegree(double dependeceDegree)
	{
		generalParameters.setDependeceDegree(dependeceDegree);
	}
////////
	private GeneralParameters generalParameters;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private class ProbabilityOfEndVertexMeeting
	{
		private JTextField minimum;
		private JTextField maximum;

		public ProbabilityOfEndVertexMeeting(int x, int y, int width, String str)
		{
			minimum = createLabelAndTextField(x + 10, y + 1 * heightLabel, "Minimum [0%, 100%]:", 6);
			maximum = createLabelAndTextField(x + 10, y + 2 * heightLabel, "Maximum [0%, 100%]:", 6);
			createParametersPanel(x, y, width, 3 * heightLabel + heightLabel / 2, str);
		}
		public double getMinimum()
		{
			return Double.valueOf(minimum.getText());
		}
		public void setMinimum(double minimum)
		{
			this.minimum.setText(new Double(minimum).toString());
		}
		public double getMaximum()
		{
			return Double.valueOf(maximum.getText());
		}
		public void setMaximum(double maximum)
		{
			this.maximum.setText(new Double(maximum).toString());
		}
	}
////////
	public void setMinimumProbabilityOfEndVertexMeeting(double minimum)
	{
		probabilityOfEndVertexMeeting.setMinimum(minimum);
	}
	public double getMinimumProbabilityOfEndVertexMeeting()
	{
		return probabilityOfEndVertexMeeting.getMinimum();
	}
	public void setMaximumProbabilityOfEndVertexMeeting(double maximum)
	{
		probabilityOfEndVertexMeeting.setMaximum(maximum);
	}
	public double getMaximumProbabilityOfEndVertexMeeting()
	{
		return probabilityOfEndVertexMeeting.getMaximum();
	}
////////
	private ProbabilityOfEndVertexMeeting probabilityOfEndVertexMeeting;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private JDesktopPane jDesktopPane = new JDesktopPane();
	
	private JButton buttonDefaultValues = new JButton("Default values");;
	private JButton buttonNewRandValues = new JButton("Random values");;
	private JButton buttonGenerate = new JButton("Generate CDCG file");;
	private JButton buttonCancel = new JButton("Cancel");;
	
	public SyntheticApplicationGeneratorGui(String title, int width, int height)
	{
		setSize(new Dimension(width, height));
		setContentPane(jDesktopPane);
		createInterface();
		setTitle(title);
		setVisible(true);
		setDefaultValues();
	}
	private void createInterface()
	{
		jDesktopPane.setBackground(new Color(230, 228, 228));

		createParametersPanel(10, 10, 420, 13 * heightLabel, "Task characterization (according to processor type)");
		int gaussWidth = 200;
		computationTime = new Gauss(20, 30, gaussWidth, "Computation time settings");
		communicationVolume = new Gauss(20, 150, gaussWidth, "Communication volume settings");
		parallelCommunications = new Gauss(220, 30, gaussWidth, "Parallel communication settings");
	
		generalParameters = new GeneralParameters(440, 10, 260, "General");
		probabilityOfEndVertexMeeting = new ProbabilityOfEndVertexMeeting(440, 110, 260, "Probability of end vertex meeting");

		createButton(440, 190,                   260, heightLabel, buttonDefaultValues);
		createButton(440, 190 + 1 * heightLabel, 260, heightLabel, buttonNewRandValues);
		createButton(440, 190 + 2 * heightLabel, 260, heightLabel, buttonCancel);
		createButton(440, 190 + 3 * heightLabel, 260, heightLabel, buttonGenerate);
	}

//////////////////////////////////////////////////////
	private void createButton(int x, int y, int width, int height, JButton jButton)
	{
		jButton.setFont(new Font("Arial", Font.BOLD, 12));
		jButton.setBounds(new Rectangle(x, y, width, height));
		jDesktopPane.add(jButton);
		jButton.addActionListener(this);
	}
	private int createLabel(int x, int y, String strLabel)
	{
		JLabel label = new JLabel(strLabel);
		Rectangle2D rectangle = jDesktopPane.getFontMetrics(label.getFont()).getStringBounds(strLabel, jDesktopPane.getGraphics());
		int labelWidth = (int)rectangle.getWidth();
		label.setBounds(new Rectangle(x, y, labelWidth, heightLabel));
		jDesktopPane.add(label);
		return x + labelWidth + 2;
	}
	private int computeTextFieldWidth(JTextField textField, int numbOfDig)
	{
		textField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
		byte array[] = new byte[numbOfDig];
		String strVazia = new String(array);
		Rectangle2D rectangle = jDesktopPane.getFontMetrics(textField.getFont()).getStringBounds(strVazia, jDesktopPane.getGraphics());
		return (int)rectangle.getWidth() + 6; // Somado 6 para dar uma folga;
	}
	private JTextField createTextField(int x, int y, int numbOfDig)
	{
		JTextField textField = new JTextField();
		int textFieldWidth = computeTextFieldWidth(textField, numbOfDig);
		textField.setBounds(new Rectangle(x, y, textFieldWidth, heightLabel));
		textField.setHorizontalAlignment(JTextField.RIGHT);
		jDesktopPane.add(textField);
		return textField;
	}
	private JTextField createLabelAndTextField(int x, int y, String strLabel, int numbOfDig)
	{
		int labelFinal_x = createLabel(x, y, strLabel);
		return createTextField(labelFinal_x + 2, y, numbOfDig);
	}
	private void createParametersPanel(int x, int y, int width, int height, String stringPane)
	{
		JLayeredPane parametros = new JLayeredPane();
		if(parametros == null)
			return;
		parametros.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		parametros.setBounds(x, y, width, height);
		parametros.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), stringPane));
		jDesktopPane.add(parametros);
	}

//////////////////////////////////////////////////////
	public void setDefaultValues()
	{
		setNumberOfProcessors(4);
		setNumberOfGraphLevels(5);
		setDependeceDegree(40);

		setMinimumProbabilityOfEndVertexMeeting(10);
		setMaximumProbabilityOfEndVertexMeeting(50);

		setParallelCommunicationMean(6);
		setParallelCommunicationStandardDeviation(6);
		setParallelCommunicationMinimum(3);
		setParallelCommunicationMaximum(9);

		setComputationTimeMean(50);
		setComputationTimeStandardDeviation(50);
		setComputationTimeMinimum(20);
		setComputationTimeMaximum(100);

		setCommunicationVolumeMean(200);
		setCommunicationVolumeStandardDeviation(200);
		setCommunicationVolumeMinimum(50);
		setCommunicationVolumeMaximum(400);
	}
	public void setRandomValues()
	{
		Randomico rand = new Randomico();

		setNumberOfProcessors(rand.randomNumber(1, 20));
		setNumberOfGraphLevels(rand.randomNumber(1, 7));
		setDependeceDegree(rand.randomNumber(1, 100));

		int minProb = rand.randomNumber(1, 100);
		setMinimumProbabilityOfEndVertexMeeting(minProb);
		setMaximumProbabilityOfEndVertexMeeting(rand.randomNumber(minProb, 100));

		int meanParCom = rand.randomNumber(1, 20);
		setParallelCommunicationMean(meanParCom);
		setParallelCommunicationStandardDeviation(rand.randomNumber(0, meanParCom * 2));
		setParallelCommunicationMinimum(rand.randomNumber(1, meanParCom));
		setParallelCommunicationMaximum(rand.randomNumber(meanParCom, 40));

		int meanCompTime = rand.randomNumber(10, 50);
		setComputationTimeMean(meanCompTime);
		setComputationTimeStandardDeviation(rand.randomNumber(0, meanCompTime * 2));
		setComputationTimeMinimum(rand.randomNumber(1, meanCompTime));
		setComputationTimeMaximum(rand.randomNumber(meanParCom, 100));

		int meanComVol = rand.randomNumber(50, 200);
		setCommunicationVolumeMean(meanComVol);
		setCommunicationVolumeStandardDeviation(rand.randomNumber(0, meanComVol * 2));
		setCommunicationVolumeMinimum(rand.randomNumber(1, meanComVol));
		setCommunicationVolumeMaximum(rand.randomNumber(meanComVol, 400));
	}
	public void writeDescription()
	{
		FileDialog fd = new FileDialog(this, "Create Description", FileDialog.SAVE);
		fd.setVisible(true);
		String fileName = fd.getDirectory() + fd.getFile();
		if(fileName == null)
			return;
		Description description = new Description(fileName);
		description.createCDCG(this);
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equalsIgnoreCase(buttonDefaultValues.getText()))
		{
			setDefaultValues();
			jDesktopPane.update(jDesktopPane.getGraphics());
			return;
		}
		if(e.getActionCommand().equalsIgnoreCase(buttonNewRandValues.getText()))
		{
			setRandomValues();
			jDesktopPane.update(jDesktopPane.getGraphics());
			return;
		}
		if(e.getActionCommand().equalsIgnoreCase(buttonGenerate.getText()))
		{
			VertexContent.resetGlobalId();
			writeDescription();
			return;
		}
		if(e.getActionCommand().equalsIgnoreCase(buttonCancel.getText()))
		{
			this.dispose();
			return;
		}
	}
}


