package tasktop.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.management.Query;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;

import ctt.Task;
import tasktop.ModelResource;
import tasktop.QueryEngine;
import tasktop.TransformationEngine;
import tasktop.query.NotDeadlockQuery;
import tasktop.query.ReachabilityQuery;
import tasktop.query.Query.Expr;

public class QueryPanel extends Panel implements ActionListener{
	
	private TransformationEngine engine = null;
	
	public enum QueryType {
		REACHABILITY,
		OTHER,
		NONE
	}
	

	private JRadioButton rbtnReachQuery;
	private JRadioButton rbtnOtherQuery;
	private JComboBox<String> cmbTasks;
	private Panel panelQueryImpl;

	public QueryPanel(TransformationEngine engine) {
		super(new GridLayout(4,1));
		
		this.engine = engine;

		Panel rbtnPanel = new Panel(new GridLayout(1,3));
		ButtonGroup buttonGroup = new ButtonGroup();

		rbtnReachQuery = new JRadioButton("Reachability");
		rbtnReachQuery.setActionCommand("reach");
		rbtnReachQuery.addActionListener(this);
		buttonGroup.add(rbtnReachQuery);

		rbtnOtherQuery = new JRadioButton("Other");
		rbtnOtherQuery.setActionCommand("other");
		rbtnOtherQuery.addActionListener(this);
		buttonGroup.add(rbtnOtherQuery);

		rbtnPanel.add(rbtnReachQuery, 0);
		rbtnPanel.add(rbtnOtherQuery, 1);

		panelQueryImpl = new Panel(new CardLayout());

		Panel panelReachQuery = new Panel(new GridLayout(1,2));

		cmbTasks = new JComboBox<String>();

		panelReachQuery.add(cmbTasks);
		
		JComboBox<String> cmbTaskStates = new JComboBox<String>();
		cmbTaskStates.addItem("Disabled");
		cmbTaskStates.addItem("Enabled");
		cmbTaskStates.addItem("Suspended");
		cmbTaskStates.addItem("Done");
		
		panelReachQuery.add(cmbTaskStates);

		Panel panelOtherQuery = new Panel(new BorderLayout());


		panelQueryImpl.add(panelReachQuery);
		panelQueryImpl.add(panelOtherQuery);
		
		JButton btnQuery = new JButton("Query...");
		btnQuery.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				btnQuery.setBackground(null);
				QueryEngine qe = new QueryEngine(engine);
				
				qe.add(new ReachabilityQuery(
						new Expr("t_" + cmbTasks.getSelectedItem().toString() + "." + cmbTaskStates.getSelectedItem().toString())));
				
				if(qe.execute()) {
					if(qe.allTracesSuccessful())
						btnQuery.setBackground(Color.GREEN);
					else
						btnQuery.setBackground(Color.ORANGE);
				} else {
					btnQuery.setBackground(Color.RED);
				}
			}
		});

		add(rbtnPanel);
		add(panelQueryImpl);
		
		JTextArea txtResults = new JTextArea();
		
		add(btnQuery);
		add(txtResults);
		
	}

	private void refreshTasks() {
		cmbTasks.removeAllItems();

		if(engine != null && engine.isSuccess()) {
			ModelResource cttModel = engine.getCTTModel();
			try {

				Collection<?> tasks;
				tasks = cttModel.model().getAllOfKind("Task");

				for(Object t : tasks) {
					Task task = (Task)t;
					
					// Skip intermediate tasks
					if(task.getId().endsWith("_null") || task.getId().endsWith("_opt") || task.getId().endsWith("_b")) {
						continue;
					}
					
					cmbTasks.addItem(task.getId());
				}
			} catch (EolModelElementTypeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public QueryType getCurrentSelectedQueryType() {
		if(rbtnReachQuery.isSelected()) return QueryType.REACHABILITY;
		if(rbtnOtherQuery.isSelected()) return QueryType.OTHER;

		return QueryType.NONE;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		switch(e.getActionCommand()) {
		case "reach":
			refreshTasks();
			break;
		case "other": 
			break;
		}
	}
}