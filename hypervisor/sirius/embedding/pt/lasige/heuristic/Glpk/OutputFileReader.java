package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Utils;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic.NodeHeuristicInfo;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.SubstrateNetworkHeu;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;

/**
 * Handles the interpretation of the output files that result from
 * the execution of the formulations
 * @authors Luis Ferrolho, fc41914, Max Alaluna fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class OutputFileReader {

	private double backupResources;
	private String executionTime = "0";
	private boolean wasAccepted;
	private ArrayList<Pair<String>> wEdgesUsed;
	private ArrayList<Pair<String>> wMappedEdges;
	private ArrayList<Pair<String>> bEdgesUsed;
	private ArrayList<Pair<String>> bMappedEdges;
	private ArrayList<String> wNodesUsed;
	private ArrayList<String> wMappedNodes;
	private ArrayList<String> bNodesUsed;
	private ArrayList<String> bMappedNodes;
	private ArrayList<Pair<String>> edgesUsed;
	private ArrayList<Pair<String>> mappedEdges;
	private ArrayList<String> nodesUsed;
	private ArrayList<String> mappedNodes;
	private ArrayList<Double> bwEdgesUsed;
	private long nodeWMappingTime = 0;
	private long linkWMappingTime = 0;
	private long nodeBMappingTime = 0;
	private long linkBMappingTime = 0;
	private boolean timeout = false;
	
	HashMap<Integer,HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksWorking = new HashMap<Integer,HashMap<Integer, List<String>>>();
	HashMap<Integer,HashMap<Integer, Double>> indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking = new HashMap<Integer,HashMap<Integer, Double>>();
	HashMap<Integer,HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking = new HashMap<Integer,HashMap<Integer, Double>>();
	HashMap<Integer,HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksBackup = new HashMap<Integer,HashMap<Integer, List<String>>>();
	HashMap<Integer,HashMap<Integer, Double>> indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup = new HashMap<Integer,HashMap<Integer, Double>>();
	HashMap<Integer,HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup = new HashMap<Integer,HashMap<Integer, Double>>();

	private ArrayList<String> virtualEdgeUsed = new ArrayList<>();
	HashMap<String,ArrayList<Pair<String>>> indexVirtualLinkToEdgeUsed = new HashMap<String,ArrayList<Pair<String>>>();

	public OutputFileReader() {
		
		this.wEdgesUsed = new ArrayList<>();
		this.wNodesUsed = new ArrayList<>();
		this.wMappedEdges = new ArrayList<>();
		this.wMappedNodes = new ArrayList<>();
		this.bNodesUsed = new ArrayList<>();
		this.bMappedNodes = new ArrayList<>();
		this.bEdgesUsed = new ArrayList<>();
		this.bMappedEdges = new ArrayList<>();
		this.wasAccepted = true;
		edgesUsed = new ArrayList<>();
		mappedEdges = new ArrayList<>();
		nodesUsed = new ArrayList<>();
		mappedNodes = new ArrayList<>();
		bwEdgesUsed = new ArrayList<>();
	}

	/**
	 * Interprets the results of the execution of the formulations
	 * @param virNet Virtual network that tried the embedding
	 * @param numOfNodes Number of substrate nodes in the substrate network
	 * @param outputFile File that has to be interpreted
	 */
	public void collectAllInfo(VirtualNetworkHeu virNet, int numOfNodes, String outputFile) {

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null;
		Pair<String> tmp = null;
		Pair<String> tmpSub = null;

		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");
				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used"))
					executionTime = parts[2];
				else if(parts[1].contains("r[") && !parts[2].equalsIgnoreCase("0.000000"))
					backupResources += Double.parseDouble(parts[2]);
				else if(parts[1].contains("gama[") && !parts[2].equalsIgnoreCase("0.000000"))
					backupResources += Double.parseDouble(parts[2]);

				if((parts[1].contains("fw[") && !parts[2].equalsIgnoreCase("0.000000")) || 
						(parts[1].contains("fb[") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW
					
					String bwEdgesUsedString = ""+parts[2];

					if(outputFile.contains("SecDep") || outputFile.contains("milpHeu")){ //SECDEP

						tmp = new Pair<>(parts2[1],parts2[2]);
						tmpSub = new Pair<>(parts2[3],parts2[4]);
						
						if(virNet.getEdges().contains(tmp)){
							
							if(parts[1].contains("fw[")){
								wMappedEdges.add(tmp);
								mappedEdges.add(tmp);
								wEdgesUsed.add(tmpSub);
								edgesUsed.add(tmpSub);
								bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
								
							}else if(parts[1].contains("fb[")){
								if(!wEdgesUsed.contains(tmpSub)){
									bMappedEdges.add(tmp);
									mappedEdges.add(tmp);
									bEdgesUsed.add(tmpSub);
									edgesUsed.add(tmpSub);
									bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
								}
							}
						}
					}else{ //DVINE
						parts = parts2[1].split("f");

						tmp = virNet.getEdge(Integer.parseInt(parts[1]));

						String n1 = String.valueOf(Integer.parseInt(tmp.getLeft()) + numOfNodes);
						String n2 = String.valueOf(Integer.parseInt(tmp.getRight()) + numOfNodes);

						if(!parts2[2].equals(n1) && !parts2[2].equals(n2) && !parts2[3].equals(n1) && !parts2[3].equals(n2)){
							wMappedEdges.add(tmp);
							mappedEdges.add(tmp);

							tmp = new Pair<String>(parts2[2],parts2[3]);
							wEdgesUsed.add(tmp);
							edgesUsed.add(tmp);
							bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
						}
					}

				}else if((parts[1].contains("thetaw[") && !parts[2].equalsIgnoreCase("0")) || 
						(parts[1].contains("thetab[") && !parts[2].equalsIgnoreCase("0"))){ //CPU

					if(outputFile.contains("SecDep") || outputFile.contains("milpHeu")){ //SECDEP

						if(parts[1].contains("thetaw[")){
							wMappedNodes.add(parts2[1]);
							wNodesUsed.add(parts2[2]);
							mappedNodes.add(parts2[1]);
							nodesUsed.add(parts2[2]);
						}else if(parts[1].contains("thetab[")){
							if(!wNodesUsed.contains(parts2[2])){
								bMappedNodes.add(parts2[1]);
								bNodesUsed.add(parts2[2]);
								mappedNodes.add(parts2[1]);
								nodesUsed.add(parts2[2]);
							}
						}
					}else{ //DVINE

						String opLeft = String.valueOf(Math.abs(numOfNodes - Integer.parseInt(parts2[1])));
//						String opRight = String.valueOf(Math.abs(numOfNodes - Integer.parseInt(parts2[2])));
//						if(virNet.getNodes().contains(opLeft) && !virNet.getNodes().contains(opRight) && Integer.parseInt(parts2[1]) >= numOfNodes){
						int opRight = Integer.parseInt(parts2[2]);

						if(virNet.getNodes().contains(opLeft) && opRight < numOfNodes && Integer.parseInt(parts2[1]) >= numOfNodes){
							wMappedNodes.add(opLeft);
							wNodesUsed.add(parts2[2]);
							mappedNodes.add(opLeft);
							nodesUsed.add(parts2[2]);
						}
					}
				}
			}
			bufferedReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void collectAllHeuristicInfo(VirtualNetworkHeu virNet, int numOfNodes, String outputFile, 
			SubstrateNetworkHeu subNet, ArrayList<Pair<String>> mappedWorkingNodesHeu, ArrayList<Pair<String>> mappedBackupNodesHeu){

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null, parts3 = null;

		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}
				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");

					parts3 = parts[1].split("_");

					tmp = virNet.getEdge(Integer.parseInt(parts3[2]));

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);

					//if(subNet.getEdges().contains(new Pair<String>(parts3[0],parts3[1]))){
					//tmp = new Pair<String>(parts3[0],parts3[1]);
					//} else if(subNet.getEdges().contains(new Pair<String>(parts3[1],parts3[0]))){
					//tmp = new Pair<String>(parts3[1],parts3[0]);
					//}
					if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3])))){

						tmp = new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3]));

					} else if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2])))){

						tmp = new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2]));
					}
					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}
			executionTime = partialExecutionTime +"";
			bufferedReader.close();

			for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

				wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			}
			if(mappedBackupNodesHeu != null){

				for(int i = 0; i < mappedBackupNodesHeu.size(); i++){

					bMappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
					bNodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
					mappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
					nodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectAllHeuristicInfoMCF(VirtualNetworkHeu virNet, int numOfNodes, String outputFile, 
			SubstrateNetworkHeu subNet, ArrayList<Pair<String>> mappedWorkingNodesHeu, ArrayList<Pair<String>> mappedBackupNodesHeu){

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null, parts3 = null;

		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}
				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");

					parts3 = parts[1].split("_");

					tmp = virNet.getEdge(Integer.parseInt(parts3[2]));

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);

					//if(subNet.getEdges().contains(new Pair<String>(parts3[0],parts3[1]))){
					//tmp = new Pair<String>(parts3[0],parts3[1]);
					//} else if(subNet.getEdges().contains(new Pair<String>(parts3[1],parts3[0]))){
					//tmp = new Pair<String>(parts3[1],parts3[0]);
					//}
					if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3])))){

						tmp = new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3]));

					} else if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2])))){

						tmp = new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2]));
					}
					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}
			executionTime = partialExecutionTime +"";
			bufferedReader.close();

			for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

				wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			}
			if(mappedBackupNodesHeu != null){

				for(int i = 0; i < mappedBackupNodesHeu.size(); i++){

					bMappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
					bNodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
					mappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
					nodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectAllHeuristicInfoSP(VirtualNetworkHeu virNet, int numOfNodes, SubstrateNetworkHeu subNet, 
			ArrayList<Pair<String>> mappedWorkingNodesHeu, ArrayList<Pair<String>> mappedBackupNodesHeu, 
			ArrayList<Pair<String>> finalSubstrateWorkingAndBackupEdges, ArrayList<Pair<String>> finalVirtualWorkingAndBackupEdges){

		cleanAllInfo();

		for (int i = 0; i < finalSubstrateWorkingAndBackupEdges.size(); i++){

			wEdgesUsed.add(new Pair<String>(finalSubstrateWorkingAndBackupEdges.get(i).getLeft(), 
					finalSubstrateWorkingAndBackupEdges.get(i).getRight()));
			edgesUsed.add(new Pair<String>(finalSubstrateWorkingAndBackupEdges.get(i).getLeft(), 
					finalSubstrateWorkingAndBackupEdges.get(i).getRight()));
			wMappedEdges.add(new Pair<String>(finalVirtualWorkingAndBackupEdges.get(i).getLeft(), 
					finalVirtualWorkingAndBackupEdges.get(i).getRight()));
			mappedEdges.add(new Pair<String>(finalVirtualWorkingAndBackupEdges.get(i).getLeft(), 
					finalVirtualWorkingAndBackupEdges.get(i).getRight()));
		}

		for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

			wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
			wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
			nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
		}
		
		if(mappedBackupNodesHeu != null){

			for(int i = 0; i < mappedBackupNodesHeu.size(); i++){
				bMappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
				bNodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
				nodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
			}
		}
	}
	
	public void collectAllHeuristicInfoDK(VirtualNetworkHeu virNet, int numOfNodes, SubstrateNetworkHeu subNet, 
			ArrayList<Pair<String>> mappedWorkingNodesHeu, ArrayList<Pair<String>> mappedBackupNodesHeu, 
			ArrayList<Pair<String>> finalSubstrateWorkingAndBackupEdges, ArrayList<Pair<String>> finalVirtualWorkingAndBackupEdges){

		cleanAllInfo();

		for (int i = 0; i < finalSubstrateWorkingAndBackupEdges.size(); i++){

			wEdgesUsed.add(new Pair<String>(finalSubstrateWorkingAndBackupEdges.get(i).getLeft(), 
					finalSubstrateWorkingAndBackupEdges.get(i).getRight()));
			edgesUsed.add(new Pair<String>(finalSubstrateWorkingAndBackupEdges.get(i).getLeft(), 
					finalSubstrateWorkingAndBackupEdges.get(i).getRight()));
			wMappedEdges.add(new Pair<String>(finalVirtualWorkingAndBackupEdges.get(i).getLeft(), 
					finalVirtualWorkingAndBackupEdges.get(i).getRight()));
			mappedEdges.add(new Pair<String>(finalVirtualWorkingAndBackupEdges.get(i).getLeft(), 
					finalVirtualWorkingAndBackupEdges.get(i).getRight()));
		}

		for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

			wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
			wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
			nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
		}
		
		if(mappedBackupNodesHeu != null){

			for(int i = 0; i < mappedBackupNodesHeu.size(); i++){
				bMappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
				bNodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedBackupNodesHeu.get(i).getRight());
				nodesUsed.add(mappedBackupNodesHeu.get(i).getLeft());
			}
		}
	}

	public void collectDVineHeuristicInfo(VirtualNetworkHeu virNet, int numOfNodes, String outputFile, 
			SubstrateNetworkHeu subNet, ArrayList<Pair<String>> mappedWorkingNodesHeu){

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null;

		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}

				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");

					tmp = virNet.getEdge(Integer.parseInt(parts[1]));

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);

					tmp = new Pair<String>(parts2[2],parts2[3]);
					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}

			executionTime = partialExecutionTime +"";
			bufferedReader.close();

			for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

				wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectFullGreedyHeuristicInfo(VirtualNetworkHeu virNet, int numOfNodes, String outputFile, 
			SubstrateNetworkHeu subNet, ArrayList<Pair<String>> mappedWorkingNodesHeu){

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null;

		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {
			FileReader fileReader = new FileReader(outputFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");

				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION"))
					wasAccepted = false;

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}

				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");

					tmp = virNet.getEdge(Integer.parseInt(parts[1]));
					virtualEdgeUsed.add(parts[1]);

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);

					tmp = new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3]));
					
					if(indexVirtualLinkToEdgeUsed.get(parts[1])== null){
						ArrayList<Pair<String>> edges = new ArrayList<>();
						edges.add(tmp);
						indexVirtualLinkToEdgeUsed.put(parts[1], edges);
					}else{
						ArrayList<Pair<String>> edges = indexVirtualLinkToEdgeUsed.get(parts[1]);
						edges.add(tmp);
						indexVirtualLinkToEdgeUsed.put(parts[1], edges);
					}
					
					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}
			executionTime = partialExecutionTime +"";
			bufferedReader.close();

			for(int i = 0; i < mappedWorkingNodesHeu.size(); i++){

				System.out.println("fileReaders.collectFullGreedyHeuristicInfo ## mappedWorkingNodesHeu.get(i).getRight(): "+mappedWorkingNodesHeu.get(i).getRight()+
						" -- mappedWorkingNodesHeu.get(i).getLeft(): "+ mappedWorkingNodesHeu.get(i).getLeft());
				wMappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodesHeu.get(i).getRight());
				nodesUsed.add(mappedWorkingNodesHeu.get(i).getLeft());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public  HashMap<String, ArrayList<Pair<String>>> getIndexVirtualLinkToEdgeUsed() {
		return indexVirtualLinkToEdgeUsed;
	}

	public void setIndexVirtualLinkToEdgeUsed(HashMap<String, ArrayList<Pair<String>>> indexVirtualLinkToEdgeUsed) {
		this.indexVirtualLinkToEdgeUsed = indexVirtualLinkToEdgeUsed;
	}

	public void collectAllHeuristicWorkingInfoPartial(VirtualNetworkHeu virNet, int numOfNodes, String outputFile, SubstrateNetworkHeu subNet, ArrayList<Pair<String>> mappedNodesHeu) {

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null, parts3 = null;
		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {

			BufferedReader bufferedReader = new BufferedReader(new StringReader(outputFile));

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");
				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					partialExecutionTime += Double.valueOf(parts[2]);
				}
				
				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];
					parts = parts2[1].split("f");
					parts3 = parts[1].split("_");

					tmp = virNet.getEdge(Integer.parseInt(parts3[2]));

					wMappedEdges.add(tmp);
					mappedEdges.add(tmp);

					tmp = new Pair<String>(subNet.getNode(Integer.parseInt(parts2[2])),subNet.getNode(Integer.parseInt(parts2[3])));

					wEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}

			executionTime = partialExecutionTime +"";

			bufferedReader.close();

			for(int i = 0; i < mappedNodesHeu.size(); i++){

				wMappedNodes.add(mappedNodesHeu.get(i).getRight());
				wNodesUsed.add(mappedNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedNodesHeu.get(i).getRight());
				nodesUsed.add(mappedNodesHeu.get(i).getLeft());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectAllHeuristicWorkingInfoPartial(ArrayList<Pair<String>> mappedWorkingNodes, ArrayList<Pair<String>> mappedEdges, 
			ArrayList<Pair<String>> edgeUsed, long partialExecutionTime) {

		cleanAllInfo();

		executionTime += partialExecutionTime;
		//wMappedEdges.add(new Pair<String>(mappedEdges.get(0).getLeft(), mappedEdges.get(0).getRight()));
		//for(int i = 0; i < mappedEdges.size(); i++){
		//wMappedEdges.add(new Pair<String>(mappedEdges.get(i).getLeft(), mappedEdges.get(i).getRight()));
		//}

		for(int i = 0; i < edgeUsed.size(); i++){
			wEdgesUsed.add(new Pair<String>(edgeUsed.get(i).getLeft(), edgeUsed.get(i).getRight()));
			wMappedEdges.add(new Pair<String>(mappedEdges.get(0).getLeft(), mappedEdges.get(0).getRight()));

		}

		if(mappedWorkingNodes != null){
			for(int i = 0; i < mappedWorkingNodes.size(); i++){

				wMappedNodes.add(mappedWorkingNodes.get(i).getRight());
				wNodesUsed.add(mappedWorkingNodes.get(i).getLeft());
				mappedNodes.add(mappedWorkingNodes.get(i).getRight());
				nodesUsed.add(mappedWorkingNodes.get(i).getLeft());
			}
		}
	}


	public void collectAllHeuristicBackupInfoPartial(VirtualNetworkHeu virNet, int numOfNodes, String outputFile, SubstrateNetworkHeu subNet, ArrayList<Pair<String>> mappedNodesHeu) {

		cleanAllInfo();

		String line = null;
		String[] parts = null, parts2 = null, parts3 = null;
		Pair<String> tmp = null;

		try {

			BufferedReader bufferedReader = new BufferedReader(new StringReader(outputFile));

			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");
				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){

					executionTime += parts[2];
				}

				if((parts[1].contains("fw") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW

					String bwEdgesUsedString = ""+parts[2];

					parts = parts2[1].split("f");

					parts3 = parts[1].split("_");

					tmp = virNet.getEdge(Integer.parseInt(parts3[2]));

					bMappedEdges.add(tmp);
					mappedEdges.add(tmp);

					//if(subNet.getEdges().contains(new Pair<String>(parts3[0],parts3[1]))){
					//tmp = new Pair<String>(parts3[0],parts3[1]);
					//} else if(subNet.getEdges().contains(new Pair<String>(parts3[1],parts3[0]))){
					//tmp = new Pair<String>(parts3[1],parts3[0]);
					//}
					//if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[2]),Utils.convertToAlphabet(parts2[3])))){
					tmp = new Pair<String>(subNet.getNode(Integer.parseInt(parts2[2])),subNet.getNode(Integer.parseInt(parts2[3])));
					//} else if(subNet.getEdges().contains(new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2])))){
					//tmp = new Pair<String>(Utils.convertToAlphabet(parts2[3]),Utils.convertToAlphabet(parts2[2]));
					//}
					bEdgesUsed.add(tmp);
					edgesUsed.add(tmp);
					bwEdgesUsed.add(Double.valueOf(bwEdgesUsedString));
				}
			}

			bufferedReader.close();

			for(int i = 0; i < mappedNodesHeu.size(); i++){
				bMappedNodes.add(mappedNodesHeu.get(i).getRight());
				bNodesUsed.add(mappedNodesHeu.get(i).getLeft());
				mappedNodes.add(mappedNodesHeu.get(i).getRight());
				nodesUsed.add(mappedNodesHeu.get(i).getLeft());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectAllHeuristicBackupInfoPartial(ArrayList<Pair<String>> mappedBackupNodes, ArrayList<Pair<String>> mappedEdges, 
			ArrayList<Pair<String>> edgeUsed, long partialExecutionTime) {
		cleanAllInfo();

		executionTime += partialExecutionTime;
		for(int i = 0; i < edgeUsed.size(); i++){
			bEdgesUsed.add(new Pair<String>(edgeUsed.get(i).getLeft(), edgeUsed.get(i).getRight()));
			bMappedEdges.add(new Pair<String>(mappedEdges.get(0).getLeft(), mappedEdges.get(0).getRight()));
		}

		if(mappedBackupNodes != null){

			for(int i = 0; i < mappedBackupNodes.size(); i++){
				bMappedNodes.add(mappedBackupNodes.get(i).getRight());
				bNodesUsed.add(mappedBackupNodes.get(i).getLeft());
				mappedNodes.add(mappedBackupNodes.get(i).getRight());
				nodesUsed.add(mappedBackupNodes.get(i).getLeft());
			}
		}
	}

	public HashMap<Integer, ArrayList<NodeHeuristicInfo>> populateDVineHeuristicWorkingInfo(
			VirtualNetworkHeu virtualNetwork, int numOfNodes,
			String partialResult, SubstrateNetworkHeu subNet) {

		cleanAllInfo();

		HashMap<Integer, ArrayList<NodeHeuristicInfo>> virtualNodeIndexArraySubstrateNodeInfoMap = new HashMap<Integer, ArrayList<NodeHeuristicInfo>>();
		ArrayList<NodeHeuristicInfo> aux;
		String line = null;
		String flow = "";
		String x = "";
		String[] parts = null, parts2 = null;

		try {

			BufferedReader bufferedReader = new BufferedReader(new StringReader(partialResult));
			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted){
				parts = line.split(" +");
				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");
				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION")){
					return null;
				}

				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){
					executionTime = parts[2];
				}

				if((parts[1].contains("fw[") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW
					//					linesf += line+"\n";
					flow = parts[2];
					parts = parts2[1].split("f");
					//tmp = virtualNetwork.getEdge(Integer.parseInt(parts[1]));
					if((subNet.getNodes().contains(Utils.convertToAlphabet(parts2[2])) && !subNet.getNodes().contains(Utils.convertToAlphabet(parts2[3])))){

						if(virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[3]))== null){
							aux = new ArrayList<NodeHeuristicInfo>();
							aux.add(new NodeHeuristicInfo(Integer.parseInt(parts2[2]), Integer.parseInt(parts[1]), -1.0, Double.parseDouble(flow)));
							virtualNodeIndexArraySubstrateNodeInfoMap.put(Integer.parseInt(parts2[3]), aux);
						}else{
							aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[3]));
							aux.add(new NodeHeuristicInfo(Integer.parseInt(parts2[2]), Integer.parseInt(parts[1]), -1.0, Double.parseDouble(flow)));
						}
					}else{
						if((!subNet.getNodes().contains(Utils.convertToAlphabet(parts2[2])) && subNet.getNodes().contains(Utils.convertToAlphabet(parts2[3])))){
							if(virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[2]))== null){
								aux = new ArrayList<NodeHeuristicInfo>();
								aux.add(new NodeHeuristicInfo(Integer.parseInt(parts2[3]), Integer.parseInt(parts[1]), -1.0, Double.parseDouble(flow)));
								virtualNodeIndexArraySubstrateNodeInfoMap.put(Integer.parseInt(parts2[2]), aux);
							}else{
								aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[2]));
								aux.add(new NodeHeuristicInfo(Integer.parseInt(parts2[3]), Integer.parseInt(parts[1]), -1.0, Double.parseDouble(flow)));
							}
						}
					}

				}else if((parts[1].contains("thetaw[") && !parts[2].equalsIgnoreCase("0.000000"))){
					x = parts[2];
					if((subNet.getNodes().contains(Utils.convertToAlphabet(parts2[1])) && !subNet.getNodes().contains(Utils.convertToAlphabet(parts2[2])))){
						if(virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[2]))!= null){
							aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[2]));
							for(int i = 0; i < aux.size() ; i++){
								if(aux.get(i).getIndexNode() == Integer.parseInt(parts2[1])){
									aux.get(i).setX(Double.parseDouble(x));
								}
							}
						}else{
							if((!subNet.getNodes().contains(Utils.convertToAlphabet(parts2[1])) && subNet.getNodes().contains(Utils.convertToAlphabet(parts2[2])))){
								if(virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[1]))!= null){
									aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(Integer.parseInt(parts2[1]));

									for(int i = 0; i < aux.size() ; i++){
										if(aux.get(i).getIndexNode() == Integer.parseInt(parts2[2])){
											aux.get(i).setX(Double.parseDouble(x));
										}
									}
								}
							}
						}
					}
				}
			}
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return virtualNodeIndexArraySubstrateNodeInfoMap;
	}

	public String populateDVineHeuristicWorkingInfoTest(
			VirtualNetworkHeu virtualNetwork, int numOfNodes,
			String partialResult, SubstrateNetworkHeu subNet) {

		cleanAllInfo();

		String line = null;
		String linesf = "";
		String linesx = "";
		String[] parts = null, parts2 = null;
		try {
			BufferedReader bufferedReader = new BufferedReader(new StringReader(partialResult));
			while( (line = bufferedReader.readLine()) != null && !line.isEmpty() && wasAccepted) {
				parts = line.split(" +");

				// Don't need lines with one word or less
				if(parts.length < 2)
					continue;

				parts2 = parts[1].split("\\[|\\]|,");
				// Check if the embedding was successful
				if(line.equals("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
						line.equals("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
						line.equals("LP HAS NO PRIMAL FEASIBLE SOLUTION")){
					return null;
				}
				// Read time execution and the quantity of resources used for backup
				if(parts[0].equalsIgnoreCase("time") && parts[1].contains("used")){
					executionTime = parts[2];
				}
				if((parts[1].contains("fw[") && !parts[2].equalsIgnoreCase("0.000000"))){ //BW
					linesf += line+"\n";
					parts = parts2[1].split("f");
				}else if((parts[1].contains("thetaw[") && !parts[2].equalsIgnoreCase("0.000000"))){ //CPU
					linesx += line+"\n";
				}
			}
			bufferedReader.close();
			return linesf+"\n"+linesx;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Empty all attributes
	 */
	public void cleanAllInfo() {
		wasAccepted = true;
		wEdgesUsed.clear();
		wMappedEdges.clear();
		bEdgesUsed.clear();
		bMappedEdges.clear();
		wNodesUsed.clear();
		wMappedNodes.clear();
		bNodesUsed.clear();
		bMappedNodes.clear();
		backupResources = 0;
		mappedEdges.clear();
		edgesUsed.clear();
		mappedNodes.clear();
		nodesUsed.clear();
		bwEdgesUsed.clear();
	}

	public double getBackupResourcesQuantity() {
		return backupResources;
	}

	public String getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(String time) {
		this.executionTime = time;
	}

	public boolean wasAccepted() {
		return wasAccepted;
	}

	public void setWasAccepted(boolean wasAccepted) {
		this.wasAccepted = wasAccepted;
	}

	public ArrayList<Pair<String>> getwEdgesUsed() {
		return wEdgesUsed;
	}

	public ArrayList<Pair<String>> getwMappedEdges() {
		return wMappedEdges;
	}

	public ArrayList<Pair<String>> getEdgesUsed() {
		return edgesUsed;
	}

	public ArrayList<Pair<String>> getMappedEdges() {
		return mappedEdges;
	}

	public ArrayList<Pair<String>> getbEdgesUsed() {
		return bEdgesUsed;
	}

	public ArrayList<Pair<String>> getbMappedEdges() {
		return bMappedEdges;
	}

	public ArrayList<String> getwNodesUsed() {
		return wNodesUsed;
	}

	public ArrayList<String> getwMappedNodes() {
		return wMappedNodes;
	}

	public ArrayList<Double> getBwEdgesUsed() {
		return bwEdgesUsed;
	}

	public void setwMappedNodes(ArrayList<String> wMappedNodes) {
		this.wMappedNodes = wMappedNodes;
	}

	public ArrayList<String> getNodesUsed() {
		return nodesUsed;
	}

	public ArrayList<String> getMappedNodes() {
		return mappedNodes;
	}

	public ArrayList<String> getbNodesUsed() {
		return bNodesUsed;
	}

	public ArrayList<String> getbMappedNodes() {
		return bMappedNodes;
	}

	public void setEdgesUsed(ArrayList<Pair<String>> edgesUsed) {

		for(int i = 0; i < edgesUsed.size(); i++)
			this.edgesUsed.add(new Pair<String>(edgesUsed.get(i).getLeft(), 
					edgesUsed.get(i).getRight()));
	}

	public void setMappedEdges(ArrayList<Pair<String>> mappedEdges) {

		for(int i = 0; i < edgesUsed.size(); i++)
			this.mappedEdges.add(new Pair<String>(mappedEdges.get(i).getLeft(), 
					mappedEdges.get(i).getRight()));
	}

	public long getNodeWMappingTime() {
		return nodeWMappingTime;
	}

	public void setNodeWMappingTime(long nodeMappingTime) {
		this.nodeWMappingTime = nodeMappingTime;
	}

	public long getLinkWMappingTime() {
		return linkWMappingTime;
	}

	public void setLinkWMappingTime(long linkMappingTime) {
		if(linkMappingTime >= 0)
			this.linkWMappingTime = linkMappingTime;
		else
			this.linkWMappingTime = 0;
	}

	public long getNodeBMappingTime() {
		return nodeBMappingTime;
	}

	public void setNodeBMappingTime(long nodeBMappingTime) {
		this.nodeBMappingTime = nodeBMappingTime;
	}

	public long getLinkBMappingTime() {
		return linkBMappingTime;
	}

	public void setLinkBMappingTime(long linkBMappingTime) {
		this.linkBMappingTime = linkBMappingTime;
	}

	public boolean isTimeout() {
		return timeout;
	}

	public void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}

	public HashMap<Integer, HashMap<Integer, List<String>>> getIndexVirtualLinkToPahtsForVirtualLinksWorking() {
		return indexVirtualLinkToPahtsForVirtualLinksWorking;
	}

	public void setIndexVirtualLinkToPahtsForVirtualLinksWorking(
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksWorking) {
		this.indexVirtualLinkToPahtsForVirtualLinksWorking.putAll(indexVirtualLinkToPahtsForVirtualLinksWorking);
	}

	public HashMap<Integer, HashMap<Integer, Double>> getIndexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking() {
		return indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking;
	}

	public void setIndexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking(
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking) {
		this.indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking.putAll(indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksWorking);
	}

	public HashMap<Integer, HashMap<Integer, Double>> getIndexVirtualLinkToBandwidthInpahtForVirtualLinksWorking() {
		return indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking;
	}

	public void setIndexVirtualLinkToBandwidthInpahtForVirtualLinksWorking(
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking) {
		this.indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking.putAll(indexVirtualLinkToBandwidthInpahtForVirtualLinksWorking);
	}

	public HashMap<Integer, HashMap<Integer, List<String>>> getIndexVirtualLinkToPahtsForVirtualLinksBackup() {
		return indexVirtualLinkToPahtsForVirtualLinksBackup;
	}

	public void setIndexVirtualLinkToPahtsForVirtualLinksBackup(
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksBackup) {
		this.indexVirtualLinkToPahtsForVirtualLinksBackup.putAll(indexVirtualLinkToPahtsForVirtualLinksBackup);
	}

	public HashMap<Integer, HashMap<Integer, Double>> getIndexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup() {
		return indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup;
	}

	public void setIndexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup(
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup) {
		this.indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup.putAll(indexVirtualLinkToSmallerBandwidthInpathForVirtualLinksBackup);
	}

	public HashMap<Integer, HashMap<Integer, Double>> getIndexVirtualLinkToBandwidthInpahtForVirtualLinksBackup() {
		return indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup;
	}

	public void setIndexVirtualLinkToBandwidthInpahtForVirtualLinksBackup(
			HashMap<Integer, HashMap<Integer, Double>> indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup) {
		this.indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup.putAll(indexVirtualLinkToBandwidthInpahtForVirtualLinksBackup);
	}

	public ArrayList<String> getVirtualEdgeUsed() {
		return virtualEdgeUsed;
	}

	public void setVirtualEdgeUsed(ArrayList<String> virtualEdgeUsed) {
		this.virtualEdgeUsed = virtualEdgeUsed;
	}	
}
