package org.tangram.gae.test;

import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;
import org.tangram.gae.protection.PasswordProtection;

public class GaeContentTest {

    @Test
    public void testIsEnhanced() {
        Method[] methods = PasswordProtection.class.getMethods();
        boolean flag = false;
        for (Method method : methods) {
            System.out.println(""+method.getName());
            if (method.getName().startsWith("jdo")) {
                flag = true;
            } // if
        } // for
        Assert.assertTrue("Classes not enhanced - output unusable", flag);
    } // testIsEnhanced()

} // GaeContentTest
