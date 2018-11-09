package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.DisjointKShortestPath.bhandari.EdgeDisjointShortestPair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.DisjointKShortestPath.graph.Device;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.DisjointKShortestPath.graph.Link;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.DisjointKShortestPath.graph.Network;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.KShortestPath.model.Graph;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.KShortestPath.model.VariableGraph;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.KShortestPath.model.abstracts.BaseVertex;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.ShortestPath.Edge;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.ShortestPath.GraphShortestPath;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.ShortestPath.Vertex;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk.DVineDatFileCreator;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk.OutputFileReader;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic.LinkHeuristicInfo;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic.NodeHeuristicInfo;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic.SecLoc;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.SubstrateNetworkHeu;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;

/**
 * Util funtions needed by the simulator
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class Utils {

	static Random random = new Random();

	public static String convertToAlphabet(String number) {
		int n = Integer.parseInt(number);
		int mod = 0, tmp = n;

		String res = "";

		if(tmp == 0)
			return "A";

		while (tmp != 0) {
			mod = tmp % 26;
			res = ((char) (65 + mod)) + res;
			tmp /= 26;
		}
		return res;
	}

	public static int convertFromAlphabet(String word) {
		int result = 0, power = 0, mantissa = 0;

		for (int i = word.length() - 1; i >= 0; i--) {
			mantissa = word.charAt(i) - 65;
			result += mantissa * Math.pow(26, power++);
		}
		return result;
	}

	//TODO Uncomment if there is no file at ../gt-itm/graphs/alt_files/random

	public static void generateAltFiles() {
		try {
			Process p = Runtime.getRuntime().exec("/home/secdepvne-master/gt-itm/Runall.sh");
			p.waitFor();
			System.out.println("End gen.");
		}catch (InterruptedException e) {
			e.printStackTrace();    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void generateAltFiles(String experimentSets) {
		try {
			Process p = Runtime.getRuntime().exec("/home/secdepvne-master/gt-itm/Runall_"+experimentSets+".sh");
			p.waitFor();
			System.out.println("End "+experimentSets+" genenerate.");
		}catch (InterruptedException e) {
			e.printStackTrace();    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double roundDecimals(double d) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		DecimalFormat twoDForm = new DecimalFormat("##.####", otherSymbols);

		return Double.valueOf(twoDForm.format(d));
	}

	public static double roundDownDecimals(double d) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		DecimalFormat twoDForm = new DecimalFormat("##.####", otherSymbols);
		twoDForm.setRoundingMode(RoundingMode.DOWN);

		return Double.valueOf(twoDForm.format(d));
	}

	public static double roundUpDecimals(double d) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		DecimalFormat twoDForm = new DecimalFormat("##.####", otherSymbols);
		twoDForm.setRoundingMode(RoundingMode.UP);

		return Double.valueOf(twoDForm.format(d));
	}

	public static double truncateDecimal(double x){

		if ( x > 0) {
			return roundUpDecimals(x);
		} else {
			return roundDownDecimals(x);
		}
	}

	/**
	 * Run the formulation over an input file
	 * @param datFile The input file
	 * @param modFile The formulation
	 * @param outputFile The output file with the results
	 * @return True if it finished before timeout, false otherwise
	 */
	public static boolean runGLPSOL(String datFile, String modFile, String outputFile) {		

		int TIMEOUT = 600;

		try {
			ProcessBuilder builder = new ProcessBuilder("glpsol","--model", modFile, "--data", datFile);
			builder.redirectOutput(new File(outputFile));

			Process p = builder.start();

			// Establish a timer to not allow the mip to run more than TIMEOUT
			long now = System.currentTimeMillis(); 
			long timeoutInMillis = 1000L * TIMEOUT; 
			long finish = now + timeoutInMillis; 

			while(isAlive(p)){

				Thread.sleep(10);

				if (System.currentTimeMillis() > finish){
					System.out.println("!!! Timeout while solving this request !!! "+modFile);
					p.destroy();
					return false;
				}
			}
		}catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static boolean runGLPSOL(String datFile, String modFile, String outputFile, int timeout) {		

		int TIMEOUT = timeout;

		try {
			ProcessBuilder builder = new ProcessBuilder("glpsol","--model", modFile, "--data", datFile);
			builder.redirectOutput(new File(outputFile));

			Process p = builder.start();

			// Establish a timer to not allow the mip to run more than TIMEOUT
			long now = System.currentTimeMillis(); 
			long timeoutInMillis = 1000L * TIMEOUT; 
			long finish = now + timeoutInMillis; 

			while(isAlive(p)){

				Thread.sleep(10);

				if (System.currentTimeMillis() > finish){
					System.out.println("!!! Timeout while solving this request !!! "+modFile);
					p.destroy();
					return false;
				}
			}
		}catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static String runGLPSOLHeuristic(String datFile, String modFile){		

		int TIMEOUT = 300;
		String result = "";

		try {
			ProcessBuilder builder = new ProcessBuilder("glpsol","--model", modFile, "--data", datFile);

			Process p = builder.start();

			//BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			// Establish a timer to not allow the mip to run more than TIMEOUT
			long now = System.currentTimeMillis(); 
			long timeoutInMillis = 1000L * TIMEOUT; 
			long finish = now + timeoutInMillis; 

			while(isAlive(p)){

				Thread.sleep(10);

				if (System.currentTimeMillis() > finish){
					System.out.println("Timeout while solving this request!!!");
					p.destroy();
					return "timeout";
				}
			}

			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			while ( (line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(System.getProperty("line.separator"));
			}

			result = stringBuilder.toString();

		}catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static String readFile(String path, Charset encoding) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	// Stops the mip execution if there is a timeout
	private static boolean isAlive(Process p) {  
		try{  
			p.exitValue();  
			return false;  
		}catch (IllegalThreadStateException e) {  
			return true;  
		}  
	}

	public static void populateSubstrateInfo(SubstrateNetworkHeu subNetAux,
			SubstrateNetworkHeu subNet){

		for (int i = 0; i < subNet.getNodes().size(); i++){

			if(!subNetAux.getNodes().contains(subNet.getNode(i))){
				subNetAux.getNodes().add(subNet.getNode(i));
			}
		}
		
		subNetAux.setnNodesInClouds(subNet.getnNodesInClouds());

		for (int i = 0; i < subNet.getEdges().size(); i++){
			//TODO Verificar a possibilidade de retirar esta verificação de contains
			//if(!subNetAux.getEdges().contains(new Pair<String>(subNet.getEdge(i).getLeft(), subNet.getEdge(i).getRight()))){
			subNetAux.getEdges().add(new Pair<String>(subNet.getEdge(i).getLeft(), subNet.getEdge(i).getRight()));
			//}
		}

		for (int i = 0; i < subNet.getEdgesBw().size(); i++){

			subNetAux.getEdgesBw().add(subNet.getEdgeBw(i));
		}

		for (int i = 0; i < subNet.getEdgesLatency().size(); i++){

			subNetAux.getEdgesLatency().add(subNet.getEdgeLatency(i));
		}

		for (int i = 0; i < subNet.getEdgesSec().size(); i++){

			subNetAux.getEdgesSec().add(subNet.getEdgeSec(i));
		}		

		for (int i = 0; i < subNet.getNodesSec().size(); i++){

			subNetAux.getNodesSec().add(subNet.getNodesSec().get(i));
		}
		
		for (int i = 0; i < subNet.getCloudsSecurity().size(); i++){

			subNetAux.getCloudsSecurity().add(subNet.getCloudsSecurity().get(i));
		}

		for (String key: subNet.getCloudSecSup().keySet()){

			subNetAux.getCloudSecSup().put(key, subNet.getCloudSecSup().get(key));
		}

		for (int i = 0; i < subNet.getNodesCPU().size(); i++){

			subNetAux.getNodesCPU().add(subNet.getNodesCPU().get(i));
		}

		subNetAux.setNClouds(subNet.getNClouds());

		if(subNet.getnPrivateDataCenters()>0){
			int[][] doesItBelong = new int[subNet.getNClouds()+subNet.getnPrivateDataCenters()][subNet.getNumOfNodes()];

			for(int i = 0; i < subNet.getNClouds()+subNet.getnPrivateDataCenters(); i++){
				for(int j = 0; j < subNet.getNumOfNodes(); j++){
					doesItBelong[i][j] = subNet.getDoesItBelong()[i][j];
				}
			}
			subNetAux.setDoesItBelong(doesItBelong);
			subNetAux.setnPrivateDataCenters(subNet.getnPrivateDataCenters());
		}else{
			int[][] doesItBelong = new int[subNet.getNClouds()][subNet.getNumOfNodes()];

			for(int i = 0; i < subNet.getNClouds(); i++){
				for(int j = 0; j < subNet.getNumOfNodes(); j++){
					doesItBelong[i][j] = subNet.getDoesItBelong()[i][j];
				}
			}
			subNetAux.setDoesItBelong(doesItBelong);
		}
		subNetAux.setTotalNodesCPU(subNet.getTotalNodesCPU());
		subNetAux.setTotalEdgesBw(subNet.getTotalEdgesBw());

		for(int i = 0; i < subNet.getAcceptNodesEmbedding().size(); i++){
			subNetAux.getAcceptNodesEmbedding().add(subNet.getAcceptNodesEmbedding().get(i));
		}
	}

	public static void writeFile(String info, String file){

		try {

			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(info);

			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void desableWorkingNodesAndLinks(SubstrateNetworkHeu subNetAux, ArrayList<Pair<String>> mappedWorkingNodes, ArrayList<Pair<String>> mappedWorkingEdges) {

		for(int i = 0; i < mappedWorkingNodes.size(); i++){

			subNetAux.getNodesCPU().set(subNetAux.getNodes().indexOf(mappedWorkingNodes.get(i).getLeft()), 0.0);
		}

		Pair<String> tmp;

		for(int i = 0; i < mappedWorkingEdges.size(); i++){

			int a = subNetAux.getEdges().indexOf(mappedWorkingEdges.get(i));

			if(subNetAux.getEdges().contains(mappedWorkingEdges.get(i))){
				subNetAux.getEdgesBw().set(subNetAux.getEdges().indexOf(mappedWorkingEdges.get(i)), 0.0);
			} else{
				tmp = new Pair<String>(mappedWorkingEdges.get(i).getRight(), mappedWorkingEdges.get(i).getLeft());
				a = subNetAux.getEdges().indexOf(tmp);
				if(subNetAux.getEdges().contains(tmp)){
					subNetAux.getEdgesBw().set(subNetAux.getEdges().indexOf(tmp), 0.0);
				}
			}
		}
	}

	public static void desableWorkingNodesAndLinksDK(SubstrateNetworkHeu subNetAux, ArrayList<Pair<String>> mappedWorkingNodes, 
			HashMap<Integer, HashMap<Integer, List<String>>> indexVirtualLinkToPahtsForVirtualLinksWorking) {

		for(int i = 0; i < mappedWorkingNodes.size(); i++){

			subNetAux.getNodesCPU().set(subNetAux.getNodes().indexOf(mappedWorkingNodes.get(i).getLeft()), 0.0);
		}

		HashMap<Integer, List<String>> pathsDK = null;
		List<String> path = null;
		Pair<String> tmp;
		String left = "";
		String right = "";
		Pair<String> tmpEdge = null;
		boolean belongsToToRInPrivateDataCenter = false;
		ArrayList<Pair<String>> specialLinksFromCloudsToPrivateDatacenters = new  ArrayList<Pair<String>>();
		specialLinksFromCloudsToPrivateDatacenters.add(new Pair<String>(Utils.convertToAlphabet("199"), Utils.convertToAlphabet("600")));
		specialLinksFromCloudsToPrivateDatacenters.add(new Pair<String>(Utils.convertToAlphabet("199"), Utils.convertToAlphabet("624")));
		specialLinksFromCloudsToPrivateDatacenters.add(new Pair<String>(Utils.convertToAlphabet("199"), Utils.convertToAlphabet("696")));
		specialLinksFromCloudsToPrivateDatacenters.add(new Pair<String>(Utils.convertToAlphabet("299"), Utils.convertToAlphabet("601")));
		specialLinksFromCloudsToPrivateDatacenters.add(new Pair<String>(Utils.convertToAlphabet("299"), Utils.convertToAlphabet("625")));
		specialLinksFromCloudsToPrivateDatacenters.add(new Pair<String>(Utils.convertToAlphabet("299"), Utils.convertToAlphabet("697")));
		specialLinksFromCloudsToPrivateDatacenters.add(new Pair<String>(Utils.convertToAlphabet("599"), Utils.convertToAlphabet("602")));
		specialLinksFromCloudsToPrivateDatacenters.add(new Pair<String>(Utils.convertToAlphabet("599"), Utils.convertToAlphabet("626")));
		specialLinksFromCloudsToPrivateDatacenters.add(new Pair<String>(Utils.convertToAlphabet("599"), Utils.convertToAlphabet("698")));

		Iterator<Entry<Integer, HashMap<Integer, List<String>>>> it = indexVirtualLinkToPahtsForVirtualLinksWorking.entrySet().iterator();

		while(it.hasNext()){
			Map.Entry pair = it.next();
			pathsDK = (HashMap<Integer, List<String>>) pair.getValue();

			Iterator<Entry<Integer, List<String>>> it2 = pathsDK.entrySet().iterator();

			while(it2.hasNext()){
				Map.Entry pair2 = it2.next();
				path =  (List<String>) pair2.getValue();

				Iterator<String> it3 = path.iterator();

				if(it3 != null){
					left = it3.next();
				}
				while(it3.hasNext()){
					right = it3.next();
					tmpEdge = new Pair<String>(left, right);
					//Identifies if a link belongs to a ToR in a Private Data Center
					if((convertFromAlphabet(right)>subNetAux.getnNodesInClouds() && subNetAux.getAcceptNodeEmbedding(convertFromAlphabet(right)))
							|| (convertFromAlphabet(left)>subNetAux.getnNodesInClouds() && subNetAux.getAcceptNodeEmbedding(convertFromAlphabet(left)))){
						belongsToToRInPrivateDataCenter = true;
					}
					//In case of a link belongs to a ToR in a Private Data Center, it is not excluded	
					if(subNetAux.getEdges().contains(tmpEdge) && !belongsToToRInPrivateDataCenter){
						subNetAux.getEdgesBw().set(subNetAux.getEdges().indexOf(tmpEdge), 0.0);
					} else{
						tmp = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

						if(subNetAux.getEdges().contains(tmp) && !belongsToToRInPrivateDataCenter){
							subNetAux.getEdgesBw().set(subNetAux.getEdges().indexOf(tmp), 0.0);
						}
					}
					left = right;
				}
			}
		}
	}

	public static void desableLinksDK(SubstrateNetworkHeu subNetAux, List<String> path) {

		Pair<String> tmp;
		String left = "";
		String right = "";
		Pair<String> tmpEdge = null;

		Iterator<String> it3 = path.iterator();
		if(it3 != null){
			left = it3.next();
		}
		while(it3.hasNext()){
			right = it3.next();
			tmpEdge = new Pair<String>(left, right);

			if(subNetAux.getEdges().contains(tmpEdge)){
				subNetAux.getEdgesBw().remove(subNetAux.getEdges().indexOf(tmpEdge));
				subNetAux.getEdges().remove((subNetAux.getEdges().indexOf(tmpEdge)));
			} else{
				tmp = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

				if(subNetAux.getEdges().contains(tmp)){
					subNetAux.getEdgesBw().remove(subNetAux.getEdges().indexOf(tmp));
					subNetAux.getEdges().remove((subNetAux.getEdges().indexOf(tmp)));
				}
			}
			left = right;
		}		
	}

	public static void copyMappedEdges(
			ArrayList<Pair<String>> mappedWorkingEdges,
			ArrayList<Pair<String>> mappedEdges) {

		for(int i = 0; i < mappedEdges.size(); i++){

			mappedWorkingEdges.add(new Pair<String>(mappedEdges.get(i).getLeft(), mappedEdges.get(i).getRight()));
		}
	}
	
	public static HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> calculateUtility(SubstrateNetworkHeu subNet, 
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate){

		ArrayList<Double> nodeUtility = new ArrayList<Double>();
		double lambda = 1;
		double kn = 1;
		double percentageCPUNodeAux;
		double sumPercentageTotalEdgesAux;
		double nodeUtilityAux = 0;
		HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> secLocNodeHeuristicInfoArrayMap = new HashMap<SecLoc, ArrayList<NodeHeuristicInfo>>();

		securityValuesSubstrate.clear();
		locationValuesSubstrate.clear();

		for (int i = 0; i < subNet.getNumOfNodes(); i++){
			percentageCPUNodeAux = subNet.getNodeCPU(i)/subNet.getTotalNodeCPU(i);
			sumPercentageTotalEdgesAux = 0;

			if(securityValuesSubstrate != null)
				if(!(securityValuesSubstrate.contains(subNet.getNodeSec(i))))
					securityValuesSubstrate.add(subNet.getNodeSec(i));
			if(locationValuesSubstrate != null)
				if(!(locationValuesSubstrate.contains(subNet.getCloudSecSup().get(subNet.getNode(i)))))
					locationValuesSubstrate.add(subNet.getCloudSecSup().get(subNet.getNode(i)));

			for(int j = 0; j < subNet.getNumOfEdges(); j++)
				if ((subNet.getNode(i).equals(subNet.getEdge(j).getLeft()))||
						(subNet.getNode(i).equals(subNet.getEdge(j).getRight())))
					sumPercentageTotalEdgesAux += subNet.getEdgeBw(j)/subNet.getTotalEdgeBw(j);

			nodeUtilityAux = lambda*percentageCPUNodeAux*sumPercentageTotalEdgesAux*kn*
					(1/(subNet.getNodeSec(i)*subNet.getCloudSecSup().get(subNet.getNode(i))));
			nodeUtility.add(nodeUtilityAux);

			SecLoc secLocAux = new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)));
			int cloud = -1;
			if(secLocNodeHeuristicInfoArrayMap.get(secLocAux) == null){
				ArrayList<NodeHeuristicInfo> aux = new ArrayList<NodeHeuristicInfo>();
				for (int k = 0; k < subNet.getNClouds(); k++){
					if (subNet.getDoesItBelong(k, i) == 1){
						cloud = k;
					}
				}
				aux.add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
				secLocNodeHeuristicInfoArrayMap.put(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i))), aux);
			}else{
				for (int k = 0; k < subNet.getNClouds(); k++){
					if (subNet.getDoesItBelong(k, i) == 1){
						cloud = k;
					}
				}
				secLocNodeHeuristicInfoArrayMap.get(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)))).add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
			}
		}
		Collections.sort(securityValuesSubstrate);
		Collections.sort(locationValuesSubstrate);
		for (SecLoc key : secLocNodeHeuristicInfoArrayMap.keySet()) {
			Collections.sort(secLocNodeHeuristicInfoArrayMap.get(key));
		}
		return secLocNodeHeuristicInfoArrayMap;
	}

