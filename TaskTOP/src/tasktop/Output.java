package tasktop;

public class Output extends ModelResource {
	
	public Output(String loc, Language lang) {
		super(loc, lang, Role.TARGET);
	}

	public boolean store() {
		if(model == null) return false;
		return model.store("file:" + location);
	}
}
