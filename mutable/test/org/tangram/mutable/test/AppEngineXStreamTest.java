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
package org.tangram.mutable.test;

import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.tangram.content.TransientCode;
import org.tangram.mutable.AppEngineXStream;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Give the app engine related XStream modification a try.
 */
public class AppEngineXStreamTest {

    @Test
    public void testAppEngineXStream() {
        AppEngineXStream appEngineXStream = new AppEngineXStream(new StaxDriver());
        appEngineXStream.alias(TransientCode.class.getSimpleName(), TransientCode.class);
        TransientCode tc = new TransientCode("screen", "text/css", "CodeResource:2", "// empty css", 12345);
        Assert.assertEquals(appEngineXStream.toXML(tc), "<?xml version=\"1.0\" ?><TransientCode><annotation>screen</annotation><mimeType>text/css</mimeType><codeText>// empty css</codeText><modificationTime>12345</modificationTime><id>CodeResource:2</id></TransientCode>", "Unexpected XML serialization occured.");
    } // testAppEngineXStream()

} // AppEngineXStreamTest
