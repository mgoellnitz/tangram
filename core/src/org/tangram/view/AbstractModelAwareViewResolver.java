/**
 * 
 * Copyright 2011 Martin Goellnitz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.tangram.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.exception.ParseErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.View;
import org.tangram.Constants;
import org.tangram.monitor.Statistics;

public abstract class AbstractModelAwareViewResolver implements ModelAwareViewResolver {

    private final static View NOT_FOUND_DUMMY = new View() {

        @Override
        public String getContentType() {
            return null;
        }


        @Override
        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        }

    };

    private int order = Integer.MAX_VALUE;

    private boolean activateCaching = false;

    private Map<String, View> cache = new HashMap<String, View>();

    @Autowired
    private Statistics statistics;

    private static Log log = LogFactory.getLog(AbstractModelAwareViewResolver.class);


    @Override
    public int getOrder() {
        return order;
    }


    public void setOrder(int order) {
        this.order = order;
    }


    public boolean isActivateCaching() {
        return activateCaching;
    }


    public void setActivateCaching(boolean activateCaching) {
        this.activateCaching = activateCaching;
    }


    public Map<String, View> getCache() {
        return cache;
    }


    protected abstract String getFullViewName(String view, String packageName, String simpleName);


    protected abstract View resolveView(String path, Locale locale) throws Exception;


    protected abstract View checkResourceExists(View result);


    protected View checkView(String view, String packageName, String simpleName, String key, Locale locale) throws IOException {
        View result = null;

        String path = getFullViewName(view, packageName, simpleName);
        if (log.isInfoEnabled()) {
            log.info("checkView("+getClass().getSimpleName()+") view="+view+"  path="+path);
        } // if
        try {
            result = resolveView(path, locale);
            if (log.isDebugEnabled()) {
                log.debug("checkView() resolved view "+result);
            } // if
            if (result!=null) {
                result = checkResourceExists(result);
            } // if
            if (log.isInfoEnabled()) {
                log.info("checkView() result="+result);
            } // if
        } catch (Exception e) {
            if (e.getCause() instanceof ParseErrorException) {
                throw (RuntimeException)(e.getCause());
            } else {
                log.warn("checkView()", e);
            } // if
        } // try/catch
        return result;
    } // checkView()


    protected View lookupView(String viewName, Locale locale, Object content, String key) throws IOException {
        Class<? extends Object> cls = content.getClass();
        View view = null;
        Set<String> alreadyChecked = new HashSet<String>();
        while ((view==null)&&(cls!=null)) {
            String pack = (cls.getPackage()==null) ? "" : cls.getPackage().getName();
            view = checkView(viewName, pack, cls.getSimpleName(), key, locale);
            if (view==null) {
                for (Class<? extends Object> c : cls.getInterfaces()) {
                    if (log.isDebugEnabled()) {
                        log.debug("lookupView() type to check templates against "+c.getName());
                    } // if
                    if ( !(alreadyChecked.contains(c.getName()))) {
                        alreadyChecked.add(c.getName());
                        String interfacePackage = (c.getPackage()==null) ? "" : c.getPackage().getName();
                        view = checkView(viewName, interfacePackage, c.getSimpleName(), key, locale);
                        if (view!=null) {
                            break;
                        } // if
                    } // if
                } // for
            } // if
            cls = cls.getSuperclass();
        } // while
        return view;
    } // lookupView()


    @Override
    public View resolveView(String viewName, Map<String, Object> model, Locale locale) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("resolveViewName("+getClass().getSimpleName()+") "+viewName);
        } // if
        Object content = model.get(Constants.THIS);
        if (content==null) {
            return null;
        } // if
        Class<? extends Object> cls = content.getClass();
        String key = cls.getName()+"#"+viewName;
        if (activateCaching&&cache.containsKey(key)) {
            statistics.increase("template lookup cached "+getClass().getSimpleName());
            View result = cache.get(key);
            if (result==NOT_FOUND_DUMMY) {
                result = null;
            } // if
            return result;
        } // if
        statistics.increase("template lookup uncached "+getClass().getSimpleName());
        View view = lookupView(viewName, locale, content, key);
        View cacheView = view;
        if (view==null) {
            if (log.isInfoEnabled()) {
                log.info("resolveViewName() no template found for "+content.getClass().getSimpleName());
            } // if
            cacheView = NOT_FOUND_DUMMY;
        } // if
        if (activateCaching) {
            cache.put(key, cacheView);
        } // if
        return view;
    } // resolveView()

} // AbstractModelAwareViewResolver
