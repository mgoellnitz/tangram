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

import org.tangram.content.CodeHelper;
import org.testng.Assert;
import org.testng.annotations.Test;


public class CodeHelperTest {

    @Test
    public void testCodeHelper() {
        Assert.assertEquals("velocity", CodeHelper.getFolder("text/html"), "Folder for html templates is velocity");
        Assert.assertEquals("velocity-xml", CodeHelper.getFolder("application/xml"), "Folder for xml templates is velocity-xml");
        Assert.assertEquals("css", CodeHelper.getFolder("text/css"), "Folder for css files is css");
        Assert.assertEquals("js", CodeHelper.getFolder("application/javascript"), "Folder for JavaScript files is js");
        Assert.assertEquals("groovy", CodeHelper.getFolder("application/x-groovy"), "Folder for groovy code is groovy");
        Assert.assertEquals(".vtl", CodeHelper.getExtension("text/html"), "Extension for velocity templates is vtl");
        Assert.assertEquals(".vtl", CodeHelper.getExtension("application/xml"), "Extension for velocity templates is vtl");
        Assert.assertEquals(".css", CodeHelper.getExtension("text/css"), "Extension for css files is css");
        Assert.assertEquals(".js", CodeHelper.getExtension("application/javascript"), "Extension for JavaScript files is js");
        Assert.assertEquals(".groovy", CodeHelper.getExtension("application/x-groovy"), "Folder for groovy code is groovy");
        Assert.assertEquals("text/css", CodeHelper.getMimetype("css"), "MimeType for folder css is text/css");
        Assert.assertEquals("screen", CodeHelper.getAnnotation("screen.css.new"), "Annotation for css file is its filename without extension");
        Assert.assertEquals("org.tangram.example.TestClass", CodeHelper.getAnnotation("org.tangram.example.TestClass.groovy.old"), "Annotation for groovy code is its filename without extension");
    } // testJavaBean()

} // CodeHelperTest
