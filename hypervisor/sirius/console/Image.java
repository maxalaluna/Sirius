package net.floodlightcontroller.sirius.console;

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Image {

	private String name;
	private String file;
	private ArrayList<String> script;
	private int priority;

	public Image(String name, String file, ArrayList<String> script, int priority) {
		this.name = name;
		this.file = file;
		this.script = script;
		this.priority = priority;
	}

	public String getName() {
		return name;
	}
	
	public String getFile() {
		return file;
	}
	
	public ArrayList<String> getScript() {
		return script;
	}
	
	public int getPriority() {
		return priority;
	}

	public Element toXML(Document doc) {
		Element elem = doc.createElement("image");
		Config.writeXMLString(doc, elem, "name", name);
		Config.writeXMLString(doc, elem, "file", file);
		for (String cmd : script)
			Config.writeXMLString(doc, elem, "script", cmd);
		Config.writeXMLInteger(doc, elem, "priority", priority);
		return elem;
	}
	
	public static String[] toArray(Network network) {
		ArrayList<Image> images = network.getImages();
		String[] tmp = new String[images.size()];
		for (int k = 0; k < images.size(); k++)
			tmp[k] = images.get(k).getName();
		return tmp;
	}
		
	public static int indexOf(Network network, String name) {
		ArrayList<Image> images = network.getImages();
		for (int k = 0; k < images.size(); k++) 
			if (images.get(k).getName().equals(name))
				return k;
		return -1;
	}
	
	public static Image findByName(Network network, String name) throws ServerError {
		int index = indexOf(network, name);
		if (index == -1)
			throw new ServerError("Image '" + name + "' not found");
		return network.getImages().get(index);
	}
}

