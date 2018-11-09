package server;

import static com.google.common.base.Charsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import org.jclouds.http.HttpResponseException;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.compute.options.TemplateOptions.Builder;
import org.jclouds.domain.Credentials;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.jclouds.aws.ec2.reference.AWSEC2Constants;
import org.jclouds.compute.config.ComputeServiceProperties;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.inject.Module;

import common.Cloud;
import common.Config;
import common.Console;
import common.Network;
import common.ServerError;
import common.Switch;
import common.Vm;
import server.Task.Type;

public class PublicProvider extends Provider {
	
	public static final String PROPERTY_OAUTH_ENDPOINT = "oauth.endpoint";
	
	private ComputeService client;
	private ComputeServiceContext context;
	
	public PublicProvider(Server server, int id, String name) throws ServerError {
		super(server, id);
		Network network = server.getNetworks().get(0);
		Cloud cloud = Cloud.findFromId(network, getId());
		Properties properties = new Properties();
		properties.setProperty(AWSEC2Constants.PROPERTY_EC2_AMI_QUERY, 
				"owner-id=137112412989;state=available;image-type=machine");
		properties.setProperty(AWSEC2Constants.PROPERTY_EC2_CC_AMI_QUERY, "");
		long scriptTimeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES);
		properties.setProperty(ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE, scriptTimeout + "");
		String oAuthEndpoint = System.getProperty(PROPERTY_OAUTH_ENDPOINT);
		if (oAuthEndpoint != null) {
			properties.setProperty(PROPERTY_OAUTH_ENDPOINT, oAuthEndpoint);
		}
		
		// Google credential
		String credential = cloud.getCredential();
		if (name.equalsIgnoreCase("google-compute-engine")) {
			String path = Console.getPath(credential);
			credential = getCredentialFromJsonKeyFile(path);
		}
		
