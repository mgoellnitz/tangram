package org.tangram.nucleus.test;

import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;
import org.tangram.nucleus.NucleusContent;

public class NucleusContentTest {

    @Test
    public void testIsEnhanced() {
        Method[] methods = NucleusContent.class.getMethods();
        boolean flag = false;
        for (Method method : methods) {
            System.out.println(""+method.getName());
            if (method.getName().startsWith("jdo")) {
                flag = true;
            } // if
        } // for
        Assert.assertTrue("Classes not enhanced - output unusable", flag);
    } // testIsEnhanced()

} // NucleusContentTest
