package org.tangram.logic;

import org.tangram.content.Content;

/**
 * 
 * Abstract base class for logic extension classes in the view layer
 * 
 */
public abstract class AbstractShim<T extends Content> implements Shim {

    protected String attributeName;

    protected T delegate;


    public AbstractShim(T delegate) {
        this.attributeName = getClass().getSimpleName();
        this.delegate = delegate;
    } // AbstractViewShim()


    public String getAttributeName() {
        return attributeName;
    } // getAttributeName()


    public String getId() {
        return delegate.getId();
    } // getDelegate()

} // AbstractViewShim
