package net.floodlightcontroller.sirius.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class Container2 extends Node {

	private String ip;
	private String mac;
	private int tenant;
	
	public Container2(int id, int index, String name, Vm vm, 
			boolean deployed, String ip, String mac, int tenant) {
		super(id, index, name, vm, deployed);
		this.ip = ip;
		this.mac = mac;
		this.tenant = tenant;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public int getTenant() {
		return tenant;
	}

	public void setTenant(int tenant) {
		this.tenant = tenant;
	}

	public String getType() {
		return "host";
	}
	
	public String getTitle() {
		return ip;
	}

	public String getImage() {
		int state = getVm().getCloud().getId();
		if (!isDeployed()) state = - state;
		return "img/host" + state + ".png";
	}

	public Element toXML(Document doc) {
		Element elem = super.toXML(doc);
		Config.writeXMLString(doc, elem, "ip", ip);
		Config.writeXMLString(doc, elem, "mac", mac);
		Config.writeXMLInteger(doc, elem, "tenant", tenant);
		return elem;
	}
	
	public static int newIndex(Network network) {
		List<Integer> ids = new LinkedList<Integer>();
		for (Node node : network.getNodes())
			if (node instanceof Container2)
				ids.add(node.getIndex());
		Collections.sort(ids);
		int index = 1;
		for (int id : ids)
			if (id != index)
				return index;
			else index++;
		return index;
	}
}
