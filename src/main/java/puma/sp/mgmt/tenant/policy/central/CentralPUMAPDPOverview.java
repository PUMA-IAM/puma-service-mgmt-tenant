package puma.sp.mgmt.tenant.policy.central;

/**
 * Simple helper class to represent the overview of a PDP.
 * 
 * @author Maarten Decat
 *
 */
public class CentralPUMAPDPOverview {
	
	private String status;
	
	private String policy;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}
	
	public CentralPUMAPDPOverview(String status, String policy) {
		this.status = status;
		this.policy = policy;
	}

}
