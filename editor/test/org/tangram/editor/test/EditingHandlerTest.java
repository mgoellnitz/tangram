/**
 *
 * Copyright 2014-2016 Martin Goellnitz
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
package org.tangram.editor.test;

import org.tangram.components.editor.EditingHandler;
import org.tangram.content.Content;
import org.tangram.link.Link;
import org.testng.Assert;
import org.testng.annotations.Test;


public class EditingHandlerTest {

    private static final String DUMMY_ID = "pling:plong";


    /**
     * Mock the name pattern of generated ORM classes.
     */
    private class Content$Test implements Content {

        @Override
        public String getId() {
            return "";
        }


        @Override
        public int compareTo(Content o) {
            return 0;
        }

    }


    @Test
    public void testUrlGeneration() {
        Content c = new Content() {

            @Override
            public String getId() {
                return DUMMY_ID;
            }


            @Override
            public int compareTo(Content o) {
                return 0;
            }

        };

        EditingHandler handler = new EditingHandler();
        Assert.assertEquals(EditingHandler.getDesignClass(Content.class), Content.class, "Content is no generated class.");
        Assert.assertEquals(EditingHandler.getDesignClass(Content$Test.class), Object.class, "Simulated ORM class should issue design class.");

        for (String a : EditingHandler.PARAMETER_ACTIONS) {
            Link link = handler.createLink(null, null, handler, a, null);
            Assert.assertNotNull(link, "The generation of "+a+" action link failed.");
            Assert.assertEquals(link.getUrl(), "/"+a, "The generation of "+a+" action link with strange result.");
        } // for

        for (String a : EditingHandler.ID_URL_ACTIONS) {
            Link link = handler.createLink(null, null, c, a, null);
            Assert.assertNotNull(link, "The generation of "+a+" action link failed.");
            Assert.assertEquals(link.getUrl(), "/"+a+"/id_"+DUMMY_ID, "The generation of "+a+" action link with strange result.");
        } // for
    } // testUrlGeneration()

} // EditingHandlerTest
