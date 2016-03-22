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
package org.tangram.components.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.tangram.components.ProtectionHook;
import org.tangram.content.Content;
import org.tangram.protection.ProtectedContent;
import org.tangram.protection.Protection;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test protection hook behaviour.
 */
public class ProtectionHookTest {

    @Test
    public void testProtectionHook() {
        ProtectionHook protectionHook = new ProtectionHook();

        Content c = Mockito.mock(Content.class);
        final List<Content> protectionPath = new ArrayList<>(2);
        ProtectedContent pc = new ProtectedContent() {

            @Override
            public List<? extends Content> getProtectionPath() {
                return protectionPath;
            }


            @Override
            public String getId() {
                return "PC:2";
            }


            @Override
            public int compareTo(Content o) {
                return 0;
            }

        };
        protectionPath.add(c);
        protectionPath.add(pc);

        final List<Content> protectedContents = new ArrayList<>(2);
        protectedContents.add(c);
        Protection p = new Protection() {

            @Override
            public String getProtectionKey() {
                return "testkey";
            }


            @Override
            public List<? extends Content> getProtectedContents() {
                return protectedContents;
            }


            @Override
            public String handleLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
                throw new UnsupportedOperationException("Not supported yet.");
            }


            @Override
            public boolean isContentVisible(HttpServletRequest request) throws Exception {
                throw new UnsupportedOperationException("Not supported yet.");
            }


            @Override
            public boolean needsAuthorization(HttpServletRequest request) {
                throw new UnsupportedOperationException("Not supported yet.");
            }


            @Override
            public List<? extends Content> getProtectionPath() {
                throw new UnsupportedOperationException("Not supported yet.");
            }


            @Override
            public String getId() {
                throw new UnsupportedOperationException("Not supported yet.");
            }


            @Override
            public int compareTo(Content o) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };


        boolean protectedBy = protectionHook.isProtectedBy(pc, p);
        Assert.assertTrue(protectedBy, "This content should be protected by the given protection.");

        Map<String, Protection> requiredProtections = protectionHook.getRequiredProtections(p);
        Assert.assertEquals(requiredProtections.size(), 1, "Expected one required protection.");
        Assert.assertEquals(requiredProtections.keySet().iterator().next(), "testkey", "Unexpected protection key.");
        Assert.assertEquals(requiredProtections.get("testkey"), p, "Unexpected protection discovered.");
    } // testProtectionHook()

} // ProtectionHookTest
