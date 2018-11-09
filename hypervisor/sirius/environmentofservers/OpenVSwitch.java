package net.floodlightcontroller.sirius.environmentofservers;

import java.util.ArrayList;
import java.util.UUID;

public class OpenVSwitch {
	
	protected UUID uuid;
	protected ArrayList<Controller> arrayListController;
	protected ArrayList<Bridge> arrayListBridge;
	protected String version;
	
	
	public OpenVSwitch(UUID uuid, ArrayList<Controller> arrayListController,
			ArrayList<Bridge> arrayListBridge, String version) {
		super();
		this.uuid = uuid;
		this.arrayListController = arrayListController;
		this.arrayListBridge = arrayListBridge;
		this.version = version;
	}


	public UUID getUuid() {
		return uuid;
	}


	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}


	public ArrayList<Controller> getArrayListController() {
		return arrayListController;
	}


	public void setArrayListController(ArrayList<Controller> arrayListController) {
		this.arrayListController = arrayListController;
	}


	public ArrayList<Bridge> getArrayListBridge() {
		return arrayListBridge;
	}


	public void setArrayListBridge(ArrayList<Bridge> arrayListBridge) {
		this.arrayListBridge = arrayListBridge;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}

}
