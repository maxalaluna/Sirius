/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.sirius.learningswitchtenant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.types.MacVlanPair;
import net.floodlightcontroller.sirius.environmentoftenants.Tenant;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearningSwitchTableTenant extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(LearningSwitchTableTenant.class);

//    protected Map<String, Object> formatTableEntry(MacVlanPair key, OFPort port) {
    protected Map<String, Object> formatTableEntry(Tenant tenant, Map<MacVlanPair, OFPort> map) {
        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put("mac", tenant.getMacList().toString());
//        entry.put("vlan", tenant.vlan.getVlan());
        entry.put("port", map.get(tenant).getPortNumber());
        return entry;
    }

//    protected List<Map<String, Object>> getOneSwitchTable(Map<MacVlanPair, OFPort> switchMap) {
    protected List<Map<String, Object>> getOneSwitchTable(Map<Tenant, Map<MacVlanPair, OFPort>> map) {
        List<Map<String, Object>> switchTable = new ArrayList<Map<String, Object>>();
        for (Entry<Tenant, Map<MacVlanPair, OFPort>> entry : map.entrySet()) {
//            switchTable.add(formatTableEntry(entry.getKey(), entry.getValue()));
        	switchTable.add(formatTableEntry(entry.getKey(), entry.getValue()));
        }
        return switchTable;
    }

    @Get("json")
    public Map<String, List<Map<String, Object>>> getSwitchTableJson() {
        ILearningSwitchServiceTenant lsp =
                (ILearningSwitchServiceTenant)getContext().getAttributes().
                    get(ILearningSwitchServiceTenant.class.getCanonicalName());

//      Map<IOFSwitch, Map<MacVlanPair, OFPort>> table = lsp.getTable();
        Map<IOFSwitch, Map<Tenant, Map<MacVlanPair, OFPort>>> table = lsp.getTable();    
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
