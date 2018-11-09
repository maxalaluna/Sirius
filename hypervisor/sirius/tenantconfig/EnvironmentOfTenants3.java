/**
 * This class is the one that belongs to the SIRIUS Environment's first prototype and covers the set of tenants with their respective 
 * MAC Address and IP Address. As the separation of the trafic is done by the MAC Address, this information in the header of the packet is used 
 * to verify to which tenant a packet belongs. In this fase, only a IP Address is virtualized, hence there is no 2º layer virtualization.
 * 
 * The tenant information is defined hard coded in the class, but it could be acquired from a text file or a database. However, based on the 
 * implementation design, it is necessary to the proper functioning of the all system.
 * 
 * In this version there are 2 tenants. Each tenant have a set of MAC Address that belong to him. Only for facilitating, we defined the first
 * 16 bits in the MAC to indicate the tenant. So, "00:01:XX:XX:XX:XX" belongs to "Tenant 1". But, considering the initial design, we can choose 
 * the MAC Address we want to distribute to a tenant.
 * 
 **/

package net.floodlightcontroller.sirius.tenantconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.FileUtils;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.floodlightcontroller.sirius.console.Client;
import net.floodlightcontroller.sirius.console.Config;
import net.floodlightcontroller.sirius.console.Link;
import net.floodlightcontroller.sirius.console.Network;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Common.Pair;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Embeddor.EmbeddorHeu;
import net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Virtual.VirtualNetworkHeu;
import net.floodlightcontroller.sirius.embedding.pt.lasige.secdepvne.Embeddor.EmbeddorMILP;
import net.floodlightcontroller.sirius.providerconfig.EnvironmentOfServices3;
import net.floodlightcontroller.sirius.providerconfig.OpenVSwitch3;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualEdgeSC;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualSwitch;
import net.floodlightcontroller.sirius.topology.FlowEntry;
import net.floodlightcontroller.sirius.topology.FlowTable;
import net.floodlightcontroller.sirius.util.Enum.CloudType;
import net.floodlightcontroller.sirius.util.Enum.DependabilityLevelVirtualNode;
import net.floodlightcontroller.sirius.util.Enum.SecurityLevelLinks;
import net.floodlightcontroller.sirius.util.Enum.SecurityLevelVirtualNode;

public class EnvironmentOfTenants3 {


	protected static Logger log = LoggerFactory.getLogger(EnvironmentOfTenants3.class);
	//To do the cbench test, this IP address were used because every packet-in have these 2 IP during the tests
	//String ipSrcCbench = new String ("192.168.0.40");
	//String ipDstCbench = new String ("192.168.1.40");

	//As the MAC Address is unique, for now, in system, we create this hashmap to facilitate the discovery and verification of the IP Address 
	//when a packet-in arrives in the controller
	//protected Map<MacAddress,IPv4Address> macIpMap = new HashMap<MacAddress,IPv4Address>();
	//Another hashmap is created to allow recovery in an easier way switch which a VM with a MAC address is connected
	//Commented in 22Jun16 -> Virtualiz L2
	//protected Map<MacAddress, IOFSwitch> macSwMap = new HashMap<MacAddress,IOFSwitch>();

	protected Map<HostLocation,Host> hostLocationHostMap = new HashMap<HostLocation,Host>();
	protected Map<String,HostLocation> hostNameHostLocationMap = new HashMap<String,HostLocation>();
	protected Map<Integer,Tenant3> tenantIdTenantMap = new HashMap<Integer,Tenant3>();
	protected Map<HostLocation,VirtualNetwork> hostLocationVirtualNetworkMap = new HashMap<HostLocation,VirtualNetwork>();
	protected Map<HostLocation,Tenant3> hostLocationTenantMap = new HashMap<HostLocation,Tenant3>();
	ArrayList<Tenant3> tenantList = new ArrayList<Tenant3>();
	VirtualSwitch virtualS1 = new VirtualSwitch(20, 0, 50, CloudType.PRIVATEDATACENTER1);
	VirtualSwitch virtualSG8 = new VirtualSwitch(25, 0, 50, CloudType.PRIVATEDATACENTER1);
	private VirtualEdgeSC virtualEdgeS1_SG8 = new VirtualEdgeSC("1", virtualS1, virtualSG8, 100, 100, 100, null, null, true);
	private ArrayList<FlowEntry> flowEntryS1 = new ArrayList<FlowEntry>();
	FlowTable flowTableS1 = new FlowTable(0, flowEntryS1 );
	private ArrayList<FlowEntry> flowEntrySG8 = new ArrayList<FlowEntry>();
	FlowTable flowTableSG8 = new FlowTable(0, flowEntrySG8 );
	VirtualNetwork virtualNetwork;
	boolean heuristicEmbedding = true;
	String typeOfHeuristic = "Sirius";
	ArrayList<VirtualNetwork> virtualNetworkArrayList = new ArrayList<VirtualNetwork>();
	private Map<Integer, VirtualSwitch> virtualSwitchIdVirtualSwitch = new HashMap<Integer, VirtualSwitch>();
	private Map<Integer, ArrayList<VirtualNetwork>> tenantIdVirtualNetworkArrayList = new HashMap<Integer, ArrayList<VirtualNetwork>>();
	private Map<Integer, Host> virtualHostIdVirtualHost = new HashMap<Integer, Host>();
	private Map<Integer, Long> virtualNetworkIdTenantIdMap = new HashMap<Integer, Long>();
	private Map<Integer, VirtualNetwork> virtualNetworkIdVirtualNetworkMap = new HashMap<Integer, VirtualNetwork>();
	private String mininetIp = "192.168.56.130";
	private String mininetUser = "mininet";
	private String mininetPassword = "mininet";
	int initialPortNumber = 128;
	//MAC Address of the tenants
	//"00:01:XX:XX:XX:XX" indicate "Tenant 1"

	//	String address1 = "00:01:00:00:00:01";
	//	String address2 = "00:01:00:00:00:02";
	//	String address3 = "00:02:00:00:00:01";
	//	String address4 = "00:02:00:00:00:02";
	//	String address5 = "00:01:00:00:00:03";
	//	String address6 = "00:01:00:00:00:04";
	//	String address7 = "00:02:00:00:00:03";
	//	String address8 = "00:02:00:00:00:04";
	//	String address9 = "00:01:00:00:00:05";
	//	String address10 = "00:01:00:00:00:06";
	//	String address11 = "00:02:00:00:00:05";
	//	String address12 = "00:02:00:00:00:06";
	//
	//	//IP Address of the tenants
	//	String ip1 = new String ("10.200.0.1");
	//	String ip2 = new String ("10.200.0.2");
	//	String ip3 = new String ("10.200.0.1");
	//	String ip4 = new String ("10.200.0.2");
	//	String ip5 = new String ("10.200.0.3");
	//	String ip6 = new String ("10.200.0.4");
	//	String ip7 = new String ("10.200.0.3");
	//	String ip8 = new String ("10.200.0.4");
	//	String ip9 = new String ("10.200.0.5");
	//	String ip10 = new String ("10.200.0.6");
	//	String ip11 = new String ("10.200.0.5");
	//	String ip12 = new String ("10.200.0.6");
	//
	//	Host T1Mac1 = new Host(1, MacAddress.of(address1), VlanVid.ofVlan(0), IPv4Address.of(ip1), "T1Mac1", 1 );
	//	Host T1Mac2 = new Host(2, MacAddress.of(address2), VlanVid.ofVlan(0), IPv4Address.of(ip2), "T1Mac2", 1 );
	//	Host T1Mac3 = new Host(3, MacAddress.of(address5), VlanVid.ofVlan(0), IPv4Address.of(ip5), "T1Mac3", 1 );
	//	Host T1Mac4 = new Host(4, MacAddress.of(address6), VlanVid.ofVlan(0), IPv4Address.of(ip6), "T1Mac4", 1 );
	//	Host T1Mac5 = new Host(5, MacAddress.of(address9), VlanVid.ofVlan(0), IPv4Address.of(ip9), "T1Mac5", 1 );
	//	Host T1Mac6 = new Host(6, MacAddress.of(address10), VlanVid.ofVlan(0), IPv4Address.of(ip10), "T1Mac6", 1 );
	//	Host T2Mac1 = new Host(7, MacAddress.of(address3), VlanVid.ofVlan(0), IPv4Address.of(ip3), "T2Mac1", 2 );
	//	Host T2Mac2 = new Host(8, MacAddress.of(address4), VlanVid.ofVlan(0), IPv4Address.of(ip4), "T2Mac2", 2 );
	//	Host T2Mac3 = new Host(9, MacAddress.of(address7), VlanVid.ofVlan(0), IPv4Address.of(ip7), "T2Mac3", 2 );
	//	Host T2Mac4 = new Host(10, MacAddress.of(address8), VlanVid.ofVlan(0), IPv4Address.of(ip8), "T2Mac4", 2 );
	//	Host T2Mac5 = new Host(11, MacAddress.of(address11), VlanVid.ofVlan(0), IPv4Address.of(ip11), "T2Mac5", 2 );
	//	Host T2Mac6 = new Host(12, MacAddress.of(address1), VlanVid.ofVlan(0), IPv4Address.of(ip12), "T2Mac6", 2 );

