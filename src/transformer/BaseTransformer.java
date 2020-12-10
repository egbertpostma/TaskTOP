package transformer;

import java.net.URI;
import java.net.URL;

import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.etl.EtlModule;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.execute.FixInstance;
import org.eclipse.epsilon.evl.execute.UnsatisfiedConstraint;

import cttconverter.ModelResource;
import cttconverter.Output;

public abstract class BaseTransformer {

	private final URI transformerURI;
	private final URI validatorURI;
	
	public BaseTransformer() {
		this(true);
	}
	
	public BaseTransformer(boolean requiresETL) {
		transformerURI = toFileURI("/transformations/" + this.getClass().getSimpleName() + ".etl");
		validatorURI   = toFileURI("/validations/" 	   + this.getClass().getSimpleName() + ".evl");
		
		// If transformer needs ETL, verify if transformerURI exists.
		if(requiresETL && transformerURI == null) {
			System.err.println("Transformer URI not valid, does '" + this.getClass().getSimpleName() + ".etl' exist?");
		}
		
		// We're not verifying the validatorURI as it might not exist, which is allowed.
	}
	
	public boolean execute(ModelResource i, ModelResource o) throws Exception {

		if(i == null || !i.isValid() || o == null || !o.isValid()) return false;
		if(transformerURI == null) {
			System.err.println("Transformer URI not valid, does '" + this.getClass().getSimpleName() + ".etl' exist?");
			return false;
		}

		EtlModule module = new EtlModule();
		module.parse(transformerURI);
		if (module.getParseProblems().size() > 0) {
			System.err.println("Parse errors occured...");
			for (ParseProblem problem : module.getParseProblems()) {
				System.err.println(problem.toString());
			}
			return false;
		}			

		module.getContext().getModelRepository().addModel(i.model());	
		module.getContext().getModelRepository().addModel(o.model());	

		module.execute();

		o.validate();
		Output _o = (Output)o;
		if(_o != null) _o.store();

		return (validatorURI != null) ? validate(i, o) : true;

	}

	public boolean validate(ModelResource i, ModelResource o) throws Exception {

		if(i == null || !i.isValid() || o == null || !o.isValid()) return false;
		if(validatorURI == null) {
			System.out.println("Validator not set, so transformation is valid.");
			return true;
		}

		EvlModule module = new EvlModule();
		module.parse(validatorURI);
		if (module.getParseProblems().size() > 0) {
			System.err.println("Parse errors occured...");
			for (ParseProblem problem : module.getParseProblems()) {
				System.err.println(problem.toString());
			}
			return false;
		}			

		module.getContext().getModelRepository().addModel(i.model());	
		module.getContext().getModelRepository().addModel(o.model());	

		module.execute();

		for (UnsatisfiedConstraint uc : module.getContext().getUnsatisfiedConstraints()) {
			System.err.format("Validation error in transformation:\n");
			System.err.println(uc);
			
		}
		if (!module.getContext().getUnsatisfiedConstraints().isEmpty()) {
			return false;
		}

		return true;
	}


	private static URI toFileURI(String fileName) {
		URI fileURI = null;
		try {
			URL fileURL = BaseTransformer.class.getResource(fileName);
			if (fileURL != null)
				fileURI = fileURL.toURI();
		} catch (Exception e) {
			System.err.format("Error getting resource for file %s\n", fileName);
		}
		return fileURI;
	}

}
