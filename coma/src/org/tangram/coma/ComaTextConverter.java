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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.tangram.coma;

import java.io.IOException;


/**
 * This is the perfectly most simple and non sufficient implementation
 * It definitely only deals with the cases I have in my newly generated example database.
 */
public final class ComaTextConverter {

    private static final byte[] HEX_VALUES = new byte['g'];

    private final StringBuilder dataBuilder;

    private final StringBuilder textBuilder;

    private final StringBuilder resultBuilder;

    private int dataPosition;

    private int textPosition;

    static {
        HEX_VALUES['0'] = 0;
        HEX_VALUES['1'] = 1;
        HEX_VALUES['2'] = 2;
        HEX_VALUES['3'] = 3;
        HEX_VALUES['4'] = 4;
        HEX_VALUES['5'] = 5;
        HEX_VALUES['6'] = 6;
        HEX_VALUES['7'] = 7;
        HEX_VALUES['8'] = 8;
        HEX_VALUES['9'] = 9;
        HEX_VALUES['A'] = 10;
        HEX_VALUES['B'] = 11;
        HEX_VALUES['C'] = 12;
        HEX_VALUES['D'] = 13;
        HEX_VALUES['E'] = 14;
        HEX_VALUES['F'] = 15;
        HEX_VALUES['a'] = 10;
        HEX_VALUES['b'] = 11;
        HEX_VALUES['c'] = 12;
        HEX_VALUES['d'] = 13;
        HEX_VALUES['e'] = 14;
        HEX_VALUES['f'] = 15;
    }


    private ComaTextConverter(StringBuilder text, StringBuilder data) {
        this.dataBuilder = data;
        this.textBuilder = text;
        dataPosition = 0;
        textPosition = 0;
        this.resultBuilder = new StringBuilder(512);
    } // ComaTextConverter()


    private int readHex(StringBuilder buf, int pos) throws IndexOutOfBoundsException {
        return ((HEX_VALUES[buf.charAt(pos)]<<12)+(HEX_VALUES[buf.charAt(pos+1)]<<8)+(HEX_VALUES[buf.charAt(pos+2)]<<4)+HEX_VALUES[buf
                .charAt(pos+3)]);
    } // readHex()


    private int readStringLength() {
        int result = -1;
        if (dataPosition+3<dataBuilder.length()) {
            result = readHex(dataBuilder, dataPosition);
        } // if
        return result;
    } // readStringLength()


    private String getStringFromData() {
        int length = readStringLength();
        dataPosition += 4;
        String result = null;
        if (length>=0&&dataPosition+length<=dataBuilder.length()) {
            char[] buffer = new char[length];
            if (length>0) {
                dataBuilder.getChars(dataPosition, dataPosition+length, buffer, 0);
            } // if
            result = String.valueOf(buffer);
            dataPosition += length;
        } // if
        return result;
    } // getStringFromData()


    /**
     * reads name and attributes of an element from markupBuffer
     */
    private void issueElementStart() {
        String name = getStringFromData();
        if (name==null) {
            return;
        } // if

        resultBuilder.append('<');
        resultBuilder.append(name);
        while (true) {
            if (dataPosition>=dataBuilder.length()) {
                return;
            } // if
            char flag = dataBuilder.charAt(dataPosition);
            if (flag!='a') {
                break;
            } // if

            dataPosition++ ;
            String attributeName = getStringFromData();
            if (attributeName==null) {
                return;
            } // if

            String attributeValue = getStringFromData();
            if (attributeValue==null) {
                return;
            } // if

            boolean hasValue = (attributeValue.length()>0);
            if (hasValue) {
                attributeValue = attributeValue.substring(0, attributeValue.length()-1);
            } // if
            resultBuilder.append(' ');
            resultBuilder.append(attributeName);
            if (hasValue) {
                resultBuilder.append("=\"");
                resultBuilder.append(attributeValue);
                resultBuilder.append('\"');
            } // if
        } // while - attribute loop
        resultBuilder.append('>');
    } // issueElementStart()


    private void issueElementEnd() {
        String name = getStringFromData();
        if (name!=null) {
            resultBuilder.append("</");
            resultBuilder.append(name);
            resultBuilder.append('>');
        } // if
    } // issueElementEnd()


    /**
     * read plain text from text builder while the length is read from data builder
     */
    private void writeText() {
        int length = readStringLength();
        if (length<0) {
            dataPosition-- ;
            return;
        } // if
        dataPosition += 4;

        int buffersize = 0;
        if (length>0) {
            if (length<=(textBuilder.length()-textPosition)) {
                buffersize = length;
            } else {
                buffersize = (textBuilder.length()-textPosition);
            } // if
            if (buffersize>0) {
                char[] buffer = new char[buffersize];
                textBuilder.getChars(textPosition, textPosition+buffersize, buffer, 0);
                textPosition += buffersize;
                resultBuilder.append(buffer);
            } // if
        } // if
    } // writeText()


    /**
     * starts merging the two separate buffers
     *
     * @throws IOException
     */
    private String mergeBuilders() {
        while (dataPosition<dataBuilder.length()) {
            char flag = dataBuilder.charAt(dataPosition++);
            switch (flag) {
            case '(':
                issueElementStart();
                break;
            case '-':
                writeText();
                break;
            case ')':
                issueElementEnd();
                break;
            default:
                throw new RuntimeException("Unknown code ("+dataPosition+","+flag+")");
            } // switch
        } // while
        return resultBuilder.toString();
    } // mergeBuilders()


    public static String convert(StringBuilder text, StringBuilder data) {
        ComaTextConverter converter = new ComaTextConverter(text, data);
        return converter.mergeBuilders();
    } // convert()

} // ComaTextConverter
