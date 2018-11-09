package http;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Console;
import common.Network;
import common.ServerError;
import server.Server;

public class SaveServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       	
	public static void saveAll(Server server) throws ServerError {
		String prefix = server.getConfig().consoleFilePrefix;
		String path = Console.getPath("xml");
		ArrayList<Integer> ids = new ArrayList<>();
		for (int key : server.getNetworks().keySet()) {
			Network network = server.getNetworks().get(key);
			if (network.isChanged()) {
				Console.info("Saving file network" + key);
				network.writeXML(path + "/" + prefix + key + ".xml");
			}
			ids.add(key);
		}
		
		// Delete unused tenants
		for (File file : new File(path).listFiles()) {
			if (file.getName().startsWith(prefix)) {
				int pos = file.getName().indexOf('.');
				String str = file.getName().substring(prefix.length(), pos);
				int key = Integer.parseInt(str);
				if (ids.indexOf(key) == -1) {
					Console.info("Deleting file network" + key);
					file.delete();
				}
			}
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
	    response.setContentType("text/plain");
	    response.setCharacterEncoding("UTF-8");
		try {
			if (server.getConfig().consoleDemoMode)
				throw new ServerError("Function disabled");
			saveAll(server);
			response.getWriter().write("Networks saved");
		}
		catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
			Console.error(error.getMessage());
		}
	}
}
