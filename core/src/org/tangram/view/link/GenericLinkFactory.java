package org.tangram.view.link;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tangram.monitor.Statistics;

public class GenericLinkFactory implements LinkFactory {

    private static final Log log = LogFactory.getLog(GenericLinkFactory.class);

    @Autowired
    private Statistics statistics;

    private String dispatcherPath = "";

    private List<LinkHandler> handlers = new ArrayList<LinkHandler>();


    public String getDispatcherPath() {
        return dispatcherPath;
    }


    public void setDispatcherPath(String dispatcherPath) {
        this.dispatcherPath = dispatcherPath;
    }


    @Override
	public void registerHandler(LinkHandler handler) {
        handlers.add(handler);
    } // registerHandler()


    @Override
	public void unregisterHandler(LinkHandler handler) {
        handlers.remove(handler);
    } // unregisterHandler()

    String prefix = null;


    @Override
	public String getPrefix(HttpServletRequest request) {
        if (prefix==null) {
            String contextPath = request.getContextPath();
            if (contextPath.length()==1) {
                contextPath = "";
            } // if
            prefix = contextPath+dispatcherPath;
        } // if
        return prefix;
    } // getPrefix()


    public void postProcessResult(Link result, HttpServletRequest request) {
        String urlString = result.getUrl();
        StringBuffer url = new StringBuffer(urlString);
        int idx = urlString.indexOf('/');
        if (log.isDebugEnabled()) {
            log.debug("postProcessResult() "+idx+" ("+url+")");
        } // if
        if (idx>=0) {
            url.insert(idx, getPrefix(request));
        } // if
        if (log.isDebugEnabled()) {
            log.debug("postProcessResult() "+idx+" ("+url+")");
        } // if
        result.setUrl(url.toString());
    } // postProcessResult()


    @Override
	public Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action,
            String view) {
        if (bean==null) {
            throw new RuntimeException("No bean issued for link generation in action "+action+" for view "+view);
        } // if
        for (LinkHandler handler : handlers) {
            long startTime = System.currentTimeMillis();
            Link result = handler.createLink(request, response, bean, action, view);
            if (log.isDebugEnabled()) {
                log.debug("createLink() "+handler.getClass().getName()+" -> "+result+" ["
                        +bean.getClass().getSimpleName()+"]");
            } // if
            if (result!=null) {
                postProcessResult(result, request);
                statistics.avg("generate url time", System.currentTimeMillis()-startTime);
                return result;
            } // if
        } // for
        throw new RuntimeException("Cannot create link for "+bean+" in action "+action+" for view "+view);
    } // createLink()

} // GenericLinkBuilder
