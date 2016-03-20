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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tangram.PersistentRestartCache;
import org.tangram.components.GenericCodeResourceCache;
import org.tangram.content.BeanFactory;
import org.tangram.content.BeanListener;
import org.tangram.util.DummyRestartCache;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test code resource cache behaviour.
 */
public class GenericCodeResourceCacheTest {

    @Mock
    private BeanFactory factory = null;

    @Mock
    private PersistentRestartCache restartCache = new DummyRestartCache();

    @InjectMocks
    private GenericCodeResourceCache codeResourceCache = new GenericCodeResourceCache();


    private class TestBeanLister implements BeanListener {

        private boolean result = false;


        @Override
        public void reset() {
            result = true;
        }


        public boolean isResult() {
            return result;
        }

    }


    @Test
    public void testGenericCodeResourceCache() {
        MockitoAnnotations.initMocks(this);
        TestBeanLister beanListener = new TestBeanLister();
        codeResourceCache.addListener(beanListener);
        codeResourceCache.reset();
        Assert.assertTrue(beanListener.isResult(), "Bean listener should be called.");
        codeResourceCache.afterPropertiesSet();
        boolean isInInterval = (System.currentTimeMillis() - codeResourceCache.getLastUpdate()) < 20;
        Assert.assertTrue(isInInterval, "Modification time of cache should be initialized.");
    } // testGenericCodeResourceCache()

} // GenericCodeResourceCacheTest
