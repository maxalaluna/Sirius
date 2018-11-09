package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic;

/**
 * Stores information about Nodes in the context of the Heuristic
 * @author Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class NodeHeuristicInfo implements Comparable<Object>{

	private int indexNode = -1;
	private int indexVirtualEdge = -1;
	private double nodeUtility = -1;
	int backupLocalization = -1;
	int belongsToCloud = -1;
	private double x = -1;
	private double flow = -1;

	public NodeHeuristicInfo(int indexNode, double nodeUtility, int backupLocalization) {
		super();
		this.indexNode = indexNode;
		this.nodeUtility = nodeUtility;
		this.backupLocalization = backupLocalization;
	}
	
	public NodeHeuristicInfo(int indexNode, double nodeUtility, int backupLocalization, int cloud) {
		super();
		this.indexNode = indexNode;
		this.nodeUtility = nodeUtility;
		this.belongsToCloud = cloud;
	}
	
	public NodeHeuristicInfo(int indexNode, double nodeUtility) {
		super();
		this.indexNode = indexNode;
		this.nodeUtility = nodeUtility;
	}
	
	public NodeHeuristicInfo(int indexNode, int indexVirtualEdge, double x, double flow) {
		super();
		this.indexNode = indexNode;
		this.indexVirtualEdge = indexVirtualEdge;
		this.x = x;
		this.flow = flow;
	}

	public int getIndexVirtualEdge() {
		return indexVirtualEdge;
	}

	public void setIndexVirtualEdge(int indexVirtualEdge) {
		this.indexVirtualEdge = indexVirtualEdge;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getFlow() {
		return flow;
	}

	public void setFlow(double flow) {
		this.flow = flow;
	}

	public int getIndexNode() {
		return indexNode;
	}

	public void setIndexNode(int indexNode) {
		this.indexNode = indexNode;
	}

	public double getNodeUtility() {
		return nodeUtility;
	}

	public void setNodeUtility(double nodeUtility) {
		this.nodeUtility = nodeUtility;
	}
	
	public int getBackupLocalization() {
		return backupLocalization;
	}

	public void setBackupLocalization(int backupLocalization) {
		this.backupLocalization = backupLocalization;
	}

	public int getBelongsToCloud() {
		return belongsToCloud;
	}

	public void setBelongsToCloud(int belongsToCloud) {
		this.belongsToCloud = belongsToCloud;
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (o instanceof NodeHeuristicInfo) && 
				(indexNode == ((NodeHeuristicInfo) o).indexNode) && 
//				(indexVirtualEdge == ((NodeHeuristicInfo) o).indexVirtualEdge) &&
				(nodeUtility == ((NodeHeuristicInfo) o).nodeUtility);
	}

	@Override
	public int hashCode()
	{
		return 3 * indexNode + 7 * (int)nodeUtility + 13 * indexVirtualEdge;
	}

	@Override
	public int compareTo(Object nodeHeuristicInfoToCompare){

		return Double.compare(((NodeHeuristicInfo) nodeHeuristicInfoToCompare).getNodeUtility(), this.nodeUtility);
	}

	@Override
	public String toString(){

		return "index: " +this.indexNode +"; nodeUtility: "+this.nodeUtility;
	}
}