//	public static HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> calculateUtility(SubstrateNetworkHeu subNet, 
//			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate){
//
//		ArrayList<Double> nodeUtility = new ArrayList<Double>();
//		double lambda = 1;
//		double kn = 1;
//		double percentageCPUNodeAux;
//		double sumPercentageTotalEdgesAux;
//		int numberOfLinksAux;
//		double nodeUtilityAux = 0;
//		HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> secLocNodeHeuristicInfoArrayMap = new HashMap<SecLoc, ArrayList<NodeHeuristicInfo>>();
//
//		securityValuesSubstrate.clear();
//		locationValuesSubstrate.clear();
//
//		for (int i = 0; i < subNet.getNumOfNodes(); i++){
//			if(subNet.getAcceptNodeEmbedding(i)){
//				percentageCPUNodeAux = subNet.getNodeCPU(i)/subNet.getTotalNodeCPU(i);
//				numberOfLinksAux = 0;
//				sumPercentageTotalEdgesAux = 0;
//
//				if(securityValuesSubstrate != null){
//
//					if(!(securityValuesSubstrate.contains(subNet.getNodeSec(i)))){
//
//						securityValuesSubstrate.add(subNet.getNodeSec(i));
//					}
//				}
//				Collections.sort(securityValuesSubstrate);
//
//				if(locationValuesSubstrate != null){
//
//					if(!(locationValuesSubstrate.contains(subNet.getCloudSecSup().get(subNet.getNode(i))))){
//
//						locationValuesSubstrate.add(subNet.getCloudSecSup().get(subNet.getNode(i)));
//					}
//				}try{
//				Collections.sort(locationValuesSubstrate);
//				}catch(Exception e){
//					System.out.println(e.toString());
//				}
//				for(int j = 0; j < subNet.getNumOfEdges(); j++){
//
//					if ((subNet.getNode(i).equals(subNet.getEdge(j).getLeft()))||(subNet.getNode(i).equals(subNet.getEdge(j).getRight()))){
//						numberOfLinksAux ++;
//						sumPercentageTotalEdgesAux += subNet.getEdgeBw(j)/subNet.getTotalEdgeBw(j);
//					}
//				}
//
//				//			nodeUtilityAux = lambda*(subNet.getNodeCPU(i)*sumEdgesAux)*kn*(Math.log10(numberOfLinksAux) + 1)*
//				//					subNet.getNodeSec(i)*(1/(100.0*numberOfLinksAux+100.0));
//				//				nodeUtilityAux = lambda*(subNet.getNodeCPU(i)*sumEdgesAux)*kn*(Math.log10(numberOfLinksAux) + 1)*
//				//						(1/(subNet.getNodeSec(i)*subNet.getCloudSecSup().get(subNet.getNode(i))))*(1/(1000.0*numberOfLinksAux+100.0));
//				nodeUtilityAux = lambda*percentageCPUNodeAux*sumPercentageTotalEdgesAux*kn*(Math.log10(numberOfLinksAux) + 1)*
//						(1/(subNet.getNodeSec(i)*subNet.getCloudSecSup().get(subNet.getNode(i))));
//
//				nodeUtility.add(nodeUtilityAux);
//
//				SecLoc secLocAux = new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)));
//				int cloud = -1;
//				if(secLocNodeHeuristicInfoArrayMap.get(secLocAux) == null){
//
//					ArrayList<NodeHeuristicInfo> aux = new ArrayList<NodeHeuristicInfo>();
//
//					for (int k = 0; k < subNet.getNClouds(); k++){
//						if (subNet.getDoesItBelong(k, i) == 1){
//							cloud = k;
//						}
//					}
//
//					aux.add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
//					secLocNodeHeuristicInfoArrayMap.put(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i))), aux);
//				}else{
//
//					//				if(i == 25){
//					//					System.out.println("25");
//					//				}
//					for (int k = 0; k < subNet.getNClouds(); k++){
//						if (subNet.getDoesItBelong(k, i) == 1){
//							cloud = k;
//						}
//					}
//					if(i%100==0){
//
//						@SuppressWarnings("unused")
//						int o = 0;
//					}
//					secLocNodeHeuristicInfoArrayMap.get(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)))).add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
//				}
//			}
//		}
//		for (SecLoc key : secLocNodeHeuristicInfoArrayMap.keySet()) {
//
//			Collections.sort(secLocNodeHeuristicInfoArrayMap.get(key));
//		}
//		return secLocNodeHeuristicInfoArrayMap;
//	}
	
	public static HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> calculateCPUBW(SubstrateNetworkHeu subNet, 
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate){

		ArrayList<Double> nodeUtility = new ArrayList<Double>();
		double lambda = 1;
		double kn = 1;
		double percentageCPUNodeAux;
		double sumPercentageTotalEdgesAux;
		double nodeUtilityAux = 0;
		HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> secLocNodeHeuristicInfoArrayMap = new HashMap<SecLoc, ArrayList<NodeHeuristicInfo>>();

		securityValuesSubstrate.clear();
		locationValuesSubstrate.clear();

		for (int i = 0; i < subNet.getNumOfNodes(); i++){
			if(subNet.getAcceptNodeEmbedding(i)){
				percentageCPUNodeAux = subNet.getNodeCPU(i)/subNet.getTotalNodeCPU(i);
				sumPercentageTotalEdgesAux = 0;

				if(securityValuesSubstrate != null){

					if(!(securityValuesSubstrate.contains(subNet.getNodeSec(i)))){

						securityValuesSubstrate.add(subNet.getNodeSec(i));
					}
				}
				Collections.sort(securityValuesSubstrate);

				if(locationValuesSubstrate != null){

					if(!(locationValuesSubstrate.contains(subNet.getCloudSecSup().get(subNet.getNode(i))))){

						locationValuesSubstrate.add(subNet.getCloudSecSup().get(subNet.getNode(i)));
					}
				}
				Collections.sort(locationValuesSubstrate);

				for(int j = 0; j < subNet.getNumOfEdges(); j++){

					if ((subNet.getNode(i).equals(subNet.getEdge(j).getLeft()))||(subNet.getNode(i).equals(subNet.getEdge(j).getRight()))){
						sumPercentageTotalEdgesAux += subNet.getEdgeBw(j)/subNet.getTotalEdgeBw(j);
					}
				}
				nodeUtilityAux = lambda*percentageCPUNodeAux*sumPercentageTotalEdgesAux*kn;

				nodeUtility.add(nodeUtilityAux);

				SecLoc secLocAux = new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)));
				int cloud = -1;
				if(secLocNodeHeuristicInfoArrayMap.get(secLocAux) == null){

					ArrayList<NodeHeuristicInfo> aux = new ArrayList<NodeHeuristicInfo>();

					for (int k = 0; k < subNet.getNClouds(); k++){
						if (subNet.getDoesItBelong(k, i) == 1){
							cloud = k;
						}
					}

					aux.add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
					secLocNodeHeuristicInfoArrayMap.put(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i))), aux);
				}else{

					//				if(i == 25){
					//					System.out.println("25");
					//				}
					for (int k = 0; k < subNet.getNClouds(); k++){
						if (subNet.getDoesItBelong(k, i) == 1){
							cloud = k;
						}
					}
					if(i%100==0){

						@SuppressWarnings("unused")
						int o = 0;
					}
					secLocNodeHeuristicInfoArrayMap.get(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)))).add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
				}
			}
		}
		for (SecLoc key : secLocNodeHeuristicInfoArrayMap.keySet()) {

			Collections.sort(secLocNodeHeuristicInfoArrayMap.get(key));
		}
		return secLocNodeHeuristicInfoArrayMap;
	}
	
	public static HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> calculateCPUBW_SECS(SubstrateNetworkHeu subNet, 
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate){

		ArrayList<Double> nodeUtility = new ArrayList<Double>();
		double lambda = 1;
		double kn = 1;
		double percentageCPUNodeAux;
		double sumPercentageTotalEdgesAux;
		double nodeUtilityAux = 0;
		HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> secLocNodeHeuristicInfoArrayMap = new HashMap<SecLoc, ArrayList<NodeHeuristicInfo>>();

		securityValuesSubstrate.clear();
		locationValuesSubstrate.clear();

		for (int i = 0; i < subNet.getNumOfNodes(); i++){
			if(subNet.getAcceptNodeEmbedding(i)){
				percentageCPUNodeAux = subNet.getNodeCPU(i)/subNet.getTotalNodeCPU(i);
				sumPercentageTotalEdgesAux = 0;

				if(securityValuesSubstrate != null){

					if(!(securityValuesSubstrate.contains(subNet.getNodeSec(i)))){

						securityValuesSubstrate.add(subNet.getNodeSec(i));
					}
				}
				Collections.sort(securityValuesSubstrate);

				if(locationValuesSubstrate != null){

					if(!(locationValuesSubstrate.contains(subNet.getCloudSecSup().get(subNet.getNode(i))))){

						locationValuesSubstrate.add(subNet.getCloudSecSup().get(subNet.getNode(i)));
					}
				}
				Collections.sort(locationValuesSubstrate);

				for(int j = 0; j < subNet.getNumOfEdges(); j++){

					if ((subNet.getNode(i).equals(subNet.getEdge(j).getLeft()))||(subNet.getNode(i).equals(subNet.getEdge(j).getRight()))){
						sumPercentageTotalEdgesAux += subNet.getEdgeBw(j)/subNet.getTotalEdgeBw(j);
					}
				}

					nodeUtilityAux = lambda*percentageCPUNodeAux*sumPercentageTotalEdgesAux*kn*
						(1/(subNet.getNodeSec(i)*subNet.getCloudSecSup().get(subNet.getNode(i))));

				nodeUtility.add(nodeUtilityAux);

				SecLoc secLocAux = new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)));
				int cloud = -1;
				if(secLocNodeHeuristicInfoArrayMap.get(secLocAux) == null){

					ArrayList<NodeHeuristicInfo> aux = new ArrayList<NodeHeuristicInfo>();

					for (int k = 0; k < subNet.getNClouds(); k++){
						if (subNet.getDoesItBelong(k, i) == 1){
							cloud = k;
						}
					}

					aux.add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
					secLocNodeHeuristicInfoArrayMap.put(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i))), aux);
				}else{

					for (int k = 0; k < subNet.getNClouds(); k++){
						if (subNet.getDoesItBelong(k, i) == 1){
							cloud = k;
						}
					}
					if(i%100==0){

						@SuppressWarnings("unused")
						int o = 0;
					}
					secLocNodeHeuristicInfoArrayMap.get(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)))).add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
				}
			}
		}
		for (SecLoc key : secLocNodeHeuristicInfoArrayMap.keySet()) {

			Collections.sort(secLocNodeHeuristicInfoArrayMap.get(key));
		}
		return secLocNodeHeuristicInfoArrayMap;
	}
	
	public static HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> calculateCPUBW_LINKS(SubstrateNetworkHeu subNet, 
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate){

		ArrayList<Double> nodeUtility = new ArrayList<Double>();
		double lambda = 1;
		double kn = 1;
		double percentageCPUNodeAux;
		double sumPercentageTotalEdgesAux;
		int numberOfLinksAux;
		double nodeUtilityAux = 0;
		HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> secLocNodeHeuristicInfoArrayMap = new HashMap<SecLoc, ArrayList<NodeHeuristicInfo>>();

		securityValuesSubstrate.clear();
		locationValuesSubstrate.clear();

		for (int i = 0; i < subNet.getNumOfNodes(); i++){
			if(subNet.getAcceptNodeEmbedding(i)){
				percentageCPUNodeAux = subNet.getNodeCPU(i)/subNet.getTotalNodeCPU(i);
				numberOfLinksAux = 0;
				sumPercentageTotalEdgesAux = 0;

				if(securityValuesSubstrate != null){

					if(!(securityValuesSubstrate.contains(subNet.getNodeSec(i)))){

						securityValuesSubstrate.add(subNet.getNodeSec(i));
					}
				}
				Collections.sort(securityValuesSubstrate);

				if(locationValuesSubstrate != null){

					if(!(locationValuesSubstrate.contains(subNet.getCloudSecSup().get(subNet.getNode(i))))){

						locationValuesSubstrate.add(subNet.getCloudSecSup().get(subNet.getNode(i)));
					}
				}
				Collections.sort(locationValuesSubstrate);

				for(int j = 0; j < subNet.getNumOfEdges(); j++){

					if ((subNet.getNode(i).equals(subNet.getEdge(j).getLeft()))||(subNet.getNode(i).equals(subNet.getEdge(j).getRight()))){
						numberOfLinksAux ++;
						sumPercentageTotalEdgesAux += subNet.getEdgeBw(j)/subNet.getTotalEdgeBw(j);
					}
				}
				nodeUtilityAux = lambda*percentageCPUNodeAux*sumPercentageTotalEdgesAux*kn*(Math.log10(numberOfLinksAux) + 1);

				nodeUtility.add(nodeUtilityAux);

				SecLoc secLocAux = new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)));
				int cloud = -1;
				if(secLocNodeHeuristicInfoArrayMap.get(secLocAux) == null){

					ArrayList<NodeHeuristicInfo> aux = new ArrayList<NodeHeuristicInfo>();

					for (int k = 0; k < subNet.getNClouds(); k++){
						if (subNet.getDoesItBelong(k, i) == 1){
							cloud = k;
						}
					}

					aux.add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
					secLocNodeHeuristicInfoArrayMap.put(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i))), aux);
				}else{
					for (int k = 0; k < subNet.getNClouds(); k++){
						if (subNet.getDoesItBelong(k, i) == 1){
							cloud = k;
						}
					}
					if(i%100==0){

						@SuppressWarnings("unused")
						int o = 0;
					}
					secLocNodeHeuristicInfoArrayMap.get(new SecLoc(subNet.getNodeSec(i), subNet.getCloudSecSup().get(subNet.getNode(i)))).add(new NodeHeuristicInfo(i, nodeUtilityAux, -1, cloud));
				}
			}
		}
		for (SecLoc key : secLocNodeHeuristicInfoArrayMap.keySet()) {

			Collections.sort(secLocNodeHeuristicInfoArrayMap.get(key));
		}
		return secLocNodeHeuristicInfoArrayMap;
	}

	public static ArrayList<NodeHeuristicInfo> calculateGreedy(SubstrateNetworkHeu subNet){

		double sumEdgesAux;
		double nodeGreedyAux = 0;
		ArrayList<NodeHeuristicInfo> nodeGreedyHeuristicInfoArray = new ArrayList<NodeHeuristicInfo>();

		for (int i = 0; i < subNet.getNumOfNodes(); i++){
			sumEdgesAux = 0;
			for(int j = 0; j < subNet.getNumOfEdges(); j++){
				if ((subNet.getNode(i).equals(subNet.getEdge(j).getLeft()))||(subNet.getNode(i).equals(subNet.getEdge(j).getRight()))){;
				sumEdgesAux += subNet.getEdgeBw(j);
				}
			}
			nodeGreedyAux = (subNet.getNodeCPU(i)*sumEdgesAux);
//			nodeGreedyHeuristicInfoArray.add(new NodeHeuristicInfo(i, nodeGreedyAux, -1, -1));
			nodeGreedyHeuristicInfoArray.add(new NodeHeuristicInfo(i, nodeGreedyAux, -1, -1));
		}
		//TODO Must be verified.
		Collections.sort(nodeGreedyHeuristicInfoArray);
		return nodeGreedyHeuristicInfoArray;
	}

	public static ArrayList<NodeHeuristicInfo> calculateUtility(VirtualNetworkHeu virtualNetwork) {

		double lambda = 1;
		double kn = 1;
		double sumEdgesAux;
		double nodeUtilityAux = 0.0;
		ArrayList<NodeHeuristicInfo> secLocNodeHeuristicInfoArray = new ArrayList<NodeHeuristicInfo>();

		for (int i = 0; i < virtualNetwork.getNumOfNodes(); i++){
			sumEdgesAux = 0;

			for(int j = 0; j < virtualNetwork.getNumOfEdges(); j++)
				if ((virtualNetwork.getNode(i).equals(virtualNetwork.getEdge(j).getLeft()))||
						(virtualNetwork.getNode(i).equals(virtualNetwork.getEdge(j).getRight())))
					sumEdgesAux += virtualNetwork.getEdgeBw(j);

			nodeUtilityAux = lambda*(virtualNetwork.getNodeCPU(i)*sumEdgesAux)*kn*
					(1/(virtualNetwork.getNodeSec(i)*virtualNetwork.getCloudsSecurity().get(i)));

			secLocNodeHeuristicInfoArray.add(new NodeHeuristicInfo(i, nodeUtilityAux, virtualNetwork.getBackupLocalization(i)));
		}
		Collections.sort(secLocNodeHeuristicInfoArray);

		return secLocNodeHeuristicInfoArray;
	}

