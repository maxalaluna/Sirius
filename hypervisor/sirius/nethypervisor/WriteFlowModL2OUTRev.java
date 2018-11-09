package net.floodlightcontroller.sirius.nethypervisor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.sirius.providerconfig.EnvironmentOfServices3;
import net.floodlightcontroller.sirius.tenantconfig.EnvironmentOfTenants3;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.tenantconfig.HostLocation;
import net.floodlightcontroller.sirius.tenantconfig.Tenant3;
import net.floodlightcontroller.sirius.tenantconfig.TenantVirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualSwitch;
import net.floodlightcontroller.sirius.topology.FlowEntry;
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

public class WriteFlowModL2OUTRev extends Thread{
	
	IOFSwitch swSrc;
	IOFSwitch swDst;
	OFFlowModCommand command;
	OFBufferId bufferId;
	Match match;
	OFPort outPort;
	OFPort outPortDst;
	boolean direct;
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

	public WriteFlowModL2OUTRev(IOFSwitch swSrc, IOFSwitch swDst, OFFlowModCommand command, 
			OFBufferId bufferId, Match match, OFPort outPort, OFPort outPortDst, boolean direct, 
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
		this.tenantVirtualNetwork = tenantVirtualNetwork;
		this.tenantVirtualNetwork = tenantVirtualNetwork;
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
	
	public WriteFlowModL2OUTRev(IOFSwitch swSrc, IOFSwitch swDst, OFFlowModCommand command, 
			OFBufferId bufferId, Match match, OFPort outPort, OFPort outPortDst, boolean direct, 
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
		this.tenantVirtualNetwork = tenantVirtualNetwork;
		this.tenantVirtualNetwork = tenantVirtualNetwork;
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

	public void run(){

		writeFlowModL2OUTRev(swSrc, swDst, command, bufferId, match, outPort, outPortDst, direct, tenantVirtualNetwork, 
				cookieFlowEntryMap, cookieVirtualSwitchMap, cookieVirtualNetworkMap, environmentOfTenants, environmentOfServers,
				macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap,	tenantIdMacCount,
				hostIdMacCount, virtualMacAddressRaw, start);
	}

	private synchronized void writeFlowModL2OUTRev(IOFSwitch swSrc, IOFSwitch swDst, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, OFPort outPortDst, boolean direct, TenantVirtualNetwork tenantVirtualNetwork,
			HashMap<Long,FlowEntry> cookieFlowEntryMap, HashMap<Long,VirtualSwitch> cookieVirtualSwitchMap, HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap, 
			EnvironmentOfTenants3 environmentOfTenants, EnvironmentOfServices3 environmentOfServers, HashMap<MacAddress, Host> macAddressHostMap, 
			HashMap<Tenant3, Long> tenantTenantMacIdToVirtualMacsMap, HashMap<Tenant3, Long> tenantCurrentHostIdToVirtualMacsMap, 
			long tenantIdMacCount, long hostIdMacCount, long virtualMacAddressRaw, long start){

		OFFlowMod.Builder fmb;
		OFFactory myOF13Factory = OFFactories.getFactory(OFVersion.OF_13);
		OFActions actions = myOF13Factory.actions();
		OFOxms oxms = myOF13Factory.oxms();
		MacAddress virtualSrcMac = null;
		MacAddress virtualDstMac = null;
		if (direct){
			virtualDstMac= Utils.virtualMacAddress(environmentOfServers.getHostLocationHostMap().get(new HostLocation(swSrc.getId(), outPortDst)), tenantVirtualNetwork,
					macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap, tenantIdMacCount, hostIdMacCount, virtualMacAddressRaw, environmentOfTenants);
			virtualSrcMac = Utils.virtualMacAddress(environmentOfServers.getHostLocationHostMap().get(new HostLocation(swDst.getId(), outPort)), tenantVirtualNetwork,
					macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap, tenantIdMacCount, hostIdMacCount, virtualMacAddressRaw, environmentOfTenants);

		} else{
			virtualSrcMac = Utils.virtualMacAddress(environmentOfServers.getHostLocationHostMap().get(new HostLocation(swSrc.getId(), outPortDst)), tenantVirtualNetwork,
					macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap, tenantIdMacCount, hostIdMacCount, virtualMacAddressRaw, environmentOfTenants);
			virtualDstMac = Utils.virtualMacAddress(environmentOfServers.getHostLocationHostMap().get(new HostLocation(swDst.getId(), match.get(MatchField.IN_PORT))), tenantVirtualNetwork,
					macAddressHostMap, tenantTenantMacIdToVirtualMacsMap, tenantCurrentHostIdToVirtualMacsMap, tenantIdMacCount, hostIdMacCount, virtualMacAddressRaw, environmentOfTenants);
		}
		if (command == OFFlowModCommand.DELETE) {
			fmb = swSrc.getOFFactory().buildFlowDelete();
		} else {
			fmb = swSrc.getOFFactory().buildFlowAdd();
		}
		if(direct){
			Match matchPCK = Utils.createMatchFromFieldsDirect(outPort, match.get(MatchField.ETH_DST), match.get(MatchField.ETH_SRC), match.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : match.get(MatchField.VLAN_VID).getVlanVid(), swSrc);
			fmb.setMatch(matchPCK);
		}else{
			Match matchPCK = Utils.createMatchFromFieldsDirect(outPort, virtualSrcMac, virtualDstMac, match.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : match.get(MatchField.VLAN_VID).getVlanVid(), swSrc);
			fmb.setMatch(matchPCK);
		}
		fmb.setCookie((U64.of(SiriusNetHypervisor.LEARNING_SWITCH_COOKIE)));
		fmb.setIdleTimeout(SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT);
		fmb.setHardTimeout(SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT);
		fmb.setPriority(SiriusNetHypervisor.FLOWMOD_PRIORITY);
		fmb.setBufferId(bufferId);
		fmb.setOutPort((command == OFFlowModCommand.DELETE) ? OFPort.ANY : outPort);
		fmb.setOutPort((command == OFFlowModCommand.DELETE) ? OFPort.ANY : outPort);
		Set<OFFlowModFlags> sfmf = new HashSet<OFFlowModFlags>();
		if (command != OFFlowModCommand.DELETE) {
			sfmf.add(OFFlowModFlags.SEND_FLOW_REM);
		}
		fmb.setFlags(sfmf);
		List<OFAction> al = new ArrayList<OFAction>();
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
		if(direct){
			al.add(swSrc.getOFFactory().actions().buildOutput().setPort(match.get(MatchField.IN_PORT)).setMaxLen(0xffFFffFF).build());
		}else{

			al.add(swSrc.getOFFactory().actions().buildOutput().setPort(match.get(MatchField.IN_PORT)).setMaxLen(0xffFFffFF).build());
		}
		fmb.setActions(al);
		if (log.isTraceEnabled()) {
			log.trace("{} {} flow mod {}",
					new Object[]{ swSrc, (command == OFFlowModCommand.DELETE) ? "deleting" : "adding", fmb.build() });
		}
		swDst.write(fmb.build());
//		if(start >0){
//			long end = System.currentTimeMillis();
//
//			System.out.println("Time to insert flows : " + ((end - start)));
//		}
	}
}
