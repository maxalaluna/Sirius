package net.floodlightcontroller.sirius.topology.xml;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Network {

	private ArrayList<Node> nodes;
	private ArrayList<Link> links;
	private ArrayList<Hull> hulls;
	private int controllerIndex = 0;
	private int switchIndex = 0;
	private int hostIndex = 0;
	private int nodeIndex = 0;
	private int linkIndex = 0;
	
	public Network() {
		nodes = new ArrayList<>();
		links = new ArrayList<>();
		hulls = new ArrayList<>();
	}
	
	public Iterator<Node> getNodeIterator() {
		return nodes.iterator();
	}
	
	public Iterator<Link> getLinkIterator() {
		return links.iterator();
	}
		
	public Iterator<Hull> getHullIterator() {
		return hulls.iterator();
	}
	
	public int getHullSize() {
		return hulls.size();
	}
	
	public int getNodeSize() {
		return nodes.size();
	}
	
	public int getLinkSize() {
		return links.size();
	}
	
    public void clearNetwork() {
      	controllerIndex = 0;
    	switchIndex = 0;
    	hostIndex = 0;
 		nodeIndex = 0;
 		linkIndex = 0;
    	hulls.clear();
		nodes.clear(); 
		links.clear(); 
    }
        
    public Hull getHullById(int id) {
    	for (Hull hull : hulls) 
    		if (hull.getId() == id)
    			return hull;
    	return null;
    }

    public Node getNodeById(int id) {
    	for (Node node : nodes) 
    		if (node.getId() == id)
    			return node;
    	return null;
    }
    
    public Node getNodeByName(String name) {
    	for (Node node : nodes) 
    		if (node.getName().equals(name))
    			return node;
    	return null;
    }
    
    public Link getLinkById(int id) {
    	for (Link link : links)
    		if (link.getId() == id)
    			return link;
    	return null;
    }
    
	public int newNodeId() {
		return ++nodeIndex;
	}
	
	public int newLinkId() {
		return ++linkIndex;
	}
	
	public int newHostIndex() {
		return ++hostIndex;
	}
	
	public int newSwitchIndex() {
		return ++switchIndex;
	}
	
	public int newControllerIndex() {
		return ++controllerIndex;
	}
	
	public void addHull(Hull hull) {
		hulls.add(hull);
	}
	
	public void addHost(Host node) {
		if (node.getIndex() > hostIndex) hostIndex = node.getIndex();
		if (node.getId() > nodeIndex) nodeIndex = node.getId();
		nodes.add(node);
	}
	
	public void addSwitch(Switch node) {
		if (node.getIndex() > switchIndex) switchIndex = node.getIndex();
		if (node.getId() > nodeIndex) nodeIndex = node.getId();
		nodes.add(node);
	}
	
	public void addController(Controller node) {
		if (node.getIndex() > controllerIndex) controllerIndex = node.getIndex();
		if (node.getId() > nodeIndex) nodeIndex = node.getId();
		nodes.add(node);
	}
	
	public void addLink(Link link) {
		Node node1 = getNodeById(link.getFrom());
		Node node2 = getNodeById(link.getTo());
		if (link.getId() > linkIndex)
			linkIndex = link.getId();
		if (node1 != null && node2 != null) {
			if (node1 instanceof Switch && node2 instanceof Switch)
				link.setBetweenSwitches(true);
			if (node1.getTenant() == node2.getTenant())
				link.setTenant(node1.getTenant());
			node1.getLinks().add(link);
			node2.getLinks().add(link);
			links.add(link);
		}
	}
	
	public void removeHull(Hull hull) {
		hulls.remove(hull);
	}
	
	public void removeNode(Node node) {
		nodes.remove(node);
	}
	
	public void removeLink(Link link) {
		links.remove(link);
	}
	
	private ArrayList<Point> readPoints(Element root) {
		ArrayList<Point> points = new ArrayList<>();
		NodeList nodeLst = root.getElementsByTagName("point");
    	for (int k = 0; k < nodeLst.getLength(); k++) {
    		Element elem = (Element)nodeLst.item(k);
    		int x = Config.readXMLInteger(elem, "x", 0);
    		int y = Config.readXMLInteger(elem, "y", 0);
    		points.add(new Point(x, y));
    	}
		return points;
	}
	
	public void readDocument(Config config, Document doc) {
		clearNetwork();
		
		// Read node elements
    	NodeList nodeLst = doc.getElementsByTagName("node");
    	for (int k = 0; k < nodeLst.getLength(); k++) {
    		Element elem = (Element)nodeLst.item(k);
    		int id = Integer.parseInt(elem.getAttribute("id"));
    		int index = Config.readXMLInteger(elem, "index", 0);
    		String type = Config.readXMLString(elem, "type", "none");
			int cloud = Config.readXMLInteger(elem, "cloud", 0);
    		int cpu = Config.readXMLInteger(elem, "cpu", config.defaultNodeCpu);
    		int mem = Config.readXMLInteger(elem, "mem", config.defaultNodeMemory);
    		int x = Config.readXMLInteger(elem, "x", 0);
    		int y = Config.readXMLInteger(elem, "y", 0);
    		
    		// Host
    		if (type.equals("host"))  {
    			String defIp = config.createHostIp(index);
    			String name = Config.readXMLString(elem, "name", "h" + index);
    			String ip = Config.readXMLString(elem, "ip", defIp);
	    		String mac = Config.readXMLString(elem, "mac", null);
    			int tenant = Config.readXMLInteger(elem, "tenant", 0);
    			int map = Config.readXMLInteger(elem, "map", 0);
	    		Host node = new Host(id, index, x, y, name, ip, 
	    				cloud, cpu, mem, mac, tenant, map);
    			addHost(node); 
    		}
    		
    		// Switch
    		else if (type.equals("switch")) {
    			String defIp = config.createSwitchIp(index);
    			String name = Config.readXMLString(elem, "name", "s" + index);
    			String ip = Config.readXMLString(elem, "ip", defIp);
    			String dpid = Config.readXMLString(elem, "dpid", null);
    			int version = Config.readXMLInteger(elem, "version", config.defaultOpenFlowVersion);
    			int flows = Config.readXMLInteger(elem, "flows", config.defaultFlowsMaximum);
    			int secure = Config.readXMLInteger(elem, "secure", config.defaultNodeSecurity);
    			int depend = Config.readXMLInteger(elem, "depend", config.defaultNodeDependability);
    			int trust = Config.readXMLInteger(elem, "trust", config.defaultCloudType);
    			int tenant = Config.readXMLInteger(elem, "tenant", 0);
    			int map = Config.readXMLInteger(elem, "map", 0);
    			Switch node = new Switch(id, index, x, y, name, ip, 
    					cloud, cpu, mem, dpid, version, flows, 
    					secure, depend, trust, tenant, map);
    			addSwitch(node);
    		}
    		
    		// Controller
    		else if (type.equals("controller")) {
    			String name = Config.readXMLString(elem, "name", "c" + index);
    			String ip = Config.readXMLString(elem, "ip", config.defaultControllerIp);
    			int port = Config.readXMLInteger(elem, "port", config.defaultControllerPort);
    			Controller node = new Controller(id, index, x, y, name, ip, 
    					cloud, cpu, mem, port);
    			addController(node);
    		}
    	}
    	System.out.println("Total nodes loaded : " + nodeLst.getLength());
    	
    	// Read link elements
    	nodeLst = doc.getElementsByTagName("link");
    	for (int k = 0; k < nodeLst.getLength(); k++) {
    		Element elem = (Element)nodeLst.item(k);
    		int id = Integer.parseInt(elem.getAttribute("id"));
    		int from = Config.readXMLInteger(elem, "from", 0);
    		int to = Config.readXMLInteger(elem, "to", 0);
    		int band = Config.readXMLInteger(elem, "band", config.defaultLinkBand);
    		int delay = Config.readXMLInteger(elem, "delay", config.defaultLinkDelay);
    		int loss = Config.readXMLInteger(elem, "loss", config.defaultLinkLoss);
    		int secure = Config.readXMLInteger(elem, "secure", config.defaultLinkSecurity);
    		String route = Config.readXMLString(elem, "route", null);
    		Link link = new Link(id, from, to, band, delay, loss, secure, route);
    		addLink(link);
    	}
    	System.out.println("Total links loaded : " + nodeLst.getLength());
    	
    	// Read hull elements
    	nodeLst = doc.getElementsByTagName("hull");
    	for (int k = 0; k < nodeLst.getLength(); k++) {
    		Element elem = (Element)nodeLst.item(k);
    		int id = Integer.parseInt(elem.getAttribute("id"));
    		ArrayList<Point> points = readPoints(elem);
    		hulls.add(new Hull(id, points));
    	}
    	System.out.println("Total hulls loaded : " + nodeLst.getLength());
	}
	
	public Document writeDocument(Config config, boolean extended) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();

			// Root elements
			doc = docBuilder.newDocument();
			Element root = doc.createElement("config");
			doc.appendChild(root);
					
			// Add node elements
			for (Node node : nodes) {
				Element aux = node.toXML(doc, config);
				root.appendChild(aux);
			}
			
			// Add link elements
			for (Link link : links) {
				Element aux = link.toXML(doc, config);
				root.appendChild(aux);
			}
			
			// Add hull elements
			if (extended == true) {
				for (Hull hull : hulls) {
					Element aux = hull.toXML(doc, config);
					root.appendChild(aux);
				}
			}
		} 
		catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		return doc;		
	}
}
