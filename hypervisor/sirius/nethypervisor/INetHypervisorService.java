/**
 *    This the definition of the Scapp's routing mapping table Interface
 **/

package net.floodlightcontroller.sirius.nethypervisor;

import java.util.Map;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.tenantconfig.Tenant3;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;

import org.projectfloodlight.openflow.types.OFPort;

public interface INetHypervisorService extends IFloodlightService {
    /**
     * Returns the Scapp's routing mapping table
     * @return The learned Scapp's routing mapping table
     */
	public Map<IOFSwitch, Map<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>>> getTable();
}
