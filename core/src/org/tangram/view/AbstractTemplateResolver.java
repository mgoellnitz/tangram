/**
 *
 * Copyright 2013-2019 Martin Goellnitz
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.monitor.Statistics;


public abstract class AbstractTemplateResolver<T extends Object> implements TemplateResolver<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTemplateResolver.class);

    /**
     * replace brackets [] in array type names with _array in class names for resolution.
     */
    private final boolean suppressBrackets;

    /**
     * String to use as a separator between package name and class name for resolution.
     */
    private final String packageSeparator;

    private String name = getClass().getSimpleName();

    private boolean activateCaching = true;

    private final Map<String, T> cache = new HashMap<>();

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
        LOG.info("checkView({}) view={}  path={}", getName(), view, path);
        try {
            result = resolveView(path, locale);
            LOG.debug("checkView() resolved view {}", result);
            if (result!=null) {
                result = checkResourceExists(result);
            } // if
            LOG.debug("checkView() result={}", result);
        } catch (Exception e) {
            if ((e.getCause()!=null)&&(e.getCause().getClass().getName().contains("Parse"))) {
                throw (RuntimeException) (e.getCause());
            } else {
                LOG.warn("checkView()", e);
            } // if
        } // try/catch
        return result;
    } // checkView()


    /**
     * Traverses type hierarchy of given content item for look up a view for the given parameter set.
     *
     * @param viewName name of the view to look up - may be null for the default view
     * @param locale locale of the current rendering situation
     * @param content instance to find view for
     * @param key cache key for the lookup - used to avoid too many lookups of the same view situation
     * @return resulting view or null if no view is found
     * @throws IOException - in subclasses not this one
     */
    protected T lookupView(String viewName, Locale locale, Object content, String key) throws IOException {
        Class<? extends Object> cls = content.getClass();
        T view = null;
        Set<String> alreadyChecked = new HashSet<>();
        List<Object> allInterfaces = new ArrayList<>();
        while ((view==null)&&(cls!=null)) {
            String pack = (cls.getPackage()==null) ? "" : cls.getPackage().getName();
            view = checkView(viewName, pack, cls.getSimpleName(), key, locale);
            if (view==null) {
                for (Object i : ClassUtils.getAllInterfaces(cls)) {
                    if (allInterfaces.contains(i)) {
                        allInterfaces.remove(i);
                    } // if
                } // for
                for (Object i : ClassUtils.getAllInterfaces(cls)) {
                    allInterfaces.add(i);
                } // for
            } // if
            cls = cls.getSuperclass();
        } // while
        // Walk down interfaces
        if (view==null) {
            for (Object i : allInterfaces) {
                Class<? extends Object> c = (Class<? extends Object>) i;
                LOG.debug("lookupView() type to check templates for {}", c.getName());
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
        return view;
    } // lookupView()


    @Override
    public T resolveTemplate(String viewName, Map<String, Object> model, Locale locale) throws IOException {
        LOG.debug("resolveTemplate({}) {}", getName(), viewName);
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
            LOG.info("resolveTemplate({}) no template found for {}", getName(), content.getClass().getSimpleName());
            cacheView = getNotFoundDummy();
        } // if
        if (activateCaching) {
            cache.put(key, cacheView);
        } // if
        return view;
    } // resolveView()

} // AbstractTemplateResolver
