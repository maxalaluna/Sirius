package http;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Cloud;
import common.Config;
import common.GsonData;
import common.Host;
import common.Link;
import common.Network;
import common.Node;
import common.Switch;
import common.Vm;
import server.Server;

public class AddNodeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
       
	private Vm newVM(Server server, Network network, Cloud cloud, GsonData data) {
		Config config = server.getConfig();
		Vm vm = network.addDefaultVm(config, cloud);
		data.addVM(vm.getId(), -cloud.getId(), vm.getName());
		return vm;
	}
	
	private Switch addSwitch(Server server, Network network, Switch node, GsonData data) {
		Config config = server.getConfig();
		Cloud cloud = node.getVm().getCloud();
		if (!cloud.isDeployed())
			cloud = network.addDefaultCloud();
		Vm vm = newVM(server, network, cloud, data);
		Switch ovs = network.addDefaultSwitch(config, vm);
		data.addNode(ovs.getId(), vm.getId(), cloud.getId(), ovs.getName(), 
				ovs.getTitle(), ovs.getIcon(), false);
		Link link = network.addDefaultLink(config, node, ovs);
		data.addLink(link.getId(), link.getTitle(), 
				node.getId(), ovs.getId(), false);
		return ovs;
	}

	private void addVM(Server server, Network network, Switch node, int nb1, GsonData data) {
		Switch ovs = addSwitch(server, network, node, data);
		int max = server.getConfig().maxHostsPerSwitch;
		for (int k = 0; k < nb1; k++) {
			addHost(server, network, ovs, data, k >= max);
		}
	}

	private void addHost(Server server, Network network, Switch ovs, GsonData data, boolean hidden) {
		Config config = server.getConfig();
		Vm vm = ovs.getVm();
		Host node = network.addDefaultHost(config, vm);
		Link link = network.addDefaultLink(config, node, ovs);
		if (hidden == false) {
			data.addNode(node.getId(), vm.getId(), vm.getCloud().getId(), node.getName(), 
					node.getTitle(), node.getIcon(), false);
			data.addLink(link.getId(), link.getTitle(), 
					node.getId(), ovs.getId(), false);
		}
		node.setHidden(hidden);
	}
	
	private String getJson(Server server, Network network, int id, int nb1, int nb2) {
		Node node = Node.findFromId(network, id);
		Switch ovs = node.getVm().findSwitch(network);
		GsonData data = new GsonData();
		
		// Add VMs
		if (nb2 > 0)
			for (int k = 0; k < nb2; k++)
				addVM(server, network, ovs, nb1, data);
		
		// Add containers
		else {
			int max = server.getConfig().maxHostsPerSwitch;
			int nb = node.getVm().findContainers(network).size();
			for (int k = 0; k < nb1; k++) {
				boolean hidden = (nb + k >= max);
				addHost(server, network, ovs, data, hidden);
			}
		}
		return new Gson().toJson(data);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int net = Integer.parseInt(request.getParameter("net"));
		int id = Integer.parseInt(request.getParameter("id"));
		int nb1 = Integer.parseInt(request.getParameter("nb1"));
		int nb2 = Integer.parseInt(request.getParameter("nb2"));		
		ServletContext context = request.getServletContext();
		Server server = (Server)context.getAttribute("server");
		Network network = server.getNetworks().get(net);
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    String str = getJson(server, network, id, nb1, nb2);
	    response.getWriter().write(str);
	}
}
