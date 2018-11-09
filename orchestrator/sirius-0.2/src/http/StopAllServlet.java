package http;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import common.Cloud;
import common.Console;
import common.Network;
import common.ServerError;
import common.Vm;
import server.GsonStatus;
import server.Provider;
import server.PublicProvider;
import server.Server;

public class StopAllServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private ArrayList<Vm> vmToStop;
	private int index, length;
	
	private String nextAction() {
		if (vmToStop.size() > 0) {
			Vm vm = vmToStop.get(0);
			return "Stopping vm " + vm.getName();
		}
		return null;
	}

	private void performAction(Server server) throws ServerError {
		if (vmToStop.size() > 0) {
			Vm vm = vmToStop.remove(0);
			Cloud cloud = vm.getCloud();
			Provider provider = server.getProvider(cloud);
			((PublicProvider)provider).suspend(vm);
		}
		index++;
	}

	private void initialize(Server server) throws ServerError {
		Network network = server.getNetworks().get(0);
		for (Vm vm : network.getVms()) {
			if (vm.isDeployed()) {
				Cloud cloud = vm.getCloud();
				Provider provider = server.getProvider(cloud);
				if (provider instanceof PublicProvider) {
					vmToStop.add(vm);
					length++;
				}
			}
		}
	}
	
	private String getJson(Server server, boolean init) throws ServerError {
		if (init == true) {
			vmToStop = new ArrayList<>();
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
			if (server.getConfig().consoleDemoMode)
				throw new ServerError("Function disabled");
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
