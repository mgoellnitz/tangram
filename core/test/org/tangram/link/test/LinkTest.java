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
package org.tangram.link.test;

import org.tangram.link.Link;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test of link descriptor class.
 */
public class LinkTest {

    @Test
    public void testLink() {
        Link link = new Link("http://www.example.org/");
        link.setTarget("new");
        link.addHandler("onclick", "clock");
        link.addHandler("onload", "save");
        Assert.assertEquals(link.getHandlers().size(), 2, "Unexpected number of handlers discovered.");
        link.addHandler("onremove", "gone");
        Assert.assertEquals(link.getHandlers().size(), 3, "Unexpected number of handlers discovered.");
        link.removeHandler("onremove");
        Assert.assertEquals(link.getHandlers().size(), 2, "Unexpected number of handlers discovered.");
        Assert.assertEquals(link.getTarget(), "new", "Unexpected target found.");
        link.setUrl("http://www.example.com/");
        Assert.assertEquals(link.getUrl(), "http://www.example.com/", "Unexpected url found.");
        // TODO: Doesn't work on travis CI but locally
        // String expected = "http://www.example.com/@new: {onclick=clock, onload=save}";
        // Assert.assertEquals(link.toString(), expected, "Unexpected string representation found.");
    } // testLink()

} // LinkTest
