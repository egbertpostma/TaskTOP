package tasktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
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

	private class FilePanel extends Panel implements ActionListener
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6191533394581383230L;
		public JTextField txtFileName = new JTextField();
		private final boolean isInput;
		
		public FilePanel(boolean isInput)
		{
			super(new BorderLayout());
			this.isInput = isInput;
			add(txtFileName, BorderLayout.CENTER);
			txtFileName.setColumns(20);
			JButton browseButton = new JButton("Browse");
			browseButton.addActionListener(this);
			add(browseButton, BorderLayout.EAST);
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
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		FilePanel inputFilePanel = new FilePanel(true);
		FilePanel outputFilePanel = new FilePanel(false);
		
		add(inputFilePanel);
		add(outputFilePanel);
		
		Panel transformPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
		
		JButton btnTransform = new JButton("Transform");
		JCheckBox chkOpenInUppaal = new JCheckBox("Open in UPPAAL", false);		
		
		JLabel overlay = new JLabel("Busy...");
		overlay.setOpaque(false);
		overlay.setBackground(Color.RED);
		
		setGlassPane(overlay);
		
		transformPanel.add(btnTransform);
		
		btnTransform.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	

				t.setInputFile(inputFilePanel.txtFileName.getText());
				t.setOutputFile(outputFilePanel.txtFileName.getText());
				
				if(t.execute()) {
					btnTransform.setBackground(Color.GREEN);
					
					qp.setEnabled(true);
					
					if(chkOpenInUppaal.isSelected()) {
						runUppaal(t.getOutputFile());
					}
				} else {
					btnTransform.setBackground(Color.RED);
					
					qp.setEnabled(true);
				}
				

			}
		});
		
		
		transformPanel.add(chkOpenInUppaal);
		
		add(transformPanel);
		
		qp.setEnabled(false);	
		add(qp);
		
		pack();
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
