package http;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Console;
import common.ServerError;
import server.Server;

public class ReloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
      
	private void reloadNetworks(Server server) throws ServerError {
		server.getNetworks().clear();
		server.loadNetworks();
		server.setSelectedNode(null);
		server.getSelectedTenants().clear();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
	    response.setContentType("text/plain");
	    response.setCharacterEncoding("UTF-8");
		try {
			reloadNetworks(server);
		    response.getWriter().write("Networks reloaded\n");
		}
		catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
			Console.error(error.getMessage());
		}
	}
}
