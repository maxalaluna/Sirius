package net.floodlightcontroller.sirius.embedding.pt.lasige.secdepvne.Embeddor;

import java.io.IOException;
import net.floodlightcontroller.sirius.util.Utils;

/**
 * Entity responsible for solving the embedding of a VN to a SN
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class EmbeddorMILP {
	
	private static String MOD_FILE = "/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/secdepvne/topology/SecDep_MIP.mod";
	private static int TIMEOUT = 15000;
	private ConfigsReader cr;
	private DatFileCreator dfc;
	private OutputReader or;
	
	/**
	 * Constructor for the embeddor
	 * Create instances of ConfigsReader and DatFileCreator
	 */
	public EmbeddorMILP() {
		cr = new ConfigsReader();
		dfc = new DatFileCreator(cr);
		or = new OutputReader(cr);
	}
	
	/**
	 * Read the configuration file given by the virtualizer where is all the info about the VN and the SN
	 * and solves the embedding
	 * @param configFile The configuration file with all the information
	 * @return true if request was accepted; false if request was denied or the embedding reached the timeout
	 */
	public boolean solve(String configFile) {
		
		try {
			cr.readConfigFile(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		configFile = Utils.fileNameWithoutExt(configFile);
		
		try {
			dfc.createDatFile(configFile+".dat");
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Execute the model
		boolean res = Utils.runGLPSOL(configFile+".dat", MOD_FILE, configFile+".out", TIMEOUT);
		
		// If not timeout check if request was accepted or not
		if(res){
			try {
				res = or.wasAccepted(configFile+".out");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// If not timeout and accepted created the simplified file with the info of the embedding
		if(res){
			try {
				or.generateSimplifiedOutput(configFile+".out", configFile+".smplout");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	/**
	 * Checks if a request was accepted.
	 * @param configFile the file with the networks info (basically the request to embed)
	 * @return true if the request was accepted, false otherwise
	 */
	public boolean requestWasAccepted(String configFile) {
		configFile = Utils.fileNameWithoutExt(configFile);
		
		boolean res = false;
		try {
			res = or.wasAccepted(configFile+".out");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}
	
}
