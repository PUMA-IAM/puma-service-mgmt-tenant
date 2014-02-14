package puma.sp.mgmt.tenant;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import puma.sp.mgmt.model.organization.Tenant;
import puma.sp.mgmt.repositories.organization.TenantService;
import puma.sp.mgmt.tenant.msgs.MessageManager;


@Controller
public class MainController {
	private static final Logger logger = Logger.getLogger(MainController.class
			.getName());
	public static final String AUTHENTICATION_URL = "http://sis3s-puma:8080/authn/ServiceAccessServlet";
	private static final Integer MAX_SESSION_DURATION = 2; // Custom session duration
	private static final String LOGOUT_URL = "http://sis3s-puma:8080/authn/LogoutServlet";
	
	@Autowired
	private TenantService tenantService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(		 
			ModelMap model, HttpSession session, HttpServletRequest request) {
		model.addAttribute("msgs",
    			MessageManager.getInstance().getMessages(session)); 
		Tenant tenant = (Tenant) session.getAttribute("tenant");
		if (tenant != null) {
			model.addAttribute("tenantId", tenant.getId());
		} else {
			model.addAttribute("tenantId", null);
		}
		if (!isAuthenticated(session)) {
			return "loginPage";
		}
		if (tenant != null)
			return "redirect:/" + tenant.getId().toString();
		else {
			model.addAttribute("tenantId", null);			
			return "loginPage";
		}
	}
    
    @RequestMapping(value = "/{tenantId}", method = RequestMethod.GET)
    public String addressedIndex(@PathVariable("tenantId") Long tenantId, ModelMap model, HttpSession session) {
    	if (!isAuthenticated(session)) {
    		model.addAttribute("tenantId", tenantId);
			logger.log(Level.WARNING, "Access to tenant configuration page without authentication");
			return "loginPage";
    	}
    	// User is authenticated
    	Tenant tenant = this.tenantService.findOne(tenantId);
		if (tenant == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Could not open page. Could not find the tenant you are associated with.");
			logger.log(Level.WARNING, "Access to tenant configuration page with illegal tenant id");
			model.addAttribute("tenantId", null);
			return "loginPage";
    	}
		if (session.getAttribute("tenant") == null) {
			MessageManager.getInstance().addMessage(session, "failure", "Error in the authentication process. Please retry the authentication process");
			logger.log(Level.WARNING, "For some reason, the tenant id was not set for a session. Perhaps this session has expired?");
			model.addAttribute("tenantId", tenantId);			
			return "loginPage";
		}
		if (!tenant.equals(session.getAttribute("tenant"))) {
			Tenant currentTenant = (Tenant) session.getAttribute("tenant");
			logger.log(Level.WARNING, "Unauthorized access: user of tenant " + currentTenant.getName() + "(" + currentTenant.getId() + ") attempted to access the configuration management page for tenant " + tenant.getName() + "(" + tenant.getId() + ")");
			MessageManager.getInstance().addMessage(session, "failure", "Could not open page. You are not authorized to view or change the configuration of another tenant.");
			return "redirect:/" + currentTenant.getId(); 
		}			
		model.addAttribute("tenant", tenant);
		model.addAttribute("msgs", MessageManager.getInstance().getMessages(session));
        return "index";
    }
    
    @RequestMapping(value = "/login/{tenantId}", method = RequestMethod.GET)
    public String logInRedirect(@PathVariable(value = "tenantId") String tenantId, 
    		ModelMap model, HttpSession session, HttpServletRequest request, UriComponentsBuilder builder) {
    	Tenant tenant = null;
    	if (tenantId.isEmpty() || !canParseId(tenantId))
    		tenant = ((Tenant) session.getAttribute("tenant"));
    	else
    		tenant = this.tenantService.findOne(Long.parseLong(tenantId));
    	// Build relay state
    	String relayState = builder.path("/login-callback").build().toString();
    	try {
			relayState = URLEncoder.encode(relayState, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	// If tenant is not available, change call accordingly
		if (tenant == null) {
			logger.log(Level.INFO, "Redirecting request for unknown tenant to " + relayState);
			return "redirect:" + AUTHENTICATION_URL + "?RelayState=" + relayState;
		}
		// Otherwise redirect to correct login page
		logger.log(Level.INFO, "Redirecting request for tenant " + tenantId + " to " + relayState);
		return "redirect:" + AUTHENTICATION_URL + "?RelayState=" + relayState + "&Tenant=" + tenantId.toString();
    }

	@RequestMapping(value = "/login-callback", method = RequestMethod.GET)
    public String loginCallback(@RequestParam(value = "Tenant", defaultValue = "") String tenantId, 
    		ModelMap model, HttpSession session, HttpServletRequest request) {
    	// Here, we assume that the callback also contains some signature of the authentication service to prove that the authentication was indeed acknowledged by that party
    	// Note that the signature must also verify that the user was authenticated for the particular tenant id.
    	session.setAttribute("authenticated", new DateTime());
    	// Set the tenant, if existing (should exist)
    	Tenant tenant = null;
    	if (tenantId == null || tenantId.isEmpty())
    		tenant = null;
    	else
    		tenant = this.tenantService.findOne(Long.parseLong(tenantId));
    	if (tenant == null) {
    		logger.log(Level.WARNING, "Could not do callback for tenant id " + tenantId + ": non-existing tenant. Is the authentication service trusted?");
    		MessageManager.getInstance().addMessage(session, "failure", "Authentication service appears to have returned corrupt data. Please try to log in again.");
    		model.addAttribute("tenantId", null);
    		return "loginPage";
    	}    	
    	session.setAttribute("tenant", tenant);
    	if (session.getAttribute("returnAddress") != null) {
    		String returnAddress = (String) session.getAttribute("returnAddress");
    		session.removeAttribute("returnAddress");
    		return "redirect:/" + returnAddress;
    	} else {
    		return "redirect:/" + tenantId;
    	}
    }

    private boolean canParseId(String tenantId) {
		try {
			Long.parseLong(tenantId);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
    
	public static Boolean isAuthenticated(HttpSession session) {
		DateTime loginTime = (DateTime) session.getAttribute("authenticated");
		// There never was any authentication flow which succeeded
		if (loginTime == null)
			return false;
		// The authentication exceeded the custom maximum session duration
		if (!loginTime.plusHours(MAX_SESSION_DURATION).isAfterNow()) {
			session.removeAttribute("authenticated");
			return false;
		}
		// No tenant was specified for the session.
		if (session.getAttribute("tenant") == null)
			return false;
    	return true;
    }
	
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(ModelMap model, HttpSession session, HttpServletRequest request, UriComponentsBuilder builder) {
		session.invalidate();
		String relayState = builder.path("/").build().toString();
    	try {
			relayState = URLEncoder.encode(relayState, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "redirect:" + LOGOUT_URL + "?RelayState=" + relayState;
	}
}
