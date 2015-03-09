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

import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.tangram.util.SystemUtils;


public class SystemUtilsTest {

    @Test
    public void testStringUtils() {
        Set<String> stringSet = SystemUtils.stringSetFromParameterString("hallo,this,is,a,test");
        Assert.assertEquals("The set should contain five elements", stringSet.size(), (long) 5);
        Assert.assertTrue("The set should contain the string 'this'", stringSet.contains("this"));
        Set<String> emptySet = SystemUtils.stringSetFromParameterString("");
        Assert.assertNotNull("The set should exist despite the empty input", emptySet);
        Assert.assertTrue("The set should be empty the empty input", emptySet.isEmpty());
        Assert.assertNotNull("The set should exist despite the null input", SystemUtils.stringSetFromParameterString(null));
    } // testStringUtils()


    @Test
    public void testResourceListing() throws Exception {
        Set<String> resourceListing = SystemUtils.getResourceListing("", "properties");
        Assert.assertEquals("Unexpected number of resources listed", 1, resourceListing.size());
        Assert.assertEquals("Unexpected resources listed", "[/log4j.properties]", resourceListing.toString());
    } // testResourceListing()


    @Test
    public void testHashGeneration() throws Exception {
        String hash = SystemUtils.getSha256Hash("tangram");
        Assert.assertEquals("Unexpected hash value generated", "35d51bb942eeb1528f19ba81e9db37fd47fc1f5b838d98286e1bc0bbb82b7a71", hash);
    } // testHashGeneration()

} // SystemUtilsTest
