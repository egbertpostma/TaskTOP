package tasktop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tasktop.query.*;

public class QueryEngine {
	
	public enum TraceType {
		SOME("-t0"),
		SHORTEST("-t1"),
		FASTEST("-t2");
		
		private String parameter = "";
		
		TraceType(String s) {
			parameter = s;
		}
	}
	
	private TraceType traceType = TraceType.SOME;

	private TransformationEngine transformationEngine;

	private List<String> queries = new ArrayList<String>();

	private File queryFile = null;

	public QueryEngine(TransformationEngine te) {
		this.transformationEngine = te;
	}

	private final static String UPPAAL_CMDL_EXEC = "/home/egbert/dev-tools/uppaal64-4.1.24/bin-Linux/verifyta";

	public boolean execute(TraceType traceType) {
		this.traceType = traceType;
		return execute();
	}
	
	public boolean execute() {
		if(transformationEngine == null) {
			System.err.println("Cannot execute query engine, transformation engine not set!");
			return false;
		}

		if(!transformationEngine.isSuccess()) {
			System.err.println("Cannot execute query engine, transformation was not successful!");
			return false;
		}

		if(transformationEngine.getOutputFile().isBlank()) {
			System.err.println("Cannot execute query engine, transformation output file is empty...");
			return false;
		}

		if(!build()) {
			System.err.println("Cannot execute query engine, build failed!");
			return false;
		}

		File outputFile = new File("/home/egbert/Documents/uppaal_result.res");

		try {
			if(!outputFile.createNewFile()) {
				outputFile.delete();
				outputFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		ProcessBuilder pb = new ProcessBuilder(new String[] { UPPAAL_CMDL_EXEC, "-s", "-o2", traceType.parameter, transformationEngine.getOutputFile(), queryFile.getAbsolutePath() }); 
		pb.redirectError(ProcessBuilder.Redirect.appendTo(outputFile));
		pb.redirectOutput(ProcessBuilder.Redirect.appendTo(outputFile));


		try {
			Process procUppaalCmdl = pb.start();
			System.out.println("Starting verifyta as: " + procUppaalCmdl.toString());
			if(procUppaalCmdl.waitFor() != 0) {
				System.err.println("verifyta process terminated with an error! See " + outputFile.getAbsolutePath() + " for details.");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		showFile(outputFile.getAbsolutePath());
		
		return true;

	} 

	public QueryEngine add(String query) {
		queries.add(query);
		return this;
	}
	
	public QueryEngine add(Query query) {
		queries.add(query.toString());
		return this;
	}

	private boolean build() {
		if(queryFile != null && queryFile.exists()) {
			queryFile.delete();
		}

		if(queries.isEmpty()) {
			return false;
		}

		try {
			queryFile = File.createTempFile(UUID.randomUUID().toString(), ".q");
			queryFile.deleteOnExit();

			FileOutputStream os = new FileOutputStream(queryFile);

			for (String query : queries) {
				System.out.println("Writing query: " + query);
				os.write(query.getBytes());
				os.write('\n');
			}
			os.close();

		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private void showFile(String file) {		
		ProcessBuilder pb = new ProcessBuilder("code", file); 
		try {
			Process procCode = pb.start();
			if(procCode.waitFor() != 0) {
				System.err.println("UPPAAL process has terminated with error.");
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
