package net.floodlightcontroller.sirius.embedding.pt.lasige.secdepvne.Main;

import net.floodlightcontroller.sirius.embedding.pt.lasige.secdepvne.Embeddor.EmbeddorMILP;

public class Main2 {

	public static void main(String[] args) {
		
		EmbeddorMILP embeddor = new EmbeddorMILP();
		
		embeddor.solve("../netsConfigurations/input2.config");
		
	}
	
}
