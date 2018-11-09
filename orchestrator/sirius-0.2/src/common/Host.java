package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Host extends Node {

	private String ip;
	private String mac;
	private int port;
	private int tenant;
	private int mapping;
	private String image;
	private int cpu;

	public Host(int id, int index, String name, Vm vm, boolean deployed, boolean hidden,
			String ip, String mac, int port, String image, int cpu, int tenant, int mapping) {
		super(id, index, name, vm, deployed, hidden);
		this.ip = ip;
		this.mac = mac;
		this.port = port;
		this.tenant = tenant;
		this.mapping = mapping;
		this.image = image;
		this.cpu = cpu;
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTenant() {
		return tenant;
	}

	public void setTenant(int tenant) {
		this.tenant = tenant;
	}

	public int getMapping() {
		return mapping;
	}

	public void setMapping(int mapping) {
		this.mapping = mapping;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public String getType() {
		return "host";
	}
	
	public String getTitle() {
		if (ip != null) 
			return image + " / " + ip;
		return "unmapped";
	}

	public String getIcon() {
		Cloud cloud = getVm().getCloud();
		if (!cloud.isDeployed())
			return "img/host0.png";
		int state = cloud.getId();
		if (ip == null) state = - state;
		return "img/host" + state + ".png";
	}

	public Element toXML(Document doc) {
		Element elem = super.toXML(doc);
		Config.writeXMLString(doc, elem, "ip", ip);
		Config.writeXMLString(doc, elem, "mac", mac);
		Config.writeXMLInteger(doc, elem, "port", port);
		Config.writeXMLInteger(doc, elem, "tenant", tenant);
		Config.writeXMLInteger(doc, elem, "mapping", mapping);
		Config.writeXMLString(doc, elem, "image", image);
		Config.writeXMLInteger(doc, elem, "cpu", cpu);
		return elem;
	}
	
	public void toJson(Network network, ArrayList<Object> data) {
		super.toJson(network, data);
		String images[] = Image.toArray(network);
		String ipValue = (ip != null)? ip : "none";
		String macValue = (mac != null)? mac: "none";
		int imgValue = Image.indexOf(network, image);
		data.add(Console.tuple("Virtual IP", ipValue, true));
		data.add(Console.tuple("Virtual MAC", macValue, true));
		data.add(Console.tuple("Docker Image", imgValue, images));
		data.add(Console.tuple("Switch Port", port, false));
		if (network.getTenant() == 0)
			data.add(Console.tuple("Tenant ID", tenant, false));
		else 
			data.add(Console.tuple("Mapping ID", mapping, false));
		data.add(Console.tuple("CPU Strength", cpu, true));
	}
	
	public boolean update(Network network, String name, String value, 
			GsonData data) throws ServerError {
		boolean refreshRequired = super.update(network, name, value, data);
		boolean updated = refreshRequired;
		if (refreshRequired == false) 
			switch (name) {
			case "Virtual IP":
				this.ip = value;
				refreshRequired = true;
				updated = true;
				break;
			case "Virtual MAC": 
				this.mac = value;
				updated = true;
				break;
			case "Docker Image":
				int index = Console.parse(value);
				Image image = network.getImages().get(index);
				this.image = image.getName();
				refreshRequired = true;
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
			if (node instanceof Host)
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
