package http;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Console;
import common.GsonData;
import common.Host;
import common.Link;
import common.Network;
import common.Node;
import common.ServerError;
import common.Switch;
import server.Server;

public class DeleteLinkServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
	private void checkLink(Server server, Network network, Link link, 
			Switch node1, Switch node2, GsonData data) throws Error {
		if (node1.getVm().getCloud() != node2.getVm().getCloud())
			throw new Error("Cannot remove intercloud link");
		
		// Remove node1's VM
		else if (node1.isLeaf(network))
			DeleteNodeServlet.delSwitch(server, network, node1, data);
		
		// Remove node2's VM
		else if (node2.isLeaf(network))
			DeleteNodeServlet.delSwitch(server, network, node2, data);
		
		// Remove inter-OVS link
		else {
			Network deleted = server.getDeleted();
			ArrayList<Link> links = network.getLinks();
			data.addLink(link.getId(), "", 0, 0, false);
			deleted.getLinks().add(link);
			links.remove(link);
		}
	}
	
	private String getJson(Server server, Network network, int id) throws ServerError {		
		Link link = Link.findFromId(network, id);
		GsonData data = new GsonData();
		Node node1 = link.getFrom();
		Node node2 = link.getTo();
		if (node1 instanceof Host)
			DeleteNodeServlet.delHost(server, network, (Host)node1, data);
		else if (node2 instanceof Host)
			DeleteNodeServlet.delHost(server, network, (Host)node2, data);
		else if (node1 instanceof Switch && node2 instanceof Switch) 
			checkLink(server, network, link, (Switch)node1, (Switch)node2, data);
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
