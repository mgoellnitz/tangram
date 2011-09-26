package org.tangram.logic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 
 * This type of shim represents a runtime aspect of content used for the view layer. Hence it has session and request
 * available and will not be cacheable
 * 
 */
public interface ViewShim extends Shim {

    HttpSession getSession();


    HttpServletRequest getRequest();

} // ViewShim
