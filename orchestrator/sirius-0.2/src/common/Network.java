package common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import server.Tunnel;


public class Network {

	private ArrayList<Image> images;
	private ArrayList<Tunnel> tunnels;
	private ArrayList<Cloud> clouds;
	private ArrayList<Node> nodes;
	private ArrayList<Link> links;
	private ArrayList<Vm> vms;
	private boolean changed;
	private Config config;
	private int tenant;
	
	public Network(Config config) {
		images = new ArrayList<>();
		tunnels = new ArrayList<>();
		clouds = new ArrayList<>();
		vms = new ArrayList<>();
		nodes = new ArrayList<>();
		links = new ArrayList<>();
		this.config = config;
	}
	
	public int getTenant() {
		return tenant;
	}

	public void setTenant(int tenant) {
		this.tenant = tenant;
	}

	public ArrayList<Image> getImages() {
		return images;
	}
	
	public ArrayList<Tunnel> getTunnels() {
		return tunnels;
	}
	
	public ArrayList<Cloud> getClouds() {
		return clouds;
	}
	
	public ArrayList<Vm> getVms() {
		return vms;
	}
	
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	public ArrayList<Link> getLinks() {
		return links;
	}
	
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public void clear() {
		clouds.clear();
		vms.clear();
		nodes.clear();
		links.clear();
	}
	
