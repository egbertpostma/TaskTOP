package tasktop.panels;

import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import tasktop.query.Query.Expr;

public class StatementTimePanel extends JPanel {
	
	private Collection<String> tasks;
	
	private JComboBox<String> cmbTask;
	private JComboBox<ComboItem> cmbConnector;
	private JSpinner spnTime;
	
	private JLabel lblTimeVariable;
	private JLabel lblConnector;
	
	private class ComboItem {
		private String value;
		private String label;
		
		public ComboItem(String value, String label) {
			this.value = value;
			this.label = label;
		}
		
		public String getValue() { return this.value; }
		public String getLabel() { return this.label; }
		
		@Override
		public String toString() {
			return this.label;
		}
	}
	
	public StatementTimePanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		cmbTask = new JComboBox<String>();
		lblTimeVariable = new JLabel("elapsed time of");
		cmbConnector = new JComboBox<ComboItem>();
		cmbConnector.addItem(new ComboItem("<=", "smaller than or equal to"));
		cmbConnector.addItem(new ComboItem("<",  "smaller than"));
		cmbConnector.addItem(new ComboItem("==", "equal to"));
		cmbConnector.addItem(new ComboItem(">",  "greater than"));
		cmbConnector.addItem(new ComboItem(">=", "greater than or equal to"));
		
		SpinnerModel spnModel = new SpinnerNumberModel(0, 0, 100, 1);
		spnTime = new JSpinner(spnModel);
		
		
		add(lblTimeVariable);
		add(cmbTask);
		add(cmbConnector);
		add(spnTime);
		
	}
	
	public void SetConnector(String connector) {
		if(connector.isEmpty()) return;
		
		lblConnector.setText(connector);
	}
	
	public void SetTasks(Collection<String> tasks) {
		this.tasks = tasks;
		
		cmbTask.removeAllItems();
		for (String task : this.tasks) {
			cmbTask.addItem(task);
		}	
	}
	
	public Expr GetExpression() {
		if(cmbTask.getSelectedItem() == null) return new Expr("");
		String selectedTask = cmbTask.getSelectedItem().toString();
		if(!selectedTask.equals("top_level")) selectedTask = "t_" + selectedTask;
		
		return new Expr(selectedTask + ".elapsedTime" + ((ComboItem)cmbConnector.getSelectedItem()).getValue() + " " + (Integer)spnTime.getValue());
	}

}
