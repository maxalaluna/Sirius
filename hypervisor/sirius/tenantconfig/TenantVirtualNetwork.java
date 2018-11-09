package net.floodlightcontroller.sirius.tenantconfig;

import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;

public class TenantVirtualNetwork {

	private Tenant3 tenant;
	private VirtualNetwork virtualNetwork;
	
	public TenantVirtualNetwork(Tenant3 tenant, VirtualNetwork virtualNetwork) {
		super();
		this.tenant = tenant;
		this.virtualNetwork = virtualNetwork;
	}
	
	public Tenant3 getTenant() {
		return tenant;
	}
	public void setTenant(Tenant3 tenant) {
		this.tenant = tenant;
	}
	public VirtualNetwork getVirtualNetwork() {
		return virtualNetwork;
	}
	public void setVirtualNetwork(VirtualNetwork virtualNetwork) {
		this.virtualNetwork = virtualNetwork;
	}
}
