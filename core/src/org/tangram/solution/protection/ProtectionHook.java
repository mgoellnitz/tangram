package org.tangram.solution.protection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.tangram.Constants;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.controller.ControllerHook;
import org.tangram.view.TargetDescriptor;

@Component
public class ProtectionHook implements ControllerHook {

    private static final Log log = LogFactory.getLog(ProtectionHook.class);

    @Autowired
    private BeanFactory beanFactory;


    /*** Protections ***/

    @SuppressWarnings("unchecked")
    public Map<String, Protection> getRequiredProtections(ProtectedContent content) {
        // This is the line "unchecked" is good for...
        Map<String, Protection> result = Collections.EMPTY_MAP;

        if (content instanceof Protection) {
            result = new HashMap<String, Protection>();
            Protection prot = (Protection)content;
            result.put(prot.getProtectionKey(), prot);
        } else {
            List<Protection> protections = beanFactory.listBeans(Protection.class);

            if (log.isInfoEnabled()) {
                log.info("getRequiredProtections() total # of protections: "+protections.size());
            } // if

            if (protections.size()>0) {
                result = new HashMap<String, Protection>();
                for (Protection prot : protections) {
                    if (StringUtils.hasText(prot.getProtectionKey())) {
                        if (isProtectedBy(content, prot)) {
                            result.put(prot.getProtectionKey(), prot);
                        } // if
                    } // if
                } // for
            } // if
        } // if

        return result;
    } // getRequiredProtections()


    private String getFailingProtectionKey(HttpServletRequest request, Map<String, Protection> protections)
            throws Exception {
        for (Protection p : protections.values()) {
            if ( !p.isContentVisible(request)) {
                return p.getProtectionKey();
            } // if
        } // for
        return null;
    } // getFailingProtectionKey


    public boolean isProtectedBy(ProtectedContent protectedContent, Protection p) {
        boolean result = false;

        List<? extends Content> path = protectedContent.getProtectionPath();

        for (Content protectedTopic : p.getProtectedContents()) {
            if (path.contains(protectedTopic)) {
                result = true;
            } // if
        } // for

        return result;
    } // isProtectedBy()


    @Override
	public boolean intercept(TargetDescriptor descriptor, Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // for now only topics are protected
        Protection protection = null;
        String loginResult = null;
        if (descriptor.bean instanceof ProtectedContent) {
            ProtectedContent protectedContent = (ProtectedContent)(descriptor.bean);
            Map<String, Protection> protections = getRequiredProtections(protectedContent);

            if (log.isInfoEnabled()) {
                log.info("render() # of relevant protections: "+protections.size());
            } // if

            String protectionKey = request.getParameter(Constants.PARAMETER_PROTECTION_KEY);
            if ((protectionKey!=null)&&("POST".equals(request.getMethod()))) {
                // handle login
                if (log.isInfoEnabled()) {
                    log.info("render() handling login "+protectionKey+" for topic "+protectedContent.getId());
                } // if
                loginResult = protections.get(protectionKey).handleLogin(request, response);
            } // if

            String necessaryProtectionKey = getFailingProtectionKey(request, protections);
            if (necessaryProtectionKey!=null) {
                if (log.isInfoEnabled()) {
                    log.info("render() topic "+protectedContent.getId()+" is not visible");
                } // if

                for (Protection p : protections.values()) {
                    if (p.needsAuthorization(request)) {
                        if (log.isInfoEnabled()) {
                            log.info("render() Protection specific authorization necessary: "+p.getProtectionKey());
                        } // if
                        protection = p;
                    } // if
                } // for

                if (protection==null) {
                    // TODO: Show nicer disallowance page.
                    necessaryProtectionKey = getFailingProtectionKey(request, protections);
                    if (necessaryProtectionKey!=null) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, necessaryProtectionKey);
                        return true;
                    } // if
                } // if
            } // if
        } // if

        // save the results of our work for others - say templates - to use it
        // Setting login protection object
        model.put(Constants.ATTRIBUTE_PROTECTION, protection);
        // Setting login result
        model.put(Constants.ATTRIBUTE_LOGIN_RESULT, loginResult);

        return false;
    } // intercept()

} // ProtectionHook
