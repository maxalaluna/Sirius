package net.floodlightcontroller.sirius.providerconfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.internal.LinkInfo;
import net.floodlightcontroller.routing.Link;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.util.LRULinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkHandle3 implements IFloodlightModule, ILinkDiscoveryListener {

    private static final Logger log = LoggerFactory.getLogger(LinkHandle3.class);

    private ILinkDiscoveryService linkDiscService;

    protected Map<DatapathIdSwitchesSrcDst3,Link> datapathIdSwitchSrcSwitchDstLinkMap = 
    	Collections.synchronizedMap(new LRULinkedHashMap<DatapathIdSwitchesSrcDst3, Link>(10000));
    	//Collections.synchronizedMap(new LRULinkedHashMap<IpMacVlanPair2, OFPort>(MAX_MACS_PER_SWITCH));

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {

        Collection<Class<? extends IFloodlightService>> deps = new ArrayList<>();
        deps.add(ILinkDiscoveryService.class);
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

        linkDiscService = cntx.getServiceImpl(ILinkDiscoveryService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext cntx) throws FloodlightModuleException {

        linkDiscService.addListener(this);
    }

    @Override
    public void linkDiscoveryUpdate(List<LDUpdate> updates) {

        for (LDUpdate updt : updates) {
            linkDiscoveryUpdate(updt);
        }
    }

    public void linkDiscoveryUpdate(LDUpdate updt) {

        switch (updt.getOperation()) {
            case LINK_UPDATED: {
                Link link = new Link(updt.getSrc(), updt.getSrcPort(), updt.getDst(), updt.getDstPort(),U64.ZERO);
                linkUpdated(link);
                break;
            }
            case LINK_REMOVED: {
                Link link = new Link(updt.getSrc(), updt.getSrcPort(), updt.getDst(), updt.getDstPort(),U64.ZERO);
                linkRemoved(link);
                break;
            }
            default: {
                // we don't care about other event types
                break;
            }
        }
    }

    private void linkUpdated(Link link) {

        LinkInfo linkInfo = linkDiscService.getLinkInfo(link);
 
		Link l = new Link();
		if (linkInfo != null) {
			l.setSrc(link.getSrc());
			l.setSrcPort(link.getSrcPort());
			l.setDst(link.getDst());
			l.setDstPort(link.getDstPort());
			this.datapathIdSwitchSrcSwitchDstLinkMap.put(new DatapathIdSwitchesSrcDst3(l.getSrc(), l.getDst()), l);
        }
    }

    private void linkRemoved(Link link) {

        log.info("Link {} was removed", link);
    }
}

