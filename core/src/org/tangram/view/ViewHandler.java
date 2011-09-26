package org.tangram.view;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.web.servlet.View;

/**
 * 
 * Instances of this bean are used to find valid views
 * 
 * Used by many components and thus should not be private element of TangramServlet
 * 
 */
public interface ViewHandler {

    View resolveView(String viewName, Map<String, Object> model, Locale locale, ServletRequest request)
            throws IOException;

} // ViewHandler
