package tasktop;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import transformer.CTT2UPPAAL;
import transformer.CTTXML2CTT;
import transformer.UPPAAL2XML;

public class TransformationEngine extends Thread {
	
	public interface ThreadCompleteListener {
	    void notifyOfThreadComplete(final Thread thread);
	}
	
	private final Set<ThreadCompleteListener> listeners
	    = new CopyOnWriteArraySet<ThreadCompleteListener>();
	public final void addListener(final ThreadCompleteListener listener) {
		listeners.add(listener);
	}
	public final void removeListener(final ThreadCompleteListener listener) {
		listeners.remove(listener);
	}
	private final void notifyListeners() {
		for (ThreadCompleteListener listener : listeners) {
			listener.notifyOfThreadComplete(this);
		}
	}
	
	private String inputFile = "";
	private String outputFile = "";
	private boolean isSuccess = false;
	
	public TransformationEngine() {
		
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	
	public boolean isSuccess() { return isSuccess; }
	
	public boolean execute() {
		if(outputFile.isBlank())
			outputFile = inputFile.replace(".xml", "_uppaal.xml");
				
		System.out.println("Input: " + inputFile);
		System.out.println("Output: " + outputFile);
		
		// Create resources
		ModelResource cttxml	= new Input(inputFile, Language.CTT_XML);
		ModelResource ctt 		= new Temporary(Language.CTT);
		ModelResource uppaal	= new Temporary(Language.UPPAAL);
		ModelResource uppaalxml = new Output(outputFile, Language.UPPAAL_XML);

		// Start conversion
		
		CTTXML2CTT test = new CTTXML2CTT();
		CTT2UPPAAL c2u = new CTT2UPPAAL();
		UPPAAL2XML u2x = new UPPAAL2XML();

		boolean success = true;

		try {
			if (success)
				success = test.execute(cttxml, ctt);
			if (success)
				success = c2u.execute(ctt, uppaal);
			if (success)
				success = u2x.execute(uppaal, uppaalxml);

			System.out.println(success ? "Successfully converted models" : "Failed to convert models");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		isSuccess = success;
		
		return success;
		
	}

	@Override
	public void run() {
		
		try {
			execute();
		} finally {
			notifyListeners();
		}
		
	}
	
}
