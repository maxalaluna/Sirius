package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Utils;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;

/**
 * Handles total information of a substrate network
 * @author Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class SubstrateNetworkHeu {

	private ArrayList<String> nodes;
	private ArrayList<Double> totalNodesCPU;
	private ArrayList<Double> nodesCPU;
	private ArrayList<Double> nodesSec;
	private HashMap<String, Double> cloudSecSup;
	private ArrayList<Pair<String>> edges;
	private ArrayList<Double> totalEdgesBw;
	private ArrayList<Double> edgesBw;
	private ArrayList<Double> edgesSec;
	private ArrayList<Integer> edgesWeight;
	private int nClouds;
	private ArrayList<Double> cloudsSecurity;
	private int[][] doesItBelong;
	private ArrayList<Double> edgesLatency;
	private ArrayList<Double> totalEdgesLatency;
	private ArrayList<Boolean> acceptNodesEmbedding;
	private double utilityNetwork = -1;
	private int nPrivateDataCenters;
	private int nNodesInClouds;
	private int [][] distancesBetweenNodes;
	
	public int[][] getDistancesBetweenNodes() {
		return distancesBetweenNodes;
	}

	public void setDistancesBetweenNodes(int[][] distancesBetweenNodes) {
		this.distancesBetweenNodes = distancesBetweenNodes;
	}

	public int getnNodesInClouds() {
		return nNodesInClouds;
	}

	public void setnNodesInClouds(int nNodesInClouds) {
		this.nNodesInClouds = nNodesInClouds;
	}

	/**
	 * Create a substrate network without attributes set
	 * @param nClouds Number of clouds
	 */
	public SubstrateNetworkHeu(int nClouds) {

		this.nodes = new ArrayList<>();
		this.totalNodesCPU = new ArrayList<>();
		this.nodesCPU = new ArrayList<>();
		this.nodesSec = new ArrayList<>();
		this.cloudSecSup = new HashMap<>();
		this.edges = new ArrayList<>();
		this.totalEdgesBw = new ArrayList<>();
		this.edgesBw = new ArrayList<>();
		this.edgesSec = new ArrayList<>();
		this.edgesWeight = new ArrayList<>();
		this.nClouds = nClouds;
		this.cloudsSecurity = new ArrayList<>();
		this.edgesLatency = new ArrayList<>();
		this.totalEdgesLatency = new ArrayList<>();
		this.acceptNodesEmbedding = new ArrayList<>();
	}

	public int getnPrivateDataCenters() {
		return nPrivateDataCenters;
	}

	public void setnPrivateDataCenters(int nPrivateDataCenters) {
		this.nPrivateDataCenters = nPrivateDataCenters;
	}

	/**
	 * Create a substrate network without attributes set
	 * Behaves has a single datacenter (no clouds)
	 */
	public SubstrateNetworkHeu() {
		this(0);
	}

	/**
	 * Create a copy of a substrate network
	 * @param subNet The substrate network that will be copied
	 */
	public SubstrateNetworkHeu(SubstrateNetworkHeu subNet) {

		this.nodes = new ArrayList<>();
		this.totalNodesCPU = new ArrayList<>();
		this.nodesCPU = new ArrayList<>();
		this.nodesSec = new ArrayList<>();
		this.cloudSecSup = new HashMap<>();

		this.edges = new ArrayList<>();
		this.totalEdgesBw = new ArrayList<>();
		this.edgesBw = new ArrayList<>();
		this.edgesSec = new ArrayList<>();
		this.edgesWeight = new ArrayList<>();

		this.nClouds = subNet.getNClouds();
		this.cloudsSecurity = new ArrayList<>();
		this.edgesLatency = new ArrayList<>();

		for(int i = 0; i < subNet.getNumOfNodes(); i++){
			this.nodes.add(subNet.getNode(i));
			this.totalNodesCPU.add(subNet.getTotalNodeCPU(i));
			this.nodesCPU.add(subNet.getNodeCPU(i));
			this.nodesSec.add(subNet.getNodeSec(i));
		}

		for(int i = 0; i < subNet.getNumOfEdges(); i++){
			this.edges.add(new Pair<String>(subNet.getEdge(i).getLeft(), subNet.getEdge(i).getRight()));
			this.totalEdgesBw.add(subNet.getTotalEdgeBw(i));
			this.edgesBw.add(subNet.getEdgeBw(i));
			this.edgesSec.add(subNet.getEdgeSec(i));
			this.edgesWeight.add(subNet.getEdgeWeight(i));
			this.edgesLatency.add(subNet.getEdgeLatency(i));
		}

		this.doesItBelong = subNet.getDoesItBelong();

		this.cloudSecSup = subNet.getCloudSecSup();

		for(int i = 0; i < subNet.getNClouds(); i++)
			this.cloudsSecurity.add(subNet.getCloudSecurity(i));
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
		totalNodesCPU.add(cpu);
		nodesCPU.add(cpu);
	}

	public ArrayList<Double> getNodesCPU() {
		return nodesCPU;
	}

	public ArrayList<Double> getTotalNodesCPU() {
		return totalNodesCPU;
	}

	public double getTotalNodeCPU(int index) {
		return totalNodesCPU.get(index);
	}

	public double getNodeCPU(int index) {
		return nodesCPU.get(index);
	}

	public void updateNodeCPU(int index, double value) {
		System.out.println("updateNodeCPU ## -- before nodesCPU.get("+index+")"+ nodesCPU.get(index));
		nodesCPU.set(index, Utils.roundUpDecimals(nodesCPU.get(index)+Utils.truncateDecimal(value)));
		System.out.println("updateNodeCPU ## -- after nodesCPU.get("+index+")"+ nodesCPU.get(index));
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
		if(index==-1)
			System.out.println("-1");
		return edges.get(index);
		
	}

	public int getNumOfEdges() {
		return edges.size();
	}

	public void addEdgeBw(double bw) {
		totalEdgesBw.add(bw);
		edgesBw.add(bw);
	}

	public void addEdgeLatency(double latency) {
		totalEdgesLatency.add(latency);
		edgesLatency.add(latency);
	}

	public ArrayList<Double> getEdgesBw() {
		return edgesBw;
	}

	public ArrayList<Double> getEdgesLatency() {
		return edgesLatency;
	}

	public ArrayList<Double> getTotalEdgesBw() {
		return totalEdgesBw;
	}

	public double getTotalEdgeBw(int index) {
		return totalEdgesBw.get(index);
	}

	public double getEdgeBw(int index) {
		return edgesBw.get(index);
	}

	public double getEdgeLatency(int index) {
		return edgesLatency.get(index);
	}

	public void updateEdgeBw(int index, double value) {
		if((edgesBw.get(index)+Utils.truncateDecimal(value)<0))
			System.out.println(index);	
		edgesBw.set(index, Utils.roundUpDecimals(edgesBw.get(index)+Utils.truncateDecimal(value)));
	}

	public void updateEdgeLatency(int index, double value) {
		edgesBw.set(index, Utils.roundUpDecimals(edgesLatency.get(index)+Utils.truncateDecimal(value)));
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

	public void addEdgeWeight(int weight) {
		edgesWeight.add(weight);
	}

	public ArrayList<Integer> getEdgesWeight() {
		return edgesWeight;
	}

	public int getEdgeWeight(int index) {
		return edgesWeight.get(index);
	}

	public void addNClouds(int nClouds) {
		this.nClouds = nClouds;
	}

	public int getNClouds() {
		return nClouds;
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

	public void setDoesItBelong(int[][] doesItBelong) {
		this.doesItBelong = doesItBelong;
	}

	/**
	 * Fills the matrix doesItBelong to know where each substrate node is
	 * located in each cloud
	 */
	public void fillDoesItBelong() {

		this.doesItBelong = new int[nClouds][nodes.size()];

		int nodesPerCloud = nodes.size() / nClouds;
		int counter = 0;
		int index = 0;
		// Fill the matrix
		// Put a 0 if the substrate node does not belong to the clouds
		// 1 otherwise
		for(int i = 0; i < nClouds; i++){
			while(counter < nodesPerCloud){
				doesItBelong[i][index] = 1;
				counter++;
				index++;
			}
			counter = 0;
		}

		for(int i = doesItBelong[0].length - 1; i >= index; i--){
			doesItBelong[nClouds-1][i] = 1;
		}
		cloudSecSup();
	}

	public int[][] getDoesItBelong() {
		return doesItBelong;
	}

	public HashMap<String, Double> getCloudSecSup() {
		return cloudSecSup;
	}

	public int getDoesItBelong(int i, int j) {
		try {
			return doesItBelong[i][j];
		}
		catch(ArrayIndexOutOfBoundsException exception) {
		    System.out.println("ArrayIndexOutOfBoundsException.");
		}
		return doesItBelong[i][j];
		
	}

	/**
	 * 
	 * @return The maximum security cloud level
	 */
	public double getMaxSecCloud() {
		double max = 0;

		for(Double i: cloudsSecurity){
			if(i > max)
				max = i;
		}
		return max;
	}

	/**
	 * 
	 * @return The minimum security cloud level
	 */
	public double getMinSecCloud() {
		double min = 100;

		for(Double i: cloudsSecurity){
			if(i < min)
				min = i;
		}
		return min;
	}

	/**
	 * Fills the matrix cloudSecSup accordingly with the matrix doesItBelong
	 */
	public void cloudSecSup() {
		for(int i = 0; i < getNClouds(); i ++){
			for(int j = 0; j < getNumOfNodes(); j++){
				if(getDoesItBelong(i,j) == 1){
					cloudSecSup.put(getNode(j), getCloudSecurity(j));}
			}
		}
	}
	
	public void cloudSecSupWithPrivateDataCenter(int nPrivateDataCenters) {
		for(int i = 0; i < getNClouds() + nPrivateDataCenters; i ++){
			for(int j = 0; j < getNumOfNodes(); j++){
				if(getDoesItBelong(i,j) == 1){
					cloudSecSup.put(getNode(j), getCloudSecurity(i));}
			}
		}
	}

	/**
	 * Calculates the costs the InP incurs when accepts a virtual network with SecDep formulation
	 * @param vn The virtual network embedded
	 * @param wUsedNodes Substrate nodes IDs that are mapping the working part of the vn
	 * @param wMappedNodes Virtual nodes IDs mapped
	 * @param bUsedNodes Substrate nodes IDs that are mapping the backup part of the vn
	 * @param bMappedNodes Virtual nodes IDs mapped
	 * @param wUsedEdges Substrate edges IDs that are mapping the working part of the vn
	 * @param wMappedEdges Virtual edges IDs mapped
	 * @param bUsedEdges Substrate edges IDs that are mapping the backup part of the vn
	 * @param bMappedEdges Virtual edges IDs mapped
	 * @return Embedding cost
	 */
	public double getSecDepCost(VirtualNetworkHeu vn, ArrayList<String> wUsedNodes, ArrayList<String> wMappedNodes,
			ArrayList<String> bUsedNodes, ArrayList<String> bMappedNodes,
			ArrayList<Pair<String>> wUsedEdges, ArrayList<Pair<String>> wMappedEdges,
			ArrayList<Pair<String>> bUsedEdges, ArrayList<Pair<String>> bMappedEdges) {

		double nodesCost = 0;
		double edgesCost = 0;

		int index = 0;

		for(int i = 0; i < wMappedNodes.size(); i++){
			index = vn.getNodes().indexOf(wMappedNodes.get(i));

			nodesCost += vn.getNodeCPU(index) * nodesSec.get(nodes.indexOf(wUsedNodes.get(i))) * cloudSecSup.get(wUsedNodes.get(i));
		}
		if(bMappedNodes.size() != 0){
			for(int i = 0; i < bMappedNodes.size(); i++){
				index = vn.getNodes().indexOf(bMappedNodes.get(i));
				nodesCost += vn.getNodeCPU(index) * nodesSec.get(nodes.indexOf(bUsedNodes.get(i))) * cloudSecSup.get(bUsedNodes.get(i));
			}
		}		

		for(int i = 0; i < wUsedEdges.size(); i++){
			Pair<String> tmp = new Pair<>(wUsedEdges.get(i).getRight(), wUsedEdges.get(i).getLeft());
			Pair<String> tmp2 = new Pair<>(wMappedEdges.get(i).getRight(), wMappedEdges.get(i).getLeft());

			boolean hasNormal = vn.getEdges().contains(wMappedEdges.get(i));
			boolean hasTmp2 = vn.getEdges().contains(tmp2);

			if(edges.contains(wUsedEdges.get(i))){
				if(hasNormal)
					index = vn.getEdges().indexOf(wMappedEdges.get(i));
				else if(hasTmp2)
					index = vn.getEdges().indexOf(tmp2);
				edgesCost += vn.getEdgeBw(index) * edgesSec.get(edges.indexOf(wUsedEdges.get(i)));
			}else if(edges.contains(tmp)){
				if(hasNormal)
					index = vn.getEdges().indexOf(wMappedEdges.get(i));
				else if(hasTmp2)
					index = vn.getEdges().indexOf(tmp2);
				edgesCost += vn.getEdgeBw(index) * edgesSec.get(edges.indexOf(tmp));
			}
		}

		if(bMappedEdges.size() != 0){
			//System.out.println("Entrei no backup");

			for(int i = 0; i < bUsedEdges.size(); i++){
				Pair<String> tmp = new Pair<>(bUsedEdges.get(i).getRight(), bUsedEdges.get(i).getLeft());
				Pair<String> tmp2 = new Pair<>(bMappedEdges.get(i).getRight(), bMappedEdges.get(i).getLeft());

				boolean hasNormal = vn.getEdges().contains(bMappedEdges.get(i));
				boolean hasTmp2 = vn.getEdges().contains(tmp2);

				if(edges.contains(bUsedEdges.get(i))){
					if(hasNormal)
						index = vn.getEdges().indexOf(bMappedEdges.get(i));
					else if(hasTmp2)
						index = vn.getEdges().indexOf(tmp2);
					edgesCost += vn.getEdgeBw(index) * edgesSec.get(edges.indexOf(bUsedEdges.get(i)));
				}else if(edges.contains(tmp)){
					if(hasNormal)
						index = vn.getEdges().indexOf(bMappedEdges.get(i));
					else if(hasTmp2)
						index = vn.getEdges().indexOf(tmp2);
					edgesCost += vn.getEdgeBw(index) * edgesSec.get(edges.indexOf(tmp));
				}
			}
		}
		return nodesCost + edgesCost;
	}

	//All edges were insert as working ones
	public double getSecDepCostHeuristic(VirtualNetworkHeu vn, ArrayList<String> wUsedNodes, ArrayList<String> wMappedNodes,
			ArrayList<String> bUsedNodes, ArrayList<String> bMappedNodes,
			ArrayList<Pair<String>> wUsedEdges, ArrayList<Pair<String>> wMappedEdges, ArrayList<Double> bwEdgesUsed) {

		double nodesCost = 0;
		double edgesCost = 0;

		int index = 0;

		for(int i = 0; i < wMappedNodes.size(); i++){
			index = vn.getNodes().indexOf(wMappedNodes.get(i));

			nodesCost += vn.getNodeCPU(index) * nodesSec.get(nodes.indexOf(wUsedNodes.get(i))) * cloudSecSup.get(wUsedNodes.get(i));
		}
		if(bMappedNodes.size() != 0){
			for(int i = 0; i < bMappedNodes.size(); i++){
				index = vn.getNodes().indexOf(bMappedNodes.get(i));
				nodesCost += vn.getNodeCPU(index) * nodesSec.get(nodes.indexOf(bUsedNodes.get(i))) * cloudSecSup.get(bUsedNodes.get(i));
			} 
		}		 
		for(int i = 0; i < wUsedEdges.size(); i++){
			if(edges.contains(wUsedEdges.get(i))){
				edgesCost += bwEdgesUsed.get(i) * edgesSec.get(edges.indexOf(wUsedEdges.get(i)));
			}else{ 
				Pair<String> tmp = new Pair<>(wUsedEdges.get(i).getRight(), wUsedEdges.get(i).getLeft());
				if(edges.contains(tmp)){
					edgesCost += bwEdgesUsed.get(i) * edgesSec.get(edges.indexOf(tmp));
				}
			}
		}
		return nodesCost + edgesCost;
	}

	public double getSecDepCostHeuristicDK(VirtualNetworkHeu vn, ArrayList<String> wUsedNodes, 
			ArrayList<String> wMappedNodes,	ArrayList<String> bUsedNodes, ArrayList<String> bMappedNodes,
			ArrayList<Pair<String>> getwEdgesUsed, ArrayList<Pair<String>> getwMappedEdges,
			ArrayList<Pair<String>> getbEdgesUsed, ArrayList<Pair<String>> getbMappedEdges,
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksWorking,
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking,
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksBackup,
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup) {

		double nodesCost = 0;
		double edgesCost = 0;

		int index = 0;

		for(int i = 0; i < wMappedNodes.size(); i++){
			index = vn.getNodes().indexOf(wMappedNodes.get(i));

			nodesCost += vn.getNodeCPU(index) * nodesSec.get(nodes.indexOf(wUsedNodes.get(i))) * cloudSecSup.get(wUsedNodes.get(i));
		}
		if(bMappedNodes.size() != 0){
			for(int i = 0; i < bMappedNodes.size(); i++){
				index = vn.getNodes().indexOf(bMappedNodes.get(i));
				nodesCost += vn.getNodeCPU(index) * nodesSec.get(nodes.indexOf(bUsedNodes.get(i))) * cloudSecSup.get(bUsedNodes.get(i));
			}
		}

		HashMap<Integer, List<String>> listOfKPathsComplyLatency = null;
		HashMap<Integer, Double> bandwidthInPahtForVirtualLinks = null;

		//Working edges
		if((indexVirtualLinkToPahtsForVirtualLinksWorking != null) && (indexVirtualLinkToPahtsForVirtualLinksWorking.size() >= 0)){

			Iterator<Entry<Integer, HashMap<Integer, List<String>>>> it = indexVirtualLinkToPahtsForVirtualLinksWorking.entrySet().iterator();

			while(it.hasNext()){
				Map.Entry pair = it.next();
				int key = (int) pair.getKey();
				listOfKPathsComplyLatency = indexVirtualLinkToPahtsForVirtualLinksWorking.get(key);
				bandwidthInPahtForVirtualLinks = indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking.get(key);
				ArrayList<Pair<String>> edgesInAPath = new ArrayList<Pair<String>>();
				List<String> path;
				int key2 = -1; 
				Iterator<Entry<Integer, List<String>>> it2 = listOfKPathsComplyLatency.entrySet().iterator();
				double bw = 0;
				while(it2.hasNext()){
					Map.Entry pair2 = it2.next();
					key2 = (int) pair2.getKey();
					path = listOfKPathsComplyLatency.get(key2);
					Utils.copyEdgesUsed(edgesInAPath, path, this);
					for(int i = 0; i < edgesInAPath.size(); i++){
						bw = bandwidthInPahtForVirtualLinks.get(key2);
						edgesCost += bw * edgesSec.get(edges.indexOf(edgesInAPath.get(i)));
					}
					edgesInAPath.clear();
				}
			}
		}
		//Backup edges
		if((indexVirtualLinkToPahtsForVirtualLinksBackup != null) && (indexVirtualLinkToPahtsForVirtualLinksBackup.size() >= 0)){

			Iterator<Entry<Integer, HashMap<Integer, List<String>>>> it3 = indexVirtualLinkToPahtsForVirtualLinksBackup.entrySet().iterator();

			while(it3.hasNext()){
				Map.Entry pair = it3.next();
				int key = (int) pair.getKey();
				listOfKPathsComplyLatency = indexVirtualLinkToPahtsForVirtualLinksBackup.get(key);
				bandwidthInPahtForVirtualLinks = indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup.get(key);
				ArrayList<Pair<String>> edgesInAPath = new ArrayList<Pair<String>>();
				List<String> path;
				int key2 = -1; 
				Iterator<Entry<Integer, List<String>>> it2 = listOfKPathsComplyLatency.entrySet().iterator();
				double bw = 0;
				while(it2.hasNext()){
					Map.Entry pair2 = it2.next();
					key2 = (int) pair2.getKey();
					path = listOfKPathsComplyLatency.get(key2);
					Utils.copyEdgesUsed(edgesInAPath, path, this);
					for(int i = 0; i < edgesInAPath.size(); i++){
						bw = bandwidthInPahtForVirtualLinks.get(key2);
						edgesCost += bw * edgesSec.get(edges.indexOf(edgesInAPath.get(i)));
					}
					edgesInAPath.clear();
				}
			}
		}
		return nodesCost + edgesCost;
	}

	/**
	 * Calculates the costs the InP incurs when accepts a virtual network with DViNE formulation
	 * @param vn The virtual network
	 * @param usedEdges Substrate edges IDs used
	 * @param mappedEdges Virtual edges IDs mapped
	 * @param bwEdgesUsed 
	 * @return Embedding cost
	 */
	public double getDvineCost(VirtualNetworkHeu vn, ArrayList<Pair<String>> usedEdges, ArrayList<Pair<String>> mappedEdges) {

		double nodesCost = 0;
		double edgesCost = 0;

		int aux = 0;

		for(int i = 0; i < vn.getNumOfNodes(); i++)
			nodesCost += vn.getNodeCPU(i);

		for(int i = 0; i < usedEdges.size(); i++){
			Pair<String> tmp = new Pair<>(usedEdges.get(i).getRight(), usedEdges.get(i).getLeft());
			Pair<String> tmp2 = new Pair<>(mappedEdges.get(i).getRight(), mappedEdges.get(i).getLeft());
			boolean hasNormal = vn.getEdges().contains(mappedEdges.get(i));
			boolean hasTmp2 = vn.getEdges().contains(tmp2);
			if(edges.contains(usedEdges.get(i))){
				if(hasNormal)
					aux = vn.getEdges().indexOf(mappedEdges.get(i));
				else if(hasTmp2)
					aux = vn.getEdges().indexOf(tmp2);
				edgesCost += vn.getEdgeBw(aux);
			}else if(edges.contains(tmp)){
				if(hasNormal)
					aux = vn.getEdges().indexOf(mappedEdges.get(i));
				else if(hasTmp2)
					aux = vn.getEdges().indexOf(tmp2);
				edgesCost += vn.getEdgeBw(aux);
			}
		}
		return nodesCost + edgesCost;
	}

	public double getDvineCost(VirtualNetworkHeu vn, ArrayList<String> mappedNodes, ArrayList<String> nodesUsed, 
			ArrayList<Pair<String>> usedEdges, ArrayList<Pair<String>> mappedEdges, ArrayList<Double> bwEdgesUsed) {

		double nodesCost = 0;
		double edgesCost = 0;
		
		int index = 0;

		for(int i = 0; i < mappedNodes.size(); i++){
			index = vn.getNodes().indexOf(mappedNodes.get(i));

			nodesCost += vn.getNodeCPU(index) * nodesSec.get(nodes.indexOf(nodesUsed.get(i))) * cloudSecSup.get(nodesUsed.get(i));
		}
		for(int i = 0; i < usedEdges.size(); i++){
			Pair<String> tmp = new Pair<>(usedEdges.get(i).getRight(), usedEdges.get(i).getLeft());
			if(edges.contains(usedEdges.get(i))){
				edgesCost += bwEdgesUsed.get(i) * edgesSec.get(edges.indexOf(usedEdges.get(i)));
			}else if(edges.contains(tmp)){
				edgesCost += bwEdgesUsed.get(i) * edgesSec.get(edges.indexOf(tmp));
			}
		}
		return nodesCost + edgesCost;
	}
	
	public double getDvineAndFullGreedyHeuCost(VirtualNetworkHeu vn, ArrayList<String> mappedNodes, ArrayList<String> nodesUsed, 
			ArrayList<Pair<String>> usedEdges, ArrayList<Pair<String>> mappedEdges, ArrayList<Double> bwEdgesUsed) {

		double nodesCost = 0;
		double edgesCost = 0;
		
		int index = 0;

		for(int i = 0; i < mappedNodes.size(); i++){
			index = vn.getNodes().indexOf(mappedNodes.get(i));
			nodesCost += vn.getNodeCPU(index) * nodesSec.get(nodes.indexOf(nodesUsed.get(i))) * cloudSecSup.get(nodesUsed.get(i));
		}
		for(int i = 0; i < usedEdges.size(); i++){
			Pair<String> tmp = new Pair<>(usedEdges.get(i).getRight(), usedEdges.get(i).getLeft());
			if(edges.contains(usedEdges.get(i))){
				edgesCost += bwEdgesUsed.get(i) * edgesSec.get(edges.indexOf(usedEdges.get(i)));
			}else if(edges.contains(tmp)){
				edgesCost += bwEdgesUsed.get(i) * edgesSec.get(edges.indexOf(tmp));
			}
		}
		return nodesCost + edgesCost;
	}
	
	public double getDvineAndFullGreedyHeuNoSecCost(VirtualNetworkHeu vn, ArrayList<String> mappedNodes, ArrayList<String> nodesUsed, 
			ArrayList<Pair<String>> usedEdges, ArrayList<Pair<String>> mappedEdges, ArrayList<Double> bwEdgesUsed) {

		double nodesCost = 0;
		double edgesCost = 0;
		int index = 0;

		for(int i = 0; i < mappedNodes.size(); i++){
			index = vn.getNodes().indexOf(mappedNodes.get(i));
			nodesCost += vn.getNodeCPU(index);
		}
		for(int i = 0; i < usedEdges.size(); i++){
			Pair<String> tmp = new Pair<>(usedEdges.get(i).getRight(), usedEdges.get(i).getLeft());
			if(edges.contains(usedEdges.get(i))){
				edgesCost += bwEdgesUsed.get(i);
			}else if(edges.contains(tmp)){
				edgesCost += bwEdgesUsed.get(i);
			}
		}
		return nodesCost + edgesCost;
	}

	/**
	 * Calculates the average substrate node utilization
	 * @return average node stress
	 */
	public double getAverageNodeStress() {
		double averageStress = 0;
		double tmp = 0;

		for (int i = 0; i < getNumOfNodes(); i++) {
			tmp = (totalNodesCPU.get(i) - nodesCPU.get(i)) / totalNodesCPU.get(i);
			tmp = Utils.roundDecimals(tmp);
			averageStress += tmp;
		}
		averageStress = Utils.roundDecimals(averageStress/getNumOfNodes());
		return averageStress;
	}

	/**
	 * Calculates the average substrate link utilization
	 * @return average link stress
	 */
	public double getAverageLinkStress(){
		double averageStress = 0;
		double tmp = 0;

		for (int i = 0; i < getNumOfEdges(); i++) {
			tmp = (totalEdgesBw.get(i) - edgesBw.get(i)) / totalEdgesBw.get(i);
			tmp = Utils.roundDecimals(tmp);
			averageStress += tmp;
		}
		averageStress = Utils.roundDecimals(averageStress/getNumOfEdges());
		return averageStress;
	}

	public void setNClouds(int nClouds) {
		this.nClouds = nClouds;
	}

	public void setCloudsSecurity(ArrayList<Double> cloudsSecurity) {
		this.cloudsSecurity = cloudsSecurity;
	}

	public void setCloudSecSup(HashMap<String,Double> cloudSecSup) {
		this.cloudSecSup = cloudSecSup;
	}

	public void setNodesSecurity(ArrayList<Double> nodesSecurity) {
		this.nodesSec = nodesSecurity;
	}

	public void setEdgesSecurity(ArrayList<Double> edgesSecurity) {
		this.edgesSec = edgesSecurity;
	}

	/**
	 * Just prints the current state of the substrate network
	 */
	public void printNetworkState() {

		for(int i = 0; i < nodes.size(); i++){
			System.out.print("Node cpu used:"+nodes.get(i) + ": "+Utils.roundDecimals((totalNodesCPU.get(i) - nodesCPU.get(i)))+"\n");
			System.out.print("Total CPU cap:"+nodes.get(i) + ": "+totalNodesCPU.get(i)+"\n");
			System.out.print("Cur CPU cap:"+nodes.get(i) + ": "+nodesCPU.get(i)+"\n");
		}
		System.out.println();
		for(int i = 0; i < edges.size(); i++){
			System.out.print("Edge bw used ("+edges.get(i).getLeft()+","+edges.get(i).getRight()+"): "+(totalEdgesBw.get(i) - edgesBw.get(i))+"\n");
			System.out.print("Total bw cap ("+edges.get(i).getLeft()+","+edges.get(i).getRight()+"): "+totalEdgesBw.get(i)+"\n");
			System.out.print("Cur bw cap ("+edges.get(i).getLeft()+","+edges.get(i).getRight()+"): "+edgesBw.get(i)+"\n");
		}
	}

	public int getNodesCloudIndex(int indexNode){

		for(int i = 0; i < getNClouds(); i ++){
			if(getDoesItBelong(i,indexNode) == 1){
				return i;
			}
		}
		return -1;
	}

	/**
	 * Save the substrate topology on a file
	 * @param staticFile The file to where the topology will be saved
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
			
			bufferedWriter.write("NNODESINCLOUDS\n");
			bufferedWriter.write(nNodesInClouds+"\n");

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

			bufferedWriter.write("EDGESWEIGHT\n");
			for(Integer weight: edgesWeight)
				bufferedWriter.write(weight+"\n");

			bufferedWriter.write("NPRIVATEDATACENTERS\n");
			bufferedWriter.write(nPrivateDataCenters+"\n");
			
			bufferedWriter.write("NCLOUDS\n");
			bufferedWriter.write(nClouds+"\n");

			bufferedWriter.write("CLOUDSSEC\n");
			//			System.out.println("CloudsSec: "+cloudsSecurity.size());
			for(Double sec: cloudsSecurity)
				bufferedWriter.write(sec+"\n");

			bufferedWriter.write("DOESITBELONG\n");
			for(int i = 0; i < doesItBelong.length; i++)
				for(int j = 0; j < doesItBelong[0].length; j++)
					bufferedWriter.write(doesItBelong[i][j]+"\n");
			
			bufferedWriter.write("ACCEPTNODEEMBEDDING\n");
			for(Boolean accept: acceptNodesEmbedding)
				bufferedWriter.write(accept ? "1\n" : "0\n");

			bufferedWriter.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public double getDvineCostSec(VirtualNetworkHeu vn, ArrayList<String> nodesUsedAlpha,
			ArrayList<String> mappedNodes, ArrayList<Pair<String>> edgesUsedAlpha, ArrayList<Pair<String>> mappedEdges) {

		double nodesCost = 0;
		double edgesCost = 0;

		int index = 0;

		for(int i = 0; i < mappedNodes.size(); i++){
			index = vn.getNodes().indexOf(mappedNodes.get(i));

			nodesCost += vn.getNodeCPU(index) * nodesSec.get(nodes.indexOf(nodesUsedAlpha.get(i))) * cloudSecSup.get(nodesUsedAlpha.get(i));
		}
		for(int i = 0; i < edgesUsedAlpha.size(); i++){
			Pair<String> tmp = new Pair<>(edgesUsedAlpha.get(i).getRight(), edgesUsedAlpha.get(i).getLeft());
			Pair<String> tmp2 = new Pair<>(mappedEdges.get(i).getRight(), mappedEdges.get(i).getLeft());

			boolean hasNormal = vn.getEdges().contains(mappedEdges.get(i));
			boolean hasTmp2 = vn.getEdges().contains(tmp2);

			if(edges.contains(edgesUsedAlpha.get(i))){

				if(hasNormal)
					index = vn.getEdges().indexOf(mappedEdges.get(i));
				else if(hasTmp2)
					index = vn.getEdges().indexOf(tmp2);

				edgesCost += vn.getEdgeBw(index) * edgesSec.get(edges.indexOf(edgesUsedAlpha.get(i)));

			}else if(edges.contains(tmp)){

				if(hasNormal)
					index = vn.getEdges().indexOf(mappedEdges.get(i));
				else if(hasTmp2)
					index = vn.getEdges().indexOf(tmp2);

				edgesCost += vn.getEdgeBw(index) * edgesSec.get(edges.indexOf(tmp));
			}
		}
		return nodesCost + edgesCost;
	}
	
	//Reconfig
	public void resetSubstrateNetwork(){
		nodesCPU.clear();
		nodesCPU.addAll(totalNodesCPU);
		
		edgesBw.clear();
		edgesBw.addAll(totalEdgesBw);
	}
	
	public void setCPUBW(SubstrateNetworkHeu tmp){
		nodesCPU.clear();
		edgesBw.clear();
		
		for(Double cpu: tmp.getNodesCPU())
			nodesCPU.add(cpu);
		
		for(Double bw: tmp.getEdgesBw())
			edgesBw.add(bw);
	}
	
	public void setTotalNodesCPU(ArrayList<Double> totalNodesCPU) {
		this.totalNodesCPU = totalNodesCPU;
	}
	
	public void setTotalEdgesBw(ArrayList<Double> totalEdgesBw) {
		this.totalEdgesBw = totalEdgesBw;
	}
	//Reconfig
	
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
					(1/(this.getNodeSec(i)*this.getCloudSecSup().get(this.getNode(i))))*(1/(100.0*numberOfLinksAux+100.0));
		}
		return auxUtilityNet;
	}

	public ArrayList<Boolean> getAcceptNodesEmbedding() {
		return acceptNodesEmbedding;
	}

	public boolean getAcceptNodeEmbedding(int i) {
		return acceptNodesEmbedding.get(i);
	}
	
	public void setAcceptNodesEmbedding(ArrayList<Boolean> acceptNodesEmbedding) {
		this.acceptNodesEmbedding = acceptNodesEmbedding;
	}
	
	public void addAcceptNodeEmbedding(boolean accept) {
		this.acceptNodesEmbedding.add(accept);
	}

	public void fillDoesItBelongWithPrivateDataCenter(int initialNumOfNodes) {
		this.doesItBelong = new int[nClouds + nPrivateDataCenters][nodes.size()];

		int nodesPerCloud = initialNumOfNodes/nClouds;
		int counter = 0;
		int index = 0;

		// Fill the matrix
		// Put a 0 if the substrate node does not belong to the clouds
		// 1 otherwise
		for(int i = 0; i < nClouds+nPrivateDataCenters; i++){
			while(counter < nodesPerCloud){
				doesItBelong[i][index] = 1;
				counter++;
				index++;
			}
			counter = 0;
		}
		for(int i = doesItBelong[0].length - 1; i >= index; i--){
			doesItBelong[nClouds+nPrivateDataCenters-1][i] = 1;
		}
		cloudSecSupWithPrivateDataCenter(nPrivateDataCenters);
		
	}
}
