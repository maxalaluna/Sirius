package net.floodlightcontroller.sirius.topology.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Cloud {

	private int id;
	private int secType;
	private String user;
	private String password;
	private String description;
	
	public Cloud(int id, int type, String user, String password,
			String description) {
		this.id = id;
		this.secType = type;
		this.user = user;
		this.password = password;
		this.description = description;
	}

	/**
	 * Return cloud's id
	 * @return
	 */
	
	public int getId() {
		return id;
	}

	/**
	 * Set cloud's id
	 * @param id
	 */
	
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Return type of cloud
	 * @return type: 0, 1 or 2
	 */
	
	public int getSecType() {
		return secType;
	}

	/**
	 * Set type of cloud
	 * @param trust
	 */
	
	public void setSecType(int type) {
		this.secType = type;
	}

	/**
	 * Return username
	 * @return
	 */
	
	public String getUser() {
		return user;
	}

	/**
	 * Set username
	 * @param user
	 */
	
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Return user's password
	 * @return
	 */
	
	public String getPassword() {
		return password;
	}

	/**
	 * Set user's password
	 * @param password
	 */
	
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Return cloud description
	 * @return
	 */
	
	public String getDescription() {
		return description;
	}

	/**
	 * Set cloud's description
	 * @param description
	 */
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Convert host to string
	 */
	
	public String toString() {
		return "cloud id = " + id + " type = " + secType + " user = " + user 
				+ " password = " + password + " description = " + description;
	}
	
	/**
	 * Write cloud's attributes into XML document
	 * @param doc
	 * @param config
	 * @return XML element
	 */
	
	public Element toXML(Document doc) {
		Element elem = doc.createElement("cloud");
		elem.setAttribute("id", Integer.toString(id));
		Config.writeXMLInteger(doc, elem, "type", secType);
		Config.writeXMLString(doc, elem, "user", user);		
		Config.writeXMLString(doc, elem, "password", password);
		Config.writeXMLString(doc, elem, "desc", description);
		return elem;
	}
	
	public String getType() {
		return "cloud";
	}
}
