package net.floodlightcontroller.sirius.providerconfig;

import java.util.ArrayList;
import java.util.UUID;
import org.projectfloodlight.openflow.types.DatapathId;
import net.floodlightcontroller.sirius.topology.FlowTable;

public class OpenVSwitch3 {
	
	protected UUID uuid;
	protected ArrayList<Controller3> arrayListController;
	protected ArrayList<Bridge3> arrayListBridge;
	protected String version;
	protected String dataPathId;
	protected DatapathId datapathId;
	private ArrayList<FlowTable> flowTable = new ArrayList<FlowTable>();
	
	public OpenVSwitch3(UUID uuid, ArrayList<Controller3> arrayListController,
			ArrayList<Bridge3> arrayListBridge, String version, DatapathId datapathId) {
		super();
		this.uuid = uuid;
		this.arrayListController = arrayListController;
		this.arrayListBridge = arrayListBridge;
		this.version = version;
		this.datapathId = datapathId;
		
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public ArrayList<Controller3> getArrayListController() {
		return arrayListController;
	}

	public void setArrayListController(ArrayList<Controller3> arrayListController) {
		this.arrayListController = arrayListController;
	}

	public ArrayList<Bridge3> getArrayListBridge() {
		return arrayListBridge;
	}

	public void setArrayListBridge(ArrayList<Bridge3> arrayListBridge) {
		this.arrayListBridge = arrayListBridge;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDataPathId() {
		return dataPathId;
	}

	public void setDataPathId(String dataPathId) {
		this.dataPathId = dataPathId;
	}

	public DatapathId getDatapathId() {
		return datapathId;
	}

	public void setDatapathId(DatapathId datapathId) {
		this.datapathId = datapathId;
	}

	public ArrayList<FlowTable> getFlowTable() {
		return flowTable;
	}

	public void setFlowTable(ArrayList<FlowTable> flowTable) {
		this.flowTable = flowTable;
	}
}
