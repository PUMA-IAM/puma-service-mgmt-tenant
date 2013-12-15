package puma.sp.mgmt.tenant.policy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import puma.rmi.pdp.mgmt.CentralPUMAPDPMgmtRemote;
import puma.sp.mgmt.model.organization.Tenant;
import puma.sp.mgmt.model.policy.Policy;
import puma.sp.mgmt.model.policy.PolicyType;
import puma.sp.mgmt.repositories.organization.TenantService;
import puma.sp.mgmt.repositories.policy.PolicyRepository;
import puma.sp.mgmt.repositories.policy.PolicyService;
import puma.sp.mgmt.tenant.msgs.MessageManager;
import puma.sp.mgmt.tenant.policy.central.CentralPUMAPDPManager;

@Controller
public class PolicyController {
	private static final Logger logger = Logger.getLogger(PolicyController.class.getName()); 
	private static final String DEFAULT_POLICY_TEXT =
    		"<Policy xmlns=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\" \n" + 
    		"          xmlns:xacml-context=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\" \n" + 
    		"          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" + 
    		"          xsi:schemaLocation=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os http://docs.oasis-open.org/xacml/access_control-xacml-2.0-policy-schema-os.xsd\" \n" + 
    		"          xmlns:md=\"urn:mdc:xacml\" \n" + 
    		"          PolicyId=\"\" \n" + 
    		"          RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides\">\n" + 
    		"	<Description></Description>\n" + 
    		"	<Target>\n" + 
    		"	</Target>\n" + 
    		"	<Rule RuleId=\"rule:\" Effect=\"\">\n" + 
    		"		<Description></Description>\n" + 
    		"		<Condition>	      \n" + 
    		"		</Condition>\n" + 
    		"	</Rule>\n" + 
    		" </Policy>\n";
	
	@Autowired
	private PolicyService policyService;
	@Autowired
	private PolicyRepository policyRep;
	@Autowired
	private TenantService tenantService;
    
    @RequestMapping(value = "/policy/{tenantId}")
    public String policyOverview(@PathVariable("tenantId") Long tenantId, ModelMap model, HttpSession session) {
    	Tenant tenant = this.tenantService.findOne(tenantId);
    	if (tenant == null) {
    		MessageManager.getInstance().addMessage(session, "failure", "Could not find the tenant with id " + tenantId.toString());
    		model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
    		return "index";
    	}
    	model.addAttribute("policySet", this.assemblePolicy(tenant));
    	model.addAttribute("skeleton", DEFAULT_POLICY_TEXT);
    	model.addAttribute("policies", this.policyService.getPolicies(tenant));
    	model.addAttribute("tenant", tenant);
    	model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
    	return "policy/show";
    }
    
