package net.floodlightcontroller.sirius.providerconfig;

public class InterfacesGREToDelete3 {
	
	protected int indexOfServer;
	protected int indexOfBridge;
	protected int indexOfInterfaces;
	protected Interfaces3 interfaces;
	
	public InterfacesGREToDelete3(int indexOfServer, int indexOfBridge,
			int indexOfInterfaces, Interfaces3 interfaces) {
		super();
		this.indexOfServer = indexOfServer;
		this.indexOfBridge = indexOfBridge;
		this.indexOfInterfaces = indexOfInterfaces;
		this.interfaces = interfaces;
	}

	public int getIndexOfServer() {
		return indexOfServer;
	}

	public void setIndexOfServer(int indexOfServer) {
		this.indexOfServer = indexOfServer;
	}

	public int getIndexOfBridge() {
		return indexOfBridge;
	}

	public void setIndexOfBridge(int indexOfBridge) {
		this.indexOfBridge = indexOfBridge;
	}

	public int getIndexOfInterfaces() {
		return indexOfInterfaces;
	}

	public void setIndexOfInterfaces(int indexOfInterfaces) {
		this.indexOfInterfaces = indexOfInterfaces;
	}

	public Interfaces3 getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Interfaces3 interfaces) {
		this.interfaces = interfaces;
	}
}
