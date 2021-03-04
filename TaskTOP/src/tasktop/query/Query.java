package tasktop.query;

public abstract class Query {
	
	private boolean bOpen = false;
	
	protected StringBuilder sb = new StringBuilder();
	
	protected Query(String id, Expr expression) {
		sb.append(id);
		if(expression != null) {
			sb.append(" ");
			sb.append(expression.toString());
		}
	}
	
	protected Query() {
		
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
			return Expr.b(new Expr(left + " && " + right));
		}
		
		public static Expr or(Expr left, Expr right) {
			return Expr.b(new Expr(left + " || " + right));
		}
		
		public static Expr not(Expr expr) {
			return new Expr("!" + Expr.b(expr));
		}
		
		public static Expr b(Expr expr) {
			return new Expr("(" + expr + ")");
		}
		
	}
	
	
	
	

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return sb.toString();
	}
}
