package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Embeddor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Utils;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.DisjointKShortestPath.bhandari.EdgeDisjointShortestPair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.DisjointKShortestPath.graph.Link;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.DisjointKShortestPath.graph.Network;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk.HeuristicFullGreedyDatFileCreator;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk.OutputFileReader;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic.LinkHeuristicInfo;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic.NodeHeuristicInfo;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic.SecLoc;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.SubstrateManager;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.SubstrateNetworkHeu;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.UpdateMode;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;

public class EmbeddorHeu {

	SubstrateNetworkHeu subNetHeu;
	VirtualNetworkHeu vnHeu;
	String configFile;
	private static String MOD_FILE = "/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/secdepvne/topology/FULLGREEDY_MCF_HEURISTIC.mod";

	public EmbeddorHeu(SubstrateNetworkHeu subNetHeu, VirtualNetworkHeu vnHeu, String configFile) {
		this.subNetHeu = subNetHeu;
		this.vnHeu = vnHeu;
		this.configFile = configFile;
	}

	public boolean solve(String typeOfHeuristic) throws IOException {

		boolean res = true;
		int numberRequiredShortestPaths = 1;
		String configFile = net.floodlightcontroller.sirius.util.Utils.fileNameWithoutExt(this.configFile);
		HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> substrateSecLocNodeHeuristicInfoArrayMap;
		ArrayList<NodeHeuristicInfo> virtualSecLocNodeHeuristicInfoArray = null;
		ArrayList<Double> securityValuesSubstrate = new ArrayList<Double>();
		ArrayList<Double> securityValuesVirtual = new ArrayList<Double>();
		ArrayList<Double> locationValuesSubstrate = new ArrayList<Double>();
		ArrayList<Double> locationValuesVirtual = new ArrayList<Double>();
		ArrayList <Pair<String>> mappedWorkingNodes = new ArrayList <Pair<String>>();
		ArrayList <Pair<String>> mappedBackupNodes = new ArrayList <Pair<String>>();
		OutputFileReader fileReaders = new OutputFileReader();
		ArrayList<LinkHeuristicInfo> virtualLinkHeuristicInfoArray = new ArrayList<LinkHeuristicInfo>();
		HashMap<Integer,HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksWorking = new HashMap<Integer,HashMap<Integer, List<String>>>();
		HashMap<Integer,HashMap<Integer, Double>> indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking = new HashMap<Integer,HashMap<Integer, Double>>();
		HashMap<Integer,HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking = new HashMap<Integer,HashMap<Integer, Double>>();
		HashMap<Integer,HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksBackup = new HashMap<Integer,HashMap<Integer, List<String>>>();
		HashMap<Integer,HashMap<Integer, Double>> indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup = new HashMap<Integer,HashMap<Integer, Double>>();
		HashMap<Integer,HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup = new HashMap<Integer,HashMap<Integer, Double>>();
		SubstrateManager subMngr = new SubstrateManager(this.subNetHeu);
		SubstrateNetworkHeu subNetAux = new SubstrateNetworkHeu();
		Utils.populateSubstrateInfo(subNetAux, this.subNetHeu);
		long ini;
		ini = System.currentTimeMillis();
		subNetAux.setDistancesBetweenNodes(Utils.distanceHopsNode(subNetAux));
		//virtualSecLocNodeHeuristicInfoArray = Utils.calculateUtility(this.vnHeu, securityValuesVirtual, locationValuesVirtual);
		SubstrateManager subMngrAuxHeuristic = new SubstrateManager(subNetAux);
		if(typeOfHeuristic.equals("Sirius")){
			substrateSecLocNodeHeuristicInfoArrayMap = Utils.calculateUtility(subNetAux, securityValuesSubstrate, locationValuesSubstrate);
			virtualSecLocNodeHeuristicInfoArray = Utils.calculateUtility(this.vnHeu);
			mappedWorkingNodes = Utils.mappingVirtualWorkingNodesMinimazePaths(substrateSecLocNodeHeuristicInfoArrayMap, virtualSecLocNodeHeuristicInfoArray, subNetAux, this.vnHeu, 
					securityValuesSubstrate, locationValuesSubstrate, 100);
		}
		else if (typeOfHeuristic.equals("fullGreed")){
			ArrayList<NodeHeuristicInfo> substrateNodeGreedyHeuristic = Utils.calculateGreedy(subNetAux);
			ArrayList<NodeHeuristicInfo> virtualNodeGreedyHeuristic = Utils.calculateGreedyDesc(this.vnHeu);
			mappedWorkingNodes = Utils.mappingFullGreedyVirtualNodes(subNetAux, this.vnHeu, substrateNodeGreedyHeuristic, virtualNodeGreedyHeuristic);
		}
		//mappedWorkingNodes = Utils.mappingVirtualWorkingNodes(substrateSecLocNodeHeuristicInfoArrayMap, virtualSecLocNodeHeuristicInfoArray, subNetAux, this.vnHeu, 
		//securityValuesSubstrate, locationValuesSubstrate);
		ArrayList<Pair<String>> finalSubstrateWorkingAndBackupEdges = new ArrayList<Pair<String>>();
		ArrayList<Pair<String>> finalVirtualWorkingAndBackupEdges = new ArrayList<Pair<String>>();

		if(mappedWorkingNodes != null){

			if(typeOfHeuristic.equals("Sirius")){
				for(int j = 0; j < this.vnHeu.getEdges().size(); j++){
					virtualLinkHeuristicInfoArray.add(new LinkHeuristicInfo(j, this.vnHeu.getEdgeBw(j), this.vnHeu.getEdgeLatency(j)));
				}

				Collections.sort(virtualLinkHeuristicInfoArray);
				
				for(int k = 0; k < virtualLinkHeuristicInfoArray.size(); k++){
					int indexVirtualLink = virtualLinkHeuristicInfoArray.get(k).getIndexLink();
					Network graph = Utils.populateGraphDisjointKShortestPathSubstrateInfo(subNetAux, 
							this.vnHeu.getEdgeSec(indexVirtualLink));
					Pair<String> sourceDestinationNodes = Utils.getSourceDestinationNodes(mappedWorkingNodes, 
							this.vnHeu, virtualLinkHeuristicInfoArray.get(k));
					if(sourceDestinationNodes != null){
						List<List<Link>> pathsDisjointKShortestPath = EdgeDisjointShortestPair.findShortestPath(graph, 
								graph.getDevice(sourceDestinationNodes.getLeft()), 
								graph.getDevice(sourceDestinationNodes.getRight()));
						if((pathsDisjointKShortestPath != null) && (pathsDisjointKShortestPath.size()>0)){
							List<String> path = null;
							int pathSize = 0;
							for(int a = 0; a < pathsDisjointKShortestPath.size(); a++){

								path = Utils.getDisjointKShortestPath(pathsDisjointKShortestPath.get(a),
										graph.getDevice(sourceDestinationNodes.getLeft()), graph.getDevice(sourceDestinationNodes.getRight()));

								double minAvailBw = Utils.getMinAvailableBW(path, subNetAux);

								if((Utils.isComplyDelayDK(path, subNetAux, virtualLinkHeuristicInfoArray.get(k).getLatency(), 
										mappedWorkingNodes, this.vnHeu, indexVirtualLink))){

									if(indexVirtualLinkToPahtsForVirtualLinksWorking.get(indexVirtualLink)== null){

										HashMap<Integer, List<String>> newPath = new HashMap<Integer, List<String>>();
										newPath.put(pathSize, path);
										indexVirtualLinkToPahtsForVirtualLinksWorking.put(indexVirtualLink, newPath);
									}else{
										HashMap<Integer, List<String>> newPath = indexVirtualLinkToPahtsForVirtualLinksWorking.get(indexVirtualLink);
										newPath.put(pathSize, path);
										indexVirtualLinkToPahtsForVirtualLinksWorking.put(indexVirtualLink, newPath);
									}

									if(indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking.get(indexVirtualLink)== null){

										HashMap<Integer, Double> newPath = new HashMap<Integer, Double>();
										newPath.put(pathSize, minAvailBw);
										indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking.put(indexVirtualLink, newPath);
									}else{
										HashMap<Integer, Double> newPath = indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking.get(indexVirtualLink);
										newPath.put(pathSize, minAvailBw);
										indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking.put(indexVirtualLink, newPath);
									}
									pathSize++;
								}
								if((indexVirtualLinkToPahtsForVirtualLinksWorking == null) || (indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking == null))
									return false;
								if(indexVirtualLinkToPahtsForVirtualLinksWorking.get(indexVirtualLink).size() == numberRequiredShortestPaths){
									break;
								}
							}

							double sumBw = Utils.sumBandwitdhInPaths(indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking.get(indexVirtualLink));

							if(sumBw >= this.vnHeu.getEdgeBw(indexVirtualLink)){

								HashMap<Integer, Double> bandwidthInPahtForVirtualLinks = Utils.getBwInpaths(indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking.get(indexVirtualLink), 
										this.vnHeu.getEdgeBw(indexVirtualLink), sumBw);
								indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking.put(indexVirtualLink, bandwidthInPahtForVirtualLinks);

								ArrayList<Pair<String>> mappedEdges = new ArrayList<Pair<String>>();
								mappedEdges.add(new Pair<String>(this.vnHeu.getEdge(virtualLinkHeuristicInfoArray.get(k).getIndexLink()).getLeft(),
										this.vnHeu.getEdge(virtualLinkHeuristicInfoArray.get(k).getIndexLink()).getRight()));
								HashMap<Integer, List<String>> listOfDKPathsComplyLatency = indexVirtualLinkToPahtsForVirtualLinksWorking.get(indexVirtualLink);
								subMngrAuxHeuristic.updateSubstrateNetworkDK(this.vnHeu, listOfDKPathsComplyLatency, bandwidthInPahtForVirtualLinks, UpdateMode.DECREMENT);

							}else{
								return false;
							}
						}else{
							return false;
						}
					}else{
						return false;
					}
				}
				if(this.vnHeu.getWantBackup() && (mappedWorkingNodes != null)){

					Utils.desableWorkingNodesAndLinksDK(subNetAux, mappedWorkingNodes, indexVirtualLinkToPahtsForVirtualLinksWorking);
					substrateSecLocNodeHeuristicInfoArrayMap = Utils.calculateUtility(subNetAux, securityValuesSubstrate, locationValuesSubstrate);
					mappedBackupNodes = Utils.mappingVirtualBackupNodesPartial(subNetAux, 
							this.vnHeu, mappedWorkingNodes, securityValuesSubstrate, locationValuesSubstrate, virtualSecLocNodeHeuristicInfoArray,
							substrateSecLocNodeHeuristicInfoArrayMap);
					ArrayList <Pair<String>> mappedBackupNodesEnriched = null;

					if(mappedBackupNodes != null){

						Utils.removeLinksNotRequestBackup(virtualLinkHeuristicInfoArray, this.vnHeu);
						mappedBackupNodesEnriched = Utils.enrichMappedBackupNodes(mappedWorkingNodes, mappedBackupNodes, virtualLinkHeuristicInfoArray, this.vnHeu);

						for(int p = 0; p < virtualLinkHeuristicInfoArray.size(); p++){

							int indexVirtualLink = virtualLinkHeuristicInfoArray.get(p).getIndexLink();

							Network graph = Utils.populateGraphDisjointKShortestPathSubstrateInfo(subNetAux, 
									this.vnHeu.getEdgeSec(indexVirtualLink));

							Pair<String> sourceDestinationNodes = Utils.getSourceDestinationNodes(mappedBackupNodesEnriched, 
									this.vnHeu, virtualLinkHeuristicInfoArray.get(p));

							if(sourceDestinationNodes != null){

								List<List<Link>> pathsDisjointKShortestPath = EdgeDisjointShortestPair.findShortestPath(graph, 
										graph.getDevice(sourceDestinationNodes.getLeft()), 
										graph.getDevice(sourceDestinationNodes.getRight()));

								if((pathsDisjointKShortestPath != null) && (pathsDisjointKShortestPath.size() > 0)){
									List<String> path = null;
									int pathSize = 0;

									for(int a = 0; a < pathsDisjointKShortestPath.size(); a++){

										path = Utils.getDisjointKShortestPath(pathsDisjointKShortestPath.get(a),
												graph.getDevice(sourceDestinationNodes.getLeft()), graph.getDevice(sourceDestinationNodes.getRight()));

										double minAvailBw = Utils.getMinAvailableBW(path, subNetAux);

										if((Utils.isComplyDelayDK(path, subNetAux, virtualLinkHeuristicInfoArray.get(p).getLatency(), 
												mappedBackupNodes, this.vnHeu, indexVirtualLink))){

											if(indexVirtualLinkToPahtsForVirtualLinksBackup.get(indexVirtualLink)== null){

												HashMap<Integer, List<String>> newPath = new HashMap<Integer, List<String>>();
												newPath.put(pathSize, path);
												indexVirtualLinkToPahtsForVirtualLinksBackup.put(indexVirtualLink, newPath);
											}else{
												HashMap<Integer, List<String>> newPath = indexVirtualLinkToPahtsForVirtualLinksBackup.get(indexVirtualLink);
												newPath.put(pathSize, path);
												indexVirtualLinkToPahtsForVirtualLinksBackup.put(indexVirtualLink, newPath);
											}

											if(indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup.get(indexVirtualLink)== null){

												HashMap<Integer, Double> newPath = new HashMap<Integer, Double>();
												newPath.put(pathSize, minAvailBw);
												indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup.put(indexVirtualLink, newPath);
											}else{
												HashMap<Integer, Double> newPath = indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup.get(indexVirtualLink);
												newPath.put(pathSize, minAvailBw);
												indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup.put(indexVirtualLink, newPath);
											}
											pathSize++;
										}										
										if(indexVirtualLinkToPahtsForVirtualLinksBackup.get(indexVirtualLink).size() == numberRequiredShortestPaths){
											break;
										}
									}
									double sumBw = Utils.sumBandwitdhInPaths(indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup.get(indexVirtualLink));

									if(sumBw >= this.vnHeu.getEdgeBw(indexVirtualLink)){

										HashMap<Integer, Double> bandwidthInPahtForVirtualLinks = Utils.getBwInpaths(indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup.get(indexVirtualLink), 
												this.vnHeu.getEdgeBw(indexVirtualLink), sumBw);
										indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup.put(indexVirtualLink, bandwidthInPahtForVirtualLinks);

										ArrayList<Pair<String>> mappedEdges = new ArrayList<Pair<String>>();
										mappedEdges.add(new Pair<String>(this.vnHeu.getEdge(virtualLinkHeuristicInfoArray.get(p).getIndexLink()).getLeft(),
												this.vnHeu.getEdge(virtualLinkHeuristicInfoArray.get(p).getIndexLink()).getRight()));
										HashMap<Integer, List<String>> listOfKPathsComplyLatency = indexVirtualLinkToPahtsForVirtualLinksBackup.get(indexVirtualLink);

										subMngrAuxHeuristic.updateSubstrateNetworkDK(this.vnHeu, listOfKPathsComplyLatency, bandwidthInPahtForVirtualLinks, UpdateMode.DECREMENT);
									}else{
										return false;
									}	
								}else{
									return false;
								}
							}else{
								return false;
							}							
						}

						fileReaders.collectAllHeuristicInfoDK(this.vnHeu, this.subNetHeu.getNumOfNodes(), this.subNetHeu, 
								mappedWorkingNodes, mappedBackupNodes, finalSubstrateWorkingAndBackupEdges, 
								finalVirtualWorkingAndBackupEdges);

						if(fileReaders.wasAccepted()){

							fileReaders.setIndexVirtualLinkToPahtsForVirtualLinksWorking(indexVirtualLinkToPahtsForVirtualLinksWorking);
							fileReaders.setIndexVirtualLinkToBandwidthInpahtForVirtualLinksWorking(indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking);
							fileReaders.setIndexVirtualLinkToPahtsForVirtualLinksBackup(indexVirtualLinkToPahtsForVirtualLinksBackup);
							fileReaders.setIndexVirtualLinkToBandwidthInpahtForVirtualLinksBackup(indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup);
							subMngr.updateSubstrateNetworkDK(this.vnHeu, fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksWorking(), 
									fileReaders.getIndexVirtualLinkToBandwidthInpahtForVirtualLinksWorking(), 
									fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksBackup(), fileReaders.getIndexVirtualLinkToBandwidthInpahtForVirtualLinksBackup(), 
									fileReaders.getMappedNodes(), fileReaders.getNodesUsed(), UpdateMode.DECREMENT);
						}
					}else{
						return false;
					}
				}else{
					if((mappedWorkingNodes != null) && fileReaders.wasAccepted()){

						if(fileReaders.wasAccepted()){
							fileReaders.collectAllHeuristicInfoDK(this.vnHeu, this.subNetHeu.getNumOfNodes(), this.subNetHeu, 
									mappedWorkingNodes, mappedBackupNodes, finalSubstrateWorkingAndBackupEdges, 
									finalVirtualWorkingAndBackupEdges);

							fileReaders.setIndexVirtualLinkToPahtsForVirtualLinksWorking(indexVirtualLinkToPahtsForVirtualLinksWorking);
							fileReaders.setIndexVirtualLinkToBandwidthInpahtForVirtualLinksWorking(indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking);
							fileReaders.setIndexVirtualLinkToPahtsForVirtualLinksBackup(indexVirtualLinkToPahtsForVirtualLinksBackup);
							fileReaders.setIndexVirtualLinkToBandwidthInpahtForVirtualLinksBackup(indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup);
							subMngr.updateSubstrateNetworkDK(this.vnHeu, fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksWorking(), 
									fileReaders.getIndexVirtualLinkToBandwidthInpahtForVirtualLinksWorking(), 
									fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksBackup(), fileReaders.getIndexVirtualLinkToBandwidthInpahtForVirtualLinksBackup(), 
									fileReaders.getMappedNodes(), fileReaders.getNodesUsed(), UpdateMode.DECREMENT);
						}
					}else{
						return false;
					}
				}
			}else if (typeOfHeuristic.equals("fullGreed")){

				ArrayList<String> tmp = new ArrayList<>();
				ArrayList<Pair<String>> tmp2 = new ArrayList<>();
				HeuristicFullGreedyDatFileCreator heuristicFullGreedyDatCreator = new HeuristicFullGreedyDatFileCreator();
				for(int p = 0; p < mappedWorkingNodes.size(); p++)
					System.out.println("Left: "+mappedWorkingNodes.get(p).getLeft()+"; Right: "+mappedWorkingNodes.get(p).getRight());
				heuristicFullGreedyDatCreator.createDatFile("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/heuristic/fullgreed/datfiles/randomMCF_req.dat", 
						this.subNetHeu, this.vnHeu, mappedWorkingNodes);

				String partialResult = Utils.runGLPSOLHeuristic("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/heuristic/fullgreed/datfiles/randomMCF_req.dat", MOD_FILE);

				if(partialResult.equals("timeout")){
					fileReaders.setTimeout(true);
					fileReaders.setWasAccepted(false);
				}else if(partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")){

					fileReaders.setWasAccepted(false);
					Utils.writeFile(partialResult, "/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/heuristic/fullgreed/datfiles/randomMCF_req.txt");
				}else{	
					Utils.writeFile(partialResult, "/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/heuristic/fullgreed/datfiles/randomMCF_req.txt");

					fileReaders.collectFullGreedyHeuristicInfo(this.vnHeu, this.subNetHeu.getNumOfNodes(),
							"/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/heuristic/fullgreed/datfiles/randomMCF_req.txt", this.subNetHeu, mappedWorkingNodes);
					if(fileReaders.wasAccepted()){

						for(String s: fileReaders.getNodesUsed()){
							tmp.add(s);
						}

						for(Pair<String> s: fileReaders.getEdgesUsed()){
							tmp2.add(new Pair<String>(s.getLeft(), s.getRight()));
						}
						subMngr.updateSubstrateNetworkDist(this.vnHeu, fileReaders.getMappedEdges(), tmp2, 
								fileReaders.getMappedNodes(), tmp, fileReaders.getBwEdgesUsed(), UpdateMode.DECREMENT, fileReaders);
					}
				}
			}
		}else{
			return false;
		}
		//End of mapping working nodes and links
		//End of mapping backup nodes and links

		this.generateSimplifiedOutput(mappedWorkingNodes, mappedBackupNodes, fileReaders, configFile+".smplout", typeOfHeuristic);
		return res;
	}

