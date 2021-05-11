package tasktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import tasktop.panels.QueryPanel;
import tasktop.query.NotDeadlockQuery;
import tasktop.query.Query.Expr;
import tasktop.query.ReachabilityQuery;


public class MainWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1035975675755521209L;

	private class FilePanel extends JPanel implements ActionListener
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6191533394581383230L;
		public JTextField txtFileName = new JTextField();
		private final boolean isInput;
		
		public FilePanel(boolean isInput)
		{
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			setBorder(new EmptyBorder(2,5,2,5));
			
			this.isInput = isInput;
			JLabel label = new JLabel(isInput ? "Input:" : "Output:");
			label.setPreferredSize(new Dimension(50, 0));
			add(label);
			add(txtFileName);
			txtFileName.setColumns(20);
			JButton browseButton = new JButton("Browse");
			browseButton.setPreferredSize(new Dimension(100, 24));
			browseButton.addActionListener(this);
			add(browseButton);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser c = new JFileChooser(".");
			c.setFileFilter(new FileNameExtensionFilter("XML", "xml"));
			try {
				if (isInput && c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
					txtFileName.setText(c.getSelectedFile().getCanonicalPath());
				else if (!isInput && c.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
					txtFileName.setText(c.getSelectedFile().getCanonicalPath());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private TransformationEngine t = new TransformationEngine();
	private QueryPanel qp = new QueryPanel(t);
		
	public MainWindow() {
		super();
		
		setTitle("TaskTOP");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setSize(800,400);
		
		getContentPane().setLayout(new BorderLayout());
		
		

		
		Panel transformPanel = new Panel();
		transformPanel.setLayout(new BoxLayout(transformPanel, BoxLayout.PAGE_AXIS));
		
		FilePanel inputFilePanel = new FilePanel(true);
		FilePanel outputFilePanel = new FilePanel(false);
		
		transformPanel.add(inputFilePanel);
		transformPanel.add(outputFilePanel);
		
		JButton btnTransform = new JButton("Transform");
//		btnTransform.setPreferredSize(new Dimension(100, 24));
		JCheckBox chkOpenInUppaal = new JCheckBox("Open in UPPAAL", false);	
		
		JPanel executeTransformPanel = new JPanel(new BorderLayout());
		executeTransformPanel.setBorder(new EmptyBorder(2,5,2,5));
		executeTransformPanel.add(btnTransform);
		
		transformPanel.add(executeTransformPanel);
		
		btnTransform.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	

				t.setInputFile(inputFilePanel.txtFileName.getText());
				t.setOutputFile(outputFilePanel.txtFileName.getText());
				
				if(t.execute()) {
					btnTransform.setBackground(Color.GREEN);
					
					qp.setEnabled(true);
					qp.refreshTasks();
					
					if(chkOpenInUppaal.isSelected()) {
						runUppaal(t.getOutputFile());
					}
				} else {
					btnTransform.setBackground(Color.RED);
					
					qp.setEnabled(false);
					
					JOptionPane.showMessageDialog(null, t.getLog(), "Error!", JOptionPane.ERROR_MESSAGE);
				}
				

			}
		});
		
		
		
		
		add(transformPanel, BorderLayout.NORTH);
		
		qp.setEnabled(false);	
		add(qp, BorderLayout.CENTER);
		
	}
	
	private void runUppaal(String outputFileName) {
		
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "c:/uppaal-4.1.24/uppaal.jar", outputFileName); 
		try {
			Process procUppaal = pb.start();
			if(procUppaal.waitFor() != 0) {
				System.err.println("UPPAAL process has terminated with error.");
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
