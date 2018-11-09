package http;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Config;
import common.Console;
import common.GsonData;
import common.Link;
import common.Network;
import common.Node;
import common.ServerError;
import common.Switch;
import server.Server;

public class AddLinkServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private String getJson(Server server, Network network, int from, int to) throws ServerError {
		Config config = server.getConfig();
		GsonData data = new GsonData();
		Node node1 = Node.findFromId(network, from);
		Node node2 = Node.findFromId(network, to);
		if (node1 == node2)
			throw new ServerError("Self links are not allowed");
		else if (!(node1 instanceof Switch) || !(node2 instanceof Switch))
			throw new ServerError("Only links between switches are allowed");
		else if (node1.findPeers(network).indexOf(node2) != -1)
			throw new ServerError("Switches are already connected");
		else {
			int lid = Link.newId(network);
			Link link = new Link(lid, node1, node2, config.defaultLinkBandwidth, 
					config.defaultLinkDelay, config.defaultLinkLossRate, 0, null, false);
			data.addLink(lid, link.getTitle(), node1.getId(), node2.getId(), false);
			network.getLinks().add(link);
		}
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		int to = Integer.parseInt(request.getParameter("to"));
		int from = Integer.parseInt(request.getParameter("from"));
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network = server.getNetworks().get(net);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    try {
		    String str = getJson(server, network, from, to);
		    response.getWriter().write(str);
	    }
    	catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
			Console.error(error.getMessage());
    	}
	}
}
