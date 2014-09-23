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
package org.tangram.content.test;

import org.junit.Assert;
import org.junit.Test;
import org.tangram.content.CodeHelper;


public class CodeHelperTest {

    @Test
    public void testCodeHelper() {
        Assert.assertEquals("Folder for html templates is velocity", "velocity", CodeHelper.getFolder("text/html"));
        Assert.assertEquals("Folder for css files is css", "css", CodeHelper.getFolder("text/css"));
        Assert.assertEquals("Folder for JavaScript files is js", "js", CodeHelper.getFolder("text/javascript"));
        Assert.assertEquals("Extension for velocity templates is vtl", ".vtl", CodeHelper.getExtension("text/html"));
        Assert.assertEquals("MimeType for folder css is text/css", "text/css", CodeHelper.getMimetype("css"));
        Assert.assertEquals("Annotation for css file is its filename without extension", "screen", CodeHelper.getAnnotation("screen.css"));
    } // testJavaBean()

} // CodeHelperTest
