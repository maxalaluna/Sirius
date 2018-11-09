package net.floodlightcontroller.sirius.environmentofservers;

import java.util.UUID;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

public class Interfaces {
	
	/*
#ovs-vsctl list interface "s1-eth1"

_uuid               : ee4608db-7c5b-48cb-90d3-98db761b9347
admin_state         : up
bfd                 : {}
bfd_status          : {}
cfm_fault           : []
cfm_fault_status    : []
cfm_health          : []
cfm_mpid            : []
cfm_remote_mpids    : []
cfm_remote_opstate  : []
duplex              : full
external_ids        : {}
ifindex             : 588
ingress_policing_burst: 0
ingress_policing_rate: 0
lacp_current        : []
link_resets         : 1
link_speed          : 10000000000
link_state          : up
mac                 : []
mac_in_use          : "e6:e8:2f:93:25:53"
mtu                 : 1500
name                : "s1-eth1"
ofport              : 1
ofport_request      : 1
options             : {}
other_config        : {}
statistics          : {collisions=0, rx_bytes=810, rx_crc_err=0, rx_dropped=0, r                                                                                        x_errors=0, rx_frame_err=0, rx_over_err=0, rx_packets=13, tx_bytes=0, tx_dropped                                                                                        =0, tx_errors=0, tx_packets=0}
status              : {driver_name=veth, driver_version="1.0", firmware_version=                                                                                        ""}
type                : ""

	 * */
	
	protected UUID uuid; //_uuid
	protected boolean adminState; //admin_state
	protected String duplex; //duplex
	protected long linkSpeed; // link_speed          : 10000000000
	protected boolean linkState; // link_state          : up
	protected MacAddress macAddress; // mac_in_use          : "e6:e8:2f:93:25:53"
	protected int mtu; // mtu
	protected String name; // name                : "s1-eth3"
	protected OFPort ofport; // ofport
	protected String ofportRequest; // 	ofport_request      : 3
	protected String type; // type                : ""
	
	
	public Interfaces(UUID uuid, boolean adminState, long linkSpeed,
			boolean linkState, MacAddress macAddress, int mtu, String name,
			OFPort ofport, String ofportRequest, String type) {
		super();
		this.uuid = uuid;
		this.adminState = adminState;
		this.linkSpeed = linkSpeed;
		this.linkState = linkState;
		this.macAddress = macAddress;
		this.mtu = mtu;
		this.name = name;
		this.ofport = ofport;
		this.ofportRequest = ofportRequest;
		this.type = type;
	}


	public Interfaces(String name) {
		// TODO Auto-generated constructor stub
		this.name = name;
	}


	public UUID getUuid() {
		return uuid;
	}


	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}


	public boolean isAdminState() {
		return adminState;
	}


	public void setAdminState(boolean adminState) {
		this.adminState = adminState;
	}

	

	public void setDuplex(String duplex) {
		this.duplex = duplex;
	}


	public long getLinkSpeed() {
		return linkSpeed;
	}


	public void setLinkSpeed(long linkSpeed) {
		this.linkSpeed = linkSpeed;
	}


	public boolean isLinkState() {
		return linkState;
	}


	public void setLinkState(boolean linkState) {
		this.linkState = linkState;
	}


	public MacAddress getMacAddress() {
		return macAddress;
	}


	public void setMacAddress(MacAddress macAddress) {
		this.macAddress = macAddress;
	}


	public int getMtu() {
		return mtu;
	}


	public void setMtu(int mtu) {
		this.mtu = mtu;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public OFPort getOfport() {
		return ofport;
	}


	public void setOfport(OFPort ofport) {
		this.ofport = ofport;
	}


	public String getOfportRequest() {
		return ofportRequest;
	}


	public void setOfportRequest(String ofportRequest) {
		this.ofportRequest = ofportRequest;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getDuplex() {
		return duplex;
	}


	public void populateInfoOvsVsctlListInterface(String ovsVsctlListInterface) {
		// TODO Auto-generated method stub
		String[] lines = ovsVsctlListInterface.split("\\s*\\r?\\n\\s*");
		
		for (String line : lines) {

			//line = line.trim();
			String[] tokensOfLine = line.split(" ");
			
			if (tokensOfLine[0].compareTo("_uuid")==0 ){
				this.setUuid(UUID.fromString(tokensOfLine[tokensOfLine.length-1]));
			}
			
			if (tokensOfLine[0].compareTo("admin_state")==0 ){
				if (tokensOfLine[tokensOfLine.length-1].compareTo("up")==0 ){
					this.setAdminState(true);
				} else {
					this.setAdminState(true);
				}
				
			}
			
			if (tokensOfLine[0].compareTo("duplex")==0 ){
				this.setDuplex(tokensOfLine[tokensOfLine.length-1]);
			}
			
			if (tokensOfLine[0].compareTo("link_speed")==0 ){
				this.setLinkSpeed(Long.parseLong(tokensOfLine[tokensOfLine.length-1]));
			}
			
			if (tokensOfLine[0].compareTo("link_state")==0 ){
				if (tokensOfLine[tokensOfLine.length-1].compareTo("up")==0 ){
					this.setAdminState(true);
				} else {
					this.setAdminState(true);
				}
				
			}
			
			if (tokensOfLine[0].compareTo("mac_in_use")==0 ){
				this.setMacAddress(MacAddress.of(tokensOfLine[tokensOfLine.length-1].replace("\"", "")));
			}
			
			if (tokensOfLine[0].compareTo("mtu")==0 ){
				this.setMtu((Integer.parseInt(tokensOfLine[tokensOfLine.length-1])));
			}
			
			if (tokensOfLine[0].compareTo("ofport")==0 ){
				this.setOfport(OFPort.of(Integer.parseInt(tokensOfLine[tokensOfLine.length-1])));
			}
			
			if (tokensOfLine[0].compareTo("ofport_request")==0 ){
				this.setOfportRequest(tokensOfLine[tokensOfLine.length-1]);
			}
			
			if (tokensOfLine[0].compareTo("type")==0 ){
				this.setType(tokensOfLine[tokensOfLine.length-1].replace("\"", ""));
			}
		}
		
	}

}
