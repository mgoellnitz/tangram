/**
 *
 * Copyright 2011-2013 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.view.velocity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import javax.inject.Inject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.velocity.VelocityView;
import org.tangram.Constants;
import org.tangram.components.spring.CodeResourceCache;
import org.tangram.content.BeanListener;
import org.tangram.content.CodeResource;
import org.tangram.view.AbstractModelAwareViewResolver;

public class ModelAwareRepositoryViewResolver extends AbstractModelAwareViewResolver implements BeanListener,
        InitializingBean {

    @Inject
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
    protected View resolveView(String path, Locale locale) throws Exception {
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
