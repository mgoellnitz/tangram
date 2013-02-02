/**
 * 
 * Copyright 2011-2013 Martin Goellnitz
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

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.tangram.components.ClassRepository;
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.logic.Shim;
import org.tangram.logic.ViewShim;

/**
 * This ModelAndView factory implementation adds potential groovy based implementations of related codes as instances to
 * the context.
 * 
 */
public class GroovyModelAndViewFactory extends DefaultModelAndViewFactory implements InitializingBean, BeanListener {

    private static final Log log = LogFactory.getLog(GroovyModelAndViewFactory.class);

    @Autowired
    private ClassRepository classRepository;

    private Map<String, List<Constructor<Shim>>> definedViewShims;

    private Map<String, List<Constructor<Shim>>> definedBeanShims;

    private Map<String, List<Constructor<Shim>>> cachedViewShims;

    private Map<String, List<Constructor<Shim>>> cachedBeanShims;


    private void defineShim(Map<String, List<Constructor<Shim>>> definedShims, Class<Content> beanClass, Constructor<Shim> shimClass) {
        List<Constructor<Shim>> shims = definedShims.get(beanClass.getName());
        if (shims==null) {
            shims = new ArrayList<Constructor<Shim>>();
            definedShims.put(beanClass.getName(), shims);
        } // if
        shims.add(shimClass);
    } // defineShim()


    private void defineViewShim(Class<Content> beanClass, Constructor<Shim> shimClass) {
        defineShim(definedViewShims, beanClass, shimClass);
    } // defineViewShim()


    private void defineBeanShim(Class<Content> beanClass, Constructor<Shim> shimClass) {
        defineShim(definedBeanShims, beanClass, shimClass);
    } // defineBeanShim()


    @Override
    @SuppressWarnings("unchecked")
    public void reset() {
        definedViewShims = new HashMap<String, List<Constructor<Shim>>>();
        definedBeanShims = new HashMap<String, List<Constructor<Shim>>>();
        cachedViewShims = new HashMap<String, List<Constructor<Shim>>>();
        cachedBeanShims = new HashMap<String, List<Constructor<Shim>>>();

        for (Map.Entry<String, Class<Shim>> entry : classRepository.get(Shim.class).entrySet()) {
            try {
                String annotation = entry.getKey();
                Class<Shim> c = entry.getValue();
                ParameterizedType pt = ((ParameterizedType)c.getGenericSuperclass());
                Type[] actualTypes = pt.getActualTypeArguments();
                Class<Content> beanClass = (Class<Content>)(actualTypes[0]);
                String className = c.getName();
                if (ViewShim.class.isAssignableFrom(c)) {
                    if (log.isInfoEnabled()) {
                        log.info("reset() defining view shim "+className+" for "+annotation);
                    } // if
                    Constructor<Shim> ct = c.getConstructor(HttpServletRequest.class, beanClass);
                    defineViewShim(beanClass, ct);
                } else {
                    if (Shim.class.isAssignableFrom(c)) {
                        if (log.isInfoEnabled()) {
                            log.info("reset() defining bean shim "+className+" for "+annotation);
                        } // if
                        Constructor<Shim> ct = c.getConstructor(beanClass);
                        defineBeanShim(beanClass, ct);
                    } // if
                } // if
            } catch (Throwable e) {
                // who cares
                if (log.isErrorEnabled()) {
                    log.error("reset()", e);
                } // if
            } // try/catch
        } // for
    } // reset()


    private List<Constructor<Shim>> getShimsFor(Map<String, List<Constructor<Shim>>> definedShims,
            Map<String, List<Constructor<Shim>>> cachedShims, Class<? extends Object> shimFor) {
        // try this first, since we hope to deal with data views most times
        List<Constructor<Shim>> result = cachedShims.get(shimFor.getName());
        if (result==null) {
            result = new ArrayList<Constructor<Shim>>();
            cachedShims.put(shimFor.getName(), result);
            if (log.isDebugEnabled()) {
                log.debug("getShimsFor() defining shims for "+shimFor.getName());
            } // if
            while ((shimFor!=null)&&( !"JdoContent".equals(shimFor.getSimpleName()))) {
                List<Constructor<Shim>> shims = definedShims.get(shimFor.getName());
                if (shims!=null) {
                    result.addAll(shims);
                } // if
                shimFor = shimFor.getSuperclass();
            } // if
            if (log.isDebugEnabled()) {
                for (Constructor<Shim> sct : result) {
                    log.debug("getShimsFor() - "+sct.getDeclaringClass().getSimpleName());
                } // for
            } // if
        } // if
        return result;
    } // getShimsFor()


    public Map<String, Object> getShims(HttpServletRequest request, Object bean) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            Class<? extends Object> shimFor = bean.getClass();
            if (log.isDebugEnabled()) {
                log.debug("getShims() getting shims for "+shimFor.getName());
            } // if

            List<Constructor<Shim>> viewShims = getShimsFor(definedViewShims, cachedViewShims, shimFor);
            for (Constructor<Shim> ct : viewShims) {
                if (ct!=null) {
                    if (log.isDebugEnabled()) {
                        log.debug("getShims() view shim for bean "+bean.getClass().getSimpleName()+" is "
                                +ct.getDeclaringClass().getSimpleName());
                    } // if
                    Shim result = ct.newInstance(request, bean);
                    if (log.isDebugEnabled()) {
                        log.debug("getShims() storing shim as "+result.getAttributeName());
                    } // if
                    resultMap.put(result.getAttributeName(), result);
                } // if
            } // for

            List<Constructor<Shim>> beanShims = getShimsFor(definedBeanShims, cachedBeanShims, shimFor);
            for (Constructor<Shim> ct : beanShims) {
                if (ct!=null) {
                    if (log.isDebugEnabled()) {
                        log.debug("getShims() shim for bean "+bean.getClass().getSimpleName()+" is "
                                +ct.getDeclaringClass().getSimpleName());
                    } // if
                    Shim result = ct.newInstance(bean);
                    if (log.isDebugEnabled()) {
                        log.debug("getShims() storing shim as "+result.getAttributeName());
                    } // if
                    resultMap.put(result.getAttributeName(), result);
                } // if
            } // for
        } catch (Exception e) {
            log.error("getShims() ", e);
        } // try/catch
        return resultMap;
    } // getShims()


    @Override
    public Map<String, Object> createModel(Object bean, ServletRequest request, ServletResponse response) {
        Map<String, Object> model = super.createModel(bean, request, response);

        Map<String, Object> shims = getShims((HttpServletRequest)request, bean);
        for (String key : shims.keySet()) {
            model.put(key, shims.get(key));
        } // for

        return model;
    } // createModel()


    @Override
    public void afterPropertiesSet() throws Exception {
        classRepository.addListener(this);
        reset();
    } // afterPropertiesSet()

} // GroovyModelAndViewFactory
