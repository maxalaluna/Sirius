/**
 *    Copyright 2011, Big Switch Networks, Inc.
 *    Originally created by David Erickson, Stanford University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

/**
 * Floodlight
 * A BSD licensed, Java based OpenFlow controller
 *
 * Floodlight is a Java based OpenFlow controller originally written by David Erickson at Stanford
 * University. It is available under the BSD license.
 *
 * For documentation, forums, issue tracking and more visit:
 *
 * http://www.openflowhub.org/display/Floodlight/Floodlight+Home
 **/

package net.floodlightcontroller.sirius.learningswitchtenant;

import java.util.ArrayList;
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

//import sun.nio.cs.ext.MacArabic;

public class LearningSwitchTenant
implements IFloodlightModule, ILearningSwitchServiceTenant, IOFMessageListener {
	protected static Logger log = LoggerFactory.getLogger(LearningSwitchTenant.class);

	//Tenants

	//	byte[] address1 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)1};
	//	byte[] address2 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)2};
	//	byte[] address3 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)3};
	//	byte[] address4 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)4};

	//	Tenant tenant1 = new Tenant("Tenant 1", 1, null);
	//	Tenant tenant2 = new Tenant("Tenant 2", 2, null);

	//	ArrayList<MacAddress> macList1 = new ArrayList<MacAddress>();
	//	ArrayList<MacAddress> macList2 = new ArrayList<MacAddress>();

	//ArrayList<Tenant> tenantList = new ArrayList<Tenant>();

	//	Server server1 = new Server(null, "192.168.4.21","root","quinta","Server01 (s1) - 192.168.4.21");
	//	Server server2 = new Server(null, "192.168.5.34","root","quinta","Server02 (s14) - 192.168.5.34");
	//	Server server3 = new Server(null, "192.168.4.201","root","quinta","Server03 (sg1) - 192.168.4.201");

	//	ArrayList<Server> serverList = new ArrayList<Server>();

	EnvironmentOfServices environmentOfServices = new EnvironmentOfServices("gre");

	EnvironmentOfTenants environmentOfTenants = new EnvironmentOfTenants();


	// Module dependencies
	protected IFloodlightProviderService floodlightProviderService;
	protected IRestApiService restApiService;

	protected IDebugCounterService debugCounterService;
	private IDebugCounter counterFlowMod;
	private IDebugCounter counterPacketOut;


	// Stores the learned state for each switch - altered to exist an intermediary structure concerning to the Tenants
	//protected Map<IOFSwitch, Map<MacVlanPair, OFPort>> macVlanToSwitchPortMap;
	protected Map<Tenant, Map<MacVlanPair, OFPort>> macVlanToTenantPortMap; 
	//protected Map<IOFSwitch, TenantVlanMac> switchTenantMap;
	protected Map<IOFSwitch, Map<Tenant, Map<MacVlanPair, OFPort>>> switchTenantMap;

	// flow-mod - for use in the cookie
	public static final int LEARNING_SWITCH_APP_ID = 1;
	// LOOK! This should probably go in some class that encapsulates
	// the app cookie management
	public static final int APP_ID_BITS = 12;
	public static final int APP_ID_SHIFT = (64 - APP_ID_BITS);
	public static final long LEARNING_SWITCH_COOKIE = (long) (LEARNING_SWITCH_APP_ID & ((1 << APP_ID_BITS) - 1)) << APP_ID_SHIFT;

	// more flow-mod defaults
	//protected static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 5; // in seconds
	protected static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 5; // infinite
	protected static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
	protected static short FLOWMOD_PRIORITY = 100;

	// for managing our map sizes
	protected static final int MAX_MACS_PER_SWITCH  = 1000;

	// normally, setup reverse flow as well. Disable only for using cbench for comparison with NOX etc.
	protected static final boolean LEARNING_SWITCH_REVERSE_FLOW = true;

	/**
	 * @param floodlightProvider the floodlightProvider to set
	 */
	public void setFloodlightProvider(IFloodlightProviderService floodlightProviderService) {
		this.floodlightProviderService = floodlightProviderService;
	}

	@Override
	public String getName() {
		return "learningswitch";
	}

	public Tenant getTenantByMac(MacAddress mac){

		Tenant tenant;

		//		if (this.tenantList != null) {
		if (this.environmentOfTenants.getTenantList() != null) {

			//			Iterator<Tenant> iteratorTenant = tenantList.iterator();
			Iterator<Tenant> iteratorTenant = this.environmentOfTenants.getTenantList().iterator();

			while(iteratorTenant.hasNext()){

				tenant = iteratorTenant.next();

				if (tenant.getMacList().contains(mac)){
					return tenant;
				}
			}			
		}

		return null;
	}

	/**
	 * Adds a host to the MAC/VLAN->SwitchPort mapping
	 * @param sw The switch to add the mapping to
	 * @param mac The MAC address of the host to add
	 * @param vlan The VLAN that the host is on
	 * @param portVal The switchport that the host is on
	 */
	//	protected void addToPortMap(IOFSwitch sw, MacAddress mac, VlanVid vlan, OFPort portVal) {
	protected void addToPortMap(IOFSwitch sw, MacAddress mac, VlanVid vlan, OFPort portVal) {

		Tenant tenant = this.getTenantByMac(mac);
		//Map<MacVlanPair, OFPort> swMap = macVlanToSwitchPortMap.get(sw);
		Map<MacVlanPair, OFPort> tenantMap = macVlanToTenantPortMap.get(tenant);
		Map<Tenant, Map<MacVlanPair, OFPort>> swMap = switchTenantMap.get(sw);

		if (vlan == VlanVid.FULL_MASK || vlan == null) {
			vlan = VlanVid.ofVlan(0);
		}

		//		if (swMap == null) {
		if (tenantMap == null) {
			// May be accessed by REST API so we need to make it thread safe
			//			swMap = Collections.synchronizedMap(new LRULinkedHashMap<MacVlanPair, OFPort>(MAX_MACS_PER_SWITCH));
			tenantMap = Collections.synchronizedMap(new LRULinkedHashMap<MacVlanPair, OFPort>(MAX_MACS_PER_SWITCH));
			//macVlanToSwitchPortMap.put(sw, swMap);
			macVlanToTenantPortMap.put(tenant, tenantMap);
		}
		//swMap.put(new MacVlanPair(mac, vlan), portVal);
		tenantMap.put(new MacVlanPair(mac, vlan), portVal);


		if (swMap == null) {
			// May be accessed by REST API so we need to make it thread safe
			swMap = Collections.synchronizedMap(new LRULinkedHashMap<Tenant, Map<MacVlanPair, OFPort>>(MAX_MACS_PER_SWITCH));
			switchTenantMap.put(sw, swMap);
		}

		swMap.put(tenant,tenantMap);

	}

	/**
	 * Removes a host from the MAC/VLAN->SwitchPort mapping
	 * @param sw The switch to remove the mapping from
	 * @param mac The MAC address of the host to remove
	 * @param vlan The VLAN that the host is on
	 */
	protected void removeFromPortMap(IOFSwitch sw, MacAddress sourceMac, MacAddress destMac, VlanVid vlan) {

		if (vlan == VlanVid.FULL_MASK) {
			vlan = VlanVid.ofVlan(0);
		}
		Tenant tenant = getTenantfrom2Macs(sw, sourceMac, destMac);

		if (tenant != null) {

			//			Map<MacVlanPair, OFPort> swMap = macVlanToSwitchPortMap.get(sw);
			//			Map<Tenant, Map<MacVlanPair, OFPort>> swMap = switchTenantMap.get(sw).get(tenant);
			Map<MacVlanPair, OFPort> tenantMap = switchTenantMap.get(sw).get(tenant);
			//			Map<MacVlanPair, OFPort> tenantMap2 = macVlanToTenantPortMap.get(tenant);
			//			if (swMap != null) {
			if (tenantMap != null) {
				tenantMap.remove(new MacVlanPair(sourceMac, vlan));
			}
		}

	}

	/**
	 * Get the port that a MAC/VLAN pair is associated with
	 * @param sw The switch to get the mapping from
	 * @param mac The MAC address to get
	 * @param vlan The VLAN number to get
	 * @return The port the host is on
	 */
	public OFPort getFromPortMap(IOFSwitch sw, MacAddress sourceMac, MacAddress destMac, VlanVid vlan) {

		if (vlan == VlanVid.FULL_MASK || vlan == null) {
			vlan = VlanVid.ofVlan(0);
		}

		Tenant tenant = getTenantfrom2Macs(sw, sourceMac, destMac);

		if (tenant != null) {

			Map<MacVlanPair, OFPort> tenantMap = switchTenantMap.get(sw).get(tenant);
			//		Map<MacVlanPair, OFPort> swMap = macVlanToSwitchPortMap.get(sw);

			//		if (swMap != null ) {
			if (tenantMap != null ) {

				//return swMap.get(new MacVlanPair(mac, vlan));
				return tenantMap.get(new MacVlanPair(sourceMac, vlan));
			}}

		// if none found
		return null;
	}

	/**
	 * Clears the MAC/VLAN -> SwitchPort map for all switches
	 */
	public void clearLearnedTable() {
		//		macVlanToSwitchPortMap.clear();
		macVlanToTenantPortMap.clear();
		switchTenantMap.clear();
	}

	/**
	 * Clears the MAC/VLAN -> SwitchPort map for a single switch
	 * @param sw The switch to clear the mapping for
	 */
	public void clearLearnedTable(IOFSwitch sw) {
		//		Map<MacVlanPair, OFPort> swMap = macVlanToSwitchPortMap.get(sw);
		Map<Tenant, Map<MacVlanPair, OFPort>> swMap = switchTenantMap.get(sw);

		if (swMap != null) {

			Iterator<Tenant> iterator = swMap.keySet().iterator();

			while(iterator.hasNext()){	
				Map<MacVlanPair, OFPort> tenantMap = macVlanToTenantPortMap.get(iterator.next());
				tenantMap.clear();
			}

			swMap.clear();
		}
	}

	@Override
	//	public synchronized Map<IOFSwitch, Map<MacVlanPair, OFPort>> getTable() {
	public synchronized Map<IOFSwitch, Map<Tenant, Map<MacVlanPair, OFPort>>> getTable() {
		//		return macVlanToSwitchPortMap;
		return switchTenantMap;
	}

	public Tenant getTenantfrom2Macs (IOFSwitch sw, MacAddress sourceMac, MacAddress destMac){

		boolean sourceMacFromATenant = false;
		boolean destMacFromATenant = false;
		Tenant tenant;
		log.info("getTenantfrom2Macs SourceMAC-{}- seen on switch: DestMAC-{}-", sourceMac.toString(), destMac.toString());
		//Map<Tenant, Map<MacVlanPair, OFPort>> swMap = switchTenantMap.get(sw);

		log.info("getTenantfrom2Macs2 -{}",sw.toString());
		//log.info("getTenantfrom2Macs2.1 -{}",swMap.toString());
		//		if (tenantList != null) {
		if (this.environmentOfTenants.getTenantList() != null) {

			//		Iterator<Tenant> iteratorTenant = tenantList.iterator();
			Iterator<Tenant> iteratorTenant = this.environmentOfTenants.getTenantList().iterator();

			while(iteratorTenant.hasNext()){
				tenant = iteratorTenant.next();

				log.info("getTenantfrom2Macs3 SourceMAC-{}- seen on switch: DestMAC-{}-", tenant.getMacList().contains(sourceMac), tenant.getMacList().contains(destMac));
				log.info("getTenantfrom2Macs3.1 -{}-",tenant.getMacList().toString());
				if (tenant.getMacList().contains(sourceMac)){
					sourceMacFromATenant = true;
				}

				if (tenant.getMacList().contains(destMac)){
					destMacFromATenant = true;					
				}
				if (sourceMacFromATenant && destMacFromATenant){
					log.info("getTenantfrom2Macs4 sourceMacFromATenant && destMacFromATenant");
					return tenant;
				}
				sourceMacFromATenant = false;
				destMacFromATenant = false;
			}			
		}
		return null;
	}


	/**
	 * Writes a OFFlowMod to a switch.
	 * @param sw The switch to write the flowmod to.
	 * @param command The FlowMod actions (add, delete, etc).
	 * @param bufferId The buffer ID if the switch has buffered the packet.
	 * @param match The OFMatch structure to write.
	 * @param outPort The switch port to output it to.
	 */
	private void writeFlowMod(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort) {
		log.info("writeFlowMod11111111111111111111111111111111111111111");
		// from openflow 1.0 spec - need to set these on a struct ofp_flow_mod:
		// struct ofp_flow_mod {
		//    struct ofp_header header;
		//    struct ofp_match match; /* Fields to match */
		//    uint64_t cookie; /* Opaque controller-issued identifier. */
		//
		//    /* Flow actions. */
		//    uint16_t command; /* One of OFPFC_*. */
		//    uint16_t idle_timeout; /* Idle time before discarding (seconds). */
		//    uint16_t hard_timeout; /* Max time before discarding (seconds). */
		//    uint16_t priority; /* Priority level of flow entry. */
		//    uint32_t buffer_id; /* Buffered packet to apply to (or -1).
		//                           Not meaningful for OFPFC_DELETE*. */
		//    uint16_t out_port; /* For OFPFC_DELETE* commands, require
		//                          matching entries to include this as an
		//                          output port. A value of OFPP_NONE
		//                          indicates no restriction. */
		//    uint16_t flags; /* One of OFPFF_*. */
		//    struct ofp_action_header actions[0]; /* The action length is inferred
		//                                            from the length field in the
		//                                            header. */
		//    };

		OFFlowMod.Builder fmb;
		if (command == OFFlowModCommand.DELETE) {
			fmb = sw.getOFFactory().buildFlowDelete();
		} else {
			fmb = sw.getOFFactory().buildFlowAdd();
		}
		fmb.setMatch(match);
		fmb.setCookie((U64.of(LearningSwitchTenant.LEARNING_SWITCH_COOKIE)));
		fmb.setIdleTimeout(LearningSwitchTenant.FLOWMOD_DEFAULT_IDLE_TIMEOUT);
		fmb.setHardTimeout(LearningSwitchTenant.FLOWMOD_DEFAULT_HARD_TIMEOUT);
		fmb.setPriority(LearningSwitchTenant.FLOWMOD_PRIORITY);
		fmb.setBufferId(bufferId);
		fmb.setOutPort((command == OFFlowModCommand.DELETE) ? OFPort.ANY : outPort);
		Set<OFFlowModFlags> sfmf = new HashSet<OFFlowModFlags>();
		if (command != OFFlowModCommand.DELETE) {
			sfmf.add(OFFlowModFlags.SEND_FLOW_REM);
		}
		fmb.setFlags(sfmf);


		// set the ofp_action_header/out actions:
		// from the openflow 1.0 spec: need to set these on a struct ofp_action_output:
		// uint16_t type; /* OFPAT_OUTPUT. */
		// uint16_t len; /* Length is 8. */
		// uint16_t port; /* Output port. */
		// uint16_t max_len; /* Max length to send to controller. */
		// type/len are set because it is OFActionOutput,
		// and port, max_len are arguments to this constructor
		List<OFAction> al = new ArrayList<OFAction>();
		al.add(sw.getOFFactory().actions().buildOutput().setPort(outPort).setMaxLen(0xffFFffFF).build());
		fmb.setActions(al);

		if (log.isTraceEnabled()) {
			log.trace("{} {} flow mod {}",
					new Object[]{ sw, (command == OFFlowModCommand.DELETE) ? "deleting" : "adding", fmb.build() });
		}

		counterFlowMod.increment();

		// and write it out
		sw.write(fmb.build());
	}

	/**
	 * Pushes a packet-out to a switch.  The assumption here is that
	 * the packet-in was also generated from the same switch.  Thus, if the input
	 * port of the packet-in and the outport are the same, the function will not
	 * push the packet-out.
	 * @param sw        switch that generated the packet-in, and from which packet-out is sent
	 * @param match     OFmatch
	 * @param pi        packet-in
	 * @param outport   output port
	 */
	private void pushPacket(IOFSwitch sw, Match match, OFPacketIn pi, OFPort outport) {
		if (pi == null) {
			return;
		}

		OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));

		// The assumption here is (sw) is the switch that generated the
		// packet-in. If the input port is the same as output port, then
		// the packet-out should be ignored.
		if (inPort.equals(outport)) {
			if (log.isDebugEnabled()) {
				log.debug("Attempting to do packet-out to the same " +
						"interface as packet-in. Dropping packet. " +
						" SrcSwitch={}, match = {}, pi={}",
						new Object[]{sw, match, pi});
				return;
			}
		}

		if (log.isTraceEnabled()) {
			log.trace("PacketOut srcSwitch={} match={} pi={}",
					new Object[] {sw, match, pi});
		}

		OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();

		// set actions
		List<OFAction> actions = new ArrayList<OFAction>();
		actions.add(sw.getOFFactory().actions().buildOutput().setPort(outport).setMaxLen(0xffFFffFF).build());

		pob.setActions(actions);

		// If the switch doens't support buffering set the buffer id to be none
		// otherwise it'll be the the buffer id of the PacketIn
		if (sw.getBuffers() == 0) {
			// We set the PI buffer id here so we don't have to check again below
			pi = pi.createBuilder().setBufferId(OFBufferId.NO_BUFFER).build();
			pob.setBufferId(OFBufferId.NO_BUFFER);
		} else {
			pob.setBufferId(pi.getBufferId());
		}

		pob.setInPort(inPort);

		// If the buffer id is none or the switch doesn's support buffering
		// we send the data with the packet out
		if (pi.getBufferId() == OFBufferId.NO_BUFFER) {
			byte[] packetData = pi.getData();
			pob.setData(packetData);
		}

		counterPacketOut.increment();
		sw.write(pob.build());
	}

	/**
	 * Writes an OFPacketOut message to a switch.
	 * @param sw The switch to write the PacketOut to.
	 * @param packetInMessage The corresponding PacketIn.
	 * @param egressPort The switchport to output the PacketOut.
	 */
	private void writePacketOutForPacketIn(IOFSwitch sw, OFPacketIn packetInMessage, OFPort egressPort) {
		OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();

		// Set buffer_id, in_port, actions_len
		pob.setBufferId(packetInMessage.getBufferId());
		pob.setInPort(packetInMessage.getVersion().compareTo(OFVersion.OF_12) < 0 ? packetInMessage.getInPort() : packetInMessage.getMatch().get(MatchField.IN_PORT));

		// set actions
		List<OFAction> actions = new ArrayList<OFAction>(1);
		actions.add(sw.getOFFactory().actions().buildOutput().setPort(egressPort).setMaxLen(0xffFFffFF).build());
		pob.setActions(actions);

		// set data - only if buffer_id == -1
		if (packetInMessage.getBufferId() == OFBufferId.NO_BUFFER) {
			byte[] packetData = packetInMessage.getData();
			pob.setData(packetData);
		}

		// and write it out
		counterPacketOut.increment();
		sw.write(pob.build());

	}

	protected Match createMatchFromPacket(IOFSwitch sw, OFPort inPort, FloodlightContext cntx) {
		// The packet in match will only contain the port number.
		// We need to add in specifics for the hosts we're routing between.
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		VlanVid vlan = VlanVid.ofVlan(eth.getVlanID());
		MacAddress srcMac = eth.getSourceMACAddress();
		MacAddress dstMac = eth.getDestinationMACAddress();

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_SRC, srcMac)
		.setExact(MatchField.ETH_DST, dstMac);

		if (!vlan.equals(VlanVid.ZERO)) {
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
		}

		return mb.build();
	}

	/**
	 * Processes a OFPacketIn message. If the switch has learned the MAC/VLAN to port mapping
	 * for the pair it will write a FlowMod for. If the mapping has not been learned the
	 * we will flood the packet.
	 * @param sw
	 * @param pi
	 * @param cntx
	 * @return
	 */
	private Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx) {
		OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));

		log.info("processPacketInMessage");
		Tenant tenant;
		/* Read packet header attributes into Match */
		Match m = createMatchFromPacket(sw, inPort, cntx);
		MacAddress sourceMac = m.get(MatchField.ETH_SRC);
		MacAddress destMac = m.get(MatchField.ETH_DST);
		VlanVid vlan = m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid();
		log.info("processPacketInMessage2 SM:-{}- DM:-{}", sourceMac,destMac );
		if (sourceMac == null) {
			sourceMac = MacAddress.NONE;
		}
		if (destMac == null) {
			destMac = MacAddress.NONE;
		}
		if (vlan == null) {
			vlan = VlanVid.ZERO;
		}

		if ((destMac.getLong() & 0xfffffffffff0L) == 0x0180c2000000L) {
			if (log.isTraceEnabled()) {
				log.trace("ignoring packet addressed to 802.1D/Q reserved addr: switch {} vlan {} dest MAC {}",
						new Object[]{ sw, vlan, destMac.toString() });
			}
			return Command.STOP;
		}
		if ((sourceMac.getLong() & 0x010000000000L) == 0) {
			// If source MAC is a unicast address, learn the port for this MAC/VLAN
			//Create a function to verify if the sourceMac and the destMac belongs to the same tenant
			//			if (hasTheSameTenant){
			//log.info("processPacketInMessage2");
			//tenant = getTenantfrom2Macs(sw,sourceMac,destMac);
			//if ( tenant instanceof Tenant){
			//	log.info("processPacketInMessage333333333333333333333");
			//	this.addToPortMap(sw, tenant, sourceMac, vlan, inPort);
			//}
			//			}
			this.addToPortMap(sw, sourceMac, vlan, inPort);
		}

		// Now output flow-mod and/or packet
		OFPort outPort = getFromPortMap(sw, destMac, sourceMac, vlan);
		log.info("processPacketInMessage3 SM:-{}- DM:-{}", sourceMac,destMac );
		tenant = getTenantfrom2Macs(sw,sourceMac,destMac);
		log.info("processPacketInMessage4 SM:-{}- DM:-{}", sourceMac,destMac );
		if ( outPort == null ) {
			if ( destMac.toString().equals("ff:ff:ff:ff:ff:ff") ){
//				if (tenant != null) {
				// If we haven't learned the port for the dest MAC/VLAN, flood it
				// Don't flood broadcast packets if the broadcast is disabled.
				// XXX For LearningSwitch this doesn't do much. The sourceMac is removed
				//     from port map whenever a flow expires, so you would still see
				//     a lot of floods.
				//if ( tenant instanceof Tenant){
				log.info("OFPort.FLOODOFPort.FLOODOFPort.FLOODOFPort.FLOODOFPort.FLOOD");
				this.writePacketOutForPacketIn(sw, pi, OFPort.FLOOD);
//			}
			} else if (tenant != null) {
				log.info("OFPort.FLOODOFPort.FLOODOFPort.FLOODOFPort.FLOODOFPort.FLOOD Tenant");
				this.writePacketOutForPacketIn(sw, pi, OFPort.FLOOD);
			}
		} else if (outPort.equals(inPort)) {
			log.trace("ignoring packet that arrived on same port as learned destination:"
					+ " switch {} vlan {} dest MAC {} port {}",
					new Object[]{ sw, vlan, destMac.toString(), outPort.getPortNumber() });
			log.info("outPort.equals(inPort)outPort.equals(inPort)outPort.equals(inPort)outPort.equals(inPort)");
		} else {
			log.info("pushPacketpushPacketpushPacketpushPacketpushPacket");

			if ( tenant instanceof Tenant){
				// Add flow table entry matching source MAC, dest MAC, VLAN and input port
				// that sends to the port we previously learned for the dest MAC/VLAN.  Also
				// add a flow table entry with source and destination MACs reversed, and
				// input and output ports reversed.  When either entry expires due to idle
				// timeout, remove the other one.  This ensures that if a device moves to
				// a different port, a constant stream of packets headed to the device at
				// its former location does not keep the stale entry alive forever.
				// FIXME: current HP switches ignore DL_SRC and DL_DST fields, so we have to match on
				// NW_SRC and NW_DST as well
				// We write FlowMods with Buffer ID none then explicitly PacketOut the buffered packet
				this.pushPacket(sw, m, pi, outPort);
				this.writeFlowMod(sw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, m, outPort);
				if (LEARNING_SWITCH_REVERSE_FLOW) {
					Match.Builder mb = m.createBuilder();
					mb.setExact(MatchField.ETH_SRC, m.get(MatchField.ETH_DST))                         
					.setExact(MatchField.ETH_DST, m.get(MatchField.ETH_SRC))     
					.setExact(MatchField.IN_PORT, outPort);
					if (m.get(MatchField.VLAN_VID) != null) {
						mb.setExact(MatchField.VLAN_VID, m.get(MatchField.VLAN_VID));
					}

					this.writeFlowMod(sw, OFFlowModCommand.ADD, OFBufferId.NO_BUFFER, mb.build(), inPort);
				}
			}}
		return Command.CONTINUE;
	}

	/**
	 * Processes a flow removed message. We will delete the learned MAC/VLAN mapping from
	 * the switch's table.
	 * @param sw The switch that sent the flow removed message.
	 * @param flowRemovedMessage The flow removed message.
	 * @return Whether to continue processing this message or stop.
	 */
	private Command processFlowRemovedMessage(IOFSwitch sw, OFFlowRemoved flowRemovedMessage) {
		
		log.info("processFlowRemovedMessage");
		log.info("processFlowRemovedMessage getIdleTimeout:-{}- getReason:-{}", flowRemovedMessage.getIdleTimeout(),flowRemovedMessage.getReason() );
		
		if (!flowRemovedMessage.getCookie().equals(U64.of(LearningSwitchTenant.LEARNING_SWITCH_COOKIE))) {
			return Command.CONTINUE;
		}
		if (log.isTraceEnabled()) {
			log.trace("{} flow entry removed {}", sw, flowRemovedMessage);
		}
		Match match = flowRemovedMessage.getMatch();
		// When a flow entry expires, it means the device with the matching source
		// MAC address and VLAN either stopped sending packets or moved to a different
		// port.  If the device moved, we can't know where it went until it sends
		// another packet, allowing us to re-learn its port.  Meanwhile we remove
		// it from the macVlanToPortMap to revert to flooding packets to this device.
		this.removeFromPortMap(sw, match.get(MatchField.ETH_SRC), match.get(MatchField.ETH_DST),
				match.get(MatchField.VLAN_VID) == null 
				? VlanVid.ZERO 
						: match.get(MatchField.VLAN_VID).getVlanVid());

		// Also, if packets keep coming from another device (e.g. from ping), the
		// corresponding reverse flow entry will never expire on its own and will
		// send the packets to the wrong port (the matching input port of the
		// expired flow entry), so we must delete the reverse entry explicitly.
		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.ETH_SRC, match.get(MatchField.ETH_DST))                         
		.setExact(MatchField.ETH_DST, match.get(MatchField.ETH_SRC));
		if (match.get(MatchField.VLAN_VID) != null) {
			mb.setExact(MatchField.VLAN_VID, match.get(MatchField.VLAN_VID));                    
		}
		this.writeFlowMod(sw, OFFlowModCommand.DELETE, OFBufferId.NO_BUFFER, mb.build(), match.get(MatchField.IN_PORT));
		return Command.CONTINUE;
	}

	// IOFMessageListener

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		switch (msg.getType()) {
		case PACKET_IN:
			return this.processPacketInMessage(sw, (OFPacketIn) msg, cntx);
		case FLOW_REMOVED:
			return this.processFlowRemovedMessage(sw, (OFFlowRemoved) msg);
		case ERROR:
			log.info("received an error {} from switch {}", msg, sw);
			return Command.CONTINUE;
		default:
			log.error("received an unexpected message {} from switch {}", msg, sw);
			return Command.CONTINUE;
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

	// IFloodlightModule

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(ILearningSwitchServiceTenant.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>,  IFloodlightService> m = 
				new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		m.put(ILearningSwitchServiceTenant.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IDebugCounterService.class);
		l.add(IRestApiService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		log.info("INIT!!!");

		//		macVlanToSwitchPortMap = new ConcurrentHashMap<IOFSwitch, Map<MacVlanPair, OFPort>>();
		macVlanToTenantPortMap = new ConcurrentHashMap<Tenant, Map<MacVlanPair, OFPort>>();
		switchTenantMap = new ConcurrentHashMap<IOFSwitch, Map<Tenant, Map<MacVlanPair, OFPort>>>();

		floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
		debugCounterService = context.getServiceImpl(IDebugCounterService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);

		//		this.macList1.add(MacAddress.of(address1));
		//		this.macList1.add(MacAddress.of(address2));

		//		this.tenant1.setMacList(macList1);

		//		this.macList2.add(MacAddress.of(address3));
		//		this.macList2.add(MacAddress.of(address4));

		//		this.tenant2.setMacList(macList2);

		//		this.tenantList.add(tenant1);
		//		this.tenantList.add(tenant2);

		//		this.serverList.add(server1);
		//		this.serverList.add(server2);
		//		this.serverList.add(server3);

		this.environmentOfTenants.initEnvironmentOfTenants();

		this.environmentOfServices.initEnvironmentOfServers();

	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		floodlightProviderService.addOFMessageListener(OFType.PACKET_IN, this);
		floodlightProviderService.addOFMessageListener(OFType.FLOW_REMOVED, this);
		floodlightProviderService.addOFMessageListener(OFType.ERROR, this);
		restApiService.addRestletRoutable(new LearningSwitchWebRoutableTenant());

		// read our config options
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
		log.debug("FlowMod idle timeout set to {} seconds", FLOWMOD_DEFAULT_IDLE_TIMEOUT);
		log.debug("FlowMod hard timeout set to {} seconds", FLOWMOD_DEFAULT_HARD_TIMEOUT);
		log.debug("FlowMod priority set to {}", FLOWMOD_PRIORITY);

		debugCounterService.registerModule(this.getName());
		counterFlowMod = debugCounterService.registerCounter(this.getName(), "flow-mods-written", "Flow mods written to switches by LearningSwitch", MetaData.WARN);
		counterPacketOut = debugCounterService.registerCounter(this.getName(), "packet-outs-written", "Packet outs written to switches by LearningSwitch", MetaData.WARN);
	}
}
