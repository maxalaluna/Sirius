package net.floodlightcontroller.sirius.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Utils;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk.OutputFileReader;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;

public class Test4 {
	private double backupResources;
	private static String executionTime = "0";
	private static boolean wasAccepted = true;

	private static ArrayList<Pair<String>> wEdgesUsed = new ArrayList<>() ;
	private static ArrayList<Pair<String>> wMappedEdges = new ArrayList<>() ;
	private static ArrayList<Pair<String>> edgesUsed = new ArrayList<>() ;
	private static ArrayList<Pair<String>> mappedEdges = new ArrayList<>() ;
	private static ArrayList<Double> bwEdgesUsed = new ArrayList<>() ;
	private static ArrayList<String> virtualEdgeUsed = new ArrayList<>() ;
	static HashMap<String,ArrayList<Pair<String>>> indexVirtualLinkToEdgeUsed = new HashMap<String,ArrayList<Pair<String>>>();

	public static void main(String[] args) {
		OutputFileReader fileReaders = new OutputFileReader();
		VirtualNetworkHeu virNet = new VirtualNetworkHeu(0);
		virNet.addEdge(new Pair<String>(Utils.convertToAlphabet("58"),Utils.convertToAlphabet("35")));
		virNet.addEdge(new Pair<String>(Utils.convertToAlphabet("58"),Utils.convertToAlphabet("6")));
		virNet.addEdge(new Pair<String>(Utils.convertToAlphabet("81"),Utils.convertToAlphabet("8")));
		virNet.addEdge(new Pair<String>(Utils.convertToAlphabet("90"),Utils.convertToAlphabet("6")));
		virNet.addEdge(new Pair<String>(Utils.convertToAlphabet("90"),Utils.convertToAlphabet("19")));
		virNet.addEdge(new Pair<String>(Utils.convertToAlphabet("90"),Utils.convertToAlphabet("8")));
		virNet.addEdge(new Pair<String>(Utils.convertToAlphabet("19"),Utils.convertToAlphabet("16")));
		virNet.addEdge(new Pair<String>(Utils.convertToAlphabet("16"),Utils.convertToAlphabet("36")));
		virNet.addEdgeBw(0); virNet.addEdgeBw(1); virNet.addEdgeBw(2); virNet.addEdgeBw(3);
		virNet.addEdgeBw(4); virNet.addEdgeBw(5); virNet.addEdgeBw(6); virNet.addEdgeBw(7);
		
		String line = null;
		String[] parts = null, parts2 = null;

		Pair<String> tmp = null;

		double partialExecutionTime = 0;

		try {
			String outputFile = "/home/floodlight/src/main/java/net/floodlightcontroller/sirius/tests/randomMCF_req.txt";
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
			Iterator<Entry<String, ArrayList<Pair<String>>>> it = indexVirtualLinkToEdgeUsed.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry pair = it.next();
				String key = (String) pair.getKey();
				ArrayList<Pair<String>> edgesInAPath = indexVirtualLinkToEdgeUsed.get(key);
				String paths = "";
				for(int i = 0; i < edgesInAPath.size(); i++)
					paths +=" ("+edgesInAPath.get(i).getLeft()+","+edgesInAPath.get(i).getRight()+")";
				System.out.print("Working links for ("+virNet.getEdge(Integer.parseInt(key)).getLeft()+","+virNet.getEdge(Integer.parseInt(key)).getRight()+") ->"+paths+"\n");
//				for(int i =0; i < edgesInAPath.size(); i++)
//					System.out.print("("+edgesInAPath.get(i).getLeft()+","+edgesInAPath.get(i).getRight()+") ");
//				System.out.println();
			}			
			executionTime = partialExecutionTime +"";
			bufferedReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
