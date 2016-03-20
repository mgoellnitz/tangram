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

import org.tangram.PersistentRestartCache;
import org.tangram.util.DummyRestartCache;
import org.tangram.util.FileRestartCache;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test the two generic restart cache implementations.
 */
public class PersistentRestartCacheTest {

    private static final String CACHE_FILE_NAME = "build/restart-cache-test.ser";

    private static final String MARKER_RESOURCE_NAME = PersistentRestartCacheTest.class.getPackage().getName().replace('.', '/');


    @Test(priority = 1)
    public void testDummyRestartCache() {
        PersistentRestartCache cache = new DummyRestartCache();
        String value = cache.get("test", String.class);
        Assert.assertNull(value, "Values from dummy cache should be null.");
        cache.put("test", "hallo");
        value = cache.get("test", String.class);
        Assert.assertNull(value, "Values from dummy cache should be null.");
    } // testDummyRestartCache()


    @Test(priority = 2)
    public void testFileRestartCache() {
        FileRestartCache cache = new FileRestartCache();
        cache.setFilename(CACHE_FILE_NAME);
        cache.setMarkerResourceName(MARKER_RESOURCE_NAME);
        cache.afterPropertiesSet();
        String value = cache.get("test", String.class);
        Assert.assertNull(value, "Values from empty cache should be null.");
        cache.put("test", "hallo");
        value = cache.get("test", String.class);
        Assert.assertEquals(value, "hallo", "Value should be retrievable from cache.");
        Long couldBeABoolean;
        Long markerValue = 42L;
        try {
            couldBeABoolean = cache.get("test", Long.class);
        } catch (ClassCastException e) {
            couldBeABoolean = markerValue;
        } // try/catch
        Assert.assertEquals(couldBeABoolean, markerValue, "Using wrong class while reading should result in a ClassCastException.");
    } // testFileRestartCache()


    @Test(priority = 3)
    public void testFileRestartCacheReread() {
        FileRestartCache cache = new FileRestartCache();
        cache.setFilename(CACHE_FILE_NAME);
        cache.setMarkerResourceName(MARKER_RESOURCE_NAME);
        cache.afterPropertiesSet();
        String value = cache.get("test", String.class);
        Assert.assertEquals(value, "hallo", "Value should be retrievable from cache.");
    } // testFileRestartCacheReread()

} // PersistentRestartCacheTest
