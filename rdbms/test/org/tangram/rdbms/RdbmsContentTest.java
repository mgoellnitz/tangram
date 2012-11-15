package org.tangram.rdbms;

import java.lang.reflect.Method;

import org.junit.Test;

import org.springframework.util.Assert;

public class RdbmsContentTest {

    @Test
    public void testIsEnhanced() {
        Method[] methods = RdbmsContent.class.getMethods();
        boolean flag = false;
        for (Method method : methods) {
            System.out.println(""+method.getName());
            if (method.getName().startsWith("jdo")) {
                flag = true;
            } // if
        } // for
        Assert.isTrue(flag, "Classes not enhanced - output unusable");
    } // testIsEnhanced()

}
