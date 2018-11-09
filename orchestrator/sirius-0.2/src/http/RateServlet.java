package http;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Cloud;
import common.GsonData;
import common.Network;
import server.Server;

public class RateServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
    
	private String getJson(Network network) {
		GsonData data = new GsonData();
		for (Cloud cloud : network.getClouds()) {
			int rate = cloud.getRate(network);
			data.addCloud(cloud.getId(), cloud.getName(), rate);
		}
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network = server.getNetworks().get(0);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    String str = getJson(network);
	    response.getWriter().write(str);
	}
}
