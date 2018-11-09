package http;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import common.ServerError;
import server.Server;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		try {
			Server server = new Server();
			context.setAttribute("server", server);
		} 
		catch (ServerError ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext context = event.getServletContext();	
		try {
			Server server = (Server)context.getAttribute("server");
			server.close();
		} 
		catch (ServerError ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
}