	public int getInitialPortNumber() {
		return initialPortNumber;
	}

	public void setInitialPortNumber(int initialPortNumber) {
		this.initialPortNumber = initialPortNumber;
	}

	public Map<Integer, Long> getVirtualNetworkIdTenantIdMap() {
		return virtualNetworkIdTenantIdMap;
	}

	public void setVirtualNetworkIdTenantIdMap(
			Map<Integer, Long> virtualNetworkIdTenantIdMap) {
		this.virtualNetworkIdTenantIdMap = virtualNetworkIdTenantIdMap;
	}

	public ArrayList<VirtualNetwork> getVirtualNetworkArrayList() {
		return virtualNetworkArrayList;
	}

	public void setVirtualNetworkArrayList(
			ArrayList<VirtualNetwork> virtualNetworkArrayList) {
		this.virtualNetworkArrayList = virtualNetworkArrayList;
	}

	public Map<HostLocation, VirtualNetwork> getHostLocationVirtualNetworkMap() {
		return hostLocationVirtualNetworkMap;
	}

	public void setHostLocationVirtualNetworkMap(
			Map<HostLocation, VirtualNetwork> hostLocationVirtualNetworkMap) {
		this.hostLocationVirtualNetworkMap = hostLocationVirtualNetworkMap;
	}

	public Map<HostLocation, Tenant3> getHostLocationTenantMap() {
		return hostLocationTenantMap;
	}

	public void setHostLocationTenantMap(
			Map<HostLocation, Tenant3> hostLocationTenantMap) {
		this.hostLocationTenantMap = hostLocationTenantMap;
	}
	/*
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
	 */

	public Map<Integer, Tenant3> getTenantIdTenantMap() {
		return tenantIdTenantMap;
	}

	public void setTenantIdTenantMap(Map<Integer, Tenant3> tenantIdTenantMap) {
		this.tenantIdTenantMap = tenantIdTenantMap;
	}

	//Each tenant has one ArrayList of the class "IpMacVlanPair3".
	//	ArrayList<IpMacVlanPair3> ipMacList1 = new ArrayList<IpMacVlanPair3>();
	//	ArrayList<IpMacVlanPair3> ipMacList2 = new ArrayList<IpMacVlanPair3>();

	public EnvironmentOfTenants3() {
		super();
	}

	public ArrayList<Tenant3> getTenantList() {
		return tenantList;
	}

	public void setTenantList(ArrayList<Tenant3> tenantList) {
		this.tenantList = tenantList;
	}

	// In this function, will be set up some MAC and IP Address to a specific tenant. Without the configuration between a tenant and his 
	// address, the system do not work properly

	public void initEnvironmentOfAllTenants() {
		/*
		this.ipMacList1.add(new IpMacVlanPair3(MacAddress.of(address1), VlanVid.ofVlan(0), IPv4Address.of(ip1)));
		this.ipMacList1.add(new IpMacVlanPair3(MacAddress.of(address2), VlanVid.ofVlan(0), IPv4Address.of(ip2)));
		this.ipMacList1.add(new IpMacVlanPair3(MacAddress.of(address5), VlanVid.ofVlan(0), IPv4Address.of(ip5)));
		this.ipMacList1.add(new IpMacVlanPair3(MacAddress.of(address6), VlanVid.ofVlan(0), IPv4Address.of(ip6)));
		this.ipMacList1.add(new IpMacVlanPair3(MacAddress.of(address9), VlanVid.ofVlan(0), IPv4Address.of(ip9)));
		this.ipMacList1.add(new IpMacVlanPair3(MacAddress.of(address10), VlanVid.ofVlan(0), IPv4Address.of(ip10)));

		macIpMap.put(MacAddress.of(address1), IPv4Address.of(ip1));
		macIpMap.put(MacAddress.of(address2), IPv4Address.of(ip2));
		macIpMap.put(MacAddress.of(address5), IPv4Address.of(ip5));
		macIpMap.put(MacAddress.of(address6), IPv4Address.of(ip6));
		macIpMap.put(MacAddress.of(address9), IPv4Address.of(ip9));
		macIpMap.put(MacAddress.of(address10), IPv4Address.of(ip10));
		 */
		//cbench only will get the metrics of latency and throughput if it receives the flow mod based with a packe-in. In this case, if only will
		//happen if some address are correctly set up to a specif tenant. It will only happen if the function "this.populateMacsIpsTenant" have been
		//executed as the function right below.
		//this.populateMacsIpsTenant(1600,5,this.tenant1);

		//		this.tenant1.getHostList().add(T1Mac1);
		//		this.tenant1.getHostList().add(T1Mac2);
		//		this.tenant1.getHostList().add(T1Mac3);
		//		this.tenant1.getHostList().add(T1Mac4);
		//		this.tenant1.getHostList().add(T1Mac5);
		//		this.tenant1.getHostList().add(T1Mac6);

		//this.tenant1.setHostList(ipMacList1);
		/*
		this.ipMacList2.add(new IpMacVlanPair3(MacAddress.of(address3), VlanVid.ofVlan(0), IPv4Address.of(ip3)));
		this.ipMacList2.add(new IpMacVlanPair3(MacAddress.of(address4), VlanVid.ofVlan(0), IPv4Address.of(ip4)));
		this.ipMacList2.add(new IpMacVlanPair3(MacAddress.of(address7), VlanVid.ofVlan(0), IPv4Address.of(ip7)));
		this.ipMacList2.add(new IpMacVlanPair3(MacAddress.of(address8), VlanVid.ofVlan(0), IPv4Address.of(ip8)));
		this.ipMacList2.add(new IpMacVlanPair3(MacAddress.of(address11), VlanVid.ofVlan(0), IPv4Address.of(ip11)));
		this.ipMacList2.add(new IpMacVlanPair3(MacAddress.of(address12), VlanVid.ofVlan(0), IPv4Address.of(ip12)));

		macIpMap.put(MacAddress.of(address3), IPv4Address.of(ip3));
		macIpMap.put(MacAddress.of(address4), IPv4Address.of(ip4));
		macIpMap.put(MacAddress.of(address7), IPv4Address.of(ip7));
		macIpMap.put(MacAddress.of(address8), IPv4Address.of(ip8));
		macIpMap.put(MacAddress.of(address11), IPv4Address.of(ip11));
		macIpMap.put(MacAddress.of(address12), IPv4Address.of(ip12));
		 */
		//		this.tenant2.getHostList().add(T2Mac1);
		//		this.tenant2.getHostList().add(T2Mac2);
		//		this.tenant2.getHostList().add(T2Mac3);
		//		this.tenant2.getHostList().add(T2Mac4);
		//		this.tenant2.getHostList().add(T2Mac5);
		//		this.tenant2.getHostList().add(T2Mac6);
		//this.tenant2.setHostList(ipMacList2);

		//		this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(2)), T1Mac1);
		//		this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(3)), T1Mac2);
		//this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(4)), T2Mac1);
		//this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(5)), T2Mac2);
		//		this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(2)), T1Mac3);
		//		this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(3)), T1Mac4);
		//this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(4)), T2Mac3);
		//this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(5)), T2Mac4);
		//		this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(3)), T1Mac5);
		//		this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(4)), T1Mac6);
		//this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(5)), T2Mac5);
		//this.hostLocationHostMap.put(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(6)), T2Mac6);

		//		this.hostNameHostLocationMap.put("T1Mac1", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(2)));
		//		this.hostNameHostLocationMap.put("T1Mac2", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(3)));
		//this.hostNameHostLocationMap.put("T2Mac1", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(4)));
		//this.hostNameHostLocationMap.put("T2Mac2", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(5)));
		//		this.hostNameHostLocationMap.put("T1Mac3", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(2)));
		//		this.hostNameHostLocationMap.put("T1Mac4", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(3)));
		//this.hostNameHostLocationMap.put("T2Mac3", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(4)));
		//this.hostNameHostLocationMap.put("T2Mac4", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(5)));
		//		this.hostNameHostLocationMap.put("T1Mac5", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(3)));
		//		this.hostNameHostLocationMap.put("T1Mac6", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(4)));
		//this.hostNameHostLocationMap.put("T2Mac5", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(5)));
		//this.hostNameHostLocationMap.put("T2Mac6", new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(6)));

		ArrayList<VirtualEdgeSC> virtualEdgeSCSet = new ArrayList<VirtualEdgeSC>();
		ArrayList<VirtualSwitch> virtualSwitchSet = new ArrayList<VirtualSwitch>();
		virtualS1.setOpenVSwitch(new OpenVSwitch3(null, null, null, null, DatapathId.of("00:00:00:00:00:00:00:01")));
		virtualS1.getFlowTable().add(flowTableS1);
		virtualSwitchSet.add(virtualS1);
		virtualSG8.setOpenVSwitch(new OpenVSwitch3(null, null, null, null, DatapathId.of("00:00:00:00:00:00:00:08")));
		virtualSG8.getFlowTable().add(flowTableSG8);
		virtualSwitchSet.add(virtualSG8);

		virtualEdgeSCSet.add(virtualEdgeS1_SG8);

		//virtualNetwork = new VirtualNetwork(this.tenant1.getHostList(), virtualEdgeSCSet, virtualSwitchSet, true, 1000, 1L);
		//this.tenant1.getVirtualNetworkList().put(this.virtualNetwork.getVirtualNetworkId(), this.virtualNetwork);
		//virtualNetworkArrayList.add(virtualNetwork);

	}

