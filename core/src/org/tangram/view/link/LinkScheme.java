package org.tangram.view.link;

import javax.servlet.http.HttpServletResponse;

import org.tangram.content.BeanFactory;
import org.tangram.controller.DefaultController;
import org.tangram.view.TargetDescriptor;

public interface LinkScheme extends LinkHandler {
    
    /**
     * underlying implementations might want to use this
     * @param beanFactory
     */
    public void setBeanFactory(BeanFactory beanFactory);
    
    /**
     * underlying implementations might want to use this
     * to deactivate views and URLs handled here in the default controller
     * @param beanFactory
     */
    public void setDefaultController(DefaultController defaultController);
    
    /**
     * return the id of the object to be show, null otherwise
     * 
     * @param url
     * @param response for error handling
     * @return
     */
    public TargetDescriptor parseLink(String url, HttpServletResponse response);

} // LinkScheme