//	public static ArrayList<NodeHeuristicInfo> calculateUtility(VirtualNetworkHeu virtualNetwork, ArrayList<Double> securityValuesVirtual, ArrayList<Double> locationValuesVirtual) {
//
//		ArrayList<Double> nodeUtility = new ArrayList<Double>();
//		double lambda = 1;
//		double kn = 1;
//		double sumEdgesAux;
//		int numberOfLinksAux;
//		double nodeUtilityAux = 0.0;
//		HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> secLocNodeHeuristicInfoArrayMap = new HashMap<SecLoc, ArrayList<NodeHeuristicInfo>>();
//		ArrayList<NodeHeuristicInfo> secLocNodeHeuristicInfoArray = new ArrayList<NodeHeuristicInfo>();
//
//		securityValuesVirtual.clear();
//		locationValuesVirtual.clear();
//
//		for (int i = 0; i < virtualNetwork.getNumOfNodes(); i++){
//
//			sumEdgesAux = 0;
//			numberOfLinksAux = 0;
//
//			if(securityValuesVirtual != null){
//
//				if(!(securityValuesVirtual.contains(virtualNetwork.getNodeSec(i)))){
//
//					securityValuesVirtual.add(virtualNetwork.getNodeSec(i));
//				}
//			}
//			Collections.sort(securityValuesVirtual, Collections.reverseOrder());
//
//			if(locationValuesVirtual != null){
//
//				if(!(locationValuesVirtual.contains(virtualNetwork.getCloudsSecurity().get(i)))){
//
//					locationValuesVirtual.add(virtualNetwork.getCloudsSecurity().get(i));
//				}
//			}
//			Collections.sort(locationValuesVirtual, Collections.reverseOrder());
//
//			for(int j = 0; j < virtualNetwork.getNumOfEdges(); j++){ 
//
//				String l = virtualNetwork.getEdge(j).getLeft();
//				String r = virtualNetwork.getEdge(j).getRight();
//				String n = virtualNetwork.getNode(i);
//				boolean b = virtualNetwork.getNode(i).equals(virtualNetwork.getEdge(j).getLeft());
//				if ((virtualNetwork.getNode(i).equals(virtualNetwork.getEdge(j).getLeft()))||(virtualNetwork.getNode(i).equals(virtualNetwork.getEdge(j).getRight()))){
//
//					sumEdgesAux += virtualNetwork.getEdgeBw(j);
//					numberOfLinksAux ++;
//				}
//			}
//
//			nodeUtilityAux = lambda*(virtualNetwork.getNodeCPU(i)*sumEdgesAux)*kn*(Math.log10(numberOfLinksAux) + 1)*
//					(1/(virtualNetwork.getNodeSec(i)*virtualNetwork.getCloudsSecurity().get(i)))*(1/(1000.0*numberOfLinksAux))*(1/100.0);
//
//			nodeUtility.add(nodeUtilityAux);
//
//			SecLoc secLocAux = new SecLoc(virtualNetwork.getNodeSec(i), virtualNetwork.getCloudsSecurity().get(i));
//
//			if(secLocNodeHeuristicInfoArrayMap.get(secLocAux) == null){
//
//				ArrayList<NodeHeuristicInfo> aux = new ArrayList<NodeHeuristicInfo>();
//				aux.add(new NodeHeuristicInfo(i, nodeUtilityAux, virtualNetwork.getBackupLocalization(i)));
//				secLocNodeHeuristicInfoArrayMap.put(new SecLoc(virtualNetwork.getNodeSec(i), virtualNetwork.getCloudsSecurity().get(i)), aux);
//			}else{
//
//				secLocNodeHeuristicInfoArrayMap.get(new SecLoc(virtualNetwork.getNodeSec(i), virtualNetwork.getCloudsSecurity().get(i))).add(new NodeHeuristicInfo(i, nodeUtilityAux, virtualNetwork.getBackupLocalization(i)));
//			}
//
//			secLocNodeHeuristicInfoArray.add(new NodeHeuristicInfo(i, nodeUtilityAux, virtualNetwork.getBackupLocalization(i)));
//		}
//
//		Collections.sort(secLocNodeHeuristicInfoArray, Collections.reverseOrder());
//
//		for (SecLoc key : secLocNodeHeuristicInfoArrayMap.keySet()) {
//
//			Collections.sort(secLocNodeHeuristicInfoArrayMap.get(key), Collections.reverseOrder());
//		}
//
//		return secLocNodeHeuristicInfoArray;
//	}

	public static ArrayList<NodeHeuristicInfo> calculateGreedyDesc(VirtualNetworkHeu virtualNetwork) {

		double nodeGreedyAsc = 0.0;
		ArrayList<NodeHeuristicInfo> nodeGreedyHeuristicInfoArray = new ArrayList<NodeHeuristicInfo>();

		for (int i = 0; i < virtualNetwork.getNumOfNodes(); i++){

			nodeGreedyAsc = (virtualNetwork.getNodeCPU(i));

			nodeGreedyHeuristicInfoArray.add(new NodeHeuristicInfo(i, nodeGreedyAsc, virtualNetwork.getBackupLocalization(i)));
		}

		Collections.sort(nodeGreedyHeuristicInfoArray);
		//Collections.reverse(nodeGreedyHeuristicInfoArray);

		return nodeGreedyHeuristicInfoArray;
	}

	
	public static ArrayList<Pair<String>> mappingVirtualWorkingNodesMinimazePaths(
			HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> substrateSecLocNodeHeuristicInfoArrayMap,
			ArrayList<NodeHeuristicInfo> virtualSecLocNodeHeuristicInfoArray, SubstrateNetworkHeu subNet, VirtualNetworkHeu virtualNetwork,
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate, int percentagemOfNeighborsToSearch) {

		int indexSecVirtualNodeAux, indexLocationVirtualNodeAux;
		int virtualIndexAux, substrateIndexAux = -1;
		ArrayList<NodeHeuristicInfo> substrateSecLocNodeHeuristicInfoArrayAux;
		ArrayList<Pair<String>> mappingVirtualNodes = new ArrayList<Pair<String>>();
		ArrayList<NodeHeuristicInfo> utilityDividedByDistances = new ArrayList<NodeHeuristicInfo>();
		ArrayList<String> mappedSubstrateNode = new ArrayList<>();
		int numberOfNeighborsToSearch = percentagemOfNeighborsToSearch * subNet.getNumOfNodes()/100;

		found:
			for (NodeHeuristicInfo virtualNodeHeuristicInfo : virtualSecLocNodeHeuristicInfoArray){
				boolean found = false;
				virtualIndexAux = virtualNodeHeuristicInfo.getIndexNode();
				indexSecVirtualNodeAux = minimumIndexSecurityValuesSubstrate(virtualNetwork.getNodeSec(virtualIndexAux), securityValuesSubstrate);
				indexLocationVirtualNodeAux = minimumIndexLocationValuesSubstrate(virtualNetwork.getCloudSecurity(virtualIndexAux), locationValuesSubstrate);
				if((indexSecVirtualNodeAux == -1) || (indexLocationVirtualNodeAux == -1))
					return null;
				utilityDividedByDistances.clear();
				int counterNumberOfNeighborsToSearch = 0;

				//Populate the array with utility divided by the neighbors of the virtual node to be currently mapped
				ArrayList<String> substrateNeighborsNodes = Utils.findSubstrateNeighborsNodes(virtualNetwork, mappingVirtualNodes, virtualNodeHeuristicInfo);
				double averageDistance;
				NodeHeuristicInfo auxUtilityDividedByDistances;
				mainLoop:
					for (int i = indexSecVirtualNodeAux; i < securityValuesSubstrate.size(); i++){
						for (int j = indexLocationVirtualNodeAux; j < locationValuesSubstrate.size(); j++){
							substrateSecLocNodeHeuristicInfoArrayAux = substrateSecLocNodeHeuristicInfoArrayMap.get( new SecLoc(securityValuesSubstrate.get(i), locationValuesSubstrate.get(j)));
							if(substrateSecLocNodeHeuristicInfoArrayAux != null){
								for (NodeHeuristicInfo key : substrateSecLocNodeHeuristicInfoArrayAux){
									substrateIndexAux = key.getIndexNode();
									if(!mappedSubstrateNode.contains(subNet.getNode(substrateIndexAux))){
										averageDistance = Utils.calculateAverageDistance(substrateNeighborsNodes, subNet, substrateIndexAux);
										auxUtilityDividedByDistances = Utils.calculateUtilityDividedByDistances(key, averageDistance);
										utilityDividedByDistances.add(auxUtilityDividedByDistances);
										if(counterNumberOfNeighborsToSearch == numberOfNeighborsToSearch - 1)
											break mainLoop;
										counterNumberOfNeighborsToSearch++;
									}
								}
							}
						}
					}
				Collections.sort(utilityDividedByDistances);
				for (int i = 0; i < utilityDividedByDistances.size(); i++){
					if((subNet.getNodeCPU(utilityDividedByDistances.get(i).getIndexNode()) >= virtualNetwork.getNodeCPU(virtualIndexAux))){
						substrateIndexAux = utilityDividedByDistances.get(i).getIndexNode();
						if(subNet.getAcceptNodeEmbedding(substrateIndexAux)){
							mappingVirtualNodes.add(new Pair<String>(subNet.getNode(substrateIndexAux),virtualNetwork.getNode(virtualIndexAux)));
							mappedSubstrateNode.add(subNet.getNode(substrateIndexAux));
							found = true;
							continue found;
						}else{
							if(!virtualNetwork.getDemandsVirtualHosts().get(virtualIndexAux)){
								mappingVirtualNodes.add(new Pair<String>(subNet.getNode(substrateIndexAux),virtualNetwork.getNode(virtualIndexAux)));
								mappedSubstrateNode.add(subNet.getNode(substrateIndexAux));
								found = true;
								continue found;
							}
						}
					}
				}

				for (int i = indexSecVirtualNodeAux; i < securityValuesSubstrate.size(); i++){
					for (int j = indexLocationVirtualNodeAux; j < locationValuesSubstrate.size(); j++){
						substrateSecLocNodeHeuristicInfoArrayAux = substrateSecLocNodeHeuristicInfoArrayMap.get( new SecLoc(securityValuesSubstrate.get(i), locationValuesSubstrate.get(j)));
						if(substrateSecLocNodeHeuristicInfoArrayAux != null){
							for (NodeHeuristicInfo key : substrateSecLocNodeHeuristicInfoArrayAux){
								substrateIndexAux = key.getIndexNode();
								if(subNet.getNodeCPU(substrateIndexAux) >= virtualNetwork.getNodeCPU(virtualIndexAux)){
									if(subNet.getAcceptNodeEmbedding(substrateIndexAux) && !mappedSubstrateNode.contains(subNet.getNode(substrateIndexAux))){
										mappingVirtualNodes.add(new Pair<String>(subNet.getNode(substrateIndexAux),virtualNetwork.getNode(virtualIndexAux)));
										substrateSecLocNodeHeuristicInfoArrayAux.remove(key);
										found = true;
										continue found;
									}else{
										if(!virtualNetwork.getDemandsVirtualHosts().get(virtualIndexAux) && !mappedSubstrateNode.contains(subNet.getNode(substrateIndexAux))){
											mappingVirtualNodes.add(new Pair<String>(subNet.getNode(substrateIndexAux),virtualNetwork.getNode(virtualIndexAux)));
											substrateSecLocNodeHeuristicInfoArrayAux.remove(key);
											found = true;
											continue found;
										}
									}
								}
							}
						}
					}
					if((i == securityValuesSubstrate.size() - 1)&&(!found)){
						return null;
					}
				}
			}
		return mappingVirtualNodes;
	}
	
	public static ArrayList<Pair<String>> mappingVirtualWorkingNodes(
			HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> substrateSecLocNodeHeuristicInfoArrayMap,
			ArrayList<NodeHeuristicInfo> virtualSecLocNodeHeuristicInfoArray, SubstrateNetworkHeu subNet, VirtualNetworkHeu virtualNetwork,
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate) {

		int indexSecVirtualNodeAux, indexLocationVirtualNodeAux;
		int virtualIndexAux, substrateIndexAux = -1;
		ArrayList<NodeHeuristicInfo> substrateSecLocNodeHeuristicInfoArrayAux;

		ArrayList<Pair<String>> mappingVirtualNodes = new ArrayList<Pair<String>>();

		found:
			for (NodeHeuristicInfo virtualNodeHeuristicInfo : virtualSecLocNodeHeuristicInfoArray){

				boolean found = false;
				virtualIndexAux = virtualNodeHeuristicInfo.getIndexNode();
				indexSecVirtualNodeAux = minimumIndexSecurityValuesSubstrate(virtualNetwork.getNodeSec(virtualIndexAux), securityValuesSubstrate);
				indexLocationVirtualNodeAux = minimumIndexLocationValuesSubstrate(virtualNetwork.getCloudSecurity(virtualIndexAux), locationValuesSubstrate);
				if((indexSecVirtualNodeAux == -1) || (indexLocationVirtualNodeAux == -1))
					return null;

				for (int i = indexSecVirtualNodeAux; i < securityValuesSubstrate.size(); i++){

					for (int j = indexLocationVirtualNodeAux; j < locationValuesSubstrate.size(); j++){

						substrateSecLocNodeHeuristicInfoArrayAux = substrateSecLocNodeHeuristicInfoArrayMap.get( new SecLoc(securityValuesSubstrate.get(i), locationValuesSubstrate.get(j)));

						if(substrateSecLocNodeHeuristicInfoArrayAux != null){

							for (NodeHeuristicInfo key : substrateSecLocNodeHeuristicInfoArrayAux){

								substrateIndexAux = key.getIndexNode();

								if((subNet.getNodeCPU(substrateIndexAux) >= virtualNetwork.getNodeCPU(virtualIndexAux)) &&
										(subNet.getNodeSec(substrateIndexAux) >= virtualNetwork.getNodeSec(virtualIndexAux)) &&
										(subNet.getCloudSecSup().get(subNet.getNode(substrateIndexAux)) >= virtualNetwork.getCloudSecurity(virtualIndexAux))){

									mappingVirtualNodes.add(new Pair<String>(subNet.getNode(substrateIndexAux),virtualNetwork.getNode(virtualIndexAux)));

									substrateSecLocNodeHeuristicInfoArrayAux.remove(key);
									found = true;

									continue found;
								}
							}
						}
					}
					if((i == securityValuesSubstrate.size() - 1)&&(!found)){
						return null;
					}
				}
			}
		return mappingVirtualNodes;
	}

	private static int minimumIndexSecurityValuesSubstrate(double nodeSec, ArrayList<Double> securityValuesSubstrate) {

		for(int i = 0; i < securityValuesSubstrate.size(); i++){
			if(nodeSec <= securityValuesSubstrate.get(i))
				return i;
		}
		return -1;
	}
	
	private static int minimumIndexLocationValuesSubstrate(double nodeLoc, ArrayList<Double> locationValuesSubstrate) {

		for(int i = 0; i < locationValuesSubstrate.size(); i++){
			if(nodeLoc <= locationValuesSubstrate.get(i))
				return i;
		}
		return -1;
	}

	public static ArrayList<Pair<String>> mappingVirtualBackupNodes(SubstrateNetworkHeu subNetAux, 
			VirtualNetworkHeu virtualNetwork, ArrayList<Pair<String>> mappedWorkingNodes2,
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate,
			ArrayList<NodeHeuristicInfo> virtualSecLocNodeHeuristicInfoArray,
			HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> substrateSecLocNodeHeuristicInfoArrayMap){

		double secVirtualNodeAux, locationVirtualNodeAux;
		int virtualIndexAux, substrateIndexAux, backupNodeLocalization = -1;
		ArrayList<NodeHeuristicInfo> substrateSecLocNodeHeuristicInfoArrayAux;//

		ArrayList<Pair<String>> mappingVirtualNodes = new ArrayList<Pair<String>>();

		found:
			for (NodeHeuristicInfo virtualNodeHeuristicInfo : virtualSecLocNodeHeuristicInfoArray){

				boolean found = false;
				virtualIndexAux = virtualNodeHeuristicInfo.getIndexNode();
				secVirtualNodeAux = virtualNetwork.getNodeSec(virtualIndexAux);
				locationVirtualNodeAux = virtualNetwork.getCloudSecurity(virtualIndexAux);
				backupNodeLocalization = virtualNetwork.getBackupsLocalization().get(virtualIndexAux);

				if(backupNodeLocalization == 1){
					@SuppressWarnings("unused")
					int a = 0;
				}

				for (int i = securityValuesSubstrate.indexOf(secVirtualNodeAux); i < securityValuesSubstrate.size(); i++){

					for (int j = locationValuesSubstrate.indexOf(locationVirtualNodeAux); j < locationValuesSubstrate.size(); j++){

						substrateSecLocNodeHeuristicInfoArrayAux = substrateSecLocNodeHeuristicInfoArrayMap.get( new SecLoc(securityValuesSubstrate.get(i), locationValuesSubstrate.get(j)));

						if(substrateSecLocNodeHeuristicInfoArrayAux != null){

							for (NodeHeuristicInfo key : substrateSecLocNodeHeuristicInfoArrayAux){

								substrateIndexAux = key.getIndexNode();

								if((subNetAux.getNodeCPU(substrateIndexAux) >= virtualNetwork.getNodeCPU(virtualIndexAux)) &&
										(subNetAux.getNodeSec(substrateIndexAux) >= virtualNetwork.getNodeSec(virtualIndexAux)) &&
										(subNetAux.getCloudSecSup().get(subNetAux.getNode(substrateIndexAux)) >= virtualNetwork.getCloudSecurity(virtualIndexAux))){

									if((backupNodeLocalization == 1) && 
											(subNetAux.getDoesItBelong(getCloudFromVirtualNode(subNetAux, virtualNetwork, virtualIndexAux, mappedWorkingNodes2), 
													substrateIndexAux) == 1)){
										//(subNet.getDoesItBelong(virtualNodeHeuristicInfo.getBelongsToCloud(), substrateIndexAux)==1)
										mappingVirtualNodes.add(new Pair<String>(subNetAux.getNode(substrateIndexAux),virtualNetwork.getNode(virtualIndexAux)));

										substrateSecLocNodeHeuristicInfoArrayAux.remove(key);
										found = true;

										continue found;
									}else{

										if((backupNodeLocalization == 2) && 
												(subNetAux.getDoesItBelong(getCloudFromVirtualNode(subNetAux, virtualNetwork, virtualIndexAux, mappedWorkingNodes2), 
														substrateIndexAux) == 0)){

											mappingVirtualNodes.add(new Pair<String>(subNetAux.getNode(substrateIndexAux),virtualNetwork.getNode(virtualIndexAux)));

											substrateSecLocNodeHeuristicInfoArrayAux.remove(key);
											found = true;

											continue found;
										}
									}
								}
							}
						}
					}
					if((i == securityValuesSubstrate.size()-1)&&(!found)){
						return null;
					}
				}
			}
		return mappingVirtualNodes;
	}

	public static ArrayList<Pair<String>> mappingVirtualBackupNodesPartial(SubstrateNetworkHeu subNetAux, 
			VirtualNetworkHeu virtualNetwork, ArrayList<Pair<String>> mappedWorkingNodes2,
			ArrayList<Double> securityValuesSubstrate,	ArrayList<Double> locationValuesSubstrate,
			ArrayList<NodeHeuristicInfo> virtualSecLocNodeHeuristicInfoArray,
			HashMap<SecLoc, ArrayList<NodeHeuristicInfo>> substrateSecLocNodeHeuristicInfoArrayMap){

		double secVirtualNodeAux, locationVirtualNodeAux;
		int virtualIndexAux, substrateIndexAux, backupNodeLocalization = -1;
		ArrayList<NodeHeuristicInfo> substrateSecLocNodeHeuristicInfoArrayAux;//

		ArrayList<Pair<String>> mappingVirtualNodes = new ArrayList<Pair<String>>();

		Utils.removeNodesNotRequestBackup(virtualSecLocNodeHeuristicInfoArray, virtualNetwork);

		found:
			for (NodeHeuristicInfo virtualNodeHeuristicInfo : virtualSecLocNodeHeuristicInfoArray){

				boolean found = false;
				virtualIndexAux = virtualNodeHeuristicInfo.getIndexNode();
				secVirtualNodeAux = virtualNetwork.getNodeSec(virtualIndexAux);
				locationVirtualNodeAux = virtualNetwork.getCloudSecurity(virtualIndexAux);
				backupNodeLocalization = virtualNetwork.getBackupsLocalization().get(virtualIndexAux);

				if(backupNodeLocalization == 1){
					@SuppressWarnings("unused")
					int a = 0;
				}

				for (int i = securityValuesSubstrate.indexOf(secVirtualNodeAux); i < securityValuesSubstrate.size(); i++){

					for (int j = locationValuesSubstrate.indexOf(locationVirtualNodeAux); j < locationValuesSubstrate.size(); j++){

						substrateSecLocNodeHeuristicInfoArrayAux = substrateSecLocNodeHeuristicInfoArrayMap.get( new SecLoc(securityValuesSubstrate.get(i), locationValuesSubstrate.get(j)));

						if(substrateSecLocNodeHeuristicInfoArrayAux != null){

							for (NodeHeuristicInfo key : substrateSecLocNodeHeuristicInfoArrayAux){

								substrateIndexAux = key.getIndexNode();

								if((subNetAux.getNodeCPU(substrateIndexAux) >= virtualNetwork.getNodeCPU(virtualIndexAux)) &&
										(subNetAux.getNodeSec(substrateIndexAux) >= virtualNetwork.getNodeSec(virtualIndexAux)) &&
										(subNetAux.getCloudSecSup().get(subNetAux.getNode(substrateIndexAux)) >= virtualNetwork.getCloudSecurity(virtualIndexAux))){

									if((backupNodeLocalization == 1) && 
											(subNetAux.getDoesItBelong(getCloudFromVirtualNode(subNetAux, virtualNetwork, virtualIndexAux, mappedWorkingNodes2), 
													substrateIndexAux) == 1)){
										//(subNet.getDoesItBelong(virtualNodeHeuristicInfo.getBelongsToCloud(), substrateIndexAux)==1)
										mappingVirtualNodes.add(new Pair<String>(subNetAux.getNode(substrateIndexAux),virtualNetwork.getNode(virtualIndexAux)));

										substrateSecLocNodeHeuristicInfoArrayAux.remove(key);
										found = true;

										continue found;
									}else{

										if((backupNodeLocalization == 2) && 
												(subNetAux.getDoesItBelong(getCloudFromVirtualNode(subNetAux, virtualNetwork, virtualIndexAux, mappedWorkingNodes2), 
														substrateIndexAux) == 0)){

											mappingVirtualNodes.add(new Pair<String>(subNetAux.getNode(substrateIndexAux),virtualNetwork.getNode(virtualIndexAux)));

											substrateSecLocNodeHeuristicInfoArrayAux.remove(key);
											found = true;

											continue found;
										}
									}
								}
							}
						}
					}
					if((i == securityValuesSubstrate.size()-1)&&(!found)){
						return null;
					}
				}
			}
		return mappingVirtualNodes;
	}

	private static void removeNodesNotRequestBackup(ArrayList<NodeHeuristicInfo> virtualSecLocNodeHeuristicInfoArray,
			VirtualNetworkHeu virtualNetwork) {

		ArrayList<NodeHeuristicInfo> nodesToRemove = new ArrayList<NodeHeuristicInfo>();

		for (NodeHeuristicInfo virtualNodeHeuristicInfo : virtualSecLocNodeHeuristicInfoArray){
			int indexNode = virtualNodeHeuristicInfo.getIndexNode();
			boolean wantBackupNode = virtualNetwork.getWantBackupNode(indexNode);
			if(!wantBackupNode){
				nodesToRemove.add(virtualNodeHeuristicInfo);

			}
		}
		virtualSecLocNodeHeuristicInfoArray.removeAll(nodesToRemove);
	}

	public static void removeLinksNotRequestBackup(ArrayList<LinkHeuristicInfo> virtualLinkHeuristicInfoArray,
			VirtualNetworkHeu virtualNetwork) {

		ArrayList<LinkHeuristicInfo> linksToRemove = new ArrayList<LinkHeuristicInfo>();

		for (LinkHeuristicInfo virtualLinkHeuristicInfo : virtualLinkHeuristicInfoArray){
			int indexLink = virtualLinkHeuristicInfo.getIndexLink();
			int indexNodeLeft = Integer.parseInt(virtualNetwork.getEdge(indexLink).getLeft());
			int indexNodeRight = Integer.parseInt(virtualNetwork.getEdge(indexLink).getRight());
			boolean noNeedbackupLink = !virtualNetwork.getWantBackupNode(indexNodeLeft) && !virtualNetwork.getWantBackupNode(indexNodeRight);
			if(noNeedbackupLink){
				linksToRemove.add(virtualLinkHeuristicInfo);
			}
		}
		virtualLinkHeuristicInfoArray.removeAll(linksToRemove);
	}

	public static ArrayList<Pair<String>> enrichMappedBackupNodes(ArrayList<Pair<String>> mappedWorkingNodes,
			ArrayList<Pair<String>> mappedBackupNodes, ArrayList<LinkHeuristicInfo> virtualLinkHeuristicInfoArray, VirtualNetworkHeu virtualNetwork) {

		ArrayList<Pair<String>> mappedBackupNodesEnriched = new ArrayList <Pair<String>>();
		mappedBackupNodesEnriched.addAll(mappedBackupNodes);
		ArrayList<String> mappedVirtualBackupNodes = new ArrayList<String>();
		for(int i = 0; i < mappedBackupNodes.size(); i++){
			mappedVirtualBackupNodes.add(mappedBackupNodes.get(i).getRight());
		}
		for(LinkHeuristicInfo virtualLinkHeuristicInfo : virtualLinkHeuristicInfoArray){
			int indexLink = virtualLinkHeuristicInfo.getIndexLink();
			String nodeLeft = virtualNetwork.getEdge(indexLink).getLeft();
			String nodeRight = virtualNetwork.getEdge(indexLink).getRight();
			if(!mappedVirtualBackupNodes.contains(nodeLeft)){
				for(int i = 0; i < mappedWorkingNodes.size(); i++){
					if(nodeLeft.equals(mappedWorkingNodes.get(i).getRight())){
						mappedBackupNodesEnriched.add(new Pair<String>(mappedWorkingNodes.get(i).getLeft(), nodeLeft));
						break;
					}
				}
			}
			if(!mappedVirtualBackupNodes.contains(nodeRight)){
				for(int i = 0; i < mappedWorkingNodes.size(); i++){
					if(nodeLeft.equals(mappedWorkingNodes.get(i).getRight())){
						mappedBackupNodesEnriched.add(new Pair<String>(mappedWorkingNodes.get(i).getLeft(), nodeRight));
						break;
					}
				}
			}
		}
		return mappedBackupNodesEnriched;
	}

	public static boolean verifyNodeRequirements(VirtualNetworkHeu virtualNetwork, int i,
			SubstrateNetworkHeu subNet, int nodeIndexAux) {

		if(nodeIndexAux != -1){
			if(subNet.getNode(nodeIndexAux) != null){
				if(subNet.getCloudSecSup().get(subNet.getNode(nodeIndexAux)) != null){
					if(subNet.getCloudSecSup().get(subNet.getNode(nodeIndexAux)) >= virtualNetwork.getCloudSecurity(i)){	
						if (subNet.getNodeCPU(nodeIndexAux) >= virtualNetwork.getNodeCPU(i)){
							if(subNet.getNodeSec(nodeIndexAux) >= virtualNetwork.getNodeSec(i)){

								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean verifyNodeRequirementsBackup(VirtualNetworkHeu virtualNetwork, int i,
			SubstrateNetworkHeu subNet, int nodeIndexAux, ArrayList<Pair<String>> mappedWorkingNodes) {

		if(nodeIndexAux != -1){
			if(subNet.getNode(nodeIndexAux) != null){
				if(subNet.getCloudSecSup().get(subNet.getNode(nodeIndexAux)) != null){
					if(subNet.getCloudSecSup().get(subNet.getNode(nodeIndexAux)) >= virtualNetwork.getCloudSecurity(i)){	
						if (subNet.getNodeCPU(nodeIndexAux) >= virtualNetwork.getNodeCPU(i)){
							if(subNet.getNodeSec(nodeIndexAux) >= virtualNetwork.getNodeSec(i)){

								int nodeCloud = getCloudFromVirtualNode(subNet, virtualNetwork, i, mappedWorkingNodes);

								if(nodeCloud != -1){
									if((virtualNetwork.getBackupsLocalization().get(i) == 2) && (subNet.getDoesItBelong(nodeCloud, nodeIndexAux) == 1)){

										return true;
									}
									if((virtualNetwork.getBackupsLocalization().get(i) == 1) && (subNet.getDoesItBelong(nodeCloud, nodeIndexAux) == 0)){

										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private static int getCloudFromVirtualNode(SubstrateNetworkHeu subNet, VirtualNetworkHeu virtualNetwork,
			int i, ArrayList<Pair<String>> mappedWorkingNodes){

		for(int j = 0; j < mappedWorkingNodes.size(); j++){

			if(mappedWorkingNodes.get(j).getRight().equals(virtualNetwork.getNode(i))){

				int indexNodeSubNet = subNet.getNodes().indexOf(mappedWorkingNodes.get(j).getLeft());

				for(int k = 0; k < subNet.getDoesItBelong().length; k++){

					if(subNet.getDoesItBelong()[k][indexNodeSubNet] == 1){

						return k;
					}
				}
			}
		}
		return -1;
	}

	public static ArrayList<Pair<String>> mappingFullRandomVirtualNodes(
			SubstrateNetworkHeu subNet, VirtualNetworkHeu virtualNetwork, ArrayList<Pair<String>> mappedWorkingNodes){

		int nodeIndexAux = -1;
		int count = 0;
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		ArrayList<Pair<String>> mappingRandomVirtualNodes = new ArrayList<Pair<String>>();

		if(virtualNetwork.getNumOfNodes() > subNet.getNumOfNodes()){
			return null;
		}

		if(mappedWorkingNodes != null){

			for(int i = 0; i < mappedWorkingNodes.size(); i++){

				mappedNodes.add(subNet.getNodes().indexOf(mappedWorkingNodes.get(i).getLeft()));
			}
		}

		for (int i = 0; i < virtualNetwork.getNumOfNodes(); i++){

			while (nodeIndexAux == -1 && count < 1000) {

				nodeIndexAux = random.nextInt(subNet.getNumOfNodes());

				if (mappedNodes.contains(nodeIndexAux)){
					nodeIndexAux  = -1;
				}
				count++;
			}

			if(mappedWorkingNodes == null){

				if(!verifyNodeRequirements(virtualNetwork, i, subNet, nodeIndexAux)){

					return null;
				}
			}else{
				if(!verifyNodeRequirementsBackup(virtualNetwork, i, subNet, nodeIndexAux, mappedWorkingNodes)){

					return null;
				}
			}
			mappedNodes.add(nodeIndexAux);
			mappingRandomVirtualNodes.add(new Pair<String>(subNet.getNode(nodeIndexAux),virtualNetwork.getNode(i)));

			count = 0;
			nodeIndexAux = -1;
		}		
		return mappingRandomVirtualNodes;
	}

	public static ArrayList<Pair<String>> mappingPartialRandomVirtualNodes(
			SubstrateNetworkHeu subNet, VirtualNetworkHeu virtualNetwork, ArrayList<Pair<String>> mappedWorkingNodes){

		int nodeIndexAux = -1;
		int count = 0;
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		ArrayList<Pair<String>> mappingPartialRandomVirtualNodes = new ArrayList<Pair<String>>();
		HashMap<Integer,Integer> indexIndexNodesMeetRequirements = new HashMap<Integer,Integer>();

		if(virtualNetwork.getNumOfNodes() > subNet.getNumOfNodes()){
			return null;
		}

		if(mappedWorkingNodes != null){

			for(int i = 0; i < mappedWorkingNodes.size(); i++){

				mappedNodes.add(subNet.getNodes().indexOf(mappedWorkingNodes.get(i).getLeft()));
			}
		}

		for (int i = 0; i < virtualNetwork.getNumOfNodes(); i++){

			if(mappedWorkingNodes == null){

				indexIndexNodesMeetRequirements = Utils.nodesMeetRequirements(subNet, virtualNetwork, i, null);
			}else{
				indexIndexNodesMeetRequirements = Utils.nodesMeetRequirements(subNet, virtualNetwork, i, mappedWorkingNodes);
			}

			if(indexIndexNodesMeetRequirements.size() <= 0){

				return null;
			}

			while (nodeIndexAux == -1 && count < 1000) {

				nodeIndexAux = indexIndexNodesMeetRequirements.get(random.nextInt(indexIndexNodesMeetRequirements.size()));

				if (mappedNodes.contains(nodeIndexAux)){
					nodeIndexAux  = -1;
				}
				count++;
			}

			if(nodeIndexAux == -1){

				return null;

			}else{
				if(mappedWorkingNodes == null){

					if(!verifyNodeRequirements(virtualNetwork, i, subNet, nodeIndexAux)){

						return null;
					}
				}else{
					if(!verifyNodeRequirementsBackup(virtualNetwork, i, subNet, nodeIndexAux, mappedWorkingNodes)){

						return null;
					}
				}
			}
			mappedNodes.add(nodeIndexAux);
			mappingPartialRandomVirtualNodes.add(new Pair<String>(subNet.getNode(nodeIndexAux), virtualNetwork.getNode(i)));

			count = 0;
			nodeIndexAux = -1;
		}		
		return mappingPartialRandomVirtualNodes;
	}

	public static ArrayList<Pair<String>> mappingFullGreedyVirtualNodes(
			SubstrateNetworkHeu subNet, VirtualNetworkHeu virtualNetwork, 
			ArrayList<NodeHeuristicInfo> substrateNodeGreedyHeuristic, 
			ArrayList<NodeHeuristicInfo> virtualNodeGreedyHeuristic){

		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		ArrayList<Pair<String>> mappingFullGreedyVirtualNodes = new ArrayList<Pair<String>>();
		HashMap<Integer,Integer> indexIndexNodesMeetRequirements = new HashMap<Integer,Integer>();

		if(virtualNetwork.getNumOfNodes() > subNet.getNumOfNodes()){
			return null;
		}
		for(int i = 0; i < virtualNodeGreedyHeuristic.size(); i++){
			indexIndexNodesMeetRequirements = Utils.nodesMeetCPURequirements(subNet, virtualNetwork, 
					virtualNodeGreedyHeuristic.get(i).getIndexNode());
			if(indexIndexNodesMeetRequirements.size() <= 0){
				return null;
			}
			for(int j = 0; j < substrateNodeGreedyHeuristic.size(); j++){
				if(indexIndexNodesMeetRequirements.containsValue(substrateNodeGreedyHeuristic.get(j).getIndexNode()) &&
						!mappedNodes.contains(substrateNodeGreedyHeuristic.get(j).getIndexNode())){
					mappingFullGreedyVirtualNodes.add(new Pair<String>(
							""+subNet.getNode(substrateNodeGreedyHeuristic.get(j).getIndexNode()),
							""+virtualNetwork.getNode(virtualNodeGreedyHeuristic.get(i).getIndexNode())));
					mappedNodes.add(substrateNodeGreedyHeuristic.get(j).getIndexNode());
					break;
				}
				if(j == substrateNodeGreedyHeuristic.size()-1){
					return null;
				}
			}
		}
		//TODO Must be verified.
		return mappingFullGreedyVirtualNodes;
	}

	public static HashMap<Integer,Integer> nodesMeetRequirements( SubstrateNetworkHeu subNet, VirtualNetworkHeu virtualNetwork, int indexVirtualNetwork, ArrayList<Pair<String>> mappedWorkingNodes){

		int indexMap = 0;
		HashMap<Integer,Integer> indexIndexNodesMeetRequirements = new HashMap<Integer,Integer>();

		for (int i = 0; i < subNet.getNodes().size(); i++){

			if((subNet.getNodeSec(i) >= virtualNetwork.getNodeSec(indexVirtualNetwork)) && 
					(subNet.getCloudSecSup().get(subNet.getNode(i)) >= virtualNetwork.getCloudSecurity(indexVirtualNetwork)) &&
					(subNet.getNodeCPU(i) >= virtualNetwork.getNodeCPU(indexVirtualNetwork))){

				if(mappedWorkingNodes != null){

					int nodeCloud = getCloudFromVirtualNode(subNet, virtualNetwork, indexVirtualNetwork, mappedWorkingNodes);

					if(nodeCloud != -1){
						if((virtualNetwork.getBackupsLocalization().get(indexVirtualNetwork) == 2) && (subNet.getDoesItBelong(nodeCloud, i) == 1)){

							indexIndexNodesMeetRequirements.put(indexMap, i);
							indexMap++;
						}
						if((virtualNetwork.getBackupsLocalization().get(indexVirtualNetwork) == 1) && (subNet.getDoesItBelong(nodeCloud, i) == 0)){

							indexIndexNodesMeetRequirements.put(indexMap, i);
							indexMap++;
						}
					}
				}else{
					indexIndexNodesMeetRequirements.put(indexMap, i);
					indexMap++;
				}
			}
		}
		return indexIndexNodesMeetRequirements;
	}

	public static HashMap<Integer,Integer> nodesMeetCPURequirements( SubstrateNetworkHeu subNet, VirtualNetworkHeu virtualNetwork, int indexVirtualNetwork){

		int indexMap = 0;
		HashMap<Integer,Integer> indexIndexNodesMeetRequirements = new HashMap<Integer,Integer>();

		for (int i = 0; i < subNet.getNodes().size(); i++){
			if((subNet.getNodeCPU(i) >= virtualNetwork.getNodeCPU(indexVirtualNetwork))){
				indexIndexNodesMeetRequirements.put(indexMap, i);
				indexMap++;
			}
		}
		return indexIndexNodesMeetRequirements;
	}

	public static ArrayList<Pair<String>> mappingVirtualWorkingNodes(
			HashMap<Integer, ArrayList<NodeHeuristicInfo>> virtualNodeIndexArraySubstrateNodeInfoMap, SubstrateNetworkHeu subNet){

		ArrayList<Pair<String>> mappingVirtualNodes = new ArrayList<Pair<String>>();
		ArrayList<Integer> mappedNodes = new ArrayList<Integer>();
		int virtualNodeIndex, substrateNodeIndex = -1;
		double pz = -1;
		ArrayList<NodeHeuristicInfo> aux;
		NodeHeuristicInfo nodeHeuristicInfoAux;
		HashMap<Integer, Double> auxPZ = new HashMap<Integer, Double>();

		if(virtualNodeIndexArraySubstrateNodeInfoMap == null || virtualNodeIndexArraySubstrateNodeInfoMap.size() <=0){
			return null;
		}

		for (Integer key : virtualNodeIndexArraySubstrateNodeInfoMap.keySet()){
			virtualNodeIndex = key - subNet.getNumOfNodes();
			aux = virtualNodeIndexArraySubstrateNodeInfoMap.get(key);
			for (int i = 0; i < aux.size(); i++){
				nodeHeuristicInfoAux = aux.get(i);
				if(auxPZ.get(nodeHeuristicInfoAux.getIndexNode())== null){
					auxPZ.put(nodeHeuristicInfoAux.getIndexNode(), nodeHeuristicInfoAux.getFlow()*nodeHeuristicInfoAux.getX());
				}else{
					double oldPZ = auxPZ.get(nodeHeuristicInfoAux.getIndexNode());
					auxPZ.put(nodeHeuristicInfoAux.getIndexNode(), oldPZ + nodeHeuristicInfoAux.getFlow()*nodeHeuristicInfoAux.getX());
				} 
			}
			for (Integer nodeNumber : auxPZ.keySet()){
				if((pz < (auxPZ.get(nodeNumber))) && !mappedNodes.contains(nodeNumber)){

					pz = auxPZ.get(nodeNumber);
					substrateNodeIndex = nodeNumber;
				}
			}
			if(substrateNodeIndex == -1){
				return null;
			}
			mappingVirtualNodes.add(new Pair<String>(""+substrateNodeIndex, ""+virtualNodeIndex));
			mappedNodes.add(substrateNodeIndex);
			substrateNodeIndex = -1;
			virtualNodeIndex = -1;
			pz=-1;
			auxPZ.clear();
		}
		return mappingVirtualNodes;
	}

	public static ArrayList<Pair<String>> mappingDVineVirtualWorkingNodes(
			SubstrateNetworkHeu subNet, VirtualNetworkHeu virtualNetwork, OutputFileReader fileReaders) {

		DVineDatFileCreator dVineDatCreator = new DVineDatFileCreator();
		HashMap<Integer, ArrayList<NodeHeuristicInfo>> virtualNodeIndexArraySubstrateNodeInfoMap = new HashMap<Integer, ArrayList<NodeHeuristicInfo>>();
		ArrayList <Pair<String>> mappedWorkingNodes = new ArrayList <Pair<String>>();

		dVineDatCreator.createDatFile("/home/secdep_18nov16_bkp1/testesHeuristicas/testeDVINE_HEU.dat", subNet, virtualNetwork);

		String partialResult = Utils.runGLPSOLHeuristic("/home/secdep_18nov16_bkp1/testesHeuristicas/testeDVINE_HEU.dat", 
				"/home/secdep_18nov16_bkp1/testesHeuristicas/DVINE_HEURISTIC2.mod");

		if(partialResult.contains("PROBLEM HAS NO PRIMAL FEASIBLE SOLUTION") || 
				partialResult.contains("PROBLEM HAS NO INTEGER FEASIBLE SOLUTION") || 
				partialResult.contains("LP HAS NO PRIMAL FEASIBLE SOLUTION")){
			fileReaders.setWasAccepted(false);
			return null;

		} else{
			virtualNodeIndexArraySubstrateNodeInfoMap = fileReaders.populateDVineHeuristicWorkingInfo(virtualNetwork, subNet.getNumOfNodes(), partialResult, subNet);
			mappedWorkingNodes = Utils.mappingVirtualWorkingNodes(virtualNodeIndexArraySubstrateNodeInfoMap, subNet);
		}
		return mappedWorkingNodes;
	}

	public static ArrayList<String> populateDVineFS(ArrayList<Pair<String>> mappedNodes,
			VirtualNetworkHeu virNet, SubstrateNetworkHeu subNet) {

		ArrayList<String> fs = new ArrayList<String>();
		for(int i = 0; i < virNet.getEdges().size(); i++){
			fs.add(Utils.getDVineSubstrateNodeIndex(mappedNodes, virNet.getEdges().get(i).getLeft(), subNet));
		}

		return fs;
	}

	public static ArrayList<String> populateDVineFE(ArrayList<Pair<String>> mappedNodes,
			VirtualNetworkHeu virNet, SubstrateNetworkHeu subNet) {

		ArrayList<String> fe = new ArrayList<String>();
		for(int i = 0; i < virNet.getEdges().size(); i++){
			fe.add(Utils.getDVineSubstrateNodeIndex(mappedNodes, virNet.getEdges().get(i).getRight(), subNet));
		}
		return fe;
	}

	private static String getDVineSubstrateNodeIndex(ArrayList<Pair<String>> mappedNodes,
			String virtualNode, SubstrateNetworkHeu subNet) {

		for(int i = 0; i < mappedNodes.size(); i++){
			if(virtualNode.equals(mappedNodes.get(i).getRight())){
				return mappedNodes.get(i).getLeft();
			}
		}
		return null;
	}

	public static ArrayList<String> populateFullGreedyFS(ArrayList<Pair<String>> mappedNodes,
			VirtualNetworkHeu virNet, SubstrateNetworkHeu subNet) {

		ArrayList<String> fs = new ArrayList<String>();
		for(int i = 0; i < virNet.getEdges().size(); i++){
			fs.add(Utils.getFullGreedySubstrateNodeIndex(mappedNodes, virNet.getEdges().get(i).getLeft(), subNet));
		}

		return fs;
	}

	public static ArrayList<String> populateFullGreedyFE(ArrayList<Pair<String>> mappedNodes,
			VirtualNetworkHeu virNet, SubstrateNetworkHeu subNet) {

		ArrayList<String> fe = new ArrayList<String>();
		for(int i = 0; i < virNet.getEdges().size(); i++){
			fe.add(Utils.getFullGreedySubstrateNodeIndex(mappedNodes, virNet.getEdges().get(i).getRight(), subNet));
		}

		return fe;
	}

	private static String getFullGreedySubstrateNodeIndex(ArrayList<Pair<String>> mappedNodes,
			String virtualNode, SubstrateNetworkHeu subNet) {

		System.out.println("getFullGreedySubstrateNodeIndex - virtualNode: "+ virtualNode+"---");
		for(int i = 0; i < mappedNodes.size(); i++){
			System.out.println("mappedNodes.get(i).getLeft(): "+ mappedNodes.get(i).getLeft()+"---");
			System.out.println("mappedNodes.get(i).getRight(): "+ mappedNodes.get(i).getRight()+"---");
			if(virtualNode.equals(mappedNodes.get(i).getRight())){
				System.out.println("return mappedNodes.get(i).getLeft();");
				return mappedNodes.get(i).getLeft();
			}
		}
		return null;
	}

	public static ArrayList<Pair<String>> convertFromAlphabet(
			ArrayList<Pair<String>> mappedNodes) {

		ArrayList<Pair<String>> mappedNodesAux = new ArrayList<Pair<String>>();
		for (int i = 0; i < mappedNodes.size(); i++){
			mappedNodesAux.add(new Pair<String>(""+Utils.convertFromAlphabet(mappedNodes.get(i).getLeft()), ""+Utils.convertFromAlphabet(mappedNodes.get(i).getRight())));
		}

		return mappedNodesAux;
	}

	public static boolean isComplyDelay(
			ArrayList<Pair<String>> mappedWorkingEdges, SubstrateNetworkHeu subNet, double latencyRequest,
			ArrayList<Pair<String>> mappedNodes, VirtualNetworkHeu virNet, int indexVirtualLink){

		String sourceSubstrateLink = "";
		String destinationSubstrateLink = "";
		net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.AllPaths.GraphAllPaths graph;
		LinkedList<String> visited = new LinkedList<String>();
		Pair<String> tmpEdge, tmpEdge2;

		for(int i = 0; i < mappedNodes.size(); i++){
			if(mappedNodes.get(i).getRight().equals(virNet.getEdge(indexVirtualLink).getLeft())){
				sourceSubstrateLink = mappedNodes.get(i).getLeft();
			}
			if(mappedNodes.get(i).getRight().equals(virNet.getEdge(indexVirtualLink).getRight())){
				destinationSubstrateLink = mappedNodes.get(i).getLeft();
			}
			if((!sourceSubstrateLink.equals("") && !destinationSubstrateLink.equals(""))){
				break;
			}
		}
		if((sourceSubstrateLink.equals("") || destinationSubstrateLink.equals(""))){
			return false;
		} 

		graph = constructGraph(mappedWorkingEdges);
		visited.add(sourceSubstrateLink);
		depthFirst(graph, visited, sourceSubstrateLink, destinationSubstrateLink);

		for(int i = 0; i < graph.getRoutes().size(); i++){
			double latencySubstratePath = 0.0;
			int indexSubstrateLink = -1;
			String left = "";
			String right = "";
			Iterator<String> it = graph.getRoutes().get(i).iterator();
			if(it != null){
				left = it.next();
			}
			while(it.hasNext()){
				right = it.next();
				tmpEdge = new Pair<String>(left, right);
				tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());
				if(subNet.getEdges().contains(tmpEdge))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
				else if(subNet.getEdges().contains(tmpEdge2))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);
				left = right;
				if(indexSubstrateLink == -1){
					return false;
				}
				latencySubstratePath += subNet.getEdgeLatency(indexSubstrateLink);
				indexSubstrateLink = -1;
			}
			if(latencySubstratePath > latencyRequest){
				return false;
			}
			latencySubstratePath = 0.0;
		}
		return true;
	}

	public static boolean isComplySecurity(
			ArrayList<Pair<String>> mappedWorkingEdges, SubstrateNetworkHeu subNet, double securityRequest,
			ArrayList<Pair<String>> mappedNodes, VirtualNetworkHeu virNet, int indexVirtualLink){

		String sourceSubstrateLink = "";
		String destinationSubstrateLink = "";
		net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.AllPaths.GraphAllPaths graph;
		LinkedList<String> visited = new LinkedList<String>();
		Pair<String> tmpEdge, tmpEdge2;

		for(int i = 0; i < mappedNodes.size(); i++){
			if(mappedNodes.get(i).getRight().equals(virNet.getEdge(indexVirtualLink).getLeft())){
				sourceSubstrateLink = mappedNodes.get(i).getLeft();
			}
			if(mappedNodes.get(i).getRight().equals(virNet.getEdge(indexVirtualLink).getRight())){
				destinationSubstrateLink = mappedNodes.get(i).getLeft();
			}
			if((!sourceSubstrateLink.equals("") && !destinationSubstrateLink.equals(""))){
				break;
			}
		}
		if((sourceSubstrateLink.equals("") || destinationSubstrateLink.equals(""))){
			return false;
		}

		graph = constructGraph(mappedWorkingEdges);
		depthFirst(graph, visited, sourceSubstrateLink, destinationSubstrateLink);

		for(int i = 0; i < graph.getRoutes().size(); i++){
			double securitySubstratelink = 0.0;
			int indexSubstrateLink = -1;
			String left = "";
			String right = "";
			Iterator<String> it = graph.getRoutes().get(i).iterator();
			if(it != null){
				left = it.next();
			}
			while(it.hasNext()){
				right = it.next();
				tmpEdge = new Pair<String>(left, right);
				tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());
				if(subNet.getEdges().contains(tmpEdge))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
				else if(subNet.getEdges().contains(tmpEdge2))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);
				left = right;
				if(indexSubstrateLink == -1){
					return false;
				}
				securitySubstratelink = subNet.getEdgeSec(indexSubstrateLink);

				if(securitySubstratelink < securityRequest){
					return false;
				}
				indexSubstrateLink = -1;
			}
		}
		return true;
	}

	private static net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.AllPaths.GraphAllPaths 
	constructGraph(ArrayList<Pair<String>> mappedWorkingEdges) {
		net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.AllPaths.GraphAllPaths graph = new 
				net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.AllPaths.GraphAllPaths();

		for (int i = 0; i < mappedWorkingEdges.size(); i++){
			graph.addEdge(mappedWorkingEdges.get(i).getLeft(), mappedWorkingEdges.get(i).getRight());
		}
		return graph;
	}

	public static void depthFirst(net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.AllPaths.GraphAllPaths graph, 
			LinkedList<String> visited, String start, String end){
		if(!start.equals("")){
			visited.add(start);	
		}

		LinkedList<String> nodes = graph.adjacentNodes(visited.getLast());
		// examine adjacent nodes
		for (String node : nodes) {
			if (visited.contains(node)) {
				continue;
			}
			if (node.equals(end)) {
				visited.add(node);
				LinkedHashSet<String> r = new LinkedHashSet<String>();
				for (String nodeVisited : visited) {
					r.add(nodeVisited);
				}
				graph.getRoutes().put(graph.getIndexRoutes(), r);
				graph.setIndexRoutes(graph.getIndexRoutes() + 1);
				visited.removeLast();
				break;
			}
		}
		for (String node : nodes) {
			if (visited.contains(node) || node.equals(end)) {
				continue;
			}
			visited.addLast(node);
			depthFirst(graph, visited, "", end);
			visited.removeLast();
		}
	}

	public static boolean isComplyBandwidth(
			LinkedList<Vertex> shortestPath, SubstrateNetworkHeu subNet, double bandwidthRequest,
			ArrayList<Pair<String>> mappedNodes, VirtualNetworkHeu virNet, int indexVirtualLink){

		String sourceSubstrateLink = "";
		String destinationSubstrateLink = "";
		Pair<String> tmpEdge, tmpEdge2;

		double bandwidthSubstratelink = 0.0;
		int indexSubstrateLink = -1;
		String left = "";
		String right = "";


		for(int i = 0; i < mappedNodes.size(); i++){
			if(mappedNodes.get(i).getRight().equals(virNet.getEdge(indexVirtualLink).getLeft())){
				sourceSubstrateLink = mappedNodes.get(i).getLeft();
			}
			if(mappedNodes.get(i).getRight().equals(virNet.getEdge(indexVirtualLink).getRight())){
				destinationSubstrateLink = mappedNodes.get(i).getLeft();
			}
			if((!sourceSubstrateLink.equals("") && !destinationSubstrateLink.equals(""))){
				break;
			}
		}
		if((sourceSubstrateLink.equals("") || destinationSubstrateLink.equals(""))){
			return false;
		}

		Iterator<Vertex> it = shortestPath.iterator();
		if(it != null){
			left = it.next().getName();
		}
		while(it.hasNext()){
			right = it.next().getName();
			tmpEdge = new Pair<String>(left, right);
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());
			if(subNet.getEdges().contains(tmpEdge))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
			else if(subNet.getEdges().contains(tmpEdge2))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);
			left = right;
			if(indexSubstrateLink == -1){
				return false;
			}
			bandwidthSubstratelink = subNet.getEdgeSec(indexSubstrateLink);

			if(bandwidthSubstratelink < bandwidthRequest){
				return false;
			}
			indexSubstrateLink = -1;
		}
		return true;
	}

	public static GraphShortestPath populateGraphShortestPathSubstrateInfo(SubstrateNetworkHeu subNet, double secEdge, double bandwidth){

		List<Vertex> nodes= new ArrayList<Vertex>();
		List<Edge> edges= new ArrayList<Edge>();

		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			nodes.add(new Vertex(""+i, subNet.getNode(i)));
		}
		for (int i = 0; i < subNet.getNumOfEdges(); i++) {
			if((subNet.getEdgeBw(i) >= bandwidth) && (subNet.getEdgeSec(i) >= secEdge)){
				int indexLeft = Utils.convertFromAlphabet(subNet.getEdge(i).getLeft());
				int indexRight = Utils.convertFromAlphabet(subNet.getEdge(i).getRight());
				edges.add(new Edge(subNet.getEdge(i).getLeft()+"_"+subNet.getEdge(i).getRight(), 
						nodes.get(indexLeft), nodes.get(indexRight), (int) (10000/subNet.getEdgeBw(i))));
				edges.add(new Edge(subNet.getEdge(i).getRight()+"_"+subNet.getEdge(i).getLeft(), 
						nodes.get(indexRight), nodes.get(indexLeft), (int) (10000/subNet.getEdgeBw(i))));
			}
		}
		GraphShortestPath graphShortestPath = new GraphShortestPath(nodes, edges);

		return graphShortestPath;
	}

	public static SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> populateGraphKShortestPathSubstrateInfo(SubstrateNetworkHeu subNet, double secEdge){

		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  graphKShortestPath = 
				new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>
		(DefaultWeightedEdge.class);

		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			graphKShortestPath.addVertex(subNet.getNode(i));
		}

		for (int i = 0; i < subNet.getNumOfEdges(); i++) {
			if((subNet.getEdgeSec(i)>=secEdge)){
				DefaultWeightedEdge e1 = graphKShortestPath.addEdge(subNet.getEdge(i).getLeft(), subNet.getEdge(i).getRight());
				graphKShortestPath.setEdgeWeight(e1, (int) (10000/subNet.getEdgeBw(i)));
				DefaultWeightedEdge e2 = graphKShortestPath.addEdge(subNet.getEdge(i).getRight(), subNet.getEdge(i).getLeft());
				graphKShortestPath.setEdgeWeight(e2, (int) (10000/subNet.getEdgeBw(i)));
			}
		}
		return graphKShortestPath;
	}

	public static Graph populateGraphKShortestPathSubstrateInfoK(SubstrateNetworkHeu subNet, double secEdge){

		Graph graphK = new VariableGraph(subNet, secEdge);

		return graphK;
	}

	public static Network populateGraphDisjointKShortestPathSubstrateInfo(SubstrateNetworkHeu subNet, double secEdge){

		Network graphDisjointKShortestPath = new Network();

		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			graphDisjointKShortestPath.addDevice(subNet.getNode(i));
		}
		for (int i = 0; i < subNet.getNumOfEdges(); i++) {

			if((subNet.getEdgeSec(i) >= (secEdge) && subNet.getEdgeBw(i) > 0)){
				graphDisjointKShortestPath.connectDevices(subNet.getEdge(i).getLeft(), subNet.getEdge(i).getRight(), (100000/subNet.getEdgeBw(i)));
			}
		}
		return graphDisjointKShortestPath;
	}

	public static Pair<String> getSourceDestinationNodes(ArrayList<Pair<String>> mappedWorkingNodes,
			VirtualNetworkHeu virNet, LinkHeuristicInfo virtualLinkHeuristicInfo) {

		String sourceSubstrateLink = "";
		String destinationSubstrateLink = "";

		for(int i = 0; i < mappedWorkingNodes.size(); i++){
			if(mappedWorkingNodes.get(i).getRight().equals(virNet.getEdge(virtualLinkHeuristicInfo.getIndexLink()).getLeft())){
				sourceSubstrateLink = mappedWorkingNodes.get(i).getLeft();
			}
			if(mappedWorkingNodes.get(i).getRight().equals(virNet.getEdge(virtualLinkHeuristicInfo.getIndexLink()).getRight())){
				destinationSubstrateLink = mappedWorkingNodes.get(i).getLeft();
			}
			if((!sourceSubstrateLink.equals("") && !destinationSubstrateLink.equals(""))){
				return new Pair<String>(sourceSubstrateLink, destinationSubstrateLink);
			}
		}
		return null;
	}

	public static boolean isComplyDelay(LinkedList<Vertex> path, SubstrateNetworkHeu subNet, double latencyRequest,
			ArrayList<Pair<String>> mappedWorkingNodes, VirtualNetworkHeu virNet, int indexVirtualLink){

		if(path == null){
			return false;
		} else{
			Pair<String> tmpEdge, tmpEdge2;
			double latencySubstratePath = 0.0;
			int indexSubstrateLink = -1;
			Vertex left = null;
			Vertex right;
			Iterator<Vertex> it = path.iterator();
			if(it != null){
				left = it.next();
			}
			while(it.hasNext()){
				right = it.next();
				tmpEdge = new Pair<String>(left.getName(), right.getName());
				tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());
				if(subNet.getEdges().contains(tmpEdge))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
				else if(subNet.getEdges().contains(tmpEdge2))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);
				left = right;
				if(indexSubstrateLink == -1){
					return false;
				}
				latencySubstratePath += subNet.getEdgeLatency(indexSubstrateLink);
				indexSubstrateLink = -1;
			}
			if(latencySubstratePath > latencyRequest){
				return false;
			}
		}
		return true;
	}

	public static boolean isComplyDelayK(List<BaseVertex> path, SubstrateNetworkHeu subNet, double latencyRequest,
			ArrayList<Pair<String>> mappedWorkingNodes, VirtualNetworkHeu virNet, int indexVirtualLink){

		if(path == null){
			return false;
		} else{
			Pair<String> tmpEdge, tmpEdge2;
			double latencySubstratePath = 0.0;
			int indexSubstrateLink = -1;
			BaseVertex left = null;
			BaseVertex right;
			Iterator<BaseVertex> it = path.iterator();
			if(it != null){
				left = it.next();
			}
			while(it.hasNext()){
				right = it.next();
				tmpEdge = new Pair<String>(Utils.convertToAlphabet(""+left.get_id()), Utils.convertToAlphabet(""+right.get_id()));
				tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());
				if(subNet.getEdges().contains(tmpEdge))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
				else if(subNet.getEdges().contains(tmpEdge2))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);
				left = right;
				if(indexSubstrateLink == -1){
					return false;
				}
				latencySubstratePath += subNet.getEdgeLatency(indexSubstrateLink);
				indexSubstrateLink = -1;
			}
			if(latencySubstratePath > latencyRequest){
				return false;
			}
		}
		return true;
	}

	public static boolean isComplyDelayDK(List<String> path, SubstrateNetworkHeu subNet, double latencyRequest,
			ArrayList<Pair<String>> mappedWorkingNodes, VirtualNetworkHeu virNet, int indexVirtualLink){

		if(path == null){
			return false;
		} else{
			Pair<String> tmpEdge, tmpEdge2;
			double latencySubstratePath = 0.0;
			int indexSubstrateLink = -1;
			String left = null;
			String right;
			Iterator<String> it = path.iterator();
			if(it != null){
				left = it.next();
			}
			while(it.hasNext()){
				right = it.next();
				tmpEdge = new Pair<String>(left, right);
				tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());
				if(subNet.getEdges().contains(tmpEdge))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
				else if(subNet.getEdges().contains(tmpEdge2))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);
				left = right;
				if(indexSubstrateLink == -1){
					return false;
				}
				latencySubstratePath += subNet.getEdgeLatency(indexSubstrateLink);
				indexSubstrateLink = -1;
			}
			if(latencySubstratePath > latencyRequest){
				return false;
			}
		}
		return true;
	}

	public static void copyEdgesUsed(ArrayList<Pair<String>> finalWorkingAndBackupEdges, LinkedList<Vertex> path, SubstrateNetworkHeu subNet,
			ArrayList<Pair<String>> finalVirtualWorkingAndBackupEdges, ArrayList<Pair<String>> mappedEdges) {

		Vertex left = null;
		Vertex right;
		Pair<String> tmpEdge, tmpEdge2;
		int indexSubstrateLink = -1;
		Iterator<Vertex> it = path.iterator();
		if(it != null){
			left = it.next();
		}
		while(it.hasNext()){
			right = it.next();
			tmpEdge = new Pair<String>(left.getName(), right.getName());
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
			else if(subNet.getEdges().contains(tmpEdge2))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);

			finalWorkingAndBackupEdges.add(new Pair<String>(subNet.getEdge(indexSubstrateLink).getLeft(), subNet.getEdge(indexSubstrateLink).getRight()));

			if(finalVirtualWorkingAndBackupEdges != null && mappedEdges != null){
				finalVirtualWorkingAndBackupEdges.add(new Pair<String>(mappedEdges.get(0).getLeft(), mappedEdges.get(0).getRight()));
			}

			left = right;
		}
	}

	public static void copyEdgesUsed(ArrayList<Pair<String>> finalWorkingAndBackupEdges, List<String> path, SubstrateNetworkHeu subNet) {

		String left = "";
		String right;
		Pair<String> tmpEdge, tmpEdge2;
		int indexSubstrateLink = -1;
		Iterator<String> it = path.iterator();
		if(it != null){
			left = it.next();
		}
		while(it.hasNext()){
			right = it.next();
			tmpEdge = new Pair<String>(left, right);
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
			else if(subNet.getEdges().contains(tmpEdge2))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);

			finalWorkingAndBackupEdges.add(new Pair<String>(subNet.getEdge(indexSubstrateLink).getLeft(), subNet.getEdge(indexSubstrateLink).getRight()));

			left = right;
		}
	}

	public static void copyEdgesUsedK(ArrayList<Pair<String>> finalWorkingAndBackupEdges, List<BaseVertex> path, SubstrateNetworkHeu subNet) {

		BaseVertex left = null;
		BaseVertex right;
		Pair<String> tmpEdge, tmpEdge2;
		int indexSubstrateLink = -1;
		Iterator<BaseVertex> it = path.iterator();
		if(it != null){
			left = it.next();
		}
		while(it.hasNext()){
			right = it.next();
			tmpEdge = new Pair<String>(Utils.convertToAlphabet(""+left.get_id()), Utils.convertToAlphabet(""+right.get_id()));
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
			else if(subNet.getEdges().contains(tmpEdge2))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);

			finalWorkingAndBackupEdges.add(new Pair<String>(subNet.getEdge(indexSubstrateLink).getLeft(), subNet.getEdge(indexSubstrateLink).getRight()));

			left = right;
		}
	}

	public static void copyEdgesUsedDK(ArrayList<Pair<String>> finalWorkingAndBackupEdges, List<String> path, SubstrateNetworkHeu subNet) {

		String left = null;
		String right;
		Pair<String> tmpEdge, tmpEdge2;
		int indexSubstrateLink = -1;
		Iterator<String> it = path.iterator();
		if(it != null){
			left = it.next();
		}
		while(it.hasNext()){
			right = it.next();
			tmpEdge = new Pair<String>(left, right);
			tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());

			if(subNet.getEdges().contains(tmpEdge))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
			else if(subNet.getEdges().contains(tmpEdge2))
				indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);

			finalWorkingAndBackupEdges.add(new Pair<String>(subNet.getEdge(indexSubstrateLink).getLeft(), subNet.getEdge(indexSubstrateLink).getRight()));

			left = right;
		}
	}

	public static double getMinAvailableBW(List<String> path, SubstrateNetworkHeu subNet) {

		double minBwAvail = 10000000.0;
		if(path == null){
			return 0;
		} else{
			Pair<String> tmpEdge, tmpEdge2;
			int indexSubstrateLink = -1;
			String left = null;
			String right;
			Iterator<String> it = path.iterator();
			if(it != null){
				left = it.next();
			}
			while(it.hasNext()){
				right = it.next();
				tmpEdge = new Pair<String>(left, right);
				tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());
				if(subNet.getEdges().contains(tmpEdge))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
				else if(subNet.getEdges().contains(tmpEdge2))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);
				left = right;
				if(indexSubstrateLink == -1){
					return 0;
				}
				if(minBwAvail > subNet.getEdgeBw(indexSubstrateLink)){
					minBwAvail = subNet.getEdgeBw(indexSubstrateLink);
				}
				indexSubstrateLink = -1;
			}
		}
		return minBwAvail;
	}

	public static boolean isComplyDelay(List<String> path, SubstrateNetworkHeu subNet, double latencyRequest,
			ArrayList<Pair<String>> mappedWorkingNodes, VirtualNetworkHeu virNet, int indexVirtualLink) {
		if(path == null){
			return false;
		} else{
			Pair<String> tmpEdge, tmpEdge2;
			double latencySubstratePath = 0.0;
			int indexSubstrateLink = -1;
			String left = null;
			String right;
			Iterator<String> it = path.iterator();
			if(it != null){
				left = it.next();
			}
			while(it.hasNext()){
				right = it.next();
				tmpEdge = new Pair<String>(left, right);
				tmpEdge2 = new Pair<String>(tmpEdge.getRight(), tmpEdge.getLeft());
				if(subNet.getEdges().contains(tmpEdge))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge);
				else if(subNet.getEdges().contains(tmpEdge2))
					indexSubstrateLink = subNet.getEdges().indexOf(tmpEdge2);
				left = right;
				if(indexSubstrateLink == -1){
					return false;
				}
				latencySubstratePath += subNet.getEdgeLatency(indexSubstrateLink);
				indexSubstrateLink = -1;
			}
			if(latencySubstratePath > latencyRequest){
				return false;
			}
		}
		return true;
	}

	public static double sumBandwitdhInPaths(HashMap<Integer, Double> smallerBandwidthInpahtForVirtualLinks){

		double sumBw = 0;
		Iterator<Entry<Integer, Double>> it = smallerBandwidthInpahtForVirtualLinks.entrySet().iterator();

		while(it.hasNext()){
			Map.Entry pair = it.next();
			sumBw += (double)pair.getValue();
		}
		return sumBw;
	}

	public static HashMap<Integer, Integer> quantization(
			HashMap<Integer, Double> smallerBandwidthInpahtForVirtualLinks, double requestBw, double sumBw){

		boolean adjustMaxInteger = false;
		long highestWeigthInOvS = 65535;
		int highestvalue = -1;
		HashMap<Integer, Integer> quantizationBandwidthInpahtForVirtualLinks = new HashMap<Integer, Integer>();
		Iterator<Entry<Integer, Double>> it2 = smallerBandwidthInpahtForVirtualLinks.entrySet().iterator();

		while(it2.hasNext()){
			Map.Entry pair = it2.next();
			double bw = ((double)pair.getValue()/sumBw)*requestBw;
			quantizationBandwidthInpahtForVirtualLinks.put((Integer) pair.getKey(), (int) (bw * 1000));
			if ((int) (bw * 1000) > highestWeigthInOvS){
				adjustMaxInteger = true;
				if((int) (bw * 1000) > highestvalue){
					highestvalue = (int) (bw * 1000);
				}
			}
		}
		if(adjustMaxInteger){
			Iterator<Entry<Integer, Integer>> it3 = quantizationBandwidthInpahtForVirtualLinks.entrySet().iterator();
			while(it3.hasNext()){
				Map.Entry pair = it3.next();
				long a = ((int) pair.getValue())* highestWeigthInOvS;
				long newValue = (a)/highestvalue;
				quantizationBandwidthInpahtForVirtualLinks.put((Integer) pair.getKey(), (int) newValue);
			}
		}
		return quantizationBandwidthInpahtForVirtualLinks;
	}

	public static HashMap<Integer, Double> getBwInpaths(
			HashMap<Integer, Double> smallerBandwidthInpahtForVirtualLinks, double requestBw, double sumBw) {

		//double sumBwAux = 0;
		HashMap<Integer, Double> bandwidthInpahtForVirtualLinks = new HashMap<Integer, Double>();
		Iterator<Entry<Integer, Double>> it2 = smallerBandwidthInpahtForVirtualLinks.entrySet().iterator();
		//int lastKey = -1;

		while(it2.hasNext()){
			Map.Entry pair = it2.next();
			double bw =  (((double)pair.getValue()/sumBw)*requestBw);
			bandwidthInpahtForVirtualLinks.put((Integer) pair.getKey(), Utils.roundDownDecimals(bw));
			//			lastKey = (Integer) pair.getKey();
			//			sumBwAux += bw;
		}
		//		sumBwAux -= bandwidthInpahtForVirtualLinks.get(lastKey);
		//		double lastValue = (((requestBw)-sumBwAux));
		//
		//		if(lastValue <= smallerBandwidthInpahtForVirtualLinks.get(lastKey)){
		//			bandwidthInpahtForVirtualLinks.put(lastKey, Utils.roundDownDecimals(lastValue));
		//		}else{
		//			bandwidthInpahtForVirtualLinks.put(lastKey, Utils.roundDownDecimals((smallerBandwidthInpahtForVirtualLinks.get(lastKey))));
		//		}
		return bandwidthInpahtForVirtualLinks;
	}

	public static List<String> getDisjointKShortestPath(List<Link> list, Device source, Device destination) {

		List<String> path = new LinkedList<String>();

		Device emergentDevice = source;
		path.add(emergentDevice.toString());
		for ( Link hop: list )
		{
			emergentDevice = net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.DisjointKShortestPath.graph.Utils.getOtherEndpoint(hop, emergentDevice );
			path.add(emergentDevice.toString());
		}
		return path;
	}

	public static List<List<Link>> mergePaths(List<List<Link>> pathsDisjointKShortestPath, List<List<Link>> shortestPath2) {

		ArrayList<List<Link>> auxList = new ArrayList<List<Link>>();
		if(pathsDisjointKShortestPath != null)
			auxList.addAll(pathsDisjointKShortestPath);
		if(shortestPath2 != null)
			auxList.addAll(shortestPath2);

		return auxList;
	}
	
	private static ArrayList<String> findSubstrateNeighborsNodes(VirtualNetworkHeu virtualNetwork, ArrayList<Pair<String>> mappingVirtualNodes,
			NodeHeuristicInfo virtualNodeHeuristicInfo) {
		ArrayList<String> substrateNeighborsNodes = new ArrayList<>();
		ArrayList<String> virtualNeighborsNodes = new ArrayList<>();
		String currentNode = virtualNetwork.getNode(virtualNodeHeuristicInfo.getIndexNode());
		for (int i =0; i < virtualNetwork.getEdges().size(); i++)
			if (virtualNetwork.getEdges().get(i).getLeft().equals(currentNode))
				virtualNeighborsNodes.add(virtualNetwork.getEdges().get(i).getRight());
			else if (virtualNetwork.getEdges().get(i).getRight().equals(currentNode))
				virtualNeighborsNodes.add(virtualNetwork.getEdges().get(i).getLeft());

		for (int i = 0; i < mappingVirtualNodes.size(); i++)
			for (int j = 0; j < virtualNeighborsNodes.size(); j++)
				if(mappingVirtualNodes.get(i).getRight().equals(virtualNeighborsNodes.get(j)))
					substrateNeighborsNodes.add(mappingVirtualNodes.get(i).getLeft());

		return substrateNeighborsNodes;
	}
	
	private static double calculateAverageDistance(ArrayList<String> substrateNeighborsNodes, SubstrateNetworkHeu subNet, int substrateIndexAux) {
		int[][] distances = subNet.getDistancesBetweenNodes();
		double averageDistance = 0;

		if(substrateNeighborsNodes.size() == 0)
			return 1;
		for (int i = 0; i < substrateNeighborsNodes.size(); i++){
			averageDistance += distances[substrateIndexAux][Utils.convertFromAlphabet(substrateNeighborsNodes.get(i))];
		}
		averageDistance = averageDistance/substrateNeighborsNodes.size();
		return averageDistance;
	}
	
	private static NodeHeuristicInfo calculateUtilityDividedByDistances(NodeHeuristicInfo key, double averageDistance) {
		NodeHeuristicInfo auxUtility = new NodeHeuristicInfo(key.getIndexNode(), key.getNodeUtility()/averageDistance, -1, key.getBelongsToCloud());
		auxUtility.setX(key.getNodeUtility());
		return auxUtility;
	}
	
	public static int[][] distanceHopsNode(SubstrateNetworkHeu subNet){
		Network graph = Utils.populateGraphDisjointKShortestPathSubstrateInfoDEFAULT(subNet, 0);
		int [][] distances = new int[subNet.getNumOfNodes()][subNet.getNumOfNodes()];
		int auxDistance = -1;
		for(int i = 0; i < subNet.getNumOfNodes(); i++){
			distances[i][i] = 0;
			for(int j = i+1; j < subNet.getNumOfNodes(); j++ ){
				auxDistance = Utils.calculateDistanceBetweenNodes(i, j, subNet, graph);
				distances[i][j] = auxDistance;
				distances[j][i] = auxDistance;
			}
		}
		return distances;
	}
	
	public static Network populateGraphDisjointKShortestPathSubstrateInfoDEFAULT(SubstrateNetworkHeu subNet, double secEdge){

		Network graphDisjointKShortestPath = new Network();
		for (int i = 0; i < subNet.getNumOfNodes(); i++) {
			graphDisjointKShortestPath.addDevice(subNet.getNode(i));
		}
		for (int i = 0; i < subNet.getNumOfEdges(); i++) {
			if((subNet.getEdgeSec(i) >= (secEdge) && subNet.getEdgeBw(i) > 0)){
				graphDisjointKShortestPath.connectDevices(subNet.getEdge(i).getLeft(), subNet.getEdge(i).getRight(), 1.0);
			}
		}
		return graphDisjointKShortestPath;
	}
	
	public static int calculateDistanceBetweenNodes(int i, int j, SubstrateNetworkHeu subNet, Network graph) {

		graph = Utils.populateGraphDisjointKShortestPathSubstrateInfoDEFAULT(subNet, 0);
		Device source = graph.getDevice(convertToAlphabet(""+i));
		Device destination = graph.getDevice(convertToAlphabet(""+j));
		List<List<Link>> pathsDisjointKShortestPath = EdgeDisjointShortestPair.findShortestPath(graph,
				source, destination);
		if (pathsDisjointKShortestPath == null || pathsDisjointKShortestPath.size() == 0)
			return -1;
		List<String> path = Utils.getDisjointKShortestPath(pathsDisjointKShortestPath.get(0),
				source, destination);

		if (path == null || path.size() == 0)
			return -1;
		else{
			return path.size() - 1;
		}
	}
}
