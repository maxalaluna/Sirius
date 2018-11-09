package net.floodlightcontroller.sirius.nethypervisor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.tenantconfig.EnvironmentOfTenants3;
import net.floodlightcontroller.sirius.tenantconfig.TenantVirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualSwitch;
import net.floodlightcontroller.sirius.topology.FlowEntry;
import net.floodlightcontroller.sirius.topology.FlowTable;
import net.floodlightcontroller.sirius.util.Utils;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteFlowModSameSwitch extends Thread{

	IOFSwitch sw;
	OFFlowModCommand command;
	OFBufferId bufferId;
	Match match;
	OFPort outPort;
	Server3 server;
	VirtualSwitch virtualSwitch;
	TenantVirtualNetwork tenantVirtualNetwork;
	HashMap<Long,FlowEntry> cookieFlowEntryMap;
	HashMap<Long,VirtualSwitch> cookieVirtualSwitchMap;
	HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap;
	EnvironmentOfTenants3 environmentOfTenants;
	String str;
	long start;
	protected static Logger log = LoggerFactory.getLogger(WriteFlowMod.class);

	public WriteFlowModSameSwitch(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, Server3 server, VirtualSwitch virtualSwitch,
			TenantVirtualNetwork tenantVirtualNetwork, HashMap<Long,FlowEntry> cookieFlowEntryMap, 
			HashMap<Long,VirtualSwitch> cookieVirtualSwitchMap, HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap,
			EnvironmentOfTenants3 environmentOfTenants,
			String str) {

		super(str);
		this.sw = sw;
		this.command = command;
		this.bufferId = bufferId;
		this.match = match;
		this.outPort = outPort;
		this.server = server;
		this.tenantVirtualNetwork = tenantVirtualNetwork;
		this.virtualSwitch = virtualSwitch;
		this.cookieFlowEntryMap = cookieFlowEntryMap;
		this.cookieVirtualSwitchMap = cookieVirtualSwitchMap;
		this.cookieVirtualNetworkMap = cookieVirtualNetworkMap;
		this.environmentOfTenants = environmentOfTenants;
	}
	
	public WriteFlowModSameSwitch(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, Server3 server, VirtualSwitch virtualSwitch,
			TenantVirtualNetwork tenantVirtualNetwork, HashMap<Long,FlowEntry> cookieFlowEntryMap, 
			HashMap<Long,VirtualSwitch> cookieVirtualSwitchMap, HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap,
			EnvironmentOfTenants3 environmentOfTenants,
			String str, long start) {
		
		super(str);
		this.sw = sw;
		this.command = command;
		this.bufferId = bufferId;
		this.match = match;
		this.outPort = outPort;
		this.server = server;
		this.tenantVirtualNetwork = tenantVirtualNetwork;
		this.virtualSwitch = virtualSwitch;
		this.cookieFlowEntryMap = cookieFlowEntryMap;
		this.cookieVirtualSwitchMap = cookieVirtualSwitchMap;
		this.cookieVirtualNetworkMap = cookieVirtualNetworkMap;
		this.environmentOfTenants = environmentOfTenants;
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
		
	writeFlowModSameSwitch(sw, command, bufferId, match, outPort, server, virtualSwitch, 
			tenantVirtualNetwork, cookieFlowEntryMap, cookieVirtualSwitchMap, cookieVirtualNetworkMap, 
			environmentOfTenants, start);
	}

private synchronized void writeFlowModSameSwitch(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
		Match match, OFPort outPort, Server3 server, VirtualSwitch virtualSwitch, TenantVirtualNetwork tenantVirtualNetwork, 
		HashMap<Long, FlowEntry> cookieFlowEntryMap, HashMap<Long, VirtualSwitch> cookieVirtualSwitchMap, HashMap<Long, 
		VirtualNetwork> cookieVirtualNetworkMap, EnvironmentOfTenants3 environmentOfTenants, long start) {

	List<OFAction> al = null;
	OFFlowMod.Builder fmb = null;
	long cookie = 0L;
	//FlowEntryAux flowEntryAux;
	FlowEntry flowEntry = null;

	if (command == OFFlowModCommand.DELETE) {
		fmb = sw.getOFFactory().buildFlowDelete();
		al = new ArrayList<OFAction>();
		al.add(sw.getOFFactory().actions().buildOutput().setPort(outPort).setMaxLen(0xffFFffFF).build());
		fmb.setOutPort((command == OFFlowModCommand.DELETE) ? OFPort.ANY : outPort);
		Set<OFFlowModFlags> sfmf = new HashSet<OFFlowModFlags>();
		fmb.setFlags(sfmf);
		fmb.setActions(al);
		sw.write(fmb.build());

	} else {


		long partialCookie = Utils.getPartialCookieWriteVirtualAndPhysicalFlows(match.get(MatchField.IN_PORT), server, virtualSwitch, environmentOfTenants);
		if (partialCookie != 0L){
			al = new ArrayList<OFAction>();
			al.add(sw.getOFFactory().actions().buildOutput().setPort(outPort).setMaxLen(0xffFFffFF).build());
			//flowEntryAux = new FlowEntryAux(partialCookie, SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT, SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT, 
			//SiriusNetHypervisor.FLOWMOD_PRIORITY, match, al);
			//Insert the FlowEntry in the VirtualSwitch and PhysicalSwitch
			//Only table 0 is used in this version
			if(virtualSwitch.getFlowTable().size() == 0){
				ArrayList<FlowTable> flowTableArray = new ArrayList<FlowTable>();
				ArrayList<FlowEntry> flowEntryArray = new ArrayList<FlowEntry>();
				FlowTable e = new FlowTable(0, flowEntryArray);
				flowTableArray.add(e);
				virtualSwitch.setFlowTable(flowTableArray);
				if(!Utils.hasFlowEntry(virtualSwitch.getFlowTable().get(0).getFlowEntry(), match, al)){
					flowEntry = new FlowEntry(partialCookie, SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT, SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT, 
							SiriusNetHypervisor.FLOWMOD_PRIORITY, match, al);

					virtualSwitch.getFlowTable().get(0).addFlowEntry(flowEntry);
				}
				//TODO uncomment
//				System.out.println("VIRTUAL FLOW TABLE - "+virtualSwitch.getName()+" - "+ virtualSwitch.getFlowTable().get(0).toString());
			} else{
				//Only table 0 is used
				if(!Utils.hasFlowEntry(virtualSwitch.getFlowTable().get(0).getFlowEntry(), match, al)){
					flowEntry = new FlowEntry(partialCookie, SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT, SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT, 
							SiriusNetHypervisor.FLOWMOD_PRIORITY, match, al);
					virtualSwitch.getFlowTable().get(0).addFlowEntry(flowEntry);
				}
				//TODO uncomment
//				System.out.println("VIRTUAL FLOW TABLE - "+virtualSwitch.getName()+" - "+ virtualSwitch.getFlowTable().get(0).toString());
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
					cookieVirtualSwitchMap.put(partialCookie + flowEntry.getFlowEntryId(), virtualSwitch);
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
						cookieVirtualSwitchMap.put(partialCookie + flowEntry.getFlowEntryId(), virtualSwitch);
						cookieVirtualNetworkMap.put(partialCookie + flowEntry.getFlowEntryId(), tenantVirtualNetwork.getVirtualNetwork());
					}
				}
				//TODO uncomment
//				System.out.println("PHYSICAL FLOW TABLE - "+server.getDescription()+" - "+server.getOpenVSwitch().getFlowTable().get(0).toString());
			}
			if(flowEntry != null){
				cookie =  partialCookie + flowEntry.getFlowEntryId();
				fmb = sw.getOFFactory().buildFlowAdd();
				fmb.setMatch(match);
				fmb.setCookie((U64.of(cookie)));
				fmb.setIdleTimeout(SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT);
				fmb.setHardTimeout(SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT);
				fmb.setPriority(SiriusNetHypervisor.FLOWMOD_PRIORITY);
				fmb.setBufferId(bufferId);
				fmb.setOutPort((command == OFFlowModCommand.DELETE) ? OFPort.ANY : outPort);
				Set<OFFlowModFlags> sfmf = new HashSet<OFFlowModFlags>();
				if (command != OFFlowModCommand.DELETE) {
					sfmf.add(OFFlowModFlags.SEND_FLOW_REM);
				}
				fmb.setFlags(sfmf);
				fmb.setActions(al);
				if (log.isTraceEnabled()) {
					log.trace("{} {} flow mod {}",
							new Object[]{ sw, (command == OFFlowModCommand.DELETE) ? "deleting" : "adding", fmb.build() });
				}				
//				if(start >0){
//					long end = System.currentTimeMillis();
//
//					System.out.println("Time to insert flows : " + ((end - start)));
//				}
				boolean write = sw.write(fmb.build());
			}
		}
	}
}
}