package server;

import java.util.ArrayList;
import common.Config;
import common.Console;
import common.Host;
import common.Link;
import common.Network;
import common.Node;
import common.Image;
import common.ServerError;
import common.Switch;
import common.Vm;

public class Task extends Thread {

	public enum Type {
		CHECK_CONFIGURATION, CHECK_SCRIPTS, CHECK_IMAGES, CHECK_TUNNELS, 
		CHECK_CONTAINERS, DEPLOY_CONTAINERS,  REMOVE_CONTAINERS
	};

	private Server server;
	private Type type;
	private Vm vm;
	
	public Task(Server server, Type type, Vm vm) {
		this.server = server;
		this.type = type;
		this.vm = vm;
	}
	
	public void run() {
		try {
			switch (type) {
			case CHECK_CONFIGURATION:
				checkConfiguration();
				break;
			case CHECK_SCRIPTS:
				checkScripts();
				break;
			case CHECK_IMAGES:
				checkImages();
				break;
			case CHECK_TUNNELS:
				checkTunnels();
				break;
			case CHECK_CONTAINERS:
				checkContainers();
				break;
			case DEPLOY_CONTAINERS:
				deployContainers();
				break;
			case REMOVE_CONTAINERS:
				removeContainers();
				break;
			}
		} 
		catch (ServerError ex) {
			ex.printStackTrace();
		}
	}
	
	private void checkScripts() throws ServerError {
		if (server.getConfig().consoleUpdateScripts) {
			Console.info("Updating sirius scripts");
			vm.uploadFile(Console.getPath("script/sirius.zip"), ".");
			vm.runCmd("unzip -o sirius.zip");
		}
	}
	
	private void checkImages() throws ServerError {
		ArrayList<String> images = Docker.getImages(vm);
		Network substrate = server.getNetworks().get(0);
		ArrayList<String> installed = new ArrayList<>();
		for (Image image : substrate.getImages())
			if (images.contains(image.getFile()) 
					|| installed.contains(image.getFile()))
				Console.info("Image '" + image.getName() + "' is created in " + vm.getName());
			else {
				Docker.deployImage(server, vm, image);
				installed.add(image.getFile());
			}
	}
	
	public void checkTunnels() throws ServerError {
		boolean deepSync = server.getConfig().consoleDeepSync;
		Network substrate = server.getNetworks().get(0);
		Switch ovs = vm.findSwitch(substrate);
		ArrayList<String> tunnels = Tunnel.getGreTunnels(ovs);
		ArrayList<Node> peers = ovs.findPeers(substrate);
		Console.info("Checking tunnels' status");

		// Initialize bridge
		if (deepSync) Tunnel.initializeBridge(ovs);
		
		// Create missing tunnels
		for (Node node: peers) {
			if (node instanceof Switch) {
				Link link = Link.findFromEnds(substrate, ovs, node);
				if (link != null) {
					String name = "gre" + link.getId();
					if (deepSync == true) {
						Tunnel.removeGreTunnel(ovs, name);
						tunnels.remove(name);
					}
					if (!tunnels.contains(name)) 
						Tunnel.createGreTunnel(substrate, ovs, node.getVm(), name);
					else tunnels.remove(name);
					link.setDeployed(true);
				}
			}
		}

		// Remove unused tunnels
		for (String name: tunnels) {
			if (name.startsWith("gre"))
				Tunnel.removeGreTunnel(ovs, name);
		}
	}
	
	public void checkConfiguration() throws ServerError {
		if (vm.runCmd("ls sirius").contains("cannot access")) {
			Console.info("Configuring Vm " + vm.getName() + " from scratch");
			vm.runCmd("sudo sh -c \"echo '" +  vm.getCloud().getUsername() 
					+ " ALL=NOPASSWD: ALL' >> /etc/sudoers\"");
			vm.runCmd("sudo apt-get -y update ; sudo apt-get -y install unzip");
			vm.uploadFile(Console.getPath("script/sirius.zip"), ".");
			vm.runCmd("unzip -o sirius.zip");
			vm.runCmd("sudo sirius/install-all.sh " + vm.getCloud().getUsername());
			Console.info("Vm configured. Rebooting now...");
			vm.runCmd("sudo reboot");
		}
		else {
			Console.info("Vm " + vm.getName() + " is already configured");
			checkContainers();
		}
	}
	
	public void checkContainers() throws ServerError {
		ArrayList<Container> containers = Docker.getContainers(vm);
		boolean deepSync = server.getConfig().consoleDeepSync;
		Network substrate = server.getNetworks().get(0);
		
		// Restart docker service
		if (deepSync == true) {
			Docker.clearVolumes(vm);
			Docker.restartService(vm);
		}
		
		// Check containers' status
		for (Host node : vm.findContainers(substrate)) {
			if (node.isDeployed()) {
				Console.info("Checking " + node.getName() + "'s status");
				Container container = Container.findByName(containers, node.getName()); 
				if (container == null)
					Console.info("WARNING: " + node.getName() + " unfound on " + vm.getName());
				else containers.remove(container);
			}
		}

		// Remove undeclared containers
		if (containers.size() > 0) Docker.removeContainers(server, vm, containers);
	}
	
	public static synchronized Host addHost(Network network, Vm vm, Config config) {
		Switch ovs = vm.findSwitch(network);
		Host node = network.addDefaultHost(config, vm);
		network.addDefaultLink(config, node, ovs);
		return node;
	}
		
	public static synchronized void removeNode(Network network, Node node) {
		ArrayList<Link> links = network.getLinks();
		network.getNodes().remove(node);
		network.setChanged(true);
		int k = 0;
		while (k < links.size()) {
			Link link = links.get(k);
			if (link.getFrom() == node 
					|| link.getTo() == node) {
				links.remove(link);
			}
			else k++;
		}
	}
	
	public void deployContainers() throws ServerError {
		ArrayList<Container> containers = Docker.getContainers(vm);
		Network substrate = server.getNetworks().get(0);
		ArrayList<Host> nodes = vm.findContainers(substrate);
		ArrayList<Host> tmp = new ArrayList<>();
		for (Host node : nodes) {
			if (!node.isDeployed()) {
				Container container = Container.findByName(containers, node.getName());
				if (container != null)
					Console.info("WARNING " + node.getName() + " is already created on " + vm.getName());
				else {
					node.setDeployed(true);
					tmp.add(node);
				}
			}
		}
		
		// Deploy containers
		if (tmp.size() > 0)	Docker.deployContainers(server, vm, tmp);
	}
	
	public void removeContainers() throws ServerError {
		ArrayList<Container> containers = Docker.getContainers(vm);	
		if (containers.size() > 0) 
			Docker.removeContainers(server, vm, containers);
	}
}
