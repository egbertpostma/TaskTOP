package tasktop.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;

import ctt.Task;
import tasktop.ModelResource;
import tasktop.TransformationEngine;

public class QueryPanel extends JPanel implements ActionListener {
	
	private TransformationEngine engine = null;
	
	public enum QueryType {
		REACHABILITY,
		LIVENESS,
		SAFETY,
		OTHER,
		NONE
	}
	

	private JRadioButton rbtnReachQuery;
	private JRadioButton rbtnLivenessQuery;
	private JRadioButton rbtnSafetyQuery;
	private Panel panelQueryImpl;
	
	private Collection<String> tasks = new ArrayList<String>();
	
	private LivenessQueryPanel livenessQueryPanel;
	private ReachabilityQueryPanel reachabilityQueryPanel;
	private SafetyQueryPanel safetyQueryPanel;

	public QueryPanel(TransformationEngine engine) {
		super(new BorderLayout());
		setBorder(new EmptyBorder(2,5,2,5));
		
		this.engine = engine;

		Panel rbtnPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
		ButtonGroup buttonGroup = new ButtonGroup();

		rbtnReachQuery = new JRadioButton("Reachability");
		rbtnReachQuery.setActionCommand("reach");
		rbtnReachQuery.addActionListener(this);
		buttonGroup.add(rbtnReachQuery);
		
		rbtnLivenessQuery = new JRadioButton("Liveness");
		rbtnLivenessQuery.setActionCommand("liveness");
		rbtnLivenessQuery.addActionListener(this);
		buttonGroup.add(rbtnLivenessQuery);

		rbtnSafetyQuery = new JRadioButton("Safety");
		rbtnSafetyQuery.setActionCommand("safety");
		rbtnSafetyQuery.addActionListener(this);
		buttonGroup.add(rbtnSafetyQuery);

		rbtnPanel.add(rbtnReachQuery, 0);
		rbtnPanel.add(rbtnLivenessQuery, 1);
		rbtnPanel.add(rbtnSafetyQuery, 2);

		panelQueryImpl = new Panel(new CardLayout());

		reachabilityQueryPanel = new ReachabilityQueryPanel(this.engine);
		livenessQueryPanel = new LivenessQueryPanel(this.engine);
		safetyQueryPanel = new SafetyQueryPanel(engine);
		
		panelQueryImpl.add(reachabilityQueryPanel, rbtnReachQuery.getActionCommand());
		panelQueryImpl.add(livenessQueryPanel, rbtnLivenessQuery.getActionCommand());
		panelQueryImpl.add(safetyQueryPanel, rbtnSafetyQuery.getActionCommand());
		

		add(rbtnPanel, BorderLayout.PAGE_START);
		add(panelQueryImpl, BorderLayout.CENTER);		
	}

	public void refreshTasks() {
		this.tasks.clear();
		
		this.tasks.add("top_level");

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
					
					this.tasks.add(task.getId());
				}
			} catch (EolModelElementTypeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		reachabilityQueryPanel.SetTasks(this.tasks);
		livenessQueryPanel.SetTasks(this.tasks);
		safetyQueryPanel.SetTasks(this.tasks);
	}

	public QueryType getCurrentSelectedQueryType() {
		if(rbtnReachQuery.isSelected()) return QueryType.REACHABILITY;
		if(rbtnLivenessQuery.isSelected()) return QueryType.LIVENESS;
		if(rbtnSafetyQuery.isSelected()) return QueryType.OTHER;

		return QueryType.NONE;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CardLayout cl = (CardLayout)(panelQueryImpl.getLayout());
		
		cl.show(panelQueryImpl, e.getActionCommand());
	}
}