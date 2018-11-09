package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Cloud {
	
	private int id;
	private String name;
	private String username;
	private int port;
	private String provider;
	private String identity;
	private String credential;
	private int securityLevel;
	private String keyFile;
	private boolean deployed;
	
	public Cloud(int id, String name, String provider, String username,	int port, String identity, 
			String credential, int securityLevel, String keyFile, boolean deployed) {
		this.id = id;
		this.name = name;
		this.provider = provider;
		this.username = username;
		this.port = port;
		this.identity = identity;
		this.credential = credential;
		this.securityLevel = securityLevel;
		this.keyFile = keyFile;
		this.deployed = deployed;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getProvider() {
		return provider;
	}

	public String getUsername() {
		return username;
	}

	public int getPort() {
		return port;
	}

	public String getIdentity() {
		return identity;
	}

	public String getCredential() {
		return credential;
	}
		
	public int getSecurityLevel() {
		return securityLevel;
	}

	public String getKeyFile() {
		return keyFile;
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("cloud");
		elem.setAttribute("id", Integer.toString(id));
		Config.writeXMLString(doc, elem, "name", name);
		Config.writeXMLString(doc, elem, "provider", provider);
		Config.writeXMLString(doc, elem, "username", username);
		Config.writeXMLInteger(doc, elem, "port", port);
		Config.writeXMLString(doc, elem, "identity", identity);
		Config.writeXMLString(doc, elem, "credential", credential);
		Config.writeXMLInteger(doc, elem, "security", securityLevel + 1);
		Config.writeXMLBoolean(doc, elem, "deployed", deployed);
		Config.writeXMLString(doc, elem, "key", keyFile);
		return elem;
	}
	
	public void toJson(ArrayList<Object> data) {
		data.add(Console.tuple("Cloud Name", name, true));
		data.add(Console.tuple("Cloud Provider", provider, true));
		data.add(Console.tuple("Security Level", securityLevel, new String[] 
				{ "Minimal Secured Cloud", "Default Cloud", 
						"Secured Cloud", "Highly Secured Cloud" }));
		data.add(Console.tuple("Is Deployed", deployed, false));
	}
	
	public boolean update(String name, String value, GsonData data) throws ServerError {
		switch (name) {
		case "Cloud Name": {
			this.name = value;
			data.addCloud(id, name, 0);
			return true;
		}
		case "Cloud Provider": {
			this.provider = value;
			return true;
		}
		case "Security Level": {
			this.securityLevel = Console.parse(value);
			return true;
		}
		default: 
			return false;
		}
	}
	
	public static int newId(Network network) {
		List<Integer> ids = new LinkedList<Integer>();
		for (Cloud cloud : network.getClouds())
			ids.add(cloud.getId());
		Collections.sort(ids);
		int index = 1;
		for (int id : ids)
			if (id != index)
				return index;
			else index++;
		return index;
	}
	
	public int getRate(Network network) {
		int total = 0, mapped = 0;
		for (Node node : network.getNodes()) {
			Cloud cloud = node.getVm().getCloud();
			if (node instanceof Host 
					&& node.isDeployed() 
					&& cloud.getId() == id) {
				if (((Host)node).getTenant() > 0)
					mapped++;
				total++;
			}
		}
		return (mapped * 100) / total;
	}
	
	public static Cloud findFromId(Network network, int cid) {
		for (Cloud cloud : network.getClouds())
			if (cloud.getId() == cid)
				return cloud;
		return null;
	}
	
	public static Vm findGateway(Network network, int cid) {
		for (Vm vm : network.getVms())
			if (vm.getCloud().getId() == cid 
					&& vm.isGateway() == true)
				return vm;
		return null;
	}
}
