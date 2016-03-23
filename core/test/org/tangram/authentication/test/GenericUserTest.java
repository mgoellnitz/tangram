/*
 *
 * Copyright 2016 Martin Goellnitz
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
package org.tangram.authentication.test;

import java.util.HashMap;
import java.util.Map;
import org.tangram.authentication.GenericUser;
import org.tangram.authentication.User;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Tests the few aspects of generic user instances.
 */
public class GenericUserTest {

    @Test
    public void testGenericUser() {
        Map<String,Object> properties = new HashMap<>();
        properties.put("testproperty", "testvalue");
        GenericUser genericUser = new GenericUser("form", "me", properties);
        User user = new User() {

            @Override
            public String getProvider() {
                return null;
            }


            @Override
            public String getId() {
                return "id";
            }


            @Override
            public Object getProperty(String name) {
                return null;
            }

        };

        Assert.assertNull(genericUser, "Users should not be equal to null users.");
        Assert.assertFalse(genericUser.equals(user), "Users should not be equal.");
        Assert.assertTrue(genericUser.equals(genericUser), "Users should be equal to themselves.");
        Assert.assertEquals(genericUser.hashCode(), -677480973, "Check for specific hash code failed.");
        Assert.assertEquals(genericUser.getId(), "form:me", "Unexpected compound user id discovered.");
        Assert.assertEquals(genericUser.getId(), genericUser.toString(), "toString() should return just the id.");
        Assert.assertEquals(genericUser.getProvider(), "form", "Unexpected provider for user discovered.");
        Assert.assertEquals(genericUser.getProperty("testproperty"), "testvalue", "Unexpected property value discovered.");
        Assert.assertNull(genericUser.getProperty("otherproperty"), "Unpextected property value discovered.");
    } // testGenericUser()

} // GenericUserTest
