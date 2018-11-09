package http;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import common.Console;
import common.Host;
import common.Network;
import common.Node;
import common.ServerError;
import server.Docker;
import server.Server;

public class ExecuteServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
	private String execute(Server server, Network network, int id, String cmd) throws ServerError {
		Node node = Node.findFromId(network, id);
		if (node instanceof Host && node.isDeployed()) {
			int mapping = ((Host)node).getMapping();
			Network substrate = server.getNetworks().get(0);
			node = Node.findFromId(substrate, mapping);
			if (node != null) return Docker.run((Host)node, cmd);
			else throw new ServerError("Mapped node not found");
		}
		else throw new ServerError("Node must be a deployed host");
	}
	
	private String interrupt(Server server, Network network, int id) throws ServerError {
		Node node = Node.findFromId(network, id);
		if (node instanceof Host && node.isDeployed()) {
			int mapping = ((Host)node).getMapping();
			Network substrate = server.getNetworks().get(0);
			node = Node.findFromId(substrate, mapping);
			if (node != null && node.getVm().stopCmd())
				return "User interruption sent";
			else return "User interruption failed";
		}
		else throw new ServerError("Node must be a deployed host");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		int id = Integer.parseInt(request.getParameter("id"));
		String cmd = request.getParameter("cmd");		
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network = server.getNetworks().get(net);
	    response.setContentType("text/plain");
	    response.setCharacterEncoding("UTF-8");
	    String res;
	    try {
		    if (id == 0 && cmd.equals("stop"))
		    	res = interrupt(server, network, id);
		    else res = execute(server, network, id, cmd); 
		   	response.getWriter().write(res);
	    }
	    catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
	    	Console.error(error.getMessage());
	    }
	}
}
