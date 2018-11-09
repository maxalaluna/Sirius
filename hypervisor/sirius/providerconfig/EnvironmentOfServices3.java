package net.floodlightcontroller.sirius.providerconfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.sirius.console.Config;
import net.floodlightcontroller.sirius.console.Controller;
import net.floodlightcontroller.sirius.console.Link;
import net.floodlightcontroller.sirius.console.Network;
import net.floodlightcontroller.sirius.console.Node;
import net.floodlightcontroller.sirius.console.Switch;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Substrate.SubstrateNetworkHeu;
import net.floodlightcontroller.sirius.nethypervisor.SiriusNetHypervisor;
import net.floodlightcontroller.sirius.providerconfig.topology.EdgeSC;
import net.floodlightcontroller.sirius.providerconfig.topology.PhysicalNetwork;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.tenantconfig.HostLocation;
import net.floodlightcontroller.sirius.util.Utils;
import net.floodlightcontroller.sirius.util.Enum.CloudType;
import net.floodlightcontroller.sirius.util.Enum.SecurityLevelLinks;
import net.floodlightcontroller.sirius.util.Enum.SecurityLevelPhysicalNode;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class EnvironmentOfServices3{

	protected static Logger log = LoggerFactory.getLogger(EnvironmentOfServices3.class);
	volatile ArrayList<Server3> serverList = new ArrayList<Server3>();
	private Map<Integer, Server3> serverIdServerMap = new HashMap<Integer, Server3>();
	private Map<Integer, Host> hostIdHostMap = new HashMap<Integer, Host>();
	private Map<String, Host> hostNameHostMap = new HashMap<String, Host>();
	private Map<Integer,String> serverIdletterServerIdToEmbedding = new HashMap<Integer,String>();
	private Map<String, Integer> letterServerIdToEmbeddingServerId = new HashMap<String, Integer>();
	private Map<String, Integer> linkLinkId = new HashMap<String, Integer>();
	protected Map<HostLocation,Host> hostLocationHostMap = new HashMap<HostLocation,Host>();
	protected Map<String,HostLocation> hostNameHostLocationMap = new HashMap<String,HostLocation>();
	String tunnelType;
	protected ArrayList<InterfacesGREToDelete3> interfacesGREToDelete = new ArrayList<InterfacesGREToDelete3>();
	protected ArrayList<String> BRIDGE_NAME_OF_VMS_ARRAY = new ArrayList<String>(
			Arrays.asList("s1", "sg18", "s14"));
	protected String BRIDGE_NAME_OF_VMS = "s1";

	volatile protected ArrayList<EdgeSC> edgeSCApps = new ArrayList<EdgeSC>();
	private Cloud privateCloud;
	private Map<DatapathId, IOFSwitch> datapathIdIOFSwitchMap = new HashMap<DatapathId, IOFSwitch>();
	volatile private PhysicalNetwork physicalNetwork;
	volatile ArrayList<Host> hostList = new ArrayList<Host>();
	volatile ArrayList<Cloud> cloudList = new ArrayList<Cloud>();
	Server3 controller;
	private boolean needBackup = false;
	private Network physicalNodes;
	private SubstrateNetworkHeu substrateNetworkEmbeddingHeu;
	boolean realTimeInfoContainers;


	public boolean isRealTimeInfoContainers() {
		return realTimeInfoContainers;
	}
	public void setRealTimeInfoContainers(boolean realTimeInfoContainers) {
		this.realTimeInfoContainers = realTimeInfoContainers;
	}
	public Map<Integer, Host> getHostIdHostMap() {
		return hostIdHostMap;
	}
	public void setHostIdHostMap(Map<Integer, Host> hostIdHostMap) {
		this.hostIdHostMap = hostIdHostMap;
	}
	public Map<HostLocation, Host> getHostLocationHostMap() {
		return hostLocationHostMap;
	}
	public void setHostLocationHostMap(Map<HostLocation, Host> hostLocationHostMap) {
		this.hostLocationHostMap = hostLocationHostMap;
	}
	public Map<String, HostLocation> getHostNameHostLocationMap() {
		return hostNameHostLocationMap;
	}
	public void setHostNameHostLocationMap(
			Map<String, HostLocation> hostNameHostLocationMap) {
		this.hostNameHostLocationMap = hostNameHostLocationMap;
	}

	public ArrayList<Host> getHostList() {
		return hostList;
	}
	public boolean isNeedBackup() {
		return needBackup;
	}
	public void setNeedBackup(boolean needBackup) {
		this.needBackup = needBackup;
	}
	public Map<Integer, Server3> getServerIdServerMap() {
		return serverIdServerMap;
	}
	public void setServerIdServerMap(Map<Integer, Server3> serverIdServerMap) {
		this.serverIdServerMap = serverIdServerMap;
	}
	public void setHostList(ArrayList<Host> hostList) {
		this.hostList = hostList;
	}
	public EnvironmentOfServices3(String tunnelType) {
		super();
		this.tunnelType = tunnelType;
	}

	public String getTunnelType() {
		return tunnelType;
	}

	public ArrayList<Server3> getServerList() {
		return serverList;
	}
	public void setServerList(ArrayList<Server3> serverList) {
		this.serverList = serverList;
	}
	public ArrayList<Cloud> getCloudList() {
		return cloudList;
	}
	public void setCloudList(ArrayList<Cloud> cloudList) {
		this.cloudList = cloudList;
	}
	public void setTunnelType(String tunnelType) {
		this.tunnelType = tunnelType;
	}

	public ArrayList<EdgeSC> getEdgeSCApps() {
		return edgeSCApps;
	}

	public void setEdgeSCApps(ArrayList<EdgeSC> edgeSCApps) {
		this.edgeSCApps = edgeSCApps;
	}

	public String getBRIDGE_NAME_OF_VMS() {
		return BRIDGE_NAME_OF_VMS;
	}

	public ArrayList<String> getBRIDGE_NAME_OF_VMS_ARRAY() {
		return BRIDGE_NAME_OF_VMS_ARRAY;
	}

	public void setBRIDGE_NAME_OF_VMS_ARRAY(
			ArrayList<String> bRIDGE_NAME_OF_VMS_ARRAY) {
		BRIDGE_NAME_OF_VMS_ARRAY = bRIDGE_NAME_OF_VMS_ARRAY;
	}

	public void setBRIDGE_NAME_OF_VMS(String bRIDGE_NAME_OF_VMS) {
		BRIDGE_NAME_OF_VMS = bRIDGE_NAME_OF_VMS;
	}

	public Map<DatapathId, IOFSwitch> getDatapathIdIOFSwitchMap() {
		return datapathIdIOFSwitchMap;
	}

	public void setDatapathIdIOFSwitchMap(Map<DatapathId, IOFSwitch> datapathIdIOFSwitchMap) {
		this.datapathIdIOFSwitchMap = datapathIdIOFSwitchMap;
	}

	public String nextBridgeName(Server3 server2, Connection connection){

		String nextBridgeName = "";

		try {
			Session sessServer = connection.openSession();

			String command1 = "date +%s%N";
			sessServer.execCommand(command1);

			InputStream stdoutServer = new StreamGobbler(sessServer.getStdout());
			@SuppressWarnings("resource")
			BufferedReader brServer = new BufferedReader(new InputStreamReader(stdoutServer));

			while (true){
				String line = brServer.readLine();
				if (line == null)
					break;
				nextBridgeName += line;
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nextBridgeName;
	}

	public boolean setController(ArrayList<Server3> serverList, Server3 controller) throws InterruptedException{

		Server3 server2;
		boolean result = true;

		if (serverList != null){
			Iterator<Server3> iteratorServer = serverList.iterator();
			while(iteratorServer.hasNext()){
				server2 = iteratorServer.next();
				result = (result && setController(server2, controller));
			}			
		}
		return result;
	}

	public boolean setController(Server3 server2, Server3 controller) throws InterruptedException{

		try{
			Connection connServer = null;
			boolean isAuthenticatedServer1 = false;
			String cloudName = server2.getCloud().getDescription().toLowerCase();

			switch (cloudName) {
				//Connection based on public/private keys
			case "amazon":
				connServer = new Connection(server2.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer1 = connServer.authenticateWithPublicKey("ubuntu", 
						new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);
                break;
                //Connection based on public/private keys
			case "google":
				connServer = new Connection(server2.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer1 = connServer.authenticateWithPublicKey("sirius", 
						new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);
				break;
				//Connection based on user name/password
			case "fcul":
				connServer = new Connection(server2.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer1 = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				break;
				//Connection based on user name/password
			case "imt":
				connServer = new Connection(server2.getHostname(),2006);
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer1 = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				break;
			}
			if (isAuthenticatedServer1 == false){
				throw new IOException("Authentication failed on server "+server2.getHostname()+".");
			}else{
				Session sessServer = connServer.openSession();
				String command = "sudo ovs-vsctl set-controller "+ server2.getBridgeName() +" tcp:"+controller.getHostname()+":6653";
				log.info("setController -{}- in server -{}-", command, server2.getDescription());
				sessServer.execCommand(command);
				sessServer.close();
				connServer.close();
				return true;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
		return false;
	}

	public String createTunnelsFullMesh() throws InterruptedException{

		ArrayList<ArrayList<Server3>> combination = new ArrayList<ArrayList<Server3>>();
		String result = "";
		log.info("Setting Tunnels Full Mesh...");
		
		if (this.getPhysicalNetwork().getVertexes() != null){
			for (int i = 0; i < this.getPhysicalNetwork().getVertexes().size()-1; i++){
				for (int j = i+1; j < this.getPhysicalNetwork().getVertexes().size(); j++ ){
					combination.add(new ArrayList<Server3>(Arrays.asList(this.getPhysicalNetwork().getVertexes().get(i),
							this.getPhysicalNetwork().getVertexes().get(j))));
				}
			}
		}
		for (int i = 0; i < combination.size(); i++){
			result += createTunnels(combination.get(i).get(0),combination.get(i).get(1));
			int cost = Math.max((int)(100000000/combination.get(i).get(0).getLinkSpeed()), (int)(100000000/combination.get(i).get(1).getLinkSpeed()));
			EdgeSC lane = new EdgeSC("Link-"+combination.get(i).get(0).getDescription()+"_"+combination.get(i).get(0).getHostname()+"_"+combination.get(i).get(0).getOpenVSwitch().getDataPathId()+"-"
					+combination.get(i).get(1).getDescription()+"_"+combination.get(i).get(1).getHostname()+"_"+combination.get(i).get(1).getOpenVSwitch().getDataPathId(),combination.get(i).get(0), 
					combination.get(i).get(1), cost);
			edgeSCApps.add(lane);
		}
		/*Generate combination 2 by 2 of all servers*/
		/*Use the list of two servers to create the tunnels using the function createTunnels*/
		log.info("Setting Tunnels Full Mesh finished!!!");
		return result;
	}

	public boolean hasTunnelConfigured(Server3 serverOrig, Server3 serverDst){

		ArrayList<Bridge3> bridgeList = serverOrig.getOpenVSwitch().getArrayListBridge();
		ArrayList<Interfaces3> interfaceList = new ArrayList<Interfaces3>();
		Bridge3 bridge;
		Interfaces3 interfaces;

		if (bridgeList != null){
			Iterator<Bridge3> iteratorBridge = bridgeList.iterator();
			while(iteratorBridge.hasNext()){
				bridge = iteratorBridge.next();
				//if(bridge2.getName().compareTo(this.BRIDGE_NAME_OF_VMS)==0){
				if(bridge.getName().compareTo(serverOrig.getBridgeName())==0){
					interfaceList = bridge.getArrayListInterfaces();
					if (interfaceList != null){
						Iterator<Interfaces3> iteratorInterfaces = interfaceList.iterator();
						while(iteratorInterfaces.hasNext()){
							interfaces = iteratorInterfaces.next();
							if(interfaces.getType().compareTo(this.getTunnelType())==0 
									&& interfaces.getRemoteIP().compareTo(serverDst.getHostname())==0){
								return true;
							}
						}
					}
				}
			}			
		}
		return false;
	}

	public String createTunnels(Server3 server1, Server3 server2) throws InterruptedException{

		String result = "Inicial";
		try{
			/* Create a connection instance */
			Connection connServer1 = null;
			/* Now connect */
//			connServer1.connect(null,25000,25000);

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			//			boolean isAuthenticatedServer1 = connServer1.authenticateWithPassword(server1.getUsername(), server1.getPassword());

			boolean isAuthenticatedServer1 = false;

			if(server1.getCloud().getDescription().toLowerCase().contains(((String)"Amazon").toLowerCase())){
				connServer1 = new Connection(server1.getHostname());
				this.connectServer(50, connServer1, server2);
				isAuthenticatedServer1 = connServer1.authenticateWithPublicKey("ubuntu", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);

			} else {
				if(server1.getCloud().getDescription().toLowerCase().contains(((String)"Google").toLowerCase())){
					connServer1 = new Connection(server1.getHostname());
					this.connectServer(50, connServer1, server2);
					isAuthenticatedServer1 = connServer1.authenticateWithPublicKey("sirius", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);
				} 
				if(server1.getCloud().getDescription().toLowerCase().contains(((String)"FCUL").toLowerCase())){
					connServer1 = new Connection(server1.getHostname());
					connServer1.connect(null,25000,25000);
					isAuthenticatedServer1 = connServer1.authenticateWithPassword(server1.getUsername(), server1.getPassword());
				}
				if(server2.getCloud().getDescription().toLowerCase().contains(((String)"imt").toLowerCase())){
					connServer1 = new Connection(server2.getHostname(),2006);
					this.connectServer(50, connServer1, server2);
					isAuthenticatedServer1 = connServer1.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				}
			}

			if (isAuthenticatedServer1 == false){
				throw new IOException("Authentication failed on server "+server1.getHostname()+".");
			}

			if (isAuthenticatedServer1 ){
				/* Create a session */
				Session sessServer1 = connServer1.openSession();
				//Improve this part verifing the correct execution of the command add-port
				/*Create commands*/
				String nextnextBridgeName = this.nextBridgeName(server1, connServer1);
				String command1 = "sudo ovs-vsctl add-port xenbr0 "+this.getTunnelType()+nextnextBridgeName+" -- set interface "+this.getTunnelType()+nextnextBridgeName+" type="
						+ this.getTunnelType()+" options:remote_ip="+ server2.getHostname();
				sessServer1.execCommand(command1);
				/* 
				 * This basic example does not handle stderr, which is sometimes dangerous
				 * (please read the FAQ).
				 */
				InputStream stdoutServer1 = new StreamGobbler(sessServer1.getStdout());
				BufferedReader brServer1 = new BufferedReader(new InputStreamReader(stdoutServer1));
				while (true){
					String line = brServer1.readLine();
					if (line == null)
						break;
				}
				/* Show exit status, if available (otherwise "null") */
				result = "ExitCode on server "+server1.getHostname()+" : " + sessServer1.getExitStatus()+"\n";
				/* Close this session */
				sessServer1.close();
				/* Close the connection */
				connServer1.close();
				/* Close the BufferedReader */
				brServer1.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
		}

		try{
			/* Create a connection instance */
			Connection connServer2 = null;
			/* Now connect */
			//connServer2.connect(null,25000,25000);
			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			//boolean isAuthenticatedServer2 = connServer2.authenticateWithPassword(server2.getUsername(), server2.getPassword());
			boolean isAuthenticatedServer2 = false;

			if(server2.getCloud().getDescription().toLowerCase().contains(((String)"Amazon").toLowerCase())){
				connServer2 = new Connection(server2.getHostname());
				this.connectServer(50, connServer2, server2);
				isAuthenticatedServer2 = connServer2.authenticateWithPublicKey("ubuntu", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);
			} else {
				if(server2.getCloud().getDescription().toLowerCase().contains(((String)"Google").toLowerCase())){
					connServer2 = new Connection(server2.getHostname());
					this.connectServer(50, connServer2, server2);
					isAuthenticatedServer2 = connServer2.authenticateWithPublicKey("sirius", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);
				}
				if(server2.getCloud().getDescription().toLowerCase().contains(((String)"FCUL").toLowerCase())){
					connServer2 = new Connection(server2.getHostname());
					isAuthenticatedServer2 = connServer2.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				}
				if(server2.getCloud().getDescription().toLowerCase().contains(((String)"imt").toLowerCase())){
					connServer2 = new Connection(server2.getHostname(),2006);
					this.connectServer(50, connServer2, server2);
					isAuthenticatedServer2 = connServer2.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				}
			}

			if (isAuthenticatedServer2 == false){
				throw new IOException("Authentication failed on server "+server2.getHostname()+".");
			}

			if (isAuthenticatedServer2){
				/* Create a session */
				Session sessServer2 = connServer2.openSession();
				//Improve this part verifing the correct execution of the command add-port
				/*Create commands*/
				String nextnextBridgeName = this.nextBridgeName(server2, connServer2);
				String command2 = "sudo ovs-vsctl add-port xenbr0 "+this.getTunnelType()+nextnextBridgeName+" -- set interface "+this.getTunnelType()+nextnextBridgeName+" type="
						+ this.getTunnelType()+" options:remote_ip="+ server1.getHostname();
				sessServer2.execCommand(command2);
				/* 
				 * This basic example does not handle stderr, which is sometimes dangerous
				 * (please read the FAQ).
				 */
				InputStream stdoutServer2 = new StreamGobbler(sessServer2.getStdout());
				BufferedReader brServer2 = new BufferedReader(new InputStreamReader(stdoutServer2));
				while (true){
					String line = brServer2.readLine();
					if (line == null)
						break;
				}

				/* Show exit status, if available (otherwise "null") */
				result += "ExitCode on server "+server2.getHostname()+" : " + sessServer2.getExitStatus()+"\n";
				/* Close this session */
				sessServer2.close();
				/* Close the connection */
				connServer2.close();
				/* Close the BufferedReader */
				brServer2.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
		}
		return result;
	}

	public synchronized void populateEnvironment(boolean realTimeInfoContainers, SiriusNetHypervisor siriusNetHypervisor) throws InterruptedException {

		if(realTimeInfoContainers){
			List<Server3> serverList = this.getPhysicalNetwork().getVertexes();
			String ovsVsctlShow = "";
			for (int i=0; i < serverList.size(); i++){
				ovsVsctlShow = this.getInfoOVS(serverList.get(i));
				String dataPath = this.getDataPathId(serverList.get(i));
				if(dataPath.equals("")){
					log.info("DatapathId of server -{}- is empty", serverList.get(i).getDescription());
				}else{
					DatapathId datapathId = DatapathId.of(Long.valueOf(dataPath,16));
					//switchService.getActiveSwitch(datapathId);
					serverList.get(i).setSw(siriusNetHypervisor.getSwitchService().getActiveSwitch(datapathId));
					serverList.get(i).populateInfoOvsVsctlShow(ovsVsctlShow, datapathId, this.datapathIdIOFSwitchMap.get(datapathId));
					for (int j=0; j < serverList.get(i).getOpenVSwitch().getArrayListBridge().size(); j++){
						if (serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getName().compareTo(serverList.get(i).getBridgeName())==0){
							for (int k=0; k < serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().size(); k++){
								this.setInfoInterface(i, j, k, this.getPhysicalNetwork().getVertexes().get(i), 
										serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k));
								log.info("Populating information of Interface {} finished!!!", 
										serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k).getName());
							}
						}
						log.info("Populating information of Bridge {} finished!!!", serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getName());
					}
				}
				log.info("Populating information of Server {} finished!!!", serverList.get(i).getDescription());
			}
		}else{
			List<Server3> serverList = this.getPhysicalNetwork().getVertexes();
			String ovsVsctlShow = "";
			for (int i=0; i < serverList.size(); i++){
				ovsVsctlShow = this.getInfoOVS(serverList.get(i));
				String dataPath = this.getDataPathId(serverList.get(i));
				if(dataPath.equals("")){
					log.info("DatapathId of server -{}- is empty", serverList.get(i).getDescription());
				}else{
					DatapathId datapathId = DatapathId.of(Long.valueOf(dataPath,16));
					serverList.get(i).setSw(siriusNetHypervisor.getSwitchService().getActiveSwitch(datapathId));
					serverList.get(i).populateInfoOvsVsctlShow(ovsVsctlShow, datapathId, this.datapathIdIOFSwitchMap.get(datapathId));
					siriusNetHypervisor.getDatapathIdServerMap().put(datapathId, serverList.get(i));
					for (int j=0; j < serverList.get(i).getOpenVSwitch().getArrayListBridge().size(); j++){
						if (serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getName().compareTo(serverList.get(i).getBridgeName())==0){
							for (int k=0; k < serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().size(); k++){
								if(serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k).getName().contains("gre")){
									this.setInfoGREInterface(i, j, k, this.getPhysicalNetwork().getVertexes().get(i), 
											serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k));
									log.info("Populating information of Interface {} finished!!!", 
											serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k).getName());
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static void connectServer( int maxConnections, Connection connServer, Server3 server) throws InterruptedException{
		int n = 0;
		String nrConnection = "";
		int multiTimeout = 1;
		while(n < maxConnections){
			try{
				nrConnection = n+1+"/"+maxConnections;
				log.info("Connection {} in server {}", nrConnection, server.description );
				connServer.connect(null,500*multiTimeout,500*multiTimeout);
				nrConnection = "";
				break;
			}catch (IOException e){
				if(n%2 == 0){
					Thread.sleep(500*multiTimeout);
					multiTimeout++;
				}
				n++;
				continue;
			}
		}
		
		if(n == maxConnections){
			log.info("Number of connection attempts exceeded.");
			System.exit(0);
		}
		log.info("Server {} connected.", nrConnection, server.description );
	}

	public void setInfoGREInterface(int i, int j, int k, Server3 server, Interfaces3 interfaces) throws InterruptedException {

		try{
			String interfacesInfo = null;
			Connection connServer = null;
			boolean isAuthenticatedServer = false;
			String cloudName = server.getCloud().getDescription().toLowerCase();

			switch (cloudName) {
			case "amazon":
				connServer = new Connection(server.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server);
				isAuthenticatedServer = connServer.authenticateWithPublicKey("ubuntu", 
						new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);
				break;
			case "google":
				connServer = new Connection(server.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server);
				isAuthenticatedServer = connServer.authenticateWithPublicKey("sirius", 
						new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);
				break;
			case "fcul":
				connServer = new Connection(server.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server);
				isAuthenticatedServer = connServer.authenticateWithPassword(server.getUsername(), server.getPassword());
				break;
			case "imt":
				connServer = new Connection(server.getHostname(),2006);
				EnvironmentOfServices3.connectServer(50, connServer, server);
				isAuthenticatedServer = connServer.authenticateWithPassword(server.getUsername(), server.getPassword());
				break;
			}
			if (isAuthenticatedServer == false){
				throw new IOException("Authentication failed on server "+server.getHostname()+".");
			}
			if (isAuthenticatedServer){
				String commandOfport = "sudo ovs-vsctl get interface "+ interfaces.getName() +" ofport"; 
				Session sessServerOfport = connServer.openSession();
				sessServerOfport.execCommand(commandOfport);
				InputStream stdoutServerOfport = new StreamGobbler(sessServerOfport.getStdout());
				BufferedReader brServerOfport = new BufferedReader(new InputStreamReader(stdoutServerOfport));
				interfacesInfo = brServerOfport.readLine();
				if (interfacesInfo.compareTo("[]")==0 ){
					this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
					get(k).setOfport(OFPort.ZERO);
				} else {
					this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
					get(k).setOfport(OFPort.of(Integer.parseInt(interfacesInfo)));
				}
				sessServerOfport.close();
				brServerOfport.close();
				connServer.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
	}
	
	public void setInfoInterface(int i, int j, int k, Server3 server, Interfaces3 interfaces) throws InterruptedException {

		String interfacesInfo = null;
		DatapathId datapathId = server.getOpenVSwitch().getDatapathId();
		OFPort port = null;

		try{
			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			//boolean isAuthenticatedServer = connServer.authenticateWithPassword(server.getUsername(), server.getPassword());

			boolean isAuthenticatedServer = false;
			boolean isAuthenticatedServer2 = false;

			if(server.getCloud().getDescription().toLowerCase().contains(((String)"Amazon").toLowerCase())){
				/* Create a connection instance */
				Connection connServer = new Connection(server.getHostname());
				/* Now connect */
				this.connectServer(50, connServer, server);

				isAuthenticatedServer = connServer.authenticateWithPublicKey("ubuntu", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);

				if (isAuthenticatedServer == false){
					throw new IOException("Authentication failed on server "+server.getHostname()+".");

				}else{
//					String commandUuid = "sudo ovs-vsctl get interface "+ interfaces.getName() +" _uuid";
//					String commandAdminState = "sudo ovs-vsctl get interface "+ interfaces.getName() +" admin_state"; //admin_state
//					String commandDuplex = "sudo ovs-vsctl get interface "+ interfaces.getName() +" duplex"; //duplex
//					String commandVmsMacAddress = "sudo ovs-vsctl get interface "+ interfaces.getName() +" external_ids:attached-mac";
//					String commandLinkSpeed = "sudo ovs-vsctl get interface "+ interfaces.getName() +" link_speed"; // link_speed          : 10000000000
//					String commandLinkState = "sudo ovs-vsctl get interface "+ interfaces.getName() +" link_state"; // link_state          : up
//					String commandMacAddress = "sudo ovs-vsctl get interface "+ interfaces.getName() +" mac_in_use"; // mac_in_use          : "e6:e8:2f:93:25:53"
//					String commandMtu = "sudo ovs-vsctl get interface "+ interfaces.getName() +" mtu"; // mtu
//					String commandName = "sudo ovs-vsctl get interface "+ interfaces.getName() +" name"; // name                : "s1-eth3"
					String commandOfport = "sudo ovs-vsctl get interface "+ interfaces.getName() +" ofport"; // ofport
//					String commandOfportRequest = "sudo ovs-vsctl get interface "+ interfaces.getName() +" ofport_request"; // 	ofport_request      : 3
//					String commandRemoteIP = "sudo ovs-vsctl get interface "+ interfaces.getName() +" options:remote_ip"; // options             : {remote_ip="192.168.5.34"}
//					String commandType = "sudo ovs-vsctl get interface "+ interfaces.getName() +" type";
					String commandHostName = "sudo ovs-vsctl get interface "+ interfaces.getName() +" external_ids:container_id";

//					Session sessServerUuid = connServer.openSession();
//					Session sessServerAdminState = connServer.openSession();
//					Session sessServerDuplex = connServer.openSession();
//					Session sessServerVmsMacAddress = connServer.openSession();
//					Session sessServerLinkSpeed = connServer.openSession();
//					Session sessServerLinkState = connServer.openSession();
//					Session sessServerMacAddress = connServer.openSession();
//					Session sessServerMtu = connServer.openSession();
//					Session sessServerName = connServer.openSession();
					Session sessServerOfport = connServer.openSession();
//					Session sessServerOfportRequest = connServer.openSession();
//					Session sessServerRemoteIP = connServer.openSession();
//					Session sessServerType = connServer.openSession();
					Session sessServerHostName = connServer.openSession();

//					sessServerUuid.execCommand(commandUuid);
//					sessServerAdminState.execCommand(commandAdminState);
//					sessServerDuplex.execCommand(commandDuplex);
//					sessServerVmsMacAddress.execCommand(commandVmsMacAddress);
//					sessServerLinkSpeed.execCommand(commandLinkSpeed);
//					sessServerLinkState.execCommand(commandLinkState);
//					sessServerMacAddress.execCommand(commandMacAddress);
//					sessServerMtu.execCommand(commandMtu);
//					sessServerName.execCommand(commandName);
					sessServerOfport.execCommand(commandOfport);
//					sessServerOfportRequest.execCommand(commandOfportRequest);
//					sessServerRemoteIP.execCommand(commandRemoteIP);
//					sessServerType.execCommand(commandType);
					sessServerHostName.execCommand(commandHostName);

//					InputStream stdoutServerType = new StreamGobbler(sessServerType.getStdout());
//					BufferedReader brServerType = new BufferedReader(new InputStreamReader(stdoutServerType));
//					interfacesInfo = brServerType.readLine();


//					if(interfacesInfo != null){
//						if (interfacesInfo.equals(this.tunnelType)){

							/*
						//this.removeTunnel(connServer, this.BRIDGE_NAME_OF_VMS, interfaces2.getName());
						this.removeTunnel(connServer, server.getBridgeName(), interfaces.getName());

						this.interfacesGREToDelete.add(new InterfacesGREToDelete3(i, j, k, this.serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k)));
						//this.serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().remove(
						//	this.serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k));						
							 */
//						} else{

//							if (interfacesInfo.compareTo("\"\"")==0 ){
//
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setType("");
//
//							}else{
//
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setType(interfacesInfo);
//							}

//							InputStream stdoutServerUuid = new StreamGobbler(sessServerUuid.getStdout());
//							BufferedReader brServerUuid = new BufferedReader(new InputStreamReader(stdoutServerUuid));
//							this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//							get(k).setUuid(UUID.fromString(brServerUuid.readLine()));


//							InputStream stdoutServerAdminState = new StreamGobbler(sessServerAdminState.getStdout());
//							BufferedReader brServerAdminState = new BufferedReader(new InputStreamReader(stdoutServerAdminState));
//							interfacesInfo = brServerAdminState.readLine();
//							if (interfacesInfo.compareTo("up")==0 ){
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setAdminState(true);
//							} else {
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setAdminState(false);
//							}


//							InputStream stdoutServerDuplex = new StreamGobbler(sessServerDuplex.getStdout());
//							BufferedReader brServerDuplex = new BufferedReader(new InputStreamReader(stdoutServerDuplex));
//							interfacesInfo = brServerDuplex.readLine();
//							if (interfacesInfo.compareTo("[]")==0 ){
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setDuplex("");
//							} else {
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setDuplex(interfacesInfo);
//							}


//							InputStream stdoutServerVmsMacAddress = new StreamGobbler(sessServerVmsMacAddress.getStdout());
//							BufferedReader brServerVmsMacAddress = new BufferedReader(new InputStreamReader(stdoutServerVmsMacAddress));
//							interfacesInfo = brServerVmsMacAddress.readLine();
//							if (interfacesInfo != null){
//								if (interfacesInfo.compareTo("{}")!=0){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setVmsMacAddress(MacAddress.of(interfacesInfo.replace("\"", "")));
//								}
//							}


//							InputStream stdoutServerLinkSpeed = new StreamGobbler(sessServerLinkSpeed.getStdout());
//							BufferedReader brServerLinkSpeed = new BufferedReader(new InputStreamReader(stdoutServerLinkSpeed));
//							interfacesInfo = brServerLinkSpeed.readLine();
//							if (interfacesInfo.compareTo("[]")==0 ){
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setLinkSpeed(0);
//							} else {
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setLinkSpeed(Long.parseLong(interfacesInfo));
//							}

//							InputStream stdoutServerLinkState = new StreamGobbler(sessServerLinkState.getStdout());
//							BufferedReader brServerLinkState = new BufferedReader(new InputStreamReader(stdoutServerLinkState));
//							interfacesInfo = brServerLinkState.readLine();
//							if (interfacesInfo.compareTo("up")==0 ){
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setLinkState(true);
//							} else {
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setLinkState(false);
//							}


//							InputStream stdoutServerMacAddress = new StreamGobbler(sessServerMacAddress.getStdout());
//							BufferedReader brServerMacAddress = new BufferedReader(new InputStreamReader(stdoutServerMacAddress));
//							interfacesInfo = brServerMacAddress.readLine();
//							if (interfacesInfo.compareTo("[]")==0 ){
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setMacAddress(MacAddress.NONE);
//							} else {
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setMacAddress(MacAddress.of(interfacesInfo.replace("\"", "")));
//							}


//							InputStream stdoutServerMtu = new StreamGobbler(sessServerMtu.getStdout());
//							BufferedReader brServerMtu = new BufferedReader(new InputStreamReader(stdoutServerMtu));
//							interfacesInfo = brServerMtu.readLine();
//							if (interfacesInfo.compareTo("[]")==0 ){
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setMtu(0);
//							} else {
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setMtu(Integer.parseInt(interfacesInfo));
//							}


							InputStream stdoutServerOfport = new StreamGobbler(sessServerOfport.getStdout());
							BufferedReader brServerOfport = new BufferedReader(new InputStreamReader(stdoutServerOfport));
							interfacesInfo = brServerOfport.readLine();
							//log.info("setInfoInterface -{}-", interfacesInfo);
							if (interfacesInfo.compareTo("[]")==0 ){
								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
								get(k).setOfport(OFPort.ZERO);
							} else {
								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
								get(k).setOfport(OFPort.of(Integer.parseInt(interfacesInfo)));
								if(interfacesInfo.equals("any")){
									log.info("if(interfacesInfo.equals(\"any\")){");
								}
								port = OFPort.of(Integer.parseInt(interfacesInfo));
							}
							sessServerOfport.close();

//							InputStream stdoutServerOfportRequest = new StreamGobbler(sessServerOfportRequest.getStdout());
//							BufferedReader brServerOfportRequest = new BufferedReader(new InputStreamReader(stdoutServerOfportRequest));
//							interfacesInfo = brServerOfportRequest.readLine();
//							if (interfacesInfo.compareTo("[]")==0 ){
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setOfportRequest("");
//							} else {
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setOfportRequest(interfacesInfo);
//							}


//							InputStream stdoutServerRemoteIP = new StreamGobbler(sessServerRemoteIP.getStdout());
//							BufferedReader brServerRemoteIP = new BufferedReader(new InputStreamReader(stdoutServerRemoteIP));
//							interfacesInfo = brServerRemoteIP.readLine();
//							if (interfacesInfo != null){
//								if (interfacesInfo.compareTo("{}")==0){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setRemoteIP("");
//								}else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setRemoteIP(interfacesInfo.replace("\"", ""));
//								}
//							}

							InputStream stdoutServerHostName = new StreamGobbler(sessServerHostName.getStdout());
							BufferedReader brServerHostName = new BufferedReader(new InputStreamReader(stdoutServerHostName));
							interfacesInfo = brServerHostName.readLine();
							if (interfacesInfo != null){
								if (interfacesInfo.compareTo("{}")!=0){
									String hostName = interfacesInfo.replace("\"", "");
									if(!hostName.isEmpty() && port.getPortNumber() > 0){
										int indexHost = this.hostList.indexOf(this.hostNameHostMap.get(hostName));
										if(indexHost >= 0){
											this.hostNameHostLocationMap.put(hostName, new HostLocation(datapathId, port));
											this.hostLocationHostMap.put(new HostLocation(datapathId, port), this.hostNameHostMap.get(hostName));
											this.hostList.get(indexHost).setHostLocation(new HostLocation(datapathId, port));	
										}
									}						
								}
							}
							sessServerHostName.close();
							/* Close the BufferedReader */
//							brServerUuid.close();
//							brServerAdminState.close();
//							brServerDuplex.close();
//							brServerVmsMacAddress.close();
//							brServerLinkSpeed.close();
//							brServerLinkState.close();
//							brServerMacAddress.close();
//							brServerMtu.close();
							brServerOfport.close();
//							brServerOfportRequest.close();
//							brServerRemoteIP.close();
							brServerHostName.close();

							/* Close this session */
//							sessServerUuid.close();
//							sessServerAdminState.close();
//							sessServerDuplex.close();
//							sessServerVmsMacAddress.close();
//							sessServerLinkSpeed.close();
//							sessServerLinkState.close();
//							sessServerMacAddress.close();
//							sessServerMtu.close();
//							sessServerName.close();
//							sessServerOfport.close();
//							sessServerOfportRequest.close();
//							sessServerRemoteIP.close();
//							sessServerType.close();
//							sessServerHostName.close();
//						}
//					}
					/* Close the connection */
					connServer.close();
					/* Close the BufferedReader */
//					brServerType.close();
				}
			} else {
				if(server.getCloud().getDescription().toLowerCase().contains(((String)"Google").toLowerCase())){

					/* Create a connection instance */
					Connection connServer = new Connection(server.getHostname());

					/* Now connect */
					this.connectServer(50, connServer, server);

					isAuthenticatedServer = connServer.authenticateWithPublicKey("sirius", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);

					if (isAuthenticatedServer == false){
						throw new IOException("Authentication failed on server "+server.getHostname()+".");

					}else{
						String commandOfport = "sudo ovs-vsctl get interface "+ interfaces.getName() +" ofport"; // ofport
						String commandHostName = "sudo ovs-vsctl get interface "+ interfaces.getName() +" external_ids:container_id";
						Session sessServerOfport = connServer.openSession();
						Session sessServerHostName = connServer.openSession();
						sessServerOfport.execCommand(commandOfport);
						sessServerHostName.execCommand(commandHostName);

								InputStream stdoutServerOfport = new StreamGobbler(sessServerOfport.getStdout());
								BufferedReader brServerOfport = new BufferedReader(new InputStreamReader(stdoutServerOfport));
								interfacesInfo = brServerOfport.readLine();
								//log.info("setInfoInterface -{}-", interfacesInfo);
								if (interfacesInfo.compareTo("[]")==0 ){
									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
									get(k).setOfport(OFPort.ZERO);
								} else {
									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
									get(k).setOfport(OFPort.of(Integer.parseInt(interfacesInfo)));
									if(interfacesInfo.equals("any")){
										log.info("if(interfacesInfo.equals(\"any\")){");
									}
									port = OFPort.of(Integer.parseInt(interfacesInfo));
								}
								sessServerOfport.close();

								InputStream stdoutServerHostName = new StreamGobbler(sessServerHostName.getStdout());
								BufferedReader brServerHostName = new BufferedReader(new InputStreamReader(stdoutServerHostName));
								interfacesInfo = brServerHostName.readLine();
								if (interfacesInfo != null){
									if (interfacesInfo.compareTo("{}")!=0){
										//Verificar 6fev17
										String hostName = interfacesInfo.replace("\"", "");
										if(!hostName.isEmpty() && port.getPortNumber() > 0){
											int indexHost = this.hostList.indexOf(this.hostNameHostMap.get(hostName));
											if(indexHost >= 0){
												this.hostNameHostLocationMap.put(hostName, new HostLocation(datapathId, port));
												this.hostLocationHostMap.put(new HostLocation(datapathId, port), this.hostNameHostMap.get(hostName));
												this.hostList.get(indexHost).setHostLocation(new HostLocation(datapathId, port));	
											}
										}						
									}
								}
								sessServerHostName.close();
								brServerOfport.close();
								brServerHostName.close();

						connServer.close();
					}
				}
				if(server.getCloud().getDescription().toLowerCase().contains(((String)"FCUL").toLowerCase())){
					/* Create a connection instance */
					Connection connServer = new Connection(server.getHostname());
					Connection connServer2 = new Connection(server.getHostname());

					/* Now connect */
					this.connectServer(50, connServer, server);
					this.connectServer(50, connServer2, server);
//					connServer.connect(null,0,0);
//					connServer2.connect(null,0,0);

					isAuthenticatedServer = connServer.authenticateWithPassword(server.getUsername(), server.getPassword());
					isAuthenticatedServer2 = connServer2.authenticateWithPassword(server.getUsername(), server.getPassword());

					if ((isAuthenticatedServer && isAuthenticatedServer2) == false){
						throw new IOException("Authentication failed on server "+server.getHostname()+".");

					}else{
//						Session sessServerUuid = connServer.openSession();
//						Session sessServerAdminState = connServer.openSession();
//						Session sessServerDuplex = connServer.openSession();
//						Session sessServerVmsMacAddress = connServer.openSession();
//						Session sessServerLinkSpeed = connServer.openSession();
//						Session sessServerLinkState = connServer.openSession();
//						Session sessServerMacAddress = connServer.openSession();
//						Session sessServerMtu = connServer.openSession();
//						Session sessServerName = connServer.openSession();
						Session sessServerOfport = connServer.openSession();
//						Session sessServerOfportRequest = connServer2.openSession();
//						Session sessServerRemoteIP = connServer2.openSession();
//						Session sessServerType = connServer2.openSession();
						Session sessServerHostName = connServer2.openSession();

//						String commandUuid = "sudo ovs-vsctl get interface "+ interfaces.getName() +" _uuid";
//						String commandAdminState = "sudo ovs-vsctl get interface "+ interfaces.getName() +" admin_state"; //admin_state
//						String commandDuplex = "sudo ovs-vsctl get interface "+ interfaces.getName() +" duplex"; //duplex
//						String commandVmsMacAddress = "sudo ovs-vsctl get interface "+ interfaces.getName() +" external_ids:attached-mac";
//						String commandLinkSpeed = "sudo ovs-vsctl get interface "+ interfaces.getName() +" link_speed"; // link_speed          : 10000000000
//						String commandLinkState = "sudo ovs-vsctl get interface "+ interfaces.getName() +" link_state"; // link_state          : up
//						String commandMacAddress = "sudo ovs-vsctl get interface "+ interfaces.getName() +" mac_in_use"; // mac_in_use          : "e6:e8:2f:93:25:53"
//						String commandMtu = "sudo ovs-vsctl get interface "+ interfaces.getName() +" mtu"; // mtu
//						String commandName = "sudo ovs-vsctl get interface "+ interfaces.getName() +" name"; // name                : "s1-eth3"
						String commandOfport = "sudo ovs-vsctl get interface "+ interfaces.getName() +" ofport"; // ofport
//						String commandOfportRequest = "sudo ovs-vsctl get interface "+ interfaces.getName() +" ofport_request"; // 	ofport_request      : 3
//						String commandRemoteIP = "sudo ovs-vsctl get interface "+ interfaces.getName() +" options:remote_ip"; // options             : {remote_ip="192.168.5.34"}
//						String commandType = "sudo ovs-vsctl get interface "+ interfaces.getName() +" type";
						String commandHostName = "sudo ovs-vsctl get interface "+ interfaces.getName() +" external_ids:container_id";

//						sessServerUuid.execCommand(commandUuid);
//						sessServerAdminState.execCommand(commandAdminState);
//						sessServerDuplex.execCommand(commandDuplex);
//						sessServerVmsMacAddress.execCommand(commandVmsMacAddress);
//						sessServerLinkSpeed.execCommand(commandLinkSpeed);
//						sessServerLinkState.execCommand(commandLinkState);
//						sessServerMacAddress.execCommand(commandMacAddress);
//						sessServerMtu.execCommand(commandMtu);
//						sessServerName.execCommand(commandName);
						sessServerOfport.execCommand(commandOfport);
//						sessServerOfportRequest.execCommand(commandOfportRequest);
//						sessServerRemoteIP.execCommand(commandRemoteIP);
//						sessServerType.execCommand(commandType);
						sessServerHostName.execCommand(commandHostName);

//						InputStream stdoutServerType = new StreamGobbler(sessServerType.getStdout());
//						BufferedReader brServerType = new BufferedReader(new InputStreamReader(stdoutServerType));
//						interfacesInfo = brServerType.readLine();

//						if(interfacesInfo != null){
//							if (interfacesInfo.equals(this.tunnelType)){
								/*
							//this.removeTunnel(connServer, this.BRIDGE_NAME_OF_VMS, interfaces2.getName());
							this.removeTunnel(connServer, server.getBridgeName(), interfaces.getName());

							this.interfacesGREToDelete.add(new InterfacesGREToDelete3(i, j, k, this.serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k)));
							//this.serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().remove(
							//	this.serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k));						
								 */
//							} else{
//
//								if (interfacesInfo.compareTo("\"\"")==0 ){
//
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setType("");
//
//								}else{
//
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setType(interfacesInfo);
//								}

//								InputStream stdoutServerUuid = new StreamGobbler(sessServerUuid.getStdout());
//								BufferedReader brServerUuid = new BufferedReader(new InputStreamReader(stdoutServerUuid));
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setUuid(UUID.fromString(brServerUuid.readLine()));
//								sessServerUuid.close();
//
//								InputStream stdoutServerAdminState = new StreamGobbler(sessServerAdminState.getStdout());
//								BufferedReader brServerAdminState = new BufferedReader(new InputStreamReader(stdoutServerAdminState));
//								interfacesInfo = brServerAdminState.readLine();
//								if (interfacesInfo.compareTo("up")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setAdminState(true);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setAdminState(false);
//								}
//								sessServerAdminState.close();
//
//								InputStream stdoutServerDuplex = new StreamGobbler(sessServerDuplex.getStdout());
//								BufferedReader brServerDuplex = new BufferedReader(new InputStreamReader(stdoutServerDuplex));
//								interfacesInfo = brServerDuplex.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setDuplex("");
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setDuplex(interfacesInfo);
//								}
//								sessServerDuplex.close();
//								
//
//								InputStream stdoutServerVmsMacAddress = new StreamGobbler(sessServerVmsMacAddress.getStdout());
//								BufferedReader brServerVmsMacAddress = new BufferedReader(new InputStreamReader(stdoutServerVmsMacAddress));
//								interfacesInfo = brServerVmsMacAddress.readLine();
//								if (interfacesInfo != null){
//									if (interfacesInfo.compareTo("{}")!=0){
//										this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//										get(k).setVmsMacAddress(MacAddress.of(interfacesInfo.replace("\"", "")));
//									}
//								}
//								sessServerVmsMacAddress.close();
//
//								InputStream stdoutServerLinkSpeed = new StreamGobbler(sessServerLinkSpeed.getStdout());
//								BufferedReader brServerLinkSpeed = new BufferedReader(new InputStreamReader(stdoutServerLinkSpeed));
//								interfacesInfo = brServerLinkSpeed.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setLinkSpeed(0);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setLinkSpeed(Long.parseLong(interfacesInfo));
//								}
//								sessServerLinkSpeed.close();
//
//								InputStream stdoutServerLinkState = new StreamGobbler(sessServerLinkState.getStdout());
//								BufferedReader brServerLinkState = new BufferedReader(new InputStreamReader(stdoutServerLinkState));
//								interfacesInfo = brServerLinkState.readLine();
//								if (interfacesInfo.compareTo("up")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setLinkState(true);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setLinkState(false);
//								}
//								sessServerLinkState.close();
//
//								InputStream stdoutServerMacAddress = new StreamGobbler(sessServerMacAddress.getStdout());
//								BufferedReader brServerMacAddress = new BufferedReader(new InputStreamReader(stdoutServerMacAddress));
//								interfacesInfo = brServerMacAddress.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setMacAddress(MacAddress.NONE);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setMacAddress(MacAddress.of(interfacesInfo.replace("\"", "")));
//								}
//								sessServerMacAddress.close();
//
//								InputStream stdoutServerMtu = new StreamGobbler(sessServerMtu.getStdout());
//								BufferedReader brServerMtu = new BufferedReader(new InputStreamReader(stdoutServerMtu));
//								interfacesInfo = brServerMtu.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setMtu(0);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setMtu(Integer.parseInt(interfacesInfo));
//								}
//								sessServerMtu.close();

								InputStream stdoutServerOfport = new StreamGobbler(sessServerOfport.getStdout());
								BufferedReader brServerOfport = new BufferedReader(new InputStreamReader(stdoutServerOfport));
								interfacesInfo = brServerOfport.readLine();
								//log.info("setInfoInterface -{}-", interfacesInfo);
								if (interfacesInfo.compareTo("[]")==0 ){
									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
									get(k).setOfport(OFPort.ZERO);
								} else {
									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
									get(k).setOfport(OFPort.of(Integer.parseInt(interfacesInfo)));

									port = OFPort.of(Integer.parseInt(interfacesInfo));
								}
								sessServerOfport.close();
								
//								InputStream stdoutServerOfportRequest = new StreamGobbler(sessServerOfportRequest.getStdout());
//								BufferedReader brServerOfportRequest = new BufferedReader(new InputStreamReader(stdoutServerOfportRequest));
//								interfacesInfo = brServerOfportRequest.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setOfportRequest("");
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setOfportRequest(interfacesInfo);
//								}
//								sessServerOfportRequest.close();
								
//								InputStream stdoutServerRemoteIP = new StreamGobbler(sessServerRemoteIP.getStdout());
//								BufferedReader brServerRemoteIP = new BufferedReader(new InputStreamReader(stdoutServerRemoteIP));
//								interfacesInfo = brServerRemoteIP.readLine();
//								if (interfacesInfo != null){
//									if (interfacesInfo.compareTo("{}")==0){
//										this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//										get(k).setRemoteIP("");
//									}else {
//										this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//										get(k).setRemoteIP(interfacesInfo.replace("\"", ""));
//									}
//								}
//								sessServerRemoteIP.close();
								

								InputStream stdoutServerHostName = new StreamGobbler(sessServerHostName.getStdout());
								BufferedReader brServerHostName = new BufferedReader(new InputStreamReader(stdoutServerHostName));
								interfacesInfo = brServerHostName.readLine();
								if (interfacesInfo != null){
									if (interfacesInfo.compareTo("{}")!=0){
										//Verificar 6fev17
										String hostName = interfacesInfo.replace("\"", "");
										if(!hostName.isEmpty() && port.getPortNumber() > 0){
											int indexHost = this.hostList.indexOf(this.hostNameHostMap.get(hostName));
											if(indexHost != -1){
												this.hostNameHostLocationMap.put(hostName, new HostLocation(datapathId, port));
												this.hostLocationHostMap.put(new HostLocation(datapathId, port), this.hostNameHostMap.get(hostName));
												this.hostList.get(indexHost).setHostLocation(new HostLocation(datapathId, port));	
											}
										}								
									}
								}
								sessServerHostName.close();

								/* Close the BufferedReader */
//								brServerUuid.close();
//								brServerAdminState.close();
//								brServerDuplex.close();
//								brServerVmsMacAddress.close();
//								brServerLinkSpeed.close();
//								brServerLinkState.close();
//								brServerMacAddress.close();
//								brServerMtu.close();
								brServerOfport.close();
//								brServerOfportRequest.close();
//								brServerRemoteIP.close();
								brServerHostName.close();

								/* Close this session */
//								sessServerUuid.close();
//								sessServerAdminState.close();
//								sessServerDuplex.close();
//								sessServerVmsMacAddress.close();
//								sessServerLinkSpeed.close();
//								sessServerLinkState.close();
//								sessServerMacAddress.close();
//								sessServerMtu.close();
//								sessServerName.close();
								sessServerOfport.close();
//								sessServerOfportRequest.close();
//								sessServerRemoteIP.close();
//								sessServerType.close();
								sessServerHostName.close();
//							}
						}
						/* Close the connection */
						connServer.close();
						connServer2.close();

						/* Close the BufferedReader */
//						brServerType.close();
//					}
				}
				if(server.getCloud().getDescription().toLowerCase().contains(((String)"imt").toLowerCase())){
					/* Create a connection instance */
					Connection connServer = new Connection(server.getHostname(), 2006);
					Connection connServer2 = new Connection(server.getHostname(), 2006);

					/* Now connect */
					this.connectServer(50, connServer, server);
					this.connectServer(50, connServer2, server);
//					connServer.connect(null,0,0);
//					connServer2.connect(null,0,0);

					isAuthenticatedServer = connServer.authenticateWithPassword(server.getUsername(), server.getPassword());
					isAuthenticatedServer2 = connServer2.authenticateWithPassword(server.getUsername(), server.getPassword());

					if ((isAuthenticatedServer && isAuthenticatedServer2) == false){
						throw new IOException("Authentication failed on server "+server.getHostname()+".");

					}else{
//						Session sessServerUuid = connServer.openSession();
//						Session sessServerAdminState = connServer.openSession();
//						Session sessServerDuplex = connServer.openSession();
//						Session sessServerVmsMacAddress = connServer.openSession();
//						Session sessServerLinkSpeed = connServer.openSession();
//						Session sessServerLinkState = connServer.openSession();
//						Session sessServerMacAddress = connServer.openSession();
//						Session sessServerMtu = connServer.openSession();
//						Session sessServerName = connServer.openSession();
						Session sessServerOfport = connServer.openSession();
//						Session sessServerOfportRequest = connServer2.openSession();
//						Session sessServerRemoteIP = connServer2.openSession();
//						Session sessServerType = connServer2.openSession();
						Session sessServerHostName = connServer2.openSession();

//						String commandUuid = "sudo ovs-vsctl get interface "+ interfaces.getName() +" _uuid";
//						String commandAdminState = "sudo ovs-vsctl get interface "+ interfaces.getName() +" admin_state"; //admin_state
//						String commandDuplex = "sudo ovs-vsctl get interface "+ interfaces.getName() +" duplex"; //duplex
//						String commandVmsMacAddress = "sudo ovs-vsctl get interface "+ interfaces.getName() +" external_ids:attached-mac";
//						String commandLinkSpeed = "sudo ovs-vsctl get interface "+ interfaces.getName() +" link_speed"; // link_speed          : 10000000000
//						String commandLinkState = "sudo ovs-vsctl get interface "+ interfaces.getName() +" link_state"; // link_state          : up
//						String commandMacAddress = "sudo ovs-vsctl get interface "+ interfaces.getName() +" mac_in_use"; // mac_in_use          : "e6:e8:2f:93:25:53"
//						String commandMtu = "sudo ovs-vsctl get interface "+ interfaces.getName() +" mtu"; // mtu
//						String commandName = "sudo ovs-vsctl get interface "+ interfaces.getName() +" name"; // name                : "s1-eth3"
						String commandOfport = "sudo ovs-vsctl get interface "+ interfaces.getName() +" ofport"; // ofport
//						String commandOfportRequest = "sudo ovs-vsctl get interface "+ interfaces.getName() +" ofport_request"; // 	ofport_request      : 3
//						String commandRemoteIP = "sudo ovs-vsctl get interface "+ interfaces.getName() +" options:remote_ip"; // options             : {remote_ip="192.168.5.34"}
//						String commandType = "sudo ovs-vsctl get interface "+ interfaces.getName() +" type";
						String commandHostName = "sudo ovs-vsctl get interface "+ interfaces.getName() +" external_ids:container_id";

//						sessServerUuid.execCommand(commandUuid);
//						sessServerAdminState.execCommand(commandAdminState);
//						sessServerDuplex.execCommand(commandDuplex);
//						sessServerVmsMacAddress.execCommand(commandVmsMacAddress);
//						sessServerLinkSpeed.execCommand(commandLinkSpeed);
//						sessServerLinkState.execCommand(commandLinkState);
//						sessServerMacAddress.execCommand(commandMacAddress);
//						sessServerMtu.execCommand(commandMtu);
//						sessServerName.execCommand(commandName);
						sessServerOfport.execCommand(commandOfport);
//						sessServerOfportRequest.execCommand(commandOfportRequest);
//						sessServerRemoteIP.execCommand(commandRemoteIP);
//						sessServerType.execCommand(commandType);
						sessServerHostName.execCommand(commandHostName);

//						InputStream stdoutServerType = new StreamGobbler(sessServerType.getStdout());
//						BufferedReader brServerType = new BufferedReader(new InputStreamReader(stdoutServerType));
//						interfacesInfo = brServerType.readLine();

//						if(interfacesInfo != null){
//							if (interfacesInfo.equals(this.tunnelType)){

								/*
							//this.removeTunnel(connServer, this.BRIDGE_NAME_OF_VMS, interfaces2.getName());
							this.removeTunnel(connServer, server.getBridgeName(), interfaces.getName());

							this.interfacesGREToDelete.add(new InterfacesGREToDelete3(i, j, k, this.serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k)));
							//this.serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().remove(
							//	this.serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k));						
								 */
//							} else{
//
//								if (interfacesInfo.compareTo("\"\"")==0 ){
//
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setType("");
//
//								}else{
//
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setType(interfacesInfo);
//								}

//								InputStream stdoutServerUuid = new StreamGobbler(sessServerUuid.getStdout());
//								BufferedReader brServerUuid = new BufferedReader(new InputStreamReader(stdoutServerUuid));
//								this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//								get(k).setUuid(UUID.fromString(brServerUuid.readLine()));
//								sessServerUuid.close();
//
//								InputStream stdoutServerAdminState = new StreamGobbler(sessServerAdminState.getStdout());
//								BufferedReader brServerAdminState = new BufferedReader(new InputStreamReader(stdoutServerAdminState));
//								interfacesInfo = brServerAdminState.readLine();
//								if (interfacesInfo.compareTo("up")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setAdminState(true);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setAdminState(false);
//								}
//								sessServerAdminState.close();
//
//								InputStream stdoutServerDuplex = new StreamGobbler(sessServerDuplex.getStdout());
//								BufferedReader brServerDuplex = new BufferedReader(new InputStreamReader(stdoutServerDuplex));
//								interfacesInfo = brServerDuplex.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setDuplex("");
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setDuplex(interfacesInfo);
//								}
//								sessServerDuplex.close();
//								
//
//								InputStream stdoutServerVmsMacAddress = new StreamGobbler(sessServerVmsMacAddress.getStdout());
//								BufferedReader brServerVmsMacAddress = new BufferedReader(new InputStreamReader(stdoutServerVmsMacAddress));
//								interfacesInfo = brServerVmsMacAddress.readLine();
//								if (interfacesInfo != null){
//									if (interfacesInfo.compareTo("{}")!=0){
//										this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//										get(k).setVmsMacAddress(MacAddress.of(interfacesInfo.replace("\"", "")));
//									}
//								}
//								sessServerVmsMacAddress.close();
//
//								InputStream stdoutServerLinkSpeed = new StreamGobbler(sessServerLinkSpeed.getStdout());
//								BufferedReader brServerLinkSpeed = new BufferedReader(new InputStreamReader(stdoutServerLinkSpeed));
//								interfacesInfo = brServerLinkSpeed.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setLinkSpeed(0);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setLinkSpeed(Long.parseLong(interfacesInfo));
//								}
//								sessServerLinkSpeed.close();
//
//								InputStream stdoutServerLinkState = new StreamGobbler(sessServerLinkState.getStdout());
//								BufferedReader brServerLinkState = new BufferedReader(new InputStreamReader(stdoutServerLinkState));
//								interfacesInfo = brServerLinkState.readLine();
//								if (interfacesInfo.compareTo("up")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setLinkState(true);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setLinkState(false);
//								}
//								sessServerLinkState.close();
//
//								InputStream stdoutServerMacAddress = new StreamGobbler(sessServerMacAddress.getStdout());
//								BufferedReader brServerMacAddress = new BufferedReader(new InputStreamReader(stdoutServerMacAddress));
//								interfacesInfo = brServerMacAddress.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setMacAddress(MacAddress.NONE);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setMacAddress(MacAddress.of(interfacesInfo.replace("\"", "")));
//								}
//								sessServerMacAddress.close();
//
//								InputStream stdoutServerMtu = new StreamGobbler(sessServerMtu.getStdout());
//								BufferedReader brServerMtu = new BufferedReader(new InputStreamReader(stdoutServerMtu));
//								interfacesInfo = brServerMtu.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setMtu(0);
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setMtu(Integer.parseInt(interfacesInfo));
//								}
//								sessServerMtu.close();

								InputStream stdoutServerOfport = new StreamGobbler(sessServerOfport.getStdout());
								BufferedReader brServerOfport = new BufferedReader(new InputStreamReader(stdoutServerOfport));
								interfacesInfo = brServerOfport.readLine();
								if (interfacesInfo.compareTo("[]")==0 ){
									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
									get(k).setOfport(OFPort.ZERO);
								} else {
									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
									get(k).setOfport(OFPort.of(Integer.parseInt(interfacesInfo)));

									port = OFPort.of(Integer.parseInt(interfacesInfo));
								}
								sessServerOfport.close();

//								InputStream stdoutServerOfportRequest = new StreamGobbler(sessServerOfportRequest.getStdout());
//								BufferedReader brServerOfportRequest = new BufferedReader(new InputStreamReader(stdoutServerOfportRequest));
//								interfacesInfo = brServerOfportRequest.readLine();
//								if (interfacesInfo.compareTo("[]")==0 ){
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setOfportRequest("");
//								} else {
//									this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//									get(k).setOfportRequest(interfacesInfo);
//								}
//								sessServerOfportRequest.close();
								
//								InputStream stdoutServerRemoteIP = new StreamGobbler(sessServerRemoteIP.getStdout());
//								BufferedReader brServerRemoteIP = new BufferedReader(new InputStreamReader(stdoutServerRemoteIP));
//								interfacesInfo = brServerRemoteIP.readLine();
//								if (interfacesInfo != null){
//									if (interfacesInfo.compareTo("{}")==0){
//										this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//										get(k).setRemoteIP("");
//									}else {
//										this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().
//										get(k).setRemoteIP(interfacesInfo.replace("\"", ""));
//									}
//								}
//								sessServerRemoteIP.close();
								
								InputStream stdoutServerHostName = new StreamGobbler(sessServerHostName.getStdout());
								BufferedReader brServerHostName = new BufferedReader(new InputStreamReader(stdoutServerHostName));
								interfacesInfo = brServerHostName.readLine();
								if (interfacesInfo != null){
									if (interfacesInfo.compareTo("{}")!=0){
										//Verificar 6fev17
										String hostName = interfacesInfo.replace("\"", "");
										if(!hostName.isEmpty() && port.getPortNumber() > 0){
											int indexHost = this.hostList.indexOf(this.hostNameHostMap.get(hostName));
											if(indexHost != -1){
												this.hostNameHostLocationMap.put(hostName, new HostLocation(datapathId, port));
												this.hostLocationHostMap.put(new HostLocation(datapathId, port), this.hostNameHostMap.get(hostName));
												this.hostList.get(indexHost).setHostLocation(new HostLocation(datapathId, port));	
											}
										}								
									}
								}
								sessServerHostName.close();

								/* Close the BufferedReader */
//								brServerUuid.close();
//								brServerAdminState.close();
//								brServerDuplex.close();
//								brServerVmsMacAddress.close();
//								brServerLinkSpeed.close();
//								brServerLinkState.close();
//								brServerMacAddress.close();
//								brServerMtu.close();
								brServerOfport.close();
//								brServerOfportRequest.close();
//								brServerRemoteIP.close();
								brServerHostName.close();

								/* Close this session */
//								sessServerUuid.close();
//								sessServerAdminState.close();
//								sessServerDuplex.close();
//								sessServerVmsMacAddress.close();
//								sessServerLinkSpeed.close();
//								sessServerLinkState.close();
//								sessServerMacAddress.close();
//								sessServerMtu.close();
//								sessServerName.close();
								sessServerOfport.close();
//								sessServerOfportRequest.close();
//								sessServerRemoteIP.close();
//								sessServerType.close();
								sessServerHostName.close();
//							}
						}
						/* Close the connection */
						connServer.close();
						connServer2.close();

						/* Close the BufferedReader */
//						brServerType.close();
//					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
		}
	}

	@SuppressWarnings("unused")
	private void removeTunnel(Connection connServer, String bridge_NAME_OF_VMS2, String name) {

		try {
			Session sessServerRemoveTunnel = connServer.openSession();

			String commandRemoveTunnel = "sudo ovs-vsctl del-port " + bridge_NAME_OF_VMS2 + " "+name;

			sessServerRemoveTunnel.execCommand(commandRemoveTunnel);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeTunnelLink(Server3 srcServer, Server3 dstServer){

		int srcServerIndex = this.getPhysicalNetwork().getVertexes().indexOf(srcServer);
		int dstServerIndex = this.getPhysicalNetwork().getVertexes().indexOf(dstServer);
		int srcBridgeServerIndex = -1;
		int dstBridgeServerIndex = -1;
		Interfaces3 interfaces = null;

		if(srcServerIndex >=0){
			try{
				//dstBridgeServerIndex = this.serverList.get(dstServerIndex).getOpenVSwitch().getArrayListBridge().indexOf(this.BRIDGE_NAME_OF_VMS);
				dstBridgeServerIndex = this.getPhysicalNetwork().getVertexes().get(dstServerIndex).getOpenVSwitch().getArrayListBridge().indexOf(dstServer.getBridgeName());
				if(dstBridgeServerIndex >= 0){
					Iterator<Interfaces3> iteratorInterfaces = this.getPhysicalNetwork().getVertexes().get(dstServerIndex).getOpenVSwitch().getArrayListBridge().
							get(dstBridgeServerIndex).getArrayListInterfaces().iterator();
					while(iteratorInterfaces.hasNext()){
						interfaces = iteratorInterfaces.next();
						if (interfaces.getRemoteIP().equals(srcServer.getHostname()) && interfaces.getType().equals(this.getTunnelType())){
							Connection connServer;

							//connServer.connect(null,25000,25000);
							//boolean isAuthenticatedServer = connServer.authenticateWithPassword(srcServer.getUsername(), srcServer.getPassword());
							boolean isAuthenticatedServer = false;

							if(srcServer.getCloud().getDescription().toLowerCase().contains(((String)"Amazon").toLowerCase())){
								connServer = new Connection(srcServer.getHostname());
								this.connectServer(50, connServer, srcServer);
								isAuthenticatedServer = connServer.authenticateWithPublicKey("ubuntu", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);
							} else {
								if(srcServer.getCloud().getDescription().toLowerCase().contains(((String)"Google").toLowerCase())){
									connServer = new Connection(srcServer.getHostname());
									this.connectServer(50, connServer, srcServer);
									isAuthenticatedServer = connServer.authenticateWithPublicKey("sirius", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);
								} 
								if(srcServer.getCloud().getDescription().toLowerCase().contains(((String)"FCUL").toLowerCase())){
									connServer = new Connection(srcServer.getHostname());
									this.connectServer(50, connServer, srcServer);
									isAuthenticatedServer = connServer.authenticateWithPassword(srcServer.getUsername(), srcServer.getPassword());
								}
								if(srcServer.getCloud().getDescription().toLowerCase().contains(((String)"imt").toLowerCase())){
									connServer = new Connection(srcServer.getHostname(), 2006);
									this.connectServer(50, connServer, srcServer);
									isAuthenticatedServer = connServer.authenticateWithPassword(srcServer.getUsername(), srcServer.getPassword());
								}
							}
							if (isAuthenticatedServer == false){
								throw new IOException("Authentication failed on server "+srcServer.getHostname()+".");
							} else{
								/*
								//this.removeTunnel(connServer, this.BRIDGE_NAME_OF_VMS, interfaces.getName());
								this.removeTunnel(connServer, srcServer.getBridgeName(), interfaces.getName());
								 */
							}								
						}			
					}				
				}
			}catch (Exception e)
			{
				e.printStackTrace(System.err);
			}
		}
		if(dstServerIndex >=0){
			try{
				//srcBridgeServerIndex = this.serverList.get(srcServerIndex).getOpenVSwitch().getArrayListBridge().indexOf(this.BRIDGE_NAME_OF_VMS);
				srcBridgeServerIndex = this.getPhysicalNetwork().getVertexes().get(srcServerIndex).getOpenVSwitch().getArrayListBridge().indexOf(srcServer.getBridgeName());
				if(srcBridgeServerIndex >= 0){
					Iterator<Interfaces3> iteratorInterfaces = this.getPhysicalNetwork().getVertexes().get(srcServerIndex).getOpenVSwitch().getArrayListBridge().
							get(srcBridgeServerIndex).getArrayListInterfaces().iterator();
					while(iteratorInterfaces.hasNext()){
						interfaces = iteratorInterfaces.next();
						if (interfaces.getRemoteIP().equals(dstServer.getHostname()) && interfaces.getType().equals(this.getTunnelType())){
							Connection connServer = null;
							//connServer.connect(null,25000,25000);
							//boolean isAuthenticatedServer = connServer.authenticateWithPassword(dstServer.getUsername(), dstServer.getPassword());
							boolean isAuthenticatedServer = false;
							if(srcServer.getCloud().getDescription().toLowerCase().contains(((String)"Amazon").toLowerCase())){
								connServer = new Connection(dstServer.getHostname());
								this.connectServer(50, connServer, srcServer);
								isAuthenticatedServer = connServer.authenticateWithPublicKey("ubuntu", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);
							} else {
								if(srcServer.getCloud().getDescription().toLowerCase().contains(((String)"Google").toLowerCase())){
									connServer = new Connection(dstServer.getHostname());
									this.connectServer(50, connServer, srcServer);
									isAuthenticatedServer = connServer.authenticateWithPublicKey("sirius", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);
								}
								if(srcServer.getCloud().getDescription().toLowerCase().contains(((String)"FCUL").toLowerCase())){

									connServer = new Connection(dstServer.getHostname());
									this.connectServer(50, connServer, srcServer);
									isAuthenticatedServer = connServer.authenticateWithPassword(dstServer.getUsername(), dstServer.getPassword());
								}
								if(srcServer.getCloud().getDescription().toLowerCase().contains(((String)"imt").toLowerCase())){
									
									connServer = new Connection(dstServer.getHostname(), 2006);
									this.connectServer(50, connServer, srcServer);
									isAuthenticatedServer = connServer.authenticateWithPassword(dstServer.getUsername(), dstServer.getPassword());
								}
							}
							if (isAuthenticatedServer == false){
								throw new IOException("Authentication failed on server "+dstServer.getHostname()+".");
							} else{
								/*
								//this.removeTunnel(connServer, this.BRIDGE_NAME_OF_VMS, interfaces.getName());
								this.removeTunnel(connServer, dstServer.getBridgeName(), interfaces.getName());
								 */
								connServer.close();
							}
						}			
					}				
				}
			}catch (Exception e)
			{
				e.printStackTrace(System.err);
				System.exit(2);
			}
		}
	}

	private String getDataPathId(Server3 server2) throws InterruptedException {

		String result = "";
		try{
			Connection connServer = null;
			boolean isAuthenticatedServer = false;
			String cloudName = server2.getCloud().getDescription().toLowerCase();

			switch (cloudName) {
			case "amazon":
				connServer = new Connection(server2.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer = connServer.authenticateWithPublicKey("ubuntu", 
						new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);
                break;
			case "google":
				connServer = new Connection(server2.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer = connServer.authenticateWithPublicKey("sirius", 
						new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);
				break;
			case "fcul":
				connServer = new Connection(server2.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				break;
			case "imt":
				connServer = new Connection(server2.getHostname(),2006);
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				break;
			}

			if (isAuthenticatedServer == false){
				throw new IOException("Authentication failed on server "+server2.getHostname()+".");
			}
			if (isAuthenticatedServer){
				Session sessServer = connServer.openSession();
				String command = "sudo ovs-vsctl get bridge "+ server2.getBridgeName() +" datapath_id";
				sessServer.execCommand(command);
				InputStream stdoutServer = new StreamGobbler(sessServer.getStdout());
				BufferedReader brServer = new BufferedReader(new InputStreamReader(stdoutServer));
				while (true){
					String line = brServer.readLine();
					if (line == null)
						break;
					result += line.replace("\"", "") ;
				}
				sessServer.close();
				connServer.close();
				brServer.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
		return result;
	}

	private String getInfoOVS(Server3 server2) throws InterruptedException {

		String result = "";
		try{
			Connection connServer = null;
			boolean isAuthenticatedServer = false;
			String cloudName = server2.getCloud().getDescription().toLowerCase();

			switch (cloudName) {
			case "amazon":
				connServer = new Connection(server2.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer = connServer.authenticateWithPublicKey("ubuntu", 
						new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);
                break;
			case "google":
				connServer = new Connection(server2.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer = connServer.authenticateWithPublicKey("sirius", 
						new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);
				break;
			case "fcul":
				connServer = new Connection(server2.getHostname());
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				break;
			case "imt":
				connServer = new Connection(server2.getHostname(),2006);
				EnvironmentOfServices3.connectServer(50, connServer, server2);
				isAuthenticatedServer = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				break;
			}

			if (isAuthenticatedServer == false){
				throw new IOException("Authentication failed on server "+server2.getHostname()+".");
			}
			if (isAuthenticatedServer){
				Session sessServer = connServer.openSession();
				String command = "sudo ovs-vsctl show";
				sessServer.execCommand(command);
				InputStream stdoutServer = new StreamGobbler(sessServer.getStdout());
				BufferedReader brServer = new BufferedReader(new InputStreamReader(stdoutServer));
				while (true){
					String line = brServer.readLine();
					result += line + "\n";
					if (line == null)
						break;
				}
				sessServer.close();
				connServer.close();
				brServer.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
		return result;
	}

	protected static String getInfoInterface(Server3 server2, Interfaces3 interfaces2) throws InterruptedException {

		String result = "";

		try{
			/* Create a connection instance */
			Connection connServer = null;
			/* Now connect */
//			connServer.connect(null,25000,25000);
			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			//boolean isAuthenticatedServer = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());

			boolean isAuthenticatedServer = false;
			if(server2.getCloud().getDescription().toLowerCase().contains(((String)"Amazon").toLowerCase())){

				connServer = new Connection(server2.getHostname());
				connectServer(50, connServer, server2);
				isAuthenticatedServer = connServer.authenticateWithPublicKey("ubuntu", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);

			} else {
				if(server2.getCloud().getDescription().toLowerCase().contains(((String)"Google").toLowerCase())){
					connServer = new Connection(server2.getHostname());
					connectServer(50, connServer, server2);
					isAuthenticatedServer = connServer.authenticateWithPublicKey("sirius", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/google.pem"), null);
				}
				if(server2.getCloud().getDescription().toLowerCase().contains(((String)"FCUL").toLowerCase())){
					connServer = new Connection(server2.getHostname());
					connectServer(50, connServer, server2);
					isAuthenticatedServer = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				}
				if(server2.getCloud().getDescription().toLowerCase().contains(((String)"imt").toLowerCase())){
					connServer = new Connection(server2.getHostname(), 2006);
					connectServer(50, connServer, server2);
					isAuthenticatedServer = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());
				}
			}

			if (isAuthenticatedServer == false){
				throw new IOException("Authentication failed on server "+server2.getHostname()+".");
			}

			if (isAuthenticatedServer){
				/* Create a session */
				Session sessServer = connServer.openSession();
				//Improve this part verifing the correct execution of the command add-port
				/*Create commands*/
				String command = "sudo ovs-vsctl list interface "+ interfaces2.getName();
				sessServer.execCommand(command);
				/* 
				 * This basic example does not handle stderr, which is sometimes dangerous
				 * (please read the FAQ).
				 */
				InputStream stdoutServer = new StreamGobbler(sessServer.getStdout());
				BufferedReader brServer = new BufferedReader(new InputStreamReader(stdoutServer));
				while (true){
					String line = brServer.readLine();
					result += line + "\n";
					if (line == null)
						break;
				}
				/* Show exit status, if available (otherwise "null") */
				//result = "ExitCode on server "+server.getHostname()+" : " + sessServer.getExitStatus()+"\n";
				/* Close this session */
				sessServer.close();
				/* Close the connection */
				connServer.close();
				/* Close the BufferedReader */
				brServer.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
		return result;
	}

	public Server3 setIOFSwitchGetServer(IOFSwitch sw) {

		if(sw != null){
			if(this.getPhysicalNetwork() != null){
				if(this.getPhysicalNetwork().getVertexes() != null){
					for (int i = 0; i < this.getPhysicalNetwork().getVertexes().size(); i++){
						if(this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch() != null){
							if (this.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getDatapathId().equals((sw.getId()))){
								this.getPhysicalNetwork().getVertexes().get(i).setSw(sw);
								return this.getPhysicalNetwork().getVertexes().get(i);
							}	
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Procedure responsible for initing the structures to be used in the hypervisor, create the GRE tunnels
	 * @param fileChanged 
	 * @param this.environmentOfServices.getDatapathIdIOFSwitchMap() 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 **/

	public synchronized void initEnvironmentOfServers(String fileChanged, boolean realTimeInfoContainersInput) throws ParserConfigurationException, SAXException, Exception{

		this.realTimeInfoContainers = realTimeInfoContainersInput;
		if(physicalNodes==null){
			//Thread.sleep (1000);
			physicalNodes = this.createPhysicalNetwork(fileChanged);
			this.createPhysicalNodes(physicalNodes);
			for(int i = 0; i < this.getPhysicalNetwork().getVertexes().size(); i ++){
				this.setController(this.getPhysicalNetwork().getVertexes().get(i), this.controller);
			}
			log.info("Populating Environment of Servers...");
			this.populateEnvironment(realTimeInfoContainers, null);
			log.info("Populating Environment of Servers finished!!!");
		}
	}
	
	public synchronized void initEnvironmentOfServers(Network substrate, boolean realTimeInfoContainersInput, SiriusNetHypervisor siriusNetHypervisor) throws ParserConfigurationException, SAXException, Exception{

		this.realTimeInfoContainers = realTimeInfoContainersInput;
		if(physicalNodes==null){
			physicalNodes = substrate;
			this.createPhysicalNodes(physicalNodes);
			for(int i = 0; i < this.getPhysicalNetwork().getVertexes().size(); i ++){
				this.setController(this.getPhysicalNetwork().getVertexes().get(i), this.controller);
			}
			log.info("Populating Environment of Servers...");
			this.populateEnvironment(realTimeInfoContainers, siriusNetHypervisor);
			log.info("Populating Environment of Servers finished!!!");
		}
	}
	private void createPhysicalNodes(Network physicalNodes) {

		log.info("Start Physical Network creation...");
		if (this.serverIdServerMap != null){
			this.serverIdServerMap.clear();
		}
		if (this.hostIdHostMap != null){
			this.hostIdHostMap.clear();
			this.hostNameHostMap.clear();
		}
		for (int i = 0; i < physicalNodes.getClouds().size(); i++){
			this.getCloudList().add(new Cloud(physicalNodes.getClouds().get(i).getUsername(), physicalNodes.getClouds().get(i).getCredential(), physicalNodes.getClouds().get(i).getProvider(), 
					CloudType.valueOf(physicalNodes.getClouds().get(i).getSecurityLevel()), physicalNodes.getClouds().get(i).getId()));
		}
		for (int i = 0; i < physicalNodes.getNodes().size(); i++){
			if(physicalNodes.getNodes().get(i) instanceof net.floodlightcontroller.sirius.console.Host){
				String mac = (((net.floodlightcontroller.sirius.console.Host)physicalNodes.getNodes().get(i)).getMac()==null)?"00:00:00:00:00:00" : ((net.floodlightcontroller.sirius.console.Host)physicalNodes.getNodes().get(i)).getMac();
				String ip = (((net.floodlightcontroller.sirius.console.Host)physicalNodes.getNodes().get(i)).getIp()==null)?"0.0.0.0" : ((net.floodlightcontroller.sirius.console.Host)physicalNodes.getNodes().get(i)).getIp();
				String hostName = ((net.floodlightcontroller.sirius.console.Host)physicalNodes.getNodes().get(i)).getName();
				int hostId = ((net.floodlightcontroller.sirius.console.Host)physicalNodes.getNodes().get(i)).getId();
				Host h = new Host(hostId, MacAddress.of(mac), VlanVid.ofVlan(0), IPv4Address.of(ip), hostName, -1);
				this.getHostList().add(h);
				if(this.hostIdHostMap.get(hostId) == null){
					this.hostIdHostMap.put(hostId, h);
					this.hostNameHostMap.put(hostName, h);
				}
			}
			if(physicalNodes.getNodes().get(i) instanceof Controller){
				String hostName = ((Controller)physicalNodes.getNodes().get(i)).getIp();
				String serverUserName = "max";//((net.floodlightcontroller.sirius.topology.xml.Switch)node).getUserName();
				String serverPassword = "max";//((net.floodlightcontroller.sirius.topology.xml.Switch)node).getIp();
				String serverDescription = ((Controller)physicalNodes.getNodes().get(i)).getName() + " - " + 
										((Controller)physicalNodes.getNodes().get(i)).getIp();
				int linkSpeed = 1000;//((net.floodlightcontroller.sirius.topology.xml.Switch)node).getBand(index);
				int cloudId = ((Controller)physicalNodes.getNodes().get(i)).getVm().getCloud().getId();
				int cpu = 100;//((Controller)physicalNodes.getNodes().get(i)).getCpu();
				int securityLevel = 2;//((Controller)physicalNodes.getNodes().get(i)).getSecurity();
				controller = new Server3 (null, hostName, serverUserName, serverPassword, serverDescription, linkSpeed,"", cloudId, SecurityLevelPhysicalNode.valueOf(securityLevel), cpu, -1);
			}
			if(physicalNodes.getNodes().get(i) instanceof Switch){
				String hostName = physicalNodes.getNodes().get(i).getVm().getPublicIp();
				//Insert the users and the passwords
				String serverUserName = "user";
				String serverPassword = "password";
				String serverUserNameIMT = "user";
				String serverPasswordIMT = "password";
				String serverDescription = physicalNodes.getNodes().get(i).getName() + " - " + 
						physicalNodes.getNodes().get(i).getVm().getLocation()+ " - " + physicalNodes.getNodes().get(i).getVm().getPrivateIp()
						+ " - " + physicalNodes.getNodes().get(i).getVm().getPublicIp();
				int linkSpeed = 100000;//((net.floodlightcontroller.sirius.topology.xml.Switch)node).getBand(index);
				String bridgeName = "br1";//((Switch)physicalNodes.getNodes().get(i)).getBridgeName();
				int cloudId = physicalNodes.getNodes().get(i).getVm().getCloud().getId();
				int cpu = ((Switch)physicalNodes.getNodes().get(i)).getCpu();
				int maxFlowSize = ((Switch)physicalNodes.getNodes().get(i)).getMaxFlowSize();
				maxFlowSize = 0;
				int securityLevel = ((Switch)physicalNodes.getNodes().get(i)).getSecurityLevel();
				int serverId = physicalNodes.getNodes().get(i).getId();
				String letter = Utils.convertToAlphabet(""+this.getServerList().size());
				String privateIP = physicalNodes.getNodes().get(i).getVm().getPrivateIp();
				Server3 s;
				
				if(physicalNodes.getNodes().get(i).getVm().getCloud().getName().equals("imt")){
					s = new Server3(null, serverId, letter, hostName, serverUserNameIMT, serverPasswordIMT, serverDescription, 
							linkSpeed, bridgeName, cloudId, SecurityLevelPhysicalNode.valueOf(securityLevel), cpu, maxFlowSize, privateIP);
				}else{
					s = new Server3(null, serverId, letter, hostName, serverUserName, serverPassword, serverDescription, 
							linkSpeed, bridgeName, cloudId, SecurityLevelPhysicalNode.valueOf(securityLevel), cpu, maxFlowSize, privateIP);
				}
				if(!this.getServerList().contains(s)){
					this.getServerList().add(s);
					this.serverIdServerMap.put(Integer.valueOf(serverId), s);
					this.letterServerIdToEmbeddingServerId.put(letter, Integer.valueOf(serverId));
					this.serverIdletterServerIdToEmbedding.put(Integer.valueOf(serverId), letter);
				}
			}
		}
		//Insert clouds objects in the servers
		for(int i = 0; i < this.getServerList().size(); i++){
			for(int j = 0; j< this.getCloudList().size(); j++){
				if (this.getServerList().get(i).getCloudId() == this.getCloudList().get(j).getCloudId()){
					this.getServerList().get(i).setCloud(this.getCloudList().get(j));
				}
			}
		}
		for (int i = 0; i < physicalNodes.getLinks().size(); i++){
			Link link = physicalNodes.getLinks().get(i);
			EdgeSC lane;
			if(link.isBetweenSwitches()){
				Server3 server1 = this.serverIdServerMap.get(link.getFrom().getId());
				Server3 server3 = this.serverIdServerMap.get(link.getTo().getId());
				lane = new EdgeSC(link.getId(), "Link-"+server1.getDescription()+"_"+server1.getHostname()+"-"
						+server3.getDescription()+"_"+server3.getHostname(),server1, server3, link.getBandwidth(), 1 , SecurityLevelLinks.valueOf(link.getSecurityLevel()), true);
				this.edgeSCApps.add(lane);
				this.linkLinkId.put("("+server1.getServerId()+","+server3.getServerId()+")", link.getId());
			}else{
				Server3 server = null;
				Host host = null;
				if(this.serverIdServerMap.get(link.getFrom().getId()) != null && this.hostIdHostMap.get(link.getTo().getId()) != null){
					server = this.serverIdServerMap.get(link.getFrom().getId());
					host = this.hostIdHostMap.get(link.getTo().getId());
				} else {
					if(this.serverIdServerMap.get(link.getTo().getId()) != null && this.hostIdHostMap.get(link.getFrom().getId()) !=null){
						server = this.serverIdServerMap.get(link.getTo().getId());
						host = this.hostIdHostMap.get(link.getFrom().getId());
					}
				}
				if (server != null && host != null){
					lane = new EdgeSC(link.getId(), "Link-"+server.getDescription()+"_"+server.getHostname()+"-"
							+host.getHostName()+"_"+host.getHostId(),server, host, link.getBandwidth(), 1 , SecurityLevelLinks.valueOf(link.getSecurityLevel()), false);
					this.edgeSCApps.add(lane);
					this.linkLinkId.put("("+host.getHostId()+","+server.getServerId()+")", link.getId());
				}
			}
		}
		this.physicalNetwork = new PhysicalNetwork(this.hostList, this.serverList, this.edgeSCApps);
		this.createPhysicalTopologyFileEmbeddingMILP(this.physicalNetwork);
		this.createPhysicalTopologyFileEmbeddingHeu(physicalNodes);
		log.info("Physical Network creation finished!");
	}
	
	private void createPhysicalTopologyFileEmbeddingHeu(Network physicalNodes) {
		SubstrateNetworkHeu subNet = new SubstrateNetworkHeu(physicalNodes.getClouds().size());
		
		for (int i = 0; i < physicalNodes.getNodes().size(); i++){
			if(physicalNodes.getNodes().get(i) instanceof Switch){
				subNet.addNode(this.serverIdletterServerIdToEmbedding.get(Integer.valueOf(physicalNodes.getNodes().get(i).getId())));
				subNet.addNodeCPU(((Switch)physicalNodes.getNodes().get(i)).getCpu());
				subNet.addNodeSec(((Switch)physicalNodes.getNodes().get(i)).getSecurityLevel());
				subNet.addCloudSecurity(physicalNodes.getNodes().get(i).getVm().getCloud().getSecurityLevel());
				subNet.addAcceptNodeEmbedding(!((Node)physicalNodes.getNodes().get(i)).getVm().isFabric());
			}
		}
		int[][] doesItBelong = new int[physicalNodes.getClouds().size()][subNet.getNodes().size()];
		for (int i = 0; i < physicalNodes.getClouds().size(); i++){
			for(int j = 0; j < subNet.getNodes().size(); j++){
				int a = (this.serverIdServerMap.get(this.letterServerIdToEmbeddingServerId.get(subNet.getNodes().get(i)))).getCloudId();
				if(((this.serverIdServerMap.get(this.letterServerIdToEmbeddingServerId.get(subNet.getNodes().get(j)))).getCloudId() - 1) == i)
					doesItBelong[i][j] = 1;
			}
		}
		subNet.setDoesItBelong(doesItBelong);
		for (int i = 0; i < physicalNodes.getLinks().size(); i++){
			Link link = physicalNodes.getLinks().get(i);
			if(link.isBetweenSwitches()){
				subNet.addEdge(new Pair<String>(this.serverIdServerMap.get(link.getFrom().getId()).getLetterServerIdToEmbedding(), 
					this.serverIdServerMap.get(link.getTo().getId()).getLetterServerIdToEmbedding()));
				subNet.addEdgeBw(link.getBandwidth());
				System.out.println(this.serverIdServerMap.get(link.getFrom().getId()).getLetterServerIdToEmbedding()+"-"+
						this.serverIdServerMap.get(link.getTo().getId()).getLetterServerIdToEmbedding()+"link.getBandwidth(): "+link.getBandwidth());
				subNet.addEdgeSec(link.getSecurityLevel());
				subNet.addEdgeLatency(1);
				subNet.addEdgeWeight(1);
			}
		}
		subNet.setnPrivateDataCenters(0);
		subNet.cloudSecSup();
		this.setSubstrateNetworkEmbeddingHeu(subNet);
	}

	private void createPhysicalTopologyFileEmbeddingMILP(PhysicalNetwork physicalNetwork) {

		FileWriter configEmbeddingFile;
		String substratePart = "Substrate\n\n";

		int NumNodes = 0;
		int NumLinks = 0;
		int NumClouds = 0;
		ArrayList<String> NodesID = new ArrayList<String>();
		ArrayList<String> NodesCPU = new ArrayList<String>();
		ArrayList<String> NodesSec = new ArrayList<String>();
		ArrayList<String> NodesLoc = new ArrayList<String>();
		ArrayList<String> Links = new ArrayList<String>();
		ArrayList<String> LinksBw = new ArrayList<String>();
		ArrayList<String> LinksSec = new ArrayList<String>(); 
		ArrayList<String> LinksWeight = new ArrayList<String>(); 
		ArrayList<String> CloudsID = new ArrayList<String>();
		ArrayList<String> CloudSec = new ArrayList<String>();

		//Info to create the physical topology to be used in the embedding
		if (physicalNetwork != null){
			if(physicalNetwork.getVertexes() != null){
				NumNodes = physicalNetwork.getVertexes().size();
				for (int i=0; i < physicalNetwork.getVertexes().size(); i++){
					NodesID.add(physicalNetwork.getVertexes().get(i).getLetterServerIdToEmbedding()+" ");
					NodesCPU.add(physicalNetwork.getVertexes().get(i).getCpu()+" ");
					NodesSec.add(physicalNetwork.getVertexes().get(i).getSecurityLevel().getValue()+" ");
					NodesLoc.add(physicalNetwork.getVertexes().get(i).getCloudId()+" ");
				}
			}

			if(physicalNetwork.getEdgeSCApps() != null){
				for (int i=0; i < physicalNetwork.getEdgeSCApps().size(); i++){
					if (physicalNetwork.getEdgeSCApps().get(i).isBetweenSwitches()){
						Links.add("("+physicalNetwork.getEdgeSCApps().get(i).getSource().getLetterServerIdToEmbedding()+","+physicalNetwork.getEdgeSCApps().get(i).getDestination().getLetterServerIdToEmbedding()+") ");
						LinksBw.add(physicalNetwork.getEdgeSCApps().get(i).getBandwidth()+" ");
						LinksSec.add(physicalNetwork.getEdgeSCApps().get(i).getSecurityLevel().getValue()+" ");
						LinksWeight.add(physicalNetwork.getEdgeSCApps().get(i).getWeight()+" ");
						NumLinks = NumLinks + 1;
					}
				}
			}
			if(this.getCloudList() != null){
				NumClouds = this.getCloudList().size();
				for (int i=0; i < this.getCloudList().size(); i++){
					CloudsID.add(this.getCloudList().get(i).getCloudId()+" ");
					CloudSec.add(this.getCloudList().get(i).getCloudType().getValue()+" ");
				}
			}
			//Substrate part creation
			String sAux = "";
			substratePart = substratePart.concat("NumNodes: "+ NumNodes +"\r\n");
			substratePart = substratePart.concat("NumLinks: "+ NumLinks +"\r\n");
			substratePart = substratePart.concat("NumClouds: "+ NumClouds +"\r\n");
			sAux = ((NodesID.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("NodesID: "+ sAux +"\r\n");
			sAux = ((NodesCPU.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("NodesCPU: "+ sAux +"\r\n");
			sAux = ((NodesSec.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("NodesSec: "+ sAux +"\r\n");
			sAux = ((NodesLoc.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("NodesLoc: "+ sAux +"\r\n");
			sAux = ((Links.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("Links: "+ sAux +"\r\n");
			sAux = ((LinksBw.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("LinksBw: "+ sAux +"\r\n");
			sAux = ((LinksSec.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("LinksSec: "+ sAux +"\r\n");
			sAux = ((LinksWeight.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("LinksWeight: "+ sAux +"\r\n");
			sAux = ((CloudsID.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("CloudsID: "+ sAux +"\r\n");
			sAux = ((CloudSec.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			substratePart = substratePart.concat("CloudSec: "+ sAux +"\r\n");
			substratePart = substratePart.concat("################################################################################\r\n");
			log.info("Substrate part creation.");
			System.out.print(substratePart);
			//Save the file
			try {
				configEmbeddingFile = new FileWriter(new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/secdepvne/topology/physical/physicalNetwork"+this.getPhysicalNetwork().hashCode()+".config"));
				configEmbeddingFile.write(substratePart);
				configEmbeddingFile.close();
				this.getPhysicalNetwork().setSubstratePartFileEmbedding(substratePart);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	//	private Network createPhysicalNetwork(String fileChanged) throws FileNotFoundException{
	//
	//		Config config = new Config();
	//		Network physicalNodesBase = new Network();
	//		InputStream is = new FileInputStream("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/topology/xml/config.properties");
	//		config.readProperties(is);
	//
	//		is = new FileInputStream(fileChanged);
	//		Document doc = Config.loadDocument(is);
	//
	//		// Read physical network information
	//		physicalNodesBase.readDocument(config, doc);
	//		log.info("createPhysicalNetwork finished.");
	//
	//		return physicalNodesBase;
	//
	//	}

	private Network createPhysicalNetwork(String fileChanged) throws ParserConfigurationException, SAXException, Exception{
		Network physicalNodesBase = new Network();
		InputStream is = new FileInputStream(fileChanged);
		physicalNodesBase.readDocument(Config.loadDocument(is));
		log.info("PhysicalNetwork created.");

		return physicalNodesBase;
	}
	@SuppressWarnings("unused")
	private void populateEnvironmentCbench(int numberOfSwitches, int numberOfMACperSwitch) {

		ArrayList<Controller3> controllerList =  new ArrayList<Controller3>();
		controllerList.add(new Controller3 ("tcp", "192.168.4.202","6653",true,""));
		ArrayList<Bridge3> bridgeList = new ArrayList<Bridge3>();
		ArrayList<Interfaces3> interfacesList = new ArrayList<Interfaces3>();

		for (int i=0; i<numberOfSwitches; i++){
			bridgeList.add(new Bridge3("xenbr0"));
			for (int j=0; j<numberOfMACperSwitch; j++){
				interfacesList.add(new Interfaces3("cb-src"+j, MacAddress.of(createMacAddress(i+1, j, true)), OFPort.of(1)));				
			}

			interfacesList.add(new Interfaces3("cb-dst",MacAddress.of(createMacAddress(i+1, i+1, false)), OFPort.of(2)));
			bridgeList.get(0).setArrayListInterfaces(interfacesList);
			interfacesList = new ArrayList<Interfaces3>();
			this.getPhysicalNetwork().getVertexes().add(new Server3(null, "192.168.1.40","","","Teste cbench",1000,"xenbr0", privateCloud, SecurityLevelPhysicalNode.SECUREVM1, 100, 1024));
			this.getPhysicalNetwork().getVertexes().get(i).setOpenVSwitch(new OpenVSwitch3(UUID.fromString("8945dad2-35b4-4549-9122-23d5558992e7"), 
					controllerList, bridgeList, "2.2.1", DatapathId.of(this.createDatapathId(i+1))));
			bridgeList = new ArrayList<Bridge3>();
		}
	}

	public String createMacAddress(int numberOfSwitch, int numberOfMAC, boolean isSourceMacAddress) {

		String macAddress = "";
		String  valorInicialSw= Long.toString(numberOfSwitch, 16);
		String  valorInicialMac= Long.toString(numberOfMAC, 16);
		StringBuilder stringBuilder = new StringBuilder(valorInicialSw);

		if(isSourceMacAddress){
			for (int i=0; i < 8 -valorInicialSw.length(); i++){
				stringBuilder.insert(0, '0');	
			}
			stringBuilder.insert(0, valorInicialMac);
			for (int i=0; i < 4 -valorInicialMac.length(); i++){
				stringBuilder.insert(0, '0');	
			}
		} else{
			for (int i=0; i < 12 - valorInicialSw.length(); i++){
				if(i != (11 - valorInicialSw.length())){
					stringBuilder.insert(0, '0');
				}else{
					stringBuilder.insert(0, '8');
				}
			}
		}
		StringBuilder stringBuilderMac = new StringBuilder(stringBuilder.toString());
		for (int i=0; i < stringBuilder.toString().length()/2 -1; i++){
			stringBuilderMac.insert(stringBuilder.toString().length() - (2 + 2*i), ':');
		}
		macAddress = stringBuilderMac.toString();
		return macAddress;
	}

	public String createDatapathId(int numberOfSwitch){

		String dataPathId = "";
		String  valorInicial= Long.toString(numberOfSwitch, 16);
		StringBuilder stringBuilder = new StringBuilder(valorInicial);
		for (int i=0; i < 16 -valorInicial.length(); i++){
			stringBuilder.insert(0, '0');
		}
		StringBuilder stringBuilderDP = new StringBuilder(stringBuilder.toString());
		for (int i=0; i < stringBuilder.toString().length()/2 -1; i++){
			stringBuilderDP.insert(stringBuilder.toString().length() - (2 + 2*i), ':');
		}
		dataPathId = stringBuilderDP.toString();
		return dataPathId;
	}

	//	public MacAddress get2MacOfVM(String name, Server3 server) {
	//
	//		try{
	//			/* Create a connection instance */
	//			Connection connServer = new Connection(server.getHostname());
	//			/* Now connect */
	//			connServer.connect(null,25000,25000)
	//			/* Authenticate.
	//			 * If you get an IOException saying something like
	//			 * "Authentication method password not supported by the server at this stage."
	//			 * then please check the FAQ.
	//			 */
	//			boolean isAuthenticatedServer = connServer.authenticateWithPassword(server.getUsername(), server.getPassword());
	//			if (isAuthenticatedServer == false){
	//				throw new IOException("Authentication failed on server "+server.getHostname()+".");
	//			}
	//			if (isAuthenticatedServer){
	//				Session sessServerVmsMacAddress = connServer.openSession();
	//				String commandVmsMacAddress = "sudo ovs-vsctl get interface "+ name +" external_ids:attached-mac";
	//				sessServerVmsMacAddress.execCommand(commandVmsMacAddress);
	//				InputStream stdoutServerVmsMacAddress = new StreamGobbler(sessServerVmsMacAddress.getStdout());
	//				BufferedReader brServerVmsMacAddress = new BufferedReader(new InputStreamReader(stdoutServerVmsMacAddress));
	//				String interfacesInfo = brServerVmsMacAddress.readLine();
	//				if (interfacesInfo != null){
	//					if (interfacesInfo.compareTo("{}")!=0){
	//						brServerVmsMacAddress.close();
	//						sessServerVmsMacAddress.close();
	//						connServer.close();
	//						return MacAddress.of(interfacesInfo.replace("\"", ""));
	//					}
	//				}
	//				brServerVmsMacAddress.close();
	//				sessServerVmsMacAddress.close();
	//				connServer.close();
	//			}
	//		}catch (IOException e)
	//		{
	//			e.printStackTrace(System.err);
	//			System.exit(2);
	//		}
	//		return null;
	//	}

	public PhysicalNetwork getPhysicalNetwork() {
		return physicalNetwork;
	}

	public void setPhysicalNetwork(PhysicalNetwork physicalNetwork) {
		this.physicalNetwork = physicalNetwork;
	}
	public Map<Integer,String> getServerIdletterServerIdToEmbedding() {
		return serverIdletterServerIdToEmbedding;
	}
	public void setServerIdletterServerIdToEmbedding(
			Map<Integer,String> serverIdletterServerIdToEmbedding) {
		this.serverIdletterServerIdToEmbedding = serverIdletterServerIdToEmbedding;
	}
	public Map<String, Integer> getLetterServerIdToEmbeddingServerId() {
		return letterServerIdToEmbeddingServerId;
	}
	public void setLetterServerIdToEmbeddingServerId(
			Map<String, Integer> letterServerIdToEmbeddingServerId) {
		this.letterServerIdToEmbeddingServerId = letterServerIdToEmbeddingServerId;
	}
	public Map<String, Integer> getLinkLinkId() {
		return linkLinkId;
	}
	public void setLinkLinkId(Map<String, Integer> linkLinkId) {
		this.linkLinkId = linkLinkId;
	}
	public Map<String, Host> getHostNameHostMap() {
		return hostNameHostMap;
	}
	public void setHostNameHostMap(Map<String, Host> hostNameHostMap) {
		this.hostNameHostMap = hostNameHostMap;
	}

	public HostLocation APIMininetInfoHostLocation(String hostName, Node node, Network physical){

		String dpid = "";
		int port = 0;
		//		if (node.getLinks().size() > 0) {
		//			Node peer = node.getPeer(physical, 0);
		//			dpid = ((Switch)peer).getDpid();
		//			port = node.getPort(physical, 0) + 1;
		//			System.out.println(" connected to " + peer.getName() 
		//					+ " dpid = " + dpid + " on port " + port);
		//		}
		if(!dpid.equals("") && port != 0){
			return new HostLocation(DatapathId.of(dpid), OFPort.of(port));
		}
		return null;
	}

	public HostLocation emulate2APIMininetInfoHostLocation(String hostName){

		if (hostName != null){
			if (hostName.equals("h1")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(1));
			}
			if (hostName.equals("h2")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(2));
			}
			if (hostName.equals("h3")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(3));
			}
			if (hostName.equals("h4")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(4));
			}
			if (hostName.equals("h5")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(5));
			}
			if (hostName.equals("h6")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(6));
			}
			if (hostName.equals("h7")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(1));
			}
			if (hostName.equals("h8")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(2));
			}
			if (hostName.equals("h9")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(3));
			}
			if (hostName.equals("h10")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(4));
			}
			if (hostName.equals("h11")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(5));
			}
			if (hostName.equals("h12")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(6));
			}
			if (hostName.equals("h13")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:03"), OFPort.of(4));
			}
			if (hostName.equals("h14")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:03"), OFPort.of(5));
			}
			if (hostName.equals("h15")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:03"), OFPort.of(6));
			}
			if (hostName.equals("h16")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:03"), OFPort.of(7));
			}
			if (hostName.equals("h17")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(2));
			}
			if (hostName.equals("h18")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(3));
			}
			if (hostName.equals("h19")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(4));
			}
			if (hostName.equals("h20")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(5));
			}
			if (hostName.equals("h21")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:05"), OFPort.of(2));
			}
			if (hostName.equals("h22")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:05"), OFPort.of(3));
			}
			if (hostName.equals("h23")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:05"), OFPort.of(4));
			}
			if (hostName.equals("h24")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:05"), OFPort.of(5));
			}
			if (hostName.equals("h25")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:06"), OFPort.of(2));
			}
			if (hostName.equals("h26")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:06"), OFPort.of(3));
			}
			if (hostName.equals("h27")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:06"), OFPort.of(4));
			}
			if (hostName.equals("h28")){
				return new HostLocation(DatapathId.of("00:00:00:00:00:00:00:06"), OFPort.of(5));
			}
		}
		return null;
	}
	public SubstrateNetworkHeu getSubstrateNetworkEmbeddingHeu() {
		return substrateNetworkEmbeddingHeu;
	}
	public void setSubstrateNetworkEmbeddingHeu(SubstrateNetworkHeu substrateNetworkEmbeddingHeu) {
		this.substrateNetworkEmbeddingHeu = substrateNetworkEmbeddingHeu;
	}
}
