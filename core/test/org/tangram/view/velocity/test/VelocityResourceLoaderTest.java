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
package org.tangram.view.velocity.test;

import org.apache.velocity.runtime.resource.Resource;
import org.mockito.Mockito;
import org.tangram.content.CodeResourceCache;
import org.tangram.content.TransientCode;
import org.tangram.view.velocity.VelocityPatchBean;
import org.tangram.view.velocity.VelocityResourceLoader;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test some aspects of the velocity template loading from the code resource cache.
 */
public class VelocityResourceLoaderTest {

    @Test
    public void testVelocityResourceLoader() {
        CodeResourceCache codeCache = Mockito.mock(CodeResourceCache.class);
        TransientCode code = new TransientCode("org.tangram.example.Topic", "text/html", "Code:42", "", 12345);
        Mockito.when(codeCache.get("org.tangram.example.Topic")).thenReturn(code);

        VelocityPatchBean bean = new VelocityPatchBean();
        bean.setCodeResourceCache(codeCache);

        VelocityResourceLoader resourceLoader = new VelocityResourceLoader();
        resourceLoader.init(null);

        boolean resourceExists = resourceLoader.resourceExists("org.tangram.example.Topic.name");
        Assert.assertFalse(resourceExists, "The given resource should not exist.");
        resourceExists = resourceLoader.resourceExists("org.tangram.example.Topic");
        Assert.assertTrue(resourceExists, "The given resource should exist.");

        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.getName()).thenReturn("org.tangram.example.Topic");
        Mockito.when(resource.getLastModified()).thenReturn(1234L);
        boolean sourceModified = resourceLoader.isSourceModified(resource);
        Assert.assertTrue(sourceModified, "The given source should be modified.");

        resource = Mockito.mock(Resource.class);
        Mockito.when(resource.getName()).thenReturn("org.tangram.example.Topic");
        Mockito.when(resource.getLastModified()).thenReturn(12345L);
        sourceModified = resourceLoader.isSourceModified(resource);
        Assert.assertFalse(sourceModified, "The given source should not be modified.");

        Assert.assertNotNull(resourceLoader.getResourceStream("org.tangram.example.Topic"), "Stream should not be null for existing resources.");
    } // testVelocityResourceLoader()

} // VelocityResourceLoaderTest
