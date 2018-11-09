package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.ResourceGenerator;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Utils;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.SubstrateNetworkHeu;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;

/**
 * Create dat files
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class DVineDatFileCreator {
	
	private ResourceGenerator resGen;
	
	public DVineDatFileCreator() {
		this.resGen = new ResourceGenerator();
	}

	/**
	 * Constructs the input file for DViNE
	 * @param file Filepath
	 * @param subNet Substrate network
	 * @param virNet Virtual network
	 */
	public void createDatFile(String file, SubstrateNetworkHeu subNet, VirtualNetworkHeu virNet) {

		try {

			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			int sNodes = subNet.getNumOfNodes();
			int sEdges = subNet.getNumOfEdges();
			int vNodes = virNet.getNumOfNodes();
			int vEdges = virNet.getNumOfEdges();
			
			bufferedWriter.write("data;\n\n"); // Initialize
			bufferedWriter.write("# Nodes: "+sNodes+", Edges: "+sEdges+"\n\n");
			
			// ---------- Set of Substrate Nodes ----------
			bufferedWriter.write("set N := ");

			for(int i = 0; i < sNodes; i++)
				bufferedWriter.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");

			bufferedWriter.write(";\n");
			
			// ---------- Set of Virtual Nodes ----------
			bufferedWriter.write("set M := ");

			for(int i = 0; i < vNodes; i++)
				bufferedWriter.write((Integer.parseInt(virNet.getNode(i))+sNodes)+" ");

			bufferedWriter.write(";\n");
			
			// ---------- Set of Virtual Flows ----------
			bufferedWriter.write("set F := ");

			for(int i = 0; i < vEdges; i++)
				bufferedWriter.write("f"+i+" ");

			bufferedWriter.write(";\n\n");
			
			// ---------- Param CPU ----------
			bufferedWriter.write("param p := \n");

			for(int i = 0; i < sNodes; i++)
				bufferedWriter.write(Utils.convertFromAlphabet(subNet.getNode(i))+" "+subNet.getNodeCPU(i)+"\n");
			
			for(int i = 0; i < vNodes; i++)
				bufferedWriter.write((Integer.parseInt(virNet.getNode(i))+sNodes)+" "+virNet.getNodeCPU(i)+"\n");

			bufferedWriter.write(";\n\n");
			
			// ---------- Param bandwidth ----------
			// Constructs a matrix with the bandwidth in each existent edge
			ArrayList<Pair<String>> candidates = new ArrayList<>();
			int isCandidate = 0;
			
//			for(int i = 0; i < vNodes; i++){
//				for(int j = 0; j < sNodes; j++){
//					
//					//changed 3jan17
////					if(subNet.getNodeCPU(j) >= virNet.getNodeCPU(i))
//					if((subNet.getNodeCPU(j) >= virNet.getNodeCPU(i)) || true)
//						isCandidate = resGen.generateSecurity(100);
//					else
//						isCandidate = 0;
//					
//					if((isCandidate >= 90))
//						candidates.add(new Pair<String>(String.valueOf(j),String.valueOf(i)));
//				}	
//			}
			
			for(int i = 0; i < vNodes; i++){
				for(int j = 0; j < sNodes; j++){
					
					if(subNet.getNodeCPU(j) >= virNet.getNodeCPU(i))
						isCandidate = resGen.generateSecurity(2);
					else
						isCandidate = 0;
					
					if(isCandidate == 1)
						candidates.add(new Pair<String>(String.valueOf(j),String.valueOf(i)));
				}	
			}
			
			bufferedWriter.write("param b :\n  ");

			for(int i = 0; i < sNodes; i++)
				bufferedWriter.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");
			
			for(int i = 0; i < vNodes; i++)
				bufferedWriter.write((Integer.parseInt(virNet.getNode(i))+sNodes)+" ");

			bufferedWriter.write(":=\n");

			for(int i = 0; i < sNodes; i ++){
				
				bufferedWriter.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");

				for(int j = 0; j < sNodes; j++){
					if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))
						bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
					else if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
						bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
					else
						bufferedWriter.write("0 ");
				}
				
				for(int j = 0; j < vNodes; j++){
					if(candidates.contains(new Pair<String>(String.valueOf(i), String.valueOf(j))))
						bufferedWriter.write("100 ");
					else
						bufferedWriter.write("0 ");
				}
				
				bufferedWriter.write("\n");
			}
			
			for(int i = 0; i < vNodes; i ++){
				bufferedWriter.write((Integer.parseInt(virNet.getNode(i))+sNodes)+" ");

				for(int j = 0; j < sNodes; j++){
					if(candidates.contains(new Pair<String>(String.valueOf(j), String.valueOf(i))))
						bufferedWriter.write("100 ");
					else
						bufferedWriter.write("0 ");
				}
				
				for(int j = 0; j < vNodes; j++)
					bufferedWriter.write("0 ");
				
				bufferedWriter.write("\n");
			}

			bufferedWriter.write(";\n\n");
			
			// ---------- Param alpha ----------
			bufferedWriter.write("param alpha :\n  ");

			for(int i = 0; i < sNodes; i++)
				bufferedWriter.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");
			
			for(int i = 0; i < vNodes; i++)
				bufferedWriter.write((Integer.parseInt(virNet.getNode(i))+sNodes)+" ");

			bufferedWriter.write(":=\n");

			for(int i = 0; i < sNodes; i ++){
				bufferedWriter.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");

				for(int j = 0; j < sNodes; j++){
					if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))
						bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
					else if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
						bufferedWriter.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
					else
						bufferedWriter.write("1 ");
				}
				
				for(int j = 0; j < vNodes; j++){
					if(candidates.contains(new Pair<String>(String.valueOf(i), String.valueOf(j))))
						bufferedWriter.write("100 ");
					else
						bufferedWriter.write("1 ");
				}
				
				bufferedWriter.write("\n");
			}
			
			for(int i = 0; i < vNodes; i ++){
				bufferedWriter.write((Integer.parseInt(virNet.getNode(i))+sNodes)+" ");
				
				for(int j = 0; j < sNodes; j++){
					if(candidates.contains(new Pair<String>(String.valueOf(j), String.valueOf(i))))
						bufferedWriter.write("100 ");
					else
						bufferedWriter.write("1 ");
				}
				
				for(int j = 0; j < vNodes; j++)
					bufferedWriter.write("1 ");
				
				bufferedWriter.write("\n");
			}
			bufferedWriter.write(";\n\n");			
			// ---------- Param beta ----------
			bufferedWriter.write("param beta := \n");

			for(int i = 0; i < sNodes; i++)
				bufferedWriter.write(Utils.convertFromAlphabet(subNet.getNode(i))+" "+subNet.getNodeCPU(i)+"\n");
			
			for(int i = 0; i < vNodes; i++)
				bufferedWriter.write((Integer.parseInt(virNet.getNode(i))+sNodes)+" "+virNet.getNodeCPU(i)+"\n");

			bufferedWriter.write(";\n\n");
			// ---------- Param fs ----------
			bufferedWriter.write("param fs := \n");

			for(int i = 0; i < vEdges; i++)
				bufferedWriter.write("f"+i+" "+(Integer.parseInt(virNet.getEdge(i).getLeft())+sNodes)+"\n");

			bufferedWriter.write(";\n\n");
			// ---------- Param fe ----------
			bufferedWriter.write("param fe := \n");

			for(int i = 0; i < vEdges; i++)
				bufferedWriter.write("f"+i+" "+(Integer.parseInt(virNet.getEdge(i).getRight())+sNodes)+"\n");

			bufferedWriter.write(";\n\n");
			// ---------- Param fd ----------
			bufferedWriter.write("param fd := \n");

			for(int i = 0; i < vEdges; i++)
				bufferedWriter.write("f"+i+" "+virNet.getEdgeBw(i)+"\n");

			bufferedWriter.write(";\n\n");
			bufferedWriter.write("end;\n");
			bufferedWriter.flush();
			bufferedWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
