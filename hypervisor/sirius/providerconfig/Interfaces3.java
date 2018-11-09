package net.floodlightcontroller.sirius.providerconfig;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

public class Interfaces3 {

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
	protected MacAddress vmsMacAddress;
	protected long linkSpeed; // link_speed          : 10000000000
	protected boolean linkState; // link_state          : up
	protected MacAddress macAddress; // mac_in_use          : "e6:e8:2f:93:25:53"
	protected int mtu; // mtu
	protected String name; // name                : "s1-eth3"
	protected OFPort ofport; // ofport
	protected String ofportRequest; // 	ofport_request      : 3
	protected String remoteIP; // options             : {remote_ip="192.168.5.34"}
	protected String type; // type                : ""

	private String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	public Interfaces3(UUID uuid, boolean adminState, String duplex, long linkSpeed,
			MacAddress vmsMacAddress, boolean linkState, MacAddress macAddress, int mtu, String name,
			OFPort ofport, String ofportRequest, String remoteIP, String type) {
		super();
		this.uuid = uuid;
		this.adminState = adminState;
		this.duplex = duplex;
		this.vmsMacAddress = vmsMacAddress;
		this.linkSpeed = linkSpeed;
		this.linkState = linkState;
		this.macAddress = macAddress;
		this.mtu = mtu;
		this.name = name;
		this.ofport = ofport;
		this.ofportRequest = ofportRequest;
		this.remoteIP = remoteIP;
		this.type = type;
	}

	Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);

	public Interfaces3(String name) {
		this.name = name;
	}
	
	public Interfaces3(String name, MacAddress vmsMacAddress, OFPort port) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.vmsMacAddress = vmsMacAddress;
		this.ofport = port;
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

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}
	
	public MacAddress getVmsMacAddress() {
		return vmsMacAddress;
	}

	public void setVmsMacAddress(MacAddress vmsMacAddress) {
		this.vmsMacAddress = vmsMacAddress;
	}

	public void populateInfoOvsVsctlListInterface(String ovsVsctlListInterface) {
		// TODO Auto-generated method stub
		String[] lines = ovsVsctlListInterface.split("\\s*\\r?\\n\\s*");

		for (String line : lines) {
			String[] tokensOfLine = line.split(" ");
			if (tokensOfLine[0].compareTo("_uuid")==0 ){
				this.setUuid(UUID.fromString(tokensOfLine[tokensOfLine.length-1]));
			}
			if (tokensOfLine[0].compareTo("admin_state")==0 ){
				if (tokensOfLine[tokensOfLine.length-1].compareTo("up")==0 ){
					this.setAdminState(true);
				} else {
					this.setAdminState(false);
				}
			}
			if (tokensOfLine[0].compareTo("duplex")==0 ){
				this.setDuplex(tokensOfLine[tokensOfLine.length-1]);
			}
			if (tokensOfLine[0].compareTo("link_speed")==0 ){
				if (!tokensOfLine[tokensOfLine.length-1].contains("[")){
					this.setLinkSpeed(Long.parseLong(tokensOfLine[tokensOfLine.length-1]));
				}
			}
			if (tokensOfLine[0].compareTo("link_state")==0 ){
				if (tokensOfLine[tokensOfLine.length-1].compareTo("up")==0 ){
					this.setAdminState(true);
				} else {
					this.setAdminState(false);
				}
			}
			if (tokensOfLine[0].compareTo("mac_in_use")==0 ){
				this.setMacAddress(MacAddress.of(tokensOfLine[tokensOfLine.length-1].replace("\"", "")));
			}
			if (tokensOfLine[0].compareTo("mtu")==0 ){
				if (!tokensOfLine[tokensOfLine.length-1].contains("[")){
					this.setMtu((Integer.parseInt(tokensOfLine[tokensOfLine.length-1])));
				}
			}
			if (tokensOfLine[0].compareTo("ofport")==0 ){
				if (!tokensOfLine[tokensOfLine.length-1].contains("[")){
					this.setOfport(OFPort.of(Integer.parseInt(tokensOfLine[tokensOfLine.length-1])));
				}
			}
			if (tokensOfLine[0].compareTo("ofport_request")==0 ){
				this.setOfportRequest(tokensOfLine[tokensOfLine.length-1]);
			}
			if (tokensOfLine[0].compareTo("options")==0 ){
				if (tokensOfLine[tokensOfLine.length -1].compareTo("{}")!=0){
					String IPAddress = tokensOfLine[tokensOfLine.length -1].substring(tokensOfLine[tokensOfLine.length -1].
							indexOf("\"")+1,tokensOfLine[tokensOfLine.length -1].indexOf("\"", tokensOfLine[tokensOfLine.length -1].
									indexOf("\"")+1));
					Matcher matcher = pattern.matcher(IPAddress);
					if (matcher.find()) {
						this.setRemoteIP(IPAddress);
					}
				}
			}
			if (tokensOfLine[0].compareTo("type")==0 ){
				this.setType(tokensOfLine[tokensOfLine.length-1].replace("\"", ""));
			}
			if (tokensOfLine[0].compareTo("external_ids")==0 ){
				if (tokensOfLine[tokensOfLine.length -1].compareTo("{}")!=0){
					this.setVmsMacAddress(MacAddress.of(tokensOfLine[9].substring(15, 32)));
					}
				}
			}
		}
	
	public boolean equals(Object o) {
		return (o instanceof Interfaces3) && 
				(name.equals(((Interfaces3) o).name));
	}

	public int hashCode() {
		return name.hashCode();
	}
}