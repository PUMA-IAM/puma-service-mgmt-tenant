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
	
	private String langType;

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
	
	public String getLangType() {
		return langType;
	}

	public void setLangType(String langType) {
		this.langType = langType;
	}
	
	public CentralPUMAPDPOverview(String status, String policy, String langType) {
		this.status = status;
		this.policy = policy;
		this.langType = langType;
	}

}
