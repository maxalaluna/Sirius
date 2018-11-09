/**
 * SIRIUS - Testbed platform console 
 * Faculdade de Ciências da Universidade de Lisboa
 * Eric Vial (evial at lasige.di.fc.ul.pt)
 */

package net.floodlightcontroller.sirius.topology.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Link {

	private int id;
	private int from;
	private int to;
	private int band;
	private int delay;
	private int loss;
	private int security;
	private int tenant;
	private int oldUsed;
	private int newUsed;
	private boolean enable;
	private boolean betweenSwitches;
	private String route;
	
	public Link(int id, int from, int to, int band, int delay, 
			int loss, int security, String route) {
		this.id = id;
		this.from = from;
		this.to = to;
		this.band = band;
		this.delay = delay;
		this.loss = loss;
		this.security = security;
		this.tenant = 0;
		this.oldUsed = 0;
		this.newUsed = 0;
		this.route = route;
		this.enable = true;
		this.betweenSwitches = false;
	}
	
	/**
	 * Return link's id
	 * @return
	 */
	
	public int getId() {
		return id;
	}

	/**
	 * Set link's id
	 * @param id
	 */
	
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Return node'id of first link's end
	 * @return node's id
	 */
	
	public int getFrom() {
		return from;
	}

	/**
	 * Set node's if of first link's end
	 * @param from
	 */
	
	public void setFrom(int from) {
		this.from = from;
	}

	/**
	 * Return node's of second link's end
	 * @return node's id
	 */
	
	public int getTo() {
		return to;
	}

	/**
	 * Set node's if of second link's end
	 * @param to
	 */
	
	public void setTo(int to) {
		this.to = to;
	}

	/**
	 * Return link's bandwidth in Mbs 
	 * @return
	 */
	
	public int getBand() {
		return band;
	}

	/**
	 * Set link's bandwidth in Mbs
	 * @param band
	 */
	
	public void setBand(int band) {
		this.band = band;
	}

	/**
	 * Return link's status
	 * @return true if enabled
	 */
	
	public boolean isEnable() {
		return enable;
	}

	/**
	 * Set link's status
	 * @param enable
	 */
	
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	/**
	 * Return the link's tenant
	 * 0 is no tenant assigned
	 * @return tenant
	 */
	public int getTenant() {
		return tenant;
	}

	/**
	 * Set the link's tenant
	 * 0 if no tenant associated
	 * @param tenant
	 */
	
	public void setTenant(int tenant) {
		this.tenant = tenant;
	}

	/**
	 * Return comma-separated string containing ids of the  
	 * physical links the virtual link is composed of
	 * Unused for physical links
	 * Return null if no route assigned
	 * @return string
	 */
	
	public String getRoute() {
		return route;
	}

	/**
	 * Set the comma-separated string containing ids of the  
	 * physical links the virtual link is composed of
	 * @param string
	 */
	
	public void setRoute(String route) {
		this.route = route;
	}
	
	/**
	 * Return whether the link is between two switches
	 * @return
	 */
	
	public boolean isBetweenSwitches() {
		return betweenSwitches;
	}

	/**
	 * Set whether the link is between two switches
	 * @param betweenSwitches
	 */
	
	public void setBetweenSwitches(boolean betweenSwitches) {
		this.betweenSwitches = betweenSwitches;
	}

	/**
	 * Return link's delay in ms
	 * @return delay
	 */
	
	public int getDelay() {
		return delay;
	}

	/**
	 * Set link's delay in ms
	 * @param delay
	 */
	
	public void setDelay(int delay) {
		this.delay = delay;
	}

	/**
	 * Return link's loss rate 
	 * @return loss rate between 0 and 100 
	 */
	
	public int getLoss() {
		return loss;
	}

	/**
	 * Set link's loss rate
	 * @param loss
	 */
	
	public void setLoss(int loss) {
		this.loss = loss;
	}

	/**
	 * Return link's security level 
	 * @return level
	 */
	
	public int getSecurity() {
		return security;
	}

	/**
	 * Set link's security level
	 * @param security
	 */
	
	public void setSecurity(int security) {
		this.security = security;
	}

	/**
	 * Return whether the link was used before for a route
	 * Only available for physical link
	 * @return
	 */
	
	public int getOldUsed() {
		return oldUsed;
	}

	/**
	 * Set whether the link was used before for a route
	 * Only available for physical link
	 * @param used
	 */
	
	public void setOldUsed(int oldUsed) {
		this.oldUsed = oldUsed;
	}

	/**
	 * Return whether the link is used now for a route
	 * Only available for physical link
	 * @return
	 */
	
	public int getNewUsed() {
		return newUsed;
	}

	/**
	 * Set whether the link is used now for a route
	 * Only available for physical link
	 * @param used
	 */
	
	public void setNewUsed(int newUsed) {
		this.newUsed = newUsed;
	}

	/**
	 * Return link's label
	 * @return
	 */
	
	public String getTitle() {
		return band + "Mbs / " + delay + "ms / " 
				+ loss + "% [" + id + "]";
	}
	
	/**
	 * Write link's attributes into XML document
	 * @param doc
	 * @param config
	 * @return XML element
	 */
	
	public Element toXML(Document doc, Config config) {
		Element elem = doc.createElement("link");
		elem.setAttribute("id", Integer.toString(id));
		Config.writeXMLInteger(doc, elem, "from", from);
		Config.writeXMLInteger(doc, elem, "to", to);
		
		// Optional data
		if (band != config.defaultLinkBand)
			Config.writeXMLInteger(doc, elem, "band", band);
		if (delay != config.defaultLinkDelay)
			Config.writeXMLInteger(doc, elem, "delay", delay);
		if (loss != config.defaultLinkLoss)
			Config.writeXMLInteger(doc, elem, "loss", loss);
		if (security != config.defaultLinkSecurity)
			Config.writeXMLInteger(doc, elem, "secure", security);
		if (route != null)
			Config.writeXMLString(doc, elem, "route", route);
		return elem;
	}
}