	public void setHostListToTenant(Tenant3 tenant, ArrayList<Host> hostList) {

		if(tenant != null){

			if(this.getTenantList().contains(tenant)){

				for (int i=0; i < hostList.size(); i++){

					if (!tenant.getHostList().contains(hostList.get(i))){
						tenant.getHostList().add((hostList.get(i)));	
					}
				}
			}
		}
	}

	public void setHostToTenant(Tenant3 tenant, Host host) {

		if(tenant != null){

			if(this.getTenantList().contains(tenant)){

				if (!tenant.getHostList().contains(host)){

					tenant.getHostList().add((host));
				}
			}
		}
	}

	public ArrayList<Host> insertHostToArray(Host host){

		ArrayList<Host> arrayHost = new ArrayList<Host>();

		if (host != null){
			if (arrayHost.add(host)){
				return arrayHost;
			}
		}
		return null;
	}

	/**
	 * Populate tenant1 or tenant2 with many MAC Address in a specific switch based in the formation rule of cbench 
	 * @param environmentOfServers 
	 * @param numberOfSwitch The number of switches to be put in the cbench command
	 * @param numberOfMAC The number of different MAC Address to be put in the cbench command
	 * @param tenant the specific tenant that will be used for the test
	 * @throws Exception 
	 */
	/*	private void populateMacsIpsTenant(int numberOfSwitch, int numberOfMAC, Tenant3 tenant) {

		EnvironmentOfServices2 e = new EnvironmentOfServices2("gre");

		for (int i=0; i < numberOfSwitch; i++){
			for (int j=0; j < numberOfMAC; j++){
				macIpMap.put(MacAddress.of(e.createMacAddress(i+1, j, true)), IPv4Address.of(ipSrcCbench));
				macIpMap.put(MacAddress.of(e.createMacAddress(i+1, j, false)), IPv4Address.of(ipDstCbench));
				if(tenant.equals(tenant1)){
					this.ipMacList1.add(new IpMacVlanPair3(MacAddress.of(e.createMacAddress(i+1, j, true)), VlanVid.ofVlan(0), IPv4Address.of(ipSrcCbench)));
					this.ipMacList1.add(new IpMacVlanPair3(MacAddress.of(e.createMacAddress(i+1, j, false)), VlanVid.ofVlan(0), IPv4Address.of(ipDstCbench)));		
				}
				if(tenant.equals(tenant2)){
					this.ipMacList2.add(new IpMacVlanPair3(MacAddress.of(e.createMacAddress(i+1, j, true)), VlanVid.ofVlan(0), IPv4Address.of(ipSrcCbench)));
					this.ipMacList2.add(new IpMacVlanPair3(MacAddress.of(e.createMacAddress(i+1, j, false)), VlanVid.ofVlan(0), IPv4Address.of(ipDstCbench)));	
				}
			}
		}
	}

	public IPv4Address getIpFromMac(MacAddress macAddress) {
		return this.macIpMap.get(macAddress);
	}
	 */
	/*
	public Host emulateAPIMininetInfoHostLocation(HostLocation hostLocation){

		if (hostLocation != null){
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(2)))){
				return T1Mac1;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(3)))){
				return T1Mac2;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(4)))){
				return T2Mac1;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(5)))){
				return T2Mac2;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(2)))){
				return T1Mac3;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(3)))){
				return T1Mac4;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(4)))){
				return T2Mac3;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(5)))){
				return T2Mac4;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(3)))){
				return T1Mac5;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(4)))){
				return T1Mac6;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(5)))){
				return T2Mac5;
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:08"), OFPort.of(6)))){
				return T2Mac6;
			}
		}
		return null;
	}

	public Host emulateAPIMininetInfoHostLocation(HostLocation hostLocation, EnvironmentOfServices3 environmentOfServers){

		if (hostLocation != null){
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(1)))){
				return environmentOfServers.getHostNameHostMap().get("h1");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(2)))){
				return environmentOfServers.getHostNameHostMap().get("h2");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(3)))){
				return environmentOfServers.getHostNameHostMap().get("h3");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(4)))){
				return environmentOfServers.getHostNameHostMap().get("h4");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(5)))){
				return environmentOfServers.getHostNameHostMap().get("h5");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:01"), OFPort.of(6)))){
				return environmentOfServers.getHostNameHostMap().get("h6");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(1)))){
				return environmentOfServers.getHostNameHostMap().get("h7");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(2)))){
				return environmentOfServers.getHostNameHostMap().get("h8");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(3)))){
				return environmentOfServers.getHostNameHostMap().get("h9");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(4)))){
				return environmentOfServers.getHostNameHostMap().get("h10");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(5)))){
				return environmentOfServers.getHostNameHostMap().get("h11");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:02"), OFPort.of(6)))){
				return environmentOfServers.getHostNameHostMap().get("h12");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:03"), OFPort.of(4)))){
				return environmentOfServers.getHostNameHostMap().get("h13");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:03"), OFPort.of(5)))){
				return environmentOfServers.getHostNameHostMap().get("h14");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:03"), OFPort.of(6)))){
				return environmentOfServers.getHostNameHostMap().get("h15");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:03"), OFPort.of(7)))){
				return environmentOfServers.getHostNameHostMap().get("h16");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(2)))){
				return environmentOfServers.getHostNameHostMap().get("h17");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(3)))){
				return environmentOfServers.getHostNameHostMap().get("h18");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(4)))){
				return environmentOfServers.getHostNameHostMap().get("h19");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:04"), OFPort.of(5)))){
				return environmentOfServers.getHostNameHostMap().get("h20");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:05"), OFPort.of(2)))){
				return environmentOfServers.getHostNameHostMap().get("h21");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:05"), OFPort.of(3)))){
				return environmentOfServers.getHostNameHostMap().get("h22");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:05"), OFPort.of(4)))){
				return environmentOfServers.getHostNameHostMap().get("h23");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:05"), OFPort.of(5)))){
				return environmentOfServers.getHostNameHostMap().get("h24");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:06"), OFPort.of(2)))){
				return environmentOfServers.getHostNameHostMap().get("h25");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:06"), OFPort.of(3)))){
				return environmentOfServers.getHostNameHostMap().get("h26");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:06"), OFPort.of(4)))){
				return environmentOfServers.getHostNameHostMap().get("h27");
			}
			if (hostLocation.equals(new HostLocation(DatapathId.of("00:00:00:00:00:00:00:06"), OFPort.of(5)))){
				return environmentOfServers.getHostNameHostMap().get("h28");
			}
		}
		return null;
	}
	 */	

