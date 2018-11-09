package server;

import common.Cloud;
import common.Console;
import common.Network;
import common.ServerError;
import common.Vm;

public class PrivateProvider extends Provider {
	
	public PrivateProvider(Server server, int id) {
		super(server, id);
	}

	public void synchronize() throws ServerError {
		Network network = getServer().getNetworks().get(0);
		Cloud cloud = Cloud.findFromId(network, getId());
		String name = cloud.getName();
		Console.info("Synchronizing VMs in " + name + " cloud");
		Console.info("Synchronization completed");
	}
	
	public void deploy(Vm vm) throws ServerError {
		throw new ServerError("This cloud does not support vm deployment");
	}
	
	public void destroy(Vm vm) throws ServerError {
		throw new ServerError("This cloud does not support vm termination");
	}
}
