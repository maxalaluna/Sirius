package server;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import common.Cloud;
import common.Config;
import common.Console;
import common.Controller;
import common.Network;
import common.Node;
import common.ServerError;
import common.Vm;
import server.Listener;
import server.Task.Type;

public class Server {
	
	private Config config;
	private Listener listener;
	private Network deleted;
	private Node selectedNode;
	private ArrayList<Task> tasks;
	private ArrayList<Integer> selectedTenants;
	private Hashtable<Integer, Network> networks;
	private Hashtable<String, User> users;
	private ArrayList<Provider> providers;
	
	public Server() throws ServerError  {
		Console.init("console.log");
		Console.info("Starting server version " + Config.MAJOR_VERSION 
				+ "." + Config.MINOR_VERSION);
		providers = new ArrayList<>();
		selectedTenants = new ArrayList<>();
		users = new Hashtable<>();
		networks = new Hashtable<>();
		tasks = new ArrayList<Task>();

		// Read console properties
		String propPath = Console.getPath("console.properties");
		config = new Config(propPath);

		// Read file networks
		deleted = new Network(config);
		loadNetworks();
	
		// Run cloud threads
		Network network = networks.get(0); 
		for (Cloud cloud : network.getClouds()) {
			String name = cloud.getProvider();
			switch (name.toLowerCase()) {
			case "amazon": {
				Provider provider = new PublicProvider(this, cloud.getId(), "aws-ec2");
				providers.add(provider);
				provider.synchronize();
				break;
			}
			case "google": {
				Provider provider = new PublicProvider(this, cloud.getId(), "google-compute-engine");
				providers.add(provider);
				provider.synchronize();
				break;
			}
			default: 
				providers.add(new PrivateProvider(this, cloud.getId()));
				break;
			}
		}
		
		// Need to save topology
		if (network.isChanged()) {
			String prefix = config.consoleFilePrefix;
			String path = Console.getPath("xml");
			network.writeXML(path + "/" + prefix + "0.xml");
		}
		
		// Start controller listener
		listener = new Listener(this);
		listener.start();
		
		// Check configurations
		checkAll();
	}
	
	public Config getConfig() {
		return config;
	}
	
	public Hashtable<Integer, Network> getNetworks() {
		return networks;
	}
	
	public Listener getListener() {
		return listener;
	}

	public Network getDeleted() {
		return deleted;
	}
	
	public ArrayList<Integer> getSelectedTenants() {
		return selectedTenants;
	}

	public ArrayList<Provider> getProviders() {
		return providers;
	}

	public ArrayList<Task> getTasks() {
		return tasks;
	}
	
	public Provider getProvider(Cloud cloud) {
		for (Provider provider : providers)
			if (provider.getId() == cloud.getId())
				return provider;
		return null;
	}
	
	public Node getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(Node selectedNode) {
		this.selectedNode = selectedNode;
	}
	
	public Network addNetwork(int key) {
		Console.info("Creating network with id " + key);
		Network network = new Network(config);
		networks.put(key, network);
		network.setTenant(key);
		return network;
	}
	
	public Network addTenant(int key) {
		Network network = addNetwork(key);
		Network substrate = networks.get(0);
		network.copyImagesFrom(substrate);
		Cloud cloud = network.addDefaultCloud();
		Vm vm = network.addDefaultVm(config, cloud);
		network.addDefaultSwitch(config, vm);
		return network;
	}
	
	public void loadNetworks() throws ServerError {
		String prefix = config.consoleFilePrefix;
		String path = Console.getPath("xml");
		for (File file : new File(path).listFiles())
			if (file.getName().startsWith(prefix)) {
				int pos = file.getName().indexOf('.');
				String str = file.getName().substring(prefix.length(), pos);
				int id = Integer.parseInt(str);
				Network network = addNetwork(id);
				network.readXML(file.getPath());
			}
		if (networks.containsKey(0) == false)
			throw new ServerError("Topology file not found");
		if (Controller.find(networks.get(0)) == null)
			throw new ServerError("No controller found");
	}
	
	public void checkSwitchConnections() throws ServerError {
		StringBuffer buffer = new StringBuffer();
		String cmd = "sudo ovs-vsctl show";
		Network network = networks.get(0);
		boolean connected = true;
		for (Vm vm : network.getVms()) 
			if (vm.isDeployed())
				if (!vm.runCmd(cmd).contains("connected")) {
					buffer.append(vm.getName() + " is not connected\n");
					connected = false;
				}
		if (!connected) throw new ServerError(buffer.toString());
	}

	public void checkAll() throws ServerError {
		long start = System.currentTimeMillis();
		Console.info("Checking configurations");
		Network network = networks.get(0);
		for (Vm vm : network.getVms()) 
			if (vm.isDeployed()) {
				Console.info("Removing log in " + vm.getName());
				vm.runCmd("rm log.txt > /dev/null");
				Task task;
				
				// Check script
				task = new Task(this, Type.CHECK_SCRIPTS, vm); 
				tasks.add(task);
				task.start();					
				
				// Check images
				task = new Task(this, Type.CHECK_IMAGES, vm); 
				tasks.add(task);
				task.start();	
				
				// Check tunnels
				task = new Task(this, Type.CHECK_TUNNELS, vm); 
				tasks.add(task);
				task.start();
				
				// Check containers
				task = new Task(this, Type.CHECK_CONTAINERS, vm); 
				tasks.add(task);
				task.start();
			}
		waitForTasks();
		long elapsed = (System.currentTimeMillis() - start) / 1000;
		Console.info("Check completed in " + elapsed + "s");
		Console.info("Orchestrator ready to go\n");
	}
	
	public void waitForTasks() {
		try {
			for (Task task: tasks) task.join();
			Console.info("Joining " + tasks.size() + " tasks");
			tasks.clear();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void login(String username) {
		User network = users.get(username);
		if (network != null) logout(username);
		Console.info("User '" + username + "' is logging in");
		users.put(username, new User());
	}
	
	public void logout(String username) {
		User network = users.get(username);
		Console.info("User '" + username + "' is logging out");
		if (network != null) users.remove(username);
	}
	
	public void close() throws ServerError {
		listener.close();
		Console.info("Server is closing");
		Console.close();
	}
}
