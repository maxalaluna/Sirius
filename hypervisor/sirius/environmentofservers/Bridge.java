package net.floodlightcontroller.sirius.environmentofservers;

import java.util.ArrayList;

public class Bridge {
	
	protected String name;
	protected ArrayList<Interfaces> arrayListInterfaces;
	
	public Bridge(String name, ArrayList<Interfaces> arrayListInterfaces) {
		super();
		this.name = name;
		this.arrayListInterfaces = arrayListInterfaces;
	}
	
	
	
	public Bridge(String name) {
		super();
		this.name = name;
	}



	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Interfaces> getArrayListInterfaces() {
		return arrayListInterfaces;
	}
	public void setArrayListInterfaces(ArrayList<Interfaces> arrayListInterfaces) {
		this.arrayListInterfaces = arrayListInterfaces;
	}
	
}
