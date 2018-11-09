package http;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Console;
import common.GsonData;
import common.Link;
import common.Network;
import common.Node;
import common.ServerError;
import server.Server;

public class UpdateNodeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
	private void updateNode(Network network, Node node, String name, String value, GsonData data) throws ServerError {		
		if (!node.getVm().getCloud().update(name, value, data) 
				&& !node.getVm().update(name, value, data) 
				&& !node.update(network, name, value, data))
			throw new ServerError("Attribute '" + name + "' cannot be found");
		Console.info("Attribute '" + name + "' updated on node");		
	}
	
	private void updateLink(Link link, String name, String value, GsonData data) throws ServerError {		
		if (!link.update(name, value, data))
			throw new ServerError("Attribute '" + name + "' cannot be found");
		Console.info("Attribute '" + name + "' updated on link");		
	}
	
	private String getJson(Network network, String type, int id, String name, String value) throws ServerError {
		value = value.trim().replaceAll("(\\r|\\n)", "");
		GsonData data = new GsonData();
		if (type.equals("node"))  {
			Node node = Node.findFromId(network, id);
			updateNode(network, node, name, value, data);
		}
		else {
			Link link = Link.findFromId(network, id);
			updateLink(link, name, value, data);
		}
		network.setChanged(true);
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		int id = Integer.parseInt(request.getParameter("id"));
		String type = request.getParameter("type");
		String name = request.getParameter("name");
		String value = request.getParameter("value");
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network = server.getNetworks().get(net);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    try {
		    String str = getJson(network, type, id, name, value);
		    response.getWriter().write(str);
	    }
    	catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
			Console.error(error.getMessage());
    	}
	}
}