	public void initEnvironmentOfTenants(String fileChanged, String substratePart, EnvironmentOfServices3 environmentOfServers) throws Exception{

		Network VirtualNodes = this.createVirtualNetwork(fileChanged);
		this.createVirtualNodesRequest(VirtualNodes);
		this.EmbeddingAllVirtualNetworks(substratePart, environmentOfServers);
		//log.info("Populating Environment of Tenants...");
		//this.populateEnvironment();
		//log.info("Populating Environment of Tenants finished!!!");
	}

	public boolean initEnvironmentOfTenantsRequest(Network virtual, EnvironmentOfServices3 environmentOfServers, Socket socket, Client clientChangeInfoOrquestrator) throws ParserConfigurationException, SAXException, Exception, TransformerException{
		//		net.floodlightcontroller.sirius.console.Network network = new net.floodlightcontroller.sirius.console.Network();
		//		InputStream is = new FileInputStream(fileChanged);
		//		network.readDocument(Config.loadDocument(is));
		//initTenantIdTenantHashMap();
		boolean embeddingSuccess = false;
		String substratePart = environmentOfServers.getPhysicalNetwork().getSubstratePartFileEmbedding();
		Network VirtualNodes = virtual;
		this.tenantList.add(new Tenant3("Tenant "+ virtual.getTenant(), virtual.getTenant()));
		this.initTenantIdTenantHashMap();
		log.info("createVirtualNetwork finished.");
		
		if(this.virtualNetworkIdTenantIdMap.get(VirtualNodes.getTenant())==null){
			VirtualNetwork virtualNetwork = this.createVirtualNodesRequest(VirtualNodes);
			embeddingSuccess = this.EmbeddingVirtualNetwork(virtualNetwork, substratePart, environmentOfServers, clientChangeInfoOrquestrator, environmentOfServers.isRealTimeInfoContainers());
			if(embeddingSuccess){
				this.sendEmbeddingResultsRequest(virtualNetwork, VirtualNodes, socket, clientChangeInfoOrquestrator, environmentOfServers);
			}else{
				this.virtualNetworkArrayList.remove(this.virtualNetworkIdVirtualNetworkMap.get(VirtualNodes.getTenant()));
				this.virtualNetworkIdTenantIdMap.remove(VirtualNodes.getTenant());
				log.info("Embedding for Virtual Network Request {} not worked.", virtualNetwork.getTenantId());
			}
		}
		return embeddingSuccess;
	}

	private void initTenantIdTenantHashMap(){

		Tenant3 tenant = null;
		Iterator<Tenant3> iteratorTenant = this.getTenantList().iterator();

		//Iterate in all tenants in the list of tenants of the EnvironmentOfTenants
		while(iteratorTenant.hasNext()){
			tenant = iteratorTenant.next();
			this.tenantIdTenantMap.put(tenant.getId(), tenant);
		}
	}

	private Network createVirtualNetwork(String fileChanged) throws ParserConfigurationException, SAXException, Exception {

		Network network = new Network();
		InputStream is = new FileInputStream(fileChanged);
		// Read physical network information
		network.readDocument(Config.loadDocument(is));
		this.tenantList.add(new Tenant3("Tenant "+ network.getTenant(), network.getTenant()));
		this.initTenantIdTenantHashMap();
		log.info("createVirtualNetwork finished.");

		return network;
	}

	public void EmbeddingAllVirtualNetworks(String substratePart, EnvironmentOfServices3 environmentOfServers) {

		log.info("EmbeddingAllVirtualNetworks start...");

		for (int i = 0; i < this.virtualNetworkArrayList.size(); i++){
			log.info("createVirtualNetwork finished.");
			//this.EmbeddingVirtualNetwork(this.virtualNetworkArrayList.get(i), substratePart, environmentOfServers);
		}
		log.info("EmbeddingAllVirtualNetworks finished.");
	}



