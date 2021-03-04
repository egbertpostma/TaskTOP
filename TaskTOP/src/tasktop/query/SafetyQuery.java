package tasktop.query;

public class SafetyQuery extends Query {

	private static final String queryId = "A[]";

	public SafetyQuery(Expr expression) {
		super(queryId, expression);	
	}

}
