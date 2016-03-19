/**
 *
 * Copyright 2014-2016 Martin Goellnitz
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
import org.tangram.content.Markdown;
import org.tangram.view.GenericPropertyConverter;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * This test also includes the abstract property converter.
 */
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
        Assert.assertEquals(c.getEditString(null), "", "Null values should result in an empty string.");
        List<String> list = new ArrayList<>();
        list.add("hello");
        list.add("test");
        Assert.assertEquals(c.getEditString(list), "hello,test,", "This should be a list value.");
        List<Content> contentList = new ArrayList<>();
        contentList.add(content);
        Assert.assertEquals(c.getEditString(contentList), "Test:123, ", "This should be a list with just one content ID.");
        Assert.assertEquals(c.getEditString(Boolean.TRUE), "true", "This should be a readable true value.");
        Assert.assertEquals(c.getEditString(content), "Test:123", "This should be a number value string.");
        // CI Server might use different locale - and this should be supported by the framework.
        DateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT, Locale.getDefault());
        Date referenceDate = new Date(1404215130000L);
        String referenceDateString = format.format(referenceDate);
        Assert.assertEquals(referenceDateString, c.getEditString(referenceDate), "This should be a date value.");
        Assert.assertEquals(c.getEditString("Hi!"), "Hi!", "Expected a short string value.");
        String caString = "A String as a char array";
        Assert.assertEquals(c.getEditString(caString.toCharArray()), caString, "The edit string for a char[] should be its string.");
    } // testEditStrings()


    @Test
    public void testStorableObjects() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        Assert.assertNull(c.getStorableObject(null, null, Object.class, null, null), "Null values should always return null.");
        Assert.assertEquals(c.getStorableObject(null, "Hallo", String.class, null, null), "Hallo", "This should be a string value.");
        Assert.assertNull(c.getStorableObject(null, "", String.class, null, null), "Empty values should return null.");
        Assert.assertNull(c.getStorableObject(null, " ", String.class, null, null), "Empty strings should return null.");
        Assert.assertNull(c.getStorableObject(null, "\t", String.class, null, null), "Empty strings should return null.");
        final Date exptectedDate = new Date(1404215130000L);
        Assert.assertEquals(c.getStorableObject(null, "11:45:30 01.07.2014 GMT", Date.class, null), exptectedDate, "This should be a date value.");
        Assert.assertNull(c.getStorableObject(null, "11:45:30 01.12.2014 GxT", Date.class, null), "Wrong date format should result in a null value.");
        Assert.assertEquals(c.getStorableObject(null, "Hallo", char[].class, null, null), "Hallo".toCharArray(), "This should be a char[] value.");
        Assert.assertEquals(c.getStorableObject(null, "123", Integer.class, null, null), 123, "This should be an integer value.");
        Assert.assertNull(c.getStorableObject(null, "", Integer.class, null, null), "Empty values should return null.");
        Assert.assertEquals(c.getStorableObject(null, "123", Long.class, null, null), 123L, "This should be a long value.");
        Assert.assertNull(c.getStorableObject(null, "", Long.class, null, null), "Empty values should return null.");
        Assert.assertEquals(c.getStorableObject(null, "123.456", Float.class, null, null), (float) 123.456, "This should be a float value.");
        Assert.assertNull(c.getStorableObject(null, "", Float.class, null, null), "Empty values should return null.");
        Assert.assertEquals(c.getStorableObject(null, "false", Boolean.class, null, null), false, "This should be a boolean value.");
        Assert.assertEquals(c.getStorableObject(null, "true", Boolean.class, null, null), true, "This should be a boolean value.");
        Assert.assertEquals(c.getStorableObject(null, "error", Boolean.class, null, null), false, "This should be a boolean value.");
        Markdown expectedMarkdown = new Markdown("Hallo".toCharArray());
        Assert.assertEquals(c.getStorableObject(null, "Hallo", Markdown.class, null, null).getClass(), Markdown.class, "This should be a markdown instance.");
        Assert.assertEquals(c.getStorableObject(null, "Hallo", Markdown.class, null, null), expectedMarkdown, "This should be a markdown instance.");
    } // testStorableObjects()


    @Test
    public void testTypeChecks() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        Assert.assertTrue(c.isBlobType(byte[].class), "This should be recognized as blob type.");
        Assert.assertFalse(c.isBlobType(String.class), "This should not be recognized as blob type.");
        Assert.assertTrue(c.isTextType(char[].class), "This should be recognized as text type.");
        Assert.assertFalse(c.isTextType(String.class), "This should not be recognized as text type.");
    } // testTypeChecks()()


    @Test
    public void testConversions() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        byte[] blob = new byte[123];
        Assert.assertEquals(c.createBlob(blob), blob, "Standard blobs should be their byte[] representation.");
        Assert.assertEquals(c.getBlobLength(blob), 123, "Unexpected blob length discovered.");
        Assert.assertEquals(c.getBlobLength(""), 0, "Unexpected blob length discovered.");
    } // testConversions()

} // GenericPropertyConverterTest
