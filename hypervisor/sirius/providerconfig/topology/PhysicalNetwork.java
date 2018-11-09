package net.floodlightcontroller.sirius.providerconfig.topology;

import java.util.ArrayList;

import net.floodlightcontroller.sirius.providerconfig.Server3;
import net.floodlightcontroller.sirius.tenantconfig.Host;

public class PhysicalNetwork {

	private ArrayList<Host> hostList = new ArrayList<Host>();
	private ArrayList<Server3> vertexes;
	private ArrayList<EdgeSC> edgeSCApps;
	private String substratePartFileEmbedding;
	
	public PhysicalNetwork(ArrayList<Host> hostList, ArrayList<Server3> vertexes,
			ArrayList<EdgeSC> edgeSCApps) {
		super();
		this.hostList = hostList;
		this.vertexes = vertexes;
		this.edgeSCApps = edgeSCApps;
	}

	public ArrayList<Host> getHostList() {
		return hostList;
	}

	public void setHostList(ArrayList<Host> hostList) {
		this.hostList = hostList;
	}

	public ArrayList<Server3> getVertexes() {
		return vertexes;
	}

	public void setVertexes(ArrayList<Server3> vertexes) {
		this.vertexes = vertexes;
	}

	public ArrayList<EdgeSC> getEdgeSCApps() {
		return edgeSCApps;
	}

	public void setEdgeSCApps(ArrayList<EdgeSC> edgeSCApps) {
		this.edgeSCApps = edgeSCApps;
	}
	
	@Override
	public int hashCode() {
		
		return  (this.hostList.hashCode() ^ this.vertexes.hashCode() ^
				this.edgeSCApps.hashCode());
	}

	@Override
	public boolean equals(Object o) {

		return (o instanceof PhysicalNetwork) && 
				(hostList.equals(((PhysicalNetwork) o).hostList)) &&
				(vertexes.equals(((PhysicalNetwork) o).vertexes)) &&
				(edgeSCApps.equals(((PhysicalNetwork) o).edgeSCApps));

	}

	public String getSubstratePartFileEmbedding() {
		return substratePartFileEmbedding;
	}

	public void setSubstratePartFileEmbedding(String substratePartFileEmbedding) {
		this.substratePartFileEmbedding = substratePartFileEmbedding;
	}
}
