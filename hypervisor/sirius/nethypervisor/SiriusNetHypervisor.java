/**
 *    SIRIUS is a resilient multi-cloud  network virtualization  platform,
 *    spanning  multiple  heterogeneous  Cloud Service  Providers  (CSPs), that  leverages  on  software-defined networking
 *    techniques. This  network  virtualization  solution  will enable  the  creation  of  virtual  networks  that  span
 *    multiple heterogeneous  clouds  in  a  transparent way to the SIRIUS users.
 *    
 *    In this prototype, we develop a network hypervisor responsable for handling all network communication between the virtual machines-VMs that
 *    belongs to SIRIUS environment. In this stage, the SIRIUS environment is composed of an environment of servers 
 *    (a set of servers - physycal or virtuals - responsable of hosting a computer hypervisor with a group of virtual machines) and
 *    an environment of tenants (a entity the have many virtual machines with a specif set of MAC address pre-configured to belongs to one tenant).
 *    
 *    Authors: Max Alaluna and Fernando Ramos
 *    April 2018
 *    
 **/

package net.floodlightcontroller.sirius.nethypervisor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.VlanVid;
import org.projectfloodlight.openflow.util.LRULinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.debugcounter.IDebugCounter;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.debugcounter.IDebugCounterService.MetaData;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.sirius.console.Client;
import net.floodlightcontroller.sirius.console.Config;
import net.floodlightcontroller.sirius.console.Console;
import net.floodlightcontroller.sirius.console.Network;
import net.floodlightcontroller.sirius.providerconfig.Bridge3;
import net.floodlightcontroller.sirius.providerconfig.DatapathIdSwitchesSrcDst3;
import net.floodlightcontroller.sirius.providerconfig.EnvironmentOfServices3;
import net.floodlightcontroller.sirius.providerconfig.Interfaces3;
import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.providerconfig.topology.EdgeSC;
import net.floodlightcontroller.sirius.providerconfig.topology.GraphSC;
import net.floodlightcontroller.sirius.routing.DijkstraAlgorithmSCApp;
import net.floodlightcontroller.sirius.tenantconfig.EnvironmentOfTenants3;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.tenantconfig.HostLocation;
import net.floodlightcontroller.sirius.tenantconfig.Tenant3;
import net.floodlightcontroller.sirius.tenantconfig.TenantVirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualEdgeSC;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualSwitch;
import net.floodlightcontroller.sirius.topology.FlowEntry;
import net.floodlightcontroller.sirius.util.Utils;
import net.floodlightcontroller.sirius.util.WatchDirectory;


