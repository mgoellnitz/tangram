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
package org.tangram.jdo.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
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
import org.tangram.content.Content;
import org.tangram.controller.DefaultController;
import org.tangram.controller.RenderingController;
import org.tangram.edit.PropertyConverter;
import org.tangram.jdo.JdoBeanFactory;
import org.tangram.jdo.JdoContent;
import org.tangram.view.link.Link;

@Controller
public class EditingController extends RenderingController {

    private static final Log log = LogFactory.getLog(EditingController.class);

    public static final String EDIT_TARGET = "_tangram_editor";

    public static final String EDIT_VIEW = "edit";

    public static final String PARAMETER_CLASS_NAME = "cms.editor.class.name";

    public static final String PARAMETER_ID = "cms.editor.id";

    public static final String PARAMETER_PROPERTY = "cms.editor.property.name";

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
    public void setDefaultController(DefaultController defaultController) {
        // Automagically set edit view
        defaultController.getCustomLinkViews().add("edit");
    } // setDefaultController()


    @RequestMapping(value = "/store/id_{id}")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ModelAndView store(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getAttribute(Constants.ATTRIBUTE_ADMIN_USER)==null) {
                throw new Exception("User may not edit");
            } // if
            JdoContent bean = beanFactory.getBean(JdoContent.class, id);
            BeanWrapper wrapper = new BeanWrapperImpl(bean);
            Map<String, Object> newValues = new HashMap<String, Object>();

            Map parameterMap = request.getParameterMap();
            if (log.isDebugEnabled()) {
                log.debug("store() # parameters "+parameterMap.size());
                log.debug("store() "+request.getClass().getName());
            } // if
            for (Object entry : parameterMap.entrySet()) {
                Entry<String, String[]> parameter = (Entry<String, String[]>)entry;
                String key = parameter.getKey();
                if ( !key.startsWith("cms.editor")) {
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
                        if ( !(JdoContent.class.isAssignableFrom(cls)&&value==null)) {
                            newValues.put(key, value);
                        } else {
                            if (log.isInfoEnabled()) {
                                log.info("store() not setting value");
                            } // if
                        } // if
                        if (log.isInfoEnabled()) {
                            log.info("store() get "+wrapper.getPropertyValue(key));
                        } // if
                    } catch (Exception e) {
                        throw new Exception("Cannot set value for "+key, e);
                    } // try/catch
                } // if
            } // for
            if (request instanceof DefaultMultipartHttpServletRequest) {
                DefaultMultipartHttpServletRequest r = (DefaultMultipartHttpServletRequest)request;
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
                        if ( !key.startsWith("cms.editor")) {
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

            bean = beanFactory.getBeanForUpdate(JdoContent.class, id);
            wrapper = new BeanWrapperImpl(bean);
            for (String propertyName : newValues.keySet()) {
                wrapper.setPropertyValue(propertyName, newValues.get(propertyName));
            } // for

            if ( !bean.persist()) {
                throw new Exception("Could not persist bean "+bean.getId());
            } // if

            if (log.isDebugEnabled()) {
                log.debug("store() id="+id);
            } // if
            return modelAndViewFactory.createModelAndView(bean, EDIT_VIEW+getVariant(request), request, response);
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    }// store()


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
            Class<Content> cls = (Class<Content>)(this.getClass().getClassLoader().loadClass(typeName));
            Content content = beanFactory.createBean(cls);
            if (log.isDebugEnabled()) {
                log.debug("create() content="+content);
                log.debug("create() id="+content.getId());
            } // if
            content.persist();
            return modelAndViewFactory.createModelAndView(content, EDIT_VIEW+getVariant(request), request, response);
        } catch (Exception e) {
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
                    cls = (Class<Content>)(this.getClass().getClassLoader().loadClass(typeName));
                } catch (Exception e) {
                    log.error("list()", e);
                } // try/catch
            } // if
            if (cls==null) {
                Collection<Class<? extends Content>> clss = ((JdoBeanFactory)beanFactory).getClasses();
                if ( !clss.isEmpty()) {
                    cls = clss.iterator().next();
                } // if
            } // if
            List<? extends Content> contents = null;
            if (cls!=null) {
                contents = beanFactory.listBeansOfExactClass(cls);
                try {
                    Collections.sort(contents);
                } catch (Exception e) {
                    log.error("list() error while sorting", e);
                } // try/catch
            } else {
                contents = new ArrayList<Content>();
            } // if
            response.setContentType("text/html; charset=UTF-8");
            return modelAndViewFactory.createModelAndView(contents, "tangramEditorList"+getVariant(request), request, response);
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
            return modelAndViewFactory.createModelAndView(content, "edit"+getVariant(request), request, response);
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // edit()


    @RequestMapping(method = { RequestMethod.POST }, value = "/link")
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
            Class<Content> cls = (Class<Content>)(this.getClass().getClassLoader().loadClass(typeName));
            Content content = beanFactory.createBean(cls);
            if (log.isDebugEnabled()) {
                log.debug("link() content="+content);
                log.debug("link() id="+content.getId());
            } // if
            content.persist();

            Content bean = beanFactory.getBeanForUpdate(Content.class, id);
            BeanWrapper wrapper = new BeanWrapperImpl(bean);
            Object listObject = wrapper.getPropertyValue(propertyName);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>)listObject;
            list.add(content);
            wrapper.setPropertyValue(propertyName, list);
            bean.persist();

            return modelAndViewFactory.createModelAndView(content, EDIT_VIEW+getVariant(request), request, response);
        } catch (Exception e) {
            return modelAndViewFactory.createModelAndView(e, request, response);
        } // try/catch
    } // link()


    private String getUrl(Object bean, String action, String view) {
        if ("store".equals(action)) {
            return "/store/id_"+((JdoContent)bean).getId();
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
            String url = "/edit/id_"+((JdoContent)bean).getId();
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
