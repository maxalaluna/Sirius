package net.floodlightcontroller.sirius.tenantconfig.topology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.floodlightcontroller.sirius.console.Client;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;
import net.floodlightcontroller.sirius.providerconfig.EnvironmentOfServices3;
import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.providerconfig.topology.EdgeSC;
import net.floodlightcontroller.sirius.tenantconfig.EnvironmentOfTenants3;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.tenantconfig.HostLocation;
import net.floodlightcontroller.sirius.tenantconfig.Reminder;
import net.floodlightcontroller.sirius.util.Utils;
import net.floodlightcontroller.sirius.util.Enum.SecurityLevelLinks;

public class VirtualNetwork {

	private ArrayList<Host> hostList = new ArrayList<Host>();
	private ArrayList<VirtualEdgeSC> virtualEdgeSCArrayList;
	private ArrayList<VirtualSwitch> virtualSwitchArrayList;
	private VirtualGraphSC virtualGraph;
	private DijkstraAlgorithmVirtualNetwork dijkstraVirtualNetwork;
	protected static Logger log = LoggerFactory.getLogger(VirtualNetwork.class);
	private long tenantId = 0L;
	private int virtualNetworkId = 0;
	private static final AtomicInteger count = new AtomicInteger(0);
	private boolean active = false;
	private int lifeTime;
	@SuppressWarnings("unused")
	private Reminder finishLifeTime;
	private String virtualPartFileEmbedding;
	private String resultEmbedding = "";
	private boolean needBackup = false;
	private boolean insertedInitialFlows = false;
	private VirtualNetworkHeu virtualNetworkHeu;
	Map<IPv4Address, Integer> ipAddressHostArrayId = new HashMap<IPv4Address, Integer>();
	Map<MacAddress, Integer> macAddressHostArrayId = new HashMap<MacAddress, Integer>();
	Map<DatapathId, Integer> physicalDatapathIdSwitchidArrayListVirtualSwitchMap = new HashMap<DatapathId, Integer>();
	Map<String, Integer> virtualEdgeSCIdidArrayListVirtualEdgeMap = new HashMap<String, Integer>();
	Map<String, String> workingVirtualNodeIdLetterServerIdToEmbeddingMap = new HashMap<String, String>();
	Map<String, String> workingLetterServerIdToEmbeddingvirtualNodeIdMap = new HashMap<String, String>();
	Map<String, String> backupVirtualNodeIdLetterServerIdToEmbeddingMap = new HashMap<String, String>();
	Map<String, String> backupLetterServerIdToEmbeddingvirtualNodeIdMap = new HashMap<String, String>();
	Map<String, String> workingLinkIdsPathToEmbeddingMap = new HashMap<String, String>();
	Map<String, String> backupLinkIdsPathToEmbeddingMap = new HashMap<String, String>();

	public VirtualNetwork(ArrayList<Host> hostList, ArrayList<VirtualEdgeSC> virtualEdgeSC,
			ArrayList<VirtualSwitch> virtualSwitch, boolean active, int lifeTime, long tenantId) {
		super();
		this.hostList = hostList;
		this.virtualEdgeSCArrayList = virtualEdgeSC;
		this.virtualSwitchArrayList = virtualSwitch;
		//		this.virtualNetworkId = count.incrementAndGet();
		this.virtualNetworkId = (int) tenantId;
		this.active = active;
		this.lifeTime = lifeTime;
		finishLifeTime = new Reminder(this, lifeTime);
		this.tenantId = tenantId;
	}

	public VirtualNetwork(ArrayList<Host> hostList,
			ArrayList<VirtualEdgeSC> virtualEdgeSCArrayList, ArrayList<VirtualSwitch> virtualSwitchArrayList,
			long tenantId, boolean active, int lifeTime) {
		super();
		this.hostList = hostList;
		this.virtualEdgeSCArrayList = virtualEdgeSCArrayList;
		this.virtualSwitchArrayList = virtualSwitchArrayList;
		this.tenantId = tenantId;
		this.virtualNetworkId = count.incrementAndGet();
		this.active = active;
		this.lifeTime = lifeTime;
		finishLifeTime = new Reminder(this, lifeTime);
	}

	public VirtualNetwork( ArrayList<VirtualEdgeSC> virtualEdgeSCArrayList, ArrayList<VirtualSwitch> virtualSwitchArrayList,
			long tenantId, boolean active, int lifeTime) {
		super();
		this.virtualEdgeSCArrayList = virtualEdgeSCArrayList;
		this.virtualSwitchArrayList = virtualSwitchArrayList;
		this.tenantId = tenantId;
		this.virtualNetworkId = count.incrementAndGet();
		this.active = active;
		this.lifeTime = lifeTime;
		finishLifeTime = new Reminder(this, lifeTime);
	}

