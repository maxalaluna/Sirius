package net.floodlightcontroller.sirius.environmentofservers;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class EnvironmentOfServices{

	ArrayList<Server> serverList = new ArrayList<Server>();
	String tunnelType;

	public EnvironmentOfServices(String tunnelType) {
		super();
		this.tunnelType = tunnelType;
	}

	public ArrayList<Server> getServerList() {
		return serverList;
	}

	public void setServerList(ArrayList<Server> serverList) {
		this.serverList = serverList;
	}

	public String getTunnelType() {
		return tunnelType;
	}

	public void setTunnelType(String tunnelType) {
		this.tunnelType = tunnelType;
	}

	public String nextBridgeName(Server server, Connection connection){

		String nextBridgeName = "";

		try {
			Session sessServer = connection.openSession();

			String command1 = "date +%s%N";
			sessServer.execCommand(command1);

			InputStream stdoutServer = new StreamGobbler(sessServer.getStdout());
			@SuppressWarnings("resource")
			BufferedReader brServer = new BufferedReader(new InputStreamReader(stdoutServer));

			while (true){
				String line = brServer.readLine();
				if (line == null)
					break;
				nextBridgeName += line;
				//System.out.println(line);
			}			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return nextBridgeName;
	}

	public boolean setController(ArrayList<Server> serverList, Server controller){

		Server server;
		boolean result = true;

		if (serverList != null){

			Iterator<Server> iteratorTenant = serverList.iterator();

			while(iteratorTenant.hasNext()){

				server = iteratorTenant.next();

				result = (result && setController(server, controller));
			}			
		}

		return result;
	}

	public boolean setController(Server server, Server controller){

		try{

			/* Create a connection instance */
			Connection connServer = new Connection(server.getHostname());

			/* Now connect */
			connServer.connect();

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			boolean isAuthenticatedServer1 = connServer.authenticateWithPassword(server.getUsername(), server.getPassword());

			if (isAuthenticatedServer1 == false){
				throw new IOException("Authentication failed on server "+server.getHostname()+".");
			}else{

				/* Create a session */
				Session sessServer = connServer.openSession();

				//Improve this part verifing the correct execution of the command 

				/*Create commands*/

				String command = "ovs-vsctl set-controller xenbr0 tcp:"+controller.getHostname()+":6653";

				sessServer.execCommand(command);

				/* Show exit status, if available (otherwise "null") */

				System.out.println("ExitCode on server "+server.getHostname()+" : " + sessServer.getExitStatus());

				/* Close this session */
				sessServer.close();

				/* Close the connection */
				connServer.close();

				return true;

			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}

		return false;
	}
	public ArrayList<ArrayList<Server>> getCombinationOfServers(final ArrayList<Server> serverList){

		ArrayList<ArrayList<Server>> combination = new ArrayList<ArrayList<Server>>();

		if (serverList != null){

			for (int i = 0; i < serverList.size()-1; i++){
				for (int j = i+1; j < serverList.size(); j++ ){

					System.out.print(serverList.get(i).getHostname());
					System.out.println(serverList.get(j).getHostname());

					combination.add(new ArrayList<Server>(Arrays.asList(serverList.get(i),serverList.get(j))));

				}
			}
		}			
		return combination;
	}
	public String createTunnelsFullMesh(){

		ArrayList<ArrayList<Server>> combination = new ArrayList<ArrayList<Server>>();
		String result = "";

		if (this.serverList != null){

			for (int i = 0; i < this.serverList.size()-1; i++){
				for (int j = i+1; j < this.serverList.size(); j++ ){

					System.out.print(this.serverList.get(i).getHostname());
					System.out.println(this.serverList.get(j).getHostname());

					combination.add(new ArrayList<Server>(Arrays.asList(this.serverList.get(i),this.serverList.get(j))));

				}
			}
		}		

		for (int i = 0; i < combination.size(); i++){

			result += createTunnels(combination.get(i).get(0),combination.get(i).get(1));

		}


		/*Generate combination 2 by 2 of all servers*/
		/*Use the list of two servers to create the tunnels using the function createTunnels*/

		return result;
	}

	public String createTunnels(Server server1, Server server2){

		String result = "Inicial";

		try{

			/* Create a connection instance */
			Connection connServer1 = new Connection(server1.getHostname());
			Connection connServer2 = new Connection(server2.getHostname());

			/* Now connect */
			connServer1.connect();
			connServer2.connect();

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			boolean isAuthenticatedServer1 = connServer1.authenticateWithPassword(server1.getUsername(), server1.getPassword());
			boolean isAuthenticatedServer2 = connServer2.authenticateWithPassword(server2.getUsername(), server2.getPassword());

			if (isAuthenticatedServer1 == false){
				throw new IOException("Authentication failed on server "+server1.getHostname()+".");
			}
			if (isAuthenticatedServer2 == false){
				throw new IOException("Authentication failed on server "+server2.getHostname()+".");
			}

			if (isAuthenticatedServer1 && isAuthenticatedServer2){

				/* Create a session */
				Session sessServer1 = connServer1.openSession();
				Session sessServer2 = connServer2.openSession();

				//Improve this part verifing the correct execution of the command add-port

				/*Create commands*/

				String command1 = "ovs-vsctl add-port xenbr0 "+this.nextBridgeName(server1, connServer1)+" -- set interface gre1 type="
						+ this.getTunnelType()+" options:remote_ip="+ server2.getHostname();
				String command2 = "ovs-vsctl add-port xenbr0 "+this.nextBridgeName(server2, connServer2)+" -- set interface gre1 type="
						+ this.getTunnelType()+" options:remote_ip="+ server1.getHostname();

				sessServer1.execCommand(command1);
				sessServer2.execCommand(command2);

				/* 
				 * This basic example does not handle stderr, which is sometimes dangerous
				 * (please read the FAQ).
				 */
				InputStream stdoutServer1 = new StreamGobbler(sessServer1.getStdout());
				InputStream stdoutServer2 = new StreamGobbler(sessServer2.getStdout());

				BufferedReader brServer1 = new BufferedReader(new InputStreamReader(stdoutServer1));
				BufferedReader brServer2 = new BufferedReader(new InputStreamReader(stdoutServer2));

				System.out.println("Server: "+server1.getHostname());
				while (true){
					String line = brServer1.readLine();
					if (line == null)
						break;
					System.out.println(line);
				}

				System.out.println("Server: "+server2.getHostname());
				while (true){
					String line = brServer2.readLine();
					if (line == null)
						break;
					System.out.println(line);
				}

				/* Show exit status, if available (otherwise "null") */

				result = "ExitCode on server "+server1.getHostname()+" : " + sessServer1.getExitStatus()+"\n";
				result += "ExitCode on server "+server2.getHostname()+" : " + sessServer2.getExitStatus()+"\n";

				/* Close this session */
				sessServer1.close();
				sessServer2.close();

				/* Close the connection */
				connServer1.close();
				connServer2.close();

				/* Close the BufferedReader */
				brServer1.close();
				brServer2.close();

			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
		return result;
	}

	public void populateEnvironment() {
		// TODO Auto-generated method stub
		//PERCORRER TODOS OS OVS DOS SERVERS
		//EXECUTAR O COMANDO OVS-VSCTL SHOW E POPULAR OS OVS E AS BRIDGES
		//PARA CADA UMA DAS BRIDGES, POPULAR COM AS INFO DE INTERFACES
		ArrayList<Server> serverList = this.getServerList();
		@SuppressWarnings("unused")
		String ovsVsctlShow = "8945dad2-35b4-4549-9122-23d5558992e7\n"+
				"    Bridge \"s1\"\n"+
				"        Controller \"tcp:127.0.0.1:6633\"\n"+
				"        Controller \"ptcp:6634\"\n"+
				"        fail_mode: secure\n"+
				"        Port \"gre2\"\n"+
				"            Interface \"gre2\"\n"+
				"                type: gre\n"+
				"                options: {remote_ip=\"192.168.57.152\"}\n"+
				"        Port \"gre1\"\n"+
				"            Interface \"gre1\"\n"+
				"                type: gre\n"+
				"                options: {remote_ip=\"192.168.57.152\"}\n"+
				"        Port \"s1\"\n"+
				"            Interface \"s1\"\n"+
				"                type: internal\n"+
				"        Port \"s1-eth2\"\n"+
				"            Interface \"s1-eth2\"\n"+
				"        Port \"s1-eth1\"\n"+
				"            Interface \"s1-eth1\"\n"+
				"        Port \"s1-eth3\"\n"+
				"            Interface \"s1-eth3\"\n"+
				"    Bridge \"s1_2\"\n"+
				"        Controller \"tcp:127.0.0.1:6633\"\n"+
				"        fail_mode: secure\n"+
				"        Port \"gre1\"\n"+
				"            Interface \"gre1\"\n"+
				"                type: gre\n"+
				"                options: {remote_ip=\"192.168.57.152\"}\n"+
				"        Port \"s1\"\n"+
				"            Interface \"s1\"\n"+
				"                type: internal\n"+
				"        Port \"s1-eth2\"\n"+
				"            Interface \"s1-eth2\"\n"+
				"        Port \"s1-eth1\"\n"+
				"            Interface \"s1-eth1\"\n"+
				"    ovs_version: \"2.0.2\"";

		String ovsVsctlListInterface = "_uuid               : ee4608db-7c5b-48cb-90d3-98db761b9347\n"+
"admin_state         : up\n"+
"bfd                 : {}\n"+
"bfd_status          : {}\n"+
"cfm_fault           : []\n"+
"cfm_fault_status    : []\n"+
"cfm_health          : []\n"+
"cfm_mpid            : []\n"+
"cfm_remote_mpids    : []\n"+
"cfm_remote_opstate  : []\n"+
"duplex              : full\n"+
"external_ids        : {}\n"+
"ifindex             : 588\n"+
"ingress_policing_burst: 0\n"+
"ingress_policing_rate: 0\n"+
"lacp_current        : []\n"+
"link_resets         : 1\n"+
"link_speed          : 10000000000\n"+
"link_state          : up\n"+
"mac                 : []\n"+
"mac_in_use          : \"e6:e8:2f:93:25:53\"\n"+
"mtu                 : 1500\n"+
"name                : \"s1-eth1\"\n"+
"ofport              : 1\n"+
"ofport_request      : 1\n"+
"options             : {}\n"+
"other_config        : {}\n"+
"statistics          : {collisions=0, rx_bytes=810, rx_crc_err=0, rx_dropped=0, r                                                                                        x_errors=0, rx_frame_err=0, rx_over_err=0, rx_packets=13, tx_bytes=0, tx_dropped                                                                                        =0, tx_errors=0, tx_packets=0}\n"+
"status              : {driver_name=veth, driver_version=\"1.0\", firmware_version=                                                                                        \"\"}\n"+
"type                : \"\"";


		for (int i=0; i < serverList.size(); i++){
			//Para testar no sistema real
			//ovsVsctlShow = this.getInfoOVS(serverList.get(i));
			serverList.get(i).populateInfoOvsVsctlShow(ovsVsctlShow);

//			for (int j=0; j < serverList.get(i).getOpenVSwitch().getArrayListBridge().size(); j++){
				//Para testar no sistema real

//				for (int k=0; k < serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().size(); k++){
					
					//ovsVsctlListInterface = this.getInfoInterface(serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k));

					//serverList.get(i).getOpenVSwitch().getArrayListBridge().get(j).getArrayListInterfaces().get(k).populateInfoOvsVsctlListInterface(ovsVsctlListInterface);
					serverList.get(0).getOpenVSwitch().getArrayListBridge().get(0).getArrayListInterfaces().get(4).populateInfoOvsVsctlListInterface(ovsVsctlListInterface);
//				}

//			}

		}




	}

	private String getInfoOVS(Server server) {

		String result = "";

		try{

			/* Create a connection instance */
			Connection connServer = new Connection(server.getHostname());

			/* Now connect */
			connServer.connect();

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			boolean isAuthenticatedServer = connServer.authenticateWithPassword(server.getUsername(), server.getPassword());

			if (isAuthenticatedServer == false){
				throw new IOException("Authentication failed on server "+server.getHostname()+".");
			}

			if (isAuthenticatedServer){

				/* Create a session */
				Session sessServer = connServer.openSession();

				//Improve this part verifing the correct execution of the command add-port

				/*Create commands*/

				String command = "ovs-vsctl show";

				sessServer.execCommand(command);

				/* 
				 * This basic example does not handle stderr, which is sometimes dangerous
				 * (please read the FAQ).
				 */
				InputStream stdoutServer = new StreamGobbler(sessServer.getStdout());

				BufferedReader brServer = new BufferedReader(new InputStreamReader(stdoutServer));

				while (true){
					String line = brServer.readLine();
					result += line;
					if (line == null)
						break;
				}

				/* Show exit status, if available (otherwise "null") */

				//result = "ExitCode on server "+server.getHostname()+" : " + sessServer.getExitStatus()+"\n";

				/* Close this session */
				sessServer.close();

				/* Close the connection */
				connServer.close();

				/* Close the BufferedReader */
				brServer.close();

			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
		return result;

	}
	
	private String getInfoInterface(Server server, Interfaces interfaces) {

		String result = "";

		try{

			/* Create a connection instance */
			Connection connServer = new Connection(server.getHostname());

			/* Now connect */
			connServer.connect();

			/* Authenticate.
			 * If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */
			boolean isAuthenticatedServer = connServer.authenticateWithPassword(server.getUsername(), server.getPassword());

			if (isAuthenticatedServer == false){
				throw new IOException("Authentication failed on server "+server.getHostname()+".");
			}

			if (isAuthenticatedServer){

				/* Create a session */
				Session sessServer = connServer.openSession();

				//Improve this part verifing the correct execution of the command add-port

				/*Create commands*/

				String command = "ovs-vsctl list interface "+ interfaces.getName();

				sessServer.execCommand(command);

				/* 
				 * This basic example does not handle stderr, which is sometimes dangerous
				 * (please read the FAQ).
				 */
				InputStream stdoutServer = new StreamGobbler(sessServer.getStdout());

				BufferedReader brServer = new BufferedReader(new InputStreamReader(stdoutServer));

				while (true){
					String line = brServer.readLine();
					result += line;
					if (line == null)
						break;
				}

				/* Show exit status, if available (otherwise "null") */

				//result = "ExitCode on server "+server.getHostname()+" : " + sessServer.getExitStatus()+"\n";

				/* Close this session */
				sessServer.close();

				/* Close the connection */
				connServer.close();

				/* Close the BufferedReader */
				brServer.close();

			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		}
		return result;

	}

	public void initEnvironmentOfServers() {
		
		Server server1 = new Server(null, "192.168.4.21","root","quinta","Server01 (s1) - 192.168.4.21");
		Server server2 = new Server(null, "192.168.5.34","root","quinta","Server02 (s14) - 192.168.5.34");
		Server server3 = new Server(null, "192.168.4.201","root","quinta","Server03 (sg1) - 192.168.4.201");
		
		ArrayList<Server> serverList = new ArrayList<Server>();
		
		this.serverList.add(server1);
		this.serverList.add(server2);
		this.serverList.add(server3);
		
		//this.createTunnelsFullMesh();
		
	}
}