	public void readXML(String path) throws ServerError {
		Console.info("Reading topology from '" + path + "'");
		try {
			InputStream is = new FileInputStream(path);
			readDocument(Config.loadDocument(is));
			is.close();
		} 
		catch (IOException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
    
	public void writeXML(String path) throws ServerError {
		try {
			Document doc = writeDocument();
			OutputStream os = new FileOutputStream(path);
			StreamResult result = new StreamResult(os);
			Console.info("Writing topology into '" + path + "'");
			Config.saveDocument(doc, result);
			setChanged(false);
		} 
		catch (FileNotFoundException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
	
	public void removeAllLinksFromNode(Node node) {
		int k = 0;
		while (k < links.size()) {
			Link link = links.get(k);
			if (link.getFrom() == node 
					|| link.getTo() == node)
				links.remove(k);
			else k++;
		}
	}
	
	public void removeAllNodesFromVM(Vm vm) {
		int k = 0;
		while (k < nodes.size()) {
			Node node = nodes.get(k);
			if (node.getVm() == vm) {
				Console.info("Removing " + node.getType() + " " + node.getName());
				removeAllLinksFromNode(node);
				nodes.remove(k);
			}
			else k++;
		}
	}
	
	private void readImage(Element elem) {
		ArrayList<String> script = new ArrayList<>();
		String name = Config.readXMLString(elem, "name");
		String file = Config.readXMLString(elem, "file");
		NodeList nodes = elem.getElementsByTagName("script");
		for (int k = 0; k < nodes.getLength(); k++)
			script.add(nodes.item(k).getTextContent());
		int priority = Config.readXMLInteger(elem, "priority");
		images.add(new Image(name, file, script, priority));	
	}
	
	private void readTunnel(Element elem) {
		int fromId = Config.readXMLInteger(elem, "from-id");
		String fromIp = Config.readXMLString(elem, "from-ip");
		int toId = Config.readXMLInteger(elem, "to-id");
		String toIp = Config.readXMLString(elem, "to-ip");
		tunnels.add(new Tunnel(fromId, fromIp, toId, toIp));
	}
	
	private void readCloud(Element elem) {
		int id = Integer.parseInt(elem.getAttribute("id"));
		String name = Config.readXMLString(elem, "name");
		String provider = Config.readXMLString(elem, "provider");
		String username = Config.readXMLString(elem, "username");
		int port = Config.readXMLInteger(elem, "port");
		String identity = Config.readXMLString(elem, "identity");
		String credential = Config.readXMLString(elem, "credential");
		int security = Config.readXMLInteger(elem, "security") - 1;
		String key = Config.readXMLString(elem, "key");
		boolean deployed = Config.readXMLBoolean(elem, "deployed");
		clouds.add(new Cloud(id, name, provider, username, port,
				identity, credential, security, key, deployed));		
	}
	
	private void readVm(Element elem) {
		int id = Integer.parseInt(elem.getAttribute("id"));
		String name = Config.readXMLString(elem, "name");
		String pid = Config.readXMLString(elem, "pid");
		int cid = Config.readXMLInteger(elem, "cid");
		String location = Config.readXMLString(elem, "location");
		String publicIp = Config.readXMLString(elem, "public-ip");
		String privateIp = Config.readXMLString(elem, "private-ip");
		boolean gateway = Config.readXMLBoolean(elem, "gateway");
		boolean fabric = Config.readXMLBoolean(elem, "fabric");
		boolean deployed = Config.readXMLBoolean(elem, "deployed");
		Cloud cloud = Cloud.findFromId(this, cid);
		vms.add(new Vm(id, name, pid, cloud, location, publicIp, 
				privateIp, gateway, fabric, deployed, config));
	}
	
	private void readNode(Element elem) {
		String type = Config.readXMLString(elem, "type");
		int id = Integer.parseInt(elem.getAttribute("id"));
		int index = Config.readXMLInteger(elem, "index");
		String name = Config.readXMLString(elem, "name");
		int vid = Config.readXMLInteger(elem, "vid"); 
		boolean deployed = Config.readXMLBoolean(elem, "deployed");
		boolean hidden = Config.readXMLBoolean(elem, "hidden");
		Vm vm = Vm.findFromId(this, vid);
		
		// Hosts
		if (type.equals("host"))  {
			String ip = Config.readXMLString(elem, "ip");
			String mac = Config.readXMLString(elem, "mac");
			int port = Config.readXMLInteger(elem, "port");
			int cpu = Config.readXMLInteger(elem, "cpu");
			int tenant = Config.readXMLInteger(elem, "tenant");
			int mapping = Config.readXMLInteger(elem, "mapping");
			String image = Config.readXMLString(elem, "image");
    		nodes.add(new Host(id, index, name, vm, deployed, hidden, 
    				ip, mac, port, image, cpu, tenant, mapping));
		}
		
		// Switches
		else if (type.equals("switch")) {
			int openflow = Config.readXMLInteger(elem, "openflow");
			String dpid = Config.readXMLString(elem, "dpid");
			String bridge = Config.readXMLString(elem, "bridge");
			int security = Config.readXMLInteger(elem, "security") - 1;
			int dependability = Config.readXMLInteger(elem, "dependability");
			int flows = Config.readXMLInteger(elem, "flows");
			int cpu = Config.readXMLInteger(elem, "cpu");
			int mapping = Config.readXMLInteger(elem, "mapping");
			nodes.add(new Switch(id, index, name, vm, deployed, hidden, openflow, 
					dpid, bridge, security, dependability, flows, cpu, mapping));
		}
		
		// Controller
		else if (type.equals("controller")) {
			String ip = Config.readXMLString(elem, "ip"); 
			int port = Config.readXMLInteger(elem, "port");
			nodes.add(new Controller(id, index, name, vm, 
					deployed, hidden, ip, port, false));
		}	
	}
	
	private void readLink(Element elem) {
		int id = Integer.parseInt(elem.getAttribute("id"));
		int from = Config.readXMLInteger(elem, "from");
		int to = Config.readXMLInteger(elem, "to");
		int bandwidth = Config.readXMLInteger(elem, "bandwidth");
		int delay = Config.readXMLInteger(elem, "delay");
		int lossRate = Config.readXMLInteger(elem, "loss-rate");
		int security = Config.readXMLInteger(elem, "security") - 1;
		String route = Config.readXMLString(elem, "route");
		boolean deployed = Config.readXMLBoolean(elem, "deployed");
		Node node1 = Node.findFromId(this, from);
		Node node2 = Node.findFromId(this, to);
		links.add(new Link(id, node1, node2, bandwidth, delay, 
				lossRate, security, route, deployed));
	}
	
	public void readDocument(Document doc) {
		Element root = doc.getDocumentElement();
		setTenant(Integer.parseInt(root.getAttribute("id")));
    	NodeList nodeLst = root.getChildNodes();
    	int nbVms = 0, nbNodes = 0, nbLinks = 0;
    	int nbImages = 0, nbClouds = 0, nbTunnels = 0;
 
    	// Scan all child nodes
    	for (int k = 0; k < nodeLst.getLength(); k++) {
    		org.w3c.dom.Node node = nodeLst.item(k);
    		if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
    			Element elem = (Element)node;
	    		switch (elem.getNodeName()) {
	    		case "image":
	    			readImage(elem);
	    			nbImages++;
	    			break;
	    		case "tunnel":
	    			readTunnel(elem);
	    			nbTunnels++;
	    			break;
	    		case "cloud":
	    			readCloud(elem);
	    			nbClouds++;
	    			break;
	    		case "vm":
	    			readVm(elem);
	    			nbVms++;
	    			break;
	    		case "node":
	    			readNode(elem);
	    			nbNodes++;
	    			break;
	    		case "link":
	    			readLink(elem);
	    			nbLinks++;
	    			break;
	    		}
    		}
    	}
    	Console.info("Total images: " + nbImages);
    	Console.info("Total clouds: " + nbClouds);
    	Console.info("Total tunnels: " + nbTunnels);
    	Console.info("Total vms: " + nbVms);
    	Console.info("Total nodes: " + nbNodes);
    	Console.info("Total links: " + nbLinks);
	}
	
	public Document writeDocument() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();

			// Root elements
			doc = docBuilder.newDocument();
			Element root = doc.createElement("config");
			root.setAttribute("id", Integer.toString(getTenant()));
			doc.appendChild(root);
				
			// Add image elements
			for (Image image: images) {
				Element aux = image.toXML(doc);
				root.appendChild(aux);				
			}
			
			// Add tunnel elements
			for (Tunnel tunnel: tunnels) {
				Element aux = tunnel.toXML(doc);
				root.appendChild(aux);
			}
			
			// Add cloud elements
			for (Cloud cloud : clouds) {
				Element aux = cloud.toXML(doc);
				root.appendChild(aux);
			}
			
			// Add VM elements
			for (Vm vm : vms) {
				Element aux = vm.toXML(doc);
				root.appendChild(aux);
			}
			
			// Add node elements
			for (Node node : nodes) {
				Element aux = node.toXML(doc);
				root.appendChild(aux);
			}
			
			// Add link elements
			for (Link link : links) {
				Element aux = link.toXML(doc);
				root.appendChild(aux);
			}
		} 
		catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		return doc;		
	}
	
	public void copyImagesFrom(Network network) {
		Console.info("Copying images from network0");
		for (Image image : network.getImages())
			images.add(image);
	}
	
	public Cloud addDefaultCloud() {
		int id = Cloud.newId(this);
		Console.info("Creating default cloud");
		Cloud cloud = new Cloud(id, "default", "none", 
				"none", 0, "none", "none", 0, "none", false);
		clouds.add(cloud);
		setChanged(true);
		return cloud;
	}

	public Vm addDefaultVm(Config config, Cloud cloud) {
		int id = Vm.newId(this);
		Console.info("Creating default vm id " + id);
		Vm vm = new Vm(id, "vm" + id, "none", cloud, "none", 
				config.defaultPublicIp, config.defaultPrivateIp, 
				false, false, false, config);
		setChanged(true);
		vms.add(vm);
		return vm;
	}
	
	public Switch addDefaultSwitch(Config config, Vm vm) {
		int id = Node.newId(this);
		int index = Switch.newIndex(this);
		String name = (tenant == 0)? "ovs": "s";
		Console.info("Creating default switch id " + id);
		Switch node = new Switch(id, index, name + index, vm, false, false,	
				config.defaultOpenflowVersion, null, config.defaultBridgeName, 
				0, 0, config.defaultMaxFlowSize, 100, 0);
		setChanged(true);
		nodes.add(node);
		return node;
	}
	
	public Host addDefaultHost(Config config, Vm vm) {
		int id = Node.newId(this);
		int index = Host.newIndex(this);
		String image = config.defaultDockerImage;
		String ip = null, mac = null, name = null;
		//Console.info("Creating host with image " + image);
		if (tenant > 0) {
			name = "h" + index;
			ip = Console.createIP(config.defaultIpRange, index);
			mac = Console.createMAC(config.defaultMacRange, index);
		}
		else name = config.defaultDockerPrefix + index; 
		Host node = new Host(id, index, name, vm, false, 
				false, ip, mac, 0, image, 100, 0, 0);
		setChanged(true);
		nodes.add(node);
		return node;
	}
	
	public Link addDefaultLink(Config config, Node from, Node to) {
		int id = Link.newId(this);
		//Console.info("Creating default link id " + id);
		Link link = new Link(id, from, to, config.defaultLinkBandwidth, 
				config.defaultLinkDelay, config.defaultLinkLossRate, 0, null, false);
		setChanged(true);
		links.add(link);
		return link;
	}
	
	public GsonData toData() {
		GsonData data = new GsonData();
		
		// Serialize clouds	
		for (Cloud cloud : getClouds())
			data.addCloud(cloud.getId(), cloud.getName(), 0);
		
		// Serialize VMs
		for (Vm vm : getVms()) {
			int state = vm.getCloud().getId();
			if (!vm.isDeployed()) state = -state;
			data.addVM(vm.getId(), state, vm.getName());
		}
		
		// Serialize nodes
		for (Node node : getNodes())
			if (!node.isHidden())
				data.addNode(node.getId(), node.getVm().getId(), 
						node.getVm().getCloud().getId(), node.getName(), 
						node.getTitle(), node.getIcon(), false);
		
		// Serialize links
		for (Link link : getLinks())
			if (!link.getFrom().isHidden() && !link.getTo().isHidden())
				data.addLink(link.getId(), link.getTitle(), link.getFrom().getId(), 
						link.getTo().getId(), false);
		return data;
	}
}