public class SiriusNetHypervisor
implements IFloodlightModule, INetHypervisorService, IOFMessageListener, IOFSwitchListener, ILinkDiscoveryListener, Observer{


	// Module dependencies
	protected IFloodlightProviderService floodlightProviderService;
	protected IRestApiService restApiService;
	protected IDebugCounterService debugCounterService;
	@SuppressWarnings("unused")
	private IDebugCounter counterFlowMod;
	@SuppressWarnings("unused")
	private IDebugCounter counterPacketOut;
	//Module responsible for handling the switches change status
	private IOFSwitchService switchService;
	//Module responsible for handling the links change status
	private ILinkDiscoveryService linkDiscService;

	protected static Logger log = LoggerFactory.getLogger(SiriusNetHypervisor.class);

	//The servers and the tenants form the environment necessary for the hypervisor work
	volatile EnvironmentOfServices3 environmentOfServers = new EnvironmentOfServices3("gre");
	volatile EnvironmentOfTenants3 environmentOfTenants = new EnvironmentOfTenants3();

	//These attributes are used to handle the routing part of the hypervisor
	protected GraphSC graphSCApp;
	protected DijkstraAlgorithmSCApp dijkstra;
	protected LinkedList<Server3> path;

	//These attributes are the structures do allow the insertion of the proper flow
	//1. switchTenantMap: store all mapping about switch->tenant->IpMacVlan->port.
	//2. datapathIdSwitchSrcSwitchDstLinkMap: store the information of the link. The class DatapathIdSwitchesSrcDst3 
	//(source datapathId, the destination datapathId)->Link 
	//3. datapathIdServerMap: store the mapping datapathId->server. Based on the design, one server will have only one OVS, hence, one datapathId.
	//4. datapathIdPortIpMacVlanPairMap: store the mapping of a switch and a port to an address (DatapathIdPort3->IpMacVlanPair3).
	protected Map<IOFSwitch, Map<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>>> switchTenantVirtualNetworkMap = null;
	protected Map<DatapathIdSwitchesSrcDst3,Link> datapathIdSwitchSrcSwitchDstLinkMap = new HashMap<DatapathIdSwitchesSrcDst3, Link>();
	public Map<DatapathId, Server3> getDatapathIdServerMap() {
		return datapathIdServerMap;
	}

	public void setDatapathIdServerMap(Map<DatapathId, Server3> datapathIdServerMap) {
		this.datapathIdServerMap = datapathIdServerMap;
	}

	protected Map<DatapathId,Server3> datapathIdServerMap = new HashMap<DatapathId,Server3>();
	protected HashMap<Long,FlowEntry> cookieFlowEntryMap = new HashMap<Long,FlowEntry>();
	protected HashMap<Long,VirtualSwitch> cookieVirtualSwitchMap = new HashMap<Long,VirtualSwitch>();
	protected HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap = new HashMap<Long,VirtualNetwork>();

	//protected Map<DatapathIdPort3,Host> datapathIdPortIpMacVlanPairMap = new HashMap<DatapathIdPort3,Host>();

	//Variables responsible for L2 translations
	public static HashMap<MacAddress, Host> macAddressHostMap = new HashMap<MacAddress, Host>();
	public static HashMap<Tenant3, Long> tenantTenantMacIdToVirtualMacsMap = new HashMap<Tenant3, Long>();
	public static HashMap<Tenant3, Long> tenantCurrentHostIdToVirtualMacsMap = new HashMap<Tenant3, Long>();
	private static long tenantIdMacCount;
	private static long hostIdMacCount;
	private static long virtualMacAddressRaw;

	private ArrayList<IOFSwitch> iOFSwitchArray = new ArrayList<IOFSwitch>();

	// flow-mod - for use in the cookie
	public static final int LEARNING_SWITCH_APP_ID = 1;
	// This should probably go in some class that encapsulates
	// the app cookie management
	public static final int APP_ID_BITS = 12;
	public static final int APP_ID_SHIFT = (64 - APP_ID_BITS);
	//public static final long LEARNING_SWITCH_COOKIE = (long) (LEARNING_SWITCH_APP_ID & ((1 << APP_ID_BITS) - 1)) << APP_ID_SHIFT;
	public static final long LEARNING_SWITCH_COOKIE = 1125899906842636L;

	// more flow-mod defaults
	//protected static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 5; // in seconds
	protected static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 0; // infinite
	protected static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
	protected static short FLOWMOD_PRIORITY = 100;

	// for managing our map sizes
	protected static final int MAX_MACS_PER_PHYSICAL_SWITCH  = 1000;

	// normally, setup reverse flow as well. Disable only for using cbench for comparison with NOX etc.
	protected static final boolean LEARNING_SWITCH_REVERSE_FLOW = true;

	//used to retrieve parameters from floodlight.properties
	private Map<String, String> configParameters;

	//used to watch changes in the directory where the physical topology info will be set to be inserted in the Environment of Servers
	File dirLocationPhysicalTopology;
	WatchDirectory watchDirLocationPhysicalTopology;
	Thread threadDoWatchDirLocationPhysicalTopology;
	private static final String DIR_DEFAULT_LOCATION_PHYSICAL_TOPOLOGY = "/home/floodlight/src/main/java/net/floodlightcontroller/sirius/topology/physical";
	private static final String DIR_LOCATION_PHYSICAL_TOPOLOGY_PROPERTY_NAME = "dirPhysicalTopologyConfig";
	private static String DIR_LOCATION_PHYSICAL_TOPOLOGY;
	private boolean canInitEnvironmentOfTenants = false;

	//used to watch changes in the directory where the virtual topology info will be set to be inserted in the Environment of Tenants ???
	File dirLocationVirtualTopology;
	volatile WatchDirectory watchDirLocationVirtualTopology;
	Thread threadDoWatchDirLocationVirtualTopology;
	private static final String DIR_DEFAULT_LOCATION_VIRTUAL_TOPOLOGY = "/home/floodlight/src/main/java/net/floodlightcontroller/sirius/topology/virtual";
	private static final String DIR_LOCATION_VIRTUAL_TOPOLOGY_PROPERTY_NAME = "dirVirtualTopologyConfig";
	private static String DIR_LOCATION_VIRTUAL_TOPOLOGY;

	//used to watch changes in the directory where the tenant's info will be set to be inserted in the Environment of Tenants ???
	File dirLocationTenantInfo;
	WatchDirectory watchDirLocationTenantInfo;
	Thread threadDoWatchDirLocationTenantInfo;
	private static final String DIR_DEFAULT_LOCATION_TENANT_INFO = "/home/max/testeDir/tenant/info";
	private static final String DIR_LOCATION_TENANT_INFO_PROPERTY_NAME = "dirTenantInfoConfig";
	private static String DIR_LOCATION_TENANT_INFO;

	long timeStampCreatePhysicalNetwork = 0;
	long timeStampCreateVirtualNetwork = 0;
	private ArrayList<Link> linkArrayList = new ArrayList<Link>();
	protected Map<Integer,Server3> serverIdServerMap = new HashMap<Integer,Server3>();
	private Socket socket = null;
	private Client clientChangeInfoOrquestrator;

	/**
	 * @param floodlightProvider the floodlightProvider to set
	 */
	public void setFloodlightProvider(IFloodlightProviderService floodlightProviderService) {
		this.floodlightProviderService = floodlightProviderService;
	}

	@Override
	public String getName() {
		return "Sirius - Version 0.3 (L2+L3+Topology+heuristic)";
	}

	public boolean isCanInitEnvironmentOfTenants() {
		return canInitEnvironmentOfTenants;
	}

	public void setCanInitEnvironmentOfTenants(boolean canInitEnvironmentOfTenants) {
		this.canInitEnvironmentOfTenants = canInitEnvironmentOfTenants;
	}

	/**
	 * Search and return a tenant based on the MAC, IP Addresses and VLAN ID
	 * @param mac The MAC address of the VM to search
	 * @param vlan The VLAN that the VM is on
	 * @param ipAddress The IP address of the VM to search
	 */

	/**
	 * Based on the information retrieved from the Environment of Servers, where all static information about
	 * the servers, OVS and its port are inserted, in this method we populate one of the most important structure
	 * of the hypervisor: the switchTenantMap (Map<IOFSwitch, Map<Tenant3, Map<IpMacVlanPair3, OFPort>>>) 
	 * @param server The class Server3 has many attributes like: UUID, OpenVSwitch3, hostname, username etc
	 */

	public void populateSwitchTenantMap(Server3 server){

		Bridge3 bridge;
		Interfaces3 interfaces;
		Iterator<Bridge3> iteratorBridge = server.getOpenVSwitch().getArrayListBridge().iterator();
		//Iterate in all bridges of the server to collect the proper information of the bridge
		while(iteratorBridge.hasNext()){
			bridge = iteratorBridge.next();
			//Verify if the brigde name is the one configured to host all the VMs. In this version, all VMs have to be hosted 
			if(bridge.getName().compareTo(server.getBridgeName())==0){
				Iterator<Interfaces3> iteratorInterfaces = bridge.getArrayListInterfaces().iterator();
				//Iterate in all interfaces of the configured bridge to get all necessary information to populate the structures 
				while(iteratorInterfaces.hasNext()){
					interfaces = iteratorInterfaces.next();
					if(interfaces.getOfport() != null && server != null && server.getSw() != null && server.getSw().getId() != null &&
							this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(server.getSw().getId(), interfaces.getOfport())) != null &&
							this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(server.getSw().getId(), interfaces.getOfport())).getIpAddress() != null){
						this.addToPortMap(server.getSw(), interfaces.getVmsMacAddress(), VlanVid.ofVlan(0), interfaces.getOfport(), 
								this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(server.getSw().getId(), interfaces.getOfport())).getIpAddress());
					}
				}
			}
		}
	}
	
	public void populateSwitchTenantMapNotRealTimeInfoContainers(){

		Iterator<HostLocation> itHostLocation = this.environmentOfServers.getHostLocationHostMap().keySet().iterator();
		IOFSwitch iOFSwitchAux;
		DatapathId datapathIdAux;
		MacAddress hostMacAddressAux;
		OFPort hostOFPortAux;
		IPv4Address hostIPv4AddressAux;
		
		for(HostLocation hostLocation : this.environmentOfServers.getHostLocationHostMap().keySet()){
			hostMacAddressAux = this.environmentOfServers.getHostLocationHostMap().get(hostLocation).getMac();
			hostOFPortAux = hostLocation.getPort();
			hostIPv4AddressAux = this.environmentOfServers.getHostLocationHostMap().get(hostLocation).getIpAddress();
			datapathIdAux = hostLocation.getDatapathId();
			iOFSwitchAux = getSwitchService().getActiveSwitch(datapathIdAux);
			this.addToPortMap(iOFSwitchAux, hostMacAddressAux, VlanVid.ofVlan(0), hostOFPortAux, hostIPv4AddressAux);
		}
	}

	/**
	 * Adds one element in the structure switch->tenant->IpMacVlanId->port (Map<IOFSwitch, Map<Tenant3, Map<IpMacVlanPair3, OFPort>>>)
	 * @param sw The switch to add the mapping to
	 * @param mac The MAC address of the VM to add
	 * @param vlan The VLAN that the VM is on
	 * @param portVal The switchport that the VM is on
	 */
	protected synchronized void addToPortMap(IOFSwitch sw, MacAddress mac, VlanVid vlan, OFPort portVal, IPv4Address ip) {

		//Tenant3 tenant = this.getTenantByMac(mac, vlan, this.environmentOfTenants.getIpFromMac(mac));
		Tenant3 tenant = null;
		try{
		tenant = this.getTenantByHostLocation(sw.getId(), portVal);
		}catch(Exception e){
			e.printStackTrace();
		}
		VirtualNetwork virtualNetwork = this.getVirtualNetworkByHostLocation(sw.getId(), portVal);
		Map<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>> swMap = switchTenantVirtualNetworkMap.get(sw);
		Map<VirtualNetwork, Map<Host, OFPort>> tenantMap = null;
		Map<Host, OFPort> virtualNetworkMap = null;

		if (tenant != null && sw!= null && virtualNetwork != null && portVal != null){
			if (swMap == null) {
				swMap = Collections.synchronizedMap(new LRULinkedHashMap<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>>(MAX_MACS_PER_PHYSICAL_SWITCH));
				switchTenantVirtualNetworkMap.put(sw, swMap);
			} 
				tenantMap = switchTenantVirtualNetworkMap.get(sw).get(tenant);
				if(tenantMap == null){
					tenantMap = Collections.synchronizedMap(new LRULinkedHashMap<VirtualNetwork, Map<Host, OFPort>>(MAX_MACS_PER_PHYSICAL_SWITCH));
					switchTenantVirtualNetworkMap.get(sw).put(tenant, tenantMap);
				} else{
					switchTenantVirtualNetworkMap.get(sw).put(tenant,tenantMap);
				}
				virtualNetworkMap = switchTenantVirtualNetworkMap.get(sw).get(tenant).get(virtualNetwork);
				if(virtualNetworkMap == null){
					virtualNetworkMap = Collections.synchronizedMap(new LRULinkedHashMap<Host, OFPort>(MAX_MACS_PER_PHYSICAL_SWITCH));
					switchTenantVirtualNetworkMap.get(sw).get(tenant).put(virtualNetwork, virtualNetworkMap);
				} else{
					switchTenantVirtualNetworkMap.get(sw).get(tenant).put(virtualNetwork, virtualNetworkMap);
				}
				switchTenantVirtualNetworkMap.get(sw).get(tenant).get(virtualNetwork).put(this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(sw.getId(), portVal)), portVal);
		}
	}

	/**
	 * Remove one element in the structure switch->tenant->IpMacVlanId->port (Map<IOFSwitch, Map<Tenant3, Map<IpMacVlanPair3, OFPort>>>)
	 * @param sw The switch to add the mapping to
	 * @param mac The MAC address of the VM to remove
	 * @param vlan The VLAN that the VM is on
	 * @param ipAddress The IP address of the VM to remove
	 */
	protected void removeFromPortMap(IOFSwitch sw, OFPort port) {


		Tenant3 tenant = this.getTenantByHostLocation(sw.getId(), port);
		VirtualNetwork virtualNetwork = this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(new HostLocation(sw.getId(), port));
		if (sw != null && port != null && tenant != null && virtualNetwork != null) {
			if(switchTenantVirtualNetworkMap.get(sw) != null){
				if(switchTenantVirtualNetworkMap.get(sw).get(tenant) != null ){
					if(switchTenantVirtualNetworkMap.get(sw).get(tenant).get(virtualNetwork) != null){
						switchTenantVirtualNetworkMap.get(sw).get(tenant).get(virtualNetwork).remove(this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(sw.getId(), port)));
					}
				}
			}
		}
	}

	/**
	 * Return the OFPort in the structure switch->tenant->IpMacVlanId->port (Map<IOFSwitch, Map<Tenant3, Map<IpMacVlanPair3, OFPort>>>)
	 * @param sw The switch to get the mapping to
	 * @param sourceMac The source MAC address of the VM
	 * @param dstMac The destination MAC address of the VM
	 * @param vlan The VLAN that the VM is on
	 * @param ipAddressSource The source IP address of the VM
	 * @param ipAddressDst The destination IP address of the VM
	 */	
	public synchronized OFPort getFromPortMap(IOFSwitch sw, MacAddress sourceMac, MacAddress dstMac, VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, TenantVirtualNetwork tenantVirtualNetwork) {
		Tenant3 tenant = null;
		VirtualNetwork virtualNetwork = null;
		if (vlan == VlanVid.FULL_MASK || vlan == null) {
			vlan = VlanVid.ofVlan(0);
		}

		if(tenantVirtualNetwork != null){
			tenant = tenantVirtualNetwork.getTenant();
			virtualNetwork = tenantVirtualNetwork .getVirtualNetwork();
		}

		if (tenant != null && sw != null && virtualNetwork != null) {
			if (switchTenantVirtualNetworkMap.get(sw) != null) {
				if (switchTenantVirtualNetworkMap.get(sw).get(tenant) != null) {
					Map<Host, OFPort> virtualNetworkMap = switchTenantVirtualNetworkMap.get(sw).get(tenant).get(virtualNetwork);
					if (virtualNetworkMap != null ) {
						Host host = null;
						Iterator<Host> iteratorHost = virtualNetwork.getHostList().iterator();
						while(iteratorHost.hasNext()){
							host = iteratorHost.next();
							if (host.getPhysicalHost().getIpAddress().equals(ipAddressSource) && host.getPhysicalHost().getVlan().equals(vlan)){
								break;
							}	
						}
						return virtualNetworkMap.get(host.getPhysicalHost());
					}
				}
			}
		}
		return null;
	}

	/**
	 * Clean the structure of mapping switch->tenant->IpMacVlanId->port (Map<IOFSwitch, Map<Tenant3, Map<IpMacVlanPair3, OFPort>>>)
	 * 
	 **/
	public void clearLearnedTable() {
		switchTenantVirtualNetworkMap.clear();
	}

	/**
	 * Clean the structure of mapping switch->tenant->IpMacVlanId->port (Map<IOFSwitch, Map<Tenant3, Map<IpMacVlanPair3, OFPort>>>) 
	 * in a specific switch
	 * * @param sw The switch to clean the mapping
	 * 
	 **/
	public void clearLearnedTable(IOFSwitch sw) {

		Map<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>> swMap = switchTenantVirtualNetworkMap.get(sw);

		if (swMap != null) {
			Iterator<Tenant3> iterator = swMap.keySet().iterator();
			while(iterator.hasNext()){	
				Map<VirtualNetwork, Map<Host, OFPort>> tenantMap = swMap.get(iterator.next());
				tenantMap.clear();
			}
			swMap.clear();
		}
	}

	/**
	 * Return the structure of mapping switch->tenant->IpMacVlanId->port (Map<IOFSwitch, Map<Tenant3, Map<IpMacVlanPair3, OFPort>>>) 
	 * 
	 **/	
	@Override
	public synchronized Map<IOFSwitch, Map<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>>> getTable() {

		return switchTenantVirtualNetworkMap;
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		switch (msg.getType()) {
		case PACKET_IN:
			return this.processPacketInMessage(sw, (OFPacketIn) msg, cntx);
		case FLOW_REMOVED:
			return this.processFlowRemovedMessageVirtualTopo(sw, (OFFlowRemoved) msg);
		case ERROR:
			return Command.CONTINUE;
		default:
			log.error("received an unexpected message {} from switch {}", msg, sw);
			return Command.CONTINUE;
		}
	}

	/**
	 * Process the packetIn
	 * @param sw The switch to create the match
	 * @param pi The OFPacketIn
	 * @param cntx The Floodlight context
	 */
	private Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx) {

		try{
			OFPort inPortPI = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));
			Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
			TenantVirtualNetwork tenantVirtualNetwork;
			Match m = Utils.createMatchFromPacket(sw, inPortPI, cntx);
			MacAddress srcMac = m.get(MatchField.ETH_SRC);
			MacAddress dstMac = m.get(MatchField.ETH_DST);
			VlanVid vlan = m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid();
			IPv4Address ipAddressSource = null;
			IPv4Address ipAddressDst = null;
			IOFSwitch swDst2 = null;

			if ((eth.getEtherType() == EthType.ARP) && (eth.getPayload() instanceof ARP)){
				dstMac = MacAddress.of("ff:ff:ff:ff:ff:ff");
				ipAddressSource = (((ARP) eth.getPayload()).getSenderProtocolAddress());
				ipAddressDst = (((ARP) eth.getPayload()).getTargetProtocolAddress());
				tenantVirtualNetwork = getTenantVirtualNetworkFrom2Locations(sw,srcMac,ipAddressSource,dstMac,ipAddressDst,vlan, inPortPI);				
				if(dstMac != null && vlan != null && ipAddressDst != null && tenantVirtualNetwork != null){
					swDst2 = this.getSwitchByMacInVirtualNetwork(dstMac, vlan, ipAddressDst, tenantVirtualNetwork.getVirtualNetwork());
				}
				OFPort outPort = getFromPortMap(swDst2, dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
				if ( (tenantVirtualNetwork instanceof TenantVirtualNetwork) && inPortPI != null && tenantVirtualNetwork != null && outPort != null){
					m = Utils.createMatchFromPacket(sw, inPortPI, vlan, srcMac, dstMac);
					this.handleARPVirtualNetwork(ipAddressSource, ipAddressDst, srcMac, vlan, dstMac, inPortPI, sw, swDst2, m, tenantVirtualNetwork, outPort, 1);				
					m = Utils.createMatchFromPacket(swDst2, outPort, vlan, dstMac, srcMac);
					this.handleARPVirtualNetwork(ipAddressDst, ipAddressSource, dstMac, vlan, srcMac, outPort, swDst2, sw, m, tenantVirtualNetwork, inPortPI, 1);
				}
			}else{
				if ((eth.getEtherType() == EthType.IPv4) && (eth.getPayload() instanceof IPv4) ){
					ipAddressSource = ((IPv4) eth.getPayload()).getSourceAddress();
					ipAddressDst = ((IPv4) eth.getPayload()).getDestinationAddress();
					//Insert for Mininet tests
					if(ipAddressSource == null){
						ipAddressSource = IPv4Address.of("192.168.0.40");
					}
					if(ipAddressDst == null){
						ipAddressDst = IPv4Address.of("192.168.1.40");
					}
					if ((dstMac.getLong() & 0xfffffffffff0L) == 0x0180c2000000L) {
						if (log.isTraceEnabled()) {
							log.trace("ignoring packet addressed to 802.1D/Q reserved addr: switch {} vlan {} dest MAC {}",
									new Object[]{ sw, vlan, dstMac.toString() });
						}
						return Command.STOP;
					}
					if ((srcMac.getLong() & 0x010000000000L) == 0) {
						log.info("processPacketIn");
					}
					tenantVirtualNetwork = getTenantVirtualNetworkFrom2Locations(sw,srcMac,ipAddressSource,dstMac,ipAddressDst,vlan, inPortPI);
					if(dstMac != null && vlan != null && ipAddressDst != null && tenantVirtualNetwork != null){
						swDst2 = this.getSwitchByMacInVirtualNetwork(dstMac, vlan, ipAddressDst, tenantVirtualNetwork.getVirtualNetwork());
					}
					OFPort outPort = getFromPortMap(swDst2, dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
					if ( outPort == null ) {
						if ( dstMac.toString().equals("ff:ff:ff:ff:ff:ff") ){
							log.info("OFPort.FLOOD");
						} else if (tenantVirtualNetwork != null) {
							log.info("OFPort.FLOOD");
						}
					} else if (outPort.equals(inPortPI) && sw.getId().equals(swDst2.getId())) {
						log.trace("ignoring packet that arrived on same port as learned destination:"
								+ " switch {} vlan {} dest MAC {} port {}",
								new Object[]{ sw, vlan, dstMac.toString(), outPort.getPortNumber() });
					} else {
						if ( (tenantVirtualNetwork instanceof TenantVirtualNetwork) && inPortPI != null && tenantVirtualNetwork != null && outPort != null){
							if (!((eth.getEtherType() == EthType.ARP))){
								Match matchPCK = Utils.createMatchFromFieldsDirect(inPortPI, srcMac, dstMac, vlan, sw);
								this.writeFlowModWorkingPath(sw, swDst2, matchPCK, inPortPI, tenantVirtualNetwork, ipAddressDst, ipAddressSource, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, outPort);
							}
						}
					}
				}
			}
		}catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
		return Command.CONTINUE;
	}

	@SuppressWarnings("unused")
	private void insertAllFlowsProactivily(EnvironmentOfTenants3 environmentOfTenants, long start){

		ArrayList<ArrayList<Host>> combination = new ArrayList<ArrayList<Host>>();
		int indexVN = this.environmentOfTenants.getVirtualNetworkArrayList().size() -1;
		for (int ii = 0; ii < this.environmentOfTenants.getVirtualNetworkArrayList().get(indexVN).getHostList().size()-1; ii++){
			for (int j = ii+1; j < this.environmentOfTenants.getVirtualNetworkArrayList().get(indexVN).getHostList().size(); j++ ){

				combination.add(new ArrayList<Host>(Arrays.asList(this.environmentOfTenants.getVirtualNetworkArrayList().get(indexVN).getHostList().get(ii),
						this.environmentOfTenants.getVirtualNetworkArrayList().get(indexVN).getHostList().get(j))));
			}
		}
	for (int k = 0; k < combination.size(); k++){
		this.insertFlowsARPProactivily(combination.get(k).get(0), combination.get(k).get(1), start);
		this.insertFlowsARPProactivily(combination.get(k).get(1), combination.get(k).get(0), start);
		this.insertFlowsProactivily(combination.get(k).get(0), combination.get(k).get(1), start);
	}
	log.info("Finish!!!!");
	}

	private Command insertFlowsARPProactivily(Host sourceHost, Host destinationHost, long start) {

		TenantVirtualNetwork tenantVirtualNetwork;
		IPv4Address ipAddressSource = null; 
		IPv4Address ipAddressDst = null;
		IOFSwitch sw = null;
		MacAddress srcMac = null;
		MacAddress dstMac = MacAddress.of("ff:ff:ff:ff:ff:ff");
		VlanVid vlan = VlanVid.ZERO;
		OFPort inPortPI = null;
		IOFSwitch swDst2 = null;
		Match m = null;
		@SuppressWarnings("unused")
		String a;
		if(sourceHost != null && sourceHost.getHostName() != null && 
				sourceHost.getPhysicalHost() != null && sourceHost.getPhysicalHost().getHostLocation() != null &&
				sourceHost.getPhysicalHost().getHostLocation().getDatapathId() != null &&
				sourceHost.getPhysicalHost().getHostLocation().getPort() != null && destinationHost != null &&
				destinationHost.getIpAddress() != null){

			sw = this.environmentOfServers.getDatapathIdIOFSwitchMap().get(sourceHost.getPhysicalHost().getHostLocation().getDatapathId());
			srcMac = sourceHost.getPhysicalHost().getMac();
			ipAddressSource = sourceHost.getIpAddress();
			ipAddressDst = destinationHost.getIpAddress();
			inPortPI = sourceHost.getPhysicalHost().getHostLocation().getPort();
			m = Utils.createMatchFromPacket(sw, inPortPI, vlan, srcMac, dstMac);
			tenantVirtualNetwork = getTenantVirtualNetworkFrom2Locations(sw, srcMac, ipAddressSource, dstMac, ipAddressDst, vlan, inPortPI);
			if(tenantVirtualNetwork != null){
				swDst2 = this.getSwitchByMacInVirtualNetwork(dstMac, vlan, ipAddressDst, tenantVirtualNetwork.getVirtualNetwork());
			}
			if(swDst2 != null){
				OFPort outPort = getFromPortMap(swDst2, dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);

				this.handleARPVirtualNetwork(ipAddressSource, ipAddressDst, srcMac, vlan, dstMac, inPortPI, sw, swDst2, m, tenantVirtualNetwork, outPort, start);
			}
		}
		return Command.CONTINUE;
	}

	private Command insertFlowsProactivily(Host sourceHost, Host destinationHost, long start) {

		TenantVirtualNetwork tenantVirtualNetwork;
		IPv4Address ipAddressSource = null; 
		IPv4Address ipAddressDst = null;
		IOFSwitch sw = null;
		MacAddress srcMac = null;
		MacAddress dstMac = null;
		VlanVid vlan = VlanVid.ZERO;
		OFPort inPortPI = null;
		IOFSwitch swDst2 = null;

		sw = this.environmentOfServers.getDatapathIdIOFSwitchMap().get(sourceHost.getPhysicalHost().getHostLocation().getDatapathId());
		srcMac = sourceHost.getPhysicalHost().getMac();
		dstMac = destinationHost.getPhysicalHost().getMac();
		ipAddressSource = sourceHost.getIpAddress();
		ipAddressDst = destinationHost.getIpAddress();
		inPortPI = sourceHost.getPhysicalHost().getHostLocation().getPort();
		tenantVirtualNetwork = getTenantVirtualNetworkFrom2Locations(sw, srcMac, ipAddressSource, dstMac, ipAddressDst,vlan, inPortPI);

		if(dstMac != null && vlan != null && ipAddressDst != null && tenantVirtualNetwork != null){
			swDst2 = this.getSwitchByMacInVirtualNetwork(dstMac, vlan, ipAddressDst, tenantVirtualNetwork.getVirtualNetwork());
		}
		OFPort outPort = getFromPortMap(swDst2, dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);

		if ( (tenantVirtualNetwork instanceof TenantVirtualNetwork) && inPortPI != null && tenantVirtualNetwork != null && outPort != null){
			Match matchPCK = Utils.createMatchFromFieldsDirect(inPortPI, srcMac, dstMac, vlan, sw);
			this.writeFlowModWorkingPath(sw, swDst2, matchPCK, inPortPI, tenantVirtualNetwork, ipAddressDst, ipAddressSource, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, outPort, start);
		}
		return Command.CONTINUE;
	}

	private void handleARPVirtualNetwork(IPv4Address ipAddressSource, IPv4Address ipAddressDst, MacAddress srcMac, VlanVid vlan, 
			MacAddress dstMac, OFPort inPort, IOFSwitch sw, IOFSwitch swDst2, Match m, TenantVirtualNetwork tenantVirtualNetwork, OFPort outPort, long start){

		MacAddress dstMacARP = null;

		if (tenantVirtualNetwork != null){
			ArrayList<Host> hostList = tenantVirtualNetwork.getVirtualNetwork().getHostList();
			Host host;
			Iterator<Host> iteratorHost = hostList.iterator();
			while(iteratorHost.hasNext()){
				host = iteratorHost.next();
				if(host.getPhysicalHost().getIpAddress().equals(ipAddressDst)){
					dstMacARP = host.getPhysicalHost().getMac();
					break;
				}
			}
			if(dstMacARP != null){
				Match matchARP = Utils.createMatchFromFieldsDirect(srcMac, dstMacARP, inPort, ipAddressDst, ipAddressSource, m);
				this.writeFlowModWorkingPathARP(sw, swDst2, matchARP, inPort, ipAddressDst, ipAddressSource, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, dstMacARP, tenantVirtualNetwork, outPort, start);
			}
		}
	}

	@SuppressWarnings("unused")
	private void handleARPVirtualNetwork(IPv4Address ipAddressSource, IPv4Address ipAddressDst, MacAddress srcMac, VlanVid vlan, 
			MacAddress dstMac, OFPort inPort, IOFSwitch sw, IOFSwitch swDst2, Match m, TenantVirtualNetwork tenantVirtualNetwork, OFPort outPort){

		MacAddress dstMacARP = null;

		if (tenantVirtualNetwork != null){
			ArrayList<Host> hostList = tenantVirtualNetwork.getVirtualNetwork().getHostList();//(this.environmentOfTenants.getTenantList().get(this.environmentOfTenants.getTenantList().indexOf(tenantARP))).getHostList();
			Host host;
			Iterator<Host> iteratorHost = hostList.iterator();
			while(iteratorHost.hasNext()){
				host = iteratorHost.next();
				if(host.getPhysicalHost().getIpAddress().equals(ipAddressDst)){
					dstMacARP = host.getPhysicalHost().getMac();
					break;
				}
			}
			if(dstMacARP != null){
				Match matchARP = Utils.createMatchFromFieldsDirect(srcMac, dstMacARP, inPort, ipAddressDst, ipAddressSource, m);
				this.writeFlowModWorkingPathARP(sw, swDst2, matchARP, inPort, ipAddressDst, ipAddressSource, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, dstMacARP, tenantVirtualNetwork, outPort);
			}
		}
	}

	@SuppressWarnings("unused")
	private void writeFlowModWorkingPathARP(IOFSwitch srcSw, IOFSwitch swDst, Match m, OFPort inPort, IPv4Address ipAddressDst, IPv4Address ipAddressSource, OFFlowModCommand add,
			OFBufferId noBuffer, MacAddress dstMacARP, TenantVirtualNetwork tenantVirtualNetwork, OFPort outPort, long start){

		Server3 srcServer, dstServer;
		MacAddress srcMac = m.get(MatchField.ETH_SRC);
		VlanVid vlan = m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid();
		ArrayList<Server3> pathServer = new ArrayList<Server3>();

		LinkedList<VirtualSwitch> path;
		String switchesPath = "";
		int srcIdArrayListVirtualSwitch = tenantVirtualNetwork.getVirtualNetwork().getPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap().get(srcSw.getId());
		srcServer = tenantVirtualNetwork.getVirtualNetwork().getVirtualSwitchArrayList().get(srcIdArrayListVirtualSwitch).getWorkingSwitch();
		int dstIdArrayListVirtualSwitch = tenantVirtualNetwork.getVirtualNetwork().getPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap().get(swDst.getId());
		dstServer = tenantVirtualNetwork.getVirtualNetwork().getVirtualSwitchArrayList().get(dstIdArrayListVirtualSwitch).getWorkingSwitch();

		if ((dstServer != null)&&(srcServer != null)){
			if(srcServer.equals(dstServer)){
				VirtualSwitch virtualSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), srcSw.getId());
				this.writeFlowModWorkingPathSameSwitchARP(srcSw, inPort, m, srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, srcServer, virtualSwitch, outPort, tenantVirtualNetwork, start);
			}else{
				VirtualSwitch virtualSrcSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), srcSw.getId());
				VirtualSwitch virtualDstSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), swDst.getId());
				VirtualEdgeSC virtualEdgeSC = null;
				boolean foundEdgeSCDirect = false;
				boolean foundEdgeSCReverse = false;

				if(tenantVirtualNetwork != null){
					if(tenantVirtualNetwork.getVirtualNetwork() != null){
						if(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList() != null){
							path = tenantVirtualNetwork.getVirtualNetwork().getVirtualPath(virtualSrcSwitch, virtualDstSwitch);
							for (int i = 0; i < path.size()-1; i++){
								for (int j = 0; j < tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().size(); j++){
									if((path.get(i).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getSourceSwitch()) &&
											path.get(i+1).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getDestinationSwitch())) || 
											(path.get(i).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getDestinationSwitch()) &&
													path.get(i+1).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getSourceSwitch()))){
										virtualEdgeSC = tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j);
										if(virtualEdgeSC != null){
											if(switchesPath.equals("")){
												if(!virtualEdgeSC.getSourceSwitch().equals(virtualSrcSwitch)){
													switchesPath = Utils.reverseSwitches(virtualEdgeSC.getWorkingPath());
												}else{
													switchesPath = virtualEdgeSC.getWorkingPath();
												}
											} else{
												switchesPath = Utils.concatEdges(switchesPath,virtualEdgeSC.getWorkingPath());
											}
										}
									}
								} 
							}
							String[] switches = switchesPath.split(":");

							if(srcSw.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[0])).getSw().getId()) &&
									swDst.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[switches.length-1])).getSw().getId())){
								for (int i1 = 0; i1 < switches.length; i1++){
									pathServer.add(this.serverIdServerMap.get(Integer.parseInt(switches[i1])));
								}
								foundEdgeSCDirect = true;
							}else{
								if(srcSw.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[switches.length-1])).getSw().getId()) && 
										swDst.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[0])).getSw().getId())){
									for (int i1 = 0; i1 < switches.length; i1++){
										pathServer.add(this.serverIdServerMap.get(Integer.parseInt(switches[i1])));
									}
									foundEdgeSCReverse = true;
								}
							}
							if(foundEdgeSCDirect){

								this.writeFlowModWorkingPathDirectOrderARPNew(m, srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork, pathServer, virtualSrcSwitch, virtualDstSwitch, start);
							} else{
								if(foundEdgeSCReverse){

									this.writeFlowModWorkingPathReverseOrderARPNew(m, srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork, pathServer, virtualSrcSwitch, virtualDstSwitch, start);
								}
							}
						}
					}
				}
			}
		} else {
			return;
		}
	}

	private void writeFlowModWorkingPathARP(IOFSwitch srcSw, IOFSwitch swDst, Match m, OFPort inPort, IPv4Address ipAddressDst, IPv4Address ipAddressSource, OFFlowModCommand add,
			OFBufferId noBuffer, MacAddress dstMacARP, TenantVirtualNetwork tenantVirtualNetwork, OFPort outPort) {

		Server3 srcServer, dstServer;
		MacAddress srcMac = m.get(MatchField.ETH_SRC);
		VlanVid vlan = m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid();
		ArrayList<Server3> pathServer = new ArrayList<Server3>();
		LinkedList<VirtualSwitch> path;
		String switchesPath = "";
		int srcIdArrayListVirtualSwitch = tenantVirtualNetwork.getVirtualNetwork().getPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap().get(srcSw.getId());
		srcServer = tenantVirtualNetwork.getVirtualNetwork().getVirtualSwitchArrayList().get(srcIdArrayListVirtualSwitch).getWorkingSwitch();
		int dstIdArrayListVirtualSwitch = tenantVirtualNetwork.getVirtualNetwork().getPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap().get(swDst.getId());
		dstServer = tenantVirtualNetwork.getVirtualNetwork().getVirtualSwitchArrayList().get(dstIdArrayListVirtualSwitch).getWorkingSwitch();
		if ((dstServer != null)&&(srcServer != null)){
			if(srcServer.equals(dstServer)){
				VirtualSwitch virtualSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), srcSw.getId());
				this.writeFlowModWorkingPathSameSwitchARP(srcSw, inPort, m, srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, srcServer, virtualSwitch, outPort, tenantVirtualNetwork);
			}else{
				VirtualSwitch virtualSrcSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), srcSw.getId());
				VirtualSwitch virtualDstSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), swDst.getId());
				VirtualEdgeSC virtualEdgeSC = null;
				boolean foundEdgeSCDirect = false;
				boolean foundEdgeSCReverse = false;
				if(tenantVirtualNetwork != null){
					if(tenantVirtualNetwork.getVirtualNetwork() != null){
						if(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList() != null){
							path = tenantVirtualNetwork.getVirtualNetwork().getVirtualPath(virtualSrcSwitch, virtualDstSwitch);
							for (int i = 0; i < path.size()-1; i++){
								for (int j = 0; j < tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().size(); j++){
									if((path.get(i).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getSourceSwitch()) &&
											path.get(i+1).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getDestinationSwitch())) || 
											(path.get(i).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getDestinationSwitch()) &&
													path.get(i+1).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getSourceSwitch()))){
										virtualEdgeSC = tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j);
										if(virtualEdgeSC != null){
											if(switchesPath.equals("")){
												if(!virtualEdgeSC.getSourceSwitch().equals(virtualSrcSwitch)){
													switchesPath = Utils.reverseSwitches(virtualEdgeSC.getWorkingPath());
												}else{
													switchesPath = virtualEdgeSC.getWorkingPath();
												}
											} else{
												switchesPath = Utils.concatEdges(switchesPath,virtualEdgeSC.getWorkingPath());
											}
										}
									}
								} 
							}
							String[] switches = switchesPath.split(":");

							if(srcSw.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[0])).getSw().getId()) &&
									swDst.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[switches.length-1])).getSw().getId())){
								for (int i1 = 0; i1 < switches.length; i1++){
									pathServer.add(this.serverIdServerMap.get(Integer.parseInt(switches[i1])));
								}
								foundEdgeSCDirect = true;
							}else{
								if(srcSw.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[switches.length-1])).getSw().getId()) && 
										swDst.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[0])).getSw().getId())){
									for (int i1 = 0; i1 < switches.length; i1++){
										pathServer.add(this.serverIdServerMap.get(Integer.parseInt(switches[i1])));
									}
									foundEdgeSCReverse = true;
								}
							}
							if(foundEdgeSCDirect){

								this.writeFlowModWorkingPathDirectOrderARPNew(m, srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork, pathServer, virtualSrcSwitch, virtualDstSwitch);
							} else{
								if(foundEdgeSCReverse){
									this.writeFlowModWorkingPathReverseOrderARPNew(m, srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork, pathServer, virtualSrcSwitch, virtualDstSwitch);
								}
							}
						}
					}
				}
			}
		} else {
			return;
		}
	}

	private void writeFlowModWorkingPathSameSwitchARP(IOFSwitch srcSw,
			OFPort inPort, Match m, MacAddress srcMac, MacAddress dstMacARP,
			VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, Server3 srcServer, VirtualSwitch virtualSwitch, OFPort outPort, TenantVirtualNetwork tenantVirtualNetwork, long start) {

		if (inPort != null && outPort != null){
			Match matchARP = Utils.createMatchFromFieldsDirectARP(inPort, ipAddressDst, ipAddressSource, m, srcSw, MacAddress.of("ff:ff:ff:ff:ff:ff"));
			WriteFlowModSameSwitch writeFlowModSameSwitch = new WriteFlowModSameSwitch(srcSw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARP, outPort, srcServer, virtualSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, "a");
			writeFlowModSameSwitch.start();
			if (LEARNING_SWITCH_REVERSE_FLOW) {
				Match matchARPRev = Utils.createMatchFromFieldsReverse(outPort, ipAddressDst, ipAddressSource, m, srcSw, srcMac, dstMacARP);
				WriteFlowModSameSwitch writeFlowModSameSwitchRev = new WriteFlowModSameSwitch(srcSw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARPRev, inPort, srcServer, virtualSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, "a");
				writeFlowModSameSwitchRev.start();
			}
		}
	}

	private void writeFlowModWorkingPathSameSwitchARP(IOFSwitch srcSw,
			OFPort inPort, Match m, MacAddress srcMac, MacAddress dstMacARP,
			VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, Server3 srcServer, VirtualSwitch virtualSwitch, OFPort outPort, TenantVirtualNetwork tenantVirtualNetwork) {

		if (inPort != null && outPort != null){
			Match matchARP = Utils.createMatchFromFieldsDirectARP(inPort, ipAddressDst, ipAddressSource, m, srcSw, MacAddress.of("ff:ff:ff:ff:ff:ff"));
			WriteFlowModSameSwitch writeFlowModSameSwitch = new WriteFlowModSameSwitch(srcSw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARP, outPort, srcServer, virtualSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, "a");
			writeFlowModSameSwitch.start();
			if (LEARNING_SWITCH_REVERSE_FLOW) {
				Match matchARPRev = Utils.createMatchFromFieldsReverse(outPort, ipAddressDst, ipAddressSource, m, srcSw, srcMac, dstMacARP);
				WriteFlowModSameSwitch writeFlowModSameSwitchRev = new WriteFlowModSameSwitch(srcSw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARPRev, inPort, srcServer, virtualSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, "a");
				writeFlowModSameSwitchRev.start();
			}
		}
	}

	private void writeFlowModWorkingPathDirectOrderARPNew(Match m, MacAddress srcMac,
			MacAddress dstMacARP, VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, 
			TenantVirtualNetwork tenantVirtualNetwork, ArrayList<Server3> pathServer, VirtualSwitch virtualSrcSwitch, 
			VirtualSwitch virtualDstSwitch, long start) {

		Host srcHost = null;
		Host dstHost = null;
		OFPort outPort = null;
		OFPort inPort = null;
		OFPort outPortDst = null;
		OFPort outPortSrc = null;
		int indexOfSrcServer = 0;
		int indexOfDstServer = pathServer.size() -1;

		for(int i = 0; i < pathServer.size(); i++){
			if (i == indexOfSrcServer ){
				inPort = null;
				outPort = null;
				outPortSrc = null;
								@SuppressWarnings("unused")
								DatapathId ff = pathServer.get(indexOfSrcServer).getSw().getId();
								@SuppressWarnings("unused")
								DatapathId gg = pathServer.get(indexOfSrcServer+1).getSw().getId();
								@SuppressWarnings("unused")
								Link hh = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
										pathServer.get(indexOfSrcServer+1).getSw().getId()));
				if (pathServer.get(indexOfSrcServer).getSw().getId() != null && pathServer.get(indexOfSrcServer+1).getSw().getId() != null && 
						this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
								pathServer.get(indexOfSrcServer+1).getSw().getId())) != null){
					outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
							pathServer.get(indexOfSrcServer+1).getSw().getId())).getSrcPort();
				}
				if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork) != null){
					inPort = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
				}
				if (pathServer.get(indexOfDstServer).getSw() != null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork) != null){
					outPortDst = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
				}
				if((outPort != null) && (inPort != null)){
					srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), 
							getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork )));
					dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), outPortDst));
					srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
					dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));

					WriteFlowModL2SRC writeFlowModL2SRC = new WriteFlowModL2SRC(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfSrcServer).getSw(), 
							OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, true, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, 
							this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
							macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
							hostIdMacCount, virtualMacAddressRaw, "a", start);
					writeFlowModL2SRC.start();
					//					this.writeFlowModL2SRC(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, true, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork);

					if (LEARNING_SWITCH_REVERSE_FLOW) {
						WriteFlowModL2SRC writeFlowModL2SRCRev = new WriteFlowModL2SRC(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfSrcServer).getSw(), 
								OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, false, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, 
								this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
								macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
								hostIdMacCount, virtualMacAddressRaw, "a", start);
						writeFlowModL2SRCRev.start();
					}
				}
			} else{

				if (i == indexOfDstServer){

					inPort = null;
					outPort = null;
					outPortSrc = null;

					if (pathServer.get(indexOfDstServer).getSw().getId() != null && pathServer.get(indexOfDstServer-1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
									pathServer.get(indexOfDstServer-1).getSw().getId())) != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
								pathServer.get(indexOfDstServer-1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(indexOfDstServer)!=null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork ) != null){
						outPort = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork );
					}
					if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork ) != null){
						outPortSrc = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork );
					}
					if (inPort != null && outPort != null){
						srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), 
								getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork)));
						dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), outPortSrc));
						srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
						dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
						if ( srcHost != null && dstHost != null){
							if (srcHost.getVirtualMacAddress() != null && dstHost.getVirtualMacAddress() != null){
								WriteFlowModL2DST writeFlowModL2DST = new WriteFlowModL2DST(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfDstServer).getSw(), 
										OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, false, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, 
										this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
										macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
										hostIdMacCount, virtualMacAddressRaw, "a", start);
								writeFlowModL2DST.start();

								if (LEARNING_SWITCH_REVERSE_FLOW) {
									WriteFlowModL2DST writeFlowModL2DSTRev = new WriteFlowModL2DST(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfDstServer).getSw(), 
											OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, true, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, 
											this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
											macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
											hostIdMacCount, virtualMacAddressRaw, "a", start);
									writeFlowModL2DSTRev.start();
								}
							}
						}
					}
				} else {
					inPort = null;
					outPort = null;
					outPortSrc = null;
					if (pathServer.get(i).getSw().getId() != null && pathServer.get(i+1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
									pathServer.get(i+1).getSw().getId())) != null &&
									this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
											pathServer.get(i+1).getSw().getId())).getSrcPort() != null ){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i+1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(i).getSw().getId() != null && pathServer.get(i-1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
									pathServer.get(i-1).getSw().getId())) != null &&
									this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
											pathServer.get(i-1).getSw().getId())).getSrcPort() != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i-1).getSw().getId())).getSrcPort();
					}
					if (inPort != null && outPort != null){
						Match matchARP = Utils.createMatchFromFieldsDirectARP(inPort, ipAddressDst, ipAddressSource, m, pathServer.get(i).getSw(), srcHost.getVirtualMacAddress(), dstHost.getVirtualMacAddress());
						WriteFlowMod writeFlowMod = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARP, 
								outPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a", start);
						writeFlowMod.start();
						if (LEARNING_SWITCH_REVERSE_FLOW) {
							Match matchARPRev = Utils.createMatchFromFieldsDirectARP(outPort, ipAddressSource, ipAddressDst, matchARP, pathServer.get(i).getSw(), dstHost.getVirtualMacAddress(), srcHost.getVirtualMacAddress());
							WriteFlowMod writeFlowModRev = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARPRev, 
									inPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a", start);
							writeFlowModRev.start();
						}
					}
				}
			}
		}
	}

	private void writeFlowModWorkingPathDirectOrderARPNew(Match m, MacAddress srcMac,
			MacAddress dstMacARP, VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, TenantVirtualNetwork tenantVirtualNetwork, ArrayList<Server3> pathServer, VirtualSwitch virtualSrcSwitch, VirtualSwitch virtualDstSwitch) {

		Host srcHost = null;
		Host dstHost = null;
		OFPort outPort = null;
		OFPort inPort = null;
		OFPort outPortDst = null;
		OFPort outPortSrc = null;
		int indexOfSrcServer = 0;
		int indexOfDstServer = pathServer.size() -1;

		for(int i = 0; i < pathServer.size(); i++){
			if (i == indexOfSrcServer ){
				inPort = null;
				outPort = null;
				outPortSrc = null;
				if (pathServer.get(indexOfSrcServer).getSw().getId() != null && pathServer.get(indexOfSrcServer+1).getSw().getId() != null && 
						this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
								pathServer.get(indexOfSrcServer+1).getSw().getId())) != null){
					outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
							pathServer.get(indexOfSrcServer+1).getSw().getId())).getSrcPort();
				}
				if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork) != null){
					inPort = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
				}
				if (pathServer.get(indexOfDstServer).getSw() != null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork) != null){
					outPortDst = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
				}	
				if((outPort != null) && (inPort != null)){
					srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), 
							getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork )));
					dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), outPortDst));
					srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
					dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
					WriteFlowModL2SRC writeFlowModL2SRC = new WriteFlowModL2SRC(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfSrcServer).getSw(), 
							OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, true, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, 
							this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
							macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
							hostIdMacCount, virtualMacAddressRaw, "a");
					writeFlowModL2SRC.start();
					if (LEARNING_SWITCH_REVERSE_FLOW) {
						WriteFlowModL2SRC writeFlowModL2SRCRev = new WriteFlowModL2SRC(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfSrcServer).getSw(), 
								OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, false, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, 
								this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
								macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
								hostIdMacCount, virtualMacAddressRaw, "a");
						writeFlowModL2SRCRev.start();
					}
				}
			} else{
				if (i == indexOfDstServer){
					inPort = null;
					outPort = null;
					outPortSrc = null;
					if (pathServer.get(indexOfDstServer).getSw().getId() != null && pathServer.get(indexOfDstServer-1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
									pathServer.get(indexOfDstServer-1).getSw().getId())) != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
								pathServer.get(indexOfDstServer-1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(indexOfDstServer)!=null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork ) != null){
						outPort = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork );
					}
					if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork ) != null){
						outPortSrc = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork );
					}
					if (inPort != null && outPort != null){
						srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), 
								getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork)));
						dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), outPortSrc));
						srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
						dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
						if ( srcHost != null && dstHost != null){
							if (srcHost.getVirtualMacAddress() != null && dstHost.getVirtualMacAddress() != null){
								WriteFlowModL2DST writeFlowModL2DST = new WriteFlowModL2DST(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfDstServer).getSw(), 
										OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, false, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, 
										this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
										macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
										hostIdMacCount, virtualMacAddressRaw, "a");
								writeFlowModL2DST.start();
								if (LEARNING_SWITCH_REVERSE_FLOW) {
									WriteFlowModL2DST writeFlowModL2DSTRev = new WriteFlowModL2DST(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfDstServer).getSw(), 
											OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, true, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, 
											this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
											macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
											hostIdMacCount, virtualMacAddressRaw, "a");
									writeFlowModL2DSTRev.start();
								}
							}
						}
					}
				} else {
					inPort = null;
					outPort = null;
					outPortSrc = null;
					if (pathServer.get(i).getSw().getId() != null && pathServer.get(i+1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
									pathServer.get(i+1).getSw().getId())).getSrcPort() != null ){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i+1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(i).getSw().getId() != null && pathServer.get(i-1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
									pathServer.get(i-1).getSw().getId())).getSrcPort() != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i-1).getSw().getId())).getSrcPort();
					}
					if (inPort != null && outPort != null){
						Match matchARP = Utils.createMatchFromFieldsDirectARP(inPort, ipAddressDst, ipAddressSource, m, pathServer.get(i).getSw(),srcHost.getVirtualMacAddress(), dstHost.getVirtualMacAddress());
						WriteFlowMod writeFlowMod = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARP, outPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a");
						writeFlowMod.start();
						if (LEARNING_SWITCH_REVERSE_FLOW) {
							Match matchARPRev = Utils.createMatchFromFieldsDirectARP(outPort, ipAddressSource, ipAddressDst, matchARP, pathServer.get(i).getSw(), dstHost.getVirtualMacAddress(), srcHost.getVirtualMacAddress());
							WriteFlowMod writeFlowModRev = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARPRev, inPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a");
							writeFlowModRev.start();
						}
					}
				}
			}
		}
	}

	private void writeFlowModWorkingPathReverseOrderARPNew(Match m, MacAddress srcMac,
			MacAddress dstMacARP, VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, TenantVirtualNetwork tenantVirtualNetwork, 
			ArrayList<Server3> pathServer, VirtualSwitch virtualSrcSwitch, VirtualSwitch virtualDstSwitch, long start) {

		Host srcHost = null;
		Host dstHost = null;
		OFPort inPort = null;
		OFPort outPort = null;
		OFPort outPortDst = null;
		OFPort outPortSrc = null;
		int indexOfSrcServer = pathServer.size() -1;
		int indexOfDstServer = 0;

		for(int i = indexOfSrcServer; i >= indexOfDstServer; i--){
			if (i == indexOfSrcServer ){
				if (pathServer.get(indexOfSrcServer).getSw().getId() != null && pathServer.get(indexOfSrcServer-1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
						pathServer.get(indexOfSrcServer-1).getSw().getId())).getSrcPort() != null){
					outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
							pathServer.get(indexOfSrcServer-1).getSw().getId())).getSrcPort();
				}
				if (pathServer.get(indexOfSrcServer).getSw() != null && this.getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork ) != null){
					inPort = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
				}
				if (pathServer.get(indexOfDstServer).getSw() != null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork ) != null){
					outPortDst = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork );
				}
				if (inPort != null && outPort != null){
					srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), 
							getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork )));
					dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), outPortDst));
					srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
					dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
					WriteFlowModL2SRC writeFlowModL2SRC = new WriteFlowModL2SRC(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, 
							true, true, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
							this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
							macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
							hostIdMacCount, virtualMacAddressRaw, "a", start);
					writeFlowModL2SRC.start();
					if (LEARNING_SWITCH_REVERSE_FLOW) {
						WriteFlowModL2SRC writeFlowModL2SRCRev = new WriteFlowModL2SRC(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, 
								true, false, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
								this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
								macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
								hostIdMacCount, virtualMacAddressRaw, "a", start);
						writeFlowModL2SRCRev.start();
					}	
				}
			} else{
				if (i == indexOfDstServer ){
					inPort = null;
					outPort = null;
					outPortSrc = null;
					if (pathServer.get(indexOfDstServer).getSw().getId() != null && pathServer.get(indexOfDstServer+1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
							pathServer.get(indexOfDstServer+1).getSw().getId())).getSrcPort() != null){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
								pathServer.get(indexOfDstServer+1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(indexOfDstServer).getSw() != null && this.getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork) != null){
						inPort = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
					}
					if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork) != null){
						outPortSrc = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
					}
					if (inPort != null && outPort != null){
						srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), 
								getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork)));
						dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), outPortSrc));
						srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
						dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
						if ( srcHost != null && dstHost != null){
							if (srcHost.getVirtualMacAddress() != null && dstHost.getVirtualMacAddress() != null){
								WriteFlowModL2DST writeFlowModL2DST = new WriteFlowModL2DST(outPort, inPort, vlan, srcHost, dstHost, pathServer.get(indexOfDstServer).getSw(), 
										OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, true, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, 
										this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
										macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
										hostIdMacCount, virtualMacAddressRaw, "a", start);
								writeFlowModL2DST.start();
								if (LEARNING_SWITCH_REVERSE_FLOW) {
									WriteFlowModL2DST writeFlowModL2DSTRev = new WriteFlowModL2DST(outPort, inPort, vlan, srcHost, dstHost, pathServer.get(indexOfDstServer).getSw(), 
											OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, false, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, 
											this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
											macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
											hostIdMacCount, virtualMacAddressRaw, "a", start);
									writeFlowModL2DSTRev.start();
								}	
							}
						}
					}
				} else {
					inPort = null;
					outPort = null;
					if(pathServer.get(i).getSw().getId() != null && pathServer.get(i+1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
							pathServer.get(i+1).getSw().getId())).getSrcPort() != null){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i+1).getSw().getId())).getSrcPort();
					}
					if(pathServer.get(i).getSw().getId() != null && pathServer.get(i-1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
							pathServer.get(i-1).getSw().getId())).getSrcPort() != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i-1).getSw().getId())).getSrcPort();
					}
					if (inPort != null && outPort != null){
						Match matchARP = Utils.createMatchFromFieldsDirectARP(outPort, ipAddressDst, ipAddressSource, m, pathServer.get(i).getSw(), srcHost.getVirtualMacAddress(), dstHost.getVirtualMacAddress());
						WriteFlowMod writeFlowMod = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARP, 
								inPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a", start);
						writeFlowMod.start();
						if (LEARNING_SWITCH_REVERSE_FLOW) {
							Match matchARPRev = Utils.createMatchFromFieldsDirectARP(inPort, ipAddressSource, ipAddressDst, matchARP, pathServer.get(i).getSw(), dstHost.getVirtualMacAddress(), srcHost.getVirtualMacAddress());
							WriteFlowMod writeFlowModRev = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARPRev,
									outPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a", start);
							writeFlowModRev.start();
						}
					}
				}
			}
		}
	}

	private void writeFlowModWorkingPathReverseOrderARPNew(Match m, MacAddress srcMac,
			MacAddress dstMacARP, VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, TenantVirtualNetwork tenantVirtualNetwork, ArrayList<Server3> pathServer, VirtualSwitch virtualSrcSwitch, VirtualSwitch virtualDstSwitch) {

		Host srcHost = null;
		Host dstHost = null;
		OFPort inPort = null;
		OFPort outPort = null;
		OFPort outPortDst = null;
		OFPort outPortSrc = null;
		int indexOfSrcServer = pathServer.size() -1;
		int indexOfDstServer = 0;

		for(int i = indexOfSrcServer; i >= indexOfDstServer; i--){
			if (i == indexOfSrcServer ){
				if (pathServer.get(indexOfSrcServer).getSw().getId() != null && pathServer.get(indexOfSrcServer-1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
						pathServer.get(indexOfSrcServer-1).getSw().getId())).getSrcPort() != null){
					outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
							pathServer.get(indexOfSrcServer-1).getSw().getId())).getSrcPort();
				}
				if (pathServer.get(indexOfSrcServer).getSw() != null && this.getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork ) != null){
					inPort = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
				}
				if (pathServer.get(indexOfDstServer).getSw() != null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork ) != null){
					outPortDst = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork );
				}
				if (inPort != null && outPort != null){
					srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), 
							getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork )));
					dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), outPortDst));
					srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
					dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
					WriteFlowModL2SRC writeFlowModL2SRC = new WriteFlowModL2SRC(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, 
							true, true, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
							this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
							macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
							hostIdMacCount, virtualMacAddressRaw, "a");
					writeFlowModL2SRC.start();
					if (LEARNING_SWITCH_REVERSE_FLOW) {
						WriteFlowModL2SRC writeFlowModL2SRCRev = new WriteFlowModL2SRC(inPort, outPort, vlan, srcHost, dstHost, pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, 
								true, false, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
								this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
								macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
								hostIdMacCount, virtualMacAddressRaw, "a");
						writeFlowModL2SRCRev.start();
					}	
				}
			} else{
				if (i == indexOfDstServer ){
					inPort = null;
					outPort = null;
					outPortSrc = null;
					if (pathServer.get(indexOfDstServer).getSw().getId() != null && pathServer.get(indexOfDstServer+1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
							pathServer.get(indexOfDstServer+1).getSw().getId())).getSrcPort() != null){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
								pathServer.get(indexOfDstServer+1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(indexOfDstServer).getSw() != null && this.getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork) != null){
						inPort = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
					}
					if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork) != null){
						outPortSrc = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMacARP, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
					}
					if (inPort != null && outPort != null){
						srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), 
								getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMacARP, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork)));
						dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), outPortSrc));
						srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
						dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
						if ( srcHost != null && dstHost != null){
							if (srcHost.getVirtualMacAddress() != null && dstHost.getVirtualMacAddress() != null){
								WriteFlowModL2DST writeFlowModL2DST = new WriteFlowModL2DST(outPort, inPort, vlan, srcHost, dstHost, pathServer.get(indexOfDstServer).getSw(), 
										OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, true, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, 
										this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
										macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
										hostIdMacCount, virtualMacAddressRaw, "a");
								writeFlowModL2DST.start();
								if (LEARNING_SWITCH_REVERSE_FLOW) {
									WriteFlowModL2DST writeFlowModL2DSTRev = new WriteFlowModL2DST(outPort, inPort, vlan, srcHost, dstHost, pathServer.get(indexOfDstServer).getSw(), 
											OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, true, false, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, 
											this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
											macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
											hostIdMacCount, virtualMacAddressRaw, "a");
									writeFlowModL2DSTRev.start();
								}	
							}
						}
					}
				} else {
					inPort = null;
					outPort = null;
					if(pathServer.get(i).getSw().getId() != null && pathServer.get(i+1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
							pathServer.get(i+1).getSw().getId())).getSrcPort() != null){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i+1).getSw().getId())).getSrcPort();
					}
					if(pathServer.get(i).getSw().getId() != null && pathServer.get(i-1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
							pathServer.get(i-1).getSw().getId())).getSrcPort() != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i-1).getSw().getId())).getSrcPort();
					}
					if (inPort != null && outPort != null){
						Match matchARP = Utils.createMatchFromFieldsDirectARP(outPort, ipAddressDst, ipAddressSource, m, pathServer.get(i).getSw(), srcHost.getVirtualMacAddress(), dstHost.getVirtualMacAddress());
						WriteFlowMod writeFlowMod = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARP, inPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a");
						writeFlowMod.start();
						if (LEARNING_SWITCH_REVERSE_FLOW) {
							Match matchARPRev = Utils.createMatchFromFieldsDirectARP(inPort, ipAddressSource, ipAddressDst, matchARP, pathServer.get(i).getSw(), dstHost.getVirtualMacAddress(), srcHost.getVirtualMacAddress());
							WriteFlowMod writeFlowModRev = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchARPRev, outPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a");
							writeFlowModRev.start();
						}
					}
				}
			}
		}
	}

	private void writeFlowModWorkingPath(IOFSwitch srcSw, IOFSwitch swDst, Match m, OFPort inPortPI, TenantVirtualNetwork tenantVirtualNetwork, IPv4Address ipAddressDst, IPv4Address ipAddressSource, OFFlowModCommand add,
			OFBufferId noBuffer, OFPort outPort, long start) {

		Server3 srcServer, dstServer;
		MacAddress srcMac = m.get(MatchField.ETH_SRC);
		MacAddress dstMac = m.get(MatchField.ETH_DST);
		VlanVid vlan = m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid();
		ArrayList<Server3> pathServer = new ArrayList<Server3>();
		LinkedList<VirtualSwitch> path;
		String switchesPath = "";
		int srcIdArrayListVirtualSwitch = tenantVirtualNetwork.getVirtualNetwork().getPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap().get(srcSw.getId());
		srcServer = tenantVirtualNetwork.getVirtualNetwork().getVirtualSwitchArrayList().get(srcIdArrayListVirtualSwitch).getWorkingSwitch();
		int dstIdArrayListVirtualSwitch = tenantVirtualNetwork.getVirtualNetwork().getPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap().get(swDst.getId());
		dstServer = tenantVirtualNetwork.getVirtualNetwork().getVirtualSwitchArrayList().get(dstIdArrayListVirtualSwitch).getWorkingSwitch();
		if ((dstServer != null)&&(srcServer != null)){
			if(srcServer.equals(dstServer)){
				VirtualSwitch virtualSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), srcSw.getId());
				this.writeFlowModWorkingPathSameSwitch(srcSw, inPortPI, m, srcMac, dstMac, vlan, ipAddressSource, 
						ipAddressDst, tenantVirtualNetwork, srcServer, virtualSwitch, outPort, start);
			}else{
				VirtualSwitch virtualSrcSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), srcSw.getId());
				VirtualSwitch virtualDstSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), swDst.getId());
				VirtualEdgeSC virtualEdgeSC = null;
				boolean foundEdgeSCDirect = false;
				boolean foundEdgeSCReverse = false;

				if(tenantVirtualNetwork != null){
					if(tenantVirtualNetwork.getVirtualNetwork() != null){
						if(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList() != null){
							path = tenantVirtualNetwork.getVirtualNetwork().getVirtualPath(virtualSrcSwitch, virtualDstSwitch);
							for (int i = 0; i < path.size()-1; i++){
								for (int j = 0; j < tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().size(); j++){
									if((path.get(i).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getSourceSwitch()) &&
											path.get(i+1).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getDestinationSwitch())) || 
											(path.get(i).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getDestinationSwitch()) &&
													path.get(i+1).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getSourceSwitch()))){
										virtualEdgeSC = tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j);
										if(virtualEdgeSC != null){
											if(switchesPath.equals("")){
												if(!virtualEdgeSC.getSourceSwitch().equals(virtualSrcSwitch)){
													switchesPath = Utils.reverseSwitches(virtualEdgeSC.getWorkingPath());
												}else{
													switchesPath = virtualEdgeSC.getWorkingPath();
												}
											} else{
												switchesPath = Utils.concatEdges(switchesPath,virtualEdgeSC.getWorkingPath());
											}
										}
									}
								}
							}
							String[] switches = switchesPath.split(":");
							if(srcSw.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[0])).getSw().getId()) &&
									swDst.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[switches.length-1])).getSw().getId())){
								for (int i1 = 0; i1 < switches.length; i1++){
									pathServer.add(this.serverIdServerMap.get(Integer.parseInt(switches[i1])));
								}
								foundEdgeSCDirect = true;
							}else{
								if(srcSw.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[switches.length-1])).getSw().getId()) && 
										swDst.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[0])).getSw().getId())){
									for (int i1 = 0; i1 < switches.length; i1++){

										pathServer.add(this.serverIdServerMap.get(Integer.parseInt(switches[i1])));
									}
									foundEdgeSCReverse = true;
								}
							}
							if(foundEdgeSCDirect){
								this.writeFlowModWorkingPathDirectOrderNew(m, srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork, 
										pathServer, virtualSrcSwitch, virtualDstSwitch, start);
							} else{
								if(foundEdgeSCReverse){
									this.writeFlowModWorkingPathReverseOrderNew(m, srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork, 
											pathServer, virtualSrcSwitch, virtualDstSwitch, start);
								}
							}
						}
					}
				}
			}
		} else {
			return;
		}
	}

	private void writeFlowModWorkingPath(IOFSwitch srcSw, IOFSwitch swDst, Match m, OFPort inPortPI, TenantVirtualNetwork tenantVirtualNetwork, IPv4Address ipAddressDst, IPv4Address ipAddressSource, OFFlowModCommand add,
			OFBufferId noBuffer, OFPort outPort) {

		Server3 srcServer, dstServer;
		MacAddress srcMac = m.get(MatchField.ETH_SRC);
		MacAddress dstMac = m.get(MatchField.ETH_DST);
		VlanVid vlan = m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid();
		ArrayList<Server3> pathServer = new ArrayList<Server3>();
		LinkedList<VirtualSwitch> path;
		String switchesPath = "";
		int srcIdArrayListVirtualSwitch = tenantVirtualNetwork.getVirtualNetwork().getPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap().get(srcSw.getId());
		srcServer = tenantVirtualNetwork.getVirtualNetwork().getVirtualSwitchArrayList().get(srcIdArrayListVirtualSwitch).getWorkingSwitch();
		int dstIdArrayListVirtualSwitch = tenantVirtualNetwork.getVirtualNetwork().getPhysicalDatapathIdSwitchidArrayListVirtualSwitchMap().get(swDst.getId());
		dstServer = tenantVirtualNetwork.getVirtualNetwork().getVirtualSwitchArrayList().get(dstIdArrayListVirtualSwitch).getWorkingSwitch();
		if ((dstServer != null)&&(srcServer != null)){
			if(srcServer.equals(dstServer)){
				VirtualSwitch virtualSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), srcSw.getId());
				this.writeFlowModWorkingPathSameSwitch(srcSw, inPortPI, m, srcMac, dstMac, vlan, ipAddressSource, 
						ipAddressDst, tenantVirtualNetwork, srcServer, virtualSwitch, outPort);
			}else{
				VirtualSwitch virtualSrcSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), srcSw.getId());
				VirtualSwitch virtualDstSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(tenantVirtualNetwork.getVirtualNetwork(), swDst.getId());
				VirtualEdgeSC virtualEdgeSC = null;
				boolean foundEdgeSCDirect = false;
				boolean foundEdgeSCReverse = false;
				if(tenantVirtualNetwork != null){
					if(tenantVirtualNetwork.getVirtualNetwork() != null){
						if(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList() != null){
							path = tenantVirtualNetwork.getVirtualNetwork().getVirtualPath(virtualSrcSwitch, virtualDstSwitch);
							for (int i = 0; i < path.size()-1; i++){
								for (int j = 0; j < tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().size(); j++){
									if((path.get(i).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getSourceSwitch()) &&
											path.get(i+1).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getDestinationSwitch())) || 
											(path.get(i).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getDestinationSwitch()) &&
													path.get(i+1).equals(tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j).getSourceSwitch()))){

										virtualEdgeSC = tenantVirtualNetwork.getVirtualNetwork().getVirtualEdgeSCArrayList().get(j);
										if(virtualEdgeSC != null){
											if(switchesPath.equals("")){
												if(!virtualEdgeSC.getSourceSwitch().equals(virtualSrcSwitch)){
													switchesPath = Utils.reverseSwitches(virtualEdgeSC.getWorkingPath());
												}else{
													switchesPath = virtualEdgeSC.getWorkingPath();
												}
											} else{
												switchesPath = Utils.concatEdges(switchesPath,virtualEdgeSC.getWorkingPath());
											}
										}
									}
								}
							}
							String[] switches = switchesPath.split(":");
							if(srcSw.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[0])).getSw().getId()) &&
									swDst.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[switches.length-1])).getSw().getId())){
								for (int i1 = 0; i1 < switches.length; i1++){
									pathServer.add(this.serverIdServerMap.get(Integer.parseInt(switches[i1])));
								}
								foundEdgeSCDirect = true;
							}else{
								if(srcSw.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[switches.length-1])).getSw().getId()) && 
										swDst.getId().equals(this.environmentOfServers.getServerIdServerMap().get(Integer.parseInt(switches[0])).getSw().getId())){
									for (int i1 = 0; i1 < switches.length; i1++){
										pathServer.add(this.serverIdServerMap.get(Integer.parseInt(switches[i1])));
									}
									foundEdgeSCReverse = true;
								}
							}
							if(foundEdgeSCDirect){
								this.writeFlowModWorkingPathDirectOrderNew(m, srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork, 
										pathServer, virtualSrcSwitch, virtualDstSwitch);
							} else{
								if(foundEdgeSCReverse){
									this.writeFlowModWorkingPathReverseOrderNew(m, srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork, pathServer, virtualSrcSwitch, virtualDstSwitch);
								}
							}
						}
					}
				}
			}
		} else {
			return;
		}
	}

	private void writeFlowModWorkingPathSameSwitch(IOFSwitch srcSw,
			OFPort inPortPI, Match m, MacAddress srcMac, MacAddress dstMac,
			VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, TenantVirtualNetwork tenantVirtualNetwork, Server3 server, 
			VirtualSwitch virtualSwitch, OFPort outPort, long start) {

		if (inPortPI != null && outPort != null){
			WriteFlowModSameSwitch writeFlowModSameSwitch = new WriteFlowModSameSwitch(srcSw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, m, outPort, 
					server, virtualSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, 
					this.environmentOfTenants, "a", start);
			writeFlowModSameSwitch.start();

			if (LEARNING_SWITCH_REVERSE_FLOW) {
				Match matchPCK = Utils.createMatchFromFieldsReverse(outPort, vlan, m);
				WriteFlowModSameSwitch writeFlowModSameSwitchRev = new WriteFlowModSameSwitch(srcSw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, 
						inPortPI, server, virtualSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, 
						this.environmentOfTenants, "a", start);
				writeFlowModSameSwitchRev.start();
			}
		}
	}

	private void writeFlowModWorkingPathSameSwitch(IOFSwitch srcSw,
			OFPort inPortPI, Match m, MacAddress srcMac, MacAddress dstMac,
			VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, TenantVirtualNetwork tenantVirtualNetwork, Server3 server, 
			VirtualSwitch virtualSwitch, OFPort outPort) {

		if (inPortPI != null && outPort != null){
			WriteFlowModSameSwitch writeFlowModSameSwitch = new WriteFlowModSameSwitch(srcSw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, m, outPort, 
					server, virtualSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, 
					this.environmentOfTenants, "a");
			writeFlowModSameSwitch.start();

			if (LEARNING_SWITCH_REVERSE_FLOW) {
				Match matchPCK = Utils.createMatchFromFieldsReverse(outPort, vlan, m);
				WriteFlowModSameSwitch writeFlowModSameSwitchRev = new WriteFlowModSameSwitch(srcSw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, 
						inPortPI, server, virtualSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, this.cookieVirtualNetworkMap, 
						this.environmentOfTenants, "a");
				writeFlowModSameSwitchRev.start();
			}
		}
	}

	private void writeFlowModWorkingPathDirectOrderNew(Match m, MacAddress srcMac,
			MacAddress dstMac, VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, 
			TenantVirtualNetwork tenantVirtualNetwork, ArrayList<Server3> pathServer, VirtualSwitch virtualSrcSwitch, 
			VirtualSwitch virtualDstSwitch, long start) {

		Host srcHost = null;
		Host dstHost = null;
		OFPort outPort = null;
		OFPort inPort = null;
		OFPort outPortDst = null;
		OFPort outPortSrc = null;
		int indexOfSrcServer = 0;
		int indexOfDstServer = pathServer.size() -1;

		for(int i = 0; i < pathServer.size(); i++){
			if (i == indexOfSrcServer ){
				inPort = null;
				outPort = null;
				outPortSrc = null;
				if (pathServer.get(indexOfSrcServer).getSw().getId() != null && pathServer.get(indexOfSrcServer+1).getSw().getId() != null && 
						this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
								pathServer.get(indexOfSrcServer+1).getSw().getId())) != null){

					outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
							pathServer.get(indexOfSrcServer+1).getSw().getId())).getSrcPort();
				}
				if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork) != null){
					inPort = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
				}
				if (pathServer.get(indexOfDstServer).getSw() != null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork) != null){
					outPortDst = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
				}	
				if((outPort != null) && (inPort != null)){
					srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), 
							getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork )));
					dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), outPortDst));
					srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
					dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
					Match matchPCK = Utils.createMatchFromFieldsDirect(inPort, srcMac, dstMac, vlan, pathServer.get(indexOfSrcServer).getSw());
					WriteFlowModL2IN writeFlowModL2IN = new WriteFlowModL2IN(pathServer.get(indexOfSrcServer).getSw(), pathServer.get(indexOfDstServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER,
							matchPCK, outPort, outPortDst, true, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
							this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
							macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
							hostIdMacCount, virtualMacAddressRaw, "a", start);
					writeFlowModL2IN.start();
					if (LEARNING_SWITCH_REVERSE_FLOW) {
						Match matchPCKRev = Utils.createMatchFromFieldsReverse(outPort, vlan, matchPCK);
						WriteFlowModL2IN writeFlowModL2INRev = new WriteFlowModL2IN(pathServer.get(indexOfSrcServer).getSw(), pathServer.get(indexOfDstServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER,
								matchPCKRev, inPort, outPortDst, false, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
								this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
								macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
								hostIdMacCount, virtualMacAddressRaw, "a", start);
						writeFlowModL2INRev.start();
					}
				}
			} else{
				if (i == indexOfDstServer){
					inPort = null;
					outPort = null;
					outPortSrc = null;
					if (pathServer.get(indexOfDstServer).getSw().getId() != null && pathServer.get(indexOfDstServer-1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
									pathServer.get(indexOfDstServer-1).getSw().getId())) != null){

						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
								pathServer.get(indexOfDstServer-1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(indexOfDstServer)!=null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork ) != null){
						outPort = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork );
					}
					if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork ) != null){
						outPortSrc = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork );
					}
					if (inPort != null && outPort != null){
						srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), 
								getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork)));
						dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), outPortSrc));
						srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
						dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
						if ( srcHost != null && dstHost != null){
							if (srcHost.getVirtualMacAddress() != null && dstHost.getVirtualMacAddress() != null){
								Match matchPCK = Utils.createMatchFromFieldsDirect(inPort, dstMac, srcMac, vlan, pathServer.get(indexOfDstServer).getSw());
								WriteFlowModL2OUT writeFlowModL2OUT = new WriteFlowModL2OUT(pathServer.get(indexOfDstServer).getSw(), pathServer.get(indexOfSrcServer).getSw(), 
										OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, outPort, outPortSrc, false, pathServer.get(indexOfDstServer), virtualDstSwitch, 
										tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
										this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
										macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
										hostIdMacCount, virtualMacAddressRaw, "a", start);
								writeFlowModL2OUT.start();
								if (LEARNING_SWITCH_REVERSE_FLOW) {
									Match matchPCKRev = Utils.createMatchFromFieldsReverse(outPort, vlan, matchPCK);
									WriteFlowModL2OUT writeFlowModL2OUTRev = new WriteFlowModL2OUT(pathServer.get(indexOfDstServer).getSw(), pathServer.get(indexOfSrcServer).getSw(), 
											OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, inPort, outPortSrc, true, pathServer.get(indexOfDstServer), virtualDstSwitch, 
											tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
											this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
											macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
											hostIdMacCount, virtualMacAddressRaw, "a", start);
									writeFlowModL2OUTRev.start();
								}
							}
						}
					}
				} else {
					inPort = null;
					outPort = null;
					outPortSrc = null;

					if (pathServer.get(i).getSw().getId() != null && pathServer.get(i+1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
									pathServer.get(i+1).getSw().getId())) != null &&
									this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
											pathServer.get(i+1).getSw().getId())).getSrcPort() != null ){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i+1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(i).getSw().getId() != null && pathServer.get(i-1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
									pathServer.get(i-1).getSw().getId())) != null &&
									this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
											pathServer.get(i-1).getSw().getId())).getSrcPort() != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i-1).getSw().getId())).getSrcPort();
					}
					if (inPort != null && outPort != null){
						Match matchPCK = Utils.createMatchFromFieldsDirect(inPort, srcHost.getVirtualMacAddress(), dstHost.getVirtualMacAddress(), vlan, pathServer.get(i).getSw());
						WriteFlowMod writeFlowMod = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, 
								outPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a", start);
						writeFlowMod.start();
						if (LEARNING_SWITCH_REVERSE_FLOW) {
							Match matchPCKRev = Utils.createMatchFromFieldsReverse(outPort, vlan, matchPCK);
							WriteFlowMod writeFlowModRev = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, 
									inPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a", start);
							writeFlowModRev.start();
						}
					}
				}
			}
		}
	}

	private void writeFlowModWorkingPathDirectOrderNew(Match m, MacAddress srcMac,
			MacAddress dstMac, VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, TenantVirtualNetwork tenantVirtualNetwork, ArrayList<Server3> pathServer, VirtualSwitch virtualSrcSwitch, VirtualSwitch virtualDstSwitch) {

		Host srcHost = null;
		Host dstHost = null;
		OFPort outPort = null;
		OFPort inPort = null;
		OFPort outPortDst = null;
		OFPort outPortSrc = null;
		int indexOfSrcServer = 0;
		int indexOfDstServer = pathServer.size() -1;

		for(int i = 0; i < pathServer.size(); i++){
			if (i == indexOfSrcServer ){
				inPort = null;
				outPort = null;
				outPortSrc = null;
				if (pathServer.get(indexOfSrcServer).getSw().getId() != null && pathServer.get(indexOfSrcServer+1).getSw().getId() != null && 
						this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
								pathServer.get(indexOfSrcServer+1).getSw().getId())) != null){
					outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
							pathServer.get(indexOfSrcServer+1).getSw().getId())).getSrcPort();
				}
				if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork) != null){
					inPort = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
				}
				if (pathServer.get(indexOfDstServer).getSw() != null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork) != null){
					outPortDst = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
				}	
				if((outPort != null) && (inPort != null)){
					srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), 
							getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork )));
					dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), outPortDst));
					srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
					dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
					Match matchPCK = Utils.createMatchFromFieldsDirect(inPort, srcMac, dstMac, vlan, pathServer.get(indexOfSrcServer).getSw());
					WriteFlowModL2IN writeFlowModL2IN = new WriteFlowModL2IN(pathServer.get(indexOfSrcServer).getSw(), pathServer.get(indexOfDstServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER,
							matchPCK, outPort, outPortDst, true, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
							this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
							macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
							hostIdMacCount, virtualMacAddressRaw, "a");
					writeFlowModL2IN.start();
					if (LEARNING_SWITCH_REVERSE_FLOW) {
						Match matchPCKRev = Utils.createMatchFromFieldsReverse(outPort, vlan, matchPCK);
						WriteFlowModL2IN writeFlowModL2INRev = new WriteFlowModL2IN(pathServer.get(indexOfSrcServer).getSw(), pathServer.get(indexOfDstServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER,
								matchPCKRev, inPort, outPortDst, false, pathServer.get(indexOfSrcServer), virtualSrcSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
								this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
								macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
								hostIdMacCount, virtualMacAddressRaw, "a");
						writeFlowModL2INRev.start();
					}
				}
			} else{
				if (i == indexOfDstServer){
					inPort = null;
					outPort = null;
					outPortSrc = null;
					if (pathServer.get(indexOfDstServer).getSw().getId() != null && pathServer.get(indexOfDstServer-1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
									pathServer.get(indexOfDstServer-1).getSw().getId())) != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
								pathServer.get(indexOfDstServer-1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(indexOfDstServer)!=null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork ) != null){
						outPort = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork );
					}
					if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork ) != null){
						outPortSrc = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork );
					}
					if (inPort != null && outPort != null){
						srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), 
								getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork)));
						dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), outPortSrc));
						srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
						dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
						if ( srcHost != null && dstHost != null){
							if (srcHost.getVirtualMacAddress() != null && dstHost.getVirtualMacAddress() != null){
								Match matchPCK = Utils.createMatchFromFieldsDirect(inPort, dstMac, srcMac, vlan, pathServer.get(indexOfDstServer).getSw());
								WriteFlowModL2OUT writeFlowModL2OUT = new WriteFlowModL2OUT(pathServer.get(indexOfDstServer).getSw(), pathServer.get(indexOfSrcServer).getSw(), 
										OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, outPort, outPortSrc, false, pathServer.get(indexOfDstServer), virtualDstSwitch, 
										tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
										this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
										macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
										hostIdMacCount, virtualMacAddressRaw, "a");
								writeFlowModL2OUT.start();
								if (LEARNING_SWITCH_REVERSE_FLOW) {
									Match matchPCKRev = Utils.createMatchFromFieldsReverse(outPort, vlan, matchPCK);
									WriteFlowModL2OUT writeFlowModL2OUTRev = new WriteFlowModL2OUT(pathServer.get(indexOfDstServer).getSw(), pathServer.get(indexOfSrcServer).getSw(), 
											OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, inPort, outPortSrc, true, pathServer.get(indexOfDstServer), virtualDstSwitch, 
											tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
											this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
											macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
											hostIdMacCount, virtualMacAddressRaw, "a");
									writeFlowModL2OUTRev.start();
								}
							}
						}
					}
				} else {
					inPort = null;
					outPort = null;
					outPortSrc = null;
					if (pathServer.get(i).getSw().getId() != null && pathServer.get(i+1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
									pathServer.get(i+1).getSw().getId())).getSrcPort() != null ){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i+1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(i).getSw().getId() != null && pathServer.get(i-1).getSw().getId() != null && 
							this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
									pathServer.get(i-1).getSw().getId())).getSrcPort() != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i-1).getSw().getId())).getSrcPort();
					}
					if (inPort != null && outPort != null){
						Match matchPCK = Utils.createMatchFromFieldsDirect(inPort, srcHost.getVirtualMacAddress(), dstHost.getVirtualMacAddress(), vlan, pathServer.get(i).getSw());
						WriteFlowMod writeFlowMod = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, outPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a");
						writeFlowMod.start();
						if (LEARNING_SWITCH_REVERSE_FLOW) {
							Match matchPCKRev = Utils.createMatchFromFieldsReverse(outPort, vlan, matchPCK);
							WriteFlowMod writeFlowModRev = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, inPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a");
							writeFlowModRev.start();
						}
					}
				}
			}
		}
	}

	private void writeFlowModWorkingPathReverseOrderNew(Match m, MacAddress srcMac,
			MacAddress dstMac, VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, TenantVirtualNetwork tenantVirtualNetwork, 
			ArrayList<Server3> pathServer, VirtualSwitch virtualSrcSwitch, VirtualSwitch virtualDstSwitch, long start) {

		Host srcHost = null;
		Host dstHost = null;
		OFPort inPort = null;
		OFPort outPort = null;
		OFPort outPortDst = null;
		OFPort outPortSrc = null;
		int indexOfSrcServer = pathServer.size() -1;
		int indexOfDstServer = 0;

		for(int i = indexOfSrcServer; i >= indexOfDstServer; i--){
			if (i == indexOfSrcServer ){
				if (pathServer.get(indexOfSrcServer).getSw().getId() != null && pathServer.get(indexOfSrcServer-1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
						pathServer.get(indexOfSrcServer-1).getSw().getId())).getSrcPort() != null){
					outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
							pathServer.get(indexOfSrcServer-1).getSw().getId())).getSrcPort();
				}
				if (pathServer.get(indexOfSrcServer).getSw() != null && this.getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork ) != null){
					inPort = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
				}
				if (pathServer.get(indexOfDstServer).getSw() != null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork ) != null){
					outPortDst = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork );

				}
				if (inPort != null && outPort != null){
					srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), 
							getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork )));
					dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), outPortDst));
					srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
					dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
					Match matchPCK = Utils.createMatchFromFieldsDirect(outPort, dstMac, srcMac, vlan, pathServer.get(indexOfSrcServer).getSw());
					WriteFlowModL2OUTRev writeFlowModL2OUTRev = new WriteFlowModL2OUTRev(pathServer.get(indexOfDstServer).getSw(), 
							pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, inPort, 
							outPortDst, true, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
							this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
							macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
							hostIdMacCount, virtualMacAddressRaw, "a", start);
					writeFlowModL2OUTRev.start();
					if (LEARNING_SWITCH_REVERSE_FLOW) {
						Match matchPCKRev = Utils.createMatchFromFieldsReverse(inPort, vlan, matchPCK);
						WriteFlowModL2OUTRev writeFlowModL2OUTRev2 = new WriteFlowModL2OUTRev(pathServer.get(indexOfDstServer).getSw(), 
								pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, outPort, 
								outPortDst, false, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
								this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
								macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
								hostIdMacCount, virtualMacAddressRaw, "a", start);
						writeFlowModL2OUTRev2.start();
					}
				}
			} else{
				if (i == indexOfDstServer ){
					inPort = null;
					outPort = null;
					outPortSrc = null;
					if (pathServer.get(indexOfDstServer).getSw().getId() != null && pathServer.get(indexOfDstServer+1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
							pathServer.get(indexOfDstServer+1).getSw().getId())).getSrcPort() != null){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
								pathServer.get(indexOfDstServer+1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(indexOfDstServer).getSw() != null && this.getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork) != null){
						inPort = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
					}
					if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork) != null){
						outPortSrc = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
					}
					if (inPort != null && outPort != null){
						srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), 
								getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork)));
						dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), outPortSrc));
						srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
						dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
						if ( srcHost != null && dstHost != null){
							if (srcHost.getVirtualMacAddress() != null && dstHost.getVirtualMacAddress() != null){
								Match matchPCK = Utils.createMatchFromFieldsDirect(inPort, dstMac, srcMac, vlan, pathServer.get(indexOfSrcServer).getSw());
								WriteFlowModL2IN writeFlowModL2IN = new WriteFlowModL2IN(pathServer.get(indexOfDstServer).getSw(), 
										pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, outPort, outPortSrc, 
										true, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
										this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
										macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
										hostIdMacCount, virtualMacAddressRaw, "a", start);
								writeFlowModL2IN.start();
								if (LEARNING_SWITCH_REVERSE_FLOW) {
									Match matchPCKRev = Utils.createMatchFromFieldsReverse(outPort, vlan, matchPCK);
									WriteFlowModL2IN writeFlowModL2INRev = new WriteFlowModL2IN(pathServer.get(indexOfDstServer).getSw(), 
											pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, inPort, outPortSrc, 
											false, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
											this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
											macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
											hostIdMacCount, virtualMacAddressRaw, "a", start);
									writeFlowModL2INRev.start();
								}
							}
						}
					}
				} else {
					inPort = null;
					outPort = null;
					if(pathServer.get(i).getSw().getId() != null && pathServer.get(i+1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
							pathServer.get(i+1).getSw().getId())).getSrcPort() != null){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i+1).getSw().getId())).getSrcPort();
					}
					if(pathServer.get(i).getSw().getId() != null && pathServer.get(i-1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
							pathServer.get(i-1).getSw().getId())).getSrcPort() != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i-1).getSw().getId())).getSrcPort();
					}
					if (inPort != null && outPort != null){
						Match matchPCK = Utils.createMatchFromFieldsReverse(outPort, dstHost.getVirtualMacAddress(), srcHost.getVirtualMacAddress(), vlan, pathServer.get(i).getSw());
						WriteFlowMod writeFlowMod = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, inPort, 
								pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a", start);
						writeFlowMod.start();
						if (LEARNING_SWITCH_REVERSE_FLOW) {
							Match matchPCKRev = Utils.createMatchFromFieldsReverse(inPort, vlan, matchPCK);
							WriteFlowMod writeFlowModRev = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, outPort, 
									pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a", start);
							writeFlowModRev.start();
						}
					}
				}
			}
		}
	}

	private void writeFlowModWorkingPathReverseOrderNew(Match m, MacAddress srcMac,
			MacAddress dstMac, VlanVid vlan, IPv4Address ipAddressSource, IPv4Address ipAddressDst, TenantVirtualNetwork tenantVirtualNetwork, ArrayList<Server3> pathServer, VirtualSwitch virtualSrcSwitch, VirtualSwitch virtualDstSwitch) {

		Host srcHost = null;
		Host dstHost = null;
		OFPort inPort = null;
		OFPort outPort = null;
		OFPort outPortDst = null;
		OFPort outPortSrc = null;
		int indexOfSrcServer = pathServer.size() -1;
		int indexOfDstServer = 0;

		for(int i = indexOfSrcServer; i >= indexOfDstServer; i--){
			if (i == indexOfSrcServer ){
				if (pathServer.get(indexOfSrcServer).getSw().getId() != null && pathServer.get(indexOfSrcServer-1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
						pathServer.get(indexOfSrcServer-1).getSw().getId())).getSrcPort() != null){

					outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfSrcServer).getSw().getId(), 
							pathServer.get(indexOfSrcServer-1).getSw().getId())).getSrcPort();
				}
				if (pathServer.get(indexOfSrcServer).getSw() != null && this.getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork ) != null){
					inPort = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
				}
				if (pathServer.get(indexOfDstServer).getSw() != null && getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork ) != null){
					outPortDst = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork );
				}
				if (inPort != null && outPort != null){
					srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), 
							getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork )));
					dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), outPortDst));
					srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
					dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
					Match matchPCK = Utils.createMatchFromFieldsDirect(outPort, dstMac, srcMac, vlan, pathServer.get(indexOfSrcServer).getSw());
					WriteFlowModL2OUTRev writeFlowModL2OUTRev = new WriteFlowModL2OUTRev(pathServer.get(indexOfDstServer).getSw(), 
							pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, inPort, 
							outPortDst, true, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
							this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
							macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
							hostIdMacCount, virtualMacAddressRaw, "a");
					writeFlowModL2OUTRev.start();
					if (LEARNING_SWITCH_REVERSE_FLOW) {
						Match matchPCKRev = Utils.createMatchFromFieldsReverse(inPort, vlan, matchPCK);
						WriteFlowModL2OUTRev writeFlowModL2OUTRev2 = new WriteFlowModL2OUTRev(pathServer.get(indexOfDstServer).getSw(), 
								pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, outPort, 
								outPortDst, false, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
								this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
								macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
								hostIdMacCount, virtualMacAddressRaw, "a");
						writeFlowModL2OUTRev2.start();
					}
				}
			} else{
				if (i == indexOfDstServer ){
					inPort = null;
					outPort = null;
					outPortSrc = null;

					if (pathServer.get(indexOfDstServer).getSw().getId() != null && pathServer.get(indexOfDstServer+1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
							pathServer.get(indexOfDstServer+1).getSw().getId())).getSrcPort() != null){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(indexOfDstServer).getSw().getId(), 
								pathServer.get(indexOfDstServer+1).getSw().getId())).getSrcPort();
					}
					if (pathServer.get(indexOfDstServer).getSw() != null && this.getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork) != null){
						inPort = getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork);
					}
					if (pathServer.get(indexOfSrcServer).getSw() != null && getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork) != null){
						outPortSrc = getFromPortMap(pathServer.get(indexOfSrcServer).getSw(), srcMac, dstMac, vlan, ipAddressSource, ipAddressDst, tenantVirtualNetwork);
					}
					if (inPort != null && outPort != null){
						srcHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfDstServer).getSw().getId(), 
								getFromPortMap(pathServer.get(indexOfDstServer).getSw(), dstMac, srcMac, vlan, ipAddressDst, ipAddressSource, tenantVirtualNetwork)));
						dstHost = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(pathServer.get(indexOfSrcServer).getSw().getId(), outPortSrc));
						srcHost.setVirtualMacAddress(this.virtualMacAddress(srcHost, tenantVirtualNetwork));
						dstHost.setVirtualMacAddress(this.virtualMacAddress(dstHost, tenantVirtualNetwork));
						if ( srcHost != null && dstHost != null){
							if (srcHost.getVirtualMacAddress() != null && dstHost.getVirtualMacAddress() != null){
								Match matchPCK = Utils.createMatchFromFieldsDirect(inPort, dstMac, srcMac, vlan, pathServer.get(indexOfSrcServer).getSw());
								WriteFlowModL2IN writeFlowModL2IN = new WriteFlowModL2IN(pathServer.get(indexOfDstServer).getSw(), 
										pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, outPort, outPortSrc, 
										true, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
										this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
										macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
										hostIdMacCount, virtualMacAddressRaw, "a");
								writeFlowModL2IN.start();
								if (LEARNING_SWITCH_REVERSE_FLOW) {
									Match matchPCKRev = Utils.createMatchFromFieldsReverse(outPort, vlan, matchPCK);
									WriteFlowModL2IN writeFlowModL2INRev = new WriteFlowModL2IN(pathServer.get(indexOfDstServer).getSw(), 
											pathServer.get(indexOfSrcServer).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, inPort, outPortSrc, 
											false, pathServer.get(indexOfDstServer), virtualDstSwitch, tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualSwitchMap, 
											this.cookieVirtualNetworkMap, this.environmentOfTenants, this.environmentOfServers,
											macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
											hostIdMacCount, virtualMacAddressRaw, "a");
									writeFlowModL2INRev.start();
								}
							}
						}
					}
				} else {
					inPort = null;
					outPort = null;
					if(pathServer.get(i).getSw().getId() != null && pathServer.get(i+1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
							pathServer.get(i+1).getSw().getId())).getSrcPort() != null){
						outPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i+1).getSw().getId())).getSrcPort();
					}
					if(pathServer.get(i).getSw().getId() != null && pathServer.get(i-1).getSw().getId() != null && this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
							pathServer.get(i-1).getSw().getId())).getSrcPort() != null){
						inPort = this.datapathIdSwitchSrcSwitchDstLinkMap.get(new DatapathIdSwitchesSrcDst3(pathServer.get(i).getSw().getId(), 
								pathServer.get(i-1).getSw().getId())).getSrcPort();
					}
					if (inPort != null && outPort != null){
						Match matchPCK = Utils.createMatchFromFieldsReverse(outPort, dstHost.getVirtualMacAddress(), srcHost.getVirtualMacAddress(), vlan, pathServer.get(i).getSw());
						WriteFlowMod writeFlowMod = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCK, inPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a");
						writeFlowMod.start();
						if (LEARNING_SWITCH_REVERSE_FLOW) {
							Match matchPCKRev = Utils.createMatchFromFieldsReverse(inPort, vlan, matchPCK);
							WriteFlowMod writeFlowModRev = new WriteFlowMod(pathServer.get(i).getSw(), OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, matchPCKRev, outPort, pathServer.get(i), tenantVirtualNetwork, this.cookieFlowEntryMap, this.cookieVirtualNetworkMap, "a");
							writeFlowModRev.start();
						}
					}
				}
			}
		}
	}

	private Command processFlowRemovedMessageVirtualTopo(IOFSwitch sw, OFFlowRemoved flowRemovedMessage) {

		log.info("processFlowRemovedMessageVirtualTopo cookie: {}", flowRemovedMessage.getCookie());
		if ((flowRemovedMessage.getCookie().equals(U64.of(SiriusNetHypervisor.LEARNING_SWITCH_COOKIE)) && Utils.isSiriusCookie(flowRemovedMessage.getCookie().getValue()))) {
			return Command.CONTINUE;
		}
		if (log.isTraceEnabled()) {
			log.trace("{} flow entry removed {}", sw, flowRemovedMessage);
		}
		Match match = flowRemovedMessage.getMatch();
		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.ETH_SRC, match.get(MatchField.ETH_DST))                         
		.setExact(MatchField.ETH_DST, match.get(MatchField.ETH_SRC));
		if (match.get(MatchField.VLAN_VID) != null) {
			mb.setExact(MatchField.VLAN_VID, match.get(MatchField.VLAN_VID));                    
		}
		EthType ethType = flowRemovedMessage.getMatch().get(MatchField.ETH_TYPE);
		if(ethType != null){
			if(ethType.equals(EthType.ARP)){
				mb.setExact(MatchField.ETH_TYPE, EthType.ARP);
			}
		}
		String cookieRemovedFromVirtualSwitch = this.removeFlowEntryFromVirtualSwitch(sw, flowRemovedMessage);
		if ( cookieRemovedFromVirtualSwitch != null){
			log.info("Flow Entry with cookie -{}- was removed from virtual switch -{}-.", flowRemovedMessage.getCookie(), cookieRemovedFromVirtualSwitch);
		}
		String cookieRemovedFromPhysicalSwitch = this.removeFlowEntryFromPhysicalSwitch(sw, flowRemovedMessage);
		if (cookieRemovedFromPhysicalSwitch != null){
			log.info("Flow Entry with cookie -{}- was removed from physical switch {}.", flowRemovedMessage.getCookie(), cookieRemovedFromPhysicalSwitch);
		}
		WriteFlowModRemoveFlow writeFlowModRemoveFlow = new WriteFlowModRemoveFlow(sw, OFFlowModCommand.DELETE, OFBufferId.NO_BUFFER, mb.build(), match.get(MatchField.IN_PORT), sw.getId().toString());
		writeFlowModRemoveFlow.start();
		//this.writeFlowModRemoveFlow(sw, OFFlowModCommand.DELETE, OFBufferId.NO_BUFFER, mb.build(), match.get(MatchField.IN_PORT), null, null, null);
		return Command.CONTINUE;
	}

	private String removeFlowEntryFromVirtualSwitch(IOFSwitch sw, OFFlowRemoved flowRemovedMessage) {

		long cookie = flowRemovedMessage.getCookie().getValue();
		long flowEntryId = Utils.flowIdFromCookie(cookie);
		FlowEntry flowEntry = null;
		//List<OFAction> ofActions = flowRemovedMessage.;
		//long switchId = Utils.switchIdFromCookie(cookie);
		//VirtualSwitch virtualSwitch = this.getVirtualSwitch(switchId);
		VirtualSwitch virtualSwitch = this.cookieVirtualSwitchMap.get(cookie);
		if (flowEntryId != 0L){
			//Test with OFAction null and remove this field in hashcode and equals to identify if will remove correctly
			flowEntry = this.cookieFlowEntryMap.get(cookie);
			if(virtualSwitch != null){
				if(virtualSwitch.getFlowTable() != null){
					if (virtualSwitch.getFlowTable().get(0) != null){
						if(virtualSwitch.getFlowTable().get(0).getFlowEntry()!= null){
							if(virtualSwitch.getFlowTable().get(0).getFlowEntry().remove(flowEntry)){

								virtualSwitch.setFlowSize(virtualSwitch.getFlowSize() - 1);
								System.out.println(flowEntry.toString());
								//TODO - Local para criar o arquivo xml com os flows do(s) switch(es) virtual(ais), conversar com o Eric quando da sua volta
								//System.out.println(virtualSwitch.getFlowTable().get(0).toString());
								return virtualSwitch.getName();
							}
						}
					}
				}
			}
		}
		return null;
	}

	private String removeFlowEntryFromPhysicalSwitch(IOFSwitch sw, OFFlowRemoved flowRemovedMessage) {

		long cookie = flowRemovedMessage.getCookie().getValue();
		long flowEntryId = Utils.flowIdFromCookie(cookie);
		FlowEntry flowEntry = null;
		Server3 server = this.datapathIdServerMap.get(sw.getId());

		if (flowEntryId != 0L){
			//Test with OFAction null and remove this field in hashcode and equals to identify if will remove correctly
			flowEntry = this.cookieFlowEntryMap.get(cookie);
			if(server != null){
				if(server.getOpenVSwitch() != null){
					if(server.getOpenVSwitch().getFlowTable() != null){
						if(server.getOpenVSwitch().getFlowTable().get(0) != null){
							if(server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry() != null){
								if(server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry().remove(flowEntry)){
									server.setFlowSize(server.getFlowSize()-1);
									//TODO to uncomment
									//System.out.println(flowEntry.toString());
									//TODO - Local para criar o arquivo xml com os flows do(s) switch(es) físico(s), conversar com o Eric quando da sua volta
									//System.out.println(server.getOpenVSwitch().getFlowTable().get(0).toString());
									return server.getDescription();
								}	
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * The part processing before the writeFlowMod based on the shortest path algorithm
	 * @param srcSw The switch where the packet starts
	 * @param swDst 
	 * @param m The Match
	 * @param pi The OFPacketIn
	 * @param tenant The tenant that is communicating
	 * @param ipAddressDst The destination IP address of the VM
	 * @param ipAddressSource The source IP address of the VM
	 * @param eth The Ethernet packet
	 * @param add The OFFlowModCommand 
	 * @param noBuffer The OFBufferId
	 */

	public void switchActivated(DatapathId switchID) {

		//if(!iOFSwitchArray.contains(switchService.getActiveSwitch(switchID))){

//		int indexIOFSwitch = this.arrayContainsIOFSwitch(this.iOFSwitchArray, switchService.getActiveSwitch(switchID));
//		int indexIOFSwitch = iOFSwitchArray.indexOf(switchService.getActiveSwitch(switchID));
//		if(indexIOFSwitch == -1){
//			iOFSwitchArray.add(switchService.getActiveSwitch(switchID));
//		}else{
//			iOFSwitchArray.remove(indexIOFSwitch);
//			iOFSwitchArray.add(switchService.getActiveSwitch(switchID));
//		}
		log.info("-------------------------SwitchActivated server--------------------: -{}-", switchID.toString());

		this.environmentOfServers.getDatapathIdIOFSwitchMap().put(switchID, getSwitchService().getActiveSwitch(switchID));
//		this.datapathIdServerMap.put(switchID, server);
//		server = this.environmentOfServers.setIOFSwitchGetServer(sw);
//
//		if (server != null){
//
//			log.info("-------------------------SwitchActivated server--------------------: -{}-", server.getDescription() );
//			this.populateSwitchTenantMap(server);
//			this.datapathIdServerMap.put(sw.getId(), server);
//			Server3 server;
//			IOFSwitch sw = switchService.getActiveSwitch(switchID);
//			this.environmentOfServices.getDatapathIdIOFSwitchMap().put(switchID, sw);
//			server = this.environmentOfServices.setIOFSwitchGetServer(sw);
//			if (server != null){
//				log.info("-------------------------SwitchActivated server--------------------: -{}-", server.getDescription() );
//				this.populateSwitchTenantMap(server);
//				this.datapathIdServerMap.put(switchID, server);
//		}
	}

	public void handleIOFSwitches(){

//		for(int i = 0; i < this.iOFSwitchArray.size(); i++){
//			for (int j = 0; j < 4; j++){
//				this.iOFSwitchArray.retainAll(c);
//			}
//		}

		for (int i = 0; i < this.iOFSwitchArray.size(); i++){
			Server3 server;
			IOFSwitch sw = this.iOFSwitchArray.get(i);
			if(sw != null && sw.getId() != null){
				this.environmentOfServers.getDatapathIdIOFSwitchMap().put(sw.getId(), sw);
				server = this.environmentOfServers.setIOFSwitchGetServer(sw);
				if (server != null){
					log.info("-------------------------SwitchActivated server--------------------: -{}-", server.getDescription() );
					this.populateSwitchTenantMap(server);
					this.datapathIdServerMap.put(sw.getId(), server);
				}
			}
		}
	}

	public void switchRemoved(DatapathId switchID) {

		log.info("Switch {} was disconnected", switchID);
	}

	public void switchAdded(DatapathId switchID) {

//		log.info("***********************switchAdded server**********************************************: -{}-", switchID.toString() );
//		if(!iOFSwitchArray.contains(switchService.getActiveSwitch(switchID))){
//		int indexIOFSwitch = this.arrayContainsIOFSwitch(this.iOFSwitchArray, switchService.getActiveSwitch(switchID));
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		if(this.environmentOfServers.getPhysicalNetwork().getVertexes().size() != iOFSwitchArray.size()){

		int indexIOFSwitch = iOFSwitchArray.indexOf(getSwitchService().getActiveSwitch(switchID));
		if(indexIOFSwitch == -1){
			iOFSwitchArray.add(getSwitchService().getActiveSwitch(switchID));
		}else{
			iOFSwitchArray.remove(indexIOFSwitch);
			iOFSwitchArray.add(getSwitchService().getActiveSwitch(switchID));
		}

//		int indexIOFSwitch = iOFSwitchArray.indexOf(switchID);
//		if(indexIOFSwitch == -1){
//			iOFSwitchArray.add(switchService.getActiveSwitch(switchID));
//		}else{
//			iOFSwitchArray.remove(indexIOFSwitch);
//			iOFSwitchArray.add(switchService.getActiveSwitch(switchID));
//		}
	}

	public int arrayContainsIOFSwitch(ArrayList<IOFSwitch> iOFSwitchArray, IOFSwitch iOFSwitch){

		for(int i = 0; i < iOFSwitchArray.size(); i++){
			if(iOFSwitchArray.get(i) != null && iOFSwitchArray.get(i).getId() != null && iOFSwitch != null && iOFSwitch.getId() != null){
				if(iOFSwitchArray.get(i).getId().equals(iOFSwitch.getId())){
					return i;
				}
			}
		}
		return -1;
	}

	public void switchChanged(DatapathId switchID) {

		@SuppressWarnings("unused")
		IOFSwitch sw = getSwitchService().getActiveSwitch(switchID);
	}

	@SuppressWarnings("unused")
	public void switchPortChanged(DatapathId switchID, OFPortDesc portDesc, PortChangeType changeType) {

		log.info("***********************switchPortChanged**********************************************: -{}-", changeType.toString() );
		//IOFSwitch sw = switchService.getActiveSwitch(switchID);
		Server3 server = this.datapathIdServerMap.get(switchID);

		Host host = null;
		MacAddress macOfVM = null;
		IPv4Address ipOfVM = null;

		if((server != null)){
			if((changeType == PortChangeType.DOWN || changeType == PortChangeType.DELETE) && false){
				host = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(switchID, portDesc.getPortNo()));
				if(host != null){
					macOfVM = host.getMac();
					ipOfVM = host.getIpAddress();
				}
				if ((macOfVM != null) && (ipOfVM != null) && (!ipOfVM.equals(IPv4Address.of("0.0.0.0")))){
					this.removeFromPortMap(server.getSw(), portDesc.getPortNo());
					int i = this.environmentOfServers.getPhysicalNetwork().getHostList().indexOf(host);
					if(i != -1){
						this.environmentOfServers.getPhysicalNetwork().getHostList().get(i).setOperational(false);
					}
					if(this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(new HostLocation(switchID, portDesc.getPortNo())).
							getIpAddressHostArrayId() != null){
						int indexHostInVirtualNetwork = this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(new HostLocation(switchID, portDesc.getPortNo())).
								getIpAddressHostArrayId().get(ipOfVM);
						this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(new HostLocation(switchID, portDesc.getPortNo())).getHostList().get(indexHostInVirtualNetwork).setOperational(false);
					}
					/*
					this.environmentOfServers.getHostLocationHostMap().remove(new HostLocation(switchID, portDesc.getPortNo()));


					try{

						int i = this.environmentOfServers.getPhysicalNetwork().getVertexes().indexOf(server);
						int j = this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().indexOf(new Bridge3(this.environmentOfServers.getBRIDGE_NAME_OF_VMS()));
						int k = this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().indexOf(new Interfaces3(portDesc.getName()));

						if(i!= 0 && k != 0 && j != 0){

							this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().remove(k);
						}


					}catch (Exception e)
					{
						e.printStackTrace(System.err);
						System.exit(2);
					}
					 */
				}
			}

			if((changeType == PortChangeType.UP || changeType == PortChangeType.ADD) && false){
				host = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(switchID, portDesc.getPortNo()));
				if(host != null){
					macOfVM = host.getMac();
					ipOfVM = host.getIpAddress();
				}
			/*				
 			if(macOfVM != null){ 
				 if(this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(switchID, portDesc.getPortNo())) != null){
					if(macOfVM.equals(this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(switchID, portDesc.getPortNo())).getMac())){
						ipOfVM = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(switchID, portDesc.getPortNo())).getIpAddress();
					}
				}
			 */
				if ((macOfVM != null) && (ipOfVM != null)){
					this.addToPortMap(server.getSw(), macOfVM, VlanVid.ofVlan(0), portDesc.getPortNo(), ipOfVM);
					int i = this.environmentOfServers.getPhysicalNetwork().getHostList().indexOf(host);
					if(i != -1){
						this.environmentOfServers.getPhysicalNetwork().getHostList().get(i).setOperational(true);
					}
					if(this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(new HostLocation(switchID, portDesc.getPortNo())).
							getIpAddressHostArrayId() != null){

						int indexHostInVirtualNetwork = this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(new HostLocation(switchID, portDesc.getPortNo())).
								getIpAddressHostArrayId().get(ipOfVM);

						this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(new HostLocation(switchID, portDesc.getPortNo())).getHostList().get(indexHostInVirtualNetwork).setOperational(true);	

					}
					/*
					this.environmentOfServers.getHostLocationHostMap().put(new HostLocation(switchID, portDesc.getPortNo()),this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(switchID, portDesc.getPortNo())));
					try{
						Interfaces3 interfaces = new Interfaces3(portDesc.getName());
						int i = this.environmentOfServers.getPhysicalNetwork().getVertexes().indexOf(server);
						int j = this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().indexOf(new Bridge3(this.environmentOfServers.getBRIDGE_NAME_OF_VMS()));
						this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().add(interfaces);
						int k = this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().indexOf(interfaces);
						this.environmentOfServers.setInfoInterface(i, j, k, server, interfaces);
					}catch (Exception e)
					{
						e.printStackTrace(System.err);
						System.exit(2);
					}
					 */
				}
			}
		}
	}

	@Override
	public void linkDiscoveryUpdate(List<LDUpdate> updates) {
		for (LDUpdate updt : updates) {
			linkDiscoveryUpdate(updt);
		}
	}

	public void linkDiscoveryUpdate(LDUpdate updt) {
		switch (updt.getOperation()) {
		case LINK_UPDATED: {
			Link link = new Link(updt.getSrc(), updt.getSrcPort(), updt.getDst(), updt.getDstPort(), U64.ZERO);
			linkUpdated(link);
			break;
		}

		case LINK_REMOVED: {
			Link link = new Link(updt.getSrc(), updt.getSrcPort(), updt.getDst(), updt.getDstPort(), U64.ZERO);
			linkRemoved(link);
			break;
		}

		default: {
			break;
		}
		}
	}

	private synchronized void linkUpdated(Link link) {

//		int indexLink = this.linkArrayList.indexOf(link);
//		if(indexLink == -1){
//			//		if(!this.linkArrayList.contains(link)){
//			this.linkArrayList.add(link);
//		}
//		else{
//			iOFSwitchArray.remove(indexIOFSwitch);
//			iOFSwitchArray.add(switchService.getActiveSwitch(switchID));
//		}
//		this.linkArrayList.add(link);
//		this.populateInfoLinks(link);
		if(!link.getSrc().equals(link.getDst())){
			Link l = new Link();
			l.setSrc(DatapathId.of(link.getSrc().toString()));
			l.setSrcPort(OFPort.of(Integer.parseInt(link.getSrcPort().toString())));
			l.setDst(DatapathId.of(link.getDst().toString()));
			l.setDstPort(OFPort.of(Integer.parseInt(link.getDstPort().toString())));
			l.setLatency(U64.of(Long.parseLong(link.getLatency().getValue()+"")));
			this.datapathIdSwitchSrcSwitchDstLinkMap.put(new DatapathIdSwitchesSrcDst3(l.getSrc(), l.getDst()), l);
		}
	}

	private synchronized void populateInfoLinks(Link link) {

		Link l = new Link();
		if (link != null) {
			l.setSrc(DatapathId.of(link.getSrc().toString()));
			l.setSrcPort(OFPort.of(Integer.parseInt(link.getSrcPort().toString())));
			l.setDst(DatapathId.of(link.getDst().toString()));
			l.setDstPort(OFPort.of(Integer.parseInt(link.getDstPort().toString())));
			l.setLatency(U64.of(Long.parseLong(link.getLatency().getValue()+"")));
			if(this.datapathIdServerMap.get(link.getSrc())!=null && this.datapathIdServerMap.get(link.getDst())!=null){
				this.datapathIdSwitchSrcSwitchDstLinkMap.put(new DatapathIdSwitchesSrcDst3(l.getSrc(), l.getDst()), l);
				String srcServerDescription = this.datapathIdServerMap.get(link.getSrc()).getDescription();
				String srcServerHostName = this.datapathIdServerMap.get(link.getSrc()).getHostname();
				String dstServerDescription = this.datapathIdServerMap.get(link.getDst()).getDescription();
				String dstServerHostName = this.datapathIdServerMap.get(link.getDst()).getHostname();
				@SuppressWarnings("unused")
				int cost = Math.max((int)(100000000/this.datapathIdServerMap.get(link.getSrc()).getLinkSpeed()), (int)(100000000/this.datapathIdServerMap.get(link.getDst()).getLinkSpeed()));
				int weight = 100;
				EdgeSC lane = new EdgeSC("Link-"+srcServerDescription+"_"+srcServerHostName+"-"
						+dstServerDescription+"_"+dstServerHostName,this.datapathIdServerMap.get(link.getSrc()), 
						this.datapathIdServerMap.get(link.getDst()), weight);

//				if(!this.environmentOfServers.getEdgeSCApps().contains(lane)){
//					this.environmentOfServices.getEdgeSCApps().add(lane);
//
//					this.graphSCApp = new GraphSC(this.environmentOfServices.getPhysicalNetwork().getVertexes(), this.environmentOfServices.getEdgeSCApps());
//					this.dijkstra = new DijkstraAlgorithmSCApp(this.graphSCApp);
//					this.dijkstra.execute(this.environmentOfServices.getPhysicalNetwork().getVertexes().get(0));
//				}
			}
		}
	}

	private void linkRemoved(Link link) {

//TODO
//		this.datapathIdSwitchSrcSwitchDstLinkMap.remove(new DatapathIdSwitchesSrcDst3(link.getSrc(), link.getDst()));
//		String srcServerDescription = this.datapathIdServerMap.get(link.getSrc()).getDescription();
//		String srcServerHostName = this.datapathIdServerMap.get(link.getSrc()).getHostname();
//		String dstServerDescription = this.datapathIdServerMap.get(link.getDst()).getDescription();
//		String dstServerHostName = this.datapathIdServerMap.get(link.getDst()).getHostname();
//		@SuppressWarnings("unused")
//		int cost = Math.max((int)(100000000/this.datapathIdServerMap.get(link.getSrc()).getLinkSpeed()), (int)(100000000/this.datapathIdServerMap.get(link.getDst()).getLinkSpeed()));
//		int weight = 100;
//		EdgeSC lane = new EdgeSC("Link-"+srcServerDescription+"_"+srcServerHostName+"-"
//				+dstServerDescription+"_"+dstServerHostName,this.datapathIdServerMap.get(link.getSrc()), 
//				this.datapathIdServerMap.get(link.getDst()), weight);
//
//		this.environmentOfServices.getEdgeSCApps().remove(lane);
//
//		this.environmentOfServices.removeTunnelLink(this.datapathIdServerMap.get(link.getSrc()), this.datapathIdServerMap.get(link.getDst()));
//		this.graphSCApp = new GraphSC(this.environmentOfServices.getPhysicalNetwork().getVertexes(), this.environmentOfServices.getEdgeSCApps());
//		this.dijkstra = new DijkstraAlgorithmSCApp(this.graphSCApp);
//
//		this.dijkstra.execute(this.environmentOfServices.getPhysicalNetwork().getVertexes().get(0));

	}

	@Override
	public void switchDeactivated(DatapathId switchId) {

	}

	//Function responsible for monitoring the diretory where the files with new physical topology informations arrive.
	//xml and conf files are handle as physical topology
	@Override
	public void update(Observable watchDirectory, Object arg1) {

		if (watchDirectory instanceof WatchDirectory) {
			WatchDirectory watch = (WatchDirectory) watchDirectory;
			long time = watch.getTimeConfigFile();
			String fileChanged = watch.getFileChanged();
			Pattern regexPhysical = Pattern.compile("(?:/physical\\d*.xml|/physical.xml|/physical\\d*.conf|/physical.conf)");
			Matcher regexMatcherPhysical = regexPhysical.matcher(fileChanged);
			Pattern regexVirtual = Pattern.compile("(?:/request\\d*.xml|/request.xml|/request\\d*.conf|/request.conf)");
			Matcher regexMatcherVirtual = regexVirtual.matcher(fileChanged);
			if(time > this.timeStampCreatePhysicalNetwork && (regexMatcherPhysical.find())){
				this.timeStampCreatePhysicalNetwork = time;
				try {
					InputStream is = new FileInputStream(fileChanged);
					while (true){
						try {
							if (is.available() != 0){
								is.close();
								break;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					Thread.sleep(3300);
					environmentOfServers.initEnvironmentOfServers(fileChanged, false);
					Thread.sleep(3300);
					this.populateServerIDAndDatapathidToServer();
					Thread.sleep(3300);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
				this.canInitEnvironmentOfTenants = true;
			}
			if(time > this.timeStampCreateVirtualNetwork && (regexMatcherVirtual.find())){

				this.timeStampCreateVirtualNetwork = time;
				while(true){
					if(this.canInitEnvironmentOfTenants){
						break;
					}
				}
				InputStream is = null;
				try {
					is = new FileInputStream(fileChanged);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				while (true){
					try {
						if (is.available() != 0){
							is.close();
							break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
//				try {
//					if(environmentOfTenants.initEnvironmentOfTenantsRequest(fileChanged, this.environmentOfServers, this.socket, this.clientChangeInfoOrquestrator)){
//						//TODO uncomment
//						this.handleIOFSwitches();
//						this.populateInfoSwitchTenantMap2();
//						for(int i = 0; i < this.linkArrayList.size(); i++){
//							this.populateInfoLinks(this.linkArrayList.get(i));
//						}
//						this.populateDatapathIdSwitchSrcSwitchDstLinkMap();
//						//TODO uncomment
//						this.insertAllFlowsProactivily(this.environmentOfTenants, start);
//					}
//				} catch (Exception e){
//					e.printStackTrace();
//				}
//				log.info("Finish!!!!");
			}
		}
	}
public void instantiateTenant(Network network){
	try {
	if(environmentOfTenants.initEnvironmentOfTenantsRequest(network, this.environmentOfServers, this.socket, this.clientChangeInfoOrquestrator)){
		//TODO uncomment
//		this.handleIOFSwitches();
		this.populateInfoSwitchTenantMap2();
//		for(int i = 0; i < this.linkArrayList.size(); i++){
//
//			this.populateInfoLinks(this.linkArrayList.get(i));
//		}
		this.populateDatapathIdSwitchSrcSwitchDstLinkMap();
		//TODO uncomment
		Thread.sleep(5000);
//		this.insertAllFlowsProactivily(this.environmentOfTenants, start);
	}
	} catch (Exception e){
		e.printStackTrace();
	}
	
}

//	private void populateInfoSwitchTenantMap() {
//
//		Server3 server;
//
//		for (int i = 0; i < this.environmentOfServices.getPhysicalNetwork().getVertexes().size(); i++){
//
//			server = this.environmentOfServices.getPhysicalNetwork().getVertexes().get(i);
//
//			if (this.environmentOfServices.getDatapathIdIOFSwitchMap().get(server.getOpenVSwitch().getDatapathId()) != null){
//
//				//log.info("-------------------------SwitchActivated update update-------------------: -{}-", server.getDescription() );
//				this.populateSwitchTenantMap(server);
//				this.datapathIdServerMap.put(server.getOpenVSwitch().getDatapathId(), server);
//			}
//		}
//	}

	private void populateInfoSwitchTenantMap2() {

		Server3 server;
		populateSwitchTenantMapNotRealTimeInfoContainers();

		for(int i = 0; i < this.environmentOfTenants.getVirtualNetworkArrayList().size(); i++){
			for (int j = 0; j < this.environmentOfTenants.getVirtualNetworkArrayList().get(i).getVirtualSwitchArrayList().size(); j++){
				server = this.environmentOfTenants.getVirtualNetworkArrayList().get(i).getVirtualSwitchArrayList().get(j).getWorkingSwitch();
//				if (this.environmentOfServices.getDatapathIdIOFSwitchMap().get(server.getOpenVSwitch().getDatapathId()) != null){
				if(server.getOpenVSwitch() != null){
					if(server.getOpenVSwitch().getDatapathId() != null){
						log.info("-------------------------SwitchActivated update update-------------------: -{}-", server.getDescription() );
						this.populateSwitchTenantMap(server);
//						this.datapathIdServerMap.put(server.getOpenVSwitch().getDatapathId(), server);
//						this.serverIdServerMap.put(server.getServerId(), server);
					}
				}
			}
		}
	}

	private void populateServerIDAndDatapathidToServer(){

		for(int i = 0; i < this.environmentOfServers.getServerList().size(); i++){
			this.datapathIdServerMap.put(this.environmentOfServers.getServerList().get(i).getOpenVSwitch().getDatapathId(), this.environmentOfServers.getServerList().get(i));
			this.serverIdServerMap.put(this.environmentOfServers.getServerList().get(i).getServerId(), this.environmentOfServers.getServerList().get(i));
		}
	}

	private void populateDatapathIdSwitchSrcSwitchDstLinkMap(){

		for (int i = 0; i < this.environmentOfServers.getPhysicalNetwork().getVertexes().size(); i++){			
			for (int j = 0; j < this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().size(); j++){
				if(this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).getName().equals("br1")){
					for (int k = 0; k < this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).
							getArrayListInterfaces().size(); k++){
						if(this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).
								getArrayListInterfaces().get(k).getName().contains("gre")){

							for (int m = 0; m < this.environmentOfServers.getPhysicalNetwork().getVertexes().size(); m++){			
								for (int n = 0; n < this.environmentOfServers.getPhysicalNetwork().getVertexes().get(m).getOpenVSwitch().getArrayListBridge().size(); n++){
									if(this.environmentOfServers.getPhysicalNetwork().getVertexes().get(m).getOpenVSwitch().getArrayListBridge().get(n).getName().equals("br1")){
										for (int o = 0; o < this.environmentOfServers.getPhysicalNetwork().getVertexes().get(m).getOpenVSwitch().getArrayListBridge().get(n).
												getArrayListInterfaces().size(); o++){

											if(this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).
													getArrayListInterfaces().get(k).getName().equals(this.environmentOfServers.getPhysicalNetwork().getVertexes().get(m).getOpenVSwitch().
															getArrayListBridge().get(n).getArrayListInterfaces().get(o).getName()) && k != o){

												Link l = new Link();
												l.setSrc(this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getDatapathId());
												l.setSrcPort(this.environmentOfServers.getPhysicalNetwork().getVertexes().get(i).getOpenVSwitch().getArrayListBridge().get(j).
														getArrayListInterfaces().get(k).getOfport());
												l.setDst(this.environmentOfServers.getPhysicalNetwork().getVertexes().get(m).getOpenVSwitch().getDatapathId());
												l.setDstPort(this.environmentOfServers.getPhysicalNetwork().getVertexes().get(m).getOpenVSwitch().getArrayListBridge().get(n).
														getArrayListInterfaces().get(o).getOfport());
												l.setLatency(U64.of(0L));

												this.datapathIdSwitchSrcSwitchDstLinkMap.put(new DatapathIdSwitchesSrcDst3(l.getSrc(), l.getDst()), l);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
//		if (linkInfo != null) {
//		if (link != null) {
//
//			l.setSrc(DatapathId.of(link.getSrc().toString()));
//			l.setSrcPort(OFPort.of(Integer.parseInt(link.getSrcPort().toString())));
//			l.setDst(DatapathId.of(link.getDst().toString()));
//			l.setDstPort(OFPort.of(Integer.parseInt(link.getDstPort().toString())));
//			l.setLatency(U64.of(Long.parseLong(link.getLatency().getValue()+"")));
//
//			if(this.datapathIdServerMap.get(link.getSrc())!=null && this.datapathIdServerMap.get(link.getDst())!=null){
//
//				this.datapathIdSwitchSrcSwitchDstLinkMap.put(new DatapathIdSwitchesSrcDst3(l.getSrc(), l.getDst()), l);
	}

	@SuppressWarnings("static-access")
	private void clearAll() {

		this.switchTenantVirtualNetworkMap.clear();
		//this.datapathIdSwitchSrcSwitchDstLinkMap.clear();
		//this.datapathIdServerMap.clear();
		this.macAddressHostMap.clear();
		this.tenantTenantMacIdToVirtualMacsMap.clear();
		this.tenantCurrentHostIdToVirtualMacsMap.clear();
		//this.linkArrayList.clear();
		this.tenantIdMacCount = 0L;
		this.hostIdMacCount = 0L;
		this.virtualMacAddressRaw = 0L;
		if (this.environmentOfServers != null){
			if (this.environmentOfServers.getEdgeSCApps() != null){
				this.environmentOfServers.getEdgeSCApps().clear();
			}
			if(this.environmentOfServers.getPhysicalNetwork() != null){
				if(this.environmentOfServers.getPhysicalNetwork().getVertexes()!= null){
					this.environmentOfServers.getPhysicalNetwork().getVertexes().clear();
				}
			}
			if(this.environmentOfServers.getDatapathIdIOFSwitchMap() != null){
				this.environmentOfServers.getDatapathIdIOFSwitchMap().clear();
			}
			if(this.environmentOfServers.getDatapathIdIOFSwitchMap() != null){
				this.environmentOfServers.getDatapathIdIOFSwitchMap().clear();
			}
			if(this.environmentOfServers.getServerIdletterServerIdToEmbedding() != null){
				this.environmentOfServers.getServerIdletterServerIdToEmbedding().clear();
			}
			if(this.environmentOfServers.getLetterServerIdToEmbeddingServerId() != null){
				this.environmentOfServers.getLetterServerIdToEmbeddingServerId().clear();
			}
		}

		if(this.environmentOfTenants != null){
			if(this.environmentOfTenants.getTenantList() != null){
				this.environmentOfTenants.getTenantList().clear();
			}
			if(this.environmentOfServers.getHostLocationHostMap() != null){
				this.environmentOfServers.getHostLocationHostMap().clear();
			}
			if(this.environmentOfServers.getHostNameHostLocationMap() != null){
				this.environmentOfServers.getHostNameHostLocationMap().clear();
			}
			if(this.environmentOfTenants.getTenantIdTenantMap() != null){
				this.environmentOfTenants.getTenantIdTenantMap().clear();
			}
			if(this.environmentOfTenants.getVirtualNetworkArrayList() != null){
				this.environmentOfTenants.getVirtualNetworkArrayList().clear();
			}
		}
	}

	@SuppressWarnings({ "static-access", "unused" })
	private void clearTenantsInfo() {

		this.macAddressHostMap.clear();
		this.tenantTenantMacIdToVirtualMacsMap.clear();
		this.tenantCurrentHostIdToVirtualMacsMap.clear();
		//this.linkArrayList.clear();
		this.tenantIdMacCount = 0L;
		this.hostIdMacCount = 0L;
		this.virtualMacAddressRaw = 0L;
		if(this.environmentOfTenants != null){
			if(this.environmentOfTenants.getTenantList() != null){
				this.environmentOfTenants.getTenantList().clear();
			}
			if(this.environmentOfTenants.getTenantIdTenantMap() != null){
				this.environmentOfTenants.getTenantIdTenantMap().clear();
			}
			if(this.environmentOfTenants.getVirtualNetworkArrayList() != null){
				this.environmentOfTenants.getVirtualNetworkArrayList().clear();
			}
			for (int i = 0; i < this.environmentOfServers.getHostList().size(); i++){
				this.environmentOfServers.getHostList().get(i).setInUse(false);
				this.environmentOfServers.getHostList().get(i).setOperational(true);
			}
		}
	}

	public synchronized long getPartialCookieWriteVirtualAndPhysicalFlows(OFPort port, Server3 server){

		DatapathId physicalDatapathId = server.getOpenVSwitch().getDatapathId();
		Tenant3 tenant = null;
		VirtualSwitch virtualSwitch = null;
		VirtualNetwork virtualNetwork = null;
		boolean canInsertFLowVirtualSwitch = false;
		boolean canInsertFLowPhysicalSwitch = false;
		long partialCookie = 0L;
		tenant = this.getTenantByHostLocation(physicalDatapathId, port);

		virtualNetwork = this.getVirtualNetworkByTenantAndPhysicalDatapathId(tenant, physicalDatapathId, port);
		virtualSwitch = this.getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(virtualNetwork, physicalDatapathId);
		canInsertFLowVirtualSwitch = this.canInsertFLowVirtualSwitch(virtualNetwork, virtualSwitch, tenant);
		canInsertFLowPhysicalSwitch = this.canInsertFLowPhysicalSwitch(server, tenant);
		if (canInsertFLowPhysicalSwitch && canInsertFLowVirtualSwitch){
			virtualSwitch.setFlowSize(virtualSwitch.getFlowSize()+1);
			server.setFlowSize(server.getFlowSize()+1);
			partialCookie = Utils.calculatesCookieWithoutFlowId(tenant.getId(), virtualSwitch);
			if(partialCookie == 0L){
				log.info("The Flow can't be inserted!");
			}
		}
		return partialCookie;
	}

	public synchronized long getPartialCookieWriteVirtualAndPhysicalFlows(OFPort port, Server3 server, VirtualSwitch virtualSwitch){

		DatapathId physicalDatapathId = server.getOpenVSwitch().getDatapathId();
		Tenant3 tenant = null;
		VirtualNetwork virtualNetwork = null;
		boolean canInsertFLowVirtualSwitch = false;
		boolean canInsertFLowPhysicalSwitch = false;
		long partialCookie = 0L;
		HostLocation hostLocation = new HostLocation(physicalDatapathId, port);
		if(physicalDatapathId != null && port != null){
			tenant = this.environmentOfTenants.getHostLocationTenantMap().get(hostLocation);
		}
		if (tenant != null){
			virtualNetwork = this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(hostLocation);
		}
		if (virtualNetwork != null && virtualSwitch != null && tenant != null){
			canInsertFLowVirtualSwitch = this.canInsertFLowVirtualSwitch(virtualNetwork, virtualSwitch, tenant);
		}
		if(server != null && tenant != null){
			canInsertFLowPhysicalSwitch = this.canInsertFLowPhysicalSwitch(server, tenant);
		}
		if (canInsertFLowPhysicalSwitch && canInsertFLowVirtualSwitch){
			virtualSwitch.setFlowSize(virtualSwitch.getFlowSize()+1);
			server.setFlowSize(server.getFlowSize()+1);
			partialCookie = Utils.calculatesCookieWithoutFlowId(tenant.getId(), virtualSwitch);
			if(partialCookie == 0L){
				log.info("The Flow can't be inserted!");
			}
		}
		return partialCookie;
	}

	public synchronized long getPartialCookieWritePhysicalFlows(OFPort port, Server3 server, TenantVirtualNetwork tenantVirtualNetwork){

		DatapathId physicalDatapathId = server.getOpenVSwitch().getDatapathId();
		Tenant3 tenant = null;
		boolean canInsertFLowPhysicalSwitch = false;
		long partialCookie = 0L;
		if(physicalDatapathId != null && port != null){
			tenant = tenantVirtualNetwork.getTenant();
		}
		if(server != null && tenant != null && tenantVirtualNetwork.getVirtualNetwork().isActive()){
			canInsertFLowPhysicalSwitch = this.canInsertFLowPhysicalSwitch(server, tenant);
		}
		if (canInsertFLowPhysicalSwitch){
			server.setFlowSize(server.getFlowSize()+1);
			partialCookie = Utils.calculatesCookieWithoutFlowId(tenant.getId());
			if(partialCookie == 0L){
				log.info("The Flow can't be inserted!");
			}
		}
		return partialCookie;
	}

	//	public synchronized MacAddress virtualMacAddress(Host host){
	//
	//		Tenant3 tenant = null;
	//		//log.info("virtualMacAddress1 host: -{}-", host);
	//		if(host != null){
	//			//log.info("virtualMacAddress2 host.name: -{}-", host.getHostName());
	//			tenant = (this.environmentOfTenants.getTenantIdTenantMap()).get(host.getTenantId());
	//			MacAddress physicalMacAddress = host.getMac();
	//			macAddressHostMap.put(physicalMacAddress, host);
	//			if (tenant != null){
	//				//In case of the first TenantMacIdToVirtualMacs in a specific Tenant, increment tenantIdMacCount
	//				if (tenantTenantMacIdToVirtualMacsMap.get(tenant) == null){
	//					tenantIdMacCount ++;
	//					tenantTenantMacIdToVirtualMacsMap.put((this.environmentOfTenants.getTenantIdTenantMap()).get(host.getTenantId()), Long.valueOf(tenantIdMacCount));
	//				}
	//				if (tenantCurrentHostIdToVirtualMacsMap.get(tenant) == null){
	//					hostIdMacCount = Long.valueOf(1L);
	//					tenantCurrentHostIdToVirtualMacsMap.put(tenant, hostIdMacCount);
	//				} else{
	//					if (host.getVirtualMacAddress().equals(MacAddress.NONE)){
	//						hostIdMacCount = tenantCurrentHostIdToVirtualMacsMap.get(tenant) +1;
	//						tenantCurrentHostIdToVirtualMacsMap.put(tenant, hostIdMacCount);
	//					}
	//				}
	//			}
	//			if(host.getVirtualMacAddress().equals(MacAddress.NONE)){
	//				virtualMacAddressRaw = tenantIdMacCount*4294967296L + hostIdMacCount;
	//				host.setVirtualMacAddress(MacAddress.of(virtualMacAddressRaw));
	//			}
	//			return host.getVirtualMacAddress();
	//		}
	//		return null;
	//	}

	public synchronized MacAddress virtualMacAddress(Host host, TenantVirtualNetwork tenantVirtualNetwork){

		Tenant3 tenant = null;
		if(host != null){
			tenant = tenantVirtualNetwork.getTenant();
			MacAddress physicalMacAddress = host.getMac();
			macAddressHostMap.put(physicalMacAddress, host);
			if (tenant != null){
				//In case of the first TenantMacIdToVirtualMacs in a specific Tenant, increment tenantIdMacCount
				if (tenantTenantMacIdToVirtualMacsMap.get(tenant) == null){
					tenantIdMacCount ++;
					tenantTenantMacIdToVirtualMacsMap.put((this.environmentOfTenants.getTenantIdTenantMap()).get(tenant.getId()), Long.valueOf(tenantIdMacCount));
				}
				if (tenantCurrentHostIdToVirtualMacsMap.get(tenant) == null){
					hostIdMacCount = Long.valueOf(1L);
					tenantCurrentHostIdToVirtualMacsMap.put(tenant, hostIdMacCount);
				} else{
					if (host.getVirtualMacAddress().equals(MacAddress.NONE)){
						hostIdMacCount = tenantCurrentHostIdToVirtualMacsMap.get(tenant) +1;
						tenantCurrentHostIdToVirtualMacsMap.put(tenant, hostIdMacCount);
					}
				}
			}
			if(host.getVirtualMacAddress().equals(MacAddress.NONE)){
				virtualMacAddressRaw = tenantTenantMacIdToVirtualMacsMap.get(tenant)*4294967296L + hostIdMacCount;
				host.setVirtualMacAddress(MacAddress.of(virtualMacAddressRaw));
			}
			return host.getVirtualMacAddress();
		}
		return null;
	}

	public Tenant3 getTenantByHostLocation(DatapathId datapathId, OFPort port){

		if(datapathId != null &&port != null){
			return this.environmentOfTenants.getHostLocationTenantMap().get(new HostLocation(datapathId, port));
		}
		return null;
	}

	public TenantVirtualNetwork getTenantVirtualNetworkFrom2Locations(IOFSwitch sw, MacAddress sourceMac, IPv4Address sourceIpAddress, MacAddress dstMac, IPv4Address dstIpAddress, VlanVid vlan, OFPort port){

		boolean sourceLocationFromATenantVirtualNetwork = false;
		boolean destMacFromATenantVirtualNetwork = false;
		Tenant3 tenant = null;
		VirtualNetwork virtualNetwork = null;
		HostLocation hostLocation = new HostLocation(sw.getId(), port);

		if(sw != null){
			//
		}

		if (sourceMac != null && vlan != null && sourceIpAddress != null && dstMac != null && dstIpAddress != null && sw != null && port != null){
			if (this.environmentOfTenants.getTenantList() != null && port!= null &&  sw.getId() != null && 
					(this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(sw.getId(), port))) != null) {
				tenant = this.environmentOfTenants.getHostLocationTenantMap().get(hostLocation);
				virtualNetwork = this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(hostLocation);
			}
			if (tenant != null && virtualNetwork != null){
				if(virtualNetwork.getTenantId() == tenant.getId()){
					sourceLocationFromATenantVirtualNetwork = true;
					if (dstMac.equals(MacAddress.of("ff:ff:ff:ff:ff:ff"))){
						Host host = null;
						Iterator<Host> iteratorHost = virtualNetwork.getHostList().iterator();
						while(iteratorHost.hasNext()){
							host = iteratorHost.next();
							if(host.getPhysicalHost().getIpAddress().equals(dstIpAddress)){
								dstMac = host.getPhysicalHost().getMac();
								break;
							}
						}
					}
					if (this.getSwitchByMacInVirtualNetwork(dstMac, vlan, dstIpAddress, virtualNetwork) != null){
						destMacFromATenantVirtualNetwork = true;					
					}
				}
				if (sourceLocationFromATenantVirtualNetwork && destMacFromATenantVirtualNetwork){
					return new TenantVirtualNetwork(tenant, virtualNetwork);
				}
			}
		}
		return null;
	}

	private IOFSwitch getSwitchByMacInVirtualNetwork(MacAddress dstMac, VlanVid vlan,IPv4Address dstIpAddress, 
			VirtualNetwork virtualNetwork) {

		if (virtualNetwork != null && dstMac != null && vlan != null && dstIpAddress != null){
			Host host = null;
			HostLocation hostLocation = null;
			DatapathId datapathId = null;
			Iterator<Host> iteratorHost = virtualNetwork.getHostList().iterator();
			while(iteratorHost.hasNext()){
				host = iteratorHost.next();
				if (host.getPhysicalHost().getIpAddress().equals(dstIpAddress) && host.getPhysicalHost().getVlan().equals(vlan)){
					break;
				}
				host = null;
			}
			if (host != null && host.getPhysicalHost() != null){
				hostLocation = host.getPhysicalHost().getHostLocation();
				if(host.getPhysicalHost() == null || host.getPhysicalHost().getHostLocation() == null){
					//
				}
				datapathId = hostLocation.getDatapathId();
				if(this.datapathIdServerMap.get(datapathId) != null){
					return getSwitchService().getActiveSwitch(datapathId);
				}
			}
		}
		return null;
	}

	boolean hasFl2owEntry(ArrayList<FlowEntry> flowtable, Match match, List<OFAction> al){

		for(int i = 0; i< flowtable.size(); i++){
			if(flowtable.get(i).getMatch().equals(match) &&
					flowtable.get(i).getOfActions().equals(al)){
				return true;
			}
		}
		return false;
	}

	public VirtualNetwork getVirtualNetworkByHostLocation(DatapathId datapathId, OFPort port){

		if(datapathId != null &&port != null){
			return this.environmentOfTenants.getHostLocationVirtualNetworkMap().get(new HostLocation(datapathId, port));
		}
		return null;
	}

	private VirtualSwitch getVirtualSwitchByVirtualNetworkAndPhysicalDatapathId(
			VirtualNetwork virtualNetwork, DatapathId physicalDatapathId) {

		Iterator<VirtualSwitch> itVirtualSwitch = virtualNetwork.getVirtualSwitchArrayList().iterator();
		VirtualSwitch virtualSwitchAux = null;

		while (itVirtualSwitch.hasNext()){
			virtualSwitchAux = (VirtualSwitch) itVirtualSwitch.next();
			if(virtualSwitchAux != null){
				if (virtualSwitchAux.getWorkingSwitch().getOpenVSwitch().getDatapathId().equals(physicalDatapathId)){
					return virtualSwitchAux;
				}
			}
		}
		return null;
	}

	private VirtualNetwork getVirtualNetworkByTenantAndPhysicalDatapathId(
			Tenant3 tenant, DatapathId physicalDatapathId, OFPort port) {

		Host host = this.environmentOfServers.getHostLocationHostMap().get(new HostLocation(physicalDatapathId, port));

		if(tenant != null && host != null){
			Iterator<Entry<Integer, VirtualNetwork>> itVirtualNetwork = tenant.getVirtualNetworkList().entrySet().iterator();
			VirtualNetwork virtualNetwork =null;
			while (itVirtualNetwork.hasNext()){
				virtualNetwork  = itVirtualNetwork.next().getValue();
				if(virtualNetwork != null){
					if(virtualNetwork.getHostList().contains(host)){
						return virtualNetwork;
					}
				}
			}
		}
		return null;
	}

	private boolean canInsertFLowPhysicalSwitch(Server3 server, Tenant3 tenant) {

		if(server != null){
			if(server.getOpenVSwitch() != null){
				if(server.getMAX_FLOW_SIZE() == 0){
					return true;
				} else{
					if(server.getMAX_FLOW_SIZE() >= server.getFlowSize()){
						return true;
					}else{
						if(tenant != null){
							log.warn("Exceeded number of flows in the Physical Switch -{}- of Tenant -{}-", server.getOpenVSwitch().getDatapathId(), tenant.getDescription());
						}
					}
				}
			}
		}
		return false;
	}

	private boolean canInsertFLowVirtualSwitch(VirtualNetwork virtualNetwork, VirtualSwitch virtualSwitch, Tenant3 tenant) {

		if(virtualSwitch != null){
			if((virtualSwitch.getMAX_FLOW_SIZE() == 0) && (virtualNetwork.isActive())){
				return true;
			}else {
				if((virtualSwitch.getMAX_FLOW_SIZE() >= virtualSwitch.getFlowSize()) && (virtualNetwork.isActive())){
					return true;
				} else{
					if(tenant != null){
						log.warn("Exceeded number of flows in the Virtual Switch -{}- of Tenant -{}-", virtualSwitch.getOpenVSwitch().getDatapathId(), tenant.getDescription());
					}
				}
			}
		}
		return false;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		
		log.info("INITING...");
		switchTenantVirtualNetworkMap = new ConcurrentHashMap<IOFSwitch, Map<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>>>();
		floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
		debugCounterService = context.getServiceImpl(IDebugCounterService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);
		setSwitchService(context.getServiceImpl(IOFSwitchService.class)); 
		linkDiscService = context.getServiceImpl(ILinkDiscoveryService.class);
		//this.environmentOfTenants.initEnvironmentOfAllTenants();
		//Utils.insertLetters();
	}

	@Override
	public void startUp(FloodlightModuleContext context) {

		floodlightProviderService.addOFMessageListener(OFType.PACKET_IN, this);
		floodlightProviderService.addOFMessageListener(OFType.FLOW_REMOVED, this);
		floodlightProviderService.addOFMessageListener(OFType.ERROR, this);
		restApiService.addRestletRoutable(new NetHypervisorWebRoutable());
		getSwitchService().addOFSwitchListener((IOFSwitchListener) this);
		linkDiscService.addListener(this);
		Map<String, String> configOptions = context.getConfigParams(this);
		try {
			String idleTimeout = configOptions.get("idletimeout");
			if (idleTimeout != null) {
				FLOWMOD_DEFAULT_IDLE_TIMEOUT = Short.parseShort(idleTimeout);
			}
		} catch (NumberFormatException e) {
			log.warn("Error parsing flow idle timeout, " +
					"using default of {} seconds", FLOWMOD_DEFAULT_IDLE_TIMEOUT);
		}
		try {
			String hardTimeout = configOptions.get("hardtimeout");
			if (hardTimeout != null) {
				FLOWMOD_DEFAULT_HARD_TIMEOUT = Short.parseShort(hardTimeout);
			}
		} catch (NumberFormatException e) {
			log.warn("Error parsing flow hard timeout, " +
					"using default of {} seconds", FLOWMOD_DEFAULT_HARD_TIMEOUT);
		}
		try {
			String priority = configOptions.get("priority");
			if (priority != null) {
				FLOWMOD_PRIORITY = Short.parseShort(priority);
			}
		} catch (NumberFormatException e) {
			log.warn("Error parsing flow priority, " +
					"using default of {}",
					FLOWMOD_PRIORITY);
		}

		configParameters = context.getConfigParams(this);
		//Get info and config watcher regarding to physical topology
		if (configParameters.get(DIR_LOCATION_PHYSICAL_TOPOLOGY_PROPERTY_NAME) == null){
			DIR_LOCATION_PHYSICAL_TOPOLOGY = DIR_DEFAULT_LOCATION_PHYSICAL_TOPOLOGY;
		}else
			try {

				DIR_LOCATION_PHYSICAL_TOPOLOGY = configParameters.get(DIR_LOCATION_PHYSICAL_TOPOLOGY_PROPERTY_NAME);
			} catch (NumberFormatException e) {

				DIR_LOCATION_PHYSICAL_TOPOLOGY = DIR_DEFAULT_LOCATION_PHYSICAL_TOPOLOGY;
			}
		this.dirLocationPhysicalTopology = new File(DIR_LOCATION_PHYSICAL_TOPOLOGY);
		this.watchDirLocationPhysicalTopology = new WatchDirectory(dirLocationPhysicalTopology.toPath());
		this.threadDoWatchDirLocationPhysicalTopology = new Thread(watchDirLocationPhysicalTopology);
		threadDoWatchDirLocationPhysicalTopology.start();
		watchDirLocationPhysicalTopology.addObserver(this);

		//Get info and config watcher regarding to virtual topology
		if (configParameters.get(DIR_LOCATION_VIRTUAL_TOPOLOGY_PROPERTY_NAME) == null){
			DIR_LOCATION_VIRTUAL_TOPOLOGY = DIR_DEFAULT_LOCATION_VIRTUAL_TOPOLOGY;
		}else
			try {
				DIR_LOCATION_VIRTUAL_TOPOLOGY = configParameters.get(DIR_LOCATION_VIRTUAL_TOPOLOGY_PROPERTY_NAME);
			} catch (NumberFormatException e) {
				DIR_LOCATION_VIRTUAL_TOPOLOGY = DIR_DEFAULT_LOCATION_VIRTUAL_TOPOLOGY;
			}

		this.dirLocationVirtualTopology = new File(DIR_LOCATION_VIRTUAL_TOPOLOGY);
		this.watchDirLocationVirtualTopology = new WatchDirectory(dirLocationVirtualTopology.toPath());
		this.threadDoWatchDirLocationVirtualTopology = new Thread(watchDirLocationVirtualTopology);
		threadDoWatchDirLocationVirtualTopology.start();
		watchDirLocationVirtualTopology.addObserver(this);

		//Get info and config watcher regarding to the tenants
		if (configParameters.get(DIR_LOCATION_TENANT_INFO_PROPERTY_NAME) == null){
			DIR_LOCATION_TENANT_INFO = DIR_DEFAULT_LOCATION_TENANT_INFO;
		}else
			try {
				DIR_LOCATION_TENANT_INFO = configParameters.get(DIR_LOCATION_TENANT_INFO_PROPERTY_NAME);
			} catch (NumberFormatException e) {
				DIR_LOCATION_TENANT_INFO = DIR_DEFAULT_LOCATION_TENANT_INFO;
			}
		this.dirLocationTenantInfo = new File(DIR_LOCATION_TENANT_INFO);
		this.watchDirLocationTenantInfo = new WatchDirectory(dirLocationTenantInfo.toPath());
		this.threadDoWatchDirLocationTenantInfo = new Thread(watchDirLocationTenantInfo);
		threadDoWatchDirLocationTenantInfo.start();
		watchDirLocationTenantInfo.addObserver(this);

		log.debug("FlowMod idle timeout set to {} seconds", FLOWMOD_DEFAULT_IDLE_TIMEOUT);
		log.debug("FlowMod hard timeout set to {} seconds", FLOWMOD_DEFAULT_HARD_TIMEOUT);
		log.debug("FlowMod priority set to {}", FLOWMOD_PRIORITY);

		debugCounterService.registerModule(this.getName());
		counterFlowMod = debugCounterService.registerCounter(this.getName(), "flow-mods-written", "Flow mods written to switches", MetaData.WARN);
		counterPacketOut = debugCounterService.registerCounter(this.getName(), "packet-outs-written", "Packet outs written to switches", MetaData.WARN);

		/*17jan17 - Real cloud integration.*/
		//TODO uncomment
		try {
			Network network = this.initRequestPhysical();
			if(network != null){
				log.info("Request Physical topology info.");
				environmentOfServers.initEnvironmentOfServers(network, false, this);
				populateServerIDAndDatapathidToServer();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {

		Collection<Class<? extends IFloodlightService>> l =	new ArrayList<Class<? extends IFloodlightService>>();
		l.add(INetHypervisorService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {

		Map<Class<? extends IFloodlightService>,  IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		m.put(INetHypervisorService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {

		Collection<Class<? extends IFloodlightService>> l =	new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IDebugCounterService.class);
		l.add(IRestApiService.class);
		l.add(IOFSwitchService.class);
		l.add(ILinkDiscoveryService.class);

		return l;
	}

	public Network initRequestPhysical() throws Exception {

		Config config = new Config("console.properties");
		//Network network = new Network();
		// Create client socket
		//Socket socket = null;
		String ip = config.consoleAdminIp;
		int port = config.consoleListeningPort;

		while (this.socket == null) {
			try {
				Console.log("Connecting " + ip + " on port " + port);
				socket = new Socket(ip, port);
				break;
			} 
			catch (IOException e) {
				Console.error("Cannot connect " + ip + " on port " + port);
				Console.error("Retrying in 2 seconds ...");
				Console.pause(2000);
			}
		}
		clientChangeInfoOrquestrator = new Client(this.socket, DIR_LOCATION_PHYSICAL_TOPOLOGY + "/physical.xml", DIR_LOCATION_VIRTUAL_TOPOLOGY, this);
		return clientChangeInfoOrquestrator.requestPhysicalNetwork();

//		Config config = new Config("console.properties");
//		Network network = new Network();
//		
//		// Create client socket
//		Socket socket = null;
//		String ip = config.consoleIp;
//		int port = config.consolePort;
//		while (true) {
//			try {
//				Console.log("Connecting " + ip + " on port " + port);
//				socket = new Socket(ip, port);
//				break;
//			} 
//			catch (IOException e) {
//				Console.error("Cannot connect " + ip + " on port " 
//						+ port + ". Retrying in 2 seconds ...");
//				Console.error("Retrying in 2 seconds ...");
//				Console.pause(2000);
//			}
//		}
//		new Client(socket, network);
//		channel = new Channel(socket, network, DIR_LOCATION_PHYSICAL_TOPOLOGY);
//		channel.start();
//		
//		Console.pause(1000);
//		Console.log("Sending INIT_REQUEST");
//		Packet request = new Packet(Type.INIT_REQUEST, null);
//		channel.send(request);
//		
//		return true;
	}

	public IOFSwitchService getSwitchService() {
		return switchService;
	}

	public void setSwitchService(IOFSwitchService switchService) {
		this.switchService = switchService;
	}
}
