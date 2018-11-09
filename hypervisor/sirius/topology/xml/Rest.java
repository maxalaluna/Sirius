package net.floodlightcontroller.sirius.topology.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class Rest {

	private String serverURL;
	public volatile boolean mininetRunning;
	
	public Rest(Config config) {
		this.serverURL = "http://" + config.mininetRestIp + 
				":" + config.mininetRestPort;
		sendGetRequest("hello-mininet");
		mininetRunning = false;
	}
	
	protected String sendGetRequest(String name) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(serverURL + "/" + name);
		try {
			HttpResponse response = client.execute(request);
			int status = response.getStatusLine().getStatusCode();
			if (status ==  HttpStatus.SC_OK) {
				BufferedReader rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));		 
				StringBuffer buffer = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) 
					buffer.append(line);
				return buffer.toString();
			}
			else Dialog.errorDialog("Server code " + status); 
		} 
		catch (ClientProtocolException e) {
			System.out.println(e.getMessage());
		} 
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	protected String sendPostRequest(String name, List<NameValuePair> params) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(serverURL + "/" + name);
		try {
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse response = client.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if (status ==  HttpStatus.SC_OK) {
				BufferedReader rd = new BufferedReader(
				        new InputStreamReader(response.getEntity().getContent()));
				StringBuffer buffer = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null)
					buffer.append(line);
				return buffer.toString();		
			}
			else Dialog.errorDialog("Server code " + status); 
		} 
		catch (UnsupportedEncodingException e) {
			Dialog.errorDialog(e.getMessage());
		} 
		catch (ClientProtocolException e) {
			Dialog.errorDialog(e.getMessage());
		} 
		catch (IOException e) {
			Dialog.errorDialog(e.getMessage());
		}
		return null;
	}
	
	public void mnRequestAddController(Controller node) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", node.getName()));
		params.add(new BasicNameValuePair("ip", node.getIp()));
		params.add(new BasicNameValuePair("port", Integer.toString(node.getPort())));
		sendPostRequest("add-controller", params);		
	}
	
	public void mnRequestAddSwitch(Switch node) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", node.getName()));
		params.add(new BasicNameValuePair("version", Integer.toString(node.getVersion())));		
		sendPostRequest("add-switch", params);
	}

	public void mnRequestAddHost(Host node) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", node.getName()));
		params.add(new BasicNameValuePair("ip", node.getIp()));
		params.add(new BasicNameValuePair("mac", node.getMac()));
		sendPostRequest("add-host", params);
	}
	
	public void mnRequestAddLink(Link link, Node node1, Node node2) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("from", node1.getName()));
		params.add(new BasicNameValuePair("to", node2.getName()));
		params.add(new BasicNameValuePair("band", Integer.toString(link.getBand())));
		params.add(new BasicNameValuePair("delay", link.getDelay() + "ms"));
		params.add(new BasicNameValuePair("loss", Integer.toString(link.getLoss())));
		sendPostRequest("add-link", params);
	}
	
	public void mnRequestSetLink(Link link, Node node1, Node node2) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("from", node1.getName()));
		params.add(new BasicNameValuePair("to", node2.getName()));
		params.add(new BasicNameValuePair("band", Integer.toString(link.getBand())));
		params.add(new BasicNameValuePair("delay", link.getDelay() + "ms"));
		params.add(new BasicNameValuePair("loss", Integer.toString(link.getLoss())));
		sendPostRequest("set-link", params);
	}
	
	public String mnRequestNodeCommand(Node node, String cmd) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", node.getName()));
		params.add(new BasicNameValuePair("cmd", cmd));			
		return sendPostRequest("node-command", params);
	}
	
	public String mnRequestSystemCommand(String cmd) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("cmd", cmd));			
		return sendPostRequest("system-command", params);		
	}
	
	public String mnRequestScpCommand(String src, String dst) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("source", src));
		params.add(new BasicNameValuePair("dest", dst));		
		return sendPostRequest("scp-command", params);		
	}
	
	public void mnRequestOpenTerm(Node node) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", node.getName()));
		params.add(new BasicNameValuePair("label", node.getIp()));		
		sendPostRequest("open-term", params);
	}
	
	public void mnRequestWriteFile(String name, String content) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("content", content));		
		sendPostRequest("write-file", params);
	}
	
	public void mnRequestSetIp(Host node, String ip) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", node.getName()));
		params.add(new BasicNameValuePair("ip", ip));
		sendPostRequest("set-ip", params);
	}

	public void mnRequestSetMac(Host node, String mac, int index) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", node.getName()));
		params.add(new BasicNameValuePair("mac", mac));		
		params.add(new BasicNameValuePair("eth", node.getEth(index)));
		sendPostRequest("set-mac", params);
	}
	
	public void mnRequestSetExtMac(Switch node, String mac, int index) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", node.getName()));
		params.add(new BasicNameValuePair("mac", mac));		
		params.add(new BasicNameValuePair("eth", node.getEth(index)));
		sendPostRequest("set-ext-mac", params);
	}
	
	public boolean mnRequestGetStatus(Switch node, Link link) {
		int index = node.getLinks().indexOf(link);
		String cmd = "cat /sys/class/net/" 
				+ node.getEth(index) + "/operstate";
		String res = mnRequestNodeCommand(node, cmd);
		return res.equals("up");
	}
	
	public void mnRequestSetStatus(Node node1, Node node2, boolean status) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("from", node1.getName()));
		params.add(new BasicNameValuePair("to", node2.getName()));
		params.add(new BasicNameValuePair("status", status?"up":"down"));
		sendPostRequest("set-status", params);
	}
	
	public String mnRequestGetDpid(Node node) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", node.getName()));
		return sendPostRequest("get-dpid", params);		
	}
	
	public String mnRequestListFiles(String name) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("name", name));
		return sendPostRequest("list-files", params);
	}
}
