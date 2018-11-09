package net.floodlightcontroller.sirius.console;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Console {

	private static String home = System.getenv("SIRIUS_HOME");
	private static int offset = 0;
	
	public static void shiftRight() {
		offset++;
	}
	
	public static void shiftLeft() {
		offset--;
	}
	
	public static void log(String message) {
		String margin = fill("--", offset);
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("[hh:mm:ss.SSS] ");
		System.out.println(format.format(calendar.getTime()) + margin + message);
	}
	
	public static void warning(String message) {
		String margin = fill("--", offset);
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("[hh:mm:ss.SSS] ");
		System.out.println(format.format(calendar.getTime()) + margin + message);
	}
	
	public static void error(String message) {
		String margin = fill("--", offset);
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("[hh:mm:ss.SSS] ");
		System.out.println(format.format(calendar.getTime()) + margin + message);
	}
	
	public static String getPath(String name) throws ServerError {
		if (home == null)
			throw new ServerError("SIRIUS_HOME variable not set");
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
				int mod = index % 256;
				adr[k] = String.format("%02X", mod);
				index = index / 256;
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
				int mod = index % 256;
				adr[k] = String.format("%d", mod);
				index = index / 256;
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
