package net.floodlightcontroller.sirius.topology.xml;

import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.xml.transform.stream.StreamResult;
import java.util.StringTokenizer;
import org.w3c.dom.Document;

import netscape.javascript.JSObject;

public class Applet extends JApplet {

	private static final long serialVersionUID = 1L;

	public Config config;
	public Rest rest;
	
	public void init() {
    	System.out.println("Console version " 
    			+ Config.MAJOR_VERSION + "." 
    			+ Config.MINOR_VERSION + "." 
    			+ Config.MICRO_VERSION);
		Document doc;
		InputStream is;
    	
    	// Read properties
    	config = new Config();
		is = openInput("config.properties");
    	config.readProperties(is);
    	closeInput(is);
    	
       	// Read cloud information
    	is = openInput(config.defaultCloudFile);
		doc = Config.loadDocument(is);
		config.readClouds(doc);
		closeInput(is);
	}
	
	public void log(String title, String data) {
		System.out.println(title + " " + data);
	}
	
	public Image loadImage(String name) {
		String path = "img/" + name + ".png";
		return getImage(getDocumentBase(), path);
	}
	
	public ImageIcon loadIcon(String name, int width, int height) {
		Image img = loadImage(name).getScaledInstance(width, 
				height, java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(img);
	}
	
	public String getLastDate(String path) throws IOException {
		URL url = new URL(getDocumentBase(), path);
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		return connection.getHeaderField("Last-Modified");
	}
	
	public InputStream openInput(String path) {	
    	try {
    		URL url = new URL(getDocumentBase(), path);
    		System.out.println("Loading " + path);
    		URLConnection connection = url.openConnection();
    		connection.setUseCaches(false);
			return connection.getInputStream();
		} 
    	catch (IOException ex) {
			ex.printStackTrace();
		}
    	return null;
	}
	
	public void closeInput(InputStream is) {
		try {
			is.close();
		} 
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void saveDocument(Document doc, String path) {
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		System.out.println("Writing " + path);
		Config.saveDocument(doc, result);
		String content = writer.toString();
		rest.mnRequestWriteFile(path, content);		
	}
	
	public void sendFile(String src, String dst) {
		String url = String.format("%s@%s:%s" , 
				config.scpClientUsername, 
				config.scpClientIp, dst);
		rest.mnRequestScpCommand(src, url);
	}
	
	public String getParam(String name) {
		String url = getDocumentBase().toString();
		if (url.indexOf("?") == -1) return null;
		String parameters = url.substring(url.indexOf("?") + 1);
		StringTokenizer group = new StringTokenizer(parameters, "&");
		while (group.hasMoreTokens()) {
			StringTokenizer value = new StringTokenizer(group.nextToken(), "=");
			if (value.nextToken().equals(name)) return value.nextToken();
		}
		return null;
	}
	
    // Network Javascript primitives
	
	public void jsRequestAddNode(int index, int id, String label,
			String title, int x, int y, String image) {
		JSObject window = JSObject.getWindow(this);
		window.call("addNode", index, id, label, 
				title, x, y, image);
	}
	
	public void jsRequestSetNodeTitle(int index, int id, String title) {
		JSObject window = JSObject.getWindow(this);
		window.call("setNodeTitle", index, id, title);
	}
	
	public void jsRequestSetNodeLabel(int index, int id, String label) {
		JSObject window = JSObject.getWindow(this);
		window.call("setNodeLabel", index, id, label);
	}
	
	public void jsRequestSetNodePosition(int index, int id, int x, int y) {
		JSObject window = JSObject.getWindow(this);
		window.call("setNodePosition", index, id, x, y);
	}

	public void jsRequestSetNodeImage(int index, int id, String image, int size) {
		JSObject window = JSObject.getWindow(this);
		window.call("setNodeImage", index, id, image, size);
	}
	
	public void jsRequestRemoveNode(int index, int id) {
		JSObject window = JSObject.getWindow(this);
		window.call("removeNode", index, id);
	}
	
	public void jsRequestAddEdge(int index, int id, String title, 
			int from, int to, String color, int width) {
		JSObject window = JSObject.getWindow(this);
		window.call("addEdge", index, id, title, 
				from, to, color, width);
	}
	
	public void jsRequestSetEdgeTitle(int index, int id, String title) {
		JSObject window = JSObject.getWindow(this);
		window.call("setEdgeTitle", index, id, this);
	}
	
	public void jsRequestSetEdgeColor(int index, int id, String color, 
			String arrows, boolean dashes, int width) {
		JSObject window = JSObject.getWindow(this);
		window.call("setEdgeColor", index, id, color, 
				arrows, dashes, width);
	}
	
	public void jsRequestRemoveEdge(int index, int id) {
		JSObject window = JSObject.getWindow(this);
		window.call("removeEdge", index, id);
	}
	
	public ArrayList<Integer> jsRequestGetNodes(int index) {
		JSObject window = JSObject.getWindow(this);
		JSObject obj = (JSObject)window.call("getSelectedNodes", index);
		Number len = (Number)obj.getMember("length");
		ArrayList<Integer> lst = new ArrayList<Integer>(); 
		for (int k = 0; k < len.intValue(); k++)
			lst.add((Integer)obj.getSlot(k));
		return lst;
	}
	
	public ArrayList<Integer> jsRequestGetLinks(int index) {
		JSObject window = JSObject.getWindow(this);
		JSObject obj = (JSObject)window.call("getSelectedEdges", index);
		Number len = (Number)obj.getMember("length");
		ArrayList<Integer> lst = new ArrayList<Integer>(); 
		for (int k = 0; k < len.intValue(); k++)
			lst.add((Integer)obj.getSlot(k));
		return lst;
	}
	
	public void jsRequestSelectAllNodes() {
		JSObject window = JSObject.getWindow(this);
		window.eval("selectAllNodes()");
	}
	
	public void jsRequestClearNetwork(int index) {
		JSObject window = JSObject.getWindow(this);
		window.call("clearNetwork", index);
    }

	public void jsRequestClearHulls() {
		JSObject window = JSObject.getWindow(this);
		window.call("clearHulls");    	
    }
    
    public void jsRequestInitHull(int index, String color) {
		JSObject window = JSObject.getWindow(this);
		window.call("initHull", index, color);    	
    }
    
    public void jsRequestSetHull(int index, Point[] points) {
		JSObject window = JSObject.getWindow(this);
		window.call("setHull", index, points);
	}

    public void jsRequestSetPhysics(boolean enabled) {
		JSObject window = JSObject.getWindow(this);
		window.call("setPhysics", enabled);
	}
	
    public void jsRequestSetMininet(boolean enabled) {
		JSObject window = JSObject.getWindow(this);
		window.call("setMininet", enabled);	
	}
	
    public void jsRequestSetQueue(int index, boolean enabled) {
		JSObject window = JSObject.getWindow(this);
		window.call("setQueue", index, enabled);			
	}
	
    public void jsRequestFitNetwork(int index) {
		JSObject window = JSObject.getWindow(this);
		window.call("fitNetwork", index);					
	}
}
