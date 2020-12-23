package tasktop;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.muml.core.common.cmd.Command;
import org.muml.core.common.cmd.PathArgument;
import org.muml.core.common.cmd.PathCommand;
import org.muml.core.common.cmd.Process;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.emc.plainxml.StringInputStream;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
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
import org.muml.uppaal.trace.DiagnosticTraceStandaloneSetup;
import org.muml.uppaal.trace.TraceRepository;
import org.muml.uppaal.trace.scoping.DiagnosticTraceScopeProviderSingleton;
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
	
	private final static String UPPAAL_CMDL_EXEC = "/home/egbert/dev-tools/uppaal-4.1.19/bin-Linux/verifyta";

	public QueryEngine(TransformationEngine te) {
		this.transformationEngine = te;
	}
	
	private class VerifyTACommand extends PathCommand {
		public VerifyTACommand() {
			super(Path.fromOSString(UPPAAL_CMDL_EXEC));
		}
	}

	private Command createCommand(Options options) {
		Command cmd = new VerifyTACommand();
		cmd.addParameter(new NoOptionSummaryOption());
		cmd.addParameter(new NoProgressIndicatorOption());
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
			cmd.addParameter(new DiagnosticInfoOption(traceKind));

		cmd.addParameter(new HashTableSizeOption(options.getHashTableSize()));

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
			cmd.addParameter(new SpaceConsumptionOption(spaceConsumptionOperator));

		cmd.addParameter(new ReuseStateSpaceOption());

		return cmd;
	}


	private TraceRepository traceRepository;

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

		File outputFile = new File("/home/egbert/Documents/dummy.trace");

		try {
			if(!outputFile.createNewFile()) {
				outputFile.delete();
				outputFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		IPath modelPath = Path.fromOSString(transformationEngine.getOutputFile());
		IPath queryPath = Path.fromOSString(queryFile.getAbsolutePath());

		CoordinationProtocolOptions options = OptionsFactory.eINSTANCE.createCoordinationProtocolOptions();
		options.setTraceOptions(TraceOptions.FASTEST);
		options.setConnectorOutBufferSize(8);
		options.setHashTableSize(7);
		options.setStateSpaceReduction(StateSpaceReduction.CONSERVATIVE);
		Command cmd = createCommand(options);
		cmd.addParameter(new PathArgument<VerifyTACommand>(modelPath));
		cmd.addParameter(new PathArgument<VerifyTACommand>(queryPath));

		try {
			Writer stringWriter = new FileWriter(outputFile);
			Writer printWriter = new PrintWriter(System.out, true);
			Writer progressWriter = new PrintWriter(System.out, true);
			
			Process proc = new Process(cmd, printWriter, stringWriter, progressWriter);

			int exitcode = proc.waitForExitValue();
			
			if(exitcode != 0) {
				throw new Exception();
			}
			
			stringWriter.flush();
			stringWriter.close();

			Injector injector = new DiagnosticTraceStandaloneSetup().createInjectorAndDoEMFRegistration();
			XtextResourceSet resset = injector.getInstance(XtextResourceSet.class);
			resset.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
			
			NTA nta = (NTA)transformationEngine.uppaal.model().allContents().toArray()[0];

			synchronized (DiagnosticTraceScopeProviderSingleton.getScopeProvider()) {

				DiagnosticTraceScopeProviderSingleton.getScopeProvider().setNTA(nta);

				Resource resource = resset.getResource(
						URI.createURI(outputFile.getAbsolutePath()), true);
				
				Diagnostic resourceDiagnostic = EcoreUtil.computeDiagnostic(resource, false);
				
				if (!BasicDiagnostic.toIStatus(resourceDiagnostic).isOK()) {
					BasicDiagnostic parseDiagnostic = new BasicDiagnostic("org.muml.uppaal.job",
							resourceDiagnostic.getCode(), "Parsing the UPPAAL diagnostic trace failed", null);
					parseDiagnostic.merge(resourceDiagnostic);
	
					throw new CoreException(BasicDiagnostic.toIStatus(parseDiagnostic));
				}
	
				assert !resource.getContents().isEmpty()
				&& resource.getContents().get(0) instanceof TraceRepository;
				
				traceRepository = (TraceRepository) resource.getContents().get(0);
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
