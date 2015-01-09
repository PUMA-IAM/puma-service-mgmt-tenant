/*******************************************************************************
 * Copyright 2014 KU Leuven Research and Developement - iMinds - Distrinet 
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 *    Administrative Contact: dnet-project-office@cs.kuleuven.be
 *    Technical Contact: maarten.decat@cs.kuleuven.be
 *    Author: maarten.decat@cs.kuleuven.be
 ******************************************************************************/
package puma.sp.mgmt.tenant.policy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
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
import puma.sp.mgmt.model.organization.PolicyLangType;
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
	private static final String DEFAULT_POLICY_TEXT_XACML =
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
	private static final String DEFAULT_POLICY_TEXT_STAPL =
			"Policy(\"policy\") := when( true ) apply DenyOverrides to (\n" +
			"  Rule(\"rule\") := deny iff ( true ),\n" +
			"  Rule(\"default-permit\") := permit\n" +
			")";
	
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
    	
    	if(tenant.getPolicyLanguage() == PolicyLangType.STAPL) {
    		model.addAttribute("policySet", this.assembleStaplPolicy(tenant));
    		model.addAttribute("skeleton", DEFAULT_POLICY_TEXT_STAPL);
    	} else if(tenant.getPolicyLanguage() == PolicyLangType.XACML) {
    		model.addAttribute("policySet", this.assembleXacmlPolicy(tenant));
    		model.addAttribute("skeleton", DEFAULT_POLICY_TEXT_XACML);
    	} else throw new UnsupportedOperationException();
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
		Policy policy = this.policyRep.findOne(new Policy.Key(policyId, tenant.getPolicyLanguage()));
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
    public String deletePolicy(ModelMap model, HttpSession session,
    		@PathVariable("tenantId") Long tenantIdentifier,
    		@PathVariable("policyId") String id) {
    	Tenant tenant = this.tenantService.findOne(tenantIdentifier);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not find tenant with id " + tenantIdentifier.toString());
			model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
			return "index";
		}
		Policy policy = this.policyRep.findOne(new Policy.Key(id, tenant.getPolicyLanguage()));
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
    	this.policyService.removePolicy(id, tenant.getPolicyLanguage());
		// process policy (construct the new policy set and add it to the central puma pdp)
    	loadPolicy(tenant, session); // 
    	return "redirect:/policy/" + tenantIdentifier.toString();
    }

	@RequestMapping(value = "/policy/{tenantId}/create-impl", method = RequestMethod.POST)
    public String createPolicy(ModelMap model, HttpSession session,
    		@PathVariable("tenantId") Long tenantId, 
    		@RequestParam("id") String id,
			@RequestParam("policy") String policy) {
		Tenant tenant = this.tenantService.findOne(tenantId);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not find tenant with id " + tenantId.toString());
			model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
			return "index";
		}
		if (this.policyRep.findOne(new Policy.Key(id, tenant.getPolicyLanguage())) != null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not create policy: id already exists!");
			return "redirect:/policy/" + tenantId.toString();
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
    	Tenant topLevelOrganization = organization;
    	// NOTE: The current implementation might suffer from concurrency issues
    	// 1. Find the top level organization, we are going to deploy only the top-level policies (LATER: policy file with reference for each subtenant)
    	while (topLevelOrganization.getSuperTenant() != null)
    		topLevelOrganization = topLevelOrganization.getSuperTenant();
    	
    	
    	// 2. reconstruct the complete tenant policy
    	if(organization.getPolicyLanguage() == PolicyLangType.STAPL)
    		policy = this.assembleStaplPolicy(topLevelOrganization);  //FIXME generalize for stapl+xacml, update Manager... 
    	else if(organization.getPolicyLanguage() == PolicyLangType.XACML)
    		policy = this.assembleXacmlPolicy(topLevelOrganization);
    	else throw new UnsupportedOperationException();
    	
    	// 3. load into Central PUMA PDP 
    	CentralPUMAPDPMgmtRemote centralPUMAPDP = CentralPUMAPDPManager.getInstance().getCentralPUMAPDP(organization.getPolicyLanguage().getName());
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
    private String assembleXacmlPolicy(Tenant organization) {
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
		"			    <SubjectMatch MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" + 
		"			      <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + organization.getId().toString() + "</AttributeValue>\n" + 
		"				    <SubjectAttributeDesignator AttributeId=\"subject:tenant\" DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>\n" + 
		"			    </SubjectMatch>\n" + 
		"		    </Subject>\n" + 
		"	    </Subjects>\n" + 
		"	</Target>\n";
		for (Policy next: this.policyService.getPolicies(organization)) {
			result = result + next.toXACML() + "\n";
		}
		for (Tenant next: organization.getSubtenants())
			result = result + this.assembleXacmlPolicy(next) + "\n";
		return result + "</PolicySet>";
    }
    
    /**
     * Assemble all 'sub' policies into a single policy set with description
     * @param organization The tenant to construct the policy set for
     * @return The XACML representation of the complete policy set applying (only) to the subjects of the specified organization
     */
    private String assembleStaplPolicy(Tenant organization) {
    	List<String> policiesToMerge = new ArrayList<String>();
    	for (Policy next: this.policyService.getPolicies(organization)) {
    		policiesToMerge.add(next.toXACML());
    	}
		String result = "Policy(\"tenantsetid:" + organization.getId().toString() + "\") := when (subject.tenant === " + organization.getId().toString() + ") apply DenyOverrides to (\n";
		
		{
			Iterator<Policy> it = this.policyService.getPolicies(organization).iterator();
			while(it.hasNext()) {
				Policy next = it.next();
				if(it.hasNext() || !organization.getSubtenants().isEmpty())
					result = result + next.getContent() + ",\n";
				else
					result = result + next.getContent() + "\n";
			}
		}
		
		{
			Iterator<Tenant> it = organization.getSubtenants().iterator();
			while(it.hasNext()) {
				Tenant next = it.next();
				if(it.hasNext())
					result = result + this.assembleStaplPolicy(next) + ",\n";
				else
					result = result + this.assembleStaplPolicy(next) + "\n";
			}
		}
		
		return result + ")";
    }
}
