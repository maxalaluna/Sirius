package net.floodlightcontroller.sirius.providerconfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

public class SwitchExample3 implements IFloodlightModule, IOFSwitchListener {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchExample3.class);
    private IOFSwitchService switchService;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {

        Collection<Class<? extends IFloodlightService>> deps = new ArrayList<>();
        deps.add(IOFSwitchService.class);
        return deps;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return Collections.emptyList();
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {

        return Collections.emptyMap();
    }

    @Override
    public void init(FloodlightModuleContext cntx) throws FloodlightModuleException {
        switchService = cntx.getServiceImpl(IOFSwitchService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext cntx) throws FloodlightModuleException {
        switchService.addOFSwitchListener(this);
    }

    @Override
    public void switchActivated(DatapathId switchID) {
        IOFSwitch sw = switchService.getActiveSwitch(switchID);
        LOG.info("New switch connected: {} and address: {}", sw.getId(), sw.getInetAddress().toString());
        LOG.info("MANY INFO: {}", sw.getControllerRole() );
    }

    @Override
    public void switchRemoved(DatapathId switchID) {
        LOG.info("Switch {} was disconnected", switchID);
    }

    @Override
    public void switchAdded(DatapathId switchID) {
        // not used
    }

    @Override
    public void switchChanged(DatapathId switchID) {
    	
    	IOFSwitch sw = switchService.getActiveSwitch(switchID);
    	LOG.info("Switch changed: {} and address: {}",sw.getInetAddress().toString(), sw.getAttributes().values());
    }

    @Override
    public void switchPortChanged(DatapathId switchID, OFPortDesc portDesc, PortChangeType changeType) {

       	IOFSwitch sw = switchService.getActiveSwitch(switchID);
    	LOG.info("Switch changed: {} and address: {}",sw.getInetAddress().toString(), sw.getAttributes().values());
    }

	@Override
	public void switchDeactivated(DatapathId switchId) {
		// TODO Auto-generated method stub
		
	}
}