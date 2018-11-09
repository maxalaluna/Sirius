/**
 * SIRIUS - Testbed platform console 
 * Faculdade de Ciências da Universidade de Lisboa
 */

package net.floodlightcontroller.sirius.topology.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Host extends Node {

	private String mac;
	
	public Host(int id, int index, int x, int y, String name, String ip, 
			int cloud, int cpu, int mem, String mac, int tenant, int mapping) {
		super(id, index, x, y, name, ip, cloud, cpu, mem, tenant, mapping);
		this.mac = mac;
	}
	
	/**
	 * Return host's mac address
	 * @return mac address
	 */
	
	public String getMac() {
		return mac;
	}

	/**
	 * Set host's mac address
	 * @param mac
	 */
	
	public void setMac(String mac) {
		this.mac = mac;
	}

	/**
	 * Return nth Ethernet interface's name
	 * @param index of interface
	 * @return name
	 */
	
	public String getEth(int index) {
		return getName() + "-eth" + index;
	}
	
	/**
	 * Convert host to string
	 */
	
	public String toString() {
		return super.toString() + " mac = " + mac;
	}
	
	/**
	 * Write host's attributes into XML document
	 * @param doc
	 * @param config
	 * @return XML element
	 */
	
	public Element toXML(Document doc, Config config) {
		Element elem = super.toXML(doc, config);
		
		// Optional data
		if (mac != null)
			Config.writeXMLString(doc, elem, "mac", mac);
		return elem;
	}
	
	/**
	 * Return name of node's icon file
	 */
	
	public String getImage() {
		if (getTenant() > 0)
			return "img/host-tenant" + getTenant() + ".png";
		return "img/host.png";
	}
	
	/**
	 * Return host's type
	 */
	
	public String getType() {
		return "host";
	}
}
