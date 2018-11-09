package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import common.Config;
import common.Console;
import common.ServerError;

public class Ssh {

	private volatile boolean running;
	private String currentUser;
	private String currentHost;
	private Session session;
	private OutputStream out;
	private Config config;
	private String host;
	
	public Ssh(Config config) {
		this.config = config;
		this.running = true;
	}
	
	private void connect(String user, int port, String host, String keyFile) throws ServerError {
		int index = 1, timeout = config.consoleConnectTimeout;
		int maxTries = config.consoleSshMaxTries;
		while (running) {
			try {
				Console.info("Openning SSH session on " + user + "@"
						+ host + " port " + port + " key " + keyFile);
				JSch jsch = new JSch();
				jsch.addIdentity(keyFile);
				session = jsch.getSession(user, host, port);
				session.setConfig("StrictHostKeyChecking", "no");
				session.connect(1000 * timeout);
				this.host = host;
			}
			catch (Exception ex) {
				Console.warning("Failed to open SSH session with " + host + "");
				Console.warning(ex.getMessage());
				if (running && index++ < maxTries) {
					Console.warning("Retrying in " + index + "s (" 
							+ index + "/" + maxTries + ") ...");
					Console.pause(index * 1000);
					continue;
				}
				else throw new ServerError("Fail to connect '" + host + "'");
			}
			break;
		}
	}
	
	
	private void doRun(StringBuffer buffer, String cmd) throws ServerError {
		try {
			String line;
			Console.info("Running '" + cmd + "'" + " on " + host);
			Channel channel = session.openChannel("exec");
			((ChannelExec)channel).setPty(true);
	        ((ChannelExec)channel).setCommand(cmd);
	        channel.setInputStream(null);
	        ((ChannelExec)channel).setErrStream(System.err);
	        InputStream in = channel.getInputStream();
	        out = channel.getOutputStream();
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        channel.connect();
	        
	        // Read output
	        while (running == true) {
	        	while ((line = br.readLine()) != null && running) {
	        		buffer.append(line + "\n");
	        		Console.log(host + " '" + line + "'", "OUTPUT");
	        	}
	        	if (channel.isClosed()) {
	        		if (in.available() > 0) 
	        			continue; 
	        		break;
	        	}
	        	Console.pause(1000);
	        }
	        if (running == false)
	        	buffer.append("Process interrupted\n");
	        channel.disconnect();
		} 
		catch (JSchException | IOException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
	
	public void run(String user, int port, String host, String keyFile, String cmd, 
				StringBuffer buffer) throws ServerError {
		if (!user.equals(currentUser) 
				|| !host.equals(currentHost))
			close();
		if (session == null || !session.isConnected()) {
			connect(user, port, host, keyFile);
			currentUser = user;
			currentHost = host;
		}
		doRun(buffer, cmd);
	}
	
	private void doUpload(String source, String dest) throws ServerError {
		try {
			Console.info("Uploading '" + source + "' to '" + dest + "'");
			Channel channel = session.openChannel("sftp");
			channel.connect();
			((ChannelSftp)channel).cd(dest);
			File file = new File(source);
			((ChannelSftp)channel).put(new FileInputStream(file), file.getName());
	        channel.disconnect();
		} 
		catch (JSchException | IOException | SftpException ex) {
			throw new ServerError(ex.getMessage());
		}
	}

	public void upload(String user, int port, String host, String keyFile, 
				String source, String dest) throws ServerError {
		if (!user.equals(currentUser) 
				|| !host.equals(currentHost))
			close();
		if (session == null || !session.isConnected()) {
			connect(user, port, host, keyFile);
			currentUser = user;
			currentHost = host;
		}
		doUpload(source, dest);
	}

	public boolean stop() throws ServerError {
		if (session != null && session.isConnected()) {
			Console.warning("User interruption detected");
			running = false;
			if (out != null) {
				try {
					out.write(3);
					out.flush();
					return true;
				} 
				catch (IOException ex) {
					throw new ServerError(ex.getMessage());
				}
			}
		}
		return false;
	}
	
	public void close() {
		if (session != null && session.isConnected()) {
			Console.info("Closing SSH session on " 
					+ currentUser + "@" + currentHost);
			session.disconnect();
			session = null;
		}
	}
}
