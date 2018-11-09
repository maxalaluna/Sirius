package net.floodlightcontroller.sirius.tests;

import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.sirius.providerconfig.EnvironmentOfServices3;
import net.floodlightcontroller.sirius.util.Utils;

public class Tests {


	public static void main(String[] args) {
		
		Map<String, String> workingVirtualNodeIdLetterServerIdToEmbeddingMap = new HashMap<String, String>();
		Map<String, String> workingLetterServerIdToEmbeddingvirtualNodeIdMap = new HashMap<String, String>();
		Map<String, String> backupVirtualNodeIdLetterServerIdToEmbeddingMap = new HashMap<String, String>();
		Map<String, String> backupLetterServerIdToEmbeddingvirtualNodeIdMap = new HashMap<String, String>();
		Map<String, String> workingLinkIdsPathToEmbeddingMap = new HashMap<String, String>();
		Map<String, String> backupLinkIdsPathToEmbeddingMap = new HashMap<String, String>();

		String result = "Working node of 11 -> F\n";
		result = result + "Working node of 12 -> D\n";
		result = result + "Backup node of 8 -> E\n";
		result = result + "Backup node of 9 -> C\n";
		result = result + "Working links for (9,8) -> (D,F) \n";
		result = result + "Working path for (9,8) -> D F \n"; 
		result = result + "Backup links for (9,8) -> (C,E) \n"; 
		result = result + "Backup path for (9,8) -> C E \n";
		
		System.out.println(result);
		System.out.println("####################");
		String[] tokens = result.split("\n");

		for (String t : tokens){
			String[] workingNodesMappingInfo = t.split("Working node of ");
			String[] backupNodesMappingInfo = t.split("Backup node of ");
			String[] workingLinksMappingInfo = t.split("Working links for ");
			String[] workingPathMappingInfo = t.split("Working path for ");
			String[] backupLinksMappingInfo = t.split("Backup links for ");
			String[] backupPathMappingInfo = t.split("Backup path for ");

			if(workingNodesMappingInfo.length ==2){				
				System.out.println(workingNodesMappingInfo[1]);
				System.out.println(workingNodesMappingInfo[1].split(" -> ")[0]);
				workingVirtualNodeIdLetterServerIdToEmbeddingMap.
				put(workingNodesMappingInfo[1].substring(0, 1), workingNodesMappingInfo[1].substring(workingNodesMappingInfo[1].length()-1, workingNodesMappingInfo[1].length()));
				workingLetterServerIdToEmbeddingvirtualNodeIdMap.
				put(workingNodesMappingInfo[1].substring(workingNodesMappingInfo[1].length()-1, workingNodesMappingInfo[1].length()), workingNodesMappingInfo[1].substring(0, 1));
				
				//Utils.insertLetters();
				int a1 = Utils.getLetterArrayList().indexOf(workingVirtualNodeIdLetterServerIdToEmbeddingMap.
						get(workingNodesMappingInfo[1].substring(0, 1)));
				System.out.println(a1);
			}
			if(backupNodesMappingInfo.length ==2){
				System.out.println(backupNodesMappingInfo[1]);
				backupVirtualNodeIdLetterServerIdToEmbeddingMap.
				put(backupNodesMappingInfo[1].substring(0, 1), backupNodesMappingInfo[1].substring(backupNodesMappingInfo[1].length()-1, backupNodesMappingInfo[1].length()));
				backupLetterServerIdToEmbeddingvirtualNodeIdMap.
				put(backupNodesMappingInfo[1].substring(backupNodesMappingInfo[1].length()-1, backupNodesMappingInfo[1].length()), backupNodesMappingInfo[1].substring(0, 1));				
			}
			if(workingLinksMappingInfo.length ==2){
				System.out.println(workingLinksMappingInfo[1]);
			}
			if(workingPathMappingInfo.length ==2){
				String[] path = workingPathMappingInfo[1].split(" -> ");
				System.out.println(workingPathMappingInfo[1]);
				workingLinkIdsPathToEmbeddingMap.put(workingPathMappingInfo[1].substring(0, 5), (path[1].substring(0, path[1].length()-1)).replace(" ", ":"));
				System.out.println(workingLinkIdsPathToEmbeddingMap.get(workingPathMappingInfo[1].substring(0, 5)));
			}
			if(backupLinksMappingInfo.length ==2){
				System.out.println(backupLinksMappingInfo[1]);
			}
			if(backupPathMappingInfo.length ==2){
				String[] path = backupPathMappingInfo[1].split(" -> ");
				System.out.println(backupPathMappingInfo[1]);
				backupLinkIdsPathToEmbeddingMap.put(backupPathMappingInfo[1].substring(0, 5), (path[1].substring(0, path[1].length()-1)).replace(" ", ":"));
			}
		}
		System.out.println("Finish");
	}
}
