package net.floodlightcontroller.sirius.providerconfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class PopulateInfoOvsVsctlListInterfaceThread3  implements Runnable{

	int serverIndex;
	int bridgeIndex;
	int interfaceIndex;
	ArrayList<Server3> serverList;
	Connection connServer;
	String BRIDGE_NAME_OF_VMS;

	public PopulateInfoOvsVsctlListInterfaceThread3(int serverIndex,
			int bridgeIndex, int interfaceIndex, ArrayList<Server3> serverList, String BRIDGE_NAME_OF_VMS) {
		super();
		this.serverIndex = serverIndex;
		this.bridgeIndex = bridgeIndex;
		this.interfaceIndex = interfaceIndex;
		this.serverList = serverList;
		this.connServer = new Connection(this.serverList.get(serverIndex).getHostname());
		this.BRIDGE_NAME_OF_VMS = BRIDGE_NAME_OF_VMS;
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {

		String ovsVsctlListInterface = "";
		boolean isAuthenticatedServer;
		Session sessServerUuid = null;
		Session sessServerAdminState = null;
		Session sessServerDuplex = null;
		Session sessServerVmsMacAddress = null;
		Session sessServerLinkSpeed = null;
		Session sessServerLinkState= null;
		Session sessServerMacAddress = null;
		Session sessServerMtu = null;
		Session sessServerName = null;
		Session sessServerOfport = null;
		Session sessServerOfportRequest = null;
		Session sessServerRemoteIP = null;
		Session sessServerType = null;
		String ovsVsctlListInterfaceUuid;
		String ovsVsctlListInterfaceAdminState; //admin_state
		String ovsVsctlListInterfaceDuplex; //duplex
		String ovsVsctlListInterfaceVmsMacAddress;
		String ovsVsctlListInterfaceLinkSpeed; // link_speed          : 10000000000
		String ovsVsctlListInterfaceLinkState; // link_state          : up
		String ovsVsctlListInterfaceMacAddress; // mac_in_use          : "e6:e8:2f:93:25:53"
		String ovsVsctlListInterfaceMtu; // mtu
		String ovsVsctlListInterfaceName; // name                : "s1-eth3"
		String ovsVsctlListInterfaceOfport; // ofport
		String ovsVsctlListInterfaceOfportRequest; // 	ofport_request      : 3
		String ovsVsctlListInterfaceRemoteIP; // options             : {remote_ip="192.168.5.34"}
		String ovsVsctlListInterfaceType;

		try {

			/* Now connect */
			connServer.connect();

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */

			isAuthenticatedServer = connServer.authenticateWithPassword(this.serverList.get(serverIndex).getUsername(), this.serverList.get(serverIndex).getPassword());

			if (isAuthenticatedServer == false){
				throw new IOException("Authentication failed on server "+this.serverList.get(serverIndex).getHostname()+".");
			}
			if (isAuthenticatedServer){
				/* Create a session */
				sessServerUuid = connServer.openSession();
				sessServerAdminState = connServer.openSession();
				sessServerDuplex = connServer.openSession();
				sessServerVmsMacAddress = connServer.openSession();
				sessServerLinkSpeed = connServer.openSession();
				sessServerLinkState = connServer.openSession();
				sessServerMacAddress = connServer.openSession();
				sessServerMtu = connServer.openSession();
				sessServerName = connServer.openSession();
				sessServerOfport = connServer.openSession();
				sessServerOfportRequest = connServer.openSession();
				sessServerRemoteIP = connServer.openSession();
				sessServerType = connServer.openSession();
				
				String commandUuid = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" _uuid";
				String commandAdminState = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" admin_state"; //admin_state
				String commandDuplex = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" duplex"; //duplex
				String commandVmsMacAddress = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" externals_ids:attached-mac";
				String commandLinkSpeed = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" link_speed"; // link_speed          : 10000000000
				String commandLinkState = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" link_state"; // link_state          : up
				String commandMacAddress = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" mac_in_use"; // mac_in_use          : "e6:e8:2f:93:25:53"
				String commandMtu = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" mtu"; // mtu
				String commandName = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" name"; // name                : "s1-eth3"
				String commandOfport = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" ofport"; // ofport
				String commandOfportRequest = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" ofport_request"; // 	ofport_request      : 3
				String commandRemoteIP = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" options"; // options             : {remote_ip="192.168.5.34"}
				String commandType = "ovs-vsctl get interface "+ this.serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() +" type";

				sessServerUuid.execCommand(commandUuid);
				sessServerAdminState.execCommand(commandAdminState);
				sessServerDuplex.execCommand(commandDuplex);
				sessServerVmsMacAddress.execCommand(commandVmsMacAddress);
				sessServerLinkSpeed.execCommand(commandLinkSpeed);
				sessServerLinkState.execCommand(commandLinkState);
				sessServerMacAddress.execCommand(commandMacAddress);
				sessServerMtu.execCommand(commandMtu);
				sessServerName.execCommand(commandName);
				sessServerOfport.execCommand(commandOfport);
				sessServerOfportRequest.execCommand(commandOfportRequest);
				sessServerRemoteIP.execCommand(commandRemoteIP);
				sessServerType.execCommand(commandType);

				InputStream stdoutServerUuid = new StreamGobbler(sessServerUuid.getStdout());
				@SuppressWarnings("resource")
				BufferedReader brServer = new BufferedReader(new InputStreamReader(stdoutServerUuid));
				serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().
				get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).setUuid(UUID.fromString(brServer.readLine()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ovsVsctlListInterface = EnvironmentOfServices3.getInfoInterface(serverList.get(this.getServerIndex()),serverList.get(this.getServerIndex()).
					getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).populateInfoOvsVsctlListInterface(ovsVsctlListInterface);
		//serverList.get(0).getOpenVSwitch().getArrayListBridge().get(0).getArrayListInterfaces().get(4).populateInfoOvsVsctlListInterface(ovsVsctlListInterface);
		System.out.println("Populating information of Interface " + serverList.get(this.getServerIndex()).getOpenVSwitch().getArrayListBridge().get(this.getBridgeIndex()).getArrayListInterfaces().get(this.getInterfaceIndex()).getName() + " finished!!!");
	}

	public Connection getConnServer() {
		return connServer;
	}

	public void setConnServer(Connection connServer) {
		this.connServer = connServer;
	}

	public String getBRIDGE_NAME_OF_VMS() {
		return BRIDGE_NAME_OF_VMS;
	}

	public void setBRIDGE_NAME_OF_VMS(String bRIDGE_NAME_OF_VMS) {
		BRIDGE_NAME_OF_VMS = bRIDGE_NAME_OF_VMS;
	}

	public int getServerIndex() {
		return serverIndex;
	}

	public void setServerIndex(int serverIndex) {
		this.serverIndex = serverIndex;
	}

	public int getBridgeIndex() {
		return bridgeIndex;
	}

	public void setBridgeIndex(int bridgeIndex) {
		this.bridgeIndex = bridgeIndex;
	}

	public int getInterfaceIndex() {
		return interfaceIndex;
	}

	public void setInterfaceIndex(int interfaceIndex) {
		this.interfaceIndex = interfaceIndex;
	}

	public ArrayList<Server3> getServerList() {
		return serverList;
	}

	public void setServerList(ArrayList<Server3> serverList) {
		this.serverList = serverList;
	}
}
/*		new Thread("i: "+i+ "k: " + k){
	        public void run(){
	        	String ovsVsctlListInterface;
				System.out.println("Teste Thread "+getName());
				ovsVsctlListInterface = getInfoInterface(serverList.get(i),serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k));

				serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k).populateInfoOvsVsctlListInterface(ovsVsctlListInterface);
				//					serverList.get(0).getOpenVSwitch().getArrayListBridge().get(0).getArrayListInterfaces().get(4).populateInfoOvsVsctlListInterface(ovsVsctlListInterface);
				System.out.println("Populating information of Interface " + serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k).getName() + " finished!!!");
	        }
	      }.start();
 */