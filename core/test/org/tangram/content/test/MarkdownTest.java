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
package org.tangram.content.test;

import org.tangram.content.Markdown;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Tests the markdown wrapper class.
 */
public class MarkdownTest {

    @Test
    public void testMarkDown() {
        String source = "#Markdown Test\n\nGreat, isn't it?";
        String html = "<h1>Markdown Test</h1>\n<p>Great, isn't it?</p>\n";
        Markdown md = new Markdown(source.toCharArray());
        Assert.assertEquals(String.valueOf(md.getContent()), source, "Unexpected source returned");
        Assert.assertEquals(String.valueOf(md.getMarkup()), html, "Unexpected markup returned");
    } // testMarkDown()

} // MarkdownTest
