package net.floodlightcontroller.sirius.nethypervisor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.sirius.providerconfig.EnvironmentOfServices3;
import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.tenantconfig.EnvironmentOfTenants3;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.tenantconfig.HostLocation;
import net.floodlightcontroller.sirius.tenantconfig.Tenant3;
import net.floodlightcontroller.sirius.tenantconfig.TenantVirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualSwitch;
import net.floodlightcontroller.sirius.topology.FlowEntry;
import net.floodlightcontroller.sirius.topology.FlowTable;
import net.floodlightcontroller.sirius.util.Utils;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteFlowModL2OUT extends Thread{
	
	IOFSwitch swSrc;
	IOFSwitch swDst;
	OFFlowModCommand command;
	OFBufferId bufferId;
	Match match;
	OFPort outPort;
	OFPort outPortDst;
	boolean direct;
	Server3 server;
	VirtualSwitch virtualSwitch;
	TenantVirtualNetwork tenantVirtualNetwork;
	HashMap<Long,FlowEntry> cookieFlowEntryMap;
	HashMap<Long,VirtualSwitch> cookieVirtualSwitchMap;
	HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap;
	EnvironmentOfTenants3 environmentOfTenants;
	EnvironmentOfServices3 environmentOfServers;
	HashMap<MacAddress, Host> macAddressHostMap = new HashMap<MacAddress, Host>();
	HashMap<Tenant3, Long> tenantTenantMacIdToVirtualMacsMap = new HashMap<Tenant3, Long>();
	HashMap<Tenant3, Long> tenantCurrentHostIdToVirtualMacsMap = new HashMap<Tenant3, Long>();
	long tenantIdMacCount;
	long hostIdMacCount;
	long virtualMacAddressRaw;
	String str;
	long start;

	protected static Logger log = LoggerFactory.getLogger(WriteFlowMod.class);

	public WriteFlowModL2OUT(IOFSwitch swSrc, IOFSwitch swDst, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, OFPort outPortDst, boolean direct, Server3 server, VirtualSwitch virtualSwitch,
			TenantVirtualNetwork tenantVirtualNetwork, HashMap<Long,FlowEntry> cookieFlowEntryMap, 
			HashMap<Long,VirtualSwitch> cookieVirtualSwitchMap, HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap,
			EnvironmentOfTenants3 environmentOfTenants, EnvironmentOfServices3 environmentOfServers,
			HashMap<MacAddress, Host> macAddressHostMap, HashMap<Tenant3, Long> tenantTenantMacIdToVirtualMacsMap,
			HashMap<Tenant3, Long> tenantCurrentHostIdToVirtualMacsMap,	long tenantIdMacCount,
			long hostIdMacCount,long virtualMacAddressRaw,
			String str) {

		super(str);

		this.swSrc = swSrc;
		this.swDst = swDst;
		this.command = command;
		this.bufferId = bufferId;
		this.match = match;
		this.outPort = outPort;
		this.outPortDst = outPortDst;
		this.direct = direct;
		this.server = server;
		this.tenantVirtualNetwork = tenantVirtualNetwork;
		this.virtualSwitch = virtualSwitch;
		this.cookieFlowEntryMap = cookieFlowEntryMap;
		this.cookieVirtualSwitchMap = cookieVirtualSwitchMap;
		this.cookieVirtualNetworkMap = cookieVirtualNetworkMap;
		this.environmentOfTenants = environmentOfTenants;
		this.environmentOfServers = environmentOfServers;
		this.macAddressHostMap = macAddressHostMap;
		this.tenantTenantMacIdToVirtualMacsMap = tenantTenantMacIdToVirtualMacsMap;
		this.tenantCurrentHostIdToVirtualMacsMap = tenantCurrentHostIdToVirtualMacsMap;
		this.tenantIdMacCount = tenantIdMacCount;
		this.hostIdMacCount = hostIdMacCount;
		this.virtualMacAddressRaw = virtualMacAddressRaw;
		
	}
	
	public WriteFlowModL2OUT(IOFSwitch swSrc, IOFSwitch swDst, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, OFPort outPortDst, boolean direct, Server3 server, VirtualSwitch virtualSwitch,
			TenantVirtualNetwork tenantVirtualNetwork, HashMap<Long,FlowEntry> cookieFlowEntryMap, 
			HashMap<Long,VirtualSwitch> cookieVirtualSwitchMap, HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap,
			EnvironmentOfTenants3 environmentOfTenants, EnvironmentOfServices3 environmentOfServers,
			HashMap<MacAddress, Host> macAddressHostMap, HashMap<Tenant3, Long> tenantTenantMacIdToVirtualMacsMap,
			HashMap<Tenant3, Long> tenantCurrentHostIdToVirtualMacsMap,	long tenantIdMacCount,
			long hostIdMacCount,long virtualMacAddressRaw,
			String str, long start) {

		super(str);

		this.swSrc = swSrc;
		this.swDst = swDst;
		this.command = command;
		this.bufferId = bufferId;
		this.match = match;
		this.outPort = outPort;
		this.outPortDst = outPortDst;
		this.direct = direct;
		this.server = server;
		this.tenantVirtualNetwork = tenantVirtualNetwork;
		this.virtualSwitch = virtualSwitch;
		this.cookieFlowEntryMap = cookieFlowEntryMap;
		this.cookieVirtualSwitchMap = cookieVirtualSwitchMap;
		this.cookieVirtualNetworkMap = cookieVirtualNetworkMap;
		this.environmentOfTenants = environmentOfTenants;
		this.environmentOfServers = environmentOfServers;
		this.macAddressHostMap = macAddressHostMap;
		this.tenantTenantMacIdToVirtualMacsMap = tenantTenantMacIdToVirtualMacsMap;
		this.tenantCurrentHostIdToVirtualMacsMap = tenantCurrentHostIdToVirtualMacsMap;
		this.tenantIdMacCount = tenantIdMacCount;
		this.hostIdMacCount = hostIdMacCount;
		this.virtualMacAddressRaw = virtualMacAddressRaw;
		this.start = start;
		
	}
	
	public static synchronized int getFlowEntryId(ArrayList<FlowEntry> flowtable, Match match, List<OFAction> al){

		for(int i = 0; i< flowtable.size(); i++){

			if(flowtable.get(i) != null &&
					flowtable.get(i).getMatch() != null && 
					flowtable.get(i).getOfActions() != null &&
					flowtable.get(i).getMatch().equals(match)){
				return i;
			}
		}
		return -1;
	}

	public void run(){

		writeFlowModL2OUT(swSrc, swDst, command, bufferId, match, outPort, outPortDst, direct, server, virtualSwitch, 
				tenantVirtualNetwork, cookieFlowEntryMap, cookieVirtualSwitchMap, cookieVirtualNetworkMap, environmentOfTenants, environmentOfServers,
				macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
				hostIdMacCount, virtualMacAddressRaw, start);
	}
	
	private synchronized void writeFlowModL2OUT(IOFSwitch swSrc, IOFSwitch swDst, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, OFPort outPortDst, boolean direct, Server3 server, VirtualSwitch virtualDstSwitch, TenantVirtualNetwork tenantVirtualNetwork,
			HashMap<Long,FlowEntry> cookieFlowEntryMap, HashMap<Long,VirtualSwitch> cookieVirtualSwitchMap, HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap, 
			EnvironmentOfTenants3 environmentOfTenants, EnvironmentOfServices3 environmentOfServers, HashMap<MacAddress, Host> macAddressHostMap, 
			HashMap<Tenant3, Long> tenantTenantMacIdToVirtualMacsMap, HashMap<Tenant3, Long> tenantCurrentHostIdToVirtualMacsMap, 
			long tenantIdMacCount, long hostIdMacCount, long virtualMacAddressRaw, long start){

		List<OFAction> al = null;
		OFFlowMod.Builder fmb = null;
		long cookie = 0L;
		long partialCookie = 0L;
		//		FlowEntryAux flowEntryAux;
		FlowEntry flowEntry = null;
		OFFactory myOF13Factory = OFFactories.getFactory(OFVersion.OF_13);
		OFActions actions = myOF13Factory.actions();
		OFOxms oxms = myOF13Factory.oxms();
		MacAddress virtualSrcMac = null;
		MacAddress virtualDstMac = null;

		if (direct){
			virtualSrcMac = Utils.virtualMacAddress(environmentOfServers.getHostLocationHostMap().get(new HostLocation(swSrc.getId(), match.get(MatchField.IN_PORT))), tenantVirtualNetwork,
					macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap, tenantIdMacCount, hostIdMacCount, virtualMacAddressRaw, environmentOfTenants);
			virtualDstMac = Utils.virtualMacAddress(environmentOfServers.getHostLocationHostMap().get(new HostLocation(swDst.getId(), outPortDst)), tenantVirtualNetwork,
					macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap, tenantIdMacCount, hostIdMacCount, virtualMacAddressRaw, environmentOfTenants);
			partialCookie = Utils.getPartialCookieWriteVirtualAndPhysicalFlows(match.get(MatchField.IN_PORT), server, virtualDstSwitch, environmentOfTenants);
		} else{
			virtualDstMac= Utils.virtualMacAddress(environmentOfServers.getHostLocationHostMap().get(new HostLocation(swSrc.getId(), outPort)), tenantVirtualNetwork,
					macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap, tenantIdMacCount, hostIdMacCount, virtualMacAddressRaw, environmentOfTenants);
			virtualSrcMac = Utils.virtualMacAddress(environmentOfServers.getHostLocationHostMap().get(new HostLocation(swDst.getId(), outPortDst)), tenantVirtualNetwork,
					macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap, tenantIdMacCount, hostIdMacCount, virtualMacAddressRaw, environmentOfTenants);
			partialCookie = Utils.getPartialCookieWriteVirtualAndPhysicalFlows(outPort, server, virtualDstSwitch, environmentOfTenants);
		}
		if (partialCookie != 0L){
			if (command == OFFlowModCommand.DELETE) {
				fmb = swSrc.getOFFactory().buildFlowDelete();
			} else {
				fmb = swSrc.getOFFactory().buildFlowAdd();
			}
			if(direct){
				Match matchPCK = Utils.createMatchFromFieldsDirect(match.get(MatchField.IN_PORT), match.get(MatchField.ETH_DST), match.get(MatchField.ETH_SRC), match.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : match.get(MatchField.VLAN_VID).getVlanVid(), swSrc);
				fmb.setMatch(matchPCK);
			}else{
				Match matchPCK = Utils.createMatchFromFieldsDirect(match.get(MatchField.IN_PORT), virtualSrcMac, virtualDstMac, match.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : match.get(MatchField.VLAN_VID).getVlanVid(), swSrc);
				fmb.setMatch(matchPCK);
			}
			Set<OFFlowModFlags> sfmf = new HashSet<OFFlowModFlags>();
			if (command != OFFlowModCommand.DELETE) {
				sfmf.add(OFFlowModFlags.SEND_FLOW_REM);
			}
			fmb.setFlags(sfmf);
			al = new ArrayList<OFAction>();
			if (direct){
				OFActionSetField setDlSrc = actions.buildSetField()
						.setField(
								oxms.buildEthSrc()
								.setValue(virtualSrcMac)
								.build()
								)
								.build();
				al.add(setDlSrc);
				OFActionSetField setDlDst = actions.buildSetField()
						.setField(
								oxms.buildEthDst()
								.setValue(virtualDstMac)
								.build()
								)
								.build();
				al.add(setDlDst);
			} else{
				OFActionSetField setDlSrc = actions.buildSetField()
						.setField(
								oxms.buildEthSrc()
								.setValue(match.get(MatchField.ETH_DST))
								.build()
								)
								.build();
				al.add(setDlSrc);
				OFActionSetField setDlDst = actions.buildSetField()
						.setField(
								oxms.buildEthDst()
								.setValue(match.get(MatchField.ETH_SRC))
								.build()
								)
								.build();
				al.add(setDlDst);
			}
			al.add(swSrc.getOFFactory().actions().buildOutput().setPort(outPort).setMaxLen(0xffFFffFF).build());
			//flowEntryAux = new FlowEntryAux(partialCookie, SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT, SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT, 
			//SiriusNetHypervisor.FLOWMOD_PRIORITY, match, al);
			//Insert the FlowEntry in the VirtualSwitch and PhysicalSwitch
			//Only table 0 is used in this version
			if(virtualDstSwitch.getFlowTable().size() == 0){
				ArrayList<FlowTable> flowTableArray = new ArrayList<FlowTable>();
				ArrayList<FlowEntry> flowEntryArray = new ArrayList<FlowEntry>();
				FlowTable e = new FlowTable(0, flowEntryArray);
				flowTableArray.add(e);
				virtualDstSwitch.setFlowTable(flowTableArray);
				if(!Utils.hasFlowEntry(virtualDstSwitch.getFlowTable().get(0).getFlowEntry(), match, al)){
					flowEntry = new FlowEntry(partialCookie, SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT, SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT, 
							SiriusNetHypervisor.FLOWMOD_PRIORITY, match, al);
					virtualDstSwitch.getFlowTable().get(0).addFlowEntry(flowEntry);
				}
				//TODO uncomment
//				System.out.println("VIRTUAL FLOW TABLE - "+virtualDstSwitch.getName()+" - "+ virtualDstSwitch.getFlowTable().get(0).toString());
			} else{
				//Only table 0 is used
				if(!Utils.hasFlowEntry(virtualDstSwitch.getFlowTable().get(0).getFlowEntry(), match, al)){
					flowEntry = new FlowEntry(partialCookie, SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT, SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT, 
							SiriusNetHypervisor.FLOWMOD_PRIORITY, match, al);

					virtualDstSwitch.getFlowTable().get(0).addFlowEntry(flowEntry);
				}
				//TODO uncomment
//				System.out.println("VIRTUAL FLOW TABLE - "+virtualDstSwitch.getName()+" - "+ virtualDstSwitch.getFlowTable().get(0).toString());
			}
			if(server.getOpenVSwitch().getFlowTable().size() == 0){
				ArrayList<FlowTable> flowTableArray = new ArrayList<FlowTable>();
				ArrayList<FlowEntry> flowEntryArray = new ArrayList<FlowEntry>();
				FlowTable e = new FlowTable(0, flowEntryArray);
				flowTableArray.add(e);
				server.getOpenVSwitch().setFlowTable(flowTableArray);
				if(!Utils.hasFlowEntry(server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry(), match, al)){
					int indexFlowEntryId = getFlowEntryId(server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry(), match, al);
					if(indexFlowEntryId != -1){
						flowEntry = server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry().get(indexFlowEntryId);
						server.getOpenVSwitch().getFlowTable().get(0).addFlowEntry(flowEntry);
						cookieFlowEntryMap.put(partialCookie + flowEntry.getFlowEntryId(), flowEntry);
						cookieVirtualSwitchMap.put(partialCookie + flowEntry.getFlowEntryId(), virtualDstSwitch);
						cookieVirtualNetworkMap.put(partialCookie + flowEntry.getFlowEntryId(), tenantVirtualNetwork.getVirtualNetwork());
					}
				}
				//TODO uncomment
//				System.out.println("PHYSICAL FLOW TABLE - "+server.getDescription()+" - "+server.getOpenVSwitch().getFlowTable().get(0).toString());
			}else{
				if(!Utils.hasFlowEntry(server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry(), match, al)){
					int indexFlowEntryId = getFlowEntryId(server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry(), match, al);
					if(indexFlowEntryId != -1){
						flowEntry = server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry().get(indexFlowEntryId);
						server.getOpenVSwitch().getFlowTable().get(0).addFlowEntry(flowEntry);
						cookieFlowEntryMap.put(partialCookie + flowEntry.getFlowEntryId(), flowEntry);
						cookieVirtualSwitchMap.put(partialCookie + flowEntry.getFlowEntryId(), virtualDstSwitch);
						cookieVirtualNetworkMap.put(partialCookie + flowEntry.getFlowEntryId(), tenantVirtualNetwork.getVirtualNetwork());
					}
				}
				//TODO uncomment
//				System.out.println("PHYSICAL FLOW TABLE - "+server.getDescription()+" - "+server.getOpenVSwitch().getFlowTable().get(0).toString());
			}
			if(flowEntry != null){
				cookie =  partialCookie + flowEntry.getFlowEntryId();
				fmb.setCookie((U64.of(cookie)));
				fmb.setIdleTimeout(SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT);
				fmb.setHardTimeout(SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT);
				fmb.setPriority(SiriusNetHypervisor.FLOWMOD_PRIORITY);
				fmb.setBufferId(bufferId);
				fmb.setOutPort((command == OFFlowModCommand.DELETE) ? OFPort.ANY : outPort);
				fmb.setActions(al);
				if (log.isTraceEnabled()) {
					log.trace("{} {} flow mod {}",
							new Object[]{ swSrc, (command == OFFlowModCommand.DELETE) ? "deleting" : "adding", fmb.build() });
				}			
//				if(start >0){
//					long end = System.currentTimeMillis();
//
//					System.out.println("Time to insert flows : " + ((end - start)));
//				}
				boolean write = swSrc.write(fmb.build());
			}
		}
	}
}
