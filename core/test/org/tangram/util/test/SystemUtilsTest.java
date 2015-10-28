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
import org.tangram.util.SystemUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


public class SystemUtilsTest {

    @Test
    public void testStringUtils() {
        Set<String> stringSet = SystemUtils.stringSetFromParameterString("hallo,this,is,a,test");
        Assert.assertEquals(stringSet.size(), (long) 5, "The set should contain five elements");
        Assert.assertTrue(stringSet.contains("this"), "The set should contain the string 'this'");
        Set<String> emptySet = SystemUtils.stringSetFromParameterString("");
        Assert.assertNotNull(emptySet, "The set should exist despite the empty input");
        Assert.assertTrue(emptySet.isEmpty(), "The set should be empty the empty input");
        Assert.assertNotNull(SystemUtils.stringSetFromParameterString(null), "The set should exist despite the null input");
    } // testStringUtils()


    @Test
    public void testResourceListing() throws Exception {
        Set<String> resourceListing = SystemUtils.getResourceListing("", "properties");
        Assert.assertEquals(resourceListing.size(), 1, "Unexpected number of resources listed");
        Assert.assertEquals(resourceListing.toString(), "[/for-test.properties]", "Unexpected resources listed");
    } // testResourceListing()


    @Test
    public void testHashGeneration() throws Exception {
        String hash = SystemUtils.getSha256Hash("tangram");
        Assert.assertEquals(hash, "35d51bb942eeb1528f19ba81e9db37fd47fc1f5b838d98286e1bc0bbb82b7a71", "Unexpected hash value generated");
    } // testHashGeneration()

} // SystemUtilsTest
