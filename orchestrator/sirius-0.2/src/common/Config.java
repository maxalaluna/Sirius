package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

	public static final int MAJOR_VERSION = 0;
	public static final int MINOR_VERSION = 96;

	public String consoleAdminUsername;
	public String consoleAdminPassword;
	public int consoleSessionTimeout;
	public int consoleConnectTimeout;
	public int consoleListeningPort;
	public int consoleControllerTimeout;
	public boolean consoleWakeupMode;
	public boolean consoleDiscoverMode;
	public int maxHostsPerSwitch;
	public int consoleSshMaxTries;
	public String consoleAdminIp;
	public boolean consoleDemoMode;
	public boolean consoleUpdateScripts;
	public boolean consoleSimulationMode;
	public boolean consoleDeepSync;
	public String consoleFilePrefix;
	public boolean consoleAutoSave;
	public boolean publicCloudSync;
	public boolean privateCloudSync;
	public String defaultDockerPrefix;
	public String defaultDockerImage;
	public int defaultOpenflowVersion;
	public String defaultBridgeName;
	public String defaultIpRange;
	public String defaultMacRange;
	public int defaultMaxFlowSize;
	public String defaultPublicIp;
	public String defaultPrivateIp;
	public int defaultLinkBandwidth;
	public int defaultLinkDelay;
	public int defaultLinkLossRate;
	
	public Config(String path) throws ServerError {
		try {
			Console.info("Reading console properties");
			InputStream is = new FileInputStream(path);
			Properties prop = new Properties(); 
			prop.load(is);
			
			// Read properties
			consoleAdminUsername = getPropertyString(prop, "console_admin_username");
			consoleAdminPassword = getPropertyString(prop, "console_admin_password");
			consoleSessionTimeout = getPropertyInteger(prop, "console_session_timeout");
			consoleConnectTimeout = getPropertyInteger(prop, "console_connect_timeout");
			consoleControllerTimeout = getPropertyInteger(prop, "console_controller_timeout");
			consoleUpdateScripts = getPropertyBoolean(prop, "console_update_scripts");
			consoleSshMaxTries = getPropertyInteger(prop, "console_ssh_max_tries");
			consoleAdminIp = getPropertyString(prop, "console_admin_ip");
			consoleListeningPort = getPropertyInteger(prop, "console_listening_port");
			consoleWakeupMode = getPropertyBoolean(prop, "console_wakeup_mode");
			consoleDiscoverMode = getPropertyBoolean(prop, "console_discover_mode");
			consoleSimulationMode = getPropertyBoolean(prop, "console_simulation_mode");
			consoleDemoMode = getPropertyBoolean(prop, "console_demo_mode");
			consoleAutoSave = getPropertyBoolean(prop, "console_auto_save");
			consoleDeepSync = getPropertyBoolean(prop, "console_deep_sync");
			consoleFilePrefix = getPropertyString(prop, "console_file_prefix");
			maxHostsPerSwitch = getPropertyInteger(prop, "max_hosts_per_switch");
			publicCloudSync = getPropertyBoolean(prop, "public_cloud_sync");
			privateCloudSync = getPropertyBoolean(prop, "private_cloud_sync");
			
			// Default values
			defaultDockerPrefix = getPropertyString(prop, "default_docker_prefix");
			defaultDockerImage = getPropertyString(prop, "default_docker_image");
			defaultOpenflowVersion = getPropertyInteger(prop, "default_openflow_version");
			defaultBridgeName = getPropertyString(prop, "default_bridge_name");
			defaultIpRange = getPropertyString(prop, "default_ip_range");
			defaultMacRange = getPropertyString(prop, "default_mac_range");
			defaultMaxFlowSize = getPropertyInteger(prop, "default_max_flow_size");
			defaultPublicIp = getPropertyString(prop, "default_public_ip");
			defaultPrivateIp = getPropertyString(prop, "default_private_ip");
			defaultLinkBandwidth = getPropertyInteger(prop, "default_link_bandwidth");
			defaultLinkDelay = getPropertyInteger(prop, "default_link_delay");
			defaultLinkLossRate = getPropertyInteger(prop, "default_link_loss_rate");		
			is.close();
		} 
		catch (IOException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
	
	private String getPropertyString(Properties prop, String name) throws ServerError {
		String value = prop.getProperty(name);
		if (value != null) return value;
		throw new ServerError("Property '" + name + "' not set");
	}
	
	private int getPropertyInteger(Properties prop, String name) throws ServerError {
		String value = getPropertyString(prop, name);
		return Integer.parseInt(value);
	}
	
	private boolean getPropertyBoolean(Properties prop, String name) throws ServerError {
		String value = getPropertyString(prop, name);
		return Boolean.parseBoolean(value);
	}
	
	public static void writeXMLString(Document doc, Element elem, String name, String value) {
		if (value != null) {
			Element aux = doc.createElement(name);
			aux.appendChild(doc.createTextNode(value));
			elem.appendChild(aux);
		}
	}
	
	public static void writeXMLInteger(Document doc, Element elem, String name, int value) {
		if (value != 0) {
			Element aux = doc.createElement(name);
			aux.appendChild(doc.createTextNode(Integer.toString(value)));
			elem.appendChild(aux);
		}
	}

	public static void writeXMLBoolean(Document doc, Element elem, String name, boolean value) {
		if (value != false) {
			Element aux = doc.createElement(name);
			aux.appendChild(doc.createTextNode(Boolean.toString(value)));
			elem.appendChild(aux);
		}
	}
	
	public static String readXMLString(Element elem, String name) {
		NodeList nodes = elem.getElementsByTagName(name);
		if (nodes.getLength() == 0) return null;
		return nodes.item(0).getTextContent();
	}
	
	public static int readXMLInteger(Element elem, String name) {
		NodeList nodes = elem.getElementsByTagName(name);
		if (nodes.getLength() == 0) return 0;
		String content = nodes.item(0).getTextContent();
		return Integer.parseInt(content);
	}

	public static boolean readXMLBoolean(Element elem, String name) {
		NodeList nodes = elem.getElementsByTagName(name);
		if (nodes.getLength() == 0) return false;
		String content = nodes.item(0).getTextContent();
		return Boolean.parseBoolean(content);
	}
	
	public static Document loadDocument(InputStream is) throws ServerError {
		try {
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(is);
	    	doc.getDocumentElement().normalize();
			return doc;
		} 
		catch (ParserConfigurationException | SAXException | IOException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
	
	public static void saveDocument(Document doc, StreamResult result) throws ServerError {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);				 
			transformer.transform(source, result);
		} 
		catch (TransformerException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
}
