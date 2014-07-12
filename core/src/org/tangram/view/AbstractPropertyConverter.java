/**
 *
 * Copyright 2011-2014 Martin Goellnitz
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.servlet.ServletRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.Constants;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;


/**
 * Used to convert human readable values from forms to objects and vice versa.
 *
 * We had the idea of replacing this with spring conversion service but didn't succeed.
 * Now this turned out to be more portable since we don't use spring in all scenarios anymore.
 */
public abstract class AbstractPropertyConverter implements PropertyConverter {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPropertyConverter.class);

    private DateFormat dateFormat = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);

    @Inject
    private BeanFactory beanFactory;

    @Inject
    private ViewUtilities viewUtilties;


    public void setDateFormat(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
    } // setDateFormat()


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
            } else if (o instanceof Boolean) {
                return ""+o;
            } else if (o instanceof Content) {
                return ((Content) o).getId();
            } else if (o instanceof Date) {
                return dateFormat.format(o);
            } else {
                return o.toString();
            } // if
        } catch (Exception e) {
            LOG.error("getEditString() ", e);
            return "error while converting "+o;
        } // try/catch
    } // getEditString()


    private <T extends Content> List<T> getObjectsViaDescription(Class<T> c, String title, ServletRequest request) {
        List<T> result = new ArrayList<T>();

        if (StringUtils.isNotBlank(title)) {
            List<T> beans = beanFactory.listBeans(c);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getObjectsViaDescription("+title+") checking "+beans);
            } // if
            for (T bean : beans) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getObjectsViaDescription() checking "+bean);
                    } // if
                    BufferResponse r = new BufferResponse();
                    viewUtilties.render(r.getWriter(), bean, "description", request, r);
                    String description = r.getContents();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getObjectsViaDescription("+description.indexOf(title)+") "+bean+" has description "+description);
                    } // if
                    if (description.indexOf(title)>=0) {
                        result.add(bean);
                    } // if
                } catch (IOException ioe) {
                    LOG.error("getObjectsViaDescription() ignoring element "+bean, ioe);
                } // try/catch
            } // for
        } // if

        if (LOG.isDebugEnabled()) {
            LOG.debug("getObjectsViaDescription("+title+") result="+result);
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


    /**
     * Create an ID matcher from ID_PATTERN to get ids from input strings
     *
     * @param idString string which might contain a valid content id
     * @return Matcher instance from given string and ID_PATTERN
     */
    private Matcher createIdMatcher(String idString) {
        Pattern p = Pattern.compile(Constants.ID_PATTERN);
        Matcher m = p.matcher(idString);
        return m;
    } // createidMatcher()


    private Content getReferenceValue(Class<? extends Content> cls, ServletRequest request, String valueString) {
        Content value = null;
        Matcher m = createIdMatcher(valueString);
        if (m.find()) {
            valueString = m.group(1);
            if (LOG.isInfoEnabled()) {
                LOG.info("getReferenceValue() pattern match result "+valueString);
            } // if
            value = beanFactory.getBean(valueString);
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("getReferenceValue() we should have checked for selection via description template");
            } // if
            value = getObjectViaDescription(cls, valueString.trim(), request);
            if (value!=null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("getReferenceValue() found a value from description "+value);
                } // if
            } // if
        } // if
        return value;
    } // getReferenceValue()


    public Object getStorableObject(Content customer, String valueString, Class<? extends Object> cls, ServletRequest request) {
        Object value = null;
        if (valueString==null) {
            return null;
        } // if
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStorableObject() required type is "+cls.getName());
        } // if
        if (cls==String.class) {
            value = StringUtils.isNotBlank(valueString) ? ""+valueString : null;
        } else if (cls==Date.class) {
            try {
                value = dateFormat.parseObject(valueString);
            } catch (ParseException pe) {
                LOG.error("getStorableObject() cannot parse as Date: "+valueString);
            } // try/catch
        } else if (cls==Integer.class) {
            value = StringUtils.isNotBlank(valueString) ? Integer.parseInt(valueString) : null;
        } else if (cls==Float.class) {
            value = StringUtils.isNotBlank(valueString) ? Float.parseFloat(valueString) : null;
        } else if (cls==Boolean.class) {
            value = Boolean.parseBoolean(valueString);
        } else if (cls==List.class) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getStorableObject() splitting "+valueString);
            } // if
            String[] idStrings = valueString.split(",");
            List<Object> elements = new ArrayList<Object>();
            for (String idString : idStrings) {
                idString = idString.trim();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getStorableObject() idString="+idString);
                } // if
                if (StringUtils.isNotBlank(idString)) {
                    Object o = null;
                    Matcher m = createIdMatcher(idString);
                    if (m.find()) {
                        idString = m.group(1);
                        if (LOG.isInfoEnabled()) {
                            LOG.info("getStorableObject() pattern match result "+idString);
                        } // if
                        final Content bean = beanFactory.getBean(idString);
                        if ((bean!=null) && ((customer == null) || (!bean.getId().equals(customer.getId())))) {
                            elements.add(bean);
                        } // if
                    } else {
                        List<Content> results = getObjectsViaDescription(Content.class, idString, request);
                        if (results.size()>0) {
                            elements.addAll(results);
                        } // if
                    } // if
                } // if
            } // for
            value = elements;
        } else if (Content.class.isAssignableFrom(cls)) {
            @SuppressWarnings("unchecked")
            Class<? extends Content> cc = (Class<? extends Content>) cls;
            Content referenceValue = getReferenceValue(cc, request, valueString);
            value = (customer != null) && customer.equals(referenceValue) ? null : referenceValue;
        } // if
        return value;
    } // getStorableObject()



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

} // AbstractPropertyConverter