	public boolean EmbeddingVirtualNetwork(VirtualNetwork virtualNetwork, String substratePart, EnvironmentOfServices3 environmentOfServers, Client clientChangeInfoOrquestrator, boolean realTimeContainers) throws IOException {

		boolean embeddingSuccess = false;
		String fileEmbeddingWithoutExtension = "virtualNetwork" + virtualNetwork.getVirtualNetworkId() + "_embedding";
		String pathEmbedding ="/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/";
		if(heuristicEmbedding)
			pathEmbedding += "heuristic/topology/result/";
		else
			pathEmbedding += "secdepvne/topology/result/";
		String fileNameEmbedding = pathEmbedding + fileEmbeddingWithoutExtension+".config";

		if(heuristicEmbedding){	
			EmbeddorHeu embeddorHeu = new EmbeddorHeu(environmentOfServers.getSubstrateNetworkEmbeddingHeu(), virtualNetwork.getVirtualNetworkHeu(), fileNameEmbedding);
			embeddingSuccess = embeddorHeu.solve(typeOfHeuristic);
			File fileResultEmbedding;
			FileInputStream resultEmbedding = new FileInputStream(pathEmbedding + fileEmbeddingWithoutExtension+".smplout");
			while (true){
				if (resultEmbedding.available() != 0){
					break;
				}
			}
			resultEmbedding.close();
			fileResultEmbedding = new File(pathEmbedding + fileEmbeddingWithoutExtension+".smplout");
			virtualNetwork.setResultEmbedding(FileUtils.readFileToString(fileResultEmbedding, "UTF-8"));	
		}else{
			EmbeddorMILP embeddorMILP = new EmbeddorMILP();
			String embeddingInputString = substratePart + virtualNetwork.getVirtualPartFileEmbedding();
			try {
				FileWriter configEmbeddingFile = new FileWriter(new File(fileNameEmbedding));
				configEmbeddingFile.write(embeddingInputString);
				configEmbeddingFile.close();
				embeddingSuccess = embeddorMILP.solve(fileNameEmbedding);
				File fileResultEmbedding;
				FileInputStream resultEmbedding = new FileInputStream(pathEmbedding + fileEmbeddingWithoutExtension+".smplout");
				while (true){
					if (resultEmbedding.available() != 0){
						break;
					}
				}
				resultEmbedding.close();
				fileResultEmbedding = new File(pathEmbedding + fileEmbeddingWithoutExtension+".smplout");
				virtualNetwork.setResultEmbedding(FileUtils.readFileToString(fileResultEmbedding, "UTF-8"));
			}catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		try {
			if(embeddingSuccess){
				if(embeddingSuccess){
					if (virtualNetwork.populateInfoEmbedding(environmentOfServers, typeOfHeuristic)){

						log.info("Virtual Network Embedding info switches and links populate with success in Virtual Network id -{}-", 
								virtualNetwork.getVirtualNetworkId());
					} else{
						embeddingSuccess = false;
					}
				}
				if(embeddingSuccess){
					if (virtualNetwork.defineHosts(environmentOfServers, clientChangeInfoOrquestrator, realTimeContainers, this)){

						log.info("Virtual Network Embedding info hosts defined with success in Virtual Network id -{}-", 
								virtualNetwork.getVirtualNetworkId());
					}else{
						embeddingSuccess = false;
					}
				}
				if(embeddingSuccess){
					if (this.populateHostLocationToVirtualNetworkAndTenantMaps(virtualNetwork, environmentOfServers)){

						log.info("HashMaps to associete HostLocation to Virtual Network and Tenant populated with success to Virtual Network id -{}- and -{}-", 
								virtualNetwork.getVirtualNetworkId(),this.tenantIdTenantMap.get((int)(virtualNetwork.getTenantId())).description);
					}else{
						embeddingSuccess = false;
					}
				}
				log.info("EmbeddingVirtualNetwork -{}- .",pathEmbedding + fileEmbeddingWithoutExtension+".smplout");
			}else{
				String msgFailure = "The Virtual Network Request " + virtualNetwork.getVirtualNetworkId() + " embedding fail."; 	
				log.info(msgFailure);
				clientChangeInfoOrquestrator.sendVirtualFailure(msgFailure);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return embeddingSuccess;
	}

	private boolean populateHostLocationToVirtualNetworkAndTenantMaps(
			VirtualNetwork virtualNetwork,
			EnvironmentOfServices3 environmentOfServers) {

		String physicalHostName;
		Tenant3 tenant;
		HostLocation hostLocation;

		for (int i = 0; i < virtualNetwork.getHostList().size(); i++){
			physicalHostName = virtualNetwork.getHostList().get(i).getPhysicalHost().getHostName();
			tenant = this.tenantIdTenantMap.get(virtualNetwork.getHostList().get(i).getTenantId());
			hostLocation = environmentOfServers.getHostNameHostLocationMap().get(physicalHostName);
			this.hostLocationTenantMap.put(hostLocation, tenant);
			this.hostLocationVirtualNetworkMap.put(hostLocation, virtualNetwork);
		}
		return true;
	}

	private VirtualNetwork createVirtualNodesRequest(Network virtualNodes){

		log.info("Start Virtual Network creation...");
		Map<Integer, ArrayList<Host>> tenantIdHostArrayList = new HashMap<Integer, ArrayList<Host>>();
		Map<Integer, ArrayList<VirtualEdgeSC>> tenantIdVirtualEdgeSCLinkedArrayList = new HashMap<Integer, ArrayList<VirtualEdgeSC>>();
		Map<Integer, ArrayList<VirtualSwitch>> tenantIdsVirtualSwitchLinkedArrayList = new HashMap<Integer, ArrayList<VirtualSwitch>>();
		int tenantId = virtualNodes.getTenant();

		for (int i = 0; i < virtualNodes.getNodes().size(); i++){
			if(virtualNodes.getNodes().get(i) instanceof net.floodlightcontroller.sirius.console.Host){
				String mac = (((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(i)).getMac()==null)?"00:00:00:00:00:00" : ((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(i)).getMac();
				String ip = ((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(i)).getIp();
				String hostName = ((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(i)).getName();
				int virtualHostId = ((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(i)).getId();
				int cpu = ((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(i)).getCpu();
				int memory = 100;//((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(i)).get;
				log.info("TenantHost -{}- MacAddress: -{}-", tenantId+hostName, mac );
				Host h = new Host(virtualHostId, (mac==null) ? MacAddress.NONE : MacAddress.of(mac), VlanVid.ofVlan(0), IPv4Address.of(ip), hostName, tenantId, cpu, memory);
				//TODO Used to do a proper host mapping considering the type of container
				//int typeOfContainer = ((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(i)).getTypeOfContainer();
				//h.setTypeOfContainer(typeOfContainer);

				if(tenantIdHostArrayList.get(tenantId) == null){
					ArrayList<Host> hArray = new ArrayList<Host>();
					hArray.add(h);
					tenantIdHostArrayList.put(tenantId, hArray);
				} else{
					ArrayList<Host> hArray = tenantIdHostArrayList.get(tenantId);
					hArray.add(h);
					tenantIdHostArrayList.put(tenantId, hArray);
				}
				if(this.virtualHostIdVirtualHost.get(virtualHostId) == null){

					this.virtualHostIdVirtualHost.put(virtualHostId, h);
				}
			}
			if(virtualNodes.getNodes().get(i) instanceof net.floodlightcontroller.sirius.console.Switch){

				int virtualSwitchId = ((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(i)).getId();
				int cpu = ((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(i)).getCpu();
				SecurityLevelVirtualNode securityLevel = SecurityLevelVirtualNode.valueOf(((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(i)).getSecurityLevel());
				CloudType cloudType = CloudType.valueOf(((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(i)).getVm().getCloud().getSecurityLevel());
				DependabilityLevelVirtualNode dependabilityLevel = DependabilityLevelVirtualNode.valueOf(((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(i)).getAvailabilityLevel());
				int MAX_FLOW_SIZE = ((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(i)).getMaxFlowSize();
				String name = ((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(i)).getName();
				int memory = 100;//((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(i)).getMem();
				boolean transit = ((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(i)).isTransit(virtualNodes);

				VirtualSwitch v = new VirtualSwitch(virtualSwitchId, name, cpu, securityLevel, cloudType, dependabilityLevel, MAX_FLOW_SIZE, tenantId, memory, transit);

				if(tenantIdsVirtualSwitchLinkedArrayList.get(tenantId) == null){
					ArrayList<VirtualSwitch> vSArray = new ArrayList<VirtualSwitch>();
					vSArray.add(v);

					tenantIdsVirtualSwitchLinkedArrayList.put(tenantId, vSArray);
				} else{
					ArrayList<VirtualSwitch> vSArray  = tenantIdsVirtualSwitchLinkedArrayList.get(tenantId);
					vSArray.add(v);
					tenantIdsVirtualSwitchLinkedArrayList.put(tenantId, vSArray);
				}
				if(this.virtualSwitchIdVirtualSwitch.get(virtualSwitchId) == null){
					this.virtualSwitchIdVirtualSwitch.put(virtualSwitchId, v);
				}
			}
		}
		for (int i = 0; i < virtualNodes.getLinks().size(); i++){
			Link link = virtualNodes.getLinks().get(i);
			int linkId = link.getId();
			int bandwidht = link.getBandwidth();
			int security = link.getSecurityLevel();
			VirtualEdgeSC l = null;
			
			if(link.isBetweenSwitches()){
				VirtualSwitch virtualSwitch1 = this.virtualSwitchIdVirtualSwitch.get(link.getFrom().getId());
				VirtualSwitch virtualSwitch3 = this.virtualSwitchIdVirtualSwitch.get(link.getTo().getId());
				tenantId = virtualSwitch1.getTenantId();
				if(virtualSwitch1 != null && virtualSwitch3 != null){
					l = new VirtualEdgeSC(linkId, virtualSwitch1, virtualSwitch3, bandwidht, SecurityLevelLinks.valueOf(security), true);
				}
				if (tenantId != 0){
					if(tenantIdVirtualEdgeSCLinkedArrayList.get(tenantId) == null){
						ArrayList<VirtualEdgeSC> lArray = new ArrayList<VirtualEdgeSC>();
						lArray.add(l);
						tenantIdVirtualEdgeSCLinkedArrayList.put(tenantId, lArray);
					} else{
						ArrayList<VirtualEdgeSC> lArray  = tenantIdVirtualEdgeSCLinkedArrayList.get(tenantId);
						lArray.add(l);
						tenantIdVirtualEdgeSCLinkedArrayList.put(tenantId, lArray);
					}
				}
			} else{
				VirtualSwitch virtualSwitch = null;
				Host virtualHost = null;
				if(this.virtualSwitchIdVirtualSwitch.get(link.getFrom().getId()) != null && this.virtualHostIdVirtualHost.get(link.getTo().getId()) != null){
					virtualSwitch = this.virtualSwitchIdVirtualSwitch.get(link.getFrom().getId());
					virtualHost = this.virtualHostIdVirtualHost.get(link.getTo().getId());
					tenantId = virtualSwitch.getTenantId();
				} else {
					if(this.virtualSwitchIdVirtualSwitch.get(link.getTo().getId()) != null && this.virtualHostIdVirtualHost.get(link.getFrom().getId()) !=null){
						virtualSwitch = this.virtualSwitchIdVirtualSwitch.get(link.getTo().getId());
						virtualHost = this.virtualHostIdVirtualHost.get(link.getFrom().getId());
						tenantId = virtualSwitch.getTenantId();
					}
				}
				if (virtualHost != null && virtualSwitch != null){

					l = new VirtualEdgeSC(linkId, virtualHost, virtualSwitch, bandwidht, SecurityLevelLinks.valueOf(security), false);
				}
				if(tenantId != 0){
					if(tenantIdVirtualEdgeSCLinkedArrayList.get(tenantId) == null){
						ArrayList<VirtualEdgeSC> lArray = new ArrayList<VirtualEdgeSC>();
						lArray.add(l);
						tenantIdVirtualEdgeSCLinkedArrayList.put(tenantId, lArray);
					} else{
						ArrayList<VirtualEdgeSC> lArray  = tenantIdVirtualEdgeSCLinkedArrayList.get(tenantId);
						lArray.add(l);
						tenantIdVirtualEdgeSCLinkedArrayList.put(tenantId, lArray);
					}
				}
			}
		}
		if (this.virtualSwitchIdVirtualSwitch != null){

			this.virtualSwitchIdVirtualSwitch.clear();
		}
		if (this.virtualHostIdVirtualHost != null){

			this.virtualHostIdVirtualHost.clear();
		}

		Iterator<Integer> it2 = tenantIdsVirtualSwitchLinkedArrayList.keySet().iterator();

		VirtualNetwork vn = null;
		while (it2.hasNext()) {

			Integer pair = it2.next();
			@SuppressWarnings("unused")
			ArrayList<VirtualSwitch> a = tenantIdsVirtualSwitchLinkedArrayList.get(pair.intValue());
			@SuppressWarnings("unused")
			ArrayList<Host> b = tenantIdHostArrayList.get(pair.intValue());
			@SuppressWarnings("unused")
			ArrayList<VirtualEdgeSC> c = tenantIdVirtualEdgeSCLinkedArrayList.get(pair.intValue());

			if(tenantIdsVirtualSwitchLinkedArrayList.get(pair.intValue()) != null && 
					tenantIdHostArrayList.get(pair.intValue()) != null &&
					tenantIdVirtualEdgeSCLinkedArrayList.get(pair.intValue()) != null){

				int virtualNetworkLifeTime = 100000;
				vn = new VirtualNetwork(tenantIdHostArrayList.get(pair.intValue()), 
						tenantIdVirtualEdgeSCLinkedArrayList.get(pair.intValue()), tenantIdsVirtualSwitchLinkedArrayList.get(pair.intValue()),
						true, virtualNetworkLifeTime, (int) pair.intValue());

				if(!this.virtualNetworkArrayList.contains(vn)){
					if(vn.initGraph() && vn.initDijkstra()){
						this.virtualNetworkArrayList.add(vn);
						this.virtualNetworkIdTenantIdMap.put(vn.getVirtualNetworkId(), vn.getTenantId());
						this.virtualNetworkIdVirtualNetworkMap.put(vn.getVirtualNetworkId(), vn);
						LinkedList<VirtualSwitch> path = new LinkedList<VirtualSwitch>();
						if(vn.getVirtualSwitchArrayList() != null && vn.getVirtualSwitchArrayList().size()>1){
							vn.getDijkstraVirtualNetwork().execute(vn.getVirtualSwitchArrayList().get(0));
							path = vn.getDijkstraVirtualNetwork().getPath(vn.getVirtualSwitchArrayList().get(1));

							for (VirtualSwitch vertex : path) {
								System.out.println(vertex.getName()+"-"+vertex.getVirtualSwitchId());
							}
						}
					}
				}
				log.info("(int) pair.intValue() -{}-",(int) pair.intValue());
			}
		}
		
		//TODO
		//this.createAllVirtualTopologyFileEmbedding();
		if(heuristicEmbedding)
			this.createVirtualTopologyFileEmbeddingHeu(vn);
		else
			this.createVirtualTopologyFileEmbeddingMILP(vn);
			
		log.info("Virtual Network creation finished!");

		return vn;
	}
	private void createVirtualTopologyFileEmbeddingHeu(VirtualNetwork vn) {
		
		VirtualNetworkHeu vnHeu = new VirtualNetworkHeu(10);
		
		for (int i=0; i < vn.getVirtualSwitchArrayList().size(); i++){
			vnHeu.addNode(vn.getVirtualSwitchArrayList().get(i).getVirtualSwitchId()+"");
			vnHeu.addNodeCPU(vn.getVirtualSwitchArrayList().get(i).getCpu());
			vnHeu.addNodeSec(vn.getVirtualSwitchArrayList().get(i).getSecurityLevel().getValue());
			vnHeu.addCloudSecurity(vn.getVirtualSwitchArrayList().get(i).getCloudType().getValue());
			vnHeu.addBackupLocalization(vn.getVirtualSwitchArrayList().get(i).getDependabilityLevel().getValue());
			vnHeu.addWantBackupNode(false);
			//TODO Implement the verification
//			vnHeu.addDemandsVirtualHosts(true);
			vnHeu.addDemandsVirtualHosts(!vn.getVirtualSwitchArrayList().get(i).isTransit());
			
		}
		for (int i=0; i < vn.getVirtualEdgeSCArrayList().size(); i++){
			if (vn.getVirtualEdgeSCArrayList().get(i).isBetweenSwitches()){
				vnHeu.addEdge(new Pair<String>(vn.getVirtualEdgeSCArrayList().get(i).getSourceSwitch().getVirtualSwitchId()+""
						, vn.getVirtualEdgeSCArrayList().get(i).getDestinationSwitch().getVirtualSwitchId()+""));
				vnHeu.addEdgeBw(vn.getVirtualEdgeSCArrayList().get(i).getBandwidht());
				vnHeu.addEdgeSec(vn.getVirtualEdgeSCArrayList().get(i).getSecurityLevel().getValue());
				vnHeu.addEdgeLatency(100);
			}
		}
		vnHeu.setWantBackup(false);
		vn.setVirtualNetworkHeu(vnHeu);
	}


	private void createAllVirtualTopologyFileEmbedding() {

		FileWriter configEmbeddingFile;
		String virtualPart = "";

		for (int i = 0; i < this.virtualNetworkArrayList.size(); i++){
			virtualPart = "Virtual\n\n";

			int NumNodes = 0;
			int NumLinks = 0;
			ArrayList<String> NodesID = new ArrayList<String>();
			ArrayList<String> NodesCPUDem = new ArrayList<String>();
			ArrayList<String> NodesSecDem = new ArrayList<String>();
			ArrayList<String> NodesLocDem = new ArrayList<String>();
			ArrayList<String> NodesBackupLocDem = new ArrayList<String>();
			ArrayList<String> Links = new ArrayList<String>();
			ArrayList<String> LinksBwDem = new ArrayList<String>();
			ArrayList<String> LinksSecDem = new ArrayList<String>(); 

			if (this.virtualNetworkArrayList.get(i) != null){
				if(this.virtualNetworkArrayList.get(i).getVirtualSwitchArrayList() != null){
					NumNodes = this.virtualNetworkArrayList.get(i).getVirtualSwitchArrayList().size();
					for (int i1=0; i1 < this.virtualNetworkArrayList.get(i).getVirtualSwitchArrayList().size(); i1++){

						NodesID.add(this.virtualNetworkArrayList.get(i).getVirtualSwitchArrayList().get(i1).getVirtualSwitchId()+" ");
						NodesCPUDem.add(this.virtualNetworkArrayList.get(i).getVirtualSwitchArrayList().get(i1).getCpu()+" ");
						NodesSecDem.add(this.virtualNetworkArrayList.get(i).getVirtualSwitchArrayList().get(i1).getSecurityLevel().getValue()+" ");
						NodesLocDem.add(this.virtualNetworkArrayList.get(i).getVirtualSwitchArrayList().get(i1).getCloudType().getValue()+" ");
						NodesBackupLocDem.add(this.virtualNetworkArrayList.get(i).getVirtualSwitchArrayList().get(i1).getDependabilityLevel().getValue()+" ");
					}
				}
				if(this.virtualNetworkArrayList.get(i).getVirtualEdgeSCArrayList() != null){
					for (int i1=0; i1 < this.virtualNetworkArrayList.get(i).getVirtualEdgeSCArrayList().size(); i1++){
						if (this.virtualNetworkArrayList.get(i).getVirtualEdgeSCArrayList().get(i1).isBetweenSwitches()){
							Links.add("("+this.virtualNetworkArrayList.get(i).getVirtualEdgeSCArrayList().get(i1).getSourceSwitch().getVirtualSwitchId()+","+
									this.virtualNetworkArrayList.get(i).getVirtualEdgeSCArrayList().get(i1).getDestinationSwitch().getVirtualSwitchId()+") ");
							LinksBwDem.add(this.virtualNetworkArrayList.get(i).getVirtualEdgeSCArrayList().get(i1).getBandwidht()+" ");
							LinksSecDem.add(this.virtualNetworkArrayList.get(i).getVirtualEdgeSCArrayList().get(i1).getSecurityLevel().getValue()+" ");

							NumLinks = NumLinks + 1;
						}
					}
				}
				//virtual part creation
				String sAux = "";
				virtualPart = virtualPart.concat("NumNodes: "+ NumNodes +"\r\n");
				virtualPart = virtualPart.concat("NumLinks: "+ NumLinks +"\r\n");
				sAux = ((NodesID.toString().replace("[", "")).replace("]", "")).replace(", ", "");
				sAux = sAux.substring(0, sAux.length()-1);
				virtualPart = virtualPart.concat("NodesID: "+ sAux +"\r\n");
				sAux = ((NodesCPUDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
				sAux = sAux.substring(0, sAux.length()-1);
				virtualPart = virtualPart.concat("NodesCPUDem: "+ sAux +"\r\n");
				sAux = ((NodesSecDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
				sAux = sAux.substring(0, sAux.length()-1);
				virtualPart = virtualPart.concat("NodesSecDem: "+ sAux +"\r\n");
				sAux = ((NodesLocDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
				sAux = sAux.substring(0, sAux.length()-1);
				virtualPart = virtualPart.concat("NodesLocDem: "+ sAux +"\r\n");
				sAux = ((NodesBackupLocDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
				sAux = sAux.substring(0, sAux.length()-1);
				virtualPart = virtualPart.concat("NodesBackupLocDem: "+ sAux +"\r\n");
				if (NumLinks ==0){
					sAux = "0";
				} else{
					sAux = ((Links.toString().replace("[", "")).replace("]", "")).replace(", ", "");
					sAux = sAux.substring(0, sAux.length()-1);
				}
				virtualPart = virtualPart.concat("Links: "+ sAux +"\r\n");

				if (NumLinks ==0){
					sAux = "0";
				} else{
					sAux = ((LinksBwDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
					sAux = sAux.substring(0, sAux.length()-1);
				}
				virtualPart = virtualPart.concat("LinksBwDem: "+ sAux +"\r\n");

				if (NumLinks ==0){
					sAux = "0";
				} else{
					sAux = ((LinksSecDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
					sAux = sAux.substring(0, sAux.length()-1);
				}
				virtualPart = virtualPart.concat("LinksSecDem: "+ sAux +"\r\n");

				if(virtualNetworkArrayList.get(i).isNeedBackup()){

					virtualPart = virtualPart.concat("Backup: 1\r\n");
					this.virtualNetworkArrayList.get(i).setNeedBackup(true);
				}else{

					virtualPart = virtualPart.concat("Backup: 0\r\n");
					this.virtualNetworkArrayList.get(i).setNeedBackup(false);
				}
				log.info("Virtual part creation - Virtual Nettwork id: "+ this.virtualNetworkArrayList.get(i).getVirtualNetworkId());
				System.out.print(virtualPart);
				//Save the file
				try {
					configEmbeddingFile = new FileWriter(new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/secdepvne/topology/virtual/virtualNetwork"+
							this.virtualNetworkArrayList.get(i).getVirtualNetworkId()+".config"));
					configEmbeddingFile.write(virtualPart);
					configEmbeddingFile.close();

					this.getVirtualNetworkArrayList().get(i).setVirtualPartFileEmbedding(virtualPart);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.virtualNetworkArrayList.get(i).setVirtualPartFileEmbedding(virtualPart);
			virtualPart = "";
		}
	}

	private void createVirtualTopologyFileEmbeddingMILP(VirtualNetwork virtualNetwork) {

		FileWriter configEmbeddingFile;
		String virtualPart = "";
		virtualPart = "Virtual\n\n";
		int NumNodes = 0;
		int NumLinks = 0;
		ArrayList<String> NodesID = new ArrayList<String>();
		ArrayList<String> NodesCPUDem = new ArrayList<String>();
		ArrayList<String> NodesSecDem = new ArrayList<String>();
		ArrayList<String> NodesLocDem = new ArrayList<String>();
		ArrayList<String> NodesBackupLocDem = new ArrayList<String>();
		ArrayList<String> Links = new ArrayList<String>();
		ArrayList<String> LinksBwDem = new ArrayList<String>();
		ArrayList<String> LinksSecDem = new ArrayList<String>(); 

		if (virtualNetwork != null){
			if(virtualNetwork.getVirtualSwitchArrayList() != null){
				NumNodes = virtualNetwork.getVirtualSwitchArrayList().size();
				for (int i1=0; i1 < virtualNetwork.getVirtualSwitchArrayList().size(); i1++){
					NodesID.add(virtualNetwork.getVirtualSwitchArrayList().get(i1).getVirtualSwitchId()+" ");
					NodesCPUDem.add(virtualNetwork.getVirtualSwitchArrayList().get(i1).getCpu()+" ");
					NodesSecDem.add(virtualNetwork.getVirtualSwitchArrayList().get(i1).getSecurityLevel().getValue()+" ");
					NodesLocDem.add(virtualNetwork.getVirtualSwitchArrayList().get(i1).getCloudType().getValue()+" ");
					NodesBackupLocDem.add(virtualNetwork.getVirtualSwitchArrayList().get(i1).getDependabilityLevel().getValue()+" ");
				}
			}

			if(virtualNetwork.getVirtualEdgeSCArrayList() != null){
				for (int i1=0; i1 < virtualNetwork.getVirtualEdgeSCArrayList().size(); i1++){
					if (virtualNetwork.getVirtualEdgeSCArrayList().get(i1).isBetweenSwitches()){
						Links.add("("+virtualNetwork.getVirtualEdgeSCArrayList().get(i1).getSourceSwitch().getVirtualSwitchId()+","+
								virtualNetwork.getVirtualEdgeSCArrayList().get(i1).getDestinationSwitch().getVirtualSwitchId()+") ");
						LinksBwDem.add(virtualNetwork.getVirtualEdgeSCArrayList().get(i1).getBandwidht()+" ");
						LinksSecDem.add(virtualNetwork.getVirtualEdgeSCArrayList().get(i1).getSecurityLevel().getValue()+" ");
						NumLinks = NumLinks + 1;
					}
				}
			}
			
			//virtual part creation
			String sAux = "";
			virtualPart = virtualPart.concat("NumNodes: "+ NumNodes +"\r\n");
			virtualPart = virtualPart.concat("NumLinks: "+ NumLinks +"\r\n");
			sAux = ((NodesID.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			virtualPart = virtualPart.concat("NodesID: "+ sAux +"\r\n");
			sAux = ((NodesCPUDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			virtualPart = virtualPart.concat("NodesCPUDem: "+ sAux +"\r\n");
			sAux = ((NodesSecDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			virtualPart = virtualPart.concat("NodesSecDem: "+ sAux +"\r\n");
			sAux = ((NodesLocDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			virtualPart = virtualPart.concat("NodesLocDem: "+ sAux +"\r\n");
			sAux = ((NodesBackupLocDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
			sAux = sAux.substring(0, sAux.length()-1);
			virtualPart = virtualPart.concat("NodesBackupLocDem: "+ sAux +"\r\n");

			if (NumLinks ==0){
				sAux = "0";
			} else{
				sAux = ((Links.toString().replace("[", "")).replace("]", "")).replace(", ", "");
				sAux = sAux.substring(0, sAux.length()-1);
			}
			virtualPart = virtualPart.concat("Links: "+ sAux +"\r\n");
			if (NumLinks ==0){
				sAux = "0";
			} else{
				sAux = ((LinksBwDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
				sAux = sAux.substring(0, sAux.length()-1);
			}
			virtualPart = virtualPart.concat("LinksBwDem: "+ sAux +"\r\n");
			if (NumLinks ==0){
				sAux = "0";
			} else{
				sAux = ((LinksSecDem.toString().replace("[", "")).replace("]", "")).replace(", ", "");
				sAux = sAux.substring(0, sAux.length()-1);
			}
			virtualPart = virtualPart.concat("LinksSecDem: "+ sAux +"\r\n");
			if(virtualNetwork.isNeedBackup()){
				virtualPart = virtualPart.concat("Backup: 1\r\n");
				virtualNetwork.setNeedBackup(true);
			}else{
				virtualPart = virtualPart.concat("Backup: 0\r\n");
				virtualNetwork.setNeedBackup(false);
			}
			log.info("Virtual part creation - Virtual Nettwork id: "+ virtualNetwork.getVirtualNetworkId());
			System.out.print(virtualPart);

			//Save the file
			try {
				configEmbeddingFile = new FileWriter(new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/embedding/pt/lasige/secdepvne/topology/virtual/virtualNetwork"+
						virtualNetwork.getVirtualNetworkId()+".config"));
				configEmbeddingFile.write(virtualPart);
				configEmbeddingFile.close();

				virtualNetwork.setVirtualPartFileEmbedding(virtualPart);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		virtualNetwork.setVirtualPartFileEmbedding(virtualPart);
		virtualPart = "";
	}

	public void sendEmbeddingResultsRequest(VirtualNetwork virtualNetwork, Network virtualNodes, Socket socket, Client clientChangeInfoOrquestrator, EnvironmentOfServices3 environmentOfServers) throws TransformerException, Exception{

		virtualNodes = this.createResultVirtualNetworkRequest(virtualNetwork, virtualNodes, environmentOfServers);
		virtualNodes.writeXML("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/topology/virtual/requestMap"+
				virtualNetwork.getVirtualNetworkId()+".xml");

		clientChangeInfoOrquestrator.sendVirtualEmbeddingSuccess(virtualNodes);
	}


	private Network createResultVirtualNetworkRequest(VirtualNetwork virtualNetwork, Network virtualNodes, EnvironmentOfServices3 environmentOfServers){

		for (int i = 0; i < virtualNetwork.getHostList().size(); i++){
			for (int j = 0; j < virtualNodes.getNodes().size(); j++){
				if(virtualNodes.getNodes().get(j) instanceof net.floodlightcontroller.sirius.console.Host){
					if(virtualNodes.getNodes().get(j).getId() == virtualNetwork.getHostList().get(i).getHostId()){
						//TODO
						((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(j)).setMapping(
								virtualNetwork.getHostList().get(i).getPhysicalHost().getHostId());
//						((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(j)).setMapping(i);
						OFPort port = environmentOfServers.getHostNameHostLocationMap().get(
								virtualNetwork.getHostList().get(i).getPhysicalHost().getHostName()).getPort();
						((net.floodlightcontroller.sirius.console.Host)virtualNodes.getNodes().get(j)).setPort(
								port.getPortNumber());
					}
				}
			}
		}
		for (int i = 0; i < virtualNetwork.getVirtualSwitchArrayList().size(); i++){
			for (int j = 0; j < virtualNodes.getNodes().size(); j++){
				if(virtualNodes.getNodes().get(j) instanceof net.floodlightcontroller.sirius.console.Switch){
					if(virtualNodes.getNodes().get(j).getId() == virtualNetwork.getVirtualSwitchArrayList().get(i).getVirtualSwitchId()){

						((net.floodlightcontroller.sirius.console.Switch)virtualNodes.getNodes().get(j)).setMapping(
								virtualNetwork.getVirtualSwitchArrayList().get(i).getWorkingSwitch().getServerId());
					}
				}
			}
		}
		for (int i = 0; i < virtualNetwork.getVirtualEdgeSCArrayList().size(); i++){
			for (int j = 0; j < virtualNodes.getLinks().size(); j++){
				if(virtualNodes.getLinks().get(j).getId() == virtualNetwork.getVirtualEdgeSCArrayList().get(i).getVirtualEdgeSCId()){
					(virtualNodes.getLinks().get(j)).setRoute(
							virtualNetwork.getVirtualEdgeSCArrayList().get(i).getWorkingPathLinks());
				}
			}
		}
		return virtualNodes;
	}

	public String getMininetIp() {
		return mininetIp;
	}

	public void setMininetIp(String mininetIp) {
		this.mininetIp = mininetIp;
	}

	public String getMininetUser() {
		return mininetUser;
	}

	public void setMininetUser(String mininetUser) {
		this.mininetUser = mininetUser;
	}

	public String getMininetPassword() {
		return mininetPassword;
	}

	public void setMininetPassword(String mininetPassword) {
		this.mininetPassword = mininetPassword;
	}
}

