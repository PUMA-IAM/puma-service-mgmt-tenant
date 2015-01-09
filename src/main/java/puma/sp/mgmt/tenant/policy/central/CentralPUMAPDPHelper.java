package puma.sp.mgmt.tenant.policy.central;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import puma.rmi.pdp.mgmt.CentralPUMAPDPMgmtRemote;

public class CentralPUMAPDPHelper {

	private ConcurrentMap<String, CentralPUMAPDPMgmtRemote> pdps = new ConcurrentHashMap<>();
	
	public CentralPUMAPDPHelper() {
		
	}
	
	public void addPDP(String name, CentralPUMAPDPMgmtRemote pdp) {
		pdps.put(name, pdp);
	}
	
	public CentralPUMAPDPMgmtRemote getPDP(String name) {
		return pdps.get(name);
	}
	
	public Map<String, CentralPUMAPDPMgmtRemote> getAll() {
		return new HashMap<>(pdps);
	}
}
