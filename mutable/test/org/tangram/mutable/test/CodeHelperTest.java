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
package org.tangram.mutable.test;

import org.tangram.mutable.CodeHelper;
import org.testng.Assert;
import org.testng.annotations.Test;


public class CodeHelperTest {

    @Test
    public void testCodeHelper() {
        Assert.assertEquals(CodeHelper.getCodeMimeTypes().size(), 6, "Unexpected number of special mime types for codes");
        Assert.assertEquals(CodeHelper.getExtension("text/html"), ".vtl", "Extension for velocity templates is vtl");
        Assert.assertEquals(CodeHelper.getExtension("text/x-markdown"), ".md", "Extension for markdown templates is md");
        Assert.assertEquals(CodeHelper.getExtension("application/xml"), ".vtl", "Extension for velocity templates is vtl");
        Assert.assertEquals(CodeHelper.getExtension("text/css"), ".css", "Extension for css files is css");
        Assert.assertEquals(CodeHelper.getExtension("application/javascript"), ".js", "Extension for JavaScript files is js");
        Assert.assertEquals(CodeHelper.getExtension("application/x-groovy"), ".groovy", "Folder for groovy code is groovy");
        Assert.assertEquals(CodeHelper.getExtension("text/plain"), "", "No extension for plain text files");
        Assert.assertEquals(CodeHelper.getFolder("text/html"), "velocity", "Folder for html templates is velocity");
        Assert.assertEquals(CodeHelper.getFolder("text/x-markdown"), "markdown", "Folder for markdown files is md");
        Assert.assertEquals(CodeHelper.getFolder("application/xml"), "velocity-xml", "Folder for xml templates is velocity-xml");
        Assert.assertEquals(CodeHelper.getFolder("text/css"), "css", "Folder for css files is css");
        Assert.assertEquals(CodeHelper.getFolder("application/javascript"), "js", "Folder for JavaScript files is js");
        Assert.assertEquals(CodeHelper.getFolder("application/x-groovy"), "groovy", "Folder for groovy code is groovy");
        Assert.assertEquals(CodeHelper.getMimetype("velocity"), "text/html", "MimeType for folder velocity is text/html");
        Assert.assertEquals(CodeHelper.getMimetype("markdown"), "text/x-markdown", "MimeType for folder markdown is text/x-markdown");
        Assert.assertEquals(CodeHelper.getMimetype("velocity-xml"), "application/xml", "MimeType for folder velocity-xml is application/xml");
        Assert.assertEquals(CodeHelper.getMimetype("css"), "text/css", "MimeType for folder css is text/css");
        Assert.assertEquals(CodeHelper.getMimetype("js"), "application/javascript", "MimeType for folder js is application/javascript");
        Assert.assertEquals(CodeHelper.getMimetype("groovy"), "application/x-groovy", "MimeType for folder groovy is application/x-groovy");
        Assert.assertEquals(CodeHelper.getAnnotation("screen.css.new"), "screen", "Annotation for CSS file is its filename without extension");
        Assert.assertEquals(CodeHelper.getAnnotation("script.js"), "script", "Annotation for JavaScrit file is its filename without extension");
        Assert.assertEquals(CodeHelper.getAnnotation("org.tangram.example.TestClass.groovy.old"), "org.tangram.example.TestClass", "Annotation for groovy code is its filename without extension");
        Assert.assertEquals(CodeHelper.getAnnotation("org.tangram.example.TestClass.vtl"), "org.tangram.example.TestClass", "Annotation for velocity template code is its filename without extension");
        Assert.assertEquals(CodeHelper.getAnnotation("org.tangram.example.TestClass.md"), "org.tangram.example.TestClass", "Annotation for markdown template code is its filename without extension");
    } // testJavaBean()

} // CodeHelperTest
