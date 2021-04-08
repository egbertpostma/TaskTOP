package tasktop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.muml.core.common.cmd.Command;
import org.muml.core.common.cmd.PathArgument;
import org.muml.core.common.cmd.PathCommand;
import org.muml.core.common.cmd.Process;
import org.muml.uppaal.NTA;
import org.muml.uppaal.cmd.options.misc.NoOptionSummaryOption;
import org.muml.uppaal.cmd.options.misc.NoProgressIndicatorOption;
import org.muml.uppaal.cmd.options.trace.DiagnosticInfoOption;
import org.muml.uppaal.cmd.options.trace.DiagnosticInfoOption.TraceKind;
import org.muml.uppaal.cmd.options.tuning.HashTableSizeOption;
import org.muml.uppaal.cmd.options.tuning.ReuseStateSpaceOption;
import org.muml.uppaal.cmd.options.tuning.SpaceConsumptionOption;
import org.muml.uppaal.cmd.options.tuning.SpaceConsumptionOption.SpaceConsumptionOperator;
import org.muml.uppaal.options.CoordinationProtocolOptions;
import org.muml.uppaal.options.Options;
import org.muml.uppaal.options.OptionsFactory;
import org.muml.uppaal.options.StateSpaceReduction;
import org.muml.uppaal.options.TraceOptions;
import org.muml.uppaal.templates.Location;
import org.muml.uppaal.trace.DiagnosticTraceStandaloneSetup;
import org.muml.uppaal.trace.LocationActivity;
import org.muml.uppaal.trace.ProcessIdentifier;
import org.muml.uppaal.trace.Result;
import org.muml.uppaal.trace.State;
import org.muml.uppaal.trace.Trace;
import org.muml.uppaal.trace.TraceItem;
import org.muml.uppaal.trace.TracePackage;
import org.muml.uppaal.trace.TraceRepository;
import org.muml.uppaal.trace.scoping.DiagnosticTraceScopeProviderSingleton;

import com.google.inject.Injector;

import tasktop.query.Query;

public class QueryEngine {

	private TransformationEngine transformationEngine;

	private List<String> queries = new ArrayList<String>();

	private File queryFile = null;
	
	private final static String UPPAAL_CMDL_EXEC = "C:\\uppaal-4.1.19\\bin-Windows\\verifyta";

	public QueryEngine(TransformationEngine te) {
		this.transformationEngine = te;
	}
	
	private class VerifyTACommand extends PathCommand {
		public VerifyTACommand(Options options) {
			super(Path.fromOSString(UPPAAL_CMDL_EXEC));
			
			addParameter(new NoOptionSummaryOption());
			addParameter(new NoProgressIndicatorOption());
			TraceKind traceKind = null;
			switch (options.getTraceOptions()) {
			case NONE:
				traceKind = null;
				break;
			case FASTEST:
				traceKind = TraceKind.Fastest;
				break;
			case SHORTEST:
				traceKind = TraceKind.Shortest;
				break;
			case SOME:
				traceKind = TraceKind.Some;
				break;
			default:
				break;
			}
			if (traceKind != null)
				addParameter(new DiagnosticInfoOption(traceKind));

			addParameter(new HashTableSizeOption(options.getHashTableSize()));

			SpaceConsumptionOperator spaceConsumptionOperator = SpaceConsumptionOperator.Default;
			switch (options.getStateSpaceReduction()) {
			case AGGRESSIVE:
				spaceConsumptionOperator = SpaceConsumptionOperator.Most;
				break;
			case CONSERVATIVE:
				spaceConsumptionOperator = SpaceConsumptionOperator.Default;
				break;
			case NONE:
				spaceConsumptionOperator = SpaceConsumptionOperator.None;
				break;
			default:
				break;
			}
			if (spaceConsumptionOperator != null)
				addParameter(new SpaceConsumptionOption(spaceConsumptionOperator));

			addParameter(new ReuseStateSpaceOption());	
		}
	}

	private TraceRepository traceRepository;
	
