package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import server.Ssh;

public class Vm {

	private int id;
	private String name;
	private String pid;
	private Cloud cloud;
	private String publicIp;
	private String privateIp;
	private String location;
	private boolean gateway;
	private boolean fabric;
	private boolean deployed;
	private Ssh ssh;
	
	public Vm(int id, String name, String pid, Cloud cloud, String location, 
			String publicIp,  String privateIp, boolean gateway, boolean fabric, 
			boolean deployed, Config config) {
		this.id = id;
		this.name = name;
		this.pid = pid;
		this.cloud = cloud;
		this.location = location;
		this.publicIp = publicIp;
		this.privateIp = privateIp;
		this.gateway = gateway;
		this.fabric = fabric;
		this.deployed = deployed;
		this.ssh = new Ssh(config);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public Cloud getCloud() {
		return cloud;
	}

	public void setCloud(Cloud cloud) {
		this.cloud = cloud;
	}

	public String getPublicIp() {
		return publicIp;
	}

	public void setPublicIp(String publicIp) {
		this.publicIp = publicIp;
	}

	public String getPrivateIp() {
		return privateIp;
	}

	public void setPrivateIp(String privateIp) {
		this.privateIp = privateIp;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isGateway() {
		return gateway;
	}

	public void setGateway(boolean gateway) {
		this.gateway = gateway;
	}

	public boolean isFabric() {
		return fabric;
	}

	public void setFabric(boolean fabric) {
		this.fabric = fabric;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}

	public synchronized String runCmd(String cmd) throws ServerError {
		String keyFile = getCloud().getKeyFile();
		String keyPath = Console.getPath("keys/" + keyFile);
		String username = getCloud().getUsername();
		int port = getCloud().getPort();
		StringBuffer buffer = new StringBuffer();
		ssh.run(username, port, getPublicIp(), 
				keyPath, cmd, buffer);
		return buffer.toString();
	}

	public synchronized void uploadFile(String source, String dest) throws ServerError {
		String keyFile = getCloud().getKeyFile();
		String keyPath = Console.getPath("keys/" + keyFile);
		String username = getCloud().getUsername();
		int port = getCloud().getPort();
		ssh.upload(username, port, getPublicIp(), 
				keyPath, source, dest);
	}
	
	public synchronized boolean stopCmd() throws ServerError {
		return ssh.stop();
	}
	
	public Element toXML(Document doc) {
		Element elem = doc.createElement("vm");
		elem.setAttribute("id", Integer.toString(id));
		Config.writeXMLString(doc, elem, "name", name);
		Config.writeXMLString(doc, elem, "pid", pid);		
		Config.writeXMLInteger(doc, elem, "cid", cloud.getId());
		Config.writeXMLString(doc, elem, "location", location);
		Config.writeXMLString(doc, elem, "public-ip", publicIp);
		Config.writeXMLString(doc, elem, "private-ip", privateIp);
		Config.writeXMLBoolean(doc, elem, "gateway", gateway);
		Config.writeXMLBoolean(doc, elem, "fabric", fabric);
		Config.writeXMLBoolean(doc, elem, "deployed", deployed);
		return elem;
	}
	
	public void toJson(ArrayList<Object> data) {
		String boolValues[] = { "false", "true" };
		data.add(Console.tuple("VM Name", name, true));
		data.add(Console.tuple("Public IP", publicIp, false));
		data.add(Console.tuple("Private IP", privateIp, false));
		data.add(Console.tuple("Geolocation", location, true));
		data.add(Console.tuple("Is Gateway", gateway, false));
		data.add(Console.tuple("Is Fabric", fabric? 1: 0, boolValues));
		data.add(Console.tuple("Is Deployed", deployed, false));
	}
	
	public boolean update(String name, String value, GsonData data) {
		switch (name) {
		case "VM Name": {
			this.name = value;
			int state = cloud.getId();
			if (!deployed) state = -state;
			data.addVM(id, state, name);
			return true;
		}
		case "Geolocation": {
			this.location = value;
			return true;
		}
		case "Is Fabric": {
			int index = Integer.parseInt(value);
			this.fabric = (index == 0)? false: true;
			return true;
		}
		default:
			return false;
		}
	}
	
	public static int newId(Network network) {
		List<Integer> ids = new LinkedList<Integer>();
		for (Vm vm : network.getVms())
			ids.add(vm.getId());
		Collections.sort(ids);
		int index = 1;
		for (int id : ids)
			if (id != index)
				return index;
			else index++;
		return index;
	}
	
	public static Vm findFromInfo(Network network, String pid, int cid) {
		for (Vm vm : network.getVms())
			if (pid.equals(vm.getPid()) 
					&& cid == vm.getCloud().getId())
				return vm;
		return null;
	}
	
	public static Vm findFromId(Network network, int vid) {
		for (Vm vm : network.getVms())
			if (vm.getId() == vid)
				return vm;
		return null;
	}
	
	public Switch findSwitch(Network network) {
		for (Node node : network.getNodes())
			if (node instanceof Switch 
					&& node.getVm() == this)
				return (Switch)node; 
		return null;
	}
	
	public ArrayList<Host> findContainers(Network network) {
		ArrayList<Host> tmp = new ArrayList<>();
		for (Node node : network.getNodes())
			if (node instanceof Host 
					&& node.getVm() == this)
				tmp.add((Host)node);
		return tmp;
	}
}
