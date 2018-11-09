package net.floodlightcontroller.sirius.providerconfig;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.projectfloodlight.openflow.types.DatapathId;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.sirius.util.Enum.SecurityLevelPhysicalNode;

public class Server3{
	
	protected UUID uuid;
	protected OpenVSwitch3 openVSwitch;
	protected String hostname;
	protected String username;
	protected String password;
	protected String description;
	protected IOFSwitch sw;
	protected int linkSpeed;
	protected String bridgeName;
	private Cloud cloud;
	private SecurityLevelPhysicalNode securityLevel;
	private int serverId;
	private static final AtomicInteger count = new AtomicInteger(0);
	int cpu;
	private int flowSize;
	private int MAX_FLOW_SIZE = 0;
	int cloudId;
	protected String letterServerIdToEmbedding;
	private int memory;
	protected String privateIP;

	public String getPrivateIp() {
		return privateIP;
	}

	public void setPrivateIp(String privateIp) {
		this.privateIP = privateIp;
	}

	public String getLetterServerIdToEmbedding() {
		return letterServerIdToEmbedding;
	}

	public void setLetterServerIdToEmbedding(String letterServerIdToEmbedding) {
		this.letterServerIdToEmbedding = letterServerIdToEmbedding;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public Server3(UUID uuid, String hostname, String username, String password,
			String description, int linkSpeed, String bridgeName, Cloud cloud,
			SecurityLevelPhysicalNode securityLevel, int cpu, int MAX_FLOW_SIZE) {
		super();
		this.uuid = uuid;
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.description = description;
		this.linkSpeed = linkSpeed;
		this.bridgeName = bridgeName;
		this.cloud = cloud;
		this.securityLevel = securityLevel;
		this.serverId = count.incrementAndGet();
		this.cpu = cpu;
		this.setMAX_FLOW_SIZE(MAX_FLOW_SIZE);
	}
	
	public Server3(UUID uuid, String hostname, String username, String password,
			String description, int linkSpeed, String bridgeName, int cloudId,
			SecurityLevelPhysicalNode securityLevel, int cpu, int MAX_FLOW_SIZE) {
		super();
		this.uuid = uuid;
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.description = description;
		this.linkSpeed = linkSpeed;
		this.bridgeName = bridgeName;
		this.cloudId = cloudId;
		this.securityLevel = securityLevel;
		this.serverId = count.incrementAndGet();
		this.cpu = cpu;
		this.setMAX_FLOW_SIZE(MAX_FLOW_SIZE);
	}

	public Server3(UUID uuid, OpenVSwitch3 openVSwitch, String hostname,
			String username, String password, String description, int linkSpeed,
			String bridgeName, Cloud cloud, SecurityLevelPhysicalNode securityLevel, int cpu, int MAX_FLOW_SIZE) {
		super();
		this.uuid = uuid;
		this.openVSwitch = openVSwitch;
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.description = description;
		this.linkSpeed = linkSpeed;
		this.bridgeName = bridgeName;
		this.cloud = cloud;
		this.securityLevel = securityLevel;
		this.serverId = count.incrementAndGet();
		this.cpu = cpu;
		this.setMAX_FLOW_SIZE(MAX_FLOW_SIZE);
	}

	public Server3(UUID uuid, int serverId, String letterServerIdToEmbedding, String hostname, String username, String password,
			String description, int linkSpeed, String bridgeName, int cloudId,
			SecurityLevelPhysicalNode securityLevel, int cpu, int MAX_FLOW_SIZE) {
		super();
		this.uuid = uuid;
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.description = description;
		this.linkSpeed = linkSpeed;
		this.bridgeName = bridgeName;
		this.cloudId = cloudId;
		this.securityLevel = securityLevel;
		this.serverId = serverId;
		this.cpu = cpu;
		this.setMAX_FLOW_SIZE(MAX_FLOW_SIZE);
		this.letterServerIdToEmbedding = letterServerIdToEmbedding;
		
	}
	
	public Server3(UUID uuid, int serverId, String letterServerIdToEmbedding, String hostname, String username, String password,
			String description, int linkSpeed, String bridgeName, int cloudId,
			SecurityLevelPhysicalNode securityLevel, int cpu, int MAX_FLOW_SIZE, String privateIP) {
		super();
		this.uuid = uuid;
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.description = description;
		this.linkSpeed = linkSpeed;
		this.bridgeName = bridgeName;
		this.cloudId = cloudId;
		this.securityLevel = securityLevel;
		this.serverId = serverId;
		this.cpu = cpu;
		this.setMAX_FLOW_SIZE(MAX_FLOW_SIZE);
		this.letterServerIdToEmbedding = letterServerIdToEmbedding;
		this.privateIP = privateIP;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public long getLinkSpeed() {
		return linkSpeed;
	}

	public void setLinkSpeed(int linkSpeed) {
		this.linkSpeed = linkSpeed;
	}

	public OpenVSwitch3 getOpenVSwitch() {
		return openVSwitch;
	}

	public void setOpenVSwitch(OpenVSwitch3 openVSwitch) {
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

	public IOFSwitch getSw() {
		return sw;
	}

	public void setSw(IOFSwitch sw) {
		this.sw = sw;
	}
	
	public String getBridgeName() {
		return bridgeName;
	}

	public void setBridgeName(String bridgeName) {
		this.bridgeName = bridgeName;
	}

	public void populateInfoOvsVsctlShow(String ovsVsctlShow, DatapathId dataPathId, IOFSwitch iofSwitch) {

		ArrayList<Controller3> controllerList =  new ArrayList<Controller3>();
		ArrayList<Bridge3> bridgeList = new ArrayList<Bridge3>();
		ArrayList<Interfaces3> interfacesList = null;
		Bridge3 bridge2;
		String bridgeName = "";

		String[] lines = ovsVsctlShow.split("\\s*\\r?\\n\\s*");

		for (String line : lines) {
			String[] tokensOfLine = line.trim().split(" ");
			if (tokensOfLine[0].compareTo("Bridge")==0 ){
				if (bridgeList.size() != 0){
					bridgeList.get(bridgeList.size()-1).setArrayListInterfaces(interfacesList);
				}
				interfacesList = new ArrayList<Interfaces3>();
				bridgeName = tokensOfLine[1].replace("\"", "");
				bridge2 = new Bridge3(bridgeName);
				bridgeList.add(bridge2);
			}
			if (tokensOfLine[0].compareTo("Controller")==0 && tokensOfLine[1].contains(".")){	
				controllerList.add(new Controller3(tokensOfLine[1].substring(1, tokensOfLine[1].indexOf(":")), 
						tokensOfLine[1].substring(tokensOfLine[1].indexOf(":")+1,tokensOfLine[1].indexOf(":", tokensOfLine[1].indexOf(":")+1)),
						tokensOfLine[1].substring(tokensOfLine[1].indexOf(":", tokensOfLine[1].indexOf(":")+1)+1, tokensOfLine[1].lastIndexOf("\"")), false, null));
			}
			if (tokensOfLine[0].compareTo("Controller")==0 && !tokensOfLine[1].contains(".")){

				controllerList.add(new Controller3(tokensOfLine[1].substring(1, tokensOfLine[1].indexOf(":")), "",
						tokensOfLine[1].substring(tokensOfLine[1].indexOf(":")+1, tokensOfLine[1].lastIndexOf("\"")), false, null));
			}
			if (tokensOfLine[0].compareTo("Port")==0){
				interfacesList.add(new Interfaces3(tokensOfLine[1].substring(tokensOfLine[1].indexOf("\"")+1, tokensOfLine[1].lastIndexOf("\""))));
			}
		}
		if (bridgeList != null){
			bridgeList.get(bridgeList.size()-1).setArrayListInterfaces(interfacesList);
		}
		this.setOpenVSwitch(new OpenVSwitch3(UUID.fromString(lines[0]), controllerList, bridgeList, (lines[lines.length-2].split(" "))[1].replace("\"", ""), dataPathId));
		if (iofSwitch != null){
			this.setSw(iofSwitch);
		}
	}
	  @Override
	  public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((this.hostname == null) ? 0 : this.hostname.hashCode());
	    return result ^ (this.getOpenVSwitch()==null?1:this.getOpenVSwitch().getDatapathId().hashCode()) ^ this.serverId;
	  }

	  @Override
	  public boolean equals(Object o) {
		  
		  return (o instanceof Server3) && 
					(hostname.equals(((Server3) o).hostname)) && 
					(serverId ==((Server3) o).serverId) && 
					(bridgeName.equals(((Server3) o).bridgeName)) &&
					(openVSwitch.getDatapathId().equals(((Server3) o).openVSwitch.getDatapathId()));
	  }

	public Cloud getCloud() {
		return cloud;
	}

	public void setCloud(Cloud cloud) {
		this.cloud = cloud;
	}

	public SecurityLevelPhysicalNode getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(SecurityLevelPhysicalNode securityLevel) {
		this.securityLevel = securityLevel;
	}

	public int getFlowSize() {
		return flowSize;
	}

	public void setFlowSize(int flowSize) {
		this.flowSize = flowSize;
	}

	public int getMAX_FLOW_SIZE() {
		return MAX_FLOW_SIZE;
	}

	public void setMAX_FLOW_SIZE(int mAX_FLOW_SIZE) {
		MAX_FLOW_SIZE = mAX_FLOW_SIZE;
	}

	public int getCloudId() {
		return cloudId;
	}

	public void setCloudId(int cloudId) {
		this.cloudId = cloudId;
	}
}