	private void generateSimplifiedOutput(ArrayList<Pair<String>> mappedWorkingNodes, ArrayList<Pair<String>> mappedBackupNodes, OutputFileReader fileReaders,
			String simplifiedOutputFile, String typeOfHeuristic) throws IOException {

		FileWriter fileWriter = new FileWriter(simplifiedOutputFile);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		HashMap<Integer, List<String>> listOfKPathsComplyLatency = null;

		for(int i = 0; i < mappedWorkingNodes.size(); i++){
//			if(typeOfHeuristic.equals("Sirius")){
				bufferedWriter.write("Working node of "+mappedWorkingNodes.get(i).getRight()+" -> "+mappedWorkingNodes.get(i).getLeft()+"\n");
				System.out.println("Working node of "+mappedWorkingNodes.get(i).getRight()+" -> "+mappedWorkingNodes.get(i).getLeft()+"\n");
//			}
//			else if(typeOfHeuristic.equals("fullGreed")){
//				bufferedWriter.write("Working node of "+mappedWorkingNodes.get(i).getRight()+" -> "+(mappedWorkingNodes.get(i).getLeft())+"\n");
//				System.out.println("Working node of "+mappedWorkingNodes.get(i).getRight()+" -> "+(mappedWorkingNodes.get(i).getLeft())+"\n");
//			}
		}
		for(int i = 0; i < mappedBackupNodes.size(); i++){
			bufferedWriter.write("Backup node of "+mappedWorkingNodes.get(i).getRight()+" -> "+mappedWorkingNodes.get(i).getLeft()+"\n");
		}
		if((fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksWorking() != null) && (fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksWorking().size() >= 0)){
			if(typeOfHeuristic.equals("Sirius")){
				Iterator<Entry<Integer, HashMap<Integer, List<String>>>> it = fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksWorking().entrySet().iterator();
				while(it.hasNext()){
					Map.Entry pair = it.next();
					int key = (int) pair.getKey();
					listOfKPathsComplyLatency = fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksWorking().get(key);
					ArrayList<Pair<String>> edgesInAPath = new ArrayList<Pair<String>>();
					List<String> path;
					int key2 = -1; 
					Iterator<Entry<Integer, List<String>>> it2 = listOfKPathsComplyLatency.entrySet().iterator();
					while(it2.hasNext()){
						Map.Entry pair2 = it2.next();
						key2 = (int) pair2.getKey();
						path = listOfKPathsComplyLatency.get(key2);
						Utils.copyEdgesUsed(edgesInAPath, path, this.subNetHeu);
						String paths = "";
						for(int i = 0; i < edgesInAPath.size(); i++)
							paths +=" ("+edgesInAPath.get(i).getLeft()+","+edgesInAPath.get(i).getRight()+")";
						bufferedWriter.write("Working links for ("+this.vnHeu.getEdges().get(key).getLeft().toString()+","+
								this.vnHeu.getEdges().get(key).getRight().toString()+") ->"+paths+" \n");
						bufferedWriter.write("Working path for ("+this.vnHeu.getEdges().get(key).getLeft().toString()+","+
								this.vnHeu.getEdges().get(key).getRight().toString()+") -> "+((path.toString().replace("[", "")).replace("]", "").replace(",", ""))+" \n");
						System.out.println("Working links for ("+this.vnHeu.getEdges().get(key).getLeft().toString()+","+
								this.vnHeu.getEdges().get(key).getRight().toString()+") ->"+paths+"\n");
						System.out.println("Working path for ("+this.vnHeu.getEdges().get(key).getLeft().toString()+","+
								this.vnHeu.getEdges().get(key).getRight().toString()+") -> "+((path.toString().replace("[", "")).replace("]", "").replace(",", ""))+"\n");
					}
				}
			}
			else if(typeOfHeuristic.equals("fullGreed")){
				Iterator<Entry<String, ArrayList<Pair<String>>>> it = fileReaders.getIndexVirtualLinkToEdgeUsed().entrySet().iterator();
				while(it.hasNext()){
					Map.Entry pair = it.next();
					String key = (String) pair.getKey();
					ArrayList<Pair<String>> edgesInAPath = fileReaders.getIndexVirtualLinkToEdgeUsed().get(key);
					String paths = "";
					for(int i = 0; i < edgesInAPath.size(); i++)
						paths +=" ("+edgesInAPath.get(i).getLeft()+","+edgesInAPath.get(i).getRight()+")";
					bufferedWriter.write("Working links for ("+this.vnHeu.getEdge(Integer.parseInt(key)).getLeft()+","+this.vnHeu.getEdge(Integer.parseInt(key)).getRight()+") ->"+paths+"\n");
					System.out.print("Working links for ("+this.vnHeu.getEdge(Integer.parseInt(key)).getLeft()+","+this.vnHeu.getEdge(Integer.parseInt(key)).getRight()+") ->"+paths+"\n");
				}
			}
		}
		if((fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksBackup() != null) && (fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksBackup().size() >= 0)){
			Iterator<Entry<Integer, HashMap<Integer, List<String>>>> it = fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksBackup().entrySet().iterator();
			while(it.hasNext()){
				Map.Entry pair = it.next();
				int key = (int) pair.getKey();
				listOfKPathsComplyLatency = fileReaders.getIndexVirtualLinkToPahtsForVirtualLinksWorking().get(key);
				ArrayList<Pair<String>> edgesInAPath = new ArrayList<Pair<String>>();
				List<String> path;
				int key2 = -1; 
				Iterator<Entry<Integer, List<String>>> it2 = listOfKPathsComplyLatency.entrySet().iterator();
				while(it2.hasNext()){
					Map.Entry pair2 = it2.next();
					key2 = (int) pair2.getKey();
					path = listOfKPathsComplyLatency.get(key2);
					Utils.copyEdgesUsed(edgesInAPath, path, this.subNetHeu);
					String paths = "";
					for(int i = 0; i < edgesInAPath.size(); i++)
						paths +=" ("+edgesInAPath.get(i).getLeft()+","+edgesInAPath.get(i).getRight()+")";
					bufferedWriter.write("Backup links for ("+this.vnHeu.getEdges().get(key).getLeft().toString()+","+
							this.vnHeu.getEdges().get(key).getRight().toString()+") ->"+paths+"\n");
					bufferedWriter.write("Backup path for ("+this.vnHeu.getEdges().get(key).getLeft().toString()+","+
							this.vnHeu.getEdges().get(key).getRight().toString()+") -> "+((path.toString().replace("[", "")).replace("]", "").replace(",", ""))+"\n");
					System.out.println("Backup links for ("+this.vnHeu.getEdges().get(key).getLeft().toString()+","+
							this.vnHeu.getEdges().get(key).getRight().toString()+") ->"+paths+"\n");
					System.out.println("Backup path for ("+this.vnHeu.getEdges().get(key).getLeft().toString()+","+
							this.vnHeu.getEdges().get(key).getRight().toString()+") -> "+((path.toString().replace("[", "")).replace("]", "").replace(",", ""))+"\n");
				}
			}
		}
		bufferedWriter.close();
		fileWriter.close();
	}
}
