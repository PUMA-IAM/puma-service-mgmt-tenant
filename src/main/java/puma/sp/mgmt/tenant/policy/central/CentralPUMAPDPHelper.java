package puma.sp.mgmt.tenant.policy.central;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import puma.rest.client.CentralPDPClient;

public class CentralPUMAPDPHelper {

	private ConcurrentMap<String, CentralPDPClient> pdps = new ConcurrentHashMap<>();
	
	public CentralPUMAPDPHelper() {
		
	}
	
	public void addPDP(String name, String baseUrl) {
		pdps.put(name, new CentralPDPClient(baseUrl, name));
	}
	
	public CentralPDPClient getPDP(String name) {
		return pdps.get(name);
	}
	
	public Map<String, CentralPDPClient> getAll() {
		return new HashMap<>(pdps);
	}
}
