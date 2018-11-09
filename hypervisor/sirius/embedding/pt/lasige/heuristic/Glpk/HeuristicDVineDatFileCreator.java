package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Glpk;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Utils;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.SubstrateNetworkHeu;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;

/**
 * Handles the creation of a structured file to be the input of the HeuristicSecDep formulation
 * @author Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class HeuristicDVineDatFileCreator{

	/**
	 * Constructs the input file for SecDep
	 * @param file Filepath
	 * @param subNet Substrate network
	 * @param virNet Virtual network
	 * @param mappedNodes 
	 * @throws IOException 
	 */
	public void createDatFile(String file, SubstrateNetworkHeu subNet, VirtualNetworkHeu virNet, ArrayList<Pair<String>> mappedNodes){

		try {

			FileWriter fileWriterH = new FileWriter(file);
			BufferedWriter bufferedWriterHeu = new BufferedWriter(fileWriterH);

			int sNodes = subNet.getNumOfNodes();
			int sEdges = subNet.getNumOfEdges();
			int vEdges = virNet.getNumOfEdges();
			
			bufferedWriterHeu.write("data;\n\n"); // Initialize
			bufferedWriterHeu.write("# Nodes: "+sNodes+", Edges: "+sEdges+"\n\n");

			// ---------- Set of Substrate Nodes ----------
			bufferedWriterHeu.write("set N := ");

			for(int i = 0; i < sNodes; i++)
				bufferedWriterHeu.write(i+" ");
			
			bufferedWriterHeu.write(";\n\n");
			// ---------- Set of Virtual Edges ----------
			bufferedWriterHeu.write("set F := ");

			for(int i = 0; i < vEdges; i++)
				bufferedWriterHeu.write("f"+i+" ");

			bufferedWriterHeu.write(";\n\n");
			// ---------- Param b ----------
			bufferedWriterHeu.write("param b :\n  ");

			for(int i = 0; i < sNodes; i++)
				bufferedWriterHeu.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");

			bufferedWriterHeu.write(":=\n");
			Thread.sleep(100);
			
			for(int i = 0; i < sNodes; i ++){

				bufferedWriterHeu.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");
				Thread.sleep(1);
				for(int j = 0; j < sNodes; j++){
					if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))
						bufferedWriterHeu.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
					else if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
						bufferedWriterHeu.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
					else
						bufferedWriterHeu.write("0 ");
				}
				bufferedWriterHeu.write("\n"); Thread.sleep(1);
			}

			bufferedWriterHeu.write(";\n\n");

			// ---------- Param alpha ----------

			bufferedWriterHeu.write("param alpha :\n  ");

			for(int i = 0; i < sNodes; i++)
				bufferedWriterHeu.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");

			bufferedWriterHeu.write(":=\n");

			for(int i = 0; i < sNodes; i ++){

				bufferedWriterHeu.write(Utils.convertFromAlphabet(subNet.getNode(i))+" ");

				for(int j = 0; j < sNodes; j++){
					if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))
						bufferedWriterHeu.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(i),subNet.getNode(j))))+" ");
					else if(subNet.getEdges().contains(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))
						bufferedWriterHeu.write(subNet.getEdgeBw(subNet.getEdges().indexOf(new Pair<String>(subNet.getNode(j),subNet.getNode(i))))+" ");
					else
						bufferedWriterHeu.write("0 ");
				}
				bufferedWriterHeu.write("\n");
			}

			bufferedWriterHeu.write(";\n\n");
			
			ArrayList<String> fs = new ArrayList<String>();
			ArrayList<String> fe = new ArrayList<String>();

			// ---------- Set Substrate Source Link ----------
			fs = Utils.populateDVineFS(mappedNodes, virNet, subNet);
			bufferedWriterHeu.write("param fs :=\n");

			for(int i = 0; i < vEdges; i++){

				bufferedWriterHeu.write("f"+i+"      "+fs.get(i)+"\n");
			}

			bufferedWriterHeu.write(";\n\n");
			// ---------- Set Substrate Destination Link ----------
			fe = Utils.populateDVineFE(mappedNodes, virNet, subNet);
			bufferedWriterHeu.write("param fe :=\n");

			for(int i = 0; i < vEdges; i++){

				bufferedWriterHeu.write("f"+i+"      "+fe.get(i)+"\n");
			}

			bufferedWriterHeu.write(";\n\n");
			// ---------- Set Virtual bandwidth ----------
			bufferedWriterHeu.write("param fd :=\n");

			for(int i = 0; i < vEdges; i++){

				bufferedWriterHeu.write("f"+i+"      "+virNet.getEdgeBw(i)+"\n");
			}

			bufferedWriterHeu.write(";\n\n");
			bufferedWriterHeu.write("end;\n");
			bufferedWriterHeu.flush();
			bufferedWriterHeu.close(); 

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}