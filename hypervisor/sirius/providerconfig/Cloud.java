package net.floodlightcontroller.sirius.providerconfig;

import net.floodlightcontroller.sirius.util.Enum.CloudType;

public class Cloud {
	
	private String user; 
	private String password;
	private String description;
	private CloudType cloudType;
	private int cloudId;
	
	public Cloud(String user, String password, String description,
			CloudType cloudType, int cloudId) {
		super();
		this.user = user;
		this.password = password;
		this.description = description;
		this.cloudType = cloudType;
		this.cloudId = cloudId;
	}

	public int getCloudId() {
		return cloudId;
	}

	public void setCloudId(int cloudId) {
		this.cloudId = cloudId;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CloudType getCloudType() {
		return cloudType;
	}

	public void setCloudType(CloudType cloudType) {
		this.cloudType = cloudType;
	}
	
	public int hashCode() {
		
		return this.user.hashCode() ^ this.password.hashCode() ^ this.description.hashCode() ^ this.cloudType.hashCode() ^ this.cloudId;
	}

	@Override
	public boolean equals(Object o) {

		return (o instanceof Cloud) && 
				(user ==((Cloud) o).user) &&
				(password.equals(((Cloud) o).password)) &&
				(description.equals(((Cloud) o).description)) &&
				(cloudType.equals(((Cloud) o).cloudType)) &&
				(cloudId ==((Cloud) o).cloudId);
	}
}
