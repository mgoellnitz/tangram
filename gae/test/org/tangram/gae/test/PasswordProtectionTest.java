package org.tangram.gae.test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.tangram.gae.protection.PasswordProtection;


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
        Assert.assertEquals("Unexpected login result", null, loginResult);
        
        boolean isVisible = false;
        try {
            isVisible = pp.isContentVisible(request);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Assert.fail(e.getMessage());
        } // try/catch
        Assert.assertTrue("extected that this content is visible", isVisible);
    } // testPasswordProtection()

} // GaeContentTest
