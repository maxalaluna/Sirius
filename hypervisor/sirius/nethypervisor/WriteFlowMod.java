package net.floodlightcontroller.sirius.nethypervisor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.tenantconfig.TenantVirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;
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

public class WriteFlowMod extends Thread{
	
	IOFSwitch sw;
	OFFlowModCommand command;
	OFBufferId bufferId;
	Match match;
	OFPort outPort;
	Server3 server;
	TenantVirtualNetwork tenantVirtualNetwork;
	HashMap<Long,FlowEntry> cookieFlowEntryMap;
	HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap;
	String str;
	long start;

	protected static Logger log = LoggerFactory.getLogger(WriteFlowMod.class);

	
	public WriteFlowMod(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, Server3 server, TenantVirtualNetwork tenantVirtualNetwork, 
			HashMap<Long,FlowEntry> cookieFlowEntryMap, HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap,
			String str) {
		
		super(str);
		
		this.sw = sw;
		this.command = command;
		this.bufferId = bufferId;
		this.match = match;
		this.outPort = outPort;
		this.server = server;
		this.tenantVirtualNetwork = tenantVirtualNetwork;
		this.cookieFlowEntryMap = cookieFlowEntryMap;
		this.cookieVirtualNetworkMap = cookieVirtualNetworkMap;
				
    }
	
	public WriteFlowMod(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, Server3 server, TenantVirtualNetwork tenantVirtualNetwork, 
			HashMap<Long,FlowEntry> cookieFlowEntryMap, HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap,
			String str, long start) {
		
		super(str);
		
		this.sw = sw;
		this.command = command;
		this.bufferId = bufferId;
		this.match = match;
		this.outPort = outPort;
		this.server = server;
		this.tenantVirtualNetwork = tenantVirtualNetwork;
		this.cookieFlowEntryMap = cookieFlowEntryMap;
		this.cookieVirtualNetworkMap = cookieVirtualNetworkMap;
		this.start = start;
				
    }
	
	public void run(){
		
		writeFlowMod(sw, command, bufferId, match, outPort, server, tenantVirtualNetwork, 
				cookieFlowEntryMap, cookieVirtualNetworkMap);
	}

	private synchronized void writeFlowMod(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, Server3 server, TenantVirtualNetwork tenantVirtualNetwork, 
			HashMap<Long,FlowEntry> cookieFlowEntryMap, HashMap<Long,VirtualNetwork> cookieVirtualNetworkMap) {

		List<OFAction> al = null;
		OFFlowMod.Builder fmb = null;
		long cookie = 0L;
//		FlowEntryAux flowEntryAux;
		FlowEntry flowEntry = null;

		fmb = sw.getOFFactory().buildFlowAdd();
		long partialCookie = Utils.getPartialCookieWritePhysicalFlows(match.get(MatchField.IN_PORT), server, tenantVirtualNetwork);
		if (partialCookie != 0L){

			al = new ArrayList<OFAction>();
			al.add(sw.getOFFactory().actions().buildOutput().setPort(outPort).setMaxLen(0xffFFffFF).build());
//			flowEntryAux = new FlowEntryAux(partialCookie, SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT, SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT, 
//			SiriusNetHypervisor.FLOWMOD_PRIORITY, match, al);

			if(server.getOpenVSwitch().getFlowTable().size() == 0){
				ArrayList<FlowTable> flowTableArray = new ArrayList<FlowTable>();
				ArrayList<FlowEntry> flowEntryArray = new ArrayList<FlowEntry>();
				FlowTable e = new FlowTable(0, flowEntryArray);
				flowTableArray.add(e);
				server.getOpenVSwitch().setFlowTable(flowTableArray);

				//if(!server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry().contains((flowEntryAux))){
				if(!Utils.hasFlowEntry(server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry(), match, al)){
					flowEntry = new FlowEntry(partialCookie, SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT, SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT, 
							SiriusNetHypervisor.FLOWMOD_PRIORITY, match, al);
					server.getOpenVSwitch().getFlowTable().get(0).addFlowEntry(flowEntry);
					cookieFlowEntryMap.put(partialCookie + flowEntry.getFlowEntryId(), flowEntry);
					cookieVirtualNetworkMap.put(partialCookie + flowEntry.getFlowEntryId(), tenantVirtualNetwork.getVirtualNetwork());
				}
				//TODO uncomment
//				System.out.println("PHYSICAL FLOW TABLE - "+server.getDescription()+" - "+server.getOpenVSwitch().getFlowTable().get(0).toString());
			}else{
				//if(!server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry().contains((flowEntryAux))){
				if(!Utils.hasFlowEntry(server.getOpenVSwitch().getFlowTable().get(0).getFlowEntry(), match, al)){
					flowEntry = new FlowEntry(partialCookie, SiriusNetHypervisor.FLOWMOD_DEFAULT_IDLE_TIMEOUT, SiriusNetHypervisor.FLOWMOD_DEFAULT_HARD_TIMEOUT, 
							SiriusNetHypervisor.FLOWMOD_PRIORITY, match, al);
					server.getOpenVSwitch().getFlowTable().get(0).addFlowEntry(flowEntry);
					cookieFlowEntryMap.put(partialCookie + flowEntry.getFlowEntryId(), flowEntry);
					cookieVirtualNetworkMap.put(partialCookie + flowEntry.getFlowEntryId(), tenantVirtualNetwork.getVirtualNetwork());
				}
				//TODO uncomment
//				System.out.println("PHYSICAL FLOW TABLE - "+server.getDescription()+" - "+server.getOpenVSwitch().getFlowTable().get(0).toString());
			}

			if(flowEntry != null){

				cookie =  partialCookie + flowEntry.getFlowEntryId();
				//Print to verify if both cookies are equals
//				log.info("cookie =  partialCookie + flowEntry.getFlowEntryId(); -{}-", cookie);
//				log.info("flowEntry.getCookie() -{}-", flowEntry.getCookie());
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
//				sw.write(fmb.build());
				if(start >0){
					long end = System.currentTimeMillis();
					System.out.println("Time to insert flows : " + ((end - start)));
				}
				boolean write = sw.write(fmb.build());
			}
		}
	}
}
