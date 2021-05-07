package tasktop.panels;

import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tasktop.query.Query.Expr;

public class StatementPanel extends JPanel {
	
	private Collection<String> tasks;
	
	private JComboBox<String> cmbTask;
	private JComboBox<String> cmbTaskState;
	
	private JLabel lblConnector;
	
	public StatementPanel() {
		this(".");
	}
	
	public StatementPanel(String connector) {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		cmbTask = new JComboBox<String>();
		cmbTaskState = new JComboBox<String>(new String[] { "Disabled", "Done" });
		lblConnector = new JLabel(connector);
		
		add(cmbTask);
		add(lblConnector);
		add(cmbTaskState);
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
		
		return new Expr(selectedTask + "." + cmbTaskState.getSelectedItem());
	}

}
