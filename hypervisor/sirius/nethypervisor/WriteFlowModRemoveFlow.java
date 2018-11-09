package net.floodlightcontroller.sirius.nethypervisor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.floodlightcontroller.core.IOFSwitch;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFFlowModFlags;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteFlowModRemoveFlow extends Thread{

	IOFSwitch sw;
	OFFlowModCommand command;
	OFBufferId bufferId;
	Match match;
	OFPort outPort;
	String str;
	protected static Logger log = LoggerFactory.getLogger(WriteFlowModRemoveFlow.class);

	public WriteFlowModRemoveFlow(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort, String str) {

		super(str);
		this.sw = sw;
		this.command = command;
		this.bufferId = bufferId;
		this.match = match;
		this.outPort = outPort;
	}

	public void run(){
		writeFlowModRemoveFlow(sw, command, bufferId, match, outPort);
	}

	private synchronized void writeFlowModRemoveFlow(IOFSwitch sw, OFFlowModCommand command, OFBufferId bufferId,
			Match match, OFPort outPort) {

		List<OFAction> al = null;
		OFFlowMod.Builder fmb = null;
		if (command == OFFlowModCommand.DELETE) {
			fmb = sw.getOFFactory().buildFlowDelete();
			al = new ArrayList<OFAction>();
			al.add(sw.getOFFactory().actions().buildOutput().setPort(outPort).setMaxLen(0xffFFffFF).build());
			fmb.setOutPort((command == OFFlowModCommand.DELETE) ? OFPort.ANY : outPort);
			Set<OFFlowModFlags> sfmf = new HashSet<OFFlowModFlags>();
			fmb.setMatch(match);
			fmb.setFlags(sfmf);
			fmb.setActions(al);
			sw.write(fmb.build());
		} 
	}
}
