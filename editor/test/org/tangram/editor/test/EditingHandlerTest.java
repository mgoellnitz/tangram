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
package org.tangram.editor.test;

import org.junit.Assert;
import org.junit.Test;
import org.tangram.components.editor.EditingHandler;
import org.tangram.content.Content;
import org.tangram.link.Link;


public class EditingHandlerTest {

    private final String DUMMY_ID = "pling:plong";


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

        EditingHandler editingHandler = new EditingHandler();

        for (String action : EditingHandler.PARAMETER_ACTIONS) {
            Link link = editingHandler.createLink(null, null, c, action, null);
            Assert.assertNotNull("generation of "+action+" action link failed", link);
            Assert.assertEquals("generation of "+action+" action link with strange result", "/"+action, link.getUrl());
        } // for

        for (String action : EditingHandler.ID_URL_ACTIONS) {
            Link link = editingHandler.createLink(null, null, c, action, null);
            Assert.assertNotNull("generation of "+action+" action link failed", link);
            Assert.assertEquals("generation of "+action+" action link with strange result", "/"+action+"/id_"+DUMMY_ID, link.getUrl());
        } // for
    } // testUrlGeneration()

} // EditingHandlerTest
