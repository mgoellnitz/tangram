package org.tangram.view.link;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface LinkHandler {
    
    Link createLink(HttpServletRequest request, HttpServletResponse response, Object bean, String action, String view);

} // LinkHandler
