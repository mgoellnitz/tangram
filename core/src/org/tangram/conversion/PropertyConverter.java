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
package org.tangram.conversion;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.view.BufferResponse;
import org.tangram.view.Utils;
import org.tangram.view.jsp.IncludeTag;


/**
 * Used to manually convert human readable values from forms to objects and vice versa.
 */
public abstract class PropertyConverter {

    private static final Log log = LogFactory.getLog(PropertyConverter.class);

    private static DateFormat editDateFormat = new SimpleDateFormat("hh:mm:ss dd.MM.yyyy zzz");

    @Autowired
    private BeanFactory beanFactory;


    @SuppressWarnings("unchecked")
    public String getEditString(Object o) {
        try {
            if (o==null) {
                return "";
            } // if
            if (o instanceof List) {
                String result = "";
                List<? extends Object> list = (List<? extends Object>) o;
                for (Object i : list) {
                    if (i instanceof Content) {
                        result = result+((Content) i).getId()+", ";
                    } else {
                        result = result+i+",";
                    } // if
                } // for
                return result;
            } else if (o instanceof Content) {
                return ((Content) o).getId();
            } else if (o instanceof Date) {
                return editDateFormat.format(o);
            } else {
                return o.toString();
            } // if
        } catch (Exception e) {
            log.error("getEditString() ", e);
            return "error while converting "+o;
        } // try/catch
    } // getEditString()


    public Object getStorableObject(String valueString, Class<? extends Object> cls, ServletRequest request) {
        Object value = null;
        if (valueString==null) {
            return null;
        } // if
        if (log.isDebugEnabled()) {
            log.debug("getStorableObject() required type is "+cls.getName());
        } // if
        if (cls==String.class) {
            value = StringUtils.hasText(valueString) ? ""+valueString : null;
        } else if (cls==Date.class) {
            try {
                value = editDateFormat.parseObject(valueString);
            } catch (ParseException pe) {
                log.error("getStorableObject() cannot parse as Date: "+valueString);
            } // try/catch
        } else if (cls==Integer.class) {
            value = StringUtils.hasText(valueString) ? Integer.parseInt(valueString) : null;
        } else if (cls==Float.class) {
            value = StringUtils.hasText(valueString) ? Float.parseFloat(valueString) : null;
        } else if (cls==Boolean.class) {
            value = Boolean.parseBoolean(valueString);
        } else if (cls==List.class) {
            if (log.isDebugEnabled()) {
                log.debug("getStorableObject() splitting "+valueString);
            } // if
            String[] idStrings = valueString.split(",");
            List<Object> elements = new ArrayList<Object>();
            for (String idString : idStrings) {
                idString = idString.trim();
                if (log.isDebugEnabled()) {
                    log.debug("getStorableObject() idString="+idString);
                } // if
                // TODO: Check for selection via description template
                if (StringUtils.hasText(idString)) {
                    Object o = null;
                    boolean addSomething = true;
                    try {
                        o = beanFactory.getBean(idString);
                        if (log.isDebugEnabled()) {
                            log.debug("getStorableObject() o="+o);
                        } // if
                    } catch (Exception e) {
                        if (log.isWarnEnabled()) {
                            log.warn("getStorableObject() taking plain value as list element "+idString);
                        } // if
                        List<Content> results = getObjectsViaDescription(Content.class, idString, request);
                        if (results.size()>0) {
                            addSomething = false;
                            elements.addAll(results);
                        } // if
                    } // try/catch
                    elements.add(o==null ? idString : o);
                } // if
            } // for
            value = elements;
        } else if (Content.class.isAssignableFrom(cls)) {
            // Filter out possible single ID from input
            Pattern p = Pattern.compile("([A-Z][a-zA-Z]+:[0-9]+)");
            Matcher m = p.matcher(valueString);
            if (m.find()) {
                valueString = m.group(1);
                if (log.isInfoEnabled()) {
                    log.info("getStorableObject() pattern match result "+valueString);
                } // if
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("getStorableObject() we should have checked for selection via description template");
                } // if
                @SuppressWarnings("unchecked")
                Class<? extends Content> cc = (Class<? extends Content>) cls;
                value = getObjectViaDescription(cc, valueString.trim(), request);
                if (value!=null) {
                    if (log.isInfoEnabled()) {
                        log.info("getStorableObject() found a value from description "+value);
                    } // if
                    valueString = null;
                } // if
            } // if
            if (StringUtils.hasText(valueString)) {
                value = beanFactory.getBean(valueString);
            } // if
        } // if
        return value;
    } // getStorableObject()


    private <T extends Content> List<T> getObjectsViaDescription(Class<T> c, String title, ServletRequest request) {
        List<T> result = new ArrayList<T>();

        if (StringUtils.hasText(title)) {
            List<T> beans = beanFactory.listBeans(c);
            if (log.isDebugEnabled()) {
                log.debug("getObjectsViaDescription("+title+") checking "+beans);
            } // if
            for (T bean : beans) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("getObjectsViaDescription() checking "+bean);
                    } // if
                    BufferResponse r = new BufferResponse();
                    Map<String, Object> model = Utils.getModelAndViewFactory(request).createModel(bean, request, r);
                    IncludeTag.render(r.getWriter(), model, "description");
                    String description = r.getContents();
                    if (log.isDebugEnabled()) {
                        log.debug("getObjectsViaDescription("+description.indexOf(title)+") "+bean+" has description "+description);
                    } // if
                    if (description.indexOf(title)>=0) {
                        result.add(bean);
                    } // if
                } catch (IOException ioe) {
                    log.error("getObjectsViaDescription() ignoring element "+bean, ioe);
                } // try/catch
            } // for
        } // if

        if (log.isDebugEnabled()) {
            log.debug("getObjectsViaDescription("+title+") result="+result);
        } // if
        return result;
    } // getObjectsViaDescription()


    private <T extends Content> T getObjectViaDescription(Class<T> c, String title, ServletRequest request) {
        T result = null;

        List<T> suggestions = getObjectsViaDescription(c, title, request);
        if (suggestions.size()==1) {
            result = suggestions.get(0);
        } // if

        return result;
    } // getObjectViaDescription()


    public abstract boolean isBlobType(Class<?> cls);


    /**
     * if o is of class getBlobClass() it returns the blob size
     *
     * @param o
     * @return blob's size
     */
    public abstract long getBlobLength(Object o);


    public abstract boolean isTextType(Class<?> cls);


    public abstract Object createBlob(byte[] octets);

} // PropertyConverter
