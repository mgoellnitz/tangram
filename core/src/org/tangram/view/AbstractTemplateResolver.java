/**
 *
 * Copyright 2013 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.Constants;
import org.tangram.monitor.Statistics;


public abstract class AbstractTemplateResolver<T extends Object> implements TemplateResolver<T> {

    private static Log log = LogFactory.getLog(AbstractTemplateResolver.class);

    /**
     * replace bracktes [] in array type names with _array in class names for resolution.
     */
    private boolean suppressBrackets = true;

    /**
     * String to use as a separator between package name and class name for resolution.
     */
    private String packageSeparator = "/";

    private String name = getClass().getSimpleName();

    private boolean activateCaching = false;

    private Map<String, T> cache = new HashMap<String, T>();

    @Inject
    private Statistics statistics;


    protected AbstractTemplateResolver(boolean suppressBrackets, String packageSeparator) {
        this.suppressBrackets = suppressBrackets;
        this.packageSeparator = packageSeparator;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public boolean isActivateCaching() {
        return activateCaching;
    }


    public void setActivateCaching(boolean activateCaching) {
        this.activateCaching = activateCaching;
    }


    protected Map<String, T> getCache() {
        return cache;
    }


    protected abstract T getNotFoundDummy();


    protected String getFullViewName(String view, String packageName, String simpleName) {
        if ((suppressBrackets)&(simpleName.endsWith("[]"))) {
            simpleName = simpleName.replace("[]", "_array");
        } // if
        String viewPrefix = StringUtils.isNotBlank(packageName) ? packageName+packageSeparator : "";
        return viewPrefix+simpleName+(Constants.DEFAULT_VIEW.equals(view) ? "" : "."+view);
    } // getFullViewName()



    protected abstract T resolveView(String path, Locale locale) throws Exception;


    protected abstract T checkResourceExists(T result);


    protected T checkView(String view, String packageName, String simpleName, String key, Locale locale) {
        T result = null;

        String path = getFullViewName(view, packageName, simpleName);
        if (log.isInfoEnabled()) {
            log.info("checkView("+getName()+") view="+view+"  path="+path);
        } // if
        try {
            result = resolveView(path, locale);
            if (log.isDebugEnabled()) {
                log.debug("checkView() resolved view "+result);
            } // if
            if (result!=null) {
                result = checkResourceExists(result);
            } // if
            if (log.isDebugEnabled()) {
                log.debug("checkView() result="+result);
            } // if
        } catch (Exception e) {
            if ((e.getCause()!=null)&&(e.getCause().getClass().getName().indexOf("Parse")>=0)) {
                throw (RuntimeException) (e.getCause());
            } else {
                log.warn("checkView()", e);
            } // if
        } // try/catch
        return result;
    } // checkView()


    /**
     *
     * @throws IOException - in subclasses not this one
     */
    protected T lookupView(String viewName, Locale locale, Object content, String key) throws IOException {
        Class<? extends Object> cls = content.getClass();
        T view = null;
        Set<String> alreadyChecked = new HashSet<String>();
        while ((view==null)&&(cls!=null)) {
            String pack = (cls.getPackage()==null) ? "" : cls.getPackage().getName();
            view = checkView(viewName, pack, cls.getSimpleName(), key, locale);
            if (view==null) {
                for (Object i : ClassUtils.getAllInterfaces(cls)) {
                    Class<? extends Object> c = (Class<? extends Object>) i;
                    if (log.isDebugEnabled()) {
                        log.debug("lookupView() type to check templates for "+c.getName());
                    } // if
                    if (!(alreadyChecked.contains(c.getName()))) {
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
    public T resolveTemplate(String viewName, Map<String, Object> model, Locale locale) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("resolveViewName("+getName()+") "+viewName);
        } // if
        Object content = model.get(Constants.THIS);
        if (content==null) {
            return null;
        } // if
        Class<? extends Object> cls = content.getClass();
        String key = cls.getName()+"#"+viewName;
        if (activateCaching&&cache.containsKey(key)) {
            statistics.increase("template lookup cached "+getName());
            T result = cache.get(key);
            if (result==getNotFoundDummy()) {
                result = null;
            } // if
            return result;
        } // if
        statistics.increase("template lookup uncached "+getName());
        T view = lookupView(viewName, locale, content, key);
        T cacheView = view;
        if (view==null) {
            if (log.isInfoEnabled()) {
                log.info("resolveViewName("+getName()+") no template found for "+content.getClass().getSimpleName());
            } // if
            cacheView = getNotFoundDummy();
        } // if
        if (activateCaching) {
            cache.put(key, cacheView);
        } // if
        return view;
    } // resolveView()

} // AbstractTemplateResolver
