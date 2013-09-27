package org.tangram.mongo;

import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.util.Assert;

public class MongoContentTest {

    @Test
    public void testIsEnhanced() {
        Method[] methods = MongoContent.class.getMethods();
        boolean flag = false;
        for (Method method : methods) {
            System.out.println(""+method.getName());
            if (method.getName().startsWith("jdo")) {
                flag = true;
            } // if
        } // for
        Assert.isTrue(flag, "Classes not enhanced - output unusable");
    } // testIsEnhanced()

} // MongoContentTest
