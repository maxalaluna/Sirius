package net.floodlightcontroller.sirius.arphandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.debugcounter.IDebugCounter;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.debugcounter.IDebugCounterService.MetaData;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPAddress;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArpHandle implements IOFMessageListener, IFloodlightModule{

	protected IFloodlightProviderService floodlightProvider;
	protected static Logger log;
	private IDebugCounter counterPacketOut;
	protected IDebugCounterService debugCounterService;

	@Override
	public String getName() {
		return ArpHandle.class.getSimpleName();
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
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		log = LoggerFactory.getLogger(ArpHandle.class);
		debugCounterService = context.getServiceImpl(IDebugCounterService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		debugCounterService.registerModule(this.getName());
		counterPacketOut = debugCounterService.registerCounter(this.getName(), "packet-outs-written", "Packet outs written to switches by ArpHandle", MetaData.WARN);

	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		switch (msg.getType()) {
		case PACKET_IN:
			return this.processPacketInMessage(sw, (OFPacketIn) msg, cntx);
		case FLOW_REMOVED:
			log.info("received an error {} from switch {}", msg, sw);
			return Command.CONTINUE;
		case ERROR:
			log.info("received an error {} from switch {}", msg, sw);
			return Command.CONTINUE;
		default:
			log.error("received an unexpected message {} from switch {}", msg, sw);
			return Command.CONTINUE;
		}

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
		.setExact(MatchField.ETH_DST, dstMac)
		;

		if (!vlan.equals(VlanVid.ZERO)) {
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
		}

		return mb.build();
	}

	private net.floodlightcontroller.core.IListener.Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx) {

		log.info("processPacketInMessage");
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));
		Match m = createMatchFromPacket(sw, inPort, cntx);
		MacAddress sourceMac = m.get(MatchField.ETH_SRC);
		MacAddress destMac = m.get(MatchField.ETH_DST);
		VlanVid vlan = m.get(MatchField.VLAN_VID) == null ? VlanVid.ZERO : m.get(MatchField.VLAN_VID).getVlanVid();
		IPAddress<IPv4Address> srcIp;
		IPAddress<IPv4Address> dstIp;
		ArpOpcode arpCode = null;
		OFPort port = OFPort.of(0);
		log.info("processPacketInMessage  -{}- -{}-",eth.getSourceMACAddress(), eth.getDestinationMACAddress());
		log.info("processPacketInMessage  -{}- -{}-",eth.getPayload().toString(), eth.getEtherType());

		if (eth.getEtherType() == EthType.ARP){
			arpCode = ((ARP)eth.getPayload()).getOpCode();
			srcIp = (((ARP) eth.getPayload()).getSenderProtocolAddress());
			dstIp = (((ARP) eth.getPayload()).getTargetProtocolAddress());
			log.info("processPacketInMessage  -{}- -{}-",srcIp.toString(), dstIp.toString());
			log.info("if (eth.getEtherType() == EthType.ARP)");

			//Used for Mininet tests
			if(dstIp.equals(IPv4Address.of("10.0.0.2"))){
				port = OFPort.of(2);
			}
			if(dstIp.equals(IPv4Address.of("10.0.0.1"))){
				port = OFPort.of(1);
			}

			if(arpCode == ARP.OP_REQUEST && destMac.isBroadcast() ){
				log.info("ARP.OP_REQUEST && FLOOD!!!");
				this.writePacketOutForPacketIn(sw, pi, port);
			}
			if(arpCode == ARP.OP_REPLY ){

				log.info("ARP.OP_REPLY!!!");
				this.writePacketOutForPacketIn(sw, pi, port);
			}
		}
		log.info("arpCode  -{}- -{}-",arpCode, EthType.ARP);
		log.info("eth  -{}- -{}-",arpCode, EthType.ARP);

		if (sourceMac == null) {
			sourceMac = MacAddress.NONE;
		}
		if (destMac == null) {
			destMac = MacAddress.NONE;
		}
		if (vlan == null) {
			vlan = VlanVid.ZERO;
		}
		return Command.CONTINUE;
	}

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

}
