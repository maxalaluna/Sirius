package net.floodlightcontroller.sirius.topology;

import java.util.ArrayList;

public class FlowTable {

	private int flowTableNumber;
	private ArrayList<FlowEntry> flowEntry;
	private int lastFlowEntry = 0;


	public FlowTable(int flowTableNumber, ArrayList<FlowEntry> flowEntry) {
		super();
		this.flowTableNumber = flowTableNumber;
		this.flowEntry = flowEntry;
	}

	public int getFlowTableNumber() {
		return flowTableNumber;
	}

	public void setFlowTableNumber(int flowTableNumber) {
		this.flowTableNumber = flowTableNumber;
	}

	public ArrayList<FlowEntry> getFlowEntry() {
		return flowEntry;
	}

	public void setFlowEntry(ArrayList<FlowEntry> flowEntry) {
		
		int lastFlowEntryAux = 0;
		
		for(int i=0; i < flowEntry.size(); i++){
			if (flowEntry.get(i).getFlowEntryId() > lastFlowEntryAux){
				
				lastFlowEntryAux = flowEntry.get(i).getFlowEntryId();
			}
		}
		
		if(this.getLastFlowEntry() < lastFlowEntryAux){
			
			this.setLastFlowEntry(lastFlowEntryAux);
		}
		
		this.flowEntry = flowEntry;
	}

	public synchronized boolean addFlowEntry(FlowEntry flowEntry){

		if (this.flowEntry.add(flowEntry)){
			this.lastFlowEntry = flowEntry.getFlowEntryId();
			return true;
		}
		else{
			return false;
		}
	}

	public int getLastFlowEntry() {
		return lastFlowEntry;
	}

	public void setLastFlowEntry(int lastFlowEntry) {
		this.lastFlowEntry = lastFlowEntry;
	}
	
	public String toString(){
		
		String info = "\n";
		
		for (int i = 0; i < this.flowEntry.size(); i++){
			String id = this.flowEntry.get(i).getFlowEntryId() +"";
			String cookie = this.flowEntry.get(i).getCookie() +"";
			String hardTimeout = this.flowEntry.get(i).getHardTimeOut() +"";
			String idleTimeOut = this.flowEntry.get(i).getIdleTimeOut() +"";
			String priority = this.flowEntry.get(i).getPriority() +"";
			String match = this.flowEntry.get(i).getMatch().toString();
			String action = this.flowEntry.get(i).getOfActions().toString();
			
			info = info.concat("Id: "+id+"; cookie: "+cookie+"; hardTimeout: "+hardTimeout+"; idleTimeOut:"+idleTimeOut+"; priority:"+priority+"; match:"+match+"; action: "+action+"\n");
		}
		return info;
	}
}
