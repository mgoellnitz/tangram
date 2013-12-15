package org.tangram.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.Constants;


/**
 *
 * Filter implementation to simulate much of the behaviour of the password interceptor.
 *
 * This should allow us a lean solution for a protected editor login.
 *
 * values of init parameters starting with "allowed" are added to the allowed users list.
 * values of init parameters starting with "admin" are added to the adminusers list.
 * values of init parameters starting with "free.url" are added to the free urls list.
 */
public class ProtectionFilter implements Filter {

    private static final Log log = LogFactory.getLog(ProtectionFilter.class);

    private Set<String> freeUrls = new HashSet<String>();

    private Set<String> allowedUsers = new HashSet<String>();

    private Set<String> adminUsers = new HashSet<String>();



    @Override
    public void destroy() {
    } // destroy()


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String thisURL = request.getRequestURI();
        request.setAttribute("tangramURL", thisURL);
        if (log.isDebugEnabled()) {
            log.debug("preHandle() detected URI "+thisURL);
        } // if

        if (!freeUrls.contains(thisURL)) {
            Principal principal = request.getUserPrincipal();

            if (principal!=null) {
                String userName = principal.getName();
                if (log.isInfoEnabled()) {
                    log.info("preHandle() checking for user: "+userName);
                } // if
                if (adminUsers.contains(userName)) {
                    request.setAttribute(Constants.ATTRIBUTE_ADMIN_USER, Boolean.TRUE);
                } // if
                if ((allowedUsers.size()>0)&&(!allowedUsers.contains(userName))) {
                    if (log.isWarnEnabled()) {
                        log.warn("preHandle() user not allowed to access page: "+userName);
                    } // if
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, userName+" not allowed to view page");
                } // if
            } else {
                // TODO: new generic Login URL stuff
                String loginURL = "spring_security_login";
                if (allowedUsers.size()>0) {
                    if (log.isInfoEnabled()) {
                        log.info("preHandle() no logged in user found");
                    } // if
                    if (log.isDebugEnabled()) {
                        log.debug("preHandle() redirecting to '"+loginURL+"'");
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

        chain.doFilter(request, response);
    } // doFilter()


    @Override
    @SuppressWarnings("rawtypes")
    public void init(FilterConfig config) throws ServletException {
        final Enumeration initParameterNames = config.getInitParameterNames();
        while (initParameterNames.hasMoreElements()) {
            String parameterName = ""+(initParameterNames.nextElement());
            if (parameterName.startsWith("allowed.")) {
                allowedUsers.add(config.getInitParameter(parameterName));
            } // if
            if (parameterName.startsWith("admin.")) {
                adminUsers.add(config.getInitParameter(parameterName));
            } // if
            if (parameterName.startsWith("free.url.")) {
                freeUrls.add(config.getInitParameter(parameterName));
            } // if
        } // for
    } // init()

} // ProtectionFilter
