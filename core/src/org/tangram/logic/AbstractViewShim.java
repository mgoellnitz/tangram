package org.tangram.logic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.tangram.content.Content;

/**
 * 
 * Abstract base class for request shim implementations
 * 
 */
public class AbstractViewShim<T extends Content> extends AbstractShim<T> implements ViewShim {

    protected HttpSession session;

    protected HttpServletRequest request;


    public AbstractViewShim(HttpServletRequest request, T delegate) {
        super(delegate);
        this.request = request;
        this.session = request.getSession(false);
    } // AbstractViewShim()


    @Override
	public HttpSession getSession() {
        return session;
    }


    public void setSession(HttpSession session) {
        this.session = session;
    }


    @Override
	public HttpServletRequest getRequest() {
        return request;
    }


    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

} // AbstractViewShim
