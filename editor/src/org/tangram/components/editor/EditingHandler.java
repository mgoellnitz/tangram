/**
 *
 * Copyright 2011-2015 Martin Goellnitz
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.annotate.ActionParameter;
import org.tangram.annotate.LinkAction;
import org.tangram.annotate.LinkHandler;
import org.tangram.annotate.LinkPart;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.controller.AbstractRenderingBase;
import org.tangram.editor.AppEngineXStream;
import org.tangram.link.Link;
import org.tangram.link.LinkHandlerRegistry;
import org.tangram.logic.ClassRepository;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.protection.AuthorizationService;
import org.tangram.util.JavaBean;
import org.tangram.view.PropertyConverter;
import org.tangram.view.RequestParameterAccess;
import org.tangram.view.TargetDescriptor;
import org.tangram.view.Utils;
import org.tangram.view.ViewUtilities;


/**
 * Request handling component for the editing facility.
 *
 * This class does all the magic not found in the templates of the editor including URL generation and handling.
 */
@Named
@Singleton
@LinkHandler
public class EditingHandler extends AbstractRenderingBase {

    private static final Logger LOG = LoggerFactory.getLogger(EditingHandler.class);

    public static final String EDIT_TARGET = "_tangram_editor";

    public static final String EDIT_VIEW = "edit";

    public static final String PARAMETER_CLASS_NAME = "cms.editor.class.name";

    public static final String PARAMETER_ID = "cms.editor.id";

    public static final String PARAMETER_PROPERTY = "cms.editor.property.name";

    public static final String ATTRIBUTE_WRAPPER = "wrapper";

    /**
     * writable properties which should not be altered by the upper layers or persisted
     */
    public static final Set<String> SYSTEM_PROPERTIES;

    /**
     * editing actions triggered by URLs containing the obect's ID.
     */
    public static final Collection<String> ID_URL_ACTIONS = new ArrayList<>();

    /**
     * editing actions triggered only by parameters passed in http post requests.
     */
    public static final Collection<String> PARAMETER_ACTIONS = new ArrayList<>();

    @Inject
    private LinkHandlerRegistry registry;

    @Inject
    private PropertyConverter propertyConverter;

    @Inject
    private ClassRepository classRepository;

    @Inject
    private ViewUtilities viewUtilities;

    @Inject
    private AuthorizationService authorizationService;

    private boolean deleteMethodEnabled;


    static {
        SYSTEM_PROPERTIES = new HashSet<>();
        // The groovy compiler seems to use this
        SYSTEM_PROPERTIES.add("metaClass");
        SYSTEM_PROPERTIES.add("manager");
        SYSTEM_PROPERTIES.add("beanFactory");

        ID_URL_ACTIONS.add("delete");
        ID_URL_ACTIONS.add("edit");
        ID_URL_ACTIONS.add("store");

        PARAMETER_ACTIONS.add("create");
        PARAMETER_ACTIONS.add("link");
        PARAMETER_ACTIONS.add("list");
    } // static


    /**
     * Returns the class the developer modeled.
     * This isn't necessarily the same class as the system uses.
     *
     * @param cls class of an ORM obtained object
     * @return Class the developer implemented
     */
    public static Class<? extends Object> getDesignClass(Class<? extends Content> cls) {
        return (cls.getName().indexOf('$')<0) ? cls : cls.getSuperclass();
    } // getDesignClass()


    /**
     * Returns a string note about the ORM class state.
     *
     * @param cls
     * @return note indicating the underlying ORM implementation and class modification state
     * @throws SecurityException
     */
    public static String getOrmNote(Class<? extends Content> cls) throws SecurityException {
        Method[] methods = cls.getMethods();
        String note = "Plain";
        for (Method method : methods) {
            if (method.getName().startsWith("_ebean")) {
                note = "EBean enhanced";
            } // if
            if (method.getName().startsWith("jdo")) {
                note = "DataNucleus JDO/JPA Enhanced";
            } // if
            if (method.getName().startsWith("pc")) {
                note = "OpenJPA Enhanced";
            } // if
            if (method.getName().startsWith("_persistence")) {
                note = "EclipseLink Woven (Weaved)";
            } // if
            if (method.getName().startsWith("$$_hibernate")) {
                note = "Hibernate Enhanced";
            } // if
        } // for
        if (cls.getName().startsWith("org.apache.openjpa.enhance")) {
            note = "OpenJPA Subclass";
        } // if
        return note;
    } // getOrmNote()


