package http;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Cloud;
import common.GsonData;
import common.Link;
import common.Network;
import common.Node;
import common.Vm;
import server.Server;

public class LoadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static GsonData networkToData(Network network) {
		GsonData data = new GsonData();
		
		// Serialize clouds	
		for (Cloud cloud : network.getClouds())
			data.addCloud(cloud.getId(), cloud.getName(), 0);
		
		// Serialize VMs
		for (Vm vm : network.getVms()) {
			int state = vm.getCloud().getId();
			if (!vm.isDeployed()) state = -state;
			data.addVM(vm.getId(), state, vm.getName());
		}
		
		// Serialize nodes
		for (Node node : network.getNodes())
			if (!node.isHidden())
				data.addNode(node.getId(), node.getVm().getId(), 
						node.getVm().getCloud().getId(), node.getName(), 
						node.getTitle(), node.getIcon(), false);
		
		// Serialize links
		for (Link link : network.getLinks())
			if (!link.getFrom().isHidden() && !link.getTo().isHidden())
				data.addLink(link.getId(), link.getTitle(), link.getFrom().getId(), 
						link.getTo().getId(), false);
		return data;
	}
	
	private static String getJson(Network network) {
		GsonData data = networkToData(network);
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network;
		
		// Create new tenant network if no exists
		if (server.getNetworks().containsKey(net))
			network = server.getNetworks().get(net);
		else network = server.addTenant(net);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
    	String str = getJson(network);
    	response.getWriter().write(str);
	}
}
