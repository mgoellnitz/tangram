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
package org.tangram.util.test;

import java.beans.IntrospectionException;
import org.tangram.util.JavaBean;
import org.testng.Assert;
import org.testng.annotations.Test;


public class JavaBeanTest {

    public static final String PROPERTY_NAME = "value";

    public static final String STRING_VALUE = "Hallo";


    private class TestBean {

        private String value;


        public String getValue() {
            return value;
        }


        public void setValue(String value) {
            this.value = value;
        }


        public String getStuff() {
            return "stuff";
        }

    } // TestBean


    @Test
    public void testJavaBean() {
        JavaBean jb = null;
        try {
            jb = new JavaBean(new TestBean());
        } catch (IntrospectionException ie) {
            Assert.fail("Introspection failed.");
        } // try/catch
        Assert.assertNotNull(jb, "JavaBean should be instanciatable");
        Assert.assertNull(jb.get(PROPERTY_NAME), "property value should be null");
        Assert.assertTrue(jb.propertyNames().contains(PROPERTY_NAME), "property 'value' should be available");
        Assert.assertTrue(jb.isReadable(PROPERTY_NAME), "property 'value' should be readable");
        Assert.assertTrue(jb.isWritable(PROPERTY_NAME), "property 'value' should be writable");
        Assert.assertTrue(jb.isReadable("stuff"), "property 'stuff' should be readable");
        Assert.assertFalse(jb.isWritable("stuff"), "property 'stuff' should not be writable");
        Assert.assertFalse(jb.isReadable("test"), "Unavailable property value should not be readable");
        jb.set(PROPERTY_NAME, STRING_VALUE);
        Assert.assertEquals(jb.getType(PROPERTY_NAME), String.class, "Property 'value' should be of type String");
        // TODO: exceptions occur
        // Assert.assertEquals("Property value should meet the value set previously", STRING_VALUE, jb.get(PROPERTY_NAME));
    } // testJavaBean()

} // JavaBeanTest
