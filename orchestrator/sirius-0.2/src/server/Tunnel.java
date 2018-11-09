package server;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import common.Config;
import common.Network;
import common.ServerError;
import common.Switch;
import common.Vm;

public class Tunnel {
	
	private int fromId;
	private String fromIp;
	private int toId;
	private String toIp;

	public Tunnel(int fromVm, String fromIp, int toVm, String toIp) {
		this.fromId = fromVm;
		this.fromIp = fromIp;
		this.toId = toVm;
		this.toIp = toIp;
	}
	
	public int getFromId() {
		return fromId;
	}

	public String getFromIp() {
		return fromIp;
	}

	public int getToId() {
		return toId;
	}

	public String getToIp() {
		return toIp;
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("tunnel");
		Config.writeXMLInteger(doc, elem, "from-id", fromId);
		Config.writeXMLString(doc, elem, "from-ip", fromIp);
		Config.writeXMLInteger(doc, elem, "to-id", toId);
		Config.writeXMLString(doc, elem, "to-ip", toIp);
		return elem;
	}
	
	public static ArrayList<String> getGreTunnels(Switch ovs) throws ServerError {
		String cmd = "sudo ovs-vsctl list-ports " + ovs.getBridgeName();
		String[] lines = ovs.getVm().runCmd(cmd).split("\n");
		ArrayList<String> tmp = new ArrayList<>();
		for (String line: lines) 
			tmp.add(line);
		return tmp;
	}
	
	public static void initializeBridge(Switch ovs) throws ServerError {
		ovs.getVm().runCmd("sudo ovs-vsctl del-br " + ovs.getBridgeName());
		ovs.getVm().runCmd("sudo ovs-vsctl add-br " + ovs.getBridgeName());
		ovs.getVm().runCmd("sudo ovs-vsctl set bridge " + ovs.getBridgeName() + " protocols=OpenFlow13");
	}
	
	public static void createGreTunnel(Network network, Switch ovs, Vm vm, String name) throws ServerError {
		String remoteIp = vm.getPrivateIp();
		if (ovs.getVm().isGateway() && vm.isGateway())
			remoteIp = findTunnelIp(network, vm.getId(), ovs.getVm().getId());
		String cmd = "sudo ovs-vsctl add-port " + ovs.getBridgeName() + " " + name 
				+ " -- set interface " + name + " type=gre options:remote_ip=" + remoteIp;
		ovs.getVm().runCmd(cmd);
	}
	
	public static void removeGreTunnel(Switch ovs, String name) throws ServerError {
		String cmd = "sudo ovs-vsctl del-port " + ovs.getBridgeName() + " " + name;
		ovs.getVm().runCmd(cmd);
	}
	
	public static String findTunnelIp(Network network, int vid1, int vid2) throws ServerError {
		for (Tunnel tunnel: network.getTunnels()) 
			if (tunnel.getFromId() == vid1 && tunnel.getToId() == vid2)
				return tunnel.getFromIp();
			else if (tunnel.getFromId() == vid2 && tunnel.getToId() == vid1)
				return tunnel.getToIp();
		throw new ServerError("Unknown tunnel between " + vid1 + " and " + vid2);
	}
}
