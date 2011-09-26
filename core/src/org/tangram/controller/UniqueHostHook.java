package org.tangram.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;

public class UniqueHostHook implements ControllerHook {

    private static Log log = LogFactory.getLog(UniqueHostHook.class);

    @Autowired
    private LinkFactory linkFactory;

    private String primaryDomain = null;


    public void setPrimaryDomain(String primaryDomain) {
        this.primaryDomain = primaryDomain;
    }


    @Override
    public boolean intercept(TargetDescriptor descriptor, Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("intercept() serverName="+request.getServerName());
        } // if
        boolean isOnLocalhost = request.getServerName().equals("localhost");
        if ( !(request.getServerName().equals(primaryDomain)||(isOnLocalhost))) {
            Link redirectLink = linkFactory.createLink(request, response, descriptor.bean, descriptor.action,
                    descriptor.view);
            response.setHeader("Location", "http://"+primaryDomain+redirectLink.getUrl());
            response.setStatus(301);
            return true;
        } // if
        return false;
    } // intercept()

} // UniqueUrlHook
