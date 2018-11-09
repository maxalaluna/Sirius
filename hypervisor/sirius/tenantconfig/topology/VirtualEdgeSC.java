package net.floodlightcontroller.sirius.tenantconfig.topology;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.floodlightcontroller.sirius.providerconfig.topology.EdgeSC;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.util.Enum.SecurityLevelLinks;

public class VirtualEdgeSC {

	private int virtualEdgeSCId = 0; 
	private static final AtomicInteger count = new AtomicInteger(0);
	private Host virtualHost;
	private VirtualSwitch sourceSwitch;
	private VirtualSwitch destinationSwitch;
	private int weight;
	private int bandwidht;
	private int latency;
	private ArrayList<EdgeSC> linkedListEdgeSCWork = new ArrayList<EdgeSC>();
	private ArrayList<EdgeSC> linkedListEdgeSCBkp = new ArrayList<EdgeSC>();
	private SecurityLevelLinks securityLevel;
	private boolean betweenSwitches;
	private String workingPath;
	private String workingPathLinks;
	private String backupPath;


	public VirtualEdgeSC(String id, VirtualSwitch sourceSwitch,
			VirtualSwitch destinationSwitch, int weight, int bandwidht,
			int latency, SecurityLevelLinks securityLevel, boolean betweenSwitches) {
		super();
		this.virtualEdgeSCId = count.incrementAndGet();
		this.sourceSwitch = sourceSwitch;
		this.destinationSwitch = destinationSwitch;
		this.weight = weight;
		this.bandwidht = bandwidht;
		this.latency = latency;
		this.securityLevel = securityLevel;
		this.betweenSwitches = betweenSwitches;
	}

	public VirtualEdgeSC(String id, VirtualSwitch sourceSwitch,
			VirtualSwitch destinationSwitch, int weight, int bandwidht,
			int latency, ArrayList<EdgeSC> linkedListEdgeSCWork,
			ArrayList<EdgeSC> linkedListEdgeSCBkp, boolean betweenSwitches) {
		super();
		this.virtualEdgeSCId = count.incrementAndGet();
		this.sourceSwitch = sourceSwitch;
		this.destinationSwitch = destinationSwitch;
		this.weight = weight;
		this.bandwidht = bandwidht;
		this.latency = latency;
		this.linkedListEdgeSCWork = linkedListEdgeSCWork;
		this.linkedListEdgeSCBkp = linkedListEdgeSCBkp;
		this.betweenSwitches = betweenSwitches;
	}

	public VirtualEdgeSC(int virtualEdgeSCId, VirtualSwitch sourceSwitch,
			VirtualSwitch destinationSwitch, int bandwidht,
			SecurityLevelLinks securityLevel, boolean betweenSwitches) {
		super();
		this.virtualEdgeSCId = virtualEdgeSCId;
		this.sourceSwitch = sourceSwitch;
		this.destinationSwitch = destinationSwitch;
		this.bandwidht = bandwidht;
		this.securityLevel = securityLevel;
		this.betweenSwitches = betweenSwitches;

	}
	
	
	public VirtualEdgeSC(int virtualEdgeSCId, Host virtualHost,
			VirtualSwitch destinationSwitch, int bandwidht,
			SecurityLevelLinks securityLevel, boolean betweenSwitches) {
		super();
		this.virtualEdgeSCId = virtualEdgeSCId;
		this.virtualHost = virtualHost;
		this.destinationSwitch = destinationSwitch;
		this.bandwidht = bandwidht;
		this.securityLevel = securityLevel;
		this.betweenSwitches = betweenSwitches;
	}

	public int getVirtualEdgeSCId() {
		return virtualEdgeSCId;
	}

	public void setVirtualEdgeSCId(int virtualEdgeSCId) {
		this.virtualEdgeSCId = virtualEdgeSCId;
	}

	public VirtualSwitch getSourceSwitch() {
		return sourceSwitch;
	}

