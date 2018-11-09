package net.floodlightcontroller.sirius.topology.xml;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class Builder {
	
	private Applet applet;
	
	private static final int ALIGN_MARGIN = 10;

	public Builder(Applet applet) {
		this.applet = applet;
	}
	
	public abstract Host newHost(int x, int y);
	public abstract Switch newSwitch(int x, int y);
	public abstract Link newLink(int from, int to);
	
	public void showHulls(Network network) {
	    Iterator<Hull> iter = network.getHullIterator();
	    while (iter.hasNext()) {
	    	Hull hull = (Hull)iter.next();
	    	ArrayList<Point> points = hull.getPoints();
	    	String color = Config.CLOUD_HULL_COLORS[hull.getId()];
	    	Point[] data = points.toArray(new Point[points.size()]);
	    	applet.jsRequestInitHull(hull.getId(), color);
		    applet.jsRequestSetHull(hull.getId(), data);	
	    }
	}
	
	public boolean createSwitch(Network network, int x, int y) {
		ArrayList<Integer> nodes = applet.jsRequestGetNodes(0);
		
		// Save context
		applet.config.saveContext(network, "Add switch");
		
		// Add new switch
		applet.log("Creating new switch", "...");
		Switch node = newSwitch(x, y);
		Boolean error = false;
		
		// Add dynamic links
		for (int id : nodes) {
			Node aux = network.getNodeById(id);
			if (aux instanceof Switch || aux instanceof Host)
				newLink(node.getId(), id);
			else error = true;
		}
		
		// Check error
		if (error == true) 
			Dialog.errorDialog("Cannot link controller");
		return !error;
	}
	
	public boolean createHost(Network network, int x, int y) {
		ArrayList<Integer> nodes = applet.jsRequestGetNodes(0);
		
		// Save context
		applet.config.saveContext(network, "Add host");

		// Add new host
		applet.log("Creating new host", "...");
		Host node = newHost(x, y);
		Boolean error = false;
		
		// Add dynamic links
		for (int id : nodes) {
			Node aux = network.getNodeById(id);
			if (aux instanceof Switch) 
				newLink(node.getId(), id);
			else error = true;
		}

		// Check error
		if (error == true)
			Dialog.errorDialog("Host must connect switch");
		return !error;
	}
	
	public boolean createLink(Network network, int from, int to) {
		Node node1 = network.getNodeById(from);
		Node node2 = network.getNodeById(to);
		boolean error = false;
		
		// Save context
		applet.config.saveContext(network, "Add link");
		
		// Add dynamic link
		if ((node1 instanceof Switch && node2 instanceof Switch)
				|| (node1 instanceof Switch && node2 instanceof Host)
				|| (node1 instanceof Host && node2 instanceof Switch)) 
			newLink(from, to);
		else error = true;

		// Check error
		if (error == true)
			Dialog.errorDialog("Link must connect switch or host");
		else applet.log("Creating new link", node1.getName() 
				+ "/" + node2.getName());
		return !error;
	}
	
	public boolean deleteComponents(Network network) {
		ArrayList<Integer> nodes = applet.jsRequestGetNodes(0);
		ArrayList<Integer> links = applet.jsRequestGetLinks(0);
		int deletedNodes = 0;
		int deletedLinks = 0;
		
		// Save context
		applet.config.saveContext(network, "Delete component");
		
		// Remove selected nodes
		for (int id : nodes) {
			Node node = network.getNodeById(id);
			Iterator<Link> iter = network.getLinkIterator();
			while (iter.hasNext()) {
				Link link = (Link)iter.next();
				if (link.getFrom() == id || link.getTo() == id) 
					links.add(link.getId());				
			}
			applet.jsRequestRemoveNode(0, node.getId());
			network.removeNode(node);
			deletedNodes++;
		}

		// Remove selected links
		for (int id : links) {
			Link link = network.getLinkById(id);
			if (link == null) continue;
			applet.jsRequestRemoveEdge(0, link.getId());
			network.removeLink(link);
			deletedLinks++;
		}
		
		// Check error status
		if (deletedNodes > 0)
			applet.log("Deleting", deletedNodes + " node(s)");
		if (deletedLinks > 0)
			applet.log("Deleting", deletedLinks + " link(s)");
		return deletedNodes > 0 || deletedLinks > 0;
	}
	
	private int modifyNodes(Network network) {
		ArrayList<Integer> nodes = applet.jsRequestGetNodes(0);
		Config config = applet.config;
		int modifiedNodes = 0;
		
		// Modify selected nodes
		for (int id: nodes) {
			Node node = network.getNodeById(id);

			// Switch
			if (node instanceof Switch && 
					Dialog.switchDialog(config, (Switch)node)) {
	    		
	    		// Update node in graph window
	    		applet.jsRequestSetNodeLabel(0, node.getId(), node.getName());
	    		applet.jsRequestSetNodeTitle(0, node.getId(), node.getTitle());
				modifiedNodes++;
			}
			
			// Host
			else if (node instanceof Host &&
					Dialog.hostDialog(config, (Host)node)) {
				
	    		// Update node in graph window
	    		applet.jsRequestSetNodeLabel(0, node.getId(), node.getName());
	    		applet.jsRequestSetNodeTitle(0, node.getId(), node.getTitle());
				modifiedNodes++;				
			}
			
			// Controller
			else if (node instanceof Controller && 
					Dialog.controllerDialog(config, (Controller)node)) {
	    		
	    		// Update node in graph window
	    		applet.jsRequestSetNodeLabel(0, node.getId(), node.getName());
	    		applet.jsRequestSetNodeTitle(0, node.getId(), node.getTitle());
				modifiedNodes++;
			}
		}
		return modifiedNodes;
	}
	
	private int modifyLinks(Network network) {
		ArrayList<Integer> links = applet.jsRequestGetLinks(0);
		Config config = applet.config;
		int modifiedLinks = 0;
		
		// Modify selected links
		for (int id : links) {
			Link link = network.getLinkById(id);
			Node node1 = network.getNodeById(link.getFrom());
			Node node2 = network.getNodeById(link.getTo());
			if (Dialog.linkDialog(config, link)) {
	    		
	    		// Update link in graph window
	    		applet.jsRequestSetEdgeTitle(0, link.getId(), link.getTitle());

	    		// Update link in Mininet at runtime
				if (applet.rest.mininetRunning == true)
					applet.rest.mnRequestSetLink(link, node1, node2);
				modifiedLinks++;	
			}
		}
		return modifiedLinks;
	}
	
	public void modifyComponents(Network network) {
		
		// Save context
		applet.config.saveContext(network, "Modify component");
		
		// Modify nodes
		int	modifiedNodes = modifyNodes(network);
		if (modifiedNodes > 0)
			applet.log("Modifying", modifiedNodes + " node(s)");

		// Modify links
		int modifiedLinks = modifyLinks(network);
		if (modifiedLinks > 0)
			applet.log("Modifying", modifiedLinks + " link(s)");
	}
	
	public boolean addStar(Network network) {
		ArrayList<Integer> nodes = applet.jsRequestGetNodes(0);
		Config config = applet.config;
		boolean modified = false;
		
		// Check node number
		if (nodes.size() != 1)
			Dialog.errorDialog("You must select one switch");
		else {
			Node node = network.getNodeById(nodes.get(0));
			if (!(node instanceof Switch))
				Dialog.errorDialog("You must select one switch");
			
			// Scan size, zoom and orientation
			else if (Dialog.starDialog(config)) {
				int starSize = config.defaultStarSize;
				int starLength = config.defaultStarLength;
				int starStart = config.defaultStarStart;
				int starEnd = config.defaultStarEnd;
				
				// Normalize start and end values
				if (starStart < 0) starStart = 360 - starStart;
				if (starEnd < 0) starEnd = 360 - starEnd;
				if (starStart > starEnd) {
					int tmp = starStart;
					starStart = starEnd;
					starEnd = tmp;
				}
				
				// Save context
				applet.config.saveContext(network, "Add host star");
							
	    		// Calculate incremental angle 
	    		double diff = (starEnd - starStart) * Math.PI / 180;
	    		double alpha = (2 * Math.PI - diff) / (starSize - 1);
	    		double angle = starStart * Math.PI / 180;
	    		modified = true;
	    		
	    		// Create host star
	    		for (int k = 0; k < starSize; k++) {
					
					// Calculate switch position
					int x = (int)(node.getX() + starLength * Math.cos(angle));
					int y = (int)(node.getY() + starLength * Math.sin(angle));
					Node host = newHost(x, y);
		    		
		    		// Link new switch
					newLink(node.getId(), host.getId());
			    	angle += alpha;
				}
			}
		}
		return modified;
	}
	
	public boolean alignComponents(Network network) {
		ArrayList<Integer> nodes = applet.jsRequestGetNodes(0);
			
		// Save context
		applet.config.saveContext(network, "Align nodes");
		
		// Align nodes
		for (int id1 : nodes) {
			Node node1 = network.getNodeById(id1);
			for (int id2 : nodes) {
				Node node2 = network.getNodeById(id2);
				int dx = node2.getX() - node1.getX();
				if (dx > 0 && dx < ALIGN_MARGIN)
					node2.setX(node1.getX());
				int dy = node2.getY() - node1.getY();
				if (dy > 0 && dy < ALIGN_MARGIN)
					node2.setY(node1.getY());
			}
		}
		
		// Update physical view
		applet.log("Aligning nodes", "...");
		return true;
	}
	
	public boolean rotateComponents(Network network) {
		ArrayList<Integer> nodes = applet.jsRequestGetNodes(0);
		
		// Scan angle value in radian
		double angle = Dialog.rotateDialog(90);
		
		// Angle > 0
		if (angle > 0) {
			
			// Save context
			applet.config.saveContext(network, "Rotate network");
			
			// Rotate nodes
			for (int id : nodes) {
				Node node = network.getNodeById(id);
				int x = (int)(node.getX() * Math.cos(angle) 
						- node.getY() * Math.sin(angle));
				int y = (int)(node.getX() * Math.sin(angle)
						+ node.getY() * Math.cos(angle));
				node.setX(x); node.setY(y);
			}
			
			// Update physical view
			applet.log("Rotating nodes", "...");
			return true;
		}
		return false;
	}
}
