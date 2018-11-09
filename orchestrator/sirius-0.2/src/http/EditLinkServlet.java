package http;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Link;
import common.Network;
import server.Server;

public class EditLinkServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private ArrayList<Object> addLinkInfo(Link link) {
		ArrayList<Object> data = new ArrayList<>();
		link.toJson(data);
		return data;
	}
	
	private String getJson(Server server, Network network, int id) {
		ArrayList<Object> data = new ArrayList<>();
		Link link = Link.findFromId(network, id);

		// Add attributes
		data.add(addLinkInfo(link));
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		int id = Integer.parseInt(request.getParameter("id"));
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network = server.getNetworks().get(net);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    String str = getJson(server, network, id);
	    response.getWriter().write(str);
	}
}
