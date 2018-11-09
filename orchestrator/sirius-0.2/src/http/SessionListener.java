package http;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import common.Console;
import server.Server;

public class SessionListener implements HttpSessionListener {
	
	public void sessionCreated(HttpSessionEvent arg0) {
		Console.info("Opening session with client");
	}
	
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		ServletContext context = session.getServletContext();
		Server server = (Server)context.getAttribute("server");
		String username = (String)session.getAttribute("username");
		if (username != null) server.logout(username);
	}
}
