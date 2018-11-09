package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Link {

	private int id;
	private Node from;
	private Node to;
	private int bandwidth;
	private int delay;
	private int lossRate;
	private int securityLevel;
	private boolean deployed;
	private String route;
		
	public Link(int id, Node from, Node to, int bandwidth, int delay, 
			int lossRate, int securityLevel, String route, boolean deployed) {
		this.id = id;
		this.from = from;
		this.to = to;
		this.bandwidth = bandwidth;
		this.delay = delay;
		this.lossRate = lossRate;
		this.securityLevel = securityLevel;
		this.route = route;
		this.deployed = deployed;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Node getFrom() {
		return from;
	}

	public void setFrom(Node from) {
		this.from = from;
	}

	public Node getTo() {
		return to;
	}

	public void setTo(Node to) {
		this.to = to;
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getLossRate() {
		return lossRate;
	}

	public void setLossRate(int lossRate) {
		this.lossRate = lossRate;
	}
	
	public int getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(int securityLevel) {
		this.securityLevel = securityLevel;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}

	public String getTitle() {
		return bandwidth + "/" + delay + "/" + lossRate;
	}
	
	public boolean isBetweenSwitches() {
		return (from instanceof Switch) && (to instanceof Switch);
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("link");
		elem.setAttribute("id", Integer.toString(id));
		Config.writeXMLInteger(doc, elem, "from", from.getId());
		Config.writeXMLInteger(doc, elem, "to", to.getId());
		Config.writeXMLInteger(doc, elem, "bandwidth", bandwidth);
		Config.writeXMLInteger(doc, elem, "delay", delay);
		Config.writeXMLInteger(doc, elem, "loss-rate", lossRate);
		Config.writeXMLInteger(doc, elem, "security", securityLevel + 1);
		Config.writeXMLString(doc, elem, "route", route);
		Config.writeXMLBoolean(doc, elem, "deployed", deployed);
		return elem;
	}
	
	public void toJson(ArrayList<Object> data) {
		data.add(Console.tuple("Bandwidth", bandwidth, true));
		data.add(Console.tuple("Delay", delay, true));
		data.add(Console.tuple("Loss Rate", lossRate, true));
		data.add(Console.tuple("Security Level", securityLevel, new String[] 
				{ "Authenticity, Integrity", "Confid. & Auth., Integrity" }));
		data.add(Console.tuple("GRE Tunnel", deployed, false));
	}
	
	public boolean update(String name, String value, 
			GsonData data) throws ServerError {
		boolean refreshRequired = false;
		boolean updated = false;
		switch (name) {
		case "Bandwidth": {
			this.bandwidth = Console.parse(value);
			refreshRequired = true;
			updated = true;
			break;
		}
		case "Delay": {
			this.delay = Console.parse(value);
			refreshRequired = true;
			updated = true;
			break;
		}
		case "Loss Rate": {
			this.lossRate = Console.parse(value);
			refreshRequired = true;
			updated = true;
			break;
		}
		case "Security Level": {
			this.securityLevel = Console.parse(value);
			updated = true;
			break;
		}
		default:
			break;
		}
		if (refreshRequired == true)
			data.addLink(getId(), getTitle(), getFrom().getId(), 
					getTo().getId(), false);
		return updated;
	}
	
	public static int newId(Network network) {
		List<Integer> ids = new LinkedList<Integer>();
		for (Link link : network.getLinks()) 
			ids.add(link.getId());
		Collections.sort(ids);
		int index = 1;
		for (int id : ids)
			if (id != index)
				return index;
			else index++;
		return index;
	}
	
	public static Link findFromId(Network network, int id) {
		for (Link link : network.getLinks())
			if (link.getId() == id)
				return link;
		return null;
	}
	
	public static Link findFromEnds(Network network, Node from, Node to) {
		for (Link link: network.getLinks())
			if ((link.getFrom() == from && link.getTo() == to)
					|| (link.getFrom() == to && link.getTo() == from))
				return link;
		return  null;
	}
}
