package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import common.Packet.Type;

public class Channel extends Thread {
	
	private static final String MAGIC_NUMBERS = "SCP";
	
	private Socket socket;
	private Receiver receiver;
	private DataInputStream in;
	private DataOutputStream out;
	private ArrayList<Packet> queue;
	
	public Channel(Socket socket, Receiver receiver) {
		Console.info("Connected to " + socket.getInetAddress());
		this.queue = new ArrayList<>();
		this.receiver = receiver;
		this.socket = socket;
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} 
		catch (IOException ex) {
			Console.error("Cannot create in stream");
			ex.printStackTrace();
		}
	}
	
	public void send(Packet packet) throws IOException {
		Console.info("Sending packet " + packet.getType());
		out.write(MAGIC_NUMBERS.getBytes());
		out.writeInt(packet.getType().getValue());
		if (packet.getData() == null)
			out.writeInt(0);
		else {
			out.writeInt(packet.getData().length);
			out.write(packet.getData());
		}
	}
	
	public Packet waitFor(Type[] types) throws InterruptedException {
		synchronized (queue) {
			Packet packet = searchTypes(types);
			while (packet == null) {
				queue.wait();
				packet = searchTypes(types);
			}
			queue.remove(packet);
			return packet;
		}
	}
	
	public Packet waitFor(Type[] types, long timeout) throws TimeoutException, InterruptedException {
		synchronized (queue) {
			long start = System.currentTimeMillis();
			Packet packet = searchTypes(types);
			while (packet == null) {
				long elapsed = System.currentTimeMillis() - start;
				if (elapsed >= timeout) 
					throw new TimeoutException("Timeout expired with Hypervisor");
				queue.wait(timeout - elapsed);
				packet = searchTypes(types);
			}
			queue.remove(packet);
			return packet;
		}
	}
	
	private Packet searchTypes(Type[] types) {
		for (int k = 0; k < types.length; k++) {
			Packet packet = searchType(types[k]);
			if (packet != null) 
				return packet;
		}
		return null;
	}
	
	private Packet searchType(Type type) {
		for (Packet packet : queue)
			if (packet.getType() == type)
				return packet;
		return null;
	}
	
	private void store(Packet packet) {
		synchronized (queue) {
			Packet aux = searchType(packet.getType());
			if (aux != null) queue.remove(aux);
			queue.add(packet);
			queue.notifyAll();
		}
	}
	
	private void readMagicString() throws IOException {
		byte[] data = MAGIC_NUMBERS.getBytes();
		for (int k = 0; k < data.length; k++)
			if (in.readByte() != data[k]) {
				socket.close();
				throw new IOException("Bad magic number");
			}
	}
	
	public void run() {
		while (true) {
   	  		try {
   	  			readMagicString();
   	  			Type type = Type.fromValue(in.readInt());
   	  			Console.info("Receiving packet " + type);
   	  			int length = in.readInt();
				try {
					byte[] data = null;
					if (length > 0) {
						data = new byte[length];
						in.readFully(data);
					}
					Packet packet = new Packet(type, data);
					boolean res = receiver.receive(packet);
					if (res == false) store(packet);
				} 
  				catch (Exception e) {
					e.printStackTrace();
				}
   	  		}
       	  	catch (IOException ex) { 
       	  		Console.info("Connection closed");
       	  		break;
       	  	} 
   	  	}
	}
}
