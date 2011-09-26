package org.tangram.edit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;

public abstract class PropertyConverter {

    private static final Log log = LogFactory.getLog(PropertyConverter.class);

    @Autowired
    private BeanFactory beanFactory;


    @SuppressWarnings("unchecked")
    public String getEditString(Object o) {
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
        } else {
            return o.toString();
        } // if
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
                    Object o = beanFactory.getBean(idString);
                    if (log.isDebugEnabled()) {
                        log.debug("getStorableObject() o="+o);
                    } // if
                    if (o!=null) {
                        elements.add(o);
                    } // if
                } // if
            } // for
            value = elements;
        } else if (Content.class.isAssignableFrom(cls)) {
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
