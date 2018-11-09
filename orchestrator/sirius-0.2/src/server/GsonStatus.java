package server;

import common.GsonData;

public class GsonStatus {

	public int index;
	public int length;
	public String message;
	public GsonData data;
	
	public GsonStatus(int index, int length, String message, GsonData data) {
		this.index = index;
		this.length = length;
		this.message = message;
		this.data = data;
	}	
}
