package server;

import java.io.IOException;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

import common.Channel;
import common.Config;
import common.Console;
import common.Controller;
import common.Network;
import common.Packet;
import common.Receiver;
import common.ServerError;
import common.Packet.Type;
import server.Server;

public class Listener extends Thread implements Receiver {

	private volatile boolean running;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private Server server;
	private Channel channel;
	
	public Listener(Server server) {
		Config config = server.getConfig();
		int port = config.consoleListeningPort;
		try {			
			serverSocket = new ServerSocket(port);
		} 
		catch (IOException ex) {
			Console.error("Cannot create server socket");
			ex.printStackTrace();
		}
        Console.info("Listening on port " + port);
		this.server = server;
        this.running = true;
	}
	
	public void close() throws ServerError {
		running = false;
		try {
			Console.info("Closing controller listener");
			serverSocket.close();
		} 
		catch (IOException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
	
	public void send(Packet packet) throws ServerError {
		if (channel != null)
			try {
				channel.send(packet);
			} 
			catch (IOException e) {
				throw new ServerError(e.getMessage());
			}
		else throw new ServerError("Hypervisor is not connected");
	}
	
	public Packet waitFor(Type[] types) throws Exception {
		if (channel != null) return channel.waitFor(types);
		else throw new IOException("Hypervisor is not connected");
	}
	
	public Packet waitFor(Type[] types, long timeout) throws ServerError {
		if (channel != null)
			try {
				return channel.waitFor(types, timeout);
			} 
			catch (TimeoutException | InterruptedException ex) {
				throw new ServerError(ex.getMessage());
			}
		else throw new ServerError("Hypervisor is not connected");
	}
	
	public boolean isConnected() {
		return clientSocket != null && !clientSocket.isClosed();
	}
	
	public boolean receive(Packet packet) throws Exception {
		switch (packet.getType()) {
		case PHYSICAL_REQUEST: {
			Console.info("Receiving PHYSICAL_TOPO_REQUEST");
			Network network = server.getNetworks().get(0);
			Document doc = network.writeDocument();
			StringWriter writer = new StringWriter();
			Config.saveDocument(doc, new StreamResult(writer));
			byte[] data = writer.getBuffer().toString().getBytes();
			channel.send(new Packet(Type.PHYSICAL_SUCCESS, data));
			String path = Console.getPath("request.vsl");
			network.writeXML(path);
			return true;
		}
		default:
			return false;
		}
	}

	public void run() {
        while (running == true) {	
        	Socket socket = null;
        	try {
        		socket = serverSocket.accept();
        		Console.info("Accepting new connection");
        	} 
        	catch (IOException ex) {
        		break;
        	}
        	if (isConnected())
				try {
					Console.info("Closing previous connection");
					clientSocket.close();
				} 
        		catch (IOException e) {
					Console.error("Cannot close connection");
				}
        	
        	// Update controller status
        	Network network = server.getNetworks().get(0);
        	Controller controller = Controller.find(network);
        	controller.setConnected(true);

        	// Create channel
        	channel = new Channel(socket, this);
        	clientSocket = socket;
        	channel.run();
        }
	}
}