	public void setSourceSwitch(VirtualSwitch sourceSwitch) {
		this.sourceSwitch = sourceSwitch;
	}

	public VirtualSwitch getDestinationSwitch() {
		return destinationSwitch;
	}

	public void setDestinationSwitch(VirtualSwitch destinationSwitch) {
		this.destinationSwitch = destinationSwitch;
	}

	public void setLinkedListEdgeSCBkp(
			ArrayList<EdgeSC> linkedListEdgeSCBkp) {
		this.linkedListEdgeSCBkp = linkedListEdgeSCBkp;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getBandwidht() {
		return bandwidht;
	}

	public void setBandwidht(int bandwidht) {
		this.bandwidht = bandwidht;
	}

	public int getLatency() {
		return latency;
	}

	public void setLatency(int latency) {
		this.latency = latency;
	}

	public ArrayList<EdgeSC> getLinkedListEdgeSCWork() {
		return linkedListEdgeSCWork;
	}

	public void setLinkedListEdgeSCWork(ArrayList<EdgeSC> linkedListEdgeSCWork) {
		this.linkedListEdgeSCWork = linkedListEdgeSCWork;
	}

	public ArrayList<EdgeSC> getLinkedListEdgeSCBkp() {
		return linkedListEdgeSCBkp;
	}

	public void setLinkedListEdgeSCWBkp(
			ArrayList<EdgeSC> linkedListEdgeSCBkp) {
		this.linkedListEdgeSCBkp = linkedListEdgeSCBkp;
	}

	public SecurityLevelLinks getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(SecurityLevelLinks securityLevel) {
		this.securityLevel = securityLevel;
	}

	@Override
	public int hashCode() {
		
		return (this.sourceSwitch==null?1:this.sourceSwitch.hashCode()) ^ 
				(this.destinationSwitch==null?1:this.destinationSwitch.hashCode()) ^
				(this.virtualHost==null?1:this.virtualHost.hashCode()) ^
				(this.linkedListEdgeSCWork==null?1:this.linkedListEdgeSCWork.hashCode()) ^ 
				(this.linkedListEdgeSCBkp==null?1:this.linkedListEdgeSCBkp.hashCode()) ^ this.virtualEdgeSCId;
	}

	@Override
	public boolean equals(Object o) {

		return (o instanceof VirtualEdgeSC) && 
				(sourceSwitch==null?true:(sourceSwitch.equals(((VirtualEdgeSC) o).sourceSwitch))) &&
				(destinationSwitch==null?true:(destinationSwitch.equals(((VirtualEdgeSC) o).destinationSwitch))) &&
				(virtualHost==null?true:(virtualHost.equals(((VirtualEdgeSC) o).virtualHost))) &&
				(linkedListEdgeSCWork==null?true:(linkedListEdgeSCWork.equals(((VirtualEdgeSC) o).linkedListEdgeSCWork))) &&
				(linkedListEdgeSCBkp==null?true:(linkedListEdgeSCBkp.equals(((VirtualEdgeSC) o).linkedListEdgeSCBkp))) &&
				(virtualEdgeSCId==0?true:(virtualEdgeSCId ==((VirtualEdgeSC) o).virtualEdgeSCId));

	}

	public boolean isBetweenSwitches() {
		return betweenSwitches;
	}

	public void setBetweenSwitches(boolean betweenSwitches) {
		this.betweenSwitches = betweenSwitches;
	}

	public Host getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(Host virtualHost) {
		this.virtualHost = virtualHost;
	}

	public String getWorkingPath() {
		return workingPath;
	}

	public void setWorkingPath(String workingPath) {
		this.workingPath = workingPath;
	}


	public String getBackupPath() {
		return backupPath;
	}

	public void setBackupPath(String backupPath) {
		this.backupPath = backupPath;
	}

	public String getWorkingPathLinks() {
		return workingPathLinks;
	}

	public void setWorkingPathLinks(String workingPathLinks) {
		this.workingPathLinks = workingPathLinks;
	}
}
