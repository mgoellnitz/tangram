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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tangram.components.ProtectionHook;
import org.tangram.content.BeanFactory;
import org.tangram.content.Content;
import org.tangram.link.TargetDescriptor;
import org.tangram.mock.content.MockBeanFactory;
import org.tangram.protection.ProtectedContent;
import org.tangram.protection.Protection;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test protection hook behaviour.
 */
public class ProtectionHookTest {

    @Spy
    private BeanFactory beanFactory; // NOPMD - this field is not really unused

    @InjectMocks
    private final ProtectionHook protectionHook = new ProtectionHook();


    @Test
    public void testProtectionHook() throws Exception {
        beanFactory = MockBeanFactory.getInstance();
        MockitoAnnotations.initMocks(this);
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
                return false;
            }


            @Override
            public boolean needsAuthorization(HttpServletRequest request) {
                return true;
            }


            @Override
            public List<? extends Content> getProtectionPath() {
                throw new UnsupportedOperationException("Not supported yet.");
            }


            @Override
            public String getId() {
                return "Protection:13";
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

        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Map<String, Object> model = new HashMap<>();
        TargetDescriptor target = new TargetDescriptor(pc, null, null);

        boolean intercepted = true;
        try {
            intercepted = protectionHook.intercept(target, model, request, response);
        } catch (Exception e) {
            Assert.fail("There should be no exception handling the interception.", e);
        } // try/catch
        Assert.assertFalse(intercepted, "Call should be intercepted.");
        Assert.assertEquals(model.size(), 2, "Model should contain that many values.");

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        model.clear();
        target = new TargetDescriptor(p, null, null);

        intercepted = true;
        try {
            intercepted = protectionHook.intercept(target, model, request, response);
        } catch (Exception e) {
            Assert.fail("There should be no exception handling the interception.", e);
        } // try/catch
        Assert.assertFalse(intercepted, "Call should be intercepted.");
        Assert.assertEquals(model.size(), 2, "Model should contain that many values.");
    } // testProtectionHook()

} // ProtectionHookTest
