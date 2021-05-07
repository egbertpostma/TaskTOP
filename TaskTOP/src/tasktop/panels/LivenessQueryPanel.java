package tasktop.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import tasktop.QueryEngine;
import tasktop.TransformationEngine;
import tasktop.query.LivenessQuery;

public class LivenessQueryPanel extends JPanel  {
		
	private TransformationEngine transformationEngine;
	
	StatementPanel ifStatementPanel;
	StatementPanel thenStatementPanel;	
	
	private JTextArea txtResult;
	
	public LivenessQueryPanel(TransformationEngine te) {
	
		super(new BorderLayout());
		
		this.transformationEngine = te;
		
		JPanel questionPanel = new JPanel();
		questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.PAGE_AXIS));
		
		JPanel ifQuestionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JPanel thenQuestionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

		ifStatementPanel = new StatementPanel("is");
		thenStatementPanel = new StatementPanel("is");
		
		ifQuestionPanel.add(new JLabel("If"));
		ifQuestionPanel.add(ifStatementPanel);
		ifQuestionPanel.add(new JLabel(","));
		
		ifQuestionPanel.add(new JLabel("then"));
		ifQuestionPanel.add(thenStatementPanel);
		ifQuestionPanel.add(new JLabel("."));

		
		questionPanel.add(ifQuestionPanel);
		questionPanel.add(thenQuestionPanel);
		
		
		add(questionPanel, BorderLayout.NORTH);
		
		txtResult = new JTextArea();
		txtResult.setEditable(false);
		add(txtResult, BorderLayout.CENTER);
		
		JButton executeButton = new JButton("Execute");
		executeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				QueryEngine qe = new QueryEngine(transformationEngine);
				
				qe.add(new LivenessQuery(
						ifStatementPanel.GetExpression(),
						thenStatementPanel.GetExpression()
						));
				
				qe.execute();	
				txtResult.setText(qe.lastResult());
			}
		});
		
		add(executeButton, BorderLayout.SOUTH);
	}
	
	public void SetTasks(Collection<String> tasks) {
		
		ifStatementPanel.SetTasks(tasks);
		thenStatementPanel.SetTasks(tasks);		
	}

}
