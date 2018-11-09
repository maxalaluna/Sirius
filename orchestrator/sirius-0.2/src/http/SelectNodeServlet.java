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
import common.Host;
import common.Link;
import common.Network;
import common.Node;
import common.Switch;
import server.Server;

public class SelectNodeServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private void selectNodes(Server server, int net, int id, GsonData data) {
		Node selected = server.getSelectedNode();
		if (selected != null) {
			if (!selected.isHidden())
				data.addNode(selected.getId(), selected.getVm().getId(), 
						selected.getVm().getCloud().getId(), selected.getName(), 
						selected.getTitle(), selected.getIcon(), false);
			server.setSelectedNode(null);
		}
		if (net > 0) {
			Network physical = server.getNetworks().get(0);
			Network virtual = server.getNetworks().get(net);
			for (Node node : virtual.getNodes()) {
				
				// Virtual host selected
				if (node instanceof Host) {
					int mapping = ((Host)node).getMapping();
					Node source = Node.findFromId(physical, mapping);
					if (source instanceof Host) {
						boolean isSelected = node.getId() == id;
						if (!source.isHidden())
							data.addNode(source.getId(), source.getVm().getId(), 
									source.getVm().getCloud().getId(), source.getName(), 
									source.getTitle(), source.getIcon(), isSelected);
						if (isSelected) server.setSelectedNode(source);
					}
				}
				
				// Virtual switch selected
				else if (node instanceof Switch) {
					int mapping = ((Switch)node).getMapping();
					Node source = Node.findFromId(physical, mapping);
					if (source instanceof Switch) {
						boolean isSelected = node.getId() == id;
						if (!source.isHidden())
							data.addNode(source.getId(), source.getVm().getId(), 
									source.getVm().getCloud().getId(), source.getName(), 
									source.getTitle(), source.getIcon(), isSelected);
						if (isSelected) server.setSelectedNode(source);
					}
				}
			}
		}
	}
	
	private void selectLinks(Server server, int net, boolean selected, GsonData data) {
		Network substrate = server.getNetworks().get(0);
		Network network = server.getNetworks().get(net);
		for (Link link : network.getLinks()) {
			if (link.getRoute() != null) {
				for (String str : link.getRoute().split(":")) {
					int mapping = Integer.parseInt(str);
					Link source = Link.findFromId(substrate, mapping);
					if (source != null 
							&& !source.getFrom().isHidden() 
							&& !source.getTo().isHidden()) {
						data.addLink(source.getId(), source.getTitle(), 
								source.getFrom().getId(), source.getTo().getId(), selected);						
					}
				}
			}
		}
	}
	
	private String getJson(Server server, int net, int id) {		
		GsonData data = new GsonData();
		Console.info("Selecting node " + id + " in tenant " + net);
		if (net == 0 && server.getSelectedTenants().size() > 0) {
			for (int tenant : server.getSelectedTenants()) 
				selectLinks(server, tenant, false, data);
			server.getSelectedTenants().clear();
		}
		else if (net > 0 && server.getSelectedTenants().indexOf(net) == -1) {
			selectLinks(server, net, true, data);
			server.getSelectedTenants().add(net);
		}
		selectNodes(server, net, id, data);
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		int id = Integer.parseInt(request.getParameter("id"));
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    String str = getJson(server, net, id);
	    response.getWriter().write(str);
	}
}