    public void setDeleteMethodEnabled(boolean deleteMethodEnabled) {
        this.deleteMethodEnabled = deleteMethodEnabled;
    } // setDeleteMethodEnabled()


    private MutableBeanFactory getMutableBeanFactory() {
        return (MutableBeanFactory) getBeanFactory();
    } // getMutableBeanFactory()


    private TargetDescriptor describeTarget(Content bean) throws IOException {
        return new TargetDescriptor(bean, null, EDIT_VIEW);
    } // redirect()


    @LinkAction("/store/id_(.*)")
    public TargetDescriptor store(@LinkPart(1) String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        authorizationService.throwIfNotAdmin(request, response, "Store should not be called directly");
        Content bean = beanFactory.getBean(Content.class, id);
        JavaBean wrapper = new JavaBean(bean);
        Map<String, Object> newValues = new HashMap<>();
        // List<String> deleteValues = new ArrayList<>();

        RequestParameterAccess parameterAccess = viewUtilities.createParameterAccess(request);
        Map<String, String[]> parameterMap = parameterAccess.getParameterMap();
        LOG.debug("store() # parameters {} for {}", parameterMap.size(), request.getClass().getName());
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            Entry<String, String[]> parameter = entry;
            String key = parameter.getKey();
            if (!key.startsWith("cms.editor")) {
                try {
                    String[] values = parameter.getValue();
                    if (LOG.isInfoEnabled()) {
                        StringBuilder msg = new StringBuilder(128);
                        msg.append("store() ");
                        msg.append(key);
                        msg.append(": ");
                        for (String value : values) {
                            msg.append(value);
                            msg.append(' ');
                        } // for
                        LOG.info(msg.toString());
                    } // if

                    Class<? extends Object> cls = wrapper.getType(key);
                    String valueString = values.length==1 ? values[0] : "";
                    Object value = propertyConverter.getStorableObject(bean, valueString, cls, request);
                    LOG.info("store() value={}", value);
                    if (!(Content.class.isAssignableFrom(cls)&&value==null)) {
                        newValues.put(key, value);
                    } else {
                        LOG.info("store() not setting value");
                    } // if
                    if (Content.class.isAssignableFrom(cls)&&"".equals(valueString)) {
                        newValues.put(key, null);
                    } // if
                } catch (Exception e) {
                    throw new Exception("Cannot set value for "+key, e);
                } // try/catch
            } // if
        } // for

        for (String key : parameterAccess.getBlobNames()) {
            try {
                if (!key.startsWith("cms.editor")) {
                    Class<? extends Object> cls = wrapper.getType(key);
                    if (propertyConverter.isBlobType(cls)) {
                        byte[] octets = parameterAccess.getData(key);
                        newValues.put(key, propertyConverter.createBlob(octets));
                    } // if
                } // if
            } catch (Exception e) {
                throw new Exception("Cannot set value for "+key, e);
            } // try/catch
        } // for

        getMutableBeanFactory().beginTransaction();
        wrapper = new JavaBean(bean);
        Exception e = null;
        for (Map.Entry<String, Object> property : newValues.entrySet()) {
            try {
                wrapper.set(property.getKey(), property.getValue());
            } catch (Exception ex) {
                e = new Exception("Cannot set value for "+property.getKey(), ex);
            } // try/catch
        } // for

        if (!getMutableBeanFactory().persist(bean)) {
            throw new Exception("Could not persist bean "+bean.getId());
        } // if
        LOG.debug("store() id={}", id);
        if (e!=null) {
            throw e;
        } // if

