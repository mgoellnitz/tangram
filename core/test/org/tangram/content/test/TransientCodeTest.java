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

import java.io.InputStream;
import org.junit.Assert;
import org.junit.Test;
import org.tangram.content.CodeResource;
import org.tangram.content.Content;
import org.tangram.content.TransientCode;


public class TransientCodeTest {

    @Test
    public void testTransientCode() {
        CodeResource c = new CodeResource() {

            @Override
            public String getAnnotation() {
                return "org.tangram.example.Topic.name";
            }


            @Override
            public String getMimeType() {
                return "text/html";
            }


            @Override
            public InputStream getStream() throws Exception {
                return null;
            }


            @Override
            public long getSize() {
                return getCodeText().length();
            }


            @Override
            public String getCodeText() {
                return "<html></html>";
            }


            @Override
            public String getId() {
                return "CodeResource:12";
            }


            @Override
            public int compareTo(Content o) {
                return 0;
            }

        };
        TransientCode t = new TransientCode(c);
        TransientCode tc = new TransientCode("screen", "text/css", "CodeResource:2", "// empty css");
        Assert.assertEquals("id", "CodeResource:12", t.getId());
        Assert.assertEquals("mime type", "text/html", t.getMimeType());
        Assert.assertEquals("annotation", "org.tangram.example.Topic.name", t.getAnnotation());
        Assert.assertEquals("id", 13, t.getSize());
        Assert.assertEquals("toString for empty css", "screen (text/css)", tc.toString());
    } // testTransientCode()

} // TransientCodeTest
