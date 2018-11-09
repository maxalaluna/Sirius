package net.floodlightcontroller.sirius.tenantconfig;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.VlanVid;

/**
 * This class is responsible for store information about MAC Address, IP Address, VLAN ID 
 * name for each machine (usually virtual) that belongs to one tenant. It is important
 * to emphasize that the equals and hashCode methods had to be changed to work properly
 * 
 */
public class Host {
	private MacAddress mac;
	private VlanVid vlan;
	private IPv4Address ipAddress;
	private String hostName;
	private int tenantId;
	private MacAddress virtualMacAddress = MacAddress.NONE;
	private String virtualHostName;
	private boolean isInUse = false;
	private boolean operational = true;
	private int hostId;
	private Host physicalHost;
	private boolean isDefined = false;
	private int cpu;
	private int memory;
	private int x;
	private int y;
	private HostLocation hostLocation;
	private int typeOfContainer = -1;
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public Host(int hostId, MacAddress mac2, VlanVid vlan2, IPv4Address ipAddress2, String hostName, int tenantId) {
		this.hostId = hostId;
		this.mac = mac2;
		this.vlan = vlan2;
		this.ipAddress = ipAddress2;
		this.hostName = hostName;
		this.tenantId = tenantId;
	}
	
	public Host(int hostId, MacAddress mac2, VlanVid vlan2, IPv4Address ipAddress2, String hostName, int tenantId, int cpu, int memory) {
		this.hostId = hostId;
		this.mac = mac2;
		this.vlan = vlan2;
		this.ipAddress = ipAddress2;
		this.hostName = hostName;
		this.tenantId = tenantId;
		this.cpu = cpu;
		this.memory = memory;
	}
	
	public Host(int hostId, MacAddress mac2, VlanVid vlan2, IPv4Address ipAddress2, String hostName) {
		this.hostId = hostId;
		this.mac = mac2;
		this.vlan = vlan2;
		this.ipAddress = ipAddress2;
		this.hostName = hostName;
	}
	
	public MacAddress getVirtualMacAddress() {
		return virtualMacAddress;
	}

	public void setVirtualMacAddress(MacAddress virtualMacAddress) {
		this.virtualMacAddress = virtualMacAddress;
	}

	public MacAddress getMac() {
		return mac;
	}

	public VlanVid getVlan() {
		return vlan;
	}
	
	public IPv4Address getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(IPv4Address ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	public void setMac(MacAddress mac) {
		this.mac = mac;
	}

	public void setVlan(VlanVid vlan) {
		this.vlan = vlan;
	}

	//Method changed to be considered the attributes of the class in the comparison
	public boolean equals(Object o) {
		return (o instanceof Host) && 
				(mac.equals(((Host) o).mac)) && 
				(vlan.equals(((Host) o).vlan)) &&
				(ipAddress.equals(((Host) o).ipAddress)) &&
				(hostName.equals(((Host) o).hostName)) &&
				(tenantId==(((Host) o).tenantId));
	}
	
	//Method changed to be considered the attributes of the class in the hashCode
	public int hashCode() {
		return mac.hashCode() ^ vlan.hashCode() ^ ipAddress.hashCode() ^ hostName.hashCode() ^ (int)tenantId;
	}

	public String getVirtualHostName() {
		return virtualHostName;
	}

	public void setVirtualHostName(String virtualHostName) {
		this.virtualHostName = virtualHostName;
	}

	public boolean isInUse() {
		return isInUse;
	}

	public void setInUse(boolean isInUse) {
		this.isInUse = isInUse;
	}

	public int getHostId() {
		return hostId;
	}

	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	public Host getPhysicalHost() {
		return physicalHost;
	}

	public void setPhysicalHost(Host physicalHost) {
		this.physicalHost = physicalHost;
	}

	public boolean isDefined() {
		return isDefined;
	}

	public void setDefined(boolean isDefined) {
		this.isDefined = isDefined;
	}

	public HostLocation getHostLocation() {
		return hostLocation;
	}

	public void setHostLocation(HostLocation hostLocation) {
		this.hostLocation = hostLocation;
	}

	public void setOperational(boolean b) {
		this.operational = b;
	}

	public boolean isOperational() {
		return operational;
	}

	public int getTypeOfContainer() {
		return typeOfContainer;
	}

	public void setTypeOfContainer(int typeOfContainer) {
		this.typeOfContainer = typeOfContainer;
	}
}

