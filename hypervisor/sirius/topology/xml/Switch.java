/**
 * SIRIUS - Testbed platform console 
 * Faculdade de Ciências da Universidade de Lisboa
 * Eric Vial (evial at lasige.di.fc.ul.pt)
 */

package net.floodlightcontroller.sirius.topology.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Switch extends Node {

	private String dpid;
	private int version;
	private int flows;
	private int security;
	private int dependability;
	private int cloudType;
	
	public Switch(int id, int index, int x, int y, String name, String ip, 
			int cloud, int cpu, int mem, String dpid, int version, int flows, 
			int security, int dependability, int cloudType, int tenant, int mapping) {
		super(id, index, x, y, name, ip, cloud, cpu, mem, tenant, mapping);
		this.version = version;
		this.flows = flows;
		this.security = security;
		this.dependability = dependability;
		this.cloudType = cloudType;;
		this.dpid = dpid;
	}

	/**
	 * Return switch's datapath id
	 * Return null if dpif no assigned yet
	 * @return dpid
	 */
	
	public String getDpid() {
		return dpid;
	}

	/**
	 * Set switch's datapath id
	 * @param dpid
	 */
	
	public void setDpid(String dpid) {
		this.dpid = dpid;
	}

	/**
	 * Return version of openflow protocol implemented in switch
	 * 0 for OF1.0, 1 for OF1.1 and so on
	 * @return version
	 */
	
	public int getVersion() {
		return version;
	}

	/**
	 * Set version of openflow protocol implemented in switch
	 * @param version
	 */
	
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Return switch's maximum number of flows
	 * @return flows
	 */
	
	public int getFlows() {
		return flows;
	}

	/**
	 * Set switch's maximum number of flows
	 * @param flows
	 */
	
	public void setFlows(int flows) {
		this.flows = flows;
	}

	/**
	 * Return switch's security level
	 * @return level
	 */
	
	public int getSecurity() {
		return security;
	}

	/**
	 * Set switch's security level
	 * @param security
	 */
	
	public void setSecurity(int security) {
		this.security = security;
	}

	/**
	 * Return switch's dependability level
	 * @return level
	 */
	
	public int getDependability() {
		return dependability;
	}

	/**
	 * Set switch's dependability level
	 * @param depend
	 */
	
	public void setDependability(int depend) {
		this.dependability = depend;
	}

	/**
	 * Return type of cloud
	 * @return type: 0, 1 or 2
	 */
	
	public int getCloudType() {
		return cloudType;
	}

	/**
	 * Set type of cloud
	 * @param cloudType
	 */
	
	public void setCloudType(int cloudType) {
		this.cloudType = cloudType;
	}
	
	/**
	 * Return nth Ethernet interface's name
	 * @param index of interface
	 * @return name
	 */
	
	public String getEth(int index) {
		return getName() + "-eth" + (index + 1);
	}
	
	/**
	 * Convert switch to string
	 */
	
	public String toString() {
		return super.toString() + " version = " + version + " flows = " + flows 
				+ " security = " + security + " dependability = " + dependability 
				+ " cloudType = " + cloudType + " dpid = " + dpid;
	}
	
	/**
	 * Write switch's attributes into XML document
	 * @param doc
	 * @param config
	 * @return XML element
	 */
	
	public Element toXML(Document doc, Config config) {
		Element elem = super.toXML(doc, config);
		
		// Optional data
		if (dpid != null)
			Config.writeXMLString(doc, elem, "dpid", dpid);
		if (version != config.defaultOpenFlowVersion) 
			Config.writeXMLInteger(doc, elem, "version", version);
		if (flows != config.defaultFlowsMaximum)
			Config.writeXMLInteger(doc, elem, "flows", flows);
		if (security != config.defaultNodeSecurity)
			Config.writeXMLInteger(doc, elem, "secure", security);
		if (dependability != config.defaultNodeDependability)
			Config.writeXMLInteger(doc, elem, "depend", dependability);
		if (cloudType != config.defaultCloudType)
			Config.writeXMLInteger(doc, elem, "trust", cloudType);
		return elem;
	}

	/**
	 * Return name of node's icon file
	 */
	
	public String getImage() {
		if (getTenant() > 0)
			return "img/switch-tenant" + getTenant() + ".png";
		return "img/switch.png";
	}
	
	/**
	 * Return switch's type
	 */
	
	public String getType() {
		return "switch";
	}
}
