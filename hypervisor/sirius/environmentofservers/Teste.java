package net.floodlightcontroller.sirius.environmentofservers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.MacVlanPair;
import net.floodlightcontroller.debugcounter.IDebugCounter;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.debugcounter.IDebugCounterService.MetaData;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.sirius.environmentofservers.EnvironmentOfServices;
import net.floodlightcontroller.sirius.environmentoftenants.EnvironmentOfTenants;
import net.floodlightcontroller.sirius.environmentoftenants.Tenant;
import net.floodlightcontroller.sirius.learningswitchtenant.LearningSwitchTenant;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.VlanVid;
import org.projectfloodlight.openflow.util.LRULinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Teste {


	protected ArrayList<Server> serverList;
	EnvironmentOfServices environmentOfServices;
	protected static Logger log = LoggerFactory.getLogger(Teste.class);
	protected Map<Tenant, Map<MacVlanPair, OFPort>> macVlanToTenantPortMap = new ConcurrentHashMap<Tenant, Map<MacVlanPair, OFPort>>(); 
	//protected Map<IOFSwitch, TenantVlanMac> switchTenantMap;
	protected Map<Server, Map<Tenant, Map<MacVlanPair, OFPort>>> switchTenantMap = new ConcurrentHashMap<Server, Map<Tenant, Map<MacVlanPair, OFPort>>>();

	//Tenants

	public Teste(ArrayList<Server> serverList) {

		this.serverList = serverList;	
	}

	public Teste(ArrayList<Server> serverList, EnvironmentOfServices environmentOfServices) {
		super();
		this.serverList = serverList;
		this.environmentOfServices = environmentOfServices;
	}

	public ArrayList<ArrayList<Server>> getCombinationOfServers(final ArrayList<Server> serverList){

		ArrayList<ArrayList<Server>> combination = new ArrayList<ArrayList<Server>>();

		if (serverList != null){

			for (int i = 0; i < serverList.size()-1; i++){
				for (int j = i+1; j < serverList.size(); j++ ){

					System.out.print(serverList.get(i).getHostname());
					System.out.println(serverList.get(j).getHostname());

					combination.add(new ArrayList<Server>(Arrays.asList(serverList.get(i),serverList.get(j))));

				}
			}
		}			
		return combination;
	}

	protected synchronized void addToPortMap(Server sw, MacAddress mac, VlanVid vlan, OFPort portVal, Tenant tenant) {

		//		Tenant tenant = this.getTenantByMac(mac);
		//Map<MacVlanPair, OFPort> swMap = macVlanToSwitchPortMap.get(sw);
		log.info("addToPortMap1 sw: -{} mac: -{}",sw.toString(), mac.toString());
		log.info("addToPortMap2 vlan: -{} portVal: -{}",vlan.toString(), portVal.toString());
		Map<MacVlanPair, OFPort> tenantMap = null;
		
		Map<Tenant, Map<MacVlanPair, OFPort>> swMap = switchTenantMap.get(sw);

		if (tenant != null && swMap != null){

			//tenantMap = macVlanToTenantPortMap.get(tenant);
			tenantMap = switchTenantMap.get(sw).get(tenant);
			log.info("addToPortMap3 tenant: -{} ",tenant.getDescription());

		}

		if (vlan == VlanVid.FULL_MASK || vlan == null) {
			vlan = VlanVid.ofVlan(0);
		}

		//		if (swMap == null) {
		//		if (tenant !=null) {

		//		}

		if (swMap == null) {
			// May be accessed by REST API so we need to make it thread safe
			swMap = Collections.synchronizedMap(new LRULinkedHashMap<Tenant, Map<MacVlanPair, OFPort>>(1000));
			switchTenantMap.put(sw, swMap);
		}

		swMap.put(tenant,tenantMap);

		
		if (tenantMap == null ) {
			// May be accessed by REST API so we need to make it thread safe
			//			swMap = Collections.synchronizedMap(new LRULinkedHashMap<MacVlanPair, OFPort>(MAX_MACS_PER_SWITCH));
			tenantMap = Collections.synchronizedMap(new LRULinkedHashMap<MacVlanPair, OFPort>(1000));
			//macVlanToSwitchPortMap.put(sw, swMap);
			switchTenantMap.get(sw).put(tenant, tenantMap);
		}
		//swMap.put(new MacVlanPair(mac, vlan), portVal);
		tenantMap.put(new MacVlanPair(mac, vlan), portVal);
	}


	public synchronized OFPort getFromPortMap(Server sw, MacAddress sourceMac, MacAddress destMac, VlanVid vlan, Tenant tenant) {

		if (vlan == VlanVid.FULL_MASK || vlan == null) {
			vlan = VlanVid.ofVlan(0);
		}

		//		Tenant tenant = getTenantfrom2Macs(sw, sourceMac, destMac);


		if (tenant != null) {
			log.info("getFromPortMap1 sw: -{} tenant: -{}",sw.toString(), tenant.getDescription());
			log.info("getFromPortMap2 sm: -{} dm: -{}",sourceMac.toString(), destMac.toString());

			Map<MacVlanPair, OFPort> tenantMap = switchTenantMap.get(sw).get(tenant);
			//		Map<MacVlanPair, OFPort> swMap = macVlanToSwitchPortMap.get(sw);

			//		if (swMap != null ) {
			if (sourceMac.toString().compareTo("00:00:00:00:00:01")==0 && destMac.toString().compareTo("00:00:00:00:00:02")==0){
				log.info("getFromPortMap3");

			}

			if (tenantMap != null ) {

				//				log.info("getFromPortMap3 -{}",tenantMap.get(new MacVlanPair(destMac, vlan)).toString());
				//return swMap.get(new MacVlanPair(mac, vlan));
				return tenantMap.get(new MacVlanPair(sourceMac, vlan));
			}}

		// if none found
		return null;
	}

	@SuppressWarnings("unused")
	private void writeFLowModTenant(Match m, OFPacketIn pi, OFPort outPort, Tenant tenant) {

		log.info("writeFLowModTenant1");

		Collection<Server> switchSet = switchTenantMap.keySet();

		if (switchSet != null){

			Iterator<Server> iteratorSwitch = switchSet.iterator();

			while(iteratorSwitch.hasNext()){

				//Percorrer pelos mac_ports de desterminado Tenants

				Map<MacVlanPair, OFPort> macVlanPairPortMap = switchTenantMap.get(iteratorSwitch.next()).get(tenant);

				if (macVlanPairPortMap != null){

					log.info("writeFLowModTenant if macVlanPairPortMap1 -{}-", macVlanPairPortMap.get(new MacVlanPair(m.get(MatchField.ETH_SRC), m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid())));

					if (macVlanPairPortMap.get(new MacVlanPair(m.get(MatchField.ETH_SRC), m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid()))==outPort){

						log.info("writeFLowModTenant if 1");

						//writeFlow
					}

					log.info("writeFLowModTenant if macVlanPairPortMap2 -{}-", macVlanPairPortMap.get(new MacVlanPair(m.get(MatchField.ETH_DST), m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid())));
					if (macVlanPairPortMap.get(new MacVlanPair(m.get(MatchField.ETH_DST), m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid()))==pi.getInPort()){

						log.info("writeFLowModTenant if 2");

						//writeFlow
					}


					//					Iterator<MacVlanPair> iteratorMacVlanPair = macVlanPairPortMap.iterator();

					//					while(iteratorMacVlanPair.hasNext()){

					//
					//					}
				}
			}

		}		



	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String result = "";
		ArrayList<Bridge> bridgeList = null;
		Bridge bridge;
		String bn = "";
		boolean a ;
		byte[] address1 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)1};
		byte[] address2 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)2};
		byte[] address3 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)3};
		byte[] address4 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)4};

		Tenant tenant1 = new Tenant("Tenant 1", 1, null);
		Tenant tenant2 = new Tenant("Tenant 2", 2, null);

		ArrayList<MacAddress> macList1 = new ArrayList<MacAddress>();
		ArrayList<MacAddress> macList2 = new ArrayList<MacAddress>();

		macList1.add(MacAddress.of(address1));
		macList1.add(MacAddress.of(address2));
		macList2.add(MacAddress.of(address3));
		macList2.add(MacAddress.of(address4));

		tenant1.setMacList(macList1);
		tenant2.setMacList(macList2);


		//		this.tenant1.setMacList(macList1);
		Server s1 = new Server(null, null, "192.168.57.102", "mininet", "mininet", "TesteOVS");
		Server s2 = new Server(null, "S2", "", "", "");
		//Server s3 = new Server(null, "S3", "", "", "");
		//Server s4 = new Server(null, "S4", "", "", "");
		//UUID u = UUID.fromString("8945dad2-35b4-4549-9122-23d5558992e7");

		ArrayList<Server> serverList = new ArrayList<Server>();

		serverList.add(s1);
		//serverList.add(s2);
		//serverList.add(s3);
		//serverList.add(s4);

		EnvironmentOfServices environmentOfServices = new EnvironmentOfServices("gre");

		//environmentOfServices.populateEnvironment();
		//environmentOfServices.populateEnvironment();
		/*		
		bridgeList = new ArrayList<Bridge>();
		bridge = new Bridge("");
		bridgeList.add(new Bridge(""));
		bridgeList.add(new Bridge(""));

		a= bridgeList.contains(new Bridge(""));
		a=bridgeList.get(1).equals(new Bridge(""));
		 */
		//@SuppressWarnings("unused")
		//ArrayList<ArrayList<Server>> combination;
		Teste teste = new Teste(serverList,environmentOfServices);
		//combination = teste.getCombinationOfServers(serverList);
         teste.rodaThread();

		//teste.addToPortMap(s1, macList1.get(0), VlanVid.ofVlan(0), OFPort.of(34), tenant1);
		//teste.addToPortMap(s1, macList1.get(1), VlanVid.ofVlan(0), OFPort.of(38), tenant1);
		//teste.addToPortMap(s2, macList1.get(0), VlanVid.ofVlan(0), OFPort.of(13), tenant1);
		//teste.addToPortMap(s1, macList2.get(1), VlanVid.ofVlan(0), OFPort.of(37), tenant2);

		System.out.println("Fim");

	}

	private void rodaThread() {

		
		for(int i=0; i<100; i++){
		      new Thread(){
		        public void run(){
		          System.out.println("Thread: " + getName() + " running");
		        }
		      }.start();
		    }
	}



}
