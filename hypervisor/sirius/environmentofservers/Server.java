package net.floodlightcontroller.sirius.environmentofservers;

import java.util.ArrayList;
import java.util.UUID;

public class Server {

	protected UUID uuid;
	protected OpenVSwitch openVSwitch;
	protected String hostname;
	protected String username;
	protected String password;
	protected String description;

	public Server(UUID uuid, String hostname, String username, String password,
			String description) {
		super();
		this.uuid = uuid;
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.description = description;
	}


	public Server(UUID uuid, OpenVSwitch openVSwitch, String hostname,
			String username, String password, String description) {
		super();
		this.uuid = uuid;
		this.openVSwitch = openVSwitch;
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.description = description;
	}



	public UUID getUuid() {
		return uuid;
	}


	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}


	public OpenVSwitch getOpenVSwitch() {
		return openVSwitch;
	}


	public void setOpenVSwitch(OpenVSwitch openVSwitch) {
		this.openVSwitch = openVSwitch;
	}


	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public void populateInfoOvsVsctlShow(String ovsVsctlShow) {
		// TODO Auto-generated method stub

		ArrayList<Controller> controllerList =  new ArrayList<Controller>();
		ArrayList<Bridge> bridgeList = new ArrayList<Bridge>();
		ArrayList<Interfaces> interfacesList = null;
		Bridge bridge;
		//OpenVSwitch openVSwitch;
		String bridgeName = "";
		//boolean bridgeFounded = false;

		String[] lines = ovsVsctlShow.split("\\s*\\r?\\n\\s*");

		for (String line : lines) {

			String[] tokensOfLine = line.trim().split(" ");

			if (tokensOfLine[0].compareTo("Bridge")==0 ){
				
				if (bridgeList.size() != 0){
					bridgeList.get(bridgeList.size()-1).setArrayListInterfaces(interfacesList);
				}
				
				//controllerList = new ArrayList<Controller>();
				//bridgeList = new ArrayList<Bridge>();
				interfacesList = new ArrayList<Interfaces>();
				
				bridgeName = tokensOfLine[1].replace("\"", "");
				bridge = new Bridge(bridgeName);
				bridgeList.add(bridge);
				
//				if (!bridgeList.contains(bridge)){
//					bridgeList.add(bridge);
//				}
					
				
/*				
				if (!bridgeFounded){
					
				bridgeName = tokensOfLine[1].replace("\"", "");
				bridgeFounded = true;
				
				} else{
					bridge = new Bridge(bridgeName);
					bridge.setArrayListInterfaces(interfacesList);
					bridgeList.add(bridge);
					bridgeFounded = false;
					
					
				}
*/				
			}
			
			if (tokensOfLine[0].compareTo("Controller")==0 && tokensOfLine[1].contains(".")){
				
				//int h = tokensOfLine[1].indexOf(":")+1;
				controllerList.add(new Controller(tokensOfLine[1].substring(1, tokensOfLine[1].indexOf(":")), 
						tokensOfLine[1].substring(tokensOfLine[1].indexOf(":")+1,tokensOfLine[1].indexOf(":", tokensOfLine[1].indexOf(":")+1)),
						tokensOfLine[1].substring(tokensOfLine[1].indexOf(":", tokensOfLine[1].indexOf(":")+1)+1, tokensOfLine[1].lastIndexOf("\"")), false, null));

			}
			if (tokensOfLine[0].compareTo("Controller")==0 && !tokensOfLine[1].contains(".")){

				controllerList.add(new Controller(tokensOfLine[1].substring(1, tokensOfLine[1].indexOf(":")), "",
						tokensOfLine[1].substring(tokensOfLine[1].indexOf(":")+1, tokensOfLine[1].lastIndexOf("\"")), false, null));

			}
			if (tokensOfLine[0].compareTo("Port")==0){

				interfacesList.add(new Interfaces(tokensOfLine[1].substring(tokensOfLine[1].indexOf("\"")+1, tokensOfLine[1].lastIndexOf("\""))));

			}
			
			
/*
			for (String tokens : tokensOfLine) {

				if (tokens.compareTo("Bridge")==0){
					bridgeList.add(new Bridge(tokensOfLine[1].replace("\"", "")));
				}

				System.out.println(tokens);

				//POPULAR OS CONTROLLERS E BRIDGES

			}
*/
			
			//System.out.println(line);
		}
		
		if (bridgeList != null){
			bridgeList.get(bridgeList.size()-1).setArrayListInterfaces(interfacesList);
		}
		//System.out.println(lines[0]);
		//System.out.println((lines[lines.length-1].split(" "))[1].replace("\"", ""));
		this.setOpenVSwitch(new OpenVSwitch(UUID.fromString(lines[0]), controllerList, bridgeList, (lines[lines.length-1].split(" "))[1].replace("\"", "")));

		//FAZER O PARSER DO RESULTADO DO OVS-VSCTL SHOW E POPULAR AS INFO POSSIVEIS

	}


}
