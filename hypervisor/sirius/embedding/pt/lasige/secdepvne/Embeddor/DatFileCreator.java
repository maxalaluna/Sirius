package net.floodlightcontroller.sirius.embedding.pt.lasige.secdepvne.Embeddor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class responsible for creating the input for the SecDep model
 * @authors Luis Ferrolho, fc41914, Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class DatFileCreator {

	private ConfigsReader cr;

	/**
	 * 
	 * @param cr Configurations reader. Important here since it holds the information read from the configurations file passed by the virtualizer. 
	 */
	public DatFileCreator(ConfigsReader cr) {
		this.cr = cr;
	}

	/**
	 * Creates the input file for GLPK where the formatted info will be written 
	 * @param datFile Name of the file where the data will be written
	 * @throws IOException When error trying to write to the file
	 */
	public void createDatFile(String datFile) throws IOException {

		FileWriter fileWriter = new FileWriter(datFile);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		writeSubstrateInfo(bufferedWriter);
		writeVirtualInfo(bufferedWriter);

		bufferedWriter.close();

	}

	// Constructs the SN part in the file, namely the set of sNodes, sNodesCPU, sNodesSec, sNodesLoc,
	// sLinksBw, sLinksSec, sLinksWeight, clouds, cloudsSec
	private void writeSubstrateInfo(BufferedWriter bufferedWriter) throws IOException {

		int sNodes = cr.getnSNodes();
		int sEdges = cr.getnSLinks();
		int nClouds = cr.getnClouds();

		bufferedWriter.write("data;\n\n"); // Initialize

		// General info
		bufferedWriter.write("# SNodes: "+sNodes+", SEdges: "+sEdges+"\n\n");

		// ---------- Set SNodes ----------
		bufferedWriter.write("set SNodes := ");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(cr.getsNodeID(i)+" ");

		bufferedWriter.write(";\n\n");

		// ---------- Param cpuSup ----------
		bufferedWriter.write("param cpuSup := \n");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(cr.getsNodeID(i)+" "+cr.getsNodeCPU(i)+"\n");

		bufferedWriter.write(";\n\n");

		// ---------- Param secSup ----------
		bufferedWriter.write("param secSupNode := \n");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(cr.getsNodeID(i)+" "+cr.getsNodeSec(i)+"\n");

		bufferedWriter.write(";\n\n");

		// ---------- Param bwSup ----------
		bufferedWriter.write("param bwSup:\n  ");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(cr.getsNodeID(i)+" ");

		bufferedWriter.write(":=\n");

		// Create the matrix with all the bw between two end-points (sNodes)
		for(int i = 0; i < sNodes; i ++){
			bufferedWriter.write(cr.getsNodeID(i)+" ");

			for(int j = 0; j < sNodes; j++){
				if(cr.getsLinks().contains("("+cr.getsNodeID(i)+","+cr.getsNodeID(j)+")"))
					bufferedWriter.write(cr.getsLinkBw(cr.getsLinks().indexOf("("+cr.getsNodeID(i)+","+cr.getsNodeID(j)+")"))+" ");
				else if(cr.getsLinks().contains("("+cr.getsNodeID(j)+","+cr.getsNodeID(i)+")"))
					bufferedWriter.write(cr.getsLinkBw(cr.getsLinks().indexOf("("+cr.getsNodeID(j)+","+cr.getsNodeID(i)+")"))+" ");
				else
					bufferedWriter.write("0 ");
			}
			bufferedWriter.write("\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Param secSupEdge ----------
		bufferedWriter.write("param secSupEdge :\n  ");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(cr.getsNodeID(i)+" ");

		bufferedWriter.write(":=\n");

		// Create the matrix with all the sec between two end-points (sNodes)
		for(int i = 0; i < sNodes; i ++){
			bufferedWriter.write(cr.getsNodeID(i)+" ");

			for(int j = 0; j < sNodes; j++){
				if(cr.getsLinks().contains("("+cr.getsNodeID(i)+","+cr.getsNodeID(j)+")"))
					bufferedWriter.write(cr.getsLinkSec(cr.getsLinks().indexOf("("+cr.getsNodeID(i)+","+cr.getsNodeID(j)+")"))+" ");
				else if(cr.getsLinks().contains("("+cr.getsNodeID(j)+","+cr.getsNodeID(i)+")"))
					bufferedWriter.write(cr.getsLinkSec(cr.getsLinks().indexOf("("+cr.getsNodeID(j)+","+cr.getsNodeID(i)+")"))+" ");
				else
					bufferedWriter.write("0 ");
			}
			bufferedWriter.write("\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Param weight ----------
		bufferedWriter.write("param weight :\n  ");

		for(int i = 0; i < sNodes; i++)
			bufferedWriter.write(cr.getsNodeID(i)+" ");

		bufferedWriter.write(":=\n");

		// Create the matrix with all the weight between two end-points (sNodes)
		for(int i = 0; i < sNodes; i ++){
			bufferedWriter.write(cr.getsNodeID(i)+" ");

			for(int j = 0; j < sNodes; j++){
				if(cr.getsLinks().contains("("+cr.getsNodeID(i)+","+cr.getsNodeID(j)+")"))
					bufferedWriter.write(cr.getsLinkWeight(cr.getsLinks().indexOf("("+cr.getsNodeID(i)+","+cr.getsNodeID(j)+")"))+" ");
				else if(cr.getsLinks().contains("("+cr.getsNodeID(j)+","+cr.getsNodeID(i)+")"))
					bufferedWriter.write(cr.getsLinkWeight(cr.getsLinks().indexOf("("+cr.getsNodeID(j)+","+cr.getsNodeID(i)+")"))+" ");
				else
					bufferedWriter.write("0 ");
			}
			bufferedWriter.write("\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Set Clouds ----------
		bufferedWriter.write("set Clouds := ");

		for(int i = 0; i < nClouds; i++)
			bufferedWriter.write(cr.getCloudID(i)+" ");

		bufferedWriter.write(";\n\n");

		// ---------- Param doesItBelong (== sNodesLoc) ----------
		bufferedWriter.write("param doesItBelong := \n");

		for(int i = 0; i < nClouds; i ++){
			for(int j = 0; j < sNodes; j++){
				bufferedWriter.write(cr.getCloudID(i)+" "+cr.getsNodeID(j)+" "+cr.isInCloud(cr.getsNodeID(j),cr.getCloudID(i))+"\n");
			}
		}

		bufferedWriter.write(";\n\n");

		// ---------- Param cloudSecSup ----------
		bufferedWriter.write("param cloudSecSup := \n");

		for(int i = 0; i < nClouds; i ++){
			for(int j = 0; j < sNodes; j++){
				if(cr.isInCloud(cr.getsNodeID(j),cr.getCloudID(i)) == 1)
					bufferedWriter.write(cr.getsNodeID(j)+" "+cr.getCloudSec(i)+"\n");
			}
		}

		bufferedWriter.write(";\n\n");
	}

	// Constructs the SN part in the file, namely the set of vNodes, vNodesCPUDem, vNodesSecDem, vNodesLocDem,
	// vLinksBwDem, vLinksSecDem, cloudSecDem, and the location of the backup nodes (if they should be in the same cloud
	// as the working node or not)
	private void writeVirtualInfo(BufferedWriter bufferedWriter) throws IOException {

		int vNodes = cr.getnVNodes();
		int vEdges = cr.getnVLinks();

		String[] parts;

		// General Info
		bufferedWriter.write("# VNodes: "+vNodes+", VEdges: "+vEdges+"\n\n");

		// ---------- Set VNodes ----------
		bufferedWriter.write("set VNodes := ");

		for(int i = 0; i < vNodes; i++)
			bufferedWriter.write(cr.getvNodeID(i)+" ");

		bufferedWriter.write(";\n\n");

		// ---------- Set VEdges ----------
		bufferedWriter.write("set VEdges := ");

		for(int i = 0; i < vEdges; i++)
			bufferedWriter.write(cr.getvLink(i));

		bufferedWriter.write(";\n\n");

		// ---------- Param cpuDem ----------
		bufferedWriter.write("param cpuDem := \n");

		for(int i = 0; i < vNodes; i++)
			bufferedWriter.write(cr.getvNodeID(i)+" "+cr.getvNodeCPUDem(i)+"\n");

		bufferedWriter.write(";\n\n");

		// ---------- Param secDemNode ----------
		bufferedWriter.write("param secDemNode := \n");

		for(int i = 0; i < vNodes; i++)
			bufferedWriter.write(cr.getvNodeID(i)+" "+cr.getvNodeSecDem(i)+"\n");

		bufferedWriter.write(";\n\n");

		// ---------- Param bwDem ----------
		bufferedWriter.write("param bwDem := \n");

		for(int i = 0; i < vEdges; i++){
			parts = cr.getvLink(i).split("\\(|\\)|,");
			bufferedWriter.write(parts[1]+" "+parts[2]+" "+cr.getvLinkBwDem(i)+"\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Param secDemEdge ----------
		bufferedWriter.write("param secDemEdge := \n");

		for(int i = 0; i < vEdges; i++){
			parts = cr.getvLink(i).split("\\(|\\)|,");
			bufferedWriter.write(parts[1]+" "+parts[2]+" "+cr.getvLinkSecDem(i)+"\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Param cloudSecDem ----------
		bufferedWriter.write("param cloudSecDem := \n");

		for(int i = 0; i < vNodes; i++){
			bufferedWriter.write(cr.getvNodeID(i)+" "+cr.getvNodeLocDem(i)+"\n");
		}

		bufferedWriter.write(";\n\n");

		// ---------- Param wantBackup ----------
		bufferedWriter.write("param wantBackup := "+cr.getWantBackup());

		bufferedWriter.write(";\n\n");

		// ---------- Param bvNodeLocalization ----------
		bufferedWriter.write("param bvNodeLocalization := \n");
		
		for(int i = 0; i < vNodes; i++){
						
			if(cr.getWantBackup()== 1)
				bufferedWriter.write(cr.getvNodeID(i)+" "+cr.getvNodeBackupLocDem(i)+"\n");
			else
				bufferedWriter.write(cr.getvNodeID(i)+" 0\n");
		}

		bufferedWriter.write(";\n\n");

		bufferedWriter.write("end;\n");

	}

}