		// Build context
		this.context = ContextBuilder.newBuilder(name)
					.credentials(cloud.getIdentity(), credential)
		            .modules(ImmutableSet.<Module> of(
		            		new SshjSshClientModule(),
		            		new SLF4JLoggingModule(),
		            		new EnterpriseConfigurationModule()))
		            .overrides(properties)
		            .buildView(ComputeServiceContext.class);
		this.client = context.getComputeService();
	}
	
	private void configureFW(Vm vm) throws ServerError {
		Console.info("Disabling FW rules in " + vm.getName());
		vm.runCmd("sudo iptables -t nat -F");
		vm.runCmd("sudo iptables -t nat -X");
		vm.runCmd("sudo iptables -t mangle -F");
		vm.runCmd("sudo iptables -t mangle -X");
		vm.runCmd("sudo iptables -P INPUT ACCEPT");
		vm.runCmd("sudo iptables -P FORWARD ACCEPT");
		vm.runCmd("sudo iptables -P OUTPUT ACCEPT");		
	}
	
	// When connecting Amazon and Google clouds, it's only necessary to check for the Amazon route 
	// in the non-gateway VM as the Google route is directly set up from the Google Platform Console.
	
	private void configureRoute(Vm vm) throws ServerError {
		Network network = getServer().getNetworks().get(0);
		Vm gw = Cloud.findGateway(network,getId());
		Console.info("Adding routes to gateway in " + vm.getName());
		vm.runCmd("sudo route add -host 10.8.0.1 gw " + gw.getPrivateIp());
		vm.runCmd("sudo route add -host 10.8.0.46 gw " + gw.getPrivateIp());
	}
	
	private boolean checkStatus(String id) throws ServerError {
		NodeMetadata metadata = client.getNodeMetadata(id);
		String status = metadata.getStatus().toString();
		switch (status) {
		case "RUNNING":
			Console.info("Vm " + id + " is running");
			return true;
		case "SUSPENDED":
			Console.info("Vm " + id + " is suspended");
			if (getServer().getConfig().consoleWakeupMode) {
				Console.info("Trying to wake " + id + " up");
				client.resumeNode(id);
				return true;
			}
			return false;
		default:
			Console.warning("Vm " + id + " is " + status);
			return false;
		}
	}
	
	private void checkAddress(Vm vm) {
		NodeMetadata metadata = client.getNodeMetadata(vm.getPid());
		String publicIp = Iterables.getFirst(metadata.getPublicAddresses(), null);
	    String privateIp = Iterables.getFirst(metadata.getPrivateAddresses(), null);
	    Network network = getServer().getNetworks().get(0);
	    if (publicIp == null || privateIp == null) 
	    	Console.warning("Unable to update IPs for " + vm.getName());
	    else {
	    	if (!publicIp.equals(vm.getPublicIp())) {
	    		Console.info("Updating public IP for " + vm.getName() + " to " + publicIp);
		    	vm.setPublicIp(publicIp);
		    	network.setChanged(true);
	    	}
	    	if (!privateIp.equals(vm.getPrivateIp())) {
	    		Console.info("Updating private IP for " + vm.getName() + " to " + privateIp);
	    		vm.setPrivateIp(privateIp);
		    	network.setChanged(true);
	    	}
	    }
	}
	
	private Vm addNewVM(ComputeMetadata node, Cloud cloud) {
		Network network = getServer().getNetworks().get(0);
		NodeMetadata metadata = client.getNodeMetadata(node.getId());
		String location = metadata.getLocation().getId();
		int id = Vm.newId(network);
		String name = (node.getName() != null)? node.getName(): cloud.getName() + id;
		String publicIp = Iterables.getFirst(metadata.getPublicAddresses(), "nc");
	    String privateIp = Iterables.getFirst(metadata.getPrivateAddresses(), "nc");
	    Vm vm = new Vm(id, name, node.getId(), cloud, location, publicIp, 
	    		privateIp, false, false, true, getServer().getConfig());
	    
	    // Create ovs and link to cloud's gw
	    Config config = getServer().getConfig();
	    Switch ovs = network.addDefaultSwitch(config, vm);
	    Vm gw = Cloud.findGateway(network, cloud.getId());
	    network.addDefaultLink(config, ovs, gw.findSwitch(network));
		Console.info("Adding discovered VM " + node.getId() + " [" + publicIp + "]");
		network.setChanged(true);
		network.getVms().add(vm);
		ovs.setDeployed(true);
		return vm;
	}

	public void suspend(Vm vm) throws ServerError {
		client.suspendNode(vm.getPid());
	}
		
	public void synchronize() throws ServerError {
		Network network = getServer().getNetworks().get(0);
		Cloud cloud = Cloud.findFromId(network, getId());
		Config config = getServer().getConfig();
		String name = cloud.getName();
		Console.info("Synchronizing VMs in " + name + " cloud");
		long start = System.currentTimeMillis();
		while (true) {
			try {
				for (ComputeMetadata node : client.listNodes()) {
					Console.info("Checking " + node.getId() + "'s status");
					Vm vm = Vm.findFromInfo(network, node.getId(), getId());
					if (vm != null && vm.isDeployed()) {

						// Check VM status
						if (checkStatus(vm.getPid())) {
							checkAddress(vm);
							
							// Disable FW on GW
							if (vm.isGateway())
								configureFW(vm);
							
							// Configure routes
							if (!name.equals("google") 
									&& !vm.isGateway())
								configureRoute(vm);
						}
					}
					else if (config.consoleDiscoverMode && checkStatus(node.getId())) {
						vm = addNewVM(node, cloud);

						// Check VM location
						if (vm != null) {
							if (!name.equals("google")) configureRoute(vm);
							Task task = new Task(getServer(), Type.CHECK_CONFIGURATION, vm); 
							getServer().getTasks().add(task);
							task.start();
						}
					}
				}
				break;
			}
			catch (HttpResponseException ex) {
				Console.warning("Connection timeout. Retrying ...");
				Console.pause(1000);
				continue;
			}
		}
		
		// Wait for task completion
		getServer().waitForTasks();
		long elapsed = (System.currentTimeMillis() - start) / 1000;
		Console.info("VMs started in " + name + " cloud in " + elapsed + "s\n");
	}
	
	public void stopAllVms() throws ServerError {
		Network network = getServer().getNetworks().get(0);
		String name = Cloud.findFromId(network, getId()).getName();
		Console.info("Stopping VMs in " + name + " cloud");
		for (Vm vm : network.getVms()) {
			if (vm.getCloud().getId() == getId()) {
				Console.info("Checking " + vm.getName() + "'s status");
				suspend(vm);
			}
		}
		Console.info("VMs stopped in " + name + " cloud");
	}

	private static String getCredentialFromJsonKeyFile(String filename) throws ServerError {
		try {
	         String fileContents = Files.toString(new File(filename), UTF_8);
	         Supplier<Credentials> credentialSupplier = new GoogleCredentialsFromJson(fileContents);
	         String credential = credentialSupplier.get().credential;
	         return credential;
		} 
		catch (IOException ex) {
			throw new ServerError(ex.getMessage());
		}
	}

	public void deploy(Vm vm) throws ServerError {
		TemplateBuilder templateBuilder = client.templateBuilder();
        Statement bootInstructions = AdminAccess.standard();
        templateBuilder.options(Builder.runScript(bootInstructions));
        Template template = templateBuilder.build();
		try {
			NodeMetadata node = Iterables.getOnlyElement(client.createNodesInGroup("supercloud", 1, template));
            System.out.printf("<< node %s: %s%n", node.getId(), node.getPrivateAddresses(), node.getPublicAddresses());
		} 
		catch (RunNodesException ex) {
			throw new ServerError(ex.getMessage());
		}
	}

	public void destroy(Vm vm)  throws ServerError {
		client.destroyNode(vm.getPid());
	}
	
	public void close() {
		context.close();
	}
}
