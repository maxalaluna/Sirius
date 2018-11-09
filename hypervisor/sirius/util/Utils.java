package net.floodlightcontroller.sirius.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.tenantconfig.EnvironmentOfTenants3;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.tenantconfig.HostLocation;
import net.floodlightcontroller.sirius.tenantconfig.Tenant3;
import net.floodlightcontroller.sirius.tenantconfig.TenantVirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualNetwork;
import net.floodlightcontroller.sirius.tenantconfig.topology.VirtualSwitch;
import net.floodlightcontroller.sirius.topology.FlowEntry;

public class Utils {

	public final static String jpeg = "jpeg";
	public final static String jpg = "jpg";
	public final static String gif = "gif";
	public final static String tiff = "tiff";
	public final static String tif = "tif";
	public final static String png = "png";
	public final static String txt = "txt";
	public final static String conf = "conf";
	public final static String xml = "xml";
	protected static ArrayList<String> letterArrayList = new ArrayList<String>();

	protected static Logger log = LoggerFactory.getLogger(Utils.class);


	public static String getLetterByPosition(int position){

		return letterArrayList.get(position);
	}

	public static ArrayList<String> getLetterArrayList() {
		return letterArrayList;
	}

	public static void setLetterArrayList(ArrayList<String> letterArrayList) {
		Utils.letterArrayList = letterArrayList;
	}

	public static String convertToAlphabet(String number) {
		int n = Integer.parseInt(number);
		int mod = 0, tmp = n;

		String res = "";

		if(tmp == 0)
			return "A";

		while (tmp != 0) {
			mod = tmp % 26;
			res = ((char) (65 + mod)) + res;
			tmp /= 26;
		}

		return res;
	}

	public static int convertFromAlphabet(String word) {
		int result = 0, power = 0, mantissa = 0;

		for (int i = word.length() - 1; i >= 0; i--) {
			mantissa = (int)word.charAt(i) - 65;
			result += mantissa * Math.pow(26, power++);
		}

		return result;
	}


	/*
	 * Get the extension of a file.
	 */  
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
	public static boolean isConfigFile(String extension) {

		if (extension != null) {
			if (extension.equals(Utils.conf) || extension.equals(Utils.xml)) {
				return true;
			} else {
				return false;
			}
		} 
		return false;
	}

