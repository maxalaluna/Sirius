/**
 * SIRIUS - Testbed platform console 
 * Faculdade de Ciências da Universidade de Lisboa
 */

package net.floodlightcontroller.sirius.topology.xml;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class Node {

	private int id;
	private int cpu;
	private int mem;
	private int x, y;
	private int index;
	private String ip;
	private String name;
	private int cloud;
	private int tenant;
	private int mapping;
	private ArrayList<Link> links;
	
	public Node(int id, int index, int x, int y, String name, 
			String ip, int cloud, int cpu, int mem, 
			int tenant, int mapping) {
		links = new ArrayList<>();
		this.id = id;
		this.x = x;
		this.y = y;
		this.cpu = cpu;
		this.mem = mem;
		this.index = index; 
		this.name = name;
		this.cloud = cloud;
		this.tenant = tenant;
		this.mapping = mapping;
		this.ip = ip;
	}
	
	/**
	 * Return node's id
	 * @return id
	 */
	
	public int getId() {
		return id;
	}

	/**
	 * Set node's id
	 * @param id
	 */
	
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Return X coordinates
	 * @return X
	 */
	
	public int getX() {
		return x;
	}

	/** 
	 * Set X coordinate
	 * @param x
	 */
	
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Return Y coordinate
	 * @return
	 */
	
	public int getY() {
		return y;
	}

	/** 
	 * Set Y coordinate
	 * @param y
	 */
	
	public void setY(int y) {
		this.y = y;
	}
	
	/**
	 * Return number of node's cpus
	 * @return cpu number
	 */
	
	public int getCpu() {
		return cpu;
	}

	/** 
	 * Set number of node's cpus
	 * @param cpu
	 */
	
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	/**
	 * Return node's memory in Gbs
	 * @return memory
	 */
	
	public int getMem() {
		return mem;
	}

	/**
	 * Set node's memory in Gbs
	 * @param mem
	 */
	
	public void setMem(int mem) {
		this.mem = mem;
	}

	/**
	 * Return node's index. Unlike ids which are unique for every node,
	 * indexes are incremented for each types of nodes: switches, hosts
	 * and controllers. In mininet, node's name is "s" + index for switches
	 * "h" + index for hosts and "c" + index for controllers.
	 * @return index
	 */
	
	public int getIndex() {
		return index;
	}

	/**
	 * Set node's index
	 * @param index
	 */
	
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Return node's ip address
	 * @return
	 */
	
	public String getIp() {
		return ip;
	}

	/**
	 * Set node's ip address
	 * @param ip
	 */
	
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Return node's name only used in the graphic console. 
	 * It can be different from the name used in mininet
	 * @return node's name
	 */
	
	public String getName() {
		return name;
	}

	/**
	 * Set node's name
	 * @param name
	 */
	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Return id of the cloud the switch belongs to
	 * 0 for private cloud, 1, 2... for different public clouds
	 * @return cloud's id
	 */
	
	public int getCloud() {
		return cloud;
	}

	/**
	 * Set cloud's id
	 * @param cloud
	 */
	
	public void setCloud(int cloud) {
		this.cloud = cloud;
	}
	
	/**
	 * Return tenant id assigned to host
	 * Return 0 if no tenant assigned
	 * @return tenant's id
	 */
	
	public int getTenant() {
		return tenant;
	}

	/**
	 * Set tenant's assigned to host
	 * @param tenant
	 */
	
	public void setTenant(int tenant) {
		this.tenant = tenant;
	}
	
	/**
	 * Return mapped physical node
	 * Return 0 if node is not mapped
	 * Unused field for physical node
	 * @return physical's node id
	 */
	
	public int getMapping() {
		return mapping;
	}

	/**
	 * Set mapped physical node
	 * @param mapping
	 */
	
	public void setMapping(int mapping) {
		this.mapping = mapping;
	}

	/**
	 * Return list of links connected to the node
	 * For a host single connected to a switch, the list
	 * only contents one link.
	 * @return list of links
	 */
	
	public ArrayList<Link> getLinks() {
		return links;
	}

	/**
	 * Return the nth peer connected to a node 
	 * @param nodeBase
	 * @param index of link
	 * @return node
	 */
	
	public Node getPeer(Network base, int index) {
		Link link = links.get(index);
		if (link.getFrom() == id)
			return base.getNodeById(link.getTo());
		else if (link.getTo() == id)
			return base.getNodeById(link.getFrom());
		return null;
	}
	
	/**
	 * Return the bandwidth of the nth link connected to a node
	 * @param index of link
	 * @return bandwidth
	 */
	
	public int getBand(int index) {
		Link link = links.get(index);
		return link.getBand();
	}
	
	/**
	 * Return the port number of the peer where the node is connected
	 * @param nodeBase
	 * @param index of link
	 * @return port 
	 */
	
	public int getPort(Network base, int index) {
		Node peer = getPeer(base, index);
		Link link = links.get(index);
		return peer.getLinks().indexOf(link);
	}
	
	/**
	 *	Convert node into string 
	 */
	
	public String toString() {
		return getType() + " id = " + id + " name = " + name + " x = " + x 
				+ " y = " + y + " ip = " + ip + " cloud = " + cloud 
				+ " cpu = " + cpu + " mem = " + mem + " tenant = " 
				+ tenant + " mapping = " + mapping;
	}
	
	/**
	 * Return title to be shown in graph window
	 * @return title
	 */
	
	public String getTitle() {
		return ip + " [" + id + "]";
	}
	
	/**
	 * Write node's attributes into XML document
	 * @param doc
	 * @param config
	 * @return XML element
	 */
	
	public Element toXML(Document doc, Config config) {
		Element elem = doc.createElement("node");
		elem.setAttribute("id", Integer.toString(id));
		Config.writeXMLInteger(doc, elem, "index", index);
		Config.writeXMLString(doc, elem, "type", getType());
		Config.writeXMLString(doc, elem, "name", name);		
		Config.writeXMLString(doc, elem, "ip", ip);
		Config.writeXMLInteger(doc, elem, "x", x);
		Config.writeXMLInteger(doc, elem, "y", y);
		
		// Optional data
		if (cloud > 0)
			Config.writeXMLInteger(doc, elem, "cloud", cloud);
		if (cpu != config.defaultNodeCpu)
			Config.writeXMLInteger(doc, elem, "cpu", cpu);
		if (mem != config.defaultNodeMemory)
			Config.writeXMLInteger(doc, elem, "mem", mem);
		if (tenant > 0)
			Config.writeXMLInteger(doc, elem, "tenant", tenant);
		if (mapping > 0)
			Config.writeXMLInteger(doc, elem, "map", mapping);
		return elem;
	}

	/**
	 * Return name of node's icon file
	 * @return icon file name
	 */
	
	public abstract String getImage();
	
	/**
	 * Return node's type
	 * @return "switch", "host" or "controller"
	 */
	
	public abstract String getType();	
}
