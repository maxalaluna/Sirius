package net.floodlightcontroller.sirius.environmentoftenants;

import java.util.ArrayList;

import org.projectfloodlight.openflow.types.MacAddress;

import net.floodlightcontroller.sirius.environmentofservers.Server;

public class EnvironmentOfTenants {
	
	
	byte[] address1 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)1};
	byte[] address2 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)2};
	byte[] address3 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)3};
	byte[] address4 = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0,(byte)4};

	Tenant tenant1 = new Tenant("Tenant 1", 1, null);
	Tenant tenant2 = new Tenant("Tenant 2", 2, null);

	ArrayList<MacAddress> macList1 = new ArrayList<MacAddress>();
	ArrayList<MacAddress> macList2 = new ArrayList<MacAddress>();

	ArrayList<Tenant> tenantList = new ArrayList<Tenant>();

	
	public EnvironmentOfTenants() {
		super();
	}


	public ArrayList<Tenant> getTenantList() {
		return tenantList;
	}


	public void setTenantList(ArrayList<Tenant> tenantList) {
		this.tenantList = tenantList;
	}


	public void initEnvironmentOfTenants() {
		
		this.macList1.add(MacAddress.of(address1));
		this.macList1.add(MacAddress.of(address2));

		this.tenant1.setMacList(macList1);

		this.macList2.add(MacAddress.of(address3));
		this.macList2.add(MacAddress.of(address4));

		this.tenant2.setMacList(macList2);

		this.tenantList.add(tenant1);
		this.tenantList.add(tenant2);
		
	}

}
