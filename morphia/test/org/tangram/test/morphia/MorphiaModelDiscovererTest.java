/*
 *
 * Copyright 2016-2017 Martin Goellnitz
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
package org.tangram.test.morphia;

import java.io.FileNotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.tangram.components.morphia.MorphiaModelDiscoverer;
import org.tangram.components.test.GroovyClassRepositoryTest;
import org.tangram.logic.ClassRepository;
import org.tangram.morphia.MorphiaBeanFactory;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test the enhancing of classes from the runtime repository.
 */
public class MorphiaModelDiscovererTest {

    @Mock
    private MorphiaBeanFactory beanFactory = Mockito.mock(MorphiaBeanFactory.class); // NOPMD - this field is not really unused

    @Spy
    private ClassRepository classRepository; // NOPMD - this field is not really unused

    @InjectMocks
    private final MorphiaModelDiscoverer discoverer = new MorphiaModelDiscoverer();


    public MorphiaModelDiscovererTest() throws FileNotFoundException {
        classRepository = new GroovyClassRepositoryTest("/morphia-model-content.xml").getInstance();
        MockitoAnnotations.initMocks(this);
        discoverer.afterPropertiesSet();
    } // ()


    @Test
    public void testModelDiscoverer() {
        Assert.assertEquals(classRepository.get().size(), 1, "There should be one class in the repository.");
        Assert.assertEquals(""+classRepository.get(), "[org.tangram.test.morphia.content.NewsArticle]", "Unexpected list of class names.");
        Class<? extends Object> cls = classRepository.get("org.tangram.test.morphia.content.NewsArticle");
        Assert.assertNotNull(cls, "There should be some class representation.");
        Assert.assertEquals(cls.getName(), "org.tangram.test.morphia.content.NewsArticle", "Unexpected class name.");
    } // testModelDiscoverer()

} // ClassRepositoryEnhancerTest
