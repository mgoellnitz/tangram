package org.tangram.view;

import java.lang.reflect.Constructor;
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
import org.tangram.content.BeanListener;
import org.tangram.content.Content;
import org.tangram.logic.ClassRepository;
import org.tangram.logic.Shim;
import org.tangram.logic.ViewShim;

public class GroovyModelAndViewFactory extends DefaultModelAndViewFactory implements InitializingBean, BeanListener {

    private static final Log log = LogFactory.getLog(GroovyModelAndViewFactory.class);

    @Autowired
    private ClassRepository classRepository;

    private Map<String, List<Constructor<Shim>>> definedViewShims;

    private Map<String, List<Constructor<Shim>>> definedBeanShims;

    private Map<String, List<Constructor<Shim>>> cachedViewShims;

    private Map<String, List<Constructor<Shim>>> cachedBeanShims;


    private final void defineShim(Map<String, List<Constructor<Shim>>> definedShims, Class<Content> beanClass,
            Constructor<Shim> shimClass) {
        List<Constructor<Shim>> shims = definedShims.get(beanClass.getName());
        if (shims==null) {
            shims = new ArrayList<Constructor<Shim>>();
            definedShims.put(beanClass.getName(), shims);
        } // if
        shims.add(shimClass);
    } // defineShim()


    private final void defineViewShim(Class<Content> beanClass, Constructor<Shim> shimClass) {
        defineShim(definedViewShims, beanClass, shimClass);
    } // defineViewShim()


    private final void defineBeanShim(Class<Content> beanClass, Constructor<Shim> shimClass) {
        defineShim(definedBeanShims, beanClass, shimClass);
    } // defineBeanShim()


    @Override
	@SuppressWarnings("unchecked")
    public void reset() {
        definedViewShims = new HashMap<String, List<Constructor<Shim>>>();
        definedBeanShims = new HashMap<String, List<Constructor<Shim>>>();
        cachedViewShims = new HashMap<String, List<Constructor<Shim>>>();
        cachedBeanShims = new HashMap<String, List<Constructor<Shim>>>();

        ClassLoader classLoader = this.getClass().getClassLoader();
        for (Map.Entry<String, Class<Shim>> entry : classRepository.get(Shim.class).entrySet()) {
            try {
                String annotation = entry.getKey();
                Class<Content> beanClass = (Class<Content>)classLoader.loadClass(annotation);
                Class<Shim> c = entry.getValue();
                String className = c.getName();
                if (ViewShim.class.isAssignableFrom(c)) {
                    if (log.isInfoEnabled()) {
                        log.info("setShimClasses() defining view shim "+className+" for "+annotation);
                    } // if
                    Constructor<Shim> ct = c.getConstructor(HttpServletRequest.class, beanClass);
                    defineViewShim(beanClass, ct);
                } else {
                    if (Shim.class.isAssignableFrom(c)) {
                        if (log.isInfoEnabled()) {
                            log.info("setShimClasses() defining bean shim "+className+" for "+annotation);
                        } // if
                        Constructor<Shim> ct = c.getConstructor(beanClass);
                        defineBeanShim(beanClass, ct);
                    } // if
                } // if
            } catch (Throwable e) {
                // who cares
                if (log.isErrorEnabled()) {
                    log.error("fillShimClasses()", e);
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

        // Map<String, Object> model = result.getModel();
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
