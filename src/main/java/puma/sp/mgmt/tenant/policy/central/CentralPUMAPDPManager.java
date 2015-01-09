package puma.sp.mgmt.tenant.policy.central;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import puma.rmi.pdp.mgmt.CentralPUMAPDPMgmtRemote;
import puma.sp.mgmt.model.organization.PolicyLangType;

public class CentralPUMAPDPManager {

	private static final Logger logger = Logger
			.getLogger(CentralPUMAPDPManager.class.getName());

	private static final String CENTRAL_PUMA_PDP_HOST_XACML = "puma-central-puma-pdp";

	private static final String CENTRAL_PUMA_PDP_RMI_NAME_XACML = "central-puma-pdp";

	private static final String CENTRAL_PUMA_PDP_HOST_STAPL = "puma-central-puma-pdp-stapl";

	private static final String CENTRAL_PUMA_PDP_RMI_NAME_STAPL = "central-puma-pdp-stapl";
	
	private static final int CENTRAL_PUMA_PDP_RMI_PORT = 2040;
	
	/**********************
	 * SINGLETON STUFF
	 **********************/

	private static CentralPUMAPDPManager instance;

	public static CentralPUMAPDPManager getInstance() {
		if (instance == null) {
			instance = new CentralPUMAPDPManager();
		}
		return instance;
	}
	
	/**********************
	 * THE CONNECTION TO THE CENTRAL PUMA PDP
	 **********************/

	private CentralPUMAPDPHelper pdpHelper = new CentralPUMAPDPHelper();
	
	public CentralPUMAPDPMgmtRemote getCentralPUMAPDP(String langType) {
		return this.pdpHelper.getPDP(langType);
	}
	
	public CentralPUMAPDPManager() {
		initConnection(CENTRAL_PUMA_PDP_HOST_XACML, CENTRAL_PUMA_PDP_RMI_PORT, CENTRAL_PUMA_PDP_RMI_NAME_XACML, PolicyLangType.XACML.name());
		initConnection(CENTRAL_PUMA_PDP_HOST_STAPL, CENTRAL_PUMA_PDP_RMI_PORT, CENTRAL_PUMA_PDP_RMI_NAME_STAPL, PolicyLangType.STAPL.name());
	}

	private void initConnection(final String host, final int port, final String rmiName, final String langType) {
		if (! setupCentralPUMAPDPConnection(host, port, rmiName, langType)) {
			logger.info("Retrying to reach the Registry periodically");
			// retry periodically
			Thread thread = new Thread(new Runnable() {				
				@Override
				public void run() {
					boolean go = true;
					while(go) {
						try {
							if(setupCentralPUMAPDPConnection(host, port, rmiName, langType)) {
								return; // end the thread here
							} else {
								logger.info("Failed again, trying again in 5 sec");
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									logger.log(Level.WARNING, "Sleep interrupted, is this important?", e);
								}
							}
						} catch(IllegalStateException e) {
							// this is thrown if the web application was stopped (I think)
							go = false;
						}
					}
				}
			});
			thread.start();
		}
	}

	/**
	 * Idempotent helper function to set up the RMI connection to the central
	 * Application PDP Registry.
	 */
	private boolean setupCentralPUMAPDPConnection(String host, int port, String rmiName, String langType) {
		if (!isCentralPUMAPDPConnectionOK(langType)) { //
			try {
				Registry registry = LocateRegistry.getRegistry(
						host, port);
				CentralPUMAPDPMgmtRemote centralPUMAPDP = (CentralPUMAPDPMgmtRemote) registry
						.lookup(rmiName);
				pdpHelper.addPDP(langType, centralPUMAPDP);
				logger.info("Set up the connection to the Central PUMA PDP.");
				return true;
			} catch (Exception e) {
				logger.log(Level.WARNING,
						"FAILED to reach the Central PUMA PDP", e);
				//centralPUMAPDP = null; // just to be sure
				return false;
			}
		}
		return true;
	}

	/**
	 * Helper function that returns whether the RMI connection to the
	 * Application PDP Registry is set up or not.
	 */
	private boolean isCentralPUMAPDPConnectionOK(String langType) {
		return pdpHelper.getPDP(langType) != null;
	}
	
	/***********************
	 * MANAGEMENT FUNCTIONALITY
	 ***********************/

	/**
	 * Returns an overview of the central PUMA PDP.
	 * 
	 * @return
	 */
	public List<CentralPUMAPDPOverview> getOverview() {
		Map<String, CentralPUMAPDPMgmtRemote> pdps = pdpHelper.getAll();
		List<CentralPUMAPDPOverview> overview = new ArrayList<>();
		for(String name : pdps.keySet()) {
			overview.add(getOverview(pdps, name));
		}
		return overview;
	}
	
	
	private CentralPUMAPDPOverview getOverview(Map<String, CentralPUMAPDPMgmtRemote> pdps, String langType) {
		String status;
		if (!this.isCentralPUMAPDPConnectionOK(langType)) {
			return new CentralPUMAPDPOverview("Could not establish a connection", "", langType);
		}
		try {
			status = pdps.get(langType).getStatus();
		} catch (RemoteException e) {
			status = "RemoteException: " + e.getMessage();
		}
		String policy;
		try {
			policy = pdps.get(langType).getCentralPUMAPolicy();
		} catch (RemoteException e) {
			policy = "RemoteException: " + e.getMessage();
		}
		return new CentralPUMAPDPOverview(status, policy, langType);
	}
	

}
