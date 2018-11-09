package net.floodlightcontroller.sirius.topology;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;

public class FlowEntry {

	private long cookie;
	private int idleTimeOut;
	private int hardTimeOut;
	private int priority;
	private Match match;
	private List<OFAction> ofActions;
	//Considers the limit of entries 2^20 flowEntryIds in the switch because of the cookie separation
	private int flowEntryId;
	private static final AtomicInteger count = new AtomicInteger(0);


	public FlowEntry(long partialCookie, int idleTimeOut, int hardTimeOut,
			int priority, Match match, List<OFAction> ofActions) {
		super();
		
		this.idleTimeOut = idleTimeOut;
		this.hardTimeOut = hardTimeOut;
		this.priority = priority;
		this.match = match;
		this.ofActions = ofActions;
		this.flowEntryId = (count.incrementAndGet());
		this.cookie = partialCookie + this.getFlowEntryId();
	}

	public long getCookie() {
		return cookie;
	}

	public void setCookie(long cookie) {
		this.cookie = cookie;
	}

	public int getIdleTimeOut() {
		return idleTimeOut;
	}

	public void setIdleTimeOut(int idleTimeOut) {
		this.idleTimeOut = idleTimeOut;
	}

	public int getHardTimeOut() {
		return hardTimeOut;
	}

	public void setHardTimeOut(int hardTimeOut) {
		this.hardTimeOut = hardTimeOut;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Match getMatch() {
		return match;
	}

	public void setMatch(Match match) {
		this.match = match;
	}

	public List<OFAction> getOfActions() {
		return ofActions;
	}

	public void setOfActions(List<OFAction> ofActions) {
		this.ofActions = ofActions;
	}

	public synchronized int getFlowEntryId() {
		return flowEntryId;
	}

	public synchronized void setFlowEntryId(int flowEntryId) {
		this.flowEntryId = flowEntryId;
	}
	
	public String toString(){
		
		String info = "\n";
		String id = this.getFlowEntryId() +"";
		String cookie = this.getCookie() +"";
		String hardTimeout = this.getHardTimeOut() +"";
		String idleTimeOut = this.getIdleTimeOut() +"";
		String priority = this.getPriority() +"";
		String match = this.getMatch().toString();
		String action = this.getOfActions().toString();
		
		info = info.concat("Id: "+id+"; cookie: "+cookie+"; hardTimeout: "+hardTimeout+"; idleTimeOut:"+idleTimeOut+"; priority:"+priority+"; match:"+match+"; action: "+action+"\n");
	
	return info;
	}

	@Override
	public int hashCode() {

		return  (this.match.hashCode() ^ (this.ofActions==null?1:this.ofActions.hashCode()));
	}

	@Override
	public boolean equals(Object o) {
		
		return ((o instanceof FlowEntry) && 
				(match.equals(((FlowEntry) o).match)) &&
				(ofActions==null?true:(((FlowEntry) o).ofActions==null?true:(ofActions.equals(((FlowEntry) o).ofActions))))
				);
	}
}
