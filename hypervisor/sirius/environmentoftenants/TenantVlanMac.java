package net.floodlightcontroller.sirius.environmentoftenants;

import java.util.Map;
import net.floodlightcontroller.core.types.MacVlanPair;
import org.projectfloodlight.openflow.types.OFPort;

public class TenantVlanMac {
	
	protected Tenant tenant;
	protected Map<MacVlanPair, OFPort> macVlanToTenantPortMap;
	
	public TenantVlanMac(Tenant tenant,
			Map<MacVlanPair, OFPort> macVlanToTenantPortMap) {
		super();
		this.tenant = tenant;
		this.macVlanToTenantPortMap = macVlanToTenantPortMap;
	}
	
	public Tenant getTenant() {
		return tenant;
	}
	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	
	public Map<MacVlanPair, OFPort> getMacVlanToTenantPortMap() {
		return macVlanToTenantPortMap;
	}
	public void setMacVlanToTenantPortMap(
			Map<MacVlanPair, OFPort> macVlanToTenantPortMap) {
		this.macVlanToTenantPortMap = macVlanToTenantPortMap;
	}

}
