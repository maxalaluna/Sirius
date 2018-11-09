package net.floodlightcontroller.sirius.topology.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Config {
	
	// Current version
	public static final int MAJOR_VERSION = 0;
	public static final int MINOR_VERSION = 30;
	public static final int MICRO_VERSION = 0;
		
	// Default size, colors and widths
	public static final int NODE_NORMAL_SIZE = 25;
	public static final int NODE_ZOOMED_SIZE = 40;
	public static final String[] CLOUD_HULL_COLORS = {
			"#E0E0E0", "#FFFFCC", "#CCFFCC", "#FFCCE5" };
	public static final String[] EDGE_ROUTE_COLORS = { 
			"grey", "#3399FF", "#FF6666", "#99FF33" };
	public static final String EDGE_DISABLE_COLOR = "grey";
	public static final String EDGE_NORMAL_COLOR = "grey";
	public static final int EDGE_ROUTE_WIDTH = 3;
	public static final int EDGE_NORMAL_WIDTH = 2;
	
	// Default properties
	private static final String SWITCH_NETWORK_IP = "127.0.0.1";
	private static final String HOST_NETWORK_IP = "10.0.0.0";
	private static final String MAC_ADDRESS_BASE = "00:00:00:00:00:00";
	private static final String DEFAULT_PHYSICAL_FILE = "data/physical.xml";
	private static final String DEFAULT_VIRTUAL_FILE = "data/virtual.xml";
	private static final String DEFAULT_REQUEST_FILE = "data/request.xml";
	private static final String DEFAULT_ADMIN_FILE = "admin/config.xml";
	private static final String DEFAULT_USER_FILE = "user/config.xml";
	private static final String DEFAULT_CLOUD_FILE = "data/clouds.xml";
	private static final String DEFAULT_CLOUD_USER = "mininet";
	private static final String DEFAULT_CLOUD_PASSWORD = "mininet";
	private static final String MININET_REST_IP = "127.0.0.1";
	private static final int MININET_REST_PORT = 8081;
	private static final boolean LOAD_STARTUP_CONFIG = false;
	private static final boolean SPANNING_TREE = false;
	private static final boolean DEMO_MODE = false;
	private static final int MAPPING_REFRESH_FREQ = 1000;
	private static final int MAPPING_REFRESH_TIMEOUT = 10;
	private static final int CONTEXT_UNDO_SIZE = 8;
	private static final int TENANT_MAX_NUMBER = 2;
	private static final int CLOUD_MAX_NUMBER = 2;
	private static final int DEFAULT_STAR_SIZE = 8;
	private static final int DEFAULT_STAR_LENGTH = 200;
	private static final int DEFAULT_STAR_START = 315;
	private static final int DEFAULT_STAR_END = 45;
	private static final int DEFAULT_LINK_BAND = 10;
	private static final int DEFAULT_LINK_DELAY = 1;	
	private static final int DEFAULT_LINK_LOSS = 0;
	private static final String DEFAULT_CONTROLLER_IP = "127.0.0.1";
	private static final int DEFAULT_CONTROLLER_PORT = 6633;
	private static final int DEFAULT_OPENFLOW_VERSION = 3;
	private static final int DEFAULT_NODE_SECURITY = 1;
	private static final int DEFAULT_LINK_SECURITY = 1;
	private static final int DEFAULT_NODE_DEPENDABILITY = 1;
	private static final int DEFAULT_CLOUD_TYPE = 1;
	private static final int DEFAULT_FLOWS_MAXIMUM = 1000;
	private static final int DEFAULT_NODE_CPU = 100;
	private static final int DEFAULT_NODE_MEMORY = 100;
	private static final String SCP_CLIENT_IP = "127.0.0.1";
	private static final String SCP_CLIENT_USERNAME = "mininet";
	private static final String SCP_PHYSICAL_FILE = "scp_physical.xml";
	private static final String SCP_REQUEST_FILE = "scp_request.xml";
	
	// Property values
	public String switchNetIp;
	public String hostNetIp;
	public String macAdressBase;
	public String defaultPhysicalFile;
	public String defaultVirtualFile;
	public String defaultRequestFile;
	public String defaultAdminFile;
	public String defaultUserFile;
	public String defaultCloudFile;
	public String defaultCloudUser;
	public String defaultCloudPassword;
	public int mininetRestPort;
	public String mininetRestIp;
	public boolean loadStartupConfig;
	public boolean spanningTree;
	public boolean demoMode;
	public int mappingRefreshFreq;
	public int mappingRefreshTimeout;
	public int contextUndoSize;
	public int tenantMaxNumber;
	public int cloudMaxNumber;
	public int defaultStarSize;
	public int defaultStarLength;
	public int defaultStarStart;
	public int defaultStarEnd;
	public int defaultLinkBand;
	public int defaultLinkDelay;
	public int defaultLinkLoss;
	public String defaultControllerIp;
	public int defaultControllerPort;
	public int defaultOpenFlowVersion;
	public int defaultNodeSecurity;
	public int defaultLinkSecurity;
	public int defaultNodeDependability;
	public int defaultCloudType;
	public int defaultFlowsMaximum;
	public int defaultNodeCpu;
	public int defaultNodeMemory;
	public String scpClientIp;
	public String scpClientUsername;
	public String scpPhysicalFile;
	public String scpRequestFile;
	
	// Node and cloud lists
	private LinkedList<Context> contexts;
	private ArrayList<Cloud> clouds;
	
	private class Context {
		
		private Document doc;
		private String action;
		
		public Context(Document doc, String action) {
			this.doc = doc;
			this.action = action;
		}

		public Document getDoc() {
			return doc;
		}

		public String getAction() {
			return action;
		}
	}
	
	public Config() {
		contexts = new LinkedList<Context>();
    	clouds = new ArrayList<>();
	}
	
	public Iterator<Cloud> getCloudIterator() {
		return clouds.iterator();
	}
	
	private String readPropString(Properties prop, String name, String defValue) {
		return prop.getProperty(name, defValue);
	}
	
	private int readPropInteger(Properties prop, String name, int defValue) {
		String content = prop.getProperty(name, Integer.toString(defValue));
		return Integer.parseInt(content);
	}
	
	private boolean readPropBoolean(Properties prop, String name, boolean defValue) {
		String content = prop.getProperty(name, Boolean.toString(defValue));
		return Boolean.parseBoolean(content);		
	}
	
	public void readProperties(InputStream is) {	
		Properties prop = new Properties(); 
		try {
			prop.load(is);
			switchNetIp = readPropString(prop, "switch_network_ip", SWITCH_NETWORK_IP);
			hostNetIp = readPropString(prop, "host_network_ip", HOST_NETWORK_IP);
			macAdressBase = readPropString(prop, "mac_address_base", MAC_ADDRESS_BASE);
			mininetRestPort = readPropInteger(prop, "mininet_rest_port", MININET_REST_PORT);
			mininetRestIp = readPropString(prop, "mininet_rest_ip", MININET_REST_IP);
			loadStartupConfig = readPropBoolean(prop, "load_startup_config", LOAD_STARTUP_CONFIG);
			spanningTree = readPropBoolean(prop, "spanning_tree", SPANNING_TREE);
			demoMode = readPropBoolean(prop, "demo_mode", DEMO_MODE);
			mappingRefreshFreq = readPropInteger(prop, "mapping_refresh_freq", MAPPING_REFRESH_FREQ);
			mappingRefreshTimeout = readPropInteger(prop, "mapping_refresh_timeout", MAPPING_REFRESH_TIMEOUT);
			contextUndoSize = readPropInteger(prop, "context_undo_size", CONTEXT_UNDO_SIZE);
			tenantMaxNumber = readPropInteger(prop, "tenant_max_number", TENANT_MAX_NUMBER);
			cloudMaxNumber = readPropInteger(prop, "cloud_max_number", CLOUD_MAX_NUMBER);
			defaultStarSize = readPropInteger(prop, "default_star_size", DEFAULT_STAR_SIZE);
			defaultStarLength = readPropInteger(prop, "default_star_length", DEFAULT_STAR_LENGTH);
			defaultStarStart = readPropInteger(prop,  "default_star_start", DEFAULT_STAR_START);
			defaultStarEnd = readPropInteger(prop,  "default_star_end", DEFAULT_STAR_END);
			defaultPhysicalFile = readPropString(prop, "default_physical_file", DEFAULT_PHYSICAL_FILE);
			defaultVirtualFile = readPropString(prop, "default_virtual_file", DEFAULT_VIRTUAL_FILE);
			defaultRequestFile = readPropString(prop, "default_request_file", DEFAULT_REQUEST_FILE);
			defaultAdminFile = readPropString(prop, "default_admin_file", DEFAULT_ADMIN_FILE);
			defaultUserFile = readPropString(prop, "default_user_file", DEFAULT_USER_FILE);
			defaultCloudFile = readPropString(prop, "default_cloud_file", DEFAULT_CLOUD_FILE);
			defaultCloudUser = readPropString(prop, "default_cloud_user", DEFAULT_CLOUD_USER);
			defaultCloudPassword = readPropString(prop, "default_cloud_password", DEFAULT_CLOUD_PASSWORD);
			defaultLinkBand = readPropInteger(prop, "default_link_bandwidth", DEFAULT_LINK_BAND);
			defaultLinkDelay = readPropInteger(prop, "default_link_delay", DEFAULT_LINK_DELAY);
			defaultLinkLoss = readPropInteger(prop, "default_link_lossrate", DEFAULT_LINK_LOSS);
			defaultControllerIp = readPropString(prop, "default_controller_ip", DEFAULT_CONTROLLER_IP);
			defaultControllerPort = readPropInteger(prop, "default_controller_port", DEFAULT_CONTROLLER_PORT);
			defaultOpenFlowVersion = readPropInteger(prop, "default_openflow_version", DEFAULT_OPENFLOW_VERSION);
			defaultNodeSecurity = readPropInteger(prop, "default_node_security", DEFAULT_NODE_SECURITY);
			defaultLinkSecurity = readPropInteger(prop, "default_link_security", DEFAULT_LINK_SECURITY);
			defaultNodeDependability = readPropInteger(prop, "default_node_dependability", DEFAULT_NODE_DEPENDABILITY);
			defaultCloudType = readPropInteger(prop, "default_cloud_type", DEFAULT_CLOUD_TYPE);
			defaultFlowsMaximum = readPropInteger(prop, "default_flows_maximum", DEFAULT_FLOWS_MAXIMUM);
			defaultNodeCpu = readPropInteger(prop, "default_node_cpu", DEFAULT_NODE_CPU);
			defaultNodeMemory = readPropInteger(prop, "default_node_memory", DEFAULT_NODE_MEMORY);
			scpClientIp = readPropString(prop, "scp_client_ip", SCP_CLIENT_IP);
			scpClientUsername = readPropString(prop, "scp_client_username", SCP_CLIENT_USERNAME);
			scpPhysicalFile = readPropString(prop, "scp_physical_file", SCP_PHYSICAL_FILE);
			scpRequestFile = readPropString(prop, "scp_request_file", SCP_REQUEST_FILE);
			is.close();
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Document loadDocument(InputStream is) {
		Document doc = null;
		try {
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//	    	while (true){
//				if (is.available() != 0){
//					break;
//				}
//			}
	    	doc = dBuilder.parse(is);
	    	doc.getDocumentElement().normalize();
		} 
		catch (ParserConfigurationException | IOException | SAXException ex) {
			//ex.printStackTrace();
			return null;
		}
		return doc;
	}

	public static void saveDocument(Document doc, StreamResult result) {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);				 
			transformer.transform(source, result);
		} 
		catch (TransformerException ex) {
			ex.printStackTrace();
		}
	}
	
	public void readClouds(Document doc) {
    	NodeList nodeLst = doc.getElementsByTagName("cloud");
    	for (int k = 0; k < nodeLst.getLength(); k++) {
    		Element elem = (Element)nodeLst.item(k);
    		int id = Integer.parseInt(elem.getAttribute("id"));
    		int type = Config.readXMLInteger(elem, "type", defaultCloudType);
    		String user = Config.readXMLString(elem, "user", defaultCloudUser);
    		String password = Config.readXMLString(elem, "password", defaultCloudPassword);
    		String description = Config.readXMLString(elem, "desc", null);
    		clouds.add(new Cloud(id, type, user, password, description));
    	}
    	System.out.println("Total clouds loaded : " + nodeLst.getLength());
	}
	
	public Document writeClouds() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();

			// Root elements
			doc = docBuilder.newDocument();
			Element root = doc.createElement("config");
			doc.appendChild(root);
					
			// Add cloud elements
			for (Cloud cloud : clouds) {
				Element aux = cloud.toXML(doc);
				root.appendChild(aux);
			}
		} 
		catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		return doc;		
	}
	
	public String createSwitchIp(int index) {
		if (switchNetIp.equals("127.0.0.1")) return switchNetIp;
		return Toolbox.createIP(switchNetIp, index);
	}

	public String createHostIp(int index) {
		if (hostNetIp.equals("127.0.0.1")) return hostNetIp;
		return Toolbox.createIP(hostNetIp, index);
	}
	
	public static void writeXMLString(Document doc, Element elem, String name, String value) {
		Element aux = doc.createElement(name);
		aux.appendChild(doc.createTextNode(value));
		elem.appendChild(aux);
	}
	
	public static void writeXMLInteger(Document doc, Element elem, String name, int value) {
		Element aux = doc.createElement(name);
		aux.appendChild(doc.createTextNode(Integer.toString(value)));
		elem.appendChild(aux);
	}

	public static void writeXMLBoolean(Document doc, Element elem, String name, boolean value) {
		Element aux = doc.createElement(name);
		aux.appendChild(doc.createTextNode(Boolean.toString(value)));
		elem.appendChild(aux);
	}
	
	public static String readXMLString(Element elem, String name, String defValue) {
		NodeList nodes = elem.getElementsByTagName(name);
		if (nodes.getLength() == 0) return defValue;
		return nodes.item(0).getTextContent();
	}
	
	public static int readXMLInteger(Element elem, String name, int defValue) {
		NodeList nodes = elem.getElementsByTagName(name);
		if (nodes.getLength() == 0) return defValue;
		String content = nodes.item(0).getTextContent();
		return Integer.parseInt(content);
	}

	public static boolean readXMLBoolean(Element elem, String name, boolean defValue) {
		NodeList nodes = elem.getElementsByTagName(name);
		if (nodes.getLength() == 0) return defValue;
		String content = nodes.item(0).getTextContent();
		return Boolean.parseBoolean(content);
	}
	
	public String[] listContext() {
		String[] lst = new String[contexts.size()];
		for (int k = 0; k < contexts.size(); k++)
			lst[k] = Integer.toString(k + 1) + " : " 
					+ contexts.get(k).getAction();
		return lst;
	}
	
    public String[] getCloudNames(Config config) {
    	String[] names = new String[clouds.size()];
    	for (int k = 0; k < clouds.size(); k++)
    		names[k] = clouds.get(k).getDescription();
    	return names;
    }
    
	public void saveContext(Network base, String action) {
		Document doc = base.writeDocument(this, false);
		contexts.addFirst(new Context(doc, action));
		if (contexts.size() > contextUndoSize)
			contexts.removeLast();
	}
	
	public void restoreContext(Network base, int pos) {
		Document doc = contexts.get(pos).getDoc();
		System.out.println("Restoring context");
		base.readDocument(this, doc);
		
		// Remove unused contexts
		for (int k = 0; k <= pos; k++)
			contexts.removeFirst();
	}
}
