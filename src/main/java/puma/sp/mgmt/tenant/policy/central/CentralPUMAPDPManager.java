package puma.sp.mgmt.tenant.policy.central;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

import puma.rmi.pdp.mgmt.CentralPUMAPDPMgmtRemote;

public class CentralPUMAPDPManager {

	private static final Logger logger = Logger
			.getLogger(CentralPUMAPDPManager.class.getName());

	private static final String CENTRAL_PUMA_PDP_HOST = "puma-central-puma-pdp";

	private static final String CENTRAL_PUMA_PDP_RMI_NAME = "central-puma-pdp";

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

	private CentralPUMAPDPMgmtRemote centralPUMAPDP;
	
	public CentralPUMAPDPMgmtRemote getCentralPUMAPDP() {
		return this.centralPUMAPDP;
	}
	
	public CentralPUMAPDPManager() {
		if (! setupCentralPUMAPDPConnection()) {
			logger.info("Retrying to reach the Registry periodically");
			// retry periodically
			Thread thread = new Thread(new Runnable() {				
				@Override
				public void run() {
					boolean go = true;
					while(go) {
						try {
							if(setupCentralPUMAPDPConnection()) {
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
	private boolean setupCentralPUMAPDPConnection() {
		if (!isCentralPUMAPDPConnectionOK()) { //
			try {
				Registry registry = LocateRegistry.getRegistry(
						CENTRAL_PUMA_PDP_HOST, CENTRAL_PUMA_PDP_RMI_PORT);
				centralPUMAPDP = (CentralPUMAPDPMgmtRemote) registry
						.lookup(CENTRAL_PUMA_PDP_RMI_NAME);
				logger.info("Set up the connection to the Central PUMA PDP.");
				return true;
			} catch (Exception e) {
				logger.log(Level.WARNING,
						"FAILED to reach the Central PUMA PDP", e);
				centralPUMAPDP = null; // just to be sure
				return false;
			}
		}
		return true;
	}

	/**
	 * Helper function that returns whether the RMI connection to the
	 * Application PDP Registry is set up or not.
	 */
	private boolean isCentralPUMAPDPConnectionOK() {
		return centralPUMAPDP != null;
	}
	
	/***********************
	 * MANAGEMENT FUNCTIONALITY
	 ***********************/

	/**
	 * Returns an overview of the central PUMA PDP.
	 * 
	 * @return
	 */
	public CentralPUMAPDPOverview getOverview() {
		String status;
		try {
			status = centralPUMAPDP.getStatus();
		} catch (RemoteException e) {
			status = "RemoteException: " + e.getMessage();
		}
		String policy;
		try {
			policy = centralPUMAPDP.getCentralPUMAPolicy();
		} catch (RemoteException e) {
			policy = "RemoteException: " + e.getMessage();
		}
		return new CentralPUMAPDPOverview(status, policy);
	}

}
