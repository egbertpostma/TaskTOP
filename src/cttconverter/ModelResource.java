package cttconverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.emf.InMemoryEmfModel;
import org.eclipse.epsilon.emc.plainxml.PlainXmlModel;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.evl.EvlModule;
import org.eclipse.epsilon.evl.execute.FixInstance;
import org.eclipse.epsilon.evl.execute.UnsatisfiedConstraint;

public abstract class ModelResource {
	
	protected String location;
	protected Language language;
	protected IModel model;
	protected Role role;
	private boolean isValid = false;
	private EvlModule validator;
	
	
	public ModelResource(String loc, Language lang, Role role) {
		this.location = loc;
		this.language = lang;
		this.role = role;
		
		try {
			createModel();
			initializeValidator();
			validate();			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			isValid = false;
		} 
	}
	
	public String location() {
		return location;
	}

	public Language language() {
		return language;
	}
	
	public boolean isValid() {
		return model != null && isValid;
	}
	
	public IModel model() {
		return model;
	}
	
	private void initializeValidator() throws FileNotFoundException, URISyntaxException, Exception {
		if(model == null) throw new Exception("Call 'createModel' first!");
		if(language == null || language.validator == null) return;
		
		validator = new EvlModule();
		validator.parse(toFileURI(language.validator));
		
		if (validator.getParseProblems().size() > 0) {
			System.err.println("Parse errors occured...");
			for (ParseProblem problem : validator.getParseProblems()) {
				System.err.println(problem.toString());
			}
			return;
		}

		validator.getContext().getModelRepository().addModel(model);
	}
	
	
	private void createModel() throws EolModelLoadingException, URISyntaxException, IOException {
		if(language.pkg == null) {
			this.model = createPlainXmlModel(language, role, location);
		} else {
			this.model = createEmfModel(language, role, location);
		}
	}
	
	public void validate() throws Exception {
		isValid = true;
		
		if(validator == null) return;
	
		validator.execute();
		for (UnsatisfiedConstraint uc : validator.getContext().getUnsatisfiedConstraints()) {
			System.err.format("Validation error in %s (%s):\n", location, language);
			System.err.println(uc);
			for(FixInstance fix : uc.getFixes()) {
				System.err.println("Automatic fix: " + fix.getTitle());
				fix.perform();
			}
		}
		
		validator.execute();
		if (!validator.getContext().getUnsatisfiedConstraints().isEmpty()) {
			isValid = false; 
			return;
		}
	}
	
	
	
	
	
	
	protected PlainXmlModel createPlainXmlModel(Language lang, Role role, String model)
			throws FileNotFoundException, EolModelLoadingException,
			URISyntaxException {
		StringProperties props = new StringProperties();
		props.put(PlainXmlModel.PROPERTY_NAME, lang.getName());
		props.put(PlainXmlModel.PROPERTY_URI, model);
		props.put(PlainXmlModel.PROPERTY_READONLOAD, "" + (role == Role.SOURCE));
		props.put(PlainXmlModel.PROPERTY_STOREONDISPOSAL, "" + (role == Role.TARGET));

		PlainXmlModel result = new PlainXmlModel();
		result.load(props, (String)null);
		return result;
	}

	/**
	 * Converts a file name into a file URI string, replacing any references to
	 * "bin" directories in the path to "sub".
	 */
	protected static URI toFileURI(String fileName) throws FileNotFoundException,
	URISyntaxException {
		URI fileURI = null;
		try {
			URL fileURL = Language.class.getResource('/' + fileName);
			if (fileURL != null)
				fileURI = fileURL.toURI();
		} catch (Exception e) {
			System.err.format("Error getting resource for file %s\n", fileName);
			throw e;
		}
		if (fileURI == null) {
			File f = new File("data/" + fileName);
			if (f.exists()) {
				fileURI = f.toURI();
			} else {
				System.out.println(f.toString() + " does not exist. Trying " + fileName);
				fileURI = URI.create("file:" + new File(fileName).getAbsolutePath());
			}
		}
		return fileURI;
	}


	protected static ResourceSetImpl getResourceSet()
	{
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		return resourceSet;
	}

	protected static Resource createResourceForXml(String model, ResourceSet rs) throws java.io.IOException
	{
		if (rs == null)
			rs = getResourceSet();
		org.eclipse.emf.common.util.URI uri;
		if(model.startsWith("archive:")){
			// Edit by Bas Klein Essink
			// The content of the else clause was here before I got here,
			// I assume it is a workaround for something I am not aware of,
			// so I am leaving it in for non archive URIs,
			// but toFileURI(model) breaks archive URIs.
			uri = org.eclipse.emf.common.util.URI.createURI(model);
		}else{
			try {
				uri = org.eclipse.emf.common.util.URI.createURI(toFileURI(model).toString());
				System.out.println(uri);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
		Resource res = rs.createResource(uri, "text/xml");
		if (res == null) {
			System.err.println("No resource for model: " + model);
		}
		res.load(Collections.emptyMap());
		try {
			EcoreUtil.resolveAll(rs);
		} catch (Exception ex) {
			System.err.println("Problem with resolving names for " + res + " with " + rs);
		}
		return res;
	}

	protected static InMemoryEmfModel loadInMemoryEmfModel(Language language, Role role, String model, ResourceSet set)
			throws java.io.IOException
	{
		Resource res = createResourceForXml(model, set);
		return new InMemoryEmfModel(language.getName(), res);
	}

	protected static EmfModel createEmfModel(Language language, Role role, String model)
			throws FileNotFoundException, EolModelLoadingException,
			URISyntaxException, java.io.IOException {
		return createEmfModel(language, role, model, null);
	}

	protected static EmfModel createEmfModel(Language language, Role role, String model, ResourceSet set)
			throws FileNotFoundException, EolModelLoadingException,
			URISyntaxException, java.io.IOException {
		assert language.getLocation() != null : String.format(
				"Can't create EMF model for language %s", language);
		if (language.pkg != null && role == Role.SOURCE)
			return loadInMemoryEmfModel(language, role, model, set);
		StringProperties props = new StringProperties();
		props.put(EmfModel.PROPERTY_NAME, language.getName());
		if (language.pkg != null) {
			props.put(EmfModel.PROPERTY_METAMODEL_URI, language.pkg.getNsURI());
		} else {
			props.put(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI, toFileURI(language.getLocation()));
		}
		props.put(EmfModel.PROPERTY_MODEL_URI, model);
		props.put(EmfModel.PROPERTY_READONLOAD, "" + (role == Role.SOURCE || role == Role.BOTH));
		props.put(EmfModel.PROPERTY_STOREONDISPOSAL, "" + (role == Role.TARGET || role == Role.BOTH));

		EmfModel result = new EmfModel();
		result.load(props, (String)null);
		return result;
	}

	/** Role of a model or language in a transformation. */
	protected enum Role {
		SOURCE, TARGET, BOTH;
	}
	
}
