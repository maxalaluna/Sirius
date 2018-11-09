package net.floodlightcontroller.sirius.tenantconfig;


import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.VlanVid;

/**
 * This class is responsible for store information about MAC Address, IP Address and 
 * VLAN ID for each machine (usually virtual) that belongs to one tenant. It is important
 * to emphasize that the equals and hashCode methods had to be changed to work properly
 * 
 */
public class IpMacVlanPair3 {
	public MacAddress mac;
	public VlanVid vlan;
	public IPv4Address ipAddress;
	
	public IpMacVlanPair3(MacAddress mac2, VlanVid vlan2, IPv4Address ipAddress2) {
		this.mac = mac2;
		this.vlan = vlan2;
		this.ipAddress = ipAddress2;
	}

	public MacAddress getMac() {
		return mac;
	}

	public VlanVid getVlan() {
		return vlan;
	}

	public IPv4Address getIPAddress() {
		return ipAddress;
	}

	//Method changed to be considered the attributes of the class in the comparison
	public boolean equals(Object o) {
		return (o instanceof IpMacVlanPair3) && 
				(mac.equals(((IpMacVlanPair3) o).mac)) && 
				(vlan.equals(((IpMacVlanPair3) o).vlan)) &&
				(ipAddress.equals(((IpMacVlanPair3) o).ipAddress));
	}
	//Method changed to be considered the attributes of the class in the hashCode
	public int hashCode() {
		return mac.hashCode() ^ vlan.hashCode() ^ ipAddress.hashCode();
	}
}
