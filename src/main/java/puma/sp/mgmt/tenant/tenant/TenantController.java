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
package puma.sp.mgmt.tenant.tenant;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import puma.sp.mgmt.model.organization.Tenant;
import puma.sp.mgmt.model.organization.TenantMgmtType;
import puma.sp.mgmt.model.user.User;
import puma.sp.mgmt.repositories.organization.TenantRepository;
import puma.sp.mgmt.repositories.organization.TenantService;
import puma.sp.mgmt.repositories.user.UserService;
import puma.sp.mgmt.tenant.msgs.MessageManager;

@Controller
public class TenantController {
	@Autowired
	private TenantService tenantService;
	
	@Autowired
	private TenantRepository tenantRep;
	
	@Autowired
	private UserService userService;
	
	@RequestMapping(value = "/configuration/{tenantId}", method = RequestMethod.GET)
	public String showSubtenants(@PathVariable("tenantId") Long tenantId,
			ModelMap model, HttpSession session) {
		Tenant tenant = this.tenantService.findOne(tenantId);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not show details for the tenant: non-existing tenant id.");
			model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
			return "index";
		}
		model.addAttribute("tenant", tenant);		
		model.addAttribute("managementValues", TenantMgmtType.values());
		model.addAttribute("localAuthN", tenant.isAuthenticationLocallyManaged());
		model.addAttribute("localAuthZ", tenant.isAuthorizationLocallyManaged());
		return "configuration/conf";
	}

	@RequestMapping(value = "/configuration/{tenantId}/modify-impl", method = RequestMethod.POST)
	public String modifyTenant(@PathVariable("tenantId") Long tenantId,
			@RequestParam("name") String name,
			@RequestParam(value = "authn-endpoint", defaultValue = "") String authnEndpoint,
			@RequestParam(value = "attr-endpoint", defaultValue = "") String attrEndpoint,
			@RequestParam(value = "idp-public-key", defaultValue = "") String idpPublicKey,
			@RequestParam(value = "authz-endpoint", defaultValue = "") String authzEndpoint,
			ModelMap model, HttpSession session) {
		Tenant tenant = this.tenantService.findOne(tenantId);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Tenant with id " + tenantId + " not found.");
			return "redirect:/" + tenantId.toString();
		}
		
		// change
		tenant.setName(name);
		if (!tenant.isAuthenticationLocallyManaged()) {
			tenant.setAuthnRequestEndpoint(authnEndpoint);
			tenant.setAttrRequestEndpoint(attrEndpoint);
			tenant.setIdentityProviderPublicKey(idpPublicKey);
		}
		if (!tenant.isAuthorizationLocallyManaged())
			tenant.setAuthzRequestEndpoint(authzEndpoint);
		this.tenantRep.saveAndFlush(tenant);
		
		// Report
		MessageManager.getInstance().addMessage(session, "success", "Changed the configuration for the tenant.");
		return "redirect:/" + tenantId.toString();
	}
	
	@RequestMapping(value = "/configuration/{tenantId}/create-impl", method = RequestMethod.POST)
	public String createSubtenant(ModelMap model,
			HttpSession session, @PathVariable("tenantId") Long tenantId,  
			@RequestParam("tenantName") String tenantName,
			@RequestParam("userName") String loginName,
			@RequestParam("password") String password) {
		Tenant tenant = tenantService.findOne(tenantId);

		// First check whether the tenant exists
		if(tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Tenant with id " + tenantId + " not found.");
			return "redirect:/" + tenantId.toString();			
		}
		if (this.tenantService.byName(tenantName) != null) {
			MessageManager.getInstance().addMessage(session, "failure", "There is already a tenant with name " + tenantName + ". Please make sure that you specify a unique name.");
			model.addAttribute("tenant", tenant);
			return "redirect:/configuration/info/" + tenant.getId().toString();
		}
		try {
			// Construct tenant
			Tenant subtenant = new Tenant(tenantName, TenantMgmtType.Locally, "", "", "", "");
			subtenant.setSuperTenant(tenant);
			// Construct user
			User administrator = new User();
			administrator.setLoginName(loginName);
			administrator.setTenant(subtenant);
			administrator.setPassword(password);
			// Persist
			this.tenantService.addTenant(subtenant);
			this.userService.addUser(administrator);
			MessageManager.getInstance().addMessage(session, "success", "Subtenant successfully created.");
		} catch (NoSuchAlgorithmException e) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not create user: " + e.getMessage() + " The tenant will not be created.");
		} catch (InvalidKeySpecException e) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not create user: " + e.getMessage() + " The tenant will not be created.");
		}
		// Redirect back
		return "redirect:/configuration/info/" + tenantId.toString();
	}
	
	@RequestMapping(value = "/configuration/info/{tenantId}", method = RequestMethod.GET)
	public String showDetails(@PathVariable("tenantId") Long tenantId,
			ModelMap model, HttpSession session) {
		Tenant tenant = this.tenantService.findOne(tenantId);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not show details for the tenant: non-existing tenant id.");
			model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
			return "index";
		}
		model.addAttribute("tenant", tenant);
		model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
		return "configuration/subtenants";
	}
	
	@RequestMapping(value = "/configuration/{tenantId}/{subtenantId}/delete", method = RequestMethod.GET)
	public String deleteSubtenant(@PathVariable("tenantId") Long tenantId, @PathVariable("subtenantId") Long subtenantId,
			ModelMap model, HttpSession session) {
			Tenant subtenant = this.tenantService.findOne(subtenantId);
			Tenant tenant = this.tenantService.findOne(tenantId);
			if (subtenant == null || tenant == null) {
				MessageManager.getInstance().addMessage(session, "failure", "Could not remove tenant: non-existing tenant id.");
				model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
				return "index";
			}
			if (tenant.equals(subtenant) || !subtenant.getSuperTenant().equals(tenant)) {
				MessageManager.getInstance().addMessage(session, "failure", "Could not remove tenant: not a direct subtenant of the current tenant.");
				return "redirect:/configuration/info/" + tenant.getId().toString();
			}
			String name = subtenant.getName();
			this.tenantRep.delete(subtenantId);
			MessageManager.getInstance().addMessage(session, "success", "Tenant " + name + " succesfully deleted.");
			return "redirect:/configuration/info/" + tenant.getId().toString();
		
	}
}
