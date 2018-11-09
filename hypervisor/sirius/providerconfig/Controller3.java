package net.floodlightcontroller.sirius.providerconfig;

public class Controller3 {
	
	protected String connectionType;
	protected String IPAddress;
	protected String port;
	protected boolean connected;
	protected String description;

	public Controller3(String connectionType, String iPAddress, String port,
			boolean connected, String description) {
		super();
		this.connectionType = connectionType;
		IPAddress = iPAddress;
		this.port = port;
		this.connected = connected;
		this.description = description;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public void setIPAddress(String iPAddress) {
		IPAddress = iPAddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
