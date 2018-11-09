package net.floodlightcontroller.sirius.embedding.pt.lasige.secdepvne.Main;

import net.floodlightcontroller.sirius.embedding.pt.lasige.secdepvne.Embeddor.EmbeddorMILP;

public class Main {

	public static void main(String[] args) {
		
		EmbeddorMILP embeddor = new EmbeddorMILP();
		
		//embeddor.solve("/home/max/testeDir/embedding/netsConfigurations/input2.config");
		embeddor.solve("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/secdepvne/topology/result/virtualNetwork8_embedding.config");
		
	}
	
}