	public VirtualNetwork( int virtualNetworkId, ArrayList<VirtualEdgeSC> virtualEdgeSCArrayList, ArrayList<VirtualSwitch> virtualSwitchArrayList,
			long tenantId, int lifeTime) {
		super();
		this.virtualEdgeSCArrayList = virtualEdgeSCArrayList;
		this.virtualSwitchArrayList = virtualSwitchArrayList;
		this.tenantId = tenantId;
		this.virtualNetworkId = virtualNetworkId;
		this.lifeTime = lifeTime;
		finishLifeTime = new Reminder(this, lifeTime);
	}

	public VirtualNetwork( ) {
		super();
	}

	public Map<IPv4Address, Integer> getIpAddressHostArrayId() {
		return ipAddressHostArrayId;
	}

	public void setIpAddressHostArrayId(
			Map<IPv4Address, Integer> ipAddressHostArrayId) {
		this.ipAddressHostArrayId = ipAddressHostArrayId;
	}

	public Map<MacAddress, Integer> getMacAddressHostArrayId() {
		return macAddressHostArrayId;
	}

	public void setMacAddressHostArrayId(
			Map<MacAddress, Integer> macAddressHostArrayId) {
		this.macAddressHostArrayId = macAddressHostArrayId;
	}

	public Map<DatapathId, Integer> getPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap() {
		return physicalDatapathIdSwitchidArrayListVirtualSwitchMap;
	}


	public void setPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap(
			Map<DatapathId, Integer> physicalDatapathIdSwitchidArrayListVirtualSwitchMap) {
		this.physicalDatapathIdSwitchidArrayListVirtualSwitchMap = physicalDatapathIdSwitchidArrayListVirtualSwitchMap;
	}


	public Map<String, Integer> getVirtualEdgeSCIdidArrayListVirtualEdgeMap() {
		return virtualEdgeSCIdidArrayListVirtualEdgeMap;
	}

	public void setVirtualEdgeSCIdidArrayListVirtualEdgeMap(
			Map<String, Integer> virtualEdgeSCIdidArrayListVirtualEdgeMap) {
		this.virtualEdgeSCIdidArrayListVirtualEdgeMap = virtualEdgeSCIdidArrayListVirtualEdgeMap;
	}

	public void setVirtualNetworkId(int virtualNetworkId) {
		this.virtualNetworkId = virtualNetworkId;
	}

	public ArrayList<Host> getHostList() {
		return hostList;
	}

	public void setHostList(ArrayList<Host> hostList) {
		this.hostList = hostList;
	}

	public ArrayList<VirtualEdgeSC> getVirtualEdgeSCArrayList() {
		return virtualEdgeSCArrayList;
	}

	public void setVirtualEdgeSCArrayList(ArrayList<VirtualEdgeSC> virtualEdgeSC) {
		this.virtualEdgeSCArrayList = virtualEdgeSC;
	}

	public ArrayList<VirtualSwitch> getVirtualSwitchArrayList() {
		return virtualSwitchArrayList;
	}

	public void setVirtualSwitchArrayList(ArrayList<VirtualSwitch> virtualSwitch) {
		this.virtualSwitchArrayList = virtualSwitch;
	}

	public long getTenantId() {
		return tenantId;
	}

	public void setTenantId(long tenantId) {
		this.tenantId = tenantId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return  (this.hostList.hashCode() ^ this.virtualEdgeSCArrayList.hashCode() ^
				this.virtualSwitchArrayList.hashCode() ^	(int)this.tenantId ^ this.virtualNetworkId);
	}

	public int getVirtualNetworkId() {
		return virtualNetworkId;
	}

	@Override
	public boolean equals(Object o) {

		return (o instanceof VirtualNetwork) && 
				(hostList==null?true:(hostList.equals(((VirtualNetwork) o).hostList))) &&
				(virtualEdgeSCArrayList==null?true:(virtualEdgeSCArrayList.equals(((VirtualNetwork) o).virtualEdgeSCArrayList))) &&
				(virtualSwitchArrayList==null?true:(virtualSwitchArrayList.equals(((VirtualNetwork) o).virtualSwitchArrayList))) &&
				(tenantId==0L?true:(tenantId ==((VirtualNetwork) o).tenantId)) &&
				(virtualNetworkId==0L?true:(virtualNetworkId ==((VirtualNetwork) o).virtualNetworkId));
	}

