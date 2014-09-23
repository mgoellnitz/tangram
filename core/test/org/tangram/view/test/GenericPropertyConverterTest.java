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

import org.junit.Assert;
import org.junit.Test;
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
    } // testEditStrings()

    @Test
    public void testStorableObjects() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        Assert.assertEquals("should be an interger value", 123, c.getStorableObject(null, "123", Integer.class, null));
        Assert.assertEquals("should be string  value", "Hallo", c.getStorableObject(null, "Hallo", String.class, null));
    } // testStorableObjects()

    @Test
    public void testTypeChecks() {
        GenericPropertyConverter c = new GenericPropertyConverter();
        Assert.assertTrue("should be recognized as blob tyoe", c.isBlobType(new byte[13].getClass()));
        Assert.assertTrue("should be recognized as text tyoe", c.isTextType(new char[14].getClass()));
    } // testStorableObjects()

} // GenericPropertyConverterTest
