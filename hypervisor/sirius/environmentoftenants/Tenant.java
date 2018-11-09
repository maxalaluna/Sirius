package net.floodlightcontroller.sirius.environmentoftenants;

import java.util.ArrayList;

import org.projectfloodlight.openflow.types.MacAddress;
//import org.projectfloodlight.openflow.types.OFPort;

public class Tenant {
	
	protected String description;
	protected long id;
	protected ArrayList<MacAddress> macList = new ArrayList<MacAddress>();
	//protected ArrayList<OFPort> inPort = new ArrayList<OFPort>();
	
	public Tenant(String description, long id, ArrayList<MacAddress> macList) {
		super();
		this.description = description;
		this.id = id;
		this.macList = macList;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public ArrayList<MacAddress> getMacList() {
		return macList;
	}
	
	public void setMacList(ArrayList<MacAddress> macList) {
		this.macList = macList;
	}
	
}
