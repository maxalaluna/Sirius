package http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import common.Config;
import common.Console;
import common.ServerError;
import server.Server;

public class LogoutServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");  
		PrintWriter out = response.getWriter();  
		HttpSession session = request.getSession(false);
		if (session != null) {
			Console.info("Server session is closing");
			ServletContext context = request.getServletContext();
			Server server = (Server)context.getAttribute("server");
			Config config = server.getConfig();
			if (config.consoleAutoSave && !config.consoleDemoMode) {
				Console.info("Starting autosave checking");
				try {
					SaveServlet.saveAll(server);
				}
				catch (ServerError error) {
					Console.error("Auto saving failed");
				}
			}	
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
			dispatcher.forward(request, response);
			session.invalidate(); 
		}
		out.close(); 
	}
}
