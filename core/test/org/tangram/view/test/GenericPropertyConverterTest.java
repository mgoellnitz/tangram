/**
 *
 * Copyright 2014 Martin Goellnitz
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
package org.tangram.view.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.tangram.Constants;
import org.tangram.content.Content;
import org.tangram.view.GenericPropertyConverter;


public class GenericPropertyConverterTest {

    @Test
    public void testEditStrings() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        Content content = new Content() {
            public String getId() {
                return "Test:123";
            }


            @Override
            public int compareTo(Content o) {
                return 0;
            }
        };
        Assert.assertEquals("should be a readable true value", "true", c.getEditString(Boolean.TRUE));
        Assert.assertEquals("should be a number value string", "Test:123", c.getEditString(content));
        List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("test");
        Assert.assertEquals("should be a list value", "hello,test,", c.getEditString(list));
        // CI Server might use different locale - and this should be supported by the framework.
        DateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT, Locale.getDefault());
        Date referenceDate = new Date(1404215130000L);
        String referenceDateString = format.format(referenceDate);
        Assert.assertEquals("should be a date value", referenceDateString, c.getEditString(referenceDate));
        List<Content> contentList = new ArrayList<>();
        contentList.add(content);
        Assert.assertEquals("should be a list with just one content ID", "Test:123, ", c.getEditString(contentList));
    } // testEditStrings()


    @Test
    public void testStorableObjects() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        Assert.assertEquals("should be an interger value", 123, c.getStorableObject(null, "123", Integer.class, null));
        Assert.assertEquals("should be a float value", (float) 123.456, c.getStorableObject(null, "123.456", Float.class, null));
        Assert.assertEquals("should be a boolean value", false, c.getStorableObject(null, "false", Boolean.class, null));
        Assert.assertEquals("should be a boolean value", true, c.getStorableObject(null, "true", Boolean.class, null));
        Assert.assertEquals("should be a boolean value", false, c.getStorableObject(null, "error", Boolean.class, null));
        Assert.assertEquals("should be a date value", new Date(1404215130000L), c.getStorableObject(null, "11:45:30 01.07.2014 GMT", Date.class, null));
        Assert.assertEquals("should be string  value", "Hallo", c.getStorableObject(null, "Hallo", String.class, null));
    } // testStorableObjects()


    @Test
    public void testTypeChecks() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        Assert.assertTrue("should be recognized as blob type", c.isBlobType(byte[].class));
        Assert.assertTrue("should be recognized as text type", c.isTextType(char[].class));
    } // testStorableObjects()

} // GenericPropertyConverterTest
