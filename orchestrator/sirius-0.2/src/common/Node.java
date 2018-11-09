package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class Node {

	private int id;
	private int index;
	private String name;
	private Vm vm;
	private boolean deployed;
	private boolean hidden;

	public Node(int id, int index, String name, 
			Vm vm, boolean deployed, boolean hidden) {
		this.id = id;
		this.index = index;
		this.name = name;
		this.vm = vm;
		this.deployed = deployed;
		this.hidden = hidden;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vm getVm() {
		return vm;
	}

	public void setVm(Vm vm) {
		this.vm = vm;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void setDeployed(boolean deployed) {
		this.deployed = deployed;
	}

	public abstract String getType();
	
	public abstract String getTitle();
	
	public abstract String getIcon();
	
	public Element toXML(Document doc) {
		Element elem = doc.createElement("node");
		elem.setAttribute("id", Integer.toString(id));
		Config.writeXMLString(doc, elem, "type", getType());
		Config.writeXMLInteger(doc, elem, "index", index);
		Config.writeXMLString(doc, elem, "name", name);
		Config.writeXMLBoolean(doc, elem, "deployed", deployed);
		Config.writeXMLBoolean(doc, elem, "hidden", hidden);
		Config.writeXMLInteger(doc, elem, "vid", vm.getId());
		return elem;
	}
	
	public void toJson(Network network, ArrayList<Object> data) {
		data.add(Console.tuple("Node Name", name, network.getTenant() > 0));
		data.add(Console.tuple("Is Deployed", deployed, false));
	}
	
	public boolean update(Network network, String name, String value, 
			GsonData data) throws ServerError {
		switch (name) {
		case "Node Name": {
			this.name = value;
			return true;
		}
		default:
			return false;
		}
	}
	
	public static int newId(Network network) {
		List<Integer> ids = new LinkedList<Integer>();
		for (Node node : network.getNodes()) 
			ids.add(node.getId());
		Collections.sort(ids);
		int index = 1;
		for (int id : ids)
			if (id != index)
				return index;
			else index++;
		return index;
	}
	
	public static Node findFromId(Network network, int id) {
		for (Node node : network.getNodes())
			if (node.getId() == id)
				return node;
		return null;
	}
	
	public ArrayList<Node> findPeers(Network network) {
		ArrayList<Node> tmp = new ArrayList<>();
		for (Link link : network.getLinks())
			if (link.getFrom() == this)
				tmp.add(link.getTo());
			else if (link.getTo() == this)
				tmp.add(link.getFrom());
		return tmp;
	}
	
	public Node getPeer(Network network, int index) {
		ArrayList<Node> peers = findPeers(network);
		return peers.get(index);
	}
}
