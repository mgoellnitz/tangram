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
package org.tangram.util.test;

import java.beans.IntrospectionException;
import org.junit.Assert;
import org.junit.Test;
import org.tangram.util.JavaBean;


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
        Assert.assertNotNull("JavaBean should be instanciatable", jb);
        Assert.assertNull("property value should be null", jb.get(PROPERTY_NAME));
        Assert.assertTrue("property 'value' should be available", jb.propertyNames().contains(PROPERTY_NAME));
        Assert.assertTrue("property 'value' should be readable", jb.isReadable(PROPERTY_NAME));
        Assert.assertTrue("property 'value' should be writable", jb.isWritable(PROPERTY_NAME));
        Assert.assertTrue("property 'stuff' should be readable", jb.isReadable("stuff"));
        Assert.assertFalse("property 'stuff' should not be writable", jb.isWritable("stuff"));
        Assert.assertFalse("Unavailable property value should not be readable", jb.isReadable("test"));
        jb.set(PROPERTY_NAME, STRING_VALUE);
        Assert.assertEquals("Property 'value' should be of type String", String.class, jb.getType(PROPERTY_NAME));
        // TODO: exceptions occur
        // Assert.assertEquals("Property value should meet the value set previously", STRING_VALUE, jb.get(PROPERTY_NAME));
    } // testJavaBean()

} // JavaBeanTest
