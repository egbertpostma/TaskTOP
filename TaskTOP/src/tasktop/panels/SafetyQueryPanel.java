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

public class SafetyQueryPanel extends JPanel  {
	
	private Collection<String> tasks = new ArrayList<String>();
	
	private TransformationEngine transformationEngine;
	
	private StatementPanel statementPanel;
	
	private JTextArea txtResult;
	
	private Boolean negate = false;
	
	public SafetyQueryPanel(TransformationEngine te) {
	
		super(new BorderLayout());
		
		this.transformationEngine = te;
		
		JPanel questionPanel = new JPanel();
		questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.PAGE_AXIS));
		
		JPanel canQuestionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

		statementPanel = new StatementPanel("always");
		
		canQuestionPanel.add(new JLabel("Is"));
		canQuestionPanel.add(statementPanel);
		canQuestionPanel.add(new JLabel("?"));
		
		JCheckBox chkNegate = new JCheckBox("invert?");
		chkNegate.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				negate = (e.getStateChange() == ItemEvent.SELECTED);
				statementPanel.SetConnector(negate ? "never" : "always");
			}
		});
		
		canQuestionPanel.add(chkNegate);
		
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
						negate ? Expr.not(statementPanel.GetExpression()) : statementPanel.GetExpression()
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
	}

}
