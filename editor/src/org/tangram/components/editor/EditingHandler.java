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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.components.editor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.tangram.Constants;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.annotate.LinkPart;
import org.tangram.components.spring.DefaultController;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.link.Link;
import org.tangram.link.LinkFactory;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.logic.ClassRepository;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableContent;
import org.tangram.spring.RenderingController;
import org.tangram.view.PropertyConverter;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.Utils;


/**
 * Request handling component for the editing facility.
 *
 * This class does all the magic not found in the templates of the editor including URL generation and handling.
 */
@Named
@LinkHandler
public class EditingHandler extends RenderingController implements LinkFactory {

    private static final Log log = LogFactory.getLog(EditingHandler.class);

    public static final String EDIT_TARGET = "_tangram_editor";

    public static final String EDIT_VIEW = "edit";

    public static final String PARAMETER_CLASS_NAME = "cms.editor.class.name";

    public static final String PARAMETER_ID = "cms.editor.id";

    public static final String PARAMETER_PROPERTY = "cms.editor.property.name";

    public static final String ATTRIBUTE_WRAPPER = "wrapper";

    /**
     * writable properties which should not be altered by the upper layers or persisted
     */
    public static Set<String> SYSTEM_PROPERTIES;


    static {
        SYSTEM_PROPERTIES = new HashSet<String>();
        // The groovy compiler seems to use this
        SYSTEM_PROPERTIES.add("metaClass");
        SYSTEM_PROPERTIES.add("manager");
        SYSTEM_PROPERTIES.add("beanFactory");
    } // static

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private PropertyConverter propertyConverter;

    @Inject
    private ClassRepository classRepository;

    @Inject
    private LinkFactoryAggregator linkFactory;


    @Inject
    public void setDefaultController(DefaultController defaultController) {
        // Automagically set edit view
        defaultController.getCustomLinkViews().add("edit");
    } // setDefaultController()


    private MutableBeanFactory getMutableBeanFactory() {
        return (MutableBeanFactory) getBeanFactory();
    } // getMutableBeanFactory()


    private TargetDescriptor describeTarget(Content bean) throws IOException {
        return new TargetDescriptor(bean, null, EDIT_VIEW);
    } // redirect()


