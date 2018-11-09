package net.floodlightcontroller.sirius.console;

public class Toolbox {

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
}
