package puma.sp.mgmt.tenant;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import puma.sp.mgmt.model.organization.Tenant;
import puma.sp.mgmt.repositories.organization.TenantService;
import puma.sp.mgmt.tenant.msgs.MessageManager;


@Controller
public class MainController {
	private static final Logger logger = Logger.getLogger(MainController.class
			.getName());
	private static final String AUTHENTICATION_URL = "http://dnetcloud-tomcat:8080/authn/ServiceAccessServlet";
	
	@Autowired
		private TenantService tenantService;
	
	/*
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(ModelMap model, HttpSession session) {
    	model.addAttribute("msgs",
    			MessageManager.getInstance().getMessages(session)); 
    	if (isAuthenticated(session)) {
    		// TODO page addressing etc.
    		if (session.getAttribute("Tenant") != null) {
    			model.addAttribute("tenant", )
    		} else {
    			MessageManager.getInstance().addMessage(session, "failure", "Could not open page. Could not find the tenant you are associated with!");
    		}
    	} else {
    		logger.log(Level.INFO, "Unauthenticated user attempting tenant configuration access. Redirecting to authentication process");
    		return "redirect:" + AUTHENTICATION_URL;
    	}
    }*/
    
    @RequestMapping(value = "/{tenantId}", method = RequestMethod.GET)
    public String addressedIndex(@PathVariable("tenantId") Long tenantId, ModelMap model, HttpSession session) {
    	model.addAttribute("msgs",
    			MessageManager.getInstance().getMessages(session)); 
    	if (isAuthenticated(session)) {
    		Tenant tenant = this.tenantService.findOne(tenantId);
    		if (tenant == null) {
    			MessageManager.getInstance().addMessage(session, "failure", "Could not open page. Could not find the tenant you are associated with.");
    		} else {
    			session.setAttribute("tenant", tenant);
    			model.addAttribute("tenant", tenant);
    		}
    	} else {
    		return "redirect:" + AUTHENTICATION_URL;
    	}
        return "index";
    }
    
    private static Boolean isAuthenticated(HttpSession session) {
    	// return ((Boolean) session.getAttribute("Authenticated")).booleanValue()
    	return true;
    }
}