	public int getLifeTime() {
		return lifeTime;
	}

	public void setLifeTime(int lifeTime) {
		this.lifeTime = lifeTime;
	}

	public String getVirtualPartFileEmbedding() {
		return virtualPartFileEmbedding;
	}

	public void setVirtualPartFileEmbedding(String virtualPartFileEmbedding) {
		this.virtualPartFileEmbedding = virtualPartFileEmbedding;
	}

	public String getResultEmbedding() {
		return resultEmbedding;
	}

	public void setResultEmbedding(String resultEmbedding) {
		this.resultEmbedding = resultEmbedding;
	}

	public boolean populateInfoEmbedding(EnvironmentOfServices3 environmentOfServers, String typeOfHeuristic){

		if (!this.resultEmbedding.equals("")){
			populateMapsInfoEmbedding();
			for(int i = 0; i < this.virtualSwitchArrayList.size(); i++){
				int workingSwitchId = ((this.getVirtualSwitchArrayList().get(i)).getVirtualSwitchId());
				String workingLetterOfSwitch = workingVirtualNodeIdLetterServerIdToEmbeddingMap.get(workingSwitchId+"");
				int workingIndexOfSwitch = -1;
				workingIndexOfSwitch = Utils.convertFromAlphabet(workingLetterOfSwitch);
				this.physicalDatapathIdSwitchidArrayListVirtualSwitchMap.put(environmentOfServers.getPhysicalNetwork().getVertexes().get(workingIndexOfSwitch).getOpenVSwitch().getDatapathId(), i);

				if(needBackup){
					int backupSwitchId = ((this.getVirtualSwitchArrayList().get(i)).getVirtualSwitchId());
					String backupLetterOfSwitch = backupVirtualNodeIdLetterServerIdToEmbeddingMap.get(backupSwitchId+"");
					int backupIndexOfSwitch = Utils.convertFromAlphabet(backupLetterOfSwitch);
					(this.getVirtualSwitchArrayList()).get(i).setBackupSwitch(environmentOfServers.getPhysicalNetwork().getVertexes().get(backupIndexOfSwitch));
				}
			}

			for(int i = 0; i < this.virtualEdgeSCArrayList.size(); i++){
				if(this.virtualEdgeSCArrayList.get(i).isBetweenSwitches()){
					ArrayList<EdgeSC> linkedListEdgeSCWork = new ArrayList<EdgeSC>();
					ArrayList<EdgeSC> linkedListEdgeSCBkp = new ArrayList<EdgeSC>();
					int source = (this.getVirtualEdgeSCArrayList()).get(i).getSourceSwitch().getVirtualSwitchId();
					int destination = (this.getVirtualEdgeSCArrayList()).get(i).getDestinationSwitch().getVirtualSwitchId();
					String virtualLinkString = "("+source+","+destination+")";
					String workingPathString = workingLinkIdsPathToEmbeddingMap.get(virtualLinkString);
					String backupPathString = backupLinkIdsPathToEmbeddingMap.get(virtualLinkString);
					String[] workingNodes = null;
					if(typeOfHeuristic.equals("Sirius"))
					workingNodes =  workingPathString.split(":");
					String[] backupNodes = null;
					if(needBackup){
						backupNodes =  backupPathString.split(":");
					}
					if(typeOfHeuristic.equals("Sirius")){
						for (int j = 0; j < workingNodes.length -1; j++){
							ArrayList<EdgeSC> edgeSC = environmentOfServers.getEdgeSCApps();
							for (int k = 0; k < edgeSC.size(); k++){
								if(edgeSC.get(k).getSource() != null && edgeSC.get(k).getDestination() != null){
									if (((workingNodes[j].equals(edgeSC.get(k).getSource().getLetterServerIdToEmbedding()))&&
											(workingNodes[j+1].equals(edgeSC.get(k).getDestination().getLetterServerIdToEmbedding())))||
											((workingNodes[j+1].equals(edgeSC.get(k).getSource().getLetterServerIdToEmbedding()))&&
													(workingNodes[j].equals(edgeSC.get(k).getDestination().getLetterServerIdToEmbedding())))){
										//Found edge
										linkedListEdgeSCWork.add(edgeSC.get(k));
										break;
									}
								}
							}
						}
					}
					if(needBackup){
						for (int j = 0; j < backupNodes.length -1; j++){
							ArrayList<EdgeSC> edgeSC = environmentOfServers.getEdgeSCApps();
							for (int k = 0; k < edgeSC.size(); k++){
								if(edgeSC.get(k).getSource() != null && edgeSC.get(k).getDestination() != null){
									if (((backupNodes[j].equals(edgeSC.get(k).getSource().getLetterServerIdToEmbedding()))&&
											(backupNodes[j+1].equals(edgeSC.get(k).getDestination().getLetterServerIdToEmbedding())))||
											((backupNodes[j+1].equals(edgeSC.get(k).getSource().getLetterServerIdToEmbedding()))&&
													(backupNodes[j].equals(edgeSC.get(k).getDestination().getLetterServerIdToEmbedding())))){
										//Found edge
										linkedListEdgeSCBkp.add(edgeSC.get(k));
										break;
									}	
								}
							}
						}
					}
					(this.getVirtualEdgeSCArrayList()).get(i).setLinkedListEdgeSCWork(linkedListEdgeSCWork);
					String link = "("+(this.getVirtualEdgeSCArrayList()).get(i).getSourceSwitch().getVirtualSwitchId()+","+(this.getVirtualEdgeSCArrayList()).get(i).getDestinationSwitch().getVirtualSwitchId()+")";
					this.virtualEdgeSCIdidArrayListVirtualEdgeMap.put(link, i);
					(this.getVirtualEdgeSCArrayList()).get(i).setLinkedListEdgeSCBkp(linkedListEdgeSCBkp);
					String path = null;
					if(typeOfHeuristic.equals("Sirius")){
						path = this.changeLetterPathToServerIdPath(workingPathString, environmentOfServers);
						(this.getVirtualEdgeSCArrayList()).get(i).setWorkingPathLinks(this.getLinksId(path, environmentOfServers));
						(this.getVirtualEdgeSCArrayList()).get(i).setWorkingPath(path);
					}
					if(needBackup){
						(this.getVirtualEdgeSCArrayList()).get(i).setBackupPath(this.changeLetterPathToServerIdPath(backupPathString, environmentOfServers));
					}
				}
			}
			return true;
		}
		return false;
	}

