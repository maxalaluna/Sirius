package net.floodlightcontroller.sirius.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class Ovs extends Node {

	private int ofVersion;
	private String dpid;
	private int security;
	
	public Ovs(int id, int index, String name, Vm vm, boolean deployed, 
			int ofVersion, String dpid, int security) {
		super(id, index, name, vm, deployed);
		this.ofVersion = ofVersion;
		this.security = security;
		this.dpid = dpid;
	}

	public int getOfVersion() {
		return ofVersion;
	}

	public void setOfVersion(int ofVersion) {
		this.ofVersion = ofVersion;
	}

	public String getDpid() {
		return dpid;
	}

	public void setDpid(String dpid) {
		this.dpid = dpid;
	}

	public int getSecurity() {
		return security;
	}

	public void setSecurity(int security) {
		this.security = security;
	}

	public String getType() {
		return "switch";
	}
	
	public String getTitle() {
		return "OpenFlow 1." + ofVersion;
	}

	public String getImage() {
		int state = getVm().getCloud().getId();
		if (!isDeployed()) state = - state;
		return "img/switch" + state + ".png";
	}

	public Element toXML(Document doc) {
		Element elem = super.toXML(doc);
		Config.writeXMLInteger(doc, elem, "of-version", ofVersion);
		Config.writeXMLString(doc, elem, "dpid", dpid);
		Config.writeXMLInteger(doc, elem, "security", security);
		return elem;
	}
	
//	public void toJson(ArrayList<Object> data) {
//		super.toJson(data);
//		data.add(Console.tuple("OVS Version", "1." + ofVersion));
//		data.add(Console.tuple("Datapath ID", dpid));
//		data.add(Console.tuple("Security Level", security));
//	}
	
	public static int newIndex(Network network) {
		List<Integer> ids = new LinkedList<Integer>();
		for (Node node : network.getNodes())
			if (node instanceof Ovs)
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
		int nbOVS = 0;
		for (Node peer : peers)
			if (peer instanceof Ovs)
				nbOVS++;
		return nbOVS == 1;
	}
}
