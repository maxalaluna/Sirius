package http;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Controller;
import common.Network;
import common.Node;
import server.Listener;
import server.Server;

public class EditNodeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
	private ArrayList<Object> addNodeInfo(Network network, Node node) {
		ArrayList<Object> data = new ArrayList<>();
		node.toJson(network, data);
		return data;
	}
	
	private ArrayList<Object> addVMInfo(Node node) {
		ArrayList<Object> data = new ArrayList<>();
		node.getVm().toJson(data);
		return data;
	}
	
	private ArrayList<Object> addCloudInfo(Node node) {
		ArrayList<Object> data = new ArrayList<>();
		node.getVm().getCloud().toJson(data);
		return data;
	}
	
	private String getJson(Server server, Network network, int id) {
		ArrayList<Object> data = new ArrayList<>();
		Node node = Node.findFromId(network, id);
		
		// Update controller status
		if (node instanceof Controller) {
			Listener listener = server.getListener();
			boolean connected = listener.isConnected();
			((Controller)node).setConnected(connected);
		}
		
		// Add attributes
		data.add(addNodeInfo(network, node));
		data.add(addVMInfo(node));
		data.add(addCloudInfo(node));
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