	public static boolean isImageFile(String extension) {

		if (extension != null) {
			if (extension.equals(Utils.tiff) ||
					extension.equals(Utils.tif) ||
					extension.equals(Utils.gif) ||
					extension.equals(Utils.jpeg) ||
					extension.equals(Utils.jpg) ||
					extension.equals(Utils.png)) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}
	//Sirius Cookie id == 0x30
	public static boolean isSiriusCookie(long cookie){

		if((cookie & 0xFF00000000000000L) == 3458764513820540928L){

			return true;
		}else{

			return false;
		}
	}

	public static long tenantIdFromCookie(long cookie){

		if(isSiriusCookie(cookie)){

			long tenantId = ((cookie - 3458764513820540928L) & 0xFFFF0000000000L)/1099511627776L;
			return tenantId;
		}
		return 0L;
	}

	public static long switchIdFromCookie(long cookie){

		if(isSiriusCookie(cookie)){

			long tenantId = ((cookie - 3458764513820540928L) & 0xFFFF0000000000L)/1099511627776L;
			long virtualSwitchId = ((cookie - 3458764513820540928L - 1099511627776L*tenantId) & 0xFFFFF00000L)/1048576L;
			return virtualSwitchId;
		}
		return 0L;
	}

	public static long flowIdFromCookie(long cookie){

		if(isSiriusCookie(cookie)){

			long tenantId = ((cookie - 3458764513820540928L) & 0xFFFF0000000000L)/1099511627776L;
			long virtualSwitchId = ((cookie - 3458764513820540928L - 1099511627776L*tenantId) & 0xFFFFF00000L)/1048576L;
			long flowId = (cookie - 3458764513820540928L - 1099511627776L*tenantId - 1048576L* virtualSwitchId);
			return flowId;
		}
		return 0L;
	}

	public static long calculatesCookie(long tenantId, VirtualSwitch virtualSwitch, int flowId){

		//int flowId = virtualSwitch.getFlowTable().get(0).getLastFlowEntry() + 1;

		if (tenantId < 65536 && virtualSwitch.getVirtualSwitchId() < 1048576 && flowId < 1048576){

			return ( 3458764513820540928L+ tenantId*1099511627776L + virtualSwitch.getVirtualSwitchId()*1048576L + flowId);

		} else{
			return 0L;
		}
	}

	public static long calculatesCookieWithoutFlowId(long tenantId, VirtualSwitch virtualSwitch){

		if (tenantId < 65536 && virtualSwitch.getVirtualSwitchId() < 1048576){

			return ( 3458764513820540928L+ tenantId*1099511627776L + virtualSwitch.getVirtualSwitchId()*1048576L);

		} else{
			return 0L;
		}
	}

	public static long calculatesCookieWithoutFlowId(long tenantId){

		if (tenantId < 65536 ){

			return ( 3458764513820540928L+ tenantId*1099511627776L );

		} else{
			return 0L;
		}
	}



	/**
	 * Runs the model to embed a VN onto a SN.
	 * @param datFile Input file for the model with all the necessary info of the networks (parameters, sets, ...)
	 * @param modFile File with the model
	 * @param outputFile File with the result of the execution. The values of all variables are all here, as well as the cost of embedding and execution time
	 * @param timeout Period time during which the model is executed
	 * @return true is timeout was not reached (The execution of the model terminated); false otherwise
	 */
	public static boolean runGLPSOL(String datFile, String modFile, String outputFile, int timeout) {		

		try {
			ProcessBuilder builder = new ProcessBuilder("glpsol","--model", modFile, "--data", datFile);
			builder.redirectOutput(new File(outputFile));

			Process p = builder.start();

			long now = System.currentTimeMillis(); 
			long timeoutInMillis = 1000L * timeout; 
			long finish = now + timeoutInMillis; 

			// Check if the timeout was reached or not
			while(isAlive(p)){

				Thread.sleep(10);

				if (System.currentTimeMillis() > finish){
					System.out.println("[Error] Timeout excedeed!");
					p.destroy();
					return false;
				}
			}

		}catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * Check if a process p has terminated
	 * @param p A process p
	 * @return true if p is executing, false otherwise.
	 */
	private static boolean isAlive(Process p) {  
		try{  
			p.exitValue();  
			return false;  
		}catch (IllegalThreadStateException e) {  
			return true;  
		}  
	}

	/**
	 * Formats the string to only return the name of the file whitout the extension
	 * @param file The full name of the file
	 * @return the name of the file without the extension
	 */
	public static String fileNameWithoutExt(String file) {
		int lastIndexDot = file.lastIndexOf('.');
		return file.substring(0, lastIndexDot); 
	}

	public static Match createMatch(OFPort inPort, OFPort outPort, VlanVid vlan, MacAddress srcMACAddress,
			MacAddress dstMACAddress, IPv4Address srcIPAddress, IPv4Address dstIPAddress, 
			IOFSwitch swSrc) {

		Match.Builder mb = swSrc.getOFFactory().buildMatch();
		mb.setExact(MatchField.ETH_SRC, srcMACAddress)                         
		.setExact(MatchField.ETH_DST, dstMACAddress)      
		.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_TYPE, EthType.IPv4)
		.setExact(MatchField.IPV4_SRC, srcIPAddress)
		.setExact(MatchField.IPV4_DST, dstIPAddress)
		;
		return mb.build();
	}

	public static Match createMatchARP(OFPort inPort, OFPort outPort, VlanVid vlan, MacAddress srcMACAddress,
			MacAddress dstMACAddress, IPv4Address srcIPAddress, IPv4Address dstIPAddress, 
			IOFSwitch swSrc) {

		Match.Builder mb = swSrc.getOFFactory().buildMatch();
		mb.setExact(MatchField.ETH_SRC, srcMACAddress)                         
		.setExact(MatchField.ETH_DST, dstMACAddress)      
		.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_TYPE, EthType.ARP)
		.setExact(MatchField.ARP_TPA, dstIPAddress)
		.setExact(MatchField.ARP_SPA, srcIPAddress)
		;
		return mb.build();
	}

	public static Match createMatchFromPacket(IOFSwitch sw, OFPort inPort, FloodlightContext cntx) {

		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		VlanVid vlan = VlanVid.ofVlan(eth.getVlanID());
		MacAddress srcMac = eth.getSourceMACAddress();
		MacAddress dstMac = eth.getDestinationMACAddress();

		if (srcMac == null) {
			srcMac = MacAddress.NONE;
		}
		if (dstMac == null) {
			dstMac = MacAddress.NONE;
		}
		if (vlan == null) {
			vlan = VlanVid.ZERO;
		}

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_SRC, srcMac)
		.setExact(MatchField.ETH_DST, dstMac);

