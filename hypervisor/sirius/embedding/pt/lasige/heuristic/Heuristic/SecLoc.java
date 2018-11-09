package net.floodlightcontroller.sirius.embedding.pt.lasige.heuristic.Heuristic;

/**
 * Stores information about security of Nodes and Clouds in the context of the Heuristic
 * @author Max Alaluna, fc47349, Faculdade de Ciencias da Universidade de Lisboa
 *
 */
public class SecLoc {
	
	private double security;
	private double cloudLocation;
	
	public SecLoc(double d, double double1) {
		super();
		this.security = d;
		this.cloudLocation = double1;
	}
	
	public double getSecurity() {
		return security;
	}
	public void setSecurity(double security) {
		this.security = security;
	}
	public double getCloudLocation() {
		return cloudLocation;
	}
	public void setCloudLocation(double cloudLocation) {
		this.cloudLocation = cloudLocation;
	}
	
	@Override
	public int hashCode()
	{
		return (int) (3 * security + 13 * cloudLocation);
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof SecLoc) && 
				(security == ((SecLoc) o).security) && 
				(cloudLocation == ((SecLoc) o).cloudLocation);
	}
}
