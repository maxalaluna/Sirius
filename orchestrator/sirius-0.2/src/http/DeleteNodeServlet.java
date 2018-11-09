package http;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Cloud;
import common.Console;
import common.GsonData;
import common.Host;
import common.Link;
import common.Network;
import common.Node;
import common.ServerError;
import common.Switch;
import common.Vm;
import server.Server;

public class DeleteNodeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
   
	private static void delLinksFrom(Server server, Network network, Node node, GsonData data) {
		ArrayList<Link> links = network.getLinks();
		Network deleted = server.getDeleted();
		int k = 0;
		while (k < links.size()) {
			Link link = links.get(k);
			if (link.getFrom() == node 
					|| link.getTo() == node) {
				if (!link.getFrom().isHidden() && !link.getTo().isHidden())
					data.addLink(link.getId(), "", 0, 0, false);
				deleted.getLinks().add(link);
				links.remove(link);
			}
			else k++;
		}
	}
	
	public static void delHost(Server server, Network network, Host node, GsonData data) {
		Console.info("Removing host " + node.getName());
		if (!node.isHidden())
			data.addNode(node.getId(), 0, 0, "", "", "", false);
		if (node.isDeployed()) 
			server.getDeleted().getNodes().add(node);
		network.getNodes().remove(node);		
		delLinksFrom(server, network, node, data);
		network.setChanged(true);
	}
	
	public static void delSwitch(Server server, Network network, Switch node, GsonData data) throws Error {
		int cid = node.getVm().getCloud().getId();
		Vm gw = Cloud.findGateway(network, cid);
		if (gw != null && gw.findSwitch(network) == node)
			throw new Error("Cannot remove cloud gateway");
		else if (node.isLeaf(network) == false) 
			throw new Error("Cannot remove intermediate OVS");
		else {
			
			// Remove OVS and associated containers
			ArrayList<Host> lst = node.getVm().findContainers(network);
			Console.info("Removing switch " + node.getIndex());
			data.addNode(node.getId(), 0, 0, "", "", "", false);
			for (Host peer : lst) delHost(server, network, peer, data);
			network.getNodes().remove(node);
			delLinksFrom(server, network, node, data);
			
			// Remove VM
			Vm vm = node.getVm();
			Console.info("Removing vm " + node.getId());
			if (vm.isDeployed())
				server.getDeleted().getVms().add(vm);
			data.addVM(vm.getId(), 0, "");
			network.getVms().remove(vm);
			network.setChanged(true);
		}
	}
	
	private String getJson(Server server, Network network, int id) throws ServerError {
		Node node = Node.findFromId(network, id);
		GsonData data = new GsonData();
		if (node instanceof Host)
			delHost(server, network, (Host)node, data);
		else if (node instanceof Switch)
			delSwitch(server, network, (Switch)node, data);
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		int id = Integer.parseInt(request.getParameter("id"));
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network = server.getNetworks().get(net);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    try {
		    String str = getJson(server, network, id);
		    response.getWriter().write(str);
	    }
    	catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
			Console.error(error.getMessage());
    	}
	}
}
