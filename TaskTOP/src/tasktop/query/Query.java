package tasktop.query;

public abstract class Query {
	
	private boolean bOpen = false;
	
	private StringBuilder sb = new StringBuilder();
	
	protected Query(String id, Expr expression) {
		sb.append(id);
		if(expression != null) {
			sb.append(" ");
			sb.append(expression.toString());
		}
	}
	
	public static class Expr {
	
		private String expression = "";
		public Expr(String expression) {
			this.expression = expression;
		}
		
		@Override
		public String toString() {
			return expression;
		}
		
		
		public static Expr and(Expr left, Expr right) {
			return new Expr(left.toString() + " && " + right.toString());
		}
		
		public static Expr or(Expr left, Expr right) {
			return new Expr(left.toString() + " || " + right.toString());
		}
		
		public static Expr not(Expr expr) {
			return new Expr("!(" + expr.toString() + ")");
		}
		
		public static Expr b(Expr expr) {
			return new Expr("(" + expr.toString() + ")");
		}
		
	}
	
	
	
	

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return sb.toString();
	}
}
