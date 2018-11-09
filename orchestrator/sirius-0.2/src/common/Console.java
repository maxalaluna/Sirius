package common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Console {

	private static String home = System.getenv("SUPERCLOUD_HOME");
	private static PrintWriter log;
	
	public static void init(String name) throws ServerError {
		try {
			String path = getPath(name);
			FileWriter fw = new FileWriter(path);
			BufferedWriter bw = new BufferedWriter(fw);
			log = new PrintWriter(bw);
		} 
		catch (IOException ex) {
			throw new ServerError(ex.getMessage());
		}
	}
	
	public static void close() {
		log.close();
	}
	
	public static String log(String message, String type) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("[hh:mm:ss.SSS] ");
		String str = format.format(calendar.getTime()) + type + " " + message;
		log.write(str + "\n");
		return str;
	}
	
	public static void info(String message) {
		String str = log(message, "INFO");
		System.out.println(str);
	}
	
	public static void warning(String message) {
		String str = log(message, "WARNING");
		System.out.println(str);
	}
	
	public static void error(String message) {
		String str = log(message, "ERROR");
		System.out.println(str);
	}
	
	public static String getPath(String name) throws ServerError {
		if (home == null)
			throw new ServerError("SUPERCLOUD_HOME variable not set");
		File file = new File(home, name);
		return file.getAbsolutePath();
	}
	
	public static ArrayList<Object> tuple(String name, Object value, Object options) {
		ArrayList<Object> tmp = new ArrayList<>();
		tmp.add(name); 
		tmp.add(value); 
		tmp.add(options);
		return tmp;
	}
	
	public static String fill(String data, int n) {
		StringBuffer buffer = new StringBuffer();
		for (int k = 0; k < n; k++)
			buffer.append(data);
		if (n > 0) buffer.append(" ");
		return buffer.toString();
	}
	
	public static String createMAC(String mac, int index) {
		String[] adr = mac.split(":");
		for (int k = 5; k >= 0; k--) {
			if (index > 0) {
				int cur = Integer.parseInt(adr[k]);
				int mod = (index + cur) % 256;
				adr[k] = String.format("%02X", mod);
				index = (index + cur) / 256;
			}
		}
		StringBuffer buffer = new StringBuffer();
		for (int k = 0; k < 6; k++)
			buffer.append(":" + adr[k]);
		return buffer.toString().substring(1);
	}
	
	public static String createIP(String ip, int index) {
		String[] adr = ip.split("\\.");
		for (int k = 3; k >= 0; k--) {
			if (index > 0) {
				int cur = Integer.parseInt(adr[k]);
				int mod = (index  + cur) % 256;
				adr[k] = String.format("%d", mod);
				index = (index + cur) / 256;
			}
		}
		StringBuffer buffer = new StringBuffer();
		for (int k = 0; k < 4; k++)
			buffer.append("." + adr[k]);
		return buffer.toString().substring(1);
	}
	
	public static int parse(String data) throws ServerError {
		try {
			return Integer.parseInt(data);
		}
		catch (NumberFormatException ex) {
			throw new ServerError("Cannot convert value to integer");
		}
	}
	
	public static void pause(int time) {
    	try {
    		Thread.sleep(time);
    	}
    	catch(Exception ex) {
    		warning("Cannot pause thread");
    	}
	}
}