	public boolean allTracesSuccessful() {
		if(traceRepository == null)
			return false;
		
		for(Trace t : traceRepository.getTraces()) {
			if(t.getResult() == Result.FAILURE)
				return false;
		}
		
		return true;
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

		File outputFile = new File(transformationEngine.getOutputFile().replace("xml", "trace"));

		try {
			if(!outputFile.createNewFile()) {
				outputFile.delete();
				outputFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		traceRepository = null;

		
		IPath modelPath = Path.fromOSString(transformationEngine.getOutputFile());
		IPath queryPath = Path.fromOSString(queryFile.getAbsolutePath());

		CoordinationProtocolOptions options = OptionsFactory.eINSTANCE.createCoordinationProtocolOptions();
		options.setTraceOptions(TraceOptions.FASTEST);
		options.setConnectorOutBufferSize(8);
		options.setHashTableSize(7);
		options.setStateSpaceReduction(StateSpaceReduction.CONSERVATIVE);
		Command cmd = new VerifyTACommand(options);
		cmd.addParameter(new PathArgument<VerifyTACommand>(modelPath));
		cmd.addParameter(new PathArgument<VerifyTACommand>(queryPath));

		try {
			Writer stringWriter = new FileWriter(outputFile);
			
			Process proc = new Process(cmd, stringWriter);
			
			System.out.print("Verifying using UPPAAL...");

			int exitcode = proc.waitForExitValue();
			
			System.out.println("\tDONE");
	
			
			stringWriter.flush();
			stringWriter.close();
			
			
			System.out.print("Register the metamodel...");
			Injector injector = new DiagnosticTraceStandaloneSetup().createInjectorAndDoEMFRegistration();
			XtextResourceSet resset = injector.getInstance(XtextResourceSet.class);
			resset.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
			System.out.println("\tDONE");
			
			NTA nta = (NTA)transformationEngine.uppaal.model().allContents().toArray()[0];

			synchronized (DiagnosticTraceScopeProviderSingleton.getScopeProvider()) {

				DiagnosticTraceScopeProviderSingleton.getScopeProvider().setNTA(nta);
//
				Resource resource = resset.getResource(
						URI.createFileURI("C:/Users/Egbert/Documents/test_ctte_uppaal.trace"), true);
//				
				Diagnostic resourceDiagnostic = EcoreUtil.computeDiagnostic(resource, false);
				
				if (!BasicDiagnostic.toIStatus(resourceDiagnostic).isOK()) {
					BasicDiagnostic parseDiagnostic = new BasicDiagnostic("org.muml.uppaal.job",
							resourceDiagnostic.getCode(), "Parsing the UPPAAL diagnostic trace failed", null);
					parseDiagnostic.merge(resourceDiagnostic);
	
					throw new CoreException(BasicDiagnostic.toIStatus(parseDiagnostic));
				}
	
				assert !resource.getContents().isEmpty()
				&& resource.getContents().get(0) instanceof TraceRepository;
				
				System.out.print("Start loading the model...");
				
				EmfModel emfModel = new EmfModel();
//				emfModel.setName("Trace");
//				emfModel.setMetamodelUri(TracePackage.eINSTANCE.getNsURI());
//				emfModel.setModelFile("C:/Users/Egbert/Documents/test_ctte_uppaal.trace");
//				emfModel.setReadOnLoad(true);
//				emfModel.load();
				emfModel.setResource(resource);
				
				System.out.println("\tDONE");
				
				
				traceRepository = (TraceRepository) emfModel.allContents().iterator().next();
				
				for (Trace trace : traceRepository.getTraces()) {
					System.out.println("Trace: " + trace.getProperty());
					System.out.println(trace.getResult());
					
					for (TraceItem traceItem : trace.getTraceItems()) {
						if(traceItem instanceof State) {
							State state = (State) traceItem;
							System.out.println("State:");
							
							for (LocationActivity locationActivity : state.getLocationActivities()) {
								ProcessIdentifier process = locationActivity.getProcess();								
								Location location = locationActivity.getLocation();
								System.out.println("\t" + process.getTemplate().getName() + "." + location.getName());
							}
						}
					}
				}
				
			}
		

//			Map<String, Boolean> opt = new HashMap<String, Boolean>();
//			opt.put(XtextResource.OPTION_RESOLVE_ALL, true);
//
//			NTA nta = (NTA)transformationEngine.uppaal.model().allContents().toArray()[0];
//
//			synchronized (DiagnosticTraceScopeProviderSingleton.getScopeProvider()) {
//
//				DiagnosticTraceScopeProviderSingleton.getScopeProvider().setNTA(nta);
//
//				resource.load(new StringInputStream(result), opt);
//			}
//
//			Diagnostic resourceDiagnostic = EcoreUtil.computeDiagnostic(resource, false);
//
//			if (!BasicDiagnostic.toIStatus(resourceDiagnostic).isOK()) {
//				BasicDiagnostic parseDiagnostic = new BasicDiagnostic("org.muml.uppaal.job",
//						resourceDiagnostic.getCode(), "Parsing the UPPAAL diagnostic trace failed", null);
//				parseDiagnostic.merge(resourceDiagnostic);
//
//				throw new CoreException(BasicDiagnostic.toIStatus(parseDiagnostic));
//			}
//
//			assert !resource.getContents().isEmpty()
//			&& resource.getContents().get(0) instanceof TraceRepository;




			//		PropertyRepository pr = RequirementsFactory.eINSTANCE.createPropertyRepository();
			//		UnaryProperty up = RequirementsFactory.eINSTANCE.createUnaryProperty();
			//		up.setQuantifier(PathQuantifier.EXISTS);
			//		up.setOperator(TemporalOperator.FUTURE);
			//		CompareExpression exp = ExpressionsFactory.eINSTANCE.createCompareExpression();
			//		
			//		LiteralExpression expTopLevelDone = ExpressionsFactory.eINSTANCE.createLiteralExpression();
			//		expTopLevelDone.setText("top_level.Done");
			//
			//		LiteralExpression expTrue = ExpressionsFactory.eINSTANCE.createLiteralExpression();
			//		expTrue.setText("true");
			//		
			//		exp.setFirstExpr(expTopLevelDone);
			//		exp.setOperator(CompareOperator.EQUAL);
			//		exp.setSecondExpr(expTrue);
			//		
			//		up.setExpression(exp);
			//		
			//		pr.getProperties().add(up);
			//		
			//		CoordinationProtocolOptions options = OptionsFactory.eINSTANCE.createCoordinationProtocolOptions();
			//		options.setTraceOptions(TraceOptions.FASTEST);
			//		options.setConnectorOutBufferSize(8);
			//		options.setHashTableSize(7);
			//		options.setStateSpaceReduction(StateSpaceReduction.CONSERVATIVE);

		} catch (Exception e) {
			e.printStackTrace();
		}
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


}