    @LinkAction("/store/id_(.*)")
    @SuppressWarnings({"rawtypes", "unchecked"})
    public TargetDescriptor store(@LinkPart(1) String id, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            MutableContent bean = beanFactory.getBean(MutableContent.class, id);
            BeanWrapper wrapper = Utils.createWrapper(bean);
            Map<String, Object> newValues = new HashMap<String, Object>();
            // List<String> deleteValues = new ArrayList<String>();

            Map parameterMap = request.getParameterMap();
            if (log.isDebugEnabled()) {
                log.debug("store() # parameters "+parameterMap.size());
                log.debug("store() "+request.getClass().getName());
            } // if
            for (Object entry : parameterMap.entrySet()) {
                Entry<String, String[]> parameter = (Entry<String, String[]>) entry;
                String key = parameter.getKey();
                if (!key.startsWith("cms.editor")) {
                    try {
                        StringBuilder msg = new StringBuilder("store() ");
                        msg.append(key);
                        msg.append(": ");
                        String[] values = parameter.getValue();
                        for (String value : values) {
                            msg.append(value);
                            msg.append(" ");
                        } // for
                        if (log.isInfoEnabled()) {
                            log.info(msg.toString());
                        } // if

                        Class cls = wrapper.getPropertyType(key);
                        String valueString = values.length==1 ? values[0] : "";
                        Object value = propertyConverter.getStorableObject(valueString, cls, request);
                        if (log.isInfoEnabled()) {
                            log.info("store() value="+value);
                        } // if
                        if (!(Content.class.isAssignableFrom(cls)&&value==null)) {
                            newValues.put(key, value);
                        } else {
                            if (log.isInfoEnabled()) {
                                log.info("store() not setting value");
                            } // if
                        } // if
                        if (Content.class.isAssignableFrom(cls)&&"".equals(valueString)) {
                            newValues.put(key, null);
                        } // if
                    } catch (Exception e) {
                        throw new Exception("Cannot set value for "+key, e);
                    } // try/catch
                } // if
            } // for
            if (request instanceof DefaultMultipartHttpServletRequest) {
                DefaultMultipartHttpServletRequest r = (DefaultMultipartHttpServletRequest) request;
                Map<String, MultipartFile> fileMap = r.getFileMap();

                for (Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                    String key = entry.getKey();
                    String filename = entry.getValue().getName();
                    if (log.isInfoEnabled()) {
                        log.info("store() size "+entry.getValue().getSize());
                        log.info("store() name "+filename);
                    } // if
                    if (log.isDebugEnabled()) {
                        log.debug("store() key "+key);
                        log.debug("store() original filename "+entry.getValue().getOriginalFilename());
                    } // if
                    if (filename.length()>0) {
                        if (!key.startsWith("cms.editor")) {
                            try {
                                if (log.isInfoEnabled()) {
                                    log.info("multipart file "+key);
                                } // if

                                Class cls = wrapper.getPropertyType(key);
                                if (propertyConverter.isBlobType(cls)) {
                                    byte[] octets = entry.getValue().getBytes();
                                    if (log.isDebugEnabled()) {
                                        log.debug("store() number of bytes to store is "+octets.length);
                                    } // if
                                    newValues.put(key, propertyConverter.createBlob(octets));
                                } // if
                            } catch (Exception e) {
                                throw new Exception("Cannot set value for "+key);
                            } // try/catch
                        } // if
                    } // if
                } // for
            } // if

            bean = getMutableBeanFactory().getBean(MutableContent.class, id);
            getMutableBeanFactory().beginTransaction();
            wrapper = Utils.createWrapper(bean);
            Exception e = null;
            for (String propertyName : newValues.keySet()) {
                try {
                    wrapper.setPropertyValue(propertyName, newValues.get(propertyName));
                } catch (Exception ex) {
                    e = new Exception("Cannot set value for "+propertyName, ex);
                } // try/catch
            } // for
            /*
             for (String propertyName : deleteValues) {
             try {
             wrapper.setPropertyValue(propertyName, null);
             } catch (Exception ex) {
             e = new Exception("Cannot delete value for "+propertyName, ex);
             } // try/catch
             } // for
             */

            if (!getMutableBeanFactory().persist(bean)) {
                throw new Exception("Could not persist bean "+bean.getId());
            } // if

            if (log.isDebugEnabled()) {
                log.debug("store() id="+id);
            } // if

            if (e!=null) {
                throw e;
            } // if

            return describeTarget(bean);
        } catch (Exception e) {
            return new TargetDescriptor(e, null, null);
        } // try/catch
    } // store()


    private String getVariant(HttpServletRequest request) {
        String agent = request.getHeader("user-agent");
        String variant = "";
        if (agent.indexOf("iPhone")>0) {
            variant = "$mobile";
        } // if
        if (agent.indexOf("Android")>0) {
            variant = "$mobile";
        } // if
        if (agent.indexOf("Pre/1.0")>0) {
            variant = "$mobile";
        } // if
        if (agent.indexOf("MIDP")>0) {
            variant = "$mobile";
        } // if
        if (log.isDebugEnabled()) {
            log.debug("getVariant() agent="+agent+" -> "+variant);
        } // if
        return variant;
    } // getVariant()


    /**
     * Load a class for a given type name.
     *
     * Don't use a simple ClassLoader.loadClass since this classloader might not know about classes available
     * from the class repository, so use this simple iterator here.
     *
     * @param typeName
     * @return Class for the given type name or null
     */
    private Class<? extends MutableContent> loadClass(String typeName) {
        // Class<MutableContent> cls = (Class<MutableContent>)(this.getClass().getClassLoader().loadClass(typeName));
        // TODO: do this more efficiently
        Class<? extends MutableContent> cls = null;
        for (Class<? extends MutableContent> c : getMutableBeanFactory().getClasses()) {
            if (c.getName().equals(typeName)) {
                cls = c;
            } // if
        } // for
        return cls;
    } // loadClass()


    @LinkAction("/create")
    public TargetDescriptor create(@ActionParameter(PARAMETER_CLASS_NAME) String typeName, HttpServletRequest request,
                                   HttpServletResponse response) {
        try {
            if (log.isInfoEnabled()) {
                log.info("create() creating new instance of type "+typeName);
            } // if
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            @SuppressWarnings("unchecked")
            Class<? extends MutableContent> cls = loadClass(typeName);
            MutableContent content = getMutableBeanFactory().createBean(cls);
            if (getMutableBeanFactory().persist(content)) {
                if (log.isDebugEnabled()) {
                    log.debug("create() content="+content);
                    log.debug("create() id="+content.getId());
                } // if
                return describeTarget(content);
            } // if
            return new TargetDescriptor(new Exception("Cannot persist new "+typeName), null, null);
        } catch (Exception e) {
            log.error("create() error while creating object ", e);
            return new TargetDescriptor(e, null, null);
        } // try/catch
    } // create()


    @LinkAction("/list")
    @SuppressWarnings("unchecked")
    public TargetDescriptor list(@ActionParameter(PARAMETER_CLASS_NAME) String typeName,
                             HttpServletRequest request, HttpServletResponse response) {
        try {
            if (log.isInfoEnabled()) {
                log.info("list() listing instances of type '"+typeName+"'");
            } // if
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            Collection<Class<? extends MutableContent>> classes = getMutableBeanFactory().getClasses();
            Class<? extends Content> cls = null;
            // take first one of classes available
            if (!classes.isEmpty()) {
                cls = classes.iterator().next();
            } // if
            // try to take class from provided classes
            if (StringUtils.isNotBlank(typeName)) {
                for (Class<? extends MutableContent> c : classes) {
                    if (c.getName().equals(typeName)) {
                        cls = c;
                    } // if
                } // for
            } // if

            List<? extends Content> contents = Collections.emptyList();
            if (cls!=null) {
                contents = beanFactory.listBeansOfExactClass(cls);
                try {
                    Collections.sort(contents);
                } catch (Exception e) {
                    log.error("list() error while sorting", e);
                } // try/catch
            } // if
            response.setContentType("text/html; charset=UTF-8");
            request.setAttribute(Constants.THIS, contents);
            request.setAttribute("request", request);
            request.setAttribute("response", response);
            request.setAttribute("classes", classes);
            request.setAttribute("prefix", Utils.getUriPrefix(request));
            if (cls!=null) {
                Class<? extends Object> designClass = (cls.getName().indexOf('$')<0) ? cls : cls.getSuperclass();
                request.setAttribute("designClass", designClass);
                request.setAttribute("designClassPackage", designClass.getPackage());
            } // if
            return new TargetDescriptor(contents, "tangramEditorList"+getVariant(request), null);
        } catch (Exception e) {
            return new TargetDescriptor(e, null, null);
        } // try/catch
    } // list()


    @LinkAction("/edit/id_(.*)")
    public TargetDescriptor edit(@LinkPart(1) String id, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (log.isInfoEnabled()) {
                log.info("edit() editing "+id);
            } // if
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            response.setContentType("text/html; charset=UTF-8");
            Content content = beanFactory.getBean(id);

            request.setAttribute(ATTRIBUTE_WRAPPER, Utils.createWrapper(content));
            if (content instanceof CodeResource) {
                CodeResource code = (CodeResource) content;
                request.setAttribute("compilationErrors", classRepository.getCompilationErrors().get(code.getAnnotation()));
            } // if
            request.setAttribute("classes", getMutableBeanFactory().getClasses());
            request.setAttribute("prefix", Utils.getUriPrefix(request));
            Class<? extends Content> cls = content.getClass();
            final Class<? extends Object> designClass = (cls.getName().indexOf('$')<0) ? cls : cls.getSuperclass();
            request.setAttribute("designClass", designClass);
            request.setAttribute("designClassPackage", designClass.getPackage());

            return new TargetDescriptor(content, "edit"+getVariant(request), null);
        } catch (Exception e) {
            return new TargetDescriptor(e, null, null);
        } // try/catch
    } // edit()


    // Changed to method get for new class selection mimik
    @LinkAction("/link")
    public TargetDescriptor link(@ActionParameter(PARAMETER_CLASS_NAME) String typeName,
                             @ActionParameter(PARAMETER_ID) String id, @ActionParameter(PARAMETER_PROPERTY) String propertyName,
                             HttpServletRequest request, HttpServletResponse response) {
        try {
            if (log.isInfoEnabled()) {
                log.info("link() creating new instance of type "+typeName);
            } // if
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            @SuppressWarnings("unchecked")
            Class<? extends MutableContent> cls = loadClass(typeName);
            MutableContent content = getMutableBeanFactory().createBean(cls);
            if (getMutableBeanFactory().persist(content)) {
                if (log.isDebugEnabled()) {
                    log.debug("link() content="+content);
                    log.debug("link() id="+content.getId());
                } // if

                // re-get for update to avoid xg transactions where ever possible
                MutableContent bean = getMutableBeanFactory().getBean(MutableContent.class, id);
                getMutableBeanFactory().beginTransaction();
                BeanWrapper wrapper = Utils.createWrapper(bean);

                Object listObject = wrapper.getPropertyValue(propertyName);
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) listObject;
                list.add(content);

                wrapper.setPropertyValue(propertyName, list);
                getMutableBeanFactory().persist(bean);
                return describeTarget(content);
            } else {
                throw new Exception("could not create new instance of type "+typeName);
            } // if
        } catch (Exception e) {
            return new TargetDescriptor(e, null, null);
        } // try/catch
    } // link()


    private String getUrl(Object bean, String action, String view) {
        if ("store".equals(action)) {
            return "/store/id_"+((Content) bean).getId();
        } else {
            if ("create".equals(action)) {
                return "/create";
            } else {
                if ("list".equals(action)) {
                    return "/list";
                } else {
                    if ("link".equals(action)) {
                        return "/link";
                    } else {
                        return null;
                    } // if
                } // if
            } // if
        } // if
    } // getUrl()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        Link result = null;
        if ("edit".equals(action)) {
            String url = "/edit/id_"+((Content) bean).getId();
            result = new Link();
            result.setUrl(url);
            result.setTarget(EDIT_TARGET);
            String jsOpenWindow = "window.open('"+result.getUrl()+"', '"+EDIT_TARGET
                    +"', 'menubar=no,status=no,toolbar=no,resizable=yes, scrollbars=yes');";
            result.addHandler("onclick", jsOpenWindow);
        } else {
            String url = getUrl(bean, action, view);
            if (url!=null) {
                result = new Link();
                result.setUrl(url);
            } // if
        } // if
        return result;
    } // createLink()


    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        linkFactory.registerHandler(this);
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // EditingHandler
