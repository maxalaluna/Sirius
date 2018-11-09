package server;

import common.Host;
import common.Network;
import common.ServerError;
import common.Vm;

public abstract class Provider {

	private Server server;
	private int id;
	
	public Provider(Server server, int id) {
		this.server = server;
		this.id = id;
	}
	
	public Server getServer() {
		return server;
	}

	public int getId() {
		return id;
	}
	
	public void testConnectivity(StringBuffer buffer, Host node) throws ServerError {
		Network network = server.getNetworks().get(0);
		int tenant = node.getTenant();
		for (Vm vm : network.getVms()) {
			for (Host peer : vm.findContainers(network)) {
				if (peer != node && peer.isDeployed() 
						&& peer.getTenant() == tenant) {
					buffer.append("Ping to " + peer.getName() 
							+ " of " + peer.getVm().getName()+ "\n");
					String res = Docker.run(node, "ping -c 1 " + peer.getIp());
					buffer.append(res + "\n");
				}
			}
		}
	}
	
	public abstract void synchronize() throws ServerError;
	
	public abstract void deploy(Vm vm) throws ServerError;
	
	public abstract void destroy(Vm vm) throws ServerError;
}