        return describeTarget(bean);
    } // store()


    /**
     * Load a class for a given type name.
     *
     * Don't use a simple ClassLoader.loadClass since this classloader might not know about classes available
     * from the class repository, so use this simple iterator here.
     *
     * @param typeName
     * @return Class for the given type name or null
     */
    private Class<? extends Content> loadClass(String typeName) {
        Class<? extends Content> cls = null;
        for (Class<? extends Content> c : getMutableBeanFactory().getClasses()) {
            if (c.getName().equals(typeName)) {
                cls = c;
            } // if
        } // for
        return cls;
    } // loadClass()


    @LinkAction("/create")
    public TargetDescriptor create(@ActionParameter(PARAMETER_CLASS_NAME) String typeName, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        LOG.info("create() creating new instance of type {}", typeName);
        authorizationService.throwIfNotAdmin(request, response, "Create should not be called directly");
        Class<? extends Content> cls = loadClass(typeName);
        Content content = getMutableBeanFactory().createBean(cls);
        if (getMutableBeanFactory().persist(content)) {
            LOG.debug("create() content={}", content);
            LOG.debug("create() id={}", content.getId());
            return describeTarget(content);
        } // if
        throw new Exception("Cannot persist new "+typeName);
    } // create()


    @LinkAction("/list")
    public TargetDescriptor list(@ActionParameter(PARAMETER_CLASS_NAME) String typeName,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("list() listing instances of type '{}'", typeName);
        if (!authorizationService.isAdminUser(request, response)) {
            return authorizationService.getLoginTarget(request);
        } // if
        Collection<Class<? extends Content>> classes = getMutableBeanFactory().getClasses();
        Class<? extends Content> cls = null;
        // take first one of classes available
        if (!classes.isEmpty()) {
            cls = classes.iterator().next();
        } // if
        // try to take class from provided classes
        if (StringUtils.isNotBlank(typeName)) {
            for (Class<? extends Content> c : classes) {
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
                LOG.error("list() error while sorting", e);
            } // try/catch
        } // if
        response.setContentType(Constants.MIME_TYPE_HTML_UTF8);
        request.setAttribute("editingHandler", this);
        request.setAttribute(Constants.THIS, contents);
        request.setAttribute(Constants.ATTRIBUTE_REQUEST, request);
        request.setAttribute(Constants.ATTRIBUTE_RESPONSE, response);
        request.setAttribute("classes", classes);
        request.setAttribute("canDelete", deleteMethodEnabled);
        request.setAttribute("prefix", Utils.getUriPrefix(request));
        if (cls!=null) {
            Class<? extends Object> designClass = (cls.getName().indexOf('$')<0) ? cls : cls.getSuperclass();
            request.setAttribute("designClass", designClass);
            request.setAttribute("designClassPackage", designClass.getPackage());
        } // if
        return new TargetDescriptor(contents, "tangramEditorList", null);
    } // list()


    @LinkAction("/edit/id_(.*)")
    public TargetDescriptor edit(@LinkPart(1) String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("edit() editing {}", id);
        if (!authorizationService.isAdminUser(request, response)) {
            return authorizationService.getLoginTarget(request);
        } // if
        response.setContentType(Constants.MIME_TYPE_HTML_UTF8);
        Content content = beanFactory.getBean(id);
        if (content==null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "no content with id "+id+" in repository.");
            return null;
        } // if
        if (content instanceof CodeResource) {
            CodeResource code = (CodeResource) content;
            request.setAttribute("compilationErrors", classRepository.getCompilationErrors().get(code.getAnnotation()));
        } // if
        request.setAttribute("editingHandler", this);
        request.setAttribute("beanFactory", getMutableBeanFactory());
        request.setAttribute("propertyConverter", propertyConverter);
        request.setAttribute("classes", getMutableBeanFactory().getClasses());
        request.setAttribute("prefix", Utils.getUriPrefix(request));
        request.setAttribute("cmprefix", Utils.getUriPrefix(request)+"/editor/codemirror");
        request.setAttribute("ckprefix", Utils.getUriPrefix(request)+"/editor/ckeditor");
        Class<? extends Content> cls = content.getClass();
        String note = getOrmNote(cls);
        Class<? extends Object> designClass = getDesignClass(cls);
        request.setAttribute("note", note);
        request.setAttribute("contentClass", cls);
        request.setAttribute("designClass", designClass);
        request.setAttribute("designClassPackage", designClass.getPackage());

        return new TargetDescriptor(content, "edit", null);
    } // edit()


    @LinkAction("/link")
    public TargetDescriptor link(@ActionParameter(PARAMETER_CLASS_NAME) String typeName,
            @ActionParameter(PARAMETER_ID) String id, @ActionParameter(PARAMETER_PROPERTY) String propertyName,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("link() creating new instance of type {}", typeName);
        authorizationService.throwIfNotAdmin(request, response, "Link should not be called directly");
        Class<? extends Content> cls = loadClass(typeName);
        Content content = getMutableBeanFactory().createBean(cls);
        if (getMutableBeanFactory().persist(content)) {
            LOG.debug("link() content={}", content);
            LOG.debug("link() id={}", content.getId());

            // get bean here for update to avoid xg transactions where ever possible
            Content bean = getMutableBeanFactory().getBean(Content.class, id);
            getMutableBeanFactory().beginTransaction();
            JavaBean wrapper = new JavaBean(bean);

            Object listObject = wrapper.get(propertyName);
            List<Content> list = convertList(listObject);
            list.add(content);

            wrapper.set(propertyName, list);
            getMutableBeanFactory().persist(bean);
            return describeTarget(content);
        } else {
            throw new Exception("could not create new instance of type "+typeName);
        } // if
    } // link()


    @LinkAction("/delete/id_(.*)")
    public TargetDescriptor delete(@LinkPart(1) String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.info("delete() trying to delete instance {}", id);
        authorizationService.throwIfNotAdmin(request, response, "Delete should not be called directly");
        if (!deleteMethodEnabled) {
            throw new Exception("Object deletion not activated");
        } // if
        Content bean = getMutableBeanFactory().getBean(Content.class, id);
        if (bean==null) {
            throw new Exception("No object found for deletion of id "+id);
        } // if
        String typeName = bean.getClass().getName();
        getMutableBeanFactory().beginTransaction();
        getMutableBeanFactory().delete(bean);
        return list(typeName, request, response);
    } // delete()


    @LinkAction("/export")
    public TargetDescriptor contentExport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!authorizationService.isAdminUser(request, response)) {
            return authorizationService.getLoginTarget(request);
        } // if
        response.setContentType(Constants.MIME_TYPE_XML);
        response.setCharacterEncoding("UTF-8");
        // The pure reflection provider is used because of Google App Engines API limitations
        XStream xstream = new AppEngineXStream(new StaxDriver());
        Collection<Class<? extends Content>> classes = getMutableBeanFactory().getClasses();

        // Dig out root class of all this evil to find out where the id field is defined
        Class<? extends Object> oneClass = classes.iterator().next();
        while (oneClass.getSuperclass()!=Object.class) {
            oneClass = oneClass.getSuperclass();
        } // while
        LOG.info("contentExport() root class to ignore fields in: {}", oneClass.getName());
        xstream.omitField(oneClass, "id");
        xstream.omitField(oneClass, "ebeanInternalId");
        final Class<? extends Content> baseClass = getMutableBeanFactory().getBaseClass();
        if (baseClass!=oneClass) {
            LOG.info("contentExport() additional base class to ignore fields in: {}", oneClass.getName());
            xstream.omitField(baseClass, "id");
            xstream.omitField(baseClass, "beanFactory");
            xstream.omitField(baseClass, "gaeBeanFactory");
            xstream.omitField(baseClass, "ebeanInternalId");
        } // if

        for (Class<? extends Content> c : classes) {
            LOG.info("contentExport() aliasing and ignoring fields for {}", c.getName());
            xstream.omitField(c, "beanFactory");
            xstream.omitField(c, "gaeBeanFactory");
            xstream.omitField(c, "userServices");
            xstream.alias(c.getSimpleName(), c);
        } // for
        Collection<Content> allContent = new ArrayList<>();
        for (Class<? extends Content> c : classes) {
            try {
                allContent.addAll(beanFactory.listBeansOfExactClass(c));
            } catch (Exception e) {
                LOG.error("contentExport()/list", e);
            } // try/catch
        } // for
        try {
            xstream.toXML(allContent, response.getWriter());
            response.getWriter().flush();
        } catch (IOException e) {
            LOG.error("contentExport()/toxml", e);
        } // try/catch
        return TargetDescriptor.DONE;
    } // contentExport()


    /**
     * Small helper method to heep areas with suppressed warnings small.
     *
     * @param contents
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<Content> convertList(Object contents) {
        return (List<Content>) contents;
    } // convertList()


    private TargetDescriptor doImport(Reader input, HttpServletRequest request, HttpServletResponse response) throws Exception {
        authorizationService.throwIfNotAdmin(request, response, "Import should not be called directly");

        getMutableBeanFactory().beginTransaction();
        XStream xstream = new AppEngineXStream(new StaxDriver());
        Collection<Class<? extends Content>> classes = getMutableBeanFactory().getClasses();
        for (Class<? extends Content> c : classes) {
            xstream.alias(c.getSimpleName(), c);
        } // for

        Object contents = xstream.fromXML(input);
        LOG.info("read() {}", contents);
        if (contents instanceof List) {
            List<? extends Content> list = convertList(contents);
            for (Content o : list) {
                LOG.info("read() {}", o);
                getMutableBeanFactory().persistUncommitted(o);
            } // for
        } // if
        getMutableBeanFactory().commitTransaction();

        return TargetDescriptor.DONE;
    } // doImport()


    @LinkAction("/import-text")
    public TargetDescriptor contentImport(@ActionParameter("xmltext") String xmltext, HttpServletRequest request, HttpServletResponse response) throws Exception {
        authorizationService.throwIfNotAdmin(request, response, "Import should not be called directly");
        return doImport(new StringReader(xmltext), request, response);
    } // contentImport()


    @LinkAction("/import")
    public TargetDescriptor contentImport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        authorizationService.throwIfNotAdmin(request, response, "Import should not be called directly");
        RequestParameterAccess parameterAccess = viewUtilities.createParameterAccess(request);
        return doImport(new StringReader(new String(parameterAccess.getData("xmlfile"), "UTF-8")), request, response);
    } // contentImport()


    @LinkAction("/importer")
    public TargetDescriptor importer(@ActionParameter("xmltext") String xmltext, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!authorizationService.isAdminUser(request, response)) {
            return authorizationService.getLoginTarget(request);
        } // if
        request.setAttribute("editingHandler", this);
        request.setAttribute("classes", getMutableBeanFactory().getClasses());
        response.setContentType(Constants.MIME_TYPE_HTML);
        response.setCharacterEncoding("UTF-8");
        return new TargetDescriptor(this, null, null);
    } // importer()


    private String getUrl(Object bean, String action, String view) {
        if ("edit".equals(view)) {
            action = view;
        } // if
        if (ID_URL_ACTIONS.contains(action)) {
            return "/"+action+"/id_"+((Content) bean).getId();
        } else {
            return PARAMETER_ACTIONS.contains(action) ? ((bean==this) ? "/"+action : null) : null;
        } // if
    } // getUrl()


    @Override
    public Link createLink(HttpServletRequest request, HttpServletResponse r, Object bean, String action, String view) {
        Link result = null;
        String url = getUrl(bean, action, view);
        if (url!=null) {
            result = new Link();
            result.setUrl(url);
            if ("edit".equals(action)) {
                result.setTarget(EDIT_TARGET);
                String jsOpenWindow = "window.open('"+result.getUrl()+"', '"+EDIT_TARGET
                        +"', 'menubar=no,status=no,toolbar=no,resizable=yes, scrollbars=yes');";
                result.addHandler("onclick", jsOpenWindow);
            } // if
        } // if
        return result;
    } // createLink()


    @PostConstruct
    public void afterPropertiesSet() {
        registry.registerLinkHandler(this);
    } // afterPropertiesSet()

} // EditingHandler
