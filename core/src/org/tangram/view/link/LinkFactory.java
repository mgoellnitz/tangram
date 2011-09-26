package org.tangram.view.link;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface LinkFactory {

    String getPrefix(HttpServletRequest request);


    void registerHandler(LinkHandler handler);


    void unregisterHandler(LinkHandler handler);


    Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);

} // LinkBuilder
