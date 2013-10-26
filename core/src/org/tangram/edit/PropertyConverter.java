/**
 *
 * Copyright 2011 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.edit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

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
                List<? extends Object> list = (List<? extends Object>)o;
                for (Object i : list) {
                    if (i instanceof Content) {
                        result = result+((Content)i).getId()+", ";
                    } else {
                        result = result+i+",";
                    } // if
                } // for
                return result;
            } else if (o instanceof Content) {
                return ((Content)o).getId();
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


    public Object getStorableObject(String valueString, Class<? extends Object> cls) {
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
                if (StringUtils.hasText(idString)) {
                    Object o = null;
                    try {
                        o = beanFactory.getBean(idString);
                        if (log.isDebugEnabled()) {
                            log.debug("getStorableObject() o="+o);
                        } // if
                    } catch (Exception e) {
                        if (log.isWarnEnabled()) {
                            log.warn("getStorableObject() taking plain value as list element "+idString);
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
            } // if
            if (StringUtils.hasText(valueString)) {
                value = beanFactory.getBean(valueString);
            } // if
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

} // PropertyConverter
