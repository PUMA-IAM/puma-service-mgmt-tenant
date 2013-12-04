package puma.sp.mgmt.tenant.users;

import java.util.ArrayList;
import java.util.List;
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
import puma.sp.mgmt.model.attribute.DataType;
import puma.sp.mgmt.model.attribute.Multiplicity;
import puma.sp.mgmt.model.organization.Organization;
import puma.sp.mgmt.model.organization.Tenant;
import puma.sp.mgmt.model.user.User;
import puma.sp.mgmt.repositories.attribute.AttributeFamilyService;
import puma.sp.mgmt.repositories.attribute.AttributeService;
import puma.sp.mgmt.repositories.organization.OrganizationService;
import puma.sp.mgmt.repositories.organization.TenantService;
import puma.sp.mgmt.repositories.user.UserService;
import puma.sp.mgmt.tenant.MainController;
import puma.sp.mgmt.tenant.msgs.MessageManager;

@Controller
public class AttributeController {
	private static final Logger logger = Logger.getLogger(AttributeController.class
			.getName());

	private static final String PROVIDER_NAME = "provider";
	
	@Autowired
	private TenantService tenantService;
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private AttributeService attributeService;
	@Autowired
	private AttributeFamilyService attributeFamilyService;
	@Autowired
	private UserService userService;
	
	@RequestMapping(value = "/attributes/{tenantId}/{userId}/create-impl", method = RequestMethod.POST)
	public String assignAttribute(
			@PathVariable("tenantId") Long tenantId, 
			@PathVariable("userId") Long userId, 
			@RequestParam("familyId") Long familyId,
			@RequestParam("value") String value,
			ModelMap model, HttpSession session, HttpServletRequest request
			) {
		if (!doCheck(tenantId, session))
			return "redirect:/" + MainController.AUTHENTICATION_URL + "?RelayState=" + request.getRequestURL().toString();
		AttributeFamily family = this.attributeFamilyService.findOne(familyId);
		if (family == null) {
			MessageManager.getInstance().addMessage(session, "failure",
					"The family you specified does not exist.");			
			return "redirect:/users/" + tenantId + "/info/" + userId;
		}
		User user = this.userService.byId(userId);
		if (user == null) {
			MessageManager.getInstance().addMessage(session, "failure",
					"The user you specified does not exist.");			
			return "redirect:/users/" + tenantId.toString();
		}
		Attribute attribute = new Attribute();
		attribute.setFamily(family);
		attribute.setValue(value);
		attribute.setUser(user);
		this.attributeService.addAttribute(attribute);
		MessageManager.getInstance().addMessage(session, "success", "Attribute was successfully generated.");
		return "redirect:/users/" + tenantId + "/info/" + userId;
	}
	
	
	@RequestMapping(value = "/attributes/{tenantId}/{userId}/{attributeId}/delete")
	public String deleteAttribute(
			@PathVariable("tenantId") Long tenantId, 
			@PathVariable("userId") Long userId, 
			@PathVariable("attributeId") Long attributeId, 
			ModelMap model, HttpSession session, HttpServletRequest request) {
		if (!doCheck(tenantId, session))
			return "redirect:/" + MainController.AUTHENTICATION_URL + "?RelayState=" + request.getRequestURL().toString();
		Attribute attribute = this.attributeService.findOne(attributeId);
		if (attribute == null) {
			MessageManager.getInstance().addMessage(session, "failure",
					"Could not find the attribute with id " + attributeId.toString() + ".");
		} else {
			this.attributeService.deleteAttribute(attribute);
			MessageManager.getInstance().addMessage(session, "success", "Attribute was successfully removed.");
		}
		return "redirect:/users/" + tenantId + "/info/" + userId;
	}

	@RequestMapping(value = "/attributes/{tenantId}", method = RequestMethod.GET)
	public String showFamilies(
			@PathVariable("tenantId") Long tenantId,
			ModelMap model, HttpSession session, HttpServletRequest request		
			) {
		if (!doCheck(tenantId, session))
			return "redirect:/" + MainController.AUTHENTICATION_URL + "?RelayState=" + request.getRequestURL().toString();
		Organization organization = this.organizationService.findOne(tenantId);
		model.addAttribute("families", this.attributeFamilyService.findAllOrganizationProvider(organization));
		List<String> dataTypes = new ArrayList<String>(DataType.values().length);
		for (DataType next: DataType.values())
			dataTypes.add(next.toString());
		model.addAttribute("datatypes", dataTypes);
		List<String> multiplicityValues = new ArrayList<String>(Multiplicity.values().length);
		for (Multiplicity next: Multiplicity.values()) 
			multiplicityValues.add(next.toString());
		model.addAttribute("multiplicityValues", multiplicityValues);
		return "attributes/show";
	}
	
	@RequestMapping(value = "/attributes/{tenantId}/{familyId}/delete")
	public String deleteFamily(
			@PathVariable("tenantId") Long tenantId, 
			@PathVariable("familyId") Long familyId, 
			ModelMap model, HttpSession session, HttpServletRequest request) {
		if (!doCheck(tenantId, session))
			return "redirect:/" + MainController.AUTHENTICATION_URL + "?RelayState=" + request.getRequestURL().toString();
		AttributeFamily family = this.attributeFamilyService.findOne(familyId);
		if (family == null) {
			MessageManager.getInstance().addMessage(session, "failure",
					"Could not find the attribute family with id " + familyId.toString() + ".");
		} else {
			Organization tenant = this.tenantService.findOne(tenantId);
			// Organizations can only remove their own families. The provider can remove all families.
			if (tenant.equals(family.getDefinedBy()) || tenant.getName().equals(PROVIDER_NAME)) {
				this.attributeFamilyService.delete(family);
				MessageManager.getInstance().addMessage(session, "success", "Attribute family '" + family.getName() + "' was successfully removed.");								
			} else {
				MessageManager.getInstance().addMessage(session, "failure", "Could not remove attribute family '" + family.getName() + "': you should be from the same organization!");
			}
		}
		return "redirect:/attributes/" + tenantId.toString();
	}

	@RequestMapping(value = "/attributes/{tenantId}/create-impl", method = RequestMethod.POST)
	public String addFamily(
			@PathVariable("tenantId") Long tenantId, 
			@RequestParam("name") String name,
			@RequestParam("xacmlid") String xacmlIdentifier,
			@RequestParam("multiplicity") String multiplicity,
			@RequestParam("datatype") String datatype,
			ModelMap model, HttpSession session, HttpServletRequest request	
			) {
		if (!doCheck(tenantId, session))
			return "redirect:/" + MainController.AUTHENTICATION_URL + "?RelayState=" + request.getRequestURL().toString();
		Organization tenant = this.tenantService.findOne(tenantId);
		AttributeFamily family = new AttributeFamily();
		family.setDataType(DataType.valueOf(datatype));
		family.setDefinedBy(tenant);
		family.setMultiplicity(Multiplicity.valueOf(multiplicity));
		family.setName(name);
		family.setXacmlIdentifier(xacmlIdentifier);
		this.attributeFamilyService.add(family);
		return "redirect:/attributes/" + tenantId.toString();		
	}

	private Boolean doCheck(Long tenantId, HttpSession session) {
		Tenant tenant = this.tenantService.findOne(tenantId);
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
		// TODO Authorization checks for tenant
		return true;
	}
}
