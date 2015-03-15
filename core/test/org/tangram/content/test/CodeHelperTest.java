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
package org.tangram.content.test;

import org.junit.Assert;
import org.junit.Test;
import org.tangram.content.CodeHelper;


public class CodeHelperTest {

    @Test
    public void testCodeHelper() {
        Assert.assertEquals("Folder for html templates is velocity", "velocity", CodeHelper.getFolder("text/html"));
        Assert.assertEquals("Folder for xml templates is velocity-xml", "velocity-xml", CodeHelper.getFolder("application/xml"));
        Assert.assertEquals("Folder for css files is css", "css", CodeHelper.getFolder("text/css"));
        Assert.assertEquals("Folder for JavaScript files is js", "js", CodeHelper.getFolder("application/javascript"));
        Assert.assertEquals("Folder for groovy code is groovy", "groovy", CodeHelper.getFolder("application/x-groovy"));
        Assert.assertEquals("Extension for velocity templates is vtl", ".vtl", CodeHelper.getExtension("text/html"));
        Assert.assertEquals("Extension for velocity templates is vtl", ".vtl", CodeHelper.getExtension("application/xml"));
        Assert.assertEquals("Extension for css files is css", ".css", CodeHelper.getExtension("text/css"));
        Assert.assertEquals("Extension for JavaScript files is js", ".js", CodeHelper.getExtension("application/javascript"));
        Assert.assertEquals("Folder for groovy code is groovy", ".groovy", CodeHelper.getExtension("application/x-groovy"));
        Assert.assertEquals("MimeType for folder css is text/css", "text/css", CodeHelper.getMimetype("css"));
        Assert.assertEquals("Annotation for css file is its filename without extension", "screen", CodeHelper.getAnnotation("screen.css.new"));
        Assert.assertEquals("Annotation for groovy code is its filename without extension", "org.tangram.example.TestClass", CodeHelper.getAnnotation("org.tangram.example.TestClass.groovy.old"));
    } // testJavaBean()

} // CodeHelperTest
