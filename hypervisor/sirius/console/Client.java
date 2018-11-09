package net.floodlightcontroller.sirius.console;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Hashtable;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import net.floodlightcontroller.sirius.console.Packet.Type;
import net.floodlightcontroller.sirius.nethypervisor.SiriusNetHypervisor;

	public class Client implements Receiver {

		private Channel channel;
		private Hashtable<Integer, Network> networks;
		private String PATH_LOCATION_PHYSICAL_TOPOLOGY;
		private String PATH_LOCATION_VIRTUAL_TOPOLOGY;
		protected static Logger log = LoggerFactory.getLogger(Client.class);
		private Socket socket = null;
		SiriusNetHypervisor hy;

		public Client(Socket socket) {
			this.socket = socket;
			networks = new Hashtable<>();
			channel = new Channel(socket, this);
			channel.start();		
			try {
				Console.pause(1000);
				Network network = new Network();
				channel.send(new Packet(Type.PHYSICAL_REQUEST, null));
				Type[] types = new Type[] { Type.PHYSICAL_SUCCESS, Type.PHYSICAL_FAILURE };
				Packet packet = channel.waitFor(types);
				if (packet.getType() == Type.PHYSICAL_SUCCESS) {
					InputStream is = new ByteArrayInputStream(packet.getData());
					network.readDocument(Config.loadDocument(is));
				}
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public Client(Socket socket, String pATH_LOCATION_PHYSICAL_TOPOLOGY, String pATH_LOCATION_VIRTUAL_TOPOLOGY, SiriusNetHypervisor hy) {
			this.socket = socket;
			networks = new Hashtable<>();
			channel = new Channel(socket, this);
			channel.start();
			this.PATH_LOCATION_PHYSICAL_TOPOLOGY = pATH_LOCATION_PHYSICAL_TOPOLOGY;
			this.PATH_LOCATION_VIRTUAL_TOPOLOGY = pATH_LOCATION_VIRTUAL_TOPOLOGY;
			this.hy = hy;
		}

		public boolean receive(Packet packet) throws Exception {
			switch (packet.getType()) {

			case VIRTUAL_REQUEST:{
				Console.log("Receiving VIRTUAL_TOPO_REQUEST");
				Network network = new Network();
				InputStream is = new ByteArrayInputStream(packet.getData());
				network.readDocument(Config.loadDocument(is));
				hy.instantiateTenant(network);
				return true;
			}
			default:
				return false;
			}
		}

		public static void main(String[] args) throws Exception {
			Config config = new Config("console.properties");

			// Create client socket
			Socket socket = null;
			String ip = config.consoleAdminIp;
			int port = config.consoleListeningPort;
			while (true) {
				try {
					Console.log("Connecting " + ip + " on port " + port);
					socket = new Socket(ip, port);
					break;
				} 
				catch (IOException e) {
					Console.error("Retrying in 2 seconds ...");
					Console.pause(2000);
				}
			}
			new Client(socket);
		}

		public Network requestPhysicalNetwork() {
			try {
				Console.pause(1000);
				Network network = new Network();
				channel.send(new Packet(Type.PHYSICAL_REQUEST, null));
				Type[] types = new Type[] { Type.PHYSICAL_SUCCESS, Type.PHYSICAL_FAILURE };
				Packet packet = channel.waitFor(types);
				if (packet.getType() == Type.PHYSICAL_SUCCESS) {
					InputStream is = new ByteArrayInputStream(packet.getData());
					network.readDocument(Config.loadDocument(is));
//					network.writeXML(this.PATH_LOCATION_PHYSICAL_TOPOLOGY);
					return network;
				}else{
					if (packet.getType() == Type.PHYSICAL_FAILURE) {
						log.info("Physical topology request failure.");
						return null;
					}
				}		
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}

		public void sendVirtualEmbeddingSuccess(Network virtualNodes) throws TransformerException, Exception {
			
			Document doc = virtualNodes.writeDocument();
			StringWriter writer = new StringWriter();
			Config.saveDocument(doc, new StreamResult(writer));
			String str = writer.getBuffer().toString();
			//channel.send(Type.VIRTUAL_TOPO_REPLY, str.getBytes());
			channel.send(new Packet(Type.VIRTUAL_SUCCESS, str.getBytes()));
		}
		
		public void sendVirtualFailure(String msgFailure) throws IOException{
			channel.send(new Packet(Type.VIRTUAL_FAILURE, msgFailure.getBytes()));
		}
	}

