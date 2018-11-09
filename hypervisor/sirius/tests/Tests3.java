package net.floodlightcontroller.sirius.tests;

public class Tests3 {

	public static String createMAC(String mac, int index) {
		String[] adr = mac.split(":");
		for (int k = 5; k >= 0; k--) {
			if (index > 0) {
				int mod = index % 256;
				adr[k] = String.format("%02X", mod);
				index = index / 256;
			}
		}
		String address="";
		for (int i = 0; i < adr.length; i++){
			if(i!=adr.length-2){
				address += adr[i] + ":";
			}
		}
		return address;
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
		//return String.join(".", adr);
		return "";
	}
	public static void main(String[] args) {

		System.out.println("Tests03");
		System.out.println(createMAC("00:00:00:AA:88:12",2));
	}
}
