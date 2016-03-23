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

import groovy.lang.GroovyClassLoader;
import javax.inject.Singleton;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.tangram.PersistentRestartCache;
import org.tangram.components.GroovyClassRepository;
import org.tangram.content.CodeResourceCache;
import org.tangram.content.test.MockBeanFactory;
import org.tangram.util.DummyRestartCache;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test some aspects of the groovy class repository.
 */
public class GroovyClassRepositoryTest {

    @Spy
    private final PersistentRestartCache restartCache = new DummyRestartCache(); // NOPMD - this field is not really unused

    @Spy
    private final MockBeanFactory factory = new MockBeanFactory();

    @Spy
    private CodeResourceCache codeCache; // NOPMD - this field is not really unused

    @InjectMocks
    private final GroovyClassRepository repository = new GroovyClassRepository();


    @BeforeClass
    public void init() throws Exception {
        GenericCodeResourceCacheTest codeCacheTest = new GenericCodeResourceCacheTest();
        codeCacheTest.init();
        codeCache = codeCacheTest.getInstance();
        MockitoAnnotations.initMocks(this);
        factory.init();
        repository.afterPropertiesSet();
    } // init()


    /**
     * Return the instance in test with every mock needed for other tests to include a working instance.
     *
     * @return class repository to be used in other tests
     */
    public GroovyClassRepository getInstance() {
        return repository;
    } // getInstance()


    @Test
    public void testListenerHandling() {
        MockBeanListener beanListener = new MockBeanListener();
        Assert.assertFalse(beanListener.isResult(), "Bean listener should not have been called.");
        repository.addListener(beanListener);
        Assert.assertTrue(beanListener.isResult(), "Bean listener should be called.");
        beanListener.rewind();
        Assert.assertFalse(beanListener.isResult(), "Bean listener should not have been called.");
        repository.reset();
        Assert.assertTrue(beanListener.isResult(), "Bean listener should be called.");
    } // testListenerHandling()


    @Test
    public void testGroovyClassRepository() {
        Assert.assertEquals(repository.getClassLoader().getClass(), GroovyClassLoader.class, "Must be a groovy class loader.");
        Assert.assertNotNull(repository.getBytes("org.tangram.test.GroovyTest"), "Expected to find byte code for test class.");
        Assert.assertEquals(repository.getBytes("org.tangram.test.GroovyTest").length, 2419, "Expected to find byte code for test class.");
        Assert.assertEquals(repository.get(Object.class).size(), 4, "Expected to find exactly fixed number of test classes.");
        Assert.assertEquals(repository.getAnnotated(Singleton.class).size(), 1, "Expected to find fixed number of annotated test classes.");
        Assert.assertEquals(repository.getCompilationErrors().size(), 1, "We have one intentional error in the test codes.");
    } // testGroovyClassRepository()

} // GroovyClassRepositoryTest
