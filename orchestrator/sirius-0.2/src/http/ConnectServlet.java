package http;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Cloud;
import common.Console;
import common.Host;
import common.Network;
import common.Node;
import common.ServerError;
import server.Provider;
import server.Server;

public class ConnectServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private String test(Server server, Network network, int id) throws ServerError {
		StringBuffer buffer = new StringBuffer();
		Node node = Node.findFromId(network, id);
		if (node instanceof Host && node.isDeployed()) {
			int mapping = ((Host)node).getMapping();
			Network physical = server.getNetworks().get(0);
			node = Node.findFromId(physical, mapping);
			if (node != null) {
				Cloud cloud = node.getVm().getCloud();
				Provider provider = server.getProvider(cloud);
				provider.testConnectivity(buffer, (Host)node);
			}
			else throw new ServerError("Mapped node not found");
		}
		else throw new ServerError("Node must be a deployed host");
		return buffer.toString();
	}
		
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		int id = Integer.parseInt(request.getParameter("id"));
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network = server.getNetworks().get(net);
	    response.setContentType("text/plain");
	    response.setCharacterEncoding("UTF-8");
	    try {
			String res = test(server, network, id); 
			response.getWriter().write(res);
	    }
	    catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
	    	Console.error(error.getMessage());
	    }
	}
}
