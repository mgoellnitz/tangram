package org.tangram.rdbms;

import org.tangram.edit.PropertyConverter;

public class RdbmsPropertyConverter extends PropertyConverter {

    @Override
    public Object createBlob(byte[] octets) {
        return octets;
    } // createBlob()


    @Override
    public long getBlobLength(Object o) {
        long result = 0;
        if (o instanceof byte[]) {
            result = ((byte[])o).length;
        } // if
        return result;
    } // getBlobLength()


    @Override
    public boolean isBlobType(Class<?> cls) {
        return cls==byte[].class;
    } // isBlobType()


    @Override
    public boolean isTextType(Class<?> cls) {
        return cls==char[].class;
    } // isTextType()


    @Override
    public String getEditString(Object o) {
        if (o instanceof char[]) {
            return new String((char[])o);
        } else {
            return super.getEditString(o);
        } // if
    } // getEditString()


    @Override
    public Object getStorableObject(String valueString, Class<? extends Object> cls) {
        if (cls==char[].class) {
            return valueString.toCharArray();
        } else {
            return super.getStorableObject(valueString, cls);
        } // if
    } // getStorableObject()

} // DatanucleusPropertyConverter
