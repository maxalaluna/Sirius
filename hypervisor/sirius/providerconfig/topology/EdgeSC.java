package net.floodlightcontroller.sirius.providerconfig.topology;

import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.util.Enum.SecurityLevelLinks;

//Link
public class EdgeSC  {
	private int edgeSCId;
	private String description; 
	private Server3 source;
	private Server3 destination;
	private Host host;
	private int bandwidth;
	private int weight = 1;
	private SecurityLevelLinks securityLevel;
	private boolean betweenSwitches;

	public EdgeSC(String description, Server3 source, Server3 destination, int bandwidth, int weight, SecurityLevelLinks securityLevel, boolean betweenSwitches) {
		this.description = description;
		this.source = source;
		this.destination = destination;
		this.bandwidth = bandwidth;
		this.weight = weight;
		this.securityLevel = securityLevel;
		this.betweenSwitches = betweenSwitches;
	}

	public EdgeSC(int edgeSCId, String description, Server3 source, Server3 destination, int bandwidth, int weight, SecurityLevelLinks securityLevel, boolean betweenSwitches) {
		this.setEdgeSCId(edgeSCId);
		this.description = description;
		this.source = source;
		this.destination = destination;
		this.bandwidth = bandwidth;
		this.weight = weight;
		this.securityLevel = securityLevel;
		this.betweenSwitches = betweenSwitches;
	}

	public EdgeSC(String description, Server3 source, Host destination, int bandwidth, int weight, SecurityLevelLinks securityLevel, boolean betweenSwitches) {
		this.description = description;
		this.source = source;
		this.setHost(destination);
		this.bandwidth = bandwidth;
		this.weight = weight;
		this.securityLevel = securityLevel;
		this.betweenSwitches = betweenSwitches;
	}
	
	public EdgeSC(int edgeSCId, String description, Server3 source, Host destination, int bandwidth, int weight, SecurityLevelLinks securityLevel, boolean betweenSwitches) {
		this.setEdgeSCId(edgeSCId);
		this.description = description;
		this.source = source;
		this.setHost(destination);
		this.bandwidth = bandwidth;
		this.weight = weight;
		this.securityLevel = securityLevel;
		this.betweenSwitches = betweenSwitches;
	}

	public EdgeSC(String description, Server3 source, Server3 destination, int weight) {
		this.description = description;
		this.source = source;
		this.destination = destination;
		this.weight = weight;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSource(Server3 source) {
		this.source = source;
	}

	public void setDestination(Server3 destination) {
		this.destination = destination;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getDescription() {
		return description;
	}
	public Server3 getDestination() {
		return destination;
	}

	public Server3 getSource() {
		return source;
	}
	public int getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return source + " " + destination;
	}

	public boolean equals(Object o) {

		return (o instanceof EdgeSC) && 
				(description.equals(((EdgeSC) o).description)) && 
				(source.equals(((EdgeSC) o).source)) &&
				(destination.equals(((EdgeSC) o).destination)) &&
				(weight == ((EdgeSC) o).weight);
	}

	public int hashCode() {

		return description.hashCode() ^ (source == null? 1:source.hashCode()) ^ (destination == null? 1:destination.hashCode()) ^ (host == null? 1:host.hashCode()) ^ weight;  

	}

	public SecurityLevelLinks getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(SecurityLevelLinks securityLevel) {
		this.securityLevel = securityLevel;
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	public boolean isBetweenSwitches() {
		return betweenSwitches;
	}

	public void setBetweenSwitches(boolean betweenSwitches) {
		this.betweenSwitches = betweenSwitches;
	}

	public int getEdgeSCId() {
		return edgeSCId;
	}

	public void setEdgeSCId(int edgeSCId) {
		this.edgeSCId = edgeSCId;
	}
}

