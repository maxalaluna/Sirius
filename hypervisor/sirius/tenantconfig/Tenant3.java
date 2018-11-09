package net.floodlightcontroller.sirius.tenantconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;

/**
 * This class is the one that have the main information about one tenant in the system.
 * The most important attribute is the ArrayList of IpMacVlanPair3, because the tenant 
 * isolation is base on that. 
 * 
 */
public class Tenant3 {

	protected String description;
	protected int tenantId;
	protected ArrayList<Host> hostList = new ArrayList<Host>();
	private long MAX_TENANT_FLOW_SIZE = 0;
	private HashMap<Integer, VirtualNetwork> virtualNetworkList = new HashMap<Integer, VirtualNetwork>();
	private static final AtomicInteger count = new AtomicInteger(0);

	public Tenant3(String description, ArrayList<Host> hostList) {
		super();
		this.description = description;
		this.tenantId = count.incrementAndGet();
		this.hostList = hostList;
	}

	public Tenant3(String description, ArrayList<Host> hostList,
			long mAX_TENANT_FLOW_SIZE) {
		super();
		this.description = description;
		this.tenantId = count.incrementAndGet();
		this.hostList = hostList;
		MAX_TENANT_FLOW_SIZE = mAX_TENANT_FLOW_SIZE;
	}

	public Tenant3(String description, ArrayList<Host> hostList,
			long mAX_TENANT_FLOW_SIZE,
			HashMap<Integer, VirtualNetwork> virtualNetworkList) {
		super();
		this.description = description;
		this.tenantId = count.incrementAndGet();
		this.hostList = hostList;
		MAX_TENANT_FLOW_SIZE = mAX_TENANT_FLOW_SIZE;
		this.virtualNetworkList = virtualNetworkList;
	}

	public Tenant3(String description) {
		super();
		this.description = description;
		this.tenantId = count.incrementAndGet();
	}
	
	public Tenant3(String description, int tenantId) {
		super();
		this.description = description;
		this.tenantId = tenantId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getId() {
		return tenantId;
	}

	public void setId(int id) {
		this.tenantId = id;
	}

	public ArrayList<Host> getHostList() {
		return hostList;
	}

	public void setHostList(ArrayList<Host> hostList) {
		this.hostList = hostList;
	}

	public long getMAX_TENANT_FLOW_SIZE() {
		return MAX_TENANT_FLOW_SIZE;
	}

	public void setMAX_TENANT_FLOW_SIZE(long mAX_TENANT_FLOW_SIZE) {
		MAX_TENANT_FLOW_SIZE = mAX_TENANT_FLOW_SIZE;
	}

	public HashMap<Integer, VirtualNetwork> getVirtualNetworkList() {
		return virtualNetworkList;
	}

	public void setVirtualNetworkList(HashMap<Integer, VirtualNetwork> virtualNetworkList) {
		this.virtualNetworkList = virtualNetworkList;
	}

	@Override
	public int hashCode() {

		return  (this.description.hashCode() ^ (int)this.tenantId ^
				this.hostList.hashCode() ^	(int)MAX_TENANT_FLOW_SIZE ^ 
				this.virtualNetworkList.hashCode());
	}

	@Override
	public boolean equals(Object o) {

		return (o instanceof Tenant3) && 
				(description.equals(((Tenant3) o).description)) &&
				(tenantId ==((Tenant3) o).tenantId) &&
				(hostList.equals(((Tenant3) o).hostList)) &&
				(MAX_TENANT_FLOW_SIZE ==((Tenant3) o).MAX_TENANT_FLOW_SIZE) &&
				(virtualNetworkList.equals(((Tenant3) o).virtualNetworkList));

	}
}
