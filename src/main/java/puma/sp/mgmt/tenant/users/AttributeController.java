package puma.sp.mgmt.tenant.users;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import puma.sp.mgmt.model.attribute.Attribute;
import puma.sp.mgmt.model.organization.Tenant;
import puma.sp.mgmt.model.user.User;
import puma.sp.mgmt.repositories.attribute.AttributeService;
import puma.sp.mgmt.repositories.organization.TenantService;
import puma.sp.mgmt.tenant.msgs.MessageManager;

@Controller
public class AttributeController {
	private static final Logger logger = Logger.getLogger(AttributeController.class
			.getName());
	
	@Autowired
	private TenantService tenantService;
	@Autowired
	private AttributeService attributeService;
	
	@RequestMapping(value = "/attributes/{tenantId}/{userId}/{attributeId}/delete", method = RequestMethod.GET)
	public String deleteUser(
			@PathVariable("tenantId") Long tenantId, 
			@PathVariable("userId") Long userId, 
			@PathVariable("attributeId") Long attributeId, 
			ModelMap model, HttpSession session) {
		Tenant tenant = this.tenantService.findOne(tenantId);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure",
					"You are not yet authenticated. Please authenticate before performing this operation.");
			logger.log(Level.WARNING, "User not authenticated.");			
			return "index";
		}
		if (!isAuthorized(session, tenant)) {
			MessageManager.getInstance().addMessage(session, "failure",
					"You are not authorized to perform this operation.");
			logger.log(Level.WARNING, "Unauthorized operation caught");
			return "index";
		}
		Attribute attribute = this.attributeService.findOne(attributeId);
		if (attribute == null) {
			MessageManager.getInstance().addMessage(session, "failure",
					"Could not find the attribute with id " + attributeId.toString() + ".");
		} else {
			this.attributeService.deleteAttribute(attribute);
		}
		return "redirect:/users/" + tenantId + "/info/" + userId;
	}
	
	
	private Boolean isAuthorized(HttpSession session, Tenant tenant) {
		// TODO Authorization checks for tenant
		return true;
	}
}