	public String changeLetterPathToServerIdPath(String path, EnvironmentOfServices3 environmentOfServers){

		String serversIdPath = "";

		String[] switches = path.split(":");

		for (int i = 0; i < switches.length; i++){
			if(i != switches.length - 1){
				serversIdPath = serversIdPath.concat(environmentOfServers.getLetterServerIdToEmbeddingServerId().get(switches[i])+":");
			} else {
				serversIdPath = serversIdPath.concat(environmentOfServers.getLetterServerIdToEmbeddingServerId().get(switches[i])+"");
			}
		}
		return serversIdPath;
	}

	@SuppressWarnings("unused")
	public void populateMapsInfoEmbedding(){

		String[] tokens = this.resultEmbedding.split("\n");

		for (String t : tokens){
			String[] workingNodesMappingInfo = t.split("Working node of ");
			String[] backupNodesMappingInfo = t.split("Backup node of ");
			String[] workingLinksMappingInfo = t.split("Working links for ");
			String[] workingPathMappingInfo = t.split("Working path for ");
			String[] backupLinksMappingInfo = t.split("Backup links for ");
			String[] backupPathMappingInfo = t.split("Backup path for ");
			if(workingNodesMappingInfo.length ==2){
				workingVirtualNodeIdLetterServerIdToEmbeddingMap.
				put(workingNodesMappingInfo[1].split(" -> ")[0], workingNodesMappingInfo[1].split(" -> ")[1]);
				workingLetterServerIdToEmbeddingvirtualNodeIdMap.
				put(workingNodesMappingInfo[1].split(" -> ")[1], workingNodesMappingInfo[1].split(" -> ")[0]);
			}
			if(backupNodesMappingInfo.length ==2){
				backupVirtualNodeIdLetterServerIdToEmbeddingMap.
				put(backupNodesMappingInfo[1].split(" -> ")[0], backupNodesMappingInfo[1].split(" -> ")[1]);
				backupLetterServerIdToEmbeddingvirtualNodeIdMap.
				put(backupNodesMappingInfo[1].split(" -> ")[1], backupNodesMappingInfo[1].split(" -> ")[0]);				
			}
			if(workingPathMappingInfo.length ==2){
				String[] path = workingPathMappingInfo[1].split(" -> "); 
				System.out.println("workingPathMappingInfo.length ==2|"+workingPathMappingInfo[1]);
				System.out.println("path[0]|"+path[0]);
				System.out.println("(path[1].replace|"+(path[1]).substring(0, path[1].length()-1).replace(" ", ":"));
				workingLinkIdsPathToEmbeddingMap.put(path[0], (path[1]).substring(0, path[1].length()-1).replace(" ", ":"));
			}
			if(backupPathMappingInfo.length ==2){
				String[] path = backupPathMappingInfo[1].split(" -> ");
				backupLinkIdsPathToEmbeddingMap.put(path[0], (path[1]).substring(0, path[1].length()-1).replace(" ", ":"));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public boolean defineHosts(EnvironmentOfServices3 environmentOfServers, Client clientChangeInfoOrquestrator, boolean realTimeContainers, EnvironmentOfTenants3 environmentOfTenants) throws IOException {

		Map<Integer, String> virtualHostIdLetterServerIdToEmbedding = new HashMap<Integer, String>();
		Map<String, ArrayList<Host>> letterServerIdPhysicalHostToEmbedding = new HashMap<String, ArrayList<Host>>();
		Map<String, ArrayList<Host>> VirtualSwitchIdVirtualHostToEmbedding = new HashMap<String, ArrayList<Host>>();
		Map<Integer, Integer> idVirtualHostIndexVirtualEdgeSC = new HashMap<Integer, Integer>();
		int currentPortNumber = environmentOfTenants.getInitialPortNumber();

		for(int i = 0; i < this.virtualEdgeSCArrayList.size(); i++){
			if(this.virtualEdgeSCArrayList != null){
				if (!this.virtualEdgeSCArrayList.get(i).isBetweenSwitches()){
					String vsId = this.virtualEdgeSCArrayList.get(i).getDestinationSwitch().getVirtualSwitchId()+"";
					String letra = workingVirtualNodeIdLetterServerIdToEmbeddingMap.get(vsId);
					int virtualHostId = this.virtualEdgeSCArrayList.get(i).getVirtualHost().getHostId();
					idVirtualHostIndexVirtualEdgeSC.put(virtualHostId, i);
					virtualHostIdLetterServerIdToEmbedding.put(virtualHostId, letra);
					if (VirtualSwitchIdVirtualHostToEmbedding.get(vsId) == null){
						ArrayList<Host> hH = new ArrayList<Host>();
						hH.add(this.virtualEdgeSCArrayList.get(i).getVirtualHost());
						VirtualSwitchIdVirtualHostToEmbedding.put(vsId, hH);
					} else{
						ArrayList<Host> hH = VirtualSwitchIdVirtualHostToEmbedding.get(vsId);
						hH.add(this.virtualEdgeSCArrayList.get(i).getVirtualHost());
						VirtualSwitchIdVirtualHostToEmbedding.put(vsId, hH);
					}
					if(!realTimeContainers){
						String mac = this.virtualEdgeSCArrayList.get(i).getVirtualHost().getMac().toString(); 
						String ip = this.virtualEdgeSCArrayList.get(i).getVirtualHost().getIpAddress().toString();
						String hostName = this.virtualEdgeSCArrayList.get(i).getVirtualHost().getHostName();
						int hostId = environmentOfServers.getLetterServerIdToEmbeddingServerId().get(letra);
						Host h = new Host(hostId, MacAddress.of(mac), VlanVid.ofVlan(0), IPv4Address.of(ip), hostName, -1);
//						this.getHostList().add(h);
						if(environmentOfServers.getHostIdHostMap().get(hostId) == null){
							environmentOfServers.getHostIdHostMap().put(hostId, h);
							environmentOfServers.getHostNameHostMap().put(hostName, h);
						}
						Server3 server = environmentOfServers.getServerIdServerMap().get(environmentOfServers.getLetterServerIdToEmbeddingServerId().get(letra));
						if (server != null && h != null){
							EdgeSC lane = new EdgeSC(currentPortNumber, "Link-"+server.getDescription()+"_"+server.getHostname()+"-"
									+h.getHostName()+"_"+h.getHostId(),server, h, 1, 1 , SecurityLevelLinks.valueOf(1), false);
							environmentOfServers.getEdgeSCApps().add(lane);
							environmentOfServers.getLinkLinkId().put("("+h.getHostId()+","+server.getServerId()+")", currentPortNumber);
						}
						DatapathId datapathId = server.getOpenVSwitch().getDatapathId();
						OFPort port = OFPort.of(currentPortNumber); 
						environmentOfServers.getHostNameHostLocationMap().put(hostName, new HostLocation(datapathId, port));
						environmentOfServers.getHostLocationHostMap().put(new HostLocation(datapathId, port), h);
						h.setHostLocation(new HostLocation(datapathId, port));
						environmentOfServers.getHostList().add(h);
						this.virtualEdgeSCArrayList.get(i).getVirtualHost().setPhysicalHost(h);
						h.setInUse(true);
						currentPortNumber++;
					}
				}
			}
		}
		environmentOfTenants.setInitialPortNumber(currentPortNumber);

		if(realTimeContainers){
			for(int i = 0; i < environmentOfServers.getPhysicalNetwork().getEdgeSCApps().size(); i++){
				if(environmentOfServers.getPhysicalNetwork().getEdgeSCApps() != null){
					if (!environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).isBetweenSwitches()){
						if (letterServerIdPhysicalHostToEmbedding.get(
								environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getSource().getLetterServerIdToEmbedding()) == null){
							ArrayList<Host> hA = new ArrayList<Host>();

							if(!environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getHost().isInUse() && environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getHost().getHostLocation() != null){
								hA.add(environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getHost());
								letterServerIdPhysicalHostToEmbedding.put(
										environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getSource().getLetterServerIdToEmbedding(), hA);
							}
						} else{
							if(!environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getHost().isInUse() && environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getHost().getHostLocation() != null){
								ArrayList<Host> hA = letterServerIdPhysicalHostToEmbedding.get(
										environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getSource().getLetterServerIdToEmbedding());
								hA.add(environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getHost());
								letterServerIdPhysicalHostToEmbedding.put(
										environmentOfServers.getPhysicalNetwork().getEdgeSCApps().get(i).getSource().getLetterServerIdToEmbedding(), hA);
							}
						}
					}
				}
			}

			Iterator<Entry<String, ArrayList<Host>>> itVirtualSwitchIdToEmbedding = VirtualSwitchIdVirtualHostToEmbedding.entrySet().iterator();
			while (itVirtualSwitchIdToEmbedding.hasNext()){
				Map.Entry pair = (Map.Entry)itVirtualSwitchIdToEmbedding.next();
				String letterVirtualIdToEmbedding = (String) pair.getKey();
				if(VirtualSwitchIdVirtualHostToEmbedding.get(letterVirtualIdToEmbedding)!=null){
					String letterPhysicalSwitchId = workingVirtualNodeIdLetterServerIdToEmbeddingMap.get(letterVirtualIdToEmbedding);
					if(letterServerIdPhysicalHostToEmbedding.get(letterPhysicalSwitchId) == null){
						int index = Utils.convertFromAlphabet(letterPhysicalSwitchId);
						String msgFailure = "The server -"+environmentOfServers.getPhysicalNetwork().getVertexes().get(index).getDescription()+"- does not have enough containers."; 	
						log.info(msgFailure);
						clientChangeInfoOrquestrator.sendVirtualFailure(msgFailure);
						return false;
					}
				}
			}

			Iterator<Entry<String, ArrayList<Host>>> itVirtualSwitchIdVirtualHostToEmbedding = VirtualSwitchIdVirtualHostToEmbedding.entrySet().iterator();
			String lastLetterVirtualSwitchId = "";
			while (itVirtualSwitchIdVirtualHostToEmbedding.hasNext()){
				ArrayList<Host> hH = new ArrayList<Host>();
				ArrayList<Host> hA = new ArrayList<Host>();
				Map.Entry pair = (Map.Entry)itVirtualSwitchIdVirtualHostToEmbedding.next();
				String letterVirtualSwitchId = (String) pair.getKey();
				lastLetterVirtualSwitchId = letterVirtualSwitchId;
				String letterServerId = workingVirtualNodeIdLetterServerIdToEmbeddingMap.get(letterVirtualSwitchId);
				hH = VirtualSwitchIdVirtualHostToEmbedding.get(letterVirtualSwitchId);
				hA = letterServerIdPhysicalHostToEmbedding.get(letterServerId);
				if(hA.size() < hH.size()){
					Iterator<Entry<String, ArrayList<Host>>> itVirtualSwitchIdVirtualHostToEmbeddingRemove = VirtualSwitchIdVirtualHostToEmbedding.entrySet().iterator();
					String letterVirtualSwitchIdRemove = "";				
					while (itVirtualSwitchIdVirtualHostToEmbeddingRemove.hasNext()){
						pair = (Map.Entry)itVirtualSwitchIdVirtualHostToEmbeddingRemove.next();
						letterVirtualSwitchIdRemove = (String) pair.getKey();
						letterServerId = workingVirtualNodeIdLetterServerIdToEmbeddingMap.get(letterVirtualSwitchIdRemove);
						hH = VirtualSwitchIdVirtualHostToEmbedding.get(letterVirtualSwitchIdRemove);
						hA = letterServerIdPhysicalHostToEmbedding.get(letterServerId);
						for(int i = 0; i < hH.size(); i++){
							if(i==hA.size()){
								break;
							}
							hH.get(i).setPhysicalHost(null);
							this.virtualEdgeSCArrayList.get(idVirtualHostIndexVirtualEdgeSC.get(hH.get(i).getHostId())).setWorkingPathLinks("");
							this.virtualEdgeSCArrayList.get(idVirtualHostIndexVirtualEdgeSC.get(hH.get(i).getHostId())).setWorkingPath("");
							hA.get(i).setInUse(false);
							hA.get(i).setOperational(true);
							int indexHostInEnvironmentOfServers = environmentOfServers.getHostList().indexOf(hA.get(i));
							environmentOfServers.getHostList().get(indexHostInEnvironmentOfServers).setInUse(false);
							environmentOfServers.getHostList().get(indexHostInEnvironmentOfServers).setOperational(true);
							environmentOfServers.getHostList().get(indexHostInEnvironmentOfServers).setMac(MacAddress.of("00:00:00:00:00:00"));
							environmentOfServers.getHostList().get(indexHostInEnvironmentOfServers).setIpAddress(IPv4Address.of("0.0.0.0"));
						}
						if(lastLetterVirtualSwitchId.equals(letterVirtualSwitchIdRemove)){
							int index = Utils.convertFromAlphabet(letterServerId);
							String msgFailure = "The server -"+environmentOfServers.getPhysicalNetwork().getVertexes().get(index).getDescription()+"- does not have enough containers."; 	
							log.info(msgFailure);
							clientChangeInfoOrquestrator.sendVirtualFailure(msgFailure);
							return false;
						}
					}
				}

				for(int i = 0; i < hH.size(); i++){
					String key2 = virtualHostIdLetterServerIdToEmbedding.get(hH.get(i).getHostId());
					hH.get(i).setPhysicalHost(hA.get(i));
					String path = hA.get(i).getHostId()+":"+environmentOfServers.getLetterServerIdToEmbeddingServerId().get(key2);
					this.virtualEdgeSCArrayList.get(idVirtualHostIndexVirtualEdgeSC.get(hH.get(i).getHostId())).setWorkingPathLinks(this.getLinksId(path, environmentOfServers));
					this.virtualEdgeSCArrayList.get(idVirtualHostIndexVirtualEdgeSC.get(hH.get(i).getHostId())).setWorkingPath(path);
					hA.get(i).setInUse(true);
					hA.get(i).setOperational(true);
					int indexHostInEnvironmentOfServers = environmentOfServers.getHostList().indexOf(hA.get(i));
					environmentOfServers.getHostList().get(indexHostInEnvironmentOfServers).setInUse(true);
					environmentOfServers.getHostList().get(indexHostInEnvironmentOfServers).setOperational(true);
					environmentOfServers.getHostList().get(indexHostInEnvironmentOfServers).setMac(hH.get(i).getMac());
					environmentOfServers.getHostList().get(indexHostInEnvironmentOfServers).setIpAddress(hH.get(i).getIpAddress());
				}
				for(int i = 0; i < hH.size(); i++){
					log.info("hH: {}}", hH.get(i).getHostName());
					log.info("Phy: {}", hH.get(i).getPhysicalHost().getHostName());
				}
			}
		}
		return true;
	}

	public String getLinksId(String path, EnvironmentOfServices3 environmentOfServers){

		String[] switches = path.split(":");
		String serversLinks = "";
		String link;
		String linkRev;
		String linkId = "";

		for (int i = 0; i < switches.length-1; i++){
			if(i!=switches.length-2){
				link = "("+switches[i]+","+switches[i+1]+")";
				linkRev = "("+switches[i+1]+","+switches[i]+")";
				if (environmentOfServers.getLinkLinkId().get(link) != null){
					linkId = environmentOfServers.getLinkLinkId().get(link).toString();
				} 
				if (environmentOfServers.getLinkLinkId().get(linkRev) != null){
					linkId = environmentOfServers.getLinkLinkId().get(linkRev).toString();
				} 
				if(!linkId.equals("")){
					serversLinks = serversLinks.concat(linkId);
					serversLinks = serversLinks.concat(":");
				}
				linkId = "";
			}else{
				link = "("+switches[i]+","+switches[i+1]+")";
				linkRev = "("+switches[i+1]+","+switches[i]+")";
				if (environmentOfServers.getLinkLinkId().get(link) != null){
					linkId = environmentOfServers.getLinkLinkId().get(link).toString();
				}
				if (environmentOfServers.getLinkLinkId().get(linkRev) != null){
					linkId = environmentOfServers.getLinkLinkId().get(linkRev).toString();
				} 
				if(!linkId.equals("")){
					serversLinks = serversLinks.concat(linkId);
				}
				linkId = "";
			}
		}
		return (serversLinks);
	}

	public boolean isNeedBackup() {
		return needBackup;
	}

	public void setNeedBackup(boolean needBackup) {
		this.needBackup = needBackup;
	}

	public boolean isInsertedInitialFlows() {
		return insertedInitialFlows;
	}

	public void setInsertedInitialFlows(boolean insertedInitialFlows) {
		this.insertedInitialFlows = insertedInitialFlows;
	}

	public VirtualGraphSC getVirtualGraph() {
		return virtualGraph;
	}

	public void setVirtualGraph(VirtualGraphSC virtualGraph) {
		this.virtualGraph = virtualGraph;
	}

	public DijkstraAlgorithmVirtualNetwork getDijkstraVirtualNetwork() {
		return dijkstraVirtualNetwork;
	}

	public void setDijkstraVirtualNetwork(DijkstraAlgorithmVirtualNetwork dijkstraVirtualNetwork) {
		this.dijkstraVirtualNetwork = dijkstraVirtualNetwork;
	}

	public boolean initGraph(){

		if((this.virtualEdgeSCArrayList!= null) && (this.virtualSwitchArrayList != null) 
				&& (this.virtualSwitchArrayList.size() >0)){
			ArrayList<VirtualEdgeSC> virtualEdges = new ArrayList<VirtualEdgeSC>();
			for (int i = 0; i < this.virtualEdgeSCArrayList.size(); i++){
				if(this.virtualEdgeSCArrayList.get(i).isBetweenSwitches()){
					int linkId = this.virtualEdgeSCArrayList.get(i).getVirtualEdgeSCId();
					VirtualSwitch virtualSwitch1 = this.virtualEdgeSCArrayList.get(i).getSourceSwitch();
					VirtualSwitch virtualSwitch3 = this.virtualEdgeSCArrayList.get(i).getDestinationSwitch();
					int bandwidht = this.virtualEdgeSCArrayList.get(i).getBandwidht();
					SecurityLevelLinks security = this.virtualEdgeSCArrayList.get(i).getSecurityLevel();
					virtualEdges.add(new VirtualEdgeSC(linkId, virtualSwitch1, virtualSwitch3, bandwidht, security, true));
					virtualEdges.add(new VirtualEdgeSC(linkId, virtualSwitch3, virtualSwitch1, bandwidht, security, true));
				}
			}
			this.virtualGraph = new VirtualGraphSC(this.getVirtualSwitchArrayList(), virtualEdges);
			log.info("Graph initialized - VirtualNetwork id: {} from tenant id: {}.", this.virtualNetworkId, this.tenantId);
			return true;
		}
		return false;
	}

	public boolean initDijkstra(){

		if((this.virtualSwitchArrayList != null) && (this.virtualSwitchArrayList.size()>0) &&
				(this.virtualEdgeSCArrayList != null) && (this.virtualEdgeSCArrayList.size() > 0)){
			if((this.virtualGraph != null) && (this.virtualGraph.getEdges()!= null) && 
					(this.virtualGraph.getVertexes() != null) && (this.virtualGraph.getVertexes().size() >0)){
				this.dijkstraVirtualNetwork = new DijkstraAlgorithmVirtualNetwork(this.virtualGraph);
				this.dijkstraVirtualNetwork.execute(this.virtualSwitchArrayList.get(0));
				log.info("Routing algorithm initialized - VirtualNetwork id: {} from tenant id: {}.", this.virtualNetworkId, this.tenantId);
				return true;
			}
		}
		return false;
	}

	public LinkedList<VirtualSwitch> getVirtualPath(VirtualSwitch sourceVirtualSwitch, VirtualSwitch destinationVirtualSwitch){

		LinkedList<VirtualSwitch> path = null;

		if(this.dijkstraVirtualNetwork != null && sourceVirtualSwitch != null && destinationVirtualSwitch != null){
			this.dijkstraVirtualNetwork.execute(sourceVirtualSwitch);
			path = this.dijkstraVirtualNetwork.getPath( destinationVirtualSwitch);
		}
		return path;
	}

	public VirtualNetworkHeu getVirtualNetworkHeu() {
		return virtualNetworkHeu;
	}


	public void setVirtualNetworkHeu(VirtualNetworkHeu virtualNetworkHeu) {
		this.virtualNetworkHeu = virtualNetworkHeu;
	}
}
