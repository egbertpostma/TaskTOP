package tasktop.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import tasktop.QueryEngine;
import tasktop.TransformationEngine;
import tasktop.query.SafetyQuery;
import tasktop.query.Query.Expr;

public class SafetyQueryPanelTime extends JPanel  {
	
	private Collection<String> tasks = new ArrayList<String>();
	
	private TransformationEngine transformationEngine;
	
	private StatementPanel statementPanel;
	private StatementTimePanel statementPanel2;
	
	private JTextArea txtResult;
	
	public SafetyQueryPanelTime(TransformationEngine te) {
	
		super(new BorderLayout());
		
		this.transformationEngine = te;
		
		JPanel questionPanel = new JPanel();
		questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.PAGE_AXIS));
		
		JPanel canQuestionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

		statementPanel = new StatementPanel("is");
		statementPanel2 = new StatementTimePanel();
		
		
		canQuestionPanel.add(new JLabel("if"));
		canQuestionPanel.add(statementPanel);
		canQuestionPanel.add(new JLabel(","));
		
		canQuestionPanel.add(new JLabel("then"));
		canQuestionPanel.add(statementPanel2);
		canQuestionPanel.add(new JLabel("."));
	
				
		questionPanel.add(canQuestionPanel);
		
		add(questionPanel, BorderLayout.NORTH);
		
		txtResult = new JTextArea();
		txtResult.setEditable(false);
		add(txtResult, BorderLayout.CENTER);
		
		JButton executeButton = new JButton("Execute");
		executeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				QueryEngine qe = new QueryEngine(transformationEngine);
				
				qe.add(new SafetyQuery(
						Expr.imply(
								statementPanel.GetExpression(), 
								statementPanel2.GetExpression()
								)
						));
				
				qe.execute();	
				
				txtResult.setText(qe.lastResult());
			}
		});
		
		add(executeButton, BorderLayout.SOUTH);
	}
	
	public void SetTasks(Collection<String> tasks) {
		this.tasks = tasks;
		
		statementPanel.SetTasks(tasks);
		statementPanel2.SetTasks(tasks);
	}

}
