package net.floodlightcontroller.sirius.nethypervisor;

import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.VlanVid;

import net.floodlightcontroller.sirius.tenantconfig.EnvironmentOfTenants3;
import net.floodlightcontroller.sirius.tenantconfig.Host;
import net.floodlightcontroller.sirius.tenantconfig.Tenant3;

public class TesteMac {

	public static Map<MacAddress, Host> macAddressHostMap = new HashMap<MacAddress, Host>();
	public static Map<Tenant3, Long> tenantTenantMacIdToVirtualMacsMap = new HashMap<Tenant3, Long>();
	public static Map<Tenant3, Long> tenantCurrentHostIdToVirtualMacsMap = new HashMap<Tenant3, Long>();

	public static EnvironmentOfTenants3  environmentOfTenants3 = new EnvironmentOfTenants3();

	private static long tenantIdMacCount;
	private static long hostIdMacCount;
	private static long virtualMacAddressRaw;

	public static synchronized MacAddress virtualMacAddress(Host host){

		Tenant3 tenant = null;
		tenant = (environmentOfTenants3.getTenantIdTenantMap()).get(Long.valueOf(host.getTenantId()));
		MacAddress physicalMacAddress = host.getMac();
		macAddressHostMap.put(physicalMacAddress, host);
		if (tenant != null){
			//In case of the first TenantMacIdToVirtualMacs in a specific Tenant, increment tenantIdMacCount
			if (tenantTenantMacIdToVirtualMacsMap.get(tenant) == null){
				tenantIdMacCount ++;
				tenantTenantMacIdToVirtualMacsMap.put((environmentOfTenants3.getTenantIdTenantMap()).get(host.getTenantId()), Long.valueOf(tenantIdMacCount));
			}
			if (tenantCurrentHostIdToVirtualMacsMap.get(tenant) == null){
				hostIdMacCount = Long.valueOf(1L);
			} else{
				hostIdMacCount = tenantCurrentHostIdToVirtualMacsMap.get(tenant) +1;
			}
			tenantCurrentHostIdToVirtualMacsMap.put(tenant, hostIdMacCount);
		}
		virtualMacAddressRaw = tenantIdMacCount*4294967296L + hostIdMacCount;
		host.setVirtualMacAddress(MacAddress.of(virtualMacAddressRaw));
		return host.getVirtualMacAddress();
	}

	public static String returnInfoCookie(long cookie){

		String info = new String("");
		long tenantId, switchId, flowId;                       

		if((cookie & 0x3000000000000000L) == 3458764513820540928L){
			info = info.concat("SiriusId: 0x3;");
			tenantId = ((cookie - 3458764513820540928L) & 0xFFFF0000000000L)/1099511627776L;
			info = info.concat(" TenantId: "+tenantId);
			switchId = ((cookie - 3458764513820540928L - 1099511627776L*tenantId) & 0xFFFFF00000L)/1048576L;
			info = info.concat(" SwitchId: "+switchId);
			flowId = (cookie - 3458764513820540928L - 1099511627776L*tenantId - 1048576L* switchId);
			info = info.concat(" FlowId: "+flowId);
			return info;
		}
		return "";
	}

	public boolean isSiriusCookie(long cookie){

		if((cookie & 0x3F00000000000000L) == 3458764513820540928L){
			return true;
		}else{
			return false;
		}
	}

	public long tenantIdFromCookie(long cookie){

		if(isSiriusCookie(cookie)){
			long tenantId = ((cookie - 3458764513820540928L) & 0xFFFF0000000000L)/1099511627776L;
			return tenantId;
		}
		return 0L;
	}

	public long switchIdFromCookie(long cookie){

		if(isSiriusCookie(cookie)){
			long tenantId = ((cookie - 3458764513820540928L) & 0xFFFF0000000000L)/1099511627776L;
			long virtualSwitchId = ((cookie - 3458764513820540928L - 1099511627776L*tenantId) & 0xFFFFF00000L)/1048576L;
			return virtualSwitchId;
		}
		return 0L;
	}

	public long flowIdFromCookie(long cookie){
		
		if(isSiriusCookie(cookie)){
			long tenantId = ((cookie - 3458764513820540928L) & 0xFFFF0000000000L)/1099511627776L;
			long virtualSwitchId = ((cookie - 3458764513820540928L - 1099511627776L*tenantId) & 0xFFFFF00000L)/1048576L;
			long flowId = (cookie - 3458764513820540928L - 1099511627776L*tenantId - 1048576L* virtualSwitchId);
			return flowId;
		}
		return 0L;
	}

	public static long calculatesCookie(long tenantId, long virtualSwitchId, long flowId){

		if(tenantId < 65536 && virtualSwitchId < 1048576 && flowId < 1048576){
			return ( 3458764513820540928L+ tenantId*1099511627776L + virtualSwitchId*1048576L + flowId);
		} else{
			return 0L;
		}
	}

	public static void main(String[] args) {

		environmentOfTenants3.initEnvironmentOfAllTenants();
		
		Host T1Mac1 = new Host(1, MacAddress.of("00:00:00:00:00:01"), VlanVid.ofVlan(0), IPv4Address.of("10.200.0.1"), "T1Mac1", 1 );
		Host T1Mac2 = new Host(2, MacAddress.of("00:00:00:00:00:02"), VlanVid.ofVlan(0), IPv4Address.of("10.200.0.2"), "T1Mac2", 1 );
		Host T1Mac3 = new Host(3, MacAddress.of("00:00:00:00:00:03"), VlanVid.ofVlan(0), IPv4Address.of("10.200.0.3"), "T1Mac3", 1 );
		Host T2Mac4 = new Host(4, MacAddress.of("00:00:00:00:00:04"), VlanVid.ofVlan(0), IPv4Address.of("10.200.0.4"), "T2Mac4", 2 );

		@SuppressWarnings("unused")
		MacAddress vMAC = null;
		vMAC = virtualMacAddress(T1Mac1);
		//System.out.println(vMAC);
		vMAC = virtualMacAddress(T1Mac2);
		//System.out.println(vMAC);
		vMAC = virtualMacAddress(T1Mac3);
		//System.out.println(vMAC);
		vMAC = virtualMacAddress(T2Mac4);
		//System.out.println(vMAC);

		long tenantId = 33L;
		long switchId = 4333L;
		long flowId = 677;
		long cookie = calculatesCookie(tenantId, switchId, flowId);

		System.out.println(cookie);
		String infoCookie = returnInfoCookie(cookie);

		if(!infoCookie.equals("")){

			System.out.println(infoCookie);
		}else{
			System.out.println("Cookie inválido!!!");
		}
		//0x30 0000 00000 00000L
		System.out.println(0x3F00000000000000L);
	}

}
