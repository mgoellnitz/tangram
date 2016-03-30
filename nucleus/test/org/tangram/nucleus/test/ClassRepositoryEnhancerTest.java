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
package org.tangram.nucleus.test;

import java.io.FileNotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.tangram.components.nucleus.ClassRepositoryEnhancer;
import org.tangram.components.test.GroovyClassRepositoryTest;
import org.tangram.jdo.JdoBeanFactory;
import org.tangram.logic.ClassRepository;
import org.tangram.mutable.test.BaseContentTest;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test the enhancing of classes from the runtime repository.
 */
public class ClassRepositoryEnhancerTest {

    @Mock
    private JdoBeanFactory beanFactory = Mockito.mock(JdoBeanFactory.class); // NOPMD - this field is not really unused

    @Spy
    private ClassRepository classRepository; // NOPMD - this field is not really unused

    @InjectMocks
    private final ClassRepositoryEnhancer enhancer = new ClassRepositoryEnhancer();


    public ClassRepositoryEnhancerTest() throws FileNotFoundException {
        classRepository = new GroovyClassRepositoryTest("/jdo-enhancer-content.xml").getInstance();
        MockitoAnnotations.initMocks(this);
        enhancer.afterPropertiesSet();
    } // ()


    @Test
    public void testClassRepositoryEnhancer() {
        Class<? extends Object> cls = classRepository.get("org.tangram.nucleus.test.NewsArticle");
        Assert.assertNotNull(cls, "There should be some class representation.");
        Assert.assertEquals(cls.getName(), "org.tangram.nucleus.test.NewsArticle", "Unexpected class name.");
        Assert.assertTrue(BaseContentTest.checkMethodPrefixOccurs(cls.getMethods(), "dn"), "Classes were not enhanced.");
    } // testClassRepositoryEnhancer()

} // ClassRepositoryEnhancerTest
