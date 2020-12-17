package tasktop.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;

import ctt.Task;
import tasktop.ModelResource;
import tasktop.TransformationEngine;

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
		super(new GridLayout(2,1));
		
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

		Panel panelReachQuery = new Panel(new BorderLayout());

		cmbTasks = new JComboBox<String>();

		panelReachQuery.add(cmbTasks);

		Panel panelOtherQuery = new Panel(new BorderLayout());


		panelQueryImpl.add(panelReachQuery);
		panelQueryImpl.add(panelOtherQuery);

		add(rbtnPanel);
		add(panelQueryImpl);			
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