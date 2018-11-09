package net.floodlightcontroller.sirius.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Switch extends Node {

	private int openflowVersion;
	private int securityLevel;
	private int availabilityLevel;
	private int maxFlowSize;
	private String bridgeName;
	private String dpid;
	private int mapping;
	private int cpu;
	
	public Switch(int id, int index, String name, Vm vm, boolean deployed, 
			int openflowVersion, String dpid, String bridgeName, int securityLevel, 
			int avaibilityLevel, int maxFlowSize, int cpu, int mapping) {
		super(id, index, name, vm, deployed);
		this.openflowVersion = openflowVersion;
		this.securityLevel = securityLevel;
		this.availabilityLevel = avaibilityLevel;
		this.maxFlowSize = maxFlowSize;
		this.bridgeName = bridgeName;
		this.dpid = dpid;
		this.mapping = mapping;
		this.cpu = cpu;
	}

	public int getOpenflowVersion() {
		return openflowVersion;
	}

	public void setOpenflowVersion(int openflowVersion) {
		this.openflowVersion = openflowVersion;
	}

	public String getDpid() {
		return dpid;
	}

	public void setDpid(String dpid) {
		this.dpid = dpid;
	}

	public int getMaxFlowSize() {
		return maxFlowSize;
	}

	public void setMaxFlowSize(int maxFlowSize) {
		this.maxFlowSize = maxFlowSize;
	}

	public String getBridgeName() {
		return bridgeName;
	}

	public void setBridgeName(String bridgeName) {
		this.bridgeName = bridgeName;
	}

	public int getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(int securityLevel) {
		this.securityLevel = securityLevel;
	}

	public int getAvailabilityLevel() {
		return availabilityLevel;
	}

	public void setAvailabilityLevel(int availabilityLevel) {
		this.availabilityLevel = availabilityLevel;
	}

	public int getMapping() {
		return mapping;
	}

	public void setMapping(int mapping) {
		this.mapping = mapping;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public String getType() {
		return "switch";
	}
	
	public String getTitle() {
		return bridgeName;
	}

	public String getIcon() {
		int state = getVm().getCloud().getId();
		if (!isDeployed()) state = - state;
		return "img/switch" + state + ".png";
	}

	public Element toXML(Document doc) {
		Element elem = super.toXML(doc);
		Config.writeXMLInteger(doc, elem, "openflow", openflowVersion);
		Config.writeXMLInteger(doc, elem, "security", securityLevel);
		Config.writeXMLInteger(doc, elem, "availability", availabilityLevel);
		Config.writeXMLInteger(doc, elem, "flows", maxFlowSize);
		Config.writeXMLString(doc, elem, "bridge", bridgeName);
		Config.writeXMLString(doc, elem, "dpid", dpid);
		Config.writeXMLInteger(doc, elem, "mapping", mapping);
		Config.writeXMLInteger(doc, elem, "cpu", cpu);
		return elem;
	}
	
	public void toJson(Network network, ArrayList<Object> data) {
		super.toJson(network, data);
		String dpidValue = (dpid != null)? dpid: "none";
		String bridgeValue =  (bridgeName != null)? bridgeName: "none";
		data.add(Console.tuple("Openflow Version", openflowVersion, 
				new String[] { "1.0", "1.1", "1.2", "1.3", "1.4", "1.5" }));
		data.add(Console.tuple("Security Level", securityLevel, 
				new String[] { "Container", "VM", "Secure VM" }));
		if (network.getTenant() > 0)
			data.add(Console.tuple("Availability Level", availabilityLevel, new String[] 
					{ "Single Virtual Node", "Replication (another cloud)", 
							"Replication (same cloud)" }));
		data.add(Console.tuple("Max Flow Size", maxFlowSize, true));
		data.add(Console.tuple("Bridge Name", bridgeValue, true));
		data.add(Console.tuple("Datapath ID", dpidValue, false));
		data.add(Console.tuple("Mapping ID", mapping, false));
		data.add(Console.tuple("CPU Strength", cpu, true));
	}
	
	public boolean update(Network network, String name, 
			String value, GsonData data) throws ServerError {
		boolean refreshRequired = super.update(network, name, value, data);
		boolean updated = refreshRequired;
		if (refreshRequired == false) 
			switch (name) {
			case "Openflow Version":
				this.openflowVersion = Console.parse(value);
				refreshRequired = true;
				updated = true;
				break;
			case "Security Level": 
				this.securityLevel = Console.parse(value);
				updated = true;
				break;
			case "Availability Level":
				this.availabilityLevel = Console.parse(value);
				updated = true;
				break;
			case "Max Flow Size":
				this.maxFlowSize = Console.parse(value);
				updated = true;
				break;
			case "Bridge Name":
				this.bridgeName = value;
				updated = true;
				break;
			case "CPU Strength" :
				this.cpu = Console.parse(value);
				updated = true;
				break;
			}
		if (refreshRequired == true)
			data.addNode(getId(), getVm().getId(), getVm().getCloud().getId(), 
					getName(), getTitle(), getIcon(), false);
		return updated;
	}
	
	public static int newIndex(Network network) {
		List<Integer> ids = new LinkedList<Integer>();
		for (Node node : network.getNodes())
			if (node instanceof Switch)
				ids.add(node.getIndex());
		Collections.sort(ids);
		int index = 1;
		for (int id : ids)
			if (id != index)
				return index;
			else index++;
		return index;
	}
	
	public boolean isLeaf(Network network) {
		ArrayList<Node> peers = findPeers(network);
		int nbSwitch = 0;
		for (Node peer : peers)
			if (peer instanceof Switch)
				nbSwitch++;
		return nbSwitch == 1;
	}
	
	public boolean isTransit(Network network) {
		ArrayList<Node> peers = findPeers(network);
		for (Node peer : peers)
			if (peer instanceof Host)
				return false;
		return true;
	}
}
