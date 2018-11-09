package http;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import server.Server;

public class LoginServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		String adminPassword = server.getConfig().consoleAdminPassword;
		String adminUsername = server.getConfig().consoleAdminUsername;
		int sessionTimeout = server.getConfig().consoleSessionTimeout;
		RequestDispatcher dispatcher = null;
		if (username.equals(adminUsername) 
				&& password.equals(adminPassword)) {
			HttpSession session = request.getSession();
			session.setAttribute("username", username);
			session.setMaxInactiveInterval(sessionTimeout);
			server.login(username);
			dispatcher = request.getRequestDispatcher("admin.jsp");
		}
		else dispatcher = request.getRequestDispatcher("index.jsp");
		dispatcher.forward(request, response);
	}
}
