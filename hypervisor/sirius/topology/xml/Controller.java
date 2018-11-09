/**
 * SIRIUS - Testbed platform console 
 * Faculdade de Ciências da Universidade de Lisboa
 * Eric Vial (evial at lasige.di.fc.ul.pt)
 */

package net.floodlightcontroller.sirius.topology.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Controller extends Node {

	private int port;

	public Controller(int id, int index, int x, int y, String name, 
			String ip, int cloud, int cpu, int mem, int port) {
		super(id, index, x, y, name, ip, cloud, cpu, mem, 0, 0);
		this.port = port; 
	}

	/**
	 * Return controller's listening TCP port
	 * @return port
	 */
	
	public int getPort() {
		return port;
	}

	/**
	 * Set controller's listening TCP port
	 * @param port
	 */
	
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Convert controller into string
	 */
	
	public String toString() {
		return super.toString() + " port = " + port;
	}
	
	/**
	 * Write controller's attributes into XML document
	 * @param doc
	 * @param config
	 * @return XML element
	 */
	
	public Element toXML(Document doc, Config config) {
		Element elem = super.toXML(doc, config);
		
		// Optional data
		if (port != config.defaultControllerPort)
			Config.writeXMLInteger(doc, elem, "port", port);
		return elem;
	}
	
	/**
	 * Return name of node's icon file
	 */

	public String getImage() {
		return "img/controller.png";
	}
	
	/**
	 * Return controller's type
	 */
	
	public String getType() {
		return "controller";
	}
}
