package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Utils;

/**
 * Handles total information of a virtual network
 * @author Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class VirtualNetworkHeu {

	private int duration;
	private int arrival;
	private ArrayList<String> nodes;
	private ArrayList<Double> nodesCPU;
	private ArrayList<Double> nodesSec;
	private ArrayList<Pair<String>> edges;
	private ArrayList<Double> edgesBw;
	private ArrayList<Double> edgesSec;
	private ArrayList<Double> cloudsSecurity;
	private ArrayList<Integer> backupLocalization;
	private boolean wantBackup;
	private ArrayList<Double> edgesLatency;
	private ArrayList<Boolean> wantBackupNode;
	private HashMap<Integer,HashMap<Integer, List<String>>> pahtsForVirtualLinks = new HashMap<Integer,HashMap<Integer, List<String>>>();
	private double utilityNetwork = -1;
	private ArrayList<Boolean> demandsVirtualHosts;

	/**
	 * Creates a virtual network without any attribute
	 * @param duration Lifetime of the virtual network in the system
	 */
	public VirtualNetworkHeu(int duration) {

		this.duration = duration;
		this.arrival = 0;
		this.nodes = new ArrayList<>();
		this.nodesCPU = new ArrayList<>();
		this.nodesSec = new ArrayList<>();
		this.edges = new ArrayList<>();
		this.edgesBw = new ArrayList<>();
		this.edgesSec = new ArrayList<>();
		this.cloudsSecurity = new ArrayList<>();
		this.backupLocalization = new ArrayList<>();
		this.wantBackup = false;
		this.edgesLatency = new ArrayList<>();
		this.wantBackupNode = new ArrayList<>();
		this.demandsVirtualHosts = new ArrayList<>();
		
//		for(int i = 0; i < nodes.size(); i++)
//			demandsVirtualHosts.add(true);
	}

	/**
	 * Creates a copy of virtual network virNet
	 * @param virNet The virtual network that will be copied
	 */
	public VirtualNetworkHeu(VirtualNetworkHeu virNet) {

		this.duration = virNet.getDuration();
		this.arrival = virNet.getArrival();
		this.nodes = virNet.getNodes();
		this.nodesCPU = virNet.getNodesCPU();
		this.nodesSec = virNet.getNodesSec();
		this.edges = virNet.getEdges();
		this.edgesBw = virNet.getEdgesBw();
		this.edgesSec = virNet.getEdgesSec();
		this.cloudsSecurity = virNet.getCloudsSecurity(); 
		this.backupLocalization = virNet.getBackupsLocalization();
		this.wantBackup = virNet.getWantBackup();
		this.edgesLatency = virNet.getEdgesLatency();
		this.wantBackupNode = virNet.getWantBackupNode();
		this.demandsVirtualHosts = virNet.getDemandsVirtualHosts();
	}

	public void addDemandsVirtualHosts(boolean demand) {
		demandsVirtualHosts.add(demand);
	}
	public ArrayList<Boolean> getDemandsVirtualHosts() {
		return demandsVirtualHosts;
	}

	public void setDemandsVirtualHosts(ArrayList<Boolean> demandsVirtualHosts) {
		this.demandsVirtualHosts = demandsVirtualHosts;
	}

	public void addNode(String node) {
		nodes.add(node);
	}

	public ArrayList<String> getNodes() {
		return nodes;
	}

	public String getNode(int index) {
		return nodes.get(index);
	}

	public int getNumOfNodes() {
		return nodes.size();
	}

	public void addNodeCPU(double cpu) {
		nodesCPU.add(cpu);
	}

	public ArrayList<Double> getNodesCPU() {
		return nodesCPU;
	}

	public double getNodeCPU(int index) {
		return nodesCPU.get(index);
	}

	public void addNodeSec(double sec) {
		nodesSec.add(sec);
	}

	public ArrayList<Double> getNodesSec() {
		return nodesSec;
	}

	public double getNodeSec(int index) {
		return nodesSec.get(index);
	}

	public void addEdge(Pair<String> pair) {
		edges.add(pair);
	}

	public ArrayList<Pair<String>> getEdges() {
		return edges;
	}

	public Pair<String> getEdge(int index) {
		return edges.get(index);
	}

	public int getNumOfEdges() {
		return edges.size();
	}

	public void addEdgeBw(double bw) {
		edgesBw.add(bw);
	}

	public void addEdgeLatency(double latency) {
		edgesLatency.add(latency);
	}
	
	public ArrayList<Double> getEdgesBw() {
		return edgesBw;
	}

	public ArrayList<Double> getEdgesLatency() {
		return edgesLatency;
	}
	
	public double getEdgeBw(int index) {
		return edgesBw.get(index);
	}
	
	public double getEdgeLatency(int index) {
		return edgesLatency.get(index);
	}

	public void addEdgeSec(double sec) {
		edgesSec.add(sec);
	}

	public ArrayList<Double> getEdgesSec() {
		return edgesSec;
	}

	public double getEdgeSec(int index) {
		return edgesSec.get(index);
	}

	public void addCloudSecurity(double cloudSecurity) {
		cloudsSecurity.add(cloudSecurity);
	}

	public ArrayList<Double> getCloudsSecurity() {
		return cloudsSecurity;
	}

	public double getCloudSecurity(int index) {
		return cloudsSecurity.get(index);
	}

	public void addBackupLocalization(int desiredLoc) {
		backupLocalization.add(desiredLoc);
	}
	
	public void setBackupLocalization(ArrayList<Integer> backupLocalization) {
		this.backupLocalization = backupLocalization;
	}

	public ArrayList<Integer> getBackupsLocalization() {
		return backupLocalization;
	}

	public int getBackupLocalization(int index) {
		try {
			return backupLocalization.get(index);
		}
		catch(Exception exception) {
		   System.out.println("Exception.");
		}
		return backupLocalization.get(index);
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

	public void setArrival(int arrivalInstant) {
		arrival = arrivalInstant;
	}

	public void setWantBackup(boolean wantBackup) {
		this.wantBackup = wantBackup;
	}
	
	public boolean getWantBackup() {
		return wantBackup;
	}

	public int getArrival() {
		return arrival;
	}

	/**
	 * Returns the revenue generated for the InP if the virtual network is accepted
	 * @return The revenue value
	 */
	public double getRevenue() {
		double nodesRev = 0;
		double edgesRev = 0;
		
		for(int i = 0; i < nodes.size(); i++){
			int factorBkp = ((wantBackupNode.get(i))?2:1);
			nodesRev += nodesCPU.get(i) * nodesSec.get(i) * cloudsSecurity.get(i) * factorBkp;
		}
			
		for(int i = 0; i < edges.size(); i++){
			double factorRev = getFactorRevenue(edges.get(i));
			edgesRev += edgesBw.get(i) * edgesSec.get(i) * factorRev;
		}
			
		//for(Double cpu: nodesCPU)
		//	nodesRev += cpu;
		//		
		//for(Double bw: edgesBw)
		//	edgesRev += bw;

		return nodesRev + edgesRev;
	}
	public double getFactorRevenue(Pair<String> edge) {
		int indexL = nodes.indexOf(edge.getLeft());
		int indexR = nodes.indexOf(edge.getRight());
		
		if(wantBackupNode.get(indexL)||wantBackupNode.get(indexR)){
			return 2.0;
		}else{
			return 1.0;
		}
	}
	/**
	 * Returns the revenue generated for the InP if the virtual network is accepted
	 * @return The revenue value
	 */
	public double getDvineAndFullGreedyHeuRevenue() {
		double nodesRev = 0;
		double edgesRev = 0;
		
		for(int i = 0; i < nodes.size(); i++)
			nodesRev += nodesCPU.get(i) * nodesSec.get(i) * cloudsSecurity.get(i);

		for(int i = 0; i < edges.size(); i++)
			edgesRev += edgesBw.get(i) * edgesSec.get(i);

		//for(Double cpu: nodesCPU)
		//	nodesRev += cpu;
		//		
		//for(Double bw: edgesBw)
		//	edgesRev += bw;

		return nodesRev + edgesRev;
	}
	
	public double getNoSecRevenue() {
		double nodesRev = 0;
		double edgesRev = 0;
		
		for(int i = 0; i < nodes.size(); i++)
			nodesRev += nodesCPU.get(i);

		for(int i = 0; i < edges.size(); i++)
			edgesRev += edgesBw.get(i);

		//for(Double cpu: nodesCPU)
		//	nodesRev += cpu;
		//		
		//for(Double bw: edgesBw)
		//	edgesRev += bw;

		return nodesRev + edgesRev;
	}
	
	public void setNodesCPU(ArrayList<Double> nodesCPU) {
		this.nodesCPU = nodesCPU;
	}

	public void setEdgesBw(ArrayList<Double> edgesBw){
		this.edgesBw = edgesBw;		
	}
	
	public void setEdgesLatency(ArrayList<Double> edgesLatency){
		this.edgesLatency = edgesLatency;		
	}

	/**
	 * Prints the info of the virtual network to a file
	 * @param staticFile The file to where the info will be printed
	 */
	public void printToFile(String staticFile) {

		try {

			FileWriter fileWriter = new FileWriter(staticFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(nodes.size()+"\n");
			bufferedWriter.write(edges.size()+"\n");

			bufferedWriter.write("NODES\n"); 
			for(String node: nodes)
				bufferedWriter.write(node+"\n");

			bufferedWriter.write("NODESCPU\n");
			for(Double cpu: nodesCPU)
				bufferedWriter.write(Double.valueOf(cpu)+"\n");

			bufferedWriter.write("NODESSEC\n");
			for(Double sec: nodesSec)
				bufferedWriter.write(Double.valueOf(sec)+"\n");

			bufferedWriter.write("EDGES\n");
			for(Pair<String> edge: edges)
				bufferedWriter.write(edge.getLeft()+" "+edge.getRight()+"\n");

			bufferedWriter.write("EDGESBW\n");
			for(Double bw: edgesBw)
				bufferedWriter.write(Double.valueOf(bw)+"\n");
			
			bufferedWriter.write("EDGESLATENCY\n");
			for(Double latency: edgesLatency)
				bufferedWriter.write(Double.valueOf(latency)+"\n");

			bufferedWriter.write("EDGESSEC\n");
			for(Double sec: edgesSec)
				bufferedWriter.write(Double.valueOf(sec)+"\n");

			bufferedWriter.write("NCLOUDS\n");
			bufferedWriter.write(cloudsSecurity.size()+"\n");

			bufferedWriter.write("CLOUDSSEC\n");
			for(Double sec: cloudsSecurity)
				bufferedWriter.write(sec+"\n");

			bufferedWriter.write("BACKUPLOCALIZATION\n");
			for(Integer loc: backupLocalization)
				bufferedWriter.write(loc+"\n");

			bufferedWriter.write("DURATION\n");
			bufferedWriter.write(Long.valueOf(duration)+"\n");

			bufferedWriter.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setNodesSec(ArrayList<Double> nodesSec) {
		this.nodesSec = nodesSec;
	}

	public void setEdgesSec(ArrayList<Double> edgesSec) {
		this.edgesSec = edgesSec;
	}

	public void setCloudsSec(ArrayList<Double> cloudsSec) {
		this.cloudsSecurity = cloudsSec;
	}

	public HashMap<Integer,HashMap<Integer, List<String>>> getPahtsForVirtualLinks() {
		return pahtsForVirtualLinks;
	}

	public void setPahtsForVirtualLinks(HashMap<Integer,HashMap<Integer, List<String>>> pahtsForVirtualLinks) {
		this.pahtsForVirtualLinks = pahtsForVirtualLinks;
	}

	public ArrayList<Boolean> getWantBackupNode() {
		return wantBackupNode;
	}

	public void setWantBackupNode(ArrayList<Boolean> wantBackupNode) {
		this.wantBackupNode = wantBackupNode;
	}
	
	public void addWantBackupNode(boolean wantBackupN) {
		wantBackupNode.add(wantBackupN);
	}

	public boolean getWantBackupNode(int index) {
		return wantBackupNode.get(index);
	}

	public double getUtilityNetwork() {
		
		if(utilityNetwork != -1)
			return utilityNetwork;
		else{
			this.utilityNetwork = this.calculateUtilityNetwork();
			return utilityNetwork;
		}
	}
	
	public double calculateUtilityNetwork(){
		
		double lambda = 1;
		double kn = 1;
		double auxUtilityNet = 0;
		double sumEdgesAux;
		int numberOfLinksAux;
		
		for (int i = 0; i < this.getNodes().size(); i++){
			sumEdgesAux = 0;
			numberOfLinksAux = 0;
			for(int j = 0; j < this.getNumOfEdges(); j++){
				if ((this.getNode(i).equals(this.getEdge(j).getLeft()))||(this.getNode(i).equals(this.getEdge(j).getRight()))){
					sumEdgesAux += this.getEdgeBw(j);
					numberOfLinksAux ++;
				}
			}
			auxUtilityNet += lambda*(this.getNodeCPU(i)*sumEdgesAux)*kn*(Math.log10(numberOfLinksAux) + 1)*
					this.getNodeSec(i)*(1/(100.0*numberOfLinksAux+100.0));
		}
		return auxUtilityNet;
	}
}
