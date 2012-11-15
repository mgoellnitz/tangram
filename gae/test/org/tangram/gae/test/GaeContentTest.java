package org.tangram.gae.test;

import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.util.Assert;
import org.tangram.gae.GaeContent;

public class GaeContentTest {

    @Test
    public void testIsEnhanced() {
        Method[] methods = GaeContent.class.getMethods();
        boolean flag = false;
        for (Method method : methods) {
            System.out.println(""+method.getName());
            if (method.getName().startsWith("jdo")) {
                flag = true;
            } // if
        } // for
        Assert.isTrue(flag, "Classes not enhanced - output unusable");
    } // testIsEnhanced()

} // RdbmsContentTest
