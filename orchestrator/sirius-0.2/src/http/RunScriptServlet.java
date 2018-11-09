package http;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import common.Console;
import common.ServerError;
import server.GsonStatus;
import server.Server;

public class RunScriptServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private int index, length;
	
	private String nextAction() {
		if (index < length) {
			return "Running action";
		}
		return null;
	}
	
	private void performAction(Server server) throws ServerError {
		index++;
	}
	
	private void initialize(Server server) throws ServerError {
		length = 3;
	}
	
	private String getJson(Server server, boolean init) throws ServerError {
		if (init == true) {
			index = length = 0;
			initialize(server);
		}
		else performAction(server);
		String action = nextAction();
		GsonStatus status = new GsonStatus(index, length, action, null);
		return new Gson().toJson(status);
	}
	
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		boolean init = Boolean.parseBoolean(request.getParameter("init"));
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    try {
			String str = getJson(server, init);
			response.getWriter().write(str);
	    }
    	catch (ServerError error) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(error.getMessage());
			Console.error(error.getMessage());
    	}
	}
}
