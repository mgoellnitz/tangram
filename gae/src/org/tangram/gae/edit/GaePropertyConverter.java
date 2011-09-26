package org.tangram.gae.edit;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tangram.edit.PropertyConverter;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;

public class GaePropertyConverter extends PropertyConverter {

    private static final Log log = LogFactory.getLog(PropertyConverter.class);


    @Override
    public Object createBlob(byte[] octets) {
        return new Blob(octets);
    } // createBlob()


    @Override
    public long getBlobLength(Object o) {
        long result = 0;
        if (o!=null) {
            if (isBlobType(o.getClass())) {
                result = ((Blob)o).getBytes().length;
            } // if
        } // if
        return result;
    } // getBlobLength()


    @Override
    public boolean isBlobType(Class<?> cls) {
        return Blob.class.equals(cls);
    } // isBlobType()


    @Override
    public boolean isTextType(Class<?> cls) {
        return (Text.class.equals(cls))||(cls==char[].class);
    } // isBlobType()


    @Override
    public String getEditString(Object o) {
        if (o instanceof Text) {
            return StringEscapeUtils.escapeHtml(((Text)o).getValue());
        } else {
            if (o instanceof char[]) {
                return new String((char[])o);
            } else {
                return super.getEditString(o);
            } // if
        } // if
    } // getEditString()


    /**
     * only handle special GAE specific cases like Text and Blob
     */
    @Override
    public Object getStorableObject(String valueString, Class<? extends Object> cls) {
        Object result = super.getStorableObject(valueString, cls);
        if (result==null) {
            if (cls==Blob.class) {
                if (log.isDebugEnabled()) {
                    log.debug("getStorableObject() valueString="+valueString);
                } // if
            } else if (cls==Text.class) {
                result = new Text(valueString);
            } else if (cls==char[].class) {
                return valueString.toCharArray();
            } // if
        } // if
        return result;
    } // getStorableObject()

} // GaePropertyConverter
