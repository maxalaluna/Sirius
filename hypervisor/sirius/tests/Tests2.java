package net.floodlightcontroller.sirius.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import net.floodlightcontroller.sirius.dijkstra.teste.Vertex;
import net.floodlightcontroller.sirius.topology.FlowEntry;


public class Tests2 {

	boolean hasFlowEntry (ArrayList<FlowEntry> flowtable, FlowEntry flowEntry){

		for(int i = 0; i< flowtable.size(); i++){

			if(flowtable.get(i).getMatch().equals(flowEntry.getMatch()) &&
					flowtable.get(i).getOfActions().equals(flowEntry.getOfActions())){

				return true;
			}
		}
		return false;
	}

	public static String reverseSwitches(String path){

		String h = "";
		String[] p = path.split(":");

		for (int i = p.length -1; i >= 0; i--){

			if(i == p.length -1){
				h = h + p[i];
			}else{
				h = h + ":"+ p[i];
			}
		}
		return h;
	}

	public static String concatEdges(String firstEdge, String nextEdge){

		String h = firstEdge;
		String[] switches;
		switches= firstEdge.split(":");
		String[] s2 = nextEdge.split(":");

		if(switches[switches.length-1].equals(s2[0])){
			for(int i = 1; i < s2.length; i++){
				h = h +":"+s2[i];
			}
		}else{
			if(switches[switches.length-1].equals(s2[s2.length-1])){
				for(int i = s2.length-2; i >= 0; i--){
					h = h +":"+s2[i];
				}
			}
		}
		return h;
	}
	public static void main(String[] args) throws IOException {


		Connection connServer = new Connection("XX.XX.XX.XX");

		/* Now connect */
		connServer.connect();

		/* Authenticate.
		 * If you get an IOException saying something like
		 * "Authentication method password not supported by the server at this stage."
		 * then please check the FAQ.
		 */
		//		boolean isAuthenticatedServer1 = connServer.authenticateWithPassword(server2.getUsername(), server2.getPassword());
		boolean isAuthenticatedServer1 = connServer.authenticateWithPublicKey("ubuntu", new File("/home/floodlight/src/main/java/net/floodlightcontroller/sirius/console/amazon.pem"), null);

		if (isAuthenticatedServer1 == false){
			throw new IOException("Authentication failed on server XX.XX.XX.XX.");
		}else{

			/* Create a session */
			Session sessServer = connServer.openSession();

			//Improve this part verifing the correct execution of the command 
			/*Create commands*/
			String command = "sudo ovs-vsctl set-controller br1 tcp:10.10.5.66:6653";
			sessServer.execCommand(command);

			/* Show exit status, if available (otherwise "null") */
			//System.out.println("ExitCode on server "+server.getHostname()+" : " + sessServer.getExitStatus());
			/* Close this session */
			sessServer.close();
			/* Close the connection */
			connServer.close();
		}
	}
}
