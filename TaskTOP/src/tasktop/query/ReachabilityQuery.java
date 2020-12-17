package tasktop.query;

public class ReachabilityQuery extends Query {
	
	private static final String queryId = "E<>";

	public ReachabilityQuery(Expr expression) {
		super(queryId, expression);	
	}
	
}
