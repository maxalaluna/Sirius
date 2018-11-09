package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic;

/**
 * Stores information about Links in the context of the Heuristic
 * @author Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class LinkHeuristicInfo implements Comparable<Object>{

	private int indexLink;
	private double bandwidht;
	private double latency;

	public LinkHeuristicInfo(int indexLink, double bandwidht, double latency) {
		super();
		this.indexLink = indexLink;
		this.bandwidht = bandwidht;
		this.setLatency(latency);
	}

	public int getIndexLink() {
		return indexLink;
	}

	public void setIndexLink(int indexLink) {
		this.indexLink = indexLink;
	}

	public double getBandwidht() {
		return bandwidht;
	}

	public void setBandwidht(double bandwidht) {
		this.bandwidht = bandwidht;
	}

	@Override
	public int compareTo(Object linkHeuristicInfoToCompare){

		return Double.compare(((LinkHeuristicInfo) linkHeuristicInfoToCompare).getBandwidht(), this.bandwidht);
	}

	@Override
	public String toString(){

		return "index: " +this.indexLink +"; bandwidht: "+this.bandwidht;
	}

	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}
}
