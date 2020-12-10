package cttconverter;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.emf.ecore.EPackage;

import ctt.CttPackage;
import org.muml.uppaal.UppaalPackage;

public enum Language {
	CTT_XML("CTTXML","", "validations/CTTXML.evl"),
	CTT("CTT", CttPackage.eINSTANCE, "validations/CTT.evl"),
	
	/* Uppaal formats */
	UPPAAL_XML("UppaalXML"),
	UPPAAL("Uppaal", UppaalPackage.eINSTANCE),
	/** Command-line options to the verifyta UPPAAL tool */
	UPPAAL_OPTIONS("UppaalOptions"),
	/** UPPAAL input query language (i.e., the content of .q files) */
	UPPAAL_TEXT_QUERY("UppaalTextQuery"),
	/** Textual output from verifyta */
	UPPAAL_TEXT_RESULT("UppaalTextResult"),
	UPPAAL_PLOT_RESULT("Uppaal plot"),
	;
	
	private final String name;
	private final String location;
	public final EPackage pkg;
	public final String validator;
	
	/** Constructs a language without location or validator. */
	private Language(String name) {
		this(name, (String)null, null);
	}

	/** Constructs a language without validator. */
	private Language(String name, String location) {
		this(name, location, null);
	}

	private Language(String name, EPackage pkg) {
		this(name, pkg, null);
	}
	
	private Language(String name, EPackage pkg, String validator) {
		this.name = name;
		this.validator = validator;
		this.pkg = pkg;
		EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
		this.location = null;
	}
	
	/** Constructs a language with possibly null location. */
	private Language(String name, String location, String validator) {
		this.name = name;
		this.location = location;
		this.validator = validator;
		this.pkg = null;
	}
	
	/** Returns the name of this language. */
	public String getName() {
		return name;
	}
	
	/** Returns the (possibly <code>null</code>) location of the language definition. */
	public String getLocation() {
		return location;
	}
	
	private static final Map<String,Language> languageMap;
	
	static {
		languageMap = new TreeMap<>();
		for (Language l: Language.values()) {
			languageMap.put(l.getName(), l);
		}
	}
	
	/** Returns the language with a given name. */
	public static Language getLanguage(String name) {
		return languageMap.get(name);
	}
	
	/** Returns the map from language names to languages. */
	public static Map<String,Language> getLanguageMap() {
		return Collections.unmodifiableMap(languageMap);
	}
}
