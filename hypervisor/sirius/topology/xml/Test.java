package net.floodlightcontroller.sirius.topology.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import org.w3c.dom.Document;

public class Test {
	
	private Network physical;
		
	public Test() throws FileNotFoundException {
		Config config = new Config();
		physical = new Network();
		InputStream is;
		Document doc;
			
		// Read configuration properties
		is = new FileInputStream("config.properties");
		config.readProperties(is);
		
		// Read cloud information
		is = new FileInputStream(config.defaultCloudFile);
		doc = Config.loadDocument(is);
		config.readClouds(doc);
		
		// Print cloud attributes
		System.out.println("CLOUD BASE");
		Iterator<Cloud> citer = config.getCloudIterator();
		while (citer.hasNext()) {
			Cloud cloud = (Cloud)citer.next();
			System.out.println(cloud);
		}
		
		// Read physical network information
		is = new FileInputStream(config.defaultPhysicalFile);
		doc = Config.loadDocument(is);
		physical.readDocument(config, doc);

		// Print physical network information
		System.out.println("PHYSICAL NETWORK");
		Iterator<Node> niter1 = physical.getNodeIterator();
		while (niter1.hasNext()) {
			Node node = (Node)niter1.next();
			System.out.print(node);
			if (node instanceof Host && node.getLinks().size() > 0) {
				Node peer = node.getPeer(physical, 0);
				String dpid = ((Switch)peer).getDpid();
				int port = node.getPort(physical, 0) + 1;
				System.out.println(" connected to " + peer.getName() 
						+ " dpid = " + dpid + " on port " + port);
			}
			else System.out.println();
		}
		System.out.println();
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		new Test();
	}
}
