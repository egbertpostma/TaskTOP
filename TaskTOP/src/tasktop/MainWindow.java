package tasktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import tasktop.query.NotDeadlockQuery;


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
	
				TransformationEngine t = new TransformationEngine();
				t.setInputFile(inputFilePanel.txtFileName.getText());
				t.setOutputFile(outputFilePanel.txtFileName.getText());
				
				if(t.execute()) {
					btnTransform.setBackground(Color.GREEN);
					
					if(chkOpenInUppaal.isSelected()) {
						runUppaal(t.getOutputFile());
					} else {
						
						QueryEngine qe = new QueryEngine(t);
						
						qe.add(new NotDeadlockQuery());
						
						qe.execute();
						
					}
				} else {
					btnTransform.setBackground(Color.RED);
				}
				

			}
		});
		
		
		transformPanel.add(chkOpenInUppaal);
		
		add(transformPanel);
		
		pack();
	}
	
	private void runUppaal(String outputFileName) {
		
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/home/egbert/dev-tools/uppaal64-4.1.24/uppaal.jar", outputFileName); 
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
