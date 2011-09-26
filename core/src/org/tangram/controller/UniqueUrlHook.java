package org.tangram.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;

@Component
public class UniqueUrlHook implements ControllerHook {

    private static Log log = LogFactory.getLog(UniqueUrlHook.class);

    @Autowired
    private LinkFactory linkFactory;


    @Override
    public boolean intercept(TargetDescriptor descriptor, Map<String, Object> model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Link link = null;
        try {
            link = linkFactory.createLink(request, response, descriptor.bean, descriptor.action, descriptor.view);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("render() expensive things happen "
                        +((descriptor.bean==null) ? "" : descriptor.bean.getClass().getName()));
            } // if
        } // try/catch
        if (link!=null) {
            // TODO: is it sufficient not to decode both?
            // String decodedUrl = URLDecoder.decode(link.getUrl(), "UTF-8");
            // String requestURI = URLDecoder.decode(request.getRequestURI(), "UTF-8");
            String decodedUrl = link.getUrl();
            String requestURI = request.getRequestURI();
            if ( !decodedUrl.equals(requestURI)) {
                if (log.isInfoEnabled()) {
                    log.info("render() sending redirect for "+requestURI+" to "+decodedUrl);
                } // if
                response.setHeader("Location", link.getUrl());
                response.setStatus(301);
                return true;
            } // if
        } // if
        return false;
    } // intercept()

} // UniqueUrlHook
