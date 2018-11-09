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
import common.Switch;
import server.Server;

public class DeleteTenantServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static void clearNetwork(Server server, Network network) {
		Network physical = server.getNetworks().get(0);
		for (Node node: network.getNodes()) {
			if (node instanceof Host) {
				int id = ((Host)node).getMapping();
				Node source = Node.findFromId(physical, id);
				if (source instanceof Host) {
					((Host)source).setTenant(0);
					((Host)source).setIp(null);
					((Host)source).setMac(null);
					((Host)source).setPort(0);
				}
			}
			else if (node instanceof Switch) {
				int id = ((Switch)node).getMapping();
				Node source = Node.findFromId(physical, id);
				if (source instanceof Switch) {
					((Switch)source).setDpid(null);
				}
			}
		}
	}
	
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network = server.getNetworks().get(net);	
		try {
			if (server.getConfig().consoleDemoMode)
				throw new ServerError("Function disabled");
			clearNetwork(server, network);
			server.getNetworks().remove(net);
			response.setContentType("text/plain");
		    response.setCharacterEncoding("UTF-8");
	    	String str = "Remove tenant network " + net;
	     	response.getWriter().write(str);
	    	Console.info(str);
		}
		catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
			Console.error(error.getMessage());
		}
	}
}
