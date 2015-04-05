/*
 *
 * Copyright 2015 Martin Goellnitz
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
package org.tangram.gae.test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.mockito.Mockito;
import org.tangram.gae.protection.PasswordProtection;
import org.testng.Assert;
import org.testng.annotations.Test;


public class PasswordProtectionTest {

    @Test
    public void testPasswordProtection() {
        PasswordProtection pp = new PasswordProtection();
        String login = "rainer.zufall";
        String password = "random.test";
        String protectionKey = "key.to.be.held.in.the.session";

        pp.setLogin(login);
        pp.setPassword(password);
        pp.setProtectionKey(protectionKey);

        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(session.getAttribute(protectionKey)).thenReturn(login);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getParameter(PasswordProtection.PARAM_LOGIN)).thenReturn(login);
        Mockito.when(request.getParameter(PasswordProtection.PARAM_PASSWORD)).thenReturn(password);
        Mockito.when(request.getSession()).thenReturn(session);

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        String loginResult = pp.handleLogin(request, response);
        Assert.assertNull(loginResult, "Unexpected login result");

        boolean isVisible = false;
        try {
            isVisible = pp.isContentVisible(request);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Assert.fail(e.getMessage());
        } // try/catch
        Assert.assertTrue(isVisible, "extected that this content is visible");
    } // testPasswordProtection()

} // PasswordProtectionTest
