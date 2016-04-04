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

import java.io.FileNotFoundException;
import java.util.Map;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tangram.PersistentRestartCache;
import org.tangram.components.GenericCodeResourceCache;
import org.tangram.content.BeanFactory;
import org.tangram.content.CodeResource;
import org.tangram.content.CodeResourceCache;
import org.tangram.content.TransientCode;
import org.tangram.mock.content.MockBeanFactory;
import org.tangram.util.DummyRestartCache;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test code resource cache behaviour.
 */
public class GenericCodeResourceCacheTest {

    private static final Logger LOG = LoggerFactory.getLogger(GenericCodeResourceCacheTest.class);

    @Spy
    private final PersistentRestartCache restartCache = new DummyRestartCache(); // NOPMD - this field is not really unused

    @Spy
    private BeanFactory factory;

    @InjectMocks
    private final GenericCodeResourceCache codeResourceCache = new GenericCodeResourceCache();


    public GenericCodeResourceCacheTest(String contentResource) throws FileNotFoundException {
        factory = MockBeanFactory.getFactoryInstance(contentResource);
        MockitoAnnotations.initMocks(this);
        codeResourceCache.afterPropertiesSet();
    } // ()


    public GenericCodeResourceCacheTest() throws FileNotFoundException {
        this(null);
    } // ()


    /**
     * Return the instance in test with every mock needed for other tests to include a working instance.
     *
     * @return code resource cache to be used in other tests
     */
    public CodeResourceCache getInstance() {
        return codeResourceCache;
    } // getFactoryInstance()


    @Test
    public void testInit() {
        long difference = System.currentTimeMillis()-codeResourceCache.getLastUpdate();
        LOG.debug("testInit() difference={}", difference);
        boolean isInInterval = difference<30000;
        Assert.assertTrue(isInInterval, "Modification time of cache should be initialized.");
    } // testInit()


    @Test
    public void testListenerHandling() {
        MockBeanListener beanListener = new MockBeanListener();
        Assert.assertFalse(beanListener.isResult(), "Bean listener should not have been called.");
        codeResourceCache.addListener(beanListener);
        Assert.assertTrue(beanListener.isResult(), "Bean listener should be called.");
        beanListener.rewind();
        Assert.assertFalse(beanListener.isResult(), "Bean listener should not have been called.");
        codeResourceCache.reset();
        Assert.assertTrue(beanListener.isResult(), "Bean listener should be called.");
    } // testListenerHandling()


    @Test
    public void testGenericCodeResourceCache() {
        TransientCode referenceCode = factory.getBean(TransientCode.class, "CodeResource:42");
        Assert.assertEquals(codeResourceCache.get("CodeResource:42"), referenceCode, "Expected to find reference code.");
        Map<String, CodeResource> typeCache = codeResourceCache.getTypeCache("application/x-groovy");
        Assert.assertNotNull(typeCache, "The should be some groovy codes.");
        Assert.assertEquals(typeCache.size(), 5, "The should be some groovy codes.");
    } // testGenericCodeResourceCache()

} // GenericCodeResourceCacheTest
