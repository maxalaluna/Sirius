/**
 *    This are the implementation of the ServerResources to Scapp
 **/

package net.floodlightcontroller.sirius.nethypervisor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.tenantconfig.Tenant3;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetHypervisorTable extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(NetHypervisorTable.class);

    protected Map<String, Object> formatTableEntry(Tenant3 tenant, Map<VirtualNetwork, Map<Host, OFPort>> map) {
        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put("tenant", tenant.getId());
        entry.put("virtualNetwork", map.keySet().toString());
//        entry.put("mac", host.getMac().toString());
//        entry.put("port", (map.get(tenant)).get(host).getPortNumber());
        return entry;
    }

    protected List<Map<String, Object>> getOneSwitchTable(Map<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>> map) {
        List<Map<String, Object>> switchTable = new ArrayList<Map<String, Object>>();
        for (Entry<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>> entry : map.entrySet()) {	
        	switchTable.add(formatTableEntry(entry.getKey(), entry.getValue()));
        }
        return switchTable;
    }

    @Get("json")
    public Map<String, List<Map<String, Object>>> getSwitchTableJson() {
        INetHypervisorService lsp =
                (INetHypervisorService)getContext().getAttributes().
                    get(INetHypervisorService.class.getCanonicalName());

        Map<IOFSwitch, Map<Tenant3, Map<VirtualNetwork, Map<Host, OFPort>>>> table = lsp.getTable();    
        Map<String, List<Map<String, Object>>> allSwitchTableJson = new HashMap<String, List<Map<String, Object>>>();

        String switchId = (String) getRequestAttributes().get("switch");
        if (switchId.toLowerCase().equals("all")) {
            for (IOFSwitch sw : table.keySet()) {
            	allSwitchTableJson.put(sw.getId().toString(), getOneSwitchTable(table.get(sw)));
            }
        } else {
            try {
                IOFSwitchService switchService =
                        (IOFSwitchService) getContext().getAttributes().
                            get(IOFSwitchService.class.getCanonicalName());
                IOFSwitch sw = switchService.getSwitch(DatapathId.of(switchId));
                allSwitchTableJson.put(sw.getId().toString(), getOneSwitchTable(table.get(sw)));
            } catch (NumberFormatException e) {
                log.error("Could not decode switch ID = " + switchId);
                setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        }
        return allSwitchTableJson;
    }
}
