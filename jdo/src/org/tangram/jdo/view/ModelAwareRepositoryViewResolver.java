package org.tangram.jdo.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.velocity.VelocityView;
import org.tangram.Constants;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;
import org.tangram.view.AbstractModelAwareViewResolver;

public class ModelAwareRepositoryViewResolver extends AbstractModelAwareViewResolver implements BeanListener,
        InitializingBean {

    @Autowired
    private CodeResourceCache codeResourceCache;

    private Collection<String> supportedContenTypes;

    private ViewResolver delegate;


    public ModelAwareRepositoryViewResolver() {
        supportedContenTypes = new HashSet<String>();
        supportedContenTypes.add("text/html");
        supportedContenTypes.add("text/xml");
    } // ModelAwareRepositoryViewResolver()


    public ViewResolver getDelegate() {
        return delegate;
    }


    public void setDelegate(ViewResolver delegate) {
        this.delegate = delegate;
    }


    @Override
    protected View checkResourceExists(View view) {
        return view;
    }


    @Override
    protected View resolveViewName(String path, Locale locale) throws Exception {
        View result = null;
        CodeResource template = codeResourceCache.get("text/html", path);
        if (template==null) {
            template = codeResourceCache.get("text/xml", path);
        } // if
        if (template!=null) {
            String mimeType = template.getMimeType();
            if (supportedContenTypes.contains(mimeType)) {
                String viewId = template.getId();
                result = delegate.resolveViewName(viewId, locale);
                // FIXME: Move this to separate VelocityViewResolver implementation
                if (result!=null) {
                    if (result instanceof VelocityView) {
                        VelocityView v = (VelocityView)result;
                        v.setContentType(mimeType+";charset=UTF-8");
                        v.setEncoding("UTF-8");
                    } // if
                } // if
            } // if
        } // if
        return result;
    } // resolveViewName()


    @Override
    protected String getFullViewName(String view, String packageName, String simpleName) {
        return packageName+"."+simpleName+(Constants.DEFAULT_VIEW.equals(view) ? "" : "."+view);
    } // getFullViewName()


	@Override
    public void reset() {
        getCache().clear();
    } // reset()


    @Override
    public void afterPropertiesSet() throws Exception {
        codeResourceCache.addListener(this);
    } // afterPropertiesSet()

} // ModelAwareRepositoryViewResolver