		if (!vlan.equals(VlanVid.ZERO)) {
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
		}
		return mb.build();
	}
	public static Match createMatchFromPacket(IOFSwitch sw, OFPort inPort, VlanVid vlan, MacAddress srcMac, MacAddress dstMac) {

		if (srcMac == null) {
			srcMac = MacAddress.NONE;
		}
		if (dstMac == null) {
			dstMac = MacAddress.NONE;
		}
		if (vlan == null) {
			vlan = VlanVid.ZERO;
		}

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_SRC, srcMac)
		.setExact(MatchField.ETH_DST, dstMac);

		if (!vlan.equals(VlanVid.ZERO)) {
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
		}
		return mb.build();
	}

	public static Match createMatchFromFieldsDirect(MacAddress srcMac, MacAddress dstMac, OFPort inPort, IPv4Address ipAddressDst, IPv4Address ipAddressSource, Match m){

		Match.Builder mb = m.createBuilder();
		mb.setExact(MatchField.ETH_SRC, srcMac)                         
		.setExact(MatchField.ETH_DST, dstMac)     
		.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_TYPE, EthType.ARP)
		.setExact(MatchField.ARP_TPA, ipAddressDst)
		.setExact(MatchField.ARP_SPA, ipAddressSource)
		;
		if (m.get(MatchField.VLAN_VID) != null) {
			mb.setExact(MatchField.VLAN_VID, m.get(MatchField.VLAN_VID));
		}
		return mb.build();
	}

	public static Match createMatchFromFieldsReverse(OFPort inPort, IPv4Address ipAddressDst, IPv4Address ipAddressSource, Match m, IOFSwitch sw, MacAddress srcVirtualMacAddress, MacAddress dstVirtualMacAddress){

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.ETH_SRC, dstVirtualMacAddress)                         
		.setExact(MatchField.ETH_DST, srcVirtualMacAddress)      
		.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_TYPE, EthType.ARP)
		.setExact(MatchField.ARP_TPA, ipAddressSource)
		.setExact(MatchField.ARP_SPA, ipAddressDst)
		;
		if (m.get(MatchField.VLAN_VID) != null) {
			mb.setExact(MatchField.VLAN_VID, m.get(MatchField.VLAN_VID));
		}
		return mb.build();
	}

	public static Match createMatchFromFieldsDirect(OFPort inPort, MacAddress srcMac, MacAddress dstMac, VlanVid vlan, IOFSwitch sw){

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_SRC, srcMac)
		.setExact(MatchField.ETH_DST, dstMac);

		if (!vlan.equals(VlanVid.ZERO)) {
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
		}
		return mb.build();
	}

	public static Match createMatchFromFieldsDirectARP(OFPort inPort, IPv4Address ipAddressDst, IPv4Address ipAddressSource, Match m, IOFSwitch sw, MacAddress virtualMacAddress){

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.ETH_SRC, m.get(MatchField.ETH_SRC))                         
		.setExact(MatchField.ETH_DST, virtualMacAddress)      
		.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_TYPE, EthType.ARP)
		.setExact(MatchField.ARP_TPA, ipAddressDst)
		.setExact(MatchField.ARP_SPA, ipAddressSource)
		;
		if (m.get(MatchField.VLAN_VID) != null) {
			mb.setExact(MatchField.VLAN_VID, m.get(MatchField.VLAN_VID));
		}
		return mb.build();
	}

	public static Match createMatchFromFieldsDirectARP(OFPort inPort, IPv4Address ipAddressDst, IPv4Address ipAddressSource, Match m, IOFSwitch sw, MacAddress srcVirtualMacAddress, MacAddress dstVirtualMacAddress){

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.ETH_SRC, srcVirtualMacAddress)                         
		.setExact(MatchField.ETH_DST, dstVirtualMacAddress)      
		.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_TYPE, EthType.ARP)
		.setExact(MatchField.ARP_TPA, ipAddressDst)
		.setExact(MatchField.ARP_SPA, ipAddressSource)
		;
		if (m.get(MatchField.VLAN_VID) != null) {
			mb.setExact(MatchField.VLAN_VID, m.get(MatchField.VLAN_VID));
		}
		return mb.build();
	}

	public static Match createMatchFromFieldsReverse(OFPort inPort, MacAddress srcMac, MacAddress dstMac, VlanVid vlan, IOFSwitch sw){

		Match.Builder mb = sw.getOFFactory().buildMatch();
		mb.setExact(MatchField.IN_PORT, inPort)
		.setExact(MatchField.ETH_SRC, dstMac)
		.setExact(MatchField.ETH_DST, srcMac);

		if (!vlan.equals(VlanVid.ZERO)) {
			mb.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan));
		}
		return mb.build();
	}

	public static Match createMatchFromFieldsReverse(OFPort inPort, VlanVid vlan, Match m){

		Match.Builder mb = m.createBuilder();
		mb.setExact(MatchField.ETH_SRC, m.get(MatchField.ETH_DST))                         
		.setExact(MatchField.ETH_DST, m.get(MatchField.ETH_SRC))     
		.setExact(MatchField.IN_PORT, inPort);
		if (m.get(MatchField.VLAN_VID) != null) {
			mb.setExact(MatchField.VLAN_VID, m.get(MatchField.VLAN_VID));
		}
		return mb.build();
	}

	public static synchronized boolean hasFlowEntry(ArrayList<FlowEntry> flowtable, Match match, List<OFAction> al){

		for(int i = 0; i< flowtable.size(); i++){

			if(flowtable.get(i) != null &&
					flowtable.get(i).getMatch() != null && 
					flowtable.get(i).getOfActions() != null &&
					flowtable.get(i).getMatch().equals(match)){
//					flowtable.get(i).getMatch().equals(match) &&
//					flowtable.get(i).getOfActions().equals(al)){

				return true;
			}
		}
		return false;
	}

	public static synchronized long getPartialCookieWritePhysicalFlows(OFPort port, Server3 server, TenantVirtualNetwork tenantVirtualNetwork){

		DatapathId physicalDatapathId = server.getOpenVSwitch().getDatapathId();
		Tenant3 tenant = null;
		boolean canInsertFLowPhysicalSwitch = false;
		long partialCookie = 0L;

		if(physicalDatapathId != null && port != null){

			tenant = tenantVirtualNetwork.getTenant();
		}

		if(server != null && tenant != null && tenantVirtualNetwork.getVirtualNetwork().isActive()){

			canInsertFLowPhysicalSwitch = canInsertFLowPhysicalSwitch(server, tenant);
		}

		if (canInsertFLowPhysicalSwitch){

			server.setFlowSize(server.getFlowSize()+1);

			partialCookie = Utils.calculatesCookieWithoutFlowId(tenant.getId());

			if(partialCookie == 0L){

				log.info("The Flow can't be inserted!");
			}
		}
		return partialCookie;
	}

	public static synchronized long getPartialCookieWriteVirtualAndPhysicalFlows(OFPort port, Server3 server, VirtualSwitch virtualSwitch, EnvironmentOfTenants3 environmentOfTenants){

		DatapathId physicalDatapathId = server.getOpenVSwitch().getDatapathId();
		Tenant3 tenant = null;
		VirtualNetwork virtualNetwork = null;
		boolean canInsertFLowVirtualSwitch = false;
		boolean canInsertFLowPhysicalSwitch = false;
		long partialCookie = 0L;
		HostLocation hostLocation = new HostLocation(physicalDatapathId, port);

		if(physicalDatapathId != null && port != null){
			tenant = environmentOfTenants.getHostLocationTenantMap().get(hostLocation);
		}

		if (tenant != null){
			virtualNetwork = environmentOfTenants.getHostLocationVirtualNetworkMap().get(hostLocation);
		}

		if (virtualNetwork != null && virtualSwitch != null && tenant != null){
			canInsertFLowVirtualSwitch = canInsertFLowVirtualSwitch(virtualNetwork, virtualSwitch, tenant);
		}

		if(server != null && tenant != null){
			canInsertFLowPhysicalSwitch = canInsertFLowPhysicalSwitch(server, tenant);
		}

		if (canInsertFLowPhysicalSwitch && canInsertFLowVirtualSwitch){
			virtualSwitch.setFlowSize(virtualSwitch.getFlowSize()+1);
			server.setFlowSize(server.getFlowSize()+1);
			partialCookie = Utils.calculatesCookieWithoutFlowId(tenant.getId(), virtualSwitch);
			if(partialCookie == 0L){
				log.info("The Flow can't be inserted!");
			}
		}
		return partialCookie;
	}

	private static synchronized boolean canInsertFLowPhysicalSwitch(Server3 server, Tenant3 tenant) {

		if(server != null){
			if(server.getOpenVSwitch() != null){
				if(server.getMAX_FLOW_SIZE() == 0){
					return true;
				} else{
					if(server.getMAX_FLOW_SIZE() >= server.getFlowSize()){
						return true;
					}else{
						if(tenant != null){
							log.warn("Exceeded number of flows in the Physical Switch -{}- of Tenant -{}-", server.getOpenVSwitch().getDatapathId(), tenant.getDescription());
						}
					}
				}
			}
		}
		return false;
	}

	 private static synchronized boolean canInsertFLowVirtualSwitch(VirtualNetwork virtualNetwork, VirtualSwitch virtualSwitch, Tenant3 tenant) {

		if(virtualSwitch != null){
			if((virtualSwitch.getMAX_FLOW_SIZE() == 0) && (virtualNetwork.isActive())){
				return true;
			}else {
				if((virtualSwitch.getMAX_FLOW_SIZE() >= virtualSwitch.getFlowSize()) && (virtualNetwork.isActive())){
					return true;
				} else{
					if(tenant != null && tenant.getDescription() != null && 
							virtualSwitch != null && virtualSwitch.getOpenVSwitch() != null && 
							virtualSwitch.getOpenVSwitch().getDatapathId() != null){
						log.warn("Exceeded number of flows in the Virtual Switch -{}- of Tenant -{}-", virtualSwitch.getOpenVSwitch().getDatapathId(), tenant.getDescription());
					}
				}
			}
		}
		return false;
	}

	public static synchronized MacAddress virtualMacAddress(Host host, TenantVirtualNetwork tenantVirtualNetwork, HashMap<MacAddress, Host> macAddressHostMap, 
			HashMap<Tenant3, Long> tenantTenantMacIdToVirtualMacsMap, HashMap<Tenant3, Long> tenantCurrentHostIdToVirtualMacsMap, long tenantIdMacCount,
			long hostIdMacCount, long virtualMacAddressRaw, EnvironmentOfTenants3 environmentOfTenants){

		Tenant3 tenant = null;

		if(host != null){
			tenant = tenantVirtualNetwork.getTenant();
			MacAddress physicalMacAddress = host.getMac();
			macAddressHostMap.put(physicalMacAddress, host);
			if (tenant != null){
				//In case of the first TenantMacIdToVirtualMacs in a specific Tenant, increment tenantIdMacCount
				if (tenantTenantMacIdToVirtualMacsMap.get(tenant) == null){
					tenantIdMacCount ++;
					tenantTenantMacIdToVirtualMacsMap.put((environmentOfTenants.getTenantIdTenantMap()).get(tenant.getId()), Long.valueOf(tenantIdMacCount));
				}
				if (tenantCurrentHostIdToVirtualMacsMap.get(tenant) == null){
					hostIdMacCount = Long.valueOf(1L);
					tenantCurrentHostIdToVirtualMacsMap.put(tenant, hostIdMacCount);
				} else{
					if (host.getVirtualMacAddress().equals(MacAddress.NONE)){
						hostIdMacCount = tenantCurrentHostIdToVirtualMacsMap.get(tenant) +1;
						tenantCurrentHostIdToVirtualMacsMap.put(tenant, hostIdMacCount);
					}
				}
			}

			if(host.getVirtualMacAddress().equals(MacAddress.NONE)){
				virtualMacAddressRaw = tenantTenantMacIdToVirtualMacsMap.get(tenant)*4294967296L + hostIdMacCount;
				host.setVirtualMacAddress(MacAddress.of(virtualMacAddressRaw));
			}
			return host.getVirtualMacAddress();
		}
		return null;
	}
	public static String concatEdges(String firstEdge, String nextEdge){

		String h = firstEdge;
		String[] switches = firstEdge.split(":");
		String[] s2 = nextEdge.split(":");

		if(switches[switches.length-1].equals(s2[0])){
			for(int i = 1; i < s2.length; i++){
				h = h +":"+s2[i];
			}
		}else{
			if(switches[switches.length-1].equals(s2[s2.length-1])){
				for(int i = s2.length-2; i >= 0; i--){
					h = h +":"+s2[i];
				}
			}
		}
		return h;
	}

	public static String reverseSwitches(String path){

		String h = "";
		String[] p = path.split(":");

		for (int i = p.length -1; i >= 0; i--){

			if(i == p.length -1){
				h = h + p[i];
			}else{
				h = h + ":"+ p[i];
			}
		}
		return h;
	}
}
