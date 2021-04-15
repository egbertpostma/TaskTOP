package tasktop;

import tasktop.query.LivenessQuery;
import tasktop.query.Query.Expr;
import tasktop.query.ReachabilityQuery;

public class Tasktop {
	
	private static String inputFile = "";
	private static String outputFile = "";
	
	private static void usage(String message) {
		if(!message.isEmpty()) {
			System.err.println(message);
		}
		System.err.println("Usage: tasktop [options]");
		System.err.println("Options:");
		System.err.println("\t-v <inputfile>.xml [<outputfile>.xml]");
		System.err.format("Current languages: %s\n", languageList());
	}
	
	private static void usage()
	{
		usage("");
	}
	
	private static String languageList() {
		return Language.getLanguageMap().keySet().toString();
	}
	
	private static boolean parseArgs(String[] args) {
		// Check if we need to open the GUI
		if(args.length > 0) {
			if(args[0].equals("-h")) {
				usage();
				System.exit(0);
			}
			
			if(args[0].equals("-v")) {
				// Open verbose
				if(args.length == 1) {
					usage();
					return false;
				}
				
				if(!args[1].endsWith(".xml")) {
					usage("Input file should be of type xml!");
					return false;
				}
				
				if(args.length > 2) {
					if(!args[2].endsWith(".xml")) {
						usage("Output file should be of type xml!");
						return false;
					}
					
					if(args[2].equals(args[0])) {
						usage("Input and Output filepaths cannot be the same!");
						return false;
					}
					
					outputFile = args[2];
				}
				
				inputFile = args[1];

				return true;
			}
			
			usage();
			return false;
			
		}
		
		// Open GUI
		return true;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if(!parseArgs(args)) System.exit(1);
		
		if(inputFile.isBlank() && outputFile.isBlank()) {
			// Open GUI
			
			MainWindow window = new MainWindow();
			window.setVisible(true);
			
			
		} else {
			// Make conversion here..
			
			TransformationEngine t = new TransformationEngine();
			
			t.setInputFile(inputFile);
			t.setOutputFile(outputFile);
			
			if(t.execute()) {			
				QueryEngine qe = new QueryEngine(t);
				
//				qe.add(new NotDeadlockQuery());
//				qe.add(new ReachabilityQuery(
//							new Expr("t_Cancel.Done")
//						));
//				qe.add(new SafetyQuery(
//						Expr.not(new Expr("top_level.Done"))
//					));
				
//				qe.add(new ReachabilityQuery(
//							Expr.and(
//								new Expr("t_Task_1.Done"),
//								Expr.not(new Expr("t_Task_2.Done"))
//								)
//						));
//				
//				qe.add(new SafetyQuery(
//						Expr.not( 
//							Expr.and(
//								Expr.not(new Expr("t_Task_1.Done")),
//								new Expr("t_Task_2.Done")
//							)
//						)
//					));
//				
//				qe.add(new LivenessQuery(
//						Expr.and(
//								new Expr("t_Task_1.Done"),
//								new Expr("t_Task_2.Done")
//							), 
//						new Expr("top_level.Done")
//					));
//				
				qe.add(new ReachabilityQuery(
						new Expr("top_level.Done")
					));
				qe.add(new LivenessQuery(
						new Expr("top_level.Done")
					));
				
				qe.execute();
			}
		}
		

		

	}

}
