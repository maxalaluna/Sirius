/**
 * This class was create to allow the creation of one hashMap to do a link between a port in a switch (DatapathIdPort3) 
 * and a MAC Address, IP Address and VLAN ID (IpMacVlanPair3). It is used to verify which address is in one port, insert
 * the proper flow in a switch and handle dynamically the structures in the hypervisor based in port changed status.
 *  
 */

package net.floodlightcontroller.sirius.tenantconfig;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

public class DatapathIdPort3 {
	
	protected DatapathId datapathId;
	protected OFPort port;	
	
	public DatapathIdPort3(DatapathId datapathId, OFPort port) {
		super();
		this.datapathId = datapathId;
		this.port = port;
	}
		
	public DatapathId getDatapathId() {
		return datapathId;
	}
	public void setDatapathId(DatapathId datapathId) {
		this.datapathId = datapathId;
	}
	public OFPort getPort() {
		return port;
	}
	public void setPort(OFPort port) {
		this.port = port;
	}
	
	//Method changed to be considered the attributes of the class in the comparison
	public boolean equals(Object o) {
		return (o instanceof DatapathIdPort3) && 
				(datapathId.equals(((DatapathIdPort3) o).datapathId)) && 
				(datapathId.equals(((DatapathIdPort3) o).datapathId));
	}

	//Method changed to be considered the attributes of the class in the hashCode
	public int hashCode() {
		return datapathId.hashCode() ^ port.hashCode();
	}

}
