package tasktop;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Temporary extends ModelResource {
	
	public Temporary(Language lang) {
		super(lang, Role.TARGET);
		
		File f;
		try {
			f = File.createTempFile(UUID.randomUUID().toString(), "." + lang.getName());
			f.deleteOnExit();
			setLocation(f.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
