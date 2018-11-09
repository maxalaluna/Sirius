package net.floodlightcontroller.sirius.providerconfig;

import java.util.ArrayList;

public class Bridge3 {
	
	protected String name;
	protected ArrayList<Interfaces3> arrayListInterfaces;
	
	public Bridge3(String name, ArrayList<Interfaces3> arrayListInterfaces) {
		super();
		this.name = name;
		this.arrayListInterfaces = arrayListInterfaces;
	}
	
	public Bridge3(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Interfaces3> getArrayListInterfaces() {
		return arrayListInterfaces;
	}
	public void setArrayListInterfaces(ArrayList<Interfaces3> arrayListInterfaces) {
		this.arrayListInterfaces = arrayListInterfaces;
	}
	
	public boolean equals(Object o) {
		return (o instanceof Bridge3) && 
				(name.equals(((Bridge3) o).name));
	}

	public int hashCode() {
		return name.hashCode();
	}
}
