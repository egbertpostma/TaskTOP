package tasktop;

import transformer.CTT2UPPAAL;
import transformer.CTTXML2CTT;
import transformer.UPPAAL2XML;

public class GUIMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ModelResource cttxml = new Input("/home/egbert/Documents/test.xml", Language.CTT_XML);
		ModelResource ctt = new Output("/home/egbert/Documents/test.ctt", Language.CTT);
		ModelResource uppaal = new Output("/home/egbert/Documents/test.uppaal", Language.UPPAAL);
		ModelResource uppaalxml = new Output("/home/egbert/Documents/test_uppaal.xml", Language.UPPAAL_XML);

		CTTXML2CTT test = new CTTXML2CTT();
		CTT2UPPAAL c2u = new CTT2UPPAAL();
		UPPAAL2XML u2x = new UPPAAL2XML();

		try {
			boolean success = true;

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

	}

}
