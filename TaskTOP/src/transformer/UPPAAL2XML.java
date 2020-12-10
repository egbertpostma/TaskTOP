package transformer;

import java.io.FileWriter;
import java.io.IOException;

import org.muml.uppaal.NTA;
import org.muml.uppaal.serialization.UppaalSerialization;

import tasktop.ModelResource;

public class UPPAAL2XML extends BaseTransformer {
	
	public UPPAAL2XML() {
		super(false); // Initialize transformer without the need for external transformation file.
	}
	
	@Override
	public boolean execute(ModelResource i, ModelResource o) throws Exception {
		
		Object object = i.model().allContents().toArray()[0];
		if(object instanceof NTA) {
			NTA nta = (NTA)object;
			UppaalSerialization s = new UppaalSerialization();
			CharSequence xml = s.main(nta);
			try {
				FileWriter oFile = new FileWriter(o.location());
				oFile.write(xml.toString());
				oFile.close();
			} catch (IOException e) {
				System.err.println("Failed to write file to '" + o.location() +"'");
				System.err.println(e.getMessage());
			}
		}
				
		return true;
	}

}
