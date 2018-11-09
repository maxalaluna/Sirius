/**
 * SEGRID PROJECT - Testbed platform console 
 * Faculdade de Ciências da Universidade de Lisboa
 * Eric Vial (evial at lasige.di.fc.ul.pt)
 */

package net.floodlightcontroller.sirius.topology.xml;

public class Toolbox {

	public static String convertDpid(String dpid) {
		StringBuffer buffer = new StringBuffer();
		int length = dpid.length();
	    for (int k = 0; k < length; k += 2) {
	        String sub = dpid.substring(k, Math.min(length, k + 2));
	        if (k > 0) buffer.append(":" + sub);
	        else buffer.append(sub);
	    }
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
		return adr[0]+":"+adr[1]+":"+adr[2]+":"+adr[3]+":"+adr[4]+":"+adr[5];
		//return String.join(":", adr);
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
		return adr[0]+"."+adr[1]+"."+adr[2]+"."+adr[3];
		//return String.join(":", adr);
	}
}
