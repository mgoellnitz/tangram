/**
 *
 * Copyright 2014 Martin Goellnitz
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

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.tangram.components.SimpleStatistics;


public class SimpleStatisticsTest {
    
    @Test
    public void testSimpleStatistics() {
        SimpleStatistics s = new SimpleStatistics();
        s.increase("test");
        s.increase("test");
        s.avg("avg", 2);
        s.avg("avg", 4);
        s.avg("avg", 6);
        Map<String, Long> counter = s.getCounter();
        Assert.assertEquals("we tried to count two events", 2, (long)counter.get("test"));
        Assert.assertEquals("average value should be 4", 4, (long)counter.get("avg"));
    } // testSimpleStatistics()

} // SimpleStatisticsTest
