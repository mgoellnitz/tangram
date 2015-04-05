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
package org.tangram.gae.test;

import java.lang.reflect.Method;
import org.tangram.gae.protection.PasswordProtection;
import org.testng.Assert;
import org.testng.annotations.Test;


public class GaeContentTest {

    @Test
    public void testIsEnhanced() {
        Method[] methods = PasswordProtection.class.getMethods();
        boolean flag = false;
        for (Method method : methods) {
            if (method.getName().startsWith("jdo")) {
                flag = true;
            } // if
        } // for
        Assert.assertTrue(flag, "Classes not enhanced - output unusable");
    } // testIsEnhanced()

} // GaeContentTest
