/**
 *
 * Copyright 2014-2015 Martin Goellnitz
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
import org.tangram.Constants;
import org.tangram.content.Content;
import org.tangram.view.GenericPropertyConverter;
import org.testng.Assert;
import org.testng.annotations.Test;


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
        Assert.assertEquals(c.getEditString(Boolean.TRUE), "true", "should be a readable true value");
        Assert.assertEquals(c.getEditString(content), "Test:123", "should be a number value string");
        List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("test");
        Assert.assertEquals(c.getEditString(list), "hello,test,", "should be a list value");
        // CI Server might use different locale - and this should be supported by the framework.
        DateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT, Locale.getDefault());
        Date referenceDate = new Date(1404215130000L);
        String referenceDateString = format.format(referenceDate);
        Assert.assertEquals(referenceDateString, c.getEditString(referenceDate), "should be a date value");
        List<Content> contentList = new ArrayList<>();
        contentList.add(content);
        Assert.assertEquals(c.getEditString(contentList), "Test:123, ", "should be a list with just one content ID");
    } // testEditStrings()


    @Test
    public void testStorableObjects() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        Assert.assertEquals(c.getStorableObject(null, "123", Integer.class, null, null), 123, "should be an interger value");
        Assert.assertEquals(c.getStorableObject(null, "123.456", Float.class, null, null), (float) 123.456, "should be a float value");
        Assert.assertEquals(c.getStorableObject(null, "false", Boolean.class, null, null), false, "should be a boolean value");
        Assert.assertEquals(c.getStorableObject(null, "true", Boolean.class, null, null), true, "should be a boolean value");
        Assert.assertEquals(c.getStorableObject(null, "error", Boolean.class, null, null), false, "should be a boolean value");
        Assert.assertEquals(c.getStorableObject(null, "11:45:30 01.07.2014 GMT", Date.class, null, null), new Date(1404215130000L), "should be a date value");
        Assert.assertEquals(c.getStorableObject(null, "Hallo", String.class, null, null), "Hallo", "should be string  value");
    } // testStorableObjects()


    @Test
    public void testTypeChecks() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        Assert.assertTrue(c.isBlobType(byte[].class), "should be recognized as blob type");
        Assert.assertTrue(c.isTextType(char[].class), "should be recognized as text type");
    } // testStorableObjects()

} // GenericPropertyConverterTest
