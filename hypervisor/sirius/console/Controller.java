package net.floodlightcontroller.sirius.console;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Controller extends Node {

	private int port;
	private String ip;
	private boolean connected;
	
	public Controller(int id, int index, String name, Vm vm, 
			boolean deployed, String ip, int port, boolean connected) {
		super(id, index, name, vm, deployed);
		this.ip = ip;
		this.port = port;
		this.connected = connected;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getType() {
		return "controller";
	}
	
	public String getTitle() {
		return ip + " / " + port;
	}

	public String getIcon() {
		return "img/controller1.png";
	}

	public Element toXML(Document doc) {
		Element elem = super.toXML(doc);
		Config.writeXMLString(doc, elem, "ip", ip);
		Config.writeXMLInteger(doc, elem, "port", port);
		return elem;
	}
	
	public void toJson(Network network, ArrayList<Object> data) {
		super.toJson(network, data);
		data.add(Console.tuple("Admin IP", ip, true));
		data.add(Console.tuple("TCP Port", port, true));
		data.add(Console.tuple("Connected", connected, false));
	}
	
	public boolean update(Network network, String name, 
			String value, GsonData data) throws ServerError {
		boolean refreshRequired = super.update(network, name, value, data);
		boolean updated = refreshRequired;
		if (refreshRequired == false) 
			switch (name) {
			case "Admin IP":
				this.ip = value;
				refreshRequired = true;
				updated = true;
				break;
			case "TCP Port": 
				this.port = Console.parse(value);
				refreshRequired = true;
				updated = true;
				break;
			}
		if (refreshRequired == true)
			data.addNode(getId(), getVm().getId(), getVm().getCloud().getId(), 
					getName(), getTitle(), getIcon(), false);
		return updated;
	}
	
	public static Controller find(Network network) {
		for (Node node: network.getNodes())
			if (node instanceof Controller)
				return (Controller)node;
		return null;
	}
}
