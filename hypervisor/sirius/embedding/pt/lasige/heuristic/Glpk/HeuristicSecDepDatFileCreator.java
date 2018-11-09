package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic.LinkHeuristicInfo;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.SubstrateNetworkHeu;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;

/**
 * Handles the creation of a structured file to be the input of the HeuristicSecDep formulation
 * @author Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class HeuristicSecDepDatFileCreator{

	/**
	 * Constructs the input file for SecDep
	 * @param file Filepath
	 * @param subNet Substrate network
	 * @param virNet Virtual network
	 * @param mappedNodes 
	 */
	public void createDatFile(String file, SubstrateNetworkHeu subNet, VirtualNetworkHeu virNet, ArrayList<Pair<String>> mappedNodes, LinkHeuristicInfo linkHeuristicInfo) {

		try {

			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			putNetInfo(bufferedWriter, subNet, virNet, mappedNodes, linkHeuristicInfo);

			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inserts structured substrate network info into the files
	 * @param bufferedWriter Writer of the file
	 * @param subNet Substrate network
	 * @param virNet 
	 * @param linkHeuristicInfo 
	 * @throws IOException
	 */
	public void putNetInfo(BufferedWriter bufferedWriter, SubstrateNetworkHeu subNet, VirtualNetworkHeu virNet, ArrayList<Pair<String>> mappedNodes, LinkHeuristicInfo virtualLinkHeuristicInfo) throws IOException {

		int sNodes = subNet.getNumOfNodes();
		int sEdges = subNet.getNumOfEdges();
		String sourceSubstrateLink = "";
		String destinationSubstrateLink = "";

		bufferedWriter.write("data;\n\n"); // Initialize
		bufferedWriter.write("# Nodes: "+sNodes+", Edges: "+sEdges+"\n\n");

		// ---------- Set Substrate Nodes ----------
		bufferedWriter.write("set N := ");

		for(int i = 0; i < sNodes; i++){

			//bufferedWriter.write(subNet.getNode(i)+" ");
			bufferedWriter.write(i+" ");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Set Virtual Link ----------
		for(int i = 0; i < mappedNodes.size(); i++){

			if(mappedNodes.get(i).getRight().equals(virNet.getEdge(virtualLinkHeuristicInfo.getIndexLink()).getLeft())){
				sourceSubstrateLink = mappedNodes.get(i).getLeft();
			}
			if(mappedNodes.get(i).getRight().equals(virNet.getEdge(virtualLinkHeuristicInfo.getIndexLink()).getRight())){
				destinationSubstrateLink = mappedNodes.get(i).getLeft();
			}
			if((!sourceSubstrateLink.equals("") && !destinationSubstrateLink.equals(""))){
				break;
			}
		}

		bufferedWriter.write("set F := f"+sourceSubstrateLink+"_"+destinationSubstrateLink+"_"+virtualLinkHeuristicInfo.getIndexLink());

		bufferedWriter.write(";\n\n");

		// ---------- Param bw ----------
		bufferedWriter.write("param bw:\n  ");

		for(int i = 0; i < sNodes; i++)
			//bufferedWriter.write(subNet.getNode(i)+" ");
			bufferedWriter.write(i+" ");

		bufferedWriter.write(":=\n");

		for(int i = 0; i < sNodes; i ++){
			//bufferedWriter.write(subNet.getNode(i)+" ");
			bufferedWriter.write(i+" ");

			for(int j = 0; j < sNodes; j++){

				int indexEdgeIJ = subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j)));
				//if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))) && 
				if(indexEdgeIJ != -1 && 
						(subNet.getEdgeSec(indexEdgeIJ))
						>= virNet.getEdgeSec(virtualLinkHeuristicInfo.getIndexLink())){

					bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
				}else{
					int indexEdgeJI = subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i)));
					if( indexEdgeJI != -1 && 
							(subNet.getEdgeSec(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
									>= virNet.getEdgeSec(virtualLinkHeuristicInfo.getIndexLink()))){
						bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
					}else{
						bufferedWriter.write("0 ");
					}
				}
			}
			bufferedWriter.write("\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Param secLink ----------
		bufferedWriter.write("param secLink:\n  ");

		for(int i = 0; i < sNodes; i++){
			//bufferedWriter.write(subNet.getNode(i)+" ");
			bufferedWriter.write(i+" ");
		}


		bufferedWriter.write(":=\n");

		for(int i = 0; i < sNodes; i ++){
			//bufferedWriter.write(subNet.getNode(i)+" ");
			bufferedWriter.write(i+" ");

			for(int j = 0; j < sNodes; j++){
				int indexEdgeIJ = subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j)));
				if(indexEdgeIJ != -1){
					bufferedWriter.write(subNet.getEdgeSec(indexEdgeIJ)+" ");
				}else{
					int indexEdgeJI = subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i)));
					if(indexEdgeJI != -1){
						bufferedWriter.write(subNet.getEdgeSec(indexEdgeJI)+" ");
					}
					else{
						bufferedWriter.write("0 ");
					}
				}
			}
			bufferedWriter.write("\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Set Substrate Source Link ----------
		bufferedWriter.write("param fs :=\n");
		bufferedWriter.write("f"+sourceSubstrateLink+"_"+destinationSubstrateLink+"_"+virtualLinkHeuristicInfo.getIndexLink()+"      "+subNet.getNodes().indexOf(sourceSubstrateLink)+"\n");

		bufferedWriter.write(";\n\n");

		// ---------- Set Substrate Destination Link ----------
		bufferedWriter.write("param fe :=\n");
		bufferedWriter.write("f"+sourceSubstrateLink+"_"+destinationSubstrateLink+"_"+virtualLinkHeuristicInfo.getIndexLink()+"      "+subNet.getNodes().indexOf(destinationSubstrateLink)+"\n");

		bufferedWriter.write(";\n\n");

		// ---------- Set Virtual bandwidth ----------
		bufferedWriter.write("param fd :=\n");
		bufferedWriter.write("f"+sourceSubstrateLink+"_"+destinationSubstrateLink+"_"+virtualLinkHeuristicInfo.getIndexLink()+"      "+virNet.getEdgeBw(virtualLinkHeuristicInfo.getIndexLink())+"\n");

		bufferedWriter.write(";\n\n");

		bufferedWriter.write("end;\n");

		//		// ---------- Param weight ----------
		//		bufferedWriter.write("param weight :\n  ");
		//
		//		for(int i = 0; i < sNodes; i++)
		//			bufferedWriter.write(subNet.getNode(i)+" ");
		//
		//		bufferedWriter.write(":=\n");
		//
		//		for(int i = 0; i < sNodes; i ++){
		//			bufferedWriter.write(subNet.getNode(i)+" ");
		//
		//			for(int j = 0; j < sNodes; j++){
		//				if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))
		//					bufferedWriter.write(subNet.getEdgeWeight(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
		//				else if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
		//					bufferedWriter.write(subNet.getEdgeWeight(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
		//				else
		//					bufferedWriter.write("0 ");
		//			}
		//			bufferedWriter.write("\n");
		//		}
		//
		//		bufferedWriter.write(";\n\n");

		//		// ---------- Set Clouds ----------
		//		bufferedWriter.write("set Clouds := ");
		//
		//		for(int i = 0; i < subNet.getNClouds(); i++)
		//			bufferedWriter.write(i+" ");
		//
		//		bufferedWriter.write(";\n\n");
		//		
		//        // ---------- Param doesItBelong ----------
		//        bufferedWriter.write("param doesItBelong := \n");
		//        
		//        for(int i = 0; i < subNet.getNClouds(); i ++){
		//        	for(int j = 0; j < sNodes; j++){
		//        		bufferedWriter.write(i+" "+subNet.getNode(j)+" "+subNet.getDoesItBelong(i,j)+"\n");
		//        	}
		//        }
		//        
		//        bufferedWriter.write(";\n\n");

		//        // ---------- Param cloudSecSup ----------
		//        bufferedWriter.write("param cloudSecSup := \n");
		//        
		//        for(int i = 0; i < subNet.getNClouds(); i ++){
		//        	for(int j = 0; j < sNodes; j++){
		//        		if(subNet.getDoesItBelong(i,j) == 1)
		//        			bufferedWriter.write(subNet.getNode(j)+" "+subNet.getCloudSecurity(i)+"\n");
		//        	}
		//        }
		//        
		//        bufferedWriter.write(";\n\n");
	}

	/**
	 * Inserts structured virtual network info into the file
	 * @param bufferedWriter Writer of the file
	 * @param virNet Virtual network
	 * @throws IOException
	 */
	public void putVirNetInfo(BufferedWriter bufferedWriter, VirtualNetworkHeu virNet) throws IOException {

		int vNodes = virNet.getNumOfNodes();
		int vEdges = virNet.getNumOfEdges();

		Pair<String> tmp = null;
		// ---------- Set VNodes ----------
		bufferedWriter.write("set VNodes := ");

		for(int i = 0; i < vNodes; i++)
			bufferedWriter.write(virNet.getNode(i)+" ");

		bufferedWriter.write(";\n\n");
		// ---------- Set VEdges ----------
		bufferedWriter.write("set VEdges := ");

		for(int i = 0; i < vEdges; i++){
			tmp = virNet.getEdge(i);
			bufferedWriter.write("("+tmp.getLeft()+","+tmp.getRight()+") ");
		}

		bufferedWriter.write(";\n\n");
		// ---------- Param cpuDem ----------
		bufferedWriter.write("param cpuDem := \n");

		for(int i = 0; i < vNodes; i++)
			bufferedWriter.write(virNet.getNode(i)+" "+virNet.getNodeCPU(i)+"\n");

		bufferedWriter.write(";\n\n");
		// ---------- Param secDemNode ----------
		bufferedWriter.write("param secDemNode := \n");

		for(int i = 0; i < vNodes; i++)
			bufferedWriter.write(virNet.getNode(i)+" "+virNet.getNodeSec(i)+"\n");

		bufferedWriter.write(";\n\n");
		// ---------- Param bwDem ----------
		bufferedWriter.write("param bwDem := \n");

		for(int i = 0; i < vEdges; i++){
			tmp = virNet.getEdge(i);
			bufferedWriter.write(tmp.getLeft()+" "+tmp.getRight()+" "+virNet.getEdgeBw(i)+"\n");
		}

		bufferedWriter.write(";\n\n");
		// ---------- Param secDemEdge ----------
		bufferedWriter.write("param secDemEdge := \n");

		for(int i = 0; i < vEdges; i++){
			tmp = virNet.getEdge(i);
			bufferedWriter.write(tmp.getLeft()+" "+tmp.getRight()+" "+virNet.getEdgeSec(i)+"\n");
		}

		bufferedWriter.write(";\n\n");
		// ---------- Param cloudSecDem ----------
		bufferedWriter.write("param cloudSecDem := \n");

		for(int i = 0; i < vNodes; i++){
			bufferedWriter.write(virNet.getNode(i)+" "+virNet.getCloudSecurity(i)+"\n");
		}

		bufferedWriter.write(";\n\n");
		// ---------- Param bvNodeLocalization ----------
		bufferedWriter.write("param bvNodeLocalization := \n");

		for(int i = 0; i < vNodes; i++){
			bufferedWriter.write(virNet.getNode(i)+" "+virNet.getBackupLocalization(i)+"\n");
		}

		bufferedWriter.write(";\n\n");
		// ---------- Param wantBackup ----------
		bufferedWriter.write("param wantBackup := \n");
		bufferedWriter.write((virNet.getWantBackup() ? 1 : 0)+"\n");
		bufferedWriter.write(";\n\n");
		bufferedWriter.write("end;\n");
	}
}
