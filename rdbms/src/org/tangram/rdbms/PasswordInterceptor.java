package org.tangram.rdbms;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.tangram.Constants;

/**
 * 
 * Interceptor to check if a user is logged in, if we are a live system, or if we should use generic password protection
 * with users preconfigured in an XML config file
 * 
 * liveSuffix should be the name suffix of your live installation as opposed to the development/testing appengine apps
 * 
 * statsUrl is a URL which should be available without log-in /s/statsa typically if you use statistics page for keep
 * alive cron job
 * 
 * allowedUsers if not empty only these users are allowed to log-in.
 * 
 * Can be emails of google accounts or IDs of OpenID accounts
 * 
 * adminUsers same as allowedUsers (should be a subset of it) but these users get the access to the editing links
 * 
 */
public class PasswordInterceptor extends HandlerInterceptorAdapter {

    private static final Log log = LogFactory.getLog(PasswordInterceptor.class);

    private Set<String> freeUrls;

    private Set<String> allowedUsers = new HashSet<String>();

    private Set<String> adminUsers = new HashSet<String>();


    public Set<String> getFreeUrls() {
        return freeUrls;
    }


    public void setFreeUrls(Set<String> freeUrls) {
        this.freeUrls = freeUrls;
    }


    public Set<String> getAllowedUsers() {
        return allowedUsers;
    }


    public void setAllowedUsers(Set<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }


    public Set<String> getAdminUsers() {
        return adminUsers;
    }


    public void setAdminUsers(Set<String> adminUsers) {
        this.adminUsers = adminUsers;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String thisURL = request.getRequestURI();
        request.setAttribute("tangramURL", thisURL);
        if (log.isDebugEnabled()) {
            log.debug("preHandle() detected URI "+thisURL);
        } // if

        if ( !getFreeUrls().contains(thisURL)) {
            Principal principal = request.getUserPrincipal();

            if (principal!=null) {
                String userName = principal.getName();
                if (log.isInfoEnabled()) {
                    log.info("preHandle() checking for user: "+userName);
                } // if
                // if ( !thisURL.startsWith("/WEB-INF")&& !thisURL.startsWith("_ah")) {
                // request.setAttribute(Constants.ATTRIBUTE_LOGOUT_URL, userService.createLogoutURL(thisURL));
                // } // if
                if (adminUsers.contains(userName)) {
                    request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                } // if
                if ((allowedUsers.size()>0)&&( !allowedUsers.contains(userName))) {
                    if (log.isWarnEnabled()) {
                        log.warn("preHandle() user not allowed to access page: "+userName);
                    } // if
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, userName+" not allowed to view page");
                } // if
            } else {
                // String loginURL = userService.createLoginURL(thisURL);
                String loginURL = "";
                principal = new Principal() {
                    public String getName() {
                        return "martin@goellnitz.de";
                    } // getName()
                };
                request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, "true");
                request.setAttribute(Constants.ATTRIBUTE_USER, principal);
                if (allowedUsers.size()>0) {
                    if (log.isInfoEnabled()) {
                        log.info("preHandle() no logged in user found");
                    } // if
                    response.sendRedirect(loginURL);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("preHandle() system doesn't need login but perhaps application");
                    } // if
                    request.setAttribute(Constants.ATTRIBUTE_LOGIN_URL, loginURL);
                } // if
            } // if
        } // if

        return super.preHandle(request, response, handler);
    } // preHandle()

} // PasswordInterceptor
