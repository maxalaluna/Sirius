package http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import com.google.gson.Gson;
import common.Cloud;
import common.Config;
import common.Console;
import common.GsonData;
import common.Host;
import common.Link;
import common.Network;
import common.Node;
import common.Packet;
import common.ServerError;
import common.Switch;
import common.Vm;
import common.Packet.Type;
import server.Listener;
import server.Server;
import server.Task;

public class MappingServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
		
	private static void deployHost(Server server, Host node, GsonData data, boolean hidden) throws ServerError {
		Config config = server.getConfig();
		Network substrate = server.getNetworks().get(0);
		Node ovs = Node.findFromId(substrate, node.getMapping());
		Host source = substrate.addDefaultHost(config, ovs.getVm());
		Link link = substrate.addDefaultLink(config, source, ovs);
		if (hidden == false) {
			data.addNode(source.getId(), source.getVm().getId(), 
					source.getVm().getCloud().getId(), source.getName(), 
					source.getTitle(), source.getIcon(), false);
			data.addLink(link.getId(), link.getTitle(), 
					source.getId(), ovs.getId(), false);
		}
		source.setHidden(hidden);
		node.setMapping(source.getId());
		updateHost(substrate, source, node);
		updateNode(substrate, source, node);
	}
	
	private static void updateSwitch(Switch source, Switch dest) {
		Console.info("Switch " + dest.getName() + " is mapped on " 
				+ source.getName() + " of " + source.getVm().getName());
		dest.setOpenflowVersion(source.getOpenflowVersion());
		dest.setSecurityLevel(source.getSecurityLevel());
		dest.setMaxFlowSize(source.getMaxFlowSize());
		dest.setBridgeName(source.getBridgeName());
		dest.setCpu(source.getCpu());
		source.setDpid(dest.getDpid());	
	}
	
	private static void updateHost(Network network, Host source, Host dest) {
		//Console.info("Host " + dest.getName() + " is mapped on " 
		//		+ source.getName() + " of " + source.getVm().getName());
		dest.setCpu(source.getCpu());
		dest.setTenant(network.getTenant());
		source.setTenant(network.getTenant());
		source.setImage(dest.getImage());
		source.setIp(dest.getIp());
		source.setMac(dest.getMac());
		source.setPort(dest.getPort());		
	}
	
	private static void updateNode(Network network, Node source, Node dest) {
		
		// Copy vm information
		Vm vm = source.getVm();
		if (Vm.findFromId(network, vm.getId()) == null) 
			network.getVms().add(vm);
		dest.setVm(vm);
		
		// Copy cloud information
		Cloud cloud = vm.getCloud();
		if (Cloud.findFromId(network, cloud.getId()) == null)
			network.getClouds().add(cloud);
		vm.setCloud(cloud);
	}
	
	private static Network requestTopology(Server server, Network network) throws ServerError {
		Listener listener = server.getListener();
		Document doc = network.writeDocument();
		StringWriter writer = new StringWriter();
		Config.saveDocument(doc, new StreamResult(writer));
		String str = writer.getBuffer().toString();
		int timeout = 1000 * server.getConfig().consoleControllerTimeout;
		listener.send(new Packet(Type.VIRTUAL_REQUEST, str.getBytes()));
		Type[] types = new Type[] { Type.VIRTUAL_SUCCESS, Type.VIRTUAL_FAILURE };
		long start = System.currentTimeMillis();
		Packet packet = listener.waitFor(types, timeout);
		double time = System.currentTimeMillis() - start;
		Console.info("HYPERVISOR time is " + time + "ms");
		
		// Create network from data
		if (packet.getType() == Type.VIRTUAL_SUCCESS) {
			Network reply = new Network(server.getConfig());
			InputStream is = new ByteArrayInputStream(packet.getData());
			reply.readDocument(Config.loadDocument(is));
			return reply;
		}
		
		// Mapping failed
		else if (packet.getData() != null)
			throw new ServerError(new String(packet.getData()));
		else throw new ServerError("Mapping failed");	
	}	
	
	private static void updateNetwork(Server server, Network update, 
			Network network, GsonData data) throws ServerError {	
		Network substrate = server.getNetworks().get(0);
		network.clear();
		int k = 0;
		
		// Scan network nodes
		for (Node node: update.getNodes()) {
			Node source = null;
			int id = 0;
			
			// Deploy new host
			if (node instanceof Host) {
				int max = server.getConfig().maxHostsPerSwitch;
				int nb = node.getVm().findContainers(substrate).size();
				deployHost(server, (Host)node, data, (nb + k++) >= max);
			}
			
			// Configure mapped switch
			else if (node instanceof Switch) {
				id = ((Switch)node).getMapping();
				source = Node.findFromId(substrate, id);
				if (source instanceof Switch) {
					updateSwitch((Switch)source, (Switch)node);
					updateNode(network, source, node);
				}
				else throw new ServerError("Incorrect mapping in virtual reply");
			}
	
			// Update source node
			network.getNodes().add(node);
			node.setDeployed(true);			
		}
		
		// Scan network link
		for (Link link : update.getLinks())
			network.getLinks().add(link);
		network.setChanged(true);
	}
	
	public static String getJson(Server server, int net) throws ServerError {
		Network substrate = server.getNetworks().get(0);
		Network tenant = server.getNetworks().get(net);
		if (server.getListener().isConnected()) {
			server.checkSwitchConnections();
			Console.info("Virtual network " + tenant.getNodes().size() 
					+ " nodes " + tenant.getLinks().size() + " links");
			
			// Send request
			Network network = requestTopology(server, tenant);
			Console.info("Reply contains " + network.getNodes().size() 
					+ " nodes " + network.getLinks().size() + " links");
			
			// Save reply
			String path = Console.getPath("xml");
			network.writeXML(path + "/reply.xml");
			
			// Perform mapping
			GsonData data = new GsonData();
			long start = System.currentTimeMillis();
			DeleteTenantServlet.clearNetwork(server, tenant);
			updateNetwork(server, network, tenant, data);
			for (Vm vm: substrate.getVms()) {
				Task task = new Task(server, Task.Type.DEPLOY_CONTAINERS, vm); 
				server.getTasks().add(task);
				task.start();
			}
			
			// Wait for task completion
			server.waitForTasks();
			long elapsed = (System.currentTimeMillis() - start) / 1000;
			Console.info("Containers deployed in " + elapsed + "s\n");
			
			// Return new nodes and links
			return new Gson().toJson(data);
		}
		else throw new ServerError("Hypervisor is not connected");
	}
	
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");  	
    	try {
        	String str = getJson(server, net);
        	response.getWriter().write(str);
    	}
    	catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
			Console.error(error.getMessage());
    	}
	}
}
