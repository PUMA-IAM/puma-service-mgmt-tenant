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
package puma.sp.mgmt.tenant.users;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import puma.sp.mgmt.model.attribute.Attribute;
import puma.sp.mgmt.model.attribute.AttributeFamily;
import puma.sp.mgmt.model.attribute.Multiplicity;
import puma.sp.mgmt.model.organization.Tenant;
import puma.sp.mgmt.model.user.User;
import puma.sp.mgmt.repositories.attribute.AttributeFamilyService;
import puma.sp.mgmt.repositories.organization.TenantService;
import puma.sp.mgmt.repositories.user.UserService;
import puma.sp.mgmt.tenant.MainController;
import puma.sp.mgmt.tenant.msgs.MessageManager;

@Controller
public class UserController {
	private static final Logger logger = Logger.getLogger(UserController.class
			.getName());
	@Autowired
	private TenantService tenantService;
	@Autowired
	private UserService userService;
	@Autowired
	private AttributeFamilyService familyService;
	@Autowired
	private HttpServletRequest request;
	
	@RequestMapping(value = "/users/{tenantId}", method = RequestMethod.GET)
	public String userOverview(@PathVariable("tenantId") Long tenantId,
			ModelMap model, HttpSession session, HttpServletRequest request) {
		Tenant tenant = this.tenantService.findOne(tenantId);
		if (!doCheck(tenant, session))
			return "redirect:" + MainController.AUTHENTICATION_URL + "?RelayState=" + request.getRequestURL().toString();
		model.addAttribute("tenant", tenant);
		model.addAttribute("users", this.userService.byTenant(tenant));
		model.addAttribute("msgs",
    			MessageManager.getInstance().getMessages(session)); 
		return "users/show";
	}
	
	@RequestMapping(value = "/users/{tenantId}/create-impl", method = RequestMethod.POST)
	public String createUser(
			@PathVariable("tenantId") Long tenantId, 
			@RequestParam("name") String name,
			@RequestParam("password") String password,
			ModelMap model, HttpSession session, HttpServletRequest request) {
		Tenant tenant = this.tenantService.findOne(tenantId);
		if (!doCheck(tenant, session))
			return "redirect:" + MainController.AUTHENTICATION_URL + "?RelayState=" + request.getRequestURL().toString();
		try {
			User user = new User();
			user.setLoginName(name);
			user.setPassword(password);
			user.setTenant(tenant);
			this.userService.addUser(user);
			MessageManager.getInstance().addMessage(session, "success", "User with loginname " + name + " has been created.");
		} catch (NoSuchAlgorithmException e) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not create user: " + e.getMessage());
		} catch (InvalidKeySpecException e) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not create user: " + e.getMessage());
		}
		return "redirect:/users/" + tenantId;
	}
	
	@RequestMapping(value = "/users/{tenantId}/{userId}/delete")
	public String deleteUser(
			@PathVariable("tenantId") Long tenantId, 
			@PathVariable("userId") Long userId, 
			ModelMap model, HttpSession session, HttpServletRequest request) {
		if (!doCheck(this.tenantService.findOne(tenantId), session))
			return "redirect:" + MainController.AUTHENTICATION_URL + "?RelayState=" + request.getRequestURL().toString();
		User user = this.userService.byId(userId);
		if (user == null) {
			MessageManager.getInstance().addMessage(session, "failure",
					"Could not find a user with id " + userId + ".");
		} else {
			this.userService.deleteUser(userId);
		}
		return "redirect:/users/" + tenantId;
	}
	
	@RequestMapping(value = "/users/{tenantId}/info/{userId}", method = RequestMethod.GET)
	public String showDetails(@PathVariable("tenantId") Long tenantId, 
			@PathVariable("userId") Long userId, 
			ModelMap model, HttpSession session, HttpServletRequest request) {
		if (!doCheck(this.tenantService.findOne(tenantId), session))
			return "redirect:" + MainController.AUTHENTICATION_URL + "?RelayState=" + request.getRequestURL().toString();
		User user = this.userService.getUserById(userId);
		if (user != null) {
			model.addAttribute("msgs",
	    			MessageManager.getInstance().getMessages(session)); 
			model.addAttribute("tenant", this.tenantService.findOne(tenantId));
			model.addAttribute("selectedUser", user);
			model.addAttribute("selectedUserAttributes", user.getAttributes());
			model.addAttribute("families", this.getEligibleFamilies(user.getAttributes(), this.familyService.findAllOrganizationProvider(this.tenantService.findOne(tenantId))));
			return "/users/details";
		} else {
			MessageManager.getInstance().addMessage(session, "failure",
					"The user you selected does not exist. Could not show details");
			return "redirect:/users/" + tenantId; 
		}		
	}

	private List<AttributeFamily> getEligibleFamilies(Set<Attribute> attributes,
			List<AttributeFamily> families) {
		List<AttributeFamily> result = new ArrayList<AttributeFamily>(families.size());
		result.addAll(families);
		for (Attribute next: attributes) 
			if (next.getFamily().getMultiplicity() == Multiplicity.ATOMIC && result.contains(next.getFamily()))
				result.remove(next.getFamily());
		return result;
	}

	private Boolean doCheck(Tenant tenant, HttpSession session) {
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure",
					"You are not yet authenticated. Please authenticate before performing this operation.");
			logger.log(Level.WARNING, "User not authenticated.");			
			return false;
		}
		if (!isAuthorized(session, tenant)) {
			MessageManager.getInstance().addMessage(session, "failure",
					"You are not authorized to perform this operation.");
			logger.log(Level.WARNING, "Unauthorized operation caught");
			return false;
		}
		return true;
	}
	
	private Boolean isAuthorized(HttpSession session, Tenant tenant) {
		if (!MainController.isAuthenticated(session))
			return false;
		// TODO Authorization checks for tenant
		return true;
	}
}
