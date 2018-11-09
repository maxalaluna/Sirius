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
import common.Network;
import common.ServerError;
import server.Server;

public class SyncServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private String getJson(Server server) throws ServerError, IOException {
		Network network = server.getNetworks().get(0);
		GsonData data = network.toData();
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
		try {
			String str = getJson(server);
			response.getWriter().write(str);
		}
		catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
			Console.error(error.getMessage());
		}
	}
}
