package tasktop;

import java.util.Date;

import transformer.CTT2UPPAAL;
import transformer.CTTXML2CTT;
import transformer.UPPAAL2XML;

public class TransformationEngine {
	
	private String inputFile = "";
	private String outputFile = "";
	private boolean isSuccess = false;
	private boolean isInitialized = false;
	
	ModelResource cttxml	= null;
	ModelResource ctt 		= null;
	ModelResource uppaal	= null;
	ModelResource uppaalxml = null;
	
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
	
	public ModelResource getCTTModel() {
		return ctt;
	}
	
	public boolean isSuccess() { return isSuccess; }
	
	public boolean execute() {
		if(!initialize()) {
			return false;
		}
			
		
//		System.out.println("Input: " + inputFile);
//		System.out.println("Output: " + outputFile);

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

//			System.out.println(success ? "Successfully converted models" : "Failed to convert models");
			if(!success) 
				err("Failed to convert models");
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			log(e.getMessage());
		}
	
		isSuccess = success;
		
		return success;	
	}
	
	private boolean initialize() {
		isInitialized = false;
		
		this.log = "";
		
		if(inputFile.isBlank()) {
//			System.err.println("Cannot initialize transformation engine: Inputfile is not set.");
			err("Cannot initialize transformation engine: Inputfile is not set.");
			return false;
		}
		
		if(outputFile.isBlank()) {
			outputFile = inputFile.replace(".xml", "_uppaal.xml");	
			System.out.println("Using generated output filename: " + outputFile);
		}
		
		cttxml		= new Input(inputFile, Language.CTT_XML);
		ctt 		= new Temporary(Language.CTT);
		uppaal		= new Temporary(Language.UPPAAL);
		uppaalxml 	= new Output(outputFile, Language.UPPAAL_XML);
		
		if(!cttxml.isValid()) {
			cttxml 		= null;
			ctt	   		= null;
			uppaal 		= null;
			uppaalxml 	= null;
			
			//System.err.println("Cannot initialize transformation engine: Inputfile is not valid.");
			err("Cannot initialize transformation engine: Inputfile is not valid.");
			return false;
		}
		
		isInitialized = true;
		return true;
	}
	
	private String log = "";
	
	private void log(String message) {
		this.log += "INFO: " + message + "\n";
	}
	
	private void err(String message) {
		this.log += "ERROR: " + message + "\n";
	}
	
	public String getLog() {
		return this.log;
	}
	
}
