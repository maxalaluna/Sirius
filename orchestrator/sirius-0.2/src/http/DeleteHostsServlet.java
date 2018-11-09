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
import common.Network;
import common.Node;
import common.ServerError;
import common.Switch;
import server.Server;

public class DeleteHostsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private String getJson(Server server, Network network, int id) throws ServerError {
		Node node = Node.findFromId(network, id);
		GsonData data = new GsonData();
		if (node instanceof Switch) {
			ArrayList<Host> hosts = node.getVm().findContainers(network);
			for (Host host: hosts) DeleteNodeServlet.delHost(server, network, host, data);
		}
		else throw new ServerError("Must select a switch"); 
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
