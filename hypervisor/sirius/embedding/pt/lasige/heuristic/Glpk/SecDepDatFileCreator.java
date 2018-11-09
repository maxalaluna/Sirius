package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.SubstrateNetworkHeu;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;

/**
 * Handles the creation of a structured file to be the input of the SecDep formulation
 * @author Luis Ferrolho, fc41914, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class SecDepDatFileCreator{

	/**
	 * Constructs the input file for SecDep
	 * @param file Filepath
	 * @param subNet Substrate network
	 * @param virNet Virtual network
	 */
	public void createDatFile(String file, SubstrateNetworkHeu subNet, VirtualNetworkHeu virNet) {

		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			putSubNetInfo(bufferedWriter, subNet);
			putVirNetInfo(bufferedWriter, virNet);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Constructs the input file for SecDep with new security parameters
	 * @param file Filepath
	 * @param subNet Substrate network
	 * @param virNet Virtual network
	 */
	public void createDatFileNewSecParam(String file, SubstrateNetworkHeu subNet, VirtualNetworkHeu virNet) {

		try {

			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			putSubNetInfo(bufferedWriter, subNet);
			putVirNetInfo(bufferedWriter, virNet);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Inserts structured substrate network info into the files
	 * @param bufferedWriter Writer of the file
	 * @param subNet Substrate network
	 * @throws IOException
	 */
	public void putSubNetInfo(BufferedWriter bufferedWriter, SubstrateNetworkHeu subNet) throws IOException {
		
		int sNodes = subNet.getNumOfNodes();
		int sEdges = subNet.getNumOfEdges();
		bufferedWriter.write("data;\n\n"); // Initialize
		bufferedWriter.write("# Nodes: "+sNodes+", Edges: "+sEdges+"\n\n");
		// ---------- Set SNodes ----------
		bufferedWriter.write("set SNodes := ");
		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(subNet.getNode(i)+" ");
		bufferedWriter.write(";\n\n");
		// ---------- Param cpuSup ----------
		bufferedWriter.write("param cpuSup := \n");
		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(subNet.getNode(i)+" "+subNet.getNodeCPU(i)+"\n");

		bufferedWriter.write(";\n\n");
		// ---------- Param secSup ----------
		bufferedWriter.write("param secSupNode := \n");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(subNet.getNode(i)+" "+subNet.getNodeSec(i)+"\n");

		bufferedWriter.write(";\n\n");
		// ---------- Param bwSup ----------
		bufferedWriter.write("param bwSup:\n  ");
      
		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(subNet.getNode(i)+" ");
      
		bufferedWriter.write(":=\n");

		for(int i = 0; i < sNodes; i ++){
			bufferedWriter.write(subNet.getNode(i)+" ");

			for(int j = 0; j < sNodes; j++){
				if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))
      				bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
				else if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
      				bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
				else
					bufferedWriter.write("0 ");
			}
			bufferedWriter.write("\n");
		}
      
		bufferedWriter.write(";\n\n");
		// ---------- Param secSupEdge ----------
		bufferedWriter.write("param secSupEdge :\n  ");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(subNet.getNode(i)+" ");

		bufferedWriter.write(":=\n");

		for(int i = 0; i < sNodes; i ++){
			bufferedWriter.write(subNet.getNode(i)+" ");

			for(int j = 0; j < sNodes; j++){
				if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))
					bufferedWriter.write(subNet.getEdgeSec(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
				else if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
					bufferedWriter.write(subNet.getEdgeSec(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
				else
					bufferedWriter.write("0 ");
			}
			bufferedWriter.write("\n");
		}

		bufferedWriter.write(";\n\n");
		// ---------- Param weight ----------
		bufferedWriter.write("param weight :\n  ");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(subNet.getNode(i)+" ");

		bufferedWriter.write(":=\n");

		for(int i = 0; i < sNodes; i ++){
			bufferedWriter.write(subNet.getNode(i)+" ");

			for(int j = 0; j < sNodes; j++){
				if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))
					bufferedWriter.write(subNet.getEdgeWeight(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
				else if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
					bufferedWriter.write(subNet.getEdgeWeight(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
				else
					bufferedWriter.write("0 ");
			}
			bufferedWriter.write("\n");
		}

		bufferedWriter.write(";\n\n");
		// ---------- Set Clouds ----------
		bufferedWriter.write("set Clouds := ");

		for(int i = 0; i < subNet.getNClouds(); i++)
			bufferedWriter.write(i+" ");

		bufferedWriter.write(";\n\n");
        // ---------- Param doesItBelong ----------
        bufferedWriter.write("param doesItBelong := \n");
        
        for(int i = 0; i < subNet.getNClouds(); i ++){
        	for(int j = 0; j < sNodes; j++){
        		bufferedWriter.write(i+" "+subNet.getNode(j)+" "+subNet.getDoesItBelong(i,j)+"\n");
        	}
        }
        
        bufferedWriter.write(";\n\n");
        // ---------- Param cloudSecSup ----------
        bufferedWriter.write("param cloudSecSup := \n");
        
        for(int i = 0; i < subNet.getNClouds(); i ++){
        	for(int j = 0; j < sNodes; j++){
        		if(subNet.getDoesItBelong(i,j) == 1)
        			bufferedWriter.write(subNet.getNode(j)+" "+subNet.getCloudSecurity(i)+"\n");
        	}
        }
        
        bufferedWriter.write(";\n\n");
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
			bufferedWriter.write("("+tmp.getLeft()+","+tmp.getRight()+") ("+tmp.getRight()+","+tmp.getLeft()+") ");
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
			bufferedWriter.write(tmp.getRight()+" "+tmp.getLeft()+" "+virNet.getEdgeBw(i)+"\n");
		}
		
		bufferedWriter.write(";\n\n");
		// ---------- Param secDemEdge ----------
		bufferedWriter.write("param secDemEdge := \n");
		
		for(int i = 0; i < vEdges; i++){
			tmp = virNet.getEdge(i);
			bufferedWriter.write(tmp.getLeft()+" "+tmp.getRight()+" "+virNet.getEdgeSec(i)+"\n");
			bufferedWriter.write(tmp.getRight()+" "+tmp.getLeft()+" "+virNet.getEdgeSec(i)+"\n");
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
		
		bufferedWriter.write("param wantBackupNode := \n");

		for(int i = 0; i < vNodes; i++){
			bufferedWriter.write(virNet.getNode(i)+" "+(virNet.getWantBackupNode(i) ? 1 : 0)+"\n");
		}
		
		bufferedWriter.write(";\n\n");
		bufferedWriter.write("end;\n");
	}
}