    @RequestMapping(value = "/policy/{tenantId}/info/{policyId}", method = RequestMethod.GET)
    public String viewPolicy(@PathVariable("tenantId") Long tenantId, @PathVariable("policyId") String policyId, ModelMap model, HttpSession session) {
    	Tenant tenant = this.tenantService.findOne(tenantId);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not find tenant with id " + tenantId.toString());
			model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
			return "index";
		}
		Policy policy = this.policyRep.findOne(policyId);
    	if (policy == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not find policy with id " + policyId);
			model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
			return "redirect:/policy/" + tenantId.toString();
		}
    	model.addAttribute("policy", policy);
    	model.addAttribute("tenant", tenant);
    	return "policy/details";
    }
    
    @RequestMapping(value = "/policy/{tenantId}/{policyId}/delete", method = RequestMethod.GET)
    public String createPolicy(ModelMap model, HttpSession session,
    		@PathVariable("tenantId") Long tenantIdentifier,
    		@PathVariable("policyId") String id) {
    	Tenant tenant = this.tenantService.findOne(tenantIdentifier);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not find tenant with id " + tenantIdentifier.toString());
			model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
			return "index";
		}
		Policy policy = this.policyRep.findOne(id);
    	if (policy == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not find policy with id " + id);
			model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
			return "redirect:/policy/" + tenantIdentifier.toString();
		}
    	if (!policy.getDefiningOrganization().equals(tenant)) {
    		MessageManager.getInstance().addMessage(session, "failure", "Could not remove policy with id " + id + ": only possible from the defining tenant");
    		model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
    		return "redirect:/policy/" + tenantIdentifier.toString();    		
    	}
    	this.policyService.removePolicy(id);
		// process policy (construct the new policy set and add it to the central puma pdp)
    	loadPolicy(tenant, session); // 
    	return "redirect:/policy/" + tenantIdentifier.toString();
    }

	@RequestMapping(value = "/policy/{tenantId}/create-impl", method = RequestMethod.POST)
    public String createPolicy(ModelMap model, HttpSession session,
    		@PathVariable("tenantId") Long tenantId, 
    		@RequestParam("id") String id,
			@RequestParam("policy") String policy) {
		if (this.policyRep.findOne(id) != null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not create policy: id already exists!");
			return "redirect:/policy/" + tenantId.toString();
		}
		Tenant tenant = this.tenantService.findOne(tenantId);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not find tenant with id " + tenantId.toString());
			model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
			return "index";
		}
		// Save the policy
		Policy newPolicy = new Policy();
		newPolicy.setId(id);	// TODO The id is given by the form, so it might be inconsistent with the id which was put in the policy.
		newPolicy.setContent(policy);
		newPolicy.setDefiningOrganization(tenant);
		newPolicy.setPolicyType(PolicyType.SINGLETENANT);
		this.policyService.storePolicy(newPolicy);
		// process policy (construct the new policy set and add it to the central puma pdp)
    	loadPolicy(tenant, session); // 
    	return "redirect:/policy/" + tenantId.toString();
    }

    /**
     * Helper function for loading a policy into the Central PUMA PDP, storing it 
     * in the database and putting errors into session Messages.
     * 
     * @param policy
     * @param session
     */
    private void loadPolicy(Tenant organization, HttpSession session) {
    	String policy;
    	// 1. reconstruct the complete tenant policy
    	policy = this.assemblePolicy(organization);    		
    	
    	// 2. load into Central PUMA PDP 
    	CentralPUMAPDPMgmtRemote centralPUMAPDP = CentralPUMAPDPManager.getInstance().getCentralPUMAPDP();
		try {
			centralPUMAPDP.loadTenantPolicy(organization.getId().toString(), policy);
			logger.info("Succesfully reloaded Central PUMA PDP policy");	
    		MessageManager.getInstance().addMessage(session, "success", "Policy loaded into Central PUMA PDP.");
		} catch (RemoteException e) {
			MessageManager.getInstance().addMessage(session, "warning", e.getMessage());
			logger.log(Level.WARNING, "Error when loading Tenant PUMA PDP policy", e);
		} catch (NullPointerException e) {
			MessageManager.getInstance().addMessage(session, "warning", "Could not deploy policy. Is the PDP available? Please contact your administrator");
			logger.log(Level.WARNING, "Error when loading Tenant PUMA PDP policy", e);
		}
    }
    
	/**
     * Assemble all 'sub' policies into a single policy set with description
     * @param organization The tenant to construct the policy set for
     * @return The XACML representation of the complete policy set applying (only) to the subjects of the specified organization
     */
    private String assemblePolicy(Tenant organization) {
    	List<String> policiesToMerge = new ArrayList<String>();
    	for (Policy next: this.policyService.getPolicies(organization)) {
    		policiesToMerge.add(next.toXACML());
    	}
		String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
		"<PolicySet  xmlns=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\" \n" + 
		"            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" + 
		"            xsi:schemaLocation=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\" \n" + 
		"            PolicySetId=\"urn:xacml:2.0:puma:tenantsetid:" + organization.getId().toString() + "\" \n" + 
		"            PolicyCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides\">\n" + 
		"	<Description>Policy set for tenant " + organization.getId().toString() + "</Description>\n" + 
		"	<Target>\n" + 
		"		<Subjects>\n" + 
		"		    <Subject>\n" + 
		"			    <SubjectMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:integer-equal\">\n" + 
		"			      <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#integer\">" + organization.getId().toString() + "</AttributeValue>\n" + 
		"				    <SubjectAttributeDesignator AttributeId=\"subject:tenant\" DataType=\"http://www.w3.org/2001/XMLSchema#integer\"/>\n" + 
		"			    </SubjectMatch>\n" + 
		"		    </Subject>\n" + 
		"	    </Subjects>\n" + 
		"	</Target>\n";
		for (Policy next: this.policyService.getPolicies(organization)) {
			result = result + next.toXACML() + "\n";
		}    
		return result + "</PolicySet>";
    }
}
