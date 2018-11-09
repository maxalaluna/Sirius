package net.floodlightcontroller.sirius.embedding.pt.lasige.secdepvne.Embeddor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class responsible for reading the information about the SN and the VNR given by the virtualizer
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class ConfigsReader {

	// Data structures for substrate nodes info
	private ArrayList<String> sNodesID;
	private ArrayList<String> sNodesCPU;
	private ArrayList<String> sNodesSec;
	private ArrayList<String> sNodesLoc;

	// Data structures for substrate links info
	private ArrayList<String> sLinks;
	private ArrayList<String> sLinksBw;
	private ArrayList<String> sLinksSec;
	private ArrayList<String> sLinksWeight;

	// Data structures for clouds info
	private ArrayList<String> cloudsID;
	private ArrayList<String> cloudsSec;

	// Data structures for virtual nodes info
	private ArrayList<String> vNodesID;
	private ArrayList<String> vNodesCPUDem;
	private ArrayList<String> vNodesSecDem;
	private ArrayList<String> vNodesLocDem;
	private ArrayList<String> vNodesBackupLocDem;

	// Data structures for virtual links info
	private ArrayList<String> vLinks;
	private ArrayList<String> vLinksBwDem;
	private ArrayList<String> vLinksSecDem;
	
	private int nSNodes, nSLinks, nVNodes, nVLinks, nClouds;
	
	private int wantBackup;

	/**
	 * Initializes all the structures to store the info given by the virtualizer
	 */
	public ConfigsReader() {

		sNodesID = new ArrayList<>();
		sNodesCPU = new ArrayList<>();
		sNodesSec = new ArrayList<>();
		sNodesLoc = new ArrayList<>();
		sLinks = new ArrayList<>();
		sLinksBw = new ArrayList<>();
		sLinksSec = new ArrayList<>();
		sLinksWeight = new ArrayList<>();
		cloudsID = new ArrayList<>();
		cloudsSec = new ArrayList<>();
		vNodesID = new ArrayList<>();
		vNodesCPUDem = new ArrayList<>();
		vNodesSecDem = new ArrayList<>();
		vNodesLocDem = new ArrayList<>();
		vNodesBackupLocDem = new ArrayList<>();
		vLinks = new ArrayList<>();
		vLinksBwDem = new ArrayList<>();
		vLinksSecDem = new ArrayList<>();
		wantBackup = 0;
	}

	/**
	 * Read all the information present in configFile
	 * @param configFile The configurationFile with the current state of the SN and with the information demanded by the VN given by the virtualizer
	 * @throws IOException
	 */
	public void readConfigFile(String configFile) throws IOException {
		
		clearStructures(); // Clean the info stored in the structures
		
		String line;
		String[] parts;

		FileReader fileReader = new FileReader(configFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		bufferedReader.readLine(); //Read Substrate word
		bufferedReader.readLine(); //Read line

		// Read number of SNodes
		line = bufferedReader.readLine();
		parts = line.split(" ");
		nSNodes = Integer.parseInt(parts[1]);

		// Read number of SLinks
		line = bufferedReader.readLine();
		parts = line.split(" ");
		nSLinks= Integer.parseInt(parts[1]);

		// Read number of clouds
		line = bufferedReader.readLine();
		parts = line.split(" ");
		nClouds= Integer.parseInt(parts[1]);

		// Read SNodes IDs
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nSNodes; i++)
			sNodesID.add(parts[i+1]);

		// Read SNodes CPU
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nSNodes; i++)
			sNodesCPU.add(parts[i+1]);

		// Read SNodes Sec
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nSNodes; i++)
			sNodesSec.add(parts[i+1]);

		// Read SNodes Loc
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nSNodes; i++)
			sNodesLoc.add(parts[i+1]);

		// Read SLinks
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nSLinks; i++)
			sLinks.add(parts[i+1]);

		// Read SLinks Bw
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nSLinks; i++)
			sLinksBw.add(parts[i+1]);

		// Read SLinks Sec
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nSLinks; i++)
			sLinksSec.add(parts[i+1]);

		// Read SLinks Weight
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nSLinks; i++)
			sLinksWeight.add(parts[i+1]);

		// Read Clouds IDs
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nClouds; i++)
			cloudsID.add(parts[i+1]);

		// Read Clouds Sec
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nClouds; i++)
			cloudsSec.add(parts[i+1]);

		bufferedReader.readLine(); //Read line ###
		bufferedReader.readLine(); //Read Virtual word
		bufferedReader.readLine(); //Read line

		// Read number of VNodes
		line = bufferedReader.readLine();
		parts = line.split(" ");
		nVNodes = Integer.parseInt(parts[1]);

		// Read number of VLinks
		line = bufferedReader.readLine();
		parts = line.split(" ");
		nVLinks= Integer.parseInt(parts[1]);

		// Read VNodes IDs
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nVNodes; i++)
			vNodesID.add(parts[i+1]);

		// Read VNodes CPU dems
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nVNodes; i++)
			vNodesCPUDem.add(parts[i+1]);

		// Read VNodes Sec dems
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nVNodes; i++)
			vNodesSecDem.add(parts[i+1]);

		// Read VNodes Loc dems
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nVNodes; i++)
			vNodesLocDem.add(parts[i+1]);

		// Read VNodes backup Loc dems
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nVNodes; i++)
			vNodesBackupLocDem.add(parts[i+1]);

		// Read VLinks
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nVLinks; i++)
			vLinks.add(parts[i+1]);

		// Read VLinks Bw dems
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nVLinks; i++)
			vLinksBwDem.add(parts[i+1]);

		// Read VLinks Sec dems
		line = bufferedReader.readLine();
		parts = line.split(" ");
		for(int i = 0; i < nVLinks; i++)
			vLinksSecDem.add(parts[i+1]);
		
		//Read if client wants backup
		line = bufferedReader.readLine();
		parts = line.split(" ");
		wantBackup = Integer.parseInt(parts[1]);

		bufferedReader.close();

	}
	
	/**
	 * Cleans all the information that can be in the structures
	 */
	public void clearStructures() {
		nSNodes = 0;
		nSLinks = 0;
		nVNodes = 0;
		nVLinks = 0;
		nClouds = 0;
		sNodesID.clear();
		sNodesCPU.clear();
		sNodesSec.clear();
		sNodesLoc.clear();
		sLinks.clear();
		sLinksBw.clear();
		sLinksSec.clear();
		sLinksWeight.clear();
		cloudsID.clear();
		cloudsSec.clear();
		vNodesID.clear();
		vNodesCPUDem.clear();
		vNodesSecDem.clear();
		vNodesLocDem.clear();
		vNodesBackupLocDem.clear();
		vLinks.clear();
		vLinksBwDem.clear();
		vLinksBwDem.clear();
	}

	public ArrayList<String> getsNodesID() {
		return sNodesID;
	}
	
	public String getsNodeID(int index) {
		return sNodesID.get(index);
	}

	public void setsNodesID(ArrayList<String> sNodesID) {
		this.sNodesID = sNodesID;
	}

	public ArrayList<String> getsNodesCPU() {
		return sNodesCPU;
	}

	public String getsNodeCPU(int index) {
		return sNodesCPU.get(index);
	}
	
	public void setsNodesCPU(ArrayList<String> sNodesCPU) {
		this.sNodesCPU = sNodesCPU;
	}

	public ArrayList<String> getsNodesSec() {
		return sNodesSec;
	}
	
	public String getsNodeSec(int index) {
		return sNodesSec.get(index);
	}

	public void setsNodesSec(ArrayList<String> sNodesSec) {
		this.sNodesSec = sNodesSec;
	}

	public ArrayList<String> getsNodesLoc() {
		return sNodesLoc;
	}
	
	public String getsNodeLoc(int index) {
		return sNodesLoc.get(index);
	}

	public void setsNodesLoc(ArrayList<String> sNodesLoc) {
		this.sNodesLoc = sNodesLoc;
	}

	public ArrayList<String> getsLinks() {
		return sLinks;
	}
	
	public String getsLink(int index) {
		return sLinks.get(index);
	}

	public void setsLinks(ArrayList<String> sLinks) {
		this.sLinks = sLinks;
	}

	public ArrayList<String> getsLinksBw() {
		return sLinksBw;
	}
	
	public String getsLinkBw(int index) {
		return sLinksBw.get(index);
	}

	public void setsLinksBw(ArrayList<String> sLinksBw) {
		this.sLinksBw = sLinksBw;
	}

	public ArrayList<String> getsLinksSec() {
		return sLinksSec;
	}
	
	public String getsLinkSec(int index) {
		return sLinksSec.get(index);
	}

	public void setsLinksSec(ArrayList<String> sLinksSec) {
		this.sLinksSec = sLinksSec;
	}

	public ArrayList<String> getsLinksWeight() {
		return sLinksWeight;
	}
	
	public String getsLinkWeight(int index) {
		return sLinksWeight.get(index);
	}

	public void setsLinksWeight(ArrayList<String> sLinksWeight) {
		this.sLinksWeight = sLinksWeight;
	}

	public ArrayList<String> getCloudsID() {
		return cloudsID;
	}
	
	public String getCloudID(int index) {
		return cloudsID.get(index);
	}

	public void setCloudsID(ArrayList<String> cloudsID) {
		this.cloudsID = cloudsID;
	}

	public ArrayList<String> getCloudsSec() {
		return cloudsSec;
	}
	
	public String getCloudSec(int index) {
		return cloudsSec.get(index);
	}

	public void setCloudsSec(ArrayList<String> cloudsSec) {
		this.cloudsSec = cloudsSec;
	}

	public ArrayList<String> getvNodesID() {
		return vNodesID;
	}
	
	public String getvNodeID(int index) {
		return vNodesID.get(index);
	}

	public void setvNodesID(ArrayList<String> vNodesID) {
		this.vNodesID = vNodesID;
	}

	public ArrayList<String> getvNodesCPUDem() {
		return vNodesCPUDem;
	}
	
	public String getvNodeCPUDem(int index) {
		return vNodesCPUDem.get(index);
	}

	public void setvNodesCPUDem(ArrayList<String> vNodesCPUDem) {
		this.vNodesCPUDem = vNodesCPUDem;
	}

	public ArrayList<String> getvNodesSecDem() {
		return vNodesSecDem;
	}
	
	public String getvNodeSecDem(int index) {
		return vNodesSecDem.get(index);
	}

	public void setvNodesSecDem(ArrayList<String> vNodesSecDem) {
		this.vNodesSecDem = vNodesSecDem;
	}

	public ArrayList<String> getvNodesLocDem() {
		return vNodesLocDem;
	}
	
	public String getvNodeLocDem(int index) {
		return vNodesLocDem.get(index);
	}

	public void setvNodesLocDem(ArrayList<String> vNodesLocDem) {
		this.vNodesLocDem = vNodesLocDem;
	}

	public ArrayList<String> getvNodesBackupLocDem() {
		return vNodesBackupLocDem;
	}
	
	public String getvNodeBackupLocDem(int index) {
		return vNodesBackupLocDem.get(index);
	}

	public void setvNodesBackupLocDem(ArrayList<String> vNodesBackupLocDem) {
		this.vNodesBackupLocDem = vNodesBackupLocDem;
	}

	public ArrayList<String> getvLinks() {
		return vLinks;
	}
	
	public String getvLink(int index) {
		return vLinks.get(index);
	}

	public void setvLinks(ArrayList<String> vLinks) {
		this.vLinks = vLinks;
	}

	public ArrayList<String> getvLinksBwDem() {
		return vLinksBwDem;
	}
	
	public String getvLinkBwDem(int index) {
		return vLinksBwDem.get(index);
	}

	public void setvLinksBwDem(ArrayList<String> vLinksBwDem) {
		this.vLinksBwDem = vLinksBwDem;
	}

	public ArrayList<String> getvLinksSecDem() {
		return vLinksSecDem;
	}
	
	public String getvLinkSecDem(int index) {
		return vLinksSecDem.get(index);
	}

	public void setvLinksSecDem(ArrayList<String> vLinksSecDem) {
		this.vLinksSecDem = vLinksSecDem;
	}

	public int getnSNodes() {
		return nSNodes;
	}

	public void setnSNodes(int nSNodes) {
		this.nSNodes = nSNodes;
	}

	public int getnSLinks() {
		return nSLinks;
	}

	public void setnSLinks(int nSLinks) {
		this.nSLinks = nSLinks;
	}

	public int getnVNodes() {
		return nVNodes;
	}

	public void setnVNodes(int nVNodes) {
		this.nVNodes = nVNodes;
	}

	public int getnVLinks() {
		return nVLinks;
	}

	public void setnVLinks(int nVLinks) {
		this.nVLinks = nVLinks;
	}

	public int getnClouds() {
		return nClouds;
	}

	public void setnClouds(int nClouds) {
		this.nClouds = nClouds;
	}
	
	public int getWantBackup() {
		return wantBackup;
	}

	/**
	 * Check if substrate node nodeID is located in the cloud cloudID
	 * @param nodeID The ID of the node that we want to check
	 * @param cloudID The ID of the cloud
	 * @return 1 if node nodeID is located at cloud cloudID, 0 otherwise
	 */
	public int isInCloud(String nodeID, String cloudID) {
		return cloudID.equals(sNodesLoc.get(sNodesID.indexOf(nodeID))) ? 1 : 0;
	}

}
