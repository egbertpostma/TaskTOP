package tasktop.query;

public class LivenessQuery extends Query {
	
	private static final String queryId = "A<>";

	public LivenessQuery(Expr expression) {
		super(queryId, expression);	
	}
	
	public LivenessQuery(Expr expr1, Expr expr2) {
		sb.append(expr1.toString());
		sb.append(" --> ");
		sb.append(expr2.toString());
	}

}
