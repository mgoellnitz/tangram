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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.tangram.Constants;
import org.tangram.components.DefaultController;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.controller.RenderingController;
import org.tangram.edit.PropertyConverter;
import org.tangram.logic.ClassRepository;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.MutableContent;
import org.tangram.view.Utils;
import org.tangram.view.link.Link;
import org.tangram.view.link.LinkFactory;


@Controller
public class EditingController extends RenderingController {

    private static final Log log = LogFactory.getLog(EditingController.class);

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
        SYSTEM_PROPERTIES.add("manager");
        SYSTEM_PROPERTIES.add("beanFactory");
    } // static

    @Autowired
    private PropertyConverter propertyConverter;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private LinkFactory linkFactory;


    @Autowired
    public void setDefaultController(DefaultController defaultController) {
        // Automagically set edit view
        defaultController.getCustomLinkViews().add("edit");
    } // setDefaultController()


    private MutableBeanFactory getMutableBeanFactory() {
        return (MutableBeanFactory) getBeanFactory();
    } // getMutableBeanFactory()


    private ModelAndView redirect(HttpServletRequest request, HttpServletResponse response, Content content) throws IOException {
        // was:
        // return modelAndViewFactory.createModelAndView(content, EDIT_VIEW+getVariant(request), request, response);
        Link link = linkFactory.createLink(request, response, content, EDIT_VIEW, null);
        response.sendRedirect(link.getUrl());
        return null;
    } // redirect()


    private final BeanWrapper createWrapper(Object bean, HttpServletRequest request) {
        BeanWrapper wrapper = Utils.createWrapper(bean, request);
        if (log.isInfoEnabled()) {
            log.info("createWrapper() conversion service "+wrapper.getConversionService());
        } // if
        return wrapper;
    } // createWrapper()


    @RequestMapping(value = "/store/id_{id}")
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ModelAndView store(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            MutableContent bean = beanFactory.getBean(MutableContent.class, id);
            BeanWrapper wrapper = createWrapper(bean, request);
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
                        Object value = propertyConverter.getStorableObject(valueString, cls);
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

            bean = getMutableBeanFactory().getBeanForUpdate(MutableContent.class, id);
            // wrapper = new BeanWrapperImpl(bean);
            wrapper = createWrapper(bean, request);
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

            if (!bean.persist()) {
                throw new Exception("Could not persist bean "+bean.getId());
            } // if

            if (log.isDebugEnabled()) {
                log.debug("store() id="+id);
            } // if

            if (e!=null) {
                throw e;
            } // if

            return redirect(request, response, bean);
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
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


    @RequestMapping(value = "/create")
    public ModelAndView create(@RequestParam(value = PARAMETER_CLASS_NAME) String typeName, HttpServletRequest request,
                               HttpServletResponse response) {
        try {
            if (log.isInfoEnabled()) {
                log.info("create() creating new instance of type "+typeName);
            } // if
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            @SuppressWarnings("unchecked")
            // Class<MutableContent> cls = (Class<MutableContent>)(this.getClass().getClassLoader().loadClass(typeName));
            // TODO: do this more efficiently and in one place
            Class<? extends MutableContent> cls = null;
            for (Class<? extends MutableContent> c : getMutableBeanFactory().getClasses()) {
                if (c.getName().equals(typeName)) {
                    cls = c;
                } // if
            } // for
            MutableContent content = getMutableBeanFactory().createBean(cls);
            if (content.persist()) {
                if (log.isDebugEnabled()) {
                    log.debug("create() content="+content);
                    log.debug("create() id="+content.getId());
                } // if
                return redirect(request, response, content);
            } // if
            return modelAndViewFactory.createModelAndView(new Exception("Cannot persist new "+typeName), request, response);
        } catch (Exception e) {
            log.error("create() error while creating object ", e);
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // create()


    @RequestMapping(value = "/list")
    @SuppressWarnings("unchecked")
    public ModelAndView list(@RequestParam(value = PARAMETER_CLASS_NAME, required = false) String typeName,
                             HttpServletRequest request, HttpServletResponse response) {
        try {
            if (log.isInfoEnabled()) {
                log.info("list() listing instances of type '"+typeName+"'");
            } // if
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            Class<? extends Content> cls = null;
            if (typeName!=null) {
                try {
                    cls = (Class<Content>) (this.getClass().getClassLoader().loadClass(typeName));
                } catch (Exception e) {
                    log.error("list()", e);
                } // try/catch
            } // if
            Collection<Class<? extends MutableContent>> classes = getMutableBeanFactory().getClasses();
            if (cls==null) {
                if (!classes.isEmpty()) {
                    cls = classes.iterator().next();
                } // if
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
            Map<String, Object> model = new HashMap<String, Object>();
            model.put(Constants.THIS, contents);
            model.put("request", request);
            model.put("response", response);
            model.put("classes", classes);
            return modelAndViewFactory.createModelAndView(model, "tangramEditorList"+getVariant(request));
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // list()


    @RequestMapping(value = "/edit/id_{id}")
    public ModelAndView edit(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (log.isInfoEnabled()) {
                log.info("edit() editing "+id);
            } // if
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            response.setContentType("text/html; charset=UTF-8");
            Content content = beanFactory.getBean(id);
            ModelAndView mav = modelAndViewFactory.createModelAndView(content, "edit"+getVariant(request), request, response);
            mav.getModel().put(ATTRIBUTE_WRAPPER, createWrapper(content, request));
            if (content instanceof CodeResource) {
                CodeResource code = (CodeResource) content;
                mav.getModel().put("compilationErrors", classRepository.getCompilationErrors().get(code.getAnnotation()));
            } // if
            mav.getModel().put("classes", getMutableBeanFactory().getClasses());
            return mav;
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // edit()


    // Changed to method get for new class selection mimik
    @RequestMapping(method = {RequestMethod.GET}, value = "/link")
    public ModelAndView link(@RequestParam(value = PARAMETER_CLASS_NAME) String typeName,
                             @RequestParam(value = PARAMETER_ID) String id, @RequestParam(value = PARAMETER_PROPERTY) String propertyName,
                             HttpServletRequest request, HttpServletResponse response) {
        try {
            if (log.isInfoEnabled()) {
                log.info("link() creating new instance of type "+typeName);
            } // if
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            @SuppressWarnings("unchecked")
            // Class<MutableContent> cls = (Class<MutableContent>)(this.getClass().getClassLoader().loadClass(typeName));
            // TODO: do this more efficiently and in one place
            Class<? extends MutableContent> cls = null;
            for (Class<? extends MutableContent> c : getMutableBeanFactory().getClasses()) {
                if (c.getName().equals(typeName)) {
                    cls = c;
                } // if
            } // for
            MutableContent content = getMutableBeanFactory().createBean(cls);
            if (content.persist()) {
                if (log.isDebugEnabled()) {
                    log.debug("link() content="+content);
                    log.debug("link() id="+content.getId());
                } // if

                // re-get for update to avoid xg transactions where ever possible
                MutableContent bean = getMutableBeanFactory().getBeanForUpdate(id);
                BeanWrapper wrapper = createWrapper(bean, request);

                Object listObject = wrapper.getPropertyValue(propertyName);
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) listObject;
                list.add(content);

                wrapper.setPropertyValue(propertyName, list);
                bean.persist();
                return redirect(request, response, content);
            } else {
                throw new Exception("could not create new instance of type "+typeName);
            } // if
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
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

} // EditingController
