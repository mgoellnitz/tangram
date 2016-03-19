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
package org.tangram.util.test;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;
import org.tangram.Constants;
import org.tangram.link.LinkFactoryAggregator;
import org.tangram.monitor.Statistics;
import org.tangram.util.ClassResolver;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test deliberately copied from dinistiq where we have the same issues.
 */
public class ClassResolverTest {

    @Test
    public void testClassLoader() {
        Set<String> packages = new HashSet<>();
        packages.add(Constants.class.getPackage().getName());
        ClassResolver resolver = new ClassResolver(packages);
        Set<Class<Statistics>> subclasses = resolver.getSubclasses(Statistics.class);
        Assert.assertEquals(subclasses.size(), 2, "Cannot find expected number of implementing classes.");
        Set<Class<LinkFactoryAggregator>> annotatedSubclasses = resolver.getAnnotatedSubclasses(LinkFactoryAggregator.class, Singleton.class);
        Assert.assertEquals(annotatedSubclasses.size(), 1, "Cannot find expected number of implementing classes annotated as singleton.");
    } // testClassLoader()

} // ClassResolverTest
